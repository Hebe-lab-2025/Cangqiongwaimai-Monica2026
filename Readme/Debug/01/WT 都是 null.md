先帮你读一眼这段日志，它其实在告诉你两件事：

1. **登录接口本身：查库是成功的**
2. **后面的所有接口：JWT 都是 `null`，很大概率在拦截器里直接挂掉，导致 500**

---

## 1. 登录这几行说明什么？

```text
2026-01-07 18:31:40.555  INFO  ... EmployeeController  : 员工登录：EmployeeLoginDTO(username=admin, password=123456)
2026-01-07 18:31:40.556 ERROR ... DruidAbstractDataSource   : discard long time none received connection. ... lastPacketReceivedIdleMillis : 130374
2026-01-07 18:31:40.565 DEBUG ... EmployeeMapper.getByUsername  : ==>  Preparing: select * from employee where username = ?
2026-01-07 18:31:40.565 DEBUG ... EmployeeMapper.getByUsername  : ==> Parameters: admin(String)
2026-01-07 18:31:40.577 DEBUG ... EmployeeMapper.getByUsername  : <==      Total: 1
```

* `EmployeeController`：说明 `/admin/employee/login` 被调用了，参数是 `admin / 123456`。
* `DruidAbstractDataSource discard long time none received connection`：

  * 含义：连接池发现某个连接**太久没用**（大概 130 秒），把它丢弃重建。
  * 这一般是**警告而不是根本错误**，后面你看 SQL 还是正常执行了。
* `Total: 1`：说明 `select * from employee where username = ?` 查到了 **1 条记录**，也就是数据库里确实有 `admin` 这条员工。

👉 结论：
**用户名查询是成功的，500 错误并不是“查不到 admin” 或数据库连不上导致的。**

---

## 2. 关键问题：JWT 校验一直是 `null`

后面大量日志都是这个：

```text
2026-01-07 18:32:31.829  INFO  ... JwtTokenAdminInterceptor : jwt校验:null
...
2026-01-07 18:34:21.012  INFO  ... JwtTokenAdminInterceptor : jwt校验:null
```

这说明：

* 每次访问后端接口时，`JwtTokenAdminInterceptor.preHandle()` 都打印了一句：

  ```java
  log.info("jwt校验:{}", token);
  ```
* 现在打印出来的是：`jwt校验:null`
  👉 从 `request` 里取到的 `token` 是 `null`。

**高概率的坑：**

1. **拦截器不判断 token 是否为 null，就直接解析：**

   典型错误写法（示意）：

   ```java
   String token = request.getHeader(jwtProperties.getAdminTokenName());
   log.info("jwt校验:{}", token);

   // ❌ token 为 null 还直接解析，会抛异常 -> 500
   Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
   Long empId = Long.valueOf(claims.get("empId").toString());
   BaseContext.setCurrentId(empId);
   return true;
   ```

   如果 `token == null`，`parseJWT` 里就会抛异常，Spring 最后给你一个 **500 Internal Server Error**。

2. **前端没有把 token 带上来**

   * 登录成功后，后端应该返回一个带 token 的 `EmployeeLoginVO`。
   * 前端应该把这个 token 存起来（localStorage / sessionStorage），之后每个请求加上请求头，例如：

     ```http
     Authorization: Bearer <token>
     ```

   如果前端**没加这个 header**，拦截器每次自然都拿到 `null`。

---

## 3. 建议你先检查这两个地方

### (1) 拦截器：必须先对 `null` 做处理

在 `JwtTokenAdminInterceptor.preHandle()` 里改成类似这样：

```java
@Override
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    String token = request.getHeader(jwtProperties.getAdminTokenName());
    log.info("jwt校验:{}", token);

    // 1. 先判断 token 是否为空
    if (token == null || token.trim().length() == 0) {
        // 对于未登录的情况，返回统一的错误 JSON，而不是直接 500
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401

        Result<?> result = Result.error(MessageConstant.NOT_LOGIN);
        String json = JSONObject.toJSONString(result);
        response.getWriter().write(json);
        return false; // 阻止继续进入 controller
    }

    // 2. 不为空再解析
    try {
        Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
        Long empId = Long.valueOf(claims.get("empId").toString());
        BaseContext.setCurrentId(empId);
        return true;
    } catch (Exception e) {
        // token 非法或过期，同样返回未登录
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Result<?> result = Result.error(MessageConstant.NOT_LOGIN);
        String json = JSONObject.toJSONString(result);
        response.getWriter().write(json);
        return false;
    }
}
```

### (2) 拦截器配置：排除登录接口

确保登录接口本身**不需要带 token**，所以要在配置类里排除掉：

```java
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns(
                        "/admin/employee/login",   // 登录
                        "/admin/common/**",        // 静态资源/上传等
                        "/doc.html", "/webjars/**", "/swagger-resources/**", "/v2/api-docs"
                );
    }
}
```

> 从你日志里看，`/admin/employee/login` 那几次并没有打印 `jwt校验:null`，说明大概率已经排除了登录路径，这个应该是 ok 的；真正的问题在于**登录之后的其他接口没有拿到 token 或者 token 解析时为 null**。

---

## 4. 再确认一下登录响应 / 前端

你可以在浏览器里这样检查：

1. 打开浏览器 → F12 → Network。

2. 点一次登录，看 `/admin/employee/login` 的返回：

   * **Status** 是否是 `200`？
   * **Response Body** 里是不是类似：

     ```json
     {
       "code": 1,
       "msg": "success",
       "data": {
         "id": 1,
         "name": "管理员",
         "token": "xxxxx.yyyyy.zzzzz"
       }
     }
     ```

   如果这里就不是 `code = 1`，而是报错，那么 500 其实在登录接口内部（比如密码校验逻辑）——这时把错误信息贴出来，我们再看。

3. 如果登录返回没问题，再看后续任意接口的请求 header 里有没有 `Authorization` 或你配置的那个 header 名字（比如 `token`）。

   * 没有：前端没把 token 加上。
   * 有但拦截器拿不到：可能 header 名字写错了，或者你在拦截器用的是 `jwtProperties.getAdminTokenName()`，配置文件里和前端不一致。

---

## 5. 关于 Druid 那个 ERROR 提示

```text
discard long time none received connection ... lastPacketReceivedIdleMillis : 130374
```

可以简单理解为：

* “这个连接太久没用，我把它丢弃重建。”
* 一般是**连接池的健康检查机制**，不是真正的业务错误。
* 只要后面 SQL 能查到 `Total: 1`，就说明它已经自动恢复了。

如果你想减少这个提示，可以在 `application.yml` 里对 Druid 配一些参数（`testWhileIdle`、`validationQuery` 等），但这跟当前登录 500 问题不是一个级别的事情，先不用管。

---

## 小结给你捋一下

* **查库没问题**：`admin` 确实查到了。
* **问题关键在 JWT**：

  * 拦截器日志说明：所有后续请求的 token 都是 `null`。
  * 很可能是：

    * 前端没有带 token，或者
    * 拦截器对 `null` token 没有做保护，直接 `parseJWT` → 抛异常 → 500。

你可以先：

1. 把 `JwtTokenAdminInterceptor.preHandle()` 改成「先判空，再解析」的写法。
2. 确认配置里排除了 `/admin/employee/login`。
3. 在浏览器中看一眼登录接口的返回 & 后续请求是否带上了 token。

如果你愿意，可以把：

* `EmployeeController.login()` 的代码，
* `JwtTokenAdminInterceptor.preHandle()` 的代码，

直接贴出来，我可以帮你逐行看哪里可能抛异常 🔍。
