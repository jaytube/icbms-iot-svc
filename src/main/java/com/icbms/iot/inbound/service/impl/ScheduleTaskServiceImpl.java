package com.icbms.iot.inbound.service.impl;

import com.alibaba.fastjson.JSON;
import com.icbms.iot.common.CommonResponse;
import com.icbms.iot.common.component.GatewayKeeper;
import com.icbms.iot.common.service.GatewayConfigService;
import com.icbms.iot.dto.GatewayStatusDto;
import com.icbms.iot.dto.TerminalStatusDto;
import com.icbms.iot.entity.*;
import com.icbms.iot.inbound.service.AlarmDataService;
import com.icbms.iot.inbound.service.ScheduleTaskService;
import com.icbms.iot.mapper.*;
import com.icbms.iot.rest.LoRaCommandService;
import com.icbms.iot.util.DateUtil;
import com.icbms.iot.util.TerminalBoxConvertUtil;
import com.icbms.iot.util.TerminalStatusUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;

import static com.icbms.iot.constant.IotConstant.*;

@Service
public class ScheduleTaskServiceImpl implements ScheduleTaskService {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private AlarmDataService alarmDataService;

    @Autowired
    private GatewayDeviceMapMapper gatewayDeviceMapMapper;

    @Autowired
    private LoRaCommandService loRaCommandService;

    @Autowired
    private GatewayKeeper gatewayKeeper;

    @Autowired
    private GatewayInfoMapper gatewayInfoMapper;

    @Autowired
    private GatewayConfigService gatewayConfigService;

    @Autowired
    private ProjectInfoMapper projectInfoMapper;

    @Autowired
    private UserProjectMapper userProjectMapper;

    @Autowired
    private DeviceBoxInfoMapper deviceBoxInfoMapper;

    @Override
    @Scheduled(fixedDelay = MONITOR_DEVICE_FREQUENCY, initialDelay = MONITOR_DEVICE_FREQUENCY)
    @Transactional
    public void monitorDevice() {
        logger.info("开始监测设备状态...");
        List<GatewayDeviceMap> all = gatewayDeviceMapMapper.findAll();
        if(CollectionUtils.isEmpty(all))
            return;

        Map<Integer, List<GatewayDeviceMap>> map = all.stream().distinct().collect(Collectors.groupingBy(GatewayDeviceMap::getGatewayId));
        for (Map.Entry<Integer, List<GatewayDeviceMap>> cursor : map.entrySet()) {
            String gatewayId = Integer.toString(cursor.getKey());
            List<GatewayDeviceMap> val = cursor.getValue();
            if(CollectionUtils.isEmpty(val))
                continue;

            List<AlarmDataEntity> list = new ArrayList<>();
            Map<String, String> alarmDataMap = new HashMap<>();
            Map<String, String> terminalStatusMap = new HashMap<>();
            for (GatewayDeviceMap device : val) {
                String hashKey = TerminalBoxConvertUtil.getTerminalNo(device.getDeviceBoxNum()) + "_100";
                long currentTime = System.currentTimeMillis();
                Object lastUpdated = stringRedisTemplate.opsForHash().get(REAL_HIS_DATA_STORE_UP_TO_DATE, hashKey);
                if(lastUpdated != null && (currentTime - Long.parseLong((String) lastUpdated)) > HEART_BEAT) {
                    AlarmDataEntity alarmData = generateDeviceAlarmData(device,
                            currentTime - Long.parseLong((String) lastUpdated), gatewayId);
                    String key = alarmData.getTerminalId() + "_100_16";
                    String old = (String) stringRedisTemplate.opsForHash().get(ALARM_DATA, key);
                    if(StringUtils.isNotBlank(old)) {
                        AlarmDataEntity oldEntity = JSON.parseObject(old, AlarmDataEntity.class);
                        if (!"1".equals(oldEntity.getAlarmStatus())) {
                            list.add(alarmData);
                            alarmDataMap.put(key, JSON.toJSONString(alarmData));
                        }
                    } else {
                        list.add(alarmData);
                        alarmDataMap.put(key, JSON.toJSONString(alarmData));
                    }
                    TerminalStatusDto statusDto = TerminalStatusUtil.getTerminalBadStatus(gatewayId, alarmData.getTerminalId());
                    String statusKey = alarmData.getTerminalId() + "_LY";
                    terminalStatusMap.put(statusKey, JSON.toJSONString(statusDto));
                } else if(lastUpdated != null && (currentTime - Long.parseLong((String) lastUpdated)) <= HEART_BEAT_RECOVER) {
                    String key = TerminalBoxConvertUtil.getTerminalNo(device.getDeviceBoxNum()) + "_100_16";
                    String alarmStr = (String) stringRedisTemplate.opsForHash().get(ALARM_DATA, key);
                    AlarmDataEntity alarmData = JSON.parseObject(alarmStr, AlarmDataEntity.class);
                    if(StringUtils.isNotBlank(alarmStr)) {
                        if("1".equalsIgnoreCase(alarmData.getAlarmStatus())) {
                            alarmData.setAlarmStatus("0");
                            alarmData.setAlarmContent("第["+TerminalBoxConvertUtil.getTerminalNo(device.getDeviceBoxNum())+"]号终端恢复连接!");
                            alarmData.setReportTime(DateUtil.parseDate(System.currentTimeMillis()));
                            alarmData.setAlarmType(DEVICE_NO_SIGNAL_RECOVER);
                            alarmDataMap.put(key, JSON.toJSONString(alarmData));
                            list.add(alarmData);
                        }
                    }
                    TerminalStatusDto statusDto = TerminalStatusUtil.getTerminalOkStatus(gatewayId, alarmData.getTerminalId());
                    String statusKey = alarmData.getTerminalId() + "_LY";
                    terminalStatusMap.put(statusKey, JSON.toJSONString(statusDto));
                } else if(lastUpdated == null) {
                    stringRedisTemplate.opsForHash().put(REAL_HIS_DATA_STORE_UP_TO_DATE, hashKey, Long.toString(currentTime));
                }
            }
            alarmDataService.saveAlarmDataEntityList(list);
            stringRedisTemplate.opsForHash().putAll(ALARM_DATA, alarmDataMap);
            stringRedisTemplate.opsForHash().putAll(TERMINAL_STATUS, terminalStatusMap);
        }
    }

