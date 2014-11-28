package com.spotz;

import com.example.androidhive.R;
import com.spotz.camera.ImageLoader;
import com.spotz.utils.Const;
import java.util.ArrayList;
import java.util.HashMap;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NewsViewAdapter extends BaseAdapter {

	// Declare Variables
	Context context;
	LayoutInflater inflater;
	ArrayList<HashMap<String, String>> data;
	ImageLoader imageLoader;
	HashMap<String, String> resultp = new HashMap<String, String>();
	String TAG = "ListViewAdapter";

	// Declare Variables
	TextView txtid;
	TextView txtname;
	TextView txtcreated_at;
	TextView txtcityname;
	ImageView imgSpot;
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

	@SuppressLint("ViewHolder") public View getView(final int position, View convertView, ViewGroup parent) {



		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View itemView = inflater.inflate(R.layout.outbox_list_item, parent, false);
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

		txtid.setText(resultp.get(NewsActivity.TAG_ID));
		txtname.setText(resultp.get(NewsActivity.TAG_NAME));
		txtcreated_at.setText(resultp.get(NewsActivity.TAG_CREATED_AT));
		txtcityname.setText(resultp.get(NewsActivity.TAG_CITYNAME));
		txtemail.setText(resultp.get(NewsActivity.TAG_EMAIL));
		txtdescription.setText(resultp.get(NewsActivity.TAG_DESCRIPTION));
		txtspottype.setText(resultp.get(NewsActivity.TAG_SPOTTYPE));
		txtlikes.setText(resultp.get(NewsActivity.TAG_LIKES));
		txtdislikes.setText(resultp.get(NewsActivity.TAG_DISLIKES));


		// Capture position and set results to the ImageView
		// Passes flag images URL into ImageLoader.class
		
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize=8;      // 1/8 of original image
		Bitmap bitmap = imageLoader.DisplayImage(resultp.get(NewsActivity.TAG_IMAGE), imgSpot);
		
		
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
				context.startActivity(intent);
			}
		});

		return itemView;
	}
}