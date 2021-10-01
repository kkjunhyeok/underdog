package com.underdog.raver;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLException;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener;


import com.underdog.gpuv.camerarecorder.CameraRecordListener;
import com.underdog.gpuv.camerarecorder.GPUCameraRecorderBuilder;
import com.underdog.gpuv.camerarecorder.LensFacing;
import com.underdog.raver.widget.SampleCameraGLView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class BaseCameraActivity extends AppCompatActivity implements View.OnClickListener,OnMenuItemClickListener{
/*
    /**---------------------------------------------------*/


    private Button btnPlay;
    private SeekBar seekBar;
    static private MediaPlayer mediaPlayer;
    private Runnable runnable;
    private Handler handler;
    private Button buttonSelect;

    private TextView countDownTextView;

    private CountDownTimer countDownTimer;


    Timer timer;

    //--------------------------------------------------------------------

    private int seconds = 0;
    //Is the stopwatch running?
    private boolean running;
    private boolean wasRunning;

    private SampleCameraGLView sampleGLView;
    protected com.underdog.gpuv.camerarecorder.GPUCameraRecorder GPUCameraRecorder;
    private String filepath;
    private ImageView recordBtn,pause;
    protected LensFacing lensFacing = LensFacing.BACK;
    protected int cameraWidth = 1280;
    protected int cameraHeight = 720;
    protected int videoWidth = 720;
    public TextView timeView;
    protected int videoHeight = 720;
    private ImageView btnSwitchCamera;
    private Button addMusic;
    private Button filter;
    private Button cancelList;

    private ImageView flashOn;
    private ImageView btnFlash;
    private Button composeVideo;
    private ImageView settingMoviePreview;

    private boolean toggleClick = false;

    private ListView lv;
    private ImageView btnRatio;

    private String sound_url = null;

    protected void onCreateActivity () {
        getSupportActionBar().hide();
        recordBtn = findViewById(R.id.btn_record);
        pause = findViewById(R.id.btn_record_pause);

        settingMoviePreview = findViewById(R.id.btn_settings);
        composeVideo = findViewById(R.id.btn_compose);
        timeView = (TextView)findViewById(R.id.timeView);
        filter = findViewById(R.id.btn_filter);
        cancelList = findViewById(R.id.cancel_list);
        flashOn = findViewById(R.id.btn_flash_on);
        btnFlash = findViewById(R.id.btn_flash);
        btnRatio = findViewById(R.id.btn_ratio);


        //------------------------------------------------------------------------------------------------------

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
                countDownTextView.setText("Start!");
                recordBtn.performClick();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        countDownTextView.setVisibility(View.INVISIBLE);
                    }
                }, 1500);
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

        handler = new Handler();
        seekBar = findViewById(R.id.seekbar);

        btnPlay.setOnClickListener(this);
        buttonSelect = findViewById(R.id.btnSelect);

        mediaPlayer = MediaPlayer.create(this,uri);
        timer = new Timer();


        // 5초 뒤 녹음
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
                        //onBackPressed();
                        btnPlay.setVisibility(View.INVISIBLE);
                        //recordBtn.performClick();
                        seekBar.setVisibility(View.INVISIBLE);
                        countDownTimer.start();

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                mediaPlayer.start();
                            }
                        },6000);

                        mediaPlayer.pause();
                    }
                });


            }
        });

        // seekbar 변
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
        //-------------------------------------------------------------------------------------------------------------------------


        btnRatio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(BaseCameraActivity.this,view);
                popup.setOnMenuItemClickListener(BaseCameraActivity.this);
                popup.inflate(R.menu.popup_menu);
                
                popup.show();

            }
        });

        settingMoviePreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BaseCameraActivity.this,PlayerActivity.class);
                startActivity(intent);
            }
        });

        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filter.setVisibility(View.INVISIBLE);
                lv.setVisibility(View.VISIBLE);
                cancelList.setVisibility(View.VISIBLE);

            }
        });

        composeVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BaseCameraActivity.this,Mp4ComposeActivity.class);
                startActivity(intent);
            }
        });

        cancelList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelList.setVisibility(View.INVISIBLE);
                lv.setVisibility(View.INVISIBLE);
                filter.setVisibility(View.VISIBLE);
            }
        });



        runTimer();

        if(sound_url!=null){
            Toast.makeText(this, sound_url, Toast.LENGTH_SHORT).show();
        }

        btnSwitchCamera = findViewById(R.id.btn_switch_camera);
        btnSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseCameraActivity.this.releaseCamera();
                if (lensFacing == LensFacing.BACK) {
                    lensFacing = LensFacing.FRONT;
                } else {
                    lensFacing = LensFacing.BACK;
                }
                toggleClick = true;
            }
        });

        addMusic = findViewById(R.id.btn_add_music);
        addMusic.setOnClickListener(v -> {

            Intent intent = new Intent(BaseCameraActivity.this,MusicChooser.class);
            startActivity(intent);

            /**captureBitmap(bitmap -> {
             new Handler().post(() -> {
             String imagePath = getImageFilePath();
             saveAsPngImage(bitmap, imagePath);
             exportPngToGallery(getApplicationContext(), imagePath);
             });
             });*/
        });

        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                running = true;
                filepath = getVideoFilePath();
                GPUCameraRecorder.start(filepath);
                Toast.makeText(BaseCameraActivity.this, "Recording Started", Toast.LENGTH_SHORT).show();
                recordBtn.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);
                timeView.setVisibility(View.VISIBLE);
                addMusic.setVisibility(View.INVISIBLE);
                btnSwitchCamera.setVisibility(View.INVISIBLE);
                filter.setVisibility(View.INVISIBLE);
                settingMoviePreview.setVisibility(View.INVISIBLE);
                composeVideo.setVisibility(View.INVISIBLE);
                buttonSelect.setVisibility(View.INVISIBLE);
                seekBar.setVisibility(View.INVISIBLE);
                btnPlay.setVisibility(View.INVISIBLE);
                cancelList.setVisibility(View.INVISIBLE);
                //btnRatio.setVisibility(View.INVISIBLE);

            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                running = false;
                GPUCameraRecorder.stop();
                Toast.makeText(BaseCameraActivity.this, "Recording Stopped", Toast.LENGTH_SHORT).show();
                recordBtn.setVisibility(View.VISIBLE);
                pause.setVisibility(View.INVISIBLE);
                lv.setVisibility(View.VISIBLE);
                seconds=0;
                timeView.setVisibility(View.INVISIBLE);
                addMusic.setVisibility(View.VISIBLE);
                btnSwitchCamera.setVisibility(View.VISIBLE);
                filter.setVisibility(View.VISIBLE);
                settingMoviePreview.setVisibility(View.VISIBLE);
                composeVideo.setVisibility(View.VISIBLE);
                btnSwitchCamera.setVisibility(View.VISIBLE);
                lv.setVisibility(View.INVISIBLE);
                seekBar.setVisibility(View.INVISIBLE);
                btnPlay.setVisibility(View.INVISIBLE);
                //btnRatio.setVisibility(View.VISIBLE);

            }
        });
        findViewById(R.id.btn_flash).setOnClickListener(v -> {
            if (GPUCameraRecorder != null && GPUCameraRecorder.isFlashSupport()) {
                GPUCameraRecorder.switchFlashMode();
                GPUCameraRecorder.changeAutoFocus();
            }
            btnFlash.setVisibility(View.INVISIBLE);
            flashOn.setVisibility(View.VISIBLE);
        });
        flashOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GPUCameraRecorder != null && GPUCameraRecorder.isFlashSupport()) {
                    GPUCameraRecorder.switchFlashMode();
                    GPUCameraRecorder.changeAutoFocus();
                }
                flashOn.setVisibility(View.INVISIBLE);
                btnFlash.setVisibility(View.VISIBLE);
                GPUCameraRecorder.changeAutoFocus();
            }
        });



        lv = findViewById(R.id.filter_list);

        final List<FilterType> filterTypes = FilterType.createFilterList();
        lv.setAdapter(new FilterAdapter(this, R.layout.row_white_text, filterTypes).whiteMode());
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (GPUCameraRecorder != null) {
                    GPUCameraRecorder.setFilter(FilterType.createGlFilter(filterTypes.get(position), getApplicationContext()));
                }
            }
        });

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Toast.makeText(this, "Selected Item: " +item.getTitle(), Toast.LENGTH_SHORT).show();
        switch (item.getItemId()) {
            case R.id.full:
                Intent intent = new Intent(BaseCameraActivity.this,PortraitCameraActivity.class);
                startActivity(intent);
                // do your code
                return true;
            case R.id.one_ratio_one:
                Intent oneRationOneIntent = new Intent(BaseCameraActivity.this,OneRatioOne.class);
                startActivity(oneRationOneIntent);
                // do your code
                return true;
            case R.id.four_ratio_three:
                Intent fourRatioThreeIntent = new Intent(BaseCameraActivity.this,ThreeRatioFour.class);
                startActivity(fourRatioThreeIntent);
                // do your code
                return true;
            case R.id.sixteen_ratio_nine:
                // do your code
                Intent nineRatioSixteenIntent = new Intent(BaseCameraActivity.this,NineRatioSixteen.class);
                startActivity(nineRatioSixteenIntent);
                return true;
            case R.id.landscape:
                Intent landscapeIntent = new Intent(BaseCameraActivity.this,LandscapeCameraActivity.class);
                startActivity(landscapeIntent);
                // do your code
                return true;
            case R.id.square:
                // do your code
                Intent squreIntent = new Intent(BaseCameraActivity.this,SquareCameraActivity.class);
                startActivity(squreIntent);
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        wasRunning = running;
        running = false;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("seconds", seconds);
        savedInstanceState.putBoolean("running", running);
        savedInstanceState.putBoolean("wasRunning", wasRunning);
    }

    private void runTimer() {

        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours = seconds/3600;
                int minutes = (seconds%3600)/60;
                int secs = seconds%60;
                String time = String.format("%d:%02d:%02d",
                        hours, minutes, secs);
                timeView.setText(time);
                // timeView.setVisibility(View.VISIBLE);
                if (running) {
                    seconds++;
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpCamera();
        if (wasRunning) {
            running = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseCamera();
        mediaPlayer.stop();
    }

    private void releaseCamera() {
        if (sampleGLView != null) {
            sampleGLView.onPause();
        }

        if (GPUCameraRecorder != null) {
            GPUCameraRecorder.stop();
            GPUCameraRecorder.release();
            GPUCameraRecorder = null;
        }

        if (sampleGLView != null) {
            ((FrameLayout) findViewById(R.id.wrap_view)).removeView(sampleGLView);
            sampleGLView = null;
        }
    }


    private void setUpCameraView() {
        runOnUiThread(() -> {
            FrameLayout frameLayout = findViewById(R.id.wrap_view);
            frameLayout.removeAllViews();
            sampleGLView = null;
            sampleGLView = new SampleCameraGLView(getApplicationContext());
            sampleGLView.setTouchListener((event, width, height) -> {
                if (GPUCameraRecorder == null) return;
                GPUCameraRecorder.changeManualFocusPoint(event.getX(), event.getY(), width, height);
            });
            frameLayout.addView(sampleGLView);
        });
    }


    private void setUpCamera() {
        setUpCameraView();

        GPUCameraRecorder = new GPUCameraRecorderBuilder(this, sampleGLView)
                //.recordNoFilter(true)
                .cameraRecordListener(new CameraRecordListener() {
                    @Override
                    public void onGetFlashSupport(boolean flashSupport) {
                        runOnUiThread(() -> {
                            findViewById(R.id.btn_flash).setEnabled(flashSupport);
                        });
                    }

                    @Override
                    public void onRecordComplete() {
                        exportMp4ToGallery(getApplicationContext(), filepath);
                        mediaPlayer.stop();
                    }

                    @Override
                    public void onRecordStart() {
                        runOnUiThread(() -> {
                            lv.setVisibility(View.GONE);
                        });
                    }

                    @Override
                    public void onError(Exception exception) {
                        Log.e("GPUCameraRecorder", exception.toString());
                    }

                    @Override
                    public void onCameraThreadFinish() {
                        if (toggleClick) {
                            runOnUiThread(() -> {
                                setUpCamera();
                            });
                        }
                        toggleClick = false;
                    }

                    @Override
                    public void onVideoFileReady() {

                    }
                })
                .videoSize(videoWidth, videoHeight)
                .cameraSize(cameraWidth, cameraHeight)
                .lensFacing(lensFacing)
                .build();


    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

//    private void changeFilter(Filters filters) {
//        GPUCameraRecorder.setFilter(Filters.getFilterInstance(filters, getApplicationContext()));
//    }


    private interface BitmapReadyCallbacks {
        void onBitmapReady(Bitmap bitmap);
    }

    private void captureBitmap(final BitmapReadyCallbacks bitmapReadyCallbacks) {
        sampleGLView.queueEvent(() -> {
            EGL10 egl = (EGL10) EGLContext.getEGL();
            GL10 gl = (GL10) egl.eglGetCurrentContext().getGL();
            Bitmap snapshotBitmap = createBitmapFromGLSurface(sampleGLView.getMeasuredWidth(), sampleGLView.getMeasuredHeight(), gl);

            runOnUiThread(() -> {
                bitmapReadyCallbacks.onBitmapReady(snapshotBitmap);
            });
        });
    }

    private Bitmap createBitmapFromGLSurface(int w, int h, GL10 gl) {

        int bitmapBuffer[] = new int[w * h];
        int bitmapSource[] = new int[w * h];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);

        try {
            gl.glReadPixels(0, 0, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
            int offset1, offset2, texturePixel, blue, red, pixel;
            for (int i = 0; i < h; i++) {
                offset1 = i * w;
                offset2 = (h - i - 1) * w;
                for (int j = 0; j < w; j++) {
                    texturePixel = bitmapBuffer[offset1 + j];
                    blue = (texturePixel >> 16) & 0xff;
                    red = (texturePixel << 16) & 0x00ff0000;
                    pixel = (texturePixel & 0xff00ff00) | red | blue;
                    bitmapSource[offset2 + j] = pixel;
                }
            }
        } catch (GLException e) {
            Log.e("CreateBitmap", "createBitmapFromGLSurface: " + e.getMessage(), e);
            return null;
        }

        return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
    }

    public void saveAsPngImage(Bitmap bitmap, String filePath) {
        try {
            File file = new File(filePath);
            FileOutputStream outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void exportMp4ToGallery(Context context, String filePath) {
        final ContentValues values = new ContentValues(2);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.DATA, filePath);
        context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                values);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + filePath)));
    }

    public static String getVideoFilePath() {
        return getAndroidMoviesFolder().getAbsolutePath() + "/" + new SimpleDateFormat("yyyyMM_dd-HHmmss").format(new Date()) + "GPUCameraRecorder.mp4";
    }

    public static File getAndroidMoviesFolder() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
    }

    private static void exportPngToGallery(Context context, String filePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(filePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    public static String getImageFilePath() {
        return getAndroidImageFolder().getAbsolutePath() + "/" + new SimpleDateFormat("yyyyMM_dd-HHmmss").format(new Date()) + "GPUCameraRecorder.png";
    }

    public static File getAndroidImageFolder() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    }

    //---------------------------------------------------------------------------------------------


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

        }
    }

    //-------------------------------------------------------------

}
