package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.wechat")
@Data
/**
 * 微信配置属性类
 * 用于存储和管理微信相关的配置信息
 */
public class WeChatProperties {
    private String appid; // 小程序的appid
    private String secret; // 小程序的secret
    private String mchid; // 商户号
    private String mchSeriaNo; // 商户证书序列号
    private String privateKeyFilePath; // 商户私钥文件路径
    private String apiV3Key; // 证书解密APIv3密钥
    private String weChatPayCertFilePath; // 微信支付平台证书文件路径
    private String notifyUrl; // 支付结果通知地址
    private String refundNotifyUrl; // 退款结果通知地址
}
