先说重点：
**这个 swagger.json 是“接口说明书”，证明你的接口 `/admin/employee/login` 已经被 SpringBoot + Swagger 正常扫描到了；
但 500 还是说明：真正登录这条链路里，某一行 Java 代码抛异常了。**

也就是说：
✅ 接口有了
❌ 具体执行时还是“炸了”

---

## 1. 这个 swagger.json 说明了什么？

你贴的这段里，关键是这块：

```json
"/admin/employee/login": {
  "post": {
    "tags": ["员工管理"],
    "summary": "login",
    "parameters": [
      {
        "in": "body",
        "name": "employeeLoginDTO",
        "required": true,
        "schema": { "$ref": "#/definitions/EmployeeLoginDTO" }
      }
    ],
    "responses": {
      "200": {
        "schema": { "$ref": "#/definitions/Result«EmployeeLoginVO»" }
      }
    }
  }
}
```

配合下面：

* `EmployeeLoginDTO`：有 `username`、`password`
* `EmployeeLoginVO`：有 `id`、`name`、`userName`、`token`

说明：

* 控制器 `EmployeeController.login(...)` 已经存在
* `EmployeeLoginDTO`、`EmployeeLoginVO` 这两个类也**编译通过**了（否则 swagger 起不来）
* `Result<EmployeeLoginVO>` 泛型也认识

所以之前你 IDE 里的那些 “Cannot resolve symbol EmployeeLoginVO / login” 等问题，现在基本说明：
👉 **项目已经能跑起来了**，只是“运行时逻辑”出错（500）。

---

## 2. 500 的真正线索在哪？（必须看控制台）

浏览器 / swagger 只会给你这个简化版：

```json
{
  "timestamp": 1767839370189,
  "status": 500,
  "error": "Internal Server Error",
  "path": "/admin/employee/login"
}
```

真正关键的是 **IDE / 终端里那一长串红字**，例如类似这样：

```text
java.lang.NullPointerException: ...
    at com.sky.controller.admin.EmployeeController.login(EmployeeController.java:xx)
Caused by: org.apache.ibatis.binding.BindingException: Invalid bound statement (not found): com.sky.mapper.EmployeeMapper.getByUsername
    at ...
```

你现在要做的第一件事：

1. 再点一次登录（让它 500 一次）。
2. 回到 IDEA 的 Run Console 或终端窗口，**从第一行 `Exception` 开始，到尾巴全部复制**。
3. 这段红字就是“答案”。

我下面先根据你这个项目最常见的错误，帮你列一个最可能的排查清单，你可以边对照控制台的关键词边查。

---

## 3. 苍穹外卖登录链路长什么样？

典型代码结构（简化版）：

```java
// 1. Controller
@PostMapping("/admin/employee/login")
public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO dto) {
    Employee employee = employeeService.login(dto);

    // 这里生成 token（可能用到 jwtProperties）
    String token = JwtUtil.createJWT(..., jwtProperties.getSecretKey());

    EmployeeLoginVO vo = EmployeeLoginVO.builder()
            .id(employee.getId())
            .userName(employee.getUsername())
            .name(employee.getName())
            .token(token)
            .build();

    return Result.success(vo);
}
```

```java
// 2. Service
public Employee login(EmployeeLoginDTO employeeLoginDTO) {
    String username = employeeLoginDTO.getUsername();
    String password = employeeLoginDTO.getPassword();

    Employee employee = employeeMapper.getByUsername(username);

    if (employee == null) {
        throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
    }

    password = DigestUtils.md5DigestAsHex(password.getBytes());
    if (!password.equals(employee.getPassword())) {
        throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
    }

    if (employee.getStatus() == StatusConstant.DISABLE) {
        throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
    }

    return employee;
}
```

```java
// 3. Mapper 接口
@Mapper
public interface EmployeeMapper {
    Employee getByUsername(String username);
}
```

```xml
<!-- 4. 对应 XML -->
<select id="getByUsername" resultType="com.sky.entity.Employee">
    select * from employee where username = #{username}
</select>
```

**这条链路任意一环写错，都会变成你看到的 500。**

---

## 4. 根据你之前的情况，几个“高概率雷区”

