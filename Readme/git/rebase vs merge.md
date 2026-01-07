Great—let’s do this in **five parts** as you asked:

---

## 🧪 10 Git interview questions (with concise answers)

### 1. What is Git? How is it different from SVN?

**Answer:**
Git is a **distributed version control system**. Every developer has a full copy of the repository.
SVN is **centralized** and relies on a single central server.

**Keywords:** distributed, local commits, offline work, branching is cheap

---

### 2. What is the difference between `git pull` and `git fetch`?

**Answer:**
`git fetch` downloads remote changes but **does not merge**.
`git pull` = `git fetch` + **merge/rebase into current branch**.

---

### 3. What is a detached HEAD?

**Answer:**
HEAD points directly to a **commit**, not a branch.
You are “not on any branch”; new commits are not referenced by a branch pointer.

---

### 4. When should you use branching?

**Answer:**
Use branches to **isolate work**: features, hotfixes, experiments.
Keeps `main/master` always releasable.

---

### 5. Explain `git stash`

**Answer:**
Temporarily saves uncommitted changes so you can switch branches safely.
Restore using `git stash pop/apply`.

---

### 6. What is `git cherry-pick`?

**Answer:**
Applies **a specific commit** from one branch onto another branch.

---

### 7. Difference between `merge` and `rebase`?

**Answer:**

* merge: combine histories, **preserves history**
* rebase: re-applies commits on top of another base, **rewrites history**

---

### 8. What is a fast-forward merge?

**Answer:**
When target branch has no new commits, Git just **moves branch pointer ahead** (no merge commit).

---

### 9. What causes merge conflicts?

**Answer:**
Two commits change **the same line or nearby lines** of the same file.

---

### 10. What is `HEAD~1` vs `HEAD^`?

**Answer:**
Both usually mean **parent commit of HEAD**
`HEAD~n` = n steps back linearly
`HEAD^` = parent in merges (first parent)

---

## 🧭 ASCII diagram: rebase vs merge

### 🔹 Merge (keeps history, extra merge commit)

```
A --- B --- C   (main)
         \
          D --- E   (feature)

git merge feature

A --- B --- C --------- M
         \             /
          D --- E -----
```

History shows **true branch structure**

---

### 🔹 Rebase (rewrites feature history)

```
A --- B --- C   (main)
         \
          D --- E   (feature)

git rebase main

A --- B --- C --- D' --- E'   (feature rebased)
```

No merge commit
Looks like **linear history**

---

## 💻 Common rebase conflict resolution steps

1. Run rebase

```
git rebase main
```

2. Conflict happens ❌

3. Check conflict files

```
git status
```

4. Edit files and resolve markers

```
<<<<<<< HEAD
your code
=======
their code
>>>>>>> branch
```

5. Mark file as resolved

```
git add <file>
```

6. Continue rebase

```
git rebase --continue
```

7. If everything is broken 😅

```
git rebase --abort
```

**Mindset:** small commits, resolve carefully, test after each step

---

## 🧨 When you should absolutely NOT rebase

❌ After pushing to a shared remote branch
❌ When teammates already pulled that branch
❌ On protected release/main branches
❌ When you don’t fully understand what rewrite does

👉 Because:
Rebase **rewrites history**, breaking others’ references
They must fix using `reset`, which is risky

**Rule of thumb**

> Rebase local work you own
> Never rebase other people’s published history

---

## 🎯 Real team collaboration scenario (story style)

### Situation

Team has:

* `main` (stable, release ready)
* `feature/payment`
* multiple engineers pushing daily

### Good workflow

1. Developer creates branch

```
git checkout -b feature/payment
```

2. Work for several days

3. Main branch moves forward 🚀

4. Before opening PR, developer does:

```
git fetch origin
git rebase origin/main
```

5. Resolve conflicts locally
6. Run tests
7. Push **with force only to personal feature branch**

```
git push -f
```

8. Open Pull Request
9. Reviewer sees clean linear commits
10. Merge using **fast-forward or squash**

### Why this is good

✔ clean history
✔ small PRs
✔ fewer merge bubbles
✔ easier `git blame`

### When they would use merge instead

* long-running release branches
* avoiding history rewrite
* preserving audit trail

---

## ✅ Quick recap bullets

* merge = preserve history
* rebase = rewrite history
* never rebase shared public history
* conflicts happen when same lines change
* stash is your “temporary shelf”
* cherry-pick moves one commit
* detached HEAD = not on branch

---

If you want, I can also help you with:

🔧 simulate **real Git conflict example file**
🎯 mock interview dialog (interviewer vs you)
📚 20 more Git questions
📝 cheat sheet printable page
🧭 interactive practice scenario
