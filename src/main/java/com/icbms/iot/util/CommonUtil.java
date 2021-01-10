package com.icbms.iot.util;

import com.icbms.iot.client.MqttPushClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

public class CommonUtil {
    private static final Logger logger = LoggerFactory.getLogger(MqttPushClient.class);

    public static byte[] intToBytes(int value) {
        byte[] byte_src = new byte[]{(byte) (value & 255), (byte) ((value & '\uff00') >> 8), (byte) ((value & 16711680) >> 16), (byte) ((value & -16777216) >> 24)};
        return byte_src;
    }

    public static byte[] short2Byte(short a) {
        byte[] b = new byte[]{(byte) (a >> 8), (byte) a};
        return b;
    }

    public static int highAndLowAddressSwap(int a) {
        int b = (a & 255) << 24;
        int c = (a & '\uff00') << 8;
        int d = (a & 16711680) >> 8;
        int e = (a & -16777216) >> 24;
        return b + c + d + e;
    }

    public static String formatDouble(double i) {
        String result = String.format("%.3f", i);
        return result;
    }

    public static String format(String pattern, Map<String, Object> arguments) {
        String formatedStr = pattern;
        Iterator var4 = arguments.keySet().iterator();

        while (var4.hasNext()) {
            String key = (String) var4.next();
            String replacement = "\\{:" + key + "\\}";
            formatedStr = formatedStr.replaceAll(replacement, arguments.get(key).toString());
            System.out.println(replacement + arguments.get(key).toString());
        }

        return formatedStr;
    }

