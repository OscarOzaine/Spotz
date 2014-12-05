package com.spotz.camera;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.androidhive.R;
import com.facebook.AppEventsLogger;
import com.spotz.CameraActivity;
import com.spotz.MainActivity;
import com.spotz.UploadSpotActivity;
import com.spotz.utils.Const;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;



public class VideoActivity extends FragmentActivity implements ViewManager{

	private SurfaceView preview = null;
	
	MediaRecorder mediaRecorder;
	SurfaceHolder surfaceHolder;
	boolean recording;
	static String TAG = "VideoActivity";
	private ImageButton buttonTakeSpot, buttonChangeCamera, buttonNextScreen, buttonChangeMedia;
	private Camera camera = null;
	private boolean inPreview=false;
	private boolean cameraConfigured=false;
	
	final static int MEDIA_VIDEO = 2;
	final static int MEDIA_IMAGE = 1;
	
	static int currentCameraId = 0;
	private int currentFormat = 0;
	private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
	private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
	
	private int output_formats[] = { MediaRecorder.OutputFormat.MPEG_4,
	        MediaRecorder.OutputFormat.THREE_GPP };
	private String file_exts[] = 
		{ AUDIO_RECORDER_FILE_EXT_MP4,
		  AUDIO_RECORDER_FILE_EXT_3GP 
		  };
	
	String fileName = "";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        
        Log.d(TAG,"Video = "+getIntent().getIntExtra("orientation", Camera.CameraInfo.CAMERA_FACING_BACK));
        currentCameraId = getIntent().getIntExtra("orientation", Camera.CameraInfo.CAMERA_FACING_BACK);
        
        recording = false;
        getActionBar().setBackgroundDrawable(new ColorDrawable(0xff1f8b1f));
		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setDisplayShowTitleEnabled(true);
		getActionBar().hide();
        setContentView(R.layout.spot_camera);
        preview 		= (SurfaceView)findViewById(R.id.camera_preview);
        surfaceHolder	= preview.getHolder();
        surfaceHolder.addCallback(surfaceCallback);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        buttonTakeSpot 		= (ImageButton) findViewById(R.id.button_takespot);
        buttonChangeCamera	= (ImageButton) findViewById(R.id.changeCamera);
        buttonNextScreen 	= (ImageButton) findViewById(R.id.button_nextscreen);
		buttonChangeMedia	= (ImageButton) findViewById(R.id.changeMedia);
		
		buttonTakeSpot.setImageResource(R.drawable.ic_notrecording);
		
