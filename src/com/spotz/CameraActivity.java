package com.spotz;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.example.androidhive.R;
import com.facebook.AppEventsLogger;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.spotz.utils.Const;
import com.spotz.utils.Settings;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.Toast;


public class CameraActivity extends FragmentActivity implements ViewManager{

	// Intent request codes
    public static final int REQUEST_LOGIN = 4;
	static final int REQUEST_IMAGE_CAPTURE = 1;
	static final int REQUEST_TAKE_PHOTO = 1;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	private SurfaceView preview=null;
	private SurfaceHolder previewHolder=null;
	private Camera camera=null;
	private Button buttonNextScreen;
	private Button buttonTakeSpot;
	private boolean inPreview=false;
	private boolean cameraConfigured=false;
	String mCurrentPhotoPath;
	static String TAG = "CameraActivity";

	Intent mainIntent;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		Log.d(TAG,"onCreate");
		setContentView(R.layout.spot_camera);
		
		if(Const.height == 0){
			Const.height = getWindowManager().getDefaultDisplay().getHeight();
		}
		
		if(Const.width == 0){
			Const.width = getWindowManager().getDefaultDisplay().getWidth();
		}
		
	}

	
	@Override
	public void onStart() {
		super.onStart();
		if(Const.D) Log.e(TAG, "++ ON START ++");
		//Check if user is logged in, if not: go to LogIn Screen
		
		
		getActionBar().setBackgroundDrawable(new ColorDrawable(0xff1f8b1f));
		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setDisplayShowTitleEnabled(true);
		getActionBar().hide();
		preview=(SurfaceView)findViewById(R.id.camera_preview);
		previewHolder=preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		buttonNextScreen = (Button) findViewById(R.id.button_nextscreen);
		buttonTakeSpot = (Button) findViewById(R.id.button_takespot);


		buttonNextScreen.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(CameraActivity.this, MainActivity.class);
				startActivity(intent);      
				finish();
				
			}
		});


		buttonTakeSpot.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				camera.takePicture(null, null, mPicture);
				
			}
		});

		
	}

    @Override
    public void onStop() {
        super.onStop();
        Const.v(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Const.v(TAG, "--- ON DESTROY ---");
    }
    
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(Const.D) Log.d(TAG, "onActivityResult reqCode= "+requestCode+ " res = " + resultCode);
        switch (requestCode) {
       
        case REQUEST_LOGIN:
        	if (resultCode == LoginActivity.RESULT_OK){
        		Settings.getIns(this).getPref().edit().putBoolean(Settings.USER_ID, true).commit();
        		if(Const.D)Log.d(TAG, "OnActivityResult - User Logged in successfully");
        	} else {
        		if(Const.D)Log.d(TAG, "OnActivityResult - User didn't Logged in");
        		finish();
        	} 
        	break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			if (pictureFile == null){
				Log.d(TAG, "Error creating media file, check storage permissions: " );
				return;
			}

			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();


			} catch (FileNotFoundException e) {
				Log.d(TAG, "File not found: " + e.getMessage());
			} catch (IOException e) {
				Log.d(TAG, "Error accessing file: " + e.getMessage());
			}

			//Log.d(TAG,""+pictureFile.getAbsolutePath());
			Intent uploadSpotIntent = new Intent(CameraActivity.this, UploadSpotActivity.class);
			uploadSpotIntent.putExtra("SpotMedia",pictureFile.getAbsolutePath());
			startActivity(uploadSpotIntent);
			finish();
		}
	};

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), "Spotz");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (! mediaStorageDir.exists()){
			if (! mediaStorageDir.mkdirs()){
				Log.d(TAG, "failed to create directory");
				return null;
			}
		}
		
		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE){
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"IMG_"+ timeStamp + ".jpg");
		} else if(type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"VID_"+ timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		camera=Camera.open();
		startPreview();
		AppEventsLogger.activateApp(this);
	}

	@Override
	public void onPause() {
		if (inPreview) {
			camera.stopPreview();
		}
		camera.release();
		camera=null;
		inPreview=false;
		super.onPause();
		AppEventsLogger.deactivateApp(this);
	}

	private Camera.Size getBestPreviewSize(int width, int height,
			Camera.Parameters parameters) {
		Camera.Size result=null;
		//camera.setDisplayOrientation(90);
		
		
		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
			if (size.width<=width && size.height<=height) {
				if (result==null) {
					result=size;
				}
				else {
					int resultArea=result.width*result.height;
					int newArea=size.width*size.height;

					if (newArea>resultArea) {
						result=size;
					}
				}
			}
		}

		return(result);
	}

	private void initPreview(int width, int height) {
		
		if (camera!=null && previewHolder.getSurface()!=null) {
			try {
				camera.setPreviewDisplay(previewHolder);
			}
			catch (Throwable t) {
				Log.e("PreviewDemo-surfaceCallback",
						"Exception in setPreviewDisplay()", t);
				Toast
				.makeText(CameraActivity.this, t.getMessage(), Toast.LENGTH_LONG)
				.show();
			}

			if (!cameraConfigured) {
				Camera.Parameters parameters=camera.getParameters();
				Camera.Size size=getBestPreviewSize(width, height,
						parameters);

				if (size!=null) {
					parameters.setPreviewSize(size.width, size.height);
					camera.setParameters(parameters);
					cameraConfigured=true;
				}
			}
		}
	}

	private void startPreview() {
		if (cameraConfigured && camera!=null) {
			camera.startPreview();
			inPreview=true;
		}
	}

	SurfaceHolder.Callback surfaceCallback=new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			// no-op -- wait until surfaceChanged()
		}

		public void surfaceChanged(SurfaceHolder holder,
				int format, int width,
				int height) {
			if (inPreview){
				camera.stopPreview();
	        }
	        Parameters parameters = camera.getParameters();
	        Display display = getWindowManager().getDefaultDisplay();
	        if(display.getRotation() == Surface.ROTATION_0){
	            parameters.setPreviewSize(height, width);                           
	            camera.setDisplayOrientation(90);
	        }

	        if(display.getRotation() == Surface.ROTATION_90){
	            parameters.setPreviewSize(width, height);                           
	        }

	        if(display.getRotation() == Surface.ROTATION_180){
	            parameters.setPreviewSize(height, width);               
	        }

	        if(display.getRotation() == Surface.ROTATION_270){
	            parameters.setPreviewSize(width, height);
	            camera.setDisplayOrientation(180);
	        }

	        camera.setParameters(parameters);
	        //startPreview();
	        initPreview(width, height);
	        previewCamera();   
			
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// no-op
		}
	};
	
	public void previewCamera()
	{        
	    try 
	    {           
	        camera.setPreviewDisplay(previewHolder);          
	        camera.startPreview();
	        inPreview = true;
	    }
	    catch(Exception e)
	    {
	        Log.d(TAG, "Cannot start preview", e);    
	    }
	}
	@Override
	public void addView(View view, LayoutParams params) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void updateViewLayout(View view, LayoutParams params) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void removeView(View view) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedState) {
		super.onSaveInstanceState(savedState);
	}
	
}