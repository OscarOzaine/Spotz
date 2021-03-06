package com.spotz.services;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.spotz.database.Spot;
import com.spotz.database.SpotsHelper;
import com.spotz.utils.Const;
import com.spotz.utils.Utils;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class UploadMediaService extends IntentService  {
	
	String imagePath, spotName, spotDescription, spotTypeId, userId, latitude, longitude, spotType;
	String dbSpotID;
	String upLoadServerUri = "http://api.myhotspotz.net/app/uploadSpot";
	int serverResponseCode = 0;
	
	public static final String NOTIFICATION = "com.spotz.MainActivity";
	
	public boolean internetConection = false;
	SpotsHelper db = null;
	
	public UploadMediaService() {
		super("UploadMediaService");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Utils util = new Utils(this);
    	if(util.isOnline()){
    		internetConection = true;
    	}
    	else{
    		internetConection = false;
    	}
	    //Log.d("UploadMediaService", "onStartCommand");
    	dbSpotID 		= intent.getStringExtra("dbspotid");
	    imagePath		= intent.getStringExtra("imagepath");
	    //spotName		= intent.getStringExtra("spotname");
	    spotDescription	= intent.getStringExtra("spotdescription");
	    spotTypeId		= intent.getStringExtra("spottypeId");
	    spotType		= intent.getStringExtra("spottype");
	    userId			= intent.getStringExtra("userid");
	    latitude		= intent.getStringExtra("latitude");
	    longitude		= intent.getStringExtra("longitude");
	    return super.onStartCommand(intent,flags,startId);
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		// Normally we would do some work here, like download a file.
	      // For our sample, we just sleep for 5 seconds.
		
		int result = uploadSpot(imagePath, spotName, spotDescription, 
				spotType,spotTypeId, userId,latitude, longitude);
		publishResults(result,dbSpotID);
	}

	  
	public int uploadSpot(String sourceFileUri, String spotname, String spotdescription, 
						String spottype,String spottypeid, String userid, String latitude, String longitude) {

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
        if(Const.D) Log.e(Const.TAG,sourceFileUri);
        if (!sourceFile.isFile()) {
        	if(Const.D) Log.e(Const.TAG, "Source File not exist :" +imagePath);
            return 0;
        }
        else
        {
        	if(internetConection){
        		try { 
        			Const constants = new Const();
                    // open a URL connection to the Servlet
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL(upLoadServerUri);
                    //Log.d(Const.TAG,"URL = "+upLoadServerUri );
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
                    conn.setRequestProperty("type", ""+spottypeid);
                    conn.setRequestProperty("latitude", ""+constants.getLatitude());
                    conn.setRequestProperty("longitude", ""+constants.getLongitude());
                    conn.setRequestProperty("userid", ""+userid);
                    
                    Log.d("UploadMediaService","spottype"+spottypeid);
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
                    
                    if(Const.D) Log.i(Const.TAG, "HTTP Response is : "+ serverResponseMessage + ": " + serverResponseCode);

                    
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    serverResponse = new StringBuilder();
               
                    String line = null;
                    while ((line = reader.readLine()) != null){
                    	serverResponse.append(line);
                    }
                    
                    if(serverResponseCode == 200){
                    	if(Const.D) Log.d(Const.TAG,"File Upload Complete.");
                    }
                    
                    //close the streams //
                    fileInputStream.close();
                    dos.flush();
                    dos.close();
                } catch (MalformedURLException ex) {

                	ex.printStackTrace();
                	if(Const.D) Log.d(Const.TAG,"MalformedURLException");
                	if(Const.D) Log.e(Const.TAG, "error: " + ex.getMessage(), ex);  
                } catch (Exception e) {
                	e.printStackTrace();

                	if(Const.D) Log.d(Const.TAG,"Got Exception : see logcat");
                	if(Const.D) Log.e(Const.TAG, "Exception : "+ e.getMessage(), e); 
                }
        		if(Const.D) Log.d(Const.TAG, "READ "+serverResponse);
                
                if(Utils.isNumeric(serverResponse.toString())){
                	return Integer.parseInt(serverResponse.toString());
                }else{
                	if(Const.D) Log.d(Const.TAG,"Error: "+serverResponse.toString());
                	return Integer.parseInt(serverResponse.toString()); 
                }
        	}
        	else{
        		db = new SpotsHelper(this);
        		db.addSpot(new Spot(spotname, spotdescription,spottype,spottypeid,latitude,longitude,userid,sourceFileUri));
        		
        		return 2;
        	}
            
        } // End else block 
    }	
	
	private void publishResults(int result,String dbSpotId) {
	    Intent intent = new Intent(NOTIFICATION);
	    intent.putExtra("result", result);
	    intent.putExtra("dbspotid", dbSpotId);
	    if(Const.D) Log.d("UploadMediaaaa","dbspot = " + dbSpotId);
	    sendBroadcast(intent);
	  }
}