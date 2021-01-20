package com.icbms.iot.inbound.service.impl;

import com.icbms.iot.dto.RealTimeMessage;
import com.icbms.iot.dto.RichMqttMessage;
import com.icbms.iot.entity.AlarmDataEntity;
import com.icbms.iot.inbound.component.InboundMsgQueue;
import com.icbms.iot.inbound.component.ProcessedMsgQueue;
import com.icbms.iot.inbound.service.AlarmDataService;
import com.icbms.iot.inbound.service.InBoundMessageMaster;
import com.icbms.iot.inbound.service.MqttMsgWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.icbms.iot.constant.IotConstant.*;

@Service
public class MqttMsgWorkerImpl implements MqttMsgWorker {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final int PROCESS_CAPACITY = 10;

    @Autowired
    private InboundMsgQueue inboundMsgQueue;

    @Autowired
    private ProcessedMsgQueue processedMsgQueue;

    @Autowired
    @Qualifier("realTimeMessageProcessMaster")
    private InBoundMessageMaster realTimeProcessMaster;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AlarmDataService alarmDataService;

    @Override
    @Async
    public void processMsg() {
        while(true) {
            try {
                if (!inboundMsgQueue.isEmpty()) {
                    RichMqttMessage mqttMsg = inboundMsgQueue.poll();
                    realTimeProcessMaster.setParameter(mqttMsg);
                    realTimeProcessMaster.performExecute();
                }
                if(processedMsgQueue.size() >= PROCESS_CAPACITY) {
                    List<RealTimeMessage> msgList = new ArrayList<>();
                    IntStream.rangeClosed(0, processedMsgQueue.size())
                            .forEach(i -> msgList.add(processedMsgQueue.poll()));

                    //TODO redis store data
                    Map<String, String> resultMap = new HashMap<>();
                    List<AlarmDataEntity> resultList = new ArrayList<>();
                    for(RealTimeMessage msg : msgList) {
                        Map<String, Object> map = alarmDataService.generateAlarmData(msg);
                        Map<String, String> redisMap = (Map<String, String>) map.get(REDIS_ALARM);
                        resultMap.putAll(redisMap);
                        List<AlarmDataEntity> list = (List<AlarmDataEntity>) map.get(MYSQL_ALARM);
                        resultList.addAll(list);
                    }

                    redisTemplate.opsForHash().putAll(ALARM_DATA, resultMap);
                    alarmDataService.saveAlarmDataEntityList(resultList);
                }
            } catch(Exception e) {}
        }
    }

}
