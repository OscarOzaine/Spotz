package com.spotz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.spotz.camera.ImageLoader;
import com.spotz.camera.VideoControllerView;
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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

public class SpotActivity extends Activity implements MediaPlayer.OnPreparedListener {
	
	TextView txtName, txtLikes, txtDislikes, txtType, txtCity;
	TextView txtDescription, txtCreatedat, txtEmail;
	ImageView imgSpot;
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
	
	FrameLayout frameLayoutVideo = null;
    SurfaceView videoSurface = null;
    MediaPlayer mediaPlayer = null;
    VideoControllerView controller = null;
    SurfaceHolder videoHolder = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_spot);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.app_name);
        actionBar.setIcon(android.R.color.transparent);
        
        //actionBar.setBackgroundDrawable(new ColorDrawable(this.getResources().getColor(R.color.my_action_bar_color)));
		
		//txtName			= (TextView) findViewById(R.id.spot_name);
		//txtLikes		= (TextView) findViewById(R.id.spot_likes);
		//txtDislikes		= (TextView) findViewById(R.id.spot_dislikes);
		txtType			= (TextView) findViewById(R.id.spot_type);
		txtCity			= (TextView) findViewById(R.id.spot_city);
		txtDescription	= (TextView) findViewById(R.id.spot_description);
		//txtCreatedat	= (TextView) findViewById(R.id.spot_created_at);
		txtEmail		= (TextView) findViewById(R.id.spot_email);
	    imgSpot			= (ImageView) findViewById(R.id.spot_image);
	    frameLayoutVideo= (FrameLayout) findViewById(R.id.spotvideoSurfaceContainer);
		
		Intent intent = getIntent();
		
		spotId			= intent.getStringExtra("id");
		//spotName 		= intent.getStringExtra("name");
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
		
		
		videoSurface	= (SurfaceView) findViewById(R.id.spotvideoSurface);
		
		//videoSurface.setRotation(90);
        videoHolder		= videoSurface.getHolder();
        
        videoHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        videoHolder.addCallback(surfaceCallback);
        
        videoSurface.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
        		controller.show(); 
        		return true; 
            }
        });
		
		//Log.d(TAG,"imagepath = "+mediaPath);
		if(Utils.isVideo(mediaPath)){

			imgSpot.setVisibility(View.GONE);
			//frameLayoutVideo.setRotation(270);
			//frameLayoutVideo.refreshDrawableState();
			//frameLayoutVideo.setRotationX(90);
			
			frameLayoutVideo.setVisibility(View.VISIBLE);

			try {
	        	mediaPlayer = new MediaPlayer(); 
	    		controller = new VideoControllerView(this);
	    		
	        	mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	        	mediaPlayer.setDataSource(SpotActivity.this, Uri.parse(mediaPath));
	        	mediaPlayer.setOnPreparedListener(this);
	        	mediaPlayer.prepareAsync();
	        	Log.d(TAG,"Mediaplayer="+mediaPath);
	        } catch (IllegalArgumentException e) {
	            e.printStackTrace();
	        } catch (SecurityException e) {
	            e.printStackTrace();
	        } catch (IllegalStateException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
			
		}else{
			imgSpot.setVisibility(View.VISIBLE);
			frameLayoutVideo.setVisibility(View.GONE);
			
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
		
		//txtName.setText(spotName);
		//txtLikes.setText(spotLikes);
		//txtDislikes.setText(spotDislikes);
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
	    Utils.setCurrentLocale(this);
	    uiHelper.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
		if(mediaPlayer != null){
			mediaPlayer.stop();
			mediaPlayer.release();
		}
			
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		uiHelper.onDestroy();
	    super.onDestroy();
	    
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
        	return true;
        case R.id.action_share_facebook:
        	FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this)
            .setLink(Utils.createSpotLink(spotCity, spotId))
            //.setName(spotName)
            .setDescription(spotDescription)
            .setPicture(mediaPath)
            .setApplicationName(getString(R.string.app_name))
            .build();
    	uiHelper.trackPendingDialogCall(shareDialog.present());
        	return true;
       
        case R.id.action_mapspot:
        	Log.d(TAG,"map = "+latitude+" long = "+longitude);
        	String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)", Float.parseFloat(latitude), Float.parseFloat(longitude),Float.parseFloat(latitude), Float.parseFloat(longitude),spotDescription);
        	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        	this.startActivity(intent);
        	//onBackPressed();
            //NavUtils.navigateUpFromSameTask(this);
        return true;
        default:
        	Log.d(TAG,"Default = "+item.getItemId());
        	//NewsActivity.LoadSpots.execute();
        	onBackPressed();
        	
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
						//txtName.setText(name);
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
	
	
	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			// TODO Auto-generated method stub
		}

		public void surfaceChanged(SurfaceHolder holder,
				int format, int width,
				int height) {
			Log.d(TAG,"surfaceChanged");
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// no-op
		}
	};
    
	VideoControllerView.MediaPlayerControl mediaPlayerControl = new VideoControllerView.MediaPlayerControl(){
		
		@Override
		public void start() {
			// TODO Auto-generated method stub
			if(mediaPlayer!=null)
				mediaPlayer.start();
		}


		@Override
		public void pause() {
			// TODO Auto-generated method stub
			if(mediaPlayer!=null)
				mediaPlayer.pause();
			
		}


		@Override
		public int getDuration() {
			// TODO Auto-generated method stub
			if(mediaPlayer!=null)
				return mediaPlayer.getDuration();
			else
				return 0;
		}


		@Override
		public int getCurrentPosition() {
			// TODO Auto-generated method stub
			if(mediaPlayer!=null)
				return mediaPlayer.getCurrentPosition();
			else
				return 0;
		}


		@Override
		public void seekTo(int pos) {
			// TODO Auto-generated method stub
			mediaPlayer.seekTo(pos);
		}


		@Override
		public boolean isPlaying() {
			// TODO Auto-generated method stub
			if(mediaPlayer!=null)
				return mediaPlayer.isPlaying();
			else
				return false;
		}


		@Override
		public int getBufferPercentage() {
			// TODO Auto-generated method stub
			return 0;
		}


		@Override
		public boolean canPause() {
			// TODO Auto-generated method stub
			return true;
		}


		@Override
		public boolean canSeekBackward() {
			// TODO Auto-generated method stub
			return true;
		}


		@Override
		public boolean canSeekForward() {
			// TODO Auto-generated method stub
			return true;
		}


		@Override
		public boolean isFullScreen() {
			// TODO Auto-generated method stub
			return false;
		}


		@Override
		public void toggleFullScreen() {
			// TODO Auto-generated method stub
		}
	};
	
	@Override
	public void onPrepared(MediaPlayer mp) {
		
		//videoSurface.getHolder().setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
		//videoSurface.getHolder().lockCanvas().rotate(90);
		mediaPlayer.setDisplay(videoSurface.getHolder());
		
    	//mediaPlayer.
		controller.setMediaPlayer(mediaPlayerControl);
        controller.setAnchorView((FrameLayout) findViewById(R.id.spotvideoSurfaceContainer));
        
        mediaPlayer.start();
	}
	
}