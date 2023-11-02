package com.nfcstar.norder.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nfcstar.norder.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Response;

public class Utils {
    public static final DecimalFormat float_form = new DecimalFormat("###,###.##");
    public static final List<String> MONTH_LIST = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");

    private static final String API_HOST = "https://nfcstar.com/";
    public static final DecimalFormat CURRENCY_FORMATTER = new DecimalFormat(
            "#,###,###원");

    public static String getCurrencyString(long currencyLong) {
        return CURRENCY_FORMATTER.format(currencyLong);
    }

    public static String getCurrencyString(double currency) {
        return CURRENCY_FORMATTER.format(currency);
    }

    private static final String DATE_FORMAT_DEFAULT = "yyyy-MM-dd";
    public static final DateFormat simpleDateFormater = new SimpleDateFormat(
            DATE_FORMAT_DEFAULT, Locale.KOREA);

    public static String simpleFormatDate(Date date) {
        return simpleDateFormater.format(date);
    }

    public static int getPercent(float curt, float total) {
        return (int) ((curt / total) * 100);
    }

    public static String getMonth_ENG(int month) {
        return MONTH_LIST.get(month);
    }

    public static Drawable getDrawable(Context context, @DrawableRes int id) {
        final int version = Build.VERSION.SDK_INT;
        if (version > 21) {
            return ContextCompat.getDrawable(context, id);
        } else {
            return context.getResources().getDrawable(id);
        }
    }

    public static String EncBySha256(String data) throws Exception {
        String retVal = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(data.getBytes());

            byte[] byteData = md.digest();
            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }

            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                String hex = Integer.toHexString(0xff & byteData[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            retVal = hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("EncBySHA256 Error:" + e.toString());
        }
        return retVal;
    }
    //  public static CircularProgressDrawable getProgressDrawable(Context context) {
    //      CircularProgressDrawable progressDrawable = new CircularProgressDrawable(context);
    //      progressDrawable.setStrokeWidth(10f);
    //      progressDrawable.setCenterRadius(50f);
    //      progressDrawable.start();
    //      return progressDrawable;
    //  }

    public static void loadImage(Context context, String url, ImageView imageView, int corner) {
        Lg.e("loadImage22", url + "");
        MultiTransformation<Bitmap> multiOption = new MultiTransformation<>(
                new RoundedCorners(corner)
        );
        //  RequestOptions options = new RequestOptions()
        //          .placeholder(getProgressDrawable(context))
        //          // .error(R.drawable.img_nft_empty)
        //          ;
        Glide.with(context)
                //  .setDefaultRequestOptions(options)
                .load(API_HOST + url)
                //     .apply(RequestOptions.bitmapTransform(multiOption))
                .into(imageView);
    }

    public static boolean api_Successful(Response<String> response) {
        return response.isSuccessful() && response.body() != null;
    }

    public static JsonArray parseArray(String s) {
        try {
        return JsonParser.parseString(s).getAsJsonArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonArray();
        }
    }

    public static JsonObject parseObject(String s) {
        try{
        return JsonParser.parseString(s).getAsJsonObject();
    } catch (Exception e) {
        e.printStackTrace();
        return new JsonObject();
    }
    }

    public static SimpleJO parseSimpleJo(String s) {
        return new SimpleJO(parseObject(s));
    }

    // public static SimpleJO parseSimpleJo(String s) {
    //     return new SimpleJO(parseArray(s).get(0).getAsJsonObject());
    // }
    public static SimpleJO parseSimpleJo(String s, int pos) {
        return new SimpleJO(parseArray(s).get(pos).getAsJsonObject());
    }

    // public static SharedPreferences createPreferences(@NonNull Context context) throws GeneralSecurityException, IOException {
    //     MasterKey masterkey = new MasterKey.Builder(context, "walk")
    //             .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    //             .build();
    //     return EncryptedSharedPreferences.create(context,
    //             "divce_pref",
    //             masterkey,
    //             EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    //             EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    //     );
    // }
    static public String paytoListDateFormat(String input) throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyMMddHHmmss"); // 입력값의 형식
        Date date = null; // 입력값을 Date 객체로 파싱
        date = inputFormat.parse(input);
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm"); // 출력하고자 하는 형식
        return outputFormat.format(date != null ? date : "");

    }

  static public   String paytoCancelDateFormat(String input) throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyMMddHHmmss"); // 입력값의 형식
        Date date; // 입력값을 Date 객체로 파싱
        date = inputFormat.parse(input);
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyMMdd"); // 출력하고자 하는 형식
        return outputFormat.format(date != null ? date : "");
    }
    public static String getDate(int mindate) {
        Calendar cCal = Calendar.getInstance();
        cCal.add(Calendar.DATE, mindate);

        int cYear = cCal.get(Calendar.YEAR);
        int cMonth = cCal.get(Calendar.MONTH) + 1;
        int cDay = cCal.get(Calendar.DAY_OF_MONTH);

        String cYEAR = String.valueOf(cYear);
        String sMONTH = cMonth > 9 ? String.valueOf(cMonth) : "0" + cMonth;
        String sDAY = cDay > 9 ? String.valueOf(cDay) : "0" + cDay;

        return cYEAR + "-" + sMONTH + "-" + sDAY;
    }

}
