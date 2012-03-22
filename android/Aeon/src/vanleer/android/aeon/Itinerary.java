package vanleer.android.aeon;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
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
	private LocationManager locationManager;
	private Location currentLocation = null;
	
	private static final int GET_NEW_DESTINATION = 0;
	private static final int UPDATE_DESTINATION_SCHEDULE = 1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.itinerary);

		itineraryItemList = new ArrayList<ItineraryItem>();
		itineraryItems = new ItineraryItemAdapter(this, R.layout.itinerary_item, itineraryItemList);
		itineraryListView = (ListView) findViewById(listViewId);
		itineraryListView.setAdapter(itineraryItems);

		// Acquire a reference to the system Location Manager	    
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location provider.
				OnNewLocation(location);
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {}

			public void onProviderEnabled(String provider) {}

			public void onProviderDisabled(String provider) {}
		};
		
		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		
		if(itineraryItemList.isEmpty()) {
			//openOptionsMenu();
		}
	}

	protected void OnNewLocation(Location location) {
		currentLocation = location;
		/*//TODO: fix bug preventing display of current location if discovered after query started
		locationSensorImage.setVisibility(View.VISIBLE);
		//make the image view square
		MakeImageViewSquare(locationSensorImage);
		locationText.setText(GooglePlacesSearch.GetGeodeticString(currentLocation));
		new Thread() {
			public void run() {
				Message msg = updateCurrentLocationTextHandler.obtainMessage();
				msg.obj = googleSearch.ReverseGeocode(currentLocation, true);
				updateCurrentLocationTextHandler.sendMessage(msg);
			}
		}.start();
		if(waitingForGps) {
			waitingForGps  = false;
			onClick(searchButton);
		}*/
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
		case R.id.submenu_item_add_destination_google_search:
			StartSearchActivity();
			break;
		case R.id.submenu_item_add_destination_my_location:
			GetMyLocationInfo();
			break;
		case R.id.submenu_item_add_destination_starred_locations:
			if(loggedIntoGoogle) {
				//TODO: Something
			}
			else {
				//TODO: Something else
			}
			break;
		case R.id.menu_item_call_destination:
			break;
		case R.id.submenu_item_clear_itinerary_yes:
			itineraryItems.clear();
			itineraryItemList.clear();
			break;
		case R.id.menu_item_clear_itinerary:
		case R.id.submenu_item_clear_itinerary_no:
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

	private void StartSearchActivity() {
		Intent startItineraryOpen = new Intent(Itinerary.this, PlacesSearchActivity.class);
		
		if(itineraryItemList.isEmpty()) {
			startItineraryOpen.putExtra("location", currentLocation);
		} else {
			startItineraryOpen.putExtra("location",
					GetLastDestination().GetLocation());
		}
			
		startActivityForResult(startItineraryOpen, GET_NEW_DESTINATION);
	}

	private void GetMyLocationInfo() {
		ItineraryItem myLocation = null;
		
		try{
			ItineraryItem lastDestination = GetLastDestination();
			myLocation = new ItineraryItem(currentLocation, lastDestination.GetLocation());
		}
		catch(IllegalStateException e){
			myLocation = new ItineraryItem(currentLocation);
		}
		
		UpdateArrivalDepartureTimes(myLocation);
	}

	private ItineraryItem GetLastDestination() {
		if(itineraryItemList.size() == 0) {
			throw new IllegalStateException("The destination list is empty.  There is no previous destination");
		}
		
		return itineraryItemList.get(itineraryItemList.size() - 1);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode)
		{
		case GET_NEW_DESTINATION:
			if(resultCode == Activity.RESULT_OK) {
				ItineraryItem newDestination = (ItineraryItem) data.getParcelableExtra("itineraryItem");
				UpdateArrivalDepartureTimes(newDestination);
			}
			break;
		case UPDATE_DESTINATION_SCHEDULE:
			if(resultCode == Activity.RESULT_OK) {
				ItineraryItem scheduledDestination = (ItineraryItem) data.getParcelableExtra("destination");
				itineraryItemList.add(scheduledDestination);
				itineraryItems.add(itineraryItemList.get(itineraryItemList.size() - 1)); 
			}
			break;
		default:
		}
	}

	private void UpdateArrivalDepartureTimes(ItineraryItem newDestination) {
		Intent startDestinationSchedule = new Intent(Itinerary.this, DestinationScheduleActivity.class);
		if(!itineraryItemList.isEmpty()) {
			Calendar arrivalTimeCalculator = Calendar.getInstance();
			ItineraryItem lastDestination = GetLastDestination();
			arrivalTimeCalculator.setTime(lastDestination.GetDepartureTime());
			arrivalTimeCalculator.setTimeInMillis(arrivalTimeCalculator.getTimeInMillis() + (newDestination.GetTravelDuration() * 1000));
			newDestination.SetArrivalTime(arrivalTimeCalculator.getTime());
		}
		startDestinationSchedule.putExtra("destination", newDestination);
		startActivityForResult(startDestinationSchedule, UPDATE_DESTINATION_SCHEDULE);
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
