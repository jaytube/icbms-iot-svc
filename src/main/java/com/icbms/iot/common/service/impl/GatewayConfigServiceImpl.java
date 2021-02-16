package com.icbms.iot.common.service.impl;

import com.icbms.iot.common.CommonResponse;
import com.icbms.iot.common.service.GatewayConfigService;
import com.icbms.iot.entity.GatewayAddress;
import com.icbms.iot.entity.GatewayInfo;
import com.icbms.iot.mapper.GatewayAddressMapper;
import com.icbms.iot.mapper.GatewayInfoMapper;
import com.icbms.iot.rest.LoRaCommandService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;

import static com.icbms.iot.constant.IotConstant.GATEWAY_CONFIG;
import static com.icbms.iot.constant.IotConstant.TERMINAL_STATUS;

@Service
public class GatewayConfigServiceImpl implements GatewayConfigService {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private StringRedisTemplate redisTempalte;

    @Autowired
    private GatewayAddressMapper gateWayAddressMapper;

    @Autowired
    private LoRaCommandService loRaCommandService;

    @Autowired
    private GatewayInfoMapper gateWayInfoMapper;

    //@PostConstruct
    public void initGateWayInfos() {
        List<GatewayAddress> allAddress = gateWayAddressMapper.findAll();
        if (CollectionUtils.isEmpty(allAddress)) {
            return;
        }
        List<GatewayInfo> allInDb = gateWayInfoMapper.findAll();
        Map<String, GatewayInfo> map = new HashMap<>();
        allInDb.forEach(item -> map.put(item.getIpAddress(), item));
        List<GatewayInfo> allGateWays = new ArrayList<>();
        for (GatewayAddress address : allAddress) {
            CommonResponse<List<GatewayInfo>> gatewayListData = loRaCommandService.getGatewayList(address.getIpAddress());
            List<GatewayInfo> gatewayInfos = gatewayListData.getData();
            if (CollectionUtils.isEmpty(gatewayInfos)) {
                continue;
            }
            allGateWays.addAll(gatewayInfos);
        }
        List<GatewayInfo> infoList = allGateWays.stream().filter(gatewayInfo -> !map.containsKey(gatewayInfo.getIpAddress())).collect(Collectors.toList());
        gateWayInfoMapper.batchInsert(infoList);
    }


    public String getProjectIdByGatewayId(String gatewayId) {
        String projectId = (String) redisTempalte.opsForHash().get(GATEWAY_CONFIG, gatewayId);
        logger.info("当前网关id：" + gatewayId + "，对应项目id：" + projectId);
        return projectId;
    }

    public Set<String> getCurrentGatewayIdSet() {
        Map<Object, Object> map = redisTempalte.opsForHash().entries(GATEWAY_CONFIG);
        if (MapUtils.isEmpty(map))
            return new HashSet<>();

        Set<String> result = map.keySet().stream().filter(Objects::nonNull).map(Objects::toString).collect(Collectors.toSet());
        return result;
    }

    public Set<String> getCurrentTerminalIdSet() {
        Map<Object, Object> map = redisTempalte.opsForHash().entries(TERMINAL_STATUS);
        if (MapUtils.isEmpty(map))
            return new HashSet<>();

        Set<String> result = map.keySet().stream().filter(Objects::nonNull).map(Objects::toString).filter(t-> t.contains("_LY")).map(t -> t.split("_")[0]).collect(Collectors.toSet());
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
