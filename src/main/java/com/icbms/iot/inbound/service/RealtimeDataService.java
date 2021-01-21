package com.icbms.iot.inbound.service;

import com.icbms.iot.dto.RealtimeMessage;

import java.util.List;

public interface RealtimeDataService {

    void processRealtimeData(List<RealtimeMessage> realtimeMsgList);
}
