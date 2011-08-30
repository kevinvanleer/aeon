
package vanleer.android.aeon;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public final class Itinerary extends Activity implements  OnItemLongClickListener{

	private int listViewId = R.id.listView_itinerary;
	private ListView iteneraryListView;
	private ArrayAdapter<String> itineraryItems;
	private static String ADD_DESTINATION = "Add Destination";
	
	public void onCreate(Bundle savedInstanceState){
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.itinerary);
		
		itineraryItems = new ArrayAdapter<String>(this, R.layout.itinerary_item);
        itineraryItems.add(ADD_DESTINATION);
		
    	iteneraryListView = (ListView) findViewById(listViewId);
    	iteneraryListView.setAdapter(itineraryItems);
        
    	iteneraryListView.setOnItemLongClickListener(this);
	}

	public boolean onItemLongClick(AdapterView<?> parentView, View textView, int position, long id) {
		
		if(itineraryItems.getItem(position) == ADD_DESTINATION){
			
			setContentView(R.layout.add_destination);
			//TODO: create an intent and start a new activity for adding a destination
			/*
			 * consider consolidating menus to avoid creating so many activities
			 * explore the possibility of pop-up dialogs or something similar 
			 */
		}
		else{
			
			//TODO: edit the selected destination
		}
		
		return true;
	}
}
