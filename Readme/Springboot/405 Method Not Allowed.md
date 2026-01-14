## ❌ 405 Method Not Allowed —— 你这个错误**为什么会发生 & 怎么修**

```json
{
  "timestamp": 1768215989371,
  "status": 405,
  "error": "Method Not Allowed",
  "path": "/admin/employee/logout"
}
```

---

## 🧠 一句话结论（先记住）

> **URL 是对的，但你用的 HTTP 方法不对（GET / POST / PUT / DELETE 不匹配）**。

---

## 🔍 405 在 Spring 里到底是什么意思？

**Spring 能找到这个路径 `/admin/employee/logout`，但：**

* Controller 里 **没有这个 HTTP 方法的映射**
* 比如：

  * 前端用的是 **POST**
  * 后端只写了 **GET**

➡️ Spring 返回 **405 Method Not Allowed**

---

## 🧭 Spring 路由匹配规则（核心）

Spring 匹配的是 **二元组**：

```
(HTTP Method, URL Path)
```

不是只看 Path。

```
GET  /admin/employee/logout   ❌
POST /admin/employee/logout   ✅
```

---

## 🧪 最常见 4 种触发原因（99% 命中）

### 1️⃣ 前端方法 ≠ 后端注解（最常见）

**前端**

```http
POST /admin/employee/logout
```

**后端**

```java
@GetMapping("/admin/employee/logout") // ❌
```

✅ **修复**

```java
@PostMapping("/admin/employee/logout")
```

---

### 2️⃣ Controller 写的是 POST，你用浏览器直接访问

浏览器地址栏 = **GET**

```java
@PostMapping("/admin/employee/logout")
```

❌ 浏览器直接访问 → 405
✅ 用 Postman / 前端请求

---

### 3️⃣ 写成了 REST 风格，但用错方法

```java
@PostMapping("/admin/employee/logout")
```

但前端用：

```http
GET /admin/employee/logout
```

👉 **logout 是“状态变更”**，就应该是 POST / DELETE

---

### 4️⃣ 被 Spring Security 拦了一半（次常见）

* Security 允许访问
* 但 **方法不匹配**
* 仍然返回 405（不是 401/403）

---

## 🧠 快速自查清单（按顺序）

1. ✅ 前端到底用的什么方法？（Network 面板）
2. ✅ Controller 注解是什么？`@GetMapping / @PostMapping`
3. ✅ 路径有没有前缀（`/admin`）写错
4. ✅ 是否真的进了这个 Controller（日志）

---

## 🧠 正确示例（推荐）

### 后端

```java
@PostMapping("/admin/employee/logout")
public Result logout() {
    return Result.success();
}
```

### 前端

```http
POST /admin/employee/logout
```

---

## 🧠 ASCII 路由匹配示意图

```
Request:  POST /admin/employee/logout
            |
            v
HandlerMapping
   |
   |-- 查找 (/admin/employee/logout)
           |
           |-- GET ?  ❌
           |-- POST ? ✅
                    |
                    v
            执行 Controller 方法
```

---

## 🎯 面试 / 实战金句

> **405 = 路径存在，但 HTTP 方法不被允许**。
> 404 是“找不到路”，405 是“路对了，走法不对”。

---

如果你愿意，下一步我可以：

* 🔍 **结合你现有 Controller 代码逐行帮你对**
* 🧪 **405 vs 401 vs 403 vs 404 一张对照表**
* ⚔️ **logout 接口到底该用 POST 还是 DELETE（REST 规范）**

你直接把 **Controller 代码贴出来**，我可以精准定位是哪一行导致的。
