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
