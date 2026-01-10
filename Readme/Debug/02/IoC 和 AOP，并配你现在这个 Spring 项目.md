下面用**最直白 + 面试友好**的方式讲 **IoC** 和 **AOP**，并配你现在这个 Spring 项目能对上的例子。

---

## 一句话对比（先记住）

| 概念      | 核心一句话                          |
| ------- | ------------------------------ |
| **IoC** | **对象不自己 new，由 Spring 帮你创建和管理** |
| **AOP** | **不改原代码，给方法“加功能”**             |

---

## 一、IoC（Inversion of Control，控制反转）

### 🔹 是什么

> **对象的创建权，从程序员 → 交给 Spring 容器**

以前（没 IoC）：

```java
EmployeeService service = new EmployeeServiceImpl();
```

现在（有 IoC）：

```java
@Autowired
private EmployeeService employeeService;
```

👉 **你不 new，Spring 注入**

---

### 🔹 为什么叫“反转”

| 以前        | 现在        |
| --------- | --------- |
| 你控制对象生命周期 | Spring 控制 |
| 你决定用哪个实现  | Spring 决定 |
| 强耦合       | 解耦        |

---

### 🔹 Spring 里怎么用（你项目里的）

```java
@Service
public class EmployeeServiceImpl implements EmployeeService {
}
```

```java
@RestController
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
}
```

**发生了什么？**

1. Spring 启动
2. 扫描 `@Service`
3. 创建 `EmployeeServiceImpl` 对象
4. 注入到 `EmployeeController`

✅ **这整套就是 IoC**

---

### 🔹 IoC 核心注解（面试必说）

| 注解                              | 作用      |
| ------------------------------- | ------- |
| `@Component`                    | 普通 Bean |
| `@Service`                      | 业务层     |
| `@Repository`                   | DAO 层   |
| `@Controller / @RestController` | 控制层     |
| `@Autowired`                    | 注入      |

---

## 二、AOP（Aspect-Oriented Programming，面向切面）

### 🔹 是什么

> **把“横切关注点”抽出来，统一处理**

横切关注点 =

* 日志
* 权限校验
* 事务
* 性能监控

---

### 🔹 没 AOP 会怎样（很丑）

```java
public void addEmployee() {
    log.info("start");
    checkPermission();
    // 业务代码
    log.info("end");
}
```

❌ 到处重复
❌ 污染业务代码

---

### 🔹 用 AOP（标准写法）

```java
@Aspect
@Component
public class LogAspect {

    @Before("execution(* com.sky.service.*.*(..))")
    public void before() {
        log.info("method start");
    }
}
```

业务代码 **完全不用改**：

```java
public void addEmployee() {
    // 纯业务逻辑
}
```

👉 **运行时，Spring 自动“织入”日志**

---

### 🔹 AOP 做了什么（底层）

1. Spring 创建代理对象（JDK / CGLIB）
2. 调用方法前 → 执行切面
3. 调用目标方法
4. 调用后 → 执行切面

---

### 🔹 常见 AOP 通知类型（必背）

| 注解                | 什么时候执行      |
| ----------------- | ----------- |
| `@Before`         | 方法前         |
| `@After`          | 方法后（不管成功失败） |
| `@AfterReturning` | 方法成功        |
| `@AfterThrowing`  | 方法异常        |
| `@Around`         | 包住方法（最强）    |

---

## 三、IoC vs AOP（面试对照表）

| 维度      | IoC          | AOP        |
| ------- | ------------ | ---------- |
| 解决问题    | 对象管理         | 功能增强       |
| 关注点     | **谁创建对象**    | **方法怎么增强** |
| 核心思想    | 解耦           | 横切关注点      |
| 是否改业务代码 | ❌ 不改         | ❌ 不改       |
| 典型例子    | `@Autowired` | `@Aspect`  |

---

## 四、你项目里的真实对应（加分）

### IoC

* `@Service`
* `@RestController`
* `@Autowired EmployeeService`
* `@Component JwtTokenAdminInterceptor`

### AOP

* 日志切面
* 权限校验
* `@Transactional`（本质也是 AOP）
* PageHelper / 事务 / 缓存

---

## 五、面试标准回答（30 秒版）

> **IoC 是把对象创建和依赖管理交给 Spring，降低耦合；
> AOP 是在不修改业务代码的情况下，对方法进行统一增强，比如日志、事务和权限。
> IoC 解决的是“对象怎么来”，AOP 解决的是“方法执行时加什么逻辑”。**

---

如果你愿意，我可以下一步帮你：

* 🔹 画一张 **IoC + AOP 执行流程 ASCII 图**
* 🔹 用你项目里的 **JWT / 日志** 写一个完整 AOP 示例
* 🔹 给你一套 **面试追问 Q&A（含底层原理）**
