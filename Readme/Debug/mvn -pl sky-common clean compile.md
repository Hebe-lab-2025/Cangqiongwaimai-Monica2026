对，这句话其实是在点一个**很常见的 Maven 多模块坑**，我帮你展开讲一下、顺便给你排查思路 👇

---

## 这句话到底什么意思？

> 依赖未正确继承：虽然父pom中定义了依赖，但 sky-common 模块可能没有正确继承或引用这个依赖

翻译成人话就是：

> “你以为父 pom 里加了依赖，所有子模块（比如 sky-common）都能自动用，但实际上 **sky-common 并没有拿到这个依赖**，所以代码里用到的类会报红、编译失败。”

---

## 常见 4 个原因（基本就这几个）

### 1️⃣ sky-common 的 pom 根本没正确声明 parent

看 `sky-common/pom.xml` 最上面这块：

```xml
<parent>
    <groupId>com.sky</groupId>
    <artifactId>sky-take-out-Monica</artifactId>
    <version>1.0-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

**问题点：**

* `groupId / artifactId / version` 任意一个写错
* `relativePath` 指向一个不存在的 pom
* 干脆没有 `<parent>` 这块

👉 结果：Maven 就不会把根 pom 当成它的父级，**所有父 pom 里的依赖、插件、属性都不会继承**，相当于一个单独项目。

---

### 2️⃣ 依赖只写在 `<dependencyManagement>`，没写到 `<dependencies>`

在父 pom 里，可能是这样的：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.30</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

⚠️ 注意：

* `dependencyManagement` **只负责“统一版本号”**
* **不会真正把依赖加进 classpath**

如果想所有子模块都直接用到 Lombok，父 pom 要这么写：

```xml
<dependencies>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
        <scope>provided</scope> <!-- 可选 -->
    </dependency>
</dependencies>
```

或者：父 pom 用 `<dependencyManagement>` 管版本，**子模块自己再 `dependencies` 引用一次**，比如 sky-common：

```xml
<dependencies>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
</dependencies>
```

---

### 3️⃣ sky-common 虽然继承了 parent，但你没在这个模块里“用到”依赖

比如：

* 父 pom 里有 `spring-boot-starter-web`
* 但你期望在 `sky-common` 里用的是 MyBatis、Lombok、某个工具包
* 这些依赖根本没在父 pom 里声明，所以继承不到

👉 解决：**要么**把公共依赖放到父 pom 的 `<dependencies>` 里，**要么**在 sky-common 自己加依赖。

---

### 4️⃣ 多模块结构 / IDE 没刷新好

父 pom 里要有：

```xml
<modules>
    <module>sky-pojo</module>
    <module>sky-common</module>
    <module>sky-server</module>
