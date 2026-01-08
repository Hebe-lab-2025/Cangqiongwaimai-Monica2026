## 🧠 MyBatis 动态 SQL 速记表（if / where / set + concat）

### 1) 动态标签速记（考点=你会不会“自动拼接”）

| 场景        | 正确标签                      | 你得到的好处                      | 最常见坑                                    |
| --------- | ------------------------- | --------------------------- | --------------------------------------- |
| 可选条件查询    | `<where> ... </where>`    | 自动加 `WHERE`；自动去掉多余 `AND/OR` | 没用 `<where>` 导致 `select ... and ...` 报错 |
| 动态拼条件     | `<if test="...">...</if>` | 条件成立才拼 SQL                  | 只写 `!= null` 忘了 `!= ''`（空串也会拼进去）        |
| 动态更新字段    | `<set> ... </set>`        | 自动加 `SET`；自动去掉末尾逗号          | 不用 `<set>` 最后一项多逗号 SQL 报错               |
| 动态拼列表/多条件 | `<trim>`（可选增强）            | 可自定义前缀/去掉多余符号               | 规则写错更难排查（先会 where/set 再上 trim）          |

---

### 2) `concat + like` 速记（你问的核心）

✅ **标准模糊包含**（最常用）

```xml
and name like concat('%', #{name}, '%')
```

✅ **前缀匹配（更可能走索引）**

```xml
and name like concat(#{name}, '%')
```

✅ **后缀匹配（一般不走索引）**

```xml
and name like concat('%', #{name})
```

🚫 **错误写法（#{ } 不能被字符串包住）**

```xml
and name like '%#{name}%'
```

🚫 **危险写法（SQL 注入风险）**

```xml
and name like '%${name}%'
```

---

## 🧪 5 道 MyBatis concat 陷阱题（带答案+坑点）

### 1) 题：下面哪条能正确实现“包含查询”？

A. `name like '%#{name}%'`
B. `name like concat('%', #{name}, '%')`
C. `name like '%${name}%'`
D. `name like concat('%#{name}%', '')`

✅ **答案：B**

* A：`#{}` 不能在字符串里解析
* C：`${}` 直接拼字符串 → 注入风险
* D：语法不对，还是把 `#{}` 当文本

---

### 2) 题：参数 `name=""`（空串）时，这段 if 会不会拼条件？

```xml
<if test="name != null">
  and name like concat('%', #{name}, '%')
</if>
```

A. 不会
B. 会

✅ **答案：B（会拼）**
坑点：只判断 `!= null`，空串也满足 → 你会得到 `like '%%'`（等于没过滤，还可能影响性能）
✅ 正确写法：

```xml
<if test="name != null and name != ''">
```

---

### 3) 题：下面哪种写法“更可能走索引”（大表性能更好）？

A. `like concat('%', #{name}, '%')`
B. `like concat(#{name}, '%')`

✅ **答案：B**
坑点：`%xxx%` 通常无法利用普通索引；`xxx%` 是前缀匹配更友好。

---

### 4) 题：`ORDER BY` 想动态传字段名，下面哪个更常见（但要小心）？

A. `order by #{sortField}`
B. `order by ${sortField}`

✅ **答案：B（但危险）**
坑点：`ORDER BY` 的列名不能用 `#{}` 绑定参数（会变成 `?`）。
⚠️ 解决姿势：**白名单**校验字段名（只允许固定几种），绝不直接接收用户任意输入。

---

### 5) 题：你写了：

```xml
and name like concat('%', #{name}, '%')
```

如果 `name` 里包含 `%` 或 `_`，会怎样？
A. 当普通字符
B. 会被当通配符
C. 直接报错

✅ **答案：B**
坑点：`%` / `_` 在 LIKE 里是通配符。
✅ 处理方式：对输入做转义（具体转义语法跟数据库有关，面试说“需要 escape/转义通配符”即可）。

---

## 🔍 结合你刚才 EmployeeMapper 代码逐行讲（select + update）

### ✅ 1）`pageQuery`：动态条件 + where + concat

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

**逐行理解：**

* `id="pageQuery"`：接口方法名必须叫 `pageQuery`
* `resultType=Employee`：每行映射成一个 Employee，对外通常是 `List<Employee>`
* `select * from employee`：基础查询
* `<where>`：

  * 如果 if 生效 → 自动变成 `where name like ...`
  * 如果 if 不生效 → 直接不输出 where（只剩 `select * ... order by ...`）
* `<if test="name != null and name != ''">`：

  * 避免空串导致 `like '%%'`
* `concat('%',#{name},'%')`：

  * 正确把参数拼成 `%keyword%`，同时 `#{}` 安全绑定，防注入
* `order by create_time desc`：最新在前

✅ **最终 SQL 两种形态：**

* 传了 name：
  `select * from employee where name like '%Tom%' order by create_time desc`
* 不传 name：
  `select * from employee order by create_time desc`

---

### ✅ 2）`update`：动态更新 + set（防覆盖 null）

```xml
<update id="update" parameterType="Employee">
  update employee
  <set>
    <if test="name != null">name = #{name},</if>
    ...
    <if test="status != null">status = #{status},</if>
  </set>
  where id = #{id}
</update>
```

**逐行理解：**

* `parameterType="Employee"`：传入的是 Employee 对象，`#{name}` 等取对象属性
* `<set>`：自动输出 `set ...`，并且**自动去掉最后一个逗号**
* 每个 `<if test="xxx != null">`：

  * 只更新你传了值的字段
  * 没传的字段不会被更新成 null（非常关键）
* `where id = #{id}`：只更新这一行；没这个会出大事故

⚠️ 一个隐藏坑：如果你传入对象里**除了 id 全是 null**，那 `<set>` 会变成空，SQL 会报错。
常见做法是：调用 update 前先校验“至少有一个字段要更新”。

---

## ✅ 你可以直接复制的“速记口诀版”

* **where**：自动加 WHERE，自动去 AND/OR
* **set**：自动加 SET，自动去末尾逗号
* **if**：条件成立才拼 SQL（空串要 `!= ''`）
* **like + #{ }**：必须 `concat('%', #{x}, '%')`（别写 `'%#{x}%'`）
* **能用 `#{}` 就别用 `${}`**（`${}` 只在列名/排序等极少场景 + 白名单）

---

如果你愿意，我可以再加一页「**trim / choose(when/otherwise) 的升级速记表**」，把动态 SQL 全家桶补齐。
