package com.icbms.iot.common.component;

import com.icbms.iot.dto.GatewayDto;
import com.icbms.iot.dto.GatewayGroupDto;
import com.icbms.iot.entity.GatewayDeviceMap;
import com.icbms.iot.entity.GatewayInfo;
import com.icbms.iot.enums.GatewayRunType;
import com.icbms.iot.mapper.GatewayDeviceMapMapper;
import com.icbms.iot.mapper.GatewayInfoMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@ApplicationScope
@Component
public class GatewayKeeper {

    @Autowired
    private GatewayInfoMapper gatewayInfoMapper;

    @Autowired
    private GatewayDeviceMapMapper gatewayDeviceMapMapper;

    private volatile Map<Integer, GatewayDto> gatewayMap = new ConcurrentHashMap<>();

    private volatile Map<Integer, GatewayGroupDto> gatewayGroupMap = new ConcurrentHashMap<>();

    @PostConstruct
    private void initGatewayMap() {
        List<GatewayInfo> gatewayInfoList = gatewayInfoMapper.findAll();
        Map<Integer, GatewayDto> map = new HashMap<>();
        if(CollectionUtils.isNotEmpty(gatewayInfoList)) {
            gatewayInfoList.stream().forEach(g -> {
                GatewayDto dto = new GatewayDto();
                dto.setFinished(false);
                dto.setStopped(false);
                dto.setId(g.getGatewayId());
                dto.setIp(g.getIpAddress());
                dto.setType(GatewayRunType.SINGLE);
                map.put(g.getGatewayId(), dto);
            });
        }
        /*List<GatewayDeviceMap> allMaps = gatewayDeviceMapMapper.findAll();
        if(CollectionUtils.isNotEmpty(allMaps)) {
            List<Integer> onlineGateWayIds = allMaps.stream().map(GatewayDeviceMap::getGatewayId)
                    .distinct().collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(onlineGateWayIds)) {
                onlineGateWayIds.stream().forEach(i -> {
                    if(map.get(i) != null)
                        gatewayMap.put(i, map.get(i));
                });
            }
        }*/
        gatewayMap.putAll(map);
    }

    public Map<Integer, GatewayDto> getGatewayMap() {
        return gatewayMap;
    }

    public void setGatewayMap(Map<Integer, GatewayDto> gatewayMap) {
        this.gatewayMap = gatewayMap;
    }

    public Map<Integer, GatewayGroupDto> getGatewayGroupMap() {
        return gatewayGroupMap;
    }

    public void setGatewayGroupMap(Map<Integer, GatewayGroupDto> gatewayGroupMap) {
        this.gatewayGroupMap = gatewayGroupMap;
    }

    public GatewayDto getById(int gatewayId) {
        GatewayDto gateway = gatewayMap.getOrDefault(gatewayId, null);
        if(gateway == null) {
            for (Map.Entry<Integer, GatewayGroupDto> entry : gatewayGroupMap.entrySet()) {
                GatewayGroupDto group = entry.getValue();
                if(group != null && CollectionUtils.isNotEmpty(group.getGateways())) {
                    GatewayDto dto = group.getGateways().stream().filter(s -> s.getId() == gatewayId)
                            .findAny().orElse(null);
                    if(dto != null)
                        return dto;
                }
            }
        }
        return gateway;
    }

    public GatewayDto getByGroupIdAndId(int groupId, int gatewayId) {
        GatewayGroupDto group = gatewayGroupMap.getOrDefault(groupId, null);
        if(group != null) {
            return group.getGateways().stream().filter(t -> t.getId() == gatewayId).findAny().orElse(null);
        }

        return null;
    }

    public GatewayDto removeById(int gatewayId) {
        GatewayDto removed = gatewayMap.remove(gatewayId);
        return removed;
    }

    public GatewayGroupDto getByGroupId(int groupId) {
        return gatewayGroupMap.getOrDefault(groupId, null);
    }
}
