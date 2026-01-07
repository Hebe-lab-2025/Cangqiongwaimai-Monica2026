很好 👍 我就专门讲——**如何把多个 commit 合并成一个（squash）**，一步步操作，看着做就行。

---

# ✅ 目标

把这样：

```
A - B - C - D - E
```

合并为：

```
A - B - (CDE 合成 1 个)
```

或者把最近 N 个变成 1 个。

---

# 🟢 常见使用场景

* 写代码时反复提交：

  ```
  fix bug
  really fix bug
  final fix
  update comment
  ```
* 提交要发 PR / code review
  👉 希望只留下 **一个干净的 commit**

---

# 🚀 合并多个 commit 的标准步骤（最常用方法）

### 第 1 步：看最近提交

```
git log --oneline
```

假设你想把最近 4 个合成 1 个。

---

### 第 2 步：进入交互 rebase

```
git rebase -i HEAD~4
```

含义：

* 作用范围 = 最近 4 个 commit
* 会弹出一个编辑界面

---

### 第 3 步：把要合并的 commit 改成 squash

你会看到类似：

```
pick c1 commit message 1
pick c2 commit message 2
pick c3 commit message 3
pick c4 commit message 4
```

👉 只保留第一个 `pick`
👉 其余改成 `s` 或 `squash`

```
pick c1 commit message 1
squash c2 commit message 2
squash c3 commit message 3
squash c4 commit message 4
```

> 规则：
> ✔ 第一条保留为 pick
> ✔ 后面合并到它上面

---

### 第 4 步：保存退出

* vim：`Esc` → `:wq`
* VSCode：直接保存

Git 会打开一个新窗口让你编辑合并后的 commit message：

👉 你随便写一个**干净总结**

```
feat: implement user payment service
```

保存退出

---

### ✔ 结果

* 原来 4 个 commit → 变成 1 个
* 提交 ID 全部改变（这是正常的）

---

# 🛑 非常重要的提醒

👉 你在**改历史**

**如果这些 commit 已经 push 到远程且别人也用这个分支：**

⚠ 慎重
⚠ 慎重
⚠ 慎重

否则他们会冲突爆炸

如果一定要推送：

```
git push -f
```

（通常需要团队确认）

---

# 🧭 另一种简单方法：把“最近 N 个”直接压成 1 个提交

只要一个命令：

```
git reset --soft HEAD~4
git commit -m "new single commit"
```

含义：

* 把最近 4 个 commit 回退到暂存区
* 再打一发新的 commit

👉 效果 = 4 个 → 1 个
👉 更快，但不适用于复杂情况

---

# 🧠 记忆口诀

```
rebase -i HEAD~N
第一条 pick
后面全 squash
保存 → 写新 message
```

---

# 🎯 你可以告诉我：

* 你要合并 **几个 commit**
* 是 **最近的** 还是 **中间的**
* 是否 **已经 push 远程**

👉 我可以按你的实际日志写出完整命令 + 你应选择的具体行
