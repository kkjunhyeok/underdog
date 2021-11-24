package com.underdog.raver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class PortraitCameraActivity extends BaseCameraActivity {

    public static void startActivity(Activity activity) {
        Intent intent = new Intent(activity, PortraitCameraActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_portrate);
        onCreateActivity();
        videoWidth = 720;
        videoHeight = 1280;
        cameraWidth = 1280;
        cameraHeight = 720;
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(PortraitCameraActivity.this,MainActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }


}
