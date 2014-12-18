package com.spotz;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.spotz.MySpotsActivity.LoadSpots;
import com.spotz.database.Spot;
import com.spotz.database.SpotsHelper;
import com.spotz.gen.R;
import com.spotz.services.UploadMediaService;
import com.spotz.utils.Const;
import com.spotz.utils.JSONParser;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

public class NewsActivity extends ListActivity {
	
	boolean loadNews = true;
	// Progress Dialog
	private ProgressDialog pDialog;
	String TAG = "NewsActivity";
	// Creating JSON Parser object
	JSONParser jsonParser = new JSONParser();

	ArrayList<HashMap<String, String>> outboxList;
	NewsViewAdapter adapter;
	ListView listView;
	// products JSONArray
	JSONArray outbox = null;

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
	        	Toast.makeText(NewsActivity.this, "Successfully uploaded spot",Toast.LENGTH_LONG).show();
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
	        	Toast.makeText(NewsActivity.this, "No internet connection",Toast.LENGTH_LONG).show();
	        }
	        else if(resultCode == 3){
	        	MainActivity.instance.setProgressBarIndeterminateVisibility(false);
	        	Toast.makeText(NewsActivity.this, "Spot deleted successfully",Toast.LENGTH_LONG).show();
	        	//new LoadSpots().execute();
	        }
	        else if(resultCode < 0) {
	        	MainActivity.instance.setProgressBarIndeterminateVisibility(false);
	        	Toast.makeText(NewsActivity.this, "Failed, try again later", Toast.LENGTH_LONG).show();
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
	}

	
	
	@Override
    public synchronized void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(UploadMediaService.NOTIFICATION));
     // Hashmap for ListView
        outboxList = new ArrayList<HashMap<String, String>>();
        
        int type = -1;
        OUTBOX_URL = "http://api.myhotspotz.net/app/getlatestspots/"+type;
		new LoadSpots().execute();
		Const.v(TAG, "+ ON RESUME +");
    }
	
	public static final String SHOWITEMINTENT_EXTRA_FETCHROWID = "fetchRow";
    public static final int ACTIVITY_SHOWITEM = 0; /*Intent request user index*/

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id){
    	TextView tv = (TextView) v.findViewById(R.id.spotId);
    	String spotIdHidden = tv.getText().toString();
    	//Log.d(TAG,"S>>>potId = "+spotIdHidden);
    	Intent spotIntent = new Intent(this, SpotActivity.class);
    	spotIntent.putExtra("SpotID",spotIdHidden);
        startActivity(spotIntent);
        overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
        //Intent tmpIntent = new Intent(this, YourActivityForShowingItem.class);
       // tmpIntent.putExtra(SHOWITEMINTENT_EXTRA_FETCHROWID, position);
        //startActivityForResult(tmpIntent, ACTIVITY_SHOWITEM);
        
    }
    
    /**
	 * Background Async Task to Load all OUTBOX messages by making HTTP Request
	 * */
	
	public class LoadSpots extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setProgressBarIndeterminateVisibility(true);
			
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
				
				// Check your log cat for JSON reponse
				/*
				Log.d(TAG, json.toString());
				if(!json.toString().isEmpty()){
					
				}else{
					loadNews = false;
				}
				*/
				String urlImage = "";
			
				outbox = json.getJSONArray(TAG_SPOTS);
				// looping through All messages
				for (int i = 0; i < outbox.length(); i++) {
					JSONObject c = outbox.getJSONObject(i);

					// Storing each json item in variable
					String id = c.getString(TAG_ID);
					
					String name = c.getString(TAG_NAME);
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
					map.put(TAG_NAME, name);
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
				
			} catch (JSONException e) {
				loadNews = false;
				e.printStackTrace();
			} catch (RuntimeException e){
				loadNews = false;
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
			// dismiss the dialog after getting all products
			pDialog.dismiss();
			setProgressBarIndeterminateVisibility(false);
			// updating UI from Background Thread
			if(loadNews){
				runOnUiThread(new Runnable() {
					public void run() {
						
						adapter = new NewsViewAdapter(NewsActivity.this, outboxList);
						setListAdapter(adapter);
						
					}
				});
			}
			

		}
		
		

		
	}
	

	
}