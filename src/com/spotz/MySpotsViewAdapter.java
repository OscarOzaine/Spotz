package com.spotz;

import com.spotz.MySpotsActivity.LoadSpots;
import com.spotz.camera.ImageLoader;
import com.spotz.database.Spot;
import com.spotz.database.SpotsHelper;
import com.spotz.gen.R;
import com.spotz.services.UploadMediaService;
import com.spotz.users.User;
import com.spotz.utils.Const;
import com.spotz.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData.Item;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

public class MySpotsViewAdapter extends BaseAdapter {

	// Declare Variables
	Context context;
	LayoutInflater inflater;
	ArrayList<HashMap<String, String>> data;
	HashMap<String, String> resultp = new HashMap<String, String>();
	String TAG = "MySpotsViewAdapter";
	String mediaPath = "";
	
	// Declare Variables
	TextView txtid, txtname, txtDescription, txtType, txtLatitude, txtLongitude, txtImagePath, txtTypeId;
	ImageView imgSpot;
	ImageButton imageSettings;
	SpotsHelper db = null;
	ArrayList<HashMap<String, String>> outboxList;
	MySpotsViewAdapter adapter;
	
	final String spotIdHidden 	= "";
 	final String spotTitle 		= "";
 	final String spotImage 		= "";
 	final String spotDescription = "";
 	final String spotType 		= "";
 	final String spotTypeId		= "";
 	final String spotLatitude 	= "";
 	final String spotLongitude	= "";
	
	public MySpotsViewAdapter(Context context,
			ArrayList<HashMap<String, String>> arraylist) {
		this.context = context;
		data = arraylist;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View itemView = inflater.inflate(R.layout.myspots_list_item, parent, false);
		
		resultp = data.get(position);
		// Locate the TextViews in listview_item.xml
		txtid = (TextView) itemView.findViewById(R.id.uploadedspotId);
		txtname = (TextView) itemView.findViewById(R.id.uploadedspotTitle);
		txtDescription = (TextView) itemView.findViewById(R.id.uploadedspotDescription);
		txtType = (TextView) itemView.findViewById(R.id.uploadedspotType);
		txtTypeId = (TextView) itemView.findViewById(R.id.uploadedSpotTypeId);
		txtLatitude = (TextView) itemView.findViewById(R.id.uploadedspotLatitude);
		txtLongitude = (TextView) itemView.findViewById(R.id.uploadedspotLongitude);
		txtImagePath = (TextView) itemView.findViewById(R.id.uploadedspotImagePath);
		imageSettings = (ImageButton) itemView.findViewById(R.id.spot_myspotssettings);
		
		// Locate the ImageView in listview_item.xml
		imgSpot = (ImageView) itemView.findViewById(R.id.uploadedspotImage);
		
		txtid.setText(resultp.get(MySpotsActivity.TAG_ID));
		txtname.setText(resultp.get(MySpotsActivity.TAG_NAME));
		txtDescription.setText(resultp.get(MySpotsActivity.TAG_DESCRIPTION));
		txtType.setText(resultp.get(MySpotsActivity.TAG_SPOTTYPE));
		txtLatitude.setText(resultp.get(MySpotsActivity.TAG_LATITUDE));
		txtLongitude.setText(resultp.get(MySpotsActivity.TAG_LONGITUDE));
		txtTypeId.setText(resultp.get(MySpotsActivity.TAG_SPOTTYPE_ID));
		
		mediaPath = resultp.get(MySpotsActivity.TAG_IMAGE);
		txtImagePath.setText(mediaPath);
		Log.d(TAG,"MediaPath="+mediaPath);
		
		if(Utils.isVideo(mediaPath)){
			imgSpot.setImageResource(R.drawable.ic_play_video);
		}
		else{
			imgSpot.setVisibility(View.VISIBLE);
			File imgFile = new  File(mediaPath);
			if(imgFile.exists()){
				final BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 8;

				Bitmap bm = BitmapFactory.decodeFile(imgFile.getAbsolutePath(),options);
				imgSpot.setImageBitmap(bm);
			}
			//imageLoader.DisplayImage(mediaPath, imgSpot);
		}
		
		
		// Capture ListView item click
		itemView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Opens a dialog to select if delete
			}
		});

		
		imageSettings.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(MySpotsActivity.instance);
		     	
		        Log.d(TAG,""+spotIdHidden);
				builder.setTitle(R.string.options)
			           .setItems(R.array.myspots_options_array, new DialogInterface.OnClickListener() {
			               public void onClick(DialogInterface dialog, int which) {
			            	   if(which == 0){
			            		   Intent intentUploadService = new Intent(MySpotsActivity.instance, UploadMediaService.class);
				   	        		intentUploadService.putExtra("imagepath", spotImage);
				   	        		intentUploadService.putExtra("spotname", spotTitle);
				   	        		intentUploadService.putExtra("spotdescription", spotDescription);
				   	        		intentUploadService.putExtra("spottypeId", ""+spotTypeId);
				   	        		intentUploadService.putExtra("spottype", spotType);
				   	        		intentUploadService.putExtra("userid", ""+User.current().getID());
				   	        		intentUploadService.putExtra("latitude", spotLatitude);
				   	        		intentUploadService.putExtra("longitude", spotLongitude);
				   	        		intentUploadService.putExtra("dbspotid", spotIdHidden);
				   	        		MySpotsActivity.instance.startService(intentUploadService);
				   	        		Intent intent = new Intent(MySpotsActivity.instance, MainActivity.class);
				   	        		intent.putExtra("loading",1);
				   	        		MySpotsActivity.instance.startActivity(intent);   
				   	        		MySpotsActivity.instance.overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
				   					//finish();
					        	}
					        	else if(which == 1){
					        		db = new SpotsHelper(MySpotsActivity.instance);
					        		List<Spot> list = db.getAllSpots();
									for (int i = 0; i < list.size(); i++) {
										Log.d(TAG,spotIdHidden+" - "+list.get(i).getId());
										
										if(Integer.parseInt(spotIdHidden) == list.get(i).getId()){
											Log.d(TAG,"LIST="+list.get(i));
											db.deleteSpot(list.get(i));
											//convertView.refreshDrawableState();
											MySpotsActivity.instance.onBackPressed();
											//outboxList = new ArrayList<HashMap<String, String>>();
											//notifyDataSetChanged();
										}
									}
					        	}
			               }
			           	});
				// Create the AlertDialog
				AlertDialog dialog = builder.create();
				dialog.show();
				
			} 

		});
		return itemView;
	}
	
	
	
}