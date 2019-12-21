package com.greeting.mysqlconnecter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;

import static com.greeting.mysqlconnecter.Login.acc;
import static com.greeting.mysqlconnecter.MainMenu.BuyAmount;
import static com.greeting.mysqlconnecter.MainMenu.BuyId;
import static com.greeting.mysqlconnecter.MainMenu.PID;
import static com.greeting.mysqlconnecter.MainMenu.PIMG;
import static com.greeting.mysqlconnecter.MainMenu.Pamount;
import static com.greeting.mysqlconnecter.MainMenu.Pname;
import static com.greeting.mysqlconnecter.MainMenu.Pprice;
import static com.greeting.mysqlconnecter.MainMenu.Vendor;

public class Market extends AppCompatActivity {
    TextView loading;


    //連接資料庫的IP、帳號(不可用root)、密碼
    private static final String url = "jdbc:mysql://140.135.113.196:3360/virtualcurrencyproject";
    private static final String user = "currency";
    private static final String pass = "@SAclass";

    //array list 已移至 main menu

    int function = 0;

    Button addProd;
    LinearLayout ll;
    ScrollView sv;
    public static int cardCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_market);

        loading = findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);

        ll = findViewById(R.id.ll);
        sv = findViewById(R.id.sv);

        sv.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                // closekeybord();
            }
        });

        ConnectMySql connectMySql = new ConnectMySql();
        connectMySql.execute("");

    }

    //建立連接與查詢非同步作業
    private class ConnectMySql extends AsyncTask<String, Void, String> {
        String res="";//錯誤信息儲存變數
        //開始執行動作
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            Toast.makeText(Market.this,"請稍後...",Toast.LENGTH_SHORT).show();
        }
        //查詢執行動作(不可使用與UI相關的指令)
        @Override
        protected String doInBackground(String... strings) {
            if(function == 0) {
                try {
                    //連接資料庫
                    Class.forName("com.mysql.jdbc.Driver");
                    Connection con = DriverManager.getConnection(url, user, pass);
                    //建立查詢
                    String result = "";
                    Statement st = con.createStatement();
                    ResultSet rs = st.executeQuery("select * from product");

                    while (rs.next()) {
                        PID.add(rs.getString("productID"));
                        Pname.add(rs.getString("productName"));
                        Pprice.add(rs.getInt("price"));
                        Pamount.add(rs.getInt("amount"));
                        Vendor.add(rs.getString("vendor"));
                        PIMG.add(rs.getString("productIMG"));
                    }

                    return PID.size() + "";//回傳結果給onPostExecute==>取得輸出變數(位置)

                } catch (Exception e) {
                    e.printStackTrace();
                    res = e.toString();
                }
                return res;
            }
            ////////////////////////////////////////////
            else if(function == 1){
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    Connection con = DriverManager.getConnection(url, user, pass);
                    //建立查詢
                    String result ="";
                    //Statement st = con.createStatement();
//                ResultSet rs = st.executeQuery("call login(@fname, '"+account+"', '"+password+"'); select @fname;");
                    //experiment part start
                    //此處呼叫Stored procedure(call 函數名稱(?)==>問號數量代表輸出、輸入的變數數量)
                    CallableStatement cstmt = con.prepareCall("{call sell(?,?,?,?,?)}");
                    cstmt.setString(1,acc);//設定輸出變數(參數位置,參數型別)
                    cstmt.setString(2,PID.get(BuyId));
                    cstmt.setString(3,Vendor.get(BuyId));
                    cstmt.setInt(4,BuyAmount);
                    cstmt.registerOutParameter(5, Types.VARCHAR);
                    cstmt.executeUpdate();
                    return cstmt.getString("info");
                    //experiment part end

                } catch (Exception e) {
                    e.printStackTrace();
                    res = e.toString();
                }
                return res;
            }
            return null;
        }
        //查詢後的結果將回傳於此
        @Override
        protected void onPostExecute(String result) {
            try{
                if(function == 0){
                    cardCounter = Integer.parseInt(result);
                    cardRenderer();
                }
                else if(function == 1){
                    if(result.substring(0,4).equals("交易成功")){
                        clear();
                        recreate();
                    }
                    Toast.makeText(Market.this, result, Toast.LENGTH_SHORT).show();
                }
                function = -1;
            }catch (Exception e){
                Log.v("test","錯誤: "+e.toString());
            }

        }
    }

    //商品卡產生器
    public void cardRenderer(){
        for(int i = 0 ; i < PID.size() ; i++){
            Log.v("test", "render card "+i);
            add(i);
        }
    }


    //產生商品卡
    public void add(final int ID){
        //商品卡片
        LinearLayout frame = new LinearLayout(this);
        LinearLayout.LayoutParams framep = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                DP(150)
        );


        frame.setPadding(DP(15),DP(15),DP(15),DP(15));
        framep.setMargins(0,0,0,DP(20));
        frame.setOrientation(LinearLayout.HORIZONTAL);
        frame.setBackgroundColor(Color.WHITE);
        frame.setLayoutParams(framep);

        //圖片&價格區
        LinearLayout picpri = new LinearLayout(this);
        LinearLayout.LayoutParams picprip = new LinearLayout.LayoutParams(DP(120),DP(120));
        picprip.setMargins(0,0,DP(5),0);
        picpri.setOrientation(LinearLayout.VERTICAL);
        picpri.setLayoutParams(picprip);

        //數量
        final EditText amount = new EditText(this);
        LinearLayout.LayoutParams amountp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        amount.setEms(3);
        amount.setInputType(InputType.TYPE_CLASS_NUMBER);
        amount.setLayoutParams(amountp);
        amount.setId(5*ID+2);
        amount.setText("1");

        //商品圖片
        ImageView propic = new ImageView(this);
        LinearLayout.LayoutParams propicp = new LinearLayout.LayoutParams(DP(120),DP(90));
        //propic.setImageBitmap(Bitmap.createScaledBitmap(ConvertToBitmap(ID), 120, 90, false));
        propic.setImageBitmap(ConvertToBitmap(ID));
        propic.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        try {
//
//        }catch (Exception e){
//            Log.v("test", "recycle Bitmap error:\n"+e.toString());
//        }
        propic.setLayoutParams(propicp);
        propic.setId(5*ID);
        propic.setOnClickListener(v -> {
            final int id = ID;
            if(amount.getText().toString().trim().isEmpty()){amount.setText("0");}
            final int quantity = Integer.parseInt(amount.getText().toString());
            closekeybord();
            identifier("D",id,quantity);
        });

        //商品價格
        TextView price = new TextView(this);
        LinearLayout.LayoutParams pricep = new LinearLayout.LayoutParams(DP(120),DP(30));
        price.setText("價格: $"+Pprice.get(ID));
        price.setTextSize(18f);
        price.setLayoutParams(picprip);

        //商品訊息區
        LinearLayout proinf = new LinearLayout(this);
        LinearLayout.LayoutParams proinfp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,1f
        );
        proinf.setOrientation(LinearLayout.VERTICAL);
        proinf.setLayoutParams(proinfp);

        //商品名稱
        TextView proname = new TextView(this);
        LinearLayout.LayoutParams pronamep = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        proname.setText(Pname.get(ID));
        proname.setTextSize(18f);
        proname.setClickable(true);
        proname.setLayoutParams(pronamep);
        proname.setId(5*ID+1);

        //購買資訊
        LinearLayout buyinf = new LinearLayout(this);
        LinearLayout.LayoutParams buyinfp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        buyinf.setOrientation(LinearLayout.HORIZONTAL);
        buyinf.setLayoutParams(buyinfp);

        //數量:[標籤]
        TextView amount_label = new TextView(this);
        LinearLayout.LayoutParams amount_labelp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        amount_label.setText("數量：");
        amount_label.setTextSize(18f);
        amount_label.setLayoutParams(amount_labelp);

        //按鈕箱
        LinearLayout btnbox = new LinearLayout(this);
        LinearLayout.LayoutParams btnboxp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        btnbox.setLayoutParams(btnboxp);


        //詳情按鈕
        Button detail = new Button(this);
        LinearLayout.LayoutParams detailp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,0.5f
        );
        detail.setText("詳情");
        detail.setTextSize(18f);
        detail.setLayoutParams(detailp);
        detail.setId(5*ID+3);
        detail.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final int id = ID;
                if(amount.getText().toString().trim().isEmpty()){amount.setText("0");}
                final int quantity = Integer.parseInt(amount.getText().toString());
                closekeybord();
                identifier("D",id,quantity);
            }
        });

        //訂購按鈕
        Button buybtn = new Button(this);
        LinearLayout.LayoutParams buybtnp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,0.5f
        );
        buybtn.setText("訂購");
        buybtn.setTextSize(18f);
        buybtn.setLayoutParams(buybtnp);
        buybtn.setId(5*ID+4);
        buybtn.setOnClickListener(v -> {
            final int id = ID;
            if(amount.getText().toString().trim().isEmpty()){amount.setText("0");}
            final int quantity = Integer.parseInt(amount.getText().toString());
            closekeybord();
            identifier("B",id,quantity);
        });

        //將內容填入frame
        /*
        frame
            propic
            proinf
                proname
                buyinf
                    amount_label
                    amount
                btnbox
                    dteail
                    buybtn
        */
        proinf.addView(proname);
        buyinf.addView(amount_label);
        buyinf.addView(amount);
        proinf.addView(buyinf);
        btnbox.addView(detail);
        btnbox.addView(buybtn);
        proinf.addView(btnbox);
        picpri.addView(propic);
        picpri.addView(price);
        frame.addView(picpri);
        frame.addView(proinf);
        ll.addView(frame);
        loading.setVisibility(View.GONE);
        Log.v("test","card"+ID+"rendered");
    }

    public static int DP(float dp){
        dp = dp * ((float) Resources.getSystem().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return (int)dp;
    }
    /////////////////////////////////////////////

    public void identifier(String act, int ID,int quantity){
        BuyAmount = quantity;
        BuyId=ID;
        if(act.equals("D")){
            Log.v("test","您正在檢視第"+Pname.get(ID)+"的詳細資料");
            Intent intent = new Intent(Market.this, ProductDetail.class);
            startActivity(intent);
            finish();
        }else if(act.equals("B")){
            Log.v("test","您購買了"+quantity+"個"+Pname.get(ID));
            function = 1;
            if(quantity>0){
                ConnectMySql connectMySql = new ConnectMySql();
                connectMySql.execute("");
            }else{
                function = -1;
                Toast.makeText(Market.this,"請至少購買一項商品",Toast.LENGTH_SHORT).show();
            }
        }
    }

    //隱藏鍵盤
    public void closekeybord() {
        View view = this.getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    public void onBackPressed(){
        Intent intent = new Intent(Market.this, MainMenu.class);
        startActivity(intent);
        clear();
        finish();
    }

    public void clear(){
        PID.clear();
        Pname.clear();
        Pprice.clear();
        Pamount.clear();
        Vendor.clear();
        PIMG.clear();
    }


    public Bitmap ConvertToBitmap(int ID){
        try{
//            Log.v("test",PIMG.get(ID));
            byte[] imageBytes = Base64.decode(PIMG.get(ID), Base64.DEFAULT);
            Bitmap proimg = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            int w = proimg.getWidth();
            int h = proimg.getHeight();
            Log.v("test","pic"+ID+" original = "+w+"*"+h);
            int scale = 1;
            if(w>h && (w/DP(120))>1 || h==w && (w/DP(120))>1){
                scale = w/DP(120);
                w = w/scale;
                h = h/scale;
            }else if(h>w && (h/DP(120))>1){
                scale = h/DP(120);
                w = w/scale;
                h = h/scale;
            }
            Log.v("test","pic"+ID+" resized = "+w+"*"+h);
            proimg = Bitmap.createScaledBitmap(proimg, w, h, false);
            return proimg;
        }catch (Exception e){
            Log.v("test","error = "+e.toString());
            return null;
        }


    }
}
