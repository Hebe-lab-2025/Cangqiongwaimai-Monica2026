```
/bin/sh /Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn -Didea.version=2025.3 -Dmaven.ext.class.path=/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven-event-listener.jar -Djansi.passthrough=true -Dstyle.color=always -Dmaven.repo.local=/Users/yonggan/.m2/repository clean -f pom.xml
Picked up JAVA_TOOL_OPTIONS: -Dfile.encoding=UTF-8
[INFO] Scanning for projects...
[ERROR] [ERROR] Some problems were encountered while processing the POMs:
[WARNING] 'parent.relativePath' of POM org.example:sky-pojo:1.0-SNAPSHOT (/Users/yonggan/Downloads/sky-take-out-Monica-2026/sky-take-out-Monica/sky-pojo/pom.xml) points at com.sky:sky-take-out-Monica instead of org.example:sky-take-out-Monica, please verify your project structure @ line 5, column 13
[FATAL] Non-resolvable parent POM for org.example:sky-pojo:1.0-SNAPSHOT: The following artifacts could not be resolved: org.example:sky-take-out-Monica:pom:1.0-SNAPSHOT (absent): Could not find artifact org.example:sky-take-out-Monica:pom:1.0-SNAPSHOT and 'parent.relativePath' points at wrong local POM @ line 5, column 13
[WARNING] 'parent.relativePath' of POM com.sky:sky-common:1.0-SNAPSHOT (/Users/yonggan/Downloads/sky-take-out-Monica-2026/sky-take-out-Monica/sky-common/pom.xml) points at com.sky:sky-take-out-Monica instead of com.sky:sky-take-out, please verify your project structure @ line 5, column 13
 @ 
[ERROR] The build could not read 1 project -> [Help 1]
[ERROR]   
[ERROR]   The project org.example:sky-pojo:1.0-SNAPSHOT (/Users/yonggan/Downloads/sky-take-out-Monica-2026/sky-take-out-Monica/sky-pojo/pom.xml) has 1 error
[ERROR]     Non-resolvable parent POM for org.example:sky-pojo:1.0-SNAPSHOT: The following artifacts could not be resolved: org.example:sky-take-out-Monica:pom:1.0-SNAPSHOT (absent): Could not find artifact org.example:sky-take-out-Monica:pom:1.0-SNAPSHOT and 'parent.relativePath' points at wrong local POM @ line 5, column 13 -> [Help 2]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/ProjectBuildingException
[ERROR] [Help 2] http://cwiki.apache.org/confluence/display/MAVEN/UnresolvableModelException
```
先帮你把报错翻成「人话」，再说怎么改 👇

---

## 1. 这段报错在说什么？

关键几行：

```text
[WARNING] 'parent.relativePath' of POM org.example:sky-pojo:1.0-SNAPSHOT
points at com.sky:sky-take-out-Monica instead of org.example:sky-take-out-Monica

[FATAL] Non-resolvable parent POM for org.example:sky-pojo:1.0-SNAPSHOT:
The following artifacts could not be resolved:
org.example:sky-take-out-Monica:pom:1.0-SNAPSHOT (absent)
Could not find artifact org.example:sky-take-out-Monica:pom:1.0-SNAPSHOT
and 'parent.relativePath' points at wrong local POM
```

翻译：

1. **`sky-pojo/pom.xml` 的 `<parent>` 写错了**

   * 它声明父 POM 是：`org.example:sky-take-out-Monica:1.0-SNAPSHOT`
   * 但它实际通过 `relativePath`（通常是 `../pom.xml`）指向的文件，**坐标却是** `com.sky:sky-take-out-Monica`
     👉 也就是：**groupId / artifactId 对不上**。

2. Maven 想去本地仓库找这个父 POM：

   ```text
   org.example:sky-take-out-Monica:pom:1.0-SNAPSHOT
   ```

   结果：

   * 本地仓库里没有这个坐标的 POM（`absent`）
   * 而且本地文件的坐标也跟你声明的不一致
     👉 所以它说：**Non-resolvable parent POM（父 POM 无法解析）**

3. 另一个 warning：

```text
[WARNING] 'parent.relativePath' of POM com.sky:sky-common:1.0-SNAPSHOT
points at com.sky:sky-take-out-Monica instead of com.sky:sky-take-out
```

说明：

