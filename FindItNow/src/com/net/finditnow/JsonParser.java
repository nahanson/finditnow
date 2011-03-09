package com.net.finditnow;

/***
 * JsonParser.java by Chanel Huang
 * This class provides methods to parse a Json string into a HashMap
 * 
 * Json String:
 * [ {"lat": int, "long":int, "floor_names":[strings], "name":string} ,...]
 * 
 * 
 */

//packages for handling JSON
import java.util.HashMap;

import com.google.android.maps.GeoPoint;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

public class JsonParser {
	 /*
	 * Design Principle: Information Hiding
	 * These two arrays are only visible to this class.  Other module do not
	 * ever need to know the exact names of each data coming from the
	 * back-end database via JSON objects.
	 */
	//This is a string to keep track of the names of each piece of information in the
	//JSON array.
	private static final String[] LOCATION_NAMES = { "lat",
		"long",
		"floor_names",
		"info",
		"id"};
	private static final String[] BUILDING_NAMES = { "bid",
		"lat",
		"long",
		"name",
		"fid",
	"floor_names"};

	/**
	 * parse a json string into a map of GeoPoint to Building
	 *  
	 * @param json the json string representation of an array of building objects
	 * @return a map of location to its corresponding building object
	 */
	public static HashMap<GeoPoint, Building> parseBuildingJson(String json)
	{
		//used for parsing the JSON object
		Gson gson = new Gson();
		JsonStreamParser parser = new JsonStreamParser(json);
		JsonArray arr = parser.next().getAsJsonArray();

		//creates the map for information to be stored in
		HashMap<GeoPoint,Building> map = new HashMap<GeoPoint,Building>();

		for (int i = 0; i < arr.size(); i++)
		{
			if (arr.get(i).isJsonObject())
			{
				//Since the JsonArray contains whole bunch json array, we can get each one out
				JsonObject ob = arr.get(i).getAsJsonObject();
	
				//place the information in the map with BuildingID as key
				GeoPoint point = new GeoPoint( ob.get(BUILDING_NAMES[1]).getAsInt(),ob.get(BUILDING_NAMES[2]).getAsInt());
	
				//remove lat, long so it can be used for the Gson.fromJson
				ob.remove(BUILDING_NAMES[1]);
				ob.remove(BUILDING_NAMES[2]);
	
				//Log.i("log_tag", ob.toString());
	
				//converts a Json string directly to a building object
				Building build = gson.fromJson(ob,Building.class);
				
				//puts it in the map
				map.put(point, build);
			}
		}

		return map;
	}

	/**
	 * parses a Json Array into a map of locations and its corresponding CategoryItem
	 * 
	 * @param jsonArray jsonArray containing information
	 * @return HashMap<GeoPoint, CategoryItem> maps a location with its information
	 */
	public static HashMap<GeoPoint, CategoryItem> parseCategoryJson(String json)
	{
		//creates the map for information to be stored in
		HashMap<GeoPoint,CategoryItem> map = new HashMap<GeoPoint,CategoryItem>();
		
		if (json != null && !json.equals("")) {

			//String json = jsonArray.toString();
			//used for parsing the JSON object
			Gson gson = new Gson();
			JsonStreamParser parser = new JsonStreamParser(json);
			JsonArray arr = parser.next().getAsJsonArray();

			for (int i = 0; i < arr.size(); i++)
			{
				if (arr.get(i).isJsonObject())
				{
					//Since the JsonArray contains whole bunch json array, we can get each one out
					JsonObject ob = arr.get(i).getAsJsonObject();
	
					//place the information in the map with GeoPoint as key
					GeoPoint point = new GeoPoint( ob.get(LOCATION_NAMES[0]).getAsInt(),ob.get(LOCATION_NAMES[1]).getAsInt());
					CategoryItem item = new CategoryItem();

					//if the point is already in the map, get it out to add to it
					if (map.get(point) != null)
					{
						item = map.get(point);
					}
					
					
					if (ob.has(LOCATION_NAMES[2]))
					{
						JsonArray s = ob.get(LOCATION_NAMES[2]).getAsJsonArray();
						//the floor names associated with this point
						String[] flrNames = gson.fromJson(s,String[].class);
						if (flrNames.length == 0)
							item.addFloor_names("");
						for (String flr: flrNames)
							item.addFloor_names(flr);
					}
					if (ob.has(LOCATION_NAMES[3]))
					{
						String s = ob.get(LOCATION_NAMES[3]).getAsString();
						//the floor info associated with this point
						item.addInfo(s);
					}
					if (ob.has(LOCATION_NAMES[4]))
					{
						int id = ob.get(LOCATION_NAMES[4]).getAsInt();
						//the floor id associated with this point
						item.addId(id);
					}

					map.put(point, item);
				}
			}
		} 
		return map;
	}
}
