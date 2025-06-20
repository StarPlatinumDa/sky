package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类，用于创建AliOSSUtil对象
 */

@Configuration
@Slf4j
public class OssConfiguration {
    @Bean
    @ConditionalOnMissingBean// 如果已经创建就不再创建了
    // 这里不需要写Autowired就能注入对象 因为是@Bean spring会自动注入@Bean方法的参数 依赖注入
    // 好像又是因为同时是@Configuration和@Bean
    public AliOssUtil util(AliOssProperties properties){
        log.info("开始创建阿里云文件上传工具类对象:{}",properties);
        return new AliOssUtil(properties.getEndpoint(),
                properties.getAccessKeyId(),properties.getAccessKeySecret(),
                properties.getBucketName());
    }
}