</modules>
```

虽然 **继承不一定要在 `<modules>` 里声明**，但实际项目一般是多模块工程，常见几个小坑：

* IDE 还没 reload Maven，依赖树没刷新
* 之前 groupId 从 `org.example` 改成了 `com.sky`，但某个子模块 pom 里的 parent 还写旧的

👉 这种情况 IDEA 里就会报一堆红，比如你之前看到的：`Cannot resolve symbol 'Employee'` 之类。

---

## 如何验证 sky-common 到底有没有继承到？

### 方法 1：看 IDEA 里的 Maven 依赖树

在 IDEA：

1. 选中 `sky-common` 模块
2. 打开右侧 `Maven` 面板 → 选中 `Dependencies`
3. 看你期望的依赖（比如 lombok、mybatis、spring-boot-starter-xxx）

   * **如果没有**：说明确实没继承到 / 没加

---

### 方法 2：命令行用 `dependency:tree`

在项目根目录或 sky-common 目录执行：

```bash
mvn -pl sky-common dependency:tree
```

* 能在树里看到目标依赖 → 说明继承/引用成功
* 完全没有 → 就是没继承/没依赖

---

### 方法 3：看 effective-pom

```bash
mvn -pl sky-common help:effective-pom > effective-sky-common.xml
```

打开这个生成的 `effective-sky-common.xml`：

* 里面列出了 **最终生效的 parent、依赖、插件**
* 如果里面都找不到你期望的依赖 → 就是“依赖未正确继承”

---

## 实际怎么改，给你一个“小 checklist”

你可以按这个顺序检查 sky-common：

1. **确认 parent 正确**

   ```xml
   <parent>
       <groupId>com.sky</groupId>
       <artifactId>sky-take-out-Monica</artifactId>
       <version>1.0-SNAPSHOT</version>
       <relativePath>../pom.xml</relativePath>
   </parent>
   ```

2. 打开父 pom：

   * 你想共用的依赖，是不是放在 `<dependencies>`，而不是只有 `<dependencyManagement>`？

3. 如果只是某些模块需要某依赖（比如只有 sky-server 用 web）：

   * 就在对应模块（比如 sky-server、sky-common）里单独 `dependencies` 引入

4. 改完之后：

   * IDEA 里右键项目：`Maven` → `Reload project`
   * 或命令行跑一遍 `mvn -pl sky-common clean compile` 看是否还报错

---

太好了，你把父 pom 貼出來了，这下问题就很清楚了 👍

## 1. 先说结论：**现在这个父 pom 只“管版本”，不“给依赖”**

你在父 pom 里用的是：

```xml
<dependencyManagement>
    <dependencies>
        ...
    </dependencies>
</dependencyManagement>
```

⚠️ 这一块的作用是：

> 统一管理所有子模块用到的**版本号**，但**不会自动把依赖加进子模块的 classpath**。

所以：

* sky-common / sky-pojo / sky-server **不会自动拥有这些依赖**
* 子模块如果要用，比如 `lombok`、`mybatis-spring-boot-starter`，**还需要在子模块自己的 `<dependencies>` 里再写一遍**（可以不写 `<version>`，版本会从这里继承）

---

## 2. 正确“继承依赖”的两种写法

### ✅ 方式一：父 pom 只管版本，子模块自己声明依赖（你现在适用）

你现在这种结构是标准的“版本管理”写法，建议这样用👇

父 pom（你现在已经有了）：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>${lombok}</version>
        </dependency>
        <!-- 其它依赖同理 -->
    </dependencies>
</dependencyManagement>
```

然后在 **sky-common 的 pom.xml** 里要明确写：

```xml
<project ...>
    <parent>
        <groupId>com.sky</groupId>
        <artifactId>sky-take-out-Monica</artifactId>
        <version>1.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <modelVersion>4.0.0</modelVersion>
    <artifactId>sky-common</artifactId>

    <dependencies>
        <!-- 想用 lombok，就要显式声明一次 -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>

        <!-- 想用 fastjson，就再声明一次 -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
        </dependency>

        <!-- 其它按需加 -->
    </dependencies>
</project>
```

💡 重点：

* **不用写 `<version>`**，因为版本已经在父 pom 的 `<dependencyManagement>` 里统一好了
* 但 **artifactId / groupId 必须在子模块里再写一遍**，不然不会进依赖树

---

### ✅ 方式二：公共依赖直接写在父 pom 的 `<dependencies>`（所有子模块都能用）

如果你希望某些依赖**所有模块**（sky-common、sky-pojo、sky-server）都默认能用，比如 `lombok`，可以这样重构父 pom：

```xml
<dependencies>
    <!-- 全局公共依赖 -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>${lombok}</version>
    </dependency>
</dependencies>

<dependencyManagement>
    <dependencies>
        <!-- 只用于“管版本”的依赖 -->
        ...
    </dependencies>
</dependencyManagement>
```

这样：

* 所有子模块，只要继承了这个父 pom，**不用再写 lombok 依赖**，就可以直接用 `@Data`、`@Slf4j` 等
* 但像 `mybatis-spring-boot-starter` 这类只在 `sky-server` 用到的依赖，建议只在 sky-server 里自己写，避免所有模块都带着它

