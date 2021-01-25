package com.icbms.iot.rest;

import com.icbms.iot.common.CommonResponse;
import com.icbms.iot.dto.AddDeviceDto;
import com.icbms.iot.dto.DeviceInfoDto;
import com.icbms.iot.dto.GateWayInfoDto;
import com.icbms.iot.dto.TerminalTypeDto;
import com.icbms.iot.enums.LoRaCommand;

import java.util.List;
import java.util.Map;

/**
 * @Author: Cherry
 * @Date: 2021/1/17
 * @Desc: LoRaCommand
 */
public interface LoRaCommandService {

    CommonResponse<Map> startRoundRobin();

    CommonResponse<Map> stopRoundRobin();

    CommonResponse<Map> executeCmd(LoRaCommand command, String deviceId);

    CommonResponse<String> getToken();

    String getRedisToken();

    CommonResponse<String> getDbInstance(String code);

    String getDbInstanceFromRedis(String code);

    CommonResponse<List<GateWayInfoDto>> getGatewayList();

    CommonResponse<GateWayInfoDto> getGateWayById(String gateWayId);

    CommonResponse<List<TerminalTypeDto>> getTerminalTypes();

    CommonResponse<List<TerminalTypeDto>> getTerminalByType(String type);

    CommonResponse addDevice(AddDeviceDto addDeviceDto);

    CommonResponse<List<DeviceInfoDto>> getDevices(String deviceKey);

    CommonResponse deleteDevice(String deviceSn);

    CommonResponse<Map> deleteDevices(List<Integer> deviceIds);
}
