你把方法名改对了，但**根因没变**：

### ✅ 根因

`CategoryPageQueryDTO` 这个类 **本来就没有 `setUpdateTime(LocalDateTime)`**（分页查询 DTO 通常也不该有 updateTime）。
所以 AOP 只要还在对它做 “AutoFill”，就一定会继续报 `NoSuchMethodException`。

---

## 你现在要修的不是 setter，而是：**为什么 pageQuery 被 AutoFill 了**

从堆栈看得很清楚：

* `$Proxy93.pageQuery` → `CategoryServiceImpl.pageQuery` → `CategoryController.page`
* 说明你的 `AutoFillAspect` 切到了 `CategoryMapper.pageQuery(...)`（查询方法）

而 `AutoFill` 应该只用在 **insert / update**，不该对 **select/pageQuery** 生效。

---

## ✅ 立刻可用的修复方案（选一个就能好）

### 方案 A（最推荐）：把 `@AutoFill` 从 pageQuery 上去掉

去看 `CategoryMapper.pageQuery(...)` 方法上是不是写了类似：

```java
@AutoFill(OperationType.INSERT) // 或 UPDATE
Page<Category> pageQuery(CategoryPageQueryDTO dto);
```

**查询方法直接删掉这个注解。**

---

### 方案 B：让切面只拦截 “带 @AutoFill 的方法”，并且只对 Entity 填充

你切点如果写得太宽（例如拦截整个 mapper 包），改成只拦截注解：

```java
@Before("@annotation(com.sky.annotation.AutoFill)")
public void autoFill(JoinPoint joinPoint) { ... }
```

这样 **没加 @AutoFill 的 pageQuery** 就不会进切面。

---

### 方案 C：切面里加 “防呆”：没有这些 setter 就直接跳过

（不如 A/B 干净，但能防止误用）

```java
try {
    Method setUpdateTime = clazz.getDeclaredMethod("setUpdateTime", LocalDateTime.class);
    setUpdateTime.invoke(obj, now);
} catch (NoSuchMethodException e) {
    // ignore
}
```

---

## 为什么你日志里 SQL 还能跑出来？

你日志显示 `count` 和 `select` 都执行了，但同时抛了异常。

这通常是因为：

* AOP 在调用前抛异常，但你的分页插件/日志仍打印了部分 SQL（或异常后仍进入了某些链路）
* 不管怎样，这个异常会导致接口最终失败/不稳定，必须按上面修。

---

## 你现在最该检查的 2 个点（不用猜）

1. `CategoryMapper.pageQuery` **有没有 `@AutoFill`**（99% 是这里）
2. `AutoFillAspect` 的切点是不是 `execution(* com.sky.mapper..*(..))` 这种“全拦截”

---

如果你把下面两段代码贴出来（我就能直接给你改成正确版本）：

* `CategoryMapper` 接口（尤其 pageQuery 上的注解）
* `AutoFillAspect` 的 `@Pointcut` + `autoFill()` 方法（第 70-90 行附近）
