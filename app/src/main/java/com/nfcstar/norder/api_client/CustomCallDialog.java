package com.nfcstar.norder.api_client;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nfcstar.norder.MainActivity;
import com.nfcstar.norder.R;
import com.nfcstar.norder.util.OnSingleClickListener;
import com.nfcstar.norder.util.Utils;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomCallDialog extends Dialog {
    Context context;

    RecyclerView rc_call_list;
    TextView txt_close;
    int tblseq;
    String stoseq ,where;

    /** Context context, int stoseq , int tblseq, String where */
    public CustomCallDialog(@NonNull Context context, String stoseq , int tblseq, String where) {
        super(context);
        this.context = context;
        this.stoseq = stoseq;
        this.where = where;
        this.tblseq = tblseq;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_calling);
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        boolean isImmersiveModeEnabled = ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {
            Log.i("Is on?", "Turning immersive mode mode off. ");
        } else {
            Log.i("Is on?", "Turning immersive mode mode on.");
        }
        // 몰입 모드를 꼭 적용해야 한다면 아래의 3가지 속성을 모두 적용시켜야 합니다
        newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        rc_call_list = findViewById(R.id.rc_call_list);
       // rc_call_list.setLayoutManager(new LinearLayoutManager(context));
        rc_call_list.setLayoutManager(new GridLayoutManager(context,4));
     //   txt_close = findViewById(R.id.txt_close);
        RelativeLayout lay_close = findViewById(R.id.lay_close);

        lay_close.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                cancel();
            }
        });
        getCall();
    }

    class CallMsgData {
        String message;
        boolean select;

        public CallMsgData(String message) {
            this.message = message;
            select = false;
        }

        public String getMessage() {
            return message;
        }

        public boolean isSelect() {
            return select;
        }

        public void setSelect(boolean select) {
            this.select = select;
        }
    }

    ArrayList<CallMsgData> msgDataArrayList = new ArrayList<>();

    void getCall() {
        msgDataArrayList.clear();
        Afield af = new Afield();
        af.setF("STOSEQ", stoseq);
        Call<String> call = RetrofitAPI.getApiService().getCall(af.getF());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (Utils.api_Successful(response)) {
                    JsonArray jArray_call = JsonParser.parseString(response.body().toString()).getAsJsonArray();
                    for (int i = 0; i < jArray_call.size(); i++) {
                        JsonObject object = jArray_call.get(i).getAsJsonObject();
                        msgDataArrayList.add(new CallMsgData(object.get("CALNAM").getAsString()));
                        CallListAdapter callListAdapter = new CallListAdapter(msgDataArrayList);
                        rc_call_list.setAdapter(callListAdapter);
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    class CallListAdapter extends RecyclerView.Adapter<CallListAdapter.viewHolder> {
        ArrayList<CallMsgData> msgDataList;

        public CallListAdapter(ArrayList<CallMsgData> msgDataList) {
            this.msgDataList = msgDataList;
        }

        @NonNull
        @Override
        public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new viewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_call_list, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull viewHolder holder, int position) {
            holder.bindview(position);
        }

        @Override
        public int getItemCount() {
            return msgDataList.size();
        }

        void resetSelcet() {
            for (int i = 0; i < msgDataList.size(); i++) {
                msgDataList.get(i).setSelect(false);
            }
        }

        class viewHolder extends RecyclerView.ViewHolder {
            TextView  txt_send;

            public viewHolder(@NonNull View itemView) {
                super(itemView);
                txt_send = itemView.findViewById(R.id.txt_send);
            }

            void bindview(int pos) {
                CallMsgData data = msgDataList.get(pos);
                txt_send.setText(data.getMessage());
              // if (data.isSelect()) {
              //     txt_send.setVisibility(View.VISIBLE);
              // } else {
              //     txt_send.setVisibility(View.GONE);
              // }
              // txt_call.setOnClickListener(new OnSingleClickListener() {
              //     @Override
              //     public void onSingleClick(View v) {
              //         resetSelcet();
              //         msgDataList.get(pos).setSelect(true);
              //         notifyDataSetChanged();
              //     }
              // });

                txt_send.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        setCall(where, data.getMessage());
                    }
                });
            }
        }
    }

    void setCall(String where, String msg) {
        Afield af = new Afield();
        af.setF("STOSEQ", stoseq);
        af.setF("USERID", where);
        af.setF("TBLSEQ", tblseq);
        af.setF("TELNUM", "01000000000");
        af.setF("CALNAM", msg);
        Call<String> call = RetrofitAPI.getApiService().callInsert(af.getF());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (Utils.api_Successful(response)) {
                    //getCall();
                    cancel();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }
}
