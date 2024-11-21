package org.example.application;

import com.alibaba.fastjson.JSON;
import org.example.config.GatewayServiceProperties;
import org.example.domain.model.aggregates.ApplicationSystemRichInfo;
import org.example.domain.service.GatewayCenterService;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * 网关应用；与 Spring 链接，调用网关注册和接口拉取
 */
public class GatewayApplication implements ApplicationListener<ContextRefreshedEvent> {

    private GatewayServiceProperties properties;
    private GatewayCenterService gatewayCenterService;

    public GatewayApplication(GatewayServiceProperties properties, GatewayCenterService gatewayCenterService) {
        this.properties = properties;
        this.gatewayCenterService = gatewayCenterService;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 1. 注册网关服务；每一个用于转换 HTTP 协议泛化调用到 RPC 接口的网关都是一个算力，这些算力需要注册网关配置中心
        gatewayCenterService.doRegister(properties.getAddress(),
                properties.getGroupId(),
                properties.getGatewayId(),
                properties.getGatewayName(),
                properties.getGatewayAddress());

        // 2. 拉取网关配置；每个网关算力都会在注册中心分配上需要映射的RPC服务信息，包括；系统、接口、方法
        ApplicationSystemRichInfo applicationSystemRichInfo = gatewayCenterService.pullApplicationSystemRichInfo(properties.getAddress(), properties.getGatewayId());
        System.out.println(JSON.toJSONString(applicationSystemRichInfo));
    }

}

