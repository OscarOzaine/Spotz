package com.spotz.users;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.spotz.frags.InfoUpdateListener;
import com.spotz.utils.Const;
import com.spotz.utils.Settings;
import android.util.Log;

public class User {

	//player info listener:
	ArrayList<InfoUpdateListener> infoListeners;
	
	//User properties
	protected 	int		uID, uFollowers, uPlusReputation, uMinusReputation, uSharedlocation,uConfirmed;
	protected	double	uLatitude, uLongitude;
	protected 	String	uSuspended,uUsername, uEmail, uFirstname, uLastname, uLink, uImage,uCreated_at;
	
	//JSON proccessing constants IDs
	private final static String 
			ID = "id",
			USERNAME = "username", 	 
			EMAIL = "email", 	
			IMAGE = "image", 
			PLUS = "plus", 		 
			MINUS = "minus",	
			FOLLOWERS = "followers",
			SUSPENDED = "suspended",
			CONFIRMED = "confirmed",		 	 
			FIRST_NAME = "first_name",	 
			LAST_NAME = "last_name",
			LINK = "link",	 
			SHARELOCATION = "sharelocation",
			CURRENT_LAT = "currentlat",
			CURRENT_LNG = "currentlng",
			CREATED_AT = "created_at";
	
	private static User currentU;
	
	/** Get current Player logged in */
	public static final User current(){
		if( currentU == null){
			String playerJson = Settings.getIns(null).getPref().getString(Settings.USER_JSON, null);
			
			if( playerJson == null)
				currentU = new User( 1, "defaultusername", "default@default.com","de","fault");
			else
				currentU = new User(playerJson);
		}
		Log.d(Const.TAG,"currentuser "+currentU.getID());
		return  currentU;
	}
	
	/**Once the player has logged in, we initialize the 
	 * player's  basic parameters.
	 * @param id the user id
	 * @param name the user name
	 * @param life startinf life %	 */
	private User(int id, String username, String email, String firstname,String lastname){
		infoListeners = new ArrayList<InfoUpdateListener>();
		initPlayer(id, username, email, firstname, lastname);
	}
	
	/**Once the player has logged in, we initialize the 
	 * player's  basic parameters.
	 * @param id the user id
	 * @param name the user name
	 * @param life startinf life %	 */
	public void initPlayer(int id, String username, String email, String firstname,String lastname){
		uID 		= id;
		uUsername 	= username;
		uEmail   	= email;
		uFirstname	= firstname;
		uLastname	= lastname;
		notifyListener();
	}
	
	/** Once the player has logged in, we initialize the 
	 * player's  basic parameters.
	 * @param playerJSON the servers response with current player profile as STRING*/
	private User(String playerJSON){
		infoListeners = new ArrayList<InfoUpdateListener>();
		try {
			JSONArray res;
			res = new JSONArray(playerJSON);
			Log.d(Const.TAG,"inittaca"+res);
			initPlayer(res);
		} catch (JSONException e) {	if(Const.D) e.printStackTrace();	} 
	}
	
	/** Once the player has logged in, we initialize the 
	 * player's  basic parameters.
	 * @param playerJSON the servers response with current player profile as JSON*/
	private User(JSONArray playerJSON){
		infoListeners = new ArrayList<InfoUpdateListener>();
		initPlayer(playerJSON);
	}
	
