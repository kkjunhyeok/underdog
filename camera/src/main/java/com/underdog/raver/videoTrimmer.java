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

    Uri uri;
    Uri uri_mp3;
    ImageView imageView;
    VideoView videoView;
    TextView textViewLeft;
    TextView textViewRight;
    RangeSeekBar rangeSeekBar;
    Button btnSave;
    private String filePath;
    private String filePath_mp3;

    String imagePath;

    private static final String FILEPATH = "filepath";

    boolean isPlaying = false;

    int duration;
    String filePrefix;
    String[] command;
    String[] command_mp3;
    File dest;
    File dest_mp3;
    String original_path;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trim);
        imageView = findViewById(R.id.pause);
        videoView = findViewById(R.id.videoView);
        textViewLeft = findViewById(R.id.tvLeft);
        textViewRight = findViewById(R.id.tvRight);
        rangeSeekBar = findViewById(R.id.seekbar);
        btnSave = findViewById(R.id.btnSave);
        Intent i =getIntent();
        if (i != null) {
            imagePath = i.getStringExtra("videoPath");
            uri_mp3 = i.getParcelableExtra("uri");
            uri = Uri.parse(imagePath);
            isPlaying = true;
            videoView.setVideoURI(uri);
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
                input.setPrivateImeOptions("defaultInputmode=korean;");
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


                        finish();
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
        filePrefix = fileName;

        String fileExt = ".mp4";
        String fileExt_mp3 = ".mp3";
        System.out.println("audio"+fileExt);
        dest = new File(folder, filePrefix + fileExt);
        dest_mp3 = new File(folder, filePrefix+fileExt_mp3);
        original_path = getRealPathFromUri(getApplicationContext(), uri);

        duration = (endMs - startMs) / 1000;
        filePath = dest.getAbsolutePath();
        filePath_mp3 = dest_mp3.getAbsolutePath();
        Toast.makeText(this, filePath, Toast.LENGTH_SHORT).show();

        command = new String[]{"-ss", "" + startMs / 1000, "-y", "-i", imagePath, "-t", "" + (endMs - startMs) / 1000, "-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", filePath};
        execffmpegBinary(command);

        command_mp3 = new String[]{"-ss", "" + startMs / 1000, "-y", "-i", String.valueOf(uri_mp3), "-t", "" + (endMs - startMs) / 1000, filePath_mp3};
        execffmpegBinary(command_mp3);


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
                    Intent intent = new Intent(videoTrimmer.this, PreviewActivity.class);
                    intent.putExtra(FILEPATH, filePath);
                    startActivity(intent);

                } else if (returnCode == RETURN_CODE_CANCEL) {
                    Log.e(Config.TAG, "Async command execution canceled by user");
                } else {
                    Log.e(Config.TAG, format("Async command execution failed with returncode = %d", returnCode));
                }
            }
        });
    }


}