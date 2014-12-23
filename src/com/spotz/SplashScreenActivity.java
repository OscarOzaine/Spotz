package com.spotz;

import java.util.Timer;
import java.util.TimerTask;

import com.spotz.gen.R;
import com.spotz.location.MyLocation;
import com.spotz.location.MyLocation.LocationResult;
import com.spotz.utils.Const;
 
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
 
public class SplashScreenActivity extends Activity {
 
    // Set the duration of the splash screen
<<<<<<< HEAD
    private static final long SPLASH_SCREEN_DELAY = 1500;
    private String TAG = "SplashScreenActivity";
    
=======
    private static final long SPLASH_SCREEN_DELAY = 1000;
 
>>>>>>> origin/master
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        // Set portrait orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Hide title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
 
        setContentView(R.layout.splash_screen);
        
        LocationResult locationResult = new LocationResult(){
		    @Override
		    public void gotLocation(Location location){
		    	Const.currentLatitude = location.getLatitude();
		    	Const.currentLongitude = location.getLongitude();
		    	Log.d(TAG,"getLocationACAs"+Const.currentLongitude+"  "+Const.currentLatitude);
		        //Got the location!
		    }
		};
		MyLocation myLocation = new MyLocation();
		myLocation.getLocation(this, locationResult);
 
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
            	Intent intent;
            	if(SessionManager.requestLogin(SplashScreenActivity.this)){
            		intent = new Intent().setClass(SplashScreenActivity.this, CameraActivity.class);
            	}
            	else{
            		intent = new Intent().setClass(SplashScreenActivity.this, LoginActivity.class);
            	}
            	startActivity(intent);
                overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
                // Close the activity so the user won't able to go back this
                // activity by pressing Back button
                finish();
            }
        };
 
        // Simulate a long loading process on application startup.
        Timer timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);
    }
 
}