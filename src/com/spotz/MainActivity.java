package com.spotz;

import com.example.androidhive.R;
import com.facebook.Session;
import com.spotz.NewsActivity.LoadOutbox;
import com.spotz.utils.Const;

import android.app.ActionBar;
import android.app.SearchManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

public class MainActivity extends TabActivity {
	// TabSpec Names
	private static final String INBOX_SPEC = "Camera";
	private static final String OUTBOX_SPEC = "News";
	private static final String PROFILE_SPEC = "Profile";
	static String TAG = "MainActivity";
	//this activity Instance
	public static MainActivity instance;
	private AsyncTask<String, String, String> execute;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(R.string.app_name);
        actionBar.setBackgroundDrawable(new ColorDrawable(0xff1f8b1f));
        //actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setHomeButtonEnabled(true);
        
        instance = this;
        
        final TabHost tabHost = getTabHost();
        
        // Inbox Tab
        TabSpec inboxSpec = tabHost.newTabSpec(INBOX_SPEC);
        // Tab Icon
        inboxSpec.setIndicator(INBOX_SPEC, getResources().getDrawable(R.drawable.ic_camera));
        Intent loadingIntent = new Intent(this, LoadingActivity.class);
        // Tab Content
        inboxSpec.setContent(loadingIntent);
        //inboxSpec.set
        // Outbox Tab
        TabSpec outboxSpec = tabHost.newTabSpec(OUTBOX_SPEC);
        outboxSpec.setIndicator(OUTBOX_SPEC, getResources().getDrawable(R.drawable.ic_newsfeed));
        Intent outboxIntent = new Intent(this, NewsActivity.class);
        outboxSpec.setContent(outboxIntent);
        
        // Profile Tab
        TabSpec profileSpec = tabHost.newTabSpec(PROFILE_SPEC);
        profileSpec.setIndicator(PROFILE_SPEC, getResources().getDrawable(R.drawable.profile));
        Intent profileIntent = new Intent(this, ProfileActivity.class);
        profileSpec.setContent(profileIntent);
        
        // Adding all TabSpec to TabHost
        tabHost.addTab(inboxSpec); // Adding Inbox tab
        tabHost.addTab(outboxSpec); // Adding Outbox tab
        tabHost.addTab(profileSpec); // Adding Profile tab
        
        tabHost.setCurrentTab(1);
        
        tabHost.setOnTabChangedListener(new OnTabChangeListener() {

            public void onTabChanged(String tabId) {
                //Log.d(TAG , "onTabChanged: tab number=" + tabHost.getCurrentTab());

                switch (tabHost.getCurrentTab()) {
                case 0:
                	//tabHost.getContext();
                	Intent cameraIntent= new Intent(MainActivity.this, CameraActivity.class);
                	cameraIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                	//openMainActivity.setFlags(Intent.);
                    startActivity(cameraIntent);
                    //do what you want when tab 0 is selected
                    break;
                case 1:
                    //do what you want when tab 1 is selected
                    break;
                case 2:
                    //do what you want when tab 2 is selected
                    break;

                default:

                    break;
                }
            }
        });
        
        
    }
    
    public void onResume(Bundle savedInstanceState) {
    	//Log.d(TAG,"onResume");
    }
        
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        	 
            SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView search = (SearchView) menu.findItem(R.id.action_settings).getActionView();
            search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
            search.setOnQueryTextListener(new OnQueryTextListener() { 
                @Override 
                public boolean onQueryTextChange(String query) {
                	//Log.d(TAG,"LoadData");
                    //loadData(query);
                    return true; 
                }
				@Override
				public boolean onQueryTextSubmit(String query) {
					// TODO Auto-generated method stub
					return false;
				} 
            });
        }
	    return super.onCreateOptionsMenu(menu);
    }
    
    
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_logout:
        	
        	SessionManager.Logout();
    		SessionManager.requestLogin(this);
    		if (Session.getActiveSession() != null) {
    		    Session.getActiveSession().closeAndClearTokenInformation();
    		}

    		Session.setActiveSession(null);
    		Intent cameraIntent= new Intent(this, LoginActivity.class);
        	//openMainActivity.setFlags(Intent.);
            startActivity(cameraIntent); 
    		finish();  
        	break;
        case R.id.action_refresh:
        	//NewsActivity.LoadOutbox().execute();
        	getTabHost().setCurrentTab(2);
        	getTabHost().setCurrentTab(1);
        	break;
        }
        return true;
    }
    @Override
    public boolean onSearchRequested() {
        Log.d(TAG,"Search Requested");
        return super.onSearchRequested();
    }
    
    
}