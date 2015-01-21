package com.spotz;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.spotz.camera.ImageLoader;
import com.spotz.camera.VideoControllerView;
import com.spotz.gen.R;
import com.spotz.location.LocationUtils;
import com.spotz.location.MyLocation;
import com.spotz.location.MyLocation.LocationResult;
import com.spotz.services.UploadMediaService;
import com.spotz.users.User;
import com.spotz.utils.Const;
import com.spotz.utils.Utils;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class UploadSpotActivity extends Activity implements			
			MediaPlayer.OnPreparedListener{


	// All xml labels
	TextView txtType, txtDescription;
	EditText editSpotName, editSpotDescription;
	Spinner SpinnerSpotType;
	ImageView spotImage;
	String currentLat = "", currentLng = "";
	ImageButton btn1,btn2;
	
	static String TAG = "UploadSpotActivity";
	// Progress Dialog
	private ProgressDialog pDialog;
	String upLoadServerUri = "http://api.myhotspotz.net/app/uploadSpot";
	String mediaPath;
	int serverResponseCode = 0;
	
	// Handle to SharedPreferences for this app
    SharedPreferences mPrefs;
    // Handle to a SharedPreferences editor
    SharedPreferences.Editor mEditor;
	/*
     * Note if updates have been turned on. Starts out as "false"; is set to "true" in the
     * method handleRequestSuccess of LocationUpdateReceiver.
     *
     */
    boolean mUpdatesRequested = false;
    MediaController media_Controller;
    DisplayMetrics dm;
    Bitmap bitmap;
    
    FrameLayout frameLayoutVideo = null;
    SurfaceView videoSurface = null;
    MediaPlayer mediaPlayer = null;
    VideoControllerView controller = null;
    SurfaceHolder videoHolder = null;
    
    int position;
    
	@SuppressWarnings("deprecation")
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG,"onCreate");
		setContentView(R.layout.activity_upload_spot);
		
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.app_name);
        actionBar.setIcon(android.R.color.transparent);
        
        //actionBar.setBackgroundDrawable(new ColorDrawable(this.getResources().getColor(R.color.my_action_bar_color)));
		
		//findViewById(R.id.loadingPanel).setVisibility(View.GONE);
		spotImage 			= (ImageView) findViewById(R.id.spotImageUpload);
		editSpotDescription = (EditText) findViewById(R.id.editSpotDescription);
		frameLayoutVideo	= (FrameLayout) findViewById(R.id.videoSurfaceContainer);
		
		Intent intent = getIntent();
		
		mediaPath 		= intent.getStringExtra("SpotMedia");
		videoSurface	= (SurfaceView) findViewById(R.id.videoSurface);
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
			spotImage.setVisibility(View.GONE);
			frameLayoutVideo.setVisibility(View.VISIBLE);
			//frameLayoutVideo.setRotation(90);
	        try {
	        	mediaPlayer = new MediaPlayer(); 
	        	
	    		controller = new VideoControllerView(this);
	        	//controller.setRotation(180);
	    		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	        	mediaPlayer.setDataSource(UploadSpotActivity.this, Uri.parse(mediaPath));
	        	mediaPlayer.setOnPreparedListener(this);
	        	mediaPlayer.prepareAsync();
	        	//Log.d(TAG,"Mediaplayer="+mediaPath);
	        } catch (IllegalArgumentException e) {
	            e.printStackTrace();
	        } catch (SecurityException e) {
	            e.printStackTrace();
	        } catch (IllegalStateException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
		else{
			spotImage.setVisibility(View.VISIBLE);
			frameLayoutVideo.setVisibility(View.GONE);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize=8;      // 1/8 of original image
			bitmap = BitmapFactory.decodeFile(mediaPath,options);
			
			InputStream imageStream = null;
			try {
				imageStream = new FileInputStream(mediaPath);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			bitmap = ImageLoader.rotateBitmap(imageStream, mediaPath, bitmap);
			spotImage.setImageBitmap(bitmap);
		}
		
		SpinnerSpotType = (Spinner) findViewById(R.id.spinner_spottypes);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.spottype_upload, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		SpinnerSpotType.setAdapter(adapter);
		
		
        mUpdatesRequested = false;
        mPrefs = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        mEditor = mPrefs.edit();
        final Const constants = new Const();
        LocationResult locationResult = new LocationResult(){
		    @Override
		    public void gotLocation(Location location){
		    	constants.setLatitude(location.getLatitude());
		    	constants.setLatitude(location.getLongitude());

		    	Log.d(TAG,"getLocationACAs"+constants.getLatitude()+"  "+constants.getLongitude());
		    }
		};
		MyLocation myLocation = new MyLocation();
		if(constants.getLatitude() == 0 && constants.getLongitude() == 0){
			myLocation.getLocation(this, locationResult);
		}
        
	}
	
	
	/*
     * Called when the Activity is no longer visible at all.
     * Stop updates and disconnect.
     */
    @Override
    public void onStop() {
    	super.onStop();
    	
    }
	
    /*
     * Called when the Activity is going into the background.
     * Parts of the UI may be visible, but the Activity is inactive.
     */
    @Override
    public void onPause() {
    	super.onPause();
    	// Save the current setting for updates
        mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, mUpdatesRequested);
        mEditor.commit();
        if(frameLayoutVideo.isShown()){
	        if (mediaPlayer.isPlaying()) {  
	            //  
	            position=mediaPlayer.getCurrentPosition();  
	            mediaPlayer.stop();  
	        }
    	}
    }
	
    /*
     * Called when the Activity is restarted, even before it becomes visible.
     */
    @Override
    public void onStart() {
        super.onStart();

        /*
         * Connect the client. Don't re-start any requests here;
         * instead, wait for onResume()
         */
        
    }
    
    /*
     * Called when the system detects that this Activity is now visible.
     */
    @Override
    public void onResume() {
        super.onResume();
        Utils.setCurrentLocale(this);
        // If the app already has a setting for getting location updates, get it
        if (mPrefs.contains(LocationUtils.KEY_UPDATES_REQUESTED)) {
            mUpdatesRequested = mPrefs.getBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);

        // Otherwise, turn off location updates until requested
        } else {
            mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);
            mEditor.commit();
        }

    }
    
  
    @SuppressWarnings({ "unused", "deprecation" })
	private float getBitmapScalingFactor(Bitmap bm) {
        // Get display width from device
        int displayWidth = getWindowManager().getDefaultDisplay().getWidth();

        // Get margin to use it for calculating to max width of the ImageView
        LinearLayout.LayoutParams layoutParams = 
            (LinearLayout.LayoutParams)this.spotImage.getLayoutParams();
        int leftMargin = layoutParams.leftMargin;
        int rightMargin = layoutParams.rightMargin;

        // Calculate the max width of the imageView
        int imageViewWidth = displayWidth - (leftMargin + rightMargin);

        // Calculate scaling factor and return it
        return ( (float) imageViewWidth / (float) bm.getWidth() );
    }
    
    
    /*
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * LocationUpdateRemover and LocationUpdateRequester may call startResolutionForResult() to
     * start an Activity that handles Google Play services problems. The result of this
     * call returns here, to onActivityResult.
     */
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // Choose what to do based on the request code
        switch (requestCode) {

            // If the request code matches the code sent in onConnectionFailed
            case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :

                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:

                        // Log the result
                        Log.d(TAG, getString(R.string.resolved));

                        // Display the result
                        //mConnectionState.setText(R.string.connected);
                        //mConnectionStatus.setText(R.string.resolved);
                    break;

                    // If any other result was returned by Google Play services
                    default:
                        // Log the result
                        Log.d(TAG, getString(R.string.no_resolution));

                        // Display the result
                        //mConnectionState.setText(R.string.disconnected);
                        //mConnectionStatus.setText(R.string.no_resolution);

                    break;
                }

            // If any other request code was received
            default:
               // Report that this Activity received an unknown requestCode
               Log.d(TAG,
                       getString(R.string.unknown_activity_request_code, requestCode));

               break;
        }
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_uploadspot:
	        	//Hacer peticion de guardar
            	//Enviar imagen al servidor y registrar en la DB
	        	//final String spotName = editSpotName.getText().toString();
	        	final String spotDescription = editSpotDescription.getText().toString();
	        	final String spotType = SpinnerSpotType.getSelectedItem().toString();
	        	final long spotTypeId = SpinnerSpotType.getSelectedItemId();
	        	if(/*spotName != "" &&*/ spotDescription != "" && spotTypeId > 0){
	        		Intent intentUploadService = new Intent(UploadSpotActivity.this, UploadMediaService.class);
	        		intentUploadService.putExtra("imagepath", mediaPath);
	        		//intentUploadService.putExtra("spotname", spotName);
	        		intentUploadService.putExtra("spotdescription", spotDescription);
	        		intentUploadService.putExtra("spottypeId", ""+spotTypeId);
	        		intentUploadService.putExtra("spottype", spotType);
	        		intentUploadService.putExtra("userid", ""+User.current().getID());
	        		intentUploadService.putExtra("latitude", Const.currentLatitude);
	        		intentUploadService.putExtra("longitude", Const.currentLongitude);
	        		startService(intentUploadService);
	        		
	        		Intent intent = new Intent(UploadSpotActivity.this, MainActivity.class);
	        		intent.putExtra("loading",1);
					startActivity(intent);   
					overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
					finish();
	        	}
	        	else{
	        		Toast.makeText(UploadSpotActivity.this, "Debes llenar todos los campos", 
                            Toast.LENGTH_SHORT).show();
	        	}
	            return true;
	        default:
	        	onBackPressed();
	            return super.onOptionsItemSelected(item);
	    }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.uploadspot_menu, menu);
	    return super.onCreateOptionsMenu(menu);
	}

	public int uploadSpot(String sourceFileUri, String spotname, String spotdescription, long spottype,int userid) {

        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;  
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024; 
        StringBuilder serverResponse = null;
        BufferedReader reader = null;
        
        File sourceFile = new File(sourceFileUri); 
        Log.e(TAG,sourceFileUri);
        if (!sourceFile.isFile()) {
        	pDialog.dismiss(); 
            Log.e(TAG, "Source File not exist :" +mediaPath);
            return 0;
        }
        else
        {
            try { 

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection(); 
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("mediafile", fileName); 
                conn.setRequestProperty("name", spotname);
                conn.setRequestProperty("description", spotdescription );
                conn.setRequestProperty("type", ""+spottype);
                conn.setRequestProperty("latitude", ""+currentLat);
                conn.setRequestProperty("longitude", ""+currentLng);
                conn.setRequestProperty("userid", ""+userid);
                
                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd); 
                dos.writeBytes("Content-Disposition: form-data; name=\"mediafile\";filename="+ fileName + "" + lineEnd);
                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available(); 

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);  
                
                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);   

                }
                
                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();
                
                Log.i(TAG, "HTTP Response is : "+ serverResponseMessage + ": " + serverResponseCode);

                
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                serverResponse = new StringBuilder();
           
                String line = null;
                while ((line = reader.readLine()) != null){
                	serverResponse.append(line);
                }
                
                if(serverResponseCode == 200){

                    runOnUiThread(new Runnable() {
                        public void run() {

                            Toast.makeText(UploadSpotActivity.this, "File Upload Complete.", 
                                    Toast.LENGTH_SHORT).show();
                        }
                    });                
                }    

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();
            } catch (MalformedURLException ex) {

            	pDialog.dismiss();  
                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {

                        Toast.makeText(UploadSpotActivity.this, "MalformedURLException", 
                                Toast.LENGTH_SHORT).show();
                    }
                });

                Log.e(TAG, "error: " + ex.getMessage(), ex);  
            } catch (Exception e) {
            	pDialog.dismiss();  
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {

                        Toast.makeText(UploadSpotActivity.this, "Got Exception : see logcat ", 
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e(TAG, "Exception : "
                        + e.getMessage(), e);  
            }
            pDialog.dismiss();  
            Log.i(TAG, "READ "+serverResponse);
            
            
            return Integer.parseInt(serverResponse.toString()); 
        } // End else block 
    }

	
	/**
     * Show a dialog returned by Google Play services for the
     * connection error code
     *
     * @param errorCode An error code returned from onConnectionFailed
     */
    @SuppressWarnings("unused")
	private void showErrorDialog(final int errorCode) {

        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
            errorCode,
            this,
            LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

        	Log.d(TAG,"errorDialog");
        	
        }
    }
	/**
     * Define a DialogFragment to display the error dialog generated in
     * showErrorDialog.
     */
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /**
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
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
			mediaPlayer.start();
		}


		@Override
		public void pause() {
			// TODO Auto-generated method stub
			mediaPlayer.pause();
		}


		@Override
		public int getDuration() {
			// TODO Auto-generated method stub
			return mediaPlayer.getDuration();
		}


		@Override
		public int getCurrentPosition() {
			// TODO Auto-generated method stub
			return mediaPlayer.getCurrentPosition();
		}


		@Override
		public void seekTo(int pos) {
			// TODO Auto-generated method stub
			mediaPlayer.seekTo(pos);
		}


		@Override
		public boolean isPlaying() {
			// TODO Auto-generated method stub
			return mediaPlayer.isPlaying();
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
		mediaPlayer.setDisplay(videoSurface.getHolder());
    	controller.setMediaPlayer(mediaPlayerControl);
        controller.setAnchorView((FrameLayout) findViewById(R.id.videoSurfaceContainer));
        mediaPlayer.start();
	}

}