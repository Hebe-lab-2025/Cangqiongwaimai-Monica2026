![Image](https://mkyong.com/wp-content/uploads/2021/07/spring-boot-hello-world-project.png)

![Image](https://i2.wp.com/springframework.guru/wp-content/uploads/2017/09/mvc.png)

![Image](https://miro.medium.com/1%2AQSOWRm1PCMAfupeZnjJNPw.png)

下面**按“零基础（O基础）”**给你把这 **3 个 Spring Boot 注解**讲清楚：
👉 **@Controller、@RequestMapping、@ResponseBody**

---

## 一、@Controller ——「我是控制器」

```java
@Controller
public class HelloController {
}
```

### 它是干嘛的？

* 告诉 Spring：**这个类是用来接收 HTTP 请求的**
* Spring 启动时会扫描它，并把它交给 **DispatcherServlet（前端控制器）** 管理

### 用人话说

> **@Controller = 这个类专门处理浏览器/客户端的请求**

### 没它会怎样？

* Spring 不会识别这个类
* 里面的 `@RequestMapping` **完全失效**

---

## 二、@RequestMapping ——「路径 → 方法」

```java
@RequestMapping("/hello")
String hi() {
    return "Hello, world";
}
```

### 它是干嘛的？

* **把一个 URL 路径映射到一个 Java 方法**
* 当用户访问 `/hello`，Spring 就会调用 `hi()` 方法

### 用人话说

> **@RequestMapping = 访问哪个 URL，就执行哪个方法**

### 常见访问流程

```text
浏览器访问：http://localhost:8080/hello
        ↓
@RequestMapping("/hello")
        ↓
hi() 方法被调用
```

### 小补充（你现在不用深究）

* 它还能限制 HTTP 方法（GET / POST）
* 但你现在写的等价于：

```java
@RequestMapping(value="/hello", method=GET)
```

---

## 三、@ResponseBody ——「直接返回数据」

```java
@ResponseBody
String hi() {
    return "Hello, world";
}
```

### 它是干嘛的？

* 告诉 Spring：
  **不要去找页面（JSP/HTML），直接把返回值写到 HTTP 响应体里**

### 用人话说

> **@ResponseBody = 方法返回什么，浏览器就看到什么**

### 有它 vs 没它的区别（⚠️ 非常重要）

#### ✅ 有 @ResponseBody

```java
@ResponseBody
String hi() {
    return "Hello";
}
```

浏览器看到：

```
Hello
```

#### ❌ 没有 @ResponseBody

```java
String hi() {
    return "Hello";
}
```

Spring 会以为：

```
我要去找一个叫 Hello.html / Hello.jsp 的页面
```

👉 **页面不存在就报错**

---

## 四、三者合在一起怎么理解（整条链路）

```java
@Controller
public class HelloController {

    @RequestMapping("/hello")
    @ResponseBody
    String hi() {
        return "Hello, world";
    }
}
```

### 一步一步发生了什么？

1. `@Controller`
   → Spring 识别这是一个控制器类
2. `@RequestMapping("/hello")`
   → `/hello` 请求交给 `hi()` 方法
3. `@ResponseBody`
   → 返回的字符串直接写到 HTTP 响应里
4. 浏览器最终看到：

```
Hello, world
```

---

## 五、再看你代码里的 `/bye`

```java
@RequestMapping("/bye")
@ResponseBody
String bye() {
    return "Bye Bye";
}
```

访问：

```
http://localhost:8080/bye
```

浏览器显示：

```
Bye Bye
```

👉 **同一个 Controller，可以处理多个 URL**

---

## 六、一句话速记（面试 / 学习用）

> * **@Controller**：声明控制器
> * **@RequestMapping**：URL → 方法
> * **@ResponseBody**：返回值就是响应内容

---

## 七、进阶提前知道一点（不要求现在掌握）

以后你会看到：

```java
@RestController
```

它等价于：

```java
@Controller
@ResponseBody
```

👉 **现在你学的是“拆开版”，这是好事**

---

如果你愿意，我可以下一步帮你：

* 🧠 用 **HTTP 请求 → Spring 内部执行流程图**讲清楚
* 🧪 出 3 道 **@Controller / @RestController / @ResponseBody 易错题**
* 🔁 对比 **Controller 返回 String vs JSON 的区别**



