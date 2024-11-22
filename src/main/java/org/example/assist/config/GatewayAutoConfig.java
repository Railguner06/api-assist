package org.example.assist.config;

import io.netty.channel.Channel;
import org.example.assist.application.GatewayApplication;
import org.example.assist.domain.service.GatewayCenterService;
import org.example.core.session.defaults.DefaultGatewaySessionFactory;
import org.example.core.socket.GatewaySocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 网关服务配置
 */
@Configuration
@EnableConfigurationProperties(GatewayServiceProperties.class)
public class GatewayAutoConfig {

    private Logger logger = LoggerFactory.getLogger(GatewayAutoConfig.class);

    @Bean
    public GatewayCenterService registerGatewayService() {
        return new GatewayCenterService();
    }

    @Bean
    public GatewayApplication gatewayApplication(GatewayServiceProperties properties, GatewayCenterService registerGatewayService, org.example.core.session.Configuration configuration) {
        return new GatewayApplication(properties, registerGatewayService, configuration);
    }

    /**
     * 创建网关配置对象；Configuration 是用于贯穿整个网关核心通信服务的。
     */
    @Bean
    public org.example.core.session.Configuration gatewayCoreConfiguration(GatewayServiceProperties properties) {
        org.example.core.session.Configuration configuration = new org.example.core.session.Configuration();
        String[] split = properties.getGatewayAddress().split(":");
        configuration.setHostName(split[0].trim());
        configuration.setPort(Integer.parseInt(split[1].trim()));
        return configuration;
    }

    /**
     * 初始化网关服务；创建服务端 Channel 对象，方便获取和控制网关操作。
     */
    @Bean
    public Channel initGateway(org.example.core.session.Configuration configuration) throws ExecutionException, InterruptedException {
        // 1. 基于配置构建会话工厂
        DefaultGatewaySessionFactory gatewaySessionFactory = new DefaultGatewaySessionFactory(configuration);
        // 2. 创建启动网关网络服务
        GatewaySocketServer server = new GatewaySocketServer(configuration, gatewaySessionFactory);
        Future<Channel> future = Executors.newFixedThreadPool(2).submit(server);
        Channel channel = future.get();
        if (null == channel) throw new RuntimeException("api gateway core netty server start error channel is null");
        while (!channel.isActive()) {
            logger.info("api gateway core netty server gateway start Ing ...");
            Thread.sleep(500);
        }
        logger.info("api gateway core netty server gateway start Done! {}", channel.localAddress());
        return channel;
    }

}



