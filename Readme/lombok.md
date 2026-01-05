
下面我给你**逐段、逐字段**讲解这份 `application.yml`，
**全部是 GitHub 可直接阅读的 Markdown 格式**，适合当项目配置说明文档。

---

# Spring Boot `application.yml` 配置详解

> 本文用于解释项目中 `application.yml` 各配置块的含义、作用及运行时效果。

---

## 一、`server` —— 服务端口配置

```yaml
server:
  port: 8080
```

### 含义

* 指定 Spring Boot 内嵌服务器（Tomcat）监听端口

### 效果

```text
http://localhost:8080
```

📌 如果不配置，默认端口是 `8080`

---

## 二、`spring` —— Spring Boot 核心配置

### 1️⃣ `spring.profiles`（环境配置）

```yaml
spring:
  profiles:
    active: dev
```

### 含义

* 激活 `dev` 环境

### 实际效果

```text
application-dev.yml 会被加载
```

📌 常见环境：

* `dev`：开发环境
* `test`：测试环境
* `prod`：生产环境

---

### 2️⃣ `spring.main.allow-circular-references`

```yaml
spring:
  main:
    allow-circular-references: true
```

### 含义

* 允许 Spring Bean **循环依赖**

### 举例

```text
A → 依赖 B
B → 依赖 A
```

📌 Spring Boot 2.6+ 默认 **禁止循环依赖**

⚠ 不推荐长期使用，最好重构设计

---

### 3️⃣ `spring.datasource.druid`（数据库连接池）

```yaml
spring:
  datasource:
    druid:
      driver-class-name: ${sky.datasource.driver-class-name}
      url: jdbc:mysql://${sky.datasource.host}:${sky.datasource.port}/${sky.datasource.database}?...
      username: ${sky.datasource.username}
      password: ${sky.datasource.password}
```

### 含义

* 使用 **Druid 数据库连接池**
* 数据库信息通过 **占位符** 从其他配置注入

---

### 占位符解释

```yaml
${sky.datasource.host}
```

表示值来自：

```yaml
sky:
  datasource:
    host: localhost
```

📌 通常放在：

* `application-dev.yml`
* `application-prod.yml`

---

### JDBC URL 参数说明（重点）

```text
serverTimezone=Asia/Shanghai     时区
useUnicode=true                 使用 Unicode
characterEncoding=utf-8         UTF-8 编码
zeroDateTimeBehavior=convertToNull
useSSL=false                    关闭 SSL
allowPublicKeyRetrieval=true    允许公钥获取
```

---

## 三、`mybatis` —— MyBatis 配置

```yaml
mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.sky.entity
  configuration:
    map-underscore-to-camel-case: true
```

---

### 1️⃣ mapper-locations

```yaml
mapper-locations: classpath:mapper/*.xml
```

📌 MyBatis Mapper XML 存放路径

```text
resources/mapper/UserMapper.xml
```

---

### 2️⃣ type-aliases-package

```yaml
type-aliases-package: com.sky.entity
```

### 含义

* 为实体类设置别名
* XML 中可直接写类名

```xml
<resultType>User</resultType>
```

---

### 3️⃣ 驼峰命名自动映射（非常重要）

```yaml
map-underscore-to-camel-case: true
```

### 效果

| 数据库字段       | Java 属性    |
| ----------- | ---------- |
| user_name   | userName   |
| create_time | createTime |

📌 强烈推荐开启

---

## 四、`logging` —— 日志级别控制

```yaml
logging:
  level:
    com:
      sky:
        mapper: debug
        service: info
        controller: info
```

---

### 各级别含义

| 级别    | 说明        |
| ----- | --------- |
| debug | 调试信息（SQL） |
| info  | 业务流程      |
| warn  | 警告        |
| error | 错误        |

---

### 实际效果

```text
mapper     → 打印 SQL
service    → 业务日志
controller → 请求日志
```

📌 常用于开发环境排错

---

## 五、`sky.jwt` —— JWT 配置（自定义配置）

```yaml
sky:
  jwt:
    admin-secret-key: itcast
    admin-ttl: 7200000
    admin-token-name: token
```

---

### 1️⃣ admin-secret-key

```yaml
admin-secret-key: itcast
```

* JWT 签名密钥
* **必须保密**

---

### 2️⃣ admin-ttl

```yaml
admin-ttl: 7200000
```

* Token 过期时间（毫秒）
* `7200000 ms = 2 小时`

---

### 3️⃣ admin-token-name

```yaml
admin-token-name: token
```

### 含义

* 前端请求头中携带 Token 的名字

```http
Authorization: token=xxx
```

或

```http
token: xxx
```

---

## 六、配置整体加载顺序（重点）

```text
application.yml
    ↓
application-dev.yml
    ↓
@ConfigurationProperties
    ↓
Java Bean
```

---

## 七、常见问题排查

### ❌ 数据库连不上

* host / port 配置错误
* 占位符未定义

### ❌ MyBatis 查不到 Mapper

* mapper-locations 路径错误

### ❌ JWT 校验失败

* secret-key 不一致
* token 过期

