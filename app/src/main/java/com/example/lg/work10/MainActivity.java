package com.example.lg.work10;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {
    LinearLayout linear1,linear2;
    Button btnsave;
    EditText content;
    TextView tvcount;
    ListView listView;
    DatePicker datePicker;
    ArrayList<String> memolist;
    ArrayAdapter<String> adapter;
    boolean editmode = false;
    String editname = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }
    void init(){
        setTitle("메모장");
        linear1 = (LinearLayout)findViewById(R.id.linear1);
        linear2 = (LinearLayout)findViewById(R.id.linear2);
        btnsave = (Button)findViewById(R.id.btnsave);
        content = (EditText)findViewById(R.id.content);
        tvcount = (TextView)findViewById(R.id.tvCount);
        listView = (ListView)findViewById(R.id.listview);
        datePicker = (DatePicker)findViewById(R.id.datepicker);
        memolist = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,memolist);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                linear1.setVisibility(View.INVISIBLE);
                linear2.setVisibility(View.VISIBLE);
                editname = memolist.get(position);
                editmode = true;
                read(memolist.get(position));
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                dlg.setTitle("삭제확인")
                        .setMessage("삭제하시겠습니까?")
                        .setNegativeButton("취소",null)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                delete(memolist.get(position));
                                memolist.remove(position);
                                adapter.notifyDataSetChanged();
                                memocount();
                            }
                        });
                dlg.show();
                return true;
            }
        });
        checkpermission();
        makedir();
        getlist();
    }
    public void onClick(View v){
        if(v.getId() == R.id.btn1){
            cleardate();
            linear1.setVisibility(View.INVISIBLE);
            linear2.setVisibility(View.VISIBLE);
        }
        else if(v.getId() == R.id.btnsave){
            int year = datePicker.getYear();
            int month = datePicker.getMonth()+1;
            int day = datePicker.getDayOfMonth();
            String mName = setdateformat(year,month,day);
            Log.d("tag",mName);
            boolean flag = false;
            if(!editmode)
                for(String memo : memolist){
                    if(memo.equals(mName)){
                        flag = true;
                        break;
                    }
                }
            if(flag && !editmode){
                editmode = true;
                editname = mName;
                read(mName);
            }
            else if(editmode){
                delete(editname);
                for(int i=0;i<memolist.size();i++){
                    if(memolist.get(i).equals(editname)){
                        memolist.remove(i);
                        break;
                    }
                }
                for(int i=0;i<memolist.size();i++){
                    if(memolist.get(i).equals(mName)){
                        memolist.remove(i);
                        break;
                    }
                }
                write(year,month,day);
                editmode = false;
                editname = null;
                content.setText("");
                cleardate();
                memocount();
                linear2.setVisibility(View.INVISIBLE);
                linear1.setVisibility(View.VISIBLE);
            }
            else {
                write(year,month,day);
                linear2.setVisibility(View.INVISIBLE);
                linear1.setVisibility(View.VISIBLE);
                content.setText("");
                cleardate();
                memocount();
            }
        }
        else if(v.getId() == R.id.btncancel){
            editmode = false;
            editname = null;
            btnsave.setText("저장");
            content.setText("");
            cleardate();
            linear2.setVisibility(View.INVISIBLE);
            linear1.setVisibility(View.VISIBLE);
        }
    }
    void write(int year, int month, int day){
        String name = setdateformat(year,month,day);
        try {
            String path =getExternalPath();
            BufferedWriter bw = new BufferedWriter(new FileWriter(path +"diary/"
                    +name, false));
            bw.write(Integer.toString(year)+monthformat(month)+dayformat(day)+content.getText().toString());
            bw.close();
            Toast.makeText(this, "저장완료", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage() + ":" + getFilesDir(),
                    Toast.LENGTH_SHORT).show();
        }
        memolist.add(name);
        sort();
        btnsave.setText("저장");
    }
    void read(String name){
        btnsave.setText("수정");
        try {
            String path =getExternalPath();
            BufferedReader br = new BufferedReader(new
                    FileReader(path + "diary/" +name));
            String readStr = "";
            String str = null;
            while ((str = br.readLine()) != null) readStr += str + "\n";
            br.close();
            Log.d("read",readStr);
            String date = readStr.substring(0, 8);
            datePicker.updateDate(Integer.parseInt(date.substring(0,4)),
                    Integer.parseInt(date.substring(4,6))-1,Integer.parseInt(date.substring(6,8)));
            content.setText(readStr.substring(8, readStr.length() - 1));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "File not found",
                    Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void getlist(){
        memolist.clear();
        String path =getExternalPath();
        File[] files =
                new File(path + "diary").listFiles();
        for(File f:files) {
            memolist.add(f.getName());
        }
        sort();
        memocount();
    }

    void delete(String name){
        String path = getExternalPath();
        File file = new File(path + "diary/"+name);
        file.delete();
    }
    void makedir(){
        String path = getExternalPath();
        File file = new File(path + "diary");
        if(file.isDirectory() == false){
            file.mkdir();
        }
    }
    void checkpermission(){
        int permissioninfo = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissioninfo == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(getApplicationContext(), "SDCard 쓰기 권한 있음",Toast.LENGTH_SHORT).show();
        }
        else {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                Toast.makeText(getApplicationContext(),
                        "권한 거부시 앱 설정 필요",Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        100);
            }
            else{
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        100);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        String str = null;
        if(requestCode == 100){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                str = "권한 승인 완료";
            else str = "권한 승인 거부";
            Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
        }
    }
    public String getExternalPath(){
        String sdPath = "";
        String ext = Environment.getExternalStorageState();
        if(ext.equals(Environment.MEDIA_MOUNTED)) {
            sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        }else
            sdPath = getFilesDir() + "";
        return sdPath;
    }
    String monthformat(int month){
        String fmonth = Integer.toString(month);
        if(month < 10){
            fmonth = "0" + fmonth;
        }
        return fmonth;
    }
    String dayformat(int day){
        String fday = Integer.toString(day);
        if(day < 10){
            fday = "0" + fday;
        }
        return fday;
    }
    String setdateformat(int year,int month, int day){
        year = year % 100;
        String fyear = Integer.toString(year);
        String fmonth = Integer.toString(month);
        String fday = Integer.toString(day);

        if(year < 10){
            fyear = "0" + fyear;
        }

        if(month < 10){
            fmonth = "0" + fmonth;
        }
        if(day < 10){
            fday = "0" + fday;
        }
        return fyear+"-"+fmonth+"-"+fday+".memo";
    }
    void cleardate(){
        Calendar cal = Calendar.getInstance();
        int year=cal.get(Calendar.YEAR);
        int month=cal.get(Calendar.MONTH);
        int day=cal.get(Calendar.DAY_OF_MONTH);
        datePicker.updateDate(year, month, day);
    }
    void sort(){
        Collections.sort(memolist, dateasc);
        adapter.notifyDataSetChanged();
    }
    Comparator<String> dateasc = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    };
    void memocount(){
        tvcount.setText("등록된 메모 개수: "+memolist.size());
    }
}
