```
Cannot resolve symbol 'Employee'
Cannot resolve symbol 'EmployeeLoginVO'
Cannot resolve symbol 'Employee'
Cannot resolve method 'login' in 'EmployeeService'
Cannot resolve method 'getId()'
Cannot resolve symbol 'jwtProperties'
Cannot resolve symbol 'jwtProperties'
Cannot resolve symbol 'EmployeeLoginVO'
Cannot resolve symbol 'EmployeeLoginVO'
Cannot resolve method 'getId()'
Cannot resolve method 'getUsername()'
Cannot resolve method 'getName()'
'@param employeeLoginDTO' tag description is missing
'@return' tag description is missing
Found problems related to 'login(EmployeeLoginDTO)'
Raw use of parameterized class 'Result'
Field injection is not recommended
Typo: In word '调用service方法查询数据库'
```

好，这一堆红线其实可以分成三类问题：
1）**真正会编译失败的**（必须修）
2）**泛型 / 注入方式的代码风格问题**（可以先忽略）
3）**Javadoc 和中文拼写**（完全不影响运行）

我按“优先级 + 操作步骤”帮你理一遍，你可以照着一步一步改。

---

## 一、最关键：Employee / EmployeeLoginVO / login 全家桶 ❌


这些错误都说明：**类/方法不存在或改坏了**。

> Cannot resolve symbol `Employee`
> Cannot resolve symbol `EmployeeLoginVO`
> Cannot resolve method `login` in `EmployeeService`
> Cannot resolve method `getId()` / `getUsername()` / `getName()`

结合你刚才的 commit 记录：

```text
delete mode 100644 sky-pojo/src/main/java/com/sky/dto/EmployeeDTO.java
delete mode 100644 sky-pojo/src/main/java/com/sky/dto/EmployeeLoginDTO.java
rename sky-pojo/src/main/java/com/sky/entity/{Employee.java => EmployeeLoginDTO.java}
```

说明你当时误操作了：

* **删掉了 DTO**
* 把 `Employee.java`（实体类）**改名成了 `EmployeeLoginDTO.java`**

👉 对于苍穹外卖这个项目，正确结构应该是：

* `sky-pojo/src/main/java/com/sky/entity/Employee.java`
* `sky-pojo/src/main/java/com/sky/dto/EmployeeLoginDTO.java`
* `sky-pojo/src/main/java/com/sky/vo/EmployeeLoginVO.java`

### ✅ 第 1 步：恢复 Employee 实体类

在这个路径下新建文件：

> `sky-pojo/src/main/java/com/sky/entity/Employee.java`

内容可以先用一个“精简版”，只要有 id、username、name 就能把 getId / getUsername / getName 这几个错误消掉：

```java
package com.sky.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Employee implements Serializable {

    private Long id;              // 员工id
    private String username;      // 登录账号
    private String name;          // 员工姓名
    private String password;      // 登录密码
    private Integer status;       // 状态 1正常 0锁定
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

> 关键点：
>
> * 包名一定是 `com.sky.entity`
> * 类名一定是 `Employee`
> * 有 `@Data` 或者你生成 getter/setter，否则 getId() 这些会继续报错

---

### ✅ 第 2 步：恢复 EmployeeLoginDTO（登录入参）

路径：

> `sky-pojo/src/main/java/com/sky/dto/EmployeeLoginDTO.java`

```java
package com.sky.dto;

import lombok.Data;

@Data
public class EmployeeLoginDTO {

    private String username;   // 登录账号
    private String password;   // 登录密码
}
```

---

### ✅ 第 3 步：恢复 EmployeeLoginVO（登录出参）

路径：

> `sky-pojo/src/main/java/com/sky/vo/EmployeeLoginVO.java`

```java
package com.sky.vo;

import lombok.Data;

@Data
public class EmployeeLoginVO {

    private Long id;           // 员工id
    private String username;   // 登录账号
    private String name;       // 员工姓名
    private String token;      // JWT token
}
```

---

### ✅ 第 4 步：补上 EmployeeService 的 login 方法

错误：`Cannot resolve method 'login' in 'EmployeeService'`

说明接口里没有这个方法，或者签名不对。

在：

> `sky-server/src/main/java/com/sky/service/EmployeeService.java`

确认内容里有这个：

```java
package com.sky.service;

import com.sky.dto.EmployeeLoginDTO;
import com.sky.vo.EmployeeLoginVO;

public interface EmployeeService {

    /**
     * 员工登录
     */
    EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO);
}
```

对应实现类：

> `sky-server/src/main/java/com/sky/service/impl/EmployeeServiceImpl.java`

要有完全一样的方法签名：

```java
package com.sky.service.impl;

