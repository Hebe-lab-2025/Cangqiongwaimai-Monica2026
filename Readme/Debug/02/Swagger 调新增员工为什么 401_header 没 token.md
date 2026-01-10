好，我们直接走 **“Swagger 调新增员工为什么 401 / header 没 token” 实战排查流程**这一条 👍
（登录现在已经 OK 了，就顺着往下把 JWT + 拦截器这条链打通。）

---

## 总体结论先说清楚

现在日志里不断出现：

```text
JwtTokenAdminInterceptor : jwt校验:null
```

说明两件事：

1. **拦截器已经生效** ✅
2. 但是 **请求头里没带 token**，所以校验拿到的是 `null` ❌

如果你用 Swagger 去调用 “新增员工”等 `/admin/**` 接口：

* 这些接口大概率被 `JwtTokenAdminInterceptor` 拦截
* 没带 token → 拦截器认为「未登录」→ 返回 401 / 自定义错误

**所以问题核心：**
👉 登录接口已经正确发 token 出去了，但 **后续请求（新增员工等）没有把 token 放在正确的 Header 里**。

下面一步步教你怎么对着你本机代码排。

---

## 第 1 步：确认登录接口现在真的返回了 token

你在 Swagger/Knife4j 里打开：

* `POST /admin/employee/login`
* body 填：

```json
{
  "username": "admin",
  "password": "123456"
}
```

点击 `Execute`，在 **Response body** 里，看一下是不是类似：

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "id": 1,
    "name": "管理员",
    "userName": "admin",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

重点记住：

* `data.token` 这一长串字符串
* 等会要把它放进 header 里

如果没有 `data.token`，那要回头查 `EmployeeController.login()` 里有没有生成 JWT（我们现在先假设是有的）。

---

## 第 2 步：在拦截器里看清楚——**到底要哪个 Header？**

打开你的 `JwtTokenAdminInterceptor`（大概率在 `com.sky.interceptor` 包）：

在 `preHandle` 里应该有类似代码（伪代码）：

```java
@Override
public boolean preHandle(HttpServletRequest request,
                         HttpServletResponse response,
                         Object handler) throws Exception {

    String token = request.getHeader(jwtProperties.getAdminTokenName());
    log.info("jwt校验:{}", token);

    // 1. 如果是白名单接口，直接放行
    // 2. 如果 token 为空 -> 拦截 / 抛异常
    // 3. token 不为空 -> JwtUtil.parseJWT(...) 校验 -> 通过后放行
}
```

这里有一个关键点：

> `request.getHeader(jwtProperties.getAdminTokenName())`

也就是说：
**真正的 Header 名字不是写死的，而是从配置 `jwtProperties.adminTokenName` 来的。**

---

## 第 3 步：在 `application-dev.yml` 里找出真正的 header 名

打开你当前使用的配置文件（日志里说 profile 是 `dev`）：

```text
The following 1 profile is active: "dev"
```

所以重点看：

* `application.yml`
* `application-dev.yml`

在里面找到类似：

```yaml
sky:
  jwt:
    admin-secret-key: xxxx
    admin-ttl: 7200000
    admin-token-name: token
```

**这里的 `admin-token-name` 就是拦截器要去取的 Header 名字。**

举例几种常见情况：

1. 如果你配置的是：

   ```yaml
   admin-token-name: token
   ```

   那么拦截器实际就是：

   ```java
   request.getHeader("token");
   ```

   ➜ 你后面所有需要登录的接口，都必须在 Header 里加：

   ```http
   token: <上一步登录返回的 jwt 字符串>
   ```

2. 有人会配成：

   ```yaml
   admin-token-name: Authorization
   ```

   那就是：

   ```java
   request.getHeader("Authorization");
   ```

   ➜ 那就要在 Header 里用：

   ```http
   Authorization: <jwt>
   ```

   或者 `Authorization: Bearer <jwt>`（看你解析的时候怎么写）。

👉 你只要搞清楚 **自己项目此时此刻的 `admin-token-name` 是啥**，后面 Swagger 就好配了。

---

## 第 4 步：看 `WebMvcConfiguration` 的白名单 & 拦截路径

