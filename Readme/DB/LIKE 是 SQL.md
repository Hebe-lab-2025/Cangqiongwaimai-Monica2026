下面把 **`LIKE`** 给你**一次讲透**，只讲你现在 MyBatis / SQL 必须懂的部分。

---

## 一句话结论

> **`LIKE` 是 SQL 的模糊匹配关键字，用来做“包含 / 前缀 / 后缀”查询。**

---

## 1️⃣ `LIKE` 是干嘛的？

`LIKE` 用来 **匹配不完全相等的字符串**。

对比一下：

```sql
name = 'Tom'      -- 必须一模一样
name LIKE 'Tom'   -- 和 = 一样（没通配符）
```

真正有意义的是 **配合通配符**。

---

## 2️⃣ `LIKE` + 通配符（核心）

### `%`：任意长度字符

| 写法             | 含义       |
| -------------- | -------- |
| `LIKE '%Tom%'` | 包含 Tom   |
| `LIKE 'Tom%'`  | 以 Tom 开头 |
| `LIKE '%Tom'`  | 以 Tom 结尾 |

---

### `_`：单个字符

```sql
LIKE '_om'
```

👉 匹配：Tom、Mom
👉 不匹配：From

---

## 3️⃣ 结合 MyBatis 的正确写法（你现在最重要）

### ✅ 正确写法（推荐）

```xml
and name like concat('%', #{name}, '%')
```

原因：

* `#{}` 不能写在字符串里
* `concat` 在 SQL 层拼 `%`

---

### ❌ 错误写法（新手必踩）

```xml
and name like '%#{name}%'
```

❌ MyBatis 不会解析

---

```xml
and name like '%${name}%'
```

❌ SQL 注入风险（面试扣分）

---

## 4️⃣ `LIKE` 在你项目里的完整例子

### XML

```xml
<if test="name != null and name != ''">
    and name like concat('%', #{name}, '%')
</if>
```

### Java 调用

```java
mapper.pageQuery("Tom");
```

### 实际执行 SQL

```sql
select * from employee
where name like '%Tom%'
```

---

## 5️⃣ 性能小知识（面试加分）

* `LIKE '%xxx%'`

  * ❌ **不能走索引**
* `LIKE 'xxx%'`

  * ✅ **可以走索引（前缀匹配）**

👉 大数据量时，尽量用 **前缀匹配**

---

## 6️⃣ 面试一句话版（直接背）

> `LIKE` 是 SQL 的模糊查询关键字，通常与 `%` 或 `_` 通配符配合使用，在 MyBatis 中需要用 `concat` 与 `#{}` 组合来安全实现模糊匹配。

---

## 7️⃣ 你现在记住 5 个点就够

1. `LIKE` ≠ `=`
2. `%` 表示任意字符
3. `_` 表示一个字符
4. `#{}` + `LIKE` → 用 `concat`
5. `%xxx%` 性能最差

---

如果你愿意，下一步我可以给你：

* 🧪 10 道 `LIKE` 真题判断
* 🔥 `LIKE` + 索引的性能对比图
* 🧭 一张 `LIKE` / `%` / `_` 速记表

你说一个，我继续。
