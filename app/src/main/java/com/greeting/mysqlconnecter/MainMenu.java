package com.greeting.mysqlconnecter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.Timer;

import static com.greeting.mysqlconnecter.Login.pass;
import static com.greeting.mysqlconnecter.Login.url;
import static com.greeting.mysqlconnecter.Login.user;

public class MainMenu extends AppCompatActivity {
    TextView wmsg;
    Intent intent;
    ImageView profile;
    final String acc = Login.acc;
    int obp = 0; //times of on back pressed

    public void execute(View v){
        switch (v.getId()){
            case R.id.getcoin:
               intent = new Intent(MainMenu.this, Purchase.class);
                break;
            case R.id.paycoin:
                intent = new Intent(MainMenu.this, Gift.class);
                break;
            case R.id.market:
                intent = new Intent(MainMenu.this, Market.class);
                break;
            case R.id.diary:
                intent = new Intent(MainMenu.this, Diary.class);
                break;
            case R.id.Event:
                intent = new Intent(MainMenu.this, Event.class);
                break;
        }
        startActivity(intent);
        finish();
    }
    public void onBackPressed(){
        obp++;
        Timer timer = new Timer(true);

        if(obp>=2){
            //Login.wcm ="";
            Login.acc ="";
            Intent intent = new Intent(MainMenu.this, Login.class);
            startActivity(intent);
            Login.pf = null;
            finish();
        }
        else{
            Toast.makeText(MainMenu.this,"再按一次返回以登出",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Intent intent = getIntent();
//        String msg = intent.getStringExtra("msg");
//        String msg = Login.wcm;
        setContentView(R.layout.layout_main_menu);
        wmsg = findViewById(R.id.msg);
//        wmsg.setText(msg);
//        wmsg.setText(Login.wcm);
        profile = findViewById(R.id.profile);
        try {
            profile.setImageBitmap(Login.pf);
            profile.setRotation(Login.pfr);
        }catch (Exception e){}

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
            Toast.makeText(MainMenu.this,"請稍後...",Toast.LENGTH_SHORT).show();
        }
        //查詢執行動作(不可使用與UI相關的指令)
        @Override
        protected String doInBackground(String... strings) {
            try{
                //連接資料庫
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(url, user, pass);
                //建立查詢
                String result ="";
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("select L_name, f_name, money from client where account = '"+acc+"'");

                while(rs.next()){
                    result += rs.getString(1).toString()+rs.getString(2)+"您好!\n目前您尚有$"+rs.getInt(3);
                }
                return result;
            }catch (Exception e){
                e.printStackTrace();
                res = e.toString();
                return res;
            }

        }
        //查詢後的結果將回傳於此
        @Override
        protected void onPostExecute(String result) {
            wmsg.setText(result);
        }
    }

}
