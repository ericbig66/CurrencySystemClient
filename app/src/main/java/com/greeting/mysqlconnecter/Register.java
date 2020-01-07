package com.greeting.mysqlconnecter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.ByteArrayOutputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.greeting.mysqlconnecter.Login.pass;
import static com.greeting.mysqlconnecter.Login.url;
import static com.greeting.mysqlconnecter.Login.user;


public class Register extends AppCompatActivity {


    static final int OPEN_PIC = 1021;

    EditText ln, fn, em, bd, ad, pwd, chkpwd;
    RadioButton m, f;
    Button pic, reg, login, clr, rotate;
    DatePicker dtp;
    CircularImageView profile;
    Bitmap dataToConvert;
    String b64="";
//    LinearLayout df;
    //系統時間及格式設定
    Date curDate = new Date(System.currentTimeMillis()) ;//取得系統時間
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");//格式化日期顯示方式
    //格式化出可直接使用的年月日變數
    SimpleDateFormat year = new SimpleDateFormat("yyyy");
    SimpleDateFormat month = new SimpleDateFormat("mm");
    SimpleDateFormat day = new SimpleDateFormat("dd");
    String yyyy = year.format(curDate);
    String mm = month.format(curDate);
    String dd = day.format(curDate);
    //裝載轉換出的EditText中的文字
    String LN="", FN="" ,EM="" ,AD = "" ,GEN = "" ,PWD = "" ,CHKPWD="" ,BD="";
    //清除所有填寫的資料(會被重新填寫按鈕呼叫或註冊成功時會被呼叫)
    public void clear(){
        ln.setText("");
        fn.setText("");
        em.setText("");
        ad.setText("");
        pwd.setText("");
        bd.setText("");

        chkpwd.setText("");
        LN="";
        FN="";
        EM="";
        AD = "";
        GEN = "";
        PWD = "";
        BD="";
        b64="";
        profile.setVisibility(View.GONE);
        dtp.init(Integer.parseInt(yyyy), Integer.parseInt(mm), Integer.parseInt(dd), null);
        f.setChecked(false);
        m.setChecked(false);
        rotate.setVisibility(View.GONE);

    }
    //切換回登入模式(被該按鈕呼叫)
    public void swlogin(){
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }

    //隱藏鍵盤
    public void closekeybord() {
        View view = this.getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }
    //檢查填寫資料正確性(按下註冊鈕後呼叫)
    public void verify(){
        boolean haveError = false;
        String err ="";
        err = LN.trim().isEmpty()?err+="姓氏,":err;
        err = FN.trim().isEmpty()?err+="名字,":err;
        err = PWD.trim().isEmpty()?err+="密碼,":err;
        err = CHKPWD.trim().isEmpty()?err+="確認密碼,":err;
        err = EM.trim().isEmpty()?err+="E-mail,":err;
        err = b64.trim().isEmpty()?err+="上傳頭像,":err;
        err = err.isEmpty()?err:err.substring(0, err.length() - 1);
        if(!err.isEmpty()){err+=" 為必填項目\n請確認是否已填寫!";}
        haveError = !err.isEmpty();
        if(haveError){Toast.makeText(Register.this, err, Toast.LENGTH_LONG).show();}
        err = "";
        if(!PWD.trim().isEmpty() && !CHKPWD.trim().isEmpty() && !PWD.equals(CHKPWD)){
            err += "您輸入的密碼前後不一致，請重新輸入\n";
            chkpwd.setText("");
            CHKPWD = "";
            haveError = true;
        }
// due to register policy change this verifyer is no longer being used
//        if ( !EM.trim().isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(EM).matches() ) {
//            err += "請輸入正確的電子郵件地址";
//            em.setText("");
//            EM = "";
//            haveError = true;
//        }
        if(!EM.trim().isEmpty() && EM.trim().length()<9){
            err += "您的電話或手機號碼格式不正確!";
            haveError = true;
        }
        if(haveError && !err.trim().isEmpty()){Toast.makeText(Register.this, err, Toast.LENGTH_LONG).show();}
        if(!haveError){
            ConnectMySql connectMySql = new ConnectMySql();
            connectMySql.execute("");
        }
    }

