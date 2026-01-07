很好，现在项目已经跑到**业务层面的小问题**了 👍
这次错误非常简单直观：

```
java: cannot find symbol
symbol:   variable jwtProperties
location: class EmployeeController
```

👉 **意思：你在用 `jwtProperties` 这个变量，但你没有定义它。**

---

## ✅1. 这个对象本来应该是什么？

在你代码里有这一段：

```java
String token = JwtUtil.createJWT(
        jwtProperties.getAdminSecretKey(),
        jwtProperties.getAdminTtl(),
        claims);
```

说明需要一个：

* 名字叫：`jwtProperties`
* 类型：`JwtProperties`
* 来源：配置文件 application.yml
* 作用：读取 JWT 密钥、过期时间

也就是说项目里应该有类似：

```yaml
jwt:
  admin-secret-key: xxx
  admin-ttl: 7200000
```

---

## ✅2. 你之前已经有 `JwtProperties` 类（在 sky-common 里）

通常像这样👇

```java
package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String adminSecretKey;

    private Long adminTtl;

    private String adminTokenName;
}
```

如果你没有这个类，我可以帮你补；但从你报错历史看，这个类是存在的。

---

## ✅3. 控制器里只需要：把它注入即可

👉 在 `EmployeeController` 里，加上这几行字段 + 注解：

```java
import com.sky.properties.JwtProperties;

@Autowired
private JwtProperties jwtProperties;
```

完整示例：

```java
@RestController
@RequestMapping("/admin/employee")
@Api(tags = "员工管理")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private JwtProperties jwtProperties;   // ⭐⭐ 关键：把配置类注入进来

    ...
}
```

---

## ⚠️4. 如果依然注入失败，检查一件事

👉 `JwtProperties` 是否带这两个注解：

```java
@Component
@ConfigurationProperties(prefix = "jwt")
```

如果**缺任意一个**都会有问题。

---

## 🧪5. 再次运行/编译

```bash
mvn clean install -DskipTests
```

---

## 🎉6. 总结你刚踩的点（面试常考）

> Spring Boot 读取自定义配置属性，需要：
>
> 1. 写一个配置类 `JwtProperties`
> 2. `@ConfigurationProperties(prefix="jwt")`
> 3. `@Component` 或 `@EnableConfigurationProperties`
> 4. 在业务类中 `@Autowired` 注入使用

---

### 👉 你下一步如果还报

* `Cannot resolve symbol XxxDTO`
* `Cannot resolve symbol XxxVO`

那就是：

> 该 VO / DTO 还没建
> ➜ 放到 sky-pojo 模块
> ➜ 包名 com.sky.dto / com.sky.vo
> ➜ 加 Lombok @Data @Builder

---

把 `jwtProperties` 注入加上去后如果还有报错，继续把红字发我，我们一路清零到启动成功为止 💪
