package com.nfcstar.norder.jtnet;

import android.util.Log;

import com.nfcstar.norder.jtnet.utils.ApprovalCode;
import com.nfcstar.norder.jtnet.utils.Signature;
import com.nfcstar.norder.jtnet.utils.StringUtil;


public class JtnetClass {

    static public final String 신용승인 = "신용승인";
    static public final String 신용취소 = "신용취소";
    String deviecID,gubcod, cancelDate,cancelAppno, cancelUnino;
    int amount, inmonths, requestCode;
    boolean isCancel;

    public JtnetClass(ApprovalCode approvalCode, String deviecID, int amount, int inmonths) {
        this.deviecID = deviecID;
        this.gubcod = approvalCode.getCode();
        requestCode = approvalCode.getRequestCode();
        this.amount = amount;
        this.inmonths = inmonths;
        isCancel = false;
    }

    public JtnetClass(ApprovalCode approvalCode, String deviecID, int amount, int inmonths, String cancelDate, String cancelAppno, String cancelUnino) {
        this.deviecID = deviecID;
        this.gubcod = approvalCode.getCode();
        requestCode = approvalCode.getRequestCode();
        this.cancelDate = cancelDate;
        this.cancelAppno = cancelAppno;
        this.cancelUnino = cancelUnino;
        this.amount = amount;
        this.inmonths = inmonths;
        isCancel = true;
    }

    public int getRequestCode() {
        return requestCode;
    }

    void jtLog(String aa, byte[] arrBytes) {
        Log.e(aa, "/"+ StringUtil.byteArrayToString(arrBytes) + "|");
    }
    void jtLog(String aa, String arrBytes) {
        Log.e(aa, "/"+arrBytes + "|");
    }

    /**
     * 신용 전문
     */
    public byte[] createCreditArr() {
        byte[] code = gubcod.getBytes();
        return new CreditApproval(
                code,
                getDeviceId(deviecID),
                getWCC(),
                getTrack2(),
                getIMonths(inmonths),
                getDealAmount(amount),
                getTax(),
                getSvcCharge(),
                getOrgDealDt(isCancel,cancelDate),
                getOrgApprovalNo(isCancel,cancelAppno),
                getOrgUniqueNo(isCancel,cancelUnino),
                getSignature()
        ).create();
    }

    /**
     * 단말기 ID
     */
    private byte[] getDeviceId(String deviceID) {
        if (deviceID != null) {
            jtLog("단말기 ID", StringUtil.getRPadSpace(10, deviceID));
            jtLog("단말기 ID", StringUtil.getRPadSpace(10, deviceID).getBytes());
            return StringUtil.getRPadSpace(10, deviceID).getBytes();
        }
        jtLog("단말기 ID", StringUtil.getRPadSpace(10, "").getBytes());
        return StringUtil.getRPadSpace(10, "").getBytes();
    }

    /**
     * 카드 입력 방식
     */
    private byte[] getWCC() {

        jtLog("카드 입력 방식", StringUtil.getRPadSpace(1, ""));
        jtLog("카드 입력 방식", StringUtil.getRPadSpace(1, "").getBytes());
        return StringUtil.getRPadSpace(1, "").getBytes();
    }
    /**
     * 카드번호
     */
    private byte[] getTrack2() {
        // private byte[] getTrack2(boolean isKeyIn) {
        //if (mainListener != null && isKeyIn) {
        //    String cardNumber = mainListener.getCardNumber();
        //    int length = cardNumber.length() + 1;
        //    String track2 = length + cardNumber + "=";
        //    jtLog("카드번호",StringUtil.getRPadSpace(100, track2));
        //    jtLog("카드번호",StringUtil.getRPadSpace(100, track2).getBytes());
        //    return StringUtil.getRPadSpace(100, track2).getBytes();
        //}

        jtLog("카드번호",StringUtil.getRPadSpace(100, ""));
        jtLog("카드번호",StringUtil.getRPadSpace(100, "").getBytes());
        return StringUtil.getRPadSpace(100, "").getBytes();
    }
    /**
     * 할부
     */
    private byte[] getIMonths(int imonth) {
        jtLog("할부",StringUtil.getLPadZero(2, String.valueOf(imonth)));
        jtLog("할부",StringUtil.getLPadZero(2, String.valueOf(imonth)));
        return StringUtil.getLPadZero(2, String.valueOf(imonth)).getBytes();
    }
    /**
     * 거래금액
     */
    private byte[] getDealAmount(int amount) {
        jtLog("거래금액",StringUtil.getLPadZero(9, String.valueOf(amount)));
        jtLog("거래금액",StringUtil.getLPadZero(9, String.valueOf(amount)).getBytes());
        return StringUtil.getLPadZero(9, String.valueOf(amount)).getBytes();
    }
    /**
     * 세금
     */
    private byte[] getTax() {
        jtLog("세금", StringUtil.getLPadZero(9, ""));
        jtLog("세금", StringUtil.getLPadZero(9, "").getBytes());
        return StringUtil.getLPadZero(9, "").getBytes();
    }
    /**
     * 봉사료
     */
    private byte[] getSvcCharge() {
        jtLog("봉사료", StringUtil.getLPadZero(9, ""));
        jtLog("봉사료", StringUtil.getLPadZero(9, "").getBytes());
        return StringUtil.getLPadZero(9, "").getBytes();
    }

    /**
     * 원거래일자 (취소시)
     */
    private byte[] getOrgDealDt(boolean isCancel, String cancelDate) {
        jtLog("원거래일자 (취소시)",StringUtil.getRPadSpace(6,
                isCancel ? cancelDate.trim() : ""
        ));
        jtLog("원거래일자 (취소시)",StringUtil.getRPadSpace(6,
                isCancel ? cancelDate : ""
        ).getBytes());
        return StringUtil.getRPadSpace(6,
                isCancel ? cancelDate : ""
        ).getBytes();
    }

    /**
     * 원승인번호 (취소시)
     */
    private byte[] getOrgApprovalNo(boolean isCancel, String cancelAppno) {
        jtLog("원승인번호 (취소시)",StringUtil.getRPadSpace(12,
                isCancel ? cancelAppno.trim() : ""
        ));
        jtLog("원승인번호 (취소시)",StringUtil.getRPadSpace(12,
                isCancel ? cancelAppno.trim() : ""
        ).getBytes());
        return StringUtil.getRPadSpace(12,
                isCancel ? cancelAppno.trim() : ""
        ).getBytes();
    }

    /**
     * 원거래고유번호 (취소시)
     */
    private byte[] getOrgUniqueNo(boolean isCancel,String cancelUnino) {
        jtLog("원거래고유번호 (취소시)",StringUtil.getRPadSpace(12,
                isCancel ? cancelUnino.trim() : ""
        ));
        jtLog("원거래고유번호 (취소시)",StringUtil.getRPadSpace(12,
                isCancel ? cancelUnino.trim() : ""
        ).getBytes());
        return StringUtil.getRPadSpace(12,
                isCancel ? cancelUnino.trim() : ""
        ).getBytes();
    }

    /**
     * 서명처리구분
     */
    private byte[] getSignature() {
        Signature signature = Signature.getSignature("데몬판단처리");
        if (signature != null) {
            jtLog("서명처리구분Y",signature.getCode());
            jtLog("서명처리구분Y",signature.getCode().getBytes());
            return signature.getCode().getBytes();
        }
        jtLog("서명처리구분N",Signature.NONE.getCode());
        jtLog("서명처리구분N",Signature.NONE.getCode().getBytes());
        return Signature.NONE.getCode().getBytes();
    }
}
