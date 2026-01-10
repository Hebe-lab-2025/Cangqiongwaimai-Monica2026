好，我们一步一步把“拦截器拿不到 token”这个问题掰开看一下 👇

---

## 0. 先认清现象

现在的情况其实是：

* **拦截器的 `preHandle` 能进来**，说明拦截器注册是 OK 的；
* 但是日志里大概率是这种：

  ```java
  log.info("jwt校验:{}", token);  // 打出来是 null
  ```
* 说明这一句：

  ```java
  String token = request.getHeader(jwtProperties.getAdminTokenName());
  ```

  拿到的是 `null` → **请求根本没带这个 header**，或者 header 名字对不上。

我们就从这两个点排查：

> ① 你的 header 名叫什么
> ② 请求里到底有没有这个 header

---

## 1. 第一步：确认 header 名字到底是什么

看你的拦截器代码（教材里是这样的）：

```java
// 1、从请求头中获取令牌 jwtProperties.getAdminTokenName()获取为token
String token = request.getHeader(jwtProperties.getAdminTokenName());
```

`jwtProperties.getAdminTokenName()` 对应的是 yml 里的配置，一般是这样的：

```yaml
sky:
  jwt:
    admin-secret-key: heima
    admin-ttl: 7200000
    admin-token-name: token   # 🔴 这里的名字要记住
```

👉 **请确认：**

1. `application.yml` / `application-dev.yml` 里有没有这几行；
2. `admin-token-name` 的值是不是 `token`；
3. 如果你在 Swagger 里填的是 `Authorization` / `auth` / `x-token` 这类，就一定对不上。

**目标：**

> 拦截器里用的名字 和 Swagger / curl 里发的 header **必须一模一样**。

---

## 2. 第二步：在拦截器里把所有 header 打出来看一眼

我们可以在 `preHandle` 里加一小段日志，直接打印当前请求里都有哪些 header：

```java
public boolean preHandle(HttpServletRequest request,
                         HttpServletResponse response,
                         Object handler) throws Exception {

    if (!(handler instanceof HandlerMethod)) {
        return true;
    }

    // 🧪 打印所有 header 名称，方便对照
    Enumeration<String> headerNames = request.getHeaderNames();
    System.out.println("---- Request Headers ----");
    while (headerNames.hasMoreElements()) {
        String name = headerNames.nextElement();
        System.out.println(name + " = " + request.getHeader(name));
    }
    System.out.println("-------------------------");

    String token = request.getHeader(jwtProperties.getAdminTokenName());
    log.info("jwt校验 token = {}", token);

    // 后面保持不变……
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

然后你：

1. 重启项目；
2. 用 Swagger / curl 调一次 “新增员工” 接口；
3. 看控制台打印的所有 header，有没有：

   ```text
   token = eyJhbGciOiJIUzI1NiJ9...
   ```

如果没有，那就是 **请求根本没带 token**，问题就不在拦截器，而在“你是怎么发这个请求的”。

---

## 3. 用 curl 做一次“标准对照测试”

先别管 Swagger，直接用命令行搞一个最标准的请求，看拦截器能不能拿到 token。

### 3.1 第一步：用登录接口拿 token

```bash
curl -X POST "http://localhost:8080/admin/employee/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

你应该能得到类似这样的返回：

```json
{
  "code": 1,
  "data": {
    "id": 1,
    "userName": "admin",
    "name": "管理员",
    "token": "eyJhbGciOiJIUzI1NiJ9.xxx.yyy"
  }
}
```

把 `data.token` 这一长串复制出来。

### 3.2 第二步：带着 header 调用“新增员工”

⚠️ **这里 header 名一定要跟 yml 里 `admin-token-name` 一致**，比如是 `token`：

```bash
curl -X POST "http://localhost:8080/admin/employee" \
  -H "Content-Type: application/json" \
  -H "token: eyJhbGciOiJIUzI1NiJ9.xxx.yyy" \
  -d '{
    "id": 0,
    "idNumber": "23",
    "name": "M",
    "phone": "",
    "sex": "",
    "username": ""
  }'
```

这时候：

* 控制台应该能打印出你刚刚贴过的那种响应：

  ```json
  { "code":1, "data": { "id":1,"userName":"admin","name":"管理员", ... } }
  ```

  （或者至少新增员工成功）
* 并且你在拦截器日志里能看到：

  ```text
  jwt校验 token = eyJhbGciOiJIUzI1NiJ9.xxx.yyy
  ```

✅ 如果 curl 能成功、拦截器能拿到 token
👉 说明 **后端逻辑完全没问题**，问题百分之百出在 Swagger / 前端没把 header 带对。

---

## 4. Swagger 这边最容易踩的坑（你现在八成是踩在这里）

### 情况 A：你在 **Swagger UI 里配置了“全局参数”**，但类型选成了 Query

> 比如在页面上加的是：
> 名称：`token`，位置：`query`

那发请求的时候，请求长这样：

```http
POST /admin/employee?page=1&token=xxxx
```

而不是：

```http
POST /admin/employee
token: xxxx   ← 这是我们要的
```

**解决：**

* 确保你在 UI 里配置全局参数时，选的是 **header** 类型，而不是 query。

---

### 情况 B：代码里没定义 Swagger 的全局 header 参数，只在 UI 里点了“全局参数”

Knife4j 页面上的“全局参数”其实只是一个 UI 功能，
**不会自动帮你在每个接口的参数列表中加出来**，也不会改后端逻辑。

最稳妥的做法，是在 **Swagger 配置类里写死一个全局 header**：

```java
@Bean
public Docket docket() {
    ApiInfo apiInfo = new ApiInfoBuilder()
            .title("苍穹外卖项目接口文档")
            .version("2.0")
            .description("苍穹外卖项目接口文档")
            .build();

    // 定义 header 参数
    ParameterBuilder pb = new ParameterBuilder();
    List<Parameter> params = new ArrayList<>();

    pb.name("token")                               // 👈 和 admin-token-name 保持一致
      .description("JWT Token")
      .modelRef(new ModelRef("string"))
      .parameterType("header")                    // 👈 很重要：header
      .required(false);
    params.add(pb.build());

    return new Docket(DocumentationType.SWAGGER_2)
            .apiInfo(apiInfo)
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.sky.controller"))
            .paths(PathSelectors.any())
            .build()
            .globalOperationParameters(params);    // 👈 所有接口自动带这个 header 参数
}
```

这样：

* 每个接口文档里，会出现一个 header 参数 `token`；
* 你在 Swagger UI 的“参数列表”里填上 token 值；
* 真正发请求的时候，就会带 header 出去了；
* 拦截器自然就能拿到。

---

## 5. 总结一下你现在可以立刻做的三步 🧪

1. **在 yml 确认 header 名字**：

   * `sky.jwt.admin-token-name` 是什么？比如 `token`。
2. **加上拦截器 header 打印日志**：

   * 看一眼当前请求到底带了哪些 header。
3. **用 curl 做一次对照测试**：

   * curl + `-H "token: xxx"` 能拿到的话，说明后端 OK，
   * 剩下就是 Swagger UI 参数类型 / 名字的问题。

你先按上面这三步试一下，如果你愿意，把：

* 你现在的 `application.yml` 里 `sky.jwt` 这一段，
* 以及拦截器打印出来的 header 列表（那几行 System.out）

贴给我，我可以帮你直接指出是哪一步没对上。
