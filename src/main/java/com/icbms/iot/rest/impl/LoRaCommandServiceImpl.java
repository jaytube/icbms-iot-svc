package com.icbms.iot.rest.impl;

import com.icbms.iot.common.CommonResponse;
import com.icbms.iot.enums.LoRaCommand;
import com.icbms.iot.rest.LoRaCommandService;
import com.icbms.iot.util.Base64Util;
import com.icbms.iot.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Base64;
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

    private static String URL = "http://10.0.1.70:9900/api-sdm/";

    private static String URL_QUERY = "https://10.0.210.41:8080/api/";

    @Override
    public CommonResponse startRoundRobin() {
        Map<String, Object> param = new HashMap<>();
        param.put("tenant", "cluing");
        param.put("type", "S08");
        param.put("time", "100");
        String action = URL + "v1/pUI";
        return HttpUtil.doPost(action, param);
    }

    @Override
    public CommonResponse stopRoundRobin() {
        Map<String, Object> param = new HashMap<>();
        param.put("tenant", "cluing");
        String action = URL + "v1/stpp";
        return HttpUtil.doPost(action, param);
    }

    @Override
    public CommonResponse executeCmd(LoRaCommand command, String deviceId) {
        byte[] commandBytes = hexStringToBytes(command.getCmd());
        Map<String, Object> map = new HashMap<>();
        map.put("confirmed", false);
        map.put("data", new String(Base64Util.encodeToString(commandBytes)));
        map.put("devEUI", deviceId);
        map.put("fPort", 4);
        map.put("reference", "reference");
        String action = "nodes/" + deviceId + "/queue";
        return HttpUtil.doPost(URL_QUERY + action, map);
    }
}
