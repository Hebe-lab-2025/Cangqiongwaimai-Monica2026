我愿意。下面我直接把 3 件事都给你（偏面试 + 可落地到 Spring Boot 项目）。

---

## 1) IoC + AOP 执行流程 ASCII 图（从启动到运行时）

### 1.1 Spring 启动期：IoC 创建 Bean + AOP 织入代理

```text
Spring Boot Start
  |
  v
@ComponentScan / @Configuration
  |
  v
BeanDefinition(类信息) 注册到容器
  |
  v
实例化 Bean（Constructor）
  |
  v
属性注入（@Autowired / @Value） + aware 回调
  |
  v
BeanPostProcessor 前置处理
  |      (AOP关键：AbstractAutoProxyCreator 在这里介入)
  |----------------------------------------------+
  v                                              |
初始化（@PostConstruct / InitializingBean）        |
  |                                              |
  v                                              |
BeanPostProcessor 后置处理 ----------------------->|  判断该 Bean 是否匹配切点
  |                                              |  匹配 -> 创建 Proxy(代理对象)
  v                                              |  不匹配 -> 原对象
Bean 放入单例池（singletonObjects）               |
  |                                              |
  +-------------------- 运行时从容器拿到的 Bean ----+
                     （可能已经是代理）
```

### 1.2 运行期：调用方法时 AOP 如何执行

```text
Controller 调用 Service.method()
  |
  v
拿到的其实是 Proxy（JDK动态代理 / CGLIB）
  |
  v
Proxy.invoke()
  |
  v
按顺序执行 Advice Chain（拦截器链）
  |
  +--> @Around (前半段)
  +--> @Before
  +--> 目标方法执行 (JoinPoint)
  +--> @AfterReturning (成功)
  +--> @AfterThrowing (异常)
  +--> @After (最终都会走)
  |
  v
@Around (后半段) 返回结果
```

---

## 2) 用 JWT / 日志 写一个完整 AOP 示例（可直接放你项目）

目标：**任何带 token 的请求**，在进入 Service 方法时自动：

* 从 `HttpServletRequest` 拿 token
* 解析 JWT（拿 userId/username）
* 打日志：耗时、方法名、参数、用户信息
* 出错时统一记录异常

> 说明：你项目已经有 `JwtUtil` + `JwtProperties` + 拦截器，这里 AOP 主要做**日志/审计**（不建议用 AOP 做强制鉴权，鉴权更适合 Filter/Interceptor/Spring Security）。

### 2.1 AOP 代码（Around + Exception logging）

```java
package com.sky.aop;

import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class AuditLogAspect {

    private final JwtProperties jwtProperties;

    public AuditLogAspect(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    // ✅ 只拦截你的 service 层（你也可以改成 controller 层）
    @Pointcut("execution(* com.sky.service..*(..))")
    public void serviceMethods() {}

    @Around("serviceMethods()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();

        // 1) 方法信息
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String className = sig.getDeclaringTypeName();
        String methodName = sig.getName();

        // 2) 尝试从当前请求里取 token（没有请求上下文也不要崩）
        Long userId = null;
        String token = null;
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();

                // 你 yml 里 admin-token-name: token
                String tokenHeaderName = jwtProperties.getAdminTokenName();
                token = req.getHeader(tokenHeaderName);

                if (token != null && !token.isEmpty()) {
                    Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
                    // 这里的 key 取决于你项目 Claims 放了什么
                    // 常见：empId / userId / id
                    Object idObj = claims.get("empId"); // 你可以按项目改
                    if (idObj != null) userId = Long.valueOf(idObj.toString());
                }
            }
        } catch (Exception e) {
            // ❗日志不要影响业务
            log.warn("AuditLogAspect: failed to parse token, ignore. reason={}", e.toString());
        }

        // 3) 执行 + 记录
        try {
            Object result = pjp.proceed();
            long cost = System.currentTimeMillis() - start;

            // 注意：参数不要全打，避免敏感信息（如密码）
            log.info("[AUDIT] {}.{} userId={} costMs={}", className, methodName, userId, cost);
            return result;

        } catch (Throwable ex) {
            long cost = System.currentTimeMillis() - start;
            log.error("[AUDIT-ERR] {}.{} userId={} costMs={} ex={}",
                    className, methodName, userId, cost, ex.toString());
            throw ex;
        }
    }
}
```

**你需要注意的坑（面试也爱问）：**

* AOP 拿不到请求上下文：**异步线程 / 定时任务**里 `RequestContextHolder` 可能是 null，所以要判空。
* 不要把密码、token 明文打印到日志（安全）。
* `execution(* com.sky.service..*(..))` 会拦截所有 service，想精确就用注解方式（下面给你）。

### 2.2 更专业：用自定义注解精确拦截（推荐）

```java
// 1) 注解
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {
    String value() default "";
}

// 2) Pointcut 改成 @annotation
@Pointcut("@annotation(com.sky.aop.Audit)")
public void auditMethods() {}

// 3) 在方法上打标
@Audit("Update Employee")
public void updateEmployee(...) { ... }
```

---

## 3) 面试追问 Q&A（含底层原理，直接背关键词）

### Q1：IoC 是什么？解决什么问题？

* **关键词**：对象创建交给容器、依赖注入、解耦、可测试、生命周期管理
* **一句话**：IoC 把 “new 对象 + 组装依赖” 从业务代码移到 Spring 容器，让组件更松耦合、更易扩展。

### Q2：Spring 是怎么完成依赖注入的？

* **关键词**：BeanDefinition、实例化、属性填充（populateBean）、反射、AutowiredAnnotationBeanPostProcessor
* **一句话**：启动时解析 BeanDefinition，实例化后通过反射给字段/构造器注入依赖，最后进入初始化回调并放入单例池。

### Q3：AOP 是什么？为什么要用？

* **关键词**：横切关注点、解耦、日志/事务/监控/鉴权
* **一句话**：AOP 用代理把通用逻辑（日志、事务等）织入目标方法，避免每个方法重复写样板代码。

### Q4：Spring AOP 是怎么实现的？JDK vs CGLIB？

* **JDK 动态代理**：基于接口 `InvocationHandler`，**必须有接口**
* **CGLIB**：生成子类字节码代理，**不需要接口**，但 final 类/方法不能代理
* **Spring 选择**：有接口优先 JDK；否则用 CGLIB（也可强制）

### Q5：@Transactional 为什么有时失效？

* **关键词**：代理、同类内部调用 self-invocation、public 方法、异常类型、回滚规则
* **典型坑**：`this.method()` 内部调用绕过代理 → 事务/AOP 不生效
* **一句话**：事务是 AOP 代理实现的，必须通过代理对象调用才会进拦截器链。

### Q6：AOP 执行顺序是什么？

* **关键词**：Around 包住一切、Before、目标方法、AfterReturning/AfterThrowing、After
* **一句话**：Around(前) → Before → method → AfterReturning/Throwing → After → Around(后)。

### Q7：AOP 会影响性能吗？怎么控制？

* **关键词**：代理开销、切点范围、只拦截关键路径、注解方式、日志采样
* **一句话**：AOP 有少量代理与链路开销，控制切点范围和日志内容就能把影响降到很小。

---

如果你把你项目的 **JWT claims 里到底放的 key 名**（比如 `empId` / `userId` / `id`）和 **token header 名**（你 yml 里是 `token`）贴出来，我可以把上面 AOP 里的解析部分改成 100% 适配你项目的版本。
