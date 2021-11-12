package com.underdog.raver;

import static android.content.ContentValues.TAG;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;
import static java.lang.String.format;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.LogCallback;
import com.arthenica.mobileffmpeg.LogMessage;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;

import java.io.File;
import java.util.Arrays;
import java.util.TimerTask;

public class mixing extends AppCompatActivity {

    SeekBar change_vol, change_mix, play_vol, play_mix;
    Button btn_set_vol, btn_set_mix, btn_play_vol, btn_play_mix;
    Button btn_merge, btn_save;
    TextView num_vol, num_mix;
    String[] command;
    static private MediaPlayer mediaPlayer_mp3, mediaPlayer_mp4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mixing);
        getSupportActionBar().setTitle("믹싱");

        Toast.makeText(this, "동영상 자르기 완료", Toast.LENGTH_LONG).show();

        String videoPath = getIntent().getStringExtra("videoPath");
        String mp3Path = getIntent().getStringExtra("mp3Path");
        String changedmp3=mp3Path.replaceAll("_trim.mp3","_changeVol.mp3");


        Uri uri_video = Uri.parse(videoPath);
        Uri uri_mp3 = Uri.parse(changedmp3); //바꾸는거 mp3
        Uri uri_ori = Uri.parse(mp3Path);

        num_vol=findViewById(R.id.num_vol); //옆에 뜨는 숫자
        change_vol=findViewById(R.id.seek_bgm);
        btn_set_vol=findViewById(R.id.set_bgm);
        btn_play_vol=findViewById(R.id.play_bgm);

        mediaPlayer_mp3 = MediaPlayer.create(this,uri_ori);
        change_vol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                num_vol.setText(String.valueOf(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        btn_set_vol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int volvalue = Integer.parseInt(num_vol.getText().toString());

                //ffmpeg -y -i 1.mp4 -filter:a volume=2.0 -c:v copy -c:a aac -map 0 result.mp4
                File file = new File(changedmp3);
                if(file.exists()) {
                    file.delete();
                    command = new String[]{"-i",mp3Path,"-filter:a","volume="+volvalue/50,changedmp3};
                    //command = new String[]{"-y","-i",mp3Path,"-filter:a","volume="+volvalue/50,"-c:v","copy","-c:a","aac","-map","0",changedmp3};
                    execffmpegBinary(command);
                }else{
                    command = new String[]{"-i",mp3Path,"-filter:a","volume="+volvalue/50,changedmp3};
                    //command = new String[]{"-y","-i",mp3Path,"-filter:a","volume="+volvalue/50,"-c:v","copy","-c:a","aac","-map","0",changedmp3};
                    execffmpegBinary(command);
                }
                mediaPlayer_mp3 = MediaPlayer.create(getBaseContext(),uri_mp3);


            }

        });
        mediaPlayer_mp3.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
                mediaPlayer.pause();

                btn_play_vol.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(btn_play_vol.getText().toString().equals(">")) {
                            mediaPlayer_mp3.start();
                            btn_play_vol.setText("||");
                        }
                        else{
                            mediaPlayer_mp3.stop();
                            btn_play_vol.setText(">");
                        }
                    }
                });

            }
        });





    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
                builder.setMessage("정말 이전으로 가겠습니까?\n작업 내용이 사라질 수 있습니다.")
                        .setTitle("뒤로가기")
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
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
        }
        return super.onOptionsItemSelected(item);
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
                builder.setMessage("정말 이전으로 가겠습니까?\n작업 내용이 사라질 수 있습니다.")
                        .setTitle("뒤로가기")
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                androidx.appcompat.app.AlertDialog alert = builder.create();
                alert.show();
                break;
        }
        return false;
    }
    private void execffmpegBinary(final String[] command) {
        Config.enableLogCallback(new LogCallback() {
            @Override
            public void apply(LogMessage message) {
                Log.e(Config.TAG, message.getText());
            }
        });

        Config.enableLogCallback(new LogCallback() {
            @Override
            public void apply(LogMessage message) {
                Log.e(Config.TAG, message.getText());
            }
        });

        Config.enableStatisticsCallback(new StatisticsCallback() {
            @Override
            public void apply(Statistics newStatistics) {

            }
        });
        Log.d(TAG, "Started command : ffmpeg " + Arrays.toString(command));


        long executionId = FFmpeg.executeAsync(command, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {
                    //  progressDialog.dismiss();
                    Log.d(Config.TAG, "finished command: ffmpeg" + Arrays.toString(command));
                } else if (returnCode == RETURN_CODE_CANCEL) {
                    Log.e(Config.TAG, "Async command execution canceled by user");
                } else {
                    Log.e(Config.TAG, format("Async command execution failed with returncode = %d", returnCode));
                }
            }
        });
    }


}
