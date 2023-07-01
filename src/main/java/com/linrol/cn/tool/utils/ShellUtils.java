package com.linrol.cn.tool.utils;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description
 * @ClassName ShellUtilss
 * @Author linrol
 * @date 2021年09月23日 10:02 Copyright (c) 2020, linrol@77hub.com All Rights Reserved.
 */
public class ShellUtils {

    private static final Logger logger = LoggerFactory.getLogger(ShellUtils.class);

    public static Ret cmd(String command) throws Exception{
        try {
            logger.info("exec shell command：{}", command);
            Process process = Runtime.getRuntime().exec(command);
            int code = process.waitFor();
            String msg = execOutput(process);
            return Ret.of(code, msg);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static String execOutput(Process process) throws Exception {
        if (process == null) {
            return null;
        }
        InputStreamReader ir = new InputStreamReader(process.getInputStream());
        LineNumberReader input = new LineNumberReader(ir);
        String line;
        StringBuilder output = new StringBuilder();
        while ((line = input.readLine()) != null) {
            output.append(line).append("\n");
        }
        input.close();
        ir.close();
        if (output.length() > 0) {
            return output.toString();
        }
        return null;
    }
}
