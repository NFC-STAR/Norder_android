package com.nfcstar.norder;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class AndnvcatPayHalbuDialog extends Dialog implements AdapterView.OnItemSelectedListener {
    private String halbu;
    private String[] list_halbu = {"일시불", "2개월", "3개월", "4개월", "5개월", "6개월", "7개월", "8개월", "9개월", "10개월", "11개월", "12개월"};
    private View.OnClickListener positiveClickListener;
    private View.OnClickListener negativeClickListener;

    public AndnvcatPayHalbuDialog(@NonNull Context context) {
        super(context);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_halbu);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Spinner spi_halbu = findViewById(R.id.spi_halbu);
        Button btn_close = findViewById(R.id.btn_close);
        Button btn_apply = findViewById(R.id.btn_apply);
        spi_halbu.setOnItemSelectedListener(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.simple_spinner_dropdown_item, list_halbu);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spi_halbu.setAdapter(adapter);

        btn_apply.setOnClickListener(positiveClickListener);
        btn_close.setOnClickListener(negativeClickListener);
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(flags);
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(flags);
                }
            }
        });
    }

    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        halbu = list_halbu[i];
        ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLUE);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        halbu = "일시불";
    }

    public String getHalbu() {
        return halbu;
    }

    public void setPositiveClickListener(View.OnClickListener positiveClickListener) {
        this.positiveClickListener = positiveClickListener;
    }

    public void setNegativeClickListener(View.OnClickListener negativeClickListener) {
        this.negativeClickListener = negativeClickListener;
    }
}