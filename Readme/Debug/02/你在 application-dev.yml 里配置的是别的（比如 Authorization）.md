好，我们就按你说的，**直接给你一套“苍穹外卖专用”配置代码**，你可以 copy 进去对照教程改 👇

下面给两块：

1. `WebMvcConfiguration`（拦截器 + 静态资源 + 消息转换器）
2. `SwaggerConfiguration`（Knife4j + 全局 token header + 接口分组 + 只扫 `com.sky.controller`）

> ⚠️ 默认假设你的 token header 名叫 `token`，如果你在 `application-dev.yml` 里配置的是别的（比如 `Authorization`），只要改 **一个地方** 就行，我会标出来。

---

## 1️⃣ WebMvcConfiguration（含 JWT 拦截器 + 静态资源）

**包名建议：** `com.sky.config`
**文件名：** `WebMvcConfiguration.java`

```java
package com.sky.config;

import com.sky.interceptor.JwtTokenAdminInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC 相关配置
 * 1. 注册 JWT 管理端拦截器
 * 2. 配置静态资源映射（backend / front / swagger）
 * 3. 扩展消息转换器（统一使用自定义 ObjectMapper）
 */
@Configuration
@Slf4j
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");

        registry.addInterceptor(jwtTokenAdminInterceptor)
                // 需要拦截的路径
                .addPathPatterns("/admin/**")
                // 放行的路径（不需要登录的接口）
                .excludePathPatterns(
                        "/admin/employee/login",      // 登录
                        "/admin/employee/logout",     // 退出（看你项目有没有）
                        "/doc.html",                  // Knife4j 文档首页
                        "/swagger-resources/**",
                        "/v2/api-docs",
                        "/webjars/**",
                        "/error"                      // Spring Boot 默认错误页
                );
    }

    /**
     * 静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始设置静态资源映射...");

        // 后台管理端静态资源
        registry.addResourceHandler("/backend/**")
                .addResourceLocations("classpath:/backend/");

        // C 端（用户端）静态资源
        registry.addResourceHandler("/front/**")
                .addResourceLocations("classpath:/front/");

        // Knife4j / Swagger 静态资源
        registry.addResourceHandler("doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * 扩展消息转换器
     * （如果你项目里有自定义的 JacksonObjectMapper，就用它；没有可以先保持默认）
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");

        // 找出已有的 Jackson 转换器，替换为你自己的 ObjectMapper（可选）
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter jacksonConverter =
                        (MappingJackson2HttpMessageConverter) converter;
                // 如果你有自定义 ObjectMapper，在这里设置
                // jacksonConverter.setObjectMapper(new JacksonObjectMapper());
                break;
            }
        }
    }
}
```

> ✅ 要点回顾：
>
> * `/admin/**` 都会走 `JwtTokenAdminInterceptor`
> * `/admin/employee/login` 被放行，不需要 token
> * swagger 的 `doc.html` / `webjars/**` 被放行，方便你调试接口

---

## 2️⃣ SwaggerConfiguration（Knife4j + 全局 token header + 分组）

**包名建议：** `com.sky.config`
**文件名：** `SwaggerConfiguration.java`

依赖里你有：

* `springfox-boot-starter:3.0.0`
* `knife4j-spring-boot-starter:3.0.2`

下面配置是基于它们写的 👇

```java
package com.sky.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * Swagger / Knife4j 接口文档配置
 * 1. 只扫描 com.sky.controller 包
 * 2. 按 admin / user 分组
 * 3. 给所有接口统一加上 token header 参数（用于调试需要登录的接口）
 */
@Configuration
@EnableSwagger2
@EnableKnife4j
public class SwaggerConfiguration {

    /**
     * 全局的 header 参数列表（这里只加 token 一个）
     */
    private List<Parameter> globalParameters() {
        List<Parameter> params = new ArrayList<>();

        ParameterBuilder tokenPar = new ParameterBuilder();
        tokenPar.name("token")                 // ⚠️ 如果你 yml 里 admin-token-name 不是 token，就改这里
                .description("JWT token，在登录接口返回的 data.token 中获取")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(false);             // 不是所有接口都必须（比如登录接口不需要）

        params.add(tokenPar.build());
        return params;
    }

    /**
     * 管理端接口分组：扫描 com.sky.controller.admin 包
     */
    @Bean
    public Docket adminApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("管理端接口")
                .apiInfo(apiInfo("苍穹外卖项目接口文档 - 管理端"))
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.sky.controller.admin"))
                .paths(PathSelectors.any())
                .build()
                .globalOperationParameters(globalParameters());  // 全局 header 参数
    }

    /**
     * 用户端接口分组：扫描 com.sky.controller.user 包
     * 如果你暂时没有 user 包，也可以先注释掉
     */
    @Bean
    public Docket userApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("用户端接口")
                .apiInfo(apiInfo("苍穹外卖项目接口文档 - 用户端"))
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.sky.controller.user"))
                .paths(PathSelectors.any())
                .build()
                .globalOperationParameters(globalParameters());
    }

    /**
     * 通用文档信息
     */
    private ApiInfo apiInfo(String title) {
        return new ApiInfoBuilder()
                .title(title)
                .description("苍穹外卖项目接口文档")
                .version("1.0")
                .build();
    }
}
```

> 🔁 如果你想更“动态”一点，可以改成从配置读取 header 名，但先跑通最重要：

```java
tokenPar.name("token") // === 和 application-dev.yml 里的 sky.jwt.admin-token-name 保持一致
```

---

## 3️⃣ 使用 Swagger 调试“新增员工”时的步骤（配合上面代码）

1. **先登录一次：**

   * 打开 `POST /admin/employee/login`

   * Body 填：

     ```json
     {
       "username": "admin",
       "password": "123456"
     }
     ```

   * 拿到返回的 `data.token`（一整串）

2. Swagger 顶部会出现一个统一的 `token` 输入框（因为我们配了 global header）：

   * 把刚才获得的 `data.token` 粘进去 → 点击右边的 √ / 关掉弹窗

3. 再去调任何需要登录的接口，例如：

   * `POST /admin/employee`（新增员工）
   * 现在请求会自动带上 header：

     ```http
     token: <你的 JWT 字符串>
     ```

4. 后端的 `JwtTokenAdminInterceptor` 会在日志里打印类似：

   ```text
   jwt校验:eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```

   而不是 `jwt校验:null`。

---

如果你愿意，下一步我可以帮你：

* 对着你现在的 `JwtTokenAdminInterceptor` 源码，**逐行讲一次“拦截器到底做了什么”**；
* 或者，画一张 **“登录 + token 生成 + JwtTokenAdminInterceptor 校验 + ThreadLocal 存当前用户”** 的 ASCII 时序图，把整个链路串成一张“面试可以讲”的小图。
