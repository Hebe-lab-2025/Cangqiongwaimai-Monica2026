你这个编译失败其实是 **两类问题**：

1. `WeChatProperties` **没有对应的 getter 方法**（所以 `WeChatPayUtil` 里一堆 `getXxx()` 都找不到）
2. `AliOssUtil` 里用了 `log`，但这个类 **没有生成/声明 logger**（通常缺少 `@Slf4j` 或自己没写 `Logger`）

---

## 1) WeChatPayUtil：getter 全部找不到（根因）

报错像这样：

* `cannot find symbol method getAppid()`
* `getMchid() / getMchSeriaNo() / getNotifyUrl() / getRefundNotifyUrl()`
* `getPrivateKeyFilePath() / getWeChatPayCertFilePath()`

说明：`com.sky.properties.WeChatProperties` 这个类里 **根本没有这些方法**。

最常见原因：

* 你项目里 `WeChatProperties` **没加 Lombok 的 `@Data` / `@Getter`**
* 或者字段名和方法名 **对不上**（比如字段叫 `mchId`，代码却写 `getMchid()`）
* 或者你这份代码里 `WeChatProperties` 被改过/不完整

### ✅ 最快修法（推荐）

打开：`sky-common/src/main/java/com/sky/properties/WeChatProperties.java`

把它改成（或确保它长这样）：

```java
package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "sky.wechat")
public class WeChatProperties {

    private String appid;
    private String mchid;                 // 商户号
    private String mchSeriaNo;            // 商户证书序列号（注意拼写要和 util 里一致）
    private String privateKeyFilePath;    // 私钥文件路径
    private String weChatPayCertFilePath; // 平台证书路径
    private String notifyUrl;             // 支付回调
    private String refundNotifyUrl;       // 退款回调
}
```

> 关键点：字段名要和 `WeChatPayUtil` 里调用的 getter **严格对应**
> 例如字段 `mchid` 才会生成 `getMchid()`；字段 `mchId` 只会生成 `getMchId()`（会不匹配）

### ✅ 如果你不想用 Lombok

那就手写这些 getter/setter（但麻烦很多，不建议）。

---

## 2) AliOssUtil：`log` 变量找不到（根因）

报错：

* `AliOssUtil.java:[61,9] cannot find symbol variable log`

说明你代码里写了类似：

```java
log.info("xxx");
```

但类上没有 `@Slf4j`，也没自己声明 logger。

### ✅ 修法 A（最常见）

在 `AliOssUtil` 类上加 Lombok：

```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AliOssUtil {
    ...
}
```

### ✅ 修法 B（不用 Lombok）

自己写 logger：

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AliOssUtil {
    private static final Logger log = LoggerFactory.getLogger(AliOssUtil.class);
}
```

---

## 3) 修完怎么验证

在项目根目录重新跑：

```bash
mvn -q -DskipTests clean package
```

如果还在 `WeChatProperties` 报 getter：

* 立刻对照 **WeChatPayUtil 里调用的方法名** 和 **WeChatProperties 的字段名**
* 方法名大小写/拼写必须一致，比如 `getMchSeriaNo()` 对应字段必须叫 `mchSeriaNo`

---

## 4) 一句定位技巧（不浪费时间）

在 `WeChatPayUtil` 报错那行，把鼠标放在 `weChatProperties.getXxx()` 上：

* IntelliJ 如果提示 “Cannot resolve method”
  => 直接回到 `WeChatProperties`，改字段名/加 `@Data`

---

如果你把 **WeChatProperties.java** 和 **AliOssUtil.java** 这两个文件贴出来（哪怕只贴类声明+字段/用到 log 的那几行），我可以按你当前代码 **精确改到一次过编译**。
