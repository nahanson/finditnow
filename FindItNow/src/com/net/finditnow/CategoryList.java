/*
 * This class provides the intermediate list screen
 * that is displayed for Buildings and Supplies.
 * Selection of any option will launch the map class.
 * 
 * Auto-completion suggestions are enabled.
 */

package com.net.finditnow;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class CategoryList extends FINBaseListActivity {
	
	/**
     * On launch, determines which category type was passed
     * and displays the appropriate list.
     * Defaults to supplies if category is unrecognized.
     */
	
	private ProgressDialog myDialog;
	
    @Override
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	Bundle extras = getIntent().getExtras(); 
    	final String category = extras.getString("category");
    	setTitle("FindItNow > " + FINUtil.capFirstChar(category));
    	
    	setListAdapter(new ArrayAdapter<String>(this, R.layout.category_list, getResources().getStringArray(R.array.specific_supplies)));
    	
    	ListView lv = getListView();
    	lv.setTextFilterEnabled(true);
    	
    	// Every item will launch the map
    	lv.setOnItemClickListener(new OnItemClickListener() {
    		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				myDialog = ProgressDialog.show(CategoryList.this, "Items Loading" , "Loading " + FINUtil.capFirstChar(((TextView) v).getText().toString()) + "...", true);
    			Intent myIntent = new Intent(v.getContext(), FINMap.class);
    			myIntent.putExtra("category", category);
                myIntent.putExtra("building", "");
    			myIntent.putExtra("itemName", ((TextView) v).getText().toString());
    			startActivity(myIntent);
    		}
    	});
    }
    
	@Override
	public void onResume() {
		super.onResume();
		if (myDialog != null) {
			myDialog.dismiss();
		}
	}
}
