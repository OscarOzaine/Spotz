package com.spotz.services;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import com.spotz.utils.Const;
import com.spotz.utils.Utils;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class UploadMediaService extends IntentService  {
	
	String imagePath, spotName, spotDescription, spotTypeId, userId, latitude, longitude;
	String upLoadServerUri = "http://api.myhotspotz.net/app/uploadSpot";
	int serverResponseCode = 0;
	public static final String NOTIFICATION = "com.spotz.MainActivity";
	public UploadMediaService() {
		super("UploadMediaService");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    Log.d("UploadMediaService", "onStartCommand");
	    imagePath		= intent.getStringExtra("imagepath");
	    spotName		= intent.getStringExtra("spotname");
	    spotDescription	= intent.getStringExtra("spotdescription");
	    spotTypeId		= intent.getStringExtra("spottypeId");
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
		if(uploadSpot(imagePath, spotName, spotDescription, 
				spotTypeId, userId,latitude, longitude) == 1){
			publishResults(1);
		}		
        else{
        	publishResults(-1);
        }
	      
	}

	  
	public int uploadSpot(String sourceFileUri, String spotname, String spotdescription, 
						String spottype, String userid, String latitude, String longitude) {

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
        Log.e(Const.TAG,sourceFileUri);
        if (!sourceFile.isFile()) {
        	Log.e(Const.TAG, "Source File not exist :" +imagePath);
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
                conn.setRequestProperty("latitude", ""+latitude);
                conn.setRequestProperty("longitude", ""+longitude);
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
                
                Log.i(Const.TAG, "HTTP Response is : "+ serverResponseMessage + ": " + serverResponseCode);

                
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                serverResponse = new StringBuilder();
           
                String line = null;
                while ((line = reader.readLine()) != null){
                	serverResponse.append(line);
                }
                
                if(serverResponseCode == 200){
                	Log.d(Const.TAG,"File Upload Complete.");
                }
                
                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();
            } catch (MalformedURLException ex) {

            	ex.printStackTrace();
            	Log.d(Const.TAG,"MalformedURLException");
                Log.e(Const.TAG, "error: " + ex.getMessage(), ex);  
            } catch (Exception e) {
            	e.printStackTrace();

            	Log.d(Const.TAG,"Got Exception : see logcat");
                Log.e(Const.TAG, "Exception : "+ e.getMessage(), e); 
            }
            Log.d(Const.TAG, "READ "+serverResponse);
            
            if(Utils.isNumeric(serverResponse.toString())){
            	return Integer.parseInt(serverResponse.toString());
            }else{
            	Log.d(Const.TAG,"Error: "+serverResponse.toString());
            	return Integer.parseInt(serverResponse.toString()); 
            }
            
        } // End else block 
    }	
	
	private void publishResults(int result) {
	    Intent intent = new Intent(NOTIFICATION);
	    intent.putExtra("result", result);
	    sendBroadcast(intent);
	  }
}