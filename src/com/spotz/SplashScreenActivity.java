package com.spotz;

import java.util.Timer;
import java.util.TimerTask;

import com.spotz.gen.R;
 
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
 
public class SplashScreenActivity extends Activity {
	
    private static final long SPLASH_SCREEN_DELAY = 1000;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash_screen);
 
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
            	Intent intent = null;
            	if(SessionManager.requestLogin(SplashScreenActivity.this)){
            		intent = new Intent().setClass(
                            SplashScreenActivity.this, CameraActivity.class);
            	}
            	else{
            		intent = new Intent().setClass(
                            SplashScreenActivity.this, LoginActivity.class);
            	}
            	startActivity(intent);
                overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
                finish();
            }
        };
 
        // Simulate a long loading process on application startup.
        Timer timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);
    }
}