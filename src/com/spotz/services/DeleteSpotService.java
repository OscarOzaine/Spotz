package com.spotz.services;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.spotz.utils.Const;

public class DeleteSpotService extends IntentService  {
	
	String spotId;
	String upLoadServerUri = "http://api.myhotspotz.net/app/deleteSpot";
	int serverResponseCode = 0;
	String TAG = "DeleteSpotService";
	public static final String NOTIFICATION = "com.spotz.MainActivity";
	
	public DeleteSpotService() {
		super("DeleteSpotService");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    Log.d(TAG, "onStartCommand");
	    spotId		= intent.getStringExtra("spotid");
	    return super.onStartCommand(intent,flags,startId);
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		// Normally we would do some work here, like download a file.
	      // For our sample, we just sleep for 5 seconds.
		
		
		if(deleteSpot(spotId) == 1){
			publishResults(3);
		}else{
			publishResults(-3);
		}
		
		
		
	      
	}

	
	public int deleteSpot(String SpotID) {

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
        
        
        try { 

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
            conn.setRequestProperty("spotid", SpotID); 
            
            dos = new DataOutputStream(conn.getOutputStream());
            

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();
            
            Log.d(Const.TAG, "HTTP Response is : "+ serverResponseMessage + ": " + serverResponseCode);

            
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            serverResponse = new StringBuilder();
       
            String line = null;
            while ((line = reader.readLine()) != null){
            	serverResponse.append(line);
            }
            
            if(serverResponseCode == 200){
            	Log.d(Const.TAG,"Spot deleted successfully.");
            }
            
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
        Log.i(Const.TAG, "READ "+serverResponse);
        
        return Integer.parseInt(serverResponse.toString()); 
    }
	
	private void publishResults(int result) {
	    Intent intent = new Intent(NOTIFICATION);
	    intent.putExtra("result", result);
	    sendBroadcast(intent);
	  }
}