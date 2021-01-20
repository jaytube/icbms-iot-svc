package com.icbms.iot.enums;

import java.util.Arrays;

public enum AlarmType {

    SHORT_CIRCUIT_ALARM(15, 4, "短路报警"), SURGE_ALARM(14, 3, "浪涌报警"),
    OVERLOAD_ALARM(13, 4, "过载报警"), TEMP_WARNING(12, 3, "温度预警"),
    LEAKAGE_ALARM(11, 4, "漏电报警"), OVER_CURRENT_ALARM(10, 4, "过流报警"),
    OVER_VOLTAGE_ALARM(9, 4, "过压报警"), PROTECTION_NORMAL(8, 2, "漏电保护功能正常"),
    PROTECTION_NOT_COMPLETED(7, 2, "漏电保护自检未完成"), INPUT_PHASE_LOSS_ALARM(6, 3, "输入缺相报警"),
    STRIKE_ALARM(5, 3, "打火报警"), UNDER_VOLTAGE_ALARM(4, 3, "欠压告警"),
    OVER_VOLTAGE_WARNING(3, 3, "过压预警"), UNDER_VOLTAGE_WARNING(2, 2, "欠压预警"),
    LEAKAGE_WARNING(1, 2, "漏电预警"), CURRENT_WARNING(0, 2, "电流预警");

    private int code;
    private int level;
    private String alarmContent;

    AlarmType(int code, int level, String alarmContent) {
        this.code = code;
        this.level = level;
        this.alarmContent = alarmContent;
    }

    public int getCode() {
        return code;
    }

    public int getLevel() {
        return level;
    }

    public String getAlarmContent() {
        return alarmContent;
    }

    public static AlarmType getByCode(int code) {
        return Arrays.stream(AlarmType.values())
                .filter(v -> v.getCode() == code)
                .findAny()
                .orElse(null);
    }
}
