Day02 就是把“员工管理 + 分类管理”这两个模块，从“能登录”升级到“能增删改查 + 分页 + 启停 + 记录谁干的”。

我先用一句话帮你压缩一下：
**Day02 就是把“员工管理 + 分类管理”这两个模块，从“能登录”升级到“能增删改查 + 分页 + 启停 + 记录谁干的”。**

下面我帮你把这篇超长讲义，压成一个你“查一次就够”的思维导图版，顺便对上你现在在做的代码（Swagger、JWT、ThreadLocal 那些）。

---

## 🧩 Day02 整体大图

要搞清楚 6 件事：

1. 新增员工（POST /admin/employee）
2. 员工分页查询（GET /admin/employee/page）
3. 启用/禁用员工（POST /admin/employee/status/{status}）
4. 编辑员工（GET /admin/employee/{id} + PUT /admin/employee）
5. JWT + 拦截器 + ThreadLocal，把“谁在操作”带进 Service
6. 分类模块的 CRUD（Category）整体导入

---

## 1️⃣ 新增员工：整条调用链

**业务目标**：后台“员工管理 -> 新增员工”表单，点“保存”，把员工插进 employee 表。

### 1.1 请求 & DTO

* URL：`POST /admin/employee`
* Body（JSON）类似：

```json
{
  "id": 0,
  "idNumber": "111222333444555666",
  "name": "xiaozhi",
  "phone": "13812344321",
  "sex": "1",
  "username": "小智"
}
```

* 用 **EmployeeDTO** 接收，比实体 Employee 更干净，只包含前端传的字段。

### 1.2 Controller

```java
@PostMapping
@ApiOperation("新增员工")
public Result save(@RequestBody EmployeeDTO employeeDTO){
    log.info("新增员工：{}",employeeDTO);
    employeeService.save(employeeDTO);
    return Result.success();
}
```

### 1.3 ServiceImpl 中做的事（关键逻辑）

```java
public void save(EmployeeDTO employeeDTO) {
    Employee employee = new Employee();
    BeanUtils.copyProperties(employeeDTO, employee);

    // 1. 状态：默认启用
    employee.setStatus(StatusConstant.ENABLE);

    // 2. 密码：默认 123456 + MD5
    employee.setPassword(
        DigestUtils.md5DigestAsHex(
            PasswordConstant.DEFAULT_PASSWORD.getBytes()
        )
    );

    // 3. 时间
    employee.setCreateTime(LocalDateTime.now());
    employee.setUpdateTime(LocalDateTime.now());

    // 4. 谁创建 / 谁修改 —— 用 BaseContext 拿当前登录人的 id
    employee.setCreateUser(BaseContext.getCurrentId());
    employee.setUpdateUser(BaseContext.getCurrentId());

    // 5. 落库
    employeeMapper.insert(employee);
}
```

### 1.4 Mapper：insert SQL

```java
@Insert("insert into employee (name, username, password, phone, sex, id_number, create_time, update_time, create_user, update_user,status) " +
        "values " +
        "(#{name},#{username},#{password},#{phone},#{sex},#{idNumber},#{createTime},#{updateTime},#{createUser},#{updateUser},#{status})")
void insert(Employee employee);
```

---

## 2️⃣ JWT 拦截器 + ThreadLocal：你遇到 401 的根本原因

**场景**：
你在 Swagger 或 YApi 里直接调 `POST /admin/employee`，结果返回 `401`，断点发现压根没进 `EmployeeController.save()`，卡在 `JwtTokenAdminInterceptor`。

### 2.1 JwtTokenAdminInterceptor 做什么？

```java
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (!(handler instanceof HandlerMethod)) {
        return true; // 不是 Controller 方法，直接放行
    }

    // 1. 从 Header 取 token，header 名来自 yml 中 admin-token-name
    String token = request.getHeader(jwtProperties.getAdminTokenName());

    try {
        // 2. 解析 token，拿到 empId
        Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
        Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());

        // 3. 把 empId 丢进 ThreadLocal
        BaseContext.setCurrentId(empId);

        return true;
    } catch (Exception ex) {
        response.setStatus(401); // 校验失败
        return false;
    }
}
```

