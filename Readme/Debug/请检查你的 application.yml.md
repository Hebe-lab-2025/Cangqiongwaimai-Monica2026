很好 👍 你的 `JwtProperties` 类本身写得是**正确的、标准的 Spring Boot 配置属性类**。

但是，现在有一个**最关键的小点**👇
👉 你的前缀写的是：

```java
@ConfigurationProperties(prefix = "sky.jwt")
```

而在 Controller 里，你是这样调用的：

```java
jwtProperties.getAdminSecretKey();
jwtProperties.getAdminTtl();
```

所以要确保 **application.yml 的配置前缀完全一致** ⚠️
否则会出现：

* 注入不报错，但值是 null
* token 生成失败或 NPE

---

## ✅ 一、请检查你的 application.yml

保证是这种结构👇

```yaml
sky:
  jwt:
    admin-secret-key: your-admin-secret
    admin-ttl: 7200000
    admin-token-name: token

    user-secret-key: your-user-secret
    user-ttl: 7200000
    user-token-name: user-token
```

注意三点：

1. 用短横线形式（Spring Boot 自动映射到驼峰）
2. 层级和 `sky.jwt` 必须对应
3. key 名必须与属性名语义一致

---

## ✅ 二、Controller 里保证已经加了注入

在 `EmployeeController` 里加 👇

```java
import com.sky.properties.JwtProperties;

@Autowired
private JwtProperties jwtProperties;
```

---

## ✅ 三、你的属性类没问题，可以保持这样

```java
@Component
@ConfigurationProperties(prefix = "sky.jwt")
@Data
public class JwtProperties {
    private String adminSecretKey;
    private long adminTtl;
    private String adminTokenName;

    private String userSecretKey;
    private long userTtl;
    private String userTokenName;
}
```

👍 解释一下（帮你面试用）

* `@ConfigurationProperties` → **把 yml 配置批量绑定成对象**
* `prefix="sky.jwt"` → 映射 sky.jwt.* 下的所有配置
* `@Component` → 让 Spring 扫描并交给 IOC 容器管理
* `@Data` → Lombok 生成 getter/setter/toString

---

## 🔍 四、如果还是报错，有 3 个可能

### 1️⃣ IDEA 没打开 annotation-processor

路径：

```
Preferences / Settings
→ Build, Execution, Deployment
→ Compiler
→ Annotation Processors
→ 勾选 Enable annotation processing
```

---

### 2️⃣ sky-common 模块没被扫描

确保：

* `@SpringBootApplication` 在 sky-server
* basePackages **包含 com.sky**

比如👇

```java
@SpringBootApplication(scanBasePackages = "com.sky")
public class SkyApplication { }
```

---

### 3️⃣ 还报 cannot find symbol jwtProperties？

那就是：**你忘了写字段**

👉 再确认你 Controller 里有👇

```java
@Autowired
private JwtProperties jwtProperties;
```

---

## 🎉 总结你现在的进展

你已经完成了：

* Maven 多模块修复
* 依赖管理梳理
* sky-common 依赖补齐
* JWT 工具修复
* VO/DTO 不存在问题
* 配置类 properties 绑定问题

👉 现在处于 **业务层联调阶段**，非常接近成功启动 🚀

---

### 如果你愿意，接下来我还可以帮你：

* ✅ 完整生成 application.yml jwt 配置
* ✅ 检查 JwtUtil.createJWT 是否正确
* ✅ 一起跑登录接口
* ✅ 用 Postman / Thunder Client 调试
* ✅ 讲解登录流程 + JWT 验证器 + 拦截器（面试必考）

你把现在 **最新一条报错/截图** 发我，我们继续清 😊
