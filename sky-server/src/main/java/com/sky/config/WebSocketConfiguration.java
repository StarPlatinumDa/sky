package com.sky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket配置类，用于注册WebSocket的Bean
 * 注册和暴露 WebSocket 端点（Endpoint），让客户端可以连接到 WebSocket 服务
 * Endpoint（端点） 是 WebSocket 通信的服务端入口，类似于 HTTP 的 @RestController
 * 客户端可以通过 ws://your-domain/chat 连接到这个 Endpoint
 */
@Configuration
public class WebSocketConfiguration {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

}
