package com.icbms.iot.inbound.component;

import com.icbms.iot.constant.IotConstant;
import com.icbms.iot.dto.RealtimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.icbms.iot.constant.IotConstant.REAL_DATA_PROCESS_CAPACITY;
import static com.icbms.iot.constant.IotConstant.STOP_MSG_PROCESS_THRESHOLD;

@Component
public class RealtimeMsgQueue {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Map<String, ArrayBlockingQueue<RealtimeMessage>> map = new ConcurrentHashMap<>();

    public void offer(RealtimeMessage msg) {
        if(msg == null)
            return;

        String gatewayId = msg.getGatewayId();
        map.putIfAbsent(gatewayId, new ArrayBlockingQueue<>(1000));
        ArrayBlockingQueue<RealtimeMessage> queue = map.get(gatewayId);
        queue.offer(msg);
        logger.debug("网关" + gatewayId + "实时数据处理队列长度: " + queue.size());
    }

    public RealtimeMessage poll(String gatewayId) {
        ArrayBlockingQueue<RealtimeMessage> queue = map.get(gatewayId);
        if(queue == null) {
            logger.error("网关:" + gatewayId + "数据队列不存在!");
            return null;
        }

        try {
            return queue.poll(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("获取网关"+gatewayId+"实时消息超时, {}", e);
            return null;
        }
    }

    public List<RealtimeMessage> pollBatch(String gatewayId) {
        ArrayBlockingQueue<RealtimeMessage> queue = map.get(gatewayId);
        if(queue == null) {
            logger.error("网关:" + gatewayId + "数据队列不存在!");
            return null;
        }

        List<RealtimeMessage> list = new ArrayList<>();
        try {
            if(queue.size() >= REAL_DATA_PROCESS_CAPACITY) {
                for (int i = 0; i < REAL_DATA_PROCESS_CAPACITY; i++) {
                    RealtimeMessage msg = queue.poll(1, TimeUnit.SECONDS);
                    list.add(msg);
                }
            }
        } catch(InterruptedException e) {
            logger.info("网关" + gatewayId + "队列没有" + REAL_DATA_PROCESS_CAPACITY + "个消息!");
        }

        return list;
    }

    public List<RealtimeMessage> pollAll(String gatewayId) {
        ArrayBlockingQueue<RealtimeMessage> queue = map.get(gatewayId);
        if(queue == null) {
            logger.error("网关:" + gatewayId + "数据队列不存在!");
            return null;
        }

        List<RealtimeMessage> list = new ArrayList<>();
        try {
            for (int i = 0; i < STOP_MSG_PROCESS_THRESHOLD; i++) {
                RealtimeMessage msg = queue.poll(1, TimeUnit.SECONDS);
                list.add(msg);
            }
        } catch (InterruptedException e) {
        }

        return list;
    }

}
