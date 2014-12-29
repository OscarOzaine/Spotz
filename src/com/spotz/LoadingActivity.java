package com.spotz;

import com.spotz.gen.R;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;

public class LoadingActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading);
		/*
		ActionBar actionBar = getActionBar();
		actionBar.hide();
		*/
	}
}
