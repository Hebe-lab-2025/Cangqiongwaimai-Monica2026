热部署（Hot Deployment / Hot Reload）——中文速记版
## 🔥 热部署（Hot Deployment / Hot Reload）——中文速记版

![Image](https://media.geeksforgeeks.org/wp-content/uploads/20240815193137/devfile.png)

![Image](https://foojay.io/wp-content/uploads/2024/11/image_2024-10-03_111134341_fwi3rh.png)

![Image](https://intellij-support.jetbrains.com/hc/user_images/6k1gpJeEDLK9V5JSVaUOFQ.png)

---

### 1️⃣ 什么是热部署？

> **不重启整个应用，就让代码修改立即生效**

* 提高开发效率
* 避免频繁重启 Spring Boot

---

### 2️⃣ 热部署 vs 热加载 vs 冷部署（必考区分）

| 名称      | 含义                | 是否重启 JVM |
| ------- | ----------------- | -------- |
| **热部署** | 代码变更，应用不中断        | ❌        |
| **热加载** | 重新加载 class / Bean | ❌        |
| **冷部署** | 停机重启              | ✅        |

⚠️ 实际开发中，大家常把 **devtools 的自动重启** 也叫“热部署”（严格说是**快速重启**）。

---

### 3️⃣ Spring Boot 常用方案（面试说这 3 个）

#### ✅ 方案 1：Spring Boot DevTools（最常用）

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-devtools</artifactId>
  <scope>runtime</scope>
</dependency>
```

**原理一句话：**

* 使用 **双 ClassLoader**
* 监听 classpath 变化
* **只重启 Spring Context，不重启 JVM**

---

#### ✅ 方案 2：IDEA HotSwap

* 修改 **方法体** 生效
* 不能：

  * 改方法签名
  * 改类结构
* JVM 原生能力（JDI）

---

#### ✅ 方案 3：JRebel（商业）

* 真·热部署
* 类结构也能改
* 企业常见（收费）

---

### 4️⃣ DevTools 执行流程（ASCII）

```
代码修改
   ↓
Classpath 变化监听
   ↓
DevTools 触发
   ↓
销毁 Spring Context
   ↓
新 ClassLoader 加载
   ↓
重新初始化 Bean
```

---

### 5️⃣ DevTools 不生效的常见原因（高频）

❌ 没开启自动编译
❌ IDE 没勾选 `Build project automatically`
❌ 被 `spring.devtools.restart.exclude` 排除了
❌ 放在生产环境（默认禁用）

---

### 6️⃣ 为什么生产环境不用热部署？（面试金句）

> 热部署会增加内存占用、类加载复杂度，并可能导致状态不一致，生产环境应使用**滚动发布/蓝绿发布**。

---

### 7️⃣ 面试一句话标准答

> 热部署是在不重启 JVM 的情况下让代码变更生效，Spring Boot 通常通过 DevTools 使用双 ClassLoader 机制实现快速重启，仅重启 Spring 容器而不是整个应用。

---

### 8️⃣ 常见追问秒答

* **Q：DevTools 算不算真正热部署？**
  👉 严格说不算，是 **Context Restart**
* **Q：能用于生产吗？**
  👉 不推荐，默认只在开发环境启用
* **Q：为什么类结构改了有时不生效？**
  👉 JVM HotSwap 限制

---

如果你愿意，我可以继续：

* 🧠 **双 ClassLoader 结构图（Base / Restart）**
* 🧪 **热部署面试陷阱题 5 题**
* ⚔️ **DevTools vs JRebel 对比表**
* 🔍 **为什么 static 变量在热部署后会“脏”**

你选一个。
