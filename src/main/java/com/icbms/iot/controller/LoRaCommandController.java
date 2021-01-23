package com.icbms.iot.controller;

import com.icbms.iot.common.CommonResponse;
import com.icbms.iot.dto.AddDeviceDto;
import com.icbms.iot.enums.LoRaCommand;
import com.icbms.iot.rest.LoRaCommandService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/getToken")
    @ResponseBody
    public CommonResponse getToken() {
        return CommonResponse.success(loRaCommandService.getToken());
    }

    @GetMapping("/getDbInstance")
    @ResponseBody
    public CommonResponse getDbInstance(String code) {
        return CommonResponse.success(loRaCommandService.getDbInstance(code));
    }

    @GetMapping("/getGatewayList")
    @ResponseBody
    public CommonResponse getGatewayList() {
        return CommonResponse.success(loRaCommandService.getGatewayList());
    }

    @GetMapping("/getGateWayById")
    @ResponseBody
    public CommonResponse getGateWayById(String id) {
        return CommonResponse.success(loRaCommandService.getGateWayById(id));
    }

    @GetMapping("/getDevice")
    @ResponseBody
    public CommonResponse getDevice(String deviceSn) {
        return loRaCommandService.getDevice(deviceSn);
    }

    @PostMapping("/addDevice")
    @ResponseBody
    public CommonResponse addDevice(@RequestBody AddDeviceDto addDeviceDto) {
        return loRaCommandService.addDevice(addDeviceDto);
    }

    @GetMapping("/deleteDevice")
    @ResponseBody
    public CommonResponse deleteDevice(String deviceSn) {
        return loRaCommandService.deleteDevice(deviceSn);
    }

}
