你这个 `com.apple.eawt` 已经不影响启动了（现在日志里也没再报它）。**当前唯一阻塞点**还是同一个：

> `CategoryController` 注入 `CategoryService`，但 Spring 容器里 **没有 `CategoryService` 这个 Bean**。

这类错误 90% 是 **实现类没被注册**（没 `@Service` / 包不在扫描范围 / 实现类根本不存在）。

下面给你一个**不问问题、直接按步骤做就能定位并修掉**的最短路径。

---

## 1) 先确认：CategoryServiceImpl 到底有没有被写出来

在项目根目录执行（Mac）：

```bash
cd /Users/yonggan/Downloads/sky-take-out-Monica-2026/sky-take-out-Monica
grep -R "interface CategoryService" -n sky-server/src/main/java
grep -R "class CategoryServiceImpl" -n sky-server/src/main/java
```

### 结果解读

* **如果第二条没有输出**：说明你项目里**没有实现类** → 直接看第 2 节（补实现类）
* **如果找到了实现类**：继续看第 3 节（注解/包扫描）

---

## 2) 如果没有 CategoryServiceImpl：补一个最小实现（先让项目起）

新建文件：

`sky-server/src/main/java/com/sky/service/impl/CategoryServiceImpl.java`

```java
package com.sky.service.impl;

import com.sky.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl implements CategoryService {
    // 先空实现，让 Spring 能注入成功、项目能启动
    // 之后再按业务把方法补全
}
```

> 你此时可能会报“必须实现接口方法”，那就让 IDEA 自动生成所有方法，先返回 null/0/空集合即可。

然后重启即可验证是否过了这一关。

---

## 3) 如果有 CategoryServiceImpl：按这 3 点改到“必能注册”

打开你的实现类，确保满足：

### ✅ 3.1 包名必须在 `com.sky...` 下

比如：

```java
package com.sky.service.impl;
```

如果是 `com.itheima...` / `cn.xxx...`，Spring Boot 默认扫不到。

### ✅ 3.2 类上必须有 `@Service`

```java
@Service
public class CategoryServiceImpl implements CategoryService { ... }
```

### ✅ 3.3 启动类必须在 “根包” `com.sky`

你现在 `SkyApplication` 在 `com.sky` ✅，所以只要你的实现类也在 `com.sky.*`，就能被扫到。

---

## 4) 你可能遇到的“隐藏版本”：ServiceImpl 创建失败导致看起来像没 Bean

如果你 `CategoryServiceImpl` 里注入了 mapper：

```java
@Autowired
private CategoryMapper categoryMapper;
```

但 mapper 没扫描到，Spring 会 **创建 ServiceImpl 失败**，最后 controller 才报 “CategoryService 不存在”。

### ✅ 解决：加 Mapper 扫描（强烈建议直接加，避免后续一堆 mapper 问题）

在 `SkyApplication` 上加：

```java
import org.mybatis.spring.annotation.MapperScan;

@MapperScan("com.sky.mapper")
@SpringBootApplication
public class SkyApplication { ... }
```

并确保 mapper 在 `com.sky.mapper` 包下。

---

## 5) 最终推荐的 SkyApplication（稳定版）

```java
package com.sky;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.sky.mapper")
@SpringBootApplication
public class SkyApplication {
    public static void main(String[] args) {
        SpringApplication.run(SkyApplication.class, args);
    }
}
```

---

### 你现在立刻做这两件事，基本就能过：

1. `grep` 看有没有 `CategoryServiceImpl`
2. 在启动类加 `@MapperScan("com.sky.mapper")`

如果你把这两段贴出来（不用整文件）我可以直接告诉你是哪一种：

* `CategoryService` 接口头 5 行（package + interface）
* `CategoryServiceImpl` 头 10 行（package + 注解 + class 声明）
