package com.greeting.mysqlconnecter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;

import static com.greeting.mysqlconnecter.Login.acc;
import static com.greeting.mysqlconnecter.Login.pass;
import static com.greeting.mysqlconnecter.Login.url;
import static com.greeting.mysqlconnecter.Login.user;

public class Purchase extends AppCompatActivity {
    SurfaceView surfaceView;
    BarcodeDetector barcodeDetector;
    CameraSource cameraSource;
    String data="", tmp="", pay, qdata="", sql="", pid="";//掃描到的資料

    boolean trade;
    int amount;



    public void onBackPressed(){
        Intent intent = new Intent(Purchase.this, MainMenu.class);
        startActivity(intent);
    }

    //取得相機使用權
    public void getPromissionCamera(){

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},1);
        }
    }

    public void processQdata(){
//        Log.v("test","分割位置 = "+qdata.indexOf("zpek,"));
        if(qdata.contains("zpek,")) {
            String[] splited = qdata.split("zpek,");
            pay = splited[0];//付款方
            amount = Integer.parseInt(splited[1]);//實際發生金額
            sql ="{call tradeR(?,?,?,?)}";
        }
        else if(qdata.contains("cj/1l,")){
            String[] splited = qdata.split("cj/1l,");
            pay = splited[0];//付款方
            amount = Integer.parseInt(splited[1]);//實際發生金額
            sql = "{call tradeV(?,?,?,?)}";
        }
        else if(qdata.contains("fu02l,")){
            String[] splitted = qdata.split("fu02l,");
            pay = splitted[1];
        }
        else if(qdata.contains("2jo4cj04,")){
            String[] splitted = qdata.split("2jo4cj04,");
            pid = splitted[0];
            pay = splitted[1];//ven
            amount = Integer.parseInt(splitted[2]);
            sql = "{call sell(?,?,?,?,?)}";
        }
        else{
            pay = "QRERR";
            amount = 0;
            sql = "{call tradeV(?,?,?,?)}";
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //接收帳戶資料
//        Intent intent = getIntent();
//        acc = intent.getStringExtra("acc");
        getPromissionCamera();
//        qdata="";
        setContentView(R.layout.layout_purchase);
        surfaceView = findViewById(R.id.surfaceView);
        Boolean autoFocus = Boolean.TRUE;
        barcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build();
        cameraSource = new CameraSource.Builder(this,barcodeDetector).setRequestedPreviewSize(1080,1920).build();
        cameraSource = new CameraSource.Builder(this,barcodeDetector).setAutoFocusEnabled(autoFocus).build();
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                Toast.makeText(Purchase.this,"取得權限中...",Toast.LENGTH_LONG).show();
                if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                    recreate();
                    return;
                }

                try{
                    cameraSource.start(holder);
                }catch(IOException e){
                    Toast.makeText(Purchase.this,"無法啟動您的相機!!\n請允許使用權限!!",Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrCodes=detections.getDetectedItems();

                if(qrCodes.size()!=0){
                    surfaceView.post(() -> {
                        qdata=(qrCodes.valueAt(0).displayValue);
                        if(!tmp.equals(qdata)){
                            tmp=qdata;
                            ConnectMySql connectMySql = new ConnectMySql();
                            connectMySql.execute("");
                        }
                    });
                }
            }
        });

    }
    //建立連接與查詢非同步作業
    private class ConnectMySql extends AsyncTask<String, Void, String> {
        String res="";//錯誤信息儲存變數
        //開始執行動作
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }
        //查詢執行動作(不可使用與UI相關的指令)
        @Override
        protected String doInBackground(String... strings) {
            String result ="";

                processQdata();
                if(qdata.contains("fu02l")) {
                    try {
                        //連接資料庫
                        Class.forName("com.mysql.jdbc.Driver");
                        Connection con = DriverManager.getConnection(url, user, pass);
                        //建立查詢==>交易
                        CallableStatement cstmt = con.prepareCall("{call sign(?,?,?)}");
                        cstmt.setString(1, acc);//設定輸入變數(參數位置,輸入值)
//                Log.v("test","pay"+pay);
                        cstmt.setString(2, pay);
                        cstmt.registerOutParameter(3, Types.VARCHAR);//設定輸出變數(參數位置,參數型別)
                        cstmt.executeUpdate();
                        result = cstmt.getString(3);
                        trade = result.equals("交易成功!");
                        return result;
                    } catch (Exception e) {
                        res = e.toString();
                    }
                }
                else if(qdata.contains("2jo4cj04")){
                    try {
                        //連接資料庫
                        Class.forName("com.mysql.jdbc.Driver");
                        Connection con = DriverManager.getConnection(url, user, pass);
                        //建立查詢==>交易
                        CallableStatement cstmt = con.prepareCall(sql);
                        cstmt.setString(1, acc);//設定輸入變數(參數位置,輸入值)
//                Log.v("test","pay"+pay);
                        cstmt.setString(2, pid);
//                Log.v("test","amount"+amount);
                        cstmt.setString(3, pay);
//                Log.v("test","acc"+acc);
                        cstmt.setInt(4, amount);
                        cstmt.registerOutParameter(5, Types.VARCHAR);//設定輸出變數(參數位置,參數型別)
                        cstmt.executeUpdate();
                        result = cstmt.getString(5);
                        return result;
//                }
                        //return result ;//回傳結果給onPostExecute==>取得輸出變數(位置)
                    } catch (Exception e) {
//                }
//                res = result;
                        e.printStackTrace();
                        res = e.toString();
                    }
                }
                else {
//------------------------------------------------------------------------------------------
                    try {
                        //連接資料庫
                        Class.forName("com.mysql.jdbc.Driver");
                        Connection con = DriverManager.getConnection(url, user, pass);
                        //建立查詢==>交易
                        CallableStatement cstmt = con.prepareCall(sql);
                        cstmt.setString(1, pay);//設定輸入變數(參數位置,輸入值)
//                Log.v("test","pay"+pay);
                        cstmt.setInt(2, amount);
//                Log.v("test","amount"+amount);
                        cstmt.setString(3, acc);
//                Log.v("test","acc"+acc);
                        cstmt.registerOutParameter(4, Types.VARCHAR);//設定輸出變數(參數位置,參數型別)
                        cstmt.executeUpdate();
                        result = cstmt.getString(4);
                        trade = result.equals("交易成功!");
                        if (!trade) {
                            return result;
                        }
//                }
                        //return result ;//回傳結果給onPostExecute==>取得輸出變數(位置)
                    } catch (Exception e) {
//                }
//                res = result;
                        e.printStackTrace();
                        res = e.toString();
                    }

                    try {
                        if (trade) {
                            //連接資料庫
                            Class.forName("com.mysql.jdbc.Driver");
                            Connection con = DriverManager.getConnection(url, user, pass);
                            //建立查詢==>取得餘額
                            result = "";
                            Statement st = con.createStatement();
                            ResultSet rs = st.executeQuery("select L_name, f_name, money from client where account = '" + acc + "'");
                            while (rs.next()) {
                                result = "hide" + rs.getString("L_name") + rs.getString("f_name") + "您好!\n目前您尚有$" + Integer.parseInt(rs.getString("money"));
                            }
                        }
                        return result;
                    } catch (Exception e) {
                        res = result;
                        e.printStackTrace();
                        res = e.toString();
                        return res;
                    }
                }
                return res;
        }
        //查詢後的結果將回傳於此
        @Override
        protected void onPostExecute(String result) {
//            Log.v("test","result = "+result);

            if(result.substring(0,4).equals("hide")){
//                Log.v("test","result Length = "+result.length());
                data = result.substring(4);
//                wcm = data;
                Intent intent = new Intent(Purchase.this,MainMenu.class);
//                intent.putExtra("msg",data);
//                intent.putExtra("acc",acc);
//                Toast.makeText(Purchase.this,result,Toast.LENGTH_LONG).show();
                Toast.makeText(Purchase.this,"交易成功!",Toast.LENGTH_LONG).show();
                startActivity(intent);
                finish();
            }
            else if(result.contains("成功")){
                Toast.makeText(Purchase.this,result,Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Purchase.this,MainMenu.class);
                startActivity(intent);
                finish();
            }
            else{
                Toast.makeText(Purchase.this,result,Toast.LENGTH_LONG).show();
            }

        }
    }
}
