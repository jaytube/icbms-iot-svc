package com.icbms.iot.rest.impl;

import com.icbms.iot.common.CommonResponse;
import com.icbms.iot.dto.AddDeviceDto;
import com.icbms.iot.dto.GateWayInfoDto;
import com.icbms.iot.enums.LoRaCommand;
import com.icbms.iot.rest.LoRaCommandService;
import com.icbms.iot.util.Base64Util;
import com.icbms.iot.util.RestUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    public String getToken() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "client_credentials");
        params.add("scope", "all");
        params.add("client_id", "cluing");
        params.add("client_secret", "CngWVDbTSn");
        Map result = restUtil.doPostFormDataNoToken(DEVICE_IP + "/api-dca/oauth/token", params);
        Object token_type = result.get("token_type");
        Object access_token = result.get("access_token");
        Object expires_in = result.get("expires_in");
        String bearer_token = null;
        if (token_type != null && access_token != null && expires_in != null) {
            bearer_token = Objects.toString(token_type) + " " + Objects.toString(access_token);
            redisTemplate.opsForValue().set("BEARER_TOKEN", bearer_token);
            redisTemplate.expire("BEARER_TOKEN", Long.parseLong(Objects.toString(expires_in)), TimeUnit.SECONDS);
        }
        return bearer_token;
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
    public String getDbInstance(String code) {
        // code cluing
        Map result = restUtil.doGetNoToken(DEVICE_IP + "/api-tms/pass/scptenant/" + code + "_123");
        Map datas = MapUtils.getMap(result, "datas");
        String instance = MapUtils.getString(datas, "instance");
        return instance;
    }

    @Override
    public String getDbInstanceFromRedis(String code) {
        Object db_instance_tenant = redisTemplate.opsForValue().get("DB_INSTANCE_TENANT");
        if (db_instance_tenant != null) {
            return Objects.toString(db_instance_tenant);
        }
        return getDbInstance(code);
    }

    @Override
    public List<GateWayInfoDto> getGatewayList() {
        Map<String, Object> params = new HashMap<>();
        params.put("page", 1);
        params.put("limit", 99);
        Map result = restUtil.doGetWithToken(DEVICE_IP + "/api-sdm/SdmGateway", params);
        List<Map<String, Object>> list = (List<Map<String, Object>>) MapUtils.getObject(result, "data");
        if (list == null || list.size() == 0) {
            return new ArrayList<>();
        }
        return list.stream().map(map -> convert(map)).collect(Collectors.toList());
    }

    @Override
    public GateWayInfoDto getGateWayById(String gateWayId) {
        Map result = restUtil.doGetWithToken(DEVICE_IP + "/api-sdm/SdmGateway/" + gateWayId, null);
        Map data = MapUtils.getMap(result, "datas");
        GateWayInfoDto gateWayInfoDto = new GateWayInfoDto();
        if (data == null) {
            return gateWayInfoDto;
        }
        return convert(data);
    }

    @Override
    public CommonResponse getTerminalType() {
        Map<String, Object> params = new HashMap<>();
        params.put("page", 1);
        params.put("limit", 99);
        Map result = restUtil.doGetWithToken(DEVICE_IP + "/api-sdm/SdmTemplate", params);
        return CommonResponse.success(result);
    }

    @Override
    public CommonResponse getTerminalByType(String type) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", type); // S08
        Map result = restUtil.doGetWithToken(DEVICE_IP + "/api-sdm/SdmDevice/getTemplatesByType", params);
        return CommonResponse.success(result);
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

    private GateWayInfoDto convert(Map<String, Object> map) {
        GateWayInfoDto gateWayInfoDto = new GateWayInfoDto();
        gateWayInfoDto.setId(MapUtils.getIntValue(map, "id"));
        gateWayInfoDto.setCreateTime(MapUtils.getLongValue(map, "createTime"));
        gateWayInfoDto.setUpdateTime(MapUtils.getLongValue(map, "updateTime"));
        gateWayInfoDto.setLoraId(MapUtils.getObject(map, "loraId"));
        gateWayInfoDto.setName(MapUtils.getString(map, "name"));
        gateWayInfoDto.setApplicationId(MapUtils.getIntValue(map, "applicationId"));
        gateWayInfoDto.setApplicationName(MapUtils.getString(map, "applicationName"));
        gateWayInfoDto.setLoraApplicatonId(MapUtils.getObject(map, "loraApplicatonId"));
        gateWayInfoDto.setSceneId(MapUtils.getIntValue(map, "sceneId"));
        gateWayInfoDto.setSceneName(MapUtils.getString(map, "sceneName"));
        gateWayInfoDto.setLoraSceneId(MapUtils.getIntValue(map, "loraSceneId"));
        gateWayInfoDto.setMacAddress(MapUtils.getString(map, "macAddress"));
        gateWayInfoDto.setDes(MapUtils.getString(map, "des"));
        gateWayInfoDto.setMgrUrl(MapUtils.getString(map, "mgrUrl"));
        gateWayInfoDto.setCreateUserId(MapUtils.getIntValue(map, "createUserId"));
        gateWayInfoDto.setUpdateUserName(MapUtils.getString(map, "updateUserName"));
        gateWayInfoDto.setUpdateUserId(MapUtils.getIntValue(map, "updateUserId"));
        gateWayInfoDto.setIsDel(MapUtils.getIntValue(map, "isDel"));
        return gateWayInfoDto;
    }
}
