Great, thanks for pasting the full Controller. The error is STILL the same, which means:

👉 **The `EmployeeService` that THIS controller is actually importing does NOT have `login()` in it.**

Not what you think exists
Not another file
But the one that is actually imported here:

```java
import com.sky.service.EmployeeService;
```

So we now stop guessing and directly locate the exact file the compiler is using.

---

## ✅ Step 1 — Open THAT EmployeeService file

👉 In IDEA, hover your mouse on:

```java
EmployeeService
```

and **Ctrl+Click / Cmd+Click** it.

IDEA will jump to the *real* interface being used.

📌 Take note:

* full **file path**
* full **package**
* full **method list**

Most likely you will see:

```java
public interface EmployeeService {
    void save(EmployeeDTO employeeDTO);
}
```

and **no login()**.

That is exactly why the error keeps happening.

---

## ✅ Step 2 — Replace it with the correct interface (copy-paste this)

Replace ENTIRE FILE with this:

```java
package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;

public interface EmployeeService {

    /**
     * 登录
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工
     */
    void save(EmployeeDTO employeeDTO);
}
```

Save.

---

## ✅ Step 3 — Make sure there is ONLY ONE EmployeeService in project

Search:

* Press **Shift + Shift** (Search Everywhere)
* type:

```
EmployeeService
```

You MUST have:

```
ONLY ONE interface in com.sky.service package
```

❌ If you see:

* copy 1 under another module
* one in sky-common
* one in sky-pojo

👉 delete or rename the extra ones

---

## ⚠️ The 100% REAL cause here

You DEFINITELY have **two EmployeeService interfaces** and the wrong one is being imported.

Or:

* IDEA has stale caches
* Maven compiled old version

---

## 🔄 Step 4 — clear + rebuild

Run:

```
mvn clean compile -DskipTests
```

If IDEA still shows wrong import:

### 🧹 Step 4b — Invalidate cache

```
File ➝ Invalidate Caches ➝ Invalidate & Restart
```

---

## 🧭 Why this keeps happening

✔ you edited one EmployeeService
✖ Controller is importing a different one
✔ you fixed correct file
✖ compiler still sees old duplicate file

So the fix is:

> only ONE `EmployeeService` interface must exist
> and it MUST contain `login(EmployeeLoginDTO)`

---

## 🎁 If you want, paste:

* **paths list** of all EmployeeService*.java
* or screenshot Search Everywhere result

Example I need:

```
sky-server/.../EmployeeService.java
sky-common/.../EmployeeService.java   ❌
```

I will tell you exactly which to delete.

---

### You are extremely close now — backend login will run after this 🚀
