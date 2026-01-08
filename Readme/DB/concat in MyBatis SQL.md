## `concat` in **MyBatis SQL**

👉 **`concat` = string拼接（在 SQL 层完成）**
常见用途：**模糊查询（LIKE）**、动态拼接条件。

---

## 1️⃣ 基本用法（LIKE 模糊查询）

### MySQL 写法

```sql
select * 
from employee
where name like concat('%', #{name}, '%')
```

**等价 SQL**

```sql
where name like '%Tom%'
```

---

## 2️⃣ MyBatis Mapper 示例

```xml
<select id="list" resultType="Employee">
    select *
    from employee
    <where>
        <if test="name != null and name != ''">
            and name like concat('%', #{name}, '%')
        </if>
    </where>
</select>
```

**含义**

* `#{name}` → 预编译参数（安全）
* `concat('%', #{name}, '%')` → SQL 层拼 `%name%`

---

## 3️⃣ ❌ 常见错误（面试必考）

### 错误 1：直接写 `%#{name}%`

```sql
and name like '%#{name}%'
```

❌ **错误原因**：`#{}` 不能在字符串字面量中解析

---

### 错误 2：用 `${}` 拼接

```sql
and name like '%${name}%'
```

❌ **风险**：SQL 注入

---

## 4️⃣ 数据库差异（加分点）

| 数据库        | 拼接方式           |   |    |
| ---------- | -------------- | - | -- |
| MySQL      | `concat(a, b)` |   |    |
| PostgreSQL | `a             |   | b` |
| Oracle     | `a             |   | b` |

👉 MyBatis 本身**不管 concat**，由 **数据库决定**

---

## 5️⃣ 另一种写法（不推荐但常见）

### Java 层拼好

```java
param.setName("%" + name + "%");
```

```sql
and name like #{name}
```

📌 **缺点**：SQL 可读性差，不如 SQL 层直观

---

## 一句话总结（面试版）

> In MyBatis, `concat` is used in SQL to safely build dynamic strings, commonly for `LIKE` queries, while keeping parameters precompiled with `#{}` to avoid SQL injection.

如果你愿意，我可以：

* 🧠 把 **MyBatis 动态 SQL（if / where / set）+ concat** 做成一张速记表
* 🧪 出 5 道 **MyBatis concat 陷阱题**
* 🔍 结合你刚才的 `EmployeeMapper` 代码逐行讲
