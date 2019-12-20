package com.greeting.mysqlconnecter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Types;

import static com.greeting.mysqlconnecter.Event.AamountLeft;
import static com.greeting.mysqlconnecter.Event.Actpic;
import static com.greeting.mysqlconnecter.Event.Adesc;
import static com.greeting.mysqlconnecter.Event.Aid;
import static com.greeting.mysqlconnecter.Event.Aname;
import static com.greeting.mysqlconnecter.Event.Areward;
import static com.greeting.mysqlconnecter.Event.Avendor;
import static com.greeting.mysqlconnecter.Event.EventId;
import static com.greeting.mysqlconnecter.Event.attended;
import static com.greeting.mysqlconnecter.Login.acc;
import static com.greeting.mysqlconnecter.Login.pass;
import static com.greeting.mysqlconnecter.Login.url;
import static com.greeting.mysqlconnecter.Login.user;
import static com.greeting.mysqlconnecter.Market.BuyAmount;

public class EventDetail extends AppCompatActivity {

    public Bitmap ConvertToBitmap(int ID){
        try{
//            Log.v("test",PIMG.get(ID));
            byte[] imageBytes = Base64.decode(Actpic.get(ID), Base64.DEFAULT);
            Bitmap proimg = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            int w = proimg.getWidth();
            int h = proimg.getHeight();
            Log.v("test","pic"+ID+" original = "+w+"*"+h);
            int scale = 1;
            if(w>h && (w/DP(360))>1 || h==w && (w/DP(360))>1){
                scale = w/DP(360);
                w = w/scale;
                h = h/scale;
            }else if(h>w && (h/DP(360))>1){
                scale = h/DP(360);
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

    EditText Qt;
    Button btnBuy;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_event_detail);
        ImageView merPic = findViewById(R.id.merPic);
        merPic.setImageBitmap(ConvertToBitmap(EventId));

        TextView txtName=findViewById(R.id.txtName);
        txtName.setText(Aname.get(EventId));

        TextView txtVdrName=findViewById(R.id.txtVdrName);
        txtVdrName.setText("主辦廠商: "+Avendor.get(EventId)+"\n活動名稱: "+Aname.get(EventId)+"\n剩餘名額: "+AamountLeft.get(EventId)+"人\n回饋金額: $"+Areward.get(EventId)+"\n活動說明:\n"+Adesc.get(EventId));
        

        btnBuy = findViewById(R.id.btnBuy);
        if (attended.contains(Aid.get(EventId))){btnBuy.setText("取消報名");}
        else{btnBuy.setText("參加");}
        btnBuy.setOnClickListener(v -> Buyer());
    }

    void Buyer() {
        //if(btnBuy.getText().toString().equals("參加")){btnBuy.setText("取消報名");}
        //else{btnBuy.setText("參加");}
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
            Toast.makeText(EventDetail.this,"請稍後...",Toast.LENGTH_SHORT).show();
        }
        //查詢執行動作(不可使用與UI相關的指令)
        @Override
        protected String doInBackground(String... strings) {
            ////////////////////////////////////////////
            try {
                Log.v("test","活動報名中");
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(url, user, pass);
                //建立查詢
                String result ="";
                CallableStatement cstmt = con.prepareCall("{call activity_attend(?,?,?)}");
                cstmt.setString(1,acc);//設定輸出變數(參數位置,參數型別)
                cstmt.setString(2,Aid.get(EventId));
                cstmt.registerOutParameter(3, Types.VARCHAR);
                cstmt.executeUpdate();
                return cstmt.getString("info");
            } catch (Exception e) {
                e.printStackTrace();
                res = e.toString();
            }
            return res;
        }
        //查詢後的結果將回傳於此
        @Override
        protected void onPostExecute(String result) {
            try{
                if (result.equals("報名成功")){
                    btnBuy.setText("取消報名");
                }else if(result.equals("已取消報名")){
                    btnBuy.setText("參加");
                }
                Toast.makeText(EventDetail.this, result, Toast.LENGTH_SHORT).show();
            }catch (Exception e){
                Log.v("test","錯誤: "+e.toString());
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

    public static int DP(float dp){
        dp = dp * ((float) Resources.getSystem().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return (int)dp;
    }

}
