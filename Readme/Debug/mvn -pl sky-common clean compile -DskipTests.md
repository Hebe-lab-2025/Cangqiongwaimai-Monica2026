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



很好，现在问题已经非常清楚了：
**sky-common 编译失败，是因为少了 OSS 和 Spring 相关依赖。**

从截图里的几个关键信息看：

* `package com.aliyun.oss does not exist` → 少 **阿里云 OSS SDK**
* `package org.slf4j does not exist` → 少 **slf4j 日志接口**（@Slf4j 用的）
* `package org.springframework.boot.context.properties does not exist`
* `package org.springframework.stereotype does not exist`
* `cannot find symbol: class Component / ConfigurationProperties`
  → 少 **Spring / Spring Boot 依赖**

---

## 1️⃣ 把 sky-common 的 pom 再补齐一次（照抄就行）

请把 **`sky-common/pom.xml`** 中 `<dependencies>` 换成下面这一版（或者在你现有基础上补全这些依赖）：

```xml
<dependencies>
    <!-- Lombok：@Data、@Slf4j 等 -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>

    <!-- yml 配置提示用的，之前就有 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-configuration-processor</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- JAXB -->
    <dependency>
        <groupId>javax.xml.bind</groupId>
        <artifactId>jaxb-api</artifactId>
    </dependency>

    <!-- ✅ Spring Boot 基础：提供 @Component 等注解 + slf4j 依赖 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>

    <!-- ✅ fastjson：WeChatPayUtil 里用到 com.alibaba.fastjson.* -->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>fastjson</artifactId>
    </dependency>

    <!-- ✅ commons-lang：org.apache.commons.lang.* -->
    <dependency>
        <groupId>commons-lang</groupId>
        <artifactId>commons-lang</artifactId>
    </dependency>

    <!-- ✅ 微信支付 HTTP 客户端 -->
    <dependency>
        <groupId>com.github.wechatpay-apiv3</groupId>
        <artifactId>wechatpay-apache-httpclient</artifactId>
    </dependency>

    <!-- ✅ Apache HttpClient：HttpGet / HttpPost / HttpEntity 等 -->
    <dependency>
        <groupId>org.apache.httpcomponents</groupId>
        <artifactId>httpclient</artifactId>
    </dependency>

    <!-- ✅ 阿里云 OSS SDK：AliOssUtil 里 com.aliyun.oss.* -->
    <dependency>
        <groupId>com.aliyun.oss</groupId>
        <artifactId>aliyun-sdk-oss</artifactId>
    </dependency>
</dependencies>
```

说明一下：

* `fastjson / commons-lang / aliyun-sdk-oss / wechatpay-apache-httpclient` 的 **版本号都在父 pom 的 `<dependencyManagement>` 里**，所以这里不写 `<version>`；
* `spring-boot-starter` 会带上：

  * `spring-core / spring-context / spring-beans`（解决 `@Component` / `@Autowired` / `@ConfigurationProperties` 等）
  * `spring-boot`（里面有 `org.springframework.boot.context.properties.ConfigurationProperties`）
  * `spring-boot-starter-logging`（里面带 `slf4j-api`），解决 `org.slf4j` 不存在的问题。

---

## 2️⃣ 改完之后一定要做这两步

1. 在 IDEA 右侧 **Maven 面板 → 点击刷新（Reload All Maven Projects）**。
2. 在工程根目录执行：

   ```bash
   mvn -pl sky-common clean compile -DskipTests
   ```

   * 如果 `sky-common` 这一步能 **SUCCESS**，说明这些依赖已经补齐；
   * 然后再执行一次：

     ```bash
     mvn clean install -DskipTests
     ```

     看整个多模块是否都能成功。

---

## 3️⃣ 如果 IDE 还是一片红，怎么看下一步？

* 先看 **Build Output** 里新的第一个 `[ERROR]` 是谁（哪个类、哪一行）；
* 很可能接下来报的是项目里自己的工具类 / VO / DTO 没写全了（比如 JwtUtil 里用到的某个类不存在），这时就是代码问题，不是依赖问题了。

你先按上面把 `sky-common/pom.xml` 补全 + reload + 跑 `mvn -pl sky-common clean compile`，
把**最新的第一段错误**再丢给我，我们继续往下拆就好 👌



很好，这次就只剩一个点没补上了：**JWT 依赖没加到 sky-common 里**。

---

## 1️⃣ 错误在说什么？

```text
package io.jsonwebtoken does not exist
```

说明：

* `sky-common/src/main/java/com/sky/utils/JwtUtil.java` 里有：

  ```java
  import io.jsonwebtoken.Claims;
  import io.jsonwebtoken.Jwts;
  import io.jsonwebtoken.SignatureAlgorithm;
  ...
  ```

* 但 `sky-common` 这个模块 **没有引入 jjwt 依赖**，所以编译找不到。

父 pom 里其实已经在 `<dependencyManagement>` 中声明了版本：

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>${jjwt}</version>
</dependency>
```

所以我们只需要在 **sky-common 的 `<dependencies>` 里声明用它** 就行。

---

## 2️⃣ 给 sky-common 加上 jjwt 依赖

在 `sky-common/pom.xml` 的 `<dependencies>` 里，再加一段（和其他依赖并列）：

```xml
    <!-- ✅ JWT 工具类 JwtUtil 使用的 jjwt -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt</artifactId>
    </dependency>
```

不要写 `<version>`，版本会从父 pom 的 `<dependencyManagement>` 里继承 `${jjwt}`。

完整效果大概是这样（只画尾部）：

```xml
    <!-- 阿里云 OSS SDK -->
    <dependency>
        <groupId>com.aliyun.oss</groupId>
        <artifactId>aliyun-sdk-oss</artifactId>
    </dependency>

    <!-- ✅ JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt</artifactId>
    </dependency>
</dependencies>
```

---

## 3️⃣ 再次编译验证

1. IDEA 右侧 Maven → 点击刷新（Reload All Maven Projects）。
2. 在项目根目录执行：

   ```bash
   mvn -pl sky-common clean compile -DskipTests
   ```

* 如果 `sky-common` 这次通过 ✅，
  再跑一遍整个项目：

  ```bash
  mvn clean install -DskipTests
  ```

接下来如果还有报错，就会是别的类/模块的问题了，你再把**新的第一段 `[ERROR]`** 贴出来，我们继续往下修就好。



