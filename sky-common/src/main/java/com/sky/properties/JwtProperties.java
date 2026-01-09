package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


    @Component
    @ConfigurationProperties(prefix = "sky.jwt")
    @Data
/**
 * JWT属性配置类
 * 该类用于存储和管理JWT(JSON Web Token)相关的配置属性
 */
    public class JwtProperties {
        /**
         * 管理端员工生成jwt令牌相关配置
         */
        private String adminSecretKey;
        private long adminTtl;
        private String adminTokenName;

        /**
         * 用户端微信用户生成jwt令牌相关配置
         */
        private String userSecretKey;
        private long userTtl;
        private String userTokenName;

        // TODO: 在此处添加JWT相关的配置属性
        // 例如：密钥、过期时间、签发者等信息
    }
