package com.icbms.iot.common.service.impl;

import com.icbms.iot.common.service.DeviceInfoService;
import com.icbms.iot.dto.DevicePlainInfoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static com.icbms.iot.constant.IotConstant.DEVICE_INFO;
import static com.icbms.iot.util.TerminalBoxConvertUtil.getTerminalString;

@Service
public class DeviceInfoServiceImpl implements DeviceInfoService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public String getProjectIdByDeviceNo(String deviceNo) {
        String terminalString = getTerminalString(deviceNo);
        DevicePlainInfoDto dto = (DevicePlainInfoDto) redisTemplate.opsForHash().get(DEVICE_INFO, terminalString);
        if(dto != null)
            return dto.getProjectId();

        return "";
    }
}
