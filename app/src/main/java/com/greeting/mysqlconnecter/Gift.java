package com.greeting.mysqlconnecter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class Gift extends AppCompatActivity {
    Button pay;
    EditText amount;
    ImageView qrCode;
    final String acc =Login.acc;

    public void onBackPressed(){
        Intent intent = new Intent(Gift.this, MainMenu.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_gift);

        pay = findViewById(R.id.pay);
        amount = findViewById(R.id.amount);
        qrCode = findViewById(R.id.qrCode);
    }

    public void getCode(View v) {
        BarcodeEncoder encoder = new BarcodeEncoder();
        Log.v("test",Login.acc+"zpek," +amount .getText().toString());
        try{
            Bitmap bit = encoder.encodeBitmap(Login.acc+"zpek," +amount .getText().toString()
                    , BarcodeFormat.QR_CODE,1000,1000);
            qrCode.setImageBitmap(bit);
        }catch (WriterException e){
            e.printStackTrace();
        }
    }

}
