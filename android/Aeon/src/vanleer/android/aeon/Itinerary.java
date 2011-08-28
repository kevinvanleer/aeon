package vanleer.android.aeon;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public final class Itinerary extends ListView{

	int id = R.id.listView_itinerary;
	ListView iteneraryListView;
	ArrayAdapter<String> iteneraryItems;
	
	public Itinerary(Context context) {
		super(context);
				
		iteneraryItems = new ArrayAdapter<String>(context, R.layout.itinerary);
		iteneraryItems.add("Add Destination");
		
		iteneraryListView = (ListView) findViewById(id);		
		iteneraryListView.setAdapter(iteneraryItems);
	}

}