    @Override
    @Scheduled(fixedDelay = MONITOR_GATEWAY_FREQUENCY, initialDelay = MONITOR_GATEWAY_FREQUENCY)
    @Transactional
    public void monitorGateway() {
        logger.info("开始监测网关状态...");
        List<GatewayInfo> gatewayInfoList = gatewayInfoMapper.findAll();
        List<AlarmDataEntity> list = new ArrayList<>();
        Map<String, String> alarmDataMap = new HashMap<>();
        for (GatewayInfo gateway : gatewayInfoList) {
            String ip = gateway.getIpAddress();
            int id = gateway.getGatewayId();
            CommonResponse<List<GatewayInfo>> resp = loRaCommandService.getGatewayList(ip);
            if(resp.getCode() != 200) {
                String dto = (String)stringRedisTemplate.opsForHash().get(GATEWAY_STATUS, Integer.toString(id));
                long delta = MONITOR_GATEWAY_FREQUENCY;
                if(dto != null) {
                    GatewayStatusDto gatewayStatusDto = JSON.parseObject(dto, GatewayStatusDto.class);
                    Date lastReportDate = DateUtil.parse(gatewayStatusDto.getReportTime(), "yyyy-MM-dd HH:mm:ss");
                    long lastReportTime = lastReportDate.getTime();
                    delta = System.currentTimeMillis() - lastReportTime;
                }
                AlarmDataEntity alarmData = generateGatewayAlarmData(delta, id);
                list.add(alarmData);
                GatewayStatusDto statusDto = generateGatewayStatusDto(alarmData.getAlarmContent(), alarmData.getReportTime(), 1);
                alarmDataMap.put(Integer.toString(id), JSON.toJSONString(statusDto));
            } else {
                String dto = (String)stringRedisTemplate.opsForHash().get(GATEWAY_STATUS, Integer.toString(id));
                if(dto != null) {
                    GatewayStatusDto gatewayStatusDto = JSON.parseObject(dto, GatewayStatusDto.class);
                    if(gatewayStatusDto.getStatus() == 1) {
                        AlarmDataEntity alarmData = generateGatewayRecoverAlarmData(id);
                        list.add(alarmData);
                        GatewayStatusDto statusDto = generateGatewayStatusDto(alarmData.getAlarmContent(), alarmData.getReportTime(), 0);
                        alarmDataMap.put(Integer.toString(id), JSON.toJSONString(statusDto));
                    }
                } else {
                    GatewayStatusDto statusDto = generateGatewayStatusDto("第["+id+"]号网关运行状态正常!", DateUtil.parseDate(System.currentTimeMillis()), 0);
                    alarmDataMap.put(Integer.toString(id), JSON.toJSONString(statusDto));
                }
            }
        }

        alarmDataService.saveAlarmDataEntityList(list);
        stringRedisTemplate.opsForHash().putAll(GATEWAY_STATUS, alarmDataMap);
    }

