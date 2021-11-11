package com.underdog.raver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.LogCallback;
import com.arthenica.mobileffmpeg.LogMessage;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.io.File;
import java.util.Arrays;

import static android.content.ContentValues.TAG;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;
import static java.lang.String.format;

public class videoTrimmer extends AppCompatActivity {

    Uri uri_video;
    Uri uri_mp3;
    ImageView imageView;
    VideoView videoView;
    TextView textViewLeft;
    TextView textViewRight;
    RangeSeekBar rangeSeekBar;
    Button btnSave;
    private String filePath;
    private String filePath_mp3;
    private String filePath_merge;

    String ori_Path;

    private static final String FILEPATH = "filepath";

    boolean isPlaying = false;

    int duration;
    String filePrefix;
    String filePrefix_merge;
    String[] command;
    String[] command_mp3;
    String[] command_merge;
    File dest;
    File dest_mp3;
    File dest_merge;
    String original_path;

    final LoadingDialog loadingDialog = new LoadingDialog(videoTrimmer.this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trim);
        getSupportActionBar().setTitle("영상 자르기");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        imageView = findViewById(R.id.pause);
        videoView = findViewById(R.id.videoView);
        textViewLeft = findViewById(R.id.tvLeft);
        textViewRight = findViewById(R.id.tvRight);
        rangeSeekBar = findViewById(R.id.seekbar);
        btnSave = findViewById(R.id.btnSave);


        Intent i =getIntent();
        if (i != null) {
            ori_Path = i.getStringExtra("videoPath");
            uri_mp3 = i.getParcelableExtra("uri_mp3");
            uri_video = Uri.parse(ori_Path);
            isPlaying = true;
            videoView.setVideoURI(uri_video);
            videoView.start();
        }
        setListeners();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(videoTrimmer.this);

                LinearLayout linearLayout = new LinearLayout(videoTrimmer.this);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(50, 0, 50, 100);
                final EditText input = new EditText(videoTrimmer.this);
                input.setLayoutParams(lp);
                input.setGravity(Gravity.TOP | Gravity.START);
//                input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                input.setPrivateImeOptions("defaultInputmode=english;");
                linearLayout.addView(input, lp);
                alert.setTitle("저장");
                alert.setMessage("영상 제목을 정하시겠습니까?");
                alert.setView(linearLayout);
                alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        filePrefix = input.getText().toString();

                        trimVideo(rangeSeekBar.getSelectedMinValue().intValue() * 1000,
                                rangeSeekBar.getSelectedMaxValue().intValue() * 1000, filePrefix);