打开 `WebMvcConfiguration`（一般在 `com.sky.config`）：

找到 `addInterceptors`：

```java
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(jwtTokenAdminInterceptor)
            .addPathPatterns("/admin/**")
            .excludePathPatterns(
                    "/admin/employee/login",
                    "/admin/employee/logout",
                    "/doc.html",
                    "/webjars/**",
                    "/swagger-resources",
                    "/v2/api-docs"
            );
}
```

你要确认：

1. `addPathPatterns("/admin/**")`：

   * 说明所有 `/admin/` 开头的接口都会走 JWT 拦截

2. `excludePathPatterns` 里一定要有：

   ```text
   /admin/employee/login
   ```

   * 否则连登录接口也会要求带 token，死循环。

只要你看到类似上面这种配置，就可以确定：

* **登录接口（/admin/employee/login）不需要 token**
* 其他管理接口（例如 `/admin/employee/page`、`/admin/employee` 新增员工）都需要 token，否则就会 401 或抛 “未登录”的异常。

---

## 第 5 步：在 Swagger 里，把 token 塞进正确的 Header

现在你已经知道两件事情：

1. 登录接口可以拿到一个 `data.token`
2. 真正的 Header 名字，比如是 `token`（以你的配置为准）

接下来两种方式选一种用：

### 方式 A：每次手动加 Header（最简单直接）

在 Swagger/Knife4j 里：

1. 先调用 `/admin/employee/login` 得到 token

2. 找到其他需要登录的接口，比如：

   * `POST /admin/employee`（新增员工）

3. 在这个接口的 “Parameters” 区域：

   * 通常会有一个 “Headers” 或 “Try it out → Add parameter”
   * 手动添加一个 header 参数：

     * name：**跟你 yml 里配置的一模一样**（例如 `token`）
     * value：刚才登录返回的那整段 JWT 字符串（不要加引号）

4. 再点击 `Execute`

如果你之前是 401 或 “未登录”，现在应该能正常通过拦截器 → Controller → Service。

---

### 方式 B：在 Swagger 配置类里加“全局 Header 参数”（长期方案）

如果你不想每个接口都手动填，可以在 Swagger/Knife4j 的配置类里加一个全局 header 参数，例如：

```java
@Bean
public Docket docket() {
    ParameterBuilder tokenPar = new ParameterBuilder();
    List<Parameter> pars = new ArrayList<>();

    tokenPar.name("token")                // 这里要和 admin-token-name 一致
            .description("JWT Token")
            .modelRef(new ModelRef("string"))
            .parameterType("header")
            .required(false)
            .build();

    pars.add(tokenPar.build());

    return new Docket(DocumentationType.SWAGGER_2)
            .apiInfo(apiInfo())
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.sky.controller"))
            .paths(PathSelectors.any())
            .build()
            .globalOperationParameters(pars);  // 关键：给所有接口加上 header 参数
}
```

之后：

* Swagger UI 顶部会有一个统一的 `token` 输入框
* 你登录一次，把 token 粘进去
* 所有接口发请求时都会自动带这个 header

---

## 6. 综合一下：你现在可以这么操作

按这个顺序走一遍（不需要改很多东西）：

1. **确认 header 名**

   * 看 `application-dev.yml` 里的 `sky.jwt.admin-token-name`
2. **确认拦截路径**

   * 在 `WebMvcConfiguration` 里看：

     * 拦了 `/admin/**`
     * 放行了 `/admin/employee/login`
3. **登录一次**

   * `/admin/employee/login`，拿到 `data.token`
4. **在 Swagger 调“新增员工”接口时：**

   * 手动或者全局加一个 header：

     * key：`admin-token-name` 对应的名字（比如 `token`）
     * value：刚才拿到的 jwt 字符串

然后再看：

* ✅ 如果新增员工返回正常 JSON，说明 Swagger 的 401 / 无 token 问题已经解决
* ❌ 如果还有 401 或“未登录”，把那次请求：

  * Swagger 里的 Request/Response
  * 以及后端日志里的异常栈

贴出来，我们可以再顺着拦截器的代码，把“为什么校验失败”也一起查掉。
