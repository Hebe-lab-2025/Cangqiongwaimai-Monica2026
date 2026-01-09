短句回答：**因为你没在 Swagger/Knife4j 里“声明”这个 header 参数，拦截器用的 token 不会自动出现在文档里。**

---

## 1. 为什么 Swagger 里看不到 header？

Swagger 只根据下面两种东西生成文档：

1. **方法签名上的参数**

   * 比如 `@RequestBody EmployeeDTO employeeDTO`
   * 或 `@RequestParam`, `@PathVariable`, `@RequestHeader` 等

2. **Swagger 注解 / Docket 配置**

   * 比如 `@ApiImplicitParam`、`@ApiImplicitParams`
   * 或 Docket 里的 `securitySchemes` / `globalOperationParameters` 等

而你现在：

* `save()` 方法签名里 **没有任何 `@RequestHeader` 参数**；

* Docket 里目前也只是最基础的：

  ```java
  .apis(RequestHandlerSelectors.basePackage("com.sky.controller"))
  .paths(PathSelectors.any())
  ```

* `JwtTokenAdminInterceptor` 是拦截器，和 Swagger 文档是两条线，**它不会帮你把 header 自动加到文档里**。

所以 Swagger UI 只看到 body（EmployeeDTO），看不到 token 这个 header，很正常。

---

## 2. 最简单的两种解决方式

### 方式 A：在方法上显式写 header 参数（学习/调试时最直观）

```java
@PostMapping
@ApiOperation("新增员工")
@ApiImplicitParam(name = "token", value = "登录令牌", required = true, paramType = "header")
public Result save(@RequestBody EmployeeDTO employeeDTO) {
    log.info("新增员工：{}", employeeDTO);
    employeeService.save(employeeDTO);
    return Result.success();
}
```

👉 这样做的效果：

* Swagger 文档里，`新增员工` 接口会多一个 header 参数：`token`；
* 你在 `doc.html` 里点“调试”，就能直接在 header 输入 token。

> 注意：这里虽然声明了 header，但你代码里没有用这个参数——真正校验在拦截器里做，没问题。

---

### 方式 B：在 Docket 里配置全局 token（更接近真实项目）

在 `WebMvcConfiguration.docket()` 里补充安全配置（token 名字按你 yml 里的来，比如是 `token`）：

```java
@Bean
public Docket docket() {
    ApiInfo apiInfo = new ApiInfoBuilder()
            .title("苍穹外卖项目接口文档")
            .version("2.0")
            .description("苍穹外卖项目接口文档")
            .build();

    return new Docket(DocumentationType.SWAGGER_2)
            .apiInfo(apiInfo)
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.sky.controller"))
            .paths(PathSelectors.any())
            .build()
            .securitySchemes(securitySchemes())
            .securityContexts(securityContexts());
}

private List<ApiKey> securitySchemes() {
    List<ApiKey> list = new ArrayList<>();
    // 第一个参数随便起名，后面两个要写 header 名
    list.add(new ApiKey("token", "token", "header"));
    return list;
}

private List<SecurityContext> securityContexts() {
    List<SecurityContext> list = new ArrayList<>();
    list.add(SecurityContext.builder()
            .securityReferences(defaultAuth())
            .build());
    return list;
}

private List<SecurityReference> defaultAuth() {
    AuthorizationScope authorizationScope =
            new AuthorizationScope("global", "accessEverything");
    AuthorizationScope[] scopes = new AuthorizationScope[]{authorizationScope};
    List<SecurityReference> list = new ArrayList<>();
    list.add(new SecurityReference("token", scopes));
    return list;
}
```

效果：

* Swagger / Knife4j 顶部会出现一个「Authorize」按钮；
* 你在里边输入一次 token，之后所有需要的接口都会自动带这个 header。

---

## 3. 和你现在的问题怎么对应？

* **接口已经能登录，返回带 token 的 JSON ✔**
* 新增员工接口可以正常被调用，但是：

  * Swagger 文档里看不到 header 参数，这是**文档配置缺失**；
  * 实际请求要通过拦截器，需要你手动在 header 带上 `token`（curl / Postman / Swagger 调试都一样）。

你现在要做的就是：
👉 根据自己的习惯，从上面 **方式 A 或方式 B** 里选一个加上去，刷新 `http://localhost:8080/doc.html`，就能在 Swagger 里看到 header 了。
