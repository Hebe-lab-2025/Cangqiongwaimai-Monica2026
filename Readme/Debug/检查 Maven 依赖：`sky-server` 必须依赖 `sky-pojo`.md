OK，那我们现在假设：**类和方法的代码真的都在**，但 IDEA 还是一片红。
这种情况一般不是“没写”，而是 **IDEA / Maven / 包结构 / 依赖 链路有问题**。

我帮你按“最常见原因”给你一个 checklist，你可以一条条对。

---

## 0. 先确认下：这些类现在在哪个模块？

你现在是多模块项目：

* `sky-common`
* `sky-pojo`
* `sky-server`（Controller / Service 写在这里）

**Employee / EmployeeLoginDTO / EmployeeLoginVO** 理论上应该在：

* `Employee` → `sky-pojo/src/main/java/com/sky/entity/Employee.java`
* `EmployeeLoginDTO` → `sky-pojo/src/main/java/com/sky/dto/EmployeeLoginDTO.java`
* `EmployeeLoginVO` → `sky-pojo/src/main/java/com/sky/vo/EmployeeLoginVO.java`

而 `EmployeeController / EmployeeServiceImpl` 在：

* `sky-server/src/main/java/...`

如果路径对，但 `sky-server` 依赖里**没引入 `sky-pojo`**，IDEA 也会报 “Cannot resolve symbol”。

👉 所以后面第 2 步会专门检查 pom。

---

## 1️⃣ 检查 **package + 类名** 是否完全匹配

打开这几个类文件，顶上三行一定要像这样：

```java
// Employee.java
package com.sky.entity;

// EmployeeLoginDTO.java
package com.sky.dto;

// EmployeeLoginVO.java
package com.sky.vo;
```

常见坑：

* 路径是 `com/sky/entity/Employee.java`，但是 package 写成了 `package com.sky.entity.xxx;` ❌
* 文件名改了，类名没改，IDEA 也会疯：

  * 文件叫 `Employee.java`，类是 `public class EmployeeLoginDTO {}` ❌

**你可以快速 check：**

* 在 IDEA 里把光标放在类名上（比如 `Employee`），看文件名和 package 是否一致。

---

## 2️⃣ 检查 Maven 依赖：`sky-server` 必须依赖 `sky-pojo`

1. 打开 **根 pom.xml**，确认里面有：

```xml
<modules>
    <module>sky-common</module>
    <module>sky-pojo</module>
    <module>sky-server</module>
</modules>
```

2. 打开 **`sky-server/pom.xml`**，确认有：

```xml
<dependencies>
    <dependency>
        <groupId>com.sky</groupId>
        <artifactId>sky-pojo</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>

    <dependency>
        <groupId>com.sky</groupId>
        <artifactId>sky-common</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>

    <!-- 其他依赖 -->
</dependencies>
```

如果少了 `sky-pojo` 这段：

* `sky-server` 里引用任何 `com.sky.entity.Employee`、`com.sky.vo.EmployeeLoginVO`，IDEA 全都会报 **Cannot resolve symbol**。

3. Ctrl 左键点一下 `Employee` 的 import，看看能不能跳进实体类：

```java
import com.sky.entity.Employee;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.vo.EmployeeLoginVO;
```

* 能跳 → 依赖、包应该是 OK 的
* 跳不进去 / 不是这个包 → 要么 import 错，要么没依赖

---

## 3️⃣ 检查 **import 用的是不是对的那个类**

在 `EmployeeController` / `EmployeeServiceImpl` 中，顶部 import 应该类似：

```java
import com.sky.dto.EmployeeLoginDTO;
import com.sky.vo.EmployeeLoginVO;
import com.sky.entity.Employee;
import com.sky.result.Result;
import com.sky.properties.JwtProperties;
import com.sky.service.EmployeeService;
```

常见坑：

* 不小心 import 到别的包（比如 `xxx.Employee`）
* 之前重命名文件后，旧的 import 没改

你可以：

* 把 `Employee` 删除，再打 `Employee`，然后 `Alt+Enter` 选 `Import class`，看 IDEA 提供的候选是不是 `com.sky.entity.Employee`。

---

## 4️⃣ jwtProperties：确认字段 + 注入方式

“Cannot resolve symbol `jwtProperties`” → 根本没有这个字段。

在你使用 JWT 的类里（Controller 或 ServiceImpl）：

```java
import com.sky.properties.JwtProperties;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/admin/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private JwtProperties jwtProperties;

    // ...
}
```

**两点必看：**

1. 上面有没有 `private JwtProperties jwtProperties;`
2. 有没有对应 import：`com.sky.properties.JwtProperties;`

如果你用的是构造器注入，也要保证字段名叫 `jwtProperties`，否则你写 `jwtProperties.getAdminSecretKey()` 就会报错。

---

## 5️⃣ getId() / getUsername() / getName()：看变量真实类型

即使类里有这些字段，也可能：

* 你当前调用的是 **DTO 类型**，DTO 里根本就没 id/name。

示例对比：

```java
// ✅ 正确：employee 是 Employee 类型
Employee employee = employeeMapper.getByUsername(username);
employee.getId();
employee.getName();

// ❌ 错：employeeLoginDTO 是 EmployeeLoginDTO 类型
EmployeeLoginDTO employeeLoginDTO = ...
employeeLoginDTO.getId();    // DTO 没有 id，IDEA 会报 Cannot resolve method 'getId'
```

👉 建议你：

* 光标放在 `employee` 上，看 IDEA 提示类型是什么：

  * 如果是 `Employee` 或 `EmployeeLoginVO` → 那就检查这些类里有没有字段 + `@Data`
  * 如果是 `EmployeeLoginDTO` → 就是你用错对象了

---

## 6️⃣ IDEA / Maven 侧的“硬刷新”

既然你说「里面内容都有」，那剩下就是让 IDEA **重新认一遍项目**：

1. 在 IDEA 右侧 **Maven** 面板：

   * 点一下项目顶层
   * 点 **🔄 “Reload All Maven Projects”**

2. 终端跑一次：

```bash
cd /Users/yonggan/Downloads/sky-take-out-Monica-2026/sky-take-out-Monica
mvn clean install
```

3. 如果还是一片红，可以用“大招”：

```
File → Invalidate Caches… → Invalidate and Restart
```

---

## 7️⃣ 那些可以先不用管的 Warning

这些不会影响你现在把项目跑起来，可以以后慢慢优化：

* `'@param employeeLoginDTO' tag description is missing`
* `'@return' tag description is missing`
* `Raw use of parameterized class 'Result'` → 以后改成 `Result<EmployeeLoginVO>`
* `Field injection is not recommended` → 以后改构造器注入
* `Typo: In word '调用service方法查询数据库'` → IDEA 英文拼写检查器看不懂中文 😂

---

## ✅ 总结一句话

既然你说 **类和方法都已经写好了**，那现在最可能的问题是：

1. `sky-server` 的 `pom.xml` 里 **没依赖 `sky-pojo`**，或者根 pom 少模块；
2. 某些类的 **package / import 不匹配**；
3. 当前变量的 **真实类型不是你以为的那个类**（DTO vs Entity/VO）。

你可以照着上面的 7 步一点点点过去，
如果跑完之后 **某一两个具体地方还红着**，你可以把那一小段（比如 `EmployeeController.login` 方法）单独贴出来，我帮你对着改到 0 红线版本。
