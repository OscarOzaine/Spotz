package com.spotz.comm;

import java.net.HttpURLConnection;
import java.util.Locale;

import org.json.*;

import com.facebook.LoginActivity;
import com.spotz.MainActivity;
import com.spotz.ProfileActivity;
import com.spotz.RegisterActivity;
import com.spotz.UploadSpotActivity;
import com.spotz.gen.R;
import com.spotz.services.UploadProfilePicService;
import com.spotz.users.OnLoginListener;
import com.spotz.users.OnRegisterListener;
import com.spotz.users.User;
import com.spotz.utils.Const;
import com.spotz.utils.Settings;
import com.spotz.utils.Utils;


import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class MessageManager {

	// Debuging TAG
	private static final String TAG = Const.TAG + "-msgManager";
	public static String SRVreceived = null;
	private final static int RESPONSE_OK = 200;
	private final static int RESPONSE_DENIED = 400;
	
	// Server communication events;
	public static class Events{
		public static final String  LOGIN="login", UPDATE="update",
				FACEBOOK_LOGIN="facebook_login", REGISTER = "register";
	}
	
	//Event Listeners
	static OnLoginListener mLoginListener;
	static OnRegisterListener mRegisterListener;
	
	/**This method handles the message received through the wifi interface
	 * @param msg string storing the message */
	public static void mInternetReceived(final String response, int responseCode) {
		if (response == null || response.equals(""))
			return;
		
		if(Const.D){
			Log.d(TAG, "SRVR response: " + responseCode + " - "+ response);
		}
		
		//mUIsend(response,true);
		switch(responseCode){
			case RESPONSE_OK: // OK
				try {
					//JSONArray res = new JSONArray(response);
					JSONObject res= new JSONObject(response);
				
					Object action = res.get("event");
					if(action.toString().equals(Events.LOGIN)){
						if( mLoginListener != null)
							mLoginListener.onLoginSuccess(response);
					}
					else if(action.equals(Events.REGISTER)){
						if( mRegisterListener != null)
							mRegisterListener.onRegisterSuccess(response);
					}
					else if(action.equals(Events.FACEBOOK_LOGIN)){
						if( mLoginListener != null)
							mLoginListener.onLoginSuccess(response);
					}
					else if(action.equals(Events.UPDATE)){
						
					}
				} catch (JSONException e) {
					Log.e(TAG, e.getLocalizedMessage());
					//mUIsend("Response not parceable",false);
					if( mLoginListener != null) //in case LoginListener is waiting for a response
						mLoginListener.onLoginError(response);
				}
			break;
			
			case RESPONSE_DENIED: // Wrong Request
			default:
				try {
					JSONObject res= new JSONObject(response);
					String event = res.getString("event");
					String error = res.getString("error");
					if(event.equals(Events.LOGIN)){
						if (mLoginListener != null)
							mLoginListener.onLoginError(error);
					}
					else if(event.equals(Events.REGISTER)){
						if( mRegisterListener != null)
							mRegisterListener.onRegisterError(response);
					}
					
				} catch (JSONException e) {
					Log.e(TAG, e.getLocalizedMessage());
					//mUIsend("Response not parceable",false);
					if( mLoginListener != null) //in case LoginListener is waiting for a response
						mLoginListener.onLoginError(response);
					else if(mRegisterListener != null)
						mRegisterListener.onRegisterError(response);
					
				}
			break;
		}
	}
	
	/**Connect and send an event to the server, wait for it's
	 * response and send it to the mInternetReceived(response) method
	 * on a new thread
	 * @param query to the server
	 * @param event the message event, used to notify this event listener if there is an error
	 * @param optionparams 
	 * @return false if there is no network available
	 */
	public static boolean mInternetSend(final String query, final String event, final String[][] optionparams) {
		if( !ServerConn.isNetworkAvailable() )
			return false;
		new Thread(new Runnable() {
			public void run() {
				try {
					// Connect To Server
					HttpURLConnection con = ServerConn.Connect(ServerConn.metPOST, Settings.getIns(null).getRegEventAdd(), query, optionparams);
					// getResponse & process it
					
					SRVreceived = ServerConn.getResponse(con);
					Log.d(TAG,"SRVreceived "+ SRVreceived);
					if(SRVreceived.equals("")){
						JSONObject error = new JSONObject();
						try {
							error.put("error", "Problemas de conexion, verifique su conexion a internet");
						} catch (JSONException e) {
						    e.printStackTrace();
						}
						mInternetReceived(error.toString(), 400);
						return;
					}
					Log.d(TAG,"casc"+SRVreceived);
					mInternetReceived(SRVreceived,con.getResponseCode());
				} catch (Exception e1) {
					if (Const.D);
						e1.printStackTrace();
					try {
						//notify the server's response receiver that there was an error
						JSONObject error =new JSONObject();
						error.put("error", "" + e1.getMessage());
						error.put("event", event);
						mInternetReceived(error.toString(), 400);
						Log.d(TAG,"Double Try");
					} catch (JSONException e) {	Log.e(TAG, " INNER TRY - " + e.getMessage());}
				}
			}
		}).start();
		return true;
	}
	
	/*************************	LISTENERS	***********************************/
	/**This method sets an specific onLoginListener
	 * to notify the servers response
	 * @param oll onLoginListener to callback
	 */
	public static void setOnLoginListener(OnLoginListener oll){
		mLoginListener = oll;
	}
	
	public static void setOnRegisterListener(OnRegisterListener orl){
		mRegisterListener = orl;
	}
	
	/*************************	PREDEFINED SERVER MESSAGES	******************/
	/**This method sends a queryString to the server, informing that
	 * current player got shot by shooterID.
	 * @param shooterID the player who shoot current player
	 * @param died must be true if the shot killed the player
	 * @return true if there was a network connection available	 */
	/*
	public boolean sendGotShot(String shooterID, boolean died){
		String query = String.format(Locale.US,"p1=%s&p2=%d&hp=%d&event=%s", shooterID, Player.current().getID(), Player.current().getLife(), (died?Events.KILL:Events.SHOT));
		return mInternetSend(query, (died?Events.KILL:Events.SHOT));
	}
	*/
	
	public static boolean sendFBLogin(JSONObject userInfo, String accessToken){
		String query = String.format(Locale.US,"fblogin/");
		
		String email = null, gender = null, first_name = null, last_name = null;
		String id = null, link = null, token = null;
		JSONArray arr;
		try {
			arr = userInfo.getJSONArray("info");
		
			for (int i = 0; i < arr.length(); i++){
				email = arr.getJSONObject(i).getString("email");
				gender = arr.getJSONObject(i).getString("gender");
				first_name = arr.getJSONObject(i).getString("first_name");
				last_name = arr.getJSONObject(i).getString("last_name");
				//birthday = arr.getJSONObject(i).getString("birthday");
				id = arr.getJSONObject(i).getString("id");
				link = arr.getJSONObject(i).getString("link");
				token = accessToken;
				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String[][] optionparams = 
			{
		        { "email", email},
		        { "gender", gender},
		        { "first_name", first_name},
		        { "last_name", last_name},
		        { "id", id},
		        { "link", link},
		        { "token", token}
		    };
		
		boolean ret = mInternetSend(query, Events.FACEBOOK_LOGIN,optionparams);
		if( !ret && mLoginListener != null)
			mLoginListener.onLoginError(MainActivity.instance.getString(R.string.not_connected));
		
		return ret;
	}
	/**Login into Spotz server to fetch users data 
	 * @param usu to username or email
	 * @param pas for password
	 * @return true if there was a network connection available	 */
	public static boolean sendLogIn(String usu, String pas){
		String query = String.format(Locale.US,"login/");
		String[][] optionparams = 
			{
		        { "username", usu},
		        { "password", pas}
		        };

		boolean ret = mInternetSend(query, Events.LOGIN,optionparams);
		if( !ret && mLoginListener != null)
			mLoginListener.onLoginError(MainActivity.instance.getString(R.string.not_connected));
		return ret;
	}
	
	/**Login into Spotz server to fetch users data 
	 * @param usu to username
	 * @param email for email
	 * @param password for users password
	 * @param repeatpassword for the repeated password
	 * @return true if there was a network connection available	 */
	public static boolean sendRegister(String usu,String email, String pas, String repeatpassword){
		String query = String.format(Locale.US,"register/");
		String[][] optionparams = 
			{
		        { "username", usu},
		        { "email", email},
		        { "password", pas},
		        { "repeatpassword", repeatpassword}
		     };
		boolean ret = mInternetSend(query, Events.REGISTER,optionparams);
		if( !ret && mRegisterListener != null)
			mRegisterListener.onRegisterError(RegisterActivity.instance.getString(R.string.not_connected));
		return ret;
	}
	
}