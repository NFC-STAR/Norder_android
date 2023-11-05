package com.nfcstar.norder.util;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.nfcstar.norder.R;

public class AlertDialogActivity extends AppCompatActivity {
    public static AlertDialogActivity alertDialogActivity = null;
    public static AlertDialog.Builder alertDialogBuilder = null;
    public static AlertDialog alertDialog = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.7f;
        getWindow().setAttributes(layoutParams);

        // 이 부분이 바로 화면을 깨우는 부분 되시겠다.
        // 화면이 잠겨있을 때 보여주기
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        setContentView(R.layout.activity_alert_dialog);
        alertDialogActivity = AlertDialogActivity.this;
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String msg = intent.getStringExtra("body");

        alert(title, msg);

    }

    CountDownTimer resetTimer = new CountDownTimer(10000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            alertBtn.setText("닫기 ( " + ((millisUntilFinished+1000) / 1000)+" )");
        }

        @Override
        public void onFinish() {
            alertDialog.dismiss();
            alertDialogActivity = null;
            alertDialogBuilder = null;
            finish();
        }
    };
    Button alertBtn;
    public void alert(String title, String msg) {
        LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.custom_alert_dialog, null);
        view.setBackgroundColor(Color.TRANSPARENT);
       // ImageView alertIcon = view.findViewById(R.id.custom_alert_icon);
       // TextView alertTitle = view.findViewById(R.id.custom_alert_title);
        TextView alertBody = view.findViewById(R.id.custom_alert_body);
        alertBtn = view.findViewById(R.id.custom_alert_button);


      //  alertIcon.setImageDrawable(getApplicationContext().getResources().getDrawable(R.mipmap.ic_launcher));
      //  alertTitle.setText(title);
        alertBody.setText(msg);
        alertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                alertDialogActivity = null;
                alertDialogBuilder = null;
                finish();
                resetTimer.cancel();
            }
        });

        alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(view);

        // set title
//        alertDialogBuilder.setIcon(getApplicationContext().getResources().getDrawable(R.drawable.ic_launcher));
//        alertDialogBuilder.setTitle(title);
        // set dialog message
//        alertDialogBuilder.setMessage(msg)
//                .setCancelable(false)
//                .setNeutralButton("닫기", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // if this button is clicked, just close
//                        // the dialog box and do nothing
////                        stopService(getIntent());
//                        alertDialogActivity = null;
//                        alertDialogBuilder = null;
//                        dialog.cancel(); finish();
//                    }
//                });
        // create alert dialog
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(alertDialog != null) {
                    if (alertDialog.isShowing()) {
                        alertDialog.dismiss();
                        alertDialog = null;
                    }
                }
                alertDialog = alertDialogBuilder.create();
                alertDialog.setCancelable(false);
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
                resetTimer.start();
            }
        });

    }
}
