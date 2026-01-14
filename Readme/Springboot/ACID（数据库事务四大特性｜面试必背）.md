## 🧱 ACID（数据库事务四大特性｜面试必背）

![Image](https://assets.bytebytego.com/diagrams/0407-what-does-acid-mean.png)

![Image](https://media.geeksforgeeks.org/wp-content/cdn-uploads/20191121102921/ACID-Properties.jpg)

---

### A — **Atomicity（原子性）**

> **要么全成功，要么全失败**

* 一个事务里的所有操作是**一个整体**
* 中途出错 → **全部回滚**
* 例子：转账

  * A 扣钱成功，B 加钱失败 → **A 也要回滚**

---

### C — **Consistency（一致性）**

> **事务前后，数据都满足业务规则**

* 不允许破坏约束（主键、外键、余额不为负等）
* 事务结束后，数据必须是**合法状态**
* 例子：账户余额不能 < 0

⚠️ 注意：
一致性 ≠ 没有并发问题
一致性是**业务/约束层面的正确性**

---

### I — **Isolation（隔离性）**

> **并发事务之间互不干扰**

* 一个事务没提交，其他事务**看不到中间状态**
* 解决并发问题：

  * 脏读
  * 不可重复读
  * 幻读
* 通过 **隔离级别** 控制（Read Committed / Repeatable Read / Serializable）

---

### D — **Durability（持久性）**

> **一旦提交，数据永久保存**

* 即使宕机、重启，已提交的数据也不会丢
* 依赖：

  * redo log / WAL
  * 磁盘持久化
* commit ≠ 只是内存成功

---

## 🧠 一句话总记忆（面试版）

> **ACID 保证事务：要么全做、数据合法、互不干扰、提交不丢。**

---

## 🔗 和 `@Transactional` 的关系

* `@Transactional` 是 **应用层工具**
* ACID 是 **数据库层目标**
* Spring 通过事务管理器 + DB 机制，**尽量帮你实现 ACID**

---

## 🧪 高频追问（附秒答）

* **Q：ACID 全都由 Spring 保证吗？**
  ❌ 不是，**主要由数据库保证**，Spring 只是协调
* **Q：隔离性和一致性有什么区别？**
  👉 隔离性是并发视角，一致性是业务规则视角
* **Q：性能和 ACID 冲突吗？**
  👉 是，隔离级别越高，性能越差（典型 trade-off）

---

如果你要，我可以继续给你：

* ⚔️ **ACID vs BASE（分布式必考）**
* 🧪 **ACID × 隔离级别 × 并发问题 对照表**
* 🧠 **用转账例子一图记住 ACID**

直接说哪个。
