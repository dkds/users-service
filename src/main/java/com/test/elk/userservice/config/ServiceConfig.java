package com.test.elk.userservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@RefreshScope
@Configuration
@ConfigurationProperties("service")
@Data
public class ServiceConfig {

    private Service mock;
    private Service kafka;
    private Service todos;

    @Data
    public static class Service {
        private String baseUrl;
    }
}
