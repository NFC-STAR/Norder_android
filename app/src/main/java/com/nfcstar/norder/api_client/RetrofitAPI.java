package com.nfcstar.norder.api_client;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitAPI {
    private final ApiService apiService;
    private static final String API_HOST = "https://nfcstar.com/";
  // private static final String API_HOST = "http://192.168.0.200:8085/";
    //private static final String API_HOST = "59.22.93.204:8085/";
  //  private static final String API_HOST = "http://192.168.0.200:8085/";

    public static String getHost(){
        return API_HOST;
    }

    public RetrofitAPI() {
        Log.e("RetrofitAPI", "정말 재사용하니?");

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(20, TimeUnit.SECONDS);
        builder.writeTimeout(20, TimeUnit.SECONDS);
        builder.connectTimeout(10, TimeUnit.SECONDS);
        //if () {
            HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor(message -> Log.e("THEEND", "retrofit: " + message));
            logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.interceptors().add(logInterceptor);
        //}
        OkHttpClient client = builder.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_HOST)
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(client)
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    public static ApiService getApiService() {
        return SingletonHolder.INSTANCE.apiService;
    }
    private static class SingletonHolder {
        public static final RetrofitAPI INSTANCE = new RetrofitAPI();
    }


}
