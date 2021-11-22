package com.underdog.raver;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;


public class Tiny_DB_Mixing extends AppCompatActivity {

    private ArrayList<File> documentFile;
    private File file;
    private String ExternalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private String mp4_path, mp3_path, mp4_trim_path, mp3_trim_path;
    Uri uri_mp4_path, uri_mp4_trim_path, uri_mp3_path, uri_mp3_trim_path;

    @Override
    protected void onStart() {
        super.onStart();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("미완성곡 편집을 시작합니다. 편집할 부분을 선택해주세요.")
                .setTitle("편집 모드")
                .setCancelable(false)
                .setNeutralButton("동영상 자르기", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent mixIntent = new Intent(Tiny_DB_Mixing.this,videoTrimmer.class);
                        mixIntent.putExtra("uri_mp3", uri_mp3_path).putExtra("videoPath", mp4_path);
                        startActivity(mixIntent);
                    }
                })
                .setPositiveButton("믹싱", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent mixIntent = new Intent(Tiny_DB_Mixing.this, mixing.class);
                        mixIntent.putExtra("mp3Path", mp3_trim_path).putExtra("videoPath", mp4_trim_path);
                        startActivity(mixIntent);
                    }
                });
        // onclick 속에 intent 바꿔주기
        AlertDialog alert = builder.create();
        alert.show();

        alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
            int count = 0;
            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if(count > 1) {
                        Intent intent = new Intent(Tiny_DB_Mixing.this, Tiny_DB.class);
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"한번 더 누르면 뒤로 돌아갑니다.",Toast.LENGTH_SHORT).show();
                        count++;
                    }
                }
                return true;
            }
        });

    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);
        getSupportActionBar().setTitle("이전 작업 믹싱");


        String txt_path = getIntent().getStringExtra("name");

        documentFile = new ArrayList<>();
        file = new File(ExternalPath + "/RAVER");
        File[] files = file.listFiles();


        for(int i = 0; i < files.length; i++){
            if(files[i].isFile()){
                if(files[i].getName().endsWith(txt_path))
                {
                    documentFile.add(files[i]);
                }
            }
        }
        Uri uri_txt = Uri.parse(txt_path);      // txt 파일 가져올 경로
        String filename = new File(txt_path).getName();     // txt 파일 이름 가져오기

        String line = null;
        String content = null;

        // 파일 내용 불러오기
        File saveFile = new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/RAVER");

        ArrayList arraylist = new ArrayList();

        try {
            BufferedReader buf = new BufferedReader(new FileReader(saveFile + "/" + filename));

            while(true) {
                String str = buf.readLine();
                if(str != null) {
                    arraylist.add(str);
                } else {
                    break;
                }
            }
            buf.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        mp3_path = arraylist.get(0).toString();
        uri_mp3_path = Uri.parse(mp3_path);

        mp4_path = arraylist.get(1).toString();
        uri_mp4_path = Uri.parse(mp4_path);

        mp3_trim_path = arraylist.get(2).toString();
        uri_mp3_trim_path = Uri.parse(mp3_trim_path);

        mp4_trim_path = arraylist.get(3).toString();
        uri_mp4_trim_path = Uri.parse(mp4_trim_path);


    }
}