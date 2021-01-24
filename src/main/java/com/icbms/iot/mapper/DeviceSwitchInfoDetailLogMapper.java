package com.icbms.iot.mapper;

import com.icbms.iot.entity.DeviceSwitchInfoDetailLog;
import org.apache.ibatis.annotations.Insert;

import java.util.List;

public interface DeviceSwitchInfoDetailLogMapper {

    @Insert({
            "<script>",
            "insert into device_switch_info_detail_log (id, device_switch_info_log_id, project_id, device_box_id, address, switch_voltage_a, switch_voltage_b,",
            "switch_voltage_c, switch_electric_a, switch_electric_b, switch_electric_c, switch_electric_n, switch_power_a, switch_power_b, switch_power_c, create_time, update_time, remark) values",
            "<foreach collection='list' item='item' index='index' seperator=','>",
            "(#{item.id}, #{item.deviceSwitchInfoLogId}), #{item.projectId}, #{item.deviceBoxId}, #{item.address}, #{item.switchVoltageA}, #{item.switchVoltageB}, #{item.switchVoltageC}, ",
            "#{item.switchElectricA}, #{item.switchElectricB}, #{item.switchVoltageC}, #{item.switchElectricN}, #{item.switchPowerA}, #{item.switchPowerB}, #{item.switchPowerC}, #{item.createTime}, #{item.updateTime}, #{item.remark})",
            "</foreach>",
            "</script>"
    })
    void batchInsert(List<DeviceSwitchInfoDetailLog> list);

}