                        loadingDialog.startLoadingDialog();
//                        finish();
                        dialogInterface.dismiss();

                    }
                });
                alert.show();
            }
        });
    }

    private void setListeners() {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    imageView.setImageResource(R.drawable.ic_play);
                    videoView.pause();
                    isPlaying = false;
                } else {
                    videoView.start();
                    imageView.setImageResource(R.drawable.ic_pause);
                    isPlaying = true;
                }
            }
        });

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                videoView.start();
                duration = mediaPlayer.getDuration() / 1000;

                textViewLeft.setText("00:00:00");

                textViewRight.setText(getTime(mediaPlayer.getDuration() / 1000));
                mediaPlayer.setLooping(true);
                rangeSeekBar.setRangeValues(0, duration);
                rangeSeekBar.setSelectedMaxValue(duration);
                rangeSeekBar.setSelectedMinValue(0);
                rangeSeekBar.setEnabled(true);


                rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
                    @Override
                    public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                        videoView.seekTo((int) minValue * 1000);

                        textViewLeft.setText(getTime((int) bar.getSelectedMinValue()));
                        textViewRight.setText(getTime((int) bar.getSelectedMaxValue()));
                    }
                });
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (videoView.getCurrentPosition() >= rangeSeekBar.getSelectedMaxValue().intValue() * 1000) {
                            videoView.seekTo(rangeSeekBar.getSelectedMinValue().intValue() * 1000);
                        }

                    }
                }, 1000);
            }
        });
    }


    private String getTime(int seconds) {
        int hr = seconds / 3600;
        int rem = seconds % 3600;
        int mn = rem / 60;
        int sec = rem % 60;
        return format("%02d", hr) + ":" + format("%02d", mn) + ":" + format("%02d", sec);

    }




    private void trimVideo(int startMs, int endMs, String fileName) {
        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/RAVER/");

        if (!folder.exists()) {
            folder.mkdir();
        }
        filePrefix = fileName + "_trim";
        filePrefix_merge = filePrefix + "_merge";
        String fileExt = ".mp4";
        String fileExt_mp3 = ".mp3";
        System.out.println("audio"+fileExt);
        dest = new File(folder, filePrefix + fileExt);
        dest_mp3 = new File(folder, filePrefix+fileExt_mp3);
        dest_merge = new File(folder, filePrefix_merge + fileExt);

        duration = (endMs - startMs) / 1000;
        filePath = dest.getAbsolutePath();
        filePath_mp3 = dest_mp3.getAbsolutePath();
        filePath_merge = dest_merge.getAbsolutePath();

        //Toast.makeText(this, filePath, Toast.LENGTH_SHORT).show();

        command = new String[]{"-ss", "" + startMs / 1000, "-y", "-i", ori_Path, "-t", "" + (endMs - startMs) / 1000, "-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", filePath};

        execffmpegBinary_mp4(command);

        command_mp3 = new String[]{"-ss", "" + startMs / 1000, "-y", "-i", String.valueOf(uri_mp3), "-t", "" + (endMs - startMs) / 1000,"-ac","1", filePath_mp3};
       // command_mp3 = new String[]{"-i", String.valueOf(uri_mp3), "-map","0","-c:v","copy","-af","aecho=0.6:0.5:1000:0.5", filePath_mp3};

        execffmpegBinary_mp3(command_mp3);


    }

    private String getRealPathFromUri(Context context, Uri contentUri) {

        Cursor cursor = null;
        try {

            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    private void execffmpegBinary_mp3(final String[] command) {
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
    private void execffmpegBinary_mp4(final String[] command) {
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

                    /*영상 합치기 나중에 */
//                    command_merge = new String[]{"-i", "" + filePath, "-i", "" + filePath_mp3,"-filter_complex","[0:a][1:a]amerge=inputs=2[a]","-map","0:v","-map","[a]","-c:v","copy","-ac","2","-shortest",filePath_merge};
////                    ffmpeg -i video.mkv -i audio.m4a -filter_complex "[0:a][1:a]amerge=inputs=2[a]"-map 0:v -map "[a]" -c:v copy -ac 2 -shortest output.mkv
//
//                    Log.d(TAG, "Started command : ffmpeg " + Arrays.toString(command_merge));
//
//                    int rc = FFmpeg.execute(command_merge);
//
//                    if (rc == RETURN_CODE_SUCCESS) {
//                        Log.i(Config.TAG, "Command execution completed successfully.");
//                        loadingDialog.dismissDialog();
//                        Intent intent = new Intent(videoTrimmer.this, PreviewActivity.class);
//                        intent.putExtra(FILEPATH, filePath_merge);
//                        startActivity(intent);
//
//                    } else if (rc == RETURN_CODE_CANCEL) {
//                        Log.i(Config.TAG, "Command execution cancelled by user.");
//                    } else {
//                        Log.i(Config.TAG, String.format("Command execution failed with rc=%d and the output below.", rc));
//                        Config.printLastCommandOutput(Log.INFO);
//                    }


                } else if (returnCode == RETURN_CODE_CANCEL) {
                    Log.e(Config.TAG, "Async command execution canceled by user");
                } else {
                    Log.e(Config.TAG, format("Async command execution failed with returncode = %d", returnCode));
                }
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
}