    //初始化程式
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_register);

        ln = findViewById(R.id.ln);
        fn = findViewById(R.id.fn);
        em = findViewById(R.id.em);
        bd = findViewById(R.id.bd);
        pwd = findViewById(R.id.pwd);
        chkpwd = findViewById(R.id.chkpwd);
        ad = findViewById(R.id.ad);

        m = findViewById(R.id.m);
        f = findViewById(R.id.f);

        pic = findViewById(R.id.pic);
        reg = findViewById(R.id.reg);
        login = findViewById(R.id.login);
        clr = findViewById(R.id.clr);

        profile = findViewById(R.id.profile);

        dtp = findViewById(R.id.dtp);

        dtp.setMaxDate(new Date().getTime());

        pic.setOnClickListener(v -> picOpen());

        login.setOnClickListener(v -> swlogin());

        reg.setOnClickListener(v -> {
            closekeybord();
            LN = ln.getText().toString();
            FN = fn.getText().toString();
            EM = em.getText().toString();
            PWD = pwd.getText().toString();
            AD = ad.getText().toString();
            CHKPWD = chkpwd.getText().toString();
            //處理日期問題(月份因預設為0~11故須+1)
            //月分與日期不足10時需增加0以補齊兩位
            String MM,DD;
            MM = (dtp.getMonth()+1)>9?(dtp.getMonth()+1)+"" : "0"+(dtp.getMonth()+1);
            DD = dtp.getDayOfMonth()>9?dtp.getDayOfMonth()+"": "0"+dtp.getDayOfMonth();
            bd.setText(dtp.getYear()+"/"+MM+"/"+DD);
            BD = bd.getText().toString();

            //系統時間格式化(原始以秒為單位)
            String today = formatter.format(curDate);//將今天的日期儲存為指定格式

            if(BD.equals(today)){
                BD = "";
            }
            verify();
        });

        rotate = findViewById(R.id.rotate);


        clr.setOnClickListener(v -> clear());
        f.setOnCheckedChangeListener((buttonView, isChecked) -> {
            closekeybord();
            if(isChecked){GEN = "f";}
        });

        m.setOnCheckedChangeListener((buttonView, isChecked) -> {
            closekeybord();
            if(isChecked){GEN = "m";}
        });

        rotate.setOnClickListener(v -> {
                rotate();
        });

        for(int i =0 ; i<10 ; i++){
            Log.v("test", "data["+i+"] = "+NewRegister.data[i]);
        }

        try{
            if(NewRegister.data[9].equals("y")){autoFill();}
        }catch (Exception e){}
    }

    public void autoFill(){ //自動輸入
        String tmp;
        //性別
        try {
            if(NewRegister.data[1].equals("m")){m.setChecked(true);}
            else if(NewRegister.data[1].equals("f")){f.setChecked(true);}
        }catch (Exception e){}

        //姓名
        tmp = NewRegister.data[2];
        if(tmp.charAt(0)>=97 && tmp.charAt(0)<=122 || tmp.charAt(0)>=65 && tmp.charAt(0)<=89){
            try{
                String nm[]= new String[2];
                nm = tmp.trim().split(" ");
                fn.setText(nm[0]);
                ln.setText(nm[1]);
                Log.v("test","cut1");
            }catch (Exception e){}
        }else{
//            Log.v("test","cut2");
//            Log.v("test",tmp.substring(0,1));
//            Log.v("test",tmp.substring(1));
            ln.setText(tmp.substring(0,1));
            fn.setText(tmp.substring(1));
        }

        //生日
        tmp = NewRegister.data[3];
        String birth[] = new String[3];
        birth = tmp.split("/");
        dtp.init(Integer.parseInt(birth[0]), Integer.parseInt(birth[1]), Integer.parseInt(birth[2]), null);

        //E-mail
        em.setText(NewRegister.data[4]);

        //密碼
        pwd.setText(NewRegister.data[5]);

        //確認密碼
        chkpwd.setText(NewRegister.data[6]);

        //地址
        ad.setText(NewRegister.data[7]);

        //頭像
        tmp = NewRegister.data[8];
        String splitter[] = new String[2];
        splitter = tmp.split(",");
        Log.v("test","URI = "+splitter[0]);
        Uri imgdata = Uri.parse(splitter[0]);
        profile.setImageURI(imgdata);
        profile.setVisibility(View.VISIBLE);
        rotate.setVisibility(View.VISIBLE);
        dataToConvert = ((BitmapDrawable)profile.getDrawable()).getBitmap();
        ConvertToBase64 convertToBase64 = new ConvertToBase64();
        convertToBase64.execute("");

        //頭像角度
        degree = Float.parseFloat(splitter[1]) -90f;
        rotate();
        NewRegister.data= new String[1]; // 壓縮記憶體空間
    }

    Float degree = 0f;
    public void rotate(){
        degree=(degree+90f)>=(360f)?0f:degree+90f;
        profile.setRotation(degree);
    }

    private class ConnectMySql extends AsyncTask<String, Void, String> {
        String res="";

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            Toast.makeText(Register.this,"註冊中...",Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try{


                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(url, user, pass);
                String result ="";
                CallableStatement cstmt = con.prepareCall("{call register(?,?,?,?,?,?,?,?,?,?)}");
                cstmt.setString(1, FN);
                cstmt.setString(2, LN);
                cstmt.setString(3, EM);
                cstmt.setString(4, PWD);
                cstmt.setString(5, BD);
                cstmt.setString(6, AD);
                cstmt.setString(7, GEN);
                cstmt.setString(9, b64);
                cstmt.setFloat(10,degree);
                cstmt.registerOutParameter(8,Types.VARCHAR);
                cstmt.executeUpdate();
                return cstmt.getString(8);

            }catch (Exception e){
                e.printStackTrace();
                res = e.toString();

            }
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(Register.this, result, Toast.LENGTH_SHORT).show();
//            Log.v("test",result);
            if(result.equals("註冊成功!")){
                clear();
                swlogin();
                finish();
            }


        }


    }
    //********************************************************************************************



    //開啟頭像
    public void picOpen(){
        ((BitmapDrawable)profile.getDrawable()).getBitmap().recycle();//一定要做否則會當機
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"請選擇您的頭像"), OPEN_PIC);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == OPEN_PIC && resultCode == RESULT_OK){

            Uri imgdata = data.getData();
            profile.setImageURI(imgdata);
            profile.setVisibility(View.VISIBLE);
            rotate.setVisibility(View.VISIBLE);
            dataToConvert = ((BitmapDrawable)profile.getDrawable()).getBitmap();
            ConvertToBase64 convertToBase64 = new ConvertToBase64();
            convertToBase64.execute("");
        }

    }

    //將圖片轉換為Base64字串
    private class ConvertToBase64 extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(Register.this,"請稍後...",Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... strings) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            dataToConvert.compress(Bitmap.CompressFormat.JPEG, 30, baos);
            byte[] imageBytes = baos.toByteArray();
            String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            return imageString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            b64 = s;
        }
    }

    //    //取得圖片路徑
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
////        Toast.makeText(Register.this, "hi!",Toast.LENGTH_SHORT).show();
//        if(requestCode == OPEN_PIC && RESULT_OK == resultCode){
//            Uri uri = data.getData();
//            try{
//                String[] projection = {MediaStore.Images.Media.DATA};
//                CursorLoader cursorLoader = new CursorLoader(this, uri, projection, null, null, null);
//                Cursor cursor = cursorLoader.loadInBackground();
//                int colum_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//                cursor.moveToFirst();
//
//                String path = cursor.getString(colum_index);
//                encode(path);
//
//            }catch (Exception e){
//                Toast.makeText(Register.this,e.toString(),Toast.LENGTH_LONG).show();
//            }
//        }
//    }
    //將圖片編碼為base64
//    private void encode(String path){
//        Bitmap bitmap = BitmapFactory.decodeFile(path);
//        ByteArrayOutputStream boas = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, boas);
//        byte[] bytes = new  byte[100];//???????????
//        byte[] encode = Base64.encode(bytes,Base64.DEFAULT);
//        String encodeString = new String(encode);
//        Toast.makeText(Register.this, encodeString, Toast.LENGTH_LONG).show();
//    }

}

