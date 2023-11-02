package com.nfcstar.norder;

import static com.nfcstar.norder.util.Cnst.USERID_;
import static com.nfcstar.norder.util.Cnst.USERPW_;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.nfcstar.norder.api_client.Afield;
import com.nfcstar.norder.api_client.RetrofitAPI;
import com.nfcstar.norder.util.OnSingleClickListener;
import com.nfcstar.norder.util.Pref;
import com.nfcstar.norder.util.SimpleJO;
import com.nfcstar.norder.util.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends Activity {

    TextView txt_login, edit_pw, edit_id;
    Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this;
        txt_login = findViewById(R.id.txt_login);
        edit_pw = findViewById(R.id.edit_pw);
        edit_id = findViewById(R.id.edit_id);
        if (Pref.getPref() == null) {
            Pref.getInstance().init(context);
        }
        String id = Pref.getPref().getString(USERID_, "");
        String pw = Pref.getPref().getString(USERPW_, "");
        if (!id.equals("") && !pw.equals("")) {
            edit_id.setText(id);
            edit_pw.setText(pw);
            Login(id, pw);
        }

        txt_login.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                String id = edit_id.getText().toString();
                String pw = edit_pw.getText().toString();
                if (id.equals("")) {
                    Toast.makeText(context, "ID를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                } else if (pw.equals("")) {
                    Toast.makeText(context, "PW를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    Login(id, pw);
                }

            }
        });
    }

    void Login(String id, String pw) {
        Afield af = new Afield();
        af.setF("USERID", id);
        af.setF("USERPW", pw);
        Call<String> call = RetrofitAPI.getApiService().login(af.getF());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    SimpleJO jo = Utils.parseSimpleJo(response.body());
                    if (jo.getAsBoolean("flag")) {

                        String STOSEQ = jo.getAsString("STOSEQ");
                        String STONAM = jo.getAsString("STONAM");
                        String STOIMG = jo.getAsString("STOIMG");
                        Intent intent = new Intent(context, SelectTableActivity.class);
                        intent.putExtra("STOSEQ", STOSEQ);
                        intent.putExtra("STONAM", STONAM);
                        intent.putExtra("STOIMG", STOIMG);
                        SharedPreferences.Editor editor = Pref.getPref().edit();
                        editor.putString(USERID_, id);
                        editor.putString(USERPW_, pw);
                        editor.apply();
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(context, jo.getAsString("message"), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }
}