---

## 八、最佳实践建议

* 多环境配置拆分（dev / prod）
* 密码不要写死，使用环境变量
* `mapper` 日志仅开发环境开启
* JWT 配置统一放在 `@ConfigurationProperties`

---

## 九、一句话总结

> 这份 `application.yml`
> **负责端口、环境、数据库、MyBatis、日志、JWT 的统一配置**

---

如果你愿意，我可以继续帮你：

* 画一张 **Spring Boot 配置加载流程图**
* 拆分 `application-dev.yml / prod.yml`
* 讲 `@ConfigurationProperties` 如何绑定 `sky.jwt`
* 分析 JWT + 拦截器完整认证流程

你选一个，我继续 👌


下面我**按你的要求分三步来**，全部用 **GitHub 可直接阅读的 Markdown**，结构清晰、适合当学习笔记。

---

# Lombok：国际音标、词义与在项目中的作用

---

## 一、Lombok 的国际音标（发音）

### 英文单词：**Lombok**

📌 **国际音标（IPA）：**

```
/ˈlɒm.bɒk/
```

### 近似中文发音（仅辅助理解）

```
“隆-博克”
```

> 重音在第一个音节 **LOM**

---

## 二、Lombok 的词义来源

### 1️⃣ 原本的含义

**Lombok** 是一个地名：

* 🇮🇩 印度尼西亚的一座岛屿
* 位于巴厘岛东边

📌 项目作者用地名命名，没有具体“编程语义”

---

### 2️⃣ 在 Java 中的含义（实际使用意义）

> **Project Lombok**
> 一个用于 **减少 Java 模板代码（boilerplate code）** 的工具库

---

## 三、什么是 Lombok（核心解释）

### 一句话定义（重点）

> **Lombok 是一个 Java 编译期工具，通过注解自动生成 Getter / Setter / 构造器等代码**

---

### 解决了什么问题？

#### ❌ 没有 Lombok（传统 Java）

```java
public class User {

    private Long id;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

📌 代码多、重复、可读性差

---

#### ✅ 使用 Lombok

```java
@Data
public class User {
    private Long id;
    private String name;
}
```

📌 **编译后效果是一样的**

---

## 四、Lombok 的工作原理（非常重要）

```text
.java 文件
   ↓（编译期）
Lombok 注解处理器
   ↓
自动生成字节码
   ↓
.class 文件
```

📌 **注意：**

* Lombok **不修改源码**
* Lombok **不影响运行性能**
* 生成的代码存在于 `.class` 文件中

---

## 五、你项目中 Lombok 依赖的含义

### Maven 依赖（来自你的 `pom.xml`）

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

### 含义说明

| 项          | 说明                   |
| ---------- | -------------------- |
| groupId    | Lombok 官方组织          |
| artifactId | Lombok 核心库           |
| scope      | 默认 `compile`（仅编译期使用） |

📌 **运行时不依赖 Lombok**

---

## 六、项目中 Lombok 常用注解（必会）

### 1️⃣ `@Data`

```java
@Data
public class User {}
```

等价于：

```java
@Getter
@Setter
@ToString
@EqualsAndHashCode
```

---

### 2️⃣ `@Getter / @Setter`

```java
@Getter
@Setter
private String name;
```

---

### 3️⃣ `@NoArgsConstructor / @AllArgsConstructor`

```java
@NoArgsConstructor
@AllArgsConstructor
public class User {}
```

---

### 4️⃣ `@Builder`

```java
User user = User.builder()
        .id(1L)
        .name("Tom")
        .build();
```

---

### 5️⃣ `@Slf4j`（非常常用）

```java
@Slf4j
public class UserService {

    public void test() {
        log.info("hello lombok");
    }
}
```

---

## 七、Lombok 在你这个项目里的实际用途

结合你前面的代码：

```java
@Data
@ConfigurationProperties(prefix = "sky.alioss")
public class AliOssProperties {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
}
```

📌 Lombok 做的事情：

* 自动生成 **Setter**
* 让 `@ConfigurationProperties` 能注入值
* 自动生成 Getter，方便使用

---

## 八、使用 Lombok 的注意事项（很重要）

### 1️⃣ IDE 必须安装插件

* IntelliJ IDEA：已内置 / 可开启
* Eclipse：需要单独安装

否则会出现：

```text
找不到方法 getXxx()
```

---

### 2️⃣ 不要滥用 `@Data`

⚠ 在 **Entity / DTO** 中可以
⚠ 在 **复杂业务对象** 中慎用（equals/hashCode 问题）

---

## 九、Lombok 的优缺点

### ✅ 优点

* 大幅减少样板代码
* 提高可读性
* 开发效率高

### ❌ 缺点

* 依赖 IDE 插件
* 新手不易理解“代码从哪来的”

---

## 十、一句话总结（记住这句）

> **Lombok 是一个编译期工具，用注解帮你写 Java 重复代码**

---

如果你愿意，我可以下一步帮你：

* 逐个讲 `@Data` 的“坑”
* 讲 Lombok 与 MyBatis / JPA 的搭配
* 讲 Lombok 在 Entity / DTO / VO 中的最佳使用姿势
* 对比 Lombok vs Record（Java 16+）

你选一个，我继续 👌

我懂你的意思了 👍
你这里的 **“蕾西”=「类型 / 类别」**。
下面我用 **GitHub 可直接看的 Markdown**，把**数据源的主要类型**一次性给你讲清楚，从**项目常用 → 架构层面**。

---

# 数据源（DataSource）有哪些类型？

> **数据源（DataSource）**
> 用于给应用程序提供数据的来源，不仅仅是数据库。

---

## 一、按「是否是数据库」分类（最常见）

### 1️⃣ 关系型数据库数据源（RDBMS）✅【最常用】

📌 特点：

* 基于表
* 支持 SQL
* 支持事务（ACID）

#### 常见类型

| 数据源        | 说明       |
| ---------- | -------- |
| MySQL      | 最常用      |
| PostgreSQL | 功能强      |
| Oracle     | 商用       |
| SQL Server | 微软       |
| MariaDB    | MySQL 分支 |

#### 项目示例（你现在用的）

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sky_take_out
```

