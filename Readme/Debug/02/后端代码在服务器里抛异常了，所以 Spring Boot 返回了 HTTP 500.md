这个 JSON 的意思其实就一句话：**你调 `/admin/employee/login` 这个接口时，后端代码在服务器里抛异常了，所以 Spring Boot 返回了 HTTP 500。**

拆一下字段：

```json
{
  "timestamp": 1767838909274,   // 出错时间（毫秒时间戳）
  "status": 500,                // HTTP 状态码 500 = 服务器内部错误
  "error": "Internal Server Error", // 文本说明：服务器内部错误
  "path": "/admin/employee/login"   // 出错的接口路径
}
```

重点：
👉 **`500` 不是前端参数问题，而是后端 Java 代码 / Mapper / 数据库里抛错了。**

---

## 接下来怎么排查？（必须看“红字日志”）

现在浏览器只告诉你“我收到了 500”，**真正的错误原因在后端日志里**：

1. **去看后端启动的那个窗口/控制台**

   * 如果你是用 IntelliJ IDEA 点绿色按钮跑的 SpringBoot：

     * 底部 “Run” / “Console” 那个 tab 里会出现一大段红字。
   * 如果你是用 `mvn spring-boot:run` 或 `java -jar` 启动的：

     * 在你启动的那个终端窗口里，刚刚点登录的时候，也会打印一大堆红字。

2. 找到类似下面这种开头的一段：

   ```text
   Exception in thread "http-nio-8080-exec-1" java.lang.NullPointerException: ...
       at com.sky.service.impl.EmployeeServiceImpl.login(EmployeeServiceImpl.java:xx)
       ...
   Caused by: org.apache.ibatis.binding.BindingException: ...
       at ...
   ```

   * `java.lang.xxxException`：异常类型
   * `com.sky.xxx...:行号`：**哪一行代码出问题**

3. 把**这一整段红色异常栈**复制出来，我才能帮你具体定位到：

   * 是 `EmployeeMapper` 的 SQL 写错？
   * 还是 `EmployeeMapper` 找不到对应的 XML（`Invalid bound statement`）？
   * 还是 `MessageConstant.ACCOUNT_LOCKED` 之类字段/常量有问题？
   * 或者数据库连接失败？

---

## 结合你现在这个项目，很大概率的几个坑（你可以先自己对一下）

你之前这几个问题我有印象：

* `java: cannot find symbol ACCOUNT_LOCKED`
* `Cannot resolve symbol 'Employee'`
* `Cannot resolve symbol 'EmployeeLoginVO'`
* `Cannot resolve method 'login' in 'EmployeeService'`
* Maven 多模块、`pom.xml`、`parent` 配置刚改完

在这种情况下，**/admin/employee/login 500 常见原因**：

1. **Mapper 方法和 XML 不匹配**

   * 接口：`EmployeeMapper.getByUsername(String username);`
   * XML 里 `<select id="getByUsername" ...>` 名字/namespace 不匹配
   * 会报：`Invalid bound statement (not found): com.sky.mapper.EmployeeMapper.getByUsername`

2. **Mapper 没被扫描到**

   * `@MapperScan("com.sky.mapper")` 写错包名，或者写在了错误模块
   * 会导致 `employeeMapper` 为 `null`，在 `employeeMapper.getByUsername(username)` 这行直接 `NullPointerException`

3. **数据库配置问题**

   * `application.yml` 里的 `url/username/password` 写错、库没建、表没建
   * 会报 `CommunicationsException` 或 `Table 'xxx.employee' doesn't exist`

4. **异常没有被全局异常处理器接住**

   * 比如你主动 `throw new AccountLockedException(...)`，但是没有统一异常处理，就会直接 500。

---

## 你现在可以马上做的：

1. 再点一次登录，让它再 500 一次。
2. **立刻去看后台控制台的红字异常**，从 `Exception...` 那行开始，往下全选复制。
3. 发给我，我可以直接帮你：

   * 指出是哪一行出错
   * 对应改哪一个类 / 哪一个 XML / 哪一段配置

现在这条 JSON 只能说明 “**后端炸了**”，但不知道是**哪里炸**，真正的线索都在那段红色堆栈里。你把那段贴出来，我们就可以一起把这个 500 干掉。 💪
