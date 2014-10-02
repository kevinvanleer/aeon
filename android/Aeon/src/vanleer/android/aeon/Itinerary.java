package vanleer.android.aeon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;

// Something for sending destinations to Navigator
// I/ActivityManager( 118): Starting activity: Intent { act=android.intent.action.VIEW dat=google.navigation:///?q=Some%20place cmp=brut.googlemaps/com.google.android.maps.driveabout.app.NavigationActivity }

public final class Itinerary extends Activity implements OnClickListener {

	private static final int GPS_UPDATE_DISTANCE_M = 20;
	private static final int GPS_UPDATE_INTERVAL_MS = 2000;
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
	private int selectedItemPosition = -1;
	private Geocoder theGeocoder = null;
	private boolean traveling = false;
	private int currentDestinationIndex = 0;
	private PendingIntent pendingReminder;
	private AlarmManager alarmManager;
	private PendingIntent pendingAlarm;
	private ArrayList<Location> locations = new ArrayList<Location>();
	private LocationListener locationListener = null;
	private LocationListener appendMyLocationListener = null;
	private Handler eventHandler;
	private LocationManagerUpdater locationUpdater;
	private ScheduleUpdater scheduleUpdater;

	private void rebuildFromBundle(Bundle savedInstanceState) {

		ArrayList<ItineraryItem> savedItinerary = savedInstanceState.getParcelableArrayList("itineraryItems");
		origin = savedItinerary.get(0);
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
	public void onNewIntent(Intent theIntent) {
		if (theIntent.getAction().equals("vanleer.android.aeon.delay_departure")) {
			Object theExtra = theIntent.getExtras().get("destination");
			if (theExtra != null) {
				ItineraryItem update = (ItineraryItem) theExtra;

				if (update.getSchedule().getArrivalTime().equals(currentDestination().getSchedule().getArrivalTime())) {
					currentDestination().getSchedule().updateDepartureTime(update.getSchedule().getDepartureTime());
					setAlerts(currentDestination(), itineraryItems.getItem(currentDestinationIndex + 1));
					updateTimes();
				}
			}
		}
	}

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

		// THIS IS TEMPORARY -- HAR HAR HAR
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

		theGeocoder = new Geocoder(this);
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			buildAlertMessageNoGps();
		}