---

### 2️⃣ 非关系型数据库（NoSQL）

📌 特点：

* 不固定表结构
* 高并发 / 高性能
* 不强调复杂事务

#### 常见类型

| 类型        | 代表            |
| --------- | ------------- |
| Key-Value | Redis         |
| 文档型       | MongoDB       |
| 列存储       | HBase         |
| 搜索引擎      | Elasticsearch |

#### 示例

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

---

## 二、按「数据存储位置」分类

### 3️⃣ 内存型数据源

📌 特点：

* 数据在内存中
* 速度极快
* 重启即丢失

#### 常见

| 数据源      | 用途   |
| -------- | ---- |
| Redis    | 缓存   |
| Caffeine | 本地缓存 |
| EhCache  | 本地缓存 |

---

### 4️⃣ 文件型数据源

📌 特点：

* 数据来自文件
* 不适合高并发写

#### 常见类型

| 类型    | 示例         |
| ----- | ---------- |
| 文本    | CSV / TXT  |
| Excel | XLS / XLSX |
| XML   | 配置 / 报文    |
| JSON  | 数据交换       |

📌 你项目里已经用到了：

```xml
org.apache.poi:poi
```

---

## 三、按「数据来源方式」分类（进阶）

### 5️⃣ 接口型数据源（API）

📌 特点：

* 数据来自第三方服务
* HTTP 调用

#### 常见

| 类型       | 示例     |
| -------- | ------ |
| REST API | 第三方平台  |
| RPC      | Dubbo  |
| GraphQL  | 新型 API |

---

### 6️⃣ 消息型数据源（流式）

📌 特点：

* 数据是“流”
* 异步

#### 常见

| 数据源      | 用途      |
| -------- | ------- |
| Kafka    | 日志 / 事件 |
| RabbitMQ | 消息      |
| RocketMQ | 高吞吐     |

---

## 四、按「Spring Boot 中的 DataSource」分类（重点）

### 7️⃣ JDBC DataSource（标准接口）

```java
javax.sql.DataSource
```

📌 所有数据库连接池 **最终都实现它**

---

### 8️⃣ 数据库连接池类型（非常重要）

> **连接池也是数据源的一种**

| 连接池      | 特点       |
| -------- | -------- |
| HikariCP | 默认、最快    |
| Druid    | 监控强（你在用） |
| C3P0     | 老牌       |
| DBUtils  | 简单       |

#### 你项目中用的是：

```xml
com.alibaba:druid-spring-boot-starter
```

---

## 五、按「架构复杂度」分类（进阶）

### 9️⃣ 单数据源

```text
应用 → 一个数据库
```

📌 小项目 / 初学者

---

### 🔟 多数据源

```text
应用 → 多个数据库
```

📌 场景：

* 读写分离
* 主从库
* 多业务库

---

### 1️⃣1️⃣ 动态数据源

📌 特点：

* 运行时切换
* 多租户

```java
@DS("slave")
public List<User> list() {}
```

---

## 六、总结总表（重点记这个）

| 分类角度      | 数据源类型          |
| --------- | -------------- |
| 数据结构      | 关系型 / NoSQL    |
| 存储位置      | 内存 / 文件        |
| 来源方式      | 数据库 / API / 消息 |
| Spring 视角 | JDBC / 连接池     |
| 架构层面      | 单数据源 / 多数据源    |

---

## 七、结合你当前项目的“真实数据源”

你现在的项目至少有：

```text
MySQL        → 主数据源
Redis        → 缓存数据源
Druid        → 数据库连接池
Excel / 文件 → 导入导出数据源
```

---

## 八、一句话总结（面试级）

> **数据源不只是数据库，凡是“数据从哪来”，它就是数据源**

---

如果你愿意，我可以下一步帮你讲：

* Spring Boot **多数据源配置实战**
* Druid vs HikariCP 详细对比
* Redis 为什么也是数据源
* 项目中如何设计「主数据源 + 缓存」

你挑一个，我继续 🔥

