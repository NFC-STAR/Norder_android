package com.nfcstar.norder.util;

import android.os.SystemClock;
import android.view.View;

public abstract class OnSingleClickListener implements View.OnClickListener {
    private static long CLICK_INTERVAL = 500; //default
    private long lastClickedTime = 0L;

    public abstract void onSingleClick(View v);

    public OnSingleClickListener(){}

    public OnSingleClickListener(long clickInterval){
        CLICK_INTERVAL = clickInterval;
    }

    private long isSafe(){
        return SystemClock.uptimeMillis() - lastClickedTime;
    }

    @Override
    public void onClick(View v) {
        if (isSafe() > CLICK_INTERVAL) {
            onSingleClick(v);
        }
        lastClickedTime = SystemClock.uptimeMillis();
    }
}
