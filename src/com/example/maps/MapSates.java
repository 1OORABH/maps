package com.example.maps;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.content.SharedPreferences;

public class MapSates {
	SharedPreferences mapStatePrefs;
	private static final String LATITUDE="latitude";
	private static final String LONGITUDE="longitude";
	private static final String ZOOM="zoom";
	private static final String TILT="tilt";
	private static final String BEARING="bearing";
	private static final String MAPSTATE="mapState";
	public MapSates(Context context){
		mapStatePrefs=context.getSharedPreferences("mapstate", context.MODE_PRIVATE);
		
	}
	public void saveState(GoogleMap map){
		SharedPreferences.Editor editor = mapStatePrefs.edit();
		CameraPosition cp = map.getCameraPosition();
		editor.putFloat(LATITUDE,(float) cp.target.latitude);
		editor.putFloat(LONGITUDE,(float) cp.target.longitude);
		editor.putFloat(ZOOM, cp.zoom);
		editor.putFloat(TILT, cp.tilt);
		editor.putFloat(BEARING, cp.bearing);
		editor.putInt(MAPSTATE, map.getMapType());
		editor.commit();
		
	}
	public CameraPosition retrieve(GoogleMap gmp){
		double lat =  mapStatePrefs.getFloat(LATITUDE, 0);
		if(lat==0){
			return null;
		}
			double lng = mapStatePrefs.getFloat(LONGITUDE, 0);
			LatLng ll = new LatLng(lat, lng);
			float zoom = mapStatePrefs.getFloat(ZOOM, 0);
			float tilt = mapStatePrefs.getFloat(TILT, 0);
			float bearing = mapStatePrefs.getFloat(BEARING, 0);
			int maptype = mapStatePrefs.getInt(MAPSTATE, 0);
			gmp.setMapType(maptype);
			CameraPosition position = new CameraPosition(ll, zoom, tilt, bearing);
			return position;
			
	
	}
			

}
