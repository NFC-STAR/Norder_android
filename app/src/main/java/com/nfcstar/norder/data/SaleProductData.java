package com.nfcstar.norder.data;

public class SaleProductData {
    String prdseq, prdnum, prdsts, prdnam, optnam, dtlnum1, dtlnum2, dtlnum3, paycod, rcnnum, telnum, userid, prdimg, prtnum;
    int totamt, salqty, preqty;

    public SaleProductData(String prdseq, String prdnum, String prdsts, String prdnam, String optnam, String dtlnum1, String dtlnum2, String dtlnum3, int salqty, int preqty, int totamt, String paycod, String rcnnum, String telnum, String userid,String prdimg) {
        this.prdseq = prdseq;
        this.prdnum = prdnum;
        this.prdsts = prdsts;
        this.prdnam = prdnam;
        this.optnam = optnam;
        this.dtlnum1 = dtlnum1;
        this.dtlnum2 = dtlnum2;
        this.dtlnum3 = dtlnum3;
        this.salqty = salqty;
        this.preqty = preqty;
        this.totamt = totamt;
        this.paycod = paycod;
        this.rcnnum = rcnnum;
        this.telnum = telnum;
        this.userid = userid;
        this.prdimg = prdimg;
    }

    public SaleProductData(String prdseq, String prdnum, String prdsts, String prdnam, String optnam, String dtlnum1, String dtlnum2, String dtlnum3, int salqty, int preqty, int totamt, String paycod, String rcnnum, String telnum, String userid,String prdimg, String prtnum) {
        this.prdseq = prdseq;
        this.prdnum = prdnum;
        this.prdsts = prdsts;
        this.prdnam = prdnam;
        this.optnam = optnam;
        this.dtlnum1 = dtlnum1;
        this.dtlnum2 = dtlnum2;
        this.dtlnum3 = dtlnum3;
        this.salqty = salqty;
        this.preqty = preqty;
        this.totamt = totamt;
        this.paycod = paycod;
        this.rcnnum = rcnnum;
        this.telnum = telnum;
        this.userid = userid;
        this.prdimg = prdimg;
        this.prtnum = prtnum;
    }
    public SaleProductData(String prdseq, String prdnum, String prdsts, String prdnam, String optnam, String dtlnum1, String dtlnum2, String dtlnum3, int salqty, int preqty, int totamt, String paycod, String rcnnum, String telnum, String userid) {
        this.prdseq = prdseq;
        this.prdnum = prdnum;
        this.prdsts = prdsts;
        this.prdnam = prdnam;
        this.optnam = optnam;
        this.dtlnum1 = dtlnum1;
        this.dtlnum2 = dtlnum2;
        this.dtlnum3 = dtlnum3;
        this.salqty = salqty;
        this.preqty = preqty;
        this.totamt = totamt;
        this.paycod = paycod;
        this.rcnnum = rcnnum;
        this.telnum = telnum;
        this.userid = userid;
    }
    public String getPrdseq() {
        return prdseq;
    }

    public String getPrdnum() {
        return prdnum;
    }

    public String getPrdsts() {
        return prdsts;
    }

    public void setPrdsts(String prdsts) {
        this.prdsts = prdsts;
    }

    public String getPrdnam() {
        return prdnam;
    }

    public String getOptnam() {
        return optnam;
    }

    public String getDtlnum1() {
        return dtlnum1;
    }

    public String getDtlnum2() {
        return dtlnum2;
    }

    public String getDtlnum3() {
        return dtlnum3;
    }

    public int getSalqty() {
        return salqty;
    }

    public int getPreqty() {
        return preqty;
    }

    public int getTotamt() {
        return totamt;
    }

    public String getPaycod() {
        return paycod;
    }

    public String getRcnnum() {
        return rcnnum;
    }

    public String getTelnum() {
        return telnum;
    }

    public String getUserid() {
        return userid;
    }

    public void setSalqty(int salqty) {
        this.salqty = salqty;
    }

    public String getPrdimg() {
        return prdimg;
    }

    public String getPrtnum() {
        return prtnum;
    }
}
