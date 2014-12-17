package com.spotz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.widget.FacebookDialog;
import com.spotz.database.Spot;
import com.spotz.database.SpotsHelper;
import com.spotz.gen.R;
import com.spotz.services.UploadMediaService;
import com.spotz.users.User;
import com.spotz.utils.Const;
import com.spotz.utils.JSONParser;
import com.spotz.utils.Utils;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

public class MySpotsActivity extends ListActivity {
	
	boolean loadSpots = true;
	// Progress Dialog
	private ProgressDialog pDialog;
	String TAG = "MySpotsActivity";
	// Creating JSON Parser object
	JSONParser jsonParser = new JSONParser();

	ArrayList<HashMap<String, String>> outboxList;
	MySpotsViewAdapter adapter;
	ListView listView;
	
	// products JSONArray
	JSONArray outbox = null;

	// Outbox JSON url
	private static String OUTBOX_URL = "http://api.myhotspotz.net/app/getlatestspots";
	
	// ALL JSON node names
	static final String TAG_MYSPOTS = "myspots";
	static final String TAG_ID = "id";
	static final String TAG_NAME = "name";
	static final String TAG_IMAGE = "image";
	static final String TAG_DESCRIPTION = "description";
	static final String TAG_SPOTTYPE = "spottype";
	static final String TAG_SPOTTYPE_ID = "spottypeid";
	static final String TAG_LATITUDE = "latitude";
	static final String TAG_LONGITUDE = "longitude";
	static final String TAG_USERID = "userid";
	
	static MySpotsActivity instance = null;

