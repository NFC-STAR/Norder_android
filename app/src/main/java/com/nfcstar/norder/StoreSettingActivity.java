package com.nfcstar.norder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.nfcstar.norder.util.Cnst;
import com.nfcstar.norder.util.Pref;

public class StoreSettingActivity extends AppCompatActivity {
    CheckBox chk_payment, chk_massge;
    EditText edit_catid;
    Button btn_save, btn_logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_setting);
        Context context = this;
       SharedPreferences pref =  Pref.getInstance().init(context);
        chk_payment = findViewById(R.id.chk_payment);
        chk_massge = findViewById(R.id.chk_massge);
        edit_catid = findViewById(R.id.edit_catid);
        btn_save = findViewById(R.id.btn_save);
        btn_logout = findViewById(R.id.btn_logout);
        Button btn_paylist = findViewById(R.id.btn_paylist);
        chk_payment.setChecked(pref.getBoolean(Cnst.USEPAY,false));
        chk_massge.setChecked(pref.getBoolean(Cnst.USEMSG,false));
        edit_catid.setText(pref.getString(Cnst.jtnet_catid,""));
        btn_save.setOnClickListener(v->{
            SharedPreferences.Editor editor = Pref.getEditor();
            editor.putBoolean(Cnst.USEPAY,chk_payment.isChecked());
            editor.putString(Cnst.jtnet_catid,edit_catid.getText().toString().trim());
            editor.putBoolean(Cnst.USEMSG,chk_massge.isChecked());
            editor.apply();
            finish();
        });
        btn_logout.setOnClickListener(v->{
            SharedPreferences.Editor editor = Pref.getEditor();
            editor.remove(Cnst.USERID_);
            editor.remove(Cnst.USERPW_);
            editor.remove(Cnst.STOSEQ_);
            editor.remove(Cnst.STONAM_);
            editor.remove(Cnst.STOIMG_);
            editor.remove(Cnst.TBLSEQ_);
            editor.remove(Cnst.TBLNAM_);
            editor.remove(Cnst.FLRSEQ_);
            editor.remove(Cnst.FLRNAM_);
            editor.remove(Cnst.RCNSEQ_);
            editor.remove(Cnst.USEPAY);
            editor.remove(Cnst.jtnet_catid);
            editor.remove(Cnst.PayFinList);
            editor.clear();
            editor.apply();
            Intent intent = new Intent(context, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        });
        btn_paylist.setOnClickListener(v->{
            Intent intent = new Intent(context, PaymentListActivity.class);
            startActivity(intent);
        });

    }
}