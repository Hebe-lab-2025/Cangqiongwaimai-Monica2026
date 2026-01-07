太好了，这个报错**终于正常了**，现在是一个很标准的 Java 接口实现问题 👇

> `EmployeeServiceImpl is not abstract and does not override abstract method save(EmployeeDTO)`

意思就是：

* 你的 `EmployeeService` 接口里现在有：`void save(EmployeeDTO employeeDTO);`
* 但是 `EmployeeServiceImpl` 里 **只有 `login(...)`，没有 `save(...)` 的实现**
* 所以编译器要求：要么类是 `abstract`，要么把所有接口方法实现完。

---

## ✅ 1. 在 EmployeeServiceImpl 里补上 save 方法

在你这段代码下面，加一个 `@Override save` 方法就行了：

```java
package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     */
    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 密码比对（后续可以改成 MD5）
        if (!password.equals(employee.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        // 账号被锁定
        if (employee.getStatus() == StatusConstant.DISABLE) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        // 这里先写一个最简单的版本，保证编译通过
        // 后面你可以按教程补充：设置默认密码、状态、时间等

        Employee employee = new Employee();

        // TODO: 用 BeanUtils.copyProperties 或手动拷贝 DTO 到实体
        // BeanUtils.copyProperties(employeeDTO, employee);

        employeeMapper.insert(employee);
    }
}
```

> 重点：一定要有这一段：

```java
@Override
public void save(EmployeeDTO employeeDTO) { ... }
```

哪怕里面暂时只是个空实现：

```java
@Override
public void save(EmployeeDTO employeeDTO) {
    // TODO 实现保存逻辑
}
```

也可以先让项目**编译通过**。

---

## ✅ 2. 再跑一次编译

在项目根目录执行：

```bash
mvn clean compile -DskipTests
```

* 如果不再报 `EmployeeServiceImpl is not abstract...`，说明接口实现已经对齐；
* 接下来如果有新的错误（比如 mapper、DTO、VO 之类的），再把最新那条红字丢给我，我们继续一点点清掉。

---

### 🎯 小总结（这个点面试经常考）

> 一个类实现接口 `implements EmployeeService`
> 👉 就必须实现接口里**所有抽象方法**
> 👉 否则这个类必须是 `abstract`
> 👉 否则就会报：`is not abstract and does not override abstract method xxx(...)`

你现在做的就是把 `EmployeeService` 里的 `save(...)` 补到实现类里，就 OK 了 💪


很好，这个错误现在非常明确👇

```
java: cannot find symbol
  symbol:   variable ACCOUNT_LOCKED
  location: class com.sky.constant.MessageConstant
```

意思是：

👉 **`MessageConstant` 这个类里没有 `ACCOUNT_LOCKED` 这个常量**
但你在这里使用了：

```java
throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
```

---

## ✅ 解决思路（两种任选其一）

### 👍 方法一：在 `MessageConstant` 中补上这个常量（推荐）

找到：

```
sky-common / com/sky/constant/MessageConstant.java
```

如果你已经有类似：

```java
public class MessageConstant {
    public static final String ACCOUNT_NOT_FOUND = "账号不存在";
    public static final String PASSWORD_ERROR = "密码错误";
}
```

👉 请补充这一行：

```java
public static final String ACCOUNT_LOCKED = "账号被锁定";
```

完整示例：

```java
package com.sky.constant;

public class MessageConstant {

    public static final String ACCOUNT_NOT_FOUND = "账号不存在";

    public static final String PASSWORD_ERROR = "密码错误";

    public static final String ACCOUNT_LOCKED = "账号被锁定";
}
```

保存即可。

---

### 👍 方法二：不用常量，直接写字符串（快速临时修复）

把这行：

```java
throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
```

改成：

```java
throw new AccountLockedException("账号被锁定");
```

👉 这种方法不推荐长期使用，但可以先让项目通过编译。

---

## 🧠 你刚踩的知识点（以后看到就能立刻想到）

> `cannot find symbol variable XXX`
> = 当前类中 **没有这个字段 / 常量**

常见原因：

* 常量没写
* 拼写不一致
* 大小写错误
* 在另一个 Constant 类里
* 导错包了

---

## ✅ 接下来做什么

1. 在 `MessageConstant` 里补一行 `ACCOUNT_LOCKED`
2. 保存
3. 重新编译：

```bash
mvn clean compile -DskipTests
```

---

### 如果之后还有报错

继续把最新的红色错误贴出来
我们继续清理 🔍
