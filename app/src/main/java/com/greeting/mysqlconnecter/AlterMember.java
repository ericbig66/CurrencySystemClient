package com.greeting.mysqlconnecter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
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
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.greeting.mysqlconnecter.Login.acc;
import static com.greeting.mysqlconnecter.Login.pass;
import static com.greeting.mysqlconnecter.Login.pf;
import static com.greeting.mysqlconnecter.Login.pfs;
import static com.greeting.mysqlconnecter.Login.url;
import static com.greeting.mysqlconnecter.Login.user;

public class AlterMember extends AppCompatActivity {

    static final int OPEN_PIC = 1021;

    EditText ln, fn, em, bd, ad, pwd, chkpwd, oldpwd;
    RadioButton m, f;
    Button pic, reg, clr, rotate, cancel;
    DatePicker dtp;
    CircularImageView profile;
    Bitmap dataToConvert;
    String b64 = pfs;
    //    LinearLayout df;
    //系統時間及格式設定
    Date curDate = new Date(System.currentTimeMillis());//取得系統時間
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");//格式化日期顯示方式
    //格式化出可直接使用的年月日變數
    SimpleDateFormat year = new SimpleDateFormat("yyyy");
    SimpleDateFormat month = new SimpleDateFormat("mm");
    SimpleDateFormat day = new SimpleDateFormat("dd");
    String yyyy = year.format(curDate);
    String mm = month.format(curDate);
    String dd = day.format(curDate);
    //裝載轉換出的EditText中的文字
    String LN = "", FN = "", EM = "", AD = "", GEN = "", PWD = "", CHKPWD = "", BD = "", OPWD = "";
    int function =0;
    String autofillData="";

    //清除所有填寫的資料(會被重新填寫按鈕呼叫或註冊成功時會被呼叫)
    public void clear() {
        ln.setText("");
        fn.setText("");
        em.setText("");
        ad.setText("");
        pwd.setText("");
        bd.setText("");

        chkpwd.setText("");
        LN = "";
        FN = "";
        EM = acc;
        AD = "";
        GEN = "";
        PWD = "";
        BD = "";
        b64 = pfs;

        dtp.init(Integer.parseInt(yyyy), Integer.parseInt(mm), Integer.parseInt(dd), null);
        f.setChecked(false);
        m.setChecked(false);
        autoFill();
    }

