太好了，这三个刚好连在一起 👍
我给你 **一步到位搞懂 stash list + 面试题 + 误删恢复**。

---

## 🧭 ASCII 图：stash list 工作原理（栈 stack）

### 🔹 1）stash 就是一叠“栈”

```
Top (最新)
   |
   v
+-----------------------+
| stash@{0}  ← 最新的   |
+-----------------------+
| stash@{1}             |
+-----------------------+
| stash@{2}             |
+-----------------------+
        ...
Bottom (最早)
```

👉 特点

* **后进先出 LIFO**
* `stash@{0}` 永远是最新的

---

### 🔹 2）git stash push 时发生了什么

```
工作区修改
     |
     | git stash push
     v
+-----------------------+
| 新建 stash@{0}        |
+-----------------------+
| 原来的 0 变成 1       |
+-----------------------+
| 原来的 1 变成 2       |
+-----------------------+
```

---

### 🔹 3）git stash list 展示的是这个栈

```
git stash list
      |
      v
+---------------------------------------------+
| stash@{0}: WIP on main: 3ab1cde message     |
| stash@{1}: WIP on dev: 7c21ff2 work in prog |
| stash@{2}: On feature-x: test changes       |
+---------------------------------------------+
```

---

### 🔹 4）git stash pop 做的事情

```
stack                       working directory
-------                     -----------------
stash@{0}   ---- apply --->  恢复改动
   |
   |---- drop ----> 从栈中删除
```

👉 口诀

* `apply` = 只应用
* `pop` = 应用 + 删除

---

## 🧪 Git 面试题：stash vs branch（含答案）

### ❓1. When should you use stash instead of a branch?

👉 Answer (简洁高分)

* stash：**短暂停、未完成工作、临时切走**
* branch：**长期开发、需要提交历史**

---

### ❓2. Is stash a branch?

👉 Answer

* 不是
* stash 是 **栈**
* branch 是 **提交指针（HEAD 移动）**

---

### ❓3. What happens if you stash multiple times?

👉 Answer

* 进入栈顶，编号依次下移
* `stash@{0}` 始终是最新

---

### ❓4. Can stash conflict? When?

👉 Answer

* 会冲突
* 在 **apply / pop** 时
* 原因：目标文件已被其他提交修改

---

### ❓5. Which one is shared with teammates: stash or branch?

👉 Answer

* branch ✔ 可以 push / review / merge
* stash ✖ 仅本地，不共享

---

## ⚔️ 误删 stash 怎么找回（超常用）

### ❌ 场景

```
git stash drop stash@{0}
```

或

```
git stash clear
```

你：后悔了 😭（放心可恢复）

---

### ✅ 方法 1：用 reflog 找 commit id（最常见）

```
git reflog
```

找类似：

```
stash@{0}: WIP on main
```

记住后面的 **commit 哈希**，如：

```
abc1234
```

然后：

```
git checkout abc1234
```

或恢复成分支：

```
git branch restore-from-stash abc1234
```

👉 你就把丢失 stash 变成了一个新分支

---

### ✅ 方法 2：直接看所有 dangling commits

```
git fsck --lost-found
```

输出类型：

* commit
* blob
* tree

👉 逐个 diff 查你要的

---

### ⚠️ 注意事项（重要）

* stash 其实是 **commit + index commit**
* 只要还没被 GC（垃圾回收）→ 就能找回
* **越早恢复成功率越高**

---

## 🎯 送你一条黄金口诀

> 🔹 stash = 本地临时背包
> 🔹 branch = 长期开发线路
> 🔹 reflog = 后悔药

---

如果你愿意，下一步我可以：

* 🧭 画 **stash 内部是两个 commit 的结构图**
* 🎯 出一套 **Git rebase + stash 组合题**
* 🧪 模拟面试官问你，并帮你优化英文答案
