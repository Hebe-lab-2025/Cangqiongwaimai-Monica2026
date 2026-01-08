**`concat`** = **concatenate** → *join things together*, most commonly **strings**.

---

## What it means

* To **concat strings** = put one string **after** another to form a new string.

---

## Examples

### ✅ Java

```java
String a = "Hello";
String b = "World";
String c = a.concat(" ").concat(b);
// "Hello World"
```

More common (and clearer):

```java
String c = a + " " + b;
```

---

### ✅ JavaScript

```js
"Hello".concat(" ", "World"); // "Hello World"
```

or

```js
"Hello" + " " + "World";
```

---

### ✅ Python

```python
"Hello" + " " + "World"
```

---

## Key points (interview-style)

* **concat = join**
* Usually used for **strings**
* Creates a **new string** (original strings don’t change)
* In Java, **`StringBuilder`** is better for many concatenations (performance)

---

If you want:

* ⚡ `String` vs `StringBuilder` concat performance
* 🧪 Interview trick questions about `
