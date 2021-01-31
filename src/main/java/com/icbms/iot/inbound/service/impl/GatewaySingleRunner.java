package com.icbms.iot.inbound.service.impl;

import com.icbms.iot.common.CommonResponse;
import com.icbms.iot.common.component.GatewayKeeper;
import com.icbms.iot.dto.GatewayDto;
import com.icbms.iot.inbound.service.GatewayRunner;
import com.icbms.iot.rest.LoRaCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

@Service("singleRunner")
public class GatewaySingleRunner implements GatewayRunner {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private LoRaCommandService loRaCommandService;

    @Autowired
    private GatewayKeeper gatewayKeeper;

    public void run(int gatewayId) {
        GatewayDto gateway = gatewayKeeper.getById(gatewayId);
        if(gateway == null)
            return;

        logger.info("线程:" + Thread.currentThread().getName()+" 开启单个网关轮询...");
        while(!gateway.isStopped()) {
            CommonResponse resp = loRaCommandService.startRoundRobin(gateway.getIp());
            gateway.setFinished(false);
            logger.info("开启轮询网关" + gateway.getId() + ", ip: " + gateway.getIp() + ", 响应：" + resp.getData());
            while(!gateway.isFinished()) {
                gateway = gatewayKeeper.getById(gatewayId);
                continue;
            }
            logger.info("网关" + + gateway.getId() + ", ip: " + gateway.getIp() + "轮询结束");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
            }
            gateway = gatewayKeeper.getById(gatewayId);
        }
    }

    public void stop(int gatewayId) {
        GatewayDto gateway = gatewayKeeper.getById(gatewayId);
        if(gateway != null)
            gateway.setStopped(true);
    }
}
