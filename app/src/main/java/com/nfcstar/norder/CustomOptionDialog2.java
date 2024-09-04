package com.nfcstar.norder;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nfcstar.norder.util.OnSingleClickListener;
import com.nfcstar.norder.util.SimpleJO;
import com.nfcstar.norder.util.Utils;

import java.util.HashMap;
import java.util.Map;

public class CustomOptionDialog2 extends Dialog {
    Context context;
    SimpleJO product_jo;
    MainActivity mainActivity;
    Handler cancelHandler = new Handler(message -> {
        cancel();
        return true;
    });

    public CustomOptionDialog2(@NonNull Context context, SimpleJO product_jo, MainActivity mainActivity) {
        super(context);
        this.context = context;
        this.product_jo = product_jo;
        this.mainActivity = mainActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_custom_no_option);

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


        TextView txt_prdnam = findViewById(R.id.txt_prdnam);
        ImageView iv_prdimg = findViewById(R.id.iv_prdimg);
        TextView txt_prdexp = findViewById(R.id.txt_prdexp);
        Button btn_close = findViewById(R.id.btn_close);
        Button btn_order_fin = findViewById(R.id.btn_order_fin);
        TextView txt_prdamt  =  findViewById(R.id.txt_prdamt);
        btn_order_fin.setText(Utils.getCurrencyString(product_jo.getAsInt("TOTAMT", 0))+" 담기");
        txt_prdamt.setText(Utils.getCurrencyString(product_jo.getAsInt("TOTAMT", 0)));

        txt_prdnam.setText(Html.fromHtml(product_jo.getAsString("PRDNAM")));

        String prdimg_name = product_jo.getAsString("PRDIMG_NAM");
        String prdimg_path = product_jo.getAsString("PRDIMG_PTH");
        if (!prdimg_name.equals("") && !prdimg_path.equals("")) {
            String img = prdimg_path + "/" + prdimg_name;
            Utils.loadImage(context, img, iv_prdimg, 20);
            iv_prdimg.setVisibility(View.VISIBLE);
        } else {
            iv_prdimg.setVisibility(View.GONE);
        }

        String prdexp = product_jo.getAsString("PRDEXP");
        if (!prdexp.equals("")) {
            txt_prdexp.setVisibility(View.VISIBLE);
            txt_prdexp.setText(prdexp);
        } else {
            txt_prdexp.setVisibility(View.GONE);
        }

        btn_close.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                cancel();
            }
        });

        btn_order_fin.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                appendProduct();
                mainActivity.setDrawerViewOpen();
            }
        });
    }

    void appendProduct() {
        int totamt = product_jo.getAsInt("TOTAMT", 0);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("PRDSEQ", product_jo.getAsString("PRDSEQ"));
        map.put("PRDNUM", product_jo.getAsString("PRDNUM"));
        map.put("PRDNAM", product_jo.getAsString("PRDNAM"));
        map.put("PRDIMG", product_jo.getAsString("PRDIMG_PTH") + "/" + product_jo.getAsString("PRDIMG_NAM"));
        map.put("PRTNUM", product_jo.getAsString("PRTNUM"));
        map.put("DTLNUM1", 0);
        map.put("DTLNUM2", 0);
        map.put("DTLNUM3", 0);
        map.put("OPTNAM", "");
        map.put("TOTAMT", totamt);

        Gson gson = new Gson();
        JsonObject object = (JsonObject) gson.toJsonTree(map);
        mainActivity.new_SetOrder(object);
        cancelHandler.sendEmptyMessageDelayed(44, 40);
    }
}
