你这两段日志其实是**两个问题**，但都能快速定位。

---

## ✅ 问题 1：Aliyun OSS 报错 `Access key id should not be null or empty`

**结论：你的 OSS `accessKeyId` 没读到（是空/null）**，所以 `AliOssUtil.upload()` 在构建 OSSClient 时直接炸了。

### 证据链（从栈里看得很清楚）

* 抛错：`InvalidCredentialsException: Access key id should not be null or empty`
* 发生位置：

  * `com.sky.utils.AliOssUtil.upload(AliOssUtil.java:31)`
  * `CommonController.upload(CommonController.java:50)`

➡️ 也就是：**上传接口调用 AliOssUtil 时，accessKeyId 为空**。

---

## ✅ 你应该怎么修（最常见 4 种原因 + 对应修法）

### 1️⃣ `application.yml` / `application-dev.yml` 没配置或写错 key

你项目一般长这样（示例）：

```yaml
sky:
  alioss:
    endpoint: oss-cn-xxx.aliyuncs.com
    access-key-id: yourKeyId
    access-key-secret: yourKeySecret
    bucket-name: yourBucket
```

**常见坑：**

* 写成了 `accessKeyId`（驼峰）但你 `@ConfigurationProperties` 期待的是 `access-key-id`
* profile 用的是 `dev`，但你只在 `application.yml` 配了（或反过来）

✅ 自查：启动日志里看看 active profile：

* `The following profiles are active: dev`

---

### 2️⃣ `@ConfigurationProperties` 没生效（属性类没被 Spring 扫描/注册）

如果你用的是这种属性类：

```java
@ConfigurationProperties(prefix = "sky.alioss")
public class AliOssProperties { ... }
```

需要满足其一：

* 类上加 `@Component`
* 或在配置类上加 `@EnableConfigurationProperties(AliOssProperties.class)`
* 或 Spring Boot 主启动类加 `@ConfigurationPropertiesScan`

---

### 3️⃣ 你在 `AliOssUtil` 里 new 了 properties（导致没有注入）

很多同学会这样写导致全是 null：

```java
AliOssProperties props = new AliOssProperties(); // ❌ 永远空
```

✅ 正确方式：**让 Spring 注入**

* 在 `AliOssUtil` 构造器注入 `AliOssProperties`
* 或直接在 Service 层拿 properties 传进去

---

### 4️⃣ 环境变量/配置中心没生效

如果你把 key 放在环境变量：

* `ALIOSS_ACCESS_KEY_ID`
* `ALIOSS_ACCESS_KEY_SECRET`

那要确认你的 yml 是否引用了它们，且变量名拼对。

---

## ✅ 最快定位方法（不需要猜）

在 `AliOssUtil.upload()` 里**临时打日志**（注意别打印 secret 全量）：

```java
log.info("oss endpoint={}, keyIdPresent={}", endpoint, (accessKeyId != null && !accessKeyId.isEmpty()));
```

如果 `keyIdPresent=false`，就证明是**配置没读到**，不用再怀疑网络/权限。

---

## ✅ 建议加一个“启动即失败”的保护（避免跑到运行期才炸）

在 properties 上加校验（最爽）：

```java
@NotBlank private String accessKeyId;
@NotBlank private String accessKeySecret;
```

再配合：

```java
@Validated
@ConfigurationProperties(prefix="sky.alioss")
```

这样启动时就直接报配置缺失，比运行中 500 更好排查。

---

---

## ✅ 问题 2：`Request method 'GET' not supported`

这就是你后面这行：

```
Resolved [HttpRequestMethodNotSupportedException: Request method 'GET' not supported]
```

**结论：你用 GET 调了一个只支持 POST/PUT/DELETE 的接口。**

✅ 修法：

* 检查你的 Controller 映射，比如：

  * `@PostMapping("/admin/employee/logout")`
* 但你用浏览器或某个请求发了 `GET /admin/employee/logout`

---

## 🎯 你现在应该立刻做的 2 步（最高性价比）

1. 打开 `application.yml` / `application-dev.yml`，确认 `sky.alioss.access-key-id` **真的存在且不为空**
2. 确认你调用 `/admin/employee/logout` 的方式是 **POST**（看浏览器 Network / Postman 方法）

---

如果你把下面两段贴出来，我可以直接告诉你是哪一行导致的（不用猜）：

* `AliOssUtil.java`（大概 20~50 行，含第 31 行附近）
* 你的 `application.yml` 里 `sky.alioss` 那段（把 key 用 `****` 打码即可）
