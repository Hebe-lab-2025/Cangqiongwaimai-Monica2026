这个报错现在就只说明一件事：**编译器在你用的那个 `EmployeeService` 接口里，没找到 `login(EmployeeLoginDTO)` 这个方法**。

> ⚠️ 注意：不是“你觉得有”，而是“编译器那份源码里没有”。

我直接给你一套“保底版本”，你照着改成这样就不会再有这个错误了。

---

## 1️⃣ 把 EmployeeService 接口改成下面这样

文件路径要是：

`sky-server/src/main/java/com/sky/service/EmployeeService.java`

内容直接用这一版覆盖就行（注意 package 和 import）：

```java
package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;

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

    // 其他方法以后再慢慢加，比如分页查询、启用禁用等等
}
```

关键点：

* **package 一定是 `com.sky.service`**
* 一定要有这个签名：
  `Employee login(EmployeeLoginDTO employeeLoginDTO);`
  参数类型、顺序、名字都无所谓，但**类型必须是 `com.sky.dto.EmployeeLoginDTO`**

---

## 2️⃣ 确保实现类也匹配这个签名（防止你写成别的）

文件路径一般是：

`sky-server/src/main/java/com/sky/service/impl/EmployeeServiceImpl.java`

参考实现（先写一个简单版本占位也行）：

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
        // 这里先写一个最简单的占位逻辑，保证能编译通过
        // 后面你可以按教程完善校验（账号不存在/密码错误/账号锁定）
        log.info("员工登录 service, dto: {}", employeeLoginDTO);
        return employeeMapper.getByUsername(employeeLoginDTO.getUsername());
    }

    @Override
    public void save(EmployeeDTO employeeDTO) {
        // 同样先写一个占位逻辑
        log.info("新增员工 service, dto: {}", employeeDTO);
        // 这里按教程用加密密码、设置状态、时间等再保存
        Employee employee = new Employee();
        // TODO: 拷贝属性
        employeeMapper.insert(employee);
    }
}
```

---

## 3️⃣ 确认 Controller 用的是同一个接口

你的 Controller 里现在是：

```java
import com.sky.service.EmployeeService;
```

这一行很重要，**确认不是导成了别的包里的 EmployeeService**（比如 `...xxx.EmployeeService`）。

方法调用保持：

```java
Employee employee = employeeService.login(employeeLoginDTO);
```

就行。

---

## 4️⃣ 清理 + 重新编译

1. 先让 IDEA 刷新 Maven：

   * 右侧 Maven 面板 → `Reload All Maven Projects`

2. 在项目根目录执行：

   ```bash
   mvn clean compile -DskipTests
   ```

如果还是 **同样的错误**，那极大可能是下面几种情况之一，你可以快速检查：

### ✅ 情况 A：项目里有两个 EmployeeService

在 IDEA 里：

* `⌘ + Shift + O` / `Ctrl + N` 搜索 `EmployeeService`
* 看看是不是有两个同名接口，且 Controller 不小心 import 了另一个

解决：删除多余的 / 改包名，**只保留 `com.sky.service.EmployeeService`**。

### ✅ 情况 B：EmployeeLoginDTO 包不一致

确认：

* DTO 文件在：`com.sky.dto.EmployeeLoginDTO`
* Controller 和 Service 的 import 都是：

  ```java
  import com.sky.dto.EmployeeLoginDTO;
  ```

不要搞成别的比如 `com.sky.xxx.EmployeeLoginDTO`。

---

## 5️⃣ 一句话把这类错误记住（以后遇到就不慌）

> **`cannot find symbol method xxx(..)`**
> ➜ 编译器在“当前这个类型声明里”没找到对应方法
> ➜ 一定是：接口没声明 / 签名不一致 / 导错包 / 有多个同名接口

---

你现在按这三步来：

1. 用我给你的版本覆盖 `EmployeeService.java`
2. 确认 `EmployeeServiceImpl` 实现了 `login(EmployeeLoginDTO)`
3. Maven clean compile

如果还有新的红字，把最新那几行 `[ERROR]` 再发给我，我们继续往下排。
