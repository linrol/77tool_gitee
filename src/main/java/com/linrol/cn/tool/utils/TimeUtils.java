package com.linrol.cn.tool.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.linrol.cn.tool.utils.StringUtils.isBlank;

public class TimeUtils {

    static String defaultPattern = "yyyy-MM-dd HH:mm:ss.SSS";

    public static String getCurrentTime(String pattern) {
        if (isBlank(pattern)) {
            pattern = defaultPattern;
        }
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
    }
}
