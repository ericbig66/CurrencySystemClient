package com.greeting.mysqlconnecter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;

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
            case R.id.diary:
                intent = new Intent(MainMenu.this, Diary.class);
                break;
        }
        startActivity(intent);
        finish();
    }
    public void onBackPressed(){
        obp++;
        Timer timer = new Timer(true);

        if(obp>=2){
            Login.wcm ="";
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
        String msg = Login.wcm;
        setContentView(R.layout.layout_main_menu);
        wmsg = findViewById(R.id.msg);
//        wmsg.setText(msg);
        wmsg.setText(Login.wcm);
        profile = findViewById(R.id.profile);
        try {
            profile.setImageBitmap(Login.pf);
            profile.setRotation(Login.pfr);
        }catch (Exception e){}


    }


}
