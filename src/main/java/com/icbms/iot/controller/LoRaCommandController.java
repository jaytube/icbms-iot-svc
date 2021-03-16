package com.icbms.iot.controller;

import com.icbms.iot.common.CommonResponse;
import com.icbms.iot.dto.AddDeviceDto;
import com.icbms.iot.dto.DeviceInfoDto;
import com.icbms.iot.entity.GatewayInfo;
import com.icbms.iot.enums.LoRaCommand;
import com.icbms.iot.exception.IotException;
import com.icbms.iot.mapper.GatewayDeviceMapMapper;
import com.icbms.iot.mapper.GatewayInfoMapper;
import com.icbms.iot.rest.LoRaCommandService;
import com.icbms.iot.util.RestUtil;
import io.swagger.annotations.Api;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    @Autowired
    private GatewayInfoMapper gatewayInfoMapper;

    private String REST_IP = "http://10.0.1.73";

    @GetMapping("/cmd/{deviceId}/{command}")
    @ResponseBody
    public CommonResponse cmd(@PathVariable("deviceId") String deviceId, @PathVariable("command") LoRaCommand command, @RequestParam("gatewayIp") String gatewayIp) throws Exception {
        return loRaCommandService.executeCmd(gatewayIp, command, deviceId);
    }

    @GetMapping("/start")
    @ResponseBody
    public CommonResponse start(String ip) {
        return loRaCommandService.startRoundRobin(ip);
    }

    @GetMapping("/stop")
    @ResponseBody
    public CommonResponse stop(String ip) {
        return loRaCommandService.stopRoundRobin(ip);
    }

    @GetMapping("/getToken")
    @ResponseBody
    public CommonResponse getToken(@RequestParam("ip") String ip) {
        return loRaCommandService.getToken(ip);
    }

    @GetMapping("/getRedisToken")
    @ResponseBody
    public CommonResponse getRedisToken(String ip) {
        return CommonResponse.success(loRaCommandService.getRedisToken(ip));
    }

    @GetMapping("/getDbInstance")
    @ResponseBody
    public CommonResponse getDbInstance(String ip, String code) {
        return loRaCommandService.getDbInstance(ip, code);
    }

    @GetMapping("/getDbInstanceFromRedis")
    @ResponseBody
    public CommonResponse getDbInstanceFromRedis(String ip, String code) {
        return CommonResponse.success(loRaCommandService.getDbInstanceFromRedis(ip, code));
    }

    @GetMapping("/getGatewayList")
    @ResponseBody
    public CommonResponse getGatewayList(@RequestParam("ip") String ip) {
        return loRaCommandService.getGatewayList(ip);
    }

    @GetMapping("/getGateWayById")
    @ResponseBody
    public CommonResponse getGateWayById(String ip, String id) {
        return loRaCommandService.getGateWayById(ip, id);
    }

    @GetMapping("/getTerminalTypes")
    @ResponseBody
    public CommonResponse getTerminalTypes() {
        return loRaCommandService.getTerminalTypes(REST_IP);
    }

    @GetMapping("/getTerminalByType")
    @ResponseBody
    public CommonResponse getTerminalByType(String type) {
        return loRaCommandService.getTerminalByType(REST_IP, type);
    }

    @GetMapping("/getDevice")
    @ResponseBody
    public CommonResponse getDevice(String ip, String deviceSn) {
        return loRaCommandService.getDevices(ip, deviceSn);
    }

    @PostMapping("/addDevice")
    @ResponseBody
    public CommonResponse addDevice(@RequestBody AddDeviceDto addDeviceDto, String ip) {
        return loRaCommandService.addDevice(ip, addDeviceDto);
    }

    @GetMapping("/deleteDevice")
    @ResponseBody
    public CommonResponse deleteDevice(String deviceSn, String ip) {
        return loRaCommandService.deleteDevice(ip, deviceSn);
    }

    @PostMapping("/deleteDevices")
    @ResponseBody
    public CommonResponse deleteDevices(@RequestParam("gatewayIp") String gatewayIp, @RequestParam("id")Integer id) {
        List<Integer> ids = Arrays.asList(id);
        return loRaCommandService.deleteDevices(gatewayIp, ids);
    }

    @GetMapping("/test")
    @ResponseBody
    public CommonResponse test(String URL) {
        return restUtil.doGetNoToken(URL);
    }

    @GetMapping("/deleteBatch")
    public CommonResponse deleteBatch(@RequestParam("gatewayId") String gatewayId, @RequestParam("projectId") String projectId) {
        try {
            loRaCommandService.deleteDevicesByProjectId(projectId, gatewayId);
        } catch(IotException e) {
            return CommonResponse.faild(e.getErrorCode() + ", " + e.getErrorMessage());
        }
        return CommonResponse.success();
    }

    @GetMapping("/stopGateway")
    @ResponseBody
    public CommonResponse stopGateway(String gatewayId) {
        return loRaCommandService.stopGateway(gatewayId);
    }

    @GetMapping("/startGateway")
    @ResponseBody
    public CommonResponse startGateway(String gatewayId) {
        return loRaCommandService.startGateway(gatewayId);
    }
}
