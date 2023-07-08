package org.intellij.tool.utils;

public class StringUtils {

    public static boolean isBlank(CharSequence cs) {
        int strLen = cs == null ? 0 : cs.length();
        if (strLen == 0) {
            return true;
        } else {
            for(int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }

            return true;
        }
    }
}