    @Override
    @Scheduled(fixedDelay = ROUND_ROBIN_FREQUENCY, initialDelay = ROUND_ROBIN_FREQUENCY)
    @Transactional
    public void roundRobinControl() {
        logger.info("开始网关轮询...");
        List<GatewayInfo> gatewayInfoList = gatewayConfigService.getAvailableGateways();
        if(CollectionUtils.isEmpty(gatewayInfoList))
            return;

        if(CollectionUtils.isNotEmpty(gatewayInfoList)) {
            List<GatewayInfo> onlineList = gatewayInfoList.stream().filter(g -> "1".equals(g.getOnline())).collect(Collectors.toList());
            String str = onlineList.stream().map(t -> Integer.toString(t.getGatewayId())).collect(Collectors.joining(","));
            logger.info("网关:" + str + "开始轮询");

            List<GatewayInfo> offlineList = gatewayInfoList.stream().filter(g -> "0".equals(g.getOnline())).collect(Collectors.toList());

            if(CollectionUtils.isNotEmpty(offlineList)) {
                offlineList.stream().forEach(g -> {
                    String ip = g.getIpAddress();
                    loRaCommandService.stopRoundRobin(ip);
                    logger.debug("网关: " + ip + "停止轮询");
                });
            }

            if(CollectionUtils.isNotEmpty(onlineList)) {
                onlineList.stream().forEach(g -> {
                    String ip = g.getIpAddress();
                    loRaCommandService.startRoundRobin(ip);
                    logger.debug("网关: " + ip + "开始轮询");
                });
            }
        }
        //Map<Integer, GatewayDto> gatewayDtoMap = gatewayKeeper.getGatewayMap();
        //Map<Integer, GatewayInfo> gatewayInfoMap = new HashMap<>();
        /*gatewayInfoList.stream().forEach(t -> {
            gatewayInfoMap.put(t.getGatewayId(), t);
        });*/
        /*Iterator<Map.Entry<Integer, GatewayDto>> iterator = gatewayDtoMap.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<Integer, GatewayDto> next = iterator.next();
            int gatewayId = next.getKey();
            if(!gatewayInfoMap.containsKey(gatewayId))
                iterator.remove();
        }*/
        /*gatewayInfoMap.entrySet().stream().forEach(e -> {
            int gatewayId = e.getKey();
            GatewayInfo gatewayInfo = e.getValue();
            GatewayDto dto = gatewayDtoMap.getOrDefault(gatewayInfo.getGatewayId(), null);
            if(dto == null) {
                GatewayDto gatewayDto = new GatewayDto();
                dto.setFinished(false);
                dto.setStopped(false);
                dto.setId(gatewayId);
                dto.setIp(gatewayInfo.getIpAddress());
                dto.setType(GatewayRunType.SINGLE);
                gatewayKeeper.getGatewayMap().put(gatewayId, gatewayDto);
            }
        });*/
        /*gatewayInfoMap.entrySet().stream().forEach(e -> {
            GatewayInfo g = e.getValue();
            String ip = g.getIpAddress();
            *//*CompletableFuture.runAsync(() -> {
                loRaCommandService.startRoundRobin(ip);
            }, taskExecutor);*//*
            loRaCommandService.startRoundRobin(ip);
            logger.debug("网关: " + ip + "开始轮询");
            //g.setFinished(false);
        });*/
        /*loRaCommandService.startRoundRobin("http://10.0.1.71");*/
    }

