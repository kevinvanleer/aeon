package vanleer.android.aeon;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;

public final class DestinationScheduleActivity extends Activity implements OnClickListener {
	private static final Integer DEFAULT_DURATION_HOUR = 0;
	private static final Integer DEFAULT_DURATION_MIN = 30;
	private CheckBox checkBoxArrivalTime;
	private CheckBox checkBoxDuration;
	private CheckBox checkBoxDepartureTime;

	private TimePicker timePickerArrivalTime;
	private TimePicker timePickerDuration;
	private TimePicker timePickerDepartureTime;

	private TextView textViewArrivalTime;
	private TextView textViewDuration;
	private TextView textViewDepartureTime;

	private ItineraryItem destination;
	private CheckBox checkBoxLastChecked;
	private Button buttonDoneScheduling;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.destination_schedule);

		checkBoxArrivalTime = (CheckBox) findViewById(R.id.checkBox_arrivalTime);
		checkBoxDuration = (CheckBox) findViewById(R.id.checkBox_duration);
		checkBoxDepartureTime = (CheckBox) findViewById(R.id.checkBox_departureTime);

		timePickerArrivalTime = (TimePicker) findViewById(R.id.timePicker_arrivalTime);
		timePickerDuration = (TimePicker) findViewById(R.id.timePicker_duration);
		timePickerDepartureTime = (TimePicker) findViewById(R.id.timePicker_departureTime);

		textViewArrivalTime = (TextView) findViewById(R.id.textView_arrivalTime);
		textViewDuration = (TextView) findViewById(R.id.textView_duration);
		textViewDepartureTime = (TextView) findViewById(R.id.textView_departureTime);

		buttonDoneScheduling = (Button) findViewById(R.id.button_destinationScheduleDone);

		destination = getIntent().getExtras().getParcelable("vanleer.android.aeon.destination");

		InitializeControls();
	}

	private int wantVisible(CheckBox theBox) {
		if (theBox.isChecked() && theBox.isEnabled()) {
			return TimePicker.VISIBLE;
		} else {
			return TimePicker.GONE;
		}
	}

	private boolean wantEnabled(CheckBox theBox) {
		return (theBox.isChecked() && theBox.isEnabled());
	}

	private void InitializeControls() {
		InitializeArrivalControls();
		InitializeDurationControls();
		InitalizeDepartureControls();

		buttonDoneScheduling.setOnClickListener(this);
	}

	private void InitializeArrivalControls() {
		checkBoxArrivalTime.setOnClickListener(this);

		checkBoxArrivalTime.setChecked(true);
		textViewArrivalTime.setText("Getting to " + destination.getName() + " at");

		if (destination.getSchedule().getArrivalTime() != null) {
			checkBoxArrivalTime.setEnabled(false);
			timePickerArrivalTime.setEnabled(false);
			timePickerArrivalTime.setVisibility(TimePicker.GONE);

			Calendar arrivalTimeCalculator = Calendar.getInstance();
			arrivalTimeCalculator.setTime(destination.getSchedule().getArrivalTime());
			timePickerArrivalTime.setCurrentHour(arrivalTimeCalculator.get(Calendar.HOUR_OF_DAY));
			timePickerArrivalTime.setCurrentMinute(arrivalTimeCalculator.get(Calendar.MINUTE));

			SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
			textViewArrivalTime.setText(textViewArrivalTime.getText() + " " + timeFormat.format(destination.getSchedule().getArrivalTime()));
		} else {
			timePickerArrivalTime.setEnabled(wantEnabled(checkBoxArrivalTime));
			timePickerArrivalTime.setVisibility(wantVisible(checkBoxArrivalTime));
		}
	}

	private void InitializeDurationControls() {
		timePickerDuration.setIs24HourView(true);
		checkBoxDuration.setOnClickListener(this);

		textViewDuration.setText("Staying at " + destination.getName() + " for");
		checkBoxDuration.setChecked(wantEnabled(checkBoxArrivalTime));
		timePickerDuration.setEnabled(wantEnabled(checkBoxArrivalTime));
		timePickerDuration.setVisibility(wantVisible(checkBoxDuration));
		checkBoxLastChecked = checkBoxDuration;

		if (destination.getSchedule().getStayDuration() >= 0) {
			long duration = destination.getSchedule().getStayDuration();
			int hours = (int) (duration / 3600);
			int minutes = (int) ((duration - (hours * 3600)) / 60);
			timePickerDuration.setCurrentHour(hours);
			timePickerDuration.setCurrentMinute(minutes);
		} else {
			timePickerDuration.setCurrentHour(DEFAULT_DURATION_HOUR);
			timePickerDuration.setCurrentMinute(DEFAULT_DURATION_MIN);
		}
	}

	private void InitalizeDepartureControls() {
		checkBoxDepartureTime.setOnClickListener(this);

		textViewDepartureTime.setText("Leaving " + destination.getName() + " at");
		checkBoxDepartureTime.setChecked(!wantEnabled(checkBoxArrivalTime));
		timePickerDepartureTime.setEnabled(wantEnabled(checkBoxDepartureTime));
		timePickerDepartureTime.setVisibility(wantVisible(checkBoxDepartureTime));

		Calendar departureTimeCalculator = Calendar.getInstance();

		if (destination.getSchedule().getDepartureTime() != null) {
			departureTimeCalculator.setTime(destination.getSchedule().getDepartureTime());
		} else {
			departureTimeCalculator.setTime(destination.getSchedule().getArrivalTime());
			departureTimeCalculator.add(Calendar.HOUR_OF_DAY, DEFAULT_DURATION_HOUR);
			departureTimeCalculator.add(Calendar.MINUTE, DEFAULT_DURATION_MIN);
		}

		timePickerDepartureTime.setCurrentHour(departureTimeCalculator.get(Calendar.HOUR_OF_DAY));
		timePickerDepartureTime.setCurrentMinute(departureTimeCalculator.get(Calendar.MINUTE));
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.checkBox_arrivalTime:
			if (checkBoxArrivalTime.isChecked()) {
				if (checkBoxLastChecked != checkBoxArrivalTime) {
					checkBoxLastChecked.setChecked(false);
				} else {
					checkBoxDepartureTime.setChecked(false);
				}
			} else {
				checkBoxDuration.setChecked(true);
				checkBoxDepartureTime.setChecked(true);
			}
			checkBoxLastChecked = checkBoxArrivalTime;
			break;
		case R.id.checkBox_duration:
			if (checkBoxDuration.isChecked()) {
				if (checkBoxLastChecked != checkBoxDuration) {
					checkBoxLastChecked.setChecked(false);
				} else {
					checkBoxDepartureTime.setChecked(false);
				}
			} else {
				checkBoxArrivalTime.setChecked(true);
				checkBoxDepartureTime.setChecked(true);
			}
			checkBoxLastChecked = checkBoxDuration;
			break;
		case R.id.checkBox_departureTime:
			if (checkBoxDepartureTime.isChecked()) {
				if (checkBoxLastChecked != checkBoxDepartureTime) {
					checkBoxLastChecked.setChecked(false);
				} else {
					checkBoxDuration.setChecked(false);
				}
			} else {
				checkBoxArrivalTime.setChecked(true);
				checkBoxDuration.setChecked(true);
			}
			checkBoxLastChecked = checkBoxDepartureTime;
			break;
		case R.id.button_destinationScheduleDone:
		default:
			FinishSchedulingDestination();
			break;
		}

		timePickerArrivalTime.setEnabled(wantEnabled(checkBoxArrivalTime));
		timePickerDuration.setEnabled(wantEnabled(checkBoxDuration));
		timePickerDepartureTime.setEnabled(wantEnabled(checkBoxDepartureTime));

		timePickerArrivalTime.setVisibility(wantVisible(checkBoxArrivalTime));
		timePickerDuration.setVisibility(wantVisible(checkBoxDuration));
		timePickerDepartureTime.setVisibility(wantVisible(checkBoxDepartureTime));
	}

	private void FinishSchedulingDestination() {
		calculateScheduling();
		Intent finishedScheduling = new Intent();
		finishedScheduling.putExtra("destination", destination);
		setResult(Activity.RESULT_OK, finishedScheduling);
		finish();
	}

	private void calculateScheduling() {
		timePickerArrivalTime.clearFocus();
		timePickerDuration.clearFocus();
		timePickerDepartureTime.clearFocus();

		Calendar timeConverter = Calendar.getInstance();
		timeConverter.set(Calendar.SECOND, 0);
		if (destination.getSchedule().getArrivalTime() != null) {
			timeConverter.setTime(destination.getSchedule().getArrivalTime());
		} else if (destination.getSchedule().getArrivalTime() != null) {
			timeConverter.setTime(destination.getSchedule().getDepartureTime());
		}

		if (checkBoxArrivalTime.isChecked()) {
			timeConverter.set(Calendar.HOUR_OF_DAY, timePickerArrivalTime.getCurrentHour());
			timeConverter.set(Calendar.MINUTE, timePickerArrivalTime.getCurrentMinute());
			destination.getSchedule().initializeHardArrivalTime(timeConverter.getTime());
			Log.d("Aeon", "Setting hard arrival time for " + destination.getName());
		} else {
			timeConverter.set(Calendar.HOUR_OF_DAY, timePickerDepartureTime.getCurrentHour());
			timeConverter.set(Calendar.MINUTE, timePickerDepartureTime.getCurrentMinute());
			timeConverter.add(Calendar.HOUR_OF_DAY, -timePickerDuration.getCurrentHour());
			timeConverter.add(Calendar.MINUTE, -timePickerDuration.getCurrentMinute());
			destination.getSchedule().initializeFlexibleArrivalTime(timeConverter.getTime());
		}

		if (checkBoxDuration.isChecked()) {
			destination.getSchedule().initializeHardStayDuration((long) ((timePickerDuration.getCurrentHour() * 3600) + ((timePickerDuration.getCurrentMinute() * 60))));
			Log.d("Aeon", "Setting hard stay duration for " + destination.getName());
		} else {
			int durationHour = timePickerDepartureTime.getCurrentHour() - timePickerArrivalTime.getCurrentHour();
			int durationMin = timePickerDepartureTime.getCurrentMinute() - timePickerArrivalTime.getCurrentMinute();

			if (durationHour < 0) durationHour += 24;

			long duration = (durationHour * 3600) + (durationMin * 60);
			destination.getSchedule().initializeFlexibleStayDuration(duration);
		}

		if (checkBoxDepartureTime.isChecked()) {
			timeConverter.set(Calendar.HOUR_OF_DAY, timePickerDepartureTime.getCurrentHour());
			timeConverter.set(Calendar.MINUTE, timePickerDepartureTime.getCurrentMinute());
			destination.getSchedule().initializeHardDepartureTime(timeConverter.getTime());
			Log.d("Aeon", "Setting hard departure time for " + destination.getName());
		} else {
			timeConverter.setTime(destination.getSchedule().getArrivalTime());
			timeConverter.add(Calendar.HOUR_OF_DAY, timePickerDuration.getCurrentHour());
			timeConverter.add(Calendar.MINUTE, timePickerDuration.getCurrentMinute());
			destination.getSchedule().initializeFlexibleDepartureTime(timeConverter.getTime());
		}

		Calendar arrival = Calendar.getInstance();
		Calendar departure = Calendar.getInstance();
		arrival.setTime(destination.getSchedule().getArrivalTime());
		departure.setTime(destination.getSchedule().getDepartureTime());
		if (departure.before(arrival)) {
			departure.set(Calendar.DATE, (arrival.get(Calendar.DATE) + 1));
		}
	}
}
