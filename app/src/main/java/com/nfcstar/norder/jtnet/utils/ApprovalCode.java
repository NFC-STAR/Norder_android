package com.nfcstar.norder.jtnet.utils;


import net.jt.pos.sdk.JTNetRequestCode;

public enum ApprovalCode implements RequestCode {
    CREDIT_START(JTNetRequestCode.신용승인요청, "1010", "신용승인"),
    CREDIT_BL(JTNetRequestCode.신용승인요청, "1070", "신용카드 발급사 체크"),
    CREDIT_CANCEL(JTNetRequestCode.신용취소요청, "1050", "신용취소"),
    CASH_START(JTNetRequestCode.현금승인요청, "5010", "현금승인"),
    CASH_CANCEL(JTNetRequestCode.현금취소요청, "5050", "현금취소"),
    MEMBERSHIP_START(JTNetRequestCode.맴버쉽승인요청, "4010", "맴버쉽승인"),
    MEMBERSHIP_CANCEL(JTNetRequestCode.맴버쉽취소요청, "4050", "맴버쉽취소"),
    MEMBERSHIP_INQUIRY(JTNetRequestCode.맴버쉽조회요청, "4080", "맴버쉽조회"),
    CASH_IC_START(JTNetRequestCode.현금IC구매요청, "8080", "현금IC구매"),
    CASH_IC_CANCEL(JTNetRequestCode.현금IC환불요청, "8081", "현금IC환불"),
    CASH_IC_INQUIRY(JTNetRequestCode.현금IC조회요청, "8084", "현금IC조회"),
    POINT_SAVE(JTNetRequestCode.포인트적립승인요청, "3010", "포인트적립승인요청"),
    POINT_USE(JTNetRequestCode.포인트사용승인요청, "3011", "포인트사용승인요청"),
    POINT_SAVE_CANCEL(JTNetRequestCode.포인트적립취소요청, "3050", "포인트적립취소요청"),
    POINT_USE_CANCEL(JTNetRequestCode.포인트사용취소요청, "3051", "포인트사용취소요청"),
    POINT_INQUIRY(JTNetRequestCode.포인트조회요청, "3080", "포인트조회요청"),
    CHECK_INQUIRY(JTNetRequestCode.수표조회, "6080", "수표조회"),
    STORE_DOWN(JTNetRequestCode.가맹점다운요청, "8071", "가맹점다운"),
    ZEROPAY_AUTH(JTNetRequestCode.제로페이구매요청, "8050" ,"제로페이구매요청" ),
    ZEROPAY_CANCEL(JTNetRequestCode.제로페이환불요청, "8051" ,"제로페이환불요청" ),
    ZEROPAY_CHECK(JTNetRequestCode.제로페이환불조회,"8053","제로페이환불조회"),
    QRCODE_AUTH(JTNetRequestCode.QR_Code승인_취소,"1014","QR 승인/취소"),
    KAKAO_AUTH_INQUIRY(JTNetRequestCode.카카오승인,"8040","카카오승인"),
    KAKAO_CANCEL_INQUIRY(JTNetRequestCode.카카오취소,"8041","카카오취소");

    private int requestCode;
    private String code;
    private String msg;

    ApprovalCode(int requestCode, String code, String msg) {
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

    public static ApprovalCode getApproval(String msg) {
        for (ApprovalCode approvalCode : ApprovalCode.values()) {
            if (approvalCode.msg.equals(msg)) {
                return approvalCode;
            }
        }
        return null;
    }

    public static String[] getApprovalArr() {
        int length = ApprovalCode.values().length;

        String[] strings = new String[length];

        for (int i = 0; i < length; i++) {
            strings[i] = ApprovalCode.values()[i].msg;
        }

        return strings;
    }
}
