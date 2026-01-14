## `@Transactional` —— Spring 事务控制（中文速记版）

![Image](https://ask.qcloudimg.com/http-save/yehe-3487219/fd54239e2049644bf4ec3fcee2f4b4e4.png)

![Image](https://waylau.com/images/post/20181211-transaction-propagation-1.jpg)

![Image](https://img2020.cnblogs.com/blog/1537140/202109/1537140-20210901141033417-1480192436.png)

---

### 1️⃣ 它解决什么问题？

保证一组数据库操作 **要么全部成功，要么全部回滚**（ACID），避免**部分成功、数据不一致**。

---

### 2️⃣ 用在哪？

* **Service 层（最佳实践）**
* 方法级（最常见）
* 类级（作用于所有 public 方法）

```java
@Transactional
public void createOrder() {
    orderRepo.save(order);
    paymentRepo.pay();
}
```

---

### 3️⃣ 底层原理（一句话）

Spring 用 **AOP 代理** 包装方法：

* 方法前：开启事务
* 正常结束：提交事务
* 异常抛出：按规则回滚事务

---

### 4️⃣ 默认行为（高频考点）

| 项目   | 默认值                          |
| ---- | ---------------------------- |
| 回滚条件 | **RuntimeException / Error** |
| 不回滚  | Checked Exception            |
| 传播机制 | `REQUIRED`                   |
| 隔离级别 | 数据库默认                        |
| 生效方法 | **只能是 public**               |

---

### 5️⃣ 回滚陷阱（必考）

```java
@Transactional(rollbackFor = Exception.class)
```

👉 **抛 checked exception 也要回滚时必须加**

---

### 6️⃣ 传播机制（面试最爱）

| 类型             | 含义               |
| -------------- | ---------------- |
| `REQUIRED`     | 有事务就加入，没有就新建（默认） |
| `REQUIRES_NEW` | 挂起旧事务，新建独立事务     |
| `NESTED`       | 子事务（savepoint）   |
| `SUPPORTS`     | 有就用，没有就不用        |

📌 一句话记忆：

> **`REQUIRES_NEW` 失败不影响外层事务**

---

### 7️⃣ 常见踩坑（直接背）

❌ **自己调用自己（self-invocation）**
❌ private / protected 方法
❌ try-catch 吞异常不抛
❌ 用在 Controller 层

✅ 正确：**Service 层 + public 方法**

---

### 8️⃣ 面试标准回答（一句话）

> `@Transactional` 通过 Spring AOP 在方法执行前后控制事务，保证数据库操作的原子性，默认在运行时异常时回滚，传播机制默认是 REQUIRED。

---

如果你愿意，下一步我可以帮你：

* 🧠 一张 **@Transactional 执行流程 ASCII 图**
* 🧪 **3 道 Transactional 易错面试题**
* ⚔️ **REQUIRED vs REQUIRES_NEW 对照表**
* 🔍 **为什么 try-catch 会导致不回滚（源码级）**

你选一个即可。



## 🧠 `@Transactional` 执行流程 ASCII 图

```
[Controller] 
    |
    v
[Service Proxy (Spring AOP)]
    |
    |--(1) TransactionInterceptor.before()
    |        - 从 TransactionAttribute 读 @Transactional 配置
    |        - PlatformTransactionManager.getTransaction(...)
    |        - 开启 or 加入事务（Propagation 决定）
    |
    v
[Target Service Method 真正业务逻辑]
    |
    |   DAO/Repository 调用（JPA/MyBatis/JdbcTemplate）
    |      -> 使用同一个 Connection / Session（绑定到当前线程 ThreadLocal）
    |
    v
(2) 方法正常返回? -------------------- No(抛异常) --------------------+
    |                                                               |
   Yes                                                              v
    |                                                    TransactionInterceptor.afterThrowing()
    v                                                               |
TransactionInterceptor.afterReturning()                              |-- 根据规则判断 rollback?
    |                                                               |   - 默认：RuntimeException/Error 回滚
    |-- commit(TransactionStatus)                                    |   - checked exception 默认不回滚
    |                                                               |
    v                                                               |-- rollback(...) 或 commit(...)
[DB COMMIT]                                                         v
                                                                [DB ROLLBACK 或 COMMIT]
```

> 核心点：**事务是“代理层”控制的**，不是你方法里自己控制的。

---

## 🧪 3 道 Transactional 易错面试题（含标准答案）

### 题 1：为什么 `private` 方法上的 `@Transactional` 不生效？

**答：** Spring 默认用 **代理（JDK/CGLIB）拦截 public 方法**；`private` 无法被代理拦截，所以事务逻辑不会进入拦截器。

---

### 题 2：同一个类里 `this.inner()` 调用带 `@Transactional` 的方法，为什么不生效？

**答：** 这是 **self-invocation**：调用没有经过 Spring 生成的代理对象，而是直接调用本类方法，所以绕过了 `TransactionInterceptor`，事务不起作用。
✅ 解决：把 inner 拆到另一个 Bean，或通过代理调用（如 `AopContext.currentProxy()`，但不推荐面试写这个）。

---

### 题 3：抛了 `Exception` 但没有回滚，为什么？

**答：** `@Transactional` 默认只对 **RuntimeException / Error 回滚**，`checked Exception` 默认不回滚。
✅ 解决：`@Transactional(rollbackFor = Exception.class)` 或改成抛 RuntimeException。

---

## ⚔️ `REQUIRED` vs `REQUIRES_NEW` 对照表（面试高频）

| 项目        | `REQUIRED`（默认）  | `REQUIRES_NEW`              |
| --------- | --------------- | --------------------------- |
| 外层已有事务    | **加入外层事务**      | **挂起外层事务，新开一个**             |
| 外层没有事务    | 新建事务            | 新建事务                        |
| 内层失败影响外层？ | **可能影响（同一事务）**  | **不影响（独立事务）**               |
| 典型场景      | 大多数业务：下单+写库+扣库存 | **独立日志/审计**、通知、补偿记录（内层必须提交） |
| 风险/代价     | 简单、常用           | 更复杂：多事务、可能更慢、死锁风险更高         |

一句话记：

* **REQUIRED = 能合并就合并**
* **REQUIRES_NEW = 强制隔离一段独立提交/回滚的逻辑**

---

## 🔍 为什么 try-catch 会导致“不回滚”（源码级逻辑）

### 关键结论

`@Transactional` 的回滚判断发生在 **代理拦截器**里：

* **只有异常“抛出到代理层”**，拦截器才能看到它并触发 rollback。
* 你在方法内部 `try-catch` 把异常吞了，代理层只看到“正常返回”，于是 **提交**。

### 代理拦截器的核心流程（伪代码，接近真实逻辑）

```java
// TransactionInterceptor.invoke(...)
beginTxIfNeeded();

try {
    Object ret = method.invoke(target, args);   // 调用你的业务方法
    commit();                                   // 没看到异常 -> 提交
    return ret;
} catch (Throwable ex) {
    if (shouldRollbackOn(ex)) rollback();       // 看到异常 -> 判断是否回滚
    else commit();
    throw ex;
}
```

### 你写了 try-catch 后发生了什么？

```java
@Transactional
public void foo() {
  try {
     dao.update();
     throw new RuntimeException("fail");
  } catch (Exception e) {
     // 吞掉了，没再 throw
  }
  // 方法对外“正常结束”
}
```

代理层看到的是：**method.invoke 正常返回**
➡️ 走 `commit()`
➡️ 你以为“出错了”，但事务已经提交了。

### 正确做法（任选其一）

1. **不要吞异常**：catch 后继续 `throw`（或包装成 RuntimeException）
2. 如果必须 catch：在 catch 里标记回滚

   * `TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();`（了解即可，面试点到为止）
3. 用 `rollbackFor` 覆盖 checked exception 的回滚规则（如果你抛的是 checked）

---

如果你愿意，我可以再加一张：

* ✅ **“try-catch + REQUIRED/REQUIRES_NEW 组合”会出现哪些反直觉结果**（面试特别爱追问）
