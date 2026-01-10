下面我用**“一句话 → 本质 → 调用流程 → 和 HTTP / Client 的关系 → 面试版”**把 **RPC** 讲透。

---

## 一句话解释（先给直觉）

**RPC（Remote Procedure Call）= 像调用本地方法一样，去调用远程服务器的方法。**

---

## RPC 的“本质翻译”

> **RPC = 把“网络通信”伪装成“方法调用”**

你写的代码看起来是这样：

```java
userService.getUserById(1);
```

但实际发生的是：

```text
本地 JVM
  ↓ 序列化参数
  ↓ 网络发送
远程 JVM
  ↓ 反序列化
  ↓ 执行方法
  ↓ 返回结果
```

---

## RPC 的完整调用链（非常重要）

```
Caller（调用方）
  ↓
Client Stub（客户端代理）
  ↓
序列化（JSON / Protobuf）
  ↓
网络传输（TCP / HTTP）
  ↓
Server Stub（服务端代理）
  ↓
真实方法执行
  ↓
返回结果（反向同一条路）
```

👉 **RPC 不是“没有网络”，而是“你感觉不到网络”**

---

## RPC 一定包含的 4 个核心组件

| 组件            | 作用         |
| ------------- | ---------- |
| Client Stub   | 伪装成本地方法    |
| Serialization | 参数 / 返回值编码 |
| Transport     | 网络传输       |
| Server Stub   | 接收并调用真实方法  |

---

## RPC 和你前面学的 Client 的关系（关键连接点）

你前面学的：

```java
OSS ossClient = new OSSClientBuilder().build(...)
```

**本质就是 RPC Client**

* 你调用的是 `ossClient.putObject(...)`
* 实际是：

  * SDK 帮你序列化请求
  * 发 HTTP 请求
  * OSS 服务端执行
  * 返回结果

👉 **“SDK Client = RPC Client 的一种形式”**

---

## RPC vs HTTP（最容易混）

| 维度   | RPC  | HTTP API     |
| ---- | ---- | ------------ |
| 调用方式 | 方法调用 | URL + Method |
| 语义   | 强类型  | 弱类型          |
| 使用感受 | 本地函数 | 网络接口         |
| 常见场景 | 内部服务 | 对外接口         |

📌 **底层可以都是 HTTP**

* RPC ≠ 不用 HTTP
* RPC 是**抽象层**

---

## 常见 RPC 框架（看到就要反射）

| 框架                     | 底层                |
| ---------------------- | ----------------- |
| gRPC                   | HTTP/2 + Protobuf |
| Dubbo                  | TCP / HTTP        |
| Thrift                 | TCP               |
| Spring Cloud OpenFeign | HTTP              |

---

## 一个最小 RPC 示例（概念级）

### 接口（共享）

```java
public interface OrderService {
    Order getOrder(Long id);
}
```

### 调用方（你）

```java
orderService.getOrder(1L);
```

### 实际发生

* 你没 new 实现类
* Spring / RPC 框架给你的是 **代理对象**
* 代理对象 → 发网络请求

---

## RPC 的核心优点 & 代价

### ✅ 优点

* 调用方式自然
* 强类型
* 代码可读性高

### ❌ 代价

* 网络失败风险
* 超时 / 重试
* 版本兼容问题

---

## RPC 和 AOP / Proxy 的关系（高级理解）

RPC 本质靠的就是：

* **动态代理**
* **拦截方法调用**
* **在调用前后加逻辑**

这和你现在用的：

* `@Aspect`
* `JoinPoint`
* `Proxy`

**是同一套思想**

---

## 面试一句话版本（直接背）

> RPC 是一种远程调用机制，通过客户端代理将网络通信封装成方法调用，使开发者可以像调用本地方法一样调用远程服务，底层通常涉及序列化、网络传输和服务端代理执行。

---

## 给你一个“条件反射判断题”

看到下面代码，你应该立刻想到 **RPC** 👇

```java
userService.getUser(1L);
```

但：

* 没有实现类
* 没有 new
* 却能跑

👉 **100% 是 RPC / Proxy**

---

如果你愿意，下一步我可以直接：

* 🔥 **用你现在的 `AliOssUtil` 对比解释“SDK RPC”**
* 🔥 **画一张 RPC vs HTTP 的一页 ASCII 图**
* 🔥 **用 Spring Cloud OpenFeign 写一个 5 行 RPC 示例**

你点一个，我直接展开。
