package com.underdog.raver;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                openmainactivity();
            }
        }, 2000);
    }

    private void openmainactivity(){

        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        overridePendingTransition(0, 0);

        finish();

    }

}