这个异常很“明确”：

> `NoSuchMethodException: com.sky.dto.CategoryPageQueryDTO.setupdateTime(LocalDateTime)`

意思是：**你的 AOP 反射在 `CategoryPageQueryDTO` 里找一个叫 `setupdateTime(...)` 的方法，但这个方法根本不存在。**

---

## 1) 你为什么会遇到它

Java 的 setter 命名规则是：

* 字段 `updateTime` 的 setter 应该叫：`setUpdateTime(...)`（**U 要大写**）

但你的切面在用反射找的是：

* `setupdateTime(...)`（**u 小写**）

所以直接找不到方法 → 抛 `NoSuchMethodException`。

---

## 2) 你要改哪里（最常见原因）

去看你 `AutoFillAspect.autoFill()` 第 79 行附近（报错指到这里），大概率是类似这种代码：

```java
clazz.getDeclaredMethod("setupdateTime", LocalDateTime.class);
```

把它改成：

```java
clazz.getDeclaredMethod("setUpdateTime", LocalDateTime.class);
```

同理，如果还有 `setcreateTime`、`setupdateUser` 这种，也都要改成：

* `setCreateTime`
* `setUpdateUser`
* `setCreateUser`

---

## 3) 还有一个隐藏点：DTO 不该被 AutoFill 处理

你这个类是 **CategoryPageQueryDTO**（分页查询条件 DTO），一般只包含 `page/pageSize/name/type...`
**它不应该有 `updateTime` 这种字段，更不应该被“公共字段自动填充”切面处理**。

所以你还需要确认两件事：

### A. 你的切点是不是拦截太宽了？

比如你切到 `com.sky.mapper.*.*(..)` 或 `service.*`，然后把 DTO 也传进来了，导致它去填充。

✅ 正确做法：AutoFill 只应该对 **插入/更新的实体 Entity** 生效（比如 `Category` / `Employee`），而不是 PageQuery DTO。

### B. 你的 mapper 方法上是不是误加了 `@AutoFill`

比如你在 `pageQuery(CategoryPageQueryDTO dto)` 这种查询方法上也加了 `@AutoFill(OperationType.UPDATE/INSERT)`，那就会触发切面去“填充 updateTime”。

✅ 查询接口不要加 `@AutoFill`

---

## 4) 一步定位法（你照做就能立刻锁死）

1. 打开 `CategoryController.page()` → 调用链到 `CategoryMapper.pageQuery(...)`
2. 看 `CategoryMapper.pageQuery` **有没有 `@AutoFill`**
3. 看 `AutoFillAspect` 里反射找 setter 的字符串是不是写错大小写（`setupdateTime`）

---

## 5) 最推荐的“稳”改法（避免以后再踩）

不要手写 `"setUpdateTime"` 这种字符串，改成按字段名拼接：

```java
String fieldName = "updateTime";
String setter = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
// -> setUpdateTime
Method m = clazz.getMethod(setter, LocalDateTime.class);
```

这样以后字段名变了也不容易写错大小写。

---

如果你把 **AutoFillAspect 的 autoFill 方法代码（尤其是第 60-90 行）** 和 **CategoryMapper.pageQuery 方法**贴出来，我可以直接告诉你：到底是 **setter 拼错**、还是 **@AutoFill 标注错位置**、还是 **切点范围太大**。
