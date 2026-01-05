```
yonggan@Yonggans-MacBook-Pro sky-take-out-Monica % git status
On branch zhamonic-sky-takeout
Your branch is up to date with 'origin/zhamonic-sky-takeout'.

Changes to be committed:
  (use "git restore --staged <file>..." to unstage)
        new file:   sky-common/src/main/java/com/sky/constant/AutoFillConstant.java
        new file:   sky-common/src/main/java/com/sky/constant/JwtClaimsConstant.java
        new file:   sky-common/src/main/java/com/sky/constant/MessageConstant.java
        new file:   sky-common/src/main/java/com/sky/constant/PasswordConstant.java
        new file:   sky-common/src/main/java/com/sky/constant/StatusConstant.java
        new file:   sky-common/src/main/java/com/sky/context/BaseContext.java
        new file:   sky-common/src/main/java/com/sky/enumeration/OperationType.java
        new file:   sky-common/src/main/java/com/sky/exception/AccountLockedException.java
        new file:   sky-common/src/main/java/com/sky/exception/AccountNotFoundException.java
        new file:   sky-common/src/main/java/com/sky/exception/AddressBookBusinessException.java
        new file:   sky-common/src/main/java/com/sky/exception/BaseException.java
        new file:   sky-common/src/main/java/com/sky/json/JacksonObjectMapper.java
        new file:   sky-common/src/main/java/com/sky/properties/AliOssProperties.java
        new file:   sky-common/src/main/java/com/sky/properties/JwtProperties.java
        new file:   sky-common/src/main/java/com/sky/properties/WeChatProperties.java
        new file:   sky-common/src/main/java/com/sky/result/PageResult.java
        new file:   sky-common/src/main/java/com/sky/result/Result.java
        new file:   sky-common/src/main/java/com/sky/utils/AliOssUtil.java
        new file:   sky-common/src/main/java/com/sky/utils/HttpClentUtil.java
        new file:   sky-common/src/main/java/com/sky/utils/JwtUtil.java
        new file:   sky-common/src/main/java/com/sky/utils/WeChatPayUtil.java

Changes not staged for commit:
  (use "git add <file>..." to update what will be committed)
  (use "git restore <file>..." to discard changes in working directory)
        modified:   pom.xml
        modified:   sky-common/pom.xml
        modified:   sky-common/src/main/java/com/sky/constant/AutoFillConstant.java
        modified:   sky-common/src/main/java/com/sky/constant/JwtClaimsConstant.java
        modified:   sky-common/src/main/java/com/sky/constant/MessageConstant.java
        modified:   sky-common/src/main/java/com/sky/constant/PasswordConstant.java
        modified:   sky-common/src/main/java/com/sky/constant/StatusConstant.java
        modified:   sky-common/src/main/java/com/sky/context/BaseContext.java
        modified:   sky-common/src/main/java/com/sky/enumeration/OperationType.java
        modified:   sky-common/src/main/java/com/sky/exception/AccountLockedException.java
        modified:   sky-common/src/main/java/com/sky/exception/AccountNotFoundException.java
        modified:   sky-common/src/main/java/com/sky/exception/AddressBookBusinessException.java
        modified:   sky-common/src/main/java/com/sky/exception/BaseException.java
        modified:   sky-common/src/main/java/com/sky/json/JacksonObjectMapper.java

Untracked files:
  (use "git add <file>..." to include in what will be committed)
        Database/
        MDPPT/
        project-sky-admin-vue-ts/

yonggan@Yonggans-MacBook-Pro sky-take-out-Monica % 

```

```
Great — now everything makes sense 👍
You’re **inside the real Git repo** and `git status` is working.

Here is what it means in simple English:

---

## ✅ What your `git status` shows

### 1) You are on branch

```
zhamonic-sky-takeout
```

### 2) Repo is connected to remote and up to date

```
Your branch is up to date with 'origin/zhamonic-sky-takeout'
```

✔ remote OK
✔ branch OK

### 3) You already staged many files (green section)

> **Changes to be committed**

These are **already added** and will be committed next.

### 4) You modified files but did NOT stage them yet (red section)

> **Changes not staged for commit**

These files were edited after staging, or never staged.

### 5) You have untracked files

> **Untracked files**

Git does not know these yet (new folders you added).

---

## 🧭 What do you want to do now?

Choose one goal 👇

### 🔹 A) Commit EVERYTHING (simple, common)

```bash
git add .
git commit -m "your message"
git push
```

---

### 🔹 B) Commit ONLY staged items (green part already OK)

Just run:

```bash
git commit -m "commit staged changes"
git push
```

---

### 🔹 C) Undo staged files (move back to unstaged)

```bash
git restore --staged .
```

---

### 🔹 D) See differences in files

```bash
git diff
```

---

## ⚠️ Important small note

You have some files appearing **twice**:

* as **new file**
* and **modified**

This means you probably:

* added them
* then edited again

Totally fine — just commit normally.

---

### 👉 Tell me what you want:

* commit everything
* only some files
* discard changes
* reset to remote
* create new branch
* fix merge
* clean untracked files

and I’ll give exact commands 😊

```

