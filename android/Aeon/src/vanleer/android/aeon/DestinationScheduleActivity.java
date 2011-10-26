package vanleer.android.aeon;

import java.util.Calendar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;

public final class DestinationScheduleActivity extends Activity implements OnClickListener {
	
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
		
		destination = getIntent().getExtras().getParcelable("destination");
		
		InitializeControls();
	}
	
	private void InitializeControls() {
		InitializeArrivalControls();
		InitializeDurationControls();
		InitalizeDepartureControls();
		
		buttonDoneScheduling.setOnClickListener(this);
	}

	private void InitializeArrivalControls() {
		checkBoxArrivalTime.setOnClickListener(this);
		
		textViewArrivalTime.setText("Getting to " + destination.GetName() + " at");
		checkBoxArrivalTime.setChecked(true);
		timePickerArrivalTime.setEnabled(checkBoxArrivalTime.isChecked());
	}

	private void InitializeDurationControls() {
		timePickerDuration.setIs24HourView(true);
		checkBoxDuration.setOnClickListener(this);
		
		textViewDuration.setText("Staying at " + destination.GetName() + " for");
		checkBoxDuration.setChecked(true);
		timePickerDuration.setEnabled(checkBoxDuration.isChecked());
		checkBoxLastChecked = checkBoxDuration;
	}

	private void InitalizeDepartureControls() {
		checkBoxDepartureTime.setOnClickListener(this);
		
		textViewDepartureTime.setText("Leaving " + destination.GetName() + " at");
		checkBoxDepartureTime.setChecked(false);
		timePickerDepartureTime.setEnabled(checkBoxDepartureTime.isChecked());
	}

	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.checkBox_arrivalTime:
			if(checkBoxArrivalTime.isChecked()) {
				if(checkBoxLastChecked != checkBoxArrivalTime) {
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
			if(checkBoxDuration.isChecked()) {
				if(checkBoxLastChecked != checkBoxDuration) {
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
			if(checkBoxDepartureTime.isChecked()) {
				if(checkBoxLastChecked != checkBoxDepartureTime) {
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
		
		timePickerArrivalTime.setEnabled(checkBoxArrivalTime.isChecked());
		timePickerDuration.setEnabled(checkBoxDuration.isChecked());
		timePickerDepartureTime.setEnabled(checkBoxDepartureTime.isChecked());
	}

	private void FinishSchedulingDestination() {
		CalculateScheduling();
		
		Intent finishedScheduling = new Intent();
		finishedScheduling.putExtra("destination", destination);
		setResult(Activity.RESULT_OK, finishedScheduling);
		finish();
	}

	private void CalculateScheduling() {
		Calendar timeConverter = Calendar.getInstance();
		
		if(checkBoxArrivalTime.isChecked()) {
			timeConverter.set(Calendar.HOUR_OF_DAY, timePickerArrivalTime.getCurrentHour());
			timeConverter.set(Calendar.MINUTE, timePickerArrivalTime.getCurrentMinute());
			destination.SetArrivalTime(timeConverter.getTime());
		}
		
		if(checkBoxDuration.isChecked()) {
			destination.SetStayDuration((long) ((timePickerDuration.getCurrentHour() * 60) +
					(timePickerDuration.getCurrentMinute())));
		}
		
		if(checkBoxDepartureTime.isChecked()) {
			timeConverter.set(Calendar.HOUR_OF_DAY, timePickerDepartureTime.getCurrentHour());
			timeConverter.set(Calendar.MINUTE, timePickerDepartureTime.getCurrentMinute());
			destination.SetDepartureTime(timeConverter.getTime());
		}
	}
}
