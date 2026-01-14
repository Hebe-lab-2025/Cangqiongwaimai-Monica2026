## 🌐 请求参数（Request Parameters） vs Query 参数（Query String）

![Image](https://www.realisable.co.uk/support/documentation/Resources/Images/UG/2.IMan%20Data%20Concepts/Web%20Request-GET.png)

![Image](https://i.sstatic.net/shLT8.png)

![Image](https://www.baeldung.com/wp-content/uploads/sites/4/2023/01/Cache-GET-POST.jpg)

---

## 1️⃣ 先给结论（一句话版）

> **Query 参数是请求参数的一种**，专指 **URL `?` 后面的参数**；
> **请求参数**是更大的概念，**包含 query、表单参数等**。

---

## 2️⃣ Query 参数（Query String）是什么？

**位置：** URL 中 `?` 后面
**格式：**

```
/users?page=1&size=20
```

**特点**

* 可见（URL 明文）
* 通常用于 **GET**
* 用于 **查询 / 过滤 / 分页**

**Spring 用法**

```java
@GetMapping("/users")
public List<User> list(
    @RequestParam int page,
    @RequestParam int size
) {}
```

---

## 3️⃣ 请求参数（Request Parameters）是什么？

**广义概念，包括：**

1. **Query 参数**（URL 中）
2. **表单参数**（`application/x-www-form-urlencoded`）
3. **部分 multipart 表单字段**

```http
POST /login
Content-Type: application/x-www-form-urlencoded

username=admin&password=123456
```

```java
@PostMapping("/login")
public void login(@RequestParam String username,
                  @RequestParam String password) {}
```

---

## 4️⃣ Query 参数 vs RequestBody（必考对比）

| 维度        | Query 参数        | `@RequestBody` |
| --------- | --------------- | -------------- |
| 位置        | URL             | HTTP Body      |
| 是否可见      | ✅               | ❌              |
| 常见方法      | GET             | POST / PUT     |
| 数据结构      | 简单 KV           | 复杂对象 / JSON    |
| Spring 注解 | `@RequestParam` | `@RequestBody` |

📌 **口诀：**

> **查用 Query，改用 Body**

---

## 5️⃣ 常见误区（高频）

❌ 把 JSON 放在 query
❌ GET + `@RequestBody`
❌ 复杂对象用 query（URL 太长）
❌ 忘了 `@RequestParam(required = false)`

---

## 6️⃣ 是否必须写 `@RequestParam`？

```java
@GetMapping("/test")
public void test(String name) {}
```

* ✔️ 默认按 **request parameter（含 query）** 绑定
* ❗ 面试/规范中 **建议显式写注解**

---

## 7️⃣ 面试一句话标准答

> Query 参数是请求参数的一种，位于 URL 中，常用于查询条件；请求参数是更广义的概念，还包括表单参数等，而复杂数据通常放在请求体中用 `@RequestBody` 接收。

---

## 8️⃣ 秒答追问

* **Q：Query 参数安全吗？**
  ❌ 不安全，URL 可被日志/浏览器记录
* **Q：分页为什么用 query？**
  👉 语义是“查询”，且天然幂等
* **Q：能混用吗？**
  ✅ 可以（query + body）

---

如果你愿意，下一步我可以：

* 🧠 **HTTP → Spring 参数绑定完整 ASCII 图**
* 🧪 **5 道 Query / Body 混淆面试题**
* ⚔️ **GET vs POST 参数设计规范表**

你点一个。
