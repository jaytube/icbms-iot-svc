package com.icbms.iot.common.service;

import com.icbms.iot.entity.GatewayInfo;

import java.util.List;
import java.util.Set;

public interface GatewayConfigService {

    String getProjectIdByGatewayId(String gatewayId);

    Set<String> getCurrentGatewayIdSet();

    Set<String> getCurrentTerminalIdSet();

    String getProjectIdByTerminalId(String terminalId);

    String getGatewayIdByDevEUI(String devEui);

    List<GatewayInfo> getAvailableGateways();

}
