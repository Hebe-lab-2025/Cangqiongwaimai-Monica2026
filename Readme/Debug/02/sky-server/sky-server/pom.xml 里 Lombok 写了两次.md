你这个 `sky-server/pom.xml` 里 **Lombok 写了两次**，所以 Maven 报 “must be unique”。

重复的两段是：

1)（中间那段）

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

2)（最底下那段）

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>provided</scope>
</dependency>
```

---

## ✅ 直接改法（最推荐）

**删掉第 1 段**（没有 scope 的那段），只保留底下这个：

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>provided</scope>
</dependency>
```

> 这样 Lombok 只编译期生效，不会被打进 jar（更标准）。

---

## ✅ 改完后的关键片段（你只需要保证 Lombok 只出现一次）

把你原来的 Lombok 两段，改成只剩这一段即可：

```xml
<!-- Lombok (keep only one) -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>provided</scope>
</dependency>
```

---

## 🧪 验证

在项目根目录跑：

```bash
mvn -q -DskipTests clean package
```

如果还有同类报错，再搜：

```bash
grep -n "org.projectlombok" -n sky-server/pom.xml
```

---

如果你愿意，我也可以顺手帮你检查：父 `pom.xml` 里有没有也写了 lombok（避免“父子都写但没用 dependencyManagement”的重复风险）。你把父 `pom.xml` 里 `dependencies / dependencyManagement` 那段贴出来就行。
