package com.spotz;

import com.spotz.utils.Const;
import com.spotz.utils.Settings;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

public class SessionManager {

	private static final String TAG = Const.TAG+"-Session";
	
	/** If User is not logged in, request to login 
	 * @param activity current activity
     * @return true if user is logged in*/
	public static boolean requestLogin(Activity activity){
	    if( isLoggedIn(activity) ){
	    	return true; 
	    }
	    
    	if(Const.D) Log.e(TAG, "++ ON START ++ - USER IS NOT LOGGED IN");
    	Intent intent = new Intent(activity, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		activity.startActivityForResult(intent,Const.REQUEST_LOGIN);
		return false;
	}
	
	/** Check if user has logged in
	 * @param activity current activity
     * @return true if user is logged in*/
	public static boolean isLoggedIn(Activity activity){
		//Log.d(Const.TAG,"UserId = "+Settings.getIns(activity).getPref().getString(Settings.USER_ID, ""));
	    return Settings.getIns(activity).getPref().getBoolean(Settings.USER_ID, false);
	}
	
	/** delete current player info from android's session */
	public static void Logout(){
		Settings.getIns(null).getPref().edit()
		.remove(Settings.USER_ID)
		.remove(Settings.USER_JSON)
		.remove(Settings.TEAM_JSON)
		.commit();
	}
	
}