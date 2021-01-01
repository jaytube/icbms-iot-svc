package com.icbms.iot.mapper;

import com.icbms.iot.entity.DeviceSwitchInfoLog;
import org.apache.ibatis.annotations.Insert;

public interface DeviceSwitchInfoLogMapper {

    @Insert("INSERT INTO device_switch_info_log (id, project_id, device_box_id, device_switch_name, address, device_switch_status, switch_electric" +
            ", switch_electric_cnt, switch_voltage, switch_temperature, switch_power, switch_leakage, create_time, update_time, remark) " +
            "values(#{id}, #{projectId}, #{deviceBoxId}, #{deviceSwitchName}, #{address}, #{deviceSwitchStatus}, #{switchElectric}, #{switchElectricCnt}" +
            ", #{switchVoltage}, #{switchTemperature}, #{switchPower}, #{switchLeakage}, #{createTime}, #{updateTime}, #{remark})")
    void insert(DeviceSwitchInfoLog deviceSwitchInfoLog);

}
