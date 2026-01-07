现在这个报错就只说明一件事：
👉 **`EmployeeService` 这个接口里，编译器没看到 `login(EmployeeLoginDTO)` 这个方法。**

不管你觉得有没有，**只看编译器眼里的那份源码**。我们直接用“保底版本”，把接口和实现都改成我给你的样子，就能消掉这个错。

---

## 1️⃣ 把 `EmployeeService` 接口改成下面这样（完整覆盖）

文件路径一定要是：

`sky-server/src/main/java/com/sky/service/EmployeeService.java`

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
     * @param employeeLoginDTO 登录参数
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

关键点：

* `package` 必须是 `com.sky.service`
* `import com.sky.dto.EmployeeLoginDTO;`
* 方法签名必须 **一模一样**：

```java
Employee login(EmployeeLoginDTO employeeLoginDTO);
```

---

## 2️⃣ 把 `EmployeeServiceImpl` 也改成跟接口一致

文件路径：

`sky-server/src/main/java/com/sky/service/impl/EmployeeServiceImpl.java`

示例实现（你可以先用这个占位，后面再完善逻辑）：

```java
package com.sky.service.impl;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.mapper.EmployeeMapper;
import com.sky.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录 service, dto: {}", employeeLoginDTO);

        // 这里只写最简单逻辑，保证能编译通过
        // 按用户名查一下（方法名按你 mapper 实际情况改）
        Employee employee = employeeMapper.getByUsername(employeeLoginDTO.getUsername());

        // TODO: 按教程加上账号不存在/密码错误/被锁定等校验
        return employee;
    }

    @Override
    public void save(EmployeeDTO employeeDTO) {
        log.info("新增员工 service, dto: {}", employeeDTO);
        // TODO: 按教程实现新增逻辑（拷贝属性、加密密码、设置状态等）
        Employee employee = new Employee();
        // ... copy properties
        employeeMapper.insert(employee);
    }
}
```

---

## 3️⃣ 确认 Controller 用的是同一个接口

在 `EmployeeController` 顶部，保证是这行：

```java
import com.sky.service.EmployeeService;
```

而不是别的包里的 EmployeeService。

方法调用保持：

```java
Employee employee = employeeService.login(employeeLoginDTO);
```

不要写成 `EmployeeService.login(...)`（静态调用）或 `Login`（大小写不同）。

---

## 4️⃣ 清一下缓存再编译

1. 右侧 Maven 面板 → 点一次 **Reload All Maven Projects**
2. 根目录执行：

```bash
mvn clean compile -DskipTests
```

---

## 5️⃣ 如果还是报同样的错，快速自查 3 件事

1. **是否有多个 EmployeeService 接口**

   * 用 IDE 搜索 `EmployeeService` 看是不是有不止一个
   * 保留 `com.sky.service.EmployeeService`，删掉或改名其他的
   * Controller import 的必须是这个包

2. **DTO 包是否一致**

   * 接口、实现、Controller 里都要 `import com.sky.dto.EmployeeLoginDTO;`

3. **方法名字/参数是否完全一致**

   * 接口和实现都是 `login(EmployeeLoginDTO employeeLoginDTO)`

---

做到上面这几步，这个 `cannot find symbol method login(...)` 一定会消失。
如果消失后出现新的别的错误，再把新的红字贴给我，我们继续往下清 💪
