package com.icbms.iot.inbound.component;

import com.icbms.iot.dto.RealtimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ArrayBlockingQueue;

@Component
public class AlarmDataMsgQueue {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ArrayBlockingQueue<RealtimeMessage> alarmMsgQueue = new ArrayBlockingQueue(1000);

    public void offer(RealtimeMessage msg) {
        logger.info("告警数据处理队列长度: " + alarmMsgQueue.size());
        this.alarmMsgQueue.offer(msg);
    }

    public RealtimeMessage poll() {
        logger.info("告警数据处理队列长度: " + alarmMsgQueue.size());
        return this.alarmMsgQueue.poll();
    }

    public int size() {
        return this.alarmMsgQueue.size();
    }

    public boolean isEmpty() {
        return this.alarmMsgQueue.isEmpty();
    }
}
