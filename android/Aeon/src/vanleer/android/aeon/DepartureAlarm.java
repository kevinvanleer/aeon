package vanleer.android.aeon;

import java.io.IOException;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AnalogClock;
import android.widget.Button;
import android.widget.TextView;

public class DepartureAlarm extends Activity implements OnClickListener {
	private AudioManager audioManager;
	private MediaPlayer alarmPlayer;
	private ItineraryItem origin;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.departure_alarm);

		Button dismissAlarm = (Button) findViewById(R.id.button_departureAlarmDismiss);
		dismissAlarm.setOnClickListener(this);
		Button snoozeAlarm = (Button) findViewById(R.id.button_departureAlarmSnooze);
		snoozeAlarm.setOnClickListener(this);

		origin = (ItineraryItem) getIntent().getExtras().getParcelable("vanleer.android.aeon.departureAlarmOrigin");
		// ItineraryItem destination = (ItineraryItem) getIntent().getExtras().getParcelable("vanleer.android.aeon.departureAlarmDestination");

		TextView message = (TextView) findViewById(R.id.textView_departureAlarmMessage);
		message.setText("Time to leave " + origin.getName());

		try {
			Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
			alarmPlayer = new MediaPlayer();
			alarmPlayer.setDataSource(this, alert);

			audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			if (audioManager.getStreamVolume(AudioManager.STREAM_RING) != 0) {
				alarmPlayer.setAudioStreamType(AudioManager.STREAM_RING);
				alarmPlayer.setLooping(true);
				alarmPlayer.prepare();
				alarmPlayer.start();
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.button_departureAlarmDismiss:
			setResult(Activity.RESULT_CANCELED);
			break;
		case R.id.button_departureAlarmSnooze:
			Calendar departureTime = Calendar.getInstance();
			departureTime.setTime(origin.getSchedule().getDepartureTime());
			departureTime.add(Calendar.MINUTE, 5);
			origin.getSchedule().setDepartureTime(departureTime.getTime());
			Intent delayedDeparture = new Intent();
			delayedDeparture.putExtra("destination", origin);
			setResult(Activity.RESULT_OK, delayedDeparture);
			break;
		}

		alarmPlayer.stop();
		finish();
	}
}