    @Override
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void dailyBatchRemoveUserProjects() {
        logger.info("定时删除用户项目关系开始。。。");
        List<ProjectInfo> projects = projectInfoMapper.findAllUnEffectiveProjects(new Date());
        if(CollectionUtils.isEmpty(projects))
            return;

        List<String> projectIdList = projects.stream().filter(Objects::nonNull).map(ProjectInfo::getId).distinct().collect(Collectors.toList());
        projectInfoMapper.updateExpiredProject(projectIdList);
        logger.info("更新过期项目");
        if(CollectionUtils.isNotEmpty(projectIdList))
            logger.info("删除项目ID列表：" + projectIdList.stream().collect(Collectors.joining(", ")));
        userProjectMapper.deleteByProjectIdList(projectIdList);
        logger.info("删除用户与项目关联关系!");
        /*projects = projects.stream().filter(Objects::nonNull).filter(p -> p.getGymId() == 2).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(projects)) {
            for (ProjectInfo project : projects) {
                CommonResponse resp = loRaCommandService.deleteDeviceInGateway(project);
                if(resp.getCode() == 200) {
                    logger.info("删除项目：" + project.getProjectName() + ", 网关：" + project.getGatewayAddress() + " 下所有设备!");
                    String val = (String) stringRedisTemplate.opsForHash().get(GATEWAY_CONFIG, project.getGatewayAddress());
                    if(StringUtils.isNotBlank(val) && val.equalsIgnoreCase(project.getId()))
                        stringRedisTemplate.opsForHash().delete(GATEWAY_CONFIG, project.getGatewayAddress());
                }
            }
        }*/
    }

    private AlarmDataEntity generateDeviceAlarmData(GatewayDeviceMap deviceNumEuiDto, long delta, String gatewayId) {
        AlarmDataEntity alarmData = new AlarmDataEntity();
        String boxNo = TerminalBoxConvertUtil.getTerminalNo(deviceNumEuiDto.getDeviceBoxNum());
        alarmData.setAlarmContent("第[" + boxNo + "]号终端超过" + delta + "ms未连接!");
        alarmData.setAlarmStatus("1");
        alarmData.setSwitchAddr("100");
        alarmData.setAlarmType(DEVICE_NO_SIGNAL);
        alarmData.setAlarmLevel("3");
        alarmData.setProjectId(deviceNumEuiDto.getProjectId());
        alarmData.setTerminalId(boxNo);
        alarmData.setGatewayId(gatewayId);
        alarmData.setReportTime(DateUtil.parseDate(System.currentTimeMillis()));

        return alarmData;
    }

    private GatewayStatusDto generateGatewayStatusDto(String msg, String reportDate, int status) {
        GatewayStatusDto gatewayStatusDto = new GatewayStatusDto();
        gatewayStatusDto.setMsg(msg);
        gatewayStatusDto.setReportTime(reportDate);
        gatewayStatusDto.setStatus(status);

        return gatewayStatusDto;
    }

    private AlarmDataEntity generateGatewayAlarmData(long delta, Integer gatewayId) {
        AlarmDataEntity alarmData = new AlarmDataEntity();
        alarmData.setAlarmContent("第[" + gatewayId + "]号网关超过" + delta + "ms未连接!");
        alarmData.setAlarmStatus("1");
        alarmData.setAlarmType("网关通信中断");
        alarmData.setAlarmLevel("3");
        String projectId = gatewayConfigService.getProjectIdByGatewayId(Integer.toString(gatewayId));
        alarmData.setProjectId(projectId);
        alarmData.setGatewayId(Integer.toString(gatewayId));
        alarmData.setReportTime(DateUtil.parseDate(System.currentTimeMillis()));

        return alarmData;
    }

    private AlarmDataEntity generateGatewayRecoverAlarmData(Integer gatewayId) {
        AlarmDataEntity alarmData = new AlarmDataEntity();
        alarmData.setAlarmContent("第[" + gatewayId + "]号网关恢复正常状态!");
        alarmData.setAlarmStatus("0");
        alarmData.setAlarmType("网关通信中断");
        alarmData.setAlarmLevel("3");
        String projectId = gatewayConfigService.getProjectIdByGatewayId(Integer.toString(gatewayId));
        alarmData.setProjectId(projectId);
        alarmData.setGatewayId(Integer.toString(gatewayId));
        alarmData.setReportTime(DateUtil.parseDate(System.currentTimeMillis()));

        return alarmData;
    }
}
