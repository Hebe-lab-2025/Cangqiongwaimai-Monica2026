给你画一张清晰的 ASCII 流程图 👇（基于你刚才那套 Druid + MySQL + Spring Boot 配置）

```text
           ┌─────────────────────────┐
           │        Client           │
           │  (Browser / App / Postman) 
           └────────────┬────────────┘
                        │  HTTP Request
                        v
           ┌─────────────────────────┐
           │     Controller 层       │
           │  @RestController        │
           └────────────┬────────────┘
                        │ 调业务逻辑
                        v
           ┌─────────────────────────┐
           │       Service 层        │
           │  @Service               │
           └────────────┬────────────┘
                        │ 调持久化
                        v
           ┌─────────────────────────┐
           │   Mapper / Repository   │
           │  @Mapper / @Repository  │
           └────────────┬────────────┘
                        │ 通过 JDBC 调用
                        v
           ┌─────────────────────────┐
           │   DataSource 接口       │
           │ (实际是 DruidDataSource)│
           └────────────┬────────────┘
                        │  从连接池拿连接
                        v
           ┌─────────────────────────┐
           │   Druid 连接池 / Pool   │
           │  维护多个 JDBC Connection│
           └────────────┬────────────┘
                        │  使用已建好的连接
                        v
           ┌─────────────────────────┐
           │      MySQL 数据库       │
           │  (使用 com.mysql.cj.jdbc.Driver)
           └─────────────────────────┘
```

> 关键点（你面试可以顺口带一下）
>
> * Controller / Service / Mapper 只依赖 **DataSource** 抽象，不关心底层是 Druid 还是 Hikari。
> * Druid 作为 **连接池实现**，内部再用 MySQL Driver 建立真正的物理连接，并做复用、监控、超时回收。
