/***
 * Get.java by Eric Hare
 * This class defines our Retrieve Locations interface with the PHP layer
 * It is called to retrieve items from the database, including categories and buildings
 */

package com.net.finditnow;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.util.Log;

import com.google.android.maps.GeoPoint;

public class Get {

	// A Constant representing the location of the root of the get files
	private static final String GET_LOCATIONS_ROOT = "http://yinnopiano.com/fin/";

	/**
	 * This method processes a request to retrieve locations from the database
	 * It is used for plotting the locations properly on the map
	 * 
	 * @param category The category of items to retrieve
	 * @param item If the category is supplies, the type of supplies to retrieve
	 * @param location The location of the user making the request
	 * 
	 * @return A JSONArray of locations, categories, or buildings from the database
	 */
	public static String requestFromDB(String category, String item, GeoPoint location, Context context) {

		// Initialize input stream and response variables
		String data = "";
		InputStream iStream = null;

		// Attempt to make the HTTPPOST to the given location
		// DESIGN PATTERN: Exceptions.  In Get/Update/Create, we catch any exception in PHP communication
		//				   This also allows us to localize errors that occur during the process
		try {
			String suffix = (location == null? (category == null? "getCategories.php" : "getBuildings.php") : (category.contains(" ")? "getAllLocations.php" : "getLocations.php"));
			
			HttpPost httppost = new HttpPost(GET_LOCATIONS_ROOT + suffix);

			// If the location is not null, this is a request for items in a category
			if (location != null) {

				List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();		  			
				nameValuePairs.add(new BasicNameValuePair("cat", category));
				Log.v("asdfksadfilsd", category);

				// If the itemName is not null, this is a request for school supplies
				if (item != null) {
					nameValuePairs.add(new BasicNameValuePair("item", item));
				}

				// Add the lat and long of the user's location
				nameValuePairs.add(new BasicNameValuePair("lat", location.getLatitudeE6()+""));
				nameValuePairs.add(new BasicNameValuePair("long", location.getLongitudeE6()+""));

				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			}
			HttpParams httpParameters = new BasicHttpParams();
			
			// Set the timeout in milliseconds until a connection is established.
			int timeoutConnection = 5000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			
			// Set the default socket timeout (SO_TIMEOUT) 
			// in milliseconds which is the timeout for waiting for data.
			int timeoutSocket = 5000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			
			DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
			HttpResponse httpResponse = httpClient.execute(httppost);
			
			HttpEntity entity = httpResponse.getEntity();
			iStream = entity.getContent();
		} catch(Exception e) {
			Log.e("log_tag", "Error in http connection " + e.toString());
		}

		// Attempt to convert the response into a string
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(iStream,"iso-8859-1"),8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			iStream.close();

			data = sb.toString();
		} catch(Exception e) {
			Log.e("log_tag", "Error converting result " + e.toString());
			return context.getString(R.string.timeout);
		}
		Log.i("Get",data);
		return data.trim();
	}
}
