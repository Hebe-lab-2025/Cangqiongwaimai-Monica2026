这条 `curl` 一看就知道为什么不工作了：**你没带 JWT token，刚好又打在 `/admin/**` 上，所以一定被拦截器挡住。**

```bash
curl -X POST \
  -H "Accept:*/*" \
  -H "Content-Type:application/json" \
  -d "{\"id\":0,\"idNumber\":\"23\",\"name\":\"M\",\"phone\":\"\",\"sex\":\"\",\"username\":\"\"}" \
  "http://localhost:8080/admin/employee"
```

---

## 1. 这条请求在后端的真实流程

请求路径：`/admin/employee`（新增员工）

后端链路大概是：

1. 进入 `DispatcherServlet`

2. 命中拦截器 `JwtTokenAdminInterceptor`（`addPathPatterns("/admin/**")`）

3. 在 `preHandle` 里取 token：

   ```java
   String token = request.getHeader(jwtProperties.getAdminTokenName());
   log.info("jwt校验:{}", token);   // 你的日志里就是：jwt校验:null
   ```

4. 因为这条 curl **完全没有带任何 token 相关的 header**，所以这里拿到的是 `null`：

   * 日志打印：`jwt校验:null`
   * 然后你现在的代码，大概率就是：

     * 要么直接 `return false;`（没有写任何响应 body → `content-length: 0`）
     * 要么 `parseJWT(secret, null)` 抛异常（可能变成 500）

这就是你看到：

* 日志一堆 `jwt校验:null`
* 浏览器 / curl 返回：

  * `content-length: 0`
  * 或 500

---

## 2. 正确姿势：先登录拿 token，再带着 token 调这个接口

### 第一步：用 curl 登录拿 token

一般登录接口是 `/admin/employee/login`，JSON 形如：

```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}' \
  "http://localhost:8080/admin/employee/login"
```

返回大概是这种结构（示例）：

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "id": 1,
    "name": "管理员",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.xxx.yyy"
  }
}
```

👉 把 `data.token` 复制出来，假设叫 `YOUR_TOKEN_HERE`。

### 第二步：带上 token 再调 `/admin/employee`

关键是：**header 名称要和后端配置一致。**

在黑马这套「苍穹外卖」里，`application-dev.yml` 一般有类似：

```yaml
sky:
  jwt:
    admin-token-name: token   # 这个就是拦截器里 getHeader 用到的名字
```

如果是 `token`，那么 curl 应该这样写：

```bash
curl -X POST \
  -H "Accept:*/*" \
  -H "Content-Type: application/json" \
  -H "token: YOUR_TOKEN_HERE" \
  -d '{"id":0,"idNumber":"23","name":"M","phone":"","sex":"","username":""}' \
  "http://localhost:8080/admin/employee"
```

如果你配置的是 `Authorization` 之类的，就改成：

```bash
-H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## 3. 再顺带说一句：拦截器别返回“空气响应”

就算没带 token，你也最好**不要只 `return false;` 什么都不写**，否则就会出现你刚才那种：

* `content-length: 0`
* 浏览器/前端完全没法知道发生了什么

建议在 `JwtTokenAdminInterceptor.preHandle()` 里写成这样（伪代码，思路）：

```java
String token = request.getHeader(jwtProperties.getAdminTokenName());
log.info("jwt校验:{}", token);

if (token == null || token.trim().isEmpty()) {
    // 统一返回“未登录”JSON
    Result<?> result = Result.error(MessageConstant.NOT_LOGIN);
    String json = JSONObject.toJSONString(result);

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write(json);
    return false;
}

// 解析 token，异常也要捕获并返回 JSON
try {
    Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
    Long empId = Long.valueOf(claims.get("empId").toString());
    BaseContext.setCurrentId(empId);
    return true;
} catch (Exception e) {
    Result<?> result = Result.error(MessageConstant.NOT_LOGIN);
    String json = JSONObject.toJSONString(result);

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write(json);
    return false;
}
```

这样：

* 没有 token / token 无效 → 前端收到清晰的 JSON：`NOT_LOGIN`
* 有 token 且合法 → 正常进入 Controller，新增员工成功/失败都有统一返回。

---

## 4. 你现在可以做的快速验证

1. 用 curl 登录 `/admin/employee/login`，确认能拿到 token。
2. 拿着 token，再用 curl POST `/admin/employee`，在 header 里加上对应的 token 头。
3. 观察：

   * 后端日志是不是不再打印 `jwt校验:null`，而是 `jwt校验:xxx.yyy.zzz`。
   * curl 响应是不是再也不是 `content-length: 0`，而是 JSON。

如果你不确定 header 名称是 `token` 还是 `Authorization`，可以把 `application-dev.yml` 里 `jwt` 那一段贴出来，我帮你对一下，然后给你一条完全复制即用的 curl 命令。
