package com.nfcstar.norder;

import static com.nfcstar.norder.util.Cnst.STOSEQ_;
import static com.nfcstar.norder.util.Cnst.USERID_;
import static com.nfcstar.norder.util.Cnst.USERPW_;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.nfcstar.norder.api_client.Afield;
import com.nfcstar.norder.api_client.RetrofitAPI;
import com.nfcstar.norder.jtnet.JtnetClass;
import com.nfcstar.norder.jtnet.utils.ApprovalCode;
import com.nfcstar.norder.jtnet.utils.StringUtil;
import com.nfcstar.norder.util.Cnst;
import com.nfcstar.norder.util.OnSingleClickListener;
import com.nfcstar.norder.util.Pref;
import com.nfcstar.norder.util.SimpleAlertDialog;
import com.nfcstar.norder.util.SimpleJO;
import com.nfcstar.norder.util.Utils;

import net.jt.pos.sdk.JTNetPosManager;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PayDetailDialog extends Dialog {
    String payType, STONAM, MOID, RCNSEQ, RCTSEQ, CRDNAM, APRNUM, APRDAT, APRTIM, CNLDAT, CNLTIM, RCTCOD, RCTCOD_NM, REGUSR, MOBNUM, TAXFLG, DEVICETYP;
    TextView txt_aprnum, txt_aprdat, txt_cnldat, txt_apramt, txt_rctcod, txt_regusr, txt_regusr_name;
    Button btn_paydetail, btn_close;
    int APRAMT;

    Handler handler;
    private Context context;
    View.OnClickListener closeListener;

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    String userID, userPW, stoseq, paymentData;
    boolean usePayment, usePrint;
    int printType = 0;

    boolean isConnect_sam4s;
    String targetaddress = "172.30.1.192", sam4s_model = "";
    int nPort = 9100;
    // CustomAlertDialog customAlertDialog;
    // CustomProgressDialog customProgressDialog;
    private JTNetPosManager.RequestCallback requestCallback;

    public PayDetailDialog(@NonNull Context context, String RCTSEQ, String CRDNAM, String APRNUM,
                           String APRDAT, String APRTIM, String CNLDAT, int APRAMT,
                           String RCTCOD, String RCTCOD_NM, String REGUSR, String DEVICETYP, View.OnClickListener closeListener) {
        super(context);
        this.context = context;
        payType = "VAN";
        this.RCTSEQ = RCTSEQ;
        this.CRDNAM = CRDNAM;
        this.APRNUM = APRNUM;
        this.APRDAT = APRDAT;
        this.APRTIM = APRTIM;
        this.CNLDAT = CNLDAT;
        this.APRAMT = APRAMT;
        this.RCTCOD = RCTCOD;
        this.RCTCOD_NM = RCTCOD_NM;
        this.REGUSR = REGUSR;
        this.DEVICETYP = DEVICETYP;
        this.closeListener = closeListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_pay_detail);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pref = Pref.getPref();
        if (pref == null) {
            pref = Pref.getInstance().init(context);
        }

        JTNetPosManager.init(context);

        editor = Pref.getEditor();
        userID = Pref.getPref().getString(USERID_, "");
        userPW = Pref.getPref().getString(USERPW_, "");
        stoseq = Pref.getPref().getString(STOSEQ_, "");
        //  lay_crdnam = findViewById(R.id.lay_crdnam);
        //  txt_crdnam = findViewById(R.id.txt_crdnam);
        txt_aprnum = findViewById(R.id.txt_aprnum);
        txt_aprdat = findViewById(R.id.txt_aprdat);
        txt_cnldat = findViewById(R.id.txt_cnldat);
        txt_apramt = findViewById(R.id.txt_apramt);
        txt_rctcod = findViewById(R.id.txt_rctcod);
        txt_regusr_name = findViewById(R.id.txt_regusr_name);
        txt_regusr = findViewById(R.id.txt_regusr);
        // txt_mobnum = findViewById(R.id.txt_mobnum);
        // btn_print = findViewById(R.id.btn_print);
        btn_paydetail = findViewById(R.id.btn_paydetail);
        btn_close = findViewById(R.id.btn_close);
        // lay_mobnum = findViewById(R.id.lay_mobnum);
        // btn_cash = findViewById(R.id.btn_cash);
        txt_aprnum.setText(APRNUM);
        txt_aprdat.setText(APRDAT + " " + APRTIM);

        if (RCTCOD.equals("50")) {
            btn_paydetail.setText("취소 완료");
            btn_paydetail.setBackgroundResource(R.drawable.btn_white02);
        } else {
            btn_paydetail.setText("결제 취소요청");
            btn_paydetail.setBackgroundResource(R.drawable.btn_white);
            btn_paydetail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    switch (payType) {
                        case "PG":
                            //  PGCancelMassge(RCTSEQ, RCNSEQ);
                            break;

                        case "VAN":
                            //  VanPaymentCancelRequest(APRDAT + " " + APRTIM, APRAMT, RCTSEQ);
                            break;

                        case "CASH":
                            if (TAXFLG.equals("Y")) {
                                //     CashPaymentCancelRequest(RCTSEQ, "25");
                            } else {
                                //       CashCancelMassge(RCTSEQ, "20");
                            }
                            break;
                    }
                }
            });
        }
        if (!CNLDAT.equals("")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA);
            SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyMMddHHmm", Locale.KOREA);
            String date1 = "";
            try {
                Date d = dateFormat2.parse(CNLDAT);
                date1 = dateFormat.format(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            CNLTIM = date1;
            txt_cnldat.setText(date1);
        }
        // lay_crdnam.setVisibility(View.VISIBLE);
        // txt_crdnam.setText(CRDNAM);
        txt_regusr_name.setText("카드 번호 ");
        // lay_mobnum.setVisibility(View.GONE);
        txt_apramt.setText(Utils.getCurrencyString(APRAMT));


        txt_rctcod.setText(RCTCOD_NM);
        //txt_regusr.setText(REGUSR);


        if (closeListener != null) {
            btn_close.setOnClickListener(closeListener);
        } else {
            btn_close.setOnClickListener(v -> dismiss());
        }

        btn_paydetail.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                SimpleAlertDialog alertDialog = new SimpleAlertDialog(context, "결제를 취소하시겠습니까?", true);
                alertDialog.setOkClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        alertDialog.dismiss();
                        if (APPPRICE < 50000) {
                            JtnetClass jtnetClass = new JtnetClass(ApprovalCode.getApproval(JtnetClass.신용취소), Pref.getPref().getString(Cnst.jtnet_catid, ""), APPPRICE, 0, APPDT, APPNO, ORIAPPNO);
                            JTNetPosManager.getInstance().jtdmProcess(jtnetClass.getRequestCode(), jtnetClass.createCreditArr(), requestCallback);
                        } else {
                            HalbuDialog(APPPRICE);
                        }
                    }
                });
                alertDialog.show();
            }
        });

        requestCallback = new JTNetPosManager.RequestCallback() {
            @Override
            public void onResponse(Message msg) {

                byte[] response = msg.getData().getByteArray("RESPONSE_MSG");
                Log.e("asdf", "[응답2] : " + response.length + "||" + response[response.length - 1]);

                if (response != null) {
                    String strResData = StringUtil.byteArrayToString(response);
                    Log.e("Asdf", "[응답] : " + response.length + "||" + strResData);
                    paymentData = strResData;
                    cutStringByByte(paymentData, 5, "이거뭔데 :");
                    String gubun = cutStringByByte(paymentData, 4, "전문종류 :");
                    String deviceID = cutStringByByte(paymentData, 10, "단말기번호 :");
                    String oid = cutStringByByte(paymentData, 12, "POS번호 + 거래 일련번호 :");//POS번호 + 거래 일련번호
                    cutStringByByte(paymentData, 10, "S/W 버전 :"); // S/W 버전
                    cutStringByByte(paymentData, 10, "H/W 시리얼번호(리더기) :"); // H/W 시리얼번호(리더기)
                    cutStringByByte(paymentData, 32, "KTC인증번호 / 데몬 + 리더기 :"); //KTC인증번호 / 데몬 + 리더기
                    cutStringByByte(paymentData, 12, "전문 전송 일자 / “YYMMHHDDMMSS” :"); // 전문 전송 일자 / “YYMMHHDDMMSS”
                    cutStringByByte(paymentData, 5, "FILLER / Space :"); // FILLER / Space
                    cutStringByByte(paymentData, 1, "페킷타입 / 응답전문(정상:'S', 에러:'E') :"); // 페킷타입 / 응답전문(정상:'S', 에러:'E')
                    String resultCode = cutStringByByte(paymentData, 4, "응답코드 / 0000 : 정상, 그 외 에러코드(응답메시지 참조) :"); // 응답코드 / 0000 : 정상, 그 외 에러코드(응답메시지 참조)
                    String mag = cutStringByByte(paymentData, 36, "mag");
                    if (gubun.equals("1050")) {
                        //  Log.e("승인관련", "승인번호:" + mag.substring(0, 12) + " / 승인일시:" +
                        //          mag.substring(12, 24) + " / 고유번호:" +
                        //          mag.substring(24, 36));
                        String appNo = mag.substring(0, 12);
                        String appDt = mag.substring(12, 24);
                        String trNo = mag.substring(24, 36);// 승인 번호
                        String cancelAppno = pref.getString("cancelAppno", "");
                        if (cancelAppno.equals("")) {
                            editor.putString("cancelAppno", mag.substring(0, 12));
                            editor.putString("cancelDay", mag.substring(12, 24));
                            editor.putString("cancelUnitno", mag.substring(24, 36));
                            editor.apply();
                        } else {
                            editor.putString("cancelAppno", "");
                            editor.putString("cancelDay", "");
                            editor.putString("cancelUnitno", "");
                            editor.apply();
                        }
                        cutStringByByte(paymentData, 15, "가맹점 번호 :"); //가맹점 번호
                        String issuerCd = cutStringByByte(paymentData, 4, "발급사 코드 :");//발급사 코드
                        cutStringByByte(paymentData, 20, "발급사 명 :"); //발급사 명
                        String ACQUIRERCD = cutStringByByte(paymentData, 4, "매입사 코드 :");//매입사 코드
                        cutStringByByte(paymentData, 20, "매입사 명 :"); //매입사 명
                        cutStringByByte(paymentData, 2, "카드구분 :");//카드구분
                        cutStringByByte(paymentData, 1, "매입 구분 :");//매입 구분
                        cutStringByByte(paymentData, 1, "EMV 데이터 옵션 :");//EMV 데이터 옵션
                        cutStringByByte(paymentData, 1, "카드사 전표출력 :");//카드사 전표출력
                        cutStringByByte(paymentData, 9, "할인금액 :");//할인금액
                        cutStringByByte(paymentData, 9, "발생포인트 :");//발생포인트
                        cutStringByByte(paymentData, 9, "가용포인트 :");//가용포인트
                        cutStringByByte(paymentData, 9, "누적포인트 :");//누적포인트
                        cutStringByByte(paymentData, 9, "기프트 잔액 :");//기프트 잔액
                        cutStringByByte(paymentData, 9, "카드사 포인트 :");//카드사 포인트
                        cutStringByByte(paymentData, 60, "포인트 정보 :"); //포인트 정보
                        cutStringByByte(paymentData, 16, "부가정보1 :"); //부가정보1
                        cutStringByByte(paymentData, 64, "부가정보2 :"); //부가정보2
                        cutStringByByte(paymentData, 128, "부가정보3 :"); //부가정보3
                        String cardNo = cutStringByByte(paymentData, 20, "출력 TRACK2DATA :"); //출력 TRACK2DATA / 결제카드번호
                        // cutStringByByte(paymentData, 28,"프린트 메시지1"); //프린트 메시지1
                        // cutStringByByte(paymentData, 28,"프린트 메시지2"); //프린트 메시지2
                        // cutStringByByte(paymentData, 28,"프린트 메시지3"); //프린트 메시지3
                        // cutStringByByte(paymentData, 28,"프린트 메시지4"); //프린트 메시지4
                        // cutStringByByte(paymentData, 28,"프린트 메시지5"); //프린트 메시지5
                        // cutStringByByte(paymentData, 28,"프린트 메시지6"); //프린트 메시지6
                        // cutStringByByte(paymentData, 400,"EMV 응답데이터"); //EMV 응답데이터
                        Log.e("뭐남았냐??", "|" + paymentData + "|");

                        try {
                            NicePosPaymentCancelResult2(resultCode, appNo, appDt, issuerCd, ACQUIRERCD, cardNo, oid, trNo);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        SimpleAlertDialog alertDialog = new SimpleAlertDialog(context, "결제 취소 실패:\n" + strResData.trim());
                        alertDialog.show();
                    }
                }
                JTNetPosManager.getInstance().destroy(context);
                JTNetPosManager.init(context);
            }
        };

        PaymentCancel();
    }

    String APPNO, APPDT, ORIAPPNO;
    int APPPRICE;

    void PaymentCancel() {
        Afield af = new Afield();
        af.setF("RCTSEQ", RCTSEQ);
        af.setF("SAL005", "30");
        af.setF("STOSEQ", stoseq);
        af.setF("S_USERID", userID);
        RetrofitAPI.getApiService().PaymentCancel(af.getF()).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (Utils.api_Successful(response)) {
                    SimpleJO jo = Utils.parseSimpleJo(response.body());
                    APPNO = jo.getAsString("APPNO");
                    String appDt = jo.getAsString("APPDT");
                    txt_aprnum.setText(APPNO);
                    try {
                        txt_aprdat.setText(Utils.paytoListDateFormat(appDt));
                        APPDT = Utils.paytoCancelDateFormat(appDt);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    ORIAPPNO = jo.getAsString("ORIAPPNO");
                    String BIZNO = jo.getAsString("BIZNO");
                    RCTSEQ = jo.getAsString("RCTSEQ");
                    APPPRICE = jo.getAsInt("APPPRICE");
                    String CATID = jo.getAsString("CATID");
                    String CARDNO = jo.getAsString("CARDNO");
                    txt_regusr.setText(CARDNO);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    String finalHalbu;
    String intMon = "00";
    AndnvcatPayHalbuDialog andnvcatPayHalbuDialog;

    void HalbuDialog(int totamt) {
        int finalTotamt;
        andnvcatPayHalbuDialog = new AndnvcatPayHalbuDialog(context);
        andnvcatPayHalbuDialog.setCancelable(false);
        finalTotamt = totamt;
        andnvcatPayHalbuDialog.setPositiveClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalHalbu = andnvcatPayHalbuDialog.getHalbu();
                int halbu = 0;
                if (!finalHalbu.equals("일시불")) {
                    intMon = finalHalbu.replace("개월", "");
                    halbu = Integer.parseInt(intMon);

                }
                JtnetClass jtnetClass = new JtnetClass(ApprovalCode.getApproval(JtnetClass.신용취소), Pref.getPref().getString(Cnst.jtnet_catid, ""), APPPRICE, halbu, APPDT, APPNO, ORIAPPNO);
                JTNetPosManager.getInstance().jtdmProcess(jtnetClass.getRequestCode(), jtnetClass.createCreditArr(), requestCallback);
                andnvcatPayHalbuDialog.dismiss();
            }
        });
        if (andnvcatPayHalbuDialog != null && !andnvcatPayHalbuDialog.isShowing()) {
            andnvcatPayHalbuDialog.show();
        }
    }

    public String cutStringByByte(String str, int maxBytes, String msg) {
        if (str == null || str.isEmpty() || maxBytes <= 0) {
            return "";
        }
        int currentBytes = 0;
        int index = 0;
        try {
            while (index < str.length()) {
                char c = str.charAt(index);
                int charBytes = String.valueOf(c).getBytes("UTF-8").length;
                if (charBytes > 2) {
                    //  Log.e("바이트", charBytes + "");
                    charBytes = 2;
                }
                if (currentBytes + charBytes > maxBytes) {
                    break;
                }
                currentBytes += charBytes;
                index++;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String result = str.substring(0, index); // 이전 문자열
        Log.e("결제", msg + result + "|");
        paymentData = str.substring(index); // 남은 문자열
        return result;
    }

    void NicePosPaymentCancelResult2(String resultCode, String appNo, String appDt, String issuerCd, String ACQUIRERCD,
                                     String cardNo, String oid, String trNo) throws Exception {
        Afield af = new Afield();
        af.setF("RCTSEQ", RCTSEQ);
        af.setF("S_USERID", userID);
        af.setF("gubun", "0401");
        af.setF("resultCode", resultCode);
        af.setF("appPrice", APPPRICE);
        af.setF("intMon", intMon);
        af.setF("appNo", appNo);
        af.setF("appDt", appDt);
        af.setF("issuerCd", issuerCd);
        af.setF("ACQUIRERCD", ACQUIRERCD);
        af.setF("appCatid", Pref.getPref().getString(Cnst.jtnet_catid, ""));
        af.setF("cardNo", cardNo);
        af.setF("oid", oid);
        af.setF("trNo", trNo);
        af.setF("DEVICETYP", "00");
        af.setF("hash", Utils.EncBySha256("0401" + resultCode + appNo + APPPRICE + appDt));
        RetrofitAPI.getApiService().NicePosPaymentCancelResult2(af.getF()).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    SimpleJO jo = Utils.parseSimpleJo(response.body());
                    String msg = jo.getAsString("message");
                    if (!msg.isEmpty()) {
                        SimpleAlertDialog alertDialog = new SimpleAlertDialog(context, msg);
                        dismiss();
                        alertDialog.show();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }
}
