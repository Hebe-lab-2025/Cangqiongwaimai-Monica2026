啊这个我知道你在说啥了 😂：

> **你在 Swagger / Knife4j 页面里配置了“全局参数 token”，但是每个接口的 header 区域没有一行叫 token 的参数，对吧？**

这个现象其实是**正常的**，分两种情况给你讲清楚：

---

## ① 先说结论：全局参数 ≠ 每个方法都会多一行 header 参数

在 Swagger / Knife4j 里：

* 你在页面上配置的 **“全局参数 / 全局 header”**
  👉 作用是：**调试时自动把这个 header 带到所有请求里**
  👉 **不会** 把这个参数显示成“每个接口一个 header 行”。

就像 Swagger 上面的 “Authorize” 按钮：

* 你点了之后，每个接口请求都会带 Authorization 头
* 但每个接口的参数列表里，不会多出一个 `Authorization` 参数行 —— 只是请求时统一加上而已。

**所以：**

* ✅ 拦截器能拿到 token，业务能正常走
* ❌ 但是在接口文档的“参数列表”区域，你看不到它

👉 这就是你现在看到的情况。

---

## ② 怎么确认“全局 token”真的被带上了？

你可以这样检查一下：

1. 在 doc.html（Knife4j）里，先配置好全局参数 `token`。
2. 找一个需要登录才能访问的接口，比如 `POST /admin/employee` 新增员工。
3. 点 “调试 / Try it out”，发送请求。
4. 打开浏览器 DevTools → Network → 选中该请求 → Request Headers：

   看看里面是不是有一行：

   ```http
   token: eyJhbGciOiJIUzI1NiJ9...
   ```

👉 **只要这里有，拦截器就能正确拿到 token，员工新增功能就不会 401。**

---

## ③ 如果你“想让它在方法里也显示出来”，怎么做？

如果你不仅要它“起作用”，还想在每个接口的参数列表里**明明白白看到 header: token**，那就要走 **代码配置** 这条路，而不是只在 UI 上点“全局参数”。

### 方案 A：在 Docket 里配置全局 header 参数（所有接口共用）

在你的 `WebMvcConfiguration` 或 Swagger 配置类里，给 `docket()` 加一段全局参数：

```java
@Bean
public Docket docket() {
    // 1. 基本信息
    ApiInfo apiInfo = new ApiInfoBuilder()
            .title("苍穹外卖项目接口文档")
            .version("2.0")
            .description("苍穹外卖项目接口文档")
            .build();

    // 2. 定义一个全局 header 参数：token
    ParameterBuilder parameterBuilder = new ParameterBuilder();
    List<Parameter> parameters = new ArrayList<>();

    parameterBuilder
            .name("token")              // 🔴 这里要和 jwtProperties.getAdminTokenName() 一致
            .description("JWT Token")
            .modelRef(new ModelRef("string"))
            .parameterType("header")
            .required(false);

    parameters.add(parameterBuilder.build());

    // 3. 把全局参数挂到 Docket 上
    return new Docket(DocumentationType.SWAGGER_2)
            .apiInfo(apiInfo)
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.sky.controller"))
            .paths(PathSelectors.any())
            .build()
            .globalOperationParameters(parameters);  // ⭐ 关键
}
```

这样之后：

* 每个接口的“参数”区域里，会多出一行 header：`token`
* 调试时，也会自动带上这个 header

---

### 方案 B：给某个接口单独加 header 参数

只针对少数接口（比如新增员工、分页查询）显示 header，可以用 `@ApiImplicitParam`：

```java
@PostMapping
@ApiOperation("新增员工")
@ApiImplicitParams({
    @ApiImplicitParam(
            name = "token",
            value = "JWT Token",
            required = true,
            paramType = "header",
            dataType = "String"
    )
})
public Result save(@RequestBody EmployeeDTO employeeDTO) {
    log.info("新增员工：{}", employeeDTO);
    employeeService.save(employeeDTO);
    return Result.success();
}
```

这样：

* 这个方法的 Swagger 文档里，会看到一个 header 类型的参数 `token`
* 但其它接口不会有

---

## ④ 你现在最可能的情况是：

1. **YApi** 里已经设置了全局 header（是教程里的“文档管理 → 全局参数设置”）；
2. **Swagger/Knife4j**（`doc.html`）里只是引入了依赖 + 配了 Docket，但**没在代码里声明 header 参数**；
3. 于是：

   * YApi 里调试没问题，看得到 header；
   * Swagger 文档里接口参数列表没 header，但只要你在页面顶部/调试栏设置了 token，真正请求还是会带上。

---

## 你可以这样快速自查一下 ✅

1. 在 Swagger doc.html 里，用登录接口拿到 token；
2. 在页面里设好全局 token（或者 Authorize）；
3. 调用 `POST /admin/employee` 新增员工；
4. **如果返回 200，数据库员工表有数据**：

   * 说明 token 已经带上了，只是 UI 没显示参数行 → 正常
5. 如果还是 401：

   * 检查 `jwtProperties.getAdminTokenName()` 是不是叫 `"token"`，
   * Swagger 里全局参数名字要跟它完全一致。

---

如果你愿意，下一步我可以帮你直接写一份你项目专用的 `WebMvcConfiguration + Swagger配置` 完整代码（包含：全局 token header、接口分组、只扫描 `com.sky.controller` 包），你可以直接 copy 进去对照教程。
