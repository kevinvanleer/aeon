package vanleer.android.aeon;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import vanleer.android.aeon.ItineraryManager.ItineraryManagerBinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;

// Something for sending destinations to Navigator
// I/ActivityManager( 118): Starting activity: Intent { act=android.intent.action.VIEW dat=google.navigation:///?q=Some%20place cmp=brut.googlemaps/com.google.android.maps.driveabout.app.NavigationActivity }

public final class Itinerary extends Activity implements OnClickListener {
	public static final int GET_NEW_DESTINATION = 0;
	public static final int ADD_DESTINATION = 1;
	public static final int UPDATE_DESTINATION = 2;

	private final int listViewId = R.id.listView_itinerary;
	private ListView itineraryListView;
	private ItineraryItemAdapter itineraryItems;
	private final boolean loggedIntoGoogle = /* false */true; // for debugging
	private LocationManager locationManager;
	private ProgressDialog waitSpinner;
	private boolean waitingForGps = false;
	private int selectedItemPosition = -1;
	private Geocoder theGeocoder = null;
	private boolean traveling = false;
	private int currentDestinationIndex = 0;
	private ArrayList<Location> locations = new ArrayList<Location>();
	private ItineraryManagerBinder itineraryManagerBinder;
	private boolean boundToItineraryManager;
	private boolean callAppendMyLocationToItinerary;

	private static Handler eventHandler;

