好，这四个 `pom.xml` 放在一起以后，问题就很清楚了，我帮你对比 + 给出直接可用的修改版本。

---

## 1. 现在哪里不一致？

### 根 POM（✅ 是对的）

```xml
<groupId>com.sky</groupId>
<artifactId>sky-take-out-Monica</artifactId>
<packaging>pom</packaging>
<version>1.0-SNAPSHOT</version>
<modules>
    <module>sky-common</module>
    <module>sky-pojo</module>
    <module>sky-server</module>
</modules>
```

> 项目“真正的”坐标是：
> **`com.sky : sky-take-out-Monica : 1.0-SNAPSHOT`**

后面所有子模块的 `<parent>` 都必须写成这个坐标。

---

### (1) `sky-server/pom.xml`（✅ 已经是正确写法）

```xml
<parent>
    <groupId>com.sky</groupId>
    <artifactId>sky-take-out-Monica</artifactId>
    <version>1.0-SNAPSHOT</version>
</parent>
```

这个没问题。

---

### (2) `sky-pojo/pom.xml`（❌ groupId 写成 org.example 了）

你现在是这样：

```xml
<parent>
    <groupId>org.example</groupId>
    <artifactId>sky-take-out-Monica</artifactId>
    <version>1.0-SNAPSHOT</version>
</parent>
```

但根 POM 明明是 `com.sky`，所以 Maven 会说：

> 我去本地找 `org.example:sky-take-out-Monica:1.0-SNAPSHOT`，结果根本没这个项目。

---

### (3) `sky-common/pom.xml`（❌ artifactId 还是旧名字）

你现在是这样：

```xml
<parent>
    <artifactId>sky-take-out</artifactId>   <!-- 这里还是旧名 -->
    <groupId>com.sky</groupId>
    <version>1.0-SNAPSHOT</version>
</parent>
```

根 POM 的 artifactId 已经叫 **`sky-take-out-Monica`** 了，所以 Maven 会提示：

> `parent.relativePath` 指向的是 `com.sky:sky-take-out-Monica`，
> 但你这里写的是 `com.sky:sky-take-out`，对不上。

---

## 2. 应该怎么改？——直接给你改好的版本

### ✅ 修改版 `sky-pojo/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <!-- 统一成和根 POM 一样的 parent 坐标 -->
    <parent>
        <groupId>com.sky</groupId>
        <artifactId>sky-take-out-Monica</artifactId>
        <version>1.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <!-- 可写可不写，不写就继承 parent 的 groupId=com.sky -->
    <artifactId>sky-pojo</artifactId>

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

* `groupId` 改成 `com.sky`
* `<relativePath>../pom.xml</relativePath>` 明确告诉 Maven：父 POM 就在上一级目录

---

### ✅ 修改版 `sky-common/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <!-- 这里的 artifactId 要和根 POM 完全一致：sky-take-out-Monica -->
    <parent>
        <groupId>com.sky</groupId>
        <artifactId>sky-take-out-Monica</artifactId>
        <version>1.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>sky-common</artifactId>

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

**关键点：**

* 把 `<artifactId>sky-take-out</artifactId>` 改成 **`sky-take-out-Monica`**
* 同样加上 `<relativePath>../pom.xml</relativePath>`

---

## 3. 改完之后怎么验证？

1. 在 IntelliJ 里，对着根 `pom.xml`：

   * 右键 → **Maven → Reload Project**
2. 然后在项目根目录执行：

```bash
mvn clean install
```

如果四个 POM 的坐标都统一成：

* 父：`com.sky : sky-take-out-Monica : 1.0-SNAPSHOT`
* 子模块 `<parent>` 都是这个

之前那个：

> Non-resolvable parent POM / parent.relativePath 指向错误

的错误就会消失。

---

如果你改完再跑一次 `mvn clean` 的输出贴出来，我可以帮你看第二轮还有没有别的小问题（比如依赖版本、JDK 版本之类的）。
