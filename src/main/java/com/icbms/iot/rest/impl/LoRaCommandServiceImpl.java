package com.icbms.iot.rest.impl;

import com.alibaba.fastjson.JSON;
import com.icbms.iot.common.CommonResponse;
import com.icbms.iot.dto.AddDeviceDto;
import com.icbms.iot.dto.DeviceInfoDto;
import com.icbms.iot.dto.TerminalTypeDto;
import com.icbms.iot.entity.DeviceBoxInfo;
import com.icbms.iot.entity.GatewayInfo;
import com.icbms.iot.entity.ProjectInfo;
import com.icbms.iot.enums.LoRaCommand;
import com.icbms.iot.exception.ErrorCodeEnum;
import com.icbms.iot.exception.IotException;
import com.icbms.iot.mapper.*;
import com.icbms.iot.rest.LoRaCommandService;
import com.icbms.iot.util.Base64Util;
import com.icbms.iot.util.RestUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.icbms.iot.util.CommonUtil.hexStringToBytes;
import static com.icbms.iot.util.RestUtil.HTTP_HEADER_CONTENT_TYPE;

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
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GatewayDeviceMapMapper gatewayDeviceMapMapper;

    @Autowired
    private DeviceSwitchInfoLogMapper deviceSwitchInfoLogMapper;

    @Autowired
    private DeviceAlarmInfoLogMapper deviceAlarmInfoLogMapper;

    @Autowired
    private DeviceLocationInfoMapper deviceLocationInfoMapper;

    @Autowired
    private DeviceBoxInfoMapper deviceBoxInfoMapper;

    @Autowired
    private DeviceSwitchInfoDetailLogMapper deviceSwitchInfoDetailLogMapper;

    @Autowired
    private LocationInfoMapper locationInfoMapper;

    @Autowired
    private GatewayInfoMapper gatewayInfoMapper;

    private static final String START_ROUND_ROBIN_URI = ":9900/api-sdm/v1/pUI";

    private static final String STOP_ROUND_ROBIN_URI = ":9900/api-sdm/v1/stpp";

    private static final String EXECUTE_CMD_URI = ":8080/api/";

    private static final String DEVICE_IP_URI = ":9900";

    @Override
    public CommonResponse<Map> startRoundRobin(String gatewayIp) {
        Map<String, Object> params = new HashMap<>();
        params.put("tenant", "cluing");
        params.put("type", "S08");
        params.put("time", "200");
        return restUtil.doPost(gatewayIp + START_ROUND_ROBIN_URI, params);
    }

    @Override
    public CommonResponse<Map> stopRoundRobin(String gatewayIp) {
        Map<String, Object> params = new HashMap<>();
        params.put("tenant", "cluing");
        return restUtil.doPost(gatewayIp + STOP_ROUND_ROBIN_URI, params);
    }

    @Override
    public CommonResponse<Map> executeCmd(String gatewayIp, LoRaCommand command, String deviceId) {
        byte[] commandBytes = hexStringToBytes(command.getCmd());
        Map<String, Object> map = new HashMap<>();
        map.put("confirmed", false);
        map.put("data", Base64Util.encodeToString(commandBytes));
        map.put("devEUI", deviceId);
        map.put("fPort", 4);
        map.put("reference", "reference");
        String action = "nodes/" + deviceId + "/queue";
        return restUtil.doPost("https" + gatewayIp.substring(4) + EXECUTE_CMD_URI + action, map);
    }

    @Override
    public CommonResponse<String> getToken(String gatewayIp) {
        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "client_credentials");
        params.put("scope", "all");
        params.put("client_id", "cluing");
        params.put("client_secret", "CngWVDbTSn");
        CommonResponse<Map> response = restUtil.doPostFormDataNoToken(gatewayIp + DEVICE_IP_URI + "/api-dca/oauth/token", params);
        if (response.getCode() != 200) {
            log.error("token获取失败!");
            throw new IotException(ErrorCodeEnum.TOKEN_FETCH_ERROR);
        }
        Map result = response.getData();
        Object token_type = result.get("token_type");
        Object access_token = result.get("access_token");
        Object expires_in = result.get("expires_in");
        String bearer_token = null;
        if (token_type != null && access_token != null && expires_in != null) {
            bearer_token = Objects.toString(token_type) + " " + Objects.toString(access_token);
        }

        if (StringUtils.isNotBlank(bearer_token)) {
            CommonResponse<String> success = CommonResponse.success();
            return success.setData(bearer_token);
        } else {
            log.error("token获取失败! token为空！");
            throw new IotException(ErrorCodeEnum.TOKEN_FETCH_ERROR);
        }
    }

    @Override
    public String getRedisToken(String gatewayIp) {
        CommonResponse<String> tokenResp = getToken(gatewayIp);
        if(tokenResp.getCode() == 200) {
            return tokenResp.getData();
        } else {
            throw new IotException(ErrorCodeEnum.TOKEN_FETCH_ERROR);
        }
    }

    @Override
    public CommonResponse<String> getDbInstance(String gatewayIp, String code) {
        // code cluing
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Content-Type", HTTP_HEADER_CONTENT_TYPE);
        requestHeaders.add("Authorization", getRedisToken(gatewayIp));
        CommonResponse<Map> response = restUtil.doGetWithToken(gatewayIp + DEVICE_IP_URI + "/api-tms/pass/scptenant/" + code + "_123", requestHeaders);
        if (response.getCode() != 200) {
            return CommonResponse.faild(response.getMsg(), JSON.toJSONString(response.getData()));
        }
        Map result = response.getData();
        Map datas = MapUtils.getMap(result, "datas");
        String instance = MapUtils.getString(datas, "instance");
        if (StringUtils.isNotBlank(instance)) {
            CommonResponse<String> success = CommonResponse.success();
            return success.setData(instance);
        } else {
            return CommonResponse.faild("GET DbInstance FAILED.");
        }
    }

    @Override
    public String getDbInstanceFromRedis(String gatewayIp, String code) {
        Object db_instance_tenant = redisTemplate.opsForValue().get("DB_INSTANCE_TENANT");
        if (db_instance_tenant != null) {
            return Objects.toString(db_instance_tenant);
        }
        CommonResponse<String> dbInstance = getDbInstance(gatewayIp, code);
        if (dbInstance.getCode() == 200) {
            return dbInstance.getData();
        }
        return null;
    }

    @Override
    public CommonResponse<List<GatewayInfo>> getGatewayList(String gatewayIp) {
        CommonResponse<Map> response = restUtil.doGetWithToken(gatewayIp, gatewayIp + DEVICE_IP_URI + "/api-sdm/SdmGateway?page=1&limit=99");
        if (response.getCode() != 200) {
            return CommonResponse.faild(response.getMsg(), null);
        }
        Map result = response.getData();
        List<Map<String, Object>> list = (List<Map<String, Object>>) MapUtils.getObject(result, "data");
        if (list == null || list.size() == 0) {
            return CommonResponse.success(new ArrayList<>());
        }
        return CommonResponse.success(list.stream().map(map -> convertGateWay(gatewayIp, map)).collect(Collectors.toList()));
    }

    @Override
    public CommonResponse<GatewayInfo> getGateWayById(String gatewayIp, String gateWayId) {
        CommonResponse<Map> response = restUtil.doGetWithToken(gatewayIp, gatewayIp + DEVICE_IP_URI + "/api-sdm/SdmGateway/" + gateWayId);
        if (response.getCode() != 200) {
            return CommonResponse.faild(response.getMsg(), null);
        }
        Map result = response.getData();
        Map data = MapUtils.getMap(result, "datas");
        if (data == null) {
            return CommonResponse.success(null);
        }
        return CommonResponse.success(convertGateWay(gatewayIp, data));
    }

    @Override
    public CommonResponse<List<TerminalTypeDto>> getTerminalTypes(String gatewayIp) {
        CommonResponse<Map> response = restUtil.doGetWithToken(gatewayIp, gatewayIp + DEVICE_IP_URI + "/api-sdm/SdmTemplate?page=1&limit=99");
        if (response.getCode() != 200) {
            return CommonResponse.faild(response.getMsg(), null);
        }
        Map result = response.getData();
        List<Map<String, Object>> list = (List<Map<String, Object>>) MapUtils.getObject(result, "data");
        if (list == null || list.size() == 0) {
            return CommonResponse.success(new ArrayList<>());
        }
        return CommonResponse.success(list.stream().map(map -> convertTerminalType(map)).collect(Collectors.toList()));
    }

    @Override
    public CommonResponse<List<TerminalTypeDto>> getTerminalByType(String gatewayIp, String type) {
        // S08
        CommonResponse<Map> response = restUtil.doGetWithToken(gatewayIp, gatewayIp + DEVICE_IP_URI + "/api-sdm/SdmDevice/getTemplatesByType?type=" + type);
        if (response.getCode() != 200) {
            return CommonResponse.faild(response.getMsg(), null);
        }
        Map result = response.getData();
        Map<String, Object> datas = MapUtils.getMap(result, "datas");
        List<Map<String, Object>> list = (List<Map<String, Object>>) MapUtils.getObject(datas, "templates");
        if (list == null || list.size() == 0) {
            return CommonResponse.success(new ArrayList<>());
        }
        return CommonResponse.success(list.stream().map(map -> convertTerminalType(map)).collect(Collectors.toList()));
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
    public CommonResponse addDevice(String gatewayIp, AddDeviceDto addDeviceDto) {
        Map<String, Object> params = new HashMap<>();
        params.put("applicationId", addDeviceDto.getApplicationId());
        params.put("deviceSn", addDeviceDto.getDeviceSn());
        params.put("gatewayId", addDeviceDto.getGatewayId());
        params.put("name", addDeviceDto.getName());
        params.put("templateId", addDeviceDto.getTemplateId());
        params.put("type", addDeviceDto.getType());
        params.put("toLora", addDeviceDto.getToLora());
        params.put("typeName", addDeviceDto.getTypeName());
        return restUtil.doPostWithToken(gatewayIp, gatewayIp + DEVICE_IP_URI + "/api-sdm/SdmDevice", params);
    }

    @Override
    public CommonResponse<List<DeviceInfoDto>> getDevices(String gatewayIp, String deviceKey) {
        String uri = "/api-sdm/SdmDevice?page=1&limit=3000";
        if (StringUtils.isNotBlank(deviceKey)) {
            uri += "&keyWord=" + deviceKey;
        }
        CommonResponse<Map> response = restUtil.doGetWithToken(gatewayIp, gatewayIp + DEVICE_IP_URI + uri + deviceKey);
        if (response.getCode() != 200) {
            return CommonResponse.faild(response.getMsg(), null);
        }
        Map result = response.getData();
        List<Map<String, Object>> list = (List<Map<String, Object>>) MapUtils.getObject(result, "data");
        if (list == null || list.size() == 0) {
            return CommonResponse.success(new ArrayList<>());
        }
        List<DeviceInfoDto> data = list.stream().map(map -> convertDeviceInfo(map)).collect(Collectors.toList());
        long count = list.stream().count();
        return CommonResponse.success(count, data);
    }

    @Override
    public CommonResponse deleteDevice(String gatewayIp, String deviceSn) {
        return restUtil.doDeleteWithToken(gatewayIp, gatewayIp + DEVICE_IP_URI + "/api-sdm/SdmDevice/" + deviceSn);
    }

    @Override
    public CommonResponse<Map> deleteDevices(String gatewayIp, List<Integer> deviceIds) {
        if (CollectionUtils.isEmpty(deviceIds)) {
            return CommonResponse.error("deviceIds 为空。");
        }
        List<Map> body = deviceIds.stream().map(id -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("isDel", 1);
            return map;
        }).collect(Collectors.toList());
        return restUtil.doDeleteWithToken(gatewayIp, gatewayIp + DEVICE_IP_URI + "/api-sdm/SdmDevice/batchDel", body);
    }

    @Override
    @Transactional
    public void deleteDevicesByProjectId(String projectId, String gatewayId) {
        GatewayInfo gatewayInfo = gatewayInfoMapper.findById(gatewayId);
        String gatewayIp = gatewayInfo.getIpAddress();
        gatewayDeviceMapMapper.deleteByGatewayId(Integer.parseInt(gatewayId));
        List<DeviceBoxInfo> boxList = deviceBoxInfoMapper.findByProjectIdList(Arrays.asList(projectId));
        List<String> boxIdList = boxList.stream().filter(Objects::nonNull).map(DeviceBoxInfo::getId).distinct().collect(Collectors.toList());
        deviceBoxInfoMapper.deleteByProjectId(projectId);
        locationInfoMapper.deleteByProjectId(projectId);
        if(CollectionUtils.isNotEmpty(boxIdList))
            deviceLocationInfoMapper.deleteByDeviceBoxIds(boxIdList);
        deviceAlarmInfoLogMapper.deleteByProjectId(projectId);
        deviceSwitchInfoLogMapper.deleteByProjectId(projectId);
        deviceSwitchInfoDetailLogMapper.deleteByProjectId(projectId);
        CommonResponse<List<DeviceInfoDto>> devicesResp = getDevices(gatewayIp, "");
        List<DeviceInfoDto> devices = devicesResp.getData();
        if(CollectionUtils.isNotEmpty(devices)) {
            List<Integer> ids = devices.stream().map(DeviceInfoDto::getId).distinct().collect(Collectors.toList());
            CommonResponse<Map> deleteResp = deleteDevices(gatewayIp, ids);
            if(!"200".equals(deleteResp.getCode()))
                throw new IotException(ErrorCodeEnum.REMOTE_CALL_FAILED);
        }
    }

    @Override
    public CommonResponse stopGateway(String gatewayId) {
        GatewayInfo gatewayInfo = gatewayInfoMapper.findById(gatewayId);
        CommonResponse<Map> response = stopRoundRobin(gatewayInfo.getIpAddress());
        Map result = response.getData();
        Integer code = (Integer) MapUtils.getObject(result, "resp_code");
        String msg = (String) MapUtils.getObject(result, "resp_msg");
        if(response.getCode() != 200 || code == 1)
            return CommonResponse.faild(msg);

        gatewayInfoMapper.updateGatewayOnlineByGatewayId(gatewayId, "0");

        return CommonResponse.success();
    }

    @Override
    public CommonResponse startGateway(String gatewayId) {
        GatewayInfo gatewayInfo = gatewayInfoMapper.findById(gatewayId);
        if(gatewayInfo != null) {
            gatewayInfoMapper.updateGatewayOnlineByGatewayId(gatewayId, "1");
            return startRoundRobin(gatewayInfo.getIpAddress());
        }
        return CommonResponse.faild();
    }

    @Override
    public CommonResponse deleteDeviceInGateway(ProjectInfo projectInfo) {
        if(projectInfo.getGymId() != 2)
            return CommonResponse.success("非龙阳馆不删除!");

        String gatewayId = projectInfo.getGatewayAddress();
        GatewayInfo gatewayInfo = gatewayInfoMapper.findById(gatewayId);
        String gatewayIp = gatewayInfo.getIpAddress();
        CommonResponse<List<DeviceInfoDto>> devicesResp = getDevices(gatewayIp, "");
        List<DeviceInfoDto> devices = devicesResp.getData();
        if(CollectionUtils.isNotEmpty(devices)) {
            List<Integer> ids = devices.stream().map(DeviceInfoDto::getId).distinct().collect(Collectors.toList());
            CommonResponse<Map> deleteResp = deleteDevices(gatewayIp, ids);
            if(!"200".equals(deleteResp.getCode())) {
                log.info("错误代码: {}, 网关ID: {}", deleteResp.getMsg(), gatewayId);
                throw new IotException(ErrorCodeEnum.REMOTE_CALL_FAILED);
            } else {
                gatewayDeviceMapMapper.deleteByGatewayIdAndProjectId(Integer.parseInt(gatewayId), projectInfo.getId());
                log.info("删除网关: {}, 项目 {} 下gateway device map表数据", gatewayId, projectInfo.getProjectName());
            }
        }

        return CommonResponse.success("网关上设备删除成功！");
    }

    private GatewayInfo convertGateWay(String gatewayIp, Map<String, Object> map) {
        GatewayInfo gateWayInfo = new GatewayInfo();
        gateWayInfo.setCreateTime(MapUtils.getLongValue(map, "createTime"));
        gateWayInfo.setUpdateTime(MapUtils.getLongValue(map, "updateTime"));
        gateWayInfo.setLoraId(MapUtils.getIntValue(map, "loraId"));
        gateWayInfo.setName(MapUtils.getString(map, "name"));
        gateWayInfo.setApplicationId(MapUtils.getIntValue(map, "applicationId"));
        gateWayInfo.setApplicationName(MapUtils.getString(map, "applicationName"));
        gateWayInfo.setLoraApplicationId(MapUtils.getIntValue(map, "loraApplicationId"));
        gateWayInfo.setSceneId(MapUtils.getIntValue(map, "sceneId"));
        gateWayInfo.setSceneName(MapUtils.getString(map, "sceneName"));
        gateWayInfo.setLoraSceneId(MapUtils.getIntValue(map, "loraSceneId"));
        gateWayInfo.setMacAddress(MapUtils.getString(map, "macAddress"));
        gateWayInfo.setDes(MapUtils.getString(map, "des"));
        gateWayInfo.setMgrUrl(MapUtils.getString(map, "mgrUrl"));
        gateWayInfo.setCreateUserId(MapUtils.getIntValue(map, "createUserId"));
        gateWayInfo.setUpdateUserName(MapUtils.getString(map, "updateUserName"));
        gateWayInfo.setUpdateUserId(MapUtils.getIntValue(map, "updateUserId"));
        gateWayInfo.setIsDel(MapUtils.getIntValue(map, "isDel"));
        gateWayInfo.setIpAddress(gatewayIp);
        gateWayInfo.setGatewayId(MapUtils.getIntValue(map, "id"));
        return gateWayInfo;
    }

    private TerminalTypeDto convertTerminalType(Map<String, Object> map) {
        TerminalTypeDto terminalTypeDto = new TerminalTypeDto();
        terminalTypeDto.setId(MapUtils.getIntValue(map, "id"));
        terminalTypeDto.setCreateTime(MapUtils.getLongValue(map, "createTime"));
        terminalTypeDto.setUpdateTime(MapUtils.getLongValue(map, "updateTime"));
        terminalTypeDto.setType(MapUtils.getString(map, "type"));
        terminalTypeDto.setProcessIsShow(MapUtils.getIntValue(map, "processIsShow"));
        terminalTypeDto.setImgUrl(MapUtils.getString(map, "imgUrl"));
        terminalTypeDto.setDes(MapUtils.getString(map, "des"));
        terminalTypeDto.setTitle(MapUtils.getString(map, "title"));
        terminalTypeDto.setTypeName(MapUtils.getString(map, "typeName"));
        terminalTypeDto.setTypeColor(MapUtils.getString(map, "typeColor"));
        terminalTypeDto.setVersion(MapUtils.getIntValue(map, "version"));
        terminalTypeDto.setCreateUserId(MapUtils.getIntValue(map, "createUserId"));
        terminalTypeDto.setUpdateUserName(MapUtils.getString(map, "updateUserName"));
        terminalTypeDto.setUpdateUserId(MapUtils.getIntValue(map, "updateUserId"));
        terminalTypeDto.setIsDel(MapUtils.getIntValue(map, "isDel"));
        terminalTypeDto.setChildren((List<Object>) MapUtils.getObject(map, "children"));
        return terminalTypeDto;
    }

    private DeviceInfoDto convertDeviceInfo(Map<String, Object> map) {
        DeviceInfoDto deviceInfoDto = new DeviceInfoDto();
        deviceInfoDto.setId(MapUtils.getIntValue(map, "id"));
        deviceInfoDto.setCreateTime(MapUtils.getString(map, "createTime"));
        deviceInfoDto.setUpdateTime(MapUtils.getString(map, "updateTime"));
        deviceInfoDto.setLoraId(MapUtils.getIntValue(map, "loraId"));
        deviceInfoDto.setApplicationId(MapUtils.getIntValue(map, "applicationId"));
        deviceInfoDto.setApplicationName(MapUtils.getString(map, "applicationName"));
        deviceInfoDto.setLoraAppId(MapUtils.getIntValue(map, "loraAppId"));
        deviceInfoDto.setGatewayId(MapUtils.getIntValue(map, "gatewayId"));
        deviceInfoDto.setGatewayName(MapUtils.getString(map, "gatewayName"));
        deviceInfoDto.setType(MapUtils.getString(map, "type"));
        deviceInfoDto.setDeviceSn(MapUtils.getString(map, "deviceSn"));
        deviceInfoDto.setName(MapUtils.getString(map, "name"));
        deviceInfoDto.setDes(MapUtils.getString(map, "des"));
        deviceInfoDto.setLot(MapUtils.getString(map, "lot"));
        deviceInfoDto.setLat(MapUtils.getString(map, "lat"));
        deviceInfoDto.setGroups(MapUtils.getString(map, "groups"));
        deviceInfoDto.setTemplateId(MapUtils.getIntValue(map, "templateId"));
        deviceInfoDto.setTypeName(MapUtils.getString(map, "typeName"));
        deviceInfoDto.setTypeColor(MapUtils.getString(map, "typeColor"));
        deviceInfoDto.setCreateUserId(MapUtils.getIntValue(map, "createUserId"));
        deviceInfoDto.setUpdateUserId(MapUtils.getIntValue(map, "updateUserId"));
        deviceInfoDto.setUpdateUserName(MapUtils.getString(map, "updateUserName"));
        deviceInfoDto.setIsDel(MapUtils.getIntValue(map, "isDel"));
        deviceInfoDto.setPointNum(MapUtils.getIntValue(map, "pointNum"));
        deviceInfoDto.setTemplateDes(MapUtils.getString(map, "templateDes"));
        deviceInfoDto.setToLora(MapUtils.getIntValue(map, "toLora"));
        deviceInfoDto.setImgUrl(MapUtils.getString(map, "imgUrl"));
        deviceInfoDto.setMgrUrl(MapUtils.getString(map, "mgrUrl"));
        deviceInfoDto.setSelected(MapUtils.getBooleanValue(map, "selected"));
        deviceInfoDto.setCardId(MapUtils.getIntValue(map, "cardId"));
        deviceInfoDto.setDeviceTemplateId(MapUtils.getIntValue(map, "deviceTemplateId"));
        return deviceInfoDto;
    }
}
