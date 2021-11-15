package com.underdog.raver;

import static android.content.ContentValues.TAG;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;
import static java.lang.String.format;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

    private SeekBar change_vol, change_mix, play_vol, play_mix;
    private Button btn_set_vol, btn_set_mix, btn_play_vol, btn_play_mix;
    Button btn_merge, btn_save;
    TextView num_vol, num_mix;
    String[] command_vol, command_mix;
    private MediaPlayer mediaPlayer_mp3, mediaPlayer_mp4;
    Uri uri_mp3, uri_mp4; //바꾸는거 mp3
    private Handler handler_vol, handler_mix;
    private Runnable runnable_vol, runnable_mix ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mixing);
        getSupportActionBar().setTitle("믹싱");
        handler_vol = new Handler();
        handler_mix = new Handler();

        Toast.makeText(this, "동영상 자르기 완료", Toast.LENGTH_LONG).show();

        String videoPath = getIntent().getStringExtra("videoPath");
        String mp3Path = getIntent().getStringExtra("mp3Path");
        String changedmp3=mp3Path.replaceAll("_trim.mp3","_changeVol.mp3");
        String changedmp4=mp3Path.replaceAll("_trim.mp3","_changeVol.mp4");


        Uri uri_video = Uri.parse(videoPath);
        Uri uri_ori = Uri.parse(mp3Path);
        uri_mp3 = Uri.parse(changedmp3);
        uri_mp4 = Uri.parse(changedmp4);

        num_vol=findViewById(R.id.num_vol); //옆에 뜨는 숫자
        change_vol=findViewById(R.id.seek_bgm);
        btn_set_vol=findViewById(R.id.set_bgm);
        btn_play_vol=findViewById(R.id.play_bgm);
        play_vol=findViewById(R.id.play_bgm_seek);

        num_mix=findViewById(R.id.num_mix);
        change_mix=findViewById(R.id.seek_mix);
        btn_set_mix=findViewById(R.id.set_mix);
        btn_play_mix=findViewById(R.id.play_mix);
        play_mix=findViewById(R.id.play_mix_seek);


        TextView time_vol = (TextView)findViewById(R.id.time_vol);
        TextView time_mix = (TextView)findViewById(R.id.time_mix);


        //볼륨조절쪽
        mediaPlayer_mp3 = MediaPlayer.create(this,uri_mp3);
        play_vol.setMax(mediaPlayer_mp3.getDuration());
        //볼륨값
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
        //설정버튼
        btn_set_vol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float volvalue = Float.parseFloat(num_vol.getText().toString());

                mediaPlayer_mp3.stop();
                command_vol = new String[]{"-y","-i",mp3Path,"-filter:a","volume="+volvalue/50,changedmp3};
                execffmpegBinary(command_vol);

                btn_play_vol.setText(">");
                time_vol.setText("00:00");

