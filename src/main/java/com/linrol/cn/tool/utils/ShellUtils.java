package com.linrol.cn.tool.utils;

import java.io.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    public static String workPath;

    public static void init(String path) {
        workPath = path;
    }

    public static boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

    public static Ret cmd(String command) throws Exception{
        try {
            logger.info("exec cmd：{}", command);
            ProcessBuilder builder = new ProcessBuilder().directory(new File(workPath));
            builder.command(isWindows ? "cmd.exe" : "sh", isWindows ? "/c" : "-c", command);
            StreamGobbler streamGobbler = new StreamGobbler(builder.start());
            Future<Ret> future = Executors.newSingleThreadExecutor().submit(streamGobbler);
            return future.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static class StreamGobbler implements Callable<Ret> {
        private Process process;
        public StreamGobbler(Process process) {
            this.process = process;
        }

        @Override
        public Ret call() throws InterruptedException {
            int code = process.waitFor();
            InputStream inputStream = code != 0 ? process.getErrorStream() : process.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);
            String ret = new BufferedReader(reader).lines().collect(Collectors.joining("\n"));
            return Ret.of(code, ret);
        }
    }
}
