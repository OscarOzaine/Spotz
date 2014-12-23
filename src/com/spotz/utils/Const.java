package com.spotz.utils;
import android.util.Log;
/** Class containing constants and static methods accessible across the app*/ 
public class Const {
	//Debug variable
	public final static boolean D = true;
	public final static boolean CHEATS = D && true;
	// Intent request codes
    public static final int	REQUEST_LOGIN = 4;
	//Tag for debugging
	public final static String TAG = "Spotz";
	public static int width = 0;
	public static int height = 0;
	
	public static double currentLatitude = 0.0;
	public static double currentLongitude = 0.0;
	
    //Log\.([a-z]) -> Const.$1
    /** Android logging, only prints out if debug variable is set to true*/
    public static void v(String tag, String msg){
    	if(D)Log.v(tag, msg);
    }
    
    public static void d(String tag, String msg){
    	if(D)Log.d(tag, msg);
    }
    
    public static void i(String tag, String msg){
    	if(D)Log.i(tag, msg);
    }
    
    public static void w(String tag, String msg){
    	if(D)Log.w(tag, msg);
    }
    
    public static void e(String tag, String msg){
    	if(D)Log.e(tag, msg);
    }
    
}