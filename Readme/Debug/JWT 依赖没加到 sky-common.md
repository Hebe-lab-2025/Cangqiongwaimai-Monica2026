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
