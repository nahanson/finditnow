package com.net.finditnow;


import android.widget.TextView;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.ListView;
import java.lang.StringBuffer;
import android.widget.SimpleAdapter;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.HashMap;

public class PopUpDialogVer2 extends Dialog{

	//Local variable for displaying
	private String[] floor;
	private String buildName;
	private String name;	
	
	public PopUpDialogVer2(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	//creates a PopUpDialog with the given fields, should use this one
	public PopUpDialogVer2(Context context,String[] floor, String building, String name)
	{
		super(context);
		this.floor = floor;
		this.buildName = building;
		this.name = name;
	}
	
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.popupdialog);
    	setTitle(buildName);
    	
    	TextView cate = (TextView) findViewById(R.id.categoryName);
 
    	//Converts the first letter of category to upper case and
    	//adds the name of the service provided if it exist
    	StringBuffer buffer = new StringBuffer();
    	buffer.append(FINMap.getCategory());    	
    	//char cateName = Character.toUpperCase(buffer.charAt(0));
    	buffer.setCharAt(0, Character.toUpperCase(buffer.charAt(0)));
    	if (!(name == null) && !name.equals(""))
    		buffer.append(" : "+ name);
    	
    	//sets the text into the textView
    	cate.setText(buffer.toString());

    	/*TextView seeflr = (TextView) findViewById(R.id.seefloor);
    	seeflr.on*/
    	
    	//there is a button on this dialog, we need it to be clickable
    	Button butt = (Button) findViewById(R.id.showFlrButt);
    	//so when the user press it, it'll show the detail display
    	butt.setOnClickListener( new View.OnClickListener()
    	{
    		public void onClick(View v)
    		{
    		/*
    		 * 			//sets the different property of the text display
			TextView text2 = new TextView(this.getContext());
			text2.setText(floor[i]);
			text2.setPadding(TEXT_LEFT, TEXT_TOP + TEXT_DIFF*i, 0, 0);
			text2.setTextColor(smptext.getTextColors());
			layout.addView(text2);
			
			//sets the different property of the icon display
			ImageView img2 = new ImageView(this.getContext());
			img2.setImageResource(FINMenu.getIcon(FINMap.getCategory()));
			img2.setAdjustViewBounds(true);
			img2.setMaxHeight(IMG_TOP+IMG_DIFF*(i+1));
			img2.setPadding(IMG_LEFT,IMG_TOP+IMG_DIFF*i , IMG_RIGHT,0);
			layout.addView(img2);
    		 */
    			ArrayList<HashMap<String,Object>> hashMapListForListView = new ArrayList<HashMap<String,Object>>();
    			HashMap<String,Object> map = new HashMap<String,Object>();
    			for (String s: floor)
    			{
    				map.put("name",s);
    				map.put("icon",FINMenu.getIcon(FINMap.getCategory()));
    				hashMapListForListView.add(map);
    				map = new HashMap<String,Object>();
    			}
    			
    			ListView lv = (ListView) findViewById(R.id.flrList);
    	    	lv.setAdapter(
    	    			new SimpleAdapter(lv.getContext(),
                                hashMapListForListView, 
                                R.layout.flrlist_item,
                                new String[] {"name", "icon"},
                                new int[] { R.id.flrName, R.id.flrIcon}) 
    	    	);
    		}
    	});
		//String[] w = {"abc","def","ghi","jkl","mno","pqr","stu","vwx","yz"};

    	//ListAdapter adapter = 
    	//ListView lv = (ListView) findViewById(R.id.listV);
    	//lv.setAdapter(new ArrayAdapter<String>(lv.getContext(), R.layout.list_item, w));
    	
		//layout.addView(cat.getListView());
    	
	}
	public boolean onTouchEvent(MotionEvent e)
	{
		dismiss();
		return true;
	}
}  