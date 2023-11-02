package com.nfcstar.norder.jtnet;




import com.nfcstar.norder.jtnet.utils.ByteUtil;
import com.nfcstar.norder.jtnet.utils.HexCode;
import com.nfcstar.norder.jtnet.utils.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class BaseApproval {
    protected byte[] dealTypeCd;
    protected byte[] terminalNo;
    protected byte[] dealPosNo;
    protected final byte[] dealPosDt = StringUtil.getDateTime("yyMMddhhmmss").getBytes();
    protected final byte[] cr = new byte[]{HexCode.CR};
    public BaseApproval(byte[] dealTypeCd, byte[] terminalNo) {
        this.dealTypeCd = dealTypeCd;
        this.terminalNo = terminalNo;
    }

    public byte[] getDealTypeCd() {
        return dealTypeCd;
    }

    public void setDealTypeCd(byte[] dealTypeCd) {
        this.dealTypeCd = dealTypeCd;
    }

    public byte[] getTerminalNo() {
        return terminalNo;
    }

    public void setTerminalNo(byte[] terminalNo) {
        this.terminalNo = terminalNo;
    }

    public void setDealPosNo(byte[] dealPosNo){
        this.dealPosNo = dealPosNo;
    }
    public byte[] getDealPosNo(){
        return dealPosNo;
    }
    /**
     * cr 은 상속받은 모델에서 추가
     */
    public byte[] create() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd");
        Random mRandom = new Random();
        int iCount = mRandom.nextInt(999) + 1;
        String TransactionNo =  "POS" + simpleDateFormat.format(cal.getTime()) + String.format("%03d", iCount);



        try {
            dealPosNo = TransactionNo.getBytes("euc-kr");
        }
        catch(Exception e){
            dealPosNo = TransactionNo.getBytes();
        }

        return ByteUtil.mergeArrays(
                dealTypeCd,
                terminalNo,
                dealPosNo,
                dealPosDt
        );
    }
}