		locationUpdater = new LocationManagerUpdater();
		scheduleUpdater = new ScheduleUpdater();
		eventHandler = new Handler();

		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				onNewLocation(location);
				Itinerary.this.scheduleNextLocationUpdate();
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_UPDATE_INTERVAL_MS, GPS_UPDATE_DISTANCE_M, locationListener);

		configureItineraryListViewLongClickListener();

		if ((savedInstanceState == null) || savedInstanceState.isEmpty()) {
			initializeOrigin();
		} else {
			rebuildFromBundle(savedInstanceState);
			scheduleUpdater.run();
		}

		initializeAddNewItineraryItem();
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

	@Override
	public void onDestroy() {
		cancelAlerts();
		locationManager.removeUpdates(locationListener);
		super.onDestroy();
	}

	protected void scheduleNextLocationUpdate() {
		Log.d("Aeon", "Cancelling current location updates");
		locationManager.removeUpdates(locationListener);

		if ((getFinalDestinationIndex() == 0) || !finalDestination().atLocation()) {
			long msDelta = 0;
			if (currentDestination().enRoute()) {
				msDelta = currentDestination().getSchedule().getArrivalTime().getTime() - (new Date()).getTime();
			} else if (currentDestination().atLocation()) {
				msDelta = currentDestination().getSchedule().getDepartureTime().getTime() - (new Date()).getTime();
			}

			long msDelay = (msDelta - (1000 * 60 * 5)) / 2;
			if (msDelay < GPS_UPDATE_INTERVAL_MS) msDelay = GPS_UPDATE_INTERVAL_MS;

			Log.d("Aeon", "Scheduled GPS update for " + msDelay + "ms from now");
			eventHandler.postDelayed(locationUpdater, msDelay);
		} else {
			Log.d("Aeon", "No GPS update scheduled.  User at final destination.");
		}
	}

	private Location currentLocation() {
		Location currentLocation = null;
		if (!locations.isEmpty()) {
			currentLocation = locations.get(locations.size() - 1);
		}
		return currentLocation;
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
		Log.d("Aeon", "Setting departure reminder from " + origin.getName() + " at " + fiveMinutesBeforeDeparture.getTime().toString());
		alarmManager.set(AlarmManager.RTC_WAKEUP, fiveMinutesBeforeDeparture.getTimeInMillis(), pendingReminder);
	}

	private void setDepartureAlarm(ItineraryItem origin, ItineraryItem destination) {
		Intent alarm = new Intent(this, DepartureAlarm.class);
		alarm.putExtra("vanleer.android.aeon.departureAlarmOrigin", origin);
		alarm.putExtra("vanleer.android.aeon.departureAlarmDestination", destination);
		pendingAlarm = PendingIntent.getActivity(this, UPDATE_DESTINATION, alarm, PendingIntent.FLAG_CANCEL_CURRENT);
		Log.d("Aeon", "Setting departure alarm from " + origin.getName() + " at " + origin.getSchedule().getDepartureTime().toString());
		alarmManager.set(AlarmManager.RTC_WAKEUP, origin.getSchedule().getDepartureTime().getTime(), pendingAlarm);
	}

	public void setAlerts(ItineraryItem origin, ItineraryItem destination) {
		Log.d("Aeon", "Setting alerts for departure from " + origin.getName());
		setDepartureReminder(origin, destination);
		setDepartureAlarm(origin, destination);
	}

	public void cancelAlerts() {
		Log.d("Aeon", "Cancelling current departure alerts");
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
				Log.v("Aeon", "User arrived at " + currentDestination().getName());
				currentDestination().setAtLocation();
				itineraryItems.notifyDataSetChanged();
				updateArrivalTimeAndSchedules(currentDestination());
				if (currentDestinationIndex < (itineraryItems.getCount() - 2)) {
					setAlerts(currentDestination(), itineraryItems.getItem(currentDestinationIndex + 1));
				}
			}
		} else {
			if (haveDeparted()) { // departing TODO: unreadable -> refactor
				traveling = true;
				cancelAlerts();
				currentDestination().setLocationExpired();
				updateDepartureTimeAndSchedules(currentDestination());
				if (currentDestinationIndex < (itineraryItems.getCount() - 2)) {
					getDirections();
					++currentDestinationIndex;
					sendExternalNavigationNotification();
				}
				Log.v("Aeon", "User has departed for " + currentDestination().getName());
				currentDestination().setEnRoute();
				itineraryItems.notifyDataSetChanged();
			}
		}
	}

	private void sendExternalNavigationNotification() {
		NotificationCompat.Builder navNotiBuilder = new NotificationCompat.Builder(this);
		navNotiBuilder.setContentTitle("Navigation");
		String message = "Select for directions to " + currentDestination().getName();
		navNotiBuilder.setContentText(message);
		navNotiBuilder.setWhen(new Date().getTime());
		navNotiBuilder.setContentInfo(currentDestination().getFormattedDistance());
		navNotiBuilder.setSmallIcon(R.drawable.arrive_notification);
		navNotiBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
		navNotiBuilder.setAutoCancel(true);

		PendingIntent pendingResult = PendingIntent.getActivity(getBaseContext(), 0, getExternalNavigationIntent(), 0);

		navNotiBuilder.setContentIntent(pendingResult);
		NotificationManager notiMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notiMgr.notify(R.id.departure_reminder_notification, navNotiBuilder.build());
	}

	private void startExternalNavigation() {
		startActivity(getExternalNavigationIntent());
	}

	private Intent getExternalNavigationIntent() {
		try {
			Log.v("Aeon", "Starting external navigation.");
			String navUri = "google.navigation:ll=";
			navUri += currentDestination().getLocation().getLatitude() + ",";
			navUri += currentDestination().getLocation().getLongitude();
			Intent navIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(navUri));
			return navIntent;
		} catch (ActivityNotFoundException e) {
			try {
				Log.v("Aeon", "Navigation intent failed starting Google Maps.");
				String navUri = "http://maps.google.com/maps?&daddr=";
				navUri += currentDestination().getLocation().getLatitude() + ",";
				navUri += currentDestination().getLocation().getLongitude();
				// TODO: add the following to give a custom name to the location
				// navUri += "(Custom name here)";
				Intent navIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(navUri));
				return navIntent;
			} catch (ActivityNotFoundException er) {
				Log.d("Aeon", "No external navigation apps found.");
			}
		}
		return null;
	}

	void updateTimes() {
		Log.v("Aeon", "Updating times.");
		if (currentDestination().atLocation()) {
			if (currentDestination().getSchedule().getDepartureTime().before(new Date())) {
				Log.v("Aeon", "Updating current destination departure time while at location.");
				updateDepartureTime(currentDestination());
			}
		} else if (currentDestination().enRoute()) {
			if (currentDestination().getSchedule().getArrivalTime().before(new Date())) {
				Log.v("Aeon", "Updating current destination departure time while en route.");
				updateArrivalTime(currentDestination());
			}
		}

		updateSchedules();
	}

	private Date nearestMinute() {
		return nearestMinute(new Date());
	}

	private Date nearestMinute(Date date) {
		Calendar nearestMinute = Calendar.getInstance();
		nearestMinute.setTime(date);

		// int delta = 0;
		// if (nearestMinute.get(Calendar.SECOND) < 30) {
		// delta = 0;
		// } else {
		// delta = 1;
		// }
		// nearestMinute.add(Calendar.MINUTE, delta);
		nearestMinute.set(Calendar.SECOND, 0);

		return nearestMinute.getTime();
	}

	private void updateDepartureTimeAndSchedules(ItineraryItem currentDestination) {
		updateDepartureTime(currentDestination);
		updateSchedules();
	}

	private void updateDepartureTime(ItineraryItem currentDestination) {
		Schedule thisSchedule = currentDestination.getSchedule();
		thisSchedule.updateDepartureTime(nearestMinute());

		if (thisSchedule.getArrivalTime() != null) {
			thisSchedule.updateStayDuration((thisSchedule.getDepartureTime().getTime() - thisSchedule.getArrivalTime().getTime()) / 1000);
		}
	}

	private void updateArrivalTimeAndSchedules(ItineraryItem currentDestination) {
		updateArrivalTime(currentDestination);
		updateTravelDuration(currentDestination);
		updateSchedules();
	}

	private void updateTravelDuration(ItineraryItem currentDestination) {
		if (currentDestinationIndex == 0) {
			throw new IllegalStateException();
		}
		ItineraryItem previousDestination = itineraryItems.getItem(currentDestinationIndex - 1);
		currentDestination.setTravelDuration((currentDestination.getSchedule().getArrivalTime().getTime() - previousDestination.getSchedule().getDepartureTime().getTime()) / 1000);

	}

	private void updateArrivalTime(ItineraryItem currentDestination) {
		Schedule thisSchedule = currentDestination.getSchedule();
		thisSchedule.updateArrivalTime(nearestMinute());

		if (thisSchedule.isDepartureTimeFlexible()) {
			if (thisSchedule.getStayDuration() != null) {
				Calendar newDeparture = Calendar.getInstance();
				newDeparture.setTime(thisSchedule.getArrivalTime());
				newDeparture.add(Calendar.SECOND, thisSchedule.getStayDuration().intValue());
				thisSchedule.updateDepartureTime(newDeparture.getTime());
			}
		} else {
			if (thisSchedule.getDepartureTime() != null) {
				thisSchedule.updateStayDuration((thisSchedule.getDepartureTime().getTime() - thisSchedule.getArrivalTime().getTime()) / 1000);
			}
		}
	}

	void updateSchedules() {
		for (int i = (currentDestinationIndex + 1); i < (itineraryItems.getCount() - 1); ++i) {
			itineraryItems.getItem(i).updateSchedule(itineraryItems.getItem(i - 1).getSchedule().getDepartureTime());
		}

		itineraryItems.notifyDataSetChanged();
		Log.v("Aeon", "Updating itinerary list view.");
	}

	private void getDirections() {
		new GoogleDirectionsGiver(currentDestination().getLocation(), itineraryItems.getItem(currentDestinationIndex + 1).getLocation()) {
			@Override
			protected void onPostExecute(DirectionsResult result) {
				Address destinationAddress = result.getDestination();
				Location destinationLocation = currentDestination().getLocation();
				if (destinationAddress != null && destinationLocation != null) {
					float[] distance = new float[1];
					Location.distanceBetween(destinationAddress.getLatitude(), destinationAddress.getLongitude(), destinationLocation.getLatitude(), destinationLocation.getLongitude(), distance);
					Bundle locationExtras = new Bundle();
					// TODO: Exception thrown when trying to unmarshal this bundle because of DirectionsResult
					// locationExtras.putParcelable("address", result);
					locationExtras.putFloat("distance", distance[0]);
					destinationLocation.setExtras(locationExtras);
					String logMsg = "Destination is " + distance[0] + " from the nearest road";
					Log.v("Aeon", logMsg);
				} else {
					// TODO: Probably something to do here
				}
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
		// Log.v("Aeon", "Vicinity threshold:" + threshold);
		// Log.v("Aeon", "Distance to destination:" + distance);
		return (distance < threshold);
	}

	private boolean isMoving() {
		if (!currentLocation().hasSpeed()) {
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
		double totalLat = 0;
		double totalLng = 0;

		ListIterator<Location> iterator = locations.listIterator(locations.size());
		while (iterator.hasPrevious()) {
			item = iterator.previous();
			if (iterator.hasPrevious()) {
				previousItem = locations.get(iterator.previousIndex());

				float d2p_m = item.distanceTo(previousItem);
				// TODO: Use getElapsedRealtimeNanos -- but requires API 17
				float timeDelta_s = ((item.getTime() - previousItem.getTime()) / 1000.f);
				float speed_m_s = (d2p_m / timeDelta_s);
				Log.d("Aeon", "Performing loiter calculations; d2p_m=" + d2p_m + ", times_s=" + timeDelta_s + ", speed_m_s=" + speed_m_s + ", item::time=" + item.getTime() + ", previousItem::time=" + previousItem.getTime());
				if (speed_m_s > 5) {
					Log.v("Aeon", "Found " + locationCount + " fixes with speeds less than 5 m/s.");
					break;
				}

				++locationCount;
				totalLat += item.getLatitude();
				totalLng += item.getLongitude();
				totalTime_s += timeDelta_s;

				// TODO: incorporate these stats???
				// float d2c_m = item.distanceTo(currentLocation());
				// float d2d_m = currentDestination().getLocation().distanceTo(item);
			}
		}

		if (totalTime_s > 60.) {
			Log.v("Aeon", "Speed less than 5 m/s for more than 1 minute.");
			double averageLat = totalLat / locationCount;
			double averageLng = totalLng / locationCount;
			float[] distance = new float[1];
			Log.v("Aeon", "averageLat=" + averageLat + "; averageLng=" + averageLng);

			double distanceThreshold = totalTime_s;
			Location.distanceBetween(currentLocation().getLatitude(), currentLocation().getLongitude(), averageLat, averageLng, distance);
			Log.v("Aeon", "Distance threshold is " + distanceThreshold);
			Log.v("Aeon", "User is " + distance[0] + " m from average loiter location.");
			if (distance[0] < distanceThreshold) {
				Log.d("Aeon", "User is loitering.");
				loitering = true;
			}

			Location.distanceBetween(currentDestination().getLocation().getLatitude(), currentDestination().getLocation().getLongitude(), averageLat, averageLng, distance);
			if (distance[0] < distanceThreshold) {
				// assume user is loitering at intended destination
			}
		} else {
			Log.v("Aeon", "User has only loitered for " + totalTime_s + " seconds.");
		}

		return loitering;
	}

	private boolean haveDeparted() {
		// boolean departed = currentDestination().atLocation();
		boolean departed = !traveling;
		departed &= !isInVicinity();
		if (currentLocation().hasSpeed()) {
			// Log.v("Aeon", "Location has speed parameter. <" + currentLocation().getSpeed() + ">");
			departed &= isMoving();
		} else {
			// Log.v("Aeon", "No speed parameter for this location.");
		}

		if (departed) {
			departed &= !isLoitering();
		}

		return departed;
	}

	private boolean haveArrived() {
		// boolean arrived = currentDestination().enRoute();
		boolean arrived = traveling;
		arrived &= isInVicinity();
		if (currentLocation().hasSpeed()) {
			Log.v("Aeon", "Location has speed parameter. <" + currentLocation().getSpeed() + ">");
			arrived &= !isMoving();
		} else {
			Log.v("Aeon", "No speed parameter for this location.");
		}

		if (!arrived && traveling) {
			Log.v("Aeon", "Initial arrival criteria failed.  Attempting to detect loiter.");
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
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Toast.makeText(getApplicationContext(), "You must enable GPS to use Arrive", Toast.LENGTH_LONG).show();
		}
		menu.findItem(R.id.menu_item_add_destination).setEnabled(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_add_destination:

			break;
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
			currentDestinationIndex = 0;
			traveling = false;
			eventHandler.removeCallbacks(scheduleUpdater);
			eventHandler.removeCallbacks(locationUpdater);
			initializeOrigin();
			initializeAddNewItineraryItem();
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_UPDATE_INTERVAL_MS, GPS_UPDATE_DISTANCE_M, locationListener);
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

		startActivityForResult(startItineraryOpen, GET_NEW_DESTINATION);
	}

	private void updateOrigin() {
		// Log.v("Aeon", "Updating origin.");
		if (origin.getSchedule().getDepartureTime().before(new Date())) {
			// Log.v("Aeon", "Updating origin departure time.");
			origin.getSchedule().updateDepartureTime(nearestMinute());
		}
		if (currentLocation() != null) {
			try {
				// Log.v("Aeon", "Updating origin location.");
				origin.updateLocation(currentLocation(), getLocationAddress(currentLocation()));
			} catch (NullPointerException e) {
				// TODO Location was null
			}
		}
		itineraryItems.notifyDataSetChanged();
	}

	private void initializeOrigin() {
		origin = new ItineraryItem("My location (locating...)");
		origin.setAtLocation();
		Schedule departNow = new Schedule();
		departNow.initializeFlexibleDepartureTime(nearestMinute());
		origin.setSchedule(departNow);
		if (currentLocation() != null) {
			try {
				origin.updateLocation(currentLocation(), getLocationAddress(currentLocation()));
			} catch (NullPointerException e) {
				// TODO Location was null
			}
		}

		insertListItem(origin, 0);
		scheduleUpdater.run();
	}

	class ScheduleUpdater implements Runnable {
		public void run() {
			Log.v("Aeon", "Schedule updater is running.");
			Calendar now = Calendar.getInstance();
			now.setTime(new Date());

			Calendar nextMinute = Calendar.getInstance();
			nextMinute.setTime(now.getTime());
			nextMinute.set(Calendar.MILLISECOND, 0);
			nextMinute.set(Calendar.SECOND, 0);
			nextMinute.add(Calendar.MINUTE, 1);

			long delayMs = nextMinute.getTimeInMillis() - now.getTimeInMillis();

			Itinerary.this.eventHandler.postDelayed(Itinerary.this.scheduleUpdater, delayMs);

			doStuff();
		}
	}

	private void doStuff() {
		if (currentDestinationIndex >= 0) {
			if (currentDestination().getSchedule().isArrivalTime(1)) {
				updateTimes();
				Log.v("Aeon", "Updating itinerary to highlight stay duration.");
			} else if (currentDestination().getSchedule().isDepartureTime()) {
				updateTimes();
				Log.v("Aeon", "Updating itinerary prior to departure.");
			} else if (currentDestination().getSchedule().getDepartureTime().before(new Date())) {
				updateTimes();
				Log.v("Aeon", "Updating itinerary after departure time expiration.");
			}
		}
	}

	class LocationManagerUpdater implements Runnable {
		public void run() {
			Log.d("Aeon", "LocationManagerUpdater requesting single update from GPS provider");
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_UPDATE_INTERVAL_MS, GPS_UPDATE_DISTANCE_M, locationListener);
		}
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

	private void GetMyLocationInfo() {
		if (appendMyLocationListener == null) {
			appendMyLocationListener = new LocationListener() {

				public void onLocationChanged(Location arg0) {
					onNewLocation(arg0);
					appendMyLocationToItinerary();
					locationManager.removeUpdates(this);
				}

				public void onProviderDisabled(String arg0) {
					// TODO Auto-generated method stub

				}

				public void onProviderEnabled(String arg0) {
					// TODO Auto-generated method stub

				}

				public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
					// TODO Auto-generated method stub

				}

			};
		}

		if (currentLocation() == null) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_UPDATE_INTERVAL_MS, GPS_UPDATE_DISTANCE_M, appendMyLocationListener);
			waitForGps();
		} else {
			long threshold = new Date().getTime() - 1000 * 60 * 5;
			if (currentLocation().getTime() < threshold) {
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_UPDATE_INTERVAL_MS, GPS_UPDATE_DISTANCE_M, appendMyLocationListener);
				waitForGps();
			} else {
				appendMyLocationToItinerary();
			}
		}
	}

	private void appendMyLocationToItinerary() {
		ItineraryItem myLocation = null;

		// TODO: Wait for location service if current location is null

		try {
			ItineraryItem lastDestination = finalDestination();
			myLocation = new ItineraryItem(currentLocation(), lastDestination.getLocation(), getLocationAddress(currentLocation()));
		} catch (IllegalStateException e) {
			myLocation = new ItineraryItem(currentLocation(), getLocationAddress(currentLocation()));
		}

		// TODO: Refactor after testing behavior of search in this situation (following clause)
		if (currentDestination().equals(finalDestination())) {
			currentDestination().setLocationExpired();
			myLocation.setAtLocation();
		}
		initializeSchedule(myLocation);
		if (currentDestination().equals(finalDestination())) {
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

				if (!((currentDestinationIndex == 0) && (selectedItemPosition != 0))) {
					if (currentDestinationIndex < (itineraryItems.getCount() - 2)) {
						if (selectedItemPosition == currentDestinationIndex || selectedItemPosition == (currentDestinationIndex + 1)) {
							cancelAlerts();
							setAlerts(currentDestination(), itineraryItems.getItem(currentDestinationIndex + 1));
						}
					}
				}

				selectedItemPosition = -1;
				updateTimes();
			}
			break;
		default:
		}
		eventHandler.removeCallbacks(locationUpdater);
		scheduleNextLocationUpdate();
	}

	private void initializeSchedule(ItineraryItem newDestination) {
		Intent startDestinationSchedule = new Intent(Itinerary.this, DestinationScheduleActivity.class);
		Calendar arrivalTimeCalculator = Calendar.getInstance();

		if (!itineraryItems.isEmpty()) {
			ItineraryItem lastDestination = finalDestination();
			arrivalTimeCalculator.setTime(lastDestination.getSchedule().getDepartureTime());
			arrivalTimeCalculator.add(Calendar.SECOND, newDestination.getTravelDuration().intValue());
		} else {
			arrivalTimeCalculator.add(Calendar.SECOND, newDestination.getTravelDuration().intValue());
		}
		newDestination.getSchedule().initializeFlexibleArrivalTime(arrivalTimeCalculator.getTime());
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