		buttonNextScreen.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(VideoActivity.this, MainActivity.class);
				startActivity(intent);      
				finish();
			}
		});
		
        buttonTakeSpot.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			public void onClick(View v) {
				if(recording){
					mediaRecorder.stop();
					mediaRecorder.release();
					//Thread stopThread = new Thread (new stopRecording (mediaRecorder));
					//stopThread.start();
					recording = false;
					camera.stopPreview();
					camera.unlock();
					Intent uploadSpotIntent = new Intent(VideoActivity.this, UploadSpotActivity.class);
					uploadSpotIntent.putExtra("SpotMedia",fileName);
					startActivity(uploadSpotIntent);
					overridePendingTransition( R.anim.slide_in_left, R.anim.slide_out_left );
					//finish();
				}else{
					if(mediaRecorder == null){
						mediaRecorder = new MediaRecorder();
				        initMediaRecorder();
					}
					// The following two lines should precede setAudioSource line
					startMediaRecorder();
					recording = true;
					buttonTakeSpot.setImageResource(R.drawable.ic_recording);
					
				}
			}
		});
        
        buttonChangeCamera.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			public void onClick(View v) {
				changeCameraOrientation();
			}
		});
        
        
        buttonChangeMedia.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent uploadSpotIntent = new Intent(VideoActivity.this, CameraActivity.class);
				Log.d(TAG,"Camera = "+currentCameraId);
				uploadSpotIntent.putExtra("orientation",currentCameraId);
				startActivity(uploadSpotIntent);
				overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
				finish();
			}
		});
    }
    
	private void initMediaRecorder(){
		camera.stopPreview();
		camera.unlock();
		mediaRecorder.reset();
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setCamera(camera);
		mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		//mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        
        fileName = getFilename();
        
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
        
        mediaRecorder.setProfile(profile);
        //mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        /*
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        mediaRecorder.setVideoSize(displaymetrics.widthPixels, displaymetrics.heightPixels);
        */
        //mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        //mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
        
        //mediaRecorder.setOutputFile("/sdcard/myvideo"+timeStamp+".mp4");
        mediaRecorder.setOutputFile(fileName);
        
        mediaRecorder.setMaxDuration(60000); // Set max duration 60 sec.
        mediaRecorder.setMaxFileSize(50000000); // Set max file size 50M
        mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface()); 
        mediaRecorder.setOrientationHint(270);
	}
	
	/*
	private void stopRecording(){
		if (mediaRecorder != null){
            mediaRecorder.stop();
            mediaRecorder.release();
		}
	}
	*/
	private String getFilename() {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    //String filepath = Environment.getExternalStorageDirectory().getPath();
	    String filepath =Environment.getExternalStorageDirectory() + File.separator 
	            + "Spotz" + File.separator;
	    File file = new File(Environment.getExternalStorageDirectory(), "Spotz");
	    if (!file.exists()) {
	        file.mkdirs();
	    }
	    return (filepath + timeStamp + file_exts[currentFormat]);
	}
	
	private void startMediaRecorder(){
		try {
			mediaRecorder.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mediaRecorder.start();
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
	
	SurfaceHolder.Callback surfaceCallback=new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			// no-op -- wait until surfaceChanged()
			Log.d(TAG,"surfaceCreated");
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
		            //camera.setDisplayOrientation(90);
		        }
		        if(display.getRotation() == Surface.ROTATION_270){
		            //camera.setDisplayOrientation(180);
		        }
			}else{
		        if(display.getRotation() == Surface.ROTATION_0){
		            parameters.setPreviewSize(height, width);                           
		            //camera.setDisplayOrientation(90);
		        }
		        if(display.getRotation() == Surface.ROTATION_90 || display.getRotation() == Surface.ROTATION_180){
		            parameters.setPreviewSize(width, height);                           
		        }

		        if(display.getRotation() == Surface.ROTATION_270){
		            parameters.setPreviewSize(width, height);
		            //camera.setDisplayOrientation(180);
		        }
			}
			camera.setParameters(parameters);
	        
	        //startPreview();
	        initPreview(width, height);
	        previewCamera();   
	        Log.d(TAG,"surfaceChanged");
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// no-op
			Log.d(TAG,"surfaceDestroyed");
		}
	};
	
	@Override
	public void onResume() {
		super.onResume();
		if(currentCameraId == 1){
			Log.d(TAG,""+findFrontFacingCamera());
			camera = Camera.open(findFrontFacingCamera());
		}else{
			camera=Camera.open();
		}
		
		startPreview();
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
	}

	@Override
    public void onDestroy() {
        super.onDestroy();
        //stopRecording(mediaRecorder);
        Const.v(TAG, "--- ON DESTROY ---");
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
		if (camera!=null && surfaceHolder.getSurface()!=null) {
			try {
				camera.setPreviewDisplay(surfaceHolder);
			}
			catch (Throwable t) {
				Log.e("PreviewDemo-surfaceCallback",
						"Exception in setPreviewDisplay()", t);
				Toast
				.makeText(VideoActivity.this, t.getMessage(), Toast.LENGTH_LONG)
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
			setCameraDisplayOrientation(VideoActivity.this,  currentCameraId,camera);
		}
	}

	private void startPreview() {
		if (cameraConfigured && camera!=null) {
			camera.startPreview();
			inPreview=true;
		}
	}

	public void previewCamera(){        
	    try {
	        camera.setPreviewDisplay(surfaceHolder);          
	        camera.startPreview();
	        inPreview = true;
	    }
	    catch(Exception e){
	        Log.d(TAG, "Cannot start preview", e);    
	    }
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
	         case Surface.ROTATION_0: degrees = 0; break;
	         case Surface.ROTATION_90: degrees = 90; break;
	         case Surface.ROTATION_180: degrees = 180; break;
	         case Surface.ROTATION_270: degrees = 270; break;
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
	
	private void stopRecording(MediaRecorder mediaRecorder){
		if (null != mediaRecorder) {
			try {
				mediaRecorder.stop();
			} catch(RuntimeException e) {
			    //mFile.delete();  //you must delete the outputfile when the recorder stop failed.
			} finally {
				mediaRecorder.release();
				mediaRecorder = null;
			}
			/*
		   try{     
			   mediaRecorder.stop();
			   mediaRecorder.release();
		   }catch(RuntimeException ex){
			   //Ignore
		   }
		   */
		}
		/*
        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();
        */
        Log.d(TAG,"Stopped");
	}
	
	 public class stopRecording implements Runnable {
		 private MediaRecorder recorder ;
		 
		 public stopRecording(MediaRecorder mediaRecorder) {
			 Log.i("Media", "Stop in Cos");
			 
		     try {
		    	 recorder = mediaRecorder; }
		     catch ( Exception e ){       
		         Log.i("Media", "Stop out  Cos" + e.getMessage()) ;
		     }
		}

		public void run() {
		     Log.i("Media", "Stop in RUN");
	    	 mediaRecorder.reset();
	    	 Log.i("Media", "Stop in RUN2");
	    	 //mFile.delete();  //you must delete the outputfile when the recorder stop failed.
	    	 //Log.d(TAG,"RuntimeException");
	    	 mediaRecorder.release();
	    	 mediaRecorder = null;
		     /*
		     try{     
		    	 mediaRecorder.stop();
		    	 mediaRecorder.release();
		     }catch(RuntimeException ex){
		    	 //Ignore
		     }
		     */
		     //stopRecording(recorder);
		     Log.i("Media", "Stop out of RUN");
		 }
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

		setCameraDisplayOrientation(VideoActivity.this, currentCameraId, camera);
		try {
		    camera.setPreviewDisplay(surfaceHolder);
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