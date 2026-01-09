好，这段日志现在信息就很清楚了，我给你拆两件事说：

---

## 一、`admin1` 登录为什么是 500？

关键这几行：

```text
EmployeeLoginDTO(username=admin1, password=123456)
...
Total: 0
...
nested exception is com.sky.exception.AccountNotFoundException: 账号不存在
...
com.sky.exception.AccountNotFoundException: 账号不存在
    at com.sky.service.impl.EmployeeServiceImpl.login(EmployeeServiceImpl.java:37)
```

含义：

1. 代码执行到 `EmployeeServiceImpl.login()`。
2. 用 `username = admin1` 去查库：`Total: 0`，说明数据库里没有 `admin1`。
3. 于是第 37 行抛了业务异常：`AccountNotFoundException("账号不存在")`。
4. 但是——**这个异常没有被全局异常处理器捕获，直接抛到 Servlet，Spring 默认返回 500**。

也就是：
**业务上是「账号不存在」这种正常情况，结果因为没统一处理 → 变成了 HTTP 500。**

### 你应该有的代码（全局异常处理）

黑马这个项目里一般有一个类似这样的类：

```java
package com.sky.handler;

import com.sky.result.Result;
import com.sky.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BaseException.class)
    public Result<String> handleBaseException(BaseException ex) {
        log.error("业务异常：{}", ex.getMessage());
        // 用统一的返回格式包一层，而不是让异常往上抛成 500
        return Result.error(ex.getMessage());
    }
}
```

你的所有业务异常（例如 `AccountNotFoundException`、`PasswordErrorException`、`AccountLockedException`）一般会继承 `BaseException`：

```java
public class AccountNotFoundException extends BaseException {
    public AccountNotFoundException(String msg) {
        super(msg);
    }
}
```

👉 **检查点：**

1. 你的项目里有没有 `GlobalExceptionHandler` 这种类？

   * 没有：**按上面这个自己新建一个**。
   * 有：看一下

     * 是否在 `com.sky` 包或者子包里（要被 Spring 扫描到）
     * 是否加了 `@RestControllerAdvice`
     * 方法上是否加了 `@ExceptionHandler(BaseException.class)` 或具体异常类型。

2. `AccountNotFoundException` 是否继承了 `BaseException`（或你配置的那个父类）？
   如果不是，就改成继承，保证能被统一捕获。

做完这些之后，再用 `admin1` 登录一遍：

* **HTTP 状态码** 应该不再是 500，而是 200；
* **响应体 JSON** 类似：

```json
{
  "code": 0,
  "msg": "账号不存在",
  "data": null
}
```

前端就可以根据 `code` 做提示了。

---

## 二、`admin` 登录 & JWT 拦截器 `jwt校验:null`

日志里多次出现：

```text
EmployeeLoginDTO(username=admin, password=123456)
...
Total: 1
```

说明：

* 用 `admin` 登录时，数据库**查到了 1 条记录**，没有抛 `AccountNotFoundException`。
* 日志里也没有后续异常栈，所以后端这次登录基本是成功执行完的（很可能已经生成了 token 并返回给前端）。

紧接着你又看到很多：

```text
JwtTokenAdminInterceptor : jwt校验:null
```

说明：

* 拦截器在 `preHandle` 里打印了 token：

  ```java
  String token = request.getHeader(jwtProperties.getAdminTokenName());
  log.info("jwt校验:{}", token);
  ```
* 结果每次都是 `null`，也就是**后续请求根本没带 token**。

这里面有两个需要确认的点（跟你之前的问题是连在一起的）：

### 1）拦截器对 `null` token 的处理

若你的代码是这样（示意）：

```java
String token = request.getHeader(jwtProperties.getAdminTokenName());
log.info("jwt校验:{}", token);

// ❌ 直接解析
Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
// ...
```

当 `token == null` 的时候，`JwtUtil.parseJWT` 会直接抛异常，可能又给你整一个 500。
建议改成：

```java
String token = request.getHeader(jwtProperties.getAdminTokenName());
log.info("jwt校验:{}", token);

if (token == null || token.trim().isEmpty()) {
    // 返回未登录，而不是 500
    Result<?> result = Result.error(MessageConstant.NOT_LOGIN);
    String json = JSONObject.toJSONString(result);

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write(json);
    return false;
}

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

这样就不会因为 `token = null` 报 500 了。

### 2）前端是否带上了 token？

登录成功后，后端通常返回一个 VO：

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "id": 1,
    "name": "管理员",
    "token": "xxx.yyy.zzz"
  }
}
```

前端要做两件事：

1. 把 `data.token` 存起来（localStorage / sessionStorage 等）。
2. 所有后续请求在 header 上带上这个 token，比如：

   ```http
   Authorization: Bearer xxx.yyy.zzz
   ```

或者项目里自定义 header 名称，例如 `token: xxx.yyy.zzz`，要跟你 `jwtProperties.getAdminTokenName()` 配置一致。

---

## 三、Druid 那个 `discard long time none received connection` 要紧吗？

```text
discard long time none received connection ... lastPacketReceivedIdleMillis : 130374
```

* 只是连接池在说：

  > 有个连接闲太久了（130 秒没收包），我给它丢掉重建一下。
* 后面你马上看到 SQL 正常执行（`Total: 1`），说明它自己恢复了。
* 这不是当前 500 的主因，可以先不用管。

---

## 总结一下今天这段日志的结论

1. **`admin1` 这种不存在的账号登录 → 抛 `AccountNotFoundException` → 没有被全局异常处理 → 变成 HTTP 500。**
   👉 需要补/修 `GlobalExceptionHandler`，把业务异常转成统一的 `Result.error()` 返回。

2. **`admin` 登录时数据库查到了记录，说明这一段业务逻辑大概率是 OK 的。**

3. **大量 `jwt校验:null` 提示：所有后续接口都没带 token，或者拦截器没对 null 做保护。**
   👉 拦截器里要先判空，前端要在后续请求 header 里带 token。

---

如果你愿意，可以下一步把这两个类的代码贴出来，我可以帮你逐行看：

* `EmployeeServiceImpl.login()`
* `GlobalExceptionHandler`（如果有的话）
* `JwtTokenAdminInterceptor.preHandle()`

我们就把：
「账号不存在 → 500」
和
「token 为 null → 500 / 401」
两个路径彻底理一遍 👀
