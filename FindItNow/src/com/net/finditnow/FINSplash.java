package com.net.finditnow;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

public class FINSplash extends Activity {

	protected boolean active = true;
	protected int splashTime = 1500; // time to display the splash screen in ms
	protected Thread splashThread;

	private HashMap<String, Integer> splashes;
	private ArrayList<String> campuses;
	private Cursor cursor;
	
	private boolean manual;
	
	private FINDatabase db;

	private String campus;
	private int rid;
	private String campusJson;
	private ProgressDialog myDialog;
	private ProgressDialog regionDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fin_splash);
		manual = false;
		
		db = new FINDatabase(this);

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		rid = prefs.getInt("rid", 0);

		if (rid == 0) {
			// Acquire a reference to the system Location Manager
			final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
			
			// Define a listener that responds to location updates
			final LocationListener locationListener = new LocationListener() {
			    public void onLocationChanged(Location location) {
					SharedPreferences.Editor editor = prefs.edit();
					
					editor.putInt("location_lat", (int)(location.getLatitude()*1E6));
					editor.putInt("location_lon", (int)(location.getLatitude()*1E6));				
					editor.commit();
					
	        		handler4.sendEmptyMessage(0);

					locationManager.removeUpdates(this);
			    }
	
			    public void onStatusChanged(String provider, int status, Bundle extras) {}
	
			    public void onProviderEnabled(String provider) {}
	
			    public void onProviderDisabled(String provider) {}
			};
	
			// Register the listener with the Location Manager to receive location updates
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
			
			myDialog = new ProgressDialog(this);
			myDialog.setTitle("Welcome to FIN");
			myDialog.setMessage("Please wait while we detect the regions nearest you...");
			myDialog.setIcon(R.drawable.icon);
	        myDialog.setCancelable(false);
			myDialog.setButton("Manually Choose", new DialogInterface.OnClickListener() {

	            public void onClick(DialogInterface dialog, int which) {
					locationManager.removeUpdates(locationListener);
					manual=true;
					myDialog.dismiss();
	        		handler4.sendEmptyMessage(0);
	            }
	         });
			myDialog.show();
		} else {
			Cursor cursor = db.getReadableDatabase().query("regions", null, "regions.rid = " + rid, null, null, null, null);
			cursor.moveToFirst();
			campus = cursor.getString(1);
			
			handler3.sendEmptyMessage(0);
		}

		// thread for displaying the SplashScreen
		splashThread = new Thread() {

			@Override
			public void run() {

				Intent myIntent = null;

				splashes = new HashMap<String, Integer>();
				splashes.put("University of Washington", R.drawable.uw_splash);
				splashes.put("Western Washington University", R.drawable.wwu_splash);

				// Set color theme (hardcoded for now).
				Cursor cursor = db.getReadableDatabase().query("colors", null, "colors.rid = " + prefs.getInt("rid", 0)+"", null, null, null, null);
				cursor.moveToFirst();
				String color = cursor.getString(1);
				
				FINTheme.setTheme(color, getBaseContext());
				
				// Set default location
				handler.sendEmptyMessage(0);

				// Check logged in status
				final String phone_id = Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);
				
				String loggedinstr = DBCommunicator.loggedIn(phone_id, getBaseContext());
				String categories = DBCommunicator.getCategories(getBaseContext());
				String buildings = DBCommunicator.getBuildings(prefs.getInt("rid", 0)+"", getBaseContext());

				boolean loggedin = loggedinstr.contains(getString(R.string.login_already));
				boolean readytostart = !(loggedinstr.equals(getString(R.string.timeout)) || categories.equals(getString(R.string.timeout)) 
						|| buildings.equals(getString(R.string.timeout)));

				myIntent = new Intent(getBaseContext(), FINHome.class);
				myIntent.addCategory("App Startup");
				myIntent.putExtra("campuses", campusJson);
				myIntent.putExtra("categories", categories);
				myIntent.putExtra("buildings", buildings);
				myIntent.putExtra("loggedin", loggedin);
				myIntent.putExtra("readytostart", readytostart);

				if (loggedin) {
					myIntent.putExtra("username", loggedinstr.substring(21, loggedinstr.length()));
				}
				try {
					int waited = 0;

					while(active && (waited < splashTime)) {
						sleep(100);
						if (active) {
							waited += 100;
						}
					}
				} catch (InterruptedException e) {
					// do nothing
				} finally {
					db.close();
					startActivity(myIntent);
					finish();
				}
			}
		};
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			active = false;
		}
		return true;
	}

	// Listener for campus popup
	private OnClickListener campus_listener = new OnClickListener() {

		public void onClick(DialogInterface dialog, int which) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());  
			SharedPreferences.Editor editor = prefs.edit();
			
			cursor.moveToPosition(which);
			
			campus = cursor.getString(1);
			editor.putInt("rid", cursor.getInt(0));
			editor.commit();

			splashThread.start();
		}
	};

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			ImageView image = (ImageView) findViewById(R.id.splash_img);
			image.setImageResource(splashes.get(campus));
		}
	};
	
	private Handler handler2 = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			regionDialog.dismiss();
			if (manual) {
				selectCampus();
			} else {
				cursor.moveToFirst();
				campus = cursor.getString(1);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(FINSplash.this);
				builder.setTitle("Region Selection");
				builder.setIcon(R.drawable.icon);
				builder.setMessage("We have detected your nearest campus/region as:\n\n" + campus +"\n\nIs this correct?");
				builder.setCancelable(false);
				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());  
						SharedPreferences.Editor editor = prefs.edit();						
						editor.putInt("rid", cursor.getInt(0));
						editor.commit();
										
						splashThread.start();
					}
				});
				builder.setNegativeButton("No, Let me choose", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {					
						selectCampus();
					}
				});
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		}
	};
	
	private void selectCampus() {
		AlertDialog.Builder builder = new AlertDialog.Builder(FINSplash.this);
		builder.setTitle("Select your campus or region");
		
		campuses = new ArrayList<String>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			campuses.add(cursor.getString(1));
			cursor.moveToNext();
		}
		
		builder.setItems((String[])campuses.toArray(new String[campuses.size()]), campus_listener);
		builder.setCancelable(false);		

		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private Handler handler3 = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			setContentView(R.layout.fin_splash);
			splashThread.start();
		}
	};
	
	private Handler handler4 = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (myDialog.isShowing()) {
				myDialog.dismiss();
			}
			
			regionDialog = ProgressDialog.show(FINSplash.this, "" , "Loading list of regions...", true);

			if (!connectionThread.isAlive()) {
				connectionThread.start();
			}
		}
	};
	
	Thread connectionThread = new Thread() {
		public void run() {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			campusJson = DBCommunicator.getRegions(prefs.getInt("location_lat", 0)+"", prefs.getInt("location_lon", 0)+"", getBaseContext());
			
    		if (campusJson.equals(getString(R.string.timeout))) {
    			failureHandler.sendEmptyMessage(0);
    		} else {
				JsonParser.parseRegionJson(campusJson, getBaseContext());
				cursor = db.getReadableDatabase().query("regions", null, null, null, null, null, null);
				cursor.moveToFirst();
				
				handler2.sendEmptyMessage(0);
    		}
		}
	};
	
	private Handler failureHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
    		ConnectionChecker conCheck = new ConnectionChecker(getBaseContext(), FINSplash.this);
    		conCheck.connectionError();
		}
	};
}
