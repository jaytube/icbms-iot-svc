package com.icbms.iot.enums;

import java.util.Arrays;

public enum AlarmType {

    SHORT_CIRCUIT_ALARM(0), SURGE_ALARM(1), OVERLOAD_ALARM(2), TEMP_WARNING(3),
    LEAKAGE_ALARM(4), OVER_CURRENT_ALARM(5), OVER_VOLTAGE_ALARM(6), PROTECTION_NORMAL(7),
    PROTECTION_NOT_COMPLETED(8), INPUT_PHASE_LOSS_ALARM(9), STRIKE_ALARM(10), UNDER_VOLTAGE_ALARM(11),
    OVER_VOLTAGE_WARNING(12), UNDER_VOLTAGE_WARNING(13), LEAKAGE_WARNING(14), CURRENT_WARNING(15);

    private int code;

    AlarmType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static AlarmType getByCode(int code) {
        return Arrays.stream(AlarmType.values())
                .filter(v -> v.getCode() == code)
                .findAny()
                .orElse(null);
    }
}