> ✅ **重点**：
>
> * 后续所有需要“当前员工 id”的地方，都通过 `BaseContext.getCurrentId()` 取。
> * 如果你没在 header 里带 token，这个拦截器就会直接 401，把你挡在 Controller 外面。

### 2.2 BaseContext（ThreadLocal 封装）

```java
public class BaseContext {
    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id) { threadLocal.set(id); }
    public static Long getCurrentId()       { return threadLocal.get(); }
    public static void removeCurrentId()    { threadLocal.remove(); }
}
```

---

## 3️⃣ 登录：把 token 从哪里来的串起来

你的 `EmployeeController.login` 已经是这样：

```java
@PostMapping("/login")
public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
    log.info("员工登录：{}", employeeLoginDTO);

    Employee employee = employeeService.login(employeeLoginDTO);

    Map<String, Object> claims = new HashMap<>();
    claims.put(JwtClaimsConstant.EMP_ID, employee.getId());

    String token = JwtUtil.createJWT(
            jwtProperties.getAdminSecretKey(),
            jwtProperties.getAdminTtl(),
            claims);

    EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
            .id(employee.getId())
            .userName(employee.getUsername())
            .name(employee.getName())
            .token(token)
            .build();

    return Result.success(employeeLoginVO);
}
```

**完整闭环**：

1. `POST /admin/employee/login`
   → 返回 `EmployeeLoginVO`，里面有 `token`。
2. 前端 / YApi / Swagger 调用其它接口时：
   → **在 header 里带上** `token: {刚才返回的值}`。
3. `JwtTokenAdminInterceptor` 解析 token，把 `empId` 存到 `BaseContext`。
4. Service 里获取当前用户 id，用来写 `createUser`、`updateUser` 等。

👉 你 Swagger 文档里“新增员工 header 没内容”的问题，就在这里：
**文档不声明 header，拦截器照样要 token。**
—— 解决方式我之前已经给了两套（ApiImplicitParam 或 Docket 的 securitySchemes）。

---

## 4️⃣ 员工分页查询：PageHelper + PageResult

### 4.1 请求

* URL：`GET /admin/employee/page`
* 参数：QueryString 形式（不是 JSON）

```text
/admin/employee/page?page=1&pageSize=10&name=zhangsan
```

* DTO：`EmployeePageQueryDTO { name, page, pageSize }`

### 4.2 Controller

```java
@GetMapping("/page")
@ApiOperation("员工分页查询")
public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO){
    log.info("员工分页查询，参数为：{}", employeePageQueryDTO);
    PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);
    return Result.success(pageResult);
}
```

### 4.3 ServiceImpl

```java
public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
    PageHelper.startPage(employeePageQueryDTO.getPage(),
                         employeePageQueryDTO.getPageSize());

    Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);

    long total = page.getTotal();
    List<Employee> records = page.getResult();

    return new PageResult(total, records);
}
```

### 4.4 Mapper.xml

```xml
<select id="pageQuery" resultType="com.sky.entity.Employee">
    select * from employee
    <where>
        <if test="name != null and name != ''">
            and name like concat('%',#{name},'%')
        </if>
    </where>
    order by create_time desc
</select>
```

---

## 5️⃣ 启用/禁用 & 编辑员工：update 的复用

### 5.1 启用/禁用账号

* URL：`POST /admin/employee/status/{status}?id=4`
* status：1 启用，0 禁用

**Controller：**

```java
@PostMapping("/status/{status}")
@ApiOperation("启用禁用员工账号")
public Result startOrStop(@PathVariable Integer status, Long id){
    log.info("启用禁用员工账号：{},{}",status,id);
    employeeService.startOrStop(status,id);
    return Result.success();
}
```

**ServiceImpl：**

```java
public void startOrStop(Integer status, Long id) {
    Employee employee = Employee.builder()
            .status(status)
            .id(id)
            .build();

    employeeMapper.update(employee);
}
```

### 5.2 编辑员工：两步走

