![Image](https://media.geeksforgeeks.org/wp-content/uploads/20210829120918/externalcalltocountriesapi.JPG)

![Image](https://media.geeksforgeeks.org/wp-content/uploads/20250828113913303626/introduction-to-Spring-Boot.webp)

![Image](https://media.geeksforgeeks.org/wp-content/uploads/20220305144809/SpringResponseBodyAnnotation.JPG)

下面我**严格按你这张图**，用「**前端 → URL → Spring → 返回 → 页面更新**」的顺序，把**整条链路一次讲透**，**0 基础也能懂**。

---

# 🧠 一、你这张图整体在干什么？（一句话版）

> **浏览器点按钮 → JS 发送 HTTP 请求 → Spring Controller 接参 → 返回字符串 → JS 把结果显示到页面**

---

# 🧩 二、前端部分（HTML + JS）在干嘛？

## 1️⃣ 输入框 + 按钮（浏览器里你看到的）

```html
<input id="n">
<button id="b">say hello</button>
<h4></h4>
```

### 含义

* `input#n`：用户输入名字（比如：王五）
* `button#b`：点击触发请求
* `h4`：显示服务器返回结果

---

## 2️⃣ JS：点击按钮 → 发请求（**最关键**）

```js
document.querySelector("#b").onclick = function () {
```

👉 给按钮 **绑定点击事件**

---

```js
var n = document.querySelector("#n").value;
```

👉 从输入框里**取用户输入的值**

> 用户输入：`王五`
> 现在 JS 变量 `n = "王五"`

---

```js
fetch("http://localhost:8080/hello?name=" + n)
```

👉 **拼 URL 并发送 HTTP 请求**

实际请求是：

```
GET /hello?name=王五
```

⚠️ 这一步非常重要：

* `?name=王五` 是 **URL 查询参数**
* Spring 就是从这里拿到 `name`

---

```js
.then(resp => resp.text())
```

👉 把服务器返回的 **HTTP 响应体** 转成字符串

---

```js
.then(text => {
  document.querySelector("h4").innerText = text;
})
```

👉 把返回结果写进页面 `<h4>`

---

# 🚀 三、后端 Spring Controller 在干嘛？

## 1️⃣ Controller 本身

```java
@Controller
public class HelloController {
```

👉 告诉 Spring：

> **这是一个 Controller，要处理 HTTP 请求**

---

## 2️⃣ 请求映射（URL → 方法）

```java
@RequestMapping("/hello")
```

👉 意思是：

```
只要访问 /hello
就执行下面这个方法
```

---

## 3️⃣ @ResponseBody（最容易混）

```java
@ResponseBody
```

👉 意思是：

> **方法返回值直接写入 HTTP 响应体，不走页面**

没有它会发生什么？
👉 Spring 会以为你返回的是 **页面名**

---

## 4️⃣ 参数是怎么进来的？（重点）

```java
String hi(String name)
```

### 关键点（一定要记住）

```text
URL 参数名  ==  方法参数名
```

请求：

```
/hello?name=王五
```

Spring 自动做了这件事：

```java
String name = "王五";
```

⚠️ 这叫 **参数绑定（Data Binding）**

---

## 5️⃣ 返回结果

```java
return "Hello, " + name;
```

👉 返回：

```
Hello, 王五
```

---

# 🔄 四、完整执行链路（一步不漏）

```text
1. 用户在浏览器输入：王五
2. 点击 say hello
3. JS 执行 fetch：
   GET /hello?name=王五
4. DispatcherServlet 接收请求
5. 找到 @RequestMapping("/hello")
6. 调用 hi("王五")
7. 返回 "Hello, 王五"
8. @ResponseBody 写入 HTTP Response
9. JS 收到响应
10. 更新 <h4> 显示结果
```

---

# ❗ 五、3 个「必踩坑」对照你这张图

## ❌ 坑 1：去掉 @ResponseBody 会怎样？

```java
return "Hello, 王五";
```

Spring 会当成：

```
视图名 = "Hello, 王五"
```

👉 去找 `Hello, 王五.html`
👉 **直接报错**

---

## ❌ 坑 2：参数名不一致

```java
String hi(String username)
```

但 URL 是：

```
/hello?name=王五
```

👉 `username = null`
👉 返回：`Hello, null`

---

## ❌ 坑 3：中文乱码？

如果你看到：

```
Hello, %E7%8E%8B%E4%BA%94
```

需要：

* 前端 `encodeURIComponent(n)`
* 或 Spring 配 UTF-8（Spring Boot 默认 OK）

---

# 🧠 六、一句话终极理解（非常重要）

> **Spring Controller 就是：
> “把 URL 参数 → 变成 Java 方法参数 → 返回结果给前端”**

---

如果你愿意，我可以下一步帮你：

* 🔍 **画一张「前端 fetch ↔ Spring Controller」ASCII 架构图**
* 🧪 **出 5 道“给 URL 猜方法参数值”的练习题**
* 🔁 **对比 @RequestParam vs 直接写参数名（面试常问）**

你说继续哪个？
