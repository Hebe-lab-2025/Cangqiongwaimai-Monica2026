很好，这个 JSON 说明：**登录已经成功 + token 生成正常** ✅

```json
{
  "code": 1,
  "data": {
    "id": 1,
    "userName": "admin",
    "name": "管理员",
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJlbXBJZCI6MSwiZXhwIjoxNzY3ODQ3NTEyfQ.o8AHPFZJDWjiJnCuKX9SxfVUkXjvqqSW6_aXOVznnuE"
  }
}
```

接下来只差一步：**用这个 token 调 `/admin/employee` 等接口**。

---

## 1. 用 curl 带 token 再调一次「新增员工」

先把 token 存成一个变量（只是为了好看）：

```bash
TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJlbXBJZCI6MSwiZXhwIjoxNzY3ODQ3NTEyfQ.o8AHPFZJDWjiJnCuKX9SxfVUkXjvqqSW6_aXOVznnuE"
```

如果你在 `application-dev.yml` 里看到类似配置：

```yaml
sky:
  jwt:
    admin-token-name: token
```

那么 curl 应该这样写（**重点是这个 header** 👇）：

```bash
curl -X POST \
  -H "Accept:*/*" \
  -H "Content-Type: application/json" \
  -H "token: $TOKEN" \
  -d '{"id":0,"idNumber":"23","name":"M","phone":"","sex":"","username":""}' \
  "http://localhost:8080/admin/employee"
```

> 这样后端拦截器里：
>
> ```java
> request.getHeader(jwtProperties.getAdminTokenName())
> ```
>
> 能拿到刚才的 token，就不会再打印 `jwt校验:null` 了。

---

## 2. 如果 header 名不是 `token`

如果你在 yml 里看到的是：

```yaml
admin-token-name: Authorization
```

或者别的名字，比如 `Authorization`，那就改成：

```bash
-H "Authorization: $TOKEN"
# 或者你自己在代码里写的是 "Bearer " + token，就用：
# -H "Authorization: Bearer $TOKEN"
```

**规则只有一条：**

> `curl` 里 header 的名字，要和 `jwtProperties.getAdminTokenName()` 对应的配置一模一样。

---

## 3. 你可以自己快速验证是否 OK

1. 重新 curl 登录，拿最新 token。
2. 用上面带 token 的 curl 调 `/admin/employee`。
3. 看后台日志：

   * 不再是 `jwt校验:null`，而是 `jwt校验: eyJhbGciOiJI...`。
4. curl 返回：

   * 不再是 `content-length: 0`，而是一个 JSON（成功 或 业务错误）。

你如果把 `application-dev.yml` 里 `sky.jwt` 那小段贴出来，我可以直接帮你写一条「完全复制即可用」的 curl 命令。
