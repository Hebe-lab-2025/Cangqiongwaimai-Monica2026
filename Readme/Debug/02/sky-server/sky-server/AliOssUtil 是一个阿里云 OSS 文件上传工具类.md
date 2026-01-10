下面我**按“做什么 → 每一段在干嘛 → 关键点/坑”**给你把这个 `AliOssUtil` 讲清楚，**偏实战 + 面试都能用**。

---

## 一句话总览

**`AliOssUtil` 是一个阿里云 OSS 文件上传工具类：把字节数组上传到 OSS，并返回可访问的文件 URL。**

---

## 一、类结构在干嘛

```java
@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {
```

### 1️⃣ Lombok 注解

* `@Data`

  * 自动生成 `getter / setter / toString / equals / hashCode`
* `@AllArgsConstructor`

  * 自动生成 **全参构造函数**
* `@Slf4j`

  * 自动生成 `log` 日志对象（等价于 `private static final Logger log`）

👉 **好处**：工具类更干净，少写样板代码。

---

## 二、这四个字段是什么（非常重要）

```java
private String endpoint;
private String accessKeyId;
private String accessKeySecret;
private String bucketName;
```

| 字段              | 作用                                        |
| --------------- | ----------------------------------------- |
| endpoint        | OSS 服务地址（如 `oss-cn-beijing.aliyuncs.com`） |
| accessKeyId     | 阿里云访问 ID                                  |
| accessKeySecret | 阿里云访问密钥                                   |
| bucketName      | OSS 中的桶名                                  |

📌 这 4 个通常来自 `application.yml`，由 Spring 注入。

---

## 三、核心方法：upload()

```java
public String upload(byte[] bytes, String objectName)
```

### 参数含义

* `bytes`：文件内容（图片 / pdf / excel 等）
* `objectName`：文件在 OSS 中的路径和名字
  例：`images/2026/01/avatar.png`

---

## 四、上传流程（一步一步）

### Step 1：创建 OSS 客户端

```java
OSS ossClient = new OSSClientBuilder()
        .build(endpoint, accessKeyId, accessKeySecret);
```

👉 相当于：

* 和 OSS 建立连接
* 带着身份信息（AK / SK）

---

### Step 2：执行上传

```java
ossClient.putObject(
    bucketName,
    objectName,
    new ByteArrayInputStream(bytes)
);
```

含义：

```
bucketName / objectName  ← bytes
```

* `ByteArrayInputStream`：把 byte[] 包装成流
* OSS 是 **流式上传**

---

### Step 3：异常处理（两类）

#### 1️⃣ `OSSException`

```java
catch (OSSException oe)
```

👉 **请求到了 OSS，但 OSS 拒绝了**

* bucket 不存在
* 没权限
* objectName 不合法

#### 2️⃣ `ClientException`

```java
catch (ClientException ce)
```

👉 **客户端问题**

* 网络不通
* endpoint 配错
* DNS 问题

---

### Step 4：关闭客户端（必须）

```java
finally {
    if (ossClient != null) {
        ossClient.shutdown();
    }
}
```

📌 **非常关键**

* 不关会导致连接泄漏
* 面试经常追问

---

## 五、返回值是怎么拼出来的

```java
StringBuilder stringBuilder = new StringBuilder("https://");
stringBuilder
        .append(bucketName)
        .append(".")
        .append(endpoint)
        .append("/")
        .append(objectName);
```

最终格式：

```
https://<bucket>.<endpoint>/<objectName>
```

例子：

```
https://sky-bucket.oss-cn-beijing.aliyuncs.com/images/a.png
```

👉 **这是 OSS 的标准公网访问规则**

---

## 六、日志 + 返回

```java
log.info("上传成功，文件访问路径：{}", stringBuilder.toString());
return stringBuilder.toString();
```

* 打日志（方便排错）
* 返回 URL 给前端 / DB 存储

---

## 七、这个工具类通常怎么用？

### Controller / Service 里

```java
String url = aliOssUtil.upload(file.getBytes(), "images/" + uuid + ".png");
```

然后：

* 把 `url` 存数据库
* 或直接返回给前端

---

## 八、你现在项目里的“关键认知点”

### 1️⃣ 这是 **对象存储**，不是文件系统

* 没目录，`images/xxx.png` 只是 key

### 2️⃣ 这个类 **可以 1:1 改成 MinIO**

只要把：

* `OSSClient` → `MinioClient`
* endpoint / AK / SK 换成本地的

👉 你前面问 **MinIO 服务器**，正好接得上。

---

## 面试一句话版本（直接背）

> 我们封装了一个 OSS 工具类，基于阿里云 OSS SDK，把文件以流的方式上传到指定 bucket，并返回标准的公网访问 URL，同时对 OSS 侧异常和客户端异常分别处理，确保资源正确释放。

---

如果你愿意，我可以下一步直接帮你做：

* ✅ **把这个 `AliOssUtil` 改成 `MinioUtil`（几乎不改业务代码）**
* ✅ **画一张“前端 → Controller → OSS → URL 返回”的流程图**
* ✅ **帮你总结 OSS / MinIO 的面试对比表**