* `sky-common/pom.xml` 里，父 POM 的坐标写的是 **`com.sky:sky-take-out-Monica`**
* 但 `../pom.xml` 这个文件里，真正的坐标可能是 **`com.sky:sky-take-out`**
  👉 也就是 **artifactId 也不一致**。

**总结一句话：**
你把项目改名成 `sky-take-out-Monica` 之后，**根 POM 和子模块 POM 的 groupId / artifactId / relativePath 没有统一**，所以 Maven 找不到「真正的父 POM」。

---

## 2. 关键概念简单补一下

* **父 POM (`<parent>`)**：
  子模块继承版本、依赖、插件配置的「总配置文件」。

* **坐标（groupId + artifactId + version）**：
  Maven 识别一个项目的身份证。哪怕文件在本地，坐标不对也当成两个不同项目。

* **`relativePath`**：
  告诉 Maven：**父 POM 文件** 在本地哪里，一般是 `../pom.xml`。
  但就算路径对了，如果那个文件里的 groupId/artifactId/version 不匹配你声明的父坐标，也会报错（就是你现在的情况）。

---

## 3. 怎么一步步修？（建议按这个做）

> 目标：**所有 POM 的坐标统一**，特别是根 POM 和各个 `<parent>`。

### 第一步：看根 POM（项目根目录的 `pom.xml`）

在这个文件里找：

```xml
<groupId>???</groupId>
<artifactId>???</artifactId>
<version>1.0-SNAPSHOT</version>
<packaging>pom</packaging>
```

记住这里真正的坐标，比如很可能是：

```xml
<groupId>com.sky</groupId>
<artifactId>sky-take-out</artifactId>
<version>1.0-SNAPSHOT</version>
```

或者：

```xml
<groupId>com.sky</groupId>
<artifactId>sky-take-out-Monica</artifactId>
<version>1.0-SNAPSHOT</version>
```

**以根 POM 为标准**，不要随便改它；然后让所有子模块跟它保持一致。

---

### 第二步：修 `sky-pojo/pom.xml`

打开：
`/sky-pojo/pom.xml`，找到 `<parent>` 这一段，把它改成和根 POM 一样：

假设根 POM 是：

```xml
<groupId>com.sky</groupId>
<artifactId>sky-take-out-Monica</artifactId>
<version>1.0-SNAPSHOT</version>
```

那子模块应该写：

```xml
<parent>
    <groupId>com.sky</groupId>
    <artifactId>sky-take-out-Monica</artifactId>
    <version>1.0-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>
</parent>

<modelVersion>4.0.0</modelVersion>
<groupId>com.sky</groupId>
<artifactId>sky-pojo</artifactId>
<version>1.0-SNAPSHOT</version>
```

注意两点：

1. **不要用 `org.example`**，改成和根 POM 一样的 `com.sky`。
2. `<relativePath>../pom.xml</relativePath>` 指向的是项目根目录的 `pom.xml`。

---

### 第三步：修 `sky-common/pom.xml`

同样打开 `/sky-common/pom.xml`，检查 `<parent>`：

* 如果根 POM 是 `com.sky:sky-take-out`，这里就写 `sky-take-out`
* 如果根 POM 是 `com.sky:sky-take-out-Monica`，这里就写 `sky-take-out-Monica`

示例（假设根是 `sky-take-out`）：

```xml
<parent>
    <groupId>com.sky</groupId>
    <artifactId>sky-take-out</artifactId>
    <version>1.0-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

**总原则：所有子模块的 `<parent>` 坐标必须和根 POM 完全一致。**

---

### 第四步：重新构建

在**项目根目录**下执行：

```bash
mvn clean install
# 或者在 IntelliJ 里右键根 pom: Maven -> Reload Project，然后再点 clean / install
```

如果 parent 坐标准确、relativePath 正确，这个「Non-resolvable parent POM」错误就会消失。

---

## 4. 小结一句

你的问题本质是：

> **改了项目名 / 包名，但没有统一修改所有 POM 里的 groupId / artifactId / parent 配置，导致子模块找不到真正的父 POM。**

按照上面 4 步，保证：

* 根 POM 坐标 ✅
* 所有子模块 `<parent>` 和它完全一致 ✅
* `relativePath` 指向 `../pom.xml` ✅

就可以正常 `mvn clean` / `mvn install` 了。

如果你愿意，可以把根 `pom.xml` 和 `sky-pojo/pom.xml` 的内容贴出来，我可以帮你精确改一版。



