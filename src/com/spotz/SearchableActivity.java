package com.spotz;

import com.example.androidhive.R;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class SearchableActivity extends Activity {
	
	String TAG = "SearchableActivity";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG,"OnCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.xml.searchable);
		
	    // Get the intent, verify the action and get the query
	    Intent intent = getIntent();
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	      String query = intent.getStringExtra(SearchManager.QUERY);
	      doMySearch(query);
	    }
		
	}

	public void doMySearch(String query){
		Log.d(TAG,"Query = "+query);
	}
}
