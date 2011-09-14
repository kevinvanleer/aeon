package vanleer.android.aeon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public final class Itinerary extends Activity {

	private int listViewId = R.id.listView_itinerary;
	private ListView itineraryListView;
	private ArrayAdapter<String> itineraryItems;
	private boolean loggedIntoGoogle = /*false*/true; // for debugging	
	private static String ADD_DESTINATION = "Add Destination";
	private static String STARRED_LOCATIONS = "Starred Locations";
	private static String GOOGLE_SEARCH = "Google Search";
	private static String MY_LOCATION = "My Location";
	private static String MOVE = "Move";
	private static String EDIT = "Edit";
	private static String DELETE = "Delete";

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.itinerary);

		itineraryItems = new ArrayAdapter<String>(this, R.layout.itinerary_item);
		/*itineraryItems.add("Home");
		itineraryItems.add("Away");
		itineraryItems.add("Here");
		itineraryItems.add("There");
		itineraryItems.add("Everywhere");
		itineraryItems.add("My place");
		itineraryItems.add("Your place");
		itineraryItems.add("This place");
		itineraryItems.add("That place");
		itineraryItems.add("Another place");
		itineraryItems.add("Some place new");*/
		itineraryItems.add(ADD_DESTINATION);

		itineraryListView = (ListView) findViewById(listViewId);
		itineraryListView.setAdapter(itineraryItems);

		registerForContextMenu(itineraryListView);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == listViewId) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle(itineraryItems.getItem(info.position));
			if (itineraryItems.getItem(info.position) == ADD_DESTINATION) {
				menu.add(STARRED_LOCATIONS);
				menu.add(GOOGLE_SEARCH);
				menu.add(MY_LOCATION);
			} else {
				menu.add(MOVE);
				menu.add(EDIT);
				menu.add(DELETE);
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		String menuItemName = (String) item.getTitle();

		if (menuItemName == STARRED_LOCATIONS) {
			if (!loggedIntoGoogle) {
				// TODO: Login to Google account
			}
			setContentView(R.layout.select_favorite);
		} else if (menuItemName == GOOGLE_SEARCH) {
			Intent startItineraryOpen = new Intent(Itinerary.this, PlacesSearchActivity.class);			
			startActivity(startItineraryOpen);
		} else if (menuItemName == MY_LOCATION) {
			// TODO: get location from GPS if available
		} else if (menuItemName == MOVE) {

		} else if (menuItemName == EDIT) {

		} else if (menuItemName == DELETE) {

		} else {

		}

		return true;
	}

}
