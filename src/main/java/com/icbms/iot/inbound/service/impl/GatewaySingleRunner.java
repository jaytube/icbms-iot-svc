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

import static com.icbms.iot.constant.IotConstant.MAX_WAITING_TIME;

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
            try {
                processRoundRobin(gateway, gatewayId);
                gateway = gatewayKeeper.getById(gatewayId);
            }catch (Exception e) {
                logger.error("SINGLE RUNNER ERROR: {}", e);
            }
        }
    }

    private void processRoundRobin(GatewayDto gateway, int gatewayId) {
        CommonResponse resp = loRaCommandService.startRoundRobin(gateway.getIp());
        gateway.setFinished(false);
        if(resp == null || resp.getCode() != 200)
            return;

        logger.info("开启轮询网关" + gateway.getId() + ", ip: " + gateway.getIp() + ", 响应：" + resp.getData());
        long timeCost = 0;
        long start = System.currentTimeMillis();
        while (!gateway.isFinished() && timeCost < MAX_WAITING_TIME) {
            timeCost = (System.currentTimeMillis() - start) / 1000;
            gateway = gatewayKeeper.getById(gatewayId);
            continue;
        }
        logger.info("网关" + +gateway.getId() + ", ip: " + gateway.getIp() + "轮询结束, 本次轮询花费时间:" + timeCost + "秒！");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            logger.info("sleep interrupted...");
        }
    }

    public void stop(int gatewayId) {
        GatewayDto gateway = gatewayKeeper.getById(gatewayId);
        if(gateway != null)
            gateway.setStopped(true);
    }
}
