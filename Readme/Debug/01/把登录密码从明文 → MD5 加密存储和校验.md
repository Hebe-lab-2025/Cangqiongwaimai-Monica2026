很好，这一大段其实在说两件事：

1. **把登录密码从明文 → MD5 加密存储和校验**
2. **把接口文档（Swagger + knife4j）配置好，用 doc.html 来测接口**

我帮你整理成「你现在应该做的 checklist」，你照着一个个对就行。

---

## 一、登录功能完善：密码改成 MD5

### 1. 数据库里先改密码

现在 `employee` 表里的密码是明文 `123456`。
教程要你改成：**存 MD5 后的密文**。

`123456` 的 MD5 是：

```text
e10adc3949ba59abbe56e057f20f883e
```

你在 `employee` 表里，找到 admin 那条记录，把 `password` 改成上面这一串。

> ✅ 注意：如果你已经在 Java 里对密码做了 MD5，而数据库还是 `123456`，
> 就一定登录失败 → 抛 `PasswordErrorException`。

---

### 2. 在 `EmployeeServiceImpl.login` 里对前端密码做 MD5 再比对

关键逻辑是这一段：

```java
@Override
public Employee login(EmployeeLoginDTO employeeLoginDTO) {
    String username = employeeLoginDTO.getUsername();
    String password = employeeLoginDTO.getPassword();

    // 1. 根据用户名查数据库
    Employee employee = employeeMapper.getByUsername(username);

    // 2. 处理各种异常
    if (employee == null) {
        // 账号不存在
        throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
    }

    // ⭐ 3. 对前端传来的明文密码做 MD5
    password = DigestUtils.md5DigestAsHex(password.getBytes());

    // 4. 比对加密后的密码
    if (!password.equals(employee.getPassword())) {
        // 密码错误
        throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
    }

    // 5. 判断账号是否被锁定
    if (employee.getStatus() == StatusConstant.DISABLE) {
        // 账号被锁定
        throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
    }

    // 6. 返回 Employee
    return employee;
}
```

别忘了 import：

```java
import org.springframework.util.DigestUtils;
```

> 小结：**前端传明文 → 后端 MD5 → 和数据库里 MD5 密码比对**。
> 只要“数据库密码”和“后端加密方式”不一致，就会一直密码错。

---

## 二、YApi vs Swagger（knife4j）是干嘛的？

你贴的部分是在讲“接口文档怎么管理”和“怎么测接口”。

### 1. YApi：设计阶段的接口管理工具

* 用来 **导入/设计接口文档**，方便前后端对齐“接口长什么样”。
* 常用来：

  * 定义 path、method、请求参数、响应结构
  * 前端可以在没有后端的情况下，自己用 Mock 数据调试

> 对你来说：**只要知道课程要你把两个 JSON 导入 YApi 就行**，主要是为了看“接口长啥样”。

---

### 2. Swagger + knife4j：开发调试阶段的接口文档 & 测试工具

你项目里已经：

* 在 `pom.xml` 加了：

```xml
<dependency>
    <groupId>com.github.xiaoymin</groupId>
    <artifactId>knife4j-spring-boot-starter</artifactId>
</dependency>
```

* 在 `WebMvcConfiguration` 里写了：

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
            .build();
}

@Override
protected void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/doc.html")
            .addResourceLocations("classpath:/META-INF/resources/");
    registry.addResourceHandler("/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/");
}
```

日志里你也已经看到：

```text
com.sky.config.WebMvcConfiguration : 准备生成接口文档...
```

这说明 knife4j 已经在加载了。

### 3. 访问接口文档页面

启动项目后，浏览器打开：

```text
http://localhost:8080/doc.html
```

你能看到：

* 左边是接口列表（比如 员工登录、员工新增）
* 右边是参数、返回值示例，还有在线测试按钮

> 之后你就可以 **不用 curl**，直接在 doc.html 里选“员工登录接口”，填 `admin` / `123456`，点“发送请求”，看返回 token。

---

## 三、Swagger 注解这块，你现在需要做什么？

你后面贴的 `@Api` / `@ApiModel` / `@ApiOperation` 这些：

* `EmployeeLoginDTO`：

```java
@Data
@ApiModel(description = "员工登录时传递的数据模型")
public class EmployeeLoginDTO implements Serializable {

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("密码")
    private String password;
}
```

* `EmployeeLoginVO`：

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "员工登录返回的数据格式")
public class EmployeeLoginVO implements Serializable {
    @ApiModelProperty("主键值")
    private Long id;

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("jwt令牌")
    private String token;
}
```

* `EmployeeController`：

```java
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工相关接口")
public class EmployeeController {

    @PostMapping("/login")
    @ApiOperation(value = "员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        // ...
    }

    @PostMapping("/logout")
    @ApiOperation("员工退出")
    public Result<String> logout() {
        return Result.success();
    }
}
```

这些注解的作用就是：

> 让 doc.html 里显示更友好的中文说明，比如“员工登录时传递的数据模型”“用户名”“jwt令牌”等。

你现在需要确认的就是：

1. 这些注解已经写上去了；
2. 项目能正常启动；
3. `http://localhost:8080/doc.html` 能打开，并看到“员工相关接口”。

---

## 四、结合你现在的进度，你可以这样一步步做

1. **先把数据库 admin 的密码改成 MD5：**

   ```text
   e10adc3949ba59abbe56e057f20f883e
   ```

2. **确认 `EmployeeServiceImpl.login` 里已经对前端密码做了 MD5：**

   ```java
   password = DigestUtils.md5DigestAsHex(password.getBytes());
   if (!password.equals(employee.getPassword())) {
       throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
   }
   ```

3. **启动项目 → 打开 `http://localhost:8080/doc.html`：**

   * 找到“员工登录”接口，測一下 `admin / 123456`。
   * 看返回是否是你刚才贴的那种 JSON（有 token）。

4. **拿到 token 后 → 再在 doc.html 里測“新增员工”接口：**

   * 在请求头里带上 token（和我们刚才 curl 带 header 的原理一样）。

如果你愿意，下一步你可以把你现在的 `EmployeeServiceImpl.login` 和 `employee` 表里的那条 admin 数据截图/SQL 发给我，我帮你做一遍「从明文 → MD5 → 登录成功」的逐行检查。
