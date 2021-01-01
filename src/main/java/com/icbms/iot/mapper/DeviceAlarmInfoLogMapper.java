package com.icbms.iot.mapper;

import com.icbms.iot.entity.DeviceAlarmInfoLog;
import org.apache.ibatis.annotations.Select;

public interface DeviceAlarmInfoLogMapper {

    @Select("select * from device_alarm_info_log where id = #{id}")
    DeviceAlarmInfoLog findById(String id);

}
