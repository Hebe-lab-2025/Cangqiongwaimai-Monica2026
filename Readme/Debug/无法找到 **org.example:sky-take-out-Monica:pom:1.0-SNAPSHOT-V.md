好，我们一步一步把这个错误「拆开＋修死」。你现在的错误信息，其实已经直接把问题点出来了：

> Non-resolvable parent POM for **org.example:sky-pojo:1.0-SNAPSHOT**
> …无法找到 **org.example:sky-take-out-Monica:pom:1.0-SNAPSHOT**

说明两件事：

1. Maven 认为 **`sky-pojo` 这个模块的坐标是：`org.example:sky-pojo:1.0-SNAPSHOT`**
2. 它认为 `sky-pojo` 的父 POM 是：**`org.example:sky-take-out-Monica:1.0-SNAPSHOT`**

但你的根 POM 明明是：

```xml
<groupId>com.sky</groupId>
<artifactId>sky-take-out-Monica</artifactId>
<version>1.0-SNAPSHOT</version>
<packaging>pom</packaging>
```

也就是整套工程真正的家长是：

> **com.sky : sky-take-out-Monica : 1.0-SNAPSHOT**

---

## 🔧 第 1 步：彻底改好 `sky-pojo/pom.xml`

你刚贴的 `sky-pojo/pom.xml` 还是：

```xml
<parent>
    <groupId>org.example</groupId>
    <artifactId>sky-take-out-Monica</artifactId>
    <version>1.0-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

所以错误才会一直是 `org.example:sky-take-out-Monica`。

**请把 `sky-pojo/pom.xml` 整个文件直接替换成下面这个版本：**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <!-- ★ 这里必须和根 pom 完全一致：com.sky / sky-take-out-Monica -->
    <parent>
        <groupId>com.sky</groupId>
        <artifactId>sky-take-out-Monica</artifactId>
        <version>1.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <!-- 显式声明一下自己的坐标（可选，但清晰） -->
    <groupId>com.sky</groupId>
    <artifactId>sky-pojo</artifactId>
    <version>1.0-SNAPSHOT</version>

    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.9.2</version>
        </dependency>
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>io.swagger</groupId>
            <artifactId>swagger-annotations</artifactId>
            <version>1.5.22</version>
            <scope>compile</scope>
        </dependency>
    </dependencies>
</project>
```

**关键点：**

* `parent` 里的 `groupId` 改成 `com.sky`
* 项目自己的 `<groupId>` 也改成 `com.sky`，防止继续出现 `org.example:sky-pojo` 这种坐标

> 只要任何一个地方还写着 `org.example`，Maven 就会继续报 “org.example:xxx” 的错。

---

## 🔧 第 2 步：修好 `sky-common/pom.xml` 的 parent

错误里还有一个 warning：

> `'parent.relativePath' of POM com.sky:sky-common:1.0-SNAPSHOT … points at com.sky:sky-take-out-Monica instead of com.sky:sky-take-out`

说明 `sky-common/pom.xml` 里 parent 现在还是旧名字 `sky-take-out`。

请把 `sky-common/pom.xml` 的 `<parent>` 改成这样（只看这一段即可）：

```xml
<parent>
    <groupId>com.sky</groupId>
    <artifactId>sky-take-out-Monica</artifactId>  <!-- 一定要加上 Monica -->
    <version>1.0-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

完整文件可以直接用这版（和之前给你那版一样）：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.sky</groupId>
        <artifactId>sky-take-out-Monica</artifactId>
        <version>1.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <groupId>com.sky</groupId>
    <artifactId>sky-common</artifactId>
    <version>1.0-SNAPSHOT</version>

    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
        </dependency>
        <dependency>
            <groupId>commons-lang</groupId>
            <artifactId>commons-lang</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-json</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>com.aliyun.oss</groupId>
            <artifactId>aliyun-sdk-oss</artifactId>
        </dependency>
        <dependency>
            <groupId>javax.xml.bind</groupId>
            <artifactId>jaxb-api</artifactId>
        </dependency>
        <dependency>
            <groupId>com.github.wechatpay-apiv3</groupId>
            <artifactId>wechatpay-apache-httpclient</artifactId>
        </dependency>
    </dependencies>
</project>
```

---

## 🔄 第 3 步：刷新 Maven + 重新构建

1. 在 IntelliJ 里，对着**根目录**的 `pom.xml`：

   * 右键 → **Maven → Reload Project**
2. 然后在项目根目录执行：

```bash
mvn clean install
```

> 如果这个时候错误信息里不再出现 `org.example:...`，说明坐标已经统一成功了，接下来如果还有报错，就是别的问题（例如依赖下载失败、JDK 版本等）。

---

## 小总结（帮你记忆）

* **父 POM（根 pom）是谁？**
  👉 `com.sky : sky-take-out-Monica : 1.0-SNAPSHOT`

* **所有子模块的 `<parent>` 都必须写成这个坐标**
  👉 不要再出现 `org.example` / `sky-take-out` 旧名字

* **任何一行出现 `org.example`，Maven 就会继续报错**
  👉 所以最安全的方式：`sky-pojo` / `sky-common` 的 `groupId` 都写成 `com.sky`。

---

你可以改完再跑一次 `mvn clean install`，把新的报错（如果还有）直接贴上来，我可以帮你看第二步该处理什么。
