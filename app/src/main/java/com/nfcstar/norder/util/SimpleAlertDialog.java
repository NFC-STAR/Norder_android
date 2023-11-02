package com.nfcstar.norder.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.nfcstar.norder.R;

public class SimpleAlertDialog extends Dialog {
    String msg, name_ok = "확인", name_cancel = "취소";
    boolean useTwo;
    View.OnClickListener okClickListener = v -> dismiss();

    View.OnClickListener cancelClickListener = v -> dismiss();

    public void setOkClickListener(View.OnClickListener okClickListener) {
        this.okClickListener = okClickListener;
    }

    public void setCancelClickListener(View.OnClickListener cancelClickListener) {
        this.cancelClickListener = cancelClickListener;
    }

    public SimpleAlertDialog(@NonNull Context context, String msg) {
        super(context);
        this.msg = msg;
        this.useTwo = false;
    }
    public SimpleAlertDialog(@NonNull Context context, String msg, String name_ok, boolean useTwo) {
        super(context);
        this.msg = msg;
        this.useTwo = useTwo;
        this.name_ok = name_ok.equals("") ? "확인" : name_ok;
    }
    public SimpleAlertDialog(@NonNull Context context, String msg,  boolean useTwo) {
        super(context);
        this.msg = msg;
        this.useTwo = useTwo;
    }

    public SimpleAlertDialog(@NonNull Context context, String msg, String name_ok, String name_cancel) {
        super(context);
        this.msg = msg;
        this.useTwo = true;
        this.name_ok = name_ok.equals("") ? "확인" : name_ok;
        this.name_cancel = name_cancel.equals("") ? "취소" : name_cancel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_simple_alert);

        Button btn_ok = findViewById(R.id.btn_ok);
        Button btn_cancel = findViewById(R.id.btn_cancel);
        TextView txt_msg = findViewById(R.id.txt_msg);

        btn_ok.setOnClickListener(okClickListener);
        if (useTwo) {
            btn_cancel.setVisibility(View.VISIBLE);
            btn_cancel.setOnClickListener(cancelClickListener);
        } else {
            btn_cancel.setVisibility(View.GONE);
        }
        txt_msg.setText(msg);

    }
}
