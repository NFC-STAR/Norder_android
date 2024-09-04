package com.nfcstar.norder;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nfcstar.norder.util.OnSingleClickListener;
import com.nfcstar.norder.util.SimpleJO;
import com.nfcstar.norder.util.Utils;

import java.util.HashMap;
import java.util.Map;

public class CustomOptionDialog extends Dialog {
    Context context;
    private JsonArray jsonArray;
    private Button btn_close;
    private RecyclerView rec_option;
    private final MainActivity mainActivity;
    boolean isTablett;
    private JsonArray prddtl_1, prddtl_2, prddtl_3;
    private int select1, select2, select3;
    private boolean showOpt1, showOpt2, showOpt3, setOpt1, setOpt2, setOpt3, useOpt1, useOpt2, useOpt3;
    private LinearLayout layout_option_list_item1, layout_option_list_item2, layout_option_list_item3;
    private LinearLayout layout_prddtl1, layout_prddtl2, layout_prddtl3;

    Handler textRemoveHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            try {
                txt_msg.setVisibility(View.INVISIBLE);
            } catch (Exception e) {
            }
            return false;
        }
    });
    Handler cancelHandler = new Handler(message -> {
        cancel();
        return true;
    });
    private TextView txt_prdnam, txt_prdexp, txt_msg,txt_prdamt;
    private TextView txt_dtlnum1, txt_totamt1, txt_dtlnam_select1, txt_totamt_select1;
    private TextView txt_dtlnum2, txt_totamt2, txt_dtlnam_select2, txt_totamt_select2;
    private TextView txt_dtlnum3, txt_totamt3, txt_dtlnam_select3, txt_totamt_select3;
    private SimpleJO product_jo;
    private RelativeLayout lay_select_prddtl1, lay_select_prddtl2, lay_select_prddtl3;
    ViewGroup lay_option;
    private ImageView iv_prdimg, iv_option_open1, iv_option_open2, iv_option_open3;

    Button btn_order_fin;
    TextView txt_dtlnam1, txt_dtlnam2, txt_dtlnam3;

    public CustomOptionDialog(@NonNull Context context, SimpleJO product_jo, JsonArray prddtl_1, JsonArray prddtl_2, JsonArray prddtl_3, MainActivity mainActivity) {
        super(context);
        this.context = context;
        this.product_jo = product_jo;
        this.prddtl_1 = prddtl_1;
        this.prddtl_2 = prddtl_2;
        this.prddtl_3 = prddtl_3;
        this.mainActivity = mainActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        isTablett = isTablet();
        setContentView(R.layout.dialog_custom_option);

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

        txt_prdnam = findViewById(R.id.txt_prdnam);
        txt_prdnam.setText(Html.fromHtml(product_jo.getAsString("PRDNAM")));
        btn_close = findViewById(R.id.btn_close);

        lay_option = findViewById(R.id.lay_option);
        layout_option_list_item1 = findViewById(R.id.layout_option_list_item1);
        layout_option_list_item2 = findViewById(R.id.layout_option_list_item2);
        layout_option_list_item3 = findViewById(R.id.layout_option_list_item3);

        iv_option_open1 = findViewById(R.id.iv_option_open1);
        iv_option_open2 = findViewById(R.id.iv_option_open2);
        iv_option_open3 = findViewById(R.id.iv_option_open3);

        iv_prdimg = findViewById(R.id.iv_prdimg);
        txt_prdexp = findViewById(R.id.txt_prdexp);

        btn_order_fin = findViewById(R.id.btn_order_fin);
        btn_order_fin.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                boolean flag = true;
                String msg = "";
                if (layout_prddtl1.getVisibility() == View.VISIBLE && txt_dtlnum1.getText().equals("")) {
                    flag = false;
                    msg = "첫번째 옵션-" + txt_dtlnam1.getText() + "을 선택해 주세요.";
                } else if (layout_prddtl2.getVisibility() == View.VISIBLE && txt_dtlnum2.getText().equals("")) {
                    flag = false;
                    msg = "두번째 옵션-" + txt_dtlnam2.getText() + "을 선택해 주세요.";
                } else if (layout_prddtl3.getVisibility() == View.VISIBLE && txt_dtlnum3.getText().equals("")) {
                    flag = false;
                    msg = "세번째 옵션-" + txt_dtlnam3.getText() + "을 선택해 주세요.";
                }
                if (flag) {
                    appendProduct();
                    mainActivity.setDrawerViewOpen();
                } else {
                    txt_msg.setText(msg);
                    txt_msg.setVisibility(View.VISIBLE);
                    textRemoveHandler.removeMessages(34);
                    textRemoveHandler.sendEmptyMessageDelayed(34, 3000);
                }
            }
        });

        String prdimg_name = product_jo.getAsString("PRDIMG_NAM");
        String prdimg_path = product_jo.getAsString("PRDIMG_PTH");
        if (!prdimg_name.equals("") && !prdimg_path.equals("")) {
            String img = prdimg_path + "/" + prdimg_name;
            Utils.loadImage(context, img, iv_prdimg, 20);
            iv_prdimg.setVisibility(View.VISIBLE);
        } else {
            iv_prdimg.setVisibility(View.GONE);
        }

        String prdexp = product_jo.getAsString("PRDEXP");
        if (!prdexp.equals("")) {
            txt_prdexp.setVisibility(View.VISIBLE);
            txt_prdexp.setText(prdexp);
        } else {
            txt_prdexp.setVisibility(View.GONE);
        }

        txt_prdamt = findViewById(R.id.txt_prdamt);
        txt_prdamt.setText(Utils.getCurrencyString(product_jo.getAsInt("TOTAMT", 0)));

        // 애니메이션
        // LayoutTransition layoutTransition = new LayoutTransition();
        // lay_option.setLayoutTransition(layoutTransition);
        // layout_option_list_item1.setLayoutTransition(layoutTransition);
        // layout_option_list_item2.setLayoutTransition(layoutTransition);
        // layout_option_list_item3.setLayoutTransition(layoutTransition);

        layout_prddtl1 = findViewById(R.id.layout_prddtl1);
        layout_prddtl2 = findViewById(R.id.layout_prddtl2);
        layout_prddtl3 = findViewById(R.id.layout_prddtl3);

        lay_select_prddtl1 = findViewById(R.id.lay_select_prddtl1);
        lay_select_prddtl2 = findViewById(R.id.lay_select_prddtl2);
        lay_select_prddtl3 = findViewById(R.id.lay_select_prddtl3);

        txt_dtlnum1 = findViewById(R.id.txt_dtlnum1);
        txt_totamt1 = findViewById(R.id.txt_totamt1);
        txt_dtlnam_select1 = findViewById(R.id.txt_dtlnam_select1);
        txt_totamt_select1 = findViewById(R.id.txt_totamt_select1);

        txt_dtlnum2 = findViewById(R.id.txt_dtlnum2);
        txt_totamt2 = findViewById(R.id.txt_totamt2);
        txt_dtlnam_select2 = findViewById(R.id.txt_dtlnam_select2);
        txt_totamt_select2 = findViewById(R.id.txt_totamt_select2);

        txt_dtlnum3 = findViewById(R.id.txt_dtlnum3);
        txt_totamt3 = findViewById(R.id.txt_totamt3);
        txt_dtlnam_select3 = findViewById(R.id.txt_dtlnam_select3);
        txt_totamt_select3 = findViewById(R.id.txt_totamt_select3);
        txt_msg = findViewById(R.id.txt_msg);

        if (prddtl_1.size() != 0) {
            useOpt1 = true;
            layout_prddtl1.setVisibility(View.VISIBLE);
            txt_dtlnam1 = findViewById(R.id.txt_dtlnam1);
            txt_dtlnam1.setText(Html.fromHtml(product_jo.getAsString("OPTNAM1")));
            layout_option_list_item1.setVisibility(View.VISIBLE);
            iv_option_open1.setBackgroundResource(R.drawable.out_hi_chk);
            showOpt1 = true;
            setProductOptionList(layout_option_list_item1, prddtl_1, 1);

            // if (isTablett) {
            //     txt_dtlnam1.setTextSize(Dimension.SP, 25);
            //     txt_dtlnam_select1.setTextSize(Dimension.SP, 25);
            // }
            layout_prddtl1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OptionListAni(1);
                }
            });
            lay_select_prddtl1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OptionListAni(1);
                }
            });

        }
        if (prddtl_2.size() != 0) {
            useOpt2 = true;
            layout_prddtl2.setVisibility(View.VISIBLE);
            txt_dtlnam2 = findViewById(R.id.txt_dtlnam2);
            txt_dtlnam2.setText(Html.fromHtml(product_jo.getAsString("OPTNAM2")));
            setProductOptionList(layout_option_list_item2, prddtl_2, 2);

            // if (isTablett) {
            //     txt_dtlnam2.setTextSize(Dimension.SP, 25);
            //     txt_dtlnam_select2.setTextSize(Dimension.SP, 25);
            // }

            layout_prddtl2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OptionListAni(2);
                }
            });
            lay_select_prddtl2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OptionListAni(2);
                }
            });
        }
        if (prddtl_3.size() != 0) {
            useOpt3 = true;
            layout_prddtl3.setVisibility(View.VISIBLE);
            txt_dtlnam3 = findViewById(R.id.txt_dtlnam3);
            txt_dtlnam3.setText(Html.fromHtml(product_jo.getAsString("OPTNAM3")));
            setProductOptionList(layout_option_list_item3, prddtl_3, 3);

            // if (isTablett) {
            //     txt_dtlnam3.setTextSize(Dimension.SP, 25);
            //     txt_dtlnam_select3.setTextSize(Dimension.SP, 25);
            // }

            layout_prddtl3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OptionListAni(3);
                }
            });
            lay_select_prddtl3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OptionListAni(3);
                }
            });
        }


        btn_close.setOnClickListener(v -> cancel());


    }



    void OptionListAni(int option) {
        if (showOpt1) {
            layout_option_list_item1.setVisibility(View.GONE);
            iv_option_open1.setBackgroundResource(R.drawable.out_under_chk);
            if (select1 != 0) {
                lay_select_prddtl1.setVisibility(View.VISIBLE);
            }
            showOpt1 = false;
        } else if (showOpt2) {
            layout_option_list_item2.setVisibility(View.GONE);
            iv_option_open2.setBackgroundResource(R.drawable.out_under_chk);
            if (select2 != 0) {
                lay_select_prddtl2.setVisibility(View.VISIBLE);
            }
            showOpt2 = false;
        } else if (showOpt3) {
            layout_option_list_item3.setVisibility(View.GONE);
            iv_option_open3.setBackgroundResource(R.drawable.out_under_chk);
            if (select3 != 0) {
                lay_select_prddtl3.setVisibility(View.VISIBLE);
            }
            showOpt3 = false;
        }
        switch (option) {
            case 1:
                layout_option_list_item1.setVisibility(View.VISIBLE);
                iv_option_open1.setBackgroundResource(R.drawable.out_hi_chk);
                lay_select_prddtl1.setVisibility(View.GONE);
                showOpt1 = true;
                setProductOptionList(layout_option_list_item1, prddtl_1, 1);
                break;

            case 2:
                layout_option_list_item2.setVisibility(View.VISIBLE);
                iv_option_open2.setBackgroundResource(R.drawable.out_hi_chk);
                lay_select_prddtl2.setVisibility(View.GONE);
                showOpt2 = true;
                setProductOptionList(layout_option_list_item2, prddtl_2, 2);
                break;

            case 3:
                layout_option_list_item3.setVisibility(View.VISIBLE);
                iv_option_open3.setBackgroundResource(R.drawable.out_hi_chk);
                lay_select_prddtl3.setVisibility(View.GONE);
                showOpt3 = true;
                setProductOptionList(layout_option_list_item3, prddtl_3, 3);
                break;
        }
    }

    void setProductOptionList(LinearLayout layout, JsonArray prddtl, int optnum) {
        layout.removeAllViews();
        int select_option = 0;
        switch (optnum) {
            case 1:
                select_option = select1;
                break;
            case 2:
                select_option = select2;
                break;
            case 3:
                select_option = select3;
                break;
        }
        for (int i = 0; i < prddtl.size(); i++) {
            getLayoutInflater().inflate(R.layout.fragment_productview_option_list_item, layout);
            RelativeLayout view = (RelativeLayout) layout.getChildAt(i);

            JsonObject temp = prddtl.get(i).getAsJsonObject();

            TextView txt_dtlnum = view.findViewById(R.id.txt_dtlnum);
            String dtlnum = temp.get("DTLNUM").getAsString();
            ImageView img_opt_select = view.findViewById(R.id.img_opt_select);
            txt_dtlnum.setText(Html.fromHtml(dtlnum));

            TextView txt_dtlnam = view.findViewById(R.id.txt_dtlnam);
            String dtlnam = temp.get("DTLNAM").getAsString();
            if (select_option == i + 1) {
                view.setBackground(context.getResources().getDrawable(R.drawable.shp_option_select));
                img_opt_select.setBackground(context.getResources().getDrawable(R.drawable.out_rect_chk));
            } else {
                view.setBackground(context.getResources().getDrawable(R.drawable.shp_option));
                img_opt_select.setBackground(context.getResources().getDrawable(R.drawable.shape_option_unselect));
            }

            txt_dtlnam.setText(Html.fromHtml(dtlnam));


            TextView txt_totamt = view.findViewById(R.id.txt_totamt);
            String totamt = temp.get("TOTAMT").getAsString();
            txt_totamt.setText(totamt);


            TextView txt_totamt_show = view.findViewById(R.id.txt_totamt_show);
            if (totamt.startsWith("-")) {
                txt_totamt_show.setText(totamt + " 원");
            } else {
                txt_totamt_show.setText("+" + totamt + " 원");
            }
            // if (isTablett) {
            //     txt_dtlnam.setTextSize(Dimension.SP, 25);
            //     txt_totamt_show.setTextSize(Dimension.SP, 24);
            // }
            view.setOnClickListener(new optionItemClickListener(optnum, i + 1));
        }

    }

    public class optionItemClickListener implements View.OnClickListener {
        int optnum;
        int count;

        public optionItemClickListener(int optnum, int count) {
            this.optnum = optnum;
            this.count = count;
        }

        @Override
        public void onClick(View v) {
            TextView thisView = v.findViewById(R.id.txt_dtlnam);
            //thisView.setBackgroundColor(Color.YELLOW);

            Log.e("순번", "/" + count + "/");
            // TODO Auto-generated method stub
            if (optnum == 1) {
                setOpt1 = true;

                select1 = count;

                lay_select_prddtl1.setVisibility(View.VISIBLE);

                txt_dtlnum1.setText(((TextView) v.findViewById(R.id.txt_dtlnum)).getText());
                txt_totamt1.setText(((TextView) v.findViewById(R.id.txt_totamt)).getText());

                txt_dtlnam_select1.setText(((TextView) v.findViewById(R.id.txt_dtlnam)).getText());
                txt_totamt_select1.setText(((TextView) v.findViewById(R.id.txt_totamt_show)).getText());

                if (!setOpt2) {
                    OptionListAni(2);
                } else if (!setOpt3) {
                    OptionListAni(3);
                } else {
                    OptionListAni(0);
                }

            } else if (optnum == 2) {
                setOpt2 = true;
                select2 = count;
                lay_select_prddtl2.setVisibility(View.VISIBLE);

                txt_dtlnum2.setText(((TextView) v.findViewById(R.id.txt_dtlnum)).getText());
                txt_totamt2.setText(((TextView) v.findViewById(R.id.txt_totamt)).getText());


                txt_dtlnam_select2.setText(((TextView) v.findViewById(R.id.txt_dtlnam)).getText());
                txt_totamt_select2.setText(((TextView) v.findViewById(R.id.txt_totamt_show)).getText());


                if (!setOpt3) {
                    OptionListAni(3);
                } else if (!setOpt1) {
                    OptionListAni(1);
                } else {
                    OptionListAni(0);
                }

            } else if (optnum == 3) {
                setOpt3 = true;
                select3 = count;
                lay_select_prddtl3.setVisibility(View.VISIBLE);

                txt_dtlnum3.setText(((TextView) v.findViewById(R.id.txt_dtlnum)).getText());
                txt_totamt3.setText(((TextView) v.findViewById(R.id.txt_totamt)).getText());

                txt_dtlnam_select3.setText(((TextView) v.findViewById(R.id.txt_dtlnam)).getText());
                txt_totamt_select3.setText(((TextView) v.findViewById(R.id.txt_totamt_show)).getText());

                if (!setOpt1) {
                    OptionListAni(1);
                } else if (!setOpt2) {
                    OptionListAni(2);
                } else {
                    OptionListAni(0);
                }
            }
            if (setOpt1 && setOpt2 && setOpt3) {
                //  optionListHidden();
                setOpt1 = false;
                setOpt2 = false;
                setOpt3 = false;
            }
            boolean flag = true;

            if (layout_prddtl1.getVisibility() == View.VISIBLE && txt_dtlnum1.getText().equals("")) {
                flag = false;
            } else if (layout_prddtl2.getVisibility() == View.VISIBLE && txt_dtlnum2.getText().equals("")) {
                flag = false;
            } else if (layout_prddtl3.getVisibility() == View.VISIBLE && txt_dtlnum3.getText().equals("")) {
                flag = false;
            }
            if (flag) {
                String msg = "상품 옵션을 모두 선택하였습니다.\n상품 추가 버튼을 눌러주세요.";
                txt_msg.setText(msg);
                txt_msg.setVisibility(View.VISIBLE);
                textRemoveHandler.removeMessages(34);
                textRemoveHandler.sendEmptyMessageDelayed(34, 3000);
                int totamt = product_jo.getAsInt("TOTAMT", 0);
                if (layout_prddtl1.getVisibility() == View.VISIBLE)
                    totamt += Integer.parseInt(txt_totamt1.getText().toString());
                if (layout_prddtl2.getVisibility() == View.VISIBLE)
                    totamt += Integer.parseInt(txt_totamt2.getText().toString());
                if (layout_prddtl3.getVisibility() == View.VISIBLE)
                    totamt += Integer.parseInt(txt_totamt3.getText().toString());

                btn_order_fin.setText(Utils.getCurrencyString(totamt)+" "+"담기");
            }
        }
    }

    void appendProduct() {
        layout_prddtl1.setClickable(false);
        layout_prddtl2.setClickable(false);
        layout_prddtl3.setClickable(false);
        lay_select_prddtl1.setClickable(false);
        lay_select_prddtl2.setClickable(false);
        lay_select_prddtl3.setClickable(false);

        String otpnam = "";
        if (layout_prddtl1.getVisibility() == View.VISIBLE) {
            otpnam += txt_dtlnam_select1.getText();
        }
        if (layout_prddtl2.getVisibility() == View.VISIBLE) {
            otpnam += " | " + txt_dtlnam_select2.getText();
        }
        if (layout_prddtl3.getVisibility() == View.VISIBLE) {
            otpnam += " | " + txt_dtlnam_select3.getText();
        }
        int totamt = product_jo.getAsInt("TOTAMT", 0);
        if (layout_prddtl1.getVisibility() == View.VISIBLE)
            totamt += Integer.parseInt(txt_totamt1.getText().toString());
        if (layout_prddtl2.getVisibility() == View.VISIBLE)
            totamt += Integer.parseInt(txt_totamt2.getText().toString());
        if (layout_prddtl3.getVisibility() == View.VISIBLE)
            totamt += Integer.parseInt(txt_totamt3.getText().toString());

        String dtlnum1, dtlnum2, dtlnum3;
        dtlnum1 = txt_dtlnum1.getText().toString();
        dtlnum2 = txt_dtlnum2.getText().toString();
        dtlnum3 = txt_dtlnum3.getText().toString();
        if (dtlnum1.equals("")) {
            dtlnum1 = "0";
        }
        if (dtlnum2.equals("")) {
            dtlnum2 = "0";
        }
        if (dtlnum3.equals("")) {
            dtlnum3 = "0";
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("PRDSEQ", product_jo.getAsString("PRDSEQ"));
        map.put("PRDNUM", product_jo.getAsString("PRDNUM"));
        map.put("PRDNAM", product_jo.getAsString("PRDNAM"));
        map.put("PRDIMG", product_jo.getAsString("PRDIMG_PTH") + "/" + product_jo.getAsString("PRDIMG_NAM"));
        map.put("PRTNUM", product_jo.getAsString("PRTNUM"));
        map.put("DTLNUM1", dtlnum1);
        map.put("DTLNUM2", dtlnum2);
        map.put("DTLNUM3", dtlnum3);
        map.put("OPTNAM", otpnam);
        map.put("TOTAMT", totamt);


        Gson gson = new Gson();
        JsonObject object = (JsonObject) gson.toJsonTree(map);
        mainActivity.new_SetOrder(object);
        cancelHandler.sendEmptyMessageDelayed(44, 40);
    }

    public boolean isTablet() {
        try {
            boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE);
            boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
            Log.d("화면 확인", "/:" + large + ":/:" + xlarge + ":/");
            return (xlarge || large);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    @Override
    protected void onStop() {
        textRemoveHandler.removeMessages(34);
        super.onStop();
    }
}
