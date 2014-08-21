package vanleer.android.aeon;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class DepartureAlarm extends Activity implements OnClickListener {
	private AudioManager audioManager;
	private MediaPlayer alarmPlayer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.departure_alarm);

		Button dismissAlarm = (Button) findViewById(R.id.button_departureAlarmDismiss);
		dismissAlarm.setOnClickListener(this);
		Button snoozeAlarm = (Button) findViewById(R.id.button_departureAlarmSnooze);
		snoozeAlarm.setOnClickListener(this);

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
			alarmPlayer.stop();
			finish();
		}
	}
}
