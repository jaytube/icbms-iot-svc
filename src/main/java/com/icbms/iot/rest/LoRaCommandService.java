package com.icbms.iot.rest;

import com.icbms.iot.common.CommonResponse;
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
}
