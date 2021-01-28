package com.daasuu.gpuvideoandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class NineRatioSixteen extends BaseCameraActivity {

    public static void startActivity(Activity activity) {
        Intent intent = new Intent(activity, NineRatioSixteen.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nine_ratio_sixteen);
        onCreateActivity();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(NineRatioSixteen.this,MainActivity.class);
        startActivity(intent);
    }
}