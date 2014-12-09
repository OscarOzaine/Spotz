package com.spotz;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.spotz.gen.R;
import com.spotz.location.LocationUtils;
import com.spotz.services.UploadMediaService;
import com.spotz.users.User;
import com.spotz.utils.Utils;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class UploadSpotActivity extends Activity implements
			LocationListener,
			GooglePlayServicesClient.ConnectionCallbacks,
			GooglePlayServicesClient.OnConnectionFailedListener, 
			com.google.android.gms.location.LocationListener {

	// A request to connect to Location Services
	private LocationRequest mLocationRequest;
	
	// Stores the current instantiation of the location client in this object
	private LocationClient mLocationClient;

	
	// All xml labels
	TextView txtName, txtType, txtDescription;
	EditText editSpotName, editSpotDescription;
	Spinner SpinnerSpotType, spinnerSpotType;
	ImageView spotImage;
	VideoView spotVideo;
	String currentLat = "", currentLng = "";
	
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
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG,"onCreate");
		setContentView(R.layout.activity_upload_spot);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.app_name);
        actionBar.setBackgroundDrawable(new ColorDrawable(0xff1f8b1f));
		
		//findViewById(R.id.loadingPanel).setVisibility(View.GONE);
		spotImage = (ImageView) findViewById(R.id.spotImageUpload);
		spotVideo = (VideoView) findViewById(R.id.spotVideoUpload);
		editSpotName = (EditText) findViewById(R.id.editSpotName);
		editSpotDescription = (EditText) findViewById(R.id.editSpotDescription);
		SpinnerSpotType = (Spinner) findViewById(R.id.spinner_spottypes);
		
		Intent intent = getIntent();
		
		mediaPath = intent.getStringExtra("SpotMedia");
		Log.d(TAG,"imagepath = "+mediaPath);
		if(Utils.isVideo(mediaPath)){
			spotImage.setVisibility(View.GONE);
			
			spotVideo.setVisibility(View.VISIBLE);
			
			
			MediaController mediaController= new MediaController(this);
		    mediaController.setAnchorView(spotVideo);
	/*	    
		    DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int videoheight = displaymetrics.heightPixels;
            int videowidth = displaymetrics.widthPixels;
            int left = spotVideo.getLeft();
            int top = spotVideo.getTop();
            int right = left + (videowidth);
            int bottom = top + (videoheight);
            
            
		    AbsoluteLayout.LayoutParams params = (AbsoluteLayout.LayoutParams) spotVideo.getLayoutParams();
		    params.width = videowidth;
		    params.height = videoheight;
		    params.x = left;
		    params.y = top;
		    spotVideo.requestLayout();
*/
		    spotVideo.setVisibility(View.VISIBLE);
		    spotVideo.setFocusable(true);
		    spotVideo.setFocusableInTouchMode(true);
		    spotVideo.requestFocus();

		   /*
		    DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int videoheight = displaymetrics.heightPixels;
            int videowidth = displaymetrics.widthPixels;
            int left = spotVideo.getLeft();
            int top = spotVideo.getTop();
            int right = left + (videowidth);
            int bottom = top + (videoheight);
            
            spotVideo.layout(left, top, right, bottom);
            */
            
		    Uri uri=Uri.parse(mediaPath);        
		    spotVideo.setMediaController(mediaController);
		    spotVideo.setVideoURI(uri);        
		    spotVideo.requestFocus();
		    spotVideo.start();
		}
		else{
			spotVideo.setVisibility(View.GONE);
			spotImage.setVisibility(View.VISIBLE);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize=8;      // 1/8 of original image
			bitmap = BitmapFactory.decodeFile(mediaPath,options);
			/*
			Matrix mat = new Matrix();
	        mat.postRotate(90);
	        Bitmap bMapRotate = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);
	        // Get scaling factor to fit the max possible width of the ImageView
	        float scalingFactor = getBitmapScalingFactor(bMapRotate);
	        // Create a new bitmap with the scaling factor
	        Bitmap newBitmap = Utils.ScaleBitmap(bMapRotate, scalingFactor);
	        */
	        // Set the bitmap as the ImageView source
	        //spotMedia.setImageBitmap(newBitmap);
	        spotImage.setImageBitmap(bitmap);
		}
		
		
		spinnerSpotType = (Spinner) findViewById(R.id.spinner_spottypes);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.spottype_array, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinnerSpotType.setAdapter(adapter);
		
		
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);
        mUpdatesRequested = false;
        mPrefs = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        mEditor = mPrefs.edit();
        mLocationClient = new LocationClient(this, this, this);
	}
	
	
	/*
     * Called when the Activity is no longer visible at all.
     * Stop updates and disconnect.
     */
    @Override
    public void onStop() {
        // If the client is connected
        if (mLocationClient.isConnected()) {
            stopPeriodicUpdates();
        }
        // After disconnect() is called, the client is considered "dead".
        mLocationClient.disconnect();
        //bitmap.recycle();
        super.onStop();
    }
	
    /*
     * Called when the Activity is going into the background.
     * Parts of the UI may be visible, but the Activity is inactive.
     */
    @Override
    public void onPause() {
        // Save the current setting for updates
        mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, mUpdatesRequested);
        mEditor.commit();
        super.onPause();
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
        mLocationClient.connect();
    }
    
    /*
     * Called when the system detects that this Activity is now visible.
     */
    @Override
    public void onResume() {
        super.onResume();

        // If the app already has a setting for getting location updates, get it
        if (mPrefs.contains(LocationUtils.KEY_UPDATES_REQUESTED)) {
            mUpdatesRequested = mPrefs.getBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);

        // Otherwise, turn off location updates until requested
        } else {
            mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);
            mEditor.commit();
        }

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
    
    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(TAG, getString(R.string.play_services_available));

            // Continue
            return true;
        // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
            	Log.d(TAG,"ErrorDialog");
            	/*
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getFragmentManager(), TAG);
                */
            }
            return false;
        }
    }

    /**
     * Invoked by the "Get Location" button.
     *
     * Calls getLastLocation() to get the current location
     *
     * @param v The view object associated with this method, in this case a Button.
     */
    public void getLocation(View v) {

        // If Google Play Services is available
        if (servicesConnected()) {

            // Get the current location
            Location currentLocation = mLocationClient.getLastLocation();

            // Display the current location in the UI
            //Log.d(TAG,"Location = "+LocationUtils.getLatLng(this, currentLocation));
            //mLatLng.setText(LocationUtils.getLatLng(this, currentLocation));
        }
    }

    /**
     * Invoked by the "Start Updates" button
     * Sends a request to start location updates
     *
     * @param v The view object associated with this method, in this case a Button.
     */
    public void startUpdates(View v) {
        mUpdatesRequested = true;

        if (servicesConnected()) {
            startPeriodicUpdates();
        }
    }

    /**
     * Invoked by the "Stop Updates" button
     * Sends a request to remove location updates
     * request them.
     *
     * @param v The view object associated with this method, in this case a Button.
     */
    public void stopUpdates(View v) {
        mUpdatesRequested = false;

        if (servicesConnected()) {
            stopPeriodicUpdates();
        }
    }
    
    /**
     * In response to a request to start updates, send a request
     * to Location Services
     */
    private void startPeriodicUpdates() {

        mLocationClient.requestLocationUpdates(mLocationRequest, this);
        //mConnectionState.setText(R.string.location_requested);
    }

    /**
     * In response to a request to stop updates, send a request to
     * Location Services
     */
    private void stopPeriodicUpdates() {
        mLocationClient.removeLocationUpdates(this);
        
        Log.d(TAG,getString(R.string.location_updates_stopped));
        
        //mConnectionState.setText(R.string.location_updates_stopped);
    }

    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_uploadspot:
	        	//Hacer peticion de guardar
            	//Enviar imagen al servidor y registrar en la DB
	        	final String spotName = editSpotName.getText().toString();
	        	final String spotDescription = editSpotDescription.getText().toString();
	        	final long spotTypeId = SpinnerSpotType.getSelectedItemId();
	    		
	        	if(spotName != "" && spotDescription != "" && spotTypeId != 0){
	        		Log.d(TAG, "Uploaddd");
	        		
	        		Intent intentUploadService = new Intent(UploadSpotActivity.this, UploadMediaService.class);
	        		intentUploadService.putExtra("imagepath", mediaPath);
	        		intentUploadService.putExtra("spotname", spotName);
	        		intentUploadService.putExtra("spotdescription", spotDescription);
	        		intentUploadService.putExtra("spottypeId", ""+spotTypeId);
	        		intentUploadService.putExtra("userid", ""+User.current().getID());
	        		intentUploadService.putExtra("latitude", currentLat);
	        		intentUploadService.putExtra("longitude", currentLng);
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

	/*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */

            } catch (IntentSender.SendIntentException e) {

                // Log the error
                e.printStackTrace();
            }
        } else {

            // If no resolution is available, display a dialog to the user with the error.
            showErrorDialog(connectionResult.getErrorCode());
        }
    }
    
	@Override
	public void onConnected(Bundle arg0) {
		Log.d(TAG,getString(R.string.connected));
		//mConnectionStatus.setText(R.string.connected);
		
        if (mUpdatesRequested) {
            startPeriodicUpdates();
        }
        
        // Get the current location
        Location currentLocation = mLocationClient.getLastLocation();

        currentLat = LocationUtils.getLat(this, currentLocation);
        currentLng = LocationUtils.getLng(this, currentLocation);
        
        // Display the current location in the UI
        //Log.d(TAG,"Location = "+LocationUtils.getLatLng(this, currentLocation));
        //mLatLng.setText(LocationUtils.getLatLng(this, currentLocation));
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		Log.d(TAG,getString(R.string.disconnected));
		//mConnectionStatus.setText(R.string.disconnected);
	}

	@Override
	public void onLocationChanged(Location location) {
		// Report to the UI that the location was updated
        
		currentLat = LocationUtils.getLat(this, location);
        currentLng = LocationUtils.getLng(this, location);
        
		Log.d(TAG,getString(R.string.location_updated));
        //mConnectionStatus.setText(R.string.location_updated);

        //Log.d(TAG,LocationUtils.getLatLng(this, location));
        // In the UI, set the latitude and longitude to the value received
        //mLatLng.setText(LocationUtils.getLatLng(this, location));
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	} 
	
	/**
     * Show a dialog returned by Google Play services for the
     * connection error code
     *
     * @param errorCode An error code returned from onConnectionFailed
     */
    private void showErrorDialog(int errorCode) {

        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
            errorCode,
            this,
            LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

        	Log.d(TAG,"errorDialog");
        	/*
            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();
            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);
            // Show the error dialog in the DialogFragment
            errorFragment.show(getFragmentManager(), TAG);
            */
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
	
    
}