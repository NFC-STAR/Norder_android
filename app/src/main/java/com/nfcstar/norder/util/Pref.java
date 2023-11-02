package com.nfcstar.norder.util;

import android.content.Context;
import android.content.SharedPreferences;

public class Pref {
    SharedPreferences pref;

    public Pref() {

    }

    public SharedPreferences init(Context context) {
        pref = context.getSharedPreferences("2rf1gs3afv", Context.MODE_PRIVATE);
        return pref;
    }

    public static Pref getInstance() {
        return SingletonHolder.INSTANCE;
    }
    public static SharedPreferences getPref() {
        return SingletonHolder.INSTANCE.pref;
    }
    public static SharedPreferences.Editor getEditor() {
        return getPref().edit();
    }

    private static class SingletonHolder {
        public static final Pref INSTANCE = new Pref();
    }
}
