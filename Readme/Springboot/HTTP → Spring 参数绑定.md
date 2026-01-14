下面一次性给你 **3 件成品**，全部是**面试可直接背 + 工程真实流程**。

---

## 🧠 HTTP → Spring 参数绑定【完整 ASCII 流程图】

```
┌─────────────────────────────┐
│        HTTP Request         │
├─────────────────────────────┤
│ Method: GET / POST / PUT    │
│ URL: /users?page=1&size=20  │◄──── Query String
│ Headers:                    │
│   Content-Type              │
│ Body:                       │◄──── JSON / Form / Multipart
└───────────────┬─────────────┘
                │
                ▼
┌─────────────────────────────┐
│     DispatcherServlet       │
│   （Spring MVC 入口）       │
└───────────────┬─────────────┘
                │
                ▼
┌─────────────────────────────┐
│  HandlerMapping 找 Controller│
└───────────────┬─────────────┘
                │
                ▼
┌────────────────────────────────────────────┐
│ HandlerAdapter（参数解析核心）              │
│                                            │
│ 1️⃣ Query / Form 参数                       │
│   ├─ @RequestParam                          │
│   └─ 默认参数（无注解）                    │
│                                            │
│ 2️⃣ Path 参数                               │
│   └─ @PathVariable                          │
│                                            │
│ 3️⃣ Request Body                            │
│   └─ @RequestBody                           │
│       └─ HttpMessageConverter               │
│           └─ Jackson(JSON → Object)         │
│                                            │
│ 4️⃣ 校验                                    │
│   └─ @Valid / @Validated                    │
└───────────────┬────────────────────────────┘
                │
                ▼
┌─────────────────────────────┐
│   Controller 方法执行       │
└───────────────┬─────────────┘
                │
                ▼
┌─────────────────────────────┐
│   返回值处理                │
│   └─ @ResponseBody          │
│       └─ Jackson(Object→JSON)│
└─────────────────────────────┘
```

📌 **面试金句**：

> Query/Form 由参数解析器处理，Body 由 `HttpMessageConverter` 处理。

---

## 🧪 5 道 Query / Body 混淆面试题（含标准答案）

### ❓ 1. GET 能不能用 `@RequestBody`？

**答：** 规范不推荐，大多数客户端不支持，Spring 默认也不建议。

---

### ❓ 2. 一个接口能有多个 `@RequestBody` 吗？

**答：** ❌ 不行，一个 HTTP 请求只有一个 body。

---

### ❓ 3. JSON 放在 URL 里行不行？

**答：** ❌ 不推荐：URL 长度限制、编码复杂、安全性差。

---

### ❓ 4. `@RequestParam` 能接收 POST 吗？

**答：** ✅ 可以，只要是 **form 表单参数**。

---

### ❓ 5. Controller 方法参数不写注解会怎样？

```java
public void test(String name) {}
```

**答：** Spring 会按 **request parameter（query / form）** 绑定，不会从 body 解析。

---

## ⚔️ GET vs POST 参数设计规范表（工程级）

| 维度    | GET             | POST           |
| ----- | --------------- | -------------- |
| 语义    | 查询              | 创建 / 修改        |
| 参数位置  | Query String    | Body           |
| 是否幂等  | ✅               | ❌              |
| 是否可缓存 | ✅               | ❌              |
| 参数长度  | 有限制             | 理论无限           |
| 安全性   | ❌（URL 可见）       | 相对安全           |
| 常见注解  | `@RequestParam` | `@RequestBody` |
| 典型场景  | 列表、搜索、分页        | 新增、登录、提交表单     |

📌 **黄金口诀（直接背）**

> **查 GET + Query，改 POST + Body**
> **简单参数用 Query，复杂对象用 Body**

---

## 🎯 面试终极一句话总结

> Spring 会根据参数来源自动选择解析器：Query/Form 用参数解析器，Body 用 HttpMessageConverter，GET 负责查询，POST 负责状态变更。

---

如果你愿意，我可以下一步帮你：

* 🧠 **GET + POST + PUT + DELETE 参数规范一页速记卡**
* 🧪 **Spring MVC 参数绑定源码级追问**
* ⚔️ **为什么分页一定放 Query（REST 设计）**

直接点一个。
