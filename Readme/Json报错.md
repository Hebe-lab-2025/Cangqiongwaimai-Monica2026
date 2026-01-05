你这段代码里**核心问题不是 `LocalDateTime` 本身**，而是 **Jackson 模块配置写错了**，导致**序列化 / 反序列化混乱甚至失效**。

我按「问题 → 原因 → 正确写法」给你讲清楚。

---

## 一、最严重的问题（一定会出错）

### ❌ 你把 **Serializer 当成 Deserializer 注册了**

```java
.addDeserializer(LocalDateTime.class, new LocalDateTimeSerializer(...))
```

这是**错误的**。

* `addDeserializer` → 只能接 **XXXDeserializer**
* `addSerializer` → 才能接 **XXXSerializer**

你现在的代码 **编译可能能过，但运行一定异常或不生效**。

---

## 二、逐条指出你的错误配置

### 1️⃣ LocalDateTime 序列化 / 反序列化写反了

❌ 错误：

```java
.addDeserializer(LocalDateTime.class, new LocalDateTimeSerializer(...))
```

✅ 正确：

```java
.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(...))
```

---

### 2️⃣ LocalDate 用了 **LocalDateTimeSerializer（完全不对）**

❌ 错误：

```java
.addDeserializer(LocalDate.class, new LocalDateTimeSerializer(...))
```

✅ 正确：

```java
.addSerializer(LocalDate.class, new LocalDateSerializer(...))
```

---

### 3️⃣ LocalTime 的 formatter 用成了日期格式

❌ 错误：

```java
new LocalTimeSerializer(DateTimeFormatter.ofPattern(DEFALUT_DATE_FORMAT))
```

✅ 正确：

```java
new LocalTimeSerializer(DateTimeFormatter.ofPattern(DEFALUT_TIME_FORMAT))
```

---

## 三、正确、规范、可用的完整写法（推荐直接用）

```java
public class JacksonObjectMapper extends ObjectMapper {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    public JacksonObjectMapper() {
        super();

        // 忽略未知字段
        this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        SimpleModule module = new SimpleModule();

        // ===== LocalDateTime =====
        module.addDeserializer(
                LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT))
        );
        module.addSerializer(
                LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT))
        );

        // ===== LocalDate =====
        module.addDeserializer(
                LocalDate.class,
                new LocalDateDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT))
        );
        module.addSerializer(
                LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT))
        );

        // ===== LocalTime =====
        module.addDeserializer(
                LocalTime.class,
                new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT))
        );
        module.addSerializer(
                LocalTime.class,
                new LocalTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT))
        );

        this.registerModule(module);
    }
}
```

---

## 四、这样配置后 JSON 效果

```json
{
  "date": "2026-01-05",
  "time": "14:30:00",
  "dateTime": "2026-01-05 14:30:00"
}
```

✔ 可反序列化
✔ 可序列化
✔ 不依赖时区
✔ 适合前后端交互

---

## 五、一句话总结

> **你现在的问题不是 `LocalDateTime`，而是 Jackson 的 `Serializer / Deserializer` 用反了。**

如果你愿意，我也可以帮你：

* 改成 **全局 Spring Boot 自动配置**
* 或改成 **ISO-8601（`2026-01-05T14:30:00`）标准格式**
* 或解释 **为什么不要在 JSON 里直接用 `LocalDateTime`**

随时说 👍
