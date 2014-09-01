package vanleer.android.aeon;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class DepartureReminder extends Service {

	private NotificationManager notiMgr;

	@Override
	public void onCreate() {
		Log.d("Aeon", "Created departure notification service");
		notiMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}

	private void showNotification(Bundle extras) {
		ItineraryItem origin = extras.getParcelable("vanleer.android.aeon.departureReminderOrigin");
		ItineraryItem destination = extras.getParcelable("vanleer.android.aeon.departureReminderDestination");
		int reminderAdvance = extras.getInt("vanleer.android.aeon.departureReminderAdvance");

		NotificationCompat.Builder timeToGoNotiBuilder = new NotificationCompat.Builder(this);
		timeToGoNotiBuilder.setContentTitle("Departing in " + reminderAdvance + " minutes");
		String message = "Head to " + destination.getName();
		timeToGoNotiBuilder.setContentText(message);
		timeToGoNotiBuilder.setWhen(origin.getSchedule().getDepartureTime().getTime());
		timeToGoNotiBuilder.setContentInfo(destination.getFormattedDistance());
		timeToGoNotiBuilder.setSmallIcon(R.drawable.arrive_notification);
		timeToGoNotiBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
		timeToGoNotiBuilder.setAutoCancel(true);

		// TODO: Return to itinerary activity when notification touched

		Intent result = new Intent(getBaseContext(), Itinerary.class);
		result.setAction(Intent.ACTION_MAIN);
		result.addCategory(Intent.CATEGORY_LAUNCHER);

		PendingIntent pendingResult = PendingIntent.getActivity(getBaseContext(), 0, result, 0);

		timeToGoNotiBuilder.setContentIntent(pendingResult);
		notiMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notiMgr.notify(R.id.departure_reminder_notification, timeToGoNotiBuilder.build());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("Aeon", "Received start id " + startId + ": " + intent);
		showNotification(intent.getExtras());
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