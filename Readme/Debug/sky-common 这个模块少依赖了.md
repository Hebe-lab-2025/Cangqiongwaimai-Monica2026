很好，这个错误信息已经非常清晰地告诉我们：**sky-common 这个模块少依赖了**。

---

## 1️⃣ 现在到底哪里错了？

关键信息：

```text
[INFO] sky-common ........ FAILURE
[INFO] sky-pojo .......... SKIPPED
[INFO] sky-server ........ SKIPPED
...
[ERROR] on project sky-common: Compilation failure
[ERROR] WeChatPayUtil.java: package com.alibaba.fastjson does not exist
[ERROR] ... package com.wechat.pay.contrib.apache.httpclient does not exist
[ERROR] ... package org.apache.commons.lang does not exist
[ERROR] ... package org.apache.http... does not exist
[ERROR] ... package org.springframework.beans.factory.annotation does not exist
[ERROR] ... package org.springframework.stereotype does not exist
[ERROR] WeChatProperties.java: package org.springframework.boot.context.properties does not exist
[ERROR] ... cannot find symbol: class Component
[ERROR] ... cannot find symbol: class ConfigurationProperties
```

**解释：**

* 这些类都在 `sky-common` 里的 `WeChatPayUtil` 和 `WeChatProperties` 中使用。

* 但是 `sky-common/pom.xml` 目前只引了：

  ```xml
  lombok
  spring-boot-configuration-processor
  jaxb-api
  ```

* 所以 Maven 编译 `sky-common` 时，**找不到 fastjson / wechatpay / commons-lang / httpclient / spring 相关类** → 直接编译失败。

* 一旦 `sky-common` 失败，后面的 `sky-pojo`、`sky-server` 就被跳过了（SKIPPED）。

---

## 2️⃣ 我们要给 sky-common 补上哪些依赖？

根据错误提示 + 你父 pom 里的 dependencyManagement，`sky-common` 至少要加这些依赖：

1. fastjson：`com.alibaba.fastjson`
2. commons-lang：`org.apache.commons.lang`
3. 微信支付 HTTP Client：`com.github.wechatpay-apiv3:wechatpay-apache-httpclient`
4. Apache HttpClient：`org.apache.httpcomponents:httpclient`
5. Spring 注解相关（`@Component`、`@ConfigurationProperties` 等）
   👉 最简单就是加一个 `spring-boot-starter`（或者至少 `spring-context` + `spring-boot`）

---

## 3️⃣ 直接给你一份修改好的 `sky-common/pom.xml`

请把你现在的 `sky-common/pom.xml` 替换成下面这个版本（原来的依赖我也帮你保留了）：

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
        <!-- 已有：lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>

        <!-- 已有：配置提示处理器 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- 已有：jaxb-api -->
        <dependency>
            <groupId>javax.xml.bind</groupId>
            <artifactId>jaxb-api</artifactId>
        </dependency>

        <!-- ✅ Spring 基础：提供 @Component 等注解 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <!-- ✅ fastjson：com.alibaba.fastjson.* -->
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
    </dependencies>
</project>
```

说明几点：

* `fastjson`、`commons-lang`、`wechatpay-apache-httpclient` 都在你的父 pom 的 `<dependencyManagement>` 里已经写了版本号，所以这里 **不用写 `<version>`**；
* `httpclient` 的版本由 Spring Boot 父 pom 提供 BOM 管理，也可以不写 `<version>`；
* `spring-boot-starter` 提供了：

  * `spring-core`
  * `spring-context`（包含 `@Component` 等注解）
  * `spring-beans`（`@Autowired`）等
    足够 WeChat 这些工具类和配置类编译。

---

## 4️⃣ 改完之后，下一步怎么验证？

1. 保存 `sky-common/pom.xml`。

2. 在项目 **根目录**（有父 pom 的那个目录）执行：

   ```bash
   mvn clean install -U -DskipTests
   ```

3. 观察：

   * 如果依赖补齐了，`sky-common` 应该可以 **SUCCESS**；
   * 然后 `sky-pojo`、`sky-server` 也会继续跑。

如果这一步通过了，后面如果还有错误，就会是其它模块的类/方法找不到，我们可以再针对下一段 `[ERROR]` 处理。

---

## 5️⃣ 小总结一句（以后面试也能用）

> 在 Maven 多模块项目里，父 pom 的 `<dependencyManagement>` 只负责“统一版本号”，具体每个模块要用哪些库，还是要在自己的 `<dependencies>` 里显式声明，否则像 `sky-common` 这种公共模块会在编译时因为缺少 fastjson、Spring、HttpClient 等依赖而报 `package ... does not exist` 的错误。

你先按上面的改 `sky-common/pom.xml` 然后再跑一次 `mvn clean install -U -DskipTests`，把新的第一段报错发给我，我们就继续往下拆 💪


