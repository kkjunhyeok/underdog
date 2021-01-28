package com.daasuu.gpuvideoandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class OneRatioOne extends BaseCameraActivity {

    public static void startActivity(Activity activity) {
        Intent intent = new Intent(activity, OneRatioOne.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_ratio_one);
        onCreateActivity();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(OneRatioOne.this,MainActivity.class);
        startActivity(intent);
    }
}