package com.example.maps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity1 extends FragmentActivity implements
		OnConnectionFailedListener, ConnectionCallbacks,
		com.google.android.gms.location.LocationListener {
	GoogleMap gmap;
	Button bGo;
	EditText editlocation;
	TextView tvlocation;
	SupportMapFragment mapFragment;
	GoogleApiClient gac;
	Location mLastLocation, l;
	private LocationRequest mLocationRequest;
	ArrayList<Marker> markers = new ArrayList<Marker>();
	Polygon shape;
	static final int POLYGON_POINTS = 3;
	Circle circle;
	LatLng ll;
	Polyline line;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLocationRequest = LocationRequest.create()
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
				.setInterval(5 * 1000) // 5 seconds, in milliseconds
				.setFastestInterval(1 * 1000); // 1 second, in milliseconds
		if (gac == null) {
			gac = new GoogleApiClient.Builder(this)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.addApi(LocationServices.API).build();
		}

		bGo = (Button) findViewById(R.id.bgotolocation);
		tvlocation = (TextView) findViewById(R.id.tvformap);

		if (servicsOK()) {
			setContentView(R.layout.activity_map);
			if (init()) {
				Toast.makeText(this, "ready to map", Toast.LENGTH_SHORT).show();
				/*
				 * gmap.setMyLocationEnabled(true);//this will search for
				 * gps..hence batteryloss
				 */
				editlocation = (EditText) findViewById(R.id.etmaps2);
				gotoLocation(28.6499225, 77.4165908, 15);
			} else {
				Toast.makeText(this, "not working", Toast.LENGTH_SHORT).show();
			}

		} else {
			setContentView(R.layout.activity_main);
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		MapSates ms = new MapSates(this);
		ms.saveState(gmap);
		gac.disconnect();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		gac.connect();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MapSates ms = new MapSates(this);
		CameraPosition position = ms.retrieve(gmap);
		if (position != null) {
			CameraUpdate cu = CameraUpdateFactory.newCameraPosition(position);
			gmap.animateCamera(cu);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub

		switch (item.getItemId()) {
		case R.id.nonemap:
			gmap.setMapType(GoogleMap.MAP_TYPE_NONE);
			break;
		case R.id.normalmap:
			gmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			break;
		case R.id.hybridmap:
			gmap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			break;
		case R.id.satellitemap:
			gmap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			break;
		case R.id.terrainmap:
			gmap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
			break;
		case R.id.maplastloc:
			mLastLocation = LocationServices.FusedLocationApi
					.getLastLocation(gac);
			// Toast.makeText(this,mLastLocation.getLatitude()+" hgv"
			// ,Toast.LENGTH_SHORT).show();
			if (mLastLocation == null) {
				LocationServices.FusedLocationApi.requestLocationUpdates(gac,
						mLocationRequest, this);

			} else {
				handlenewlocation(mLastLocation);
			}
			break;
		}
		return true;

	}

	public boolean servicsOK() {
		int isAvailable = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (isAvailable == ConnectionResult.SUCCESS) {
			return true;
		} else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
			Dialog dialogerror = GooglePlayServicesUtil.getErrorDialog(
					isAvailable, this, 0000);
			dialogerror.show();
		} else {
			Toast.makeText(this, "cant connect to google play services",
					Toast.LENGTH_SHORT).show();
		}
		return false;
	}

	private boolean init() {
		if (mapFragment == null) {
			mapFragment = (SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map);
			gmap = mapFragment.getMap();
			if (gmap != null) {
				gmap.setOnMapLongClickListener(new OnMapLongClickListener() {

					@Override
					public void onMapLongClick(LatLng ll) {
						Geocoder gc = new Geocoder(MainActivity1.this);
						try {
							List<Address> list = gc.getFromLocation(
									ll.latitude, ll.longitude, 1);
							Address add = list.get(0);
							MainActivity1.this.setMarker(add.getLocality(),
									ll.latitude, ll.longitude,
									add.getCountryName());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return;
						}

					}
				});
				gmap.setOnMarkerDragListener(new OnMarkerDragListener() {

					@Override
					public void onMarkerDragStart(Marker arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onMarkerDragEnd(Marker arg0) {
						// TODO Auto-generated method stub
						Geocoder gc = new Geocoder(MainActivity1.this);

						LatLng ll = arg0.getPosition();

						List<Address> list = null;
						try {
							list = gc.getFromLocation(ll.latitude,
									ll.longitude, 1);
						} catch (Exception e) {
							e.printStackTrace();
						}
						Address add = list.get(0);
						arg0.setTitle(add.getLocality());
						arg0.setSnippet(add.getCountryName());
						arg0.showInfoWindow();

					}

					@Override
					public void onMarkerDrag(Marker arg0) {
						// TODO Auto-generated method stub

					}
				});
				gmap.setOnMarkerClickListener(new OnMarkerClickListener() {

					@Override
					public boolean onMarkerClick(Marker marker) {
						String msg = marker.getTitle() + " , "
								+ marker.getPosition().latitude + " ,"
								+ marker.getPosition().longitude + ","
								+ marker.getSnippet();
						Toast.makeText(MainActivity1.this, msg,
								Toast.LENGTH_LONG).show();
						return false;
					}
				});
				gmap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

					@Override
					public View getInfoContents(Marker arg0) {

						return null;
					}

					@Override
					public View getInfoWindow(Marker marker) {
						View v = getLayoutInflater().inflate(
								R.layout.info_window, null);
						ImageView iv = (ImageView) v
								.findViewById(R.id.ivformapinfo);
						TextView tvloc = (TextView) v
								.findViewById(R.id.tvlocoflocation);
						TextView tvlat = (TextView) v
								.findViewById(R.id.tvlatoflocation);
						TextView tvlng = (TextView) v
								.findViewById(R.id.tvlngoflocation);
						TextView tvcoun = (TextView) v
								.findViewById(R.id.tvcountryoflocation);
						LatLng ll = marker.getPosition();
						tvloc.setText(marker.getTitle());
						tvlat.setText("Latitude:" + ll.latitude);
						tvlng.setText("Longitutude:" + ll.longitude);
						tvcoun.setText(marker.getSnippet());

						return v;
					}

				});
			}
		}
		return (gmap != null);
	}

	private void gotoLocation(double lat, double lng, float zoom) {
		LatLng ll = new LatLng(lat, lng);
		CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(ll, zoom);
		gmap.animateCamera(cu);
	}

	public void geoLocation(View v) {
		String location = editlocation.getText().toString();
		hidetextkeyboard(v);
		Geocoder gc = new Geocoder(this);
		try {
			List<Address> add = gc.getFromLocationName(location, 1);
			Address ad = add.get(0);
			String country = ad.getCountryName();
			String locality = ad.getLocality();
			Double lat = ad.getLatitude();
			Double lng = ad.getLongitude();
			Toast.makeText(this, locality, Toast.LENGTH_SHORT).show();
			gotoLocation(lat, lng, 15);
			setMarker(locality, lat, lng, country);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void hidetextkeyboard(View v) {
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "connected to last location", Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		handlenewlocation(mLastLocation);
	}

	public void handlenewlocation(Location loca) {
		double lat1 = loca.getLatitude();
		double lng1 = loca.getLongitude();
		LatLng ll = new LatLng(lat1, lng1);
		CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(ll, 15);
		gmap.animateCamera(cu);
	}

	private void setMarker(String locality, double lat, double lng,
			String country) {
		ll = new LatLng(lat, lng);
		MarkerOptions options = new MarkerOptions()
				.title(locality)
				.position(ll)
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
				.snippet(country).draggable(true);
		int s = markers.size();
		if (s == 0) {
			markers.add(gmap.addMarker(options));
			drawcircle(ll);
		} else if (s == 1) {
			markers.add(gmap.addMarker(options));
			removeeverythingforcircle();
			drawline();
		} else if (s == 2) {
			markers.add(gmap.addMarker(options));
			removeEverythingforline();
			drawpolygon();
		} else if (s == 3) {
			removeEverythingforpolygon();
			markers.add(gmap.addMarker(options));
			drawcircle(ll);
		}
	}

	private void drawcircle(LatLng ll) {
		// TODO Auto-generated method stub
		CircleOptions options = new CircleOptions().center(ll).radius(1000)
				.fillColor(0x330000FF).strokeColor(Color.BLUE).strokeWidth(3);
		circle = gmap.addCircle(options);

	}

	private void removeeverythingforcircle() {
		// TODO Auto-generated method stub
		circle.remove();
		circle = null;
	}

	@SuppressWarnings("static-access")
	private void drawline() { // TODO Auto-generated method stub
		PolylineOptions options = new PolylineOptions()
				.add(markers.get(0).getPosition())
				.add(markers.get(1).getPosition()).width(5).color(Color.RED);
		line = gmap.addPolyline(options);
	/*	float[] results;
		l.distanceBetween(markers.get(0).getPosition().latitude,markers.get(0).getPosition().longitude,markers.get(1).getPosition().latitude,markers.get(0).getPosition().longitude,results[0]);
		float distance=0;
		int i =0;
		while(results[i]>0){
			distance=distance+results[i];
		}
		Toast.makeText(MainActivity1.this, "distance", Toast.LENGTH_LONG).show();
	*/}

	private void removeEverythingforline() { // TODO Auto-generated method stub

		line.remove();

	}

	private void drawpolygon() { // TODO Auto-generated method stub
		PolygonOptions option = new PolygonOptions().fillColor(0x330000FF)
				.strokeWidth(5).strokeColor(Color.BLUE);
		for (int i = 0; i < POLYGON_POINTS; i++) {
			option.add(markers.get(i).getPosition());
		}
		shape = gmap.addPolygon(option);
	}

	private void removeEverythingforpolygon() {
		for (Marker marker : markers) {
			marker.remove();
		}
		markers.clear();
		shape.remove();
		shape = null;
	}
}
