package com.icbms.iot.inbound.service;

public interface ScheduleTaskService {

    void monitorDevice();

    void monitorGateway();

    void roundRobinControl();

    void dailyBatchRemoveUserProjects();

    //void tmpJob();
}
