很好，这段 `yml` 至少说明一件事：**你的 header 名字就叫 `token`，没问题。**

```yaml
sky:
  jwt:
    admin-secret-key: itcast
    admin-ttl: 7200000
    admin-token-name: token   # 👈 这里决定了拦截器从哪个 header 里拿
    user-secret-key: itheima
    user-ttl: 7200000
    user-token-name: authentication
```

现在问题只剩两类可能：

1. **请求根本没带 `token` 这个 header**（最常见）
2. 拦截器没有被 Spring 注册，`preHandle` 根本没执行

我给你一个**一步步排查 checklist**，你直接照着做就行。

---

## ✅ 第 1 步：确认拦截器有没有执行

先在 `preHandle` 最上面加一行日志，**不依赖 token**：

```java
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    log.info("👉 JwtTokenAdminInterceptor.preHandle 被调用了, URI = {}", request.getRequestURI());

    if (!(handler instanceof HandlerMethod)) {
        return true;
    }

    ...
}
```

然后你用 Swagger 或浏览器调用任意 `/admin/xxx` 接口：

* 如果控制台有这条日志，说明**拦截器有执行**
* 如果完全没有这行日志，说明**拦截器没注册成功**

### 如果没执行：检查 WebMvcConfiguration

在你的 `WebMvcConfiguration`（或类似的配置类）里，一定要有这个：

```java
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")          // 要拦截的路径
                .excludePathPatterns("/admin/employee/login"); // 登录接口放行
    }

    // ... 其他配置（静态资源、消息转换器等）
}
```

> 如果这一段没有写 / 写错路径，那你再怎么传 token，拦截器也永远不会执行。

---

## ✅ 第 2 步：确认请求里到底有没有 `token` header

在 `preHandle` 里加一个**打印所有 header 的调试代码**（这个非常有用）：

```java
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    log.info("👉 JwtTokenAdminInterceptor.preHandle 被调用了, URI = {}", request.getRequestURI());

    if (!(handler instanceof HandlerMethod)) {
        return true;
    }

    // 🧪 打印所有 header，看看到底有没有 token
    System.out.println("---- Request Headers ----");
    java.util.Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
        String name = headerNames.nextElement();
        System.out.println(name + " = " + request.getHeader(name));
    }
    System.out.println("-------------------------");

    String headerName = jwtProperties.getAdminTokenName(); // 应该是 "token"
    log.info("从配置读取的 adminTokenName = {}", headerName);

    String token = request.getHeader(headerName);
    log.info("从 header 中获取到的 token = {}", token);

    try {
        Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
        Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
        log.info("当前员工id：{}", empId);
        BaseContext.setCurrentId(empId);
        return true;
    } catch (Exception ex) {
        response.setStatus(401);
        return false;
    }
}
```

然后做两件事：

### 2.1 用 curl 手动测一次（100% 可控）

1. **先登录拿 token：**

```bash
curl -X POST "http://localhost:8080/admin/employee/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

拿到返回里的：

```json
{
  "code": 1,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...."
  }
}
```

2. **带着 token 调用“新增员工”：**

```bash
curl -X POST "http://localhost:8080/admin/employee" \
  -H "Content-Type: application/json" \
  -H "token: eyJhbGciOiJIUzI1NiJ9...." \
  -d '{
    "id": 0,
    "idNumber": "23",
    "name": "M",
    "phone": "",
    "sex": "",
    "username": "testUser"
  }'
```

看控制台输出：

* `Request Headers` 里是否有一行：
  `token = eyJhbGciOiJIUzI1NiJ9....`
* `从 header 中获取到的 token = ...` 是否打印出来
* `当前员工id：1` 是否打印出来

> 如果 curl 方式是 **OK 的**，说明：
> 🔹 yml ✅
> 🔹 拦截器 ✅
> 🔹 Token ✅
> 👉 问题就只剩 Swagger 没带 header（见下一步）

---

## ✅ 第 3 步：Swagger / Knife4j 全局参数要注意两个点

你现在说：

> 在 swagger 全局参数文档中设置了 token，但是具体方法的 header 哪里没有 token

这里有两个常见坑：

### 3.1 你在 UI 里加的“全局参数”，位置选成了 `Query`

在 Knife4j UI 里：

* 如果你设置“全局参数”时：

  * 名字：`token`
  * **位置选成了 `Query`**

那发出去请求会变成：

```http
POST /admin/employee?token=xxx
```

而不是：

```http
POST /admin/employee
token: xxx
```

拦截器用的是：

```java
request.getHeader("token")
```

所以拿不到。

✅ 正确做法（在 doc.html 的 UI 上）：

1. 文档管理 → 全局参数设置
2. 新增参数：

   * 名称：`token`
   * 位置（in）：**header**
   * 类型：string
3. 保存，再去调用接口

---

### 3.2 用 Java 配 Swagger 的“全局 header 参数”（更稳）

在你的 `docket()` 里加上 global header 配置：

```java
@Bean
public Docket docket() {
    ApiInfo apiInfo = new ApiInfoBuilder()
            .title("苍穹外卖项目接口文档")
            .version("2.0")
            .description("苍穹外卖项目接口文档")
            .build();

    // 定义一个全局 header 参数：token
    springfox.documentation.builders.ParameterBuilder pb = new springfox.documentation.builders.ParameterBuilder();
    java.util.List<springfox.documentation.service.Parameter> params = new java.util.ArrayList<>();

    pb.name("token")   // 👈 这里要和 admin-token-name 完全一致
      .description("JWT Token")
      .modelRef(new springfox.documentation.schema.ModelRef("string"))
      .parameterType("header")  // 👈 一定是 header
      .required(false);

    params.add(pb.build());

    return new Docket(DocumentationType.SWAGGER_2)
            .apiInfo(apiInfo)
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.sky.controller"))
            .paths(PathSelectors.any())
            .build()
            .globalOperationParameters(params);  // 👈 关键
}
```

重启之后：

* 打开 `http://localhost:8080/doc.html`
* 进入 `新增员工` 接口
* 会看到多了一个 header 参数：`token`
* 把登录接口返回的 token 填进去再点「发送」

---

## 小结（你可以按这个顺序自查）

1. ✅ 确认拦截器 `preHandle` 真的被调用（看“URI 日志”）
2. ✅ 用 `curl` 带 `-H "token: xxx"` 亲自试一次
3. ✅ Swagger 里“全局参数”的 `in` 必须是 `header`
4. ✅ 或者在 `docket()` 里用 `globalOperationParameters` 配好 header 参数
5. 如果 **curl 能成功但 Swagger 不行**，问题一定出在 Swagger 没把 header 带过去

你可以先跑一遍 curl 测试，把控制台里那段 `Request Headers` 输出贴给我，我可以帮你一眼看出是不是 Swagger/前端没传 header。
