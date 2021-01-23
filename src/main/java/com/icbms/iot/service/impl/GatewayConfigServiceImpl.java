package com.icbms.iot.service.impl;

import com.icbms.iot.service.GatewayConfigService;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.icbms.iot.constant.IotConstant.*;

@Service
public class GatewayConfigServiceImpl implements GatewayConfigService {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private StringRedisTemplate redisTempalte;


    public String getProjectIdByGatewayId(String gatewayId) {
        String projectId = (String) redisTempalte.opsForHash().get(GATEWAY_CONFIG, gatewayId);
        logger.info("当前网关id：" + gatewayId + "，对应项目id：" + projectId);
        return projectId;
    }

    public Set<String> getCurrentGatewayIdSet() {
        Map<Object, Object> map = redisTempalte.opsForHash().entries(GATEWAY_CONFIG);
        if(MapUtils.isEmpty(map))
            return new HashSet<>();

        Set<String> result = map.keySet().stream().filter(Objects::nonNull).map(Objects::toString).collect(Collectors.toSet());
        return result;
    }

    public Set<String> getCurrentTerminalIdSet() {
        Map<Object, Object> map = redisTempalte.opsForHash().entries(TERMINAL_STATUS);
        if(MapUtils.isEmpty(map))
            return new HashSet<>();

        Set<String> result = map.keySet().stream().filter(Objects::nonNull).map(Objects::toString).collect(Collectors.toSet());
        return result;
    }

    public String getProjectIdByTerminalId(String terminalId) {
        //just for test
        return "c76c14ecbc05466095f96f0a782e26e6";
        /*String projectId = (String) redisTempalte.opsForHash().get(TERMINAL_CONFIG, terminalId);
        return projectId;*/
    }

    @Override
    public String getGatewayIdByDevEUI(String devEui) {
        return "23";
    }

}