# 我可以先git pull嘛
可以，但**要看情况**。你现在有**很多本地改动**，直接 `git pull` 可能会触发冲突。

### ✅ 结论（简短版）

* ✔️ **可以 pull**
* ⚠️ 但你**现在有未提交修改** → 可能会产生 merge conflict
* ⭐ 更安全做法：先 commit 或 stash

---

## 🧭 推荐安全操作方式

### 🟢 方式 1：先保存，再 pull（推荐）

```bash
git add .
git commit -m "save local changes"
git pull
```

---

### 🔵 方式 2：不想提交，只想临时放一边（stash）

```bash
git stash
git pull
git stash pop
```

👉 适合：工作到一半不想提交

---

### 🔴 **不建议** 直接：

```bash
git pull
```

因为你现在状态是：

* 有 staged changes
* 有 unstaged changes
* 有 untracked files

→ **极容易冲突，而且会很乱**

---

## 📌 如果你坚持直接 pull

Git 可能提示：

```
Please commit your changes or stash them before you merge
```

那就必须回去做上面的步骤。

---

### 👍 建议你告诉我：

* 你是想 **保留本地改动**
* 还是 **丢掉本地改动，用远程覆盖**

我可以给你**一条指令解决**（不废话版本）

# 我：方式1和2，我保留或临时放一边，经常找不到
```
Totally understandable — this是很多人最常混的两件事 👍
我用**超直观对比 + 最常用命令**帮你记住：

---

## 🧭 一句话区分

* **方式 1：commit** = 把工作**正式存进版本历史** ✔️
* **方式 2：stash** = 把工作**塞进临时抽屉** 🗂️（别人看不到）

---

## ✅ 方式 1：Commit（正式保存，永远不会丢）

👉 用在：
✔ 改动已经成型
✔ 不介意留下 commit 记录

**命令：**

```bash
git add .
git commit -m "save local work"
git pull
```

📌 特点：

* 有 commit 记录
* 永久的
* 随时可以回滚
* 最安全、最不容易迷路 ⭐

👉 **记忆法：像“存档 save”**

---

## 🟦 方式 2：Stash（临时塞起来，容易忘）

👉 用在：
✔ 工作到一半
✔ 不想 commit
✔ 只是想拉代码看看

**命令：**

```bash
git stash           # 暂存
git pull            # 拉代码
git stash pop       # 取回改动
```

📌 特点：

* 没有 commit
* 容易忘记，但不会丢
* 适合“临时挪开一下”

👉 **记忆法：像“把书塞抽屉”**

---

## ⭐ 最简单建议（不容易迷路）

👉 **优先用 commit**
因为：

* 不会丢
* 不会忘
* 比 stash 清晰很多

---

## 🧪 想让我帮你操作吗？

告诉我：

* 现在要不要**正式保存（commit）**
* 还是**临时塞抽屉（stash）**

我可以直接给你：

👉 **一行式安全命令**
👉 并解释屏幕每一步出现什么提示

你只需要说一句：

* “我选 commit”
* 或
* “我选 stash”
```


# stash怎么找到呢？
```
Great question —很多人 **stash 完就找不到了**，你这个问得非常好 👍

下面是最常用、最安全的用法：

---

## ✅ 查看我到底 stash 了什么

```bash
git stash list
```

你会看到类似：

```
stash@{0}: WIP on zhamonic-sky-takeout: xxx
stash@{1}: WIP ...
```

👉 这里每一条就是一个“抽屉”
👉 `stash@{0}` 是**最新的那一份**

---

## 👀 看具体改动内容

```bash
git stash show -p stash@{0}
```

* `-p` = patch
* 能看到 diff（非常清楚）

---

## ♻️ 取回最近的 stash（最常用）

```bash
git stash pop
```

👉 自动恢复 + 删除 stash
👉 就像把抽屉内容拿出来用

---

## 🛟 不想删除，只恢复看看

```bash
git stash apply stash@{0}
```

区别：

* `pop` = 拿出来并扔掉抽屉标签
* `apply` = 拿出来但抽屉还在

---

## 🧹 删除某个 stash

```bash
git stash drop stash@{0}
```

---

## 💣 清空全部 stash（慎用）

```bash
git stash clear
```

---

### ⭐ 推荐你记住 3 条就够：

```
git stash list      # 看有哪些
git stash pop       # 取回最新
git stash show -p   # 看里面是什么
```

---

如果你愿意，可以把你现在的输出贴出来：

```
git stash list
```

我可以帮你读，并告诉你：

* 哪一个是最新的
* 哪个分支 stash 的
* 恢复哪个好
```


