package com.icbms.iot.inbound.service.impl;

import com.icbms.iot.common.component.GatewayKeeper;
import com.icbms.iot.inbound.factory.GatewayRunnerFactory;
import com.icbms.iot.inbound.service.IotRoundRobinController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

@Service
public class IotRoundRobinControllerImpl implements IotRoundRobinController {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private GatewayRunnerFactory runnerFactory;

    @Autowired
    private GatewayKeeper gatewayKeeper;

    @Override
    @Async
    public void roundRobinControl() {
        /*logger.debug("Iot 网关控制服务初始化 ...");
        GatewayRunner groupRunner = runnerFactory.getRunnerByType(GatewayRunType.GROUP);
        Map<Integer, GatewayGroupDto> groups = gatewayKeeper.getGatewayGroupMap();FF
        if(MapUtils.isNotEmpty(groups)) {
            groups.entrySet().stream().forEach(e -> {
                CompletableFuture.runAsync(() -> {
                    groupRunner.run(e.getKey());
                }, taskExecutor);
            });
            logger.debug("启动网关群轮询 ...");
        }
        GatewayRunner singleRunner = runnerFactory.getRunnerByType(GatewayRunType.SINGLE);
        Map<Integer, GatewayDto> singles = gatewayKeeper.getGatewayMap();
        if(MapUtils.isNotEmpty(singles)) {
            singles.entrySet().stream().forEach(e -> {
                CompletableFuture.runAsync(() -> {
                    singleRunner.run(e.getKey());
                }, taskExecutor);
            });
            logger.debug("启动网关轮询 ...");
        }*/
    }
}
