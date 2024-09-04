package com.nfcstar.norder;

import static com.nfcstar.norder.util.Cnst.FLRNAM_;
import static com.nfcstar.norder.util.Cnst.FLRSEQ_;
import static com.nfcstar.norder.util.Cnst.STOIMG_;
import static com.nfcstar.norder.util.Cnst.STONAM_;
import static com.nfcstar.norder.util.Cnst.STOSEQ_;
import static com.nfcstar.norder.util.Cnst.TBLNAM_;
import static com.nfcstar.norder.util.Cnst.TBLSEQ_;
import static com.nfcstar.norder.util.Cnst.USERID_;
import static com.nfcstar.norder.util.Cnst.USERPW_;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nfcstar.norder.api_client.Afield;
import com.nfcstar.norder.api_client.ApiService;
import com.nfcstar.norder.api_client.CustomCallDialog;
import com.nfcstar.norder.api_client.RetrofitAPI;
import com.nfcstar.norder.data.SaleProductData;
import com.nfcstar.norder.jtnet.JtnetClass;
import com.nfcstar.norder.jtnet.utils.ApprovalCode;
import com.nfcstar.norder.jtnet.utils.StringUtil;
import com.nfcstar.norder.util.Cnst;
import com.nfcstar.norder.util.Lg;
import com.nfcstar.norder.util.OnSingleClickListener;
import com.nfcstar.norder.util.Pref;
import com.nfcstar.norder.util.SimpleAlertDialog;
import com.nfcstar.norder.util.SimpleJO;
import com.nfcstar.norder.util.Utils;

