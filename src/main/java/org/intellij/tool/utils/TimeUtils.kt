package org.intellij.tool.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {

    static String defaultPattern = "yyyy-MM-dd HH:mm:ss.SSS";

    public static String getCurrentTime(String pattern) {
        if (StringUtils.isBlank(pattern)) {
            pattern = defaultPattern;
        }
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
    }
}
