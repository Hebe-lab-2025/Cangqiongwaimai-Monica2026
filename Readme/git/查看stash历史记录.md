要查看 `stash` 历史记录，用这一条命令最常见：

```
git stash list
```

👉 会显示类似：

```
stash@{0}: WIP on main: 1234abc add login page
stash@{1}: WIP on dev: 89ff120 fix bug
stash@{2}: On feature-x: test changes
```

---

### 🔍 想看 **具体改了哪些内容**

#### 1️⃣ 查看单个 stash 的摘要（文件层级差异）

```
git stash show stash@{0}
```

#### 2️⃣ 查看完整 diff（代码级改动）

```
git stash show -p stash@{0}
```

---

### 📌 相关小技巧（非常实用）

| 目的           | 命令                            |
| ------------ | ----------------------------- |
| 查看 stash 栈   | `git stash list`              |
| 查看最新 stash   | `git stash show`              |
| 查看某一条详细 diff | `git stash show -p stash@{n}` |
| 恢复指定 stash   | `git stash apply stash@{n}`   |
| 删除指定 stash   | `git stash drop stash@{n}`    |
| 清空所有 stash   | `git stash clear`             |

---

如果你愿意，我还能帮你：

* 🧭 画「stash list 工作原理」ASCII 图
* 🧪 出几道 Git 面试题：stash vs branch
* ⚔️ 教你：**误删 stash 怎么找回**