### 🔥 雷 1：`jwtProperties` 为 null

你之前有报：

> Cannot resolve symbol 'jwtProperties'

如果你现在是这么写的：

```java
@Autowired
private JwtProperties jwtProperties;
```

但：

* `JwtProperties` 类上没有 `@ConfigurationProperties(prefix = "sky.jwt")`
* 或者没有在配置类里 `@EnableConfigurationProperties(JwtProperties.class)`
* 或 `application.yml` 没有对应的 `sky.jwt.admin-secret-key` 等字段

那么一旦执行到：

```java
jwtProperties.getAdminSecretKey()
```

就会出现：

```text
java.lang.NullPointerException: Cannot invoke "com.sky.properties.JwtProperties.getAdminSecretKey()" because "this.jwtProperties" is null
```

👉 解决思路：

1. 检查 `JwtProperties` 类，类似这样：

   ```java
   @Data
   @Component
   @ConfigurationProperties(prefix = "sky.jwt")
   public class JwtProperties {
       private String adminSecretKey;
       private long adminTtl;
       private String adminTokenName;
   }
   ```

2. `application.yml` 里要有：

   ```yaml
   sky:
     jwt:
       admin-secret-key: yourSecret
       admin-ttl: 7200000
       admin-token-name: token
   ```

3. Controller 里注入没写错包。

---

### 🔥 雷 2：`EmployeeMapper` / XML 绑定错误

最典型报错：

```text
org.apache.ibatis.binding.BindingException: Invalid bound statement (not found): com.sky.mapper.EmployeeMapper.getByUsername
```

原因：

* 接口全限定名和 XML 里的 `namespace` 不一致
* 方法名对不上

检查点：

1. `EmployeeMapper.java` 的包：`package com.sky.mapper;`

2. `EmployeeMapper.xml` 顶部：

   ```xml
   <mapper namespace="com.sky.mapper.EmployeeMapper">
   ```

3. XML 中 `<select id="getByUsername" ...>`，id 必须叫 `getByUsername`。

---

### 🔥 雷 3：`@MapperScan` / `@Mapper` 没生效

现象：

* 控制台里出现 `NullPointerException`，指向 `employeeMapper.getByUsername(...)` 那一行
* 上面那行说的是 `this.employeeMapper` 为 null

检查：

1. 启动类是否有：

   ```java
   @MapperScan("com.sky.mapper")
   @SpringBootApplication
   public class SkyApplication { ... }
   ```

2. 或者在 `EmployeeMapper` 上有 `@Mapper` 注解。

---

### 🔥 雷 4：数据库 / 表 / 默认用户问题

常见报错关键字：

* `CommunicationsException` / `Access denied for user`
  → 数据库地址/用户名/密码错了

* `Table 'sky.employee' doesn't exist`
  → 没建 employee 表，或库名不对

* 查出来 `employee == null`，但你没捕获 / 全局异常没处理好，最后也可能变 500

你可以先在数据库里确认：

```sql
select * from employee;
```

是否有一条默认管理员账号（一般教程里会给导入脚本）。

---

## 5. 你现在可以按这个顺序自检一下

1. **看控制台红字**，找出异常类型和第一行 `at com.sky...` 的代码行。
2. 对照上面的几个“雷”：

   * 如果看到 `NullPointerException` + `jwtProperties` → 查 JWT 配置
   * 如果看到 `BindingException Invalid bound statement` → 查 Mapper + XML
   * 如果是 `CommunicationsException` / `Table xxx` → 查数据库
3. 修完之后，再在 swagger 里用 `Try it out`：

   * body 填：

     ```json
     {
       "username": "admin",
       "password": "123456"
     }
     ```

   * 看是否：

     * 返回 `code: 1, data: { id, name, userName, token }` ✅
     * 还是继续 500 ❌

---

## 6. 如果你愿意，把这一段贴给我

> 从控制台里第一行 `Exception` 开始，到最后几行 `Caused by` 一并复制

我可以直接帮你标出来：“是哪一行、哪个类、哪种错误”，再给你精准改法。
不过在你贴之前，你就可以先用上面这几个高频雷区，对照关键词自己扫一遍，很容易就能把 500 定位掉。 💻🪛
