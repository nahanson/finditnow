package finditnow.apk;
/*
 * This class provides methods to parse a Json string into a HashMap
 * 
 * Json String:
 * [ {"lat": int, "long":int, "floor_names":[strings], "name":string} ,...]
 * 
 * TODO: need to have a map for the radius of a building for final release.
 * 
 */
import com.google.gson.*;
import com.google.android.maps.GeoPoint;
import java.util.Map;
import java.util.HashMap;

public class JsonParser {
	//This is a string to keep track of the names of each piece of information in the
	//JSON array.
	private static final String[] NAMES = { "lat",
								   "long",
								   "floor_names",
								   "name"};
	
	//parses a Json Array into a map of locations and its floor names
	public static Map<GeoPoint,String[]> parseJson(String json)
	{
		//used for parsing the JSON object
		Gson gson = new Gson();
		JsonStreamParser parser = new JsonStreamParser(json);
		JsonArray arr = parser.next().getAsJsonArray();
		
		//creates the map for information to be stored in
		Map<GeoPoint,String[]> map = new HashMap<GeoPoint,String[]>();
		

		for (int i = 0; i < arr.size(); i++)
		{
			//Since the JsonArray contains whole bunch json array, we can get each one out
			JsonObject ob = arr.get(i).getAsJsonObject();
			
			//some ways to get things out of a Json Object
			/*System.out.println("Building Name: "+ob.get("buildingName").getAsString());
			System.out.println("category Name: "+ob.get("category").getAsString());
			System.out.println("floor Num: "+ob.get("floorNum").getAsInt());
			System.out.println();*/
			
			//place the information in the map with GeoPoint as key
			GeoPoint point = new GeoPoint( ob.get(NAMES[0]).getAsInt(),ob.get(NAMES[1]).getAsInt());
			JsonArray s = ob.get(NAMES[2]).getAsJsonArray();
			//the floor names associated with this point
			String[] flrNames = gson.fromJson(s,String[].class);
			
			//if the point is not added before, we add it
			if (map.get(point) == null)
				map.put(point,flrNames);
			else
			{
				//if the point already has entries in map, we append it to the end.
				String[] temp = map.get(point);
				String[] newS = new String[temp.length+flrNames.length];
				int c = 0;
				for (; c < temp.length; c++)
					newS[c] = temp[c];
								
				for (int k = 0; k < flrNames.length; k++, c++)
				{
					newS[c] = flrNames[k];
					                   
				}
				map.put(point, newS);
			}
		}
		
		return map;
	}
	
	//a Json Array into a map of locations and its corresponding names
	public static Map<GeoPoint,String> parseNameJson(String json)
	{
	//	Gson gson = new Gson();
		//used to parse a json
		JsonStreamParser parser = new JsonStreamParser(json);
		JsonArray arr = parser.next().getAsJsonArray();

		//create the map with GeoPoint as key and string as name
		Map<GeoPoint,String> map = new HashMap<GeoPoint,String>();
		
		for (int i = 0; i < arr.size(); i++)
		{
			//have JsonObjects in the JsonArray, so get it out to process
			JsonObject ob = arr.get(i).getAsJsonObject();
			
			//get the Geopoint and the name to put in map.
			GeoPoint point = new GeoPoint( ob.get(NAMES[0]).getAsInt(),ob.get(NAMES[1]).getAsInt());
			map.put(point,ob.get(NAMES[3]).getAsString());
		}
		
		return map;
	}	
}