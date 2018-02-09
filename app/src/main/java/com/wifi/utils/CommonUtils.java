package com.wifi.utils;

import java.util.regex.Pattern;

/**
 * Created by 17993 on 2018/2/4.
 * 工具方法类
 */

public class CommonUtils {
    /**
     * 判断输入的是否为数字
     * @param str
     * @return
     */
    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    /**
     * 判断是否是IPV4地址
     * @param ip
     * @return
     */
    public static boolean isValidIP(String ip) {
        final String DELIM = "\\.";
        if(ip == null || "".equals(ip.trim())) {
            return false;
        }
        String[] parts = ip.split(DELIM);

        if(parts.length != 4) {
            return false;
        }
        for(String part : parts) {
            try {
                int intVal = Integer.parseInt(part);
                if(intVal < 0 || intVal > 255) {
                    return false;
                }

            } catch(NumberFormatException nfe) {
                return false;
            }
        }
        return true;
    }
}