	SpotsHelper db = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myspots_list);
		
		//ListView lv= (ListView) findViewById(android.R.id.list);
		final ListView lv = getListView();
		
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
			
		    @Override
		    public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int row, long arg3) {
		    	AlertDialog.Builder builder = new AlertDialog.Builder(MySpotsActivity.instance);
				// Add the buttons
		    	TextView txtSpotId = (TextView) arg1.findViewById(R.id.uploadedspotId);
		    	TextView txtSpotTitle = (TextView) arg1.findViewById(R.id.uploadedspotTitle);
		    	TextView txtImagepath = (TextView) arg1.findViewById(R.id.uploadedspotImagePath);
		    	TextView txtSpotDescription = (TextView) arg1.findViewById(R.id.uploadedspotDescription);
		    	TextView txtSpotType = (TextView) arg1.findViewById(R.id.uploadedspotType);
		    	TextView txtSpotTypeId = (TextView) arg1.findViewById(R.id.uploadedSpotTypeId);
		    	TextView txtSpotLatitude = (TextView) arg1.findViewById(R.id.uploadedspotLatitude);
		    	TextView txtSpotLongitude = (TextView) arg1.findViewById(R.id.uploadedspotLongitude);
		    	
		    	final String spotIdHidden = txtSpotId.getText().toString();
		     	final String spotTitle = txtSpotTitle.getText().toString();
		     	final String spotImage = txtImagepath.getText().toString();
		     	final String spotDescription = txtSpotDescription.getText().toString();
		     	final String spotType = txtSpotType.getText().toString();
		     	final String spotTypeId= txtSpotTypeId.getText().toString();
		     	final String spotLatitude = txtSpotLatitude.getText().toString();
		     	final String spotLongitude = txtSpotLongitude.getText().toString();
		     	
		     	/*
		        Log.d(TAG,""+spotIdHidden);
		        Log.d(TAG,""+spotTitle);
		        Log.d(TAG,""+spotImage);
		        Log.d(TAG,""+spotDescription);
		        Log.d(TAG,""+spotType);
		        Log.d(TAG,"typeid="+spotTypeId);
		        Log.d(TAG,""+spotLatitude);
		        Log.d(TAG,""+spotLongitude);
		        */
				builder.setTitle(R.string.options)
			           .setItems(R.array.myspots_options_array, new DialogInterface.OnClickListener() {
			               public void onClick(DialogInterface dialog, int which) {
			            	   if(which == 0){
			            		   	Intent intentUploadService = new Intent(MySpotsActivity.this, UploadMediaService.class);
				   	        		intentUploadService.putExtra("imagepath", spotImage);
				   	        		intentUploadService.putExtra("spotname", spotTitle);
				   	        		intentUploadService.putExtra("spotdescription", spotDescription);
				   	        		intentUploadService.putExtra("spottypeId", ""+spotTypeId);
				   	        		intentUploadService.putExtra("spottype", spotType);
				   	        		intentUploadService.putExtra("userid", ""+User.current().getID());
				   	        		intentUploadService.putExtra("latitude", spotLatitude);
				   	        		intentUploadService.putExtra("longitude", spotLongitude);
				   	        		intentUploadService.putExtra("dbspotid", spotIdHidden);
				   	        		startService(intentUploadService);
				   	        		Intent intent = new Intent(MySpotsActivity.this, MainActivity.class);
				   	        		intent.putExtra("loading",1);
				   					startActivity(intent);   
				   					overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
				   					finish();
					        	}
					        	else if(which == 1){
					        		List<Spot> list = db.getAllSpots();
									for (int i = 0; i < list.size(); i++) {
										Log.d(TAG,spotIdHidden+" - "+list.get(i).getId());
										if(Integer.parseInt(spotIdHidden) == list.get(i).getId()){
											db.deleteSpot(list.get(i));
											outboxList = new ArrayList<HashMap<String, String>>();
											new LoadSpots().execute();
										}
									}
					        	}
			               }
			           	});
				// Create the AlertDialog
				AlertDialog dialog = builder.create();
				dialog.show();
				/*
		    	PopupMenu popup = new PopupMenu(MySpotsActivity.this, arg1);  
		        //Inflating the Popup using xml file  
		    	TextView tv = (TextView) arg1.findViewById(R.id.uploadedspotId);
		     	final String spotIdHidden = tv.getText().toString();
		     	
		        Log.d(TAG,""+spotIdHidden);
		        popup.getMenuInflater().inflate(R.menu.popup_menu_myspots, popup.getMenu());  
		        
		        //registering popup with OnMenuItemClickListener  
		        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {  
			        public boolean onMenuItemClick(MenuItem item) {  
			        	if(item.getTitle().equals("Retry upload")){
			        		
			        	}
			        	else if(item.getTitle().equals("Delete spot")){
			        		List<Spot> list = db.getAllSpots();
							for (int i = 0; i < list.size(); i++) {
								Log.d(TAG,spotIdHidden+" - "+list.get(i).getId());
								if(spotIdHidden.equals(""+list.get(i).getId())){
									Log.d(TAG,"LIST="+list.get(i));
									db.deleteSpot(list.get(i));
									outboxList = new ArrayList<HashMap<String, String>>();
									new LoadSpots().execute();
								}
								
							}
			        	}
			        	//Toast.makeText(MySpotsActivity.this,item.getItemId()+" You Clicked : " + item.getTitleCondensed(),Toast.LENGTH_SHORT).show();  
			            return true;  
			        }  
		        });  
		        popup.show();//showing popup menu  
		        */
				return true;
		        // your code
		    }
		});
		
		
		
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		// get all spots
		//Spot uno = new Spot();
		
		db = new SpotsHelper(this);
		//db.addSpot(new Spot("Name", "Description",1,"Latitude","Longitude",2,"acas.mp4"));   
		
		// Hashmap for ListView
        //outboxList = new ArrayList<HashMap<String, String>>();
        
        // Loading OUTBOX in Background Thread
        //new LoadOutbox().execute();
		instance = this;
	}

	@Override
    public synchronized void onResume() {
        super.onResume();
     // Hashmap for ListView
        outboxList = new ArrayList<HashMap<String, String>>();
        
        //int type = -1;
        //OUTBOX_URL = "http://api.myhotspotz.net/app/getlatestspots/"+type;
		new LoadSpots().execute();
		Const.v(TAG, "+ ON RESUME +");
    }
	
	public static final String SHOWITEMINTENT_EXTRA_FETCHROWID = "fetchRow";
    public static final int ACTIVITY_SHOWITEM = 0; /*Intent request user index*/

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id){
    	
    	TextView tv = (TextView) v.findViewById(R.id.uploadedspotId);
    	String spotIdHidden = tv.getText().toString();
    	
    	//Log.d(TAG,"SpotId = "+spotIdHidden);
    	Intent spotIntent = new Intent(this, SpotActivity.class);
    	spotIntent.putExtra("SpotID",spotIdHidden);
        startActivity(spotIntent);
        overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
        
        Log.d(TAG,""+spotIdHidden);
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
			pDialog = new ProgressDialog(MySpotsActivity.this);
			pDialog.setMessage(getString(R.string.loading_spots));
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting Outbox JSON
		 * */
		protected String doInBackground(String... args) {
			loadSpots = true;
			try {
				List<Spot> list = db.getAllSpots();
				
				
				//db.deleteSpot(list.get(1));
				HashMap<String, String> map;
				for (int i = 0; i < list.size(); i++) {
					map = new HashMap<String, String>();
					map.put(TAG_ID, ""+list.get(i).getId());
					map.put(TAG_NAME, list.get(i).getName());
					map.put(TAG_IMAGE,list.get(i).getImagepath());
					map.put(TAG_DESCRIPTION, list.get(i).getDescription());
					map.put(TAG_SPOTTYPE, ""+list.get(i).getType());
					map.put(TAG_SPOTTYPE_ID, ""+list.get(i).getTypeId());
					map.put(TAG_LATITUDE, list.get(i).getLatitude());
					map.put(TAG_LONGITUDE, list.get(i).getLongitude());
					map.put(TAG_USERID, ""+list.get(i).getUserid());
					Log.d(TAG,"typeid = " + list.get(i).getTypeId());
					Log.d(TAG,"LIST="+list.get(i));
					outboxList.add(map);
				}
				/*
				// adding each child node to HashMap key => value
				
				*/
				// adding HashList to ArrayList
				
			
				
			} catch (RuntimeException e){
				loadSpots = false;
				Log.d(TAG,"RuntimeException");
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
			if(loadSpots){
				runOnUiThread(new Runnable() {
					public void run() {
						adapter = new MySpotsViewAdapter(MySpotsActivity.this, outboxList);
						// updating listview
						setListAdapter(adapter);
						
					}
				});
			}
			

		}
	
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
		    default:
		    	onBackPressed();
		    	return super.onOptionsItemSelected(item);
        }
    }
	
}