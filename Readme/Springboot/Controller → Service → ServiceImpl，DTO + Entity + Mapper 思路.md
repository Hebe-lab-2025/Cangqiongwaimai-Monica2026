好，这里我**完全按你现在的风格**（Controller → Service → ServiceImpl，DTO + Entity + Mapper 思路）
**帮你补一个「菜品查询」**，是你这个项目里**最标准、最常考、最不容易出坑的版本**。

---

# 一、Controller：菜品查询（GET）

👉 设计目标：

* **按分类查询菜品**
* 返回 **DishDTO（包含口味）**
* REST 风格：**GET /admin/dish/list**

```java
/**
 * 根据分类id查询菜品
 */
@GetMapping("/list")
@ApiOperation("根据分类id查询菜品")
public Result<List<DishDTO>> list(@RequestParam Long categoryId) {
    log.info("根据分类id查询菜品：{}", categoryId);
    List<DishDTO> list = dishService.listByCategoryId(categoryId);
    return Result.success(list);
}
```

📌 说明（面试点）：

* **查询 → GET**
* **简单条件 → @RequestParam**
* 返回 DTO，不直接返回 Entity（解耦）

---

# 二、Service 接口：补查询方法

```java
public interface DishService {

    void saveWithFlavor(DishDTO dishDTO);

    /**
     * 根据分类id查询菜品（包含口味）
     */
    List<DishDTO> listByCategoryId(Long categoryId);
}
```

---

# 三、ServiceImpl：核心查询逻辑（重点）

```java
/**
 * 根据分类id查询菜品及对应口味
 */
@Override
public List<DishDTO> listByCategoryId(Long categoryId) {

    // 1️⃣ 查询菜品表
    List<Dish> dishList = dishMapper.listByCategoryId(categoryId);

    // 2️⃣ 组装 DTO（菜品 + 口味）
    List<DishDTO> result = dishList.stream().map(dish -> {
        DishDTO dishDTO = new DishDTO();
        BeanUtils.copyProperties(dish, dishDTO);

        // 3️⃣ 根据菜品id查询口味
        List<DishFlavor> flavors = dishFlavorMapper.listByDishId(dish.getId());
        dishDTO.setFlavors(flavors);

        return dishDTO;
    }).toList();

    return result;
}
```

---

# 四、你需要有的 Mapper 方法（逻辑完整）

### DishMapper

```java
List<Dish> listByCategoryId(Long categoryId);
```

### DishFlavorMapper

```java
List<DishFlavor> listByDishId(Long dishId);
```

---

# 五、完整调用链（你要能背）

```
GET /admin/dish/list?categoryId=1
        ↓
DishController.list()
        ↓
DishService.listByCategoryId()
        ↓
DishMapper 查询 dish 表
        ↓
DishFlavorMapper 查询 flavor 表
        ↓
组装 DishDTO
        ↓
返回 Result.success()
```

---

# 六、为什么这样写是“正确答案”（面试加分）

### ✅ 为什么不用 JOIN？

> 因为一对多 JOIN 会导致 **数据膨胀**，DTO 组装更清晰、可控。

### ✅ 为什么返回 DTO？

> Entity 是数据库模型，DTO 是接口模型，防止字段泄漏。

### ✅ 为什么不是 @RequestBody？

> 查询是 GET，参数简单，用 Query 参数更符合 REST。

---

# 七、你现在可以直接对照自查的 3 个点

* [ ] Controller 是 `@GetMapping` + `@RequestParam`
* [ ] Service 返回 `List<DishDTO>`
* [ ] ServiceImpl 里 **先查 Dish，再查 Flavor**

---

如果你愿意，我可以下一步帮你继续补 **这套菜品模块的高频功能**（真实项目都会有）：

* 🔥 **分页查询菜品（PageHelper / MyBatis-Plus）**
* 🔥 **启用 / 停售菜品（PUT + 幂等）**
* 🔥 **根据菜品 id 查询详情（回显用）**
* 🧠 **为什么不能在 Mapper 里直接 JOIN 返回 DTO（面试）**

你直接说 **下一步写哪个**。
