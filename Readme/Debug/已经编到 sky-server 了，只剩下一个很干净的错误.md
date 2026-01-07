太好了，现在已经编到 **sky-server** 了，只剩下一个很干净的错误：

```text
EmployeeController.java:37  cannot find symbol class EmployeeLoginVO
```

意思就是：**你在用 `EmployeeLoginVO`，但这个类还没被定义**（或不在正确的包/模块）。

---

## 1️⃣ 先看一下你 Controller 里是怎么用它的

从截图里看到：

```java
public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
    ...
    EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
            .id(employee.getId())
            .userName(employee.getUsername())
            .name(employee.getName())
            .token(token)
            .build();
    ...
}
```

所以我们可以反推出：

* `EmployeeLoginVO` 需要有字段：`id`, `userName`, `name`, `token`
* 用的是 `builder()` → 最方便的就是用 Lombok 的 `@Builder`

---

## 2️⃣ 正确做法：在 **sky-pojo 模块** 里新建 VO 类

因为 VO/DTO 这类一般都放在 pojo 模块，所以我们这样建：

**文件位置：**

`sky-pojo/src/main/java/com/sky/vo/EmployeeLoginVO.java`

**文件内容：**

```java
package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 员工登录返回对象（VO）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeLoginVO implements Serializable {

    /**
     * 员工 id
     */
    private Long id;

    /**
     * 员工用户名
     */
    private String userName;

    /**
     * 员工姓名
     */
    private String name;

    /**
     * 登录成功返回的 JWT token
     */
    private String token;
}
```

---

## 3️⃣ 确认 Controller 里的 import

在 `EmployeeController.java` 顶部，确保有这一行：

```java
import com.sky.vo.EmployeeLoginVO;
```

如果 IDE 还没自动加，光标放在 `EmployeeLoginVO` 上按 `Alt+Enter`（或等效快捷键）让它自动导入即可。

---

## 4️⃣ 再跑一遍 Maven

在项目根目录执行：

```bash
mvn clean install -DskipTests
```

* 如果还有错误，看新的第一条 `[ERROR]` 再贴给我；
* 如果编译通过，你的多模块工程（依赖 + 工具类 + VO）就算是完整跑起来了 🎉

现在这个错误已经不再是 Maven 配置问题，而是“**教程里用到的 VO 类要自己补出来**”，后面遇到类似的 `Cannot find symbol XXXVO/XXXDTO` 都是同一个思路处理。
