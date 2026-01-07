重点一句话：**你还在用 `org.example`，但根 POM 是 `com.sky`，所以 Maven 死活找不到这个父 POM。**

---

## 1. 报错再翻译一下

```text
Non-resolvable parent POM for org.example:sky-pojo:1.0-SNAPSHOT:
The following artifacts could not be resolved:
org.example:sky-take-out-Monica:pom:1.0-SNAPSHOT (absent)
Could not find artifact org.example:sky-take-out-Monica:pom:1.0-SNAPSHOT
and 'parent.relativePath' points at wrong local POM
```

意思是：

* `sky-pojo` 的坐标是：`org.example:sky-pojo:1.0-SNAPSHOT`
* 它声明父 POM 是：`org.example:sky-take-out-Monica:1.0-SNAPSHOT`
* Maven 去本地仓库和 `../pom.xml` 里找 `org.example:sky-take-out-Monica`，结果：

  * 根本没有这个项目（因为你的根 POM 是 `com.sky:sky-take-out-Monica`）
  * 所以说：父 POM 无法解析 + relativePath 指到了不匹配的 POM

---

## 2. 你的 `sky-pojo/pom.xml` 现在是这样的

```xml
<parent>
    <groupId>org.example</groupId>
    <artifactId>sky-take-out-Monica</artifactId>
    <version>1.0-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

而根 POM 是：

```xml
<groupId>com.sky</groupId>
<artifactId>sky-take-out-Monica</artifactId>
<version>1.0-SNAPSHOT</version>
<packaging>pom</packaging>
```

**groupId 完全不一样**：

* 根：`com.sky`
* 子模块写的是：`org.example`

Maven 会认为这是两个完全不同的项目。

---

## 3. 正确写法（请直接替换）

把 `sky-pojo/pom.xml` 改成下面这样（完整版本给你）：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <!-- ★★ 这里非常关键：要跟根 pom 完全一致 ★★ -->
    <parent>
        <groupId>com.sky</groupId>
        <artifactId>sky-take-out-Monica</artifactId>
        <version>1.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <!-- 不写 groupId，会自动继承 parent 的 com.sky -->
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

**一定记得把 `org.example` → 改成 `com.sky`。**

---

## 4. 改完之后怎么验证？

1. 先在 IntelliJ 里对着根 `pom.xml`：

   * 右键 → Maven → **Reload Project**

2. 然后在项目根目录执行：

```bash
mvn clean install
```

如果 `sky-common` 也已经改成：

```xml
<parent>
    <groupId>com.sky</groupId>
    <artifactId>sky-take-out-Monica</artifactId>
    <version>1.0-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

那这个 **Non-resolvable parent POM** 的错误就会消失。

---

如果你改完再跑出的完整错误日志还有别的东西，可以再贴一段，我帮你看下一步是什么问题（比如依赖下载失败、JDK 版本之类的）。
