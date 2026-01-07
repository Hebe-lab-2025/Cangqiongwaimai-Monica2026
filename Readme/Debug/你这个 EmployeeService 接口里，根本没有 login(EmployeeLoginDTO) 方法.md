现在这个错误其实非常“诚实”地告诉你一句话：

> **你这个 `EmployeeService` 接口里，根本没有 `login(EmployeeLoginDTO)` 方法。**

你刚才贴出来的 `EmployeeService` 是这样的 👇

```java
package com.sky.service;

import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeeDTO;

public interface EmployeeService {
    /**
     * 新增员工
     * @param employeeDTO
     */
    void save(EmployeeDTO employeeDTO);

}
```

只有 `save`，没有 `login`，所以下面这行当然会报错：

```java
Employee employee = employeeService.login(employeeLoginDTO);
```

---

## 1️⃣ 把 EmployeeService.java 改成这个（整文件覆盖）

> 路径一定要是：`sky-server/src/main/java/com/sky/service/EmployeeService.java`
> 项目里只能有 **这一份** `public interface EmployeeService`。

```java
package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;

/**
 * 员工相关业务接口
 */
public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO 登录信息
     * @return 员工实体
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工
     * @param employeeDTO 员工信息
     */
    void save(EmployeeDTO employeeDTO);
}
```

**一定要注意：**

* 有这行：`Employee login(EmployeeLoginDTO employeeLoginDTO);`
* 有这两个 import：`EmployeeDTO` 和 `EmployeeLoginDTO`、`Employee`

---

## 2️⃣ 确保实现类也实现了这个方法

`sky-server/src/main/java/com/sky/service/impl/EmployeeServiceImpl.java` 至少要有：

```java
@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        // TODO: 按教程写登录逻辑，这里给个最简单占位也行
        return null;
    }

    @Override
    public void save(EmployeeDTO employeeDTO) {
        // 保存逻辑
    }
}
```

（先写 `return null;` 也可以，重点是 **方法签名要存在**，先让项目编译通过再慢慢完善逻辑。）

---

## 3️⃣ 清一下再编译

1. IDEA 右侧 Maven 面板 → 点一下 **Reload All Maven Projects**
2. 在项目根目录执行：

```bash
mvn clean compile -DskipTests
```

---

## 4️⃣ 一句记忆：

> `cannot find symbol method login(...)`
> = “当前这个接口类型里，没有这个方法签名”
> → 看接口定义是不是少了，或者导错接口。

你现在只要真的按上面把 **EmployeeService.java 整个替换成那一版**，这个错误就会消失。
如果消失后有新的红字，再发给我，我们继续往下清。