1. `GET /admin/employee/{id}` —— 回显
2. `PUT /admin/employee` —— 提交修改

**回显：**

```java
@GetMapping("/{id}")
@ApiOperation("根据id查询员工信息")
public Result<Employee> getById(@PathVariable Long id){
    Employee employee = employeeService.getById(id);
    return Result.success(employee);
}

// ServiceImpl
public Employee getById(Long id) {
    Employee employee = employeeMapper.getById(id);
    employee.setPassword("****"); // 避免把真实密码给前端
    return employee;
}
```

**编辑：**

```java
@PutMapping
@ApiOperation("编辑员工信息")
public Result update(@RequestBody EmployeeDTO employeeDTO){
    log.info("编辑员工信息：{}", employeeDTO);
    employeeService.update(employeeDTO);
    return Result.success();
}

// ServiceImpl
public void update(EmployeeDTO employeeDTO) {
    Employee employee = new Employee();
    BeanUtils.copyProperties(employeeDTO, employee);

    employee.setUpdateTime(LocalDateTime.now());
    employee.setUpdateUser(BaseContext.getCurrentId());

    employeeMapper.update(employee);
}
```

**Mapper.xml 中的动态 update：**

```xml
<update id="update" parameterType="Employee">
    update employee
    <set>
        <if test="name != null">name = #{name},</if>
        <if test="username != null">username = #{username},</if>
        <if test="password != null">password = #{password},</if>
        <if test="phone != null">phone = #{phone},</if>
        <if test="sex != null">sex = #{sex},</if>
        <if test="idNumber != null">id_Number = #{idNumber},</if>
        <if test="updateTime != null">update_Time = #{updateTime},</if>
        <if test="updateUser != null">update_User = #{updateUser},</if>
        <if test="status != null">status = #{status},</if>
    </set>
    where id = #{id}
</update>
```

---

## 6️⃣ 分类模块（Category）：整体导入的意义

分类模块本质上就是 **把你刚学的员工 CRUD 套一遍**：

* DTO：`CategoryDTO`, `CategoryPageQueryDTO`
* Mapper：`CategoryMapper + CategoryMapper.xml`
* Service：`CategoryService + CategoryServiceImpl`
* Controller：`CategoryController`

实现：

1. 新增分类（POST /admin/category）
2. 分类分页查询（GET /admin/category/page）
3. 删除分类（DELETE /admin/category?id=xxx）
4. 修改分类（PUT /admin/category）
5. 启用/禁用分类（POST /admin/category/status/{status}?id=xxx）
6. 根据类型查分类（GET /admin/category/list?type=1）

**重点是**：

> 你要把 “员工模块怎么设计 & 分层 & 写 SQL & 测试” 这一整套方法论，迁移到“分类模块、菜品模块、套餐模块”等后续所有功能上。

---

## 你现在最容易踩的几个点（结合你这两天的问题）

1. **Swagger 新增员工接口里看不到 header**

   * 文档 ≠ 拦截器配置。
   * 需要在 Swagger 里显式声明 header（ApiImplicitParam 或 securitySchemes），或者用 Authorize 统一配置 token。

2. **401 Unauthorized**

   * 一般就是：你没带 token，或 token 过期/错误。
   * 用登录接口拿到 token，再带到 header 里。

3. **createUser/updateUser 还是 10L**

   * 说明 `BaseContext.getCurrentId()` 取不到值，多数是拦截器没把 empId set 进去（例如你的管理端路径没被 addInterceptor 配好），或者请求没通过拦截器（被静态资源之类绕开）。

---

如果你愿意，下一步我可以帮你做两件事里选一个来深挖：

* 🔍 把“**新增员工 + JWT + ThreadLocal**”画成一张完整 ASCII 时序图（前端→网关→拦截器→Controller→Service→Mapper→DB）。
* 🧪 帮你对着你本机的代码，一起排查一次“**Swagger 调新增员工为什么 401 / header 没 token**”的实战流程（你发现在用的 `WebMvcConfiguration` 和 `application.yml` 就行）。