---

## 3. 再提醒一个你以前报过的坑：groupId 要统一

你之前有个错误：

> Non-resolvable parent POM for **org.example:sky-pojo:1.0-SNAPSHOT**:
> Could not find artifact **org.example:sky-take-out-Monica:pom:1.0-SNAPSHOT**

而你现在的父 pom 是：

```xml
<groupId>com.sky</groupId>
<artifactId>sky-take-out-Monica</artifactId>
<version>1.0-SNAPSHOT</version>
```

👉 所以子模块的 `<parent>` 一定要改成 **`com.sky`**，而不是 `org.example`，类似这样：

```xml
<parent>
    <groupId>com.sky</groupId>
    <artifactId>sky-take-out-Monica</artifactId>
    <version>1.0-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

否则 Maven 会以为你要找的是另外一个根项目：`org.example:sky-take-out-Monica`，自然找不到。

---

## 4. 给你一个排查 checklist（照着点就好）

1. **打开 sky-common/pom.xml**

   * 确认 `<parent>` 里的 `groupId` 是 `com.sky`
   * `artifactId` 是 `sky-take-out-Monica`
   * `version` 是 `1.0-SNAPSHOT`
   * `relativePath` 指向 `../pom.xml`

2. 在 sky-common 的 `<dependencies>` 里：

   * 把你代码里用到的依赖加进去（比如 lombok、fastjson），只写 `groupId + artifactId` 即可。

3. IDEA 中 `Maven → Reload Project`
   或命令行：

   ```bash
   mvn -pl sky-common clean compile
   ```

   * 如果能正常编译，说明“依赖继承 + 声明”已经生效了。

---

如果你愿意，下一步可以把 **sky-common 的 pom.xml** 也贴出来，我可以直接帮你改成一个“标准版”——顺便帮你决定哪些依赖适合放父 pom，哪些只给 sky-server 用 😄


很好，这个是 `sky-pojo` 的 pom，我帮你直接判断一下哪里“继承对了”，哪里还需要注意 👇

---

## 1. 这个 `sky-pojo` 的继承本身是 **正确的**

```xml
<parent>
    <groupId>com.sky</groupId>
    <artifactId>sky-take-out-Monica</artifactId>
    <version>1.0-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

* 和根 pom 完全对应：`com.sky / sky-take-out-Monica / 1.0-SNAPSHOT` ✅
* `relativePath` 指向 `../pom.xml`，也就是项目根目录的那个父 pom ✅

所以：
👉 **`sky-pojo` 已经成功继承了父 pom 的 `dependencyManagement`、`properties` 等配置**，这一步没问题。

---

## 2. 依赖这几行：哪些用了父 pom，哪些是单独写的？

