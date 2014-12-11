package com.spotz;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.facebook.AppEventsLogger;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.spotz.camera.VideoActivity;
import com.spotz.gen.R;
import com.spotz.utils.Const;
import com.spotz.utils.Settings;
import com.spotz.utils.Utils;
import com.spotz.utils.imaging.ImageMetadataReader;
import com.spotz.utils.imaging.ImageProcessingException;
import com.spotz.utils.metadata.Metadata;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.media.CamcorderProfile;
import android.media.ExifInterface;
import android.media.MediaRecorder;
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
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;


public class CameraActivity extends FragmentActivity implements ViewManager{

	
	// Intent request codes
    public static final int REQUEST_LOGIN = 4;
	static final int REQUEST_IMAGE_CAPTURE = 1;
	static final int REQUEST_TAKE_PHOTO = 1;
	
	public static final int MEDIA_TYPE_IMAGE = 1;
	private SurfaceView preview = null;
	private SurfaceHolder previewHolder = null;
	private Camera camera = null;
	private ImageButton buttonNextScreen;
	private ImageButton buttonTakeSpot;
	private ImageButton buttonChangeMedia;
	private ImageButton buttonChangeCamera;
	
	private boolean inPreview=false;
	private boolean cameraConfigured=false;
	String mCurrentPhotoPath;
	static String TAG = "CameraActivity";

	Intent mainIntent;
	final static int MEDIA_VIDEO = 2;
	final static int MEDIA_IMAGE = 1;
	int rotation = 0;
	static int currentCameraId = 0;
	
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
		
		buttonNextScreen 	= (ImageButton) findViewById(R.id.button_nextscreen);
		buttonTakeSpot 		= (ImageButton) findViewById(R.id.button_takespot);
		buttonChangeMedia	= (ImageButton) findViewById(R.id.changeMedia);
		buttonChangeCamera	= (ImageButton) findViewById(R.id.changeCamera);

		currentCameraId = getIntent().getIntExtra("orientation", Camera.CameraInfo.CAMERA_FACING_BACK);
        
		getActionBar().setBackgroundDrawable(new ColorDrawable(0xff1f8b1f));
		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setDisplayShowTitleEnabled(true);
		getActionBar().hide();
		