    //切換回登入模式(被該按鈕呼叫)
    public void swlogin() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }

    //隱藏鍵盤
    public void closekeybord() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    //檢查填寫資料正確性(按下註冊鈕後呼叫)
    public void verify() {
        boolean haveError = false;
        String err = "";
        err = LN.trim().isEmpty() ? err += "姓氏," : err;
        err = FN.trim().isEmpty() ? err += "名字," : err;
        if (PWD.trim().isEmpty() && CHKPWD.trim().isEmpty()&& !OPWD.trim().isEmpty()) {
            PWD = OPWD;
            CHKPWD = OPWD;
        }else if(OPWD.trim().isEmpty()){err+="目前密碼,";}
        else{
            err = PWD.trim().isEmpty() ? err += "新密碼," : err;
            err = CHKPWD.trim().isEmpty() ? err += "確認新密碼," : err;
        }

        err = EM.trim().isEmpty() ? err += "E-mail," : err;
        err = b64.trim().isEmpty() ? err += "上傳頭像," : err;
//        Log.v("test","b64 = "+b64);
        err = err.isEmpty() ? err : err.substring(0, err.length() - 1);
        if (!err.isEmpty()) {
            err += " 為必填項目\n請確認是否已填寫!";
        }
        haveError = !err.isEmpty();
        if (haveError) {
            Toast.makeText(AlterMember.this, err, Toast.LENGTH_LONG).show();
        }
        err = "";
        if (!PWD.trim().isEmpty() && !CHKPWD.trim().isEmpty() && !PWD.equals(CHKPWD)) {
            err += "您輸入的密碼前後不一致，請重新輸入\n";
            chkpwd.setText("");
            CHKPWD = "";
            haveError = true;
        }

//        if (!EM.trim().isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(EM).matches()) {
//            err += "請輸入正確的電子郵件地址";
//            em.setText("");
//            EM = "";
//            haveError = true;
//        }

//        if(!EM.trim().isEmpty() && EM.trim().length()<9){
//            err += "您的電話或手機號碼格式不正確!";
//
//        }

        if (haveError && !err.trim().isEmpty()) {
            Toast.makeText(AlterMember.this, err, Toast.LENGTH_LONG).show();
        }
        if (!haveError) {
            ConnectMySql connectMySql = new ConnectMySql();
            connectMySql.execute("");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_alter_member);

        ln = findViewById(R.id.ln);
        fn = findViewById(R.id.fn);
        em = findViewById(R.id.em);
        bd = findViewById(R.id.bd);
        pwd = findViewById(R.id.pwd);
        chkpwd = findViewById(R.id.chkpwd);
        ad = findViewById(R.id.ad);
        oldpwd = findViewById(R.id.oldpwd);//////////////////////////////自動填入問題

        m = findViewById(R.id.m);
        f = findViewById(R.id.f);

        profile = findViewById(R.id.profile);

        dtp = findViewById(R.id.dtp);
        dtp.setMaxDate(new Date().getTime());
        dtp.init(Integer.parseInt(yyyy), Integer.parseInt(mm), Integer.parseInt(dd), null);
//        cancel.setOnClickListener(v -> {
//            m.setChecked(false);
//            f.setChecked(false);
//        });
        pic = findViewById(R.id.pic);
        reg = findViewById(R.id.reg);
        clr = findViewById(R.id.clr);
        cancel = findViewById(R.id.cancel);


        pic.setOnClickListener(v -> picOpen());



        reg.setOnClickListener(v -> {
            closekeybord();
            LN = ln.getText().toString();
            FN = fn.getText().toString();
            EM = acc;
            PWD = pwd.getText().toString();

            AD = ad.getText().toString();
            CHKPWD = chkpwd.getText().toString();
            //處理日期問題(月份因預設為0~11故須+1)
            //月分與日期不足10時需增加0以補齊兩位
            String MM, DD;
            MM = (dtp.getMonth() + 1) > 9 ? (dtp.getMonth() + 1) + "" : "0" + (dtp.getMonth() + 1);
            DD = dtp.getDayOfMonth() > 9 ? dtp.getDayOfMonth() + "" : "0" + dtp.getDayOfMonth();
            bd.setText(dtp.getYear() + "/" + MM + "/" + DD);
            BD = bd.getText().toString();
            Log.v("test","BD="+BD);
            OPWD = oldpwd.getText().toString();


            //系統時間格式化(原始以秒為單位)
            String today = formatter.format(curDate);//將今天的日期儲存為指定格式

            if (BD.equals(today)) {
                BD = "";
            }
            verify();
        });

        rotate = findViewById(R.id.rotate);


        clr.setOnClickListener(v -> clear());
        f.setOnCheckedChangeListener((buttonView, isChecked) -> {
            closekeybord();
            if (isChecked) {
                GEN = "f";
            }
        });

        m.setOnCheckedChangeListener((buttonView, isChecked) -> {
            closekeybord();
            if (isChecked) {
                GEN = "m";
            }
        });

        rotate.setOnClickListener(v -> {
            rotate();
        });

//        for (int i = 0; i < 10; i++) {
//            Log.v("test", "data[" + i + "] = " + NewRegister.data[i]);
//        }

        ConnectMySql connectMySql = new ConnectMySql();
        connectMySql.execute("");

    }

    public void autoFill() { //自動輸入 L_name, f_name, gender, address, profileRotate, birthday
       Log.v("test","autofill=\n"+autofillData);
        String fdata[] = autofillData.split(",");
        String tmp;
        em.setText(acc);
        ln.setText(fdata[0]);
        fn.setText(fdata[1]);

        //性別
        try {
            if (fdata[2].equals("m")) {
                m.setChecked(true);
            } else if (fdata[2].equals("f")) {
                f.setChecked(true);
            }
        } catch (Exception e) {}

        ad.setText(fdata[3].equals("null")?"":fdata[3]);



        //生日
        tmp = fdata[5];
        if(!tmp.equals("0000-00-00")){
            String birth[] = tmp.split("-");
            dtp.init(Integer.parseInt(birth[0]), Integer.parseInt(birth[1])-1, Integer.parseInt(birth[2]), null);
        }

//頭像角度
        degree = Float.parseFloat(fdata[4]) - 90f;
        rotate();

        profile.setImageBitmap(pf);


    }


    Float degree = 0f;

    public void rotate() {
        degree = (degree + 90f) >= (360f) ? 0f : degree + 90f;
        profile.setRotation(degree);
    }

    private class ConnectMySql extends AsyncTask<String, Void, String> {
        String res = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(AlterMember.this, "註冊中...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {


                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(url, user, pass);
                String result = "";
                Statement st = con.createStatement();
                if(function == 0){

                    ResultSet rs = st.executeQuery("select L_name, f_name, gender, address, profileRotate from client where account = '"+acc+"'");

                    while(rs.next()){
                        result += rs.getString(1)+","+rs.getString(2)+","+rs.getString(3)+","+rs.getString(4)+","+rs.getInt(5)+",";
                    }


                    try {
                        rs = st.executeQuery("select birthday form client where account = '"+acc+"'");
                        result += rs.getString(1);
//                        Log.v("test","birthday = "+rs.getString(1));
                    }catch (Exception e){
                        result += "0000-00-00";
                    }

                    return result;
                }else if(function == 1){
                    CallableStatement cstmt = con.prepareCall("{call login(?,?,?,?,?,?,?)}");
                    cstmt.registerOutParameter(1, Types.VARCHAR);//設定輸出變數(參數位置,參數型別)
                    cstmt.registerOutParameter(2, Types.VARCHAR);
                    cstmt.registerOutParameter(3, Types.INTEGER);
                    cstmt.setString(4, acc);//設定輸入變數(參數位置,輸入值)
                    cstmt.setString(5, OPWD);
                    cstmt.registerOutParameter(6, Types.LONGVARCHAR);
                    cstmt.registerOutParameter(7, Types.FLOAT);
                    cstmt.executeUpdate();
                    if(!cstmt.getString("fname").equals("遊客")){
                        cstmt = con.prepareCall("{call alter_member(?,?,?,?,?,?,?,?,?,?)}");
                        cstmt.setString(1, acc);
                        cstmt.setString(2, PWD);
                        cstmt.setString(3, FN);
                        cstmt.setString(4, LN);
                        cstmt.setString(5, AD);
                        cstmt.setString(6, BD);
                        cstmt.setString(7, GEN);
                        cstmt.setString(8, b64);
                        cstmt.setFloat(9, degree);
                        cstmt.registerOutParameter(10, Types.VARCHAR);
                        cstmt.executeUpdate();
                        return cstmt.getString(10);
                    }else{return "您目前的密碼有誤\n如您忘記密碼，請聯絡客服人員";}


                }

            } catch (Exception e) {
                e.printStackTrace();
                res = e.toString();

            }
            return res;
        }

        @Override
        protected void onPostExecute(String result) {

//            Log.v("test",result);
            if (result.equals("修改成功!")) {
                Toast.makeText(AlterMember.this, result, Toast.LENGTH_SHORT).show();
                clear();
                ((BitmapDrawable)profile.getDrawable()).getBitmap().recycle();
                finish();
            }else if(function == 0){
                autofillData = result;
                autoFill();
                function =1;
            }
            else{
                Toast.makeText(AlterMember.this, result, Toast.LENGTH_SHORT).show();

            }


        }

    }
    //********************************************************************************************


    //開啟頭像
    public void picOpen() {
        ((BitmapDrawable) profile.getDrawable()).getBitmap().recycle();//一定要做否則會當機
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "請選擇您的頭像"), OPEN_PIC);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_PIC && resultCode == RESULT_OK) {

            Uri imgdata = data.getData();
            profile.setImageURI(imgdata);
            profile.setVisibility(View.VISIBLE);
            rotate.setVisibility(View.VISIBLE);
            dataToConvert = ((BitmapDrawable) profile.getDrawable()).getBitmap();
            ConvertToBase64 convertToBase64 = new ConvertToBase64();
            convertToBase64.execute("");
        }

    }



    //將圖片轉換為Base64字串
    private class ConvertToBase64 extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(AlterMember.this, "請稍後...", Toast.LENGTH_SHORT).show();
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
    public static int DP(float dp) {
        dp = dp * ((float) Resources.getSystem().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return (int) dp;
    }

    public void onBackPressed(){
        Intent intent = new Intent(AlterMember.this, MainMenu.class);
        startActivity(intent);
        clear();
        finish();
    }
}
