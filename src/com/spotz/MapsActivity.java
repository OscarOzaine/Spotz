package com.spotz;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.spotz.gen.R;
import com.spotz.utils.Const;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class MapsActivity extends FragmentActivity
	implements OnMapReadyCallback {
	
	String TAG = "MapsActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.map);
	    MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
	    
	    mapFragment.getMapAsync(this);
	}
	
	@Override
	public void onMapReady(GoogleMap map) {
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Const.currentLatitude, Const.currentLongitude), 11));
		map.addMarker(new MarkerOptions()
        .position(new LatLng(0, 0))
        .title("Marker"));
	}

}