package org.intellij.tool.utils;

public class Ret {
    private int code;

    private String ret;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getRet() {
        return ret;
    }

    public void setRet(String ret) {
        this.ret = ret;
    }

    public static Ret of(int code, String msg) {
        Ret ret = new Ret();
        ret.setCode(code);
        ret.setRet(msg);
        return ret;
    }
}
