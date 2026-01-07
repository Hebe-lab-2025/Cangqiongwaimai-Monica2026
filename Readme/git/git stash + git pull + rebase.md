太好了，这一组正好串起来 🚀
我给你：

1. 🧪 git stash + git pull + rebase 组合流程 ASCII 图
2. ⚔️ 冲突标记位置 ASCII 图
3. 🎯 10 道 stash 高频面试题（含答案、要点）

---

# 🧪 git stash + git pull + rebase 组合流程图

### ⭐ 典型真实工作场景

> 功能写一半 → 先存起来 → 更新主分支 → rebase → 继续写

```
你在 feature 分支开发中
          |
          v
+------------------------------------+
| 工作区有未完成代码 (dirty state)   |
+------------------------------------+
          |
          | git stash
          v
+------------------------------------+
| 工作区变干净 (clean working dir)   |
| 修改被放入 stash 栈                 |
+------------------------------------+
          |
          | git pull --rebase origin main
          v
+------------------------------------+
| 先 fetch                           |
| 再把 main 的最新提交放在前面        |
| 再把你的提交 rebase 到后面          |
+------------------------------------+
          |
          | git stash pop
          v
+------------------------------------+
| 取回刚才未完成的代码                |
|（此时可能产生冲突）                 |
+------------------------------------+
          |
          | 手动解决冲突 → git add → 继续
```

👉 你看到关键顺序：

```
stash  →  pull --rebase  →  pop
```

---

# ⚔️ 冲突标记位置 ASCII 图

当 `stash pop / apply / rebase / merge` 冲突时，文件里长这样：

```
<<<<<<< HEAD
这是当前分支上的内容（你的最新代码）
=======
这是被应用进来的内容（来自 stash / 其他分支）
>>>>>>> incoming change
```

📌 解释三条线：

```
<<<<<<<   冲突开始
=======   两边分界线
>>>>>>>   冲突结束
```

👉 你要做的是：

* 选 A
* 或 选 B
* 或 **手动合并两边**
* 然后删掉这些符号

最后：

```
git add file
git rebase --continue   或   git commit
```

---

# 🎯 10 道 Git stash 高频面试题（含答案）

---

### **1. What is git stash used for?**

👉 Answer
Temporarily save **unfinished** changes without committing them.

**Keywords**
temporary, unfinished work, clean working directory

---

### **2. Where is stash stored?**

👉 Answer
In a **stack structure** inside the local repository.

`stash@{0}` = latest

---

### **3. Does stash affect remote repositories?**

👉 Answer
No.
Stash is **local only** and is not pushed.

---

### **4. What happens during `git stash pop`?**

👉 Answer

* applies changes to working directory
* **removes** stash entry

对比：

* `apply` → 不删除
* `pop` → 删除

---

### **5. Can stash cause merge conflicts? When?**

👉 Answer
Yes.

Occurs when:

* file also changed on current branch
* stash tries to re-apply different edits

---

### **6. What does `git stash -u` do?**

👉 Answer
Also stashes **untracked files**.

默认 stash **不会**包含：

* new files
* 未被 git add 的东西

---

### **7. How to stash only one specific file?**

👉 Answer

```
git stash push fileA.txt
```

或

```
git stash push -p
```

交互式选择 patch

---

### **8. Difference between stash and commit?**

👉 Answer

| stash           | commit               |
| --------------- | -------------------- |
| temporary       | permanent            |
| local only      | shared               |
| unfinished work | completed checkpoint |
| stack           | project history      |

---

### **9. What is inside one stash entry technically?**

👉 Answer
A stash is actually:

* one commit for **working tree**
* one commit for **index**

所以可以 reflog 找回 ✔

---

### **10. How to recover deleted stash?**

👉 Answer

```
git reflog
```

找到 hash → 新建分支恢复：

```
git branch restore <hash>
```

⭐ 金句

> reflog is the undo history of Git

---

## 🎁 Bonus：一句超高分总结可直接在面试说

> Git stash is a local, temporary stack used to save unfinished work,
> while keeping the working directory clean for operations like switching branches, pulling, and rebasing.

---

如果你愿意，下一步我可以：

* 🧭 画一张 **stash 内部两个 commit 的结构图**
* 🛠 演示：`stash drop/clear` 后完整恢复流程
* 🎤 练习英文口语版 Git 面试问答
