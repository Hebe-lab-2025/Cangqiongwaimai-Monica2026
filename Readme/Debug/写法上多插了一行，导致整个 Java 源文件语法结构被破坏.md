很好，你这一步其实只是**写法上多插了一行**，导致整个 Java 源文件语法结构被破坏，所以编译器报：

```
java: class EmployeeController is public, should be declared in a file named EmployeeController.java
```

👉 这种报错 **通常不是 EmployeeController 本身的问题**
而是：**别的类写坏了，影响后续文件解析**

---

## ✅ 你现在这段代码里最大的问题是这一行👇

```java
@Autowired
private JwtProperties jwtProperties;
```

🔴 **它被写在了类外面！**（这是不合法的 Java 语法）

---

## ✅ 正确做法（非常重要）

### ❌ 不要在 properties 类外面写字段

请把下面两行 **删掉**

```java
@Autowired
private JwtProperties jwtProperties;
```

---

## ✅ JwtProperties.java 应该长这样（最终正确版）

```java
package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.jwt")
@Data
public class JwtProperties {

    private String adminSecretKey;
    private long adminTtl;
    private String adminTokenName;

    private String userSecretKey;
    private long userTtl;
    private String userTokenName;
}
```

✔ 只保留一个类
✔ 不要在类外写任何字段或注解
✔ 文件名必须与类名一致：**JwtProperties.java**

---

## ✅ 然后在 EmployeeController 里注入它（这才是正确位置）

在 `EmployeeController` 中写：

```java
@Autowired
private JwtProperties jwtProperties;
```

例如👇

```java
@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private JwtProperties jwtProperties;   // ⭐ 在这里注入
}
```

---

## 🔄 最后做两件事

### 1️⃣ Maven Rebuild

```bash
mvn clean install -DskipTests
```

### 2️⃣ IDEA invalidate cache（如果还红）

```
File → Invalidate Caches → Invalidate and Restart
```

---

## 🎯 你刚刚踩到的知识点（面试高频）

> “class is public, should be declared in a file named XXX.java”

产生原因：

* 一个文件里定义多个 public class
* 类名与文件名不一致
* 写了**类外字段/代码**（你的情况）
* 大括号不匹配导致文件结构错乱

👉 记住：**类外只能出现 package/import/注解/注释**
👉 不能出现变量、方法、执行语句

---

如果现在还有报错，把**最新红色错误贴给我**，我们继续清理，直到跑起来 🚀