	/** we initialize the player's  basic parameters from a json.
	 * @param res the servers response with current player profile*/
	public void initPlayer(JSONArray res){
		Log.v(Const.TAG, "PlayerInit");
		try {
			// extract jsonObejct from JsonArray
			JSONObject infoJson = res.getJSONObject(0); 
			Settings.getIns(null).getPref().edit().putString(Settings.USER_JSON, res.toString()).commit();
			Settings.getIns(null).getPref().edit().putBoolean(Settings.USER_ID, true).commit();
			Log.v(Const.TAG, "PlayerInit aca"+ Settings.getIns(null).getPref().getString(Settings.USER_JSON, "ida"));
			uID				= infoJson.getInt(ID);
			uUsername		= infoJson.getString(USERNAME);
			uEmail			= infoJson.getString(EMAIL);
			uImage			= infoJson.getString(IMAGE);
			uPlusReputation	= Integer.parseInt(infoJson.getString(PLUS));
			uMinusReputation= Integer.parseInt(infoJson.getString(MINUS));
			uFollowers		= Integer.parseInt(infoJson.getString(FOLLOWERS));
			uSuspended		= infoJson.getString(SUSPENDED);
			uConfirmed		= Integer.parseInt(infoJson.getString(CONFIRMED));
			uFirstname		= infoJson.getString(FIRST_NAME);
			uLastname		= infoJson.getString(LAST_NAME);
			uLink			= infoJson.getString(LINK);
			uSharedlocation	= Integer.parseInt(infoJson.getString(SHARELOCATION));
			uLatitude		= Double.parseDouble(infoJson.getString(CURRENT_LAT));
			uLongitude		= Double.parseDouble(infoJson.getString(CURRENT_LNG));
			uCreated_at		= infoJson.getString(CREATED_AT);
		} catch (Exception e) {	if(Const.D)e.printStackTrace(); }
		if(Const.D){
			Log.v(Const.TAG, "PlayerInitComplete");
		}
		Log.d(Const.TAG,"userjson = "+Settings.getIns(null).getPref().getString(Settings.USER_JSON, null));
		notifyListener();
	}	
	
	/***************************** Members getters  *************************/
	/** @return the pID */
	public int getID() {
		return uID;
	}
	/** @return the followers count */
	public int getFollowers() {
		return uFollowers;
	}
	/**@return the plus reputation */
	public int getPlusReputation() {
		return uPlusReputation;
	}
	/** @return the minus reputation */
	public int getMinusReputation() {
		return uMinusReputation;
	}
	/** @return if user is sharing location */
	public int getSharedLocation() {
		return uSharedlocation;
	}
	/** @return if the user is suspended  */
	public String getSuspended() {
		return uSuspended;
	}
	/** @return if the user is confirmed */
	public int getConfirmed() {
		return uConfirmed;
	}
	/** @return the latitude */
	public double getLatitude() {
		return uLatitude;
	}
	/** @return the longitude	 */
	public double getLongitude() {
		return uLongitude;
	}
	/** @return the username */
	public String getUsername() {
		return uUsername;
	}
	/** @return the email address	 */
	public String getEmail() {
		return uEmail;
	}
	
	/** @return the first name */
	public String getFirstName() {
		return uFirstname;
	}
	
	/** @return last name */
	public String getLastName() {
		return uLastname;
	}
	
	/** @return the URL profile link */
	public String getLink() {
		return uLink;
	}
	
	/** @return the profile image of the user */
	public String getImage() {
		return uImage;
	}
	
	/** @return the profile creation date of the user */
	public String getCreatedat() {
		return uCreated_at;
	}
	
	/************************* InfoUpdateListener Implementations  *************************/
	
	/** add a new listener that will be notified when info has been updated */
	public void addOnInfoUpdateListener(InfoUpdateListener iul){
		if(Const.D)
			Log.i(Const.TAG,"adding infoListeners: ");
		if (!infoListeners.contains(iul))
			infoListeners.add(iul);
	}
	
	/** Notify the listeners so it will refresh it's UI */
	public void notifyListener(){
		Log.d(Const.TAG,"notifyListener");
		if ( infoListeners.size() > 0)
			if(Const.D)
				Log.i(Const.TAG,"notifying infoListeners, total: "+ infoListeners.size());
		
		for(int i =0 ; i < infoListeners.size() ; i++){
			infoListeners.get(i).refreshUI_newThread();
		}
	}
	
	/** remove a listener, if it's known that it won't be used any more */
	public void removeOnInfoUpdateListener(InfoUpdateListener iul){
		if(Const.D)
			Log.i(Const.TAG,"removing infoListeners: ");
		infoListeners.remove(iul);
	}

	/** Recover Life and Ammo	 */
	/*
	public void recover(){
		pLife = 100;
		pAmmo = gunMagazine;
		MessageManager.getIns().sendRecover();
		if(Const.D)
			Log.i(TAG, pName+" recovered");
		notifyListener();
	}
	*/
}