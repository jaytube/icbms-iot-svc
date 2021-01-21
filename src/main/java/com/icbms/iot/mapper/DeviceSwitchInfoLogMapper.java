package com.icbms.iot.mapper;

import com.icbms.iot.entity.DeviceSwitchInfoLog;
import org.apache.ibatis.annotations.Insert;

import java.util.List;

public interface DeviceSwitchInfoLogMapper {

    @Insert({
            "<script>",
            "insert into device_switch_info_log (id, project_id, device_box_id, device_box_id, node, type, alarm_level,",
            "alarm_status, info, remark, record_time, create_time, update_time) values",
            "<foreach collection='alarmInfoLogs' item='item' index='index' seperator=','>",
            "(#{item.id}, #{item.projectId}), #{item.deviceBoxMac}, #{item.deviceBoxId}, #{item.node}, #{item.type}, #{item.alarmLevel},",
            "#{item.alarmStatus}, #{item.info}, #{item.remark}, #{item.recordTime}, #{item.createTime}, #{item.updateTime})",
            "</foreach>",
            "</script>"
    })
    //TODO modify above DDL
    void batchInsert(List<DeviceSwitchInfoLog> deviceSwitchInfoLog);

}
