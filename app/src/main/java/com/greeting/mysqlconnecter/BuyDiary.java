package com.greeting.mysqlconnecter;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import static com.greeting.mysqlconnecter.Login.pass;
import static com.greeting.mysqlconnecter.Login.url;
import static com.greeting.mysqlconnecter.Login.user;

/**
 * A simple {@link Fragment} subclass.
 */
public class BuyDiary extends Fragment {

    private ArrayList<String> pname  = new ArrayList<>();
    private ArrayList<String> pprice  = new ArrayList<>();
    private ArrayList<String> pamount = new ArrayList<>();
    private ArrayList<String> total = new ArrayList<>();
    private ArrayList<String> selldate = new ArrayList<>();

    String acc;
    TextView dt;
    TableLayout tradeData;

    public BuyDiary() {
        // Required empty public constructor
    }

    public static BuyDiary newInstance(){
        return new BuyDiary();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //TextView textView = new TextView(getActivity());
        //textView.setText(R.string.hello_blank_fragment);
        //return textView;
        View view = inflater.inflate(R.layout.fragment_new_diary,container, false);
        tradeData = view.findViewById(R.id.tradeData);
        pname.add("品名　　");
        pprice.add("單價　　");
        pamount.add("數量　　");
        total.add("總額　　");
        selldate.add("交易日期  &  時間");
        ConnectMySql connectMySql = new ConnectMySql();
        connectMySql.execute("");
        return view;
    }

    public void onBackPressed(){
        Intent intent = new Intent(getActivity(), MainMenu.class);
        startActivity(intent);
    }

    //建立連接與查詢非同步作業
    private class ConnectMySql extends AsyncTask<String, Void, String> {
        String res="";//錯誤信息儲存變數
        //開始執行動作
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
//            Toast.makeText(getActivity(),"請稍後...",Toast.LENGTH_SHORT).show();
        }
        //查詢執行動作(不可使用與UI相關的指令)
        @Override
        protected String doInBackground(String... strings) {
            try {
                //連接資料庫
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(url, user, pass);
                //建立查詢
                //String result = "對方帳戶\t交易\t金額\t餘額\n";
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("select productName, price, amount, sellDate from sellhistory where client ='"+acc+"'");
                //將查詢結果裝入陣列
                while(rs.next()){
                    //result += rs.getString("paccount")+"\t"+rs.getString("state")+"\t$"+rs.getString("amount")+"\t$"+rs.getString("moneyLeft")+"\n";
                    pname.add(rs.getString("productName")+"  ");
                    pprice.add("$"+rs.getString("price"));
                    pamount.add(rs.getString("amount")+"  ");
                    total.add("$"+Integer.parseInt(rs.getString("amount"))*Integer.parseInt(rs.getString("price")));
                    selldate.add(rs.getString("sellDate").substring(0,16));
                }
                return "0";
            }catch (Exception e){
                e.printStackTrace();
                res = e.toString();
            }
            return res;
        }
        //查詢後的結果將回傳於此
        @Override
        protected void onPostExecute(String result) {
            //dt.setText(result);
            renderTable();
        }

        private void renderTable(){
            for(int row = 0 ; row < pname.size() ; row++ ){
//                Toast.makeText(Diary.this,"第"+row+"列建構中",Toast.LENGTH_SHORT).show();
                //新增一列
                TableRow tr = new TableRow(getActivity());
                //新增一個TextView
                TextView t1 = new TextView(getActivity());
                TextView t2 = new TextView(getActivity());
                TextView t3 = new TextView(getActivity());
                TextView t4 = new TextView(getActivity());
                TextView t5 = new TextView(getActivity());
                //設定TextView的文字
                t1.setText(pname.get(row));
                t2.setText(pprice.get(row));
//                Log.v("test",trade.get(row));
                t3.setText(pamount.get(row));
                t4.setText(total.get(row));
                t5.setText(selldate.get(row));
                //將TextView放入列
                tr.addView(t1);
                tr.addView(t2);
                tr.addView(t3);
                tr.addView(t4);
                tr.addView(t5);
                //將整列加入預先建立的TableLayout中
                tradeData.addView(tr,new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            }

        }
    }

}
