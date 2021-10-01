package com.underdog.raver;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.underdog.gpuv.composer.FillMode;
import com.underdog.gpuv.composer.GPUMp4Composer;
import com.underdog.gpuv.egl.filter.GlFilter;
import com.underdog.gpuv.egl.filter.GlFilterGroup;
import com.underdog.gpuv.egl.filter.GlMonochromeFilter;
import com.underdog.gpuv.egl.filter.GlVignetteFilter;
import com.underdog.raver.compose.VideoItem;
import com.underdog.raver.compose.VideoListAdapter;
import com.underdog.raver.compose.VideoLoadListener;
import com.underdog.raver.compose.VideoLoader;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class Mp4ComposeActivity extends AppCompatActivity {

    public static void startActivity(Activity activity) {
        Intent intent = new Intent(activity, Mp4ComposeActivity.class);
        activity.startActivity(intent);
    }

    private VideoLoader videoLoader;

    private VideoItem videoItem = null;

    private static final String TAG = "SAMPLE";

    private static final int PERMISSION_REQUEST_CODE = 88888;

    private GPUMp4Composer GPUMp4Composer;

    private CheckBox muteCheckBox;
    private CheckBox flipVerticalCheckBox;
    private CheckBox flipHorizontalCheckBox;

    private String videoPath;
    private AlertDialog filterDialog;
    private GlFilter glFilter = new GlFilterGroup(new GlMonochromeFilter(), new GlVignetteFilter());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mp4compose);
        getSupportActionBar().setTitle("Compose Your Videos");

        muteCheckBox = findViewById(R.id.mute_check_box);
        flipVerticalCheckBox = findViewById(R.id.flip_vertical_check_box);
        flipHorizontalCheckBox = findViewById(R.id.flip_horizontal_check_box);

        findViewById(R.id.start_codec_button).setOnClickListener(v -> {
            v.setEnabled(false);
            startCodec();
        });

        findViewById(R.id.cancel_button).setOnClickListener(v -> {
            if (GPUMp4Composer != null) {
                GPUMp4Composer.cancel();
            }
        });

        findViewById(R.id.start_play_movie).setOnClickListener(v -> {
            Uri uri = Uri.parse(videoPath);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setDataAndType(uri, "video/mp4");
            startActivity(intent);
        });

        findViewById(R.id.btn_filter).setOnClickListener(v -> {
            if (filterDialog == null) {

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Choose a filter");
                builder.setOnDismissListener(dialog -> {
                    filterDialog = null;
                });

                final FilterType[] filters = FilterType.values();
                CharSequence[] charList = new CharSequence[filters.length];
                for (int i = 0, n = filters.length; i < n; i++) {
                    charList[i] = filters[i].name();
                }
                builder.setItems(charList, (dialog, item) -> {
                    changeFilter(filters[item]);
                });
                filterDialog = builder.show();
            } else {
                filterDialog.dismiss();
            }
        });

    }

    private void changeFilter(FilterType filter) {
        glFilter = null;
        glFilter = FilterType.createGlFilter(filter, this);
        Button button = findViewById(R.id.btn_filter);
        button.setText("Filter : " + filter.name());
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermission()) {
            videoLoader = new VideoLoader(getApplicationContext());
            videoLoader.loadDeviceVideos(new VideoLoadListener() {
                @Override
                public void onVideoLoaded(final List<VideoItem> items) {

                    ListView lv = findViewById(R.id.video_list);
                    VideoListAdapter adapter = new VideoListAdapter(getApplicationContext(), R.layout.row_video_list, items);
                    lv.setAdapter(adapter);

                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            videoItem = null;
                            videoItem = items.get(position);
                            findViewById(R.id.start_codec_button).setEnabled(true);
                        }
                    });
                }

                @Override
                public void onFailed(Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void startCodec() {
        videoPath = getVideoFilePath();

        final ProgressBar progressBar = findViewById(R.id.progress_bar);
        progressBar.setMax(100);

        findViewById(R.id.start_play_movie).setEnabled(false);

        GPUMp4Composer = null;
        GPUMp4Composer = new GPUMp4Composer(videoItem.getPath(), videoPath)
                // .rotation(Rotation.ROTATION_270)
                //.size(720, 720)
                .fillMode(FillMode.PRESERVE_ASPECT_CROP)
                .filter(glFilter)
                .mute(muteCheckBox.isChecked())
                .flipHorizontal(flipHorizontalCheckBox.isChecked())
                .flipVertical(flipVerticalCheckBox.isChecked())
                .listener(new GPUMp4Composer.Listener() {
                    @Override
                    public void onProgress(double progress) {
                        Log.d(TAG, "onProgress = " + progress);
                        runOnUiThread(() -> progressBar.setProgress((int) (progress * 100)));
                    }

                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted()");
                        exportMp4ToGallery(getApplicationContext(), videoPath);
                        runOnUiThread(() -> {
                            progressBar.setProgress(100);
                            findViewById(R.id.start_codec_button).setEnabled(true);
                            findViewById(R.id.start_play_movie).setEnabled(true);
                            Toast.makeText(Mp4ComposeActivity.this, "codec complete path =" + videoPath, Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onCanceled() {

                    }

                    @Override
                    public void onFailed(Exception exception) {
                        Log.d(TAG, "onFailed()");
                    }
                })
                .start();


    }


    public File getAndroidMoviesFolder() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
    }

    public String getVideoFilePath() {
        return getAndroidMoviesFolder().getAbsolutePath() + "/" + new SimpleDateFormat("yyyyMM_dd-HHmmss").format(new Date()) + "filter_apply.mp4";
    }

    /**
     * ギャラリーにエクスポート
     *
     * @param filePath
     * @return The video MediaStore URI
     */
    public static void exportMp4ToGallery(Context context, String filePath) {
        // ビデオのメタデータを作成する
        final ContentValues values = new ContentValues(2);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.DATA, filePath);
        // MediaStoreに登録
        context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                values);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + filePath)));
    }


    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        // request permission if it has not been grunted.
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(Mp4ComposeActivity.this, "permission has been grunted.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Mp4ComposeActivity.this, "[WARN] permission is not grunted.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

}
