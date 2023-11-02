package com.nfcstar.norder;

import static com.nfcstar.norder.util.Cnst.FLRNAM_;
import static com.nfcstar.norder.util.Cnst.FLRSEQ_;
import static com.nfcstar.norder.util.Cnst.STOIMG_;
import static com.nfcstar.norder.util.Cnst.STONAM_;
import static com.nfcstar.norder.util.Cnst.STOSEQ_;
import static com.nfcstar.norder.util.Cnst.TBLNAM_;
import static com.nfcstar.norder.util.Cnst.TBLSEQ_;
import static com.nfcstar.norder.util.Cnst.USERID_;
import static com.nfcstar.norder.util.Cnst.USERPW_;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.nfcstar.norder.api_client.Afield;
import com.nfcstar.norder.api_client.RetrofitAPI;
import com.nfcstar.norder.util.Lg;
import com.nfcstar.norder.util.OnSingleClickListener;
import com.nfcstar.norder.util.Pref;
import com.nfcstar.norder.util.SimpleJO;
import com.nfcstar.norder.util.Utils;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectTableActivity extends AppCompatActivity {
    String stoseq, stonam, stoimg;
    Context context;
    ArrayList<FlowData> flowDataArrayList;
    FloorListAdapter floorListAdapter;
    ArrayList<TableData> tableDataArrayList;
    TalbeListAdapter talbeListAdapter;
    String id;
    String pw;

    RecyclerView rec_floor, rec_table;
    TextView txt_next;
    int flrseq, tblseq;
    String flrnam = "", tblnam = "";
    TextView txt_setting;
    boolean firstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_table);
        context = this;
        if (Pref.getPref() == null) {
            Pref.getInstance().init(this);
        }
        Intent getIntent = getIntent();
        stoseq = getIntent.getStringExtra("STOSEQ");
        stonam = getIntent.getStringExtra("STONAM");
        stoimg = getIntent.getStringExtra("STOIMG");
        id = Pref.getPref().getString(USERID_, "");
        pw = Pref.getPref().getString(USERPW_, "");
        flrseq = Pref.getPref().getInt(FLRSEQ_, -1);
        tblseq = Pref.getPref().getInt(TBLSEQ_, -1);
        txt_setting = findViewById(R.id.txt_setting);
        TextView tbl_info = findViewById(R.id.tbl_info);
        flrnam = Pref.getPref().getString(FLRNAM_, "");
        tblnam = Pref.getPref().getString(TBLNAM_, "");
        tbl_info.setText(flrnam + "-" + tblnam);
        floorListAdapter = new FloorListAdapter();
        talbeListAdapter = new TalbeListAdapter();

        rec_floor = findViewById(R.id.rec_floor);
        rec_floor.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        rec_floor.setAdapter(floorListAdapter);

        rec_table = findViewById(R.id.rec_table);
        rec_table.setLayoutManager(new GridLayoutManager(context, 8));
        rec_table.setAdapter(talbeListAdapter);
        getFloor();

        txt_setting.setOnClickListener(v -> {
            Intent intent = new Intent(context,StoreSettingActivity.class);
            startActivity(intent);
        });
    }

    void getFloor() {
        Afield af = new Afield();
        af.setF("USERID", id);
        af.setF("USERPW", pw);
        af.setF("STOSEQ", stoseq);

        Call<String> call = RetrofitAPI.getApiService().getFloor(af.getF());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    SimpleJO jo = Utils.parseSimpleJo(response.body());
                    if (jo.getAsBoolean("flag")) {
                        JsonArray floors = jo.getAsJsonArray("table");
                        flowDataArrayList = new ArrayList<>();
                        for (int i = 0; i < floors.size(); i++) {
                            SimpleJO jjo = new SimpleJO(floors.get(i).getAsJsonObject());
                            int seq = jjo.getAsInt("FLRSEQ", 0);
                            String name = jjo.getAsString("FLRNAM");
                            if (firstLoad) {
                                getTable(seq, name);
                                firstLoad = false;
                            }
                            if (flrseq == seq) {
                                flrnam = name;
                                getTable(seq, name);
                                return;
                            } else {
                                FlowData fl_data = new FlowData(seq, name);
                                flowDataArrayList.add(fl_data);
                            }

                        }
                        floorListAdapter.setFlowDataArrayList(flowDataArrayList);
                        // if (flowDataArrayList.size() != 0)
                        //     getTable(flowDataArrayList.get(0).getFlrseq());

                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    void getTable(int FLRSEQ, String FLRNAM) {
        Afield af = new Afield();
        af.setF("USERID", id);
        af.setF("USERPW", pw);
        af.setF("STOSEQ", stoseq);
        af.setF("FLRSEQ", FLRSEQ);
        Call<String> call = RetrofitAPI.getApiService().getTable(af.getF());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    SimpleJO jo = Utils.parseSimpleJo(response.body());
                    if (jo.getAsBoolean("flag")) {
                        tableDataArrayList = new ArrayList<>();
                        JsonArray table = jo.getAsJsonArray("table");
                        for (int i = 0; i < table.size(); i++) {
                            SimpleJO jjo = new SimpleJO(table.get(i).getAsJsonObject());
                            int TBLSEQ = jjo.getAsInt("TBLSEQ");
                            String TBLNAM = jjo.getAsString("TBLNAM");
                            int FLRSEQ = jjo.getAsInt("FLRSEQ");
                            int rcnseq = jjo.getAsInt("RCNSEQ");
                            Lg.e("dkdh", table.size() + " / " + FLRSEQ + " / " + TBLNAM + " / " + TBLSEQ);
                            if (tblseq == TBLSEQ) {
                                Intent intent = new Intent(context, MainActivity.class);
                                SharedPreferences.Editor editor = Pref.getPref().edit();
                                editor.putString(STOSEQ_, stoseq);
                                editor.putString(STONAM_, stonam);
                                editor.putString(STOIMG_, stoimg);
                                editor.putInt(TBLSEQ_, TBLSEQ);
                                editor.putString(TBLNAM_, TBLNAM);
                                editor.putInt(FLRSEQ_, FLRSEQ);
                                editor.putString(FLRNAM_, FLRNAM);
                                editor.apply();
                                intent.putExtra("RCNSEQ", rcnseq);
                                startActivity(intent);
                                finish();
                                return;
                            } else {
                                TableData tableData = new TableData(TBLSEQ, TBLNAM, FLRSEQ, FLRNAM, rcnseq);
                                tableDataArrayList.add(tableData);
                            }
                        }
                        talbeListAdapter.setTableData(tableDataArrayList);
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }


    class FloorListAdapter extends RecyclerView.Adapter<FloorListAdapter.ViewHolder> {
        ArrayList<FlowData> flowDataArrayList;

        public FloorListAdapter() {
            this.flowDataArrayList = new ArrayList<>();
        }

        public void setFlowDataArrayList(ArrayList<FlowData> flowDataArrayList) {
            this.flowDataArrayList = flowDataArrayList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_floor, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bindview(position);
        }

        @Override
        public int getItemCount() {
            return flowDataArrayList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txt_name;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                txt_name = itemView.findViewById(R.id.txt_name);
            }

            void bindview(int pos) {
                FlowData flowData = flowDataArrayList.get(pos);
                txt_name.setText(flowData.flrname);

                txt_name.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        getTable(flowData.getFlrseq(), flowData.getFlrname());
                    }
                });
            }
        }
    }

    class TalbeListAdapter extends RecyclerView.Adapter<TalbeListAdapter.ViewHolder> {
        ArrayList<TableData> tableData;

        public TalbeListAdapter() {
            this.tableData = new ArrayList<>();
        }

        public void setTableData(ArrayList<TableData> tableData) {
            this.tableData = tableData;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_table, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bindview(position);
        }

        @Override
        public int getItemCount() {
            return tableData.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView txt_name;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                txt_name = itemView.findViewById(R.id.txt_name);
            }

            void bindview(int pos) {
                TableData data = tableData.get(pos);
                Lg.e("문데", data.getTblnam());
                if (flrnam.equals(data.getFLRNAM()) && tblnam.equals(data.getTblnam())) {
                    txt_name.setBackgroundResource(R.drawable.shp_red_c5_2_rect);
                } else {
                    txt_name.setBackgroundResource(R.drawable.shp_black_c5_rect);
                }
                txt_name.setText(data.getTblnam());

                txt_name.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {

                        for (int i = 0; i < flowDataArrayList.size(); i++) {
                            FlowData flowData = flowDataArrayList.get(i);
                            if (flowData.getFlrseq() == data.getFLRSEQ()) {
                                flrnam = flowData.getFlrname();
                            }
                        }

                        Intent intent = new Intent(context, MainActivity.class);
                        SharedPreferences.Editor editor = Pref.getPref().edit();
                        editor.putString(STOSEQ_, stoseq);
                        editor.putString(STONAM_, stonam);
                        editor.putString(STOIMG_, stoimg);
                        editor.putInt(TBLSEQ_, data.getTblseq());
                        editor.putString(TBLNAM_, data.getTblnam());
                        editor.putInt(FLRSEQ_, data.getFLRSEQ());
                        editor.putString(FLRNAM_, flrnam);
                        editor.apply();
                        intent.putExtra("RCNSEQ", data.getRcnseq());
                        startActivity(intent);
                    }
                });
            }
        }
    }

    class FlowData {
        private int flrseq;
        private String flrname;

        public FlowData(int flrseq, String flrname) {
            this.flrseq = flrseq;
            this.flrname = flrname;
        }

        public int getFlrseq() {
            return flrseq;
        }

        public String getFlrname() {
            return flrname;
        }
    }

    class TableData {
        private int tblseq, FLRSEQ, rcnseq;
        private String tblnam, FLRNAM;

        public TableData(int tblseq, String tblnam, int FLRSEQ, int rcnseq) {
            this.tblseq = tblseq;
            this.FLRSEQ = FLRSEQ;
            this.tblnam = tblnam;
            this.rcnseq = rcnseq;
        }

        public TableData(int tblseq, String tblnam, int FLRSEQ, String FLRNAM, int rcnseq) {
            this.tblseq = tblseq;
            this.FLRSEQ = FLRSEQ;
            this.tblnam = tblnam;
            this.FLRNAM = FLRNAM;
            this.rcnseq = rcnseq;
        }

        public int getTblseq() {
            return tblseq;
        }

        public String getTblnam() {
            return tblnam;
        }

        public int getFLRSEQ() {
            return FLRSEQ;
        }

        public String getFLRNAM() {
            return FLRNAM;
        }

        public int getRcnseq() {
            return rcnseq;
        }
    }
}