下面我**一次性把你点的 3 件事讲清楚**，按「**零基础 → 内部机制 → 易错点**」来。

---

# 🧠 一、HTTP 请求 → Spring 内部执行流程（全链路）

## 1️⃣ 先给你一张**文字流程图（最重要）**

```text
浏览器 / Postman
        |
        |  HTTP GET /hello
        v
┌────────────────────┐
│ DispatcherServlet  │   ← Spring 的“总调度员 / 门卫”
└────────────────────┘
        |
        |  查映射关系
        v
┌────────────────────┐
│ HandlerMapping     │   ← 找到哪个 Controller + 方法
└────────────────────┘
        |
        v
┌────────────────────┐
│ HelloController    │
│  @RequestMapping  │
│   hi() 方法        │
└────────────────────┘
        |
        |  返回 "Hello, world"
        v
┌──────────────────────────────┐
│ @ResponseBody ?              │
│  YES → 写入 HTTP Response    │
│  NO  → 当作“视图名”处理       │
└──────────────────────────────┘
        |
        v
浏览器看到结果
```

---

## 2️⃣ 用人话完整讲一遍

1. **浏览器发请求**

   ```text
   GET http://localhost:8080/hello
   ```

2. **DispatcherServlet 接住**

   * Spring Boot 启动时就已经把它注册好了
   * 它是**所有请求的入口**

3. **HandlerMapping 查路由**

   * 看所有 `@Controller`
   * 找有没有 `@RequestMapping("/hello")`

4. **调用对应方法**

   ```java
   hi()
   ```

5. **处理返回值**

   * 有 `@ResponseBody` → 直接返回数据
   * 没有 → 当作“页面名”去解析

---

# 🧪 二、3 道超高频易错题（面试 & 新手必踩）

## ❌ 易错题 1：@Controller + 返回 String，一定是字符串吗？

### 代码

```java
@Controller
@RequestMapping("/test")
public class TestController {

    @RequestMapping("/hello")
    public String hello() {
        return "hello";
    }
}
```

### 问题：浏览器会看到什么？

### ❌ 错误理解

```
hello
```

### ✅ 正确答案

👉 Spring 会去找：

```text
hello.html / hello.jsp
```

**原因**：

> 没有 `@ResponseBody`，Spring 认为你返回的是 **视图名**

---

## ❌ 易错题 2：@RestController 还要不要 @ResponseBody？

### 代码

```java
@RestController
public class HelloController {

    @RequestMapping("/hello")
    public String hi() {
        return "Hello";
    }
}
```

### 问题：需要加 @ResponseBody 吗？

### ✅ 正确答案

❌ **不需要**

### 原因（一句话记住）

```text
@RestController = @Controller + @ResponseBody
```

---

## ❌ 易错题 3：返回对象一定是 JSON 吗？

### 代码

```java
@RestController
public class UserController {

    @RequestMapping("/user")
    public User getUser() {
        return new User("Tom", 18);
    }
}
```

### 问题：为什么浏览器看到 JSON？

### 正确原因（不是 Spring “天生懂 JSON”）

```text
@ResponseBody
  ↓
HttpMessageConverter
  ↓
Jackson 把对象转成 JSON
```

⚠️ 如果 **没有 Jackson 依赖** → 会直接报错

---

# 🔁 三、Controller 返回 String vs JSON（核心对比）

## 情况 1：返回 String（页面 or 文本）

### A️⃣ 返回「页面名」

```java
@Controller
public class PageController {

    @RequestMapping("/index")
    public String index() {
        return "index";
    }
}
```

| 条件              | 结果            |
| --------------- | ------------- |
| 无 @ResponseBody | 去找 index.html |
| 适合              | MVC / 前后端不分离  |

---

### B️⃣ 返回「文本」

```java
@Controller
public class TextController {

    @RequestMapping("/hi")
    @ResponseBody
    public String hi() {
        return "Hi";
    }
}
```

| 条件              | 结果         |
| --------------- | ---------- |
| 有 @ResponseBody | 浏览器直接显示 Hi |
| 适合              | 接口 / 测试    |

---

## 情况 2：返回 JSON（最常见）

```java
@RestController
public class ApiController {

    @RequestMapping("/user")
    public User user() {
        return new User("Alice", 20);
    }
}
```

浏览器看到：

```json
{
  "name": "Alice",
  "age": 20
}
```

### 本质流程

```text
Java 对象
  ↓
@ResponseBody
  ↓
HttpMessageConverter
  ↓
JSON
```

---

# 🧠 一句话终极总结（非常重要）

> **Spring Controller 的本质是：
> “URL → 方法 → 返回值 → 如何被解释”**

| 注解              | 决定什么   |
| --------------- | ------ |
| @Controller     | 这是控制器  |
| @RequestMapping | URL 找谁 |
| @ResponseBody   | 返回值怎么用 |
| @RestController | 默认返回数据 |

---

如果你愿意，下一步我可以继续帮你：

* 🔍 **逐行拆 DispatcherServlet 源码级流程**
* 🧩 **对比 @GetMapping / @PostMapping**
* 🧪 **写 5 个“看代码猜返回结果”的训练题（秒杀面试）**
