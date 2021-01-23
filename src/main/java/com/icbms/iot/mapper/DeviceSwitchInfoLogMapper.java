package com.icbms.iot.mapper;

import com.icbms.iot.entity.DeviceSwitchInfoLog;
import org.apache.ibatis.annotations.Insert;

import java.util.List;

public interface DeviceSwitchInfoLogMapper {

    @Insert({
            "<script>",
            "insert into device_switch_info_log (id, project_id, device_box_id, device_switch_name, address, device_switch_status, switch_electric,",
            "switch_electric_cnt, switch_voltage, switch_temperature, switch_power, switch_leakage, create_time, update_time, remark) values",
            "<foreach collection='deviceSwitchInfoLog' item='item' index='index' seperator=','>",
            "(#{item.id}, #{item.projectId}), #{item.deviceBoxId}, #{item.deviceSwitchName}, #{item.address}, #{item.deviceSwitchStatus}, #{item.switchElectric},",
            "#{item.switchElectriCnt}, #{item.switchVoltage}, #{item.switchTemperature}, #{item.switchPower}, #{item.switchLeakage}, #{item.createTime}, #{item.updateTime}, #{item.remark})",
            "</foreach>",
            "</script>"
    })
    void batchInsert(List<DeviceSwitchInfoLog> deviceSwitchInfoLog);

}
