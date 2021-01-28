package com.daasuu.gpuvideoandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.daasuu.gpuvideoandroid.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class AudioPlayer extends AppCompatActivity implements View.OnClickListener {

    private Button btnPlay,btnFor,btnBack,buttonSelect;
    private SeekBar seekBar;
    static private MediaPlayer mediaPlayer;
    private Runnable runnable;
    private Handler handler;

    private TextView countDownTextView;

    private CountDownTimer countDownTimer;


    Timer timer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);


        countDownTextView = (TextView) findViewById(R.id.countDownTimerTextView);


        int minutes = 6;
        int milliseconds = minutes * 1 * 1000;

        countDownTimer = new CountDownTimer(milliseconds, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

                countDownTextView.setText(String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));

            }

            @Override
            public void onFinish() {
                countDownTextView.setText("Time Up!");
            }
        };


        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Bundle bundle = getIntent().getExtras();

        ArrayList<File> songs = (ArrayList) bundle.getParcelableArrayList("list");
        int position = bundle.getInt("position");
        Uri uri = Uri.parse(songs.get(position).toString());


        btnPlay = findViewById(R.id.btnPlay);
        btnFor = findViewById(R.id.btnFor);
        btnBack = findViewById(R.id.btnBack);
        handler = new Handler();
        seekBar = findViewById(R.id.seekbar);
        buttonSelect = findViewById(R.id.btn_select);

        btnPlay.setOnClickListener(this);
        btnFor.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        mediaPlayer = MediaPlayer.create(this,uri);

        timer = new Timer();


        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                seekBar.setMax(mediaPlayer.getDuration());
                mediaPlayer.start();
                mediaPlayer.pause();
                btnPlay.setText(">");


                buttonSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(AudioPlayer.this,PortraitCameraActivity.class);
                    startActivity(intent);
                    timer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        mediaPlayer.start();

                            }
                        },5000);
                    }
                });


            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b){
                    mediaPlayer.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }
    public void buttonAction(View view) {

        if(view.getId() == R.id.startButton)
            countDownTimer.start();
        else if(view.getId() == R.id.cancelButton)
            countDownTimer.cancel();

    }

    private void changeSeekbar(){
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
        if(mediaPlayer.isPlaying()){
            runnable = new Runnable() {
                @Override
                public void run() {
                    changeSeekbar();
                }
            };
            handler.postDelayed(runnable,1000);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnPlay:
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    btnPlay.setText(">");

                }else{
                    mediaPlayer.start();
                    btnPlay.setText("||");
                    changeSeekbar();
                }
                break;
            case R.id.btnFor:
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+5000);
                break;
            case R.id.btnBack:
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-5000);
                break;
        }
    }

}