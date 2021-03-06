package com.spotz.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.spotz.CameraActivity;
import com.spotz.gen.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

public class Utils {
	
	public static String EMAIL_REGEX = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
	static Context mContext;
	public final static boolean D = true;
	
	public Utils(Context mContext) {
		this.mContext = mContext;
	}
	
	/**
     * Copies stream
     * @param input
     * @param output
     */
    public static void CopyStream(InputStream is, OutputStream os){
        final int buffer_size=1024;
        try{
            byte[] bytes=new byte[buffer_size];
            for(;;){
              int count=is.read(bytes, 0, buffer_size);
              if (count==-1)
                  break;
              os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }
    
    /**
     * checks if a string has a valid json format
     * @param string
     */
    public static boolean isJSONValid(String test) {
	    try {
	        new JSONObject(test);
	    } catch (JSONException ex) {
	        // edited, to include @Arthur's comment
	        // e.g. in case JSONArray is valid as well...
	        try {
	            new JSONArray(test);
	        } catch (JSONException ex1) {
	            return false;
	        }
	    }
	    return true;
	}
    
    /**
     * Scales an image
     * @param bitmap
     * @param scaling factor
     */
    public static Bitmap ScaleBitmap(Bitmap bm, float scalingFactor) {
        int scaleHeight = (int) (bm.getHeight() * scalingFactor);
        int scaleWidth = (int) (bm.getWidth() * scalingFactor);
		
        return Bitmap.createScaledBitmap(bm, scaleWidth, scaleHeight, true);
    }
    
    /**
     * Checks if a string is double
     * @param string
     */
    public static boolean isNumeric(String str) {
      try {
    	  double d = Double.parseDouble(str);  
      }  
      catch(NumberFormatException nfe) {
    	  return false;
      }  
      return true;  
    }
    
    /**
     * rotates image
     * @param bitmap
     * @param degree
     */
    public static Bitmap rotateImage(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        
        Matrix mtx = new Matrix();
        mtx.postRotate(degree);
        
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }
    
    /**
     * Checks if a file is a video
     * @param string
     */
    public static boolean isVideo(String mediaName){
    	String[] mediaStrs = mediaName.split("\\.(?=[^\\.]+$)");
    	if (mediaStrs[1].equals("mp4") || mediaStrs[1].equals("3gp")) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }
    
    /**
     * creates the URL Address of the spot in the website
     * @param metadata
     */
    public static String createSpotLink(String cityName, String spotId){
    	return ("http://myhotspotz.net/spots/"+cityName+"-"+Integer.toHexString(Integer.parseInt(spotId))).replaceAll("\\s+","");
    }
    
    /**
     * Gets the metadata of a file
     * @param metadata
     */
    public static String getMetadataParenthesis(String metadata){
    	Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(metadata);
    	while(m.find()) {
    		return m.group(1);
    		//System.out.println(m.group(1));    
    	}
    	return "-1";
    }
    
    /**
     * Checks if a string is integer
     * @param string
     */
    public static boolean isInteger(String s) {
        try { 
            Integer.parseInt(s); 
        } catch(NumberFormatException e) { 
            return false; 
        }
        // only got here if we didn't return false
        return true;
    }
    
    
    /**
     * Checks if the device is online
     */
    public static boolean isOnline() {
    	ConnectivityManager cm =
            (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Changes the language inside the app
     * @param context
     */
	public static void setCurrentLocale(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String value = prefs.getString("keyLanguage", "en");
		Locale locale = new Locale(value);
	    Locale.setDefault( locale );
	    
	    Configuration config = new Configuration();
	    config.locale = locale;
	    context.getResources().updateConfiguration( config, context.getResources().getDisplayMetrics() );
	}
    
}