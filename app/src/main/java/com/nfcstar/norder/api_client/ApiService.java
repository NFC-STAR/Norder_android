package com.nfcstar.norder.api_client;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {

    @FormUrlEncoded
    @POST("mobile/admin/login.do")
    Call<String> login(@FieldMap Map<String, Object> fieldMap);

    @FormUrlEncoded
    @POST("mobile/pos/getFlow.do")
    Call<String> getFloor(@FieldMap Map<String, Object> fieldMap);

    @FormUrlEncoded
    @POST("mobile/pos/getTable.do")
    Call<String> getTable(@FieldMap Map<String, Object> fieldMap);

    @FormUrlEncoded
    @POST("mobile/pos/getSale.do")
    Call<String> getSale(@FieldMap Map<String, Object> fieldMap);

    @FormUrlEncoded
    @POST("mobile/customer/main_select.do")
    Call<String> getMain_select(@FieldMap Map<String, Object> fieldMap);

    @FormUrlEncoded
    @POST("mobile/pos/product_select.do")
    Call<String> getProduct_select2(@FieldMap Map<String, Object> fieldMap);

    @FormUrlEncoded
    @POST("/mobile/customer/productlist_select.do")
    Call<String> getProduct_select(@FieldMap Map<String, Object> fieldMap);

    @FormUrlEncoded
    @POST("mobile/pos/getOption.do")
    Call<String> getOption(@FieldMap Map<String, Object> fieldMap);

    @POST("mobile/pos/sale.do")
    Call<String> setSale(@Body String s);
    @FormUrlEncoded
    @POST("mobile/customer/call_insert.do")
    Call<String> callInsert(@FieldMap Map<String, Object> fieldMap);

    @FormUrlEncoded
    @POST("mobile/customer/call_select.do")
    Call<String> getCall(@FieldMap Map<String, Object> fieldMap);

    @POST("mobile/pos/setPay.do")
    Call<String> setPay(@Body String s);

    @FormUrlEncoded
    @POST("mobile/pos/NicePosPaymentResult2.do")
    Call<String> NicePosPaymentResult(@FieldMap Map<String, String> fieldMap);

    @FormUrlEncoded
    @POST("mobile/pos/getPayList.do")
    Call<String> getPayList(@FieldMap Map<String, Object> fieldMap);

    @FormUrlEncoded
    @POST("mobile/pos/PaymentCancel.do")
    Call<String> PaymentCancel(@FieldMap Map<String, Object> fieldMap);

    @FormUrlEncoded
    @POST("mobile/pos/NicePosPaymentCancelResult2.do")
    Call<String> NicePosPaymentCancelResult2(@FieldMap Map<String, Object> fieldMap);

    @FormUrlEncoded
    @POST("mobile/pos/updateTableGcm.do")
    Call<String> updateTableGcm(@FieldMap Map<String, Object> fieldMap);

}
