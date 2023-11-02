package com.nfcstar.norder.util;

import android.util.Log;


public class Lg {
    public static int e(String tag, String msg) {
       // if (BuildConfig.DEBUG) {
       //     return Log.e(tag, msg);
       // } else {
       //     return 0;
       // }
        return Log.e(tag, msg);
    }
    public static int w(String tag, String msg) {
       // if (BuildConfig.DEBUG) {
       //     return Log.w(tag, msg);
       // } else {
       //     return 0;
       // }
             return Log.w(tag, msg);
    }
    public static int i(String tag, String msg) {
     //  if (BuildConfig.DEBUG) {
     //      return Log.i(tag, msg);
     //  } else {
     //      return 0;
     //  }
        return Log.i(tag, msg);
    }
}
