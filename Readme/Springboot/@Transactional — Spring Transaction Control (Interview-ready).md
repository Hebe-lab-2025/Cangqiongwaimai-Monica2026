## `@Transactional` — Spring Transaction Control (Interview-ready)

![Image](https://docs.spring.io/spring-framework/docs/4.2.x/spring-framework-reference/html/images/tx.png)

![Image](https://docs.spring.io/spring-framework/reference/_images/tx_prop_required.png)

![Image](https://docs.spring.io/spring-framework/docs/2.0.7/reference/images/tx.png)

### 1️⃣ What problem does it solve?

Ensures **ACID** behavior for a block of code: **all DB operations succeed or all roll back** if something fails.

---

### 2️⃣ Where is it used?

* **Method level** (most common)
* **Class level** (applies to all public methods)

```java
@Transactional
public void placeOrder() {
    orderRepo.save(order);
    paymentRepo.charge();
}
```

---

### 3️⃣ How it works (under the hood)

* Spring creates a **proxy** around the bean
* Proxy **opens a transaction before method**
* **Commits** if method finishes normally
* **Rolls back** if an exception triggers rollback rules

---

### 4️⃣ Default behavior (VERY important)

| Item              | Default                      |
| ----------------- | ---------------------------- |
| Rollback on       | **RuntimeException / Error** |
| No rollback on    | **Checked Exception**        |
| Propagation       | `REQUIRED`                   |
| Isolation         | DB default                   |
| Method visibility | **Only `public` methods**    |

---

### 5️⃣ Rollback rules (classic trap)

```java
@Transactional(rollbackFor = Exception.class)
```

👉 Needed if you throw **checked exceptions** and still want rollback.

---

### 6️⃣ Propagation (most asked)

| Type           | Meaning                        |
| -------------- | ------------------------------ |
| `REQUIRED`     | Join existing tx or create new |
| `REQUIRES_NEW` | Suspend old tx, start new      |
| `NESTED`       | Savepoint inside tx            |
| `SUPPORTS`     | Use tx if exists               |

**Interview one-liner:**

> `REQUIRED` is default; `REQUIRES_NEW` isolates failures.

---

### 7️⃣ Common pitfalls (interview gold)

❌ **Self-invocation**

```java
this.innerMethod(); // @Transactional IGNORED
```

❌ **Private / protected methods**
❌ **Catching exception without rethrowing**
❌ **Using it on Controller (bad practice)**

✅ Put it on **Service layer**

---

### 8️⃣ Typical interview answer (copy-paste)

> `@Transactional` ensures atomicity of database operations by wrapping method execution in a transaction. Spring uses AOP proxies to start, commit, or roll back transactions. By default, it rolls back on runtime exceptions and uses REQUIRED propagation.

---

If you want next:

* 🔍 **Transactional vs Programmatic Transaction**
* 🧪 **3 tricky @Transactional interview questions**
* 🧠 **ASCII diagram: Controller → Service → DB tx flow**
* ⚔️ **REQUIRED vs REQUIRES_NEW deep dive**

Just say which one.