import net.jt.pos.sdk.JTNetPosManager;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends FragmentActivity {
    Context context;
    String id, pw, stoseq, stonam, stoimg, tblnam, flrnam, flrnam_ = "";
    int tblseq, flrseq;
    int RCNSEQ;
    JsonArray array_catagory, array_product;
    ArrayList<SaleProductData> productListData = new ArrayList<>();

    TextView txt_salamt, txt_cancel, txt_order;
    LinearLayout lay_order_detail, lay_salcrt;

    private static final String TAG = "MainActivity";
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Notifications permission granted",Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(this, "FCM can't post notifications without POST_NOTIFICATIONS permission",
                            Toast.LENGTH_LONG).show();
                }
            });

    public void setProductListData(ArrayList<SaleProductData> productListData) {
        this.productListData = productListData;
    }

    LinearLayout lay_main;
    ConstraintLayout lay_right;
    RecyclerView rc_category, rc_product;
    TextView txt_table, txt_cart_qty, txt_toast;
    ProductListAdapter m_productListAdapter;
    CategoryListAdapter categoryListAdapter;
    SaleProductListAdapter saleProductListAdapter;

    RecyclerView rc_saleList;

    ImageView iv_stoimg;
    FragmentManager fragmentManager;

    DrawerLayout lay_drawer;

    Animation animation;

    private JTNetPosManager.RequestCallback requestCallback;
    List<Map<String, Object>> product_save;
    int appPrice = 0;
    String intMon = "00";
    int count = 0;
    Handler backHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            count = 0;
            return false;
        }
    });

    CountDownTimer resetTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }

        context = this;
        if (Pref.getPref() == null) {
            Pref.getInstance().init(this);
        }

        if (Pref.getPref().getBoolean(Cnst.USEPAY, false)) {
            JTNetPosManager.init(getApplicationContext());
        }

        Display display = getWindowManager().getDefaultDisplay();  // in Activity
        /* getActivity().getWindowManager().getDefaultDisplay() */ // in Fragment
        Point size = new Point();
        display.getRealSize(size); // or getSize(size)
        int width = size.x;
        int height = size.y;
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        Log.d("device dpi", "=>" + metrics.xdpi + " / " + metrics.ydpi);
        Log.e("화면크기", " width: " + width + " / height: " + height + " / " + metrics.densityDpi + " / " + metrics.density + " / ");


        Set<String> payFinList = Pref.getPref().getStringSet(Cnst.PayFinList, new HashSet<>());
        Log.e("확인", "결제완료리스트:" + payFinList.toString());
        fragmentManager = getSupportFragmentManager();
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        boolean isImmersiveModeEnabled = ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {
            Log.i("Is on?", "Turning immersive mode mode off. ");
        } else {
            Log.i("Is on?", "Turning immersive mode mode on.");
        }
        int newUiOptions = uiOptions;
        // 몰입 모드를 꼭 적용해야 한다면 아래의 3가지 속성을 모두 적용시켜야 합니다
        newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);

        Intent getIntent = getIntent();
        stoseq = Pref.getPref().getString(STOSEQ_, "");
        stonam = Pref.getPref().getString(STONAM_, "");
        stoimg = Pref.getPref().getString(STOIMG_, "");
        tblseq = Pref.getPref().getInt(TBLSEQ_, 0);
        tblnam = Pref.getPref().getString(TBLNAM_, "");
        flrseq = Pref.getPref().getInt(FLRSEQ_, 0);
        flrnam = Pref.getPref().getString(FLRNAM_, "");
        RCNSEQ = getIntent.getIntExtra("RCNSEQ", 0);
        id = Pref.getPref().getString(USERID_, "");
        pw = Pref.getPref().getString(USERPW_, "");
        if (!flrnam.equals(""))
            flrnam_ = flrnam + " - ";
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.setUserId(stonam + "-" + tblnam);
        crashlytics.setCustomKey("stoseq", stoseq);
        crashlytics.setCustomKey("stonam", stonam);
        crashlytics.setCustomKey("tblnam", tblnam);
        crashlytics.setCustomKey("flrseq", flrseq);
        crashlytics.setCustomKey("flrnam", flrnam);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        updateTableGcm(token);

                    }
                });

        iv_stoimg = findViewById(R.id.iv_stoimg);
        Glide.with(context).load(RetrofitAPI.getHost() + stoimg).transform(new RoundedCorners(100)).into(iv_stoimg);
        txt_table = findViewById(R.id.txt_table);
        lay_main = findViewById(R.id.lay_main);
        lay_right = findViewById(R.id.lay_right);
        rc_category = findViewById(R.id.rc_category);
        rc_product = findViewById(R.id.rc_product);
        lay_drawer = findViewById(R.id.lay_drawer);
        txt_toast = findViewById(R.id.txt_toast);
        String name = flrnam_ + tblnam;
        txt_table.setText(name);


        txt_salamt = findViewById(R.id.txt_salamt);
        txt_cancel = findViewById(R.id.txt_cancel);
        txt_order = findViewById(R.id.txt_order);
        lay_salcrt = findViewById(R.id.lay_salcrt);
        rc_saleList = findViewById(R.id.rc_saleList);
        lay_order_detail = findViewById(R.id.lay_order_detail);
        txt_cart_qty = findViewById(R.id.txt_cart_qty);

        txt_cancel.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                // CartOpenLogic(false);
                setDrawerViewClose();
            }
        });
        lay_salcrt.setOnClickListener(v -> {
            int uiOptions2 = getWindow().getDecorView().getSystemUiVisibility();
            int newUiOptions2 = uiOptions2;
            // 몰입 모드를 꼭 적용해야 한다면 아래의 3가지 속성을 모두 적용시켜야 합니다
            newUiOptions2 ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            newUiOptions2 ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
            newUiOptions2 ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            getWindow().getDecorView().setSystemUiVisibility(newUiOptions2);
            if (is_MainDrawerOpen()) {
                setDrawerViewClose();
            } else {
                if (productListData.size() != 0) {
                    setDrawerViewOpen();
                }
            }
        });

        txt_order.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                chkSale();
            }
        });

        saleProductListAdapter = new SaleProductListAdapter();
        rc_saleList.setLayoutManager(new LinearLayoutManager(context));
        rc_saleList.setAdapter(saleProductListAdapter);

        m_productListAdapter = new ProductListAdapter(this);
        rc_product.setLayoutManager(new GridLayoutManager(this, 4));
        rc_product.setAdapter(m_productListAdapter);

        categoryListAdapter = new CategoryListAdapter(this);
        rc_category.setLayoutManager(new LinearLayoutManager(this));
        rc_category.setAdapter(categoryListAdapter);

        lay_order_detail.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                getTable_getSale();
            }
        });

        animation = AnimationUtils.loadAnimation(context, R.anim.anim_toast);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                txt_toast.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                txt_toast.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        ConstraintLayout lay_call = findViewById(R.id.lay_call);
        lay_call.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                try {
                    CustomCallDialog callDialog = new CustomCallDialog(context, stoseq, tblseq, name);
                    callDialog.show();
                } catch (Exception e) {
                }
            }
        });

        iv_stoimg.setOnClickListener(v -> {
            backHandler.removeMessages(66);
            backHandler.sendEmptyMessageDelayed(66, 10000);
            count++;
            if (count > 20) {
                Intent intent = new Intent(context, SelectTableActivity.class);
                intent.putExtra("STOSEQ", stoseq);
                intent.putExtra("STONAM", stonam);
                intent.putExtra("STOIMG", stoimg);
                SharedPreferences.Editor editor = Pref.getPref().edit();
                editor.putString(USERID_, id);
                editor.putString(USERPW_, pw);
                editor.putInt(FLRSEQ_, -1);
                editor.putInt(TBLSEQ_, -1);

                editor.apply();
                startActivity(intent);
                finish();
            }
        });

        setCartQty();
        //getSale();
        getMain();

        if (!hasPermissions(getApplicationContext(), PERMISSIONS)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(PERMISSIONS, CODE_ALL_PERMISSION);
            }
        }

        requestCallback = new JTNetPosManager.RequestCallback() {
            @Override
            public void onResponse(Message msg) {

                byte[] response = msg.getData().getByteArray("RESPONSE_MSG");
                Log.e("asdf", "[응답2] : " + response.length + "||" + response[response.length - 1]);

                if (response != null) {
                    String strResData = StringUtil.byteArrayToString(response);
                    Log.e("Asdf", "[응답] : " + response.length + "||" + strResData);
                    paymentData = strResData;
                    cutStringByByte(paymentData, 5, "이거뭔데 :");
                    String gubun = cutStringByByte(paymentData, 4, "전문종류 :");
                    String appCatid = cutStringByByte(paymentData, 10, "단말기번호 :");
                    String oid = cutStringByByte(paymentData, 12, "POS번호 + 거래 일련번호 :");//POS번호 + 거래 일련번호
                    cutStringByByte(paymentData, 10, "S/W 버전 :"); // S/W 버전
                    cutStringByByte(paymentData, 10, "H/W 시리얼번호(리더기) :"); // H/W 시리얼번호(리더기)
                    cutStringByByte(paymentData, 32, "KTC인증번호 / 데몬 + 리더기 :"); //KTC인증번호 / 데몬 + 리더기
                    cutStringByByte(paymentData, 12, "전문 전송 일자 / “YYMMHHDDMMSS” :"); // 전문 전송 일자 / “YYMMHHDDMMSS”
                    cutStringByByte(paymentData, 5, "FILLER / Space :"); // FILLER / Space
                    cutStringByByte(paymentData, 1, "페킷타입 / 응답전문(정상:'S', 에러:'E') :"); // 페킷타입 / 응답전문(정상:'S', 에러:'E')
                    String resultCode = cutStringByByte(paymentData, 4, "응답코드 / 0000 : 정상, 그 외 에러코드(응답메시지 참조) :"); // 응답코드 / 0000 : 정상, 그 외 에러코드(응답메시지 참조)
                    String mag = cutStringByByte(paymentData, 36, "mag");
                    if (gubun.equals("1010") || gubun.equals("1050")) {
                        Log.e("승인관련", "승인번호:" + mag.substring(0, 12) + " / 승인일시:" +
                                mag.substring(12, 24) + " / 고유번호:" +
                                mag.substring(24, 36));
                        String appNo = mag.substring(0, 12);
                        String appDt = mag.substring(12, 24);
                        String tr = mag.substring(24, 36);// 승인 번호
                        cutStringByByte(paymentData, 15, "가맹점 번호 :"); //가맹점 번호
                        String issuerCd = cutStringByByte(paymentData, 4, "발급사 코드 :");//발급사 코드
                        String issuer_name = cutStringByByte(paymentData, 20, "발급사 명 :"); //발급사 명
                        String acquirerCd = cutStringByByte(paymentData, 4, "매입사 코드 :");//매입사 코드
                        String ACQUIRER_name = cutStringByByte(paymentData, 20, "매입사 명 :"); //매입사 명
                        String CARTYP = cutStringByByte(paymentData, 2, "카드구분 :");//카드구분
                        cutStringByByte(paymentData, 1, "매입 구분 :");//매입 구분
                        cutStringByByte(paymentData, 1, "EMV 데이터 옵션 :");//EMV 데이터 옵션
                        cutStringByByte(paymentData, 1, "카드사 전표출력 :");//카드사 전표출력
                        cutStringByByte(paymentData, 9, "할인금액 :");//할인금액
                        cutStringByByte(paymentData, 9, "발생포인트 :");//발생포인트
                        cutStringByByte(paymentData, 9, "가용포인트 :");//가용포인트
                        cutStringByByte(paymentData, 9, "누적포인트 :");//누적포인트
                        cutStringByByte(paymentData, 9, "기프트 잔액 :");//기프트 잔액
                        cutStringByByte(paymentData, 9, "카드사 포인트 :");//카드사 포인트
                        cutStringByByte(paymentData, 60, "포인트 정보 :"); //포인트 정보
                        cutStringByByte(paymentData, 16, "부가정보1 :"); //부가정보1
                        cutStringByByte(paymentData, 64, "부가정보2 :"); //부가정보2
                        cutStringByByte(paymentData, 128, "부가정보3 :"); //부가정보3
                        String cardNo = cutStringByByte(paymentData, 20, "출력 TRACK2DATA :"); //출력 TRACK2DATA / 결제카드번호
                        // cutStringByByte(paymentData, 28,"프린트 메시지1"); //프린트 메시지1
                        // cutStringByByte(paymentData, 28,"프린트 메시지2"); //프린트 메시지2
                        // cutStringByByte(paymentData, 28,"프린트 메시지3"); //프린트 메시지3
                        // cutStringByByte(paymentData, 28,"프린트 메시지4"); //프린트 메시지4
                        // cutStringByByte(paymentData, 28,"프린트 메시지5"); //프린트 메시지5
                        // cutStringByByte(paymentData, 28,"프린트 메시지6"); //프린트 메시지6
                        // cutStringByByte(paymentData, 400,"EMV 응답데이터"); //EMV 응답데이터
                        Log.e("뭐남았냐??", "|" + paymentData + "|");
                        //jtasd
                        setOrder(product_save, "01000000000", "0101", resultCode.trim(), appPrice,
                                0, intMon, appNo.trim(), appDt.trim(), acquirerCd.trim(), issuerCd.trim(), issuer_name.trim(), ACQUIRER_name.trim(),
                                appCatid.trim(), cardNo.trim(), CARTYP.trim(), oid.trim(), tr.trim(), "", stonam, false);
                    } else {
                        if (strResData.contains("사용자 종료 요청")) {
                            Log.e("결재 테스트", "true - " + strResData);
                        } else if (strResData.contains("시리얼 통신 연결 Error")) {
                            Log.e("결재 테스트", "true - " + strResData);
                        } else {
                            Log.e("결재 테스트", "false - " + strResData);
                            FirebaseCrashlytics.getInstance().log("실패 /" + strResData);
                            try {
                                throw new RuntimeException("실패 /" + strResData); // Force a crash
                            } catch (Exception e) {
                                FirebaseCrashlytics.getInstance().recordException(e);
                            }
                        }
                        SimpleAlertDialog alertDialog = new SimpleAlertDialog(context, "결제 실패:\n" + strResData.trim());
                        alertDialog.show();
                    }
                } else {
                    try {
                        FirebaseCrashlytics.getInstance().log("실패 /" + msg);
                        throw new RuntimeException(msg.toString()); // Force a crash
                    } catch (Exception e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                }
                JTNetPosManager.getInstance().destroy(getApplicationContext());
                JTNetPosManager.init(getApplicationContext());
            }
        };
        resetTimer = new CountDownTimer(180000, 60000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                productListData.clear();
                setCartQty();
                setTotalMoney();
                cust_ToastMSG("시간이 경과되어 초기화 되었습니다.");
            }
        };
    }

    private void askNotificationPermission() {
        // This is only necessary for API Level > 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private static final String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int CODE_ALL_PERMISSION = 1000;

    public boolean hasPermissions(Context _context, String[] _permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && _context != null && _permissions != null) {
            for (String permission : _permissions) {
                if (ActivityCompat.checkSelfPermission(_context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    void setCartQty() {
        int cart_qty = productListData.size();
        if (cart_qty == 0) {
            txt_cart_qty.setText("0");
            txt_cart_qty.setVisibility(View.GONE);
        } else {
            txt_cart_qty.setText(String.valueOf(cart_qty));
            txt_cart_qty.setVisibility(View.VISIBLE);
        }
    }

    public boolean is_MainDrawerOpen() {
        Log.e("생명주기", lay_drawer.isDrawerOpen(GravityCompat.END) + "");
        return lay_drawer.isDrawerOpen(GravityCompat.END);
    }

    public void setDrawerViewOpen() {
        Log.d("DefaultActivity", "열리냐!!!");
        lay_drawer.openDrawer(GravityCompat.END);
    }

    public void setDrawerViewClose() {
        Log.d("test", "닫히냐!!!");
        lay_drawer.closeDrawer(GravityCompat.END);
    }

    @Override
    protected void onResume() {
        super.onResume();

        int uiOptions2 = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions2 = uiOptions2;
        // 몰입 모드를 꼭 적용해야 한다면 아래의 3가지 속성을 모두 적용시켜야 합니다
        newUiOptions2 ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        newUiOptions2 ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        newUiOptions2 ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(newUiOptions2);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (resetTimer != null) {
            resetTimer.cancel();
        }
        backHandler.removeMessages(66);
        super.onDestroy();
    }


    void CartOpenLogic(boolean isOpen) {
        if (isOpen) {
            lay_right.setVisibility(View.VISIBLE);
            rc_product.setLayoutManager(new GridLayoutManager(this, 3));
            // saleProductListAdapter.notifyDataSetChanged();
            rc_product.invalidate();
        } else {
            lay_right.setVisibility(View.GONE);
            rc_product.setLayoutManager(new GridLayoutManager(this, 4));
            // saleProductListAdapter.notifyDataSetChanged();
            rc_product.invalidate();
        }
    }

    class CategoryListAdapter extends RecyclerView.Adapter<CategoryListAdapter.CategoryHolder> {
        Context context;
        ArrayList<CatagortyData> catagoryList;

        public void setCatagoryList(ArrayList<CatagortyData> catagoryList) {
            this.catagoryList = catagoryList;
        }

        public CategoryListAdapter(Context context) {
            this.context = context;
            catagoryList = new ArrayList<>();
        }

        @NonNull
        @Override
        public CategoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new CategoryHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_category_list, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull CategoryHolder holder, int position) {
            holder.bindview(position);
        }

        @Override
        public int getItemCount() {
            if (catagoryList != null) {
                return catagoryList.size();
            } else {
                return 0;
            }
        }

        class CategoryHolder extends RecyclerView.ViewHolder {
            ConstraintLayout lay_main;
            TextView txt_ctgnam;
            View v_dat;

            public CategoryHolder(@NonNull View itemView) {
                super(itemView);
                lay_main = itemView.findViewById(R.id.lay_main);
                txt_ctgnam = itemView.findViewById(R.id.txt_ctgname);
                v_dat = itemView.findViewById(R.id.v_dat);
            }

            void bindview(int pos) {
                SimpleJO jo = new SimpleJO(array_catagory.get(pos).getAsJsonObject());
                txt_ctgnam.setText(jo.getAsString("CTGNAM"));
                int CTGSEQ = jo.getAsInt("CTGSEQ");

                CatagortyData data = catagoryList.get(pos);
                txt_ctgnam.setText(Html.fromHtml(data.getCtgnam()));
                String ctgseq = data.getCtgseq();
                if (data.isSetSelect()) {
                    lay_main.setBackgroundResource(R.drawable.shp_catagory_selected);
                    txt_ctgnam.setTextColor(Color.parseColor("#F34645"));
                    v_dat.setVisibility(View.VISIBLE);
                } else {
                    lay_main.setBackgroundResource(R.drawable.shp_catagory);
                    txt_ctgnam.setTextColor(Color.parseColor("#ffffff"));
                    v_dat.setVisibility(View.GONE);
                }

                lay_main.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        product_select(CTGSEQ);
                        for (int i = 0; i < catagoryList.size(); i++) {
                            catagoryList.get(i).setSetSelect(false);
                        }
                        catagoryList.get(pos).setSetSelect(true);
                        v.setBackgroundResource(R.drawable.shp_catagory_selected);
                        notifyDataSetChanged();
                    }
                });


            }
        }
    }

    class CatagortyData {
        String ctgnam, ctgseq;
        boolean setSelect;

        public CatagortyData(String ctgnam, String ctgseq) {
            this.ctgnam = ctgnam;
            this.ctgseq = ctgseq;
            setSelect = false;
        }

        public String getCtgseq() {
            return ctgseq;
        }

        public String getCtgnam() {
            return ctgnam;
        }

        public boolean isSetSelect() {
            return setSelect;
        }

        public void setSetSelect(boolean setSelect) {
            this.setSelect = setSelect;
        }

    }

    class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ProductViewHolder> {
        Context context;

        public ProductListAdapter(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ProductViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_product_list, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
            holder.bindView(position);
        }

        @Override
        public int getItemCount() {
            if (array_product != null) return array_product.size();
            else return 0;
        }

        class ProductViewHolder extends RecyclerView.ViewHolder {
            ImageView iv_prdimg;

            ConstraintLayout lay_main;
            TextView txt_prdnam, txt_prdamt;

            public ProductViewHolder(@NonNull View itemView) {
                super(itemView);
                lay_main = itemView.findViewById(R.id.lay_main);
                iv_prdimg = itemView.findViewById(R.id.lay_prdimg);
                txt_prdnam = itemView.findViewById(R.id.txt_prdnam);
                txt_prdamt = itemView.findViewById(R.id.txt_prdamt);
            }

            void bindView(int pos) {
                SimpleJO jo = new SimpleJO(array_product.get(pos).getAsJsonObject());
                String prdnam = jo.getAsString("PRDNAM");
                int prdamt = jo.getAsInt("TOTAMT");
                String prdexp = jo.getAsString("PRDEXP");
                String prdimg = jo.getAsString("PRDIMG_PTH") + "/" + jo.getAsString("PRDIMG_NAM");
                Utils.loadImage(context, prdimg, iv_prdimg, 30);
                txt_prdnam.setText(prdnam);
                txt_prdamt.setText(Utils.getCurrencyString(prdamt));

                lay_main.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        getOption(array_product.get(pos).getAsJsonObject());
                    }
                });
                iv_prdimg.invalidate();
            }
        }

    }

    class SaleProductListAdapter extends RecyclerView.Adapter<SaleProductListAdapter.SeleProductListHolder> {
        private ArrayList<SaleProductData> saleProductDataArrayList;

        public SaleProductListAdapter() {
            saleProductDataArrayList = new ArrayList<>();
        }

        public void setSaleProductDataArrayList(ArrayList<SaleProductData> saleProductDataArrayList) {
            this.saleProductDataArrayList = saleProductDataArrayList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public SeleProductListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new SeleProductListHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_sale_product_list, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull SeleProductListHolder holder, int position) {
            holder.bindview(position);
        }

        @Override
        public int getItemCount() {
            return saleProductDataArrayList.size();
        }

        class SeleProductListHolder extends RecyclerView.ViewHolder {
            TextView txt_prdnam, txt_delect, txt_option, txt_prdamt, txt_salqty, txt_sumamt;
            ImageView iv_minus, iv_plus, iv_prdimg;

            public SeleProductListHolder(@NonNull View itemView) {
                super(itemView);
                txt_prdnam = itemView.findViewById(R.id.txt_prdnam);
                txt_delect = itemView.findViewById(R.id.txt_delect);
                txt_option = itemView.findViewById(R.id.txt_option);
                txt_prdamt = itemView.findViewById(R.id.txt_prdamt);
                txt_salqty = itemView.findViewById(R.id.txt_salqty);
                txt_sumamt = itemView.findViewById(R.id.txt_sumamt);
                iv_minus = itemView.findViewById(R.id.iv_minus);
                iv_plus = itemView.findViewById(R.id.iv_plus);
                iv_prdimg = itemView.findViewById(R.id.iv_prdimg);
            }

            void bindview(int pos) {
                SaleProductData productData = saleProductDataArrayList.get(pos);
                int prdamt = productData.getTotamt();
                int prdqty = productData.getSalqty();
                Utils.loadImage(context, productData.getPrdimg(), iv_prdimg, 25);
                txt_prdnam.setText(productData.getPrdnam());
                txt_option.setText(productData.getOptnam());
                txt_prdamt.setText(Utils.getCurrencyString(productData.getTotamt()));
                int sum = prdamt * prdqty;
                txt_sumamt.setText(Utils.getCurrencyString(sum));
                txt_salqty.setText(prdqty + "");
                iv_minus.setOnClickListener(new QtyClickListener(pos, -1));
                iv_plus.setOnClickListener(new QtyClickListener(pos, 1));
                txt_delect.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        saleProductDataArrayList.remove(pos);
                        setProductListData(saleProductDataArrayList);
                        notifyDataSetChanged();
                        // index = 0;
                        setTotalMoney();
                        setCartQty();
                        if (saleProductDataArrayList.size() == 0) {
                            setDrawerViewClose();
                        }
                    }
                });
            }

            class QtyClickListener implements View.OnClickListener {
                int pos, qty;

                public QtyClickListener(int pos, int qty) {
                    this.pos = pos;
                    this.qty = qty;
                }

                @Override
                public void onClick(View v) {
                    SaleProductData productData = saleProductDataArrayList.get(pos);
                    int nowqty = productData.getSalqty();
                    int preqty = productData.getPreqty();
                    nowqty += qty;
                    if (productData.getPreqty() == 0 && nowqty == 0) {
                        // productData.remove(pos);
                        // setProductListData(productData);
                        // notifyDataSetChanged();
                        // index = 0;
                        // setTotalMoney();
                    } else {
                        txt_salqty.setText(nowqty + "");
                        ;
                        productData.setSalqty(nowqty);
                        setProductListData(saleProductDataArrayList);
                        notifyDataSetChanged();
                        //index = 0;
                        setTotalMoney();
                    }

                }
            }
        }
    }


    ArrayList<CatagortyData> catagortyData;

    void getMain() {
        Afield af = new Afield();
        af.setF("STOSEQ", stoseq);
        Call<String> call = RetrofitAPI.getApiService().getMain_select(af.getF());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    SimpleJO jo = Utils.parseSimpleJo(response.body());
                    array_catagory = jo.getAsJsonArray("catagory");
                    array_catagory = jo.getAsJsonArray("catagory");
                    if (array_catagory.size() != 0) {
                        catagortyData = new ArrayList<>();
                        for (int i = 0; i < array_catagory.size(); i++) {
                            SimpleJO jjo = new SimpleJO(array_catagory.get(i).getAsJsonObject());
                            catagortyData.add(new CatagortyData(jjo.getAsString("CTGNAM"), jjo.getAsString("CTGSEQ")));
                            // Map<String, Object> catatagoryD = new HashMap<>();
                            // catatagoryD.put("CTGNAM", jjo.getAsString("CTGNAM"));
                            // catatagoryD.put("CTGSEQ", jjo.getAsString("CTGSEQ"));
                            // array_catagory.add(catatagoryD);
                        }
                        catagortyData.get(0).setSetSelect(true);
                        product_select(Integer.parseInt(catagortyData.get(0).getCtgseq()));
                        rc_category.setVisibility(View.VISIBLE);
                        categoryListAdapter.setCatagoryList(catagortyData);
                        categoryListAdapter.notifyDataSetChanged();
                    } else {
                        rc_category.setVisibility(View.GONE);
                    }
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    void getSale() {
        Afield af = new Afield();
        af.setF("USERID", id);
        af.setF("USERPW", pw);
        af.setF("STOSEQ", stoseq);
        af.setF("RCNTYP", "T");
        af.setF("TBLSEQ", tblseq);
        af.setF("RCNSEQ", RCNSEQ);
        Call<String> call = RetrofitAPI.getApiService().getSale(af.getF());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    SimpleJO jo = Utils.parseSimpleJo(response.body());
                    if (jo.getAsBoolean("flag")) {
                        array_catagory = jo.getAsJsonArray("catagory");
                        if (array_catagory.size() != 0) {
                            catagortyData = new ArrayList<>();
                            for (int i = 0; i < array_catagory.size(); i++) {
                                SimpleJO jjo = new SimpleJO(array_catagory.get(i).getAsJsonObject());
                                catagortyData.add(new CatagortyData(jjo.getAsString("CTGNAM"), jjo.getAsString("CTGSEQ")));
                                // Map<String, Object> catatagoryD = new HashMap<>();
                                // catatagoryD.put("CTGNAM", jjo.getAsString("CTGNAM"));
                                // catatagoryD.put("CTGSEQ", jjo.getAsString("CTGSEQ"));
                                // array_catagory.add(catatagoryD);
                            }
                            catagortyData.get(0).setSetSelect(true);
                            rc_category.setVisibility(View.VISIBLE);
                            categoryListAdapter.setCatagoryList(catagortyData);
                            categoryListAdapter.notifyDataSetChanged();
                        } else {
                            rc_category.setVisibility(View.GONE);
                        }


                        array_product = jo.getAsJsonArray("product");
                        if (array_product.size() != 0) {
                            rc_product.setVisibility(View.VISIBLE);
                            m_productListAdapter.notifyDataSetChanged();
                        } else {
                            rc_product.setVisibility(View.GONE);
                        }
                    } else {

                    }
                } else {

                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });

    }

    OrderDetailDialog orderDetailDialog;

    void getSale_orderDetail() {
        handlerOrderDetailDismiss.removeMessages(87);
        Afield af = new Afield();
        af.setF("USERID", id);
        af.setF("USERPW", pw);
        af.setF("STOSEQ", stoseq);
        af.setF("RCNTYP", "T");
        af.setF("TBLSEQ", tblseq);
        af.setF("RCNSEQ", RCNSEQ);
        Call<String> call = RetrofitAPI.getApiService().getSale(af.getF());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    SimpleJO jo = Utils.parseSimpleJo(response.body());
                    if (jo.getAsBoolean("flag")) {
                        SimpleJO pay_jo = new SimpleJO(jo.getASJsonObject("pay"));
                        int totamt = pay_jo.getAsInt("TOTAMT");
                        JsonArray array_order = jo.getAsJsonArray("order");
                        Log.e("주문내역 확인", array_order + "");
                        if (orderDetailDialog != null && orderDetailDialog.isShowing())
                            orderDetailDialog.dismiss();
                        orderDetailDialog = new OrderDetailDialog(context, array_order, totamt);
                        handlerOrderDetailDismiss.sendEmptyMessageDelayed(87, 10000);
                        orderDetailDialog.show();
                    } else {

                    }
                } else {

                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    void product_select(int ctgseq) {
        Afield af = new Afield();
        af.setF("STOSEQ", stoseq);
        af.setF("CTGSEQ", ctgseq);
        af.setF("PRDCOD", "'10', '11'");
        af.setF("SALFLG", "Y");

        //  af.setF("USERID", id);
        //  af.setF("USERPW", pw);
        //  af.setF("STOSEQ", stoseq);
        //  af.setF("CTGSEQ", ctgseq);
        Call<String> call = RetrofitAPI.getApiService().getProduct_select(af.getF());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    array_product = Utils.parseArray(response.body());
                    if (array_product.size() != 0) {
                        rc_product.setVisibility(View.VISIBLE);
                        m_productListAdapter.notifyDataSetChanged();
                    } else {
                        rc_product.setVisibility(View.GONE);
                    }
                    // SimpleJO jo = Utils.parseSimpleJo(response.body());
                    // if (jo.getAsBoolean("flag")) {
                    //     array_product = jo.getAsJsonArray("product");
                    //     if (array_product.size() != 0) {
                    //         rc_product.setVisibility(View.VISIBLE);
                    //         m_productListAdapter.notifyDataSetChanged();
                    //     } else {
                    //         rc_product.setVisibility(View.GONE);
                    //     }
                    // } else {

                    // }
                } else {

                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    CustomOptionDialog2 optionDialog2;

    void getOption(JsonObject product) {
        SimpleJO product_jo = new SimpleJO(product);
        if (product.get("OPTNAM1").isJsonNull() && product.get("OPTNAM2").isJsonNull() && product.get("OPTNAM3").isJsonNull()) {
            // Map<String, Object> map = new HashMap<String, Object>();
            // map.put("PRDSEQ", product_jo.getAsString("PRDSEQ"));
            // map.put("PRDNUM", product_jo.getAsString("PRDNUM"));
            // map.put("PRDNAM", product_jo.getAsString("PRDNAM"));
            // map.put("DTLNUM1", 0);
            // map.put("DTLNUM2", 0);
            // map.put("DTLNUM3", 0);
            // map.put("OPTNAM", null);
            // map.put("PRDIMG", product_jo.getAsString("PRDIMG_PTH") + "/" + product_jo.getAsString("PRDIMG_NAM"));
            // map.put("TOTAMT", product_jo.getAsString("TOTAMT"));

            // Gson gson = new Gson();
            // JsonObject object = (JsonObject) gson.toJsonTree(map);
            // new_SetOrder(object);

            Log.e("버그 확인", "옵션 없음");

            if (optionDialog2 != null && optionDialog2.isShowing())
                optionDialog2.dismiss();
            optionDialog2 = new CustomOptionDialog2(context, product_jo, this);
            try {
                optionDialog2.show();
            } catch (Exception e) {

            }

        } else {
            Afield af = new Afield();
            af.setF("USERID", id);
            af.setF("USERPW", pw);
            af.setF("STOSEQ", stoseq);
            af.setF("PRDSEQ", product_jo.getAsString("PRDSEQ"));
            af.setF("PRDNUM", product_jo.getAsString("PRDNUM"));
            Call<String> call = RetrofitAPI.getApiService().getOption(af.getF());
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful()) {
                        SimpleJO jo = Utils.parseSimpleJo(response.body());
                        if (jo.getAsBoolean("flag")) {
                            if (jo.getAsJsonArray("prddtl_1") != null) {
                                new_getOption(product_jo, jo);
                            } else {
                                JsonArray array_option = jo.getAsJsonArray("option");
                                if (array_option.size() == 1) {
                                    new_SetOrder(array_option.get(0).getAsJsonObject());
                                } else {

                                }
                            }

                        } else {

                        }
                    } else {

                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                }
            });
        }
    }

    CustomOptionDialog optionDialog;

    private void new_getOption(SimpleJO product, SimpleJO jo) {
        if (jo.getAsBoolean("flag")) {
            JsonArray prddtl_1, prddtl_2, prddtl_3;
            prddtl_1 = jo.getAsJsonArray("prddtl_1");
            prddtl_2 = jo.getAsJsonArray("prddtl_2");
            prddtl_3 = jo.getAsJsonArray("prddtl_3");
            if (optionDialog != null && optionDialog.isShowing())
                optionDialog.dismiss();
            optionDialog = new CustomOptionDialog(context, product, prddtl_1, prddtl_2, prddtl_3, this);
            try {
                optionDialog.show();
            } catch (Exception e) {

            }
        }
    }

    protected void new_SetOrder(JsonObject product) { // 상품 추가
        Log.e("뭐야?!", "응???");
        boolean newflg = true;
        product.addProperty("PREQTY", "0");
        product.addProperty("SALQTY", "1");
        product.addProperty("PRDSTS", "C");
        product.addProperty("TELNUM", "");
        product.addProperty("USERID", "POS");
        // product.addProperty("USERID", "POS");//원래는 이거 였으나 포스 주문인데 밑에 새롭게 추가되는것 때문에 변경
        SimpleJO jo = new SimpleJO(product);

        String prdseq = jo.getAsString("PRDSEQ");
        String prdnum = jo.getAsString("PRDNUM");
        String prdnam = jo.getAsString("PRDNAM");
        String dtlnum1 = jo.getAsString("DTLNUM1");
        String dtlnum2 = jo.getAsString("DTLNUM2");
        String dtlnum3 = jo.getAsString("DTLNUM3");

        // Toast.makeText(context, prdnam + "가 추가 되었습니다!", Toast.LENGTH_LONG).show();
        cust_ToastMSG(prdnam + "가 추가 되었습니다!");
        String PAYCOD = "10";

        for (int i = 0; i < productListData.size(); i++) {
            SaleProductData data = productListData.get(i);
            if (data.getPrdsts().equals("C")
                    && data.getPrdseq().equals(prdseq)
                    && data.getPrdnum().equals(prdnum)
                    && data.getDtlnum1().equals(dtlnum1)
                    && data.getDtlnum2().equals(dtlnum2)
                    && data.getDtlnum3().equals(dtlnum3)
                    //  && txt_userid.getText().toString().equals(activity.getUserid())
                    && data.getPaycod().equals(PAYCOD)
                    && data.getUserid().equals("POS")//원래는 이거 였으나 포스 주문인데 밑에 새롭게 추가되는것 때문에 변경
                    && data.getSalqty() != 0
            ) {
                newflg = false;
                int nowqty = productListData.get(i).getSalqty();
                int preqty = productListData.get(i).getPreqty();
                nowqty += 1;
                if (preqty != nowqty) {
                    productListData.get(i).setPrdsts("C");
                } else {
                    productListData.get(i).setPrdsts("");
                }
                productListData.get(i).setSalqty(nowqty);
                saleProductListAdapter.setSaleProductDataArrayList(productListData);
                // productListAdapter.adapter_SalqtyChange(i, 1);

                break;
            }
        }
        if (newflg) {
            addOrder(product);
        } else {
            setTotalMoney();
        }

    }

    public void setTotalMoney() {
        int totamt = 0;
        int paym = 0;
        for (int i = 0; i < productListData.size(); i++) {
            SaleProductData data = productListData.get(i);
            totamt += data.getTotamt() * data.getSalqty();
        }
        if (totamt != 0) {
            resetTimer.cancel();
            resetTimer.start();
        }
        txt_salamt.setText(Utils.getCurrencyString(totamt));
        // if (((SaleActivity) activity).pay != null) {
        //     paym = Utils.getInt(((SaleActivity) activity).pay, "NONAMT");
        //     Log.e("돈돈돈돈돈", String.valueOf(paym));
        //     txt_money.setText(Utils.getCurrencyString(totamt));
        //     txt_payamt.setText(Utils.getCurrencyString(paym));
        // }
    }

    private void addOrder(JsonObject product) {
        SimpleJO jo = new SimpleJO(product);
        int preqty = product.get("PREQTY") == null || product.get("PREQTY").isJsonNull() ? product.get("SALQTY").getAsInt() : product.get("PREQTY").getAsInt();
        productListData.add(0, new SaleProductData(jo.getAsString("PRDSEQ"),
                jo.getAsString("PRDNUM"),
                jo.getAsString("PRDSTS"),
                jo.getAsString("PRDNAM"),
                jo.getAsString("OPTNAM"),
                jo.getAsString("DTLNUM1"),
                jo.getAsString("DTLNUM2"),
                jo.getAsString("DTLNUM3"),
                jo.getAsInt("SALQTY"),
                preqty,
                jo.getAsInt("TOTAMT"),
                jo.getAsString("PAYCOD", "10"),
                jo.getAsString("RCNNUM", "1"),
                jo.getAsString("TELNUM", "TABLET"),
                jo.getAsString("USERID"),
                jo.getAsString("PRDIMG"),
                jo.getAsString("PRTNUM")
                ));
        saleProductListAdapter.setSaleProductDataArrayList(productListData);
        setTotalMoney();
        //CartOpenLogic(true);
        setCartQty();
        // setDrawerViewOpen();
    }

    public List<Map<String, Object>> getOrder() {
        List<Map<String, Object>> orders = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < productListData.size(); i++) {

            SaleProductData data = productListData.get(i);
            Map temp = new HashMap();
            temp.put("PRDSTS", data.getPrdsts());
            temp.put("PRDSEQ", data.getPrdseq());
            temp.put("PRDNUM", data.getPrdnum());
            temp.put("PAYCOD", data.getPaycod());
            temp.put("DTLNUM1", data.getDtlnum1());
            temp.put("DTLNUM2", data.getDtlnum2());
            temp.put("DTLNUM3", data.getDtlnum3());
            temp.put("USERID", data.getUserid());
            temp.put("PREQTY", data.getPreqty());
            temp.put("SALQTY", data.getSalqty());
            temp.put("TELNUM", data.getTelnum());
            temp.put("TOTAMT", data.getTotamt());
            temp.put("RCNNUM", data.getRcnnum());
            temp.put("PRTNUM", data.getPrtnum());
            /*
            String salqty = txt_salqty.getText().toString();
            temp.put("PRDSTS", txt_prdsts.getText().toString());
            temp.put("PRDSEQ", txt_prdseq.getText().toString());
            temp.put("PRDNUM", txt_prdnum.getText().toString());
            temp.put("PAYCOD", txt_paycod.getText().toString());
            temp.put("DTLNUM1", txt_dtlnum1.getText().toString());
            temp.put("DTLNUM2", txt_dtlnum2.getText().toString());
            temp.put("DTLNUM3", txt_dtlnum3.getText().toString());
            temp.put("USERID", txt_userid.getText().toString());
            temp.put("PREQTY", txt_preqty.getText().toString());
            temp.put("SALQTY", salqty);
            temp.put("TELNUM", txt_telnum.getText().toString());
            temp.put("TOTAMT", txt_totamt.getText().toString());
            temp.put("RCNNUM", txt_rcnnum.getText().toString());

            Log.e("getOrder주문", "\nPRDSTS : " + txt_prdsts.getText().toString() + " / " + data.getPrdsts()
                    + "\nPRDSEQ : " + txt_prdseq.getText().toString() + " / " + data.getPrdseq()
                    + "\nPRDNUM : " + txt_prdnum.getText().toString() + " / " + data.getPrdnum()
                    + "\n상품명 : " + txt_prdnam.getText().toString() + " / " + data.getPrdnam()
                    + "\nPAYCOD : " + txt_paycod.getText().toString() + " / " + data.getPaycod()
                    + "\nDTLNUM1 : " + txt_dtlnum1.getText().toString() + " / " + data.getDtlnum1()
                    + "\nDTLNUM2 : " + txt_dtlnum2.getText().toString() + " / " + data.getDtlnum2()
                    + "\nDTLNUM3 : " + txt_dtlnum3.getText().toString() + " / " + data.getDtlnum3()
                    + "\n상품개수(preqty) : " + txt_preqty.getText().toString() + " / " + data.getPreqty()
                    + "\n상품개수(salqty) : " + txt_salqty.getText().toString() + " / " + data.getSalqty()
                    + "\n주문ID : " + txt_userid.getText().toString() + " / " + data.getUserid()
                    + "\n폰번호 : " + txt_telnum.getText().toString() + " / " + data.getTelnum()
                    + "\nRCNNUM : " + txt_rcnnum.getText().toString() + " / " + data.getRcnnum() + " /"
            );
            */
            orders.add(temp);

        }
        return orders;
    }

    void chkSale() {
        boolean check = false;
        List<Map<String, Object>> product = getOrder();
        if (product.size() == 0) {
            Toast.makeText(context, "주문하실 상품이 없습니다.", Toast.LENGTH_LONG).show();
            return;
        }
        Log.e("아ㅓ아아아아", RCNSEQ + "");
        if (RCNSEQ == 0) {
            for (int i = 0; i < product.size(); i++) {
                String qty = product.get(i).get("SALQTY").toString();
                if (!qty.equals("0")) {
                    check = true;
                    break;
                }
            }
        } else {
            check = true;
        }

        if (check) {    // 빌드업 테스트 확인
            getTable(product);
        } else {
            Toast.makeText(context, "주문하실 상품이 없습니다.", Toast.LENGTH_LONG).show();
        }
    }

    private void setSale(List<Map<String, Object>> product) {
        // setDestroy(true);
        Map<String, Object> jsonData = new HashMap<String, Object>();
        jsonData.put("S_USERID", id);
        jsonData.put("USERID", id);
        jsonData.put("USERPW", pw);
        jsonData.put("STOSEQ", stoseq);
        jsonData.put("RCNTYP", "T");
        jsonData.put("RCNSEQ", RCNSEQ);
        jsonData.put("product", product);
        jsonData.put("EQUTYP", 'T'); //
        jsonData.put("FLRSEQ", flrseq);
        jsonData.put("TBLSEQ", tblseq);


        Gson gson = new Gson();
        String jsonDataStr = gson.toJson(jsonData);
        Call<String> call = RetrofitAPI.getApiService().setSale(jsonDataStr);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (Utils.api_Successful(response)) {
                    SimpleJO jo = Utils.parseSimpleJo(response.body());
                    String message = jo.getAsString("message");
                    if (jo.getAsBoolean("flag")) {
                        RCNSEQ = jo.getAsInt("RCNSEQ");
                        setProductListData(new ArrayList<>());
                        saleProductListAdapter.setSaleProductDataArrayList(productListData);
                        // CartOpenLogic(false);
                        setDrawerViewClose();
                        getTable_getSale();
                        setCartQty();
                    } else {
                        FirebaseCrashlytics.getInstance().setCustomKey("API", "setSale - mobile/pos/sale.do");
                        FirebaseCrashlytics.getInstance().setCustomKey("API_state", "리스폰스O,Successful O, flag X");
                        FirebaseCrashlytics.getInstance().log("실패1 /" + jsonDataStr);
                        FirebaseCrashlytics.getInstance().log("실패결과2 /" + response.body());
                        try {
                            throw new RuntimeException("실패1 /" + jsonDataStr); // Force a crash
                        } catch (Exception e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                    }
                } else {
                    FirebaseCrashlytics.getInstance().setCustomKey("API", "setSale - mobile/pos/sale.do");
                    FirebaseCrashlytics.getInstance().setCustomKey("API_state", "리스폰스O,Successful X");
                    FirebaseCrashlytics.getInstance().log("실패1 /" + jsonDataStr);
                    FirebaseCrashlytics.getInstance().log("실패결과2 /" + response.toString());
                    try {
                        throw new RuntimeException("실패2 /" + jsonDataStr); // Force a crash
                    } catch (Exception e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                FirebaseCrashlytics.getInstance().log("실패 /" + jsonDataStr);
                FirebaseCrashlytics.getInstance().setCustomKey("API", "setSale - mobile/pos/sale.do");
                FirebaseCrashlytics.getInstance().setCustomKey("API_state", "리스폰스X");
                FirebaseCrashlytics.getInstance().recordException(t);
            }
        });
    }


    void saveOrderData(List<Map<String, Object>> product, String ordernum,
                       String gubun, String resultCode, int appPrice, int bugase,
                       String intMon, String appNo, String appDt, String acquirerCd,
                       String issuerCd, String issuer_name, String ACQUIRER_name, String appCatid, String cardNo, String CARTYP,
                       String oid, String trNo, String alrtmsg, String store_num) {
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("paymentSteps ", 1);
        jsonData.put("ordernum", ordernum);
        jsonData.put("gubun", gubun);
        jsonData.put("resultCode", resultCode);
        jsonData.put("appPrice", appPrice);
        jsonData.put("intMon", intMon);
        jsonData.put("appNo", appNo);
        jsonData.put("appDt", appDt);
        jsonData.put("acquirerCd", acquirerCd);
        jsonData.put("issuerCd", issuerCd);
        jsonData.put("issuer_name", issuer_name);
        jsonData.put("ACQUIRER_name", ACQUIRER_name);
        jsonData.put("appCatid", appCatid);
        jsonData.put("cardNo", cardNo);
        jsonData.put("CARTYP", CARTYP);
        jsonData.put("oid", oid);
        jsonData.put("trNo", trNo);
        jsonData.put("alrtmsg", alrtmsg);
        jsonData.put("bugase", bugase);
        jsonData.put("store_num", store_num);
        jsonData.put("product", product);
        Gson gson = new Gson();
        String jsonDataStr = gson.toJson(jsonData);

        FirebaseCrashlytics.getInstance().log("실패 saveOrderData / " + jsonDataStr);
        SharedPreferences.Editor editor = Pref.getInstance().init(context).edit();
        editor.putString(Cnst.saveSaleData, jsonDataStr);
        editor.apply();
    }

    void setOrder(List<Map<String, Object>> product, String ordernum,
                  String gubun, String resultCode, int appPrice, int bugase,
                  String intMon, String appNo, String appDt, String acquirerCd,
                  String issuerCd, String issuer_name, String ACQUIRER_name, String appCatid, String cardNo, String CARTYP,
                  String oid, String trNo, String alrtmsg, String store_num, boolean isSave) {
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("S_USERID", id);
        jsonData.put("USERID", id);
        jsonData.put("USERPW", pw);
        jsonData.put("STOSEQ", stoseq);
        jsonData.put("RCNTYP", "T");
        jsonData.put("EQUTYP", 'T'); // 1
        jsonData.put("RCNSEQ", RCNSEQ);
        jsonData.put("product", product);
        jsonData.put("FLRSEQ", flrseq);
        jsonData.put("TBLSEQ", tblseq);
        jsonData.put("setPay", "p");

        Gson gson = new Gson();
        String jsonDataStr = gson.toJson(jsonData);

        Call<String> call_setOrder = RetrofitAPI.getApiService().setSale(jsonDataStr);
        call_setOrder.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SimpleJO jo = Utils.parseSimpleJo(response.body());
                    boolean flag = jo.getAsBoolean("flag");
                    if (flag) {
                        try {
                            SharedPreferences.Editor editor = Pref.getInstance().init(context).edit();
                            editor.remove(Cnst.saveSaleData);
                            editor.apply();
                            String rcnseq = jo.getAsString("RCNSEQ");
                            RCNSEQ = Integer.parseInt(rcnseq);
                            int tot = jo.getAsInt("TOTAMT", 0);
                            JsonArray product = jo.getAsJsonArray("product");
                            int totamt = 0;
                            if (product != null && product.size() != 0) {
                                for (int i = 0; i < product.size(); i++) {
                                    SimpleJO jjo = new SimpleJO(product.get(i).getAsJsonObject());
                                    int salqty = jjo.getAsInt("SALQTY", 0);
                                    int pro_totamt = jjo.getAsInt("TOTAMT", 0);
                                    totamt = totamt + (pro_totamt * salqty);
                                }
                                int payamt = totamt - tot;
                                // Bundle bundle = new Bundle();
                                // bundle.putString("rcnseq", rcnseq);
                                // bundle.putString("product", product + "");
                                // bundle.putInt("payamt", payamt);

                                //    HalbuDialog(rcnseq, payamt, product + "");
                                // 결제 화면으로 이동
                                setPay(rcnseq, payamt, product, ordernum, gubun, resultCode, appPrice,
                                        bugase, intMon, appNo, appDt, acquirerCd,
                                        issuerCd, issuer_name, ACQUIRER_name, appCatid, cardNo, CARTYP,
                                        oid, trNo, alrtmsg, store_num, false);
                            }
                        } catch (Exception e) {
                            if (!isSave) {
                                setOrder(product_save, "01000000000", "0101", resultCode.trim(), appPrice,
                                        0, intMon, appNo.trim(), appDt.trim(), acquirerCd.trim(), issuerCd.trim(), issuer_name.trim(), ACQUIRER_name.trim(),
                                        appCatid.trim(), cardNo.trim(), CARTYP.trim(), oid.trim(), trNo, "", stonam, true);
                                saveOrderData(product_save, "01000000000", "0101", resultCode.trim(), appPrice,
                                        0, intMon, appNo.trim(), appDt.trim(), acquirerCd.trim(), issuerCd.trim(), issuer_name.trim(), ACQUIRER_name.trim(),
                                        appCatid.trim(), cardNo.trim(), CARTYP.trim(), oid.trim(), trNo, "", stonam);
                            }
                            FirebaseCrashlytics.getInstance().log("실패 /" + jsonDataStr);
                            FirebaseCrashlytics.getInstance().setCustomKey("API", "setOrder - mobile/pos/sale.do");
                            FirebaseCrashlytics.getInstance().setCustomKey("API_state", "리스폰스O,Successful O, flag O, try-catch");
                            FirebaseCrashlytics.getInstance().recordException(e);

                        }
                    } else {
                        if (!isSave) {
                            setOrder(product_save, "01000000000", "0101", resultCode.trim(), appPrice,
                                    0, intMon, appNo.trim(), appDt.trim(), acquirerCd.trim(), issuerCd.trim(), issuer_name.trim(), ACQUIRER_name.trim(),
                                    appCatid.trim(), cardNo.trim(), CARTYP.trim(), oid.trim(), trNo, "", stonam, true);
                            saveOrderData(product_save, "01000000000", "0101", resultCode.trim(), appPrice,
                                    0, intMon, appNo.trim(), appDt.trim(), acquirerCd.trim(), issuerCd.trim(), issuer_name.trim(), ACQUIRER_name.trim(),
                                    appCatid.trim(), cardNo.trim(), CARTYP.trim(), oid.trim(), trNo, "", stonam);
                        }
                        FirebaseCrashlytics.getInstance().setCustomKey("API", "setOrder - mobile/pos/sale.do");
                        FirebaseCrashlytics.getInstance().setCustomKey("API_state", "리스폰스O,Successful O, flag X");
                        FirebaseCrashlytics.getInstance().log("실패1 /" + jsonDataStr);
                        FirebaseCrashlytics.getInstance().log("실패결과2 /" + response.toString());
                        try {
                            throw new RuntimeException("실패2 /" + jsonDataStr); // Force a crash
                        } catch (Exception e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                    }
                } else {
                    if (!isSave) {
                        setOrder(product_save, "01000000000", "0101", resultCode.trim(), appPrice,
                                0, intMon, appNo.trim(), appDt.trim(), acquirerCd.trim(), issuerCd.trim(), issuer_name.trim(), ACQUIRER_name.trim(),
                                appCatid.trim(), cardNo.trim(), CARTYP.trim(), oid.trim(), trNo, "", stonam, true);
                        saveOrderData(product_save, "01000000000", "0101", resultCode.trim(), appPrice,
                                0, intMon, appNo.trim(), appDt.trim(), acquirerCd.trim(), issuerCd.trim(), issuer_name.trim(), ACQUIRER_name.trim(),
                                appCatid.trim(), cardNo.trim(), CARTYP.trim(), oid.trim(), trNo, "", stonam);
                    }
                    FirebaseCrashlytics.getInstance().setCustomKey("API", "setOrder - mobile/pos/sale.do");
                    FirebaseCrashlytics.getInstance().setCustomKey("API_state", "리스폰스O,Successful X");
                    FirebaseCrashlytics.getInstance().log("실패1 /" + jsonDataStr);
                    FirebaseCrashlytics.getInstance().log("실패결과2 /" + response.toString());
                    try {
                        throw new RuntimeException("실패2 /" + jsonDataStr); // Force a crash
                    } catch (Exception e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                if (!isSave) {
                    setOrder(product_save, "01000000000", "0101", resultCode.trim(), appPrice,
                            0, intMon, appNo.trim(), appDt.trim(), acquirerCd.trim(), issuerCd.trim(), issuer_name.trim(), ACQUIRER_name.trim(),
                            appCatid.trim(), cardNo.trim(), CARTYP.trim(), oid.trim(), trNo, "", stonam, true);
                    saveOrderData(product_save, "01000000000", "0101", resultCode.trim(), appPrice,
                            0, intMon, appNo.trim(), appDt.trim(), acquirerCd.trim(), issuerCd.trim(), issuer_name.trim(), ACQUIRER_name.trim(),
                            appCatid.trim(), cardNo.trim(), CARTYP.trim(), oid.trim(), trNo, "", stonam);
                }
                FirebaseCrashlytics.getInstance().log("실패1 /" + jsonDataStr);
                FirebaseCrashlytics.getInstance().setCustomKey("API", "setOrder - mobile/pos/sale.do");
                FirebaseCrashlytics.getInstance().setCustomKey("API_state", "리스폰스X");
                FirebaseCrashlytics.getInstance().recordException(t);
            }
        });
    }

    void savePayData(String rcnseq, int payamt, JsonArray product_array, String ordernum, String gubun, String resultCode, int appPrice, int bugase,
                     String intMon, String appNo, String appDt, String acquirerCd,
                     String issuerCd, String issuer_name, String ACQUIRER_name, String appCatid, String cardNo, String CARTYP,
                     String oid, String trNo, String alrtmsg, String store_num) {
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("paymentSteps ", 2);
        jsonData.put("rcnseq ", rcnseq);
        jsonData.put("payamt ", payamt);
        jsonData.put("ordernum", ordernum);
        jsonData.put("gubun", gubun);
        jsonData.put("resultCode", resultCode);
        jsonData.put("appPrice", appPrice);
        jsonData.put("intMon", intMon);
        jsonData.put("appNo", appNo);
        jsonData.put("appDt", appDt);
        jsonData.put("acquirerCd", acquirerCd);
        jsonData.put("issuerCd", issuerCd);
        jsonData.put("issuer_name", issuer_name);
        jsonData.put("ACQUIRER_name", ACQUIRER_name);
        jsonData.put("appCatid", appCatid);
        jsonData.put("cardNo", cardNo);
        jsonData.put("CARTYP", CARTYP);
        jsonData.put("oid", oid);
        jsonData.put("trNo", trNo);
        jsonData.put("alrtmsg", alrtmsg);
        jsonData.put("bugase", bugase);
        jsonData.put("store_num", store_num);
        jsonData.put("product", product_array);
        Gson gson = new Gson();
        String jsonDataStr = gson.toJson(jsonData);
        FirebaseCrashlytics.getInstance().log("실패 savePayData / " + jsonDataStr);
        SharedPreferences.Editor editor = Pref.getInstance().init(context).edit();
        editor.putString(Cnst.saveSaleData, jsonDataStr);
        editor.apply();
    }

    void setPay(String rcnseq, int payamt, JsonArray product_array, String ordernum, String gubun, String resultCode, int appPrice, int bugase,
                String intMon, String appNo, String appDt, String acquirerCd,
                String issuerCd, String issuer_name, String ACQUIRER_name, String appCatid, String cardNo, String CARTYP,
                String oid, String trNo, String alrtmsg, String store_num, boolean isSave) {
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("S_USERID", id);
        jsonData.put("USERID", id);
        jsonData.put("USERPW", pw);
        jsonData.put("STOSEQ", stoseq);
        jsonData.put("RCNTYP", "T");
        jsonData.put("FLRSEQ", flrseq);
        jsonData.put("TBLSEQ", tblseq);
        jsonData.put("RCNSEQ", rcnseq);
        jsonData.put("CSHAMT", payamt);
        jsonData.put("SAL005", "30");
        jsonData.put("INPAMT", payamt);
        // jsonData.put("EQUTYP", "T"); //2
        jsonData.put("product", product_array);
        Gson gson = new Gson();
        String jsonDataStr = gson.toJson(jsonData);
        RetrofitAPI.getApiService().setPay(jsonDataStr).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SimpleJO jo = Utils.parseSimpleJo(response.body());
                    boolean flag = jo.getAsBoolean("flag");
                    if (flag) {
                        try {
                            SharedPreferences.Editor editor = Pref.getInstance().init(context).edit();
                            editor.remove(Cnst.saveSaleData);
                            editor.apply();
                            String RCTSEQ = jo.getAsString("RCTSEQ", "0");

                            String bizno = jo.getAsString("BIZNO", "");
                            String catid = jo.getAsString("CATID", "");
                            if (catid.equals("") || bizno.equals("")) {
                                FirebaseCrashlytics.getInstance().setCustomKey("API", "setPay - mobile/pos/setPay.do");
                                FirebaseCrashlytics.getInstance().setCustomKey("API_state", "catid.equals(\"\") || bizno.equals(\"\") - false 이게 왜 없는데?");
                                FirebaseCrashlytics.getInstance().log("실패1 /" + jsonDataStr);
                                FirebaseCrashlytics.getInstance().log("실패결과2 /" + response.toString());
                                try {
                                    throw new RuntimeException("실패2 /" + jsonDataStr); // Force a crash
                                } catch (Exception e) {
                                    FirebaseCrashlytics.getInstance().recordException(e);
                                }
                            } else {
                                // if (false) {
                                //     Bundle bundle = new Bundle();
                                //     bundle.putString("resultCode", resultCode);
                                //     //  bundle.putString("merchantReserved1", rctseq);
                                //     bundle.putString("where", "sale");
                                //     if (resultCode.equals("0000")) {
                                //         bundle.putString("appNo", appNo);
                                //         bundle.putInt("appPrice", appPrice);
                                //         bundle.putInt("bugase", bugase);
                                //         bundle.putString("cardNo", cardNo);
                                //         bundle.putString("issuerCd", issuerCd);
                                //         bundle.putString("acquirerCd", acquirerCd);

                                //         bundle.putString("issuer_name", issuer_name);
                                //         bundle.putString("ACQUIRER_name", ACQUIRER_name);
                                //         bundle.putString("intMon", intMon);
                                //         bundle.putString("appDt", appDt);
                                //         bundle.putString("trNo", trNo);
                                //         bundle.putString("bizno", bizno);
                                //         bundle.putString("store_num", store_num);
                                //     }
                                //     bundle.putBoolean("flag", jo.getAsBoolean("flag"));
                                //     bundle.putString("msg", alrtmsg);
                                //     bundle.putString("SAL005", "30");
                                //     bundle.putString("ordnum", ordernum.substring(7, 11));
                                //     bundle.putString("product", product_array + "");
                                //     bundle.putBoolean("usePay", true);
                                //     // activity.moveActivity(OrderFinishActivity.class, bundle);
                                //     return;
                                // } else {
                                NicePosPaymentResult2(ordernum, product_array, RCTSEQ, gubun, resultCode, appPrice, bugase,
                                        intMon, appNo, appDt, acquirerCd,
                                        issuerCd, issuer_name, ACQUIRER_name, appCatid, cardNo, CARTYP,
                                        oid, trNo, alrtmsg, bizno, store_num, rcnseq, false);
                                return;
                                // }
                            }
                        } catch (Exception t) {
                            if (!isSave) {
                                setPay(rcnseq, payamt, product_array, ordernum, gubun, resultCode, appPrice,
                                        bugase, intMon, appNo, appDt, acquirerCd,
                                        issuerCd, issuer_name, ACQUIRER_name, appCatid, cardNo, CARTYP,
                                        oid, trNo, alrtmsg, store_num, true);
                                savePayData(rcnseq, payamt, product_array, ordernum, gubun, resultCode, appPrice,
                                        bugase, intMon, appNo, appDt, acquirerCd,
                                        issuerCd, issuer_name, ACQUIRER_name, appCatid, cardNo, CARTYP,
                                        oid, trNo, alrtmsg, store_num);
                            }
                            FirebaseCrashlytics.getInstance().log("실패1 /" + jsonDataStr);
                            FirebaseCrashlytics.getInstance().setCustomKey("API", "setPay - mobile/pos/setPay.do");
                            FirebaseCrashlytics.getInstance().setCustomKey("API_state", "리스폰스O,Successful O, flag O, try-catch");
                            FirebaseCrashlytics.getInstance().recordException(t);
                        }
                    } else {

                        if (!isSave) {
                            setPay(rcnseq, payamt, product_array, ordernum, gubun, resultCode, appPrice,
                                    bugase, intMon, appNo, appDt, acquirerCd,
                                    issuerCd, issuer_name, ACQUIRER_name, appCatid, cardNo, CARTYP,
                                    oid, trNo, alrtmsg, store_num, true);
                            savePayData(rcnseq, payamt, product_array, ordernum, gubun, resultCode, appPrice,
                                    bugase, intMon, appNo, appDt, acquirerCd,
                                    issuerCd, issuer_name, ACQUIRER_name, appCatid, cardNo, CARTYP,
                                    oid, trNo, alrtmsg, store_num);
                        }
                        FirebaseCrashlytics.getInstance().setCustomKey("API", "setPay - mobile/pos/setPay.do");
                        FirebaseCrashlytics.getInstance().setCustomKey("API_state", "리스폰스O,Successful O, flag X");
                        FirebaseCrashlytics.getInstance().log("실패1 /" + jsonDataStr);
                        FirebaseCrashlytics.getInstance().log("실패결과2 /" + response.toString());
                        try {
                            throw new RuntimeException("실패2 /" + jsonDataStr); // Force a crash
                        } catch (Exception e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                    }
                } else {
                    if (!isSave) {
                        setPay(rcnseq, payamt, product_array, ordernum, gubun, resultCode, appPrice,
                                bugase, intMon, appNo, appDt, acquirerCd,
                                issuerCd, issuer_name, ACQUIRER_name, appCatid, cardNo, CARTYP,
                                oid, trNo, alrtmsg, store_num, true);
                        savePayData(rcnseq, payamt, product_array, ordernum, gubun, resultCode, appPrice,
                                bugase, intMon, appNo, appDt, acquirerCd,
                                issuerCd, issuer_name, ACQUIRER_name, appCatid, cardNo, CARTYP,
                                oid, trNo, alrtmsg, store_num);
                    }
                    FirebaseCrashlytics.getInstance().setCustomKey("API", "setPay - mobile/pos/setPay.do");
                    FirebaseCrashlytics.getInstance().setCustomKey("API_state", "리스폰스O,Successful X");
                    FirebaseCrashlytics.getInstance().log("실패1 /" + jsonDataStr);
                    FirebaseCrashlytics.getInstance().log("실패결과2 /" + response.toString());
                    try {
                        throw new RuntimeException("실패2 /" + jsonDataStr); // Force a crash
                    } catch (Exception e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                if (!isSave) {
                    setPay(rcnseq, payamt, product_array, ordernum, gubun, resultCode, appPrice,
                            bugase, intMon, appNo, appDt, acquirerCd,
                            issuerCd, issuer_name, ACQUIRER_name, appCatid, cardNo, CARTYP,
                            oid, trNo, alrtmsg, store_num, true);
                    savePayData(rcnseq, payamt, product_array, ordernum, gubun, resultCode, appPrice,
                            bugase, intMon, appNo, appDt, acquirerCd,
                            issuerCd, issuer_name, ACQUIRER_name, appCatid, cardNo, CARTYP,
                            oid, trNo, alrtmsg, store_num);
                }
                FirebaseCrashlytics.getInstance().log("실패1 /" + jsonDataStr);
                FirebaseCrashlytics.getInstance().setCustomKey("API", "setPay - mobile/pos/setPay.do");
                FirebaseCrashlytics.getInstance().setCustomKey("API_state", "리스폰스X");
                FirebaseCrashlytics.getInstance().recordException(t);
            }
        });
    }

    void saveNicePosPaymentResultData(String ordernum, JsonArray product_array, String rctseq,
                                      String gubun, String resultCode, int appPrice, int bugase, String intMon,
                                      String appNo, String appDt, String acquirerCd, String issuerCd,
                                      String issuer_name, String ACQUIRER_name, String appCatid,
                                      String cardNo, String CARTYP, String oid, String trNo,
                                      String alrtmsg, String bizno, String store_num, String rcnseq) {
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("paymentSteps ", 2);
        jsonData.put("rcnseq ", rcnseq);
        jsonData.put("rctseq ", rctseq);
        jsonData.put("bizno ", bizno);
        jsonData.put("ordernum", ordernum);
        jsonData.put("gubun", gubun);
        jsonData.put("resultCode", resultCode);
        jsonData.put("appPrice", appPrice);
        jsonData.put("intMon", intMon);
        jsonData.put("appNo", appNo);
        jsonData.put("appDt", appDt);
        jsonData.put("acquirerCd", acquirerCd);
        jsonData.put("issuerCd", issuerCd);
        jsonData.put("issuer_name", issuer_name);
        jsonData.put("ACQUIRER_name", ACQUIRER_name);
        jsonData.put("appCatid", appCatid);
        jsonData.put("cardNo", cardNo);
        jsonData.put("CARTYP", CARTYP);
        jsonData.put("oid", oid);
        jsonData.put("trNo", trNo);
        jsonData.put("alrtmsg", alrtmsg);
        jsonData.put("bugase", bugase);
        jsonData.put("store_num", store_num);
        jsonData.put("product", product_array);
        Gson gson = new Gson();
        String jsonDataStr = gson.toJson(jsonData);
        FirebaseCrashlytics.getInstance().log("실패 saveNicePosPaymentResultData / " + jsonDataStr);
        SharedPreferences.Editor editor = Pref.getInstance().init(context).edit();
        editor.putString(Cnst.saveSaleData, jsonDataStr);
        editor.apply();
    }

    void NicePosPaymentResult2(String ordernum, JsonArray product_array, String rctseq,
                               String gubun, String resultCode, int appPrice, int bugase, String intMon,
                               String appNo, String appDt, String acquirerCd, String issuerCd,
                               String issuer_name, String ACQUIRER_name, String appCatid,
                               String cardNo, String CARTYP, String oid, String trNo,
                               String alrtmsg, String bizno, String store_num, String rcnseq, boolean isSave) {
        Bundle bundle = new Bundle();
        bundle.putString("resultCode", resultCode);
        bundle.putString("merchantReserved1", rctseq);
        bundle.putString("where", "sale");
        if (resultCode.equals("0000")) {
            bundle.putString("appNo", appNo);
            bundle.putInt("appPrice", appPrice);
            bundle.putInt("bugase", bugase);
            bundle.putString("cardNo", cardNo);
            bundle.putString("issuerCd", issuerCd);
            bundle.putString("acquirerCd", acquirerCd);
            bundle.putString("issuer_name", issuer_name);
            bundle.putString("ACQUIRER_name", ACQUIRER_name);
            bundle.putString("intMon", intMon);
            bundle.putString("appDt", appDt);
            bundle.putString("bizno", bizno);
            bundle.putString("trNo", trNo);
            bundle.putString("store_num", store_num);
        }
        Map<String, String> jsonData = new HashMap<>();
        jsonData.put("RCTSEQ", rctseq);
        jsonData.put("gubun", gubun);
        jsonData.put("S_USERID", id);
        // jsonData.put("EQUTYP", "T"); // 3
        jsonData.put("resultCode", resultCode);
        jsonData.put("appPrice", appPrice + "");
        jsonData.put("intMon", intMon);
        jsonData.put("appNo", appNo);
        jsonData.put("appDt", appDt);
        jsonData.put("ACQUIRERCD", acquirerCd);
        jsonData.put("issuerCd", issuerCd);
        jsonData.put("appCatid", appCatid);
        jsonData.put("cardNo", cardNo);
        jsonData.put("oid", oid);
        jsonData.put("trNo", trNo);
        jsonData.put("ORIAPPNO", trNo);
        jsonData.put("DEVICETYP", "00");
        Gson gson = new Gson();
        String jsonDataStr = gson.toJson(jsonData);
        try {
            jsonData.put("hash", Utils.EncBySha256(gubun + resultCode + appNo + appPrice + "" + appDt));
        } catch (Exception e) {
            e.printStackTrace();
        }
        RetrofitAPI.getApiService().NicePosPaymentResult(jsonData).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SimpleJO jo = Utils.parseSimpleJo(response.body());
                    boolean flag = jo.getAsBoolean("flag");
                    if (flag) {
                        try {
                            SharedPreferences.Editor editor1 = Pref.getInstance().init(context).edit();
                            editor1.remove(Cnst.saveSaleData);
                            editor1.apply();
                            String sal005 = jo.getAsString("SAL005");
                            Log.e("확인", "sal005 : " + sal005);
                            SharedPreferences.Editor editor = context.getSharedPreferences("pay", MODE_PRIVATE).edit();
                            editor.putString("RCTSEQ", "");
                            editor.putString("NVCATRECVDATA", "");
                            editor.putString("admin", "");
                            editor.apply();
                            //  bundle.putString("ordmsg", ordmsg);
                            bundle.putBoolean("flag", jo.getAsBoolean("flag"));
                            bundle.putString("msg", alrtmsg);
                            bundle.putString("SAL005", sal005);
                            bundle.putString("ordnum", ordernum.substring(7, 11));
                            bundle.putString("product", product_array + "");
                            bundle.putBoolean("usePay", true);
                            bundle.putString("rcnseq", rcnseq);

                            JsonObject po = product_array.get(0).getAsJsonObject();
                            String stq = "";
                            if (product_array.size() > 1) {
                                stq = "외 " + (product_array.size() - 1) + "개";
                            }
                            po.get("PRDNAM").getAsString();

                            Afield af = new Afield();
                            af.setF("issuer_name", issuer_name);
                            af.setF("appDt", appDt);
                            af.setF("appNo", appNo);
                            af.setF("appPrice", appPrice);
                            af.setF("intMon", intMon);
                            af.setF("isCancel", false);
                            Gson gson = new Gson();
                            String jsonDataStr = gson.toJson(af.getF());
                            Set<String> list = Pref.getPref().getStringSet(Cnst.PayFinList, new HashSet<>());
                            list.add(jsonDataStr);
                            SharedPreferences.Editor editors = Pref.getEditor();
                            editors.putStringSet(Cnst.PayFinList, list);
                            editors.apply();


                            setProductListData(new ArrayList<>());
                            saleProductListAdapter.setSaleProductDataArrayList(productListData);
                            // CartOpenLogic(false);
                            setDrawerViewClose();
                            getTable_getSale();
                            setCartQty();
                        } catch (Exception t) {
                            if (!isSave) {
                                NicePosPaymentResult2(ordernum, product_array, rctseq, gubun, resultCode, appPrice, bugase,
                                        intMon, appNo, appDt, acquirerCd,
                                        issuerCd, issuer_name, ACQUIRER_name, appCatid, cardNo, CARTYP,
                                        oid, trNo, alrtmsg, bizno, store_num, rcnseq, true);
                                saveNicePosPaymentResultData(ordernum, product_array, rctseq, gubun, resultCode, appPrice, bugase,
                                        intMon, appNo, appDt, acquirerCd,
                                        issuerCd, issuer_name, ACQUIRER_name, appCatid, cardNo, CARTYP,
                                        oid, trNo, alrtmsg, bizno, store_num, rcnseq);
                            }
                            FirebaseCrashlytics.getInstance().log("실패1 /" + jsonDataStr);
                            FirebaseCrashlytics.getInstance().setCustomKey("API", "NicePosPaymentResult2 - mobile/pos/NicePosPaymentResult2.do");
                            FirebaseCrashlytics.getInstance().setCustomKey("API_state", "리스폰스O,Successful O, flag O, try-catch");
                            FirebaseCrashlytics.getInstance().recordException(t);
                        }
                    } else {
                        if (!isSave) {
                            NicePosPaymentResult2(ordernum, product_array, rctseq, gubun, resultCode, appPrice, bugase,
                                    intMon, appNo, appDt, acquirerCd,
                                    issuerCd, issuer_name, ACQUIRER_name, appCatid, cardNo, CARTYP,
                                    oid, trNo, alrtmsg, bizno, store_num, rcnseq, true);
                            saveNicePosPaymentResultData(ordernum, product_array, rctseq, gubun, resultCode, appPrice, bugase,
                                    intMon, appNo, appDt, acquirerCd,
                                    issuerCd, issuer_name, ACQUIRER_name, appCatid, cardNo, CARTYP,
                                    oid, trNo, alrtmsg, bizno, store_num, rcnseq);
                        }
                        FirebaseCrashlytics.getInstance().setCustomKey("API", "NicePosPaymentResult2 - mobile/pos/NicePosPaymentResult2.do");
                        FirebaseCrashlytics.getInstance().setCustomKey("API_state", "리스폰스O,Successful O, flag X");
                        FirebaseCrashlytics.getInstance().log("실패1 /" + jsonDataStr);
                        FirebaseCrashlytics.getInstance().log("실패결과2 /" + response.toString());
                        try {
                            throw new RuntimeException("실패2 /" + jsonDataStr); // Force a crash
                        } catch (Exception e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                    }
                } else {
                    if (!isSave) {
                        NicePosPaymentResult2(ordernum, product_array, rctseq, gubun, resultCode, appPrice, bugase,
                                intMon, appNo, appDt, acquirerCd,
                                issuerCd, issuer_name, ACQUIRER_name, appCatid, cardNo, CARTYP,
                                oid, trNo, alrtmsg, bizno, store_num, rcnseq, true);
                        saveNicePosPaymentResultData(ordernum, product_array, rctseq, gubun, resultCode, appPrice, bugase,
                                intMon, appNo, appDt, acquirerCd,
                                issuerCd, issuer_name, ACQUIRER_name, appCatid, cardNo, CARTYP,
                                oid, trNo, alrtmsg, bizno, store_num, rcnseq);
                    }
                    FirebaseCrashlytics.getInstance().setCustomKey("API", "NicePosPaymentResult2 - mobile/pos/NicePosPaymentResult2.do");
                    FirebaseCrashlytics.getInstance().setCustomKey("API_state", "리스폰스O,Successful X");
                    FirebaseCrashlytics.getInstance().log("실패1 /" + jsonDataStr);
                    FirebaseCrashlytics.getInstance().log("실패결과2 /" + response.toString());
                    try {
                        throw new RuntimeException("실패2 /" + jsonDataStr); // Force a crash
                    } catch (Exception e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                if (!isSave) {
                    NicePosPaymentResult2(ordernum, product_array, rctseq, gubun, resultCode, appPrice, bugase,
                            intMon, appNo, appDt, acquirerCd,
                            issuerCd, issuer_name, ACQUIRER_name, appCatid, cardNo, CARTYP,
                            oid, trNo, alrtmsg, bizno, store_num, rcnseq, true);
                    saveNicePosPaymentResultData(ordernum, product_array, rctseq, gubun, resultCode, appPrice, bugase,
                            intMon, appNo, appDt, acquirerCd,
                            issuerCd, issuer_name, ACQUIRER_name, appCatid, cardNo, CARTYP,
                            oid, trNo, alrtmsg, bizno, store_num, rcnseq);
                }
                FirebaseCrashlytics.getInstance().setCustomKey("API", "NicePosPaymentResult2 - mobile/pos/NicePosPaymentResult2.do");
                FirebaseCrashlytics.getInstance().setCustomKey("API_state", "리스폰스X");
                FirebaseCrashlytics.getInstance().recordException(t);
            }
        });

    }

    void getTable(List<Map<String, Object>> product) {
        Afield af = new Afield();
        af.setF("USERID", id);
        af.setF("USERPW", pw);
        af.setF("STOSEQ", stoseq);
        af.setF("FLRSEQ", flrseq);
        Call<String> call = RetrofitAPI.getApiService().getTable(af.getF());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    SimpleJO jo = Utils.parseSimpleJo(response.body());
                    if (jo.getAsBoolean("flag")) {
                        JsonArray table = jo.getAsJsonArray("table");
                        for (int i = 0; i < table.size(); i++) {
                            SimpleJO jjo = new SimpleJO(table.get(i).getAsJsonObject());
                            int TBLSEQ = jjo.getAsInt("TBLSEQ");
                            String TBLNAM = jjo.getAsString("TBLNAM");
                            int FLRSEQ = jjo.getAsInt("FLRSEQ");
                            Lg.e("dkdh", table.size() + " / " + FLRSEQ + " / " + TBLNAM + " / " + TBLSEQ);
                            if (tblseq == TBLSEQ) {
                                RCNSEQ = jjo.getAsInt("RCNSEQ");
                                if (Pref.getPref().getBoolean(Cnst.USEPAY, false)) {
                                    int totamt = 0;
                                    for (int z = 0; z < productListData.size(); z++) {
                                        SaleProductData data = productListData.get(z);
                                        totamt += data.getTotamt() * data.getSalqty();
                                    }
                                    product_save = product;
                                    appPrice = totamt;
                                    if (totamt >= 50000) {
                                        HalbuDialog(totamt);
                                    } else {
                                        JtnetClass jtnetClass = new JtnetClass(ApprovalCode.getApproval(JtnetClass.신용승인),
                                                Pref.getPref().getString(Cnst.jtnet_catid, ""), totamt, 0);
                                        JTNetPosManager.getInstance().jtdmProcess(jtnetClass.getRequestCode(), jtnetClass.createCreditArr(), requestCallback);
                                    }
                                } else {
                                    setSale(product);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    void updateTableGcm(String token){
        Afield af = new Afield();
        af.setF("STOSEQ",stoseq);
        af.setF("TBLSEQ",tblseq);
        af.setF("APPRID",token);
        Call<String> call =  RetrofitAPI.getApiService().updateTableGcm(af.getF());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Log.e("토큰 저장", ""+response.body());
                    SimpleJO jo = Utils.parseSimpleJo(response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }
    void getTable_getSale() {
        Afield af = new Afield();
        af.setF("USERID", id);
        af.setF("USERPW", pw);
        af.setF("STOSEQ", stoseq);
        af.setF("FLRSEQ", flrseq);
        Call<String> call = RetrofitAPI.getApiService().getTable(af.getF());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    SimpleJO jo = Utils.parseSimpleJo(response.body());
                    if (jo.getAsBoolean("flag")) {
                        JsonArray table = jo.getAsJsonArray("table");
                        for (int i = 0; i < table.size(); i++) {
                            SimpleJO jjo = new SimpleJO(table.get(i).getAsJsonObject());
                            int TBLSEQ = jjo.getAsInt("TBLSEQ");
                            String TBLNAM = jjo.getAsString("TBLNAM");
                            int FLRSEQ = jjo.getAsInt("FLRSEQ");
                            Lg.e("dkdh", table.size() + " / " + FLRSEQ + " / " + TBLNAM + " / " + TBLSEQ);
                            if (tblseq == TBLSEQ) {
                                RCNSEQ = jjo.getAsInt("RCNSEQ");
                                getSale_orderDetail();
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    Handler handlerOrderDetailDismiss = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (orderDetailDialog != null && orderDetailDialog.isShowing())
                orderDetailDialog.dismiss();
            return false;
        }
    });

    class OrderDetailDialog extends Dialog {
        JsonArray orderArray;
        int totamt;
        CountDownTimer countDownTimer;

        public OrderDetailDialog(@NonNull Context context, JsonArray orderArray, int totamt) {
            super(context);
            this.orderArray = orderArray;
            this.totamt = totamt;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            setContentView(R.layout.dialog_order_detail);

            int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
            int newUiOptions = uiOptions;
            boolean isImmersiveModeEnabled = ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
            if (isImmersiveModeEnabled) {
                Log.i("Is on?", "Turning immersive mode mode off. ");
            } else {
                Log.i("Is on?", "Turning immersive mode mode on.");
            }
            // 몰입 모드를 꼭 적용해야 한다면 아래의 3가지 속성을 모두 적용시켜야 합니다
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            getWindow().getDecorView().setSystemUiVisibility(newUiOptions);

            RecyclerView rc_order_detail = findViewById(R.id.rc_order_detail);

            TextView txt_totamt = findViewById(R.id.txt_totamt);
            txt_totamt.setText(Utils.getCurrencyString(totamt));
            TextView txt_empty = findViewById(R.id.txt_empty);
            if (orderArray.size() != 0) {
                txt_empty.setVisibility(View.GONE);
            } else {
                txt_empty.setVisibility(View.VISIBLE);
            }

            rc_order_detail.setLayoutManager(new LinearLayoutManager(context));
            OrderListAdapter orderListAdapter = new OrderListAdapter(orderArray);
            rc_order_detail.setAdapter(orderListAdapter);

            // TextView txt_close = findViewById(R.id.txt_close);
            RelativeLayout lay_close = findViewById(R.id.lay_close);
            lay_close.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    dismiss();
                }
            });
            countDownTimer = new CountDownTimer(20000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    dismiss();
                }
            };
            countDownTimer.start();
        }

        class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.OrderListHolder> {
            JsonArray jsonArray;

            public OrderListAdapter(JsonArray jsonArray) {
                this.jsonArray = jsonArray;
            }

            @NonNull
            @Override
            public OrderListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new OrderListHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_order_list, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull OrderListHolder holder, int position) {
                holder.bindview(position);
            }

            @Override
            public int getItemCount() {
                return jsonArray.size();
            }

            class OrderListHolder extends RecyclerView.ViewHolder {
                TextView txt_num, txt_prdnam, txt_prdsqy, txt_prdamt, txt_option;
                ConstraintLayout lay_content;

                public OrderListHolder(@NonNull View itemView) {
                    super(itemView);
                    lay_content = itemView.findViewById(R.id.lay_content);
                    txt_num = itemView.findViewById(R.id.txt_num);
                    txt_option = itemView.findViewById(R.id.txt_option);
                    txt_prdnam = itemView.findViewById(R.id.txt_prdnam);
                    txt_prdsqy = itemView.findViewById(R.id.txt_prdsqy);
                    txt_prdamt = itemView.findViewById(R.id.txt_prdamt);
                }

                void bindview(int pos) {
                    SimpleJO jo = new SimpleJO(jsonArray.get(pos).getAsJsonObject());
                    String prdnam = jo.getAsString("PRDNAM");
                    String optnam = jo.getAsString("OPTNAM");
                    int prdqty = jo.getAsInt("SALQTY");
                    int prdamt = jo.getAsInt("TOTAMT", 0);
                    txt_num.setText((pos + 1) + "");
                    txt_prdnam.setText(prdnam);
                    txt_prdsqy.setText("x" + prdqty);
                    txt_prdamt.setText(Utils.getCurrencyString(prdamt));
                    if (!optnam.equals("")) {
                        txt_option.setVisibility(View.VISIBLE);
                        txt_option.setText("└ " + optnam);
                    } else {
                        txt_option.setVisibility(View.GONE);
                    }


                }
            }
        }

        @Override
        public void dismiss() {
            super.dismiss();
            Log.e("다이얼 생명", "dismiss");
            try {
                countDownTimer.cancel();
            } catch (Exception e) {
            }

        }

        @Override
        public void cancel() {
            super.cancel();
            Log.e("다이얼 생명", "cancel");
            try {
                countDownTimer.cancel();
            } catch (Exception e) {
            }
        }

    }


    public static class OrderDetailBottomSheet extends BottomSheetDialogFragment {
        Context context;
        JsonArray orderArray;
        int totamt;

        public OrderDetailBottomSheet(@NonNull Context context, JsonArray orderArray, int totamt) {
            this.context = context;
            this.orderArray = orderArray;
            this.totamt = totamt;

        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return new BottomSheetDialog(getContext(), R.style.SheetDialog);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.bottomsheet_order_detail, container, false);
            RecyclerView rc_order_detail = view.findViewById(R.id.rc_order_detail);

            TextView txt_totamt = view.findViewById(R.id.txt_totamt);
            txt_totamt.setText(Utils.getCurrencyString(totamt));
            TextView txt_empty = view.findViewById(R.id.txt_empty);
            if (orderArray.size() != 0) {
                txt_empty.setVisibility(View.GONE);
            } else {
                txt_empty.setVisibility(View.VISIBLE);
            }

            rc_order_detail.setLayoutManager(new LinearLayoutManager(context));
            OrderListAdapter orderListAdapter = new OrderListAdapter(orderArray);
            rc_order_detail.setAdapter(orderListAdapter);


            return view;
        }

        class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.OrderListHolder> {
            JsonArray jsonArray;

            public OrderListAdapter(JsonArray jsonArray) {
                this.jsonArray = jsonArray;
            }

            @NonNull
            @Override
            public OrderListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new OrderListHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_order_list, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull OrderListHolder holder, int position) {
                holder.bindview(position);
            }

            @Override
            public int getItemCount() {
                return jsonArray.size();
            }

            class OrderListHolder extends RecyclerView.ViewHolder {
                TextView txt_prdnam, txt_prdsqy, txt_prdamt;

                public OrderListHolder(@NonNull View itemView) {
                    super(itemView);
                    txt_prdnam = itemView.findViewById(R.id.txt_prdnam);
                    txt_prdsqy = itemView.findViewById(R.id.txt_prdsqy);
                    txt_prdamt = itemView.findViewById(R.id.txt_prdamt);
                }

                void bindview(int pos) {
                    SimpleJO jo = new SimpleJO(jsonArray.get(pos).getAsJsonObject());
                    String prdnam = jo.getAsString("PRDNAM");
                    int prdqty = jo.getAsInt("");
                    int prdamt = jo.getAsInt("TOTAMT", 0);

                    txt_prdnam.setText(prdnam);
                    txt_prdsqy.setText(prdqty + "");
                    txt_prdamt.setText(Utils.getCurrencyString(prdamt));


                }
            }
        }
    }

    public class DetailProdcutBottomSheet extends BottomSheetDialogFragment {
        Context context;
        SimpleJO product_jo;
        JsonArray prddtl_1, prddtl_2, prddtl_3;
        // private final boolean new_Option;

        public DetailProdcutBottomSheet(@NonNull Context context, SimpleJO product_jo, JsonArray prddtl_1, JsonArray prddtl_2, JsonArray prddtl_3) {
            this.context = context;
            this.product_jo = product_jo;
            this.prddtl_1 = prddtl_1;
            this.prddtl_2 = prddtl_2;
            this.prddtl_3 = prddtl_3;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return new BottomSheetDialog(getContext(), R.style.SheetDialog);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.bottomsheet_detail_product, container, false);

            return view;
        }
    }

    public void cust_ToastMSG(String msg) {
        txt_toast.clearAnimation();
        txt_toast.setText(msg);
        txt_toast.startAnimation(animation);
    }


    String finalHalbu;
    AndnvcatPayHalbuDialog andnvcatPayHalbuDialog;

    void HalbuDialog(int totamt) {
        int finalTotamt;
        andnvcatPayHalbuDialog = new AndnvcatPayHalbuDialog(context);
        andnvcatPayHalbuDialog.setCancelable(false);
        finalTotamt = totamt;
        andnvcatPayHalbuDialog.setPositiveClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalHalbu = andnvcatPayHalbuDialog.getHalbu();
                int halbu = 0;
                if (!finalHalbu.equals("일시불")) {
                    intMon = finalHalbu.replace("개월", "");
                    halbu = Integer.parseInt(intMon);

                }
                JtnetClass jtnetClass = new JtnetClass(ApprovalCode.getApproval(JtnetClass.신용승인),
                        Pref.getPref().getString(Cnst.jtnet_catid, ""), totamt, halbu);
                JTNetPosManager.getInstance().jtdmProcess(jtnetClass.getRequestCode(), jtnetClass.createCreditArr(), requestCallback);
                andnvcatPayHalbuDialog.dismiss();
            }
        });
        if (andnvcatPayHalbuDialog != null && !andnvcatPayHalbuDialog.isShowing()) {
            andnvcatPayHalbuDialog.show();
        }
    }

    String paymentData;

    public String cutStringByByte(String str, int maxBytes, String msg) {
        if (str == null || str.isEmpty() || maxBytes <= 0) {
            return "";
        }
        int currentBytes = 0;
        int index = 0;
        try {
            while (index < str.length()) {
                char c = str.charAt(index);
                int charBytes = String.valueOf(c).getBytes("UTF-8").length;
                if (charBytes > 2) {
                    Log.e("바이트", charBytes + "");
                    charBytes = 2;
                }
                if (currentBytes + charBytes > maxBytes) {
                    break;
                }
                currentBytes += charBytes;
                index++;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String result = str.substring(0, index); // 이전 문자열
        Log.e("결제", "msg :" + result + "|");
        paymentData = str.substring(index); // 남은 문자열
        return result;
    }
}