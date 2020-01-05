package com.greeting.mysqlconnecter;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;

import static android.app.PendingIntent.getActivity;


public class Login extends AppCompatActivity {
    //render bug resolver
    public static int rc = 0;
    final float VERSION = 0.1f;
    //連接資料庫的IP、帳號(不可用root)、密碼
    public static final String url = "jdbc:mysql://140.135.113.196:3360/virtualcurrencyproject";
    public static final String user = "currency";
    public static final String pass = "@SAclass";
    public static String pfs;//profile String
    public static Bitmap pf;//profile picture
    public static float pfr;//profile rotation
//    public static String wcm;//welcome message
    public static String acc;//account

    Button btnFetch, btnClear, reg;
    TextView txtData;
    EditText myacc, pwd;
    String account, password, data;



    public void swreg(){  //切換註冊頁面
        Intent intent = new Intent(this, NewRegister.class);
        startActivity(intent);
    }

    public void swmenu(){   //切換到主選單
        Intent intent = new Intent(this, MainMenu.class);
        //intent.putExtra("acc",account);
        intent.putExtra("msg",data);
//        wcm = data;
        startActivity(intent);
//    finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);

        btnFetch = findViewById(R.id.btnFetch);//登入
        btnClear = findViewById(R.id.btnClear);//清除
        reg = findViewById(R.id.reg);//切換到註冊頁面
        txtData =  findViewById(R.id.txtData);//登入結果
        myacc = findViewById(R.id.acc);//帳號(Email)
        pwd = findViewById(R.id.pwd);//密碼
//        dtv = findViewById(R.id.dtv);

        //註冊紐動作
        reg.setOnClickListener(v -> swreg());
        //登入紐動作
        btnFetch.setOnClickListener(v -> {
            account = myacc.getText().toString();
            password = pwd.getText().toString();
//                dtv.setText("call login(@fname, "+account+", "+password+"); select @fname;");
            if(account.trim().isEmpty()||password.trim().isEmpty()){
                Toast.makeText(Login.this,"請輸入帳號密碼以登入!",Toast.LENGTH_SHORT).show();
            }else{
                ConnectMySql connectMySql = new ConnectMySql();
                connectMySql.execute("");
            }

        });

        //清除紐動作
        btnClear.setOnClickListener(v -> {
            txtData.setText("");
            myacc.setText(null);
            pwd.setText(null);
        });

       CheckUpdate checkUpdate = new CheckUpdate();
       checkUpdate.execute("");
    }

    public void updateAlert(){
        //建立更新資訊提示
        AlertDialog.Builder newver = new AlertDialog.Builder(this);
        newver.setTitle("請更新至最新版");
        newver.setMessage("為了您的帳戶安全，請點選下載更新以更新至最新\n若您無法更新或不會更新，您可以撥打0800 000 123 詢問客服人員");
        // Add the buttons
        newver.setPositiveButton(R.string.update, (dialog, id) -> {
            // User clicked OK button
            Log.v("test","即將更新");
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://chen1998.yabi.me/client.apk"));
            startActivity(browserIntent);
        });
        newver.setNegativeButton(R.string.leave, (dialog, id) -> {
            // User cancelled the dialog
            Log.v("test", "即將離開系統");
            finish();
        });

        // Create the AlertDialog
        AlertDialog dialog = newver.create();
        closekeybord();
        dialog.show();
    }

    //隱藏鍵盤
    public void closekeybord() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void ConvertToBitmap(){
        try{
            byte[] imageBytes = Base64.decode(pfs, Base64.DEFAULT);
            pf = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        }catch (Exception e){
            Log.v("test","error = "+e.toString());
        }

    }

    private class ConnectMySql extends AsyncTask<String, Void, String> {
        String res="";//錯誤信息儲存變數
        //開始執行動作
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            Toast.makeText(Login.this,"請稍後...",Toast.LENGTH_SHORT).show();
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
                //Statement st = con.createStatement();
//                ResultSet rs = st.executeQuery("call login(@fname, '"+account+"', '"+password+"'); select @fname;");
                //experiment part start
                //此處呼叫Stored procedure(call 函數名稱(?)==>問號數量代表輸出、輸入的變數數量)
                CallableStatement cstmt = con.prepareCall("{call login(?,?,?,?,?,?,?)}");
                cstmt.registerOutParameter(1, Types.VARCHAR);//設定輸出變數(參數位置,參數型別)
                cstmt.registerOutParameter(2, Types.VARCHAR);
                cstmt.registerOutParameter(3, Types.INTEGER);
                cstmt.setString(4, account);//設定輸入變數(參數位置,輸入值)
                cstmt.setString(5, password);
                cstmt.registerOutParameter(6, Types.LONGVARCHAR);
                cstmt.registerOutParameter(7, Types.FLOAT);
                cstmt.executeUpdate();
                pfs = cstmt.getString("propic");
                pfr = cstmt.getFloat("degree");
                ConvertToBitmap();
                return cstmt.getString(1)+cstmt.getString(2)+"您好!\n目前您尚有$"+cstmt.getString(3);//回傳結果給onPostExecute==>取得輸出變數(位置)
                //experiment part end
//
//                while(rs.next()){
//                    result += rs.getString(1).toString()+"您好!";
//                }
            }catch (Exception e){
//                res = result;
                e.printStackTrace();
                res = e.toString();
            }
            return res;
        }
        //查詢後的結果將回傳於此
        @Override
        protected void onPostExecute(String result) {

            if (result.equals("null遊客您好!\n目前您尚有$null")){
                result= "遊客您好!\n如需購物請先註冊帳號\n謝謝您的合作!";
                txtData.setText(result);//設定結果顯示
            }
            else{
                data = result;
                acc=account;
//                Toast.makeText(Login.this, "請稍後", Toast.LENGTH_SHORT).show();
                Log.v("test",result);
                swmenu();
            }


        }
    }



    //建立連接與查詢非同步作業
    private class CheckUpdate extends AsyncTask<String, Void, String> {
        String res="";//錯誤信息儲存變數

        //開始執行動作
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            Toast.makeText(Login.this,"正在檢查更新...",Toast.LENGTH_SHORT).show();
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
                ResultSet rs = st.executeQuery("select ver from version ORDER BY ver DESC");
                rs.next();
                result = rs.getString("ver");
                Log.v("test","current version = "+rs.getString("ver"));
                if(Float.parseFloat(result)<VERSION){
                    Connection con2 = DriverManager.getConnection(url, user, pass);
                    //建立查詢
                    Statement st2 = con.createStatement();
                    st2.execute("insert into version(ver,ReleaseDate) values("+VERSION+", now())");
                }
                return rs.getFloat("ver")+"";
            }catch (Exception e){
//                res = result;
                e.printStackTrace();
                res = e.toString();
            }
            return res;
        }
        //查詢後的結果將回傳於此
        @Override
        protected void onPostExecute(String result) {
           Float ver = -1f;
            try {
                ver = Float.parseFloat(result.trim());
                if(VERSION<ver){
                    updateAlert();
                }else{
                    Log.v("test", "no new update detected!!");
                }
            }catch(Exception e){
                Log.v("test", "version check error = " + e.toString());
            }

        }
    }
}
