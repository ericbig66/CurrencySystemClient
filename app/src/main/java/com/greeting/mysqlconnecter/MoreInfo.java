package com.greeting.mysqlconnecter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
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
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;

import static com.greeting.mysqlconnecter.Login.acc;
import static com.greeting.mysqlconnecter.Login.pass;
import static com.greeting.mysqlconnecter.Login.url;
import static com.greeting.mysqlconnecter.Login.user;
import static com.greeting.mysqlconnecter.Market.BuyAmount;
import static com.greeting.mysqlconnecter.Market.BuyId;
import static com.greeting.mysqlconnecter.Market.BuyQuantity;
import static com.greeting.mysqlconnecter.Market.PID;
import static com.greeting.mysqlconnecter.Market.PIMG;
import static com.greeting.mysqlconnecter.Market.Pname;
import static com.greeting.mysqlconnecter.Market.Vendor;

public class MoreInfo extends AppCompatActivity {

    public Bitmap ConvertToBitmap(int ID){
        try{
//            Log.v("test",PIMG.get(ID));
            byte[] imageBytes = Base64.decode(PIMG.get(ID), Base64.DEFAULT);
            Bitmap proimg = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            int w = proimg.getWidth();
            int h = proimg.getHeight();
            Log.v("test","pic"+ID+" original = "+w+"*"+h);
            int scale = 1;
            if(w>h && (w/360)>1 || h==w && (w/360)>1){
                scale = w/360;
                w = w/scale;
                h = h/scale;
            }else if(h>w && (h/360)>1){
                scale = h/360;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_more_info);

        ImageView merPic = findViewById(R.id.merPic);
        merPic.setImageBitmap(ConvertToBitmap(BuyId));

        TextView txtName=findViewById(R.id.txtName);
        txtName.setText(Pname.get(BuyId));

        TextView txtVdrName=findViewById(R.id.txtVdrName);
        txtVdrName.setText(Vendor.get(BuyId));

        Qt = findViewById(R.id.Qt);
        Qt.setText(BuyAmount+"");

        Button btnBuy = findViewById(R.id.btnBuy);
        btnBuy.setOnClickListener(v -> Buyer());
    }

    void Buyer() {
        if(Qt.getText().toString().trim().isEmpty()){Qt.setText("0");}
        final int quantity = Integer.parseInt(Qt.getText().toString());
        closekeybord();

        if(quantity>0){
            BuyAmount = Integer.parseInt(Qt.getText().toString().trim());
            ConnectMySql connectMySql = new ConnectMySql();
            connectMySql.execute("");
        }else {
            Toast.makeText(MoreInfo.this, "請至少購買一項商品", Toast.LENGTH_SHORT).show();
        }
    }


    //建立連接與查詢非同步作業
    private class ConnectMySql extends AsyncTask<String, Void, String> {
        String res="";//錯誤信息儲存變數
        //開始執行動作
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            Toast.makeText(MoreInfo.this,"請稍後...",Toast.LENGTH_SHORT).show();
        }
        //查詢執行動作(不可使用與UI相關的指令)
        @Override
        protected String doInBackground(String... strings) {
            ////////////////////////////////////////////
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    Connection con = DriverManager.getConnection(url, user, pass);
                    //建立查詢
                    String result ="";
                    CallableStatement cstmt = con.prepareCall("{call sell(?,?,?,?,?)}");
                    cstmt.setString(1,acc);//設定輸出變數(參數位置,參數型別)
                    cstmt.setString(2,PID.get(BuyId));
                    cstmt.setString(3,Vendor.get(BuyId));
                    cstmt.setInt(4, BuyAmount);
                    cstmt.registerOutParameter(5, Types.VARCHAR);
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
                Toast.makeText(MoreInfo.this, result, Toast.LENGTH_SHORT).show();
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
}

