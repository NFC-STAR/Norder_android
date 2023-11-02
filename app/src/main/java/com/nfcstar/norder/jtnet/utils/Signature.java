package com.nfcstar.norder.jtnet.utils;

public enum Signature {
    USE("Y", "사용"),
    NONE("N", "미사용"),
    REUSE("R", "재사용"),
    AUTO(" ", "데몬판단처리");

    private String code;
    private String msg;

    Signature(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static Signature getSignature(String msg) {
        for (Signature signature : Signature.values()) {
            if (signature.msg.equals(msg)) {
                return signature;
            }
        }
        return null;
    }

    public static String[] getSignatureArr() {
        int length = Signature.values().length;

        String[] strings = new String[length];

        for (int i = 0; i < length; i++) {
            strings[i] = Signature.values()[i].msg;
        }

        return strings;
    }
}
