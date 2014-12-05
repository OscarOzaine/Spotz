package com.spotz.users;

import com.example.androidhive.R;

import android.app.ActionBar;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.MenuItem;

public class UserSettingsActivity extends PreferenceActivity {
	
	String TAG = "UserSettingsActivity";
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.app_name);
        actionBar.setBackgroundDrawable(new ColorDrawable(0xff1f8b1f));

        addPreferencesFromResource(R.xml.options);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		
        switch (item.getItemId()) {
        
        case android.R.id.home:
        	Log.d(TAG,"Home = "+item.getItemId());
        	onBackPressed();
            //NavUtils.navigateUpFromSameTask(this);
            return true;
        default:
        	Log.d(TAG,"Default = "+item.getItemId());
            return super.onOptionsItemSelected(item);
        }
    }
}