package com.icbms.iot.rest;

import com.icbms.iot.common.CommonResponse;
import com.icbms.iot.dto.AddDeviceDto;
import com.icbms.iot.enums.LoRaCommand;

/**
 * @Author: Cherry
 * @Date: 2021/1/17
 * @Desc: LoRaCommand
 */
public interface LoRaCommandService {

    CommonResponse startRoundRobin();

    CommonResponse stopRoundRobin();

    CommonResponse executeCmd(LoRaCommand command, String deviceId);

    CommonResponse getToken();

    String getRedisToken();

    CommonResponse getDbInstance(String code);

    CommonResponse getGatewayList();

    CommonResponse getGatewayApplication(String applicationId);

    CommonResponse getTerminalType();

    CommonResponse getTerminalByType(String type);

    CommonResponse addDevice(AddDeviceDto addDeviceDto);

    CommonResponse getDevice(String deviceSn);

    CommonResponse deleteDevice(String deviceSn);
}
