## ⚔️ `@GetMapping("/users")` vs `@PostMapping("/users")`（同一路径，不同语义）

![Image](https://i.sstatic.net/wCZC0.png)

![Image](https://miro.medium.com/v2/resize%3Afit%3A1400/1%2AOxxoNfhx205kCORqa4qtVw.png)

---

## 1️⃣ 一句话先给结论（面试版）

> **同一个 `/users`，GET 是“查用户”，POST 是“新建用户”。**

---

## 2️⃣ 语义层面对比（REST 核心）

| 项目      | `@GetMapping("/users")` | `@PostMapping("/users")` |
| ------- | ----------------------- | ------------------------ |
| HTTP 方法 | GET                     | POST                     |
| 语义      | 获取用户集合                  | 创建新用户                    |
| 是否修改数据  | ❌                       | ✅                        |
| 是否幂等    | ✅                       | ❌                        |
| 是否可缓存   | ✅                       | ❌                        |
| 常见返回    | List / Page             | 创建结果 / 新资源               |

---

## 3️⃣ 参数来源对比（Spring MVC）

### GET：Query 参数

```java
@GetMapping("/users")
public List<User> list(
    @RequestParam int page,
    @RequestParam int size
) {}
```

```
GET /users?page=1&size=20
```

---

### POST：Request Body

```java
@PostMapping("/users")
public User create(@RequestBody UserDTO dto) {}
```

```
POST /users
Body: { "name": "Alice", "email": "a@x.com" }
```

---

## 4️⃣ 为什么可以同一路径？

Spring 是用 **HTTP Method + Path** 一起做路由的：

```
(GET,  /users)  → listUsers()
(POST, /users)  → createUser()
```

👉 方法不同，不冲突。

---

## 5️⃣ 面试官常追问（秒答）

### ❓ 为什么不用 `/createUser`？

> REST 资源导向，动作由 **HTTP 方法表达**，路径表示资源。

---

### ❓ 能不能用 POST 查数据？

> 技术上可以，但**语义不推荐**，会影响缓存、幂等和可读性。

---

### ❓ 分页为什么放 GET？

> 查询语义 + 幂等 + 可缓存。

---

### ❓ 创建成功返回什么？

> 常见返回 **201 Created + Location 头**。

---

## 6️⃣ 设计口诀（直接背）

> **集合路径用 GET 查，用 POST 建；
> 单资源路径 `/users/{id}` 用 GET/PUT/DELETE。**

---

## 7️⃣ 面试一句话标准答

> `@GetMapping("/users")` 用于查询用户资源集合，参数通常来自 query；`@PostMapping("/users")` 用于创建新用户，数据来自请求体，同一路径通过不同 HTTP 方法区分语义。

---

如果你愿意，我可以继续给你：

* ⚔️ **`/users/{id}`：GET vs PUT vs DELETE 对照**
* 🧪 **REST 接口设计 10 道面试陷阱**
* 🧠 **Spring MVC 路由匹配完整流程图**

你点一个。
