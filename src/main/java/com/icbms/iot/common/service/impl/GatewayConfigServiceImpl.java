package com.icbms.iot.common.service.impl;

import com.icbms.iot.common.service.GatewayConfigService;
import com.icbms.iot.entity.GatewayInfo;
import com.icbms.iot.entity.ProjectInfo;
import com.icbms.iot.mapper.GatewayInfoMapper;
import com.icbms.iot.mapper.ProjectInfoMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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
    private ProjectInfoMapper projectInfoMapper;

    @Autowired
    private GatewayInfoMapper gatewayInfoMapper;


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
        return "c76c14ecbc05466095f96f0a782e26e6";
    }

    @Override
    public String getGatewayIdByDevEUI(String devEui) {
        return "23";
    }

    @Override
    public List<GatewayInfo> getAvailableGateways() {
        Date date = new Date();
        List<ProjectInfo> allEffectiveProjects = projectInfoMapper.findAllEffectiveProjects(date);
        if(CollectionUtils.isEmpty(allEffectiveProjects))
            return new ArrayList<>();

        List<String> gatewayList = allEffectiveProjects.stream().filter(Objects::nonNull)
                .map(ProjectInfo::getGatewayAddress).distinct().collect(Collectors.toList());

        if(CollectionUtils.isEmpty(gatewayList))
            return new ArrayList<>();

        List<GatewayInfo> gatewayInfoList = gatewayInfoMapper.findByGatewayId(gatewayList);

        return gatewayInfoList;
    }

}
