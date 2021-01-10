package com.icbms.iot.enums;

import java.util.Arrays;

public enum BoxAlarmType {

    SHORT_CIRCUIT_ALARM(0), RETAIN_1(1), OVERLOAD_ALARM(2), TEMP_WARNING(3), RETAIN_4(4),
    OVER_CURRENT_ALARM(5), OVER_VOLTAGE_ALARM(6), RETAIN_7(7), RETAIN_8(8), INPUT_PHASE_LOSS_ALARM(9),
    STRIKE_ALARM(10), UNDER_VOLTAGE_ALARM(11), OVER_VOLTAGE_WARNING(12),
    UNDER_VOLTAGE_WARNING(13), RETAIN_14(14), CURRENT_WARNING(15);

    private int code;

    BoxAlarmType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static BoxAlarmType getByCode(int code) {
        return Arrays.stream(BoxAlarmType.values())
                .filter(v -> v.getCode() == code)
                .findAny()
                .orElse(null);
    }
}
