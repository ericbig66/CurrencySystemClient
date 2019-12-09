package com.greeting.mysqlconnecter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.icu.text.MessagePattern;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.renderscript.Type;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewRegister extends AppCompatActivity {
    public static String data[] = new String[10];
    int counter = 0;
    TextView question, accInf;
    Button prev, next, build, upload, rotate;
    DatePicker dtp;
    EditText answer;
    RadioGroup gender, haveMail;
    RadioButton m, f, yes, no;
    CircularImageView profile;
    LinearLayout proBtns;

   final static int OPEN_PIC = 1021;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_new_register);


        proBtns = findViewById(R.id.proBtns);
        upload = findViewById(R.id.upload);
        rotate = findViewById(R.id.rotate);
        profile = findViewById(R.id.profile);
        build = findViewById(R.id.build);
        question = findViewById(R.id.question);
        accInf = findViewById(R.id.accInf);
        prev = findViewById(R.id.prev);
        next = findViewById(R.id.next);
        dtp = findViewById(R.id.dtp);
        answer =(EditText) findViewById(R.id.answer);

        m = findViewById(R.id.m);
        f = findViewById(R.id.f);
        gender = findViewById(R.id.gender);
        yes = findViewById(R.id.yes);
        no = findViewById(R.id.no);
        no.setChecked(true);
        haveMail = findViewById(R.id.haveMail);
        no.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                answer.setHint(R.string.acc);
                accInf.setVisibility(View.GONE);
                next.setEnabled(false);
                if(!answer.getText().toString().trim().isEmpty()){
                    build.setVisibility(View.VISIBLE);
                }
            }

        });

        yes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                answer.setHint(R.string.mail);
                build.setVisibility(View.GONE);
                accInf.setText(R.string.rem_acc);
                accInf.setVisibility(View.VISIBLE);
                next.setEnabled(true);
            }

        });

        build.setOnClickListener(v -> {
            accInf.setText(R.string.rem_acc_nomail);
            String tmp = accInf.getText().toString()+answer.getText().toString()+"@happy.coin";
            accInf.setText(tmp);
            accInf.setVisibility(View.VISIBLE);
            next.setEnabled(true);
        });

        answer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(counter == 4 && no.isChecked() && !answer.getText().toString().trim().isEmpty()){
                    build.setVisibility(View.VISIBLE);
                }else{
                    build.setVisibility(View.GONE);
                }

                if(counter >=4 && counter<=6 /*&& !answer.getText().toString().trim().isEmpty()*/ ){
                    Pattern ps = Pattern.compile("[\\u4e00-\\u9fa5]");
                    Log.v("test","testing = "+answer.getText().toString());
                    String tmp[] = answer.getText().toString().split("");
                    for(int i=0 ; i<answer.getText().toString().length() ; i++){
                        Matcher ms = ps.matcher(tmp[i]);
                        if(ms.matches()){
                            answer.setText("");
                            if(no.isChecked()){accInf.setVisibility(View.GONE);}
                            Toast.makeText(NewRegister.this,"檢測到非法字元",Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        upload.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"請選擇您的頭像"), OPEN_PIC);
        });

        rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotate();
            }
        });

        for(int i = 0 ; i<10 ; i++){data[i] = "";}
        question_render();

    }

    Uri imgdata;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == OPEN_PIC && resultCode == RESULT_OK) {
            imgdata = data.getData();
            profile.setImageURI(imgdata);
            profile.setVisibility(View.VISIBLE);
            rotate.setVisibility(View.VISIBLE);
        }
    }

    public void viewControler(){
        if(counter == 0){prev.setEnabled(false);}
        else{prev.setEnabled(true);}

        if(counter == 3){dtp.setVisibility(View.VISIBLE);}
        else{dtp.setVisibility(View.GONE);}

        if(counter == 1){gender.setVisibility(View.VISIBLE);}
        else{gender.setVisibility(View.GONE);}

        if(counter == 4){
            haveMail.setVisibility(View.VISIBLE);
        }
        else{
            haveMail.setVisibility(View.GONE);
            build.setVisibility(View.GONE);
            accInf.setVisibility(View.GONE);
        }

        if(counter == 8){
            proBtns.setVisibility(View.VISIBLE);}
        else{
            proBtns.setVisibility(View.GONE);
            profile.setVisibility(View.GONE);
        }

        if(counter >= 9){next.setText("檢查去");}
        else{next.setText("下一步");}

        if(counter == 1 || counter == 3 || counter >= 8){answer.setVisibility(View.GONE);}
        else if(counter == 4 && !data[counter].trim().isEmpty() && no.isChecked()){
            answer.setText(data[counter].substring(0, data[counter].length()-11));
            answer.setVisibility(View.VISIBLE);
        }else{
            if(!data[counter].trim().isEmpty()){answer.setText(data[counter]);}
            answer.setVisibility(View.VISIBLE);
        }

        if(counter == 5 || counter == 6){
            answer.setImeOptions(EditorInfo.IME_FLAG_FORCE_ASCII);
            answer.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }else if(counter == 4){
            answer.setImeOptions(EditorInfo.IME_FLAG_FORCE_ASCII);
            answer.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

        }else{
            answer.setImeOptions(EditorInfo.TYPE_CLASS_TEXT);
            answer.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }

        next.setEnabled(true);
        Log.v("test", "inputType = "+answer.getInputType());
        Log.v("test", "counter = "+counter);
    }

    public void previous(View v){
        counter--;
        question_render();
    }

    public void TextHandler(){
        String tmp;
        tmp =data[0] + question.getText().toString();
        question.setText(tmp);
    }

    public void question_render(){
        String tmp="";
        answer.setHint("");
        viewControler();
        switch (counter){
            case 0:
                answer.setHint(R.string.nickname);
                question.setText(R.string.greeting);
                break;

            case 1:
                question.setText(R.string.get_gender);
                TextHandler();
                break;

            case 2:
                answer.setHint("要真名喔!");
                question.setText(R.string.get_name);
                TextHandler();
                break;

            case 3:
                question.setText(R.string.get_birthday);
                TextHandler();
                break;

            case 4:
                answer.setHint(R.string.acc);
                question.setText(R.string.get_mail);
                if(no.isChecked()){
                    next.setEnabled(false);
                }
                TextHandler();
                break;

            case 5:
                question.setText(R.string.get_pwd);
                TextHandler();
                break;

            case 6:
                question.setText(R.string.cnf_pwd);
                TextHandler();
                break;

            case 7:
                question.setText(R.string.get_address);
                TextHandler();
                break;

            case 8:
                question.setText(R.string.get_profile);
                TextHandler();
                break;

            case 9:
                question.setText(R.string.success);
                TextHandler();
                break;
        }
    }

    public void verifier(View v){
        String tmp="";
        switch (counter){
            case 0:
                tmp = answer.getText().toString();
                if(tmp.trim().isEmpty()){
                    Toast.makeText(NewRegister.this,R.string.nickname_err,Toast.LENGTH_SHORT).show();
                }else{nextQ(tmp);}
                break;

            case 1:
                if(m.isChecked()){tmp = "m";}
                else if(f.isChecked()){tmp = "f";}
                else{tmp = "";}
                nextQ(tmp);
                break;

            case 2:
                tmp = answer.getText().toString();
                if(tmp.trim().isEmpty()){
                    Toast.makeText(NewRegister.this,R.string.name_err,Toast.LENGTH_SHORT).show();
                }else{nextQ(tmp);}
                break;

            case 3:
                int y, m, d;
                y = dtp.getYear();
                m = dtp.getMonth();
                d = dtp.getDayOfMonth();
                tmp = y+"/"+m+"/"+d;
                nextQ(tmp);
                break;

            case 4:
                answer.setText(answer.getText().toString()+" ");
                if(answer.getText().toString().trim().isEmpty()){
                    next.setEnabled(false);
                    build.setVisibility(View.GONE);
                    Toast.makeText(NewRegister.this,R.string.acc_err,Toast.LENGTH_SHORT).show();
                }else{
                    if(no.isChecked()){tmp = answer.getText().toString().trim()+"@happy.coin"; nextQ(tmp);}
                    else if(yes.isChecked()){
                        tmp = answer.getText().toString().trim();
                        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(tmp).matches()){
                            Toast.makeText(NewRegister.this,R.string.mail_err,Toast.LENGTH_SHORT).show();
                        }else{nextQ(tmp);}
                    }
                }
                break;

            case 5:
                answer.setText(answer.getText().toString()+" ");
                tmp = answer.getText().toString().trim();
                if(tmp.trim().isEmpty()){
                    Toast.makeText(NewRegister.this,R.string.pwd_err,Toast.LENGTH_SHORT).show();
                }else{nextQ(tmp);}
                break;

            case 6:
                answer.setText(answer.getText().toString()+" ");
                tmp = answer.getText().toString().trim();
                if(tmp.trim().isEmpty() || !tmp.equals(data[counter-1])){
                    Toast.makeText(NewRegister.this,R.string.cnf_pwd_err,Toast.LENGTH_SHORT).show();
                }else{nextQ(tmp);}
                break;

            case 7:
                tmp = answer.getText().toString();
                nextQ(tmp);
                break;

            case 8:
                try{
                    tmp = imgdata.toString();
                }catch (Exception e){tmp="";}

                if(tmp.trim().isEmpty()){
                    Toast.makeText(NewRegister.this,R.string.profile_err,Toast.LENGTH_SHORT).show();
                }else{nextQ(tmp+ "," + degree);}
                break;

            case 9:
                ((BitmapDrawable)profile.getDrawable()).getBitmap().recycle();//一定要做否則會當機
                data[counter] = "y";
                Intent intent = new Intent(NewRegister.this, Register.class);
                startActivity(intent);
        }
    }

    public void nextQ(String q){
        data[counter] = q.trim();
        counter++;
        answer.setText("");
        question_render();
    }

    Float degree = 0f;
    public void rotate(){
        degree=(degree+90f)>=(360f)?0f:degree+90f;
        profile.setRotation(degree);
    }

}

//Hint and profile