## 1) Spring 启动时：反射相关流程（ASCII）

```text
[Java main()]
     |
     v
SpringApplication.run()
     |
     v
[Create ApplicationContext]
     |
     v
[Scan classpath]  --(read class metadata)-->
  @Component/@Service/@Controller/@Configuration
     |
     v
[BeanDefinition Registry]
  (store: beanName, beanClass, scope, lazy, deps...)
     |
     v
[Instantiate non-lazy singletons]
     |
     +--> (Reflection) newInstance / Constructor.newInstance
     |        |
     |        v
     |   [Bean instance created]
     |
     v
[Populate Properties]  (DI 注入阶段)
     |
     +--> (Reflection) Field.set / Method.invoke / Constructor injection
     |
     v
[Aware callbacks]
  BeanNameAware / BeanFactoryAware / ApplicationContextAware ...
     |
     v
[BeanPostProcessor before-init]
  (AOP/注解处理/代理准备点之一)
     |
     v
[Init]
  @PostConstruct / InitializingBean.afterPropertiesSet / init-method
     |
     v
[BeanPostProcessor after-init]
     |
     +--> (AOP) create Proxy (JDK Proxy / CGLIB)
     |
     v
[Bean ready in container]
     |
     v
(Controller/Service 被调用时用的是：Bean 或 Proxy)
```

---

## 2) 结合你项目的 `@Autowired`：逐步模拟“反射注入”

以你常见结构举例：`CategoryController -> CategoryService -> CategoryMapper`

```text
1) 扫描到 CategoryController(@RestController)
   -> 注册 BeanDefinition(controller)

2) 创建 controller 实例
   -> 反射调用构造器 (默认无参 or 指定构造器)

3) 进入注入阶段 populateBean()
   -> 找到 controller 上的字段:
        @Autowired private CategoryService categoryService;

4) Spring 去容器里找依赖:
   - 先按 type: CategoryService
   - 若多个候选 -> 再按 @Qualifier / @Primary / 字段名

5) 若 service 还没创建 -> 递归创建 service
   -> 同样：
      a) 反射 new ServiceImpl()
      b) 注入它的依赖 (Mapper / Util / etc.)

6) 找到 mapper:
   - MyBatis 会提供一个代理对象 (JDK 动态代理)
   - Spring 把这个代理当作 bean 注入进去

7) 反射写入字段:
   Field field = CategoryController.class.getDeclaredField("categoryService");
   field.setAccessible(true);
   field.set(controllerInstance, serviceBeanOrProxy);

8) 若 AOP 命中（比如 @Transactional / 你的 @AutoFill）
   - 在 after-init 阶段，Spring 把 service 替换成 proxy
   - 所以 controller 实际拿到的是：proxy(service)
```

**你之前的报错 `NoSuchMethodException ... setUpdateTime(...)` 就是典型：AOP 切面里用反射找 setter，目标类里没有对应方法/命名不匹配。**

---

## 3) 反射 + IoC + AOP：面试追问清单（高频）

### A. 反射（Reflection）

* 反射能做什么？创建对象/访问字段/调用方法的三件套？
* `getMethod` vs `getDeclaredMethod` 区别？（含继承/私有）
* 反射为什么慢？（安全检查、方法查找、无法内联等）
* 反射怎么突破 private？`setAccessible(true)` 风险是什么？
* 反射在框架里常见用法：注解解析、DI、序列化、ORM、AOP

### B. IoC / DI

* IoC 是什么？和 DI 的关系？
* Spring 什么时候创建 Bean？（非懒加载单例在启动创建）
* Bean 生命周期你能从创建到可用按顺序说出来吗？
* `@Autowired` 按什么规则注入？（type -> qualifier -> primary -> name）
* 循环依赖怎么解决？为什么构造器注入更容易爆？
* `@ComponentScan` 扫描不到会怎样？如何排查？

### C. AOP（重点）

* AOP 解决什么问题？（横切关注点：日志、事务、鉴权、审计、自动填充）
* Spring AOP 用什么实现？（JDK Proxy vs CGLIB，选择条件）
* 什么时候创建代理？（BeanPostProcessor after-init 阶段）
* 为什么 `private/final` 方法不生效？（代理无法覆盖/拦截）
* 同类内部方法调用为什么不走 AOP？（self-invocation 绕过代理）
* `@Transactional` 失效常见原因有哪些？（public、代理方式、异常类型、传播行为）

---

如果你愿意我可以把 **“@AutoFillAspect 反射找 setUpdateTime 的坑”** 也用一张 ASCII 调用链画出来，并给你一个**最稳的修复模板**（Method 缓存 + 命名兜底 + 直接 Field 注入兜底）。
