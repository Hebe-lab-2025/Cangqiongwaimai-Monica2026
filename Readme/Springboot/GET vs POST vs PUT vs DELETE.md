## ⚔️ GET vs POST vs PUT vs DELETE —— 一张表（面试速记）

![Image](https://media.licdn.com/dms/image/v2/D4D12AQHxG4Prn4ZrBQ/article-cover_image-shrink_600_2000/article-cover_image-shrink_600_2000/0/1718998421506?e=2147483647\&t=-2LQBR1-lDFdKqTy66DNfGm4cTz7xy27k8-QYYsYaio\&v=beta)

![Image](https://miro.medium.com/v2/resize%3Afit%3A1400/1%2ASq-uCWp9l3gNgmjZqECnjw.png)

| 维度        | **GET**                | **POST**       | **PUT**       | **DELETE**       |
| --------- | ---------------------- | -------------- | ------------- | ---------------- |
| 语义        | 查询资源                   | 创建资源           | **整体更新 / 替换** | 删除资源             |
| 是否修改数据    | ❌                      | ✅              | ✅             | ✅                |
| 是否幂等      | ✅                      | ❌              | ✅             | ✅                |
| 是否可缓存     | ✅                      | ❌（默认）          | ❌             | ❌                |
| 参数位置      | Query                  | Body           | Body          | Path / Query     |
| 典型路径      | `/users` `/users/{id}` | `/users`       | `/users/{id}` | `/users/{id}`    |
| 成功状态码     | 200                    | **201**        | 200 / 204     | 204              |
| Spring 注解 | `@GetMapping`          | `@PostMapping` | `@PutMapping` | `@DeleteMapping` |
| 常见返回      | List / DTO             | 新资源 / Location | 更新后资源         | 无                |

📌 **黄金口诀**

> 查 GET；建 POST；改 PUT；删 DELETE
> 幂等：GET/PUT/DELETE；非幂等：POST

---

## 🧪 10 道 HTTP 方法混淆题（含秒答）

1️⃣ **POST 和 PUT 都能更新，怎么选？**
👉 **PUT** 用于“整体替换、幂等”；**POST** 用于“非幂等提交/创建”。

2️⃣ **PUT 能创建资源吗？**
👉 可以（若客户端知道资源 URI），但工程上更常用 **POST 创建**。

3️⃣ **DELETE 为什么是幂等的？**
👉 删除多次，最终状态一致（不存在）。

4️⃣ **GET 能不能带 Body？**
👉 规范不推荐，Spring/浏览器基本不支持。

5️⃣ **为什么分页用 GET？**
👉 查询语义 + 幂等 + 可缓存。

6️⃣ **POST 更安全吗？**
👉 ❌ 安全靠 **HTTPS**，不是方法名。

7️⃣ **PUT 和 PATCH 区别？**
👉 **PUT 整体替换**；PATCH **局部更新**（更细粒度）。

8️⃣ **DELETE 一定要有请求体吗？**
👉 不需要，通常用 Path（`/users/{id}`）。

9️⃣ **为什么不用 `/createUser` 这种路径？**
👉 REST 资源导向：**动作由方法表达**，路径表示资源。

🔟 **返回 204 代表什么？**
👉 成功但**无返回体**（常见于 PUT/DELETE）。

---

## 🎯 面试一句话终极总结

> HTTP 方法体现“对资源做什么”，路径体现“资源是什么”；幂等性和缓存能力是区分 GET/POST/PUT/DELETE 的关键。

如果你要，我可以再给你：

* 🧠 **PUT vs PATCH 深挖（含反例）**
* 🧪 **REST 反模式清单（面试陷阱）**
* 📄 **HTTP 状态码 × 方法 对照速记卡**
