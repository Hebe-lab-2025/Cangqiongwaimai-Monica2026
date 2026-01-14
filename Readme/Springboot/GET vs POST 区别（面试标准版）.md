## ⚔️ GET vs POST 区别（面试标准版）

![Image](https://miro.medium.com/1%2AxJEUm1hUvxppvcjY14IKQA.jpeg)

![Image](https://media.geeksforgeeks.org/wp-content/uploads/getpostRequest.png)

---

## 1️⃣ 一句话结论（先给）

> **GET 用来“查”，POST 用来“改 / 提交”。**

---

## 2️⃣ 核心区别对照表（必背）

| 维度    | GET               | POST           |
| ----- | ----------------- | -------------- |
| 语义    | 查询资源              | 创建 / 提交数据      |
| 参数位置  | URL（Query String） | HTTP Body      |
| 是否幂等  | ✅ 是               | ❌ 否            |
| 是否可缓存 | ✅ 是               | ❌ 默认不可         |
| 参数长度  | 有限制（URL 长度）       | 几乎无限           |
| 安全性   | ❌ 明文暴露在 URL       | 相对安全           |
| 浏览器刷新 | 可重复               | 会再次提交          |
| 常见注解  | `@RequestParam`   | `@RequestBody` |

---

## 3️⃣ HTTP 层面的本质区别（面试加分）

* **GET**：

  * 不应改变服务器状态
  * 可被浏览器 / CDN 缓存
* **POST**：

  * 会产生副作用
  * 通常不缓存

📌 不是“能不能带 body”的问题，而是 **语义约定**。

---

## 4️⃣ Spring MVC 中的区别

```java
@GetMapping("/users")
public List<User> list(@RequestParam int page) {}
```

```java
@PostMapping("/users")
public void create(@RequestBody UserDTO dto) {}
```

* GET：Query → 参数解析器
* POST：Body → HttpMessageConverter（Jackson）

---

## 5️⃣ 高频面试追问（秒答）

### ❓ GET 能带 Body 吗？

👉 **规范不禁止，但不推荐**，Spring/浏览器基本不支持。

---

### ❓ POST 更安全吗？

👉 ❌ **不是**，只是 URL 不可见；真正安全靠 **HTTPS**。

---

### ❓ 分页为什么用 GET？

👉 查询语义 + 幂等 + 可缓存。

---

### ❓ 登录为什么用 POST？

👉 会改变服务器状态，且敏感信息不应出现在 URL。

---

## 6️⃣ 设计口诀（直接背）

> **查 GET，改 POST；
> 简单参数 Query，复杂对象 Body；
> 安全靠 HTTPS，不靠方法名。**

---

## 7️⃣ 面试一句话标准答

> GET 用于获取资源，不应产生副作用，参数通过 URL 传递；POST 用于提交或创建资源，数据通过请求体传递，通常不具备幂等性。

---

如果你愿意，下一步我可以：

* ⚔️ **GET vs POST vs PUT vs DELETE 一张表**
* 🧪 **10 道 HTTP 方法混淆题**
* 🧠 **RESTful 接口设计规范速记卡**

你选一个。
