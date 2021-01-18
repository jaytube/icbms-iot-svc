package com.icbms.iot.rest.impl;

import com.icbms.iot.common.CommonResponse;
import com.icbms.iot.enums.LoRaCommand;
import com.icbms.iot.rest.LoRaCommandService;
import com.icbms.iot.util.Base64Util;
import com.icbms.iot.util.HttpUtil;
import com.icbms.iot.util.RestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.icbms.iot.util.CommonUtil.hexStringToBytes;

/**
 * @Author: Cherry
 * @Date: 2021/1/17
 * @Desc: LoRaCommandServiceImpl
 */
@Service
@Slf4j
public class LoRaCommandServiceImpl implements LoRaCommandService {

    @Autowired
    private RestUtil restUtil;

    private static final String startRoundRobin = "http://10.0.1.70:9900/api-sdm/v1/pUI";

    private static final String stopRoundRobin = "http://10.0.1.70:9900/api-sdm/v1/stpp";

    private static final String executeCmd = "https://10.0.1.70:8080/api/";

    @Override
    public CommonResponse startRoundRobin() {
        Map<String, Object> params = new HashMap<>();
        params.put("tenant", "cluing");
        params.put("type", "S08");
        params.put("time", "100");
        Map result = restUtil.doPost(startRoundRobin, params);
        return CommonResponse.success(result);
    }

    @Override
    public CommonResponse stopRoundRobin() {
        Map<String, Object> params = new HashMap<>();
        params.put("tenant", "cluing");
        Map result = restUtil.doPost(stopRoundRobin, params);
        return CommonResponse.success(result);
    }

    @Override
    public CommonResponse executeCmd(LoRaCommand command, String deviceId) {
        byte[] commandBytes = hexStringToBytes(command.getCmd());
        Map<String, Object> map = new HashMap<>();
        map.put("confirmed", false);
        map.put("data", Base64Util.encodeToString(commandBytes));
        map.put("devEUI", deviceId);
        map.put("fPort", 4);
        map.put("reference", "reference");
        String action = "nodes/" + deviceId + "/queue";
        Map result = restUtil.doPost(executeCmd + action, map);
        return CommonResponse.success(result);
    }
}