import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.service.EmployeeService;
import com.sky.vo.EmployeeLoginVO;
// 省略其他 import

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Override
    public EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO) {
        // 这里先写个假实现，后面再补逻辑也行
        return null;
    }
}
```

只要接口 + 实现类里都有这个方法，`Cannot resolve method 'login'` 就会消失。

---

### ✅ 第 5 步：Controller 里返回值用对

在 `EmployeeController` 里，大概率是类似这样：

```java
@PostMapping("/login")
public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
    EmployeeLoginVO employeeLoginVO = employeeService.login(employeeLoginDTO);
    return Result.success(employeeLoginVO);
}
```

检查几点：

1. `Result` 要带上泛型：`Result<EmployeeLoginVO>`，否则会有 `Raw use of parameterized class 'Result'`
2. import 的类型要对：

```java
import com.sky.dto.EmployeeLoginDTO;
import com.sky.vo.EmployeeLoginVO;
import com.sky.result.Result;
```

---

## 二、jwtProperties 相关 ❌

> Cannot resolve symbol `jwtProperties`

说明你在某个类里写了类似：

```java
jwtProperties.getAdminSecretKey()
```

但：

* 要么没有字段 `private JwtProperties jwtProperties;`
* 要么没 `@Autowired`
* 要么包没导对

### ✅ 做法

在使用的类里（一般是 `EmployeeController` 或 `EmployeeServiceImpl`）加字段：

```java
import com.sky.properties.JwtProperties;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
public class EmployeeController {

    @Autowired
    private JwtProperties jwtProperties;

    // ...
}
```

确保：

* `JwtProperties` 类存在：`sky-common/src/main/java/com/sky/properties/JwtProperties.java`
* `sky-server/pom.xml` 里依赖了 `sky-common`

如果你之前刚改过 pom，建议再跑一次：

```bash
mvn clean install
```

然后在 IDEA 里再点击一次 Maven 的刷新按钮（Reload All Maven Projects）。

---

## 三、getId / getUsername / getName ❌

> Cannot resolve method `getId()` / `getUsername()` / `getName()`

常见两种情况：

1. 你现在用的类型是 `EmployeeLoginDTO`，它确实没有这些字段
2. 实体类/VO 里缺字段，或者没有 getter（少了 `@Data`）

苍穹外卖里，这类调用一般是：

```java
employee.getId();
employee.getUsername();
employee.getName();
```

所以要保证：

* `employee` 对象的类型是 **Employee 或 EmployeeLoginVO**，而不是 DTO
* 这两个类里都有对应字段 + `@Data`

如果你搞不清楚是哪个类，可以：

* 在出错的地方把鼠标放在 `employee` 上，看 IDEA 提示的类型
* 如果它是 `EmployeeLoginDTO`，就错了，说明变量类型写错 / Mapper 返回类型写错

---

## 四、代码风格 / 警告类问题（可以先忽略，后面慢慢改）

这些不会阻止你启动项目：

1. `@param employeeLoginDTO tag description is missing`
   `@return tag description is missing`
   👉 这是 Javadoc 的提示：

   * 要么删掉那一串 `/** ... */`
   * 要么在里面加一句描述就行，比如：

   ```java
   /**
    * 员工登录
    *
    * @param employeeLoginDTO 登录参数（账号、密码）
    * @return 包含员工基础信息和 token 的响应对象
    */
   ```

2. `Raw use of parameterized class 'Result'`
   👉 把 `Result` 改成带泛型：

   ```java
   public Result<EmployeeLoginVO> login(...) { ... }
   ```

3. `Field injection is not recommended`
   👉 这是建议你改用构造器注入，现阶段可以无视；之后可以改成：

   ```java
   @RequiredArgsConstructor
   @RestController
   public class EmployeeController {

       private final EmployeeService employeeService;
       private final JwtProperties jwtProperties;
   }
   ```

4. `Typo: In word '调用service方法查询数据库'`
   👉 英文拼写检查器把中文当英文了，当成 IDE 吐槽就行 😂，可以直接忽略。

---

## 五、推荐操作顺序（你可以照着做）

1. **在 sky-pojo 里恢复三个类：**

   * `entity/Employee.java`
   * `dto/EmployeeLoginDTO.java`
   * `vo/EmployeeLoginVO.java`

2. **确认 EmployeeService 接口 + 实现里都有：**

   ```java
   EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO);
   ```

3. **在 Controller/Service 里注入 JwtProperties：**

   ```java
   @Autowired
   private JwtProperties jwtProperties;
   ```

4. **确认所有 import：**

   * DTO 用 `com.sky.dto.EmployeeLoginDTO`
   * VO 用 `com.sky.vo.EmployeeLoginVO`
   * Entity 用 `com.sky.entity.Employee`

5. IDEA 里点击 Maven 刷新 → 再点一遍 Build / Recompile project。

---

如果你愿意，下一步你可以把 **EmployeeController + EmployeeServiceImpl 的代码贴出来**，我可以一行一行帮你对照「苍穹外卖原版」修到没有一个红线（顺便帮你改成更“面试友好”的写法 😄）。
