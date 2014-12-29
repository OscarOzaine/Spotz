package com.spotz.users;

import java.util.Locale;

import com.facebook.AppEventsLogger;
import com.spotz.gen.R;
import com.spotz.utils.Const;
import com.spotz.utils.Utils;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;

public class UserSettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener{
	
	String TAG = "UserSettingsActivity";
	String value = "";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Const.v(TAG, "+ onCreate +");
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.app_name);
        actionBar.setBackgroundDrawable(new ColorDrawable(this.getResources().getColor(R.color.my_action_bar_color)));
        actionBar.setIcon(android.R.color.transparent);
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
    
    @Override
    public synchronized void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
        	.registerOnSharedPreferenceChangeListener(this);
        Const.v(TAG, "+ ON RESUME +");
        
    }
    @Override
	public void onPause() {
		super.onPause();
		Const.v(TAG, "+ ON PAUSE +");
		getPreferenceScreen().getSharedPreferences()
        	.unregisterOnSharedPreferenceChangeListener(this);
		Utils.setCurrentLocale(this);
	}

    

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		if(key.equals("keyLanguage")){
			if(prefs.getString(key, "").equals("1")){
				setLocale("en");
			}
			else if(prefs.getString(key, "").equals("2")){
				setLocale("es");
			}
		}
		//value = prefs.getString(key, "");
		//Log.d(TAG,""+value);
		// TODO Auto-generated method stub
	}
	
	private void setLocale( String newLocale ){
	    Locale locale = new Locale( newLocale );
	    Locale.setDefault( locale );
	    Configuration config = new Configuration();
	    config.locale = locale;
	    this.getResources().updateConfiguration( config, this.getResources().getDisplayMetrics() );
	    
	}

}