好，我们直接写一套你能 copy-paste 用的版本：**application.yml + @Configuration**。

---

## 1. `application.yml`（Druid + MySQL + Spring Boot）

假设你用的是 **MySQL 8 驱动**（推荐），数据库名叫 `sky_take_out`：

```yaml
spring:
  datasource:
    # ===== 基本数据库配置 =====
    url: jdbc:mysql://localhost:3306/sky_take_out
      ?useSSL=false
      &useUnicode=true
      &characterEncoding=UTF-8
      &serverTimezone=Asia/Shanghai
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver

    # ===== 使用 Druid 作为连接池 =====
    type: com.alibaba.druid.pool.DruidDataSource

    # ===== Druid 连接池核心参数 =====
    druid:
      # 初始化连接数
      initial-size: 5
      # 最小空闲连接数
      min-idle: 5
      # 最大连接数
      max-active: 20
      # 获取连接最大等待时间（ms）
      max-wait: 60000

      # 超过多久才认为是慢 SQL（ms）
      slow-sql-millis: 2000

      # 定期检测空闲连接 & 连接是否可用
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000

      # 用来检测连接是否可用的 SQL
      validation-query: "SELECT 1"
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false

      # 是否缓存 preparedStatement
      pool-prepared-statements: true
      max-pool-prepared-statement-per-connection-size: 20

      # 开启监控的 filter（stat:统计，wall:防火墙，slf4j或log4j:日志）
      filters: stat,wall,slf4j
```

> ⚠️ 注意
>
> * `driver-class-name` 一定要和你 pom 里的 mysql 版本对应（8.x → `com.mysql.cj.jdbc.Driver`）
> * `spring.datasource.type` 指向 `DruidDataSource`，这样 Spring Boot 会用 Druid 做连接池。

---

## 2. Java 配置类：`DruidDataSourceConfig.java`

放在例如 `com.sky.config` 包下：

```java
package com.sky.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Druid 数据源配置
 */
@Configuration
public class DruidDataSourceConfig {

    // ===== 从 application.yml 里读取基础配置 =====
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    // ===== 连接池配置（也可以继续 @Value 注入 druid.* 的配置） =====
    @Value("${spring.datasource.druid.initial-size:5}")
    private int initialSize;

    @Value("${spring.datasource.druid.min-idle:5}")
    private int minIdle;

    @Value("${spring.datasource.druid.max-active:20}")
    private int maxActive;

    @Value("${spring.datasource.druid.max-wait:60000}")
    private int maxWait;

    @Value("${spring.datasource.druid.time-between-eviction-runs-millis:60000}")
    private long timeBetweenEvictionRunsMillis;

    @Value("${spring.datasource.druid.min-evictable-idle-time-millis:300000}")
    private long minEvictableIdleTimeMillis;

    @Value("${spring.datasource.druid.validation-query:SELECT 1}")
    private String validationQuery;

    @Value("${spring.datasource.druid.test-while-idle:true}")
    private boolean testWhileIdle;

    @Value("${spring.datasource.druid.test-on-borrow:false}")
    private boolean testOnBorrow;

    @Value("${spring.datasource.druid.test-on-return:false}")
    private boolean testOnReturn;

    @Value("${spring.datasource.druid.pool-prepared-statements:true}")
    private boolean poolPreparedStatements;

    @Value("${spring.datasource.druid.max-pool-prepared-statement-per-connection-size:20}")
    private int maxPoolPreparedStatementPerConnectionSize;

    @Value("${spring.datasource.druid.filters:stat,wall,slf4j}")
    private String filters;

    @Bean
    public DataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();

        // ===== 基础 JDBC 配置 =====
        dataSource.setUrl(dbUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);

        // ===== 连接池大小配置 =====
        dataSource.setInitialSize(initialSize);
        dataSource.setMinIdle(minIdle);
        dataSource.setMaxActive(maxActive);
        dataSource.setMaxWait(maxWait);

        // ===== 空闲连接检测 & 回收策略 =====
        dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);

        // ===== 连接可用性检查 =====
        dataSource.setValidationQuery(validationQuery);
        dataSource.setTestWhileIdle(testWhileIdle);
        dataSource.setTestOnBorrow(testOnBorrow);
        dataSource.setTestOnReturn(testOnReturn);

        // ===== PSCache（可选） =====
        dataSource.setPoolPreparedStatements(poolPreparedStatements);
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(
                maxPoolPreparedStatementPerConnectionSize
        );

        // ===== 监控 filter =====
        try {
            dataSource.setFilters(filters);
        } catch (Exception e) {
            // 这里简单打印，真实项目可以打日志
            e.printStackTrace();
        }

        return dataSource;
    }
}
```

---

## 3. 一句话总结（方便你面试时说）

* **MySQL Driver** 负责“会说 MySQL 协议、建立单个连接”。
* **Druid DataSource** 负责“批量管理和复用这些连接 + 监控 + SQL 统计”。
* Spring Boot 通过 **DataSource 接口** 使用 Druid，不直接和 driver 打交道。

如果你愿意，下一步我可以帮你：

* 基于这套配置，画一张 “Controller → Service → Mapper → Druid → MySQL” 的 **ASCII 架构图**，
* 或者写一段 **Amazon 面试口吻**：“我在项目里如何配置并优化数据库连接池”。
