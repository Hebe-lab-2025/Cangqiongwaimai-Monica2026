看 **`sky-server` 这个模块的 `pom.xml`**。

原因已经在报错里写得很清楚了👇

```
Some problems were encountered while building the effective model for
com.sky:sky-server:jar:1.0-SNAPSHOT
```

👉 **出问题的是 `sky-server` 模块，不是父工程。**

---

## 一步一步定位（按 90% 项目结构来）

### ① 如果你是多模块项目（最常见）

项目结构一般是这样：

```
sky-take-out
├── pom.xml              ← 父 pom（packaging=pom）
├── sky-server
│   └── pom.xml          ← ✅ 就是这个
├── sky-common
│   └── pom.xml
```

✅ **你要改的是：**

```
sky-server/pom.xml
```

---

### ② 为什么不是父 pom？

父 pom 通常：

* `packaging = pom`
* 只做 **dependencyManagement**
* 不直接打 jar

而你的错误是：

```
sky-server:jar:1.0-SNAPSHOT
```

说明：

* Lombok 被 **在 sky-server 的 dependencies 里重复声明了**
* 或者：父 pom + 子 pom **都写了 lombok（但没用 dependencyManagement）**

---

## 🔍 快速确认方法（100% 准）

在项目根目录执行：

```bash
cd sky-server
grep -n "lombok" pom.xml
```

如果看到 **两处或以上**：

```text
45: <artifactId>lombok</artifactId>
128: <artifactId>lombok</artifactId>
```

👉 就是这里的问题

---

## ✅ 正确做法（给你一个「不会再踩雷」版本）

### 方案 A（最简单，推荐新手）

👉 **只在 `sky-server/pom.xml` 写一次 Lombok**

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.28</version>
    <scope>provided</scope>
</dependency>
```

父 pom **不要写 lombok**

---

### 方案 B（规范做法，适合多模块）

**父 pom (`sky-take-out/pom.xml`)：**

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.28</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

**子模块 (`sky-server/pom.xml`)：**

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>provided</scope>
</dependency>
```

---

## 🧠 一句话记住（考试级 + 实战级）

> **Maven 不允许同一个模块里出现两次相同的 groupId + artifactId**

---

如果你愿意：

* 👉 **直接把 `sky-server/pom.xml` 贴出来**
* 我可以 **逐行帮你删到“最干净版本”**
* 顺便帮你检查：`spring-boot-starter-test` / `mysql` / `mybatis` 有没有潜在重复 👌
