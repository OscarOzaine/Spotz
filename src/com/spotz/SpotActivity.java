package com.spotz;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.spotz.camera.ImageLoader;
import com.spotz.gen.R;
import com.spotz.utils.JSONParser;
import com.spotz.utils.Utils;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

public class SpotActivity extends Activity {
	// All xml labels
	TextView txtName, txtLikes, txtDislikes, txtType, txtCity;
	TextView txtDescription, txtCreatedat, txtEmail;
	ImageView imgSpot;
	VideoView vidSpot;
	String TAG = "SpotActivity";
	// Progress Dialog
	private ProgressDialog pDialog;

	// Creating JSON Parser object
	JSONParser jsonParser = new JSONParser();
	
	// Profile json object
	JSONObject spot;
	
	// Profile JSON url
	private static String PROFILE_URL = "http://api.myhotspotz.net/app/getspot/";
	
	// ALL JSON node names
	private static final String TAG_PROFILE = "profile";
	private static final String TAG_ID = "id";
	private static final String TAG_NAME = "name";
	private static final String TAG_EMAIL = "email";
	private static final String TAG_MOBILE = "mobile";
	private static final String TAG_ADDRESS = "address";
	
	String spotId = "";

	String spotName = "", spotLikes = "", spotDislikes = "";
	String spotType = "", spotCity = "", spotDescription = "";
	String spotCreatedat = "", spotEmail = "", mediaPath = "";
	String longitude = "", latitude = "";
	MediaController mediaController;
	
	
	private UiLifecycleHelper uiHelper;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_spot);
		
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.app_name);
        actionBar.setBackgroundDrawable(new ColorDrawable(0xff1f8b1f));
		
		txtName			= (TextView) findViewById(R.id.spot_name);
		txtLikes		= (TextView) findViewById(R.id.spot_likes);
		txtDislikes		= (TextView) findViewById(R.id.spot_dislikes);
		txtType			= (TextView) findViewById(R.id.spot_type);
		txtCity			= (TextView) findViewById(R.id.spot_city);
		txtDescription	= (TextView) findViewById(R.id.spot_description);
		//txtCreatedat	= (TextView) findViewById(R.id.spot_created_at);
		txtEmail		= (TextView) findViewById(R.id.spot_email);
	    imgSpot			= (ImageView) findViewById(R.id.spot_image);
	    vidSpot 		= (VideoView) findViewById(R.id.spot_video);
	    
		Intent intent = getIntent();
		
		spotId			= intent.getStringExtra("id");
		spotName 		= intent.getStringExtra("name");
		spotLikes 		= intent.getStringExtra("likes");
		spotDislikes 	= intent.getStringExtra("dislikes");
		spotType 		= intent.getStringExtra("spottype");
		spotCity 		= intent.getStringExtra("cityname");
		spotDescription = intent.getStringExtra("description");
		//spotCreatedat = intent.getStringExtra("created_at");
		spotEmail 		= intent.getStringExtra("email");
		mediaPath 		= intent.getStringExtra("image");
		longitude 		= intent.getStringExtra("longitude");
		latitude 		= intent.getStringExtra("latitude");
		
		//Log.d(TAG,"imagepath = "+mediaPath);
		if(Utils.isVideo(mediaPath)){
			imgSpot.setVisibility(View.GONE);
			vidSpot.setVisibility(View.VISIBLE);
			
			mediaController = new MediaController(this);
		    mediaController.setAnchorView(vidSpot);
		    vidSpot.setVisibility(View.VISIBLE);
		    vidSpot.setFocusable(true);
		    vidSpot.setFocusableInTouchMode(true);
		    
            Log.d(TAG,"Spotssss = "+mediaPath);
		    Uri uri=Uri.parse(mediaPath);        
		    vidSpot.setMediaController(mediaController);
		    vidSpot.setVideoURI(uri);        
		    vidSpot.requestFocus();
		    vidSpot.start();
		}else{
			vidSpot.setVisibility(View.GONE);
			imgSpot.setVisibility(View.VISIBLE);
			ImageLoader imageLoader = new ImageLoader(getBaseContext());
	        // Capture position and set results to the ImageView
	        // Passes flag images URL into ImageLoader.class
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize=8;      // 1/8 of original image
			Bitmap bitmap = imageLoader.DisplayImage(mediaPath, imgSpot);
			
			float scalingFactor = getBitmapScalingFactor(bitmap);
	        Bitmap newBitmap = Utils.ScaleBitmap(bitmap, scalingFactor);
	        imgSpot.setImageBitmap(newBitmap);
		}
		
		
		
        
		txtName.setText(spotName);
		txtLikes.setText(spotLikes);
		txtDislikes.setText(spotDislikes);
		txtType.setText(spotType);
		txtCity.setText(spotCity);
		txtDescription.setText(spotDescription);
		//txtCreatedat.setText("");
		txtEmail.setText(spotEmail);
		
		
		//PROFILE_URL = PROFILE_URL + "/"+spotId;
		//Log.d(TAG,spotId);
		
        // Loading Profile in Background Thread
        //new LoadSpot().execute();
		
		uiHelper = new UiLifecycleHelper(this, null);
	    uiHelper.onCreate(savedInstanceState);
	}

	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);

	    uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
	        @Override
	        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
	            Log.e("Activity", String.format("Error: %s", error.toString()));
	        }

	        @Override
	        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
	            Log.i("Activity", "Success!");
	        }
	    });
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    uiHelper.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}
	
	private float getBitmapScalingFactor(Bitmap bm) {
        // Get display width from device
        int displayWidth = getWindowManager().getDefaultDisplay().getWidth();

        // Get margin to use it for calculating to max width of the ImageView
        LinearLayout.LayoutParams layoutParams = 
                (LinearLayout.LayoutParams)this.imgSpot.getLayoutParams();
        int leftMargin = layoutParams.leftMargin;
        int rightMargin = layoutParams.rightMargin;

        // Calculate the max width of the imageView
        int imageViewWidth = displayWidth - (leftMargin + rightMargin);

        // Calculate scaling factor and return it
        return ( (float) imageViewWidth / (float) bm.getWidth() );
    }
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.spotmenu, menu);
	    return super.onCreateOptionsMenu(menu);
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		
        switch (item.getItemId()) {
        
        case R.id.action_sharespot:
        	Log.d(TAG,"link = "+Utils.createSpotLink(spotCity, spotName, spotId));
        	Log.d(TAG,"name = "+spotName);
        	Log.d(TAG,"Description = "+spotDescription);
        	Log.d(TAG,"picture = "+mediaPath);
        	
        	FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this)
	            .setLink(Utils.createSpotLink(spotCity, spotName, spotId))
	            .setName(spotName)
	            .setDescription(spotDescription)
	            .setPicture(mediaPath)
	            .setApplicationName(getString(R.string.app_name))
	            .build();
        	uiHelper.trackPendingDialogCall(shareDialog.present());
        	//onBackPressed();
            //NavUtils.navigateUpFromSameTask(this);
        return true;
        
        case R.id.action_mapspot:
        	Log.d(TAG,"map = "+latitude+" long = "+longitude);
        	String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)", Float.parseFloat(latitude), Float.parseFloat(longitude),Float.parseFloat(latitude), Float.parseFloat(longitude),spotName);
        	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        	this.startActivity(intent);
        	//onBackPressed();
            //NavUtils.navigateUpFromSameTask(this);
        return true;
        default:
        	Log.d(TAG,"Default = "+item.getItemId());
            return super.onOptionsItemSelected(item);
         
        }
        
    }
	/**
	 * Background Async Task to Load profile by making HTTP Request
	 * */
	class LoadSpot extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(SpotActivity.this);
			pDialog.setMessage("Loading spot ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting Profile JSON
		 * */
		protected String doInBackground(String... args) {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			// getting JSON string from URL
			JSONObject json = jsonParser.makeHttpRequest(PROFILE_URL, "GET",
					params);
			// Check your log cat for JSON reponse
			Log.d("Profile JSON: ", json.toString());
			try {
				// profile json object
				spot = json.getJSONObject(TAG_PROFILE);
			} catch (JSONException e) {
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
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {
					/**
					 * Updating parsed JSON data into ListView
					 * */
					// Storing each json item in variable
					try {
						String id = spot.getString(TAG_ID);
						String name = spot.getString(TAG_NAME);
						String email = spot.getString(TAG_EMAIL);
						String mobile = spot.getString(TAG_MOBILE);
						String address = spot.getString(TAG_ADDRESS);
						
						// Aqui se inserta la info del json en los views
						txtName.setText(name);
						txtLikes.setText("2");
						txtDislikes.setText("5");
						txtType.setText("Social");
						txtCity.setText("Mexicali");
						txtDescription.setText("Descripcion del spot");
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			});

		}

	}
}