	private final BroadcastReceiver itineraryManagerReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("Aeon", "Itinerary received a broadcast");
			if (intent.getAction().equals(ItineraryManager.NEW_LOCATION)) {
				Log.d("Aeon", "Itinerary got location update");
				onNewLocation((Location) intent.getExtras().getParcelable("location"));
				if (callAppendMyLocationToItinerary) {
					appendMyLocationToItinerary();
				}
			} else if (intent.getAction().equals(ItineraryManager.DATA_SET_CHANGED)) {
				Log.d("Aeon", "Itinerary notified that itinerary data changed");
				updateListView();
			}
		}
	};

	private final ServiceConnection itineraryManagerConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.d("Aeon", "Itinerary has been connected to itinerary manager");
			itineraryManagerBinder = (ItineraryManagerBinder) service;
			boundToItineraryManager = true;

			updateListView();
		}

		public void onServiceDisconnected(ComponentName name) {
			Log.d("Aeon", "Itinerary has been disconnected from itinerary manager");
			boundToItineraryManager = false;
		}
	};

	private void rebuildFromBundle(Bundle savedInstanceState) {

		ArrayList<ItineraryItem> savedItinerary = savedInstanceState.getParcelableArrayList("itineraryItems");

		Log.v("Aeon", "Restoring " + savedItinerary.size() + " itinerary items.");
		for (ItineraryItem item : savedItinerary) {
			itineraryItems.add(item);
		}
		traveling = savedInstanceState.getBoolean("traveling");
		currentDestinationIndex = savedInstanceState.getInt("currentDestinationIndex");
		selectedItemPosition = savedInstanceState.getInt("selectedItemPosition");
		locations = savedInstanceState.getParcelableArrayList("locations");
		itineraryItems.notifyDataSetChanged();
	}

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		ArrayList<ItineraryItem> savedItinerary = new ArrayList<ItineraryItem>();
		for (int i = 0; i < itineraryItems.getCount() - 1; ++i) {
			savedItinerary.add(itineraryItems.getItem(i));
		}
		savedInstanceState.putParcelableArrayList("itineraryItems", savedItinerary);
		savedInstanceState.putParcelableArrayList("locations", locations);
		savedInstanceState.putInt("currentDestinationIndex", currentDestinationIndex);
		savedInstanceState.putInt("selectedItemPosition", selectedItemPosition);
		savedInstanceState.putBoolean("traveling", traveling);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.itinerary);

		if (eventHandler == null) {
			eventHandler = new Handler();
		}

		itineraryItems = new ItineraryItemAdapter(this, R.layout.itinerary_item);
		itineraryListView = (ListView) findViewById(listViewId);
		itineraryListView.setAdapter(itineraryItems);

		// TODO: Use this to verify location service is available
		// GooglePlayServicesUtil.isGooglePlayServicesAvailable();

		// getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

		theGeocoder = new Geocoder(this);

		startService(new Intent(this, ItineraryManager.class));

		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			buildAlertMessageNoGps();
		}

		configureItineraryListViewLongClickListener();

		if ((savedInstanceState == null) || savedInstanceState.isEmpty()) {
			// initializeOrigin();
		} else {
			rebuildFromBundle(savedInstanceState);
		}

		initializeAddNewItineraryItem();
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d("Aeon", "Starting itinerary activity");
		Intent bindIntent = new Intent(this, ItineraryManager.class);
		bindService(bindIntent, itineraryManagerConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d("Aeon", "Resuming itinerary activity");
		IntentFilter myFilter = new IntentFilter();
		myFilter.addAction(ItineraryManager.NEW_LOCATION);
		myFilter.addAction(ItineraryManager.DATA_SET_CHANGED);
		LocalBroadcastManager.getInstance(this).registerReceiver(itineraryManagerReceiver, myFilter);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d("Aeon", "Pausing itinerary activity");
		LocalBroadcastManager.getInstance(this).unregisterReceiver(itineraryManagerReceiver);
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.d("Aeon", "Stopping itinerary activity");
		unbindService(itineraryManagerConnection);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("Aeon", "Destroying itinerary activity");
	}

	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Arrive requires precise location tracking, would you like to enable GPS?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
				startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			}
		}).setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
				dialog.cancel();
				Toast.makeText(getApplicationContext(), "You must enable GPS to use Arrive", Toast.LENGTH_LONG).show();
				// TODO: Disable add destination/next destination
			}
		});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	private Location currentLocation() {
		return itineraryManagerBinder.currentLocation();
	}

	private void initializeAddNewItineraryItem() {
		ItineraryItem addNewItemItem = new ItineraryItem(getString(R.string.add_destination_itinerary_item));
		LayoutInflater vi = (LayoutInflater) this.getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = vi.inflate(R.layout.itinerary_item, null);

		// TextView destinationName = (TextView) v.findViewById(R.id.textView_destinationName);
		TextView arrivalVicinity = (TextView) v.findViewById(R.id.textView_arrivalLocation);
		TextView arrivalTime = (TextView) v.findViewById(R.id.textView_arrivalTime);
		TextView travelDistance = (TextView) v.findViewById(R.id.textView_travelDistance);
		TextView travelTime = (TextView) v.findViewById(R.id.textView_travelTime);
		TextView stayDuration = (TextView) v.findViewById(R.id.textView_stayDuration);
		TextView departureVicinity = (TextView) v.findViewById(R.id.textView_departureLocation);
		TextView departureTime = (TextView) v.findViewById(R.id.textView_departureTime);

		arrivalVicinity.setVisibility(View.GONE);
		arrivalTime.setVisibility(View.GONE);
		travelDistance.setVisibility(View.GONE);
		travelTime.setVisibility(View.GONE);
		stayDuration.setVisibility(View.GONE);
		departureVicinity.setVisibility(View.GONE);
		departureTime.setVisibility(View.GONE);

		// TODO: define custom color scheme for this item
		// destinationName.setTextColor(Color.BLACK);
		// destinationName.setBackgroundColor(Color.WHITE);

		appendListItem(addNewItemItem);
	}

	private void configureItineraryListViewLongClickListener() {
		itineraryListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (position == (itineraryItems.getCount() - 1)) {
					if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
						startSearchActivity();
					} else {
						Toast.makeText(getApplicationContext(), "You must enable GPS to use Arrive", Toast.LENGTH_LONG).show();
					}
				} else if (position >= currentDestinationIndex) {
					if (locations.isEmpty()) {
						Toast.makeText(getApplicationContext(), "You must enable GPS to use Arrive", Toast.LENGTH_LONG).show();
					} else {
						selectedItemPosition = position;
						updateSchedule(itineraryItems.getItem(position));
					}
				}
				return true;
			}
		});
	}

	private void waitForGps() {
		waitSpinner = ProgressDialog.show(Itinerary.this, "", "waiting for location...", true);
		waitingForGps = true;
		new Thread() {
			@Override
			public void run() {
				while (currentLocation() == null) {
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
		Log.v("Aeon", "New location received.");
		if (locations.size() > 1000) locations.remove(0);
		locations.add(location);

		if (waitingForGps) {
			waitingForGps = false;
		}
	}

	private ItineraryItem currentDestination() {
		return itineraryManagerBinder.currentDestination();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.itinerary_options, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Toast.makeText(getApplicationContext(), "You must enable GPS to use Arrive", Toast.LENGTH_LONG).show();
		}
		menu.findItem(R.id.menu_item_add_destination).setEnabled(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
		return true;
	}

	private void quit() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Your itinerary will be cleared. Would you still like to quit?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				stopService(new Intent(Itinerary.this, ItineraryManager.class));
				finish();
			}
		}).setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

			}
		});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_exit:
			quit();
			break;
		case R.id.menu_item_add_destination:
			break;
		case R.id.submenu_item_add_destination_google_search:
			startSearchActivity();
			break;
		case R.id.submenu_item_add_destination_my_location:
			getMyLocationInfo();
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
			itineraryManagerBinder.clearList();
			itineraryItems.clear();
			currentDestinationIndex = 0;
			traveling = false;
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

	private void startSearchActivity() {
		Intent startItineraryOpen = new Intent(Itinerary.this, PlacesSearchActivity.class);

		if (itineraryItems.isEmpty()) {
			startItineraryOpen.putExtra("location", currentLocation());
		} else {
			ItineraryItem lastDestination = finalDestination();
			startItineraryOpen.putExtra("location", lastDestination.getLocation());
		}

		startItineraryOpen.putExtra("requestCode", GET_NEW_DESTINATION);
		startActivityForResult(startItineraryOpen, GET_NEW_DESTINATION);

	}

	private Address getLocationAddress(Location location) {
		Address theAddress = null;
		try {
			List<Address> addresses = theGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
			if (!addresses.isEmpty()) theAddress = addresses.get(0);
		} catch (IOException e) {
			Log.e("Aeon", e.getMessage(), e);
		}
		return theAddress;
	}

	private void getMyLocationInfo() {
		if (currentLocation() == null) {
			itineraryManagerBinder.requestLocationUpdate();
			waitForGps();
			callAppendMyLocationToItinerary = true;
		} else {
			long threshold = new Date().getTime() - 1000 * 60 * 5;
			if (currentLocation().getTime() < threshold) {
				itineraryManagerBinder.requestLocationUpdate();
				waitForGps();
				callAppendMyLocationToItinerary = true;
			} else {
				appendMyLocationToItinerary();
			}
		}
	}

	private void appendMyLocationToItinerary() {
		callAppendMyLocationToItinerary = false;
		ItineraryItem myLocation = null;

		// TODO: Wait for location service if current location is null

		try {
			ItineraryItem lastDestination = finalDestination();
			myLocation = new ItineraryItem(currentLocation(), lastDestination.getLocation(), getLocationAddress(currentLocation()));
		} catch (IllegalStateException e) {
			myLocation = new ItineraryItem(currentLocation(), getLocationAddress(currentLocation()));
		}

		// TODO: Refactor after testing behavior of search in this situation (following clause)
		if ((getFinalDestinationIndex() != 0) && finalDestination().atLocation()) {
			currentDestination().setLocationExpired();
			myLocation.setAtLocation();
		}

		itineraryManagerBinder.initializeSchedule(myLocation);

		Intent startDestinationSchedule = new Intent(Itinerary.this, DestinationScheduleActivity.class);
		startDestinationSchedule.putExtra("vanleer.android.aeon.destination", myLocation);
		startDestinationSchedule.putExtra("requestCode", ADD_DESTINATION);
		startActivityForResult(startDestinationSchedule, ADD_DESTINATION);

		if ((getFinalDestinationIndex() != 0) && finalDestination().atLocation()) {
			++currentDestinationIndex;
		}

	}

	private int getFinalDestinationIndex() {
		if (itineraryItems.getCount() < 2) {
			throw new IllegalStateException("The destination list has not been initialize correctly.");
		}

		return (itineraryItems.getCount() - 2);
	}

	private ItineraryItem finalDestination() {
		return itineraryItems.getItem(getFinalDestinationIndex());
	}

	private int getAppendDestinationIndex() {
		if (itineraryItems.getCount() < 2) {
			throw new IllegalStateException("The destination list has not been initialize correctly.");
		}

		return (itineraryItems.getCount() - 1);
	}

	private void appendDestination(ItineraryItem newItem) {
		insertListItem(newItem, getAppendDestinationIndex());
	}

	private void appendListItem(ItineraryItem newItem) {
		insertListItem(newItem, itineraryItems.getCount());
	}

	private void removeListItem(int index) {
		itineraryItems.remove(itineraryItems.getItem(index));
	}

	private void insertListItem(ItineraryItem destination, int index) {
		itineraryItems.insert(destination, index);
	}

	private void replaceListItem(ItineraryItem destination, int index) {
		removeListItem(index);
		insertListItem(destination, index);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case GET_NEW_DESTINATION:
			if (resultCode == Activity.RESULT_OK) {
				// ItineraryItem newDestination = (ItineraryItem) data.getParcelableExtra("itineraryItem");
				// initializeSchedule(newDestination);
			}
			break;
		case ADD_DESTINATION:
			if (resultCode == Activity.RESULT_OK) {
				// ItineraryItem scheduledDestination = (ItineraryItem) data.getParcelableExtra("destination");
				// appendDestination(scheduledDestination);
			}
			break;
		case UPDATE_DESTINATION:
			if (resultCode == Activity.RESULT_OK) {
				/*-ItineraryItem updatedDestination = (ItineraryItem) data.getParcelableExtra("destination");

				if (selectedItemPosition != -1) {
					replaceListItem(updatedDestination, selectedItemPosition);
				}

				if (selectedItemPosition == 0) {
					origin = updatedDestination;
				}

				if (!((currentDestinationIndex == 0) && (selectedItemPosition != 0))) {
					if (currentDestinationIndex < (itineraryItems.getCount() - 2)) {
						if (selectedItemPosition == currentDestinationIndex || selectedItemPosition == (currentDestinationIndex + 1)) {
							cancelAlerts();
							setAlerts(currentDestination(), itineraryItems.getItem(currentDestinationIndex + 1));
						}
					}
				}

				selectedItemPosition = -1;
				updateTimes();*/
			}
			break;
		default:
		}
		updateListView();
	}

	private void updateListView() {
		if (boundToItineraryManager) {
			itineraryItems.clear();
			itineraryItems.addAll(itineraryManagerBinder.getDestinations());
			initializeAddNewItineraryItem();
		}
	}

	private void updateSchedule(ItineraryItem newDestination) {
		Intent startDestinationSchedule = new Intent(Itinerary.this, DestinationScheduleActivity.class);

		startDestinationSchedule.putExtra("vanleer.android.aeon.destination", newDestination);
		startDestinationSchedule.putExtra("requestCode", UPDATE_DESTINATION);
		startActivityForResult(startDestinationSchedule, UPDATE_DESTINATION);
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub

	}
}
