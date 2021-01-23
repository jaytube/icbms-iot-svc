package com.icbms.iot.rest;

import com.icbms.iot.common.CommonResponse;
import com.icbms.iot.dto.AddDeviceDto;
import com.icbms.iot.dto.ApplicationInfoDto;
import com.icbms.iot.dto.GateWayInfoDto;
import com.icbms.iot.enums.LoRaCommand;

import java.util.List;

/**
 * @Author: Cherry
 * @Date: 2021/1/17
 * @Desc: LoRaCommand
 */
public interface LoRaCommandService {

    CommonResponse startRoundRobin();

    CommonResponse stopRoundRobin();

    CommonResponse executeCmd(LoRaCommand command, String deviceId);

    String getToken();

    String getRedisToken();

    String getDbInstance(String code);

    String getDbInstanceFromRedis(String code);

    List<GateWayInfoDto> getGatewayList();

    GateWayInfoDto getGateWayById(String gateWayId);

    CommonResponse getTerminalType();

    CommonResponse getTerminalByType(String type);

    CommonResponse addDevice(AddDeviceDto addDeviceDto);

    CommonResponse getDevice(String deviceSn);

    CommonResponse deleteDevice(String deviceSn);
}
