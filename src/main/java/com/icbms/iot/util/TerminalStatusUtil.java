package com.icbms.iot.util;

import com.icbms.iot.dto.TerminalStatusDto;

import java.util.Date;

public class TerminalStatusUtil {

    public static TerminalStatusDto getTerminalOkStatus(String gatewayId, String deviceNo) {
        TerminalStatusDto dto = new TerminalStatusDto();
        dto.setGatewayId(gatewayId);
        dto.setStatus(0);
        dto.setMsg("第[" + deviceNo + "]号终端运行状态正常!");
        dto.setReportTime(new Date());

        return dto;
    }

    public static TerminalStatusDto getTerminalBadStatus(String gatewayId, String deviceNo) {
        TerminalStatusDto dto = new TerminalStatusDto();
        dto.setGatewayId(gatewayId);
        dto.setStatus(1);
        dto.setMsg("第[" + deviceNo + "]号终端离线!");
        dto.setReportTime(new Date());

        return dto;
    }
}
