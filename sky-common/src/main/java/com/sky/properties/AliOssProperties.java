package com.sky.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
@Data
/**
 * 阿里云OSS配置属性类
 * 用于存储阿里云对象存储服务的相关配置信息
 */
public class AliOssProperties {
    /**
     * 阿里云账号的AccessKeyId
     * 用于标识阿里云账号的唯一标识符
     */
    private String accessKeyId;
    /**
     * 阿里云密码
     */
    private String accessKeySecret;
    /**
     * 阿里云oss的bucket名称
     */
    private String bucketName;
//    /**
//     * 阿里云oss的endpoint
//     */
    private String endpoint;
    /**
     * 阿里云oss的域名
     */
//    private String domain;
//    /**
//     * 阿里云oss的文件夹名称
//     */
//    private String folder;
//
//    public String getAccess
}
