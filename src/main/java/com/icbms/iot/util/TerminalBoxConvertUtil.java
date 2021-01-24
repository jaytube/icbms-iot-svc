package com.icbms.iot.util;

import org.apache.commons.lang3.StringUtils;

import static com.icbms.iot.constant.IotConstant.BOX_NO_START_INDEX;
import static com.icbms.iot.constant.IotConstant.BOX_NO_START_STRING;

public class TerminalBoxConvertUtil {

    public static String getTerminalNo(String boxNo) {
        if(StringUtils.isBlank(boxNo) || boxNo.length() != 12)
            return "";

        String terminalNo = boxNo.substring(BOX_NO_START_INDEX).replaceAll("^0+", "");
        return terminalNo;
    }

    public static String getTerminalString(String terminalId) {
        if (terminalId != null && !"".equals(terminalId)) {
            int len = terminalId.length();
            if (len == 1) {
                return BOX_NO_START_STRING + "00000" + terminalId;
            } else if (len == 2) {
                return BOX_NO_START_STRING + "0000" + terminalId;
            } else if (len == 3) {
                return BOX_NO_START_STRING + "000" + terminalId;
            } else if (len == 4) {
                return BOX_NO_START_STRING + "00" + terminalId;
            } else {
                return len == 5 ? BOX_NO_START_STRING + "0" + terminalId : BOX_NO_START_STRING + terminalId;
            }
        } else {
            return null;
        }
    }
}
