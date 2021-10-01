package com.underdog.raver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ThreeRatioFour extends BaseCameraActivity {

    public static void startActivity(Activity activity) {
        Intent intent = new Intent(activity, ThreeRatioFour.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_three_ratio_four);
        onCreateActivity();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ThreeRatioFour.this,MainActivity.class);
        startActivity(intent);
    }
}