    public static synchronized String getTimeStamp() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Date date = new Date();
        return formatter.format(date);
    }

    public static void printStacktrace(StackTraceElement[] stackTrackElements) {
        logger.error(Arrays.toString(stackTrackElements));
    }

    public static float getCpuInfo() {
        File file = new File("/proc/stat");
        BufferedReader br = null;

        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            StringTokenizer token = new StringTokenizer(br.readLine());
            token.nextToken();
            long user1 = Long.parseLong(token.nextToken());
            long nice1 = Long.parseLong(token.nextToken());
            long sys1 = Long.parseLong(token.nextToken());
            long idle1 = Long.parseLong(token.nextToken());
            Thread.sleep(1000L);
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            token = new StringTokenizer(br.readLine());
            token.nextToken();
            long user2 = Long.parseLong(token.nextToken());
            long nice2 = Long.parseLong(token.nextToken());
            long sys2 = Long.parseLong(token.nextToken());
            long idle2 = Long.parseLong(token.nextToken());
            float var20 = (float) (user2 + sys2 + nice2 - (user1 + sys1 + nice1)) / (float) (user2 + nice2 + sys2 + idle2 - (user1 + nice1 + sys1 + idle1));
            return var20;
        } catch (FileNotFoundException var32) {
            var32.printStackTrace();
        } catch (IOException var33) {
            var33.printStackTrace();
        } catch (InterruptedException var34) {
            var34.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException var31) {
                var31.printStackTrace();
            }

        }

        return 0.0F;
    }

    public static String getLocalIp() {
        String localip = null;

        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            boolean finded = false;

            while (netInterfaces.hasMoreElements() && !finded) {
                NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();
                Enumeration address = ni.getInetAddresses();

                while (address.hasMoreElements()) {
                    ip = (InetAddress) address.nextElement();
                    if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {
                        localip = ip.getHostAddress();
                    }
                }
            }
        } catch (SocketException var6) {
            var6.printStackTrace();
        }

        return localip;
    }

    public static Date dateMulti(Date date, int milliseconds) {
        Date newdate = new Date(date.getTime() + (long) milliseconds);
        return newdate;
    }

    public static int getWeekOfDate(Date date) {
        int[] weekDaysCode = new int[]{1, 2, 3, 4, 5, 6, 7};
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int intWeek = calendar.get(7) - 1;
        return weekDaysCode[intWeek];
    }

    public static String getWeekNameOfDate(Date date) {
        String[] weekDaysName = new String[]{"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int intWeek = calendar.get(7) - 1;
        return weekDaysName[intWeek];
    }

    public static long getNextDateOfMonth(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        Date date = new Date(timestamp);
        calendar.setTime(date);
        calendar.add(2, 1);
        return calendar.getTimeInMillis();
    }

    public static long getLastDateOfMonth(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        Date date = new Date(timestamp);
        calendar.setTime(date);
        calendar.add(2, -1);
        return calendar.getTimeInMillis();
    }

    public static long getDateOfCurrentMonth(long timestamp) {
        String strDate = parseDate(timestamp);
        String strTime = strDate.substring(8);
        String strDate2 = parseDate(System.currentTimeMillis());
        String strTime2 = strDate2.substring(0, 8);
        return parseDate(strTime2 + strTime).getTime();
    }

    public static long getDateOfWeek(long startTime, int nDate) {
        long lstartTime = getCurrTimestampFromHistoryTimestamp(startTime);
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(lstartTime));
        c.set(7, nDate);
        Date newDate = c.getTime();
        if (c.getTime().getTime() < System.currentTimeMillis()) {
            long l = c.getTime().getTime() + 604800000L;
            newDate = new Date(l);
        }

        return newDate.getTime();
    }

    public static String formatMilliSecond(long startDateTime, long endDateTime) {
        long between = (endDateTime - startDateTime) / 1000L;
        long day1 = between / 86400L;
        long hour1 = between % 86400L / 3600L;
        long minute1 = between % 3600L / 60L;
        String msg = "";
        if (day1 > 0L) {
            msg = msg + day1 + "天";
        }

        if (hour1 > 0L) {
            msg = msg + hour1 + "小时";
        }

        if (minute1 > 0L) {
            msg = msg + minute1 + "分钟";
        }

        return msg;
    }

    public static long getNextDaySecond(Date date, int hour, int minute) {
        long between = 0L;
        SimpleDateFormat formatter1 = new SimpleDateFormat("HH");
        SimpleDateFormat formatter2 = new SimpleDateFormat("mm");
        int hh = Integer.parseInt(formatter1.format(date));
        int mm = Integer.parseInt(formatter2.format(date));
        between = (long) ((hour - hh) * 60 * 60 + (minute - mm) * 60);
        if (between <= 0L) {
            between += 86400L;
        }

        return between;
    }

    public static long getNextHourSecond(Date date) {
        long second = 0L;
        SimpleDateFormat formatter1 = new SimpleDateFormat("mm");
        SimpleDateFormat formatter2 = new SimpleDateFormat("ss");
        int mm = Integer.parseInt(formatter1.format(date));
        int ss = Integer.parseInt(formatter2.format(date));
        second = (long) (3600 - mm * 60 - ss);
        return second;
    }

    public static Date strToDate(String strDate) {
        if (strDate != null && !"".equals(strDate)) {
            SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd");
            ParsePosition pos = new ParsePosition(0);
            Date strtodate = formatter.parse(strDate, pos);
            return strtodate;
        } else {
            return new Date();
        }
    }

    public static String getSpecifiedDayAfter(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int day = c.get(5);
        c.set(5, day + 1);
        String dayBefore = (new SimpleDateFormat("yyyy-MM-dd")).format(c.getTime());
        return dayBefore;
    }

    public static Date getDateAddition(Date date, int num) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int day = c.get(5);
        c.set(5, day + num);
        return c.getTime();
    }

    public static String getSpecifiedDayBefore(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int day = c.get(5);
        c.set(5, day - 1);
        String dayBefore = (new SimpleDateFormat("yyyy-MM-dd")).format(c.getTime());
        return dayBefore;
    }

    public static String getYestoday(Date date, String formatter) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int day = c.get(5);
        c.set(5, day - 1);
        String dayBefore = (new SimpleDateFormat(formatter)).format(c.getTime());
        return dayBefore;
    }

    public static long getCurrTimestampFromHistoryTimestamp(long lDate) {
        long timestamp = 0L;
        if (lDate != 0L) {
            Date date = new Date(lDate);
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat format2 = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String sCurrentDate = format1.format(new Date());
            String sMatchStartDate = format2.format(date);

            try {
                date = sdf.parse(sCurrentDate + " " + sMatchStartDate);
                timestamp = date.getTime();
            } catch (ParseException var11) {
                var11.printStackTrace();
            }
        }

        return timestamp;
    }

    public static long getLatestGameStartTime(long l1, long l2, int nCycle, int nBigCycle) {
        long cur = System.currentTimeMillis();
        int n = (int) (cur - l1) / nCycle;
        int dist = nCycle - (int) (cur - l1) % nCycle;
        long latest = l1;
        System.out.println(dist);
        if (cur > l2) {
            return l1 + (long) nBigCycle;
        } else if (cur < l1) {
            return l1;
        } else {
            if (dist >= 120000) {
                if (l2 - cur > (long) nCycle) {
                    latest = l1 + (long) ((n + 1) * nCycle);
                } else {
                    System.out.println(1);
                    latest = l2;
                }
            } else if (l2 - cur >= (long) nCycle) {
                latest = l1 + (long) ((n + 2) * nCycle);
            } else if (l2 - cur > 0L && l2 - cur < (long) nCycle) {
                latest = l1 + (long) ((n + 2) * nCycle);
            }

            return latest;
        }
    }

    public static boolean isTheSameDay(long l1, long l2) {
        if (l1 != 0L && l2 != 0L) {
            Date d1 = new Date(l1);
            Date d2 = new Date(l2);
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
            String s1 = sf.format(d1);
            String s2 = sf.format(d2);
            return s1.equals(s2);
        } else {
            return false;
        }
    }

    public static Date strToDate(String strDate, String format) {
        if (strDate != null && !"".equals(strDate)) {
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            ParsePosition pos = new ParsePosition(0);
            Date strtodate = formatter.parse(strDate, pos);
            return strtodate;
        } else {
            return new Date();
        }
    }

    public static Date parseDate(String strDate) {
        if (strDate != null && !"".equals(strDate)) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            ParsePosition pos = new ParsePosition(0);
            Date strtodate = formatter.parse(strDate, pos);
            return strtodate;
        } else {
            return new Date();
        }
    }

    public static Date parseDate(String strDate, String format) {
        if (strDate != null && !"".equals(strDate)) {
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            ParsePosition pos = new ParsePosition(0);
            Date strtodate = formatter.parse(strDate, pos);
            return strtodate;
        } else {
            return null;
        }
    }

    public static String formatDate(Date date, String formater) {
        SimpleDateFormat formatter = new SimpleDateFormat(formater);
        return formatter.format(date);
    }

    public static String parseDate(long dateTime) {
        if (dateTime == 0L) {
            return "";
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return formatter.format(new Date(dateTime));
        }
    }

    public static int byteArrayToInt(byte[] bytes) {
        int nResult = 0;
        int nTemp = bytes[0] & 255;
        nTemp <<= 24;
        nResult = nResult | nTemp;
        nTemp = bytes[1] & 255;
        nTemp <<= 16;
        nResult |= nTemp;
        nTemp = bytes[2] & 255;
        nTemp <<= 8;
        nResult |= nTemp;
        nTemp = bytes[3] & 255;
        nResult |= nTemp;
        return nResult;
    }

    public static byte[] intToByteArray(int integer) {
        int byteNum = (40 - Integer.numberOfLeadingZeros(integer < 0 ? ~integer : integer)) / 8;
        byte[] byteArray = new byte[4];

        for (int n = 0; n < byteNum; ++n) {
            byteArray[3 - n] = (byte) (integer >>> n * 8);
        }

        return byteArray;
    }

    public static String dateToStr(Date dateDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(dateDate);
        return dateString;
    }

    public static String timestampToStr(long n) {
        Date date = new Date(n);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }

    public static String timestampToStr2(long n) {
        Date date = new Date(n);
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd HH:mm");
        return formatter.format(date);
    }

    public static String timestampToStr3(long n) {
        Date date = new Date(n);
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd ");
        return formatter.format(date);
    }

    public static int parseInt(String s) {
        try {
            return s != null && !"".equals(s) ? (int) Double.parseDouble(s) : 0;
        } catch (Exception var2) {
            return 0;
        }
    }

    public static double parseDouble(String s) {
        try {
            return s != null && !"".equals(s) ? Double.parseDouble(s) : 0.0D;
        } catch (Exception var2) {
            return 0.0D;
        }
    }

    public static float parseFloat(String s) {
        try {
            return s != null && !"".equals(s) ? Float.parseFloat(s) : 0.0F;
        } catch (Exception var2) {
            return 0.0F;
        }
    }

    public static short parseShort(String s) {
        try {
            return s != null && !"".equals(s) ? (short) ((int) Double.parseDouble(s)) : 0;
        } catch (Exception var2) {
            return 0;
        }
    }

    public static byte parseByte(String s) {
        try {
            return s != null && !"".equals(s) ? (byte) ((int) Double.parseDouble(s)) : 0;
        } catch (Exception var2) {
            return 0;
        }
    }

    public static long parseLong(String s) {
        try {
            return s != null && !"".equals(s) ? (long) Double.parseDouble(s) : 0L;
        } catch (Exception var2) {
            return 0L;
        }
    }

    public static boolean getProbability(int probability) {
        Random random = new Random();
        int ran = Math.abs(random.nextInt()) % 100;
        return ran < probability;
    }

    public static int getProbability(List<Integer> probability) {
        Random random = new Random();
        int ran = random.nextInt(100);
        int min = 0;
        int max = 0;

        for (Iterator iterator = probability.iterator(); iterator.hasNext(); min += max) {
            Integer integer = (Integer) iterator.next();
            max += integer;
            if (min <= ran && ran < max) {
                return integer;
            }
        }

        return 0;
    }

    public static int getRangeRandom(int minRange, int maxRange) {
        Random random = new Random();
        int n = random.nextInt(maxRange - minRange + 1) + minRange;
        return n;
    }

    public static Object converce(Object fatherObj, Object childObj) {
        Field[] fatherFields = fatherObj.getClass().getFields();
        Field[] childFields = childObj.getClass().getFields();

        try {
            Field[] var7 = fatherFields;
            int var6 = fatherFields.length;

            for (int var5 = 0; var5 < var6; ++var5) {
                Field fatherField = var7[var5];
                Field[] var11 = childFields;
                int var10 = childFields.length;

                for (int var9 = 0; var9 < var10; ++var9) {
                    Field childField = var11[var9];
                    if (childField.getName().equalsIgnoreCase(fatherField.getName())) {
                        childField.set(childObj, fatherField.get(fatherObj));
                    }
                }
            }

            return childObj;
        } catch (Exception var12) {
            return childObj;
        }
    }

    public static String conventNumber(int num) {
        String str = "";
        String strNum = String.valueOf(num);
        int strLen = strNum.length();
        String[] cn = new String[]{"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
        String[] dw = new String[]{"十", "百", "千", "万", "十万", "百万", "千万", "亿", "十亿"};
        if (strLen == 1) {
            return cn[num];
        } else {
            int zeroCnt = 0;

            for (int i = 0; i < strLen; ++i) {
                int n = Integer.parseInt(strNum.substring(i, i + 1));
                if (strLen - i > 0) {
                    if (n == 0) {
                        ++zeroCnt;
                    } else {
                        if (zeroCnt > 0) {
                            str = str + cn[0];
                        }

                        zeroCnt = 0;
                        str = str + cn[n];
                    }

                    if (strLen - 2 - i >= 0 && n > 0) {
                        str = str + dw[strLen - 2 - i];
                    }
                }
            }

            return str;
        }
    }

    public static String getNextDay(String strTime) {
        Calendar cal = Calendar.getInstance();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try {
            date = sdf.parse(strTime);
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        cal.setTime(date);
        cal.add(5, 1);
        strTime = sdf.format(cal.getTime());
        return strTime;
    }

    public static long timeDelay(long time, int type, int value) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.add(type, value);
        return calendar.getTimeInMillis();
    }

    public static String getVersionStr(int nVersionCode) {
        int nMainVer = nVersionCode >> 24;
        int nSubVer = (nVersionCode & 16711680) >> 16;
        return nMainVer + "." + nSubVer;
    }

    public static String getVersion(int nVersionCode) {
        int nMainVer = nVersionCode >> 24;
        int nSubVer = (nVersionCode & 16711680) >> 16;
        return nSubVer < 10 ? nMainVer + "0" + nSubVer : String.valueOf(nMainVer) + nSubVer;
    }

    public static String getVersionWithPoint(int nVersionCode) {
        int nMainVer = nVersionCode >> 24;
        int nSubVer = (nVersionCode & 16711680) >> 16;
        return nSubVer < 10 ? nMainVer + ".0" + nSubVer : nMainVer + "." + nSubVer;
    }

    public static int[] byteArray2IntArray(byte[] ab) {
        int[] an = new int[ab.length];

        for (int i = 0; i < an.length; ++i) {
            an[i] = ab[i];
        }

        return an;
    }

    public static byte[] intArray2ByteArray(int[] an) {
        byte[] ab = new byte[an.length];

        for (int i = 0; i < an.length; ++i) {
            ab[i] = (byte) an[i];
        }

        return ab;
    }

    public static int versionStr2VersionCode(String versionStr) {
        if (versionStr != null && !"".equals(versionStr)) {
            versionStr = versionStr.replace(".", "_");
            String[] versionNumArray = versionStr.split("_");
            int[] codeArr = new int[versionNumArray.length];

            for (int i = 0; i < versionNumArray.length; ++i) {
                codeArr[i] = Integer.parseInt(versionNumArray[i]);
            }

            if (codeArr.length == 2) {
                return (codeArr[0] << 24) + (codeArr[1] << 16);
            } else {
                return codeArr.length == 4 ? (codeArr[0] << 24) + (codeArr[1] << 16) + (codeArr[2] << 8) + codeArr[3] : 0;
            }
        } else {
            return 0;
        }
    }

    public static String versionCode2VersionStr(int version) {
        return (version >> 24 & 255) + "." + (version >> 16 & 255) + "." + (version >> 8 & 255) + "." + (version & 255);
    }

    public static long getCurrMondayTimeStamp() {
        Calendar clNow = Calendar.getInstance();
        int n = clNow.get(3);
        System.out.println(n);
        Calendar cl = Calendar.getInstance();
        cl.setTimeInMillis(0L);
        cl.set(1, clNow.get(1));
        cl.set(2, clNow.get(2));
        cl.set(5, clNow.get(5));
        cl.set(10, 0);
        Calendar cl2 = Calendar.getInstance();
        cl2.setTimeInMillis(cl.getTimeInMillis());
        cl2.set(7, 2);
        return cl2.getTimeInMillis();
    }

    public static int byteToInt(byte b) {
        return b & 255;
    }

    public static short getShort(byte[] b, int index) {
        return (short) (b[index + 1] & 255 | b[index + 0] << 8);
    }

    public static int getInt(byte[] bb, int index) {
        return (bb[index + 3] & 255) << 24 | (bb[index + 2] & 255) << 16 | (bb[index + 1] & 255) << 8 | (bb[index + 0] & 255) << 0;
    }

    public static long getLong(byte[] bb, int index) {
        return ((long) bb[index + 7] & 255L) << 56 | ((long) bb[index + 6] & 255L) << 48 | ((long) bb[index + 5] & 255L) << 40 | ((long) bb[index + 4] & 255L) << 32 | ((long) bb[index + 3] & 255L) << 24 | ((long) bb[index + 2] & 255L) << 16 | ((long) bb[index + 1] & 255L) << 8 | ((long) bb[index + 0] & 255L) << 0;
    }

    public static float getFloat(byte[] b, int index) {
        int l = b[index + 0];
        l = l & 255;
        l = (int) ((long) l | (long) b[index + 1] << 8);
        l &= 65535;
        l = (int) ((long) l | (long) b[index + 2] << 16);
        l &= 16777215;
        l = (int) ((long) l | (long) b[index + 3] << 24);
        return Float.intBitsToFloat(l);
    }

    public static double getDouble(byte[] b, int index) {
        long l = (long) b[0];
        l &= 255L;
        l |= (long) b[1] << 8;
        l &= 65535L;
        l |= (long) b[2] << 16;
        l &= 16777215L;
        l |= (long) b[3] << 24;
        l &= 4294967295L;
        l |= (long) b[4] << 32;
        l &= 1099511627775L;
        l |= (long) b[5] << 40;
        l &= 281474976710655L;
        l |= (long) b[6] << 48;
        l &= 72057594037927935L;
        l |= (long) b[7] << 56;
        return Double.longBitsToDouble(l);
    }

    public static String byteToBit(byte b) {
        return "" + (byte) (b >> 7 & 1) + (byte) (b >> 6 & 1) + (byte) (b >> 5 & 1) + (byte) (b >> 4 & 1) + (byte) (b >> 3 & 1) + (byte) (b >> 2 & 1) + (byte) (b >> 1 & 1) + (byte) (b >> 0 & 1);
    }

    public static int bytesToInt(byte[] src, int offset) {
        int value = src[offset] & 255 | (src[offset + 1] & 255) << 8 | (src[offset + 2] & 255) << 16 | (src[offset + 3] & 255) << 24;
        return value;
    }

    public static int bytesToInt2(byte[] src, int offset) {
        int value = (src[offset] & 255) << 24 | (src[offset + 1] & 255) << 16 | (src[offset + 2] & 255) << 8 | src[offset + 3] & 255;
        return value;
    }

    public static int bytesToInt3(byte[] src, int offset) {
        int value = src[offset + 1] & 255 | (src[offset] & 255) << 8 | (src[offset + 3] & 255) << 16 | (src[offset + 2] & 255) << 24;
        return value;
    }

    public static byte[] shortToByte(short number) {
        int temp = number;
        byte[] b = new byte[2];

        for (int i = 0; i < b.length; ++i) {
            b[i] = (new Integer(temp & 255)).byteValue();
            temp >>= 8;
        }

        return b;
    }

    public static String uuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < bytes.length; ++i) {
            sb.append("0123456789ABCDEF".charAt(bytes[i] >> 4 & 15));
            sb.append("0123456789ABCDEF".charAt(bytes[i] & 15)).append(" ");
        }

        return sb.toString().trim();
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }


    public static void main(String[] args) {
        int a = highAndLowAddressSwap(469762048);
        System.out.println(a);
        byte[] hx = new byte[]{1, 1};
        System.out.println(bytesToHex(hx));
    }
}
