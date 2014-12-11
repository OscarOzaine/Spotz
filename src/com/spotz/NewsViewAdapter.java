package com.spotz;

import com.spotz.camera.ImageLoader;
import com.spotz.gen.R;
import com.spotz.utils.Const;
import com.spotz.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
	TextView txtid;
	TextView txtname;
	TextView txtcreated_at;
	TextView txtcityname;
	ImageView imgSpot;
	VideoView vidSpot;
	TextView txtemail;
	TextView txtdescription;
	TextView txtspottype;
	TextView txtlikes;
	TextView txtdislikes;

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
		txtemail = (TextView) itemView.findViewById(R.id.spotUser);
		txtdescription = (TextView) itemView.findViewById(R.id.spotDescription);
		txtspottype = (TextView) itemView.findViewById(R.id.spotType);
		txtlikes = (TextView) itemView.findViewById(R.id.spotLikes);
		txtdislikes = (TextView) itemView.findViewById(R.id.spotDislikes);


		// Locate the ImageView in listview_item.xml
		imgSpot = (ImageView) itemView.findViewById(R.id.spotImage);
		vidSpot = (VideoView) itemView.findViewById(R.id.spotVideo);

		txtid.setText(resultp.get(NewsActivity.TAG_ID));
		txtname.setText(resultp.get(NewsActivity.TAG_NAME));
		txtcreated_at.setText(resultp.get(NewsActivity.TAG_CREATED_AT));
		txtcityname.setText(resultp.get(NewsActivity.TAG_CITYNAME));
		txtemail.setText(resultp.get(NewsActivity.TAG_EMAIL));
		txtdescription.setText(resultp.get(NewsActivity.TAG_DESCRIPTION));
		txtspottype.setText(resultp.get(NewsActivity.TAG_SPOTTYPE));
		txtlikes.setText(resultp.get(NewsActivity.TAG_LIKES));
		txtdislikes.setText(resultp.get(NewsActivity.TAG_DISLIKES));
		
		mediaPath = resultp.get(NewsActivity.TAG_IMAGE);
		Log.d(TAG,"MediaPath="+mediaPath);
		if(Utils.isVideo(mediaPath)){
				imgSpot.setVisibility(View.GONE);
				vidSpot.setVisibility(View.VISIBLE);
				MediaController mediaController= new MediaController(NewsActivity.instance);
			    mediaController.setAnchorView(vidSpot);
			    vidSpot.setVisibility(View.VISIBLE);
			    vidSpot.setFocusable(true);
			    vidSpot.setFocusableInTouchMode(true);
			    vidSpot.requestFocus();
			    Uri uri=Uri.parse(mediaPath);
			    vidSpot.setMediaController(mediaController);
			    vidSpot.setVideoURI(uri);  
		}
		else{
			imgSpot.setVisibility(View.VISIBLE);
			vidSpot.setVisibility(View.GONE);
			imageLoader.DisplayImage(mediaPath, imgSpot);
			//Log.d("DisplayImage","mediaPath = "+mediaPath);
		}
		
		
		//////////////////
		/*
		final String result = getItem(position);   
		
		final boolean big = position % 3 == 0;
		final ViewHolder viewHolder;
		// Check if an existing view is being reused, otherwise inflate the view
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(res_id, parent, false);
			
			viewHolder = new ViewHolder();
			viewHolder.img = (ImageView) convertView;
			convertView.setTag(viewHolder);
		} else {
			//if it was already reused 
			viewHolder = (ViewHolder) convertView.getTag();
		}
	    // We change to a loding image bitmap, so it doesn't look like repeated images
	    viewHolder.img.setImageBitmap(BitmapLoader.getImage(ctx, 
	    		big?R.drawable.loading_big:R.drawable.loading_small, true));
	    viewHolder.position = position;
	    
	    // Populate the data into the template view using the data object       
	    ImageDownloaderTask idt = new ImageDownloaderTask(ctx, viewHolder, position);
	    idt.execute(result,big?"BIG":null);
	       
	    return convertView;
		*/
		////////////////////
		
		
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

		return itemView;
	}
	
	
}