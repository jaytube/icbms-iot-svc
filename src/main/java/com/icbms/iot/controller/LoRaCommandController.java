package com.icbms.iot.controller;

import com.icbms.iot.common.CommonResponse;
import com.icbms.iot.enums.LoRaCommand;
import com.icbms.iot.rest.LoRaCommandService;
import com.icbms.iot.util.Base64Util;
import com.icbms.iot.util.RestUtil;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.icbms.iot.util.CommonUtil.hexStringToBytes;

/**
 * @Author: Cherry
 * @Date: 2021/1/17
 * @Desc: LoRaCommandController
 */
@RestController
@RequestMapping("/lora")
@Api(tags = "测试LoRa接口")
public class LoRaCommandController {

    @Autowired
    private LoRaCommandService loRaCommandService;

    @Autowired
    private RestUtil restUtil;

    private static String URL = "http://10.0.1.70:9900/api-sdm/";

    private static String URL_QUERY = "https://10.0.1.70:8080/api/";

    @GetMapping("/cmd/{deviceId}/{command}")
    @ResponseBody
    public CommonResponse cmd(@PathVariable("deviceId") String deviceId, @PathVariable("command") LoRaCommand command) throws Exception {
        return loRaCommandService.executeCmd(command, deviceId);
    }

    @GetMapping("/start")
    @ResponseBody
    public CommonResponse start() {
        return loRaCommandService.startRoundRobin();
    }

    @GetMapping("/stop")
    @ResponseBody
    public CommonResponse stop() {
        return loRaCommandService.stopRoundRobin();
    }

    @GetMapping("/rest/cmd/{deviceId}/{command}")
    @ResponseBody
    public CommonResponse restCmd(@PathVariable("deviceId") String deviceId, @PathVariable("command") LoRaCommand command) throws Exception {
        byte[] commandBytes = hexStringToBytes(command.getCmd());
        Map<String, Object> map = new HashMap<>();
        map.put("confirmed", false);
        map.put("data", new String(Base64Util.encodeToString(commandBytes)));
        map.put("devEUI", deviceId);
        map.put("fPort", 4);
        map.put("reference", "reference");
        String action = "nodes/" + deviceId + "/queue";
        Map result = restUtil.doPost(URL_QUERY + action, map);
        return CommonResponse.success(result);
    }

    @GetMapping("/rest/start")
    @ResponseBody
    public CommonResponse restStart() {
        Map<String, Object> param = new HashMap<>();
        param.put("tenant", "cluing");
        param.put("type", "S08");
        param.put("time", "100");
        String action = URL + "v1/pUI";
        Map result = restUtil.doPost(action, param);
        return CommonResponse.success(result);
    }

    @GetMapping("/rest/stop")
    @ResponseBody
    public CommonResponse restStop() {
        Map<String, Object> param = new HashMap<>();
        param.put("tenant", "cluing");
        String action = URL + "v1/stpp";
        Map result = restUtil.doPost(action, param);
        return CommonResponse.success(result);
    }

}
