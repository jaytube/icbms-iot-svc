package com.icbms.iot.mapper;

import com.icbms.iot.entity.DeviceAlarmInfoLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DeviceAlarmInfoLogMapper {
    @Select("select * from device_alarm_info_log where id = #{id}")
    DeviceAlarmInfoLog findById(String id);

    @Insert({
            "<script>",
            "insert into device_alarm_info_log (id, project_id, device_box_mac, device_box_id, node, type, alarm_level,",
            "alarm_status, info, remark, record_time, create_time, update_time) values",
            "<foreach collection='alarmInfoLogs' item='item' index='index' separator=','>",
            "(#{item.id}, #{item.projectId}, #{item.deviceBoxMac}, #{item.deviceBoxId}, #{item.node}, #{item.type}, #{item.alarmLevel},",
            "#{item.alarmStatus}, #{item.info}, #{item.remark}, #{item.recordTime}, #{item.createTime}, #{item.updateTime})",
            "</foreach>",
            "</script>"
    })
    int batchInsert(List<DeviceAlarmInfoLog> alarmInfoLogs);

}
