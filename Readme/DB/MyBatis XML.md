下面我**按“一句一句干嘛 + 为什么要这么写”**给你拆这段 MyBatis XML，**这是面试 + 实战都高频的写法**。

---

## 一、整体在干什么？（先给你大局）

> 这是一个 **MyBatis 动态 SQL**，
> 用来 **按条件分页查询员工列表**，
> 如果传了 `name`，就按姓名模糊查询；
> 如果没传，就查全部员工。

---

## 二、逐行解释（重点）

---

### 1️⃣ `<mapper namespace="com.sky.mapper.EmployeeMapper">`

```xml
<mapper namespace="com.sky.mapper.EmployeeMapper">
```

#### 含义

* 这个 XML **对应** `EmployeeMapper` 接口
* 里面的 SQL 会绑定到这个接口的方法上

👉 **namespace = Mapper 接口的全限定名**

---

### 2️⃣ `<select id="pageQuery" ...>`

```xml
<select id="pageQuery" resultType="com.sky.entity.Employee">
```

#### 含义

* 定义一条 **查询 SQL**
* `id="pageQuery"`
  👉 必须和接口方法名一模一样

```java
List<Employee> pageQuery(...);
```

#### `resultType`

```text
resultType="com.sky.entity.Employee"
```

👉 表示：

* 每一行查询结果
* 映射成一个 `Employee` 对象

📌 如果查多行：

* MyBatis 自动帮你返回 `List<Employee>`

---

### 3️⃣ `select * from employee`

```sql
select * from employee
```

* 查询员工表
* 这里先不加条件，条件交给下面的动态 SQL

---

### 4️⃣ `<where>`（非常重要）

```xml
<where>
```

#### 干嘛的？

👉 **智能生成 WHERE 关键字**

它会帮你处理：

* 没有条件 → **不加 WHERE**
* 有条件 → **自动加 WHERE**
* 自动去掉多余的 `AND / OR`

📌 没有 `<where>`，你就得自己判断 SQL 拼接，非常容易写错

---

### 5️⃣ `<if test="name != null and name != ''">`

```xml
<if test="name != null and name != ''">
```

#### 含义

* 这是 **MyBatis 的动态判断**
* 只有当：

  * `name` 不为 null
  * 并且不是空字符串

才会拼接下面这段 SQL

📌 `name` 是从哪里来的？
👉 **来自 Mapper 方法的参数**

---

### 6️⃣ 模糊查询条件（like + concat）

```sql
and name like concat('%',#{name},'%')
```

#### 拆解解释

* `#{name}`：MyBatis 的 **占位符**
* `concat('%', #{name}, '%')`：

  * 拼成 `%张%`
  * 实现 **模糊查询**

👉 最终 SQL 可能变成：

```sql
where name like '%Tom%'
```

📌 用 `#{}` 而不是 `${}`：

* 防止 SQL 注入
* 是预编译参数

---

### 7️⃣ `</where>` 结束

```xml
</where>
```

* MyBatis 会根据 if 是否生效：

  * 自动决定要不要生成 `WHERE`

---

### 8️⃣ 排序语句

```sql
order by create_time desc
```

#### 含义

* 按创建时间 **倒序**
* 最新的员工排最前面

---

## 三、最终生成的 SQL（两种情况）

### ✅ 情况 1：传了 name = "Tom"

```sql
select * from employee
where name like '%Tom%'
order by create_time desc
```

---

### ✅ 情况 2：没传 name（null 或空）

```sql
select * from employee
order by create_time desc
```

👉 **不会出现 where and name ... 这种错误 SQL**

---

## 四、常见坑（你现在阶段一定会遇到）

### ❌ 忘了 `<where>`

```sql
select * from employee
and name like ...
```

→ SQL 直接报错

---

### ❌ 用 `${name}`

```sql
and name like '%${name}%'
```

→ **SQL 注入风险（面试扣分）**

---

### ❌ 接口参数名不叫 name

```java
pageQuery(String keyword);
```

→ XML 找不到 `name`，if 永远不生效

---

## 五、面试一句话总结（直接背）

> 这是一个 MyBatis 动态 SQL，通过 `<if>` 和 `<where>` 实现可选条件查询，当传入 name 时进行模糊匹配，不传时查询全部数据，并自动处理 WHERE 和 AND 的拼接。

---

如果你愿意，我可以下一步帮你：

* 🧪 **对照 Mapper 接口参数，帮你检查是否会失效**
* 🧭 **画一张 MyBatis 动态 SQL 执行流程图**
* 🔥 **给你 5 道 MyBatis 动态 SQL 易错题**

你选一个，我继续。
