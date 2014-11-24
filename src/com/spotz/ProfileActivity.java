package com.spotz;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.androidhive.R;
import com.spotz.NewsActivity.LoadOutbox;
import com.spotz.camera.ImageLoader;
import com.spotz.services.UploadMediaService;
import com.spotz.services.UploadProfilePicService;
import com.spotz.users.User;
import com.spotz.utils.JSONParser;
import com.spotz.utils.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ProfileActivity extends Activity  {
	
	boolean loadProfile = true;
	int PICK_IMAGE = 1;
	
	// All xml labels
	TextView txtUsername, txtEmail, txtCreated_at, txtPlus, txtMinus;
	TextView txtSuspended, txtConfirmed, txtFirstname, txtLastname;
	TextView txtLink, txtShareLocation, txtCurrentlat, txtCurrentLng;
	TextView txtFollowers;
	
	ImageView imageProfile;
	
	RelativeLayout profile_layout;
	PopupWindow popupMessage;
	Button popupButton, insidePopupButton;
	TextView popupText;

	
	
	
	String TAG = "ProfileActivity";
	// Progress Dialog
	private ProgressDialog pDialog;

	// Creating JSON Parser object
	JSONParser jsonParser = new JSONParser();
	
	// Profile json object
	JSONObject profile;
	
	// Profile JSON url
	private static final String PROFILE_URL = "http://api.myhotspotz.net/app/getprofile/";
	private static String MYPROFILE_URL = "http://api.myhotspotz.net/app/getprofile/";
	// ALL JSON node names
	private static final String TAG_PROFILE = "profile";
	private static final String TAG_ID = "id";
	private static final String TAG_USERNAME = "username";
	private static final String TAG_EMAIL = "email";
	private static final String TAG_CREATED_AT = "created_at";
	private static final String TAG_PLUS = "plus";
	private static final String TAG_MINUS = "minus";
	private static final String TAG_IMAGE = "image";
	private static final String TAG_SUSPENDED = "suspended";
	private static final String TAG_CONFIRMED = "confirmed";
	private static final String TAG_FIRSTNAME = "first_name";
	private static final String TAG_LASTNAME = "last_name";
	private static final String TAG_LINK = "link";
	private static final String TAG_SHARELOCATION = "sharelocation";
	private static final String TAG_CURRENTLAT = "currentlat";
	private static final String TAG_CURRENTLNG = "currentlng";
	private static final String TAG_FOLLOWERS = "followers";

	private String user;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);
		
		//txtUsername 	= (TextView) findViewById(R.id.profile_email);
		txtEmail 		= (TextView) findViewById(R.id.profile_email);
		//txtCreated_at 	= (TextView) findViewById(R.id.profile_created_at);
		txtPlus 		= (TextView) findViewById(R.id.profile_repplus);
		txtMinus 		= (TextView) findViewById(R.id.profile_repminus);
		//txtSuspended 	= (TextView) findViewById(R.id.profile_suspended);
		//txtConfirmed 	= (TextView) findViewById(R.id.profile_confirmed);
		//txtFirstname 	= (TextView) findViewById(R.id.profile_first_name);
		//txtLastname 	= (TextView) findViewById(R.id.profile_last_name);
		//txtLink 		= (TextView) findViewById(R.id.profile_link);
		//txtShareLocation = (TextView) findViewById(R.id.profile_sharelocation);
		//txtCurrentlat 	= (TextView) findViewById(R.id.profile_currentlat);
		//txtCurrentLng 	= (TextView) findViewById(R.id.profile_currentlng);
		txtFollowers 	= (TextView) findViewById(R.id.profile_followers);
		
		
		//$SQL = 'SELECT id, username, email, created_at, plus, minus, image, suspended, ';
		//$SQL.= 'confirmed, first_name, last_name, link, sharelocation, currentlat,currentlng, language ';
		MYPROFILE_URL = PROFILE_URL + User.current().getID();
		Log.d(TAG,"onCreate" + MYPROFILE_URL);
		imageProfile = (ImageView) findViewById(R.id.profile_image);

		imageProfile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
				// Add the buttons
				
				builder.setTitle(R.string.profile_options)
			           .setItems(R.array.profile_options_array, new DialogInterface.OnClickListener() {
			               public void onClick(DialogInterface dialog, int which) {
			               // The 'which' argument contains the index position
			               // of the selected item
			            	   Log.d(TAG,"user= "+User.current().getID());
			            	   Intent intent = new Intent();
			            	   intent.setType("image/*");
			            	   intent.setAction(Intent.ACTION_GET_CONTENT);
			            	   
			            	   startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
			               }
			           	});
				// Create the AlertDialog
				AlertDialog dialog = builder.create();
				dialog.show();
			} 

		});
    
		//init();
		//popupInit();
		//showDialog(ProfileActivity.this, "Tiutle", "Message");
	}
	
	@Override
	public void onResume() {
		super.onResume();
	    // Loading Profile in Background Thread
		new LoadProfile().execute();
		
		
		Log.d(TAG,"onResume " + PROFILE_URL);
	}
	
	/**
	 * Background Async Task to Load profile by making HTTP Request
	 **/
	class LoadProfile extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(ProfileActivity.this);
			pDialog.setMessage("Loading profile ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting Profile JSON
		 * */
		protected String doInBackground(String... args) {
			loadProfile = true;
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			
			JSONObject json = jsonParser.makeHttpRequest(MYPROFILE_URL, "GET", params);
			
			Log.d("Profile JSON: ", json.toString());
			try {
				profile = json.getJSONObject(TAG_PROFILE);
				//Log.d(TAG,profile.toString());
			} catch (JSONException e) {
				loadProfile = false;
				e.printStackTrace();
			}
			catch (RuntimeException e){
				loadProfile = false;
				e.printStackTrace();
			}
			
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all products
			pDialog.dismiss();
			if(loadProfile){
				// updating UI from Background Thread
				runOnUiThread(new Runnable() {
					public void run() {
						/**
						 * Updating parsed JSON data into ListView
						 * */
						// Storing each json item in variable
						try {
							
							//txtUsername 	= (TextView) findViewById(R.id.profile_email);
							txtEmail 		= (TextView) findViewById(R.id.profile_email);
							//txtCreated_at 	= (TextView) findViewById(R.id.profile_created_at);
							txtPlus 		= (TextView) findViewById(R.id.profile_repplus);
							txtMinus 		= (TextView) findViewById(R.id.profile_repminus);
							//txtSuspended 	= (TextView) findViewById(R.id.profile_suspended);
							//txtConfirmed 	= (TextView) findViewById(R.id.profile_confirmed);
							//txtFirstname 	= (TextView) findViewById(R.id.profile_first_name);
							//txtLastname 	= (TextView) findViewById(R.id.profile_last_name);
							//txtLink 		= (TextView) findViewById(R.id.profile_link);
							//txtShareLocation = (TextView) findViewById(R.id.profile_sharelocation);
							//txtCurrentlat 	= (TextView) findViewById(R.id.profile_currentlat);
							//txtCurrentLng 	= (TextView) findViewById(R.id.profile_currentlng);
							txtFollowers 	= (TextView) findViewById(R.id.profile_followers);
							imageProfile 	= (ImageView) findViewById(R.id.profile_image);
							
							String id 			= profile.getString(TAG_ID);
							String username 	= profile.getString(TAG_USERNAME);
							String email 		= profile.getString(TAG_EMAIL);
							String created_at 	= profile.getString(TAG_CREATED_AT);
							String plus 		= profile.getString(TAG_PLUS);
							String minus 		= profile.getString(TAG_MINUS);
							String image 		= profile.getString(TAG_IMAGE);
							String suspended 	= profile.getString(TAG_SUSPENDED);
							String confirmed 	= profile.getString(TAG_CONFIRMED);
							String first_name 	= profile.getString(TAG_FIRSTNAME);
							String last_name 	= profile.getString(TAG_LASTNAME);
							String link 		= profile.getString(TAG_LINK);
							String sharelocation = profile.getString(TAG_SHARELOCATION);
							String currentlat 	= profile.getString(TAG_CURRENTLAT);
							String currentlng 	= profile.getString(TAG_CURRENTLNG);
							String followers	= profile.getString(TAG_FOLLOWERS);
							
							// displaying all data in textview
							//txtName.setText(name);
							txtEmail.setText(email);
							txtPlus.setText(plus);
							txtMinus.setText(minus);
							txtFollowers.setText(followers);
							
							ImageLoader imageLoader = new ImageLoader(getBaseContext());
					        // Capture position and set results to the ImageView
					        // Passes flag images URL into ImageLoader.class
							image = "http://myhotspotz.net/public/images/profile/"+image;
					        imageLoader.clearCache();
							imageLoader.DisplayImage(image, imageProfile);
					        
							
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
				});
			}
			

		}

	}
	

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		user = "";
		if(requestCode == PICK_IMAGE && intent != null && intent.getData() != null) {
	        Uri _uri = intent.getData();

	        //User had pick an image.
	        Cursor cursor = getContentResolver().query(_uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
	        cursor.moveToFirst();

	        user = intent.getStringExtra("userid");
	        //Link to the image
	        final String imageFilePath = cursor.getString(0);
	        Log.d(TAG,imageFilePath);
	        Log.d(TAG,""+User.current().getID());
	        
	        Intent intentUploadProfilePic = new Intent(ProfileActivity.this, UploadProfilePicService.class);
	        intentUploadProfilePic.putExtra("imagepath", imageFilePath);
	        intentUploadProfilePic.putExtra("userid", ""+User.current().getID());
	        intentUploadProfilePic.putExtra("username", ""+User.current().getUsername());
	        intentUploadProfilePic.putExtra("createdat", ""+User.current().getCreatedat());
    		startService(intentUploadProfilePic);
    		
    		Intent intentProfile = new Intent(ProfileActivity.this, MainActivity.class);
			startActivity(intentProfile);      
			MainActivity.instance.getTabHost().setCurrentTab(2);
			finish();
	        cursor.close();
	    }
	    super.onActivityResult(requestCode, resultCode, intent);
	}
}
