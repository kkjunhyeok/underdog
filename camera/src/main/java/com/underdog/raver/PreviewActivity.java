package com.underdog.raver;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class PreviewActivity extends AppCompatActivity {

    private VideoView videoView;
    Button btn2home, btnshare;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("최종 영상");
        getSupportActionBar().hide();

        Toast.makeText(this, "저장 완료", Toast.LENGTH_LONG).show();

        setContentView(R.layout.activity_preview);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        videoView = (VideoView) findViewById(R.id.videoView);
        btn2home = (Button) findViewById(R.id.tohome);
        btnshare = (Button) findViewById(R.id.btnshare);

        String filePath = getIntent().getStringExtra("final_path");

        videoView.setVideoURI(Uri.parse(filePath));
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);


        btnshare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                Uri screenshotUri = Uri.parse(filePath);

                sharingIntent.setType("video/mp4");
                sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
                startActivity(Intent.createChooser(sharingIntent, "영상을 공유해보세요.")); //
            }
        });

        //videoView.start();
        btn2home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(PreviewActivity.this);
                builder.setMessage("홈 화면으로 이동")
                        .setTitle("이동하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(PreviewActivity.this,MainActivity.class);
                                startActivity(intent);
                                overridePendingTransition(0, 0);
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                androidx.appcompat.app.AlertDialog alert = builder.create();
                alert.show();
            }
        });



    }

    @Override public void onBackPressed() {
        //super.onBackPressed();
        }

}

