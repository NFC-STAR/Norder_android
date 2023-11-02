package com.nfcstar.norder.jtnet.utils;


import net.jt.pos.sdk.JTNetRequestCode;

public enum AbilityCode implements RequestCode {
    POS_NUM(JTNetRequestCode.식별번호서명패드요청, "PC", "식별번호요청"),
    KEY_EXCHANGE(JTNetRequestCode.키교환요청, "KC", "키교환요청"),
    SIGN(JTNetRequestCode.서명요청, "PS", "서명요청"),
    PRETRANSACTION(JTNetRequestCode.전거래요청,"PT","전거래데이터요청"),
    CARD_CANCEL(JTNetRequestCode.장치명령취소,"CC","장치명령취소"),
    IC_CHECK(JTNetRequestCode.리더기제어상태체크요청,"IS","리더기제어상태요청");

    private int requestCode;
    private String code;
    private String msg;

    AbilityCode(int requestCode, String code, String msg) {
        this.requestCode = requestCode;
        this.code = code;
        this.msg = msg;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
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

    public static AbilityCode getFunction(String msg) {
        for (AbilityCode abilityCode : AbilityCode.values()) {
            if (abilityCode.msg.equals(msg)) {
                return abilityCode;
            }
        }
        return null;
    }

    public static String[] getFunctionArr() {
        int length = AbilityCode.values().length;

        String[] strings = new String[length];

        for (int i = 0; i < length; i++) {
            strings[i] = AbilityCode.values()[i].msg;
        }

        return strings;
    }
}
