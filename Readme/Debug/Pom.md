好，进展很明显，现在只剩 **Jackson 的 jsr310 模块没加** 了 👌

---

## 1️⃣ 错误在说什么？

从截图和日志里看到：

```text
package com.fasterxml.jackson.datatype.jsr310.deser does not exist
package com.fasterxml.jackson.datatype.jsr310.ser does not exist
```

`JacksonObjectMapper.java` 里有类似：

```java
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
```

这些类都来自 **`jackson-datatype-jsr310`** 这个包。
现在 `sky-common` 里还没引入它，所以编译找不到。

---

## 2️⃣ 在 `sky-common/pom.xml` 里加上 Jackson 依赖

在你刚才已经补好的 `<dependencies>` 末尾，再加上这两段（或者对齐风格，只要在 `<dependencies>` 里面就行）：

```xml
    <!-- ✅ Jackson 核心：ObjectMapper 等（让版本由 Spring Boot 管） -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>

    <!-- ✅ Java 8 日期时间支持：LocalDate/LocalDateTime 的序列化与反序列化 -->
    <dependency>
        <groupId>com.fasterxml.jackson.datatype</groupId>
        <artifactId>jackson-datatype-jsr310</artifactId>
    </dependency>
```

**不要写 `<version>`**：

* 你项目的顶层父是 `spring-boot-starter-parent:2.7.3`，
* Spring Boot 会自动帮你管理 jackson 的版本（通过它自己的 BOM），直接跟着它就好。

> 🔍 小优化建议：
> 之前在 `sky-pojo/pom.xml` 里你写了：
>
> ```xml
> <dependency>
>     <groupId>com.fasterxml.jackson.core</groupId>
>     <artifactId>jackson-databind</artifactId>
>     <version>2.9.2</version>
> </dependency>
> ```
>
> 建议把 **`<version>2.9.2</version>` 删掉**，
> 让它也用 Spring Boot 的统一版本，避免版本冲突。

---

## 3️⃣ 再跑一遍编译

1. 在 IDEA 右侧 Maven → **Reload All Maven Projects**
2. 在项目根目录执行（先只编 sky-common）：

   ```bash
   mvn -pl sky-common clean compile -DskipTests
   ```

   如果通过 ✅，再执行整项目：

   ```bash
   mvn clean install -DskipTests
   ```

---

如果这一步之后还有报错，把**新的第一段 `[ERROR]`（哪一个类、哪一行）**再贴给我，我们继续往下清理就行，现在已经快收尾了 💪



很好，现在就只剩 **Jackson jsr310 模块没真正进 classpath** 这一个点了。

日志还是这几行：

```text
package com.fasterxml.jackson.datatype.jsr310.deser does not exist
package com.fasterxml.jackson.datatype.jsr310.ser does not exist
```

说明 **Maven 里还没有 `jackson-datatype-jsr310` 这个依赖（或者没加在 sky-common 上）**。

---

## 1. 请把 sky-common 的 pom 改成这样（直接全量覆盖也可以）

`sky-common/pom.xml` 建议内容如下（你可以整段对比一下，确保依赖都在）：

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

    <artifactId>sky-common</artifactId>

    <dependencies>
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>

        <!-- yml 配置提示 -->
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

        <!-- Spring 基础 + slf4j -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <!-- fastjson -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
        </dependency>

        <!-- commons-lang -->
        <dependency>
            <groupId>commons-lang</groupId>
            <artifactId>commons-lang</artifactId>
        </dependency>

        <!-- 微信支付 HTTP 客户端 -->
        <dependency>
            <groupId>com.github.wechatpay-apiv3</groupId>
            <artifactId>wechatpay-apache-httpclient</artifactId>
        </dependency>

        <!-- Apache HttpClient -->
        <dependency>
            <groupId>org.apache.httpcomponents</groupId>
            <artifactId>httpclient</artifactId>
        </dependency>

        <!-- 阿里云 OSS -->
        <dependency>
            <groupId>com.aliyun.oss</groupId>
            <artifactId>aliyun-sdk-oss</artifactId>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt</artifactId>
        </dependency>

        <!-- Jackson 核心 -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>

        <!-- ⭐️ 关键：Java8 时间模块 jsr310（LocalDate/LocalDateTime 序列化用） -->
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
        </dependency>
    </dependencies>
</project>
```

一定要确认这两行**拼写完全一致**（特别是 groupId 和 artifactId）：

```xml
<groupId>com.fasterxml.jackson.datatype</groupId>
<artifactId>jackson-datatype-jsr310</artifactId>
```

---

## 2. 改完之后的步骤

1. 保存 `pom.xml`。
2. 在 IDEA 右侧 **Maven → Reload All Maven Projects** 一次。
3. 在项目根目录执行（只编 sky-common）：

   ```bash
   mvn -pl sky-common clean compile -DskipTests
   ```

如果还报同样的错，90% 是编辑到别的 pom 了（比如改到了 sky-pojo、或外层另一个目录的 sky-common），
但先按上面这版完整覆盖 sky-common 的 pom 再编译，通常这一坨 jsr310 的错误就会消失。

消失之后，如果出现新的 `[ERROR]`，你可以再把新的那几行贴出来，我们继续往下清就好。




