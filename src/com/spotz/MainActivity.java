package com.spotz;

import java.util.List;
import java.util.Locale;

import com.facebook.Session;
import com.spotz.NewsActivity;
import com.spotz.database.Spot;
import com.spotz.database.SpotsHelper;
import com.spotz.gen.R;
import com.spotz.services.UploadMediaService;
import com.spotz.users.UserSettingsActivity;
import com.spotz.utils.Utils;
import com.spotz.NewsActivity.*;

import android.app.ActionBar;
import android.app.SearchManager;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends TabActivity {
	// TabSpec Names
	private static final String INBOX_SPEC = "Camera";
	private static final String OUTBOX_SPEC = "News";
	private static final String PROFILE_SPEC = "Profile";
	static String TAG = "MainActivity";
	//this activity Instance
	public static MainActivity instance;
	private AsyncTask<String, String, String> execute;
	
	private int loading = 0;
	SpotsHelper db = null;
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	      Bundle bundle = intent.getExtras();
	      if (bundle != null) {
	    	  
	        int resultCode = bundle.getInt("result");
	        String dbspotId = bundle.getString("dbspotid");
	        Log.d(TAG,"RESULTCODE = "+resultCode+ " spot "+dbspotId);
	        if (resultCode == 1) {
	        	MainActivity.instance.setProgressBarIndeterminateVisibility(false);
	        	Toast.makeText(MainActivity.this, "Successfully uploaded spot",Toast.LENGTH_LONG).show();
	        	db = new SpotsHelper(MainActivity.this);
	        	List<Spot> list = db.getAllSpots();
				for (int i = 0; i < list.size(); i++) {
					Log.d(TAG,dbspotId+" - "+list.get(i).getId());
					if(Integer.parseInt(dbspotId) == list.get(i).getId()){
						Log.d(TAG,"LIST="+list.get(i));
						db.deleteSpot(list.get(i));
						//new LoadSpots().execute();
					}
				}
	        } 
	        else if(resultCode == 2){
	        	MainActivity.instance.setProgressBarIndeterminateVisibility(false);
	        	Toast.makeText(MainActivity.this, "No internet connection",Toast.LENGTH_LONG).show();
	        }
	        else if(resultCode == 3){
	        	MainActivity.instance.setProgressBarIndeterminateVisibility(false);
	        	Toast.makeText(MainActivity.this, "Spot deleted successfully",Toast.LENGTH_LONG).show();
	        	findViewById(R.id.action_refresh).performClick();
	        }
	        else if(resultCode < 0) {
	        	MainActivity.instance.setProgressBarIndeterminateVisibility(false);
	        	Toast.makeText(MainActivity.this, "Failed to upload spot", Toast.LENGTH_LONG).show();
	        }
	      }
	    }
	};
	
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);
        
        
        
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(R.string.app_name);
        actionBar.setIcon(android.R.color.transparent);
        
        //actionBar.setBackgroundDrawable(new ColorDrawable(0xff1f8b1f));
        //actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setHomeButtonEnabled(true);
        
        // And when you want to turn it off
        
        instance = this;
        
        loading = getIntent().getIntExtra("loading",-1);
        if(loading == 1){
        	setProgressBarIndeterminateVisibility(true);
        }
        final TabHost tabHost = getTabHost();
        
        // Inbox Tab
        TabSpec inboxSpec = tabHost.newTabSpec(INBOX_SPEC);
        inboxSpec.setIndicator(INBOX_SPEC, getResources().getDrawable(R.drawable.ic_camera));
        Intent cameraIntent = new Intent(this, LoadingActivity.class);
        
        inboxSpec.setContent(cameraIntent);
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
                	overridePendingTransition( R.anim.slide_in_left, R.anim.slide_out_left );
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
    
    @Override
    protected void onResume() {
      super.onResume();
      Utils.setCurrentLocale(this);
      TabHost tabhost = getTabHost();
      TabWidget widget = tabhost.getTabWidget();
      for(int i=0;i<tabhost.getTabWidget().getChildCount();i++) {
    	  View v = widget.getChildAt(i);
    	  v.setBackgroundResource(R.drawable.custom_tab_selector);
    	  //tabhost.getTabWidget().getChildAt(i).findViewById(android.R.id.`)
          TextView tv = (TextView) tabhost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
          tv.setTextColor(Color.parseColor("#cc181e"));
      } 
      invalidateOptionsMenu();
      registerReceiver(receiver, new IntentFilter(UploadMediaService.NOTIFICATION));
    }
    
    @Override
    protected void onPause() {
      super.onPause();
      unregisterReceiver(receiver);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	db = new SpotsHelper(this);
    	List<Spot> list = db.getAllSpots();
    	//Log.d(TAG,"LIST SIZE = "+list.size());
    	
    	if(list.size() > 0){
    		getMenuInflater().inflate(R.menu.home_myspots, menu);
    	}else{
    		getMenuInflater().inflate(R.menu.home, menu);
    	}
        
        /*
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
        */
	    return super.onCreateOptionsMenu(menu);
    }
    
    
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        
        case R.id.action_uploadedspots:
        	Intent uploadedSpots= new Intent(this, MySpotsActivity.class);
        	//openMainActivity.setFlags(Intent.);
            startActivity(uploadedSpots); 
        	break;
        case R.id.action_logout:
        	SessionManager.Logout(this);
    		Intent cameraIntent= new Intent(this, LoginActivity.class);
        	//openMainActivity.setFlags(Intent.);
            startActivity(cameraIntent); 
            overridePendingTransition( R.anim.slide_out_up, R.anim.slide_in_up );
    		finish();  
        	break;
        case R.id.action_refresh:
        	
        	NewsActivity.initialize();
        	new LoadSpots().execute();
        	//NewsActivity.LoadOutbox().execute();
        	//getTabHost().setCurrentTab(2);
        	//getTabHost().setCurrentTab(1);
        	break;
        case R.id.action_settings:
        	Intent userSettingsIntent = new Intent(this, UserSettingsActivity.class);
        	//openMainActivity.setFlags(Intent.);
            startActivity(userSettingsIntent); 
            overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    		//finish(); 
        	
        	break;
        }
        return true;
    }
    @Override
    public boolean onSearchRequested() {
        Log.d(TAG,"Search Requested");
        return super.onSearchRequested();
    }
    
    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.popup_menu_myspots, popup.getMenu());
        popup.show();
    }
    
}