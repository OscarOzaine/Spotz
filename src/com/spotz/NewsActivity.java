package com.spotz;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.location.LocationClient;
import com.spotz.MySpotsActivity.LoadSpots;
import com.spotz.database.Spot;
import com.spotz.database.SpotsHelper;
import com.spotz.gen.R;
import com.spotz.location.MyLocation;
import com.spotz.location.MyLocation.LocationResult;
import com.spotz.services.UploadMediaService;
import com.spotz.utils.Const;
import com.spotz.utils.JSONParser;
import com.spotz.utils.Utils;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class NewsActivity extends Activity implements OnScrollListener {
	
	static boolean loadNews = true;
	static int startNew = 0;
	static int rowNews = 4;
	static boolean flag_loading = false;
	// Progress Dialog
	private static ProgressDialog pDialog;
	static String TAG = "NewsActivity";
	// Creating JSON Parser object
	static JSONParser jsonParser = new JSONParser();

	static ArrayList<HashMap<String, String>> outboxList;
	static NewsViewAdapter adapter;
	// products JSONArray
	static JSONArray outbox = null;
	

	// Outbox JSON url
	private static String OUTBOX_URL = "http://api.myhotspotz.net/app/getlatestspots";
	
	// ALL JSON node names
	static final String TAG_SPOTS = "spots";
	static final String TAG_ID = "id";
	static final String TAG_NAME = "name";
	static final String TAG_CREATED_AT = "created_at";
	static final String TAG_IMAGE = "image";
	static final String TAG_CITYNAME = "cityname";
	static final String TAG_EMAIL = "email";
	static final String TAG_DESCRIPTION = "description";
	static final String TAG_SPOTTYPE = "spottype";
	static final String TAG_LIKES = "likes";
	static final String TAG_DISLIKES = "dislikes";
	static final String TAG_LATITUDE = "latitude";
	static final String TAG_LONGITUDE = "longitude";
	
	static NewsActivity instance = null;
	SpotsHelper db = null;
	
	static boolean loading = false;
	
	static String currentLat = ""+Const.currentLatitude;
	static String currentLng = ""+Const.currentLongitude;
	
	Spinner spinnerSpotType, spinnerSpotDistance;
	static ListView listNews;
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	      Bundle bundle = intent.getExtras();
	      if (bundle != null) {
	    	  
	        int resultCode = bundle.getInt("result");
	        String dbspotId = bundle.getString("dbspotid");
	        Log.d(TAG,"RESULTCODE = "+resultCode+ " spot "+dbspotId);
	        if (resultCode == 1) {
	        	MainActivity.instance.setProgressBarIndeterminateVisibility(false);
	        	Toast.makeText(NewsActivity.this, getString(R.string.sucessfull_upload),Toast.LENGTH_LONG).show();
	        	db = new SpotsHelper(NewsActivity.this);
	        	List<Spot> list = db.getAllSpots();
				for (int i = 0; i < list.size(); i++) {
					Log.d(TAG,dbspotId+" - "+list.get(i).getId());
					if(Integer.parseInt(dbspotId) == list.get(i).getId()){
						Log.d(TAG,"LIST="+list.get(i));
						db.deleteSpot(list.get(i));
						//new LoadSpots().execute();
					}
				}
	        } 
	        else if(resultCode == 2){
	        	MainActivity.instance.setProgressBarIndeterminateVisibility(false);
	        	Toast.makeText(NewsActivity.this, getString(R.string.no_internet),Toast.LENGTH_LONG).show();
	        }
	        else if(resultCode == 3){
	        	MainActivity.instance.setProgressBarIndeterminateVisibility(false);
	        	Toast.makeText(NewsActivity.this, getString(R.string.spot_deleted_succesfull),Toast.LENGTH_LONG).show();
	        	findViewById(R.id.action_refresh).performClick();
	        }
	        else if(resultCode < 0) {
	        	MainActivity.instance.setProgressBarIndeterminateVisibility(false);
	        	Toast.makeText(NewsActivity.this, getString(R.string.failed_upload_spot), Toast.LENGTH_LONG).show();
	        }
	      }
	    }
	};
	
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.news_list);
		OUTBOX_URL = "http://api.myhotspotz.net/app/getlatestspots";
		instance = this;
		LocationResult locationResult = new LocationResult(){
		    @Override
		    public void gotLocation(Location location){
		    	Const.currentLatitude = location.getLatitude();
		    	Const.currentLongitude = location.getLongitude();
		    	Log.d(TAG,"getLocationACAs"+Const.currentLongitude+"  "+Const.currentLatitude);
		    	//if(Const.currentLongitude != 0){
		    	if(loading == false)
		    		new LoadSpots().execute();
		    	//}
		        //Got the location!
		    }
		};
		MyLocation myLocation = new MyLocation();
		if(Const.currentLongitude == 0 && Const.currentLatitude == 0){
			myLocation.getLocation(this, locationResult);
		}
		
		
		spinnerSpotType = (Spinner) findViewById(R.id.filter_spottypes);
		spinnerSpotDistance = (Spinner) findViewById(R.id.filter_spotdistance);
		spinnerSpotType.setOnItemSelectedListener(
				new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		        // your code here
		    	listNews.setAdapter(null);
		    	startNew = 0;
		    	outboxList.clear();
		    	Const.spotTypePosition 		= position;
		        Const.spotDistancePosition 	= position;
		        if(loading == false)
		    		new LoadSpots().execute();
		    	//Log.d(TAG,"onItemSelected"+position);
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
		        // your code here
		    }
		});
		
		listNews	= (ListView) findViewById(R.id.list_news);
		listNews.setOnItemClickListener(
		        new OnItemClickListener(){
		            @Override
		            public void onItemClick(AdapterView<?> arg0, View view,
		                    int position, long id) {
		            	TextView tv = (TextView) view.findViewById(R.id.spotId);
		            	String spotIdHidden = tv.getText().toString();
		            	//Log.d(TAG,"S>>>potId = "+spotIdHidden);
		            	Intent spotIntent = new Intent(NewsActivity.this, SpotActivity.class);
		            	spotIntent.putExtra("SpotID",spotIdHidden);
		                startActivity(spotIntent);
		                overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
		                //Intent tmpIntent = new Intent(this, YourActivityForShowingItem.class);
		                //tmpIntent.putExtra(SHOWITEMINTENT_EXTRA_FETCHROWID, position);
		                //startActivityForResult(tmpIntent, ACTIVITY_SHOWITEM);
		            }   
		        }       
		);
		
		
		/*
		new ShowcaseView.Builder(this)
		    .setTarget(new ActionViewTarget(this, ActionViewTarget.Type.HOME))
		    .setContentTitle("ShowcaseView")
		    .setContentText("This is highlighting the Home button")
		    .hideOnTouchOutside()
		    .build();
		*/
	}

	@Override
    public void onStop() {
        super.onStop();
        Const.spotTypePosition = spinnerSpotType.getSelectedItemPosition();
        Const.spotDistancePosition = spinnerSpotDistance.getSelectedItemPosition();
        
        Const.v(TAG, "-- ON STOP --");
    }
	
	@Override
    public synchronized void onResume() {
        super.onResume();
        Utils.setCurrentLocale(this);
        
  		ArrayAdapter<CharSequence> adapterTypes = ArrayAdapter.createFromResource(this,
  		        R.array.spottype_array, android.R.layout.simple_spinner_item);
  		adapterTypes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
  		spinnerSpotType.setAdapter(adapterTypes);
  		
  		ArrayAdapter<CharSequence> adapterDistance = ArrayAdapter.createFromResource(this,
  		        R.array.spotdistance_array, android.R.layout.simple_spinner_item);
  		adapterDistance.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
  		spinnerSpotDistance.setAdapter(adapterDistance);
        spinnerSpotType.setSelection(Const.spotTypePosition);
        spinnerSpotDistance.setSelection(Const.spotDistancePosition);
        registerReceiver(receiver, new IntentFilter(UploadMediaService.NOTIFICATION));
        listNews.setOnScrollListener(this);
     // Hashmap for ListView
        outboxList = new ArrayList<HashMap<String, String>>();
        OUTBOX_URL = "http://api.myhotspotz.net/app/getlatestspots/"+Const.spotTypePosition+"/"+Const.currentLatitude+"/"+Const.currentLongitude+"/"+Const.spotDistancePosition+"/"+startNew+"/"+rowNews;
		//new LoadSpots().execute();
		Const.v(TAG, "+ ON RESUME +"+OUTBOX_URL);
    }
	
	public static final String SHOWITEMINTENT_EXTRA_FETCHROWID = "fetchRow";
    public static final int ACTIVITY_SHOWITEM = 0; /*Intent request user index*/
    
    /**
	 * Background Async Task to Load all OUTBOX messages by making HTTP Request
	 * */
	public static class LoadSpots extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			loading = true;
			instance.setProgressBarIndeterminateVisibility(true);
			OUTBOX_URL = "http://api.myhotspotz.net/app/getlatestspots/"+Const.spotTypePosition+"/"+Const.currentLatitude+"/"+Const.currentLongitude+"/"+Const.spotDistancePosition+"/"+startNew+"/"+rowNews;
			Log.d(TAG,OUTBOX_URL);
			pDialog = new ProgressDialog(NewsActivity.instance);
			pDialog.setMessage(instance.getString(R.string.loading_spots));
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting Outbox JSON
		 * */
		protected String doInBackground(String... args) {
			loadNews = true;
			try {
				// Building Parameters
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				
				// getting JSON string from URL
				JSONObject json = jsonParser.makeHttpRequest(OUTBOX_URL, "GET",
						params);
				Log.d(TAG,"HTTP="+OUTBOX_URL);
				if(json == null){
					loadNews = false;
					Log.d(TAG,"JSONNULL");
				}
				else{
					
					startNew+=4; 
					
					String urlImage = "";
					outbox = json.getJSONArray(TAG_SPOTS);
					// looping through All messages
					for (int i = 0; i < outbox.length(); i++) {
						JSONObject c = outbox.getJSONObject(i);

						// Storing each json item in variable
						String id = c.getString(TAG_ID);
						
						//String name = c.getString(TAG_NAME);
						String created_at = c.getString(TAG_CREATED_AT);
						String image = c.getString(TAG_IMAGE);
						String cityname = c.getString(TAG_CITYNAME);
						String email = c.getString(TAG_EMAIL);
						String description = c.getString(TAG_DESCRIPTION);
						String spottype = c.getString(TAG_SPOTTYPE);
						String likes = c.getString(TAG_LIKES);
						String dislikes = c.getString(TAG_DISLIKES);
						String latitude = c.getString(TAG_LATITUDE);
						String longitude = c.getString(TAG_LONGITUDE);
						
						
						// subject taking only first 23 chars
						// to fit into screen
						/*
						if(subject.length() > 23){
							subject = subject.substring(0, 22) + "..";
						}
						*/
						// creating new HashMap
						HashMap<String, String> map = new HashMap<String, String>();
						
						// adding each child node to HashMap key => value
						map.put(TAG_ID, id);
						//map.put(TAG_NAME, name);
						map.put(TAG_CREATED_AT, created_at);
						
						urlImage = "http://myhotspotz.net/public/images/spots/"+image;
						map.put(TAG_IMAGE,urlImage);
						map.put(TAG_CITYNAME, cityname);
						map.put(TAG_EMAIL, email);
						map.put(TAG_DESCRIPTION, description);
						map.put(TAG_SPOTTYPE, spottype);
						map.put(TAG_LIKES, likes);
						map.put(TAG_DISLIKES, dislikes);
						map.put(TAG_LATITUDE, latitude);
						map.put(TAG_LONGITUDE, longitude);
						// adding HashList to ArrayList
						outboxList.add(map);
					}
				}
				
				
			} catch (JSONException e) {
				loadNews = false;
				e.printStackTrace();
			} catch (RuntimeException e){
				loadNews = false;
				e.printStackTrace();
				Log.d(TAG,"RuntimeException");
			} catch (Exception e){
				loadNews = false;
				Log.d(TAG,"Exception");
			}
			
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			pDialog.dismiss();
			instance.setProgressBarIndeterminateVisibility(false);
			// updating UI from Background Thread
			if(loadNews){
				instance.runOnUiThread(new Runnable() {
					public void run() {
						adapter = new NewsViewAdapter(NewsActivity.instance, outboxList);
						listNews.setAdapter(adapter);
					}
				});
			}
			loading = false;
		}
	}

	
	public class LoadMoreSpots extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			loading = true;
			setProgressBarIndeterminateVisibility(true);
			OUTBOX_URL = "http://api.myhotspotz.net/app/getlatestspots/"+Const.spotTypePosition+"/"+Const.currentLatitude+"/"+Const.currentLongitude+"/"+Const.spotDistancePosition+"/"+startNew+"/"+rowNews;
			pDialog = new ProgressDialog(NewsActivity.this);
			pDialog.setMessage(getString(R.string.loading_spots));
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting Outbox JSON
		 * */
		protected String doInBackground(String... args) {
			loadNews = true;
			try {
				// Building Parameters
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				
				// getting JSON string from URL
				JSONObject json = jsonParser.makeHttpRequest(OUTBOX_URL, "GET",
						params);
				
				if(json == null){
					//Log.d(TAG,"DontLoadNews"+json.toString());
					loadNews = false;
					Log.d(TAG,"JSONNULL");
				}else{
					loadNews = true;
					//Log.d(TAG,"LoadEm"+json.toString());
					
					Log.d(TAG,"MOREHTTP="+OUTBOX_URL);
					startNew+=4; 
					
					String urlImage = "";
				
					outbox = json.getJSONArray(TAG_SPOTS);
					// looping through All messages
					for (int i = 0; i < outbox.length(); i++) {
						JSONObject c = outbox.getJSONObject(i);

						// Storing each json item in variable
						String id = c.getString(TAG_ID);
						
						//String name = c.getString(TAG_NAME);
						String created_at = c.getString(TAG_CREATED_AT);
						String image = c.getString(TAG_IMAGE);
						String cityname = c.getString(TAG_CITYNAME);
						String email = c.getString(TAG_EMAIL);
						String description = c.getString(TAG_DESCRIPTION);
						String spottype = c.getString(TAG_SPOTTYPE);
						String likes = c.getString(TAG_LIKES);
						String dislikes = c.getString(TAG_DISLIKES);
						String latitude = c.getString(TAG_LATITUDE);
						String longitude = c.getString(TAG_LONGITUDE);
						
						
						// subject taking only first 23 chars
						// to fit into screen
						/*
						if(subject.length() > 23){
							subject = subject.substring(0, 22) + "..";
						}
						*/
						// creating new HashMap
						HashMap<String, String> map = new HashMap<String, String>();
						
						// adding each child node to HashMap key => value
						map.put(TAG_ID, id);
						//map.put(TAG_NAME, name);
						map.put(TAG_CREATED_AT, created_at);
						
						urlImage = "http://myhotspotz.net/public/images/spots/"+image;
						map.put(TAG_IMAGE,urlImage);
						map.put(TAG_CITYNAME, cityname);
						map.put(TAG_EMAIL, email);
						map.put(TAG_DESCRIPTION, description);
						map.put(TAG_SPOTTYPE, spottype);
						map.put(TAG_LIKES, likes);
						map.put(TAG_DISLIKES, dislikes);
						map.put(TAG_LATITUDE, latitude);
						map.put(TAG_LONGITUDE, longitude);
						// adding HashList to ArrayList
						outboxList.add(map);
					}
				}

			} catch (JSONException e) {
				loadNews = false;
				e.printStackTrace();
				Log.d(TAG,"Json exception");
			} catch (RuntimeException e){
				loadNews = false;
				e.printStackTrace();
				Log.d(TAG,"RuntimeException");
			} catch (Exception e){
				loadNews = false;
				Log.d(TAG,"Exception");
			} 
			
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			pDialog.dismiss();
			setProgressBarIndeterminateVisibility(false);
			// updating UI from Background Thread
			if(loadNews){
				runOnUiThread(new Runnable() {
					public void run() {
						Log.d(TAG,"notify changes");
						adapter.notifyDataSetChanged();
						flag_loading = false;
						
					}
				});
			}
			loading = false;
		}
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		//Log.d(TAG,"onScrollStateChanged "+scrollState);
		
	}
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		//Log.d(TAG,"onScroll firstvisible "+firstVisibleItem+" - visibleitemcnt = "+visibleItemCount+" totalItemC ="+totalItemCount);
		// TODO Auto-generated method stub
		if(firstVisibleItem+visibleItemCount == totalItemCount && totalItemCount != 0){
			//Log.d(TAG,"addItems0");
            if(flag_loading == false){
                flag_loading = true;
                new LoadMoreSpots().execute();
            }
        }
	}
	
	public static void initialize(){
		startNew = 0;
		outboxList = new ArrayList<HashMap<String, String>>();
		flag_loading = false;
		loadNews = true;
		listNews.setAdapter(null);
	}
	
}