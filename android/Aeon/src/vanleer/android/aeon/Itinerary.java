package vanleer.android.aeon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
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

// Something for sending destinations to Navigator
// I/ActivityManager( 118): Starting activity: Intent { act=android.intent.action.VIEW dat=google.navigation:///?q=Some%20place cmp=brut.googlemaps/com.google.android.maps.driveabout.app.NavigationActivity }

public final class Itinerary extends Activity implements OnClickListener {

	private final int listViewId = R.id.listView_itinerary;
	private ListView itineraryListView;
	private ItineraryItemAdapter itineraryItems;
	private final boolean loggedIntoGoogle = /* false */true; // for debugging
	private LocationManager locationManager;
	private static final int GET_NEW_DESTINATION = 0;
	private static final int ADD_DESTINATION = 1;
	private static final int UPDATE_DESTINATION = 2;
	private ProgressDialog waitSpinner;
	private boolean waitingForGps = false;
	private ItineraryItem origin = null;
	private ItineraryItem addNewItemItem = null;
	private int selectedItemPosition = -1;
	private Geocoder theGeocoder = null;
	private boolean traveling = false;
	private int currentDestinationIndex = -1;
	private PendingIntent pendingReminder;
	private AlarmManager alarmManager;
	private PendingIntent pendingAlarm;
	private final ArrayList<Location> locations = new ArrayList<Location>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.itinerary);

		// FOR TESTING
		// currentLocation() = new Location("test");
		// currentLocation().setLatitude(38.477548);
		// currentLocation().setLongitude(-91.051562);
		// FOR TESTING

		itineraryItems = new ItineraryItemAdapter(this, R.layout.itinerary_item);
		itineraryListView = (ListView) findViewById(listViewId);
		itineraryListView.setAdapter(itineraryItems);

		// TODO: Use this to verify location service is available
		// GooglePlayServicesUtil.isGooglePlayServicesAvailable();

		theGeocoder = new Geocoder(this);
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location provider.
				onNewLocation(location);
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

		configureItineraryListViewLongClickListener();
		initializeOrigin();
		initializeAddNewItineraryItem();
	}

	private Location currentLocation() {
		Location currentLocation = null;
		if (!locations.isEmpty()) {
			currentLocation = locations.get(locations.size() - 1);
		}
		return currentLocation;
	}

	private void initializeAddNewItineraryItem() {
		addNewItemItem = new ItineraryItem(getString(R.string.add_destination_itinerary_item));
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

	class ItineraryUpdater implements Runnable {
		public void run() {
			if (origin.atLocation()) {
				updateTimes();
			}
		}
	}

	private void setDepartureReminder(ItineraryItem origin, ItineraryItem destination) {
		int reminderAdvance = 5;

		Intent reminder = new Intent(this, DepartureReminder.class);
		reminder.putExtra("vanleer.android.aeon.departureReminderOrigin", origin);
		reminder.putExtra("vanleer.android.aeon.departureReminderDestination", destination);
		reminder.putExtra("vanleer.android.aeon.departureReminderAdvance", reminderAdvance);

		pendingReminder = PendingIntent.getService(this, R.id.departure_reminder_intent, reminder, PendingIntent.FLAG_CANCEL_CURRENT);

		Calendar fiveMinutesBeforeDeparture = Calendar.getInstance();
		fiveMinutesBeforeDeparture.setTime(origin.getSchedule().getDepartureTime());
		fiveMinutesBeforeDeparture.add(Calendar.MINUTE, -reminderAdvance);
		alarmManager.set(AlarmManager.RTC_WAKEUP, fiveMinutesBeforeDeparture.getTimeInMillis(), pendingReminder);
	}

	private void setDepartureAlarm(ItineraryItem origin, ItineraryItem destination) {
		// TODO Auto-generated method stub
		Intent alarm = new Intent(this, DepartureAlarm.class);
		alarm.putExtra("vanleer.android.aeon.departureAlarmOrigin", origin);
		alarm.putExtra("vanleer.android.aeon.departureAlarmDestination", destination);
		pendingAlarm = PendingIntent.getActivity(this, UPDATE_DESTINATION, alarm, PendingIntent.FLAG_CANCEL_CURRENT);

		alarmManager.set(AlarmManager.RTC_WAKEUP, origin.getSchedule().getDepartureTime().getTime(), pendingAlarm);
	}

	public void setAlerts(ItineraryItem origin, ItineraryItem destination) {
		Log.d("Departure Alerts", "Setting alerts for departure from " + origin.getName());
		setDepartureReminder(origin, destination);
		setDepartureAlarm(origin, destination);
	}

	public void cancelAlerts() {
		Log.d("Departure Alerts", "Cancelling current departure alerts");
		cancelReminder();
		cancelAlarm();
	}

	private void cancelReminder() {
		NotificationManager notiMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notiMgr.cancel(R.id.departure_reminder_notification);
		alarmManager.cancel(pendingReminder);
	}

	private void cancelAlarm() {
		alarmManager.cancel(pendingAlarm);
	}

	private void configureItineraryListViewLongClickListener() {
		itineraryListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (position == (itineraryItems.getCount() - 1)) {
					startSearchActivity();
				} else {
					selectedItemPosition = position;
					updateSchedule(itineraryItems.getItem(position));
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
		// Log.v("Itinerary", "New location received.");
		if (locations.size() > 1000) locations.remove(0);
		locations.add(location);
		if (origin.getLocation() == null || itineraryItems.getCount() <= 2) {
			currentDestinationIndex = 0;
			updateOrigin();
		}

		updateTravelStatus();

		if (waitingForGps) {
			waitingForGps = false;
		}
	}

	private void updateTravelStatus() {
		if (traveling) { // arriving TODO: unreadable -> refactor
			if (haveArrived()) {
				traveling = false;
				Log.v("Travel Status", "User arrived at " + currentDestination().getName());
				currentDestination().setAtLocation();
				itineraryItems.notifyDataSetChanged();
				setAlerts(currentDestination(), itineraryItems.getItem(currentDestinationIndex + 1));
				updateArrivalTime(currentDestination());
			}
		} else {
			if (haveDeparted()) { // departing TODO: unreadable -> refactor
				traveling = true;
				cancelAlerts();
				currentDestination().setLocationExpired();
				updateDepartureTime(currentDestination());
				if (currentDestinationIndex < (itineraryItems.getCount() - 2)) {
					getDirections();
					++currentDestinationIndex;
				}
				Log.v("Travel Status", "User has departed for " + currentDestination().getName());
				currentDestination().setEnRoute();
				itineraryItems.notifyDataSetChanged();
				// TODO: Display map
			}
		}
	}

	private void updateDepartureTime(ItineraryItem currentDestination) {
		Schedule thisSchedule = currentDestination.getSchedule();
		thisSchedule.setDepartureTime(new Date());
		thisSchedule.setStayDuration((thisSchedule.getDepartureTime().getTime() - thisSchedule.getArrivalTime().getTime()) / 1000);
		updateTimes();
	}

	private void updateArrivalTime(ItineraryItem currentDestination) {
		Schedule thisSchedule = currentDestination.getSchedule();
		thisSchedule.setArrivalTime(new Date());
		if (thisSchedule.isDepartureTimeFlexible()) {
			thisSchedule.setDepartureTime(new Date(thisSchedule.getArrivalTime().getTime() + (thisSchedule.getStayDuration() * 1000)));
		} else {
			thisSchedule.setStayDuration((thisSchedule.getDepartureTime().getTime() - thisSchedule.getArrivalTime().getTime()) / 1000);
		}
		updateTimes();
	}

	private void getDirections() {
		new GoogleDirectionsGiver(currentDestination().getLocation(), itineraryItems.getItem(currentDestinationIndex + 1).getLocation()) {
			@Override
			protected void onPostExecute(DirectionsResult result) {
				Address destinationAddress = result.getDestination();
				Location destinationLocation = currentDestination().getLocation();
				float[] distance = new float[1];
				Location.distanceBetween(destinationAddress.getLatitude(), destinationAddress.getLongitude(), destinationLocation.getLatitude(), destinationLocation.getLongitude(), distance);
				Bundle locationExtras = new Bundle();
				locationExtras.putParcelable("address", result);
				locationExtras.putFloat("distance", distance[0]);
				destinationLocation.setExtras(locationExtras);
				String logMsg = "Destination is " + distance[0] + " from the nearest road";
				Log.i("Destination", logMsg);
			}
		};
	}

	private boolean isInVicinity() {
		Bundle extras = currentDestination().getLocation().getExtras();
		float threshold = 0.f;
		if (extras != null) {
			threshold = extras.getFloat("distance");
		}
		if (threshold < 100.f) threshold = 100.f;
		float distance = currentLocation().distanceTo(currentDestination().getLocation());
		Log.v("Vicinity Detection", "Vicinity threshold:" + threshold);
		Log.v("Vicinity Detection", "Distance to destination:" + distance);
		return (distance < threshold);
	}

	private boolean isMoving() {
		if (!currentDestination().getLocation().hasSpeed()) {
			throw new IllegalStateException("No speed set for this location");
		}
		return currentLocation().getSpeed() > 5.f;
	}

	private ItineraryItem currentDestination() {
		return itineraryItems.getItem(currentDestinationIndex);
	}

	private boolean isLoitering() {
		if (locations.isEmpty()) {
			throw new IllegalStateException("No locations have been received.");
		}
		boolean loitering = false;

		Location item = null;
		Location previousItem = null;
		int locationCount = 0;
		float totalTime_s = 0;
		float totalLat = 0;
		float totalLng = 0;
		for (ListIterator<Location> iterator = locations.listIterator(locations.size()); iterator.hasPrevious(); item = iterator.previous()) {
			if (previousItem != null) {
				float d2p_m = item.distanceTo(previousItem);
				float time_s = ((item.getTime() - previousItem.getTime()) / 1000.f);
				float speed_m_s = (d2p_m / time_s);

				if (speed_m_s > 5) {
					Log.v("Loiter Detection", "Found " + locationCount + "fixes with speeds less than 5 m/s.");
					break;
				}

				++locationCount;
				totalLat += item.getLatitude();
				totalLng += item.getLongitude();
				totalTime_s += time_s;

				// float d2c_m = item.distanceTo(currentLocation());
				// float d2d_m = currentDestination().getLocation().distanceTo(item);

			}
			previousItem = item;

			if (totalTime_s > 60.) {
				Log.v("Loiter Detection", "Speed less than 5 m/s for more than 1 minute.");
				float averageLat = totalLat / locationCount;
				float averageLng = totalLng / locationCount;
				float[] distance = new float[1];

				double distanceThreshold = totalTime_s;
				Location.distanceBetween(currentLocation().getLatitude(), currentLocation().getLongitude(), averageLat, averageLng, distance);
				Log.v("Loiter Detection", "Distance threshold is " + distanceThreshold);
				Log.v("Loiter Detection", "User is " + distance[0] + " m from average loiter location.");
				if (distance[0] < distanceThreshold) {
					Log.d("Loiter Detection", "User is loitering.");
					loitering = true;
				}

				Location.distanceBetween(currentDestination().getLocation().getLatitude(), currentDestination().getLocation().getLongitude(), averageLat, averageLng, distance);
				if (distance[0] < distanceThreshold) {
					// assume user is loitering at intended destination
				}
			}

		}

		return loitering;
	}

	private boolean isLoitering_old() {
		if (locations.size() == 0) {
			throw new IllegalStateException("No locations have been received.");
		}

		boolean loitering = false;

		final int SIZE = 5;
		int sampleSize = locations.size() < SIZE ? locations.size() : SIZE;
		int locationsIndex = locations.size() - sampleSize;
		locationsIndex = (locationsIndex < 0) ? 0 : locationsIndex;

		float distance = 0;
		for (int i = 0; i < (sampleSize - 1); ++i) {
			distance += locations.get(locationsIndex + i).distanceTo(locations.get(locationsIndex + i + 1));
		}

		if (distance < 100) {
			loitering = true;
		} else {
			float elapsedTime = ((locations.get(locations.size() - 1).getTime() - locations.get(locationsIndex).getTime()) / 1000.f);
			if ((distance / elapsedTime) < 5) {
				loitering = true;
			}
		}

		return loitering;
	}

	private boolean haveDeparted() {
		// boolean departed = currentDestination().atLocation();
		boolean departed = !traveling;
		departed &= !isInVicinity();
		if (currentLocation().hasSpeed()) {
			Log.v("Departure Detection", "Location has speed parameter. <" + currentLocation().getSpeed() + ">");
			departed &= isMoving();
		}
		return departed;
	}

	private boolean haveArrived() {
		// boolean arrived = currentDestination().enRoute();
		boolean arrived = traveling;
		arrived &= isInVicinity();
		if (currentLocation().hasSpeed()) {
			Log.v("Arrival Detection", "Location has speed parameter. <" + currentLocation().getSpeed() + ">");
			arrived &= !isMoving();
		}

		if (!arrived && traveling) {
			Log.v("Arrival Detection", "Initial arrival criteria failed.  Attempting to detect loiter.");
			if (isLoitering()) {
				// add loiter location as unplanned stop or intended destination
				// TODO: prompt use to inform if arrived at new location or intended destination or still traveling
				if (currentLocation().distanceTo(currentDestination().getLocation()) < 1000) {
					// assume intended destination with 1 km
					// adjust destination
					currentDestination().setLocation(currentLocation());
					arrived = true;
				} else {
					// add destination
				}
			}
		}
		return arrived;
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
			startSearchActivity();
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
			initializeOrigin();
			initializeAddNewItineraryItem();
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
			ItineraryItem lastDestination = getFinalDestination();
			startItineraryOpen.putExtra("location", lastDestination.getLocation());
		}

		startActivityForResult(startItineraryOpen, GET_NEW_DESTINATION);
	}

	private void updateOrigin() {
		Log.v("Itinerary", "Updating origin.");
		if (origin.getSchedule().getDepartureTime().before(new Date())) {
			Log.v("updateOrigin", "Updating origin departure time.");
			origin.getSchedule().setDepartureTime(new Date());
		}
		if (currentLocation() != null) {
			try {
				Log.v("updateOrigin", "Updating origin location.");
				origin.updateLocation(currentLocation(), getLocationAddress(currentLocation()));
			} catch (NullPointerException e) {
				// TODO Location was null
			}
		}
		itineraryItems.notifyDataSetChanged();
	}

	void updateTimes() {
		Log.v("Itinerary", "Updating times.");
		if (currentDestination().getSchedule().getDepartureTime().before(new Date())) {
			Log.v("updateTimes", "Updating current destination departure time.");
			currentDestination().getSchedule().setDepartureTime(new Date());
		}

		for (int i = (currentDestinationIndex + 1); i < (itineraryItems.getCount() - 1); ++i) {
			itineraryItems.getItem(i).updateSchedule(itineraryItems.getItem(i - 1).getSchedule().getDepartureTime());
		}

		itineraryItems.notifyDataSetChanged();
	}

	private void initializeOrigin() {
		origin = new ItineraryItem("My location (locating...)");
		origin.setAtLocation();
		Schedule departNow = new Schedule();
		departNow.setDepartureTime(new Date());
		origin.setSchedule(departNow);
		if (currentLocation() != null) {
			try {
				origin.updateLocation(currentLocation(), getLocationAddress(currentLocation()));
			} catch (NullPointerException e) {
				// TODO Location was null
			}
		}

		insertListItem(origin, 0);

		new Thread() {
			@Override
			public void run() {
				// TODO: Change nextMinute to current location departure time plus one minute

				while (true) {
					Calendar now = Calendar.getInstance();
					now.setTime(new Date());

					Calendar nextMinute = Calendar.getInstance();
					nextMinute.setTime(now.getTime());
					nextMinute.add(Calendar.MINUTE, 1);
					nextMinute.set(Calendar.SECOND, 0);

					if (now.getTime().before(nextMinute.getTime())) {
						try {
							sleep(nextMinute.getTimeInMillis() - now.getTimeInMillis());
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					doStuff();
				}
			}

			private void doStuff() {
				if (currentDestinationIndex >= 0) {
					ItineraryItem currentlyAt = currentDestination();

					if (currentlyAt.getSchedule().getDepartureTime().before(new Date())) {
						Itinerary.this.runOnUiThread(new ItineraryUpdater());
					}
				}
			}
		}.start();
	}

	private Address getLocationAddress(Location location) {
		Address theAddress = null;
		try {
			List<Address> addresses = theGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
			if (!addresses.isEmpty()) theAddress = addresses.get(0);
		} catch (IOException e) {
			Log.e("Itinerary", e.getMessage(), e);
		}
		return theAddress;
	}

	private void GetMyLocationInfo() {
		if (currentLocation() == null) {
			waitForGps();
		} else {
			ItineraryItem myLocation = null;

			// TODO: Wait for location service if current location is null

			try {
				ItineraryItem lastDestination = getFinalDestination();
				myLocation = new ItineraryItem(currentLocation(), lastDestination.getLocation(), getLocationAddress(currentLocation()));
			} catch (IllegalStateException e) {
				myLocation = new ItineraryItem(currentLocation(), getLocationAddress(currentLocation()));
			}

			initializeSchedule(myLocation);
		}
	}

	private int getFinalDestinationIndex() {
		if (itineraryItems.getCount() < 2) {
			throw new IllegalStateException("The destination list has not been initialize correctly.");
		}

		return (itineraryItems.getCount() - 2); // was -1
	}

	private ItineraryItem getFinalDestination() {
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
				ItineraryItem newDestination = (ItineraryItem) data.getParcelableExtra("itineraryItem");
				initializeSchedule(newDestination);
			}
			break;
		case ADD_DESTINATION:
			if (resultCode == Activity.RESULT_OK) {
				ItineraryItem scheduledDestination = (ItineraryItem) data.getParcelableExtra("destination");
				appendDestination(scheduledDestination);
			}
			break;
		case UPDATE_DESTINATION:
			if (resultCode == Activity.RESULT_OK) {
				ItineraryItem updatedDestination = (ItineraryItem) data.getParcelableExtra("destination");

				if (selectedItemPosition != -1) {
					replaceListItem(updatedDestination, selectedItemPosition);
				}

				if (selectedItemPosition == 0) {
					origin = updatedDestination;
				}

				if (currentDestinationIndex < (itineraryItems.getCount() - 1)) {
					if (selectedItemPosition == currentDestinationIndex || selectedItemPosition == (currentDestinationIndex + 1)) {
						cancelAlerts();
						setAlerts(currentDestination(), itineraryItems.getItem(currentDestinationIndex + 1));
					}
				}

				selectedItemPosition = -1;

				Itinerary.this.runOnUiThread(new ItineraryUpdater());
			}
			break;
		default:
		}
	}

	private void initializeSchedule(ItineraryItem newDestination) {
		Intent startDestinationSchedule = new Intent(Itinerary.this, DestinationScheduleActivity.class);
		Calendar arrivalTimeCalculator = Calendar.getInstance();

		if (!itineraryItems.isEmpty()) {
			ItineraryItem lastDestination = getFinalDestination();
			arrivalTimeCalculator.setTime(lastDestination.getSchedule().getDepartureTime());
			arrivalTimeCalculator.add(Calendar.SECOND, newDestination.getTravelDuration().intValue());
		} else {
			arrivalTimeCalculator.add(Calendar.SECOND, newDestination.getTravelDuration().intValue());
		}
		newDestination.getSchedule().setArrivalTime(arrivalTimeCalculator.getTime());
		startDestinationSchedule.putExtra("vanleer.android.aeon.destination", newDestination);
		startActivityForResult(startDestinationSchedule, ADD_DESTINATION);
	}

	private void updateSchedule(ItineraryItem newDestination) {
		Intent startDestinationSchedule = new Intent(Itinerary.this, DestinationScheduleActivity.class);

		startDestinationSchedule.putExtra("vanleer.android.aeon.destination", newDestination);
		startActivityForResult(startDestinationSchedule, UPDATE_DESTINATION);
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub

	}
}
