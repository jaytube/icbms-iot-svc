package com.icbms.iot.inbound.component;

import com.icbms.iot.dto.RealtimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
public class RealtimeMsgQueue {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ArrayBlockingQueue<RealtimeMessage> processedMsgQueue = new ArrayBlockingQueue(1000);

    public void offer(RealtimeMessage msg) {
        this.processedMsgQueue.offer(msg);
        logger.info("实时数据处理队列长度: " + processedMsgQueue.size());
    }

    public RealtimeMessage poll() {
        try {
            return this.processedMsgQueue.poll(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("获取实时消息超时, {}", e);
            return null;
        }
    }

    public int size() {
        return this.processedMsgQueue.size();
    }

}
