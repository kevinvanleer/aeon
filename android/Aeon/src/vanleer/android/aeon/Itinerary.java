package vanleer.android.aeon;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
//import android.view.ContextMenu;
//import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

//Something for sending destinations to Navigator
//I/ActivityManager(  118): Starting activity: Intent { act=android.intent.action.VIEW dat=google.navigation:///?q=Some%20place cmp=brut.googlemaps/com.google.android.maps.driveabout.app.NavigationActivity }

public final class Itinerary extends Activity implements OnClickListener{

	private int listViewId = R.id.listView_itinerary;
	private ListView itineraryListView;
	private ArrayList<ItineraryItem> itineraryItemList;
	private ItineraryItemAdapter itineraryItems;
	private boolean loggedIntoGoogle = /*false*/true; // for debugging	
	/*private static final String ADD_DESTINATION = "Add Destination";
	private static final String STARRED_LOCATIONS = "Starred Locations";
	private static final String GOOGLE_SEARCH = "Google Search";
	private static final String MY_LOCATION = "My Location";
	private static final String MOVE = "Move";
	private static final String EDIT = "Edit";
	private static final String DELETE = "Delete";
	private static final String CALL = "Call";*/
	private static final int GET_NEW_DESTINATION = 0;

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.itinerary);

		itineraryItemList = new ArrayList<ItineraryItem>();
		itineraryItems = new ItineraryItemAdapter(this, R.layout.itinerary_item, itineraryItemList);
		itineraryListView = (ListView) findViewById(listViewId);
		itineraryListView.setAdapter(itineraryItems);

		//registerForContextMenu(itineraryListView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.itinerary_options, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_add_destination_google_search:
			Intent startItineraryOpen = new Intent(Itinerary.this, PlacesSearchActivity.class);
			startActivityForResult(startItineraryOpen, GET_NEW_DESTINATION);
			break;
		case R.id.menu_item_add_destination_my_location:
			break;
		case R.id.menu_item_add_destination_starred_locations:
			if(loggedIntoGoogle) {
				//TODO: Something
			}
			else {
				//TODO: Something else
			}
			break;
		case R.id.menu_item_call_destination:
			break;
		case R.id.menu_item_delete_destination:
			break;
		case R.id.menu_item_edit_destination:
			break;
		case R.id.menu_item_move_destination:
			break;
		case R.id.menu_item_save_itinerary:
			break;
		default:
			break;
		}
		return true;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode)
		{
		case GET_NEW_DESTINATION:
			if(resultCode == Activity.RESULT_OK) {
				ItineraryItem newDestination = (ItineraryItem) data.getParcelableExtra("itineraryItem");
				itineraryItemList.add(newDestination);
				itineraryItems.add(itineraryItemList.get(itineraryItemList.size() - 1)); 
			}
			break;
		default:
		}
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
