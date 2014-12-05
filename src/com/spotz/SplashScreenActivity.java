package com.spotz;

import java.util.Timer;
import java.util.TimerTask;

import com.example.androidhive.R;
 
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
 
public class SplashScreenActivity extends Activity {
 
    // Set the duration of the splash screen
    private static final long SPLASH_SCREEN_DELAY = 1500;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        // Set portrait orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Hide title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
 
        setContentView(R.layout.splash_screen);
 
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
            	
            	if(SessionManager.requestLogin(SplashScreenActivity.this)){
            		Intent mainIntent = new Intent().setClass(
                            SplashScreenActivity.this, CameraActivity.class);
                    startActivity(mainIntent);
                    overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
                    // Close the activity so the user won't able to go back this
                    // activity pressing Back button
                    finish();
            	}
            	else{
            		Intent mainIntent = new Intent().setClass(
                            SplashScreenActivity.this, LoginActivity.class);
                    startActivity(mainIntent);
                    overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
                    // Close the activity so the user won't able to go back this
                    // activity pressing Back button
                    finish();
            	}
            	
            	/*
                // Start the next activity
                Intent mainIntent = new Intent().setClass(
                        SplashScreenActivity.this, MainActivity.class);
                startActivity(mainIntent);
 
                // Close the activity so the user won't able to go back this
                // activity pressing Back button
                finish();
                */
            }
        };
 
        // Simulate a long loading process on application startup.
        Timer timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);
    }
 
}