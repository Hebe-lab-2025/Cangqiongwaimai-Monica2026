## `@RequestBody` —— 请求体绑定（中文速记版）

![Image](https://tutorialspedia.com/wp-content/uploads/2019/09/Spring-MVC-Flow-Diagram.png)

![Image](https://i.sstatic.net/VXTMm.png)

![Image](https://scaler.com/topics/images/requestbody.webp)

---

### 1️⃣ 它解决什么问题？

把 **HTTP 请求体（JSON / XML）** 自动解析成 **Java 对象**，省去手动取参数、反序列化的麻烦。

---

### 2️⃣ 典型用法

```java
@PostMapping("/login")
public Result login(@RequestBody LoginDTO dto) {
    return service.login(dto);
}
```

**前端请求（JSON）：**

```json
{
  "username": "admin",
  "password": "123456"
}
```

---

### 3️⃣ 底层执行流程（一句话）

```
HTTP Body(JSON)
   ↓
HttpMessageConverter（Jackson）
   ↓
Java Object（DTO）
```

> 不是反射直接搞，是 **HttpMessageConverter + Jackson**。

---

### 4️⃣ `@RequestBody` vs `@RequestParam`（必考）

| 注解              | 来源           | 常见场景            |
| --------------- | ------------ | --------------- |
| `@RequestBody`  | **请求体 body** | POST / PUT JSON |
| `@RequestParam` | URL 参数 / 表单  | ?id=1           |
| `@PathVariable` | URL 路径       | /users/1        |

---

### 5️⃣ 高频踩坑（直接背）

❌ **GET 请求 + @RequestBody**

* 大多数客户端不支持（HTTP 规范不推荐）

❌ **忘记加 `Content-Type: application/json`**
→ 415 Unsupported Media Type

❌ **JSON 字段名 ≠ DTO 字段名**
→ 绑定失败（可用 `@JsonProperty`）

❌ **Controller 方法参数没加注解**

```java
public void save(UserDTO dto) // ❌ 不会自动从 body 解析
```

---

### 6️⃣ 是否必须？

* **只有一个对象参数**：**必须加**
* Spring 不会“猜”你想从 body 取

---

### 7️⃣ 校验组合（实战必会）

```java
@PostMapping("/register")
public void register(@Valid @RequestBody UserDTO dto) {}
```

* `@Valid`：触发参数校验
* 失败 → 抛 `MethodArgumentNotValidException`

---

### 8️⃣ 面试一句话标准答

> `@RequestBody` 用于将 HTTP 请求体中的 JSON 数据通过 HttpMessageConverter 自动反序列化为 Java 对象，常用于 POST/PUT 接口。

---

### 9️⃣ 常见追问秒答

* **Q：它和 `@ResponseBody` 有什么关系？**
  👉 一个是 **入参反序列化**，一个是 **出参序列化**
* **Q：多个 `@RequestBody` 参数行不行？**
  ❌ 不行，一个请求体只能绑定一次
* **Q：文件上传能用吗？**
  ❌ 不能，用 `@RequestPart` / `MultipartFile`

---

如果你愿意，下一步我可以：

* 🧠 画一张 **HTTP → Controller 参数绑定完整流程 ASCII 图**
* 🧪 出 **5 道 @RequestBody / @RequestParam 混淆题**
* 🔍 深挖 **HttpMessageConverter + Jackson 原理**

你点一个。
