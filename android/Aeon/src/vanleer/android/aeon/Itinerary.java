
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
	
	public void onCreate(Bundle savedInstanceState){
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.itinerary);
		
		itineraryItems = new ArrayAdapter<String>(this, R.layout.itinerary_item);
        itineraryItems.add("Add Destination");
		
		//iteneraryListView = new ListView(this);		
    	iteneraryListView = (ListView) findViewById(listViewId);
    	iteneraryListView.setAdapter(itineraryItems);
        
    	iteneraryListView.setOnItemLongClickListener(this);
	}

	/*public void onClick(View v) {
		
		
	}*/

	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		setContentView(R.layout.add_destination);
		return true;
	}
}
