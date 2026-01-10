这是一个**非常典型、而且已经被你“顺手踩中”的 Mac-only 坑**。一句话结论先给你：

> **`com.apple.eawt.Application` 是 Mac GUI 专用包，服务器 / Spring Boot 项目里不该出现，必须删。**

你现在这个报错 **和 Spring、Maven、Java 版本都没关系**，是**代码层面 100% 错误引用**。

---

## 一、为什么会报 `package com.apple.eawt does not exist`

```java
import com.apple.eawt.Application;
```

这个包的特点：

| 特性                        | 说明                        |
| ------------------------- | ------------------------- |
| 仅存在于                      | **老版本 macOS + Apple JDK** |
| Java 8 OpenJDK / Corretto | ❌ 不包含                     |
| Linux / Windows           | ❌ 不存在                     |
| Spring Boot 后端            | ❌ 完全不该用                   |

你现在用的是：

```
Amazon Corretto 1.8.0_442
```

👉 **标准 OpenJDK，不带任何 Apple GUI API**
👉 所以编译期直接炸

---

## 二、为什么你会“莫名其妙”引入它（真相）

99% 的来源只有一个：

> **IDEA 自动补全 / 你以前写过 Mac GUI 程序 / 拷过模板**

`com.apple.eawt.Application` 常见用途是：

* Mac Dock 图标
* 菜单栏
* Swing / JavaFX 桌面程序

**和 Spring Boot Web 服务没有一毛钱关系**。

---

## 三、正确修复方式（只做一件事）

### ✅ 直接删掉这行 import

```java
// ❌ 删掉
import com.apple.eawt.Application;
```

### ✅ 最终正确的启动类应该是这样（标准答案）

```java
package com.sky;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SkyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkyApplication.class, args);
    }
}
```

> `Hello World!` 那行也可以删（不是错，但没意义）

---

## 四、删完之后你会看到什么

### 1️⃣ 这个错误 **永久消失**

```
package com.apple.eawt does not exist
```

### 2️⃣ 项目可以：

* `mvn clean package` ✅
* IDEA Run Spring Boot ✅
* Linux / Docker / 云服务器正常跑 ✅

---

## 五、一个非常重要的工程认知（记住）

> **后端项目：永远不要 import OS / GUI / Vendor 专属 API**

❌ `com.apple.*`
❌ `sun.misc.*`
❌ `java.awt.*`（除非你真在画图）

✅ 只用：

* `java.*`
* `javax.*`
* `org.springframework.*`
* 第三方依赖（Maven）

---

## 六、你现在的状态总结（放心）

* ✅ Maven 依赖已解决
* ✅ WeChat / OSS util 编译问题已修
* ❌ 启动类误引 Mac GUI 包（**已定位，秒修**）
* 下一步：**回到 CategoryService Bean 注入问题（这是最后一个正经问题）**

---

### 下一步我可以直接帮你干的事（选一个）

1️⃣ **一步一步带你修 `CategoryService` 注入失败（最推荐）**
2️⃣ 给你画一张 **Spring Boot 启动 → 扫包 → Bean 创建 → Controller 注入 的 ASCII 图**
3️⃣ 帮你做一次 **“从零到启动成功”的 checklist（以后再也不踩）**

你只要说 **选几**，我直接继续。
