package com.spotz;

import com.spotz.camera.ImageLoader;
import com.spotz.database.Spot;
import com.spotz.database.SpotsHelper;
import com.spotz.gen.R;
import com.spotz.services.DeleteSpotService;
import com.spotz.services.LoginService;
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

public class NewsViewAdapter extends BaseAdapter {

	// Declare Variables
	Context context;
	LayoutInflater inflater;
	ArrayList<HashMap<String, String>> data;
	ImageLoader imageLoader;
	HashMap<String, String> resultp = new HashMap<String, String>();
	String TAG = "ListViewAdapter";
	String mediaPath = "";
	
	// Declare Variables
	TextView txtid, txtname, txtcreated_at, txtcityname, txtemail;
	TextView txtdescription, txtspottype, txtlikes, txtdislikes;
	ImageView imgSpot;
	ImageButton imageSettings;

	public NewsViewAdapter(Context context,
			ArrayList<HashMap<String, String>> arraylist) {
		this.context = context;
		data = arraylist;
		imageLoader = new ImageLoader(context);
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

		View itemView = inflater.inflate(R.layout.news_list_item, parent, false);
		// Get the position

		resultp = data.get(position);

		// Locate the TextViews in listview_item.xml
		txtid = (TextView) itemView.findViewById(R.id.spotId);
		txtname = (TextView) itemView.findViewById(R.id.spotTitle);
		txtcreated_at = (TextView) itemView.findViewById(R.id.spotCreatedat);
		txtcityname = (TextView) itemView.findViewById(R.id.spotCity);
		//txtemail = (TextView) itemView.findViewById(R.id.spotUser);
		txtdescription = (TextView) itemView.findViewById(R.id.spotDescription);
		txtspottype = (TextView) itemView.findViewById(R.id.spotType);
		//txtlikes = (TextView) itemView.findViewById(R.id.spotLikes);
		//txtdislikes = (TextView) itemView.findViewById(R.id.spotDislikes);
		imageSettings = (ImageButton) itemView.findViewById(R.id.spot_newssettings);

		// Locate the ImageView in listview_item.xml
		imgSpot = (ImageView) itemView.findViewById(R.id.spotImage);
		
		txtid.setText(resultp.get(NewsActivity.TAG_ID));
		txtname.setText(resultp.get(NewsActivity.TAG_NAME));
		//txtcreated_at.setText(resultp.get(NewsActivity.TAG_CREATED_AT));
		txtcityname.setText(resultp.get(NewsActivity.TAG_CITYNAME));
		//txtemail.setText(resultp.get(NewsActivity.TAG_EMAIL));
		txtdescription.setText(resultp.get(NewsActivity.TAG_DESCRIPTION));
		txtspottype.setText(resultp.get(NewsActivity.TAG_SPOTTYPE));
		//txtlikes.setText(resultp.get(NewsActivity.TAG_LIKES));
		//txtdislikes.setText(resultp.get(NewsActivity.TAG_DISLIKES));
		
		mediaPath = resultp.get(NewsActivity.TAG_IMAGE);
		//Log.d(TAG,"MediaPath="+mediaPath);
		if(Utils.isVideo(mediaPath)){
			imgSpot.setImageResource(R.drawable.ic_play_video);
		}
		else{
			imgSpot.setVisibility(View.VISIBLE);
			imageLoader.DisplayImage(mediaPath, imgSpot);
		}
		
		
		// Capture ListView item click
		itemView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Get the position
				resultp = data.get(position);
				Log.d(TAG,"Position: "+position);
				Log.d(TAG,resultp.get(NewsActivity.TAG_NAME));
				Intent intent = new Intent(context, SpotActivity.class);
				intent.putExtra("id", resultp.get(NewsActivity.TAG_ID));
				intent.putExtra("name", resultp.get(NewsActivity.TAG_NAME));
				intent.putExtra("created_at", resultp.get(NewsActivity.TAG_CREATED_AT));
				intent.putExtra("cityname", resultp.get(NewsActivity.TAG_CITYNAME));
				intent.putExtra("email", resultp.get(NewsActivity.TAG_EMAIL));
				intent.putExtra("description", resultp.get(NewsActivity.TAG_DESCRIPTION));
				intent.putExtra("image", resultp.get(NewsActivity.TAG_IMAGE));
				intent.putExtra("spottype", resultp.get(NewsActivity.TAG_SPOTTYPE));
				intent.putExtra("likes", resultp.get(NewsActivity.TAG_LIKES));
				intent.putExtra("dislikes", resultp.get(NewsActivity.TAG_DISLIKES));
				intent.putExtra("longitude", resultp.get(NewsActivity.TAG_LONGITUDE));
				intent.putExtra("latitude", resultp.get(NewsActivity.TAG_LATITUDE));
				context.startActivity(intent);
			}
		});
	
		imageSettings.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				resultp = data.get(position);
				AlertDialog.Builder builder = new AlertDialog.Builder(NewsActivity.instance);
				if(resultp.get(NewsActivity.TAG_EMAIL).equals(User.current().getEmail())){
					builder.setTitle(R.string.options)
			           .setItems(R.array.mynews_options_array, new DialogInterface.OnClickListener() {
			               public void onClick(DialogInterface dialog, int which) {
			            	   if(which == 0){
				            		// Get the position
				       				resultp = data.get(position);
				       				//Log.d(TAG,"Position: "+position);
				       				//Log.d(TAG,resultp.get(NewsActivity.TAG_NAME));
				       				Intent intent = new Intent(context, SpotActivity.class);
				       				intent.putExtra("id", resultp.get(NewsActivity.TAG_ID));
				       				intent.putExtra("name", resultp.get(NewsActivity.TAG_NAME));
				       				intent.putExtra("created_at", resultp.get(NewsActivity.TAG_CREATED_AT));
				       				intent.putExtra("cityname", resultp.get(NewsActivity.TAG_CITYNAME));
				       				intent.putExtra("email", resultp.get(NewsActivity.TAG_EMAIL));
				       				intent.putExtra("description", resultp.get(NewsActivity.TAG_DESCRIPTION));
				       				intent.putExtra("image", resultp.get(NewsActivity.TAG_IMAGE));
				       				intent.putExtra("spottype", resultp.get(NewsActivity.TAG_SPOTTYPE));
				       				intent.putExtra("likes", resultp.get(NewsActivity.TAG_LIKES));
				       				intent.putExtra("dislikes", resultp.get(NewsActivity.TAG_DISLIKES));
				       				intent.putExtra("longitude", resultp.get(NewsActivity.TAG_LONGITUDE));
				       				intent.putExtra("latitude", resultp.get(NewsActivity.TAG_LATITUDE));
				       				context.startActivity(intent);
					        	}
					        	else if(which == 1){
					        		Intent intentDeleteSpot = new Intent(NewsActivity.instance, DeleteSpotService.class);
					        		intentDeleteSpot.putExtra("spotid", ""+resultp.get(NewsActivity.TAG_ID));
					        		NewsActivity.instance.startService(intentDeleteSpot);
					        	}
			               }
			           	});
				}
				else{
					builder.setTitle(R.string.options)
			           .setItems(R.array.news_options_array, new DialogInterface.OnClickListener() {
			               public void onClick(DialogInterface dialog, int which) {
			            	   if(which == 0){
				            		// Get the position
				       				resultp = data.get(position);
				       				//Log.d(TAG,"Position: "+position);
				       				//Log.d(TAG,resultp.get(NewsActivity.TAG_NAME));
				       				Intent intent = new Intent(context, SpotActivity.class);
				       				intent.putExtra("id", resultp.get(NewsActivity.TAG_ID));
				       				intent.putExtra("name", resultp.get(NewsActivity.TAG_NAME));
				       				intent.putExtra("created_at", resultp.get(NewsActivity.TAG_CREATED_AT));
				       				intent.putExtra("cityname", resultp.get(NewsActivity.TAG_CITYNAME));
				       				intent.putExtra("email", resultp.get(NewsActivity.TAG_EMAIL));
				       				intent.putExtra("description", resultp.get(NewsActivity.TAG_DESCRIPTION));
				       				intent.putExtra("image", resultp.get(NewsActivity.TAG_IMAGE));
				       				intent.putExtra("spottype", resultp.get(NewsActivity.TAG_SPOTTYPE));
				       				intent.putExtra("likes", resultp.get(NewsActivity.TAG_LIKES));
				       				intent.putExtra("dislikes", resultp.get(NewsActivity.TAG_DISLIKES));
				       				intent.putExtra("longitude", resultp.get(NewsActivity.TAG_LONGITUDE));
				       				intent.putExtra("latitude", resultp.get(NewsActivity.TAG_LATITUDE));
				       				context.startActivity(intent);
					        	}
			               }
			           	});
				}
				
				// Create the AlertDialog
				AlertDialog dialog = builder.create();
				dialog.show();
				
			} 

		});

		return itemView;
	}
	
	
}