```xml
<dependencies>
    <!-- ① lombok：版本在父 pom 的 dependencyManagement 里 -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>

    <!-- ② jackson：这里写死了 2.9.2 版本 -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.9.2</version>
    </dependency>

    <!-- ③ knife4j：版本在父 pom 的 dependencyManagement 里 -->
    <dependency>
        <groupId>com.github.xiaoymin</groupId>
        <artifactId>knife4j-spring-boot-starter</artifactId>
    </dependency>

    <!-- ④ swagger-annotations：单独写死版本 -->
    <dependency>
        <groupId>io.swagger</groupId>
        <artifactId>swagger-annotations</artifactId>
        <version>1.5.22</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

### ✅ 继承成功的地方

* `lombok`
* `knife4j-spring-boot-starter`

这两个**没有写 `<version>`**，是从父 pom 的 `<dependencyManagement>` 里继承版本的，完全 OK，说明“依赖继承”这块是生效的。

### ✅ 单独写版本的地方（不算错，只是风格问题）

* `jackson-databind`
* `swagger-annotations`

这两个没有放到父 pom 的 `dependencyManagement`，你在本模块里直接写死版本，也没问题，只是将来如果多个模块都用，就不如统一管理方便。

---

## 3. `groupId` / `version` 再说一句

```xml
<groupId>com.sky</groupId>
<artifactId>sky-pojo</artifactId>
<version>1.0-SNAPSHOT</version>
```

* 在有 `<parent>` 的情况下，子模块 **可以省略 `groupId` 和 `version`**，默认就继承父 pom 的
* 你现在写上也不会报错，只是有点**冗余但安全** ✅

所以这一块不用担心，不会导致“依赖未继承”。

---

## 4. 真正容易出错的点：**模块之间要手动声明依赖**

很多人会误会：

> “我把 sky-pojo、sky-server 都写到 `<modules>` 里了，是兄弟模块，它们就能互相访问。”

其实不是的：

* `<modules>` 只是告诉 Maven：这些是一个多模块工程
* **模块 A 想用模块 B 的类，必须在 A 的 `<dependencies>` 里写上对 B 的依赖**

比如：
你的实体类 `Employee` 在 `sky-pojo` 里，
而 `EmployeeServiceImpl` 在 `sky-server` 里用它。

⚠️ 那么 sky-server 的 pom 里必须加：

```xml
<dependency>
    <groupId>com.sky</groupId>
    <artifactId>sky-pojo</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

否则就会出现你看到的那种：

* `Cannot resolve symbol 'Employee'`
* `Cannot resolve symbol 'EmployeeLoginVO'`

👉 **这不是“父 pom 依赖没继承”，而是“模块之间没建立依赖关系”**。

---

## 5. 小总结（帮你对照一下）

* ✅ `sky-pojo` 当前这个 pom：

  * 继承父 pom ✅
  * 使用父 pom 的 `dependencyManagement` 管版本 ✅
  * 结构上没有问题

* ❌ 如果你在其他模块（比如 `sky-server`）里用到了 `sky-pojo` 里的类：

  * 记得在 `sky-server/pom.xml` 里，显式依赖 `sky-pojo`
  * 同理，如果还有 `sky-common`，`sky-server` 里用到它，也要依赖上

---

如果你愿意，下一步你可以把 **`sky-server` 的 pom.xml + 一个报红的类截图/文件路径** 发给我，我可以直接帮你画一张“模块依赖关系图”，顺便给你一句面试里也能用的解释：

> “多模块 Maven 工程里，父 pom 负责管理版本和公共依赖，但模块之间仍然需要通过 `<dependency>` 显式声明引用关系。”


很好，这两个子模块 pom 一看就知道你已经理顺得差不多了，我帮你「验收 + 点几处注意事项」👇

---

## 1️⃣ sky-server：继承 + 依赖关系是 OK 的

```xml
<parent>
    <groupId>com.sky</groupId>
    <artifactId>sky-take-out-Monica</artifactId>
    <version>1.0-SNAPSHOT</version>
</parent>

<artifactId>sky-server</artifactId>
```

* 父 pom：`com.sky:sky-take-out-Monica:1.0-SNAPSHOT` ✅
* 父 pom 又继承了 `spring-boot-starter-parent` ✅
* 所以 **版本管理链路是没问题的**：
  Spring Boot 父 → 你的根 pom → sky-server

### 依赖部分

```xml
<dependency>
    <groupId>com.sky</groupId>
    <artifactId>sky-common</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>

<dependency>
    <groupId>com.sky</groupId>
    <artifactId>sky-pojo</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

* ✅ 正确地把 **兄弟模块 sky-common / sky-pojo** 加成了依赖
* 因为你的父 pom 版本就是 `1.0-SNAPSHOT`，这里的 version 是对得上的

其它依赖，比如：

* `mybatis-spring-boot-starter`
* `lombok`
* `fastjson`
* `druid-spring-boot-starter`
* `pagehelper-spring-boot-starter`
* `knife4j-spring-boot-starter`
* `aspectjrt / aspectjweaver`
* `jaxb-api`
* `poi / poi-ooxml`

这些**没有写 version**，会自动走父 pom 的：

```xml
<dependencyManagement>
    ...
