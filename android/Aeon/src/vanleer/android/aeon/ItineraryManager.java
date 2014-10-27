package vanleer.android.aeon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class ItineraryManager extends Service {
	private static final int GPS_UPDATE_DISTANCE_M = 0;
	private static final int GPS_UPDATE_INTERVAL_MS = 10000;
	public static final String NEW_LOCATION = "itinerary-manager-new-location";
	public static final String DATA_SET_CHANGED = "itinerary-manager-data-set-changed";

	private LocationManager locationManager;
	private LocationListener locationListener;
	private Geocoder theGeocoder = null;
	private Handler eventHandler;
	private AlarmManager alarmManager;
	private PendingIntent pendingReminder;
	private PendingIntent pendingAlarm;

	private final ArrayList<ItineraryItem> itineraryItems = new ArrayList<ItineraryItem>();
	private final ArrayList<Location> locations = new ArrayList<Location>();

	private ScheduleUpdater scheduleUpdater;
	private LocationManagerUpdater locationUpdater;

	private ItineraryItem origin = null;
	private int currentDestinationIndex = 0;
	private boolean traveling = false;
	private boolean waitingForGps = false;
	private final IBinder binder = new ItineraryManagerBinder();

	class ItineraryManagerBinder extends Binder {
		ItineraryManager getService() {
			// HACK: ONLY TEMPORARY WHILE MIGRATING
			return ItineraryManager.this;
		}

		public Location currentLocation() {
			Location currentLocation = null;
			if (!locations.isEmpty()) {
				currentLocation = locations.get(locations.size() - 1);
			}
			return currentLocation;
		}

		public void requestLocationUpdate() {
			eventHandler.removeCallbacks(locationUpdater);
			locationUpdater.run();
		}

		public Collection<ItineraryItem> getDestinations() {
			return itineraryItems;
		}

		public void appendDestination(ItineraryItem destination) {
			if (currentDestination().matches(finalDestination())) {
				locationUpdater.run();
			}
			itineraryItems.add(destination);
		}

		public void initializeSchedule(ItineraryItem newDestination) {
			Calendar arrivalTimeCalculator = Calendar.getInstance();
			if (!itineraryItems.isEmpty()) {
				ItineraryItem lastDestination = finalDestination();
				arrivalTimeCalculator.setTime(lastDestination.getSchedule().getDepartureTime());
				arrivalTimeCalculator.add(Calendar.SECOND, newDestination.getTravelDuration().intValue());
			} else {
				arrivalTimeCalculator.add(Calendar.SECOND, newDestination.getTravelDuration().intValue());
			}
			newDestination.getSchedule().initializeFlexibleArrivalTime(arrivalTimeCalculator.getTime());
		}

		public void updateDestination(ItineraryItem destination) {
			for (ItineraryItem item : itineraryItems) {
				if (destination.matches(item)) {
					int itemIndex = itineraryItems.indexOf(item);
					if (item.atLocation()) {
						cancelAlerts();
						setAlerts(destination, itineraryItems.get(itemIndex + 1));
					}
					itineraryItems.set(itemIndex, destination);
					updateSchedules();
				}
			}
		}

		public void clearList() {
			itineraryItems.clear();
			currentDestinationIndex = 0;
			traveling = false;
			initializeOrigin();
			locationUpdater.run();
		}

		public ItineraryItem currentDestination() {
			return ItineraryManager.this.currentDestination();
		}
	}

	class LocationManagerUpdater implements Runnable {
		public void run() {
			Log.d("Aeon", "LocationManagerUpdater is requesting periodic updates from GPS provider");
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_UPDATE_INTERVAL_MS, GPS_UPDATE_DISTANCE_M, locationListener);
		}
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

			ItineraryManager.this.eventHandler.postDelayed(ItineraryManager.this.scheduleUpdater, delayMs);

			doStuff();
		}
	}

	private void doStuff() {
		if (currentDestinationIndex >= 0) {
			/*-if (currentDestination().getSchedule().isArrivalTime(1)) {
				updateTimes();
				Log.v("Aeon", "Updating itinerary to highlight stay duration.");
			} else if (currentDestination().getSchedule().isDepartureTime()) {
				updateTimes();
				Log.v("Aeon", "Updating itinerary prior to departure.");
			} else if (currentDestination().getSchedule().getDepartureTime().before(new Date())) {
				updateTimes();
				Log.v("Aeon", "Updating itinerary after departure time expiration.");
			}*/
			updateTimes();
		}
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
		nearestMinute.set(Calendar.SECOND, 0);
		nearestMinute.set(Calendar.MILLISECOND, 0);
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
		ItineraryItem previousDestination = itineraryItems.get(currentDestinationIndex - 1);
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
		for (int i = (currentDestinationIndex + 1); i < (itineraryItems.size() - 1); ++i) {
			itineraryItems.get(i).updateSchedule(itineraryItems.get(i - 1).getSchedule().getDepartureTime());
		}

		sendLocalBroadcast(new Intent(DATA_SET_CHANGED));
		Log.v("Aeon", "Updating itinerary list view.");
	}

	private ItineraryItem currentDestination() {
		return itineraryItems.get(currentDestinationIndex);
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

	public Location currentLocation() {
		Location currentLocation = null;
		if (!locations.isEmpty()) {
			currentLocation = locations.get(locations.size() - 1);
		}
		return currentLocation;
	}

	protected void onNewLocation(Location location) {
		Log.v("Aeon", "New location received.");

		if (locations.size() > 1000) locations.remove(0);
		locations.add(location);

		Intent newLocationMessage = new Intent(NEW_LOCATION);
		newLocationMessage.putExtra("location", location);
		sendLocalBroadcast(newLocationMessage);

		if (origin.getLocation() == null || itineraryItems.size() <= 1) {
			currentDestinationIndex = 0;
			updateOrigin();
		}

		updateTravelStatus();

		if (waitingForGps) {
			waitingForGps = false;
		}
	}

	private void initializeOrigin() {
		Log.v("Aeon", "Initializing itinerary manager origin.");
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

		itineraryItems.add(origin);
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

		sendLocalBroadcast(new Intent(DATA_SET_CHANGED));
	}

	private int getFinalDestinationIndex() {
		return itineraryItems.size() - 1;
	}

	private ItineraryItem finalDestination() {
		return itineraryItems.get(getFinalDestinationIndex());
	}

	protected void scheduleNextLocationUpdate() {
		Log.d("Aeon", "Cancelling current location updates");
		locationManager.removeUpdates(locationListener);

		if (isItineraryActive()) {
			long msDelta = 0;
			/*- TODO: DISABLED UNTIL ITINERARY IS TRACKED BAY MANAGER
			if (currentDestination().enRoute()) {
				msDelta = currentDestination().getSchedule().getArrivalTime().getTime() - (new Date()).getTime();
			} else if (currentDestination().atLocation()) {
				msDelta = currentDestination().getSchedule().getDepartureTime().getTime() - (new Date()).getTime();
			}
			 */
			long msDelay = (msDelta - (1000 * 60 * 5)) / 2;
			if (msDelay < GPS_UPDATE_INTERVAL_MS) msDelay = GPS_UPDATE_INTERVAL_MS;

			Log.d("Aeon", "Scheduled GPS update for " + msDelay + "ms from now");
			eventHandler.postDelayed(locationUpdater, msDelay);
		} else {
			Log.d("Aeon", "No GPS update scheduled.  User at final destination.");
		}
	}

	private boolean isItineraryActive() {
		return ((getFinalDestinationIndex() == 0) || !finalDestination().atLocation());
	}

	private void updateTravelStatus() {
		if (traveling) { // arriving TODO: unreadable -> refactor
			if (haveArrived()) {
				traveling = false;
				Log.v("Aeon", "User arrived at " + currentDestination().getName());
				currentDestination().setAtLocation();
				sendLocalBroadcast(new Intent(DATA_SET_CHANGED));
				updateArrivalTimeAndSchedules(currentDestination());
				if (currentDestinationIndex < getFinalDestinationIndex()) {
					setAlerts(currentDestination(), itineraryItems.get(currentDestinationIndex + 1));
				}
			}
		} else {
			if (haveDeparted()) { // departing TODO: unreadable -> refactor
				traveling = true;
				cancelAlerts();
				currentDestination().setLocationExpired();
				updateDepartureTimeAndSchedules(currentDestination());
				if (currentDestinationIndex < getFinalDestinationIndex()) {
					getDirections();
					++currentDestinationIndex;
				}
				Log.v("Aeon", "User has departed for " + currentDestination().getName());
				currentDestination().setEnRoute();
				sendLocalBroadcast(new Intent(DATA_SET_CHANGED));
				sendExternalNavigationNotification();
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

	private void getDirections() {
		new GoogleDirectionsGiver(currentDestination().getLocation(), itineraryItems.get(currentDestinationIndex + 1).getLocation()) {
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
		pendingAlarm = PendingIntent.getActivity(this, 0, alarm, PendingIntent.FLAG_CANCEL_CURRENT);
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

	@Override
	public void onCreate() {
		super.onCreate();
		Log.v("Aeon", "Itinerary manager started");
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		theGeocoder = new Geocoder(this);
		eventHandler = new Handler();

		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				onNewLocation(location);
				ItineraryManager.this.scheduleNextLocationUpdate();
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, (10 * 1000), 10, locationListener);

		scheduleUpdater = new ScheduleUpdater();
		locationUpdater = new LocationManagerUpdater();

		initializeOrigin();
		scheduleUpdater.run();
		sendLocalBroadcast(new Intent(DATA_SET_CHANGED));
	}

	private void sendLocalBroadcast(Intent intent) {
		Log.d("Aeon", "Sending local broadcast: " + intent.getAction());
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	@Override
	public int onStartCommand(Intent theIntent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d("Aeon", "Activity binding to itinerary manager");
		return binder;
	}

	@Override
	public void onRebind(Intent intent) {
		Log.d("Aeon", "Activity re-binding to itinerary manager");
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d("Aeon", "Activity unbinding from itinerary manager");
		return true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("Aeon", "Destroying itinerary manager");
		cancelAlerts();
		locationManager.removeUpdates(locationListener);
		eventHandler.removeCallbacks(locationUpdater);
	}
}