		preview=(SurfaceView)findViewById(R.id.camera_preview);
		previewHolder=preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		buttonNextScreen.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(CameraActivity.this, MainActivity.class);
				startActivity(intent);      
				overridePendingTransition( R.anim.slide_in_left, R.anim.slide_out_left );
				finish();
			}
		});


		buttonTakeSpot.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			public void onClick(View v) {
				camera.takePicture(null, null, mPicture);
			}
		});

		buttonChangeMedia.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent uploadSpotIntent = new Intent(CameraActivity.this, VideoActivity.class);
				Log.d(TAG,"Camera = "+currentCameraId);
				uploadSpotIntent.putExtra("orientation",currentCameraId);
				startActivity(uploadSpotIntent);
				overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
				finish();
			}
		});
		
		buttonChangeCamera.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			public void onClick(View v) {
				changeCameraOrientation();
			}
		});
	}

	
	@Override
	public void onStart() {
		super.onStart();
		if(Const.D) Log.e(TAG, "++ ON START ++");
		
		//Check if user is logged in, if not: go to LogIn Screen
	}

	public static void setCameraDisplayOrientation(Activity activity,
	         int cameraId, android.hardware.Camera camera) {
		
	     android.hardware.Camera.CameraInfo info =
	             new android.hardware.Camera.CameraInfo();
	     
	     android.hardware.Camera.getCameraInfo(cameraId, info);
	     
	     int rotation = activity.getWindowManager().getDefaultDisplay()
	             .getRotation();
	     
	     int degrees = 0;
	     switch (rotation) {
	         case Surface.ROTATION_0: degrees = 0; Log.d(TAG,"ACA = 0"); break;
	         case Surface.ROTATION_90: degrees = 90; Log.d(TAG,"ACA = 90"); break;
	         case Surface.ROTATION_180: degrees = 180; Log.d(TAG,"ACA = 180"); break;
	         case Surface.ROTATION_270: degrees = 270; Log.d(TAG,"ACA = 270"); break;
	     }

	     int result;
	     if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
	         result = (info.orientation + degrees) % 360;
	         result = (360 - result) % 360;  // compensate the mirror
	     } else {  // back-facing
	         result = (info.orientation - degrees + 360) % 360;
	     }
	     
	     camera.setDisplayOrientation(result);
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
			
			/*
			int rotation = CameraActivity.this.getWindowManager().getDefaultDisplay()
		             .getRotation();
			
			Log.d(TAG,"OrientationACAA = "+rotation);
			*/
			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			
			
			
			if (pictureFile == null){
				Log.d(TAG, "Error creating media file, check storage permissions: " );
				return;
			}
			try {
				
				Log.d(TAG, pictureFile.getAbsolutePath()+" -- "+rotation);
				Bitmap realImage = BitmapFactory.decodeByteArray(data, 0, data.length);
				
	            FileOutputStream fos = new FileOutputStream(pictureFile);
				realImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
				
				fos.write(data);
				fos.close();
			} catch (FileNotFoundException e) {
				Log.d(TAG, "File not found: " + e.getMessage());
			} catch (IOException e) {
				Log.d(TAG, "Error accessing file: " + e.getMessage());
			}
			
			ExifInterface exif;
			
			try {
				exif = new ExifInterface(pictureFile.getAbsolutePath());
				exif.setAttribute(ExifInterface.TAG_ORIENTATION,Integer.toString(rotation));
				exif.saveAttributes();
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//Log.d(TAG,""+pictureFile.getAbsolutePath());
			Intent uploadSpotIntent = new Intent(CameraActivity.this, UploadSpotActivity.class);
			uploadSpotIntent.putExtra("SpotMedia",pictureFile.getAbsolutePath());
			startActivity(uploadSpotIntent);
			overridePendingTransition( R.anim.slide_in_left, R.anim.slide_out_left );
			//finish();
		}
	};

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.
		File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "Spotz");
		
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
		} else {
			return null;
		}

		return mediaFile;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG,"onResume");
		
		if(currentCameraId == 1){
			Log.d(TAG,""+findFrontFacingCamera());
			camera = Camera.open(findFrontFacingCamera());
		}else{
			camera=Camera.open();
		}
		if (cameraConfigured && camera!=null) {
			camera.startPreview();
			inPreview=true;
		}
		
		AppEventsLogger.activateApp(this);
	}

	@Override
	public void onPause() {
		if (inPreview) {
			camera.stopPreview();
		}
		camera.release();
		camera		= null;
		inPreview	= false;
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
	        
			if(currentCameraId == 1){
		        if(display.getRotation() == Surface.ROTATION_0){ 
		        	rotation = 90;
		            
		            //Log.d(TAG,"ROTATION 0");
		        }
		        /*
		        if(display.getRotation() == Surface.ROTATION_90){
		        	Log.d(TAG,"ROTATION 90");
		        }
		        if(display.getRotation() == Surface.ROTATION_180){
		        	Log.d(TAG,"ROTATION 180");
		        }
		        */
		        if(display.getRotation() == Surface.ROTATION_270){
		        	rotation = 180;
		            //Log.d(TAG,"ROTATION 270");
		        }
			}else{
		        if(display.getRotation() == Surface.ROTATION_0){ 
		        	rotation = 90;
		            //Log.d(TAG,"ROTATION 0");
		        }
		        /*
		        if(display.getRotation() == Surface.ROTATION_90 ){
		            Log.d(TAG,"ROTATION 90");
		        }
		        if(display.getRotation() == Surface.ROTATION_180){
		        	Log.d(TAG,"ROTATION 180");
		        }
		        */
		        if(display.getRotation() == Surface.ROTATION_270){
		        	rotation = 180;
		            //Log.d(TAG,"ROTATION 270");
		        }
			}
			camera.setDisplayOrientation(rotation);
			parameters.setPreviewSize(height, width);
			parameters.setRotation(rotation);
			camera.setParameters(parameters);
	        initPreview(width, height);
	        previewCamera();   
			Log.d(TAG,"surfaceChanged");
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// no-op
		}
	};
	
	public void previewCamera(){        
	    try {           
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
	
	public void changeCameraOrientation(){
		if (inPreview) {
		    camera.stopPreview();
		}
		//NB: if you don't release the current camera before switching, you app will crash
		camera.release();

		//swap the id of the camera to be used
		if(currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
		    currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
		}
		else {
		    currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
		}
		camera = Camera.open(currentCameraId);

		setCameraDisplayOrientation(CameraActivity.this, currentCameraId, camera);
		try {
		    camera.setPreviewDisplay(previewHolder);
		} catch (IOException e) {
		    e.printStackTrace();
		}
		camera.startPreview();
	}
	
	private int findFrontFacingCamera() {
	    int cameraId = -1;
	    // Search for the front facing camera
	    int numberOfCameras = Camera.getNumberOfCameras();
	    for (int i = 0; i < numberOfCameras; i++) {
	      CameraInfo info = new CameraInfo();
	      Camera.getCameraInfo(i, info);
	      if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
	        Log.d(TAG, "Camera found"+i);
	        cameraId = i;
	        break;
	      }
	    }
	    return cameraId;
	  }
}