</dependencyManagement>
```

👉 **说明“依赖继承”完全是生效的**，这里没问题。
剩下像 `spring-boot-starter-*`、`mysql-connector-java` 这类没版本的是走 `spring-boot-starter-parent` 的 BOM，也 OK。

> 小优化（可做可不做）：
> `sky-common` / `sky-pojo` 这两个本地模块的 `<version>` 可以省略，默认继承父 pom 的 `1.0-SNAPSHOT`，就不会以后改版本忘了同步。

---

## 2️⃣ sky-common：结构也没有问题

```xml
<parent>
    <artifactId>sky-take-out-Monica</artifactId>
    <groupId>com.sky</groupId>
    <version>1.0-SNAPSHOT</version>
</parent>

<artifactId>sky-common</artifactId>
```

* 继承的是同一个父 pom ✅
* `spring-boot-configuration-processor`、`lombok`、`jaxb-api` 这些依赖也都能从父 pom / Spring Boot 父里继承版本 ✅

所以：
👉 sky-common 现在也是一个“继承正常 + 依赖正常”的子模块。

---

## 3️⃣ 那之前那句「依赖未正确继承」现在是不是还成立？

**现在这些 pom 看起来，继承已经是正确的了**：

* 父 pom 用 `<dependencyManagement>` 管版本 ✅
* 子模块在 `<dependencies>` 里显式声明需要用哪些依赖 ✅
* sky-server 正确依赖了 sky-common 和 sky-pojo ✅

所以，如果现在 IDEA 里 **还出现**：

* `Cannot resolve symbol 'Employee'`
* `Cannot resolve symbol 'EmployeeLoginVO'`
* `Cannot resolve symbol 'jwtProperties'`
* `Cannot resolve method 'login' in 'EmployeeService'`

那问题更可能是下面几类👇

---

## 4️⃣ 如果还在报 “Cannot resolve symbol …”，重点排查这几项

### ✅ A. 类本身有没有建对位置、包名有没有对上

例如 `Employee`：

1. 是否在 **sky-pojo 模块** 下面？路径最好类似：
   `sky-pojo/src/main/java/com/sky/entity/Employee.java`
2. 类上的包名是否是：

   ```java
   package com.sky.entity;
   ```
3. sky-server 里的 import 是否一致：

   ```java
   import com.sky.entity.Employee;
   ```

只要包名 / 路径有一个不一致，IDE 就会报 `Cannot resolve symbol`。

---

### ✅ B. Maven 构建/索引没刷新

推荐从项目根目录跑一次完整构建：

```bash
mvn clean install -U
```

然后在 IDEA 里：

1. 右侧 Maven 面板 → 点击刷新图标（Reload All Maven Projects）
2. 如果还是乱，可以再来一发：`File → Invalidate Caches / Restart`

---

### ✅ C. IDEA 模块依赖没更新（一般自动的，但有时候会乱）

在 IDEA 里打开：

> `File → Project Structure → Modules → sky-server → Dependencies`

看看：

* 里面有没有 `sky-common` 和 `sky-pojo` 这两个模块/库
* Scope 一般是 `Compile`

如果那里没有，点 `+` 手动加一下（不过正常情况下 Maven 导入会自动处理）。

---

## 5️⃣ 帮你简化一遍“脑子里的模型”（以后面试也能用）

你现在这个多模块项目可以这么理解：

1. **顶层父 pom**：

   * 继承 Spring Boot 父
   * 用 `<dependencyManagement>` 统一定义：mybatis、lombok、fastjson、poi… 的版本
   * 用 `<modules>` 声明有哪些子模块：`sky-common` / `sky-pojo` / `sky-server`

2. **中间层（sky-common / sky-pojo）**：

   * 继承这个父 pom
   * 各自声明自己**需要的依赖**（比如 Entity 模块要 lombok，common 模块要配置处理器等）

3. **最外层（sky-server）**：

   * 一方面继承父 pom 的版本管理
   * 一方面 **依赖其它模块（sky-common / sky-pojo）**，拿到实体类、工具类等
   * 再加上 Web、DB、Redis、MyBatis 这些运行时依赖

---

如果你现在项目还能报某一两个具体类找不到，你可以：

* 把 **那几个类的路径 + 顶部 package + import** 贴一下
* 我可以帮你直接对照成：应该放在哪个模块、什么包名、sky-server 要怎么 import

这样顺便你脑子里会对「多模块 + 依赖继承」这块形成一个特别清楚的图。


可以，`mvn clean install -U` 一报错，屏幕上信息会非常多，其实**核心就两三行**。我帮你分两步走：先教你怎么“抓住第一个关键错误”，再结合你这个项目猜一猜最可能的几类问题。

---

## 1️⃣ 先学会“看懂一大堆报错”的正确姿势

Maven 输出很多 `[INFO]`，真正重要的是 **第一个 `[ERROR]`**，尤其是这两段：

1. **最上面第一次出现 `[ERROR]` 的地方**：
   通常是编译失败的具体原因，例如：

   ```text
   [ERROR] /.../sky-server/src/main/java/com/sky/controller/EmployeeController.java:[23,13] cannot find symbol
   [ERROR]   symbol:   class EmployeeLoginVO
   [ERROR]   location: class com.sky.controller.EmployeeController
   ```

   或者：

   ```text
   [ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.8.1:compile ...
   ```

2. 最下面的总结块：

   ```text
   [ERROR] Failed to execute goal ... on project sky-server: Compilation failure
   [ERROR] Failed to execute goal ... on project sky-pojo: ...
   ```

👉 做法：
**不用管有多少 `[INFO]`，只要顺着往上翻，找到“第一个 `[ERROR]` 出现的位置”**，看看是：

* `cannot find symbol`
* 或者 `package ... does not exist`
* 或 `Non-resolvable parent POM`
* 或 `Failed to execute goal ...`（再看下面的小字）

---

## 2️⃣ 结合你的 POM，最可能的几类错误（你可以对号入座）

从你现在三层 POM 看，**Maven 配置本身已经基本没问题了**，所以 `mvn clean install` 报错“很多”，99% 是下面这些编译错误之一：

---

### ✅ 类型 1：Java 编译错误：`cannot find symbol` / `package ... does not exist`

例如（举例）：

```text
[ERROR] /.../sky-server/src/main/java/com/sky/controller/EmployeeController.java:[26,17] cannot find symbol
[ERROR]   symbol:   class EmployeeLoginVO
```

或者：

```text
[ERROR] package com.sky.result does not exist
```

**这类错误不是 Maven 依赖问题，而是：**

* 类 / 接口 **根本没写**，只是跟着教程用了名字；
* 类写了，但：

  * 放在了错误的模块（比如放在 sky-server，而你从 sky-pojo 导入）；
  * 放在错误的包名下，`package` 跟 import 不一致；
* 或者 import 丢了、写错了。

👉 结合你之前 IDE 里的提示：

* `Cannot resolve symbol 'Employee'`
* `Cannot resolve symbol 'EmployeeLoginVO'`
* `Cannot resolve method 'login' in 'EmployeeService'`
* `Cannot resolve symbol 'jwtProperties'`
* `Cannot resolve symbol 'Result'`

这些基本都是**代码层面的引用问题**，不是 POM 的锅：

#### 建议检查：

1. `Employee` / `EmployeeLoginVO` / `Result` / `JwtProperties` 这些类：

   * 是否真的存在对应的 `.java` 文件？
   * 是否在正确模块（常见写法：

     * `sky-pojo/src/main/java/com/sky/entity/Employee.java`
     * `sky-pojo/src/main/java/com/sky/vo/EmployeeLoginVO.java`
     * `sky-common/src/main/java/com/sky/result/Result.java`
     * `sky-server` 或 `sky-common` 里有 `JwtProperties` 配置类）

2. 顶部包名是否和 import 对得上，比如：

   ```java
   // 文件里
   package com.sky.entity;
   ```

   ```java
   // 使用处
   import com.sky.entity.Employee;
   ```

3. `EmployeeService` 里是否真的有：

   ```java
   public EmployeeLoginVO login(EmployeeLoginDTO dto) { ... }
   ```

   如果你在 Controller 调用 `employeeService.login(employeeLoginDTO)`，但 Service 里叫 `login(UserLoginDTO)` 或参数不匹配，也会报类似错。

---

### ✅ 类型 2：父 POM / 模块解析失败（你之前遇到过）

之前你有这个错误：

```text
Non-resolvable parent POM for org.example:sky-pojo:1.0-SNAPSHOT: 
Could not find artifact org.example:sky-take-out-Monica:pom:1.0-SNAPSHOT
```

这个是因为当时子模块的 `<parent>` 还是 `org.example`，而现在父 pom 是：

```xml
<groupId>com.sky</groupId>
<artifactId>sky-take-out-Monica</artifactId>
```

目前你的三个 POM 都已经改成：

```xml
<parent>
    <groupId>com.sky</groupId>
    <artifactId>sky-take-out-Monica</artifactId>
    <version>1.0-SNAPSHOT</version>
</parent>
```

✅ 所以**只要你在项目根目录**（有这个父 pom 的目录）执行：

```bash
mvn clean install -U
```

理论上不会再出现“找不到 parent POM”的问题。
如果还出现类似内容，重点检查：

* 是不是在某个子模块目录单独跑了 `mvn`；
* 某个旧模块的 POM 还残留 `org.example`。

---

### ✅ 类型 3：测试失败（用 `-DskipTests` 排除干扰）

有时候业务代码编译 OK，但测试没配置数据库、没配环境，会导致：

```text
[ERROR] Failed to execute goal ... maven-surefire-plugin ...: There are test failures.
```

短期只想先把项目**编译通过**、装进本地仓库，可以用：

```bash
mvn clean install -U -DskipTests
```

这样可以先确认：

* 依赖 / 编译 / 模块依赖关系都是 OK 的；
* 后面再专门解决测试环境问题。

---

## 3️⃣ 推荐你现在就做的 3 步（非常具体）

1. **重新跑一下（可以直接复制）**：

   ```bash
   mvn clean install -U -DskipTests
   ```

2. 执行完后，**往上滚动，找到第一个 `[ERROR]` 开头的地方**，看它更像是哪种：

   * `cannot find symbol` / `package ... does not exist` → 类/包问题；
   * `Non-resolvable parent POM` → 父 POM / groupId, artifactId 还没对齐；
   * `Failed to execute goal ...: Compilation failure`，下面跟着一堆 `cannot find symbol` → 也是代码没写全。

3. 把**第一组错误**（那几行 `[ERROR]`）复制出来，下次发给我，我可以直接告诉你：

   * 这是哪个类 / 哪个文件；
   * 需要在哪个模块创建什么文件 / 改什么包名 / 加什么方法。

---

现在从 POM 来看：**依赖继承 & 模块依赖已经搭好了**，接下来报的“很多错误”，其实是在提醒你：

> 教程代码里提到的一些 DTO / VO / 工具类 / 配置类，你这边还没全部补齐，或者放到了不对的地方。

你可以先按上面的步骤锁定**第一条具体错误**，然后我们一条一条把这些类/包名对齐掉，很快整个项目就能完整跑起来了 💪






如果你愿意，可以把 **父 pom + sky-common 的 pom** 一起贴出来，我可以帮你直接看是哪一步没继承对 👍
