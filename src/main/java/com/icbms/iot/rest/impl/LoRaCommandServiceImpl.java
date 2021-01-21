package com.icbms.iot.rest.impl;

import com.icbms.iot.common.CommonResponse;
import com.icbms.iot.dto.AddDeviceDto;
import com.icbms.iot.enums.LoRaCommand;
import com.icbms.iot.rest.LoRaCommandService;
import com.icbms.iot.util.Base64Util;
import com.icbms.iot.util.RestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private RedisTemplate redisTemplate;

    private static final String START_ROUND_ROBIN = "http://10.0.1.70:9900/api-sdm/v1/pUI";

    private static final String STOP_ROUND_ROBIN = "http://10.0.1.70:9900/api-sdm/v1/stpp";

    private static final String EXECUTE_CMD = "https://10.0.1.70:8080/api/";

    private static final String DEVICE_IP = "http://10.0.1.70:9900";

    private static final String HTTP_HEADER_TENANT = "20190701_cluing";

    private static final String HTTP_HEADER_CONTENT_TYPE = "application/json;charset=UTF-8";

    @Override
    public CommonResponse startRoundRobin() {
        Map<String, Object> params = new HashMap<>();
        params.put("tenant", "cluing");
        params.put("type", "S08");
        params.put("time", "100");
        Map result = restUtil.doPost(START_ROUND_ROBIN, params);
        return CommonResponse.success(result);
    }

    @Override
    public CommonResponse stopRoundRobin() {
        Map<String, Object> params = new HashMap<>();
        params.put("tenant", "cluing");
        Map result = restUtil.doPost(STOP_ROUND_ROBIN, params);
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
        Map result = restUtil.doPost(EXECUTE_CMD + action, map);
        return CommonResponse.success(result);
    }

    @Override
    public CommonResponse getToken() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "client_credentials");
        params.add("scope", "all");
        params.add("client_id", "cluing");
        params.add("client_secret", "CngWVDbTSn");
        Map result = restUtil.doPostFormDataNoToken(DEVICE_IP + "/api-dca/oauth/token", params);
        Object token_type = result.get("token_type");
        Object access_token = result.get("access_token");
        Object expires_in = result.get("expires_in");
        if (token_type != null && access_token != null && expires_in != null) {
            String bearer_token = Objects.toString(token_type) + Objects.toString(access_token);
            redisTemplate.opsForValue().set("BEARER_TOKEN", bearer_token);
            redisTemplate.expire("BEARER_TOKEN", Long.parseLong(Objects.toString(expires_in)), TimeUnit.SECONDS);
        }
        return CommonResponse.success(result);
    }

    @Override
    public String getRedisToken() {
        Object bearer_token = redisTemplate.opsForValue().get("BEARER_TOKEN");
        if (bearer_token == null) {
            getToken();
            bearer_token = redisTemplate.opsForValue().get("BEARER_TOKEN");
        }
        return Objects.toString(bearer_token);
    }

    @Override
    public CommonResponse getDbInstance() {
        return null;
    }

    @Override
    public CommonResponse getGatewayList() {
        return null;
    }

    @Override
    public CommonResponse getGatewayApplication(String applicationId) {
        return null;
    }

    @Override
    public CommonResponse getTerminalType() {
        return null;
    }

    @Override
    public CommonResponse getTerminalByType() {
        return null;
    }

    /**
     * {
     * "applicationId": "34",
     * "deviceSn": "393235306A55566",
     * "gatewayId": "39",
     * "name": "测试55566",
     * "templateId": "8",
     * "type": "S08",
     * "toLora":1,
     * "typeName": "RCMII"
     * }
     *
     * @return
     */
    @Override
    public CommonResponse addDevice(AddDeviceDto addDeviceDto) {
        Map<String, Object> params = new HashMap<>();
        params.put("applicationId", addDeviceDto.getApplicationId());
        params.put("deviceSn", addDeviceDto.getDeviceSn());
        params.put("gatewayId", addDeviceDto.getGatewayId());
        params.put("name", addDeviceDto.getName());
        params.put("templateId", addDeviceDto.getTemplateId());
        params.put("type", addDeviceDto.getType());
        params.put("toLora", addDeviceDto.getToLora());
        params.put("typeName", addDeviceDto.getTypeName());
        Map map = restUtil.doPostWithToken(DEVICE_IP + "/api-sdm/SdmDevice", params);
        return CommonResponse.success(map);
    }

    @Override
    public CommonResponse getDevice(String deviceSn) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", 1);
        params.put("limit", 99);
        params.put("keyWord", deviceSn);
        Map map = restUtil.doGetWithToken(DEVICE_IP + "/api-sdm/SdmDevice", params);
        return CommonResponse.success(map);
    }

    @Override
    public CommonResponse deleteDevice(String deviceSn) {
        Map map = restUtil.doDeleteWithToken(DEVICE_IP + "/api-sdm/SdmDevice/" + deviceSn);
        return CommonResponse.success(map);
    }
}
