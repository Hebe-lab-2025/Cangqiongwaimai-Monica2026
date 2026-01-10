下面用**最直白 + 面试可直接背**的方式讲 **Java 反射（Reflection）**，并且**直接连到 Spring / IoC / AOP**，你现在学的东西正好用得上。

---

## 一句话定义（先记住）

> **反射 = 在运行时，获取类的信息，并操作类、方法、字段**

关键词：
**运行时（runtime）｜类信息｜动态操作**

---

## 一、没有反射 vs 有反射

### ❌ 没有反射（写死）

```java
EmployeeService service = new EmployeeServiceImpl();
service.add();
```

👉 类名、方法 **编译期就确定**

---

### ✅ 有反射（动态）

```java
Class<?> clazz = Class.forName("com.sky.service.EmployeeServiceImpl");
Object obj = clazz.newInstance();
Method method = clazz.getMethod("add");
method.invoke(obj);
```

👉 **运行时才知道：**

* 用哪个类
* 调哪个方法

---

## 二、反射能干什么（核心 4 件事）

### 1️⃣ 获取 Class 对象（必考）

```java
Class<?> c1 = Employee.class;
Class<?> c2 = employee.getClass();
Class<?> c3 = Class.forName("com.sky.entity.Employee");
```

---

### 2️⃣ 创建对象

```java
Object obj = clazz.getDeclaredConstructor().newInstance();
```

---

### 3️⃣ 调用方法

```java
Method m = clazz.getMethod("login", String.class);
m.invoke(obj, "admin");
```

---

### 4️⃣ 操作属性（包括 private）

```java
Field f = clazz.getDeclaredField("name");
f.setAccessible(true);
f.set(obj, "Tom");
```

---

## 三、反射在 Spring 里的真实用途（重点）

### 🔥 1️⃣ IoC 的底层就是反射

```java
@Service
public class EmployeeServiceImpl {}
```

Spring 启动时做了什么？

1. 扫描 class 文件
2. **反射创建对象**
3. 放进 IoC 容器
4. 反射注入依赖（`@Autowired`）

👉 **没有反射，IoC 不可能实现**

---

### 🔥 2️⃣ AOP 也是反射 + 代理

```java
@Before("execution(* com.sky.service.*.*(..))")
```

Spring：

1. 用反射找到目标方法
2. 生成代理对象
3. 方法调用前后插入逻辑

---

### 🔥 3️⃣ 注解解析靠反射

```java
@RestController
@RequestMapping("/employee")
```

Spring：

* 反射读取注解
* 建立 URL → 方法映射

---

## 四、反射的优缺点（面试必问）

### ✅ 优点

| 优点   | 说明               |
| ---- | ---------------- |
| 灵活   | 运行时决定行为          |
| 解耦   | 不依赖具体实现          |
| 框架基础 | Spring / MyBatis |

---

### ❌ 缺点

| 缺点   | 原因         |
| ---- | ---------- |
| 性能慢  | 不能被 JIT 优化 |
| 不安全  | 可破坏封装      |
| 可读性差 | 字符串 + 动态   |

👉 **所以：业务代码尽量不用，框架用**

---

## 五、反射 vs new（一句话对比）

| 对比   | new  | 反射  |
| ---- | ---- | --- |
| 时间   | 编译期  | 运行期 |
| 性能   | 快    | 慢   |
| 灵活性  | 低    | 高   |
| 常用场景 | 业务代码 | 框架  |

---

## 六、面试标准回答（30 秒版）

> **反射是 Java 在运行时获取类信息并动态操作对象的机制。
> Spring 的 IoC、AOP 和注解解析底层都依赖反射。
> 它带来高度灵活性，但性能和安全性较差，因此主要用于框架层，而不是业务代码。**

---

## 七、和你当前知识的连线（记住这条）

> **IoC = 反射创建对象
> AOP = 反射 + 动态代理
> 注解 = 反射解析**

---

如果你愿意，下一步我可以帮你：

* 🔹 **用一张 ASCII 图画出 Spring 启动时反射流程**
* 🔹 **结合你项目的 `@Autowired`，逐步模拟反射注入**
* 🔹 **给你一套反射 + IoC + AOP 的面试追问清单**
