package vanleer.android.aeon;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class DepartureReminder extends Service {

	private NotificationManager notiMgr;

	@Override
	public void onCreate() {
		Log.d("Departure Notification", "Created departure notification service");
		notiMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}

	private void showNotification(ItineraryItem origin, ItineraryItem destination) {
		NotificationCompat.Builder timeToGoNotiBuilder = new NotificationCompat.Builder(this);
		timeToGoNotiBuilder.setContentTitle("Time to leave");
		String notiMessage = "Depart from " + origin.getName() + " and head to " + destination.getName();
		timeToGoNotiBuilder.setContentText(notiMessage);
		timeToGoNotiBuilder.setWhen(origin.getSchedule().getDepartureTime().getTime());
		timeToGoNotiBuilder.setContentInfo(destination.getFormattedDistance());
		timeToGoNotiBuilder.setSmallIcon(R.drawable.arrive_notification);
		timeToGoNotiBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

		// TODO: Display either alert or notification not both
		// TODO: Return to itinerary activity when notification touched
		// TODO: Manage frequency of notifications/alerts

		Intent result = new Intent(this, Itinerary.class);

		PendingIntent pendingResult = PendingIntent.getActivity(this, 0, result, PendingIntent.FLAG_UPDATE_CURRENT);

		timeToGoNotiBuilder.setContentIntent(pendingResult);
		notiMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notiMgr.notify(R.id.departure_reminder_notification, timeToGoNotiBuilder.build());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("Departure Notification", "Received start id " + startId + ": " + intent);
		showNotification((ItineraryItem) intent.getExtras().getParcelable("vanleer.android.aeon.departureReminderOrigin"), (ItineraryItem) intent.getExtras().getParcelable("vanleer.android.aeon.departureReminderDestination"));
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
		// mNM.cancel(R.id.departure_reminder_notification);

		// Tell the user we stopped.
		// Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
