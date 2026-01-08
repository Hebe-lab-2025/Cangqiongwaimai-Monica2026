下面我**按“这行干嘛 + 为什么要这样写”**，把这段 **MyBatis 动态 UPDATE** 给你彻底讲清楚，这是**非常高频、非常实用的一段代码**。

---

## 一、整体一句话先懂

> 这是一个 **按非空字段动态更新员工信息** 的 SQL，
> 只更新传了值的字段，**没传的字段不会被覆盖为 null**。

---

## 二、逐行拆解解释（重点）

---

### 1️⃣ `<update id="update" parameterType="Employee">`

```xml
<update id="update" parameterType="Employee">
```

#### 含义

* 定义一条 **更新 SQL**
* `id="update"`
  👉 必须和 `EmployeeMapper` 接口里的方法名一致

```java
void update(Employee employee);
```

* `parameterType="Employee"`
  👉 表示 **传进来的是一个 Employee 对象**

📌 所以下面用到的：

```text
#{name} #{username} #{id}
```

都是 **Employee 里的属性**

---

### 2️⃣ `update employee`

```sql
update employee
```

* 要更新的表：`employee`

---

### 3️⃣ `<set>`（核心标签，极其重要）

```xml
<set>
```

#### 干嘛用的？

👉 **专门为 UPDATE 服务的智能拼接器**

它会自动帮你：

* 加上 `SET`
* **自动去掉最后一个多余的逗号**

📌 这就是为什么你可以放心在每一行后面写 `,`

---

### 4️⃣ `<if test="xxx != null">`（动态更新的灵魂）

示例：

```xml
<if test="name != null">name = #{name},</if>
```

#### 含义

* 只有当 `employee.getName() != null`
* 才会生成这一段 SQL

👉 如果前端没传 `name`：

* 这行 **不会出现在 SQL 里**
* 数据库里的 `name` **不会被改成 null**

---

### 5️⃣ 每一行在干什么（统一理解）

| XML 条件               | 对应数据库字段       | 说明   |
| -------------------- | ------------- | ---- |
| `name != null`       | `name`        | 员工姓名 |
| `username != null`   | `username`    | 登录名  |
| `password != null`   | `password`    | 密码   |
| `phone != null`      | `phone`       | 手机号  |
| `sex != null`        | `sex`         | 性别   |
| `idNumber != null`   | `id_number`   | 身份证  |
| `updateTime != null` | `update_time` | 更新时间 |
| `updateUser != null` | `update_user` | 修改人  |
| `status != null`     | `status`      | 状态   |

📌 注意：

* **Java 驼峰 → 数据库下划线**
* MyBatis 不会自动帮你转字段名
  👉 这里是你**手动写映射**

---

### 6️⃣ `where id = #{id}`（生死线）

```sql
where id = #{id}
```

#### 含义

* **只更新这一条员工记录**
* `id` 是主键

⚠️ 面试 + 实战必提：

> **UPDATE 没有 WHERE = 生产事故**

---

## 三、最终生成的 SQL（举例）

### ✅ 情况 1：只传了 name 和 phone

```sql
update employee
set name = ?, phone = ?
where id = ?
```

---

### ✅ 情况 2：只传了 status

```sql
update employee
set status = ?
where id = ?
```

👉 **完全不会动其他字段**

---

## 四、为什么一定要用 `<set>` + `<if>`？

### ❌ 错误写法（新手常见）

```sql
update employee
set name = #{name},
    phone = #{phone},
    status = #{status}
where id = #{id}
```

问题：

* 前端没传 → 覆盖成 null
* 非常危险

---

### ✅ 正确写法（你现在这个）

* 只更新有值的字段
* 不破坏原数据
* MyBatis 官方推荐写法

---

## 五、常见坑（一定记住）

### ❌ 忘了传 id

```text
where id = null
```

→ 更新 0 行，或者直接报错

---

### ❌ `<if test="xxx != null">` 但前端传空字符串

```java
name = ""
```

→ 会更新成空字符串

📌 如果你要防空字符串，应该这样：

```xml
<if test="name != null and name != ''">
```

---

### ❌ 没用 `<set>`

→ 最后一个字段后面多一个逗号，SQL 报错

---

## 六、面试一句话总结（直接背）

> 这是一个 MyBatis 动态更新 SQL，通过 `<set>` 和 `<if>` 实现按非空字段更新，避免未传字段被覆盖为 null，同时 `<set>` 自动处理多余逗号，确保 SQL 正确。

---

如果你愿意，下一步我可以继续帮你：

* 🧪 **改成“防空字符串 + 防误更新”的版本**
* 🧭 **画一张 MyBatis UPDATE 动态拼接流程图**
* 🔥 **给你 5 道 MyBatis 动态 SQL 面试题**

你选一个，我继续。
