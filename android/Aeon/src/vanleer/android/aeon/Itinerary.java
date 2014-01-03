package vanleer.android.aeon;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemLongClickListener;

//Something for sending destinations to Navigator
//I/ActivityManager(  118): Starting activity: Intent { act=android.intent.action.VIEW dat=google.navigation:///?q=Some%20place cmp=brut.googlemaps/com.google.android.maps.driveabout.app.NavigationActivity }

public final class Itinerary extends Activity implements OnClickListener {

	private int listViewId = R.id.listView_itinerary;
	private ListView itineraryListView;
	private ArrayList<ItineraryItem> itineraryItemList;
	private ItineraryItemAdapter itineraryItems;
	private boolean loggedIntoGoogle = /* false */true; // for debugging
	private LocationManager locationManager;
	private Location currentLocation = null;
	private static final int GET_NEW_DESTINATION = 0;
	private static final int UPDATE_DESTINATION_SCHEDULE = 1;
	private ProgressDialog waitSpinner;
	private boolean waitingForGps = false;
	private ItineraryItem origin = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.itinerary);

		// FOR TESTING
		// currentLocation = new Location("test");
		// currentLocation.setLatitude(38.477548);
		// currentLocation.setLongitude(-91.051562);
		// FOR TESTING

		itineraryItemList = new ArrayList<ItineraryItem>();
		itineraryItems = new ItineraryItemAdapter(this,
				R.layout.itinerary_item, itineraryItemList);
		itineraryListView = (ListView) findViewById(listViewId);
		itineraryListView.setAdapter(itineraryItems);

		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location
				// provider.
				onNewLocation(location);
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		// Register the listener with the Location Manager to receive location
		// updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, locationListener);

		configureItineraryListViewLongClickListener();
		initializeOrigin();
	}
	
	private void configureItineraryListViewLongClickListener() {
		itineraryListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				
				return true;
			}
		});
	}

	private void waitForGps() {
		waitSpinner = ProgressDialog.show(Itinerary.this, "",
				"waiting for location...", true);
		waitingForGps = true;
		new Thread() {
			@Override
			public void run() {
				while (currentLocation == null) {
					try {
						sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				waitSpinner.dismiss();
			}
		}.start();
	}

	protected void onNewLocation(Location location) {
		currentLocation = location;
		updateOrigin();
		if (waitingForGps) {
			waitingForGps = false;
		}
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
			if (loggedIntoGoogle) {
				// TODO: Something
			} else {
				// TODO: Something else
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
		Intent startItineraryOpen = new Intent(Itinerary.this,
				PlacesSearchActivity.class);

		if (itineraryItemList.isEmpty()) {
			startItineraryOpen.putExtra("location", currentLocation);
		} else {
			startItineraryOpen.putExtra("location", GetLastDestination()
					.getLocation());
		}

		startActivityForResult(startItineraryOpen, GET_NEW_DESTINATION);
	}

	private void updateOrigin() {
		origin.getSchedule().setDepartureTime(new Date());
		if(currentLocation != null) {
			origin.updateLocation(currentLocation);
		}
		itineraryItems.notifyDataSetChanged();
	}

	private void initializeOrigin() {
		origin = new ItineraryItem("My location (locating...)");
		Schedule departNow = new Schedule();
		departNow.setDepartureTime(new Date());
		origin.setSchedule(departNow);
		itineraryItemList.add(origin);
		itineraryItems.add(itineraryItemList.get(itineraryItemList.size() - 1));

		new Thread() {
			@Override
			public void run() {
				Calendar nextMinute = Calendar.getInstance();
				nextMinute.setTime(new Date());				
				nextMinute.set(Calendar.MINUTE, nextMinute.get(Calendar.MINUTE) + 1);
				nextMinute.set(Calendar.SECOND, 0);
							
				while (true) {
					Calendar now = Calendar.getInstance();
					now.setTime(new Date());
					while (now.getTimeInMillis() < nextMinute.getTimeInMillis()) {						
						try {
							sleep(nextMinute.getTimeInMillis() - now.getTimeInMillis());
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}					
						now.setTime(new Date());
					}
					
					class OriginUpdater implements Runnable {
						public void run() {
							origin.getSchedule().setDepartureTime(new Date());
							itineraryItems.notifyDataSetChanged();							
						}
					}
					
					Itinerary.this.runOnUiThread(new OriginUpdater());
										
					nextMinute.setTime(new Date());
					nextMinute.set(Calendar.MINUTE, nextMinute.get(Calendar.MINUTE) + 1);
				}
			}
		}.start();
	}

	private void GetMyLocationInfo() {
		if (currentLocation == null) {
			waitForGps();
		} else {
			ItineraryItem myLocation = null;

			// TODO: Wait for location service if current location is null

			try {
				ItineraryItem lastDestination = GetLastDestination();
				myLocation = new ItineraryItem(currentLocation,
						lastDestination.getLocation());
			} catch (IllegalStateException e) {
				myLocation = new ItineraryItem(currentLocation);
			}

			UpdateArrivalDepartureTimes(myLocation);
		}
	}

	private ItineraryItem GetLastDestination() {
		if (itineraryItemList.size() == 0) {
			throw new IllegalStateException(
					"The destination list is empty.  There is no previous destination");
		}

		return itineraryItemList.get(itineraryItemList.size() - 1);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case GET_NEW_DESTINATION:
			if (resultCode == Activity.RESULT_OK) {
				ItineraryItem newDestination = (ItineraryItem) data
						.getParcelableExtra("itineraryItem");
				UpdateArrivalDepartureTimes(newDestination);
			}
			break;
		case UPDATE_DESTINATION_SCHEDULE:
			if (resultCode == Activity.RESULT_OK) {
				ItineraryItem scheduledDestination = (ItineraryItem) data
						.getParcelableExtra("destination");
				itineraryItemList.add(scheduledDestination);
				itineraryItems.add(itineraryItemList.get(itineraryItemList
						.size() - 1));
			}
			break;
		default:
		}
	}

	private void UpdateArrivalDepartureTimes(ItineraryItem newDestination) {
		Intent startDestinationSchedule = new Intent(Itinerary.this,
				DestinationScheduleActivity.class);
		Calendar arrivalTimeCalculator = Calendar.getInstance();
		if (!itineraryItemList.isEmpty()) {
			ItineraryItem lastDestination = GetLastDestination();
			arrivalTimeCalculator.setTime(lastDestination.getSchedule()
					.getDepartureTime());
			arrivalTimeCalculator.setTimeInMillis(arrivalTimeCalculator
					.getTimeInMillis()
					+ (newDestination.getTravelDuration() * 1000));
		} else {
			arrivalTimeCalculator.setTimeInMillis(arrivalTimeCalculator
					.getTimeInMillis()
					+ (newDestination.getTravelDuration() * 1000));
		}
		newDestination.getSchedule().setArrivalTime(
				arrivalTimeCalculator.getTime());

		startDestinationSchedule.putExtra("vanleer.android.aeon.destination",
				newDestination);
		startActivityForResult(startDestinationSchedule,
				UPDATE_DESTINATION_SCHEDULE);
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub

	}
}