//

            }

        });
        //미디어플레이어
        mediaPlayer_mp3.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer_mp3) {
                mediaPlayer_mp3.start();
                mediaPlayer_mp3.pause();

                btn_play_vol.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if(btn_play_vol.getText().toString().equals(">")) {
                            mediaPlayer_mp3.start();
                            btn_play_vol.setText("||");
                            changeSeekbar_vol();
                        }
                        else{
                            mediaPlayer_mp3.pause();
                            btn_play_vol.setText(">");
                        }
                    }
                });

            }
        });
        //볼륨시작
        play_vol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    mediaPlayer_mp3.seekTo(progress);
                }
                int m = progress / 60000;
                int s = (progress % 60000) / 1000;
                String strTime = String.format("%02d:%02d", m, s);
                time_vol.setText(strTime);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        /*믹싱쪽*/
        mediaPlayer_mp4 = MediaPlayer.create(this,uri_mp4);
        play_mix.setMax(mediaPlayer_mp4.getDuration());
        change_mix.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                num_mix.setText(String.valueOf(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        btn_set_mix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float mixvalue = Float.parseFloat(num_mix.getText().toString());
                mediaPlayer_mp4.stop();
                command_mix = new String[]{"-y","-i",videoPath,"-filter:a","volume="+mixvalue/50,changedmp4};
                command_mix = new String[]{"-y","-i", videoPath, "-map","0","-c:v","copy","-af","aecho=0.6:0.5:1000:0.5", changedmp4};
                execffmpegBinary_mix(command_mix);

                btn_play_mix.setText(">");
                time_mix.setText("00:00");

            }

        });
        mediaPlayer_mp4.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer_mp3) {
                mediaPlayer_mp4.start();
                mediaPlayer_mp4.pause();

                btn_play_mix.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if(btn_play_mix.getText().toString().equals(">")) {
                            mediaPlayer_mp4.start();
                            btn_play_mix.setText("||");
                            changeSeekbar_mix();
                        }
                        else{
                            mediaPlayer_mp4.pause();
                            btn_play_mix.setText(">");
                        }
                    }
                });

            }
        });
        play_mix.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    mediaPlayer_mp4.seekTo(progress);
                }
                int m = progress / 60000;
                int s = (progress % 60000) / 1000;
                String strTime = String.format("%02d:%02d", m, s);
                time_mix.setText(strTime);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }


    public void changeSeekbar_vol(){
        play_vol.setProgress(mediaPlayer_mp3.getCurrentPosition());
        if(mediaPlayer_mp3.isPlaying()){
            runnable_vol = new Runnable() {
                @Override
                public void run() {
                    changeSeekbar_vol();
                }
            };
            handler_vol.postDelayed(runnable_vol,1000);
        }
    }
    public void changeSeekbar_mix(){
        play_mix.setProgress(mediaPlayer_mp4.getCurrentPosition());
        if(mediaPlayer_mp4.isPlaying()){
            runnable_mix = new Runnable() {
                @Override
                public void run() {
                    changeSeekbar_mix();
                }
            };
            handler_mix.postDelayed(runnable_mix,1000);
        }
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
                    //  progressDialog.dismiss();료
                    Toast.makeText(getApplicationContext(), "변경 완료", Toast.LENGTH_SHORT).show();
                    Log.d(Config.TAG, "finished command: ffmpeg" + Arrays.toString(command_vol));
                    mediaPlayer_mp3 = MediaPlayer.create(getApplicationContext(),uri_mp3);
                    mediaPlayer_mp3.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer_mp3) {
                            mediaPlayer_mp3.start();
                            mediaPlayer_mp3.pause();

                            btn_play_vol.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    if(btn_play_vol.getText().toString().equals(">")) {
                                        mediaPlayer_mp3.start();
                                        btn_play_vol.setText("||");
                                        changeSeekbar_vol();
                                    }
                                    else{
                                        mediaPlayer_mp3.pause();
                                        btn_play_vol.setText(">");
                                    }
                                }
                            });

                        }
                    });

                    mediaPlayer_mp3.seekTo(0);
                    play_vol.setProgress(0);
                } else if (returnCode == RETURN_CODE_CANCEL) {
                    Log.e(Config.TAG, "Async command execution canceled by user");
                } else {
                    Log.e(Config.TAG, format("Async command execution failed with returncode = %d", returnCode));
                }
            }
        });
    }
    private void execffmpegBinary_mix(final String[] command) {
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
                    //  progressDialog.dismiss();료
                    Toast.makeText(getApplicationContext(), "변경 완료", Toast.LENGTH_SHORT).show();
                    Log.d(Config.TAG, "finished command: ffmpeg" + Arrays.toString(command_mix));
                    mediaPlayer_mp4 = MediaPlayer.create(getApplicationContext(),uri_mp4);
                    mediaPlayer_mp4.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer_mp4) {
                            mediaPlayer_mp4.start();
                            mediaPlayer_mp4.pause();

                            btn_play_mix.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    if(btn_play_mix.getText().toString().equals(">")) {
                                        mediaPlayer_mp4.start();
                                        btn_play_mix.setText("||");
                                        changeSeekbar_mix();
                                    }
                                    else{
                                        mediaPlayer_mp4.pause();
                                        btn_play_mix.setText(">");
                                    }
                                }
                            });

                        }
                    });

                    mediaPlayer_mp4.seekTo(0);
                    play_mix.setProgress(0);
                } else if (returnCode == RETURN_CODE_CANCEL) {
                    Log.e(Config.TAG, "Async command execution canceled by user");
                } else {
                    Log.e(Config.TAG, format("Async command execution failed with returncode = %d", returnCode));
                }
            }
        });
    }


}
