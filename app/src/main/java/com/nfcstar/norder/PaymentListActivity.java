package com.nfcstar.norder;

import static com.nfcstar.norder.util.Cnst.STOSEQ_;
import static com.nfcstar.norder.util.Cnst.USERID_;
import static com.nfcstar.norder.util.Cnst.USERPW_;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nfcstar.norder.api_client.Afield;
import com.nfcstar.norder.api_client.RetrofitAPI;
import com.nfcstar.norder.util.Cnst;
import com.nfcstar.norder.util.EditViewCalendar;
import com.nfcstar.norder.util.OnSingleClickListener;
import com.nfcstar.norder.util.Pref;
import com.nfcstar.norder.util.SimpleJO;
import com.nfcstar.norder.util.Utils;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentListActivity extends AppCompatActivity {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context context;
    PayListAdapter payListAdapter;
    String id,pw,stoseq;
    EditText edt_strdat, edt_enddat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_list);
        context = this;
        pref = Pref.getPref();
        if (pref == null) {
            pref = Pref.getInstance().init(context);
        }
        editor = Pref.getEditor();
        id = Pref.getPref().getString(USERID_, "");
        pw = Pref.getPref().getString(USERPW_, "");
        stoseq = Pref.getPref().getString(STOSEQ_, "");

        RecyclerView rec_paylist = findViewById(R.id.rec_paylist);
        Button btn_close = findViewById(R.id.btn_close);
        Set<String> setlist = pref.getStringSet(Cnst.PayFinList, new HashSet<>());
        JsonArray paylist = Utils.parseArray(setlist.toString());
        payListAdapter = new PayListAdapter();
        rec_paylist.setAdapter(payListAdapter);
        rec_paylist.setLayoutManager(new LinearLayoutManager(context));

        Button btn_today = findViewById(R.id.btn_today);
        Button btn_week = findViewById(R.id.btn_week);
        edt_strdat = findViewById(R.id.edt_strdat);
        edt_enddat = findViewById(R.id.edt_enddat);
        Button btn_search = findViewById(R.id.btn_search);
        btn_today.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                edt_strdat.setText(Utils.getDate(0));
                edt_enddat.setText(Utils.getDate(0));
            }
        });
        btn_week.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                edt_strdat.setText(Utils.getDate(-7));
                edt_enddat.setText(Utils.getDate(0));
            }
        });

        new EditViewCalendar(context, edt_strdat, 0);
        new EditViewCalendar(context, edt_enddat, 0);

        btn_search.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                getVanList();
            }
        });

        btn_close.setOnClickListener(v -> {
            finish();
        });
        getVanList();
    }
    void getVanList(){
        Afield af = new Afield();
        af.setF("STOSEQ",stoseq);
        af.setF("USERID",id);
        af.setF("USERPW",pw);
        af.setF("STRDAT",edt_strdat.getText().toString());
        af.setF("ENDDAT",edt_enddat.getText().toString());
        af.setF("SAL005","30");
        RetrofitAPI.getApiService().getPayList(af.getF()).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (Utils.api_Successful(response)){
                   SimpleJO jo = Utils.parseSimpleJo(response.body());
                    JsonArray jsonArray = jo.getAsJsonArray("sale");
payListAdapter.setPayList(jsonArray);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    class PayListAdapter extends RecyclerView.Adapter<PayListAdapter.PayListHolder> {
        JsonArray payList;

        public PayListAdapter() {
            payList = new JsonArray();
        }

        public void setPayList(JsonArray payList) {
            this.payList = payList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public PayListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PayListHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_paymentlist, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull PayListHolder holder, int position) {
            holder.bindview(position);
        }

        @Override
        public int getItemCount() {
            return payList.size();
        }

        class PayListHolder extends RecyclerView.ViewHolder {
            TextView txt_no, txt_date, txt_issuer, txt_price;
            Button btn_cancel;
            LinearLayout lay_main;

            public PayListHolder(@NonNull View itemView) {
                super(itemView);
                txt_no = itemView.findViewById(R.id.txt_no);
                txt_date = itemView.findViewById(R.id.txt_date);
                txt_issuer = itemView.findViewById(R.id.txt_issuer);
                txt_price = itemView.findViewById(R.id.txt_price);
                btn_cancel = itemView.findViewById(R.id.btn_cancel);
                lay_main = itemView.findViewById(R.id.lay_main);
            }

            void bindview(int pos) {
                JsonObject object = payList.get(pos).getAsJsonObject();
                // Gson gson = new Gson();
                // Map<String, Object> map = gson.fromJson(object, Map.class);
                SimpleJO jjo = new SimpleJO(object);
                String RCTCOD = jjo.getAsString("RCTCOD");
                String CANCELDT = jjo.getAsString("CANCELDT");
                String RCTSEQ = jjo.getAsString("RCTSEQ");
                String REGDAT = jjo.getAsString("REGDAT");
                String FLGNAM = jjo.getAsString("FLGNAM");
                String REGUSR = jjo.getAsString("REGUSR");
                String VIEW_APPNO = jjo.getAsString("VIEW_APPNO");
                int APRAMT = jjo.getAsInt("APRAMT");
                String CRDNAM = jjo.getAsString("CRDNAM");
                String REGTIM = jjo.getAsString("REGTIM");
                String DEVICETYP = jjo.getAsString("DEVICETYP");
               // try {
               //     dateList = Utils.paytoListDateFormat(appDt);
               //     dateCancel = Utils.paytoCancelDateFormat(appDt);
               // } catch (ParseException e) {
               //     e.printStackTrace();
               // }
               // String intMon = jo.getAsString("intMon");
               // String issuer_name = jo.getAsString("issuer_name");
               // int appPrice = jo.getAsInt("appPrice");
                if (RCTCOD.equals("50")){
                    lay_main.setBackgroundResource(R.color.cancel_fin);
                }else{
                    lay_main.setBackgroundResource(R.color.white);
                }

                txt_no.setText(pos + "");
                txt_date.setText(REGDAT +" "+REGTIM);
               // txt_issuer.setText(issuer_name);
                txt_price.setText(Utils.getCurrencyString(APRAMT));
                btn_cancel.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        payDetailDialog = new PayDetailDialog(
                                context, RCTSEQ, CRDNAM, VIEW_APPNO, REGDAT, REGTIM,
                                CANCELDT, APRAMT, RCTCOD, FLGNAM, REGUSR, DEVICETYP, closeListener);
                        payDetailDialog.show();
                    }
                });


            }
        }
    }
    PayDetailDialog payDetailDialog;
    View.OnClickListener closeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (payDetailDialog != null)
                payDetailDialog.dismiss();
            getVanList();
        }
    };
}