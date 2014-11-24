package com.spotz;

import com.example.androidhive.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.TextView;

public class LoadingActivity extends Activity {
	// All xml labels
	TextView txtName;
	TextView txtEmail;
	TextView txtMobile;
	TextView txtAddress;
	
	// Progress Dialog
	private ProgressDialog pDialog;

	
	// ALL JSON node names
	private static final String TAG_PROFILE = "profile";
	private static final String TAG_ID = "id";
	private static final String TAG_NAME = "name";
	private static final String TAG_EMAIL = "email";
	private static final String TAG_MOBILE = "mobile";
	private static final String TAG_ADDRESS = "address";
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading);
		
		//findViewById(R.id.loadingPanel).setVisibility(View.GONE);
		
	}

	
}
