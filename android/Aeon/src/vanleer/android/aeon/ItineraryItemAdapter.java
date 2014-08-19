package vanleer.android.aeon;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

class ItineraryItemAdapter extends ArrayAdapter<ItineraryItem> {

	private final ArrayList<ItineraryItem> destinationList;
	private ColorStateList defaultColors = null;

	public ItineraryItemAdapter(Context context, int textViewResourceId, ArrayList<ItineraryItem> items) {
		super(context, textViewResourceId);
		destinationList = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.itinerary_item, null);
		}
		if (!destinationList.isEmpty()) {
			ItineraryItem item = destinationList.get(position);

			if (item != null) {
				TextView destinationName = (TextView) v.findViewById(R.id.textView_destinationName);
				TextView stayDuration = (TextView) v.findViewById(R.id.textView_stayDuration);
				TextView arrivalVicinity = (TextView) v.findViewById(R.id.textView_arrivalLocation);
				TextView arrivalTime = (TextView) v.findViewById(R.id.textView_arrivalTime);
				TextView departureVicinity = (TextView) v.findViewById(R.id.textView_departureLocation);
				TextView departureTime = (TextView) v.findViewById(R.id.textView_departureTime);
				TextView travelDistance = (TextView) v.findViewById(R.id.textView_travelDistance);
				TextView travelTime = (TextView) v.findViewById(R.id.textView_travelTime);

				if (defaultColors == null) {
					defaultColors = destinationName.getTextColors();
				}

				// TODO: Get type of transportation used
				// travelDistance.setText("Drive/Walk/Bike/Ride " + item.getFormattedDistance());
				travelDistance.setText("Drive " + item.getFormattedDistance());
				travelTime.setText(" in " + item.getTravelDurationLongFormat());
				arrivalVicinity.setText("Arrive at " + item.GetVicinity());
				arrivalTime.setText(" at " + item.getSchedule().getArrivalTimeString());
				destinationName.setText(item.getName());
				stayDuration.setText(" for " + item.getSchedule().getStayDurationLongFormat());
				departureVicinity.setText("Depart from " + item.GetVicinity());
				departureTime.setText(" at " + item.getSchedule().getDepartureTimeString());

				travelDistance.setEnabled(!item.locationExpired());
				travelTime.setEnabled(!item.locationExpired());
				arrivalVicinity.setEnabled(!item.locationExpired());
				arrivalTime.setEnabled(!item.locationExpired());
				destinationName.setEnabled(!item.locationExpired());
				stayDuration.setEnabled(!item.locationExpired());
				departureVicinity.setEnabled(!item.locationExpired());
				departureTime.setEnabled(!item.locationExpired());

				travelDistance.setVisibility(View.VISIBLE);
				travelTime.setVisibility(View.VISIBLE);
				arrivalVicinity.setVisibility(View.VISIBLE);
				arrivalTime.setVisibility(View.VISIBLE);
				stayDuration.setVisibility(View.VISIBLE);
				departureVicinity.setVisibility(View.VISIBLE);
				departureTime.setVisibility(View.VISIBLE);

				destinationName.setTextColor(defaultColors);

				if (item.getName() == parent.getContext().getString(R.string.add_destination_itinerary_item)) {
					// HACK: Make this better
					arrivalVicinity.setVisibility(View.GONE);
					arrivalTime.setVisibility(View.GONE);
					travelDistance.setVisibility(View.GONE);
					travelTime.setVisibility(View.GONE);
					stayDuration.setVisibility(View.GONE);
					departureVicinity.setVisibility(View.GONE);
					departureTime.setVisibility(View.GONE);

				} else if (position == 0) {
					arrivalVicinity.setVisibility(View.GONE);
					arrivalTime.setVisibility(View.GONE);
					travelDistance.setVisibility(View.GONE);
					travelTime.setVisibility(View.GONE);
					stayDuration.setVisibility(View.GONE);

					departureVicinity.setText("Start from " + item.GetVicinity());
				} else if (position == (destinationList.size() - 1)) {
					stayDuration.setVisibility(View.GONE);
					departureVicinity.setVisibility(View.GONE);
					departureTime.setVisibility(View.GONE);

					arrivalVicinity.setText("End at " + item.GetVicinity());
				} else {

				}

				setCurrentItem(v, item);
			}
		}

		return v;
	}

	@SuppressWarnings("deprecation")
	private void setCurrentItem(View v, ItineraryItem item) {
		TableRow travelInfoRow = (TableRow) v.findViewById(R.id.travelInfo);
		TableRow arrivalInfoRow = (TableRow) v.findViewById(R.id.arrivalInfo);
		TableRow destinationInfoRow = (TableRow) v.findViewById(R.id.destinationInfo);
		TableRow departureInfoRow = (TableRow) v.findViewById(R.id.departureInfo);

		GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[] { 0xff0b4496, 0xff000000 });
		// gd.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		if (item.enRoute()) {
			// Apply gradients like this: http://stackoverflow.com/questions/6115715/how-do-i-programmatically-set-the-background-color-gradient-on-a-custom-title-ba

			// float vcenter = (float) (travelInfoRow.getHeight() / 2.);
			// float hcenter = (float) (travelInfoRow.getWidth() * 0.01);
			// gd.setGradientCenter(hcenter, vcenter);

			travelInfoRow.setBackgroundDrawable(gd);
			// travelInfoRow.setBackgroundColor(0xff0b4496);

		} else {
			travelInfoRow.setBackgroundColor(Color.BLACK);
		}

		if (item.atLocation()) {
			arrivalInfoRow.setBackgroundDrawable(gd);
			destinationInfoRow.setBackgroundDrawable(gd);
			departureInfoRow.setBackgroundDrawable(gd);
			// arrivalInfoRow.setBackgroundColor(0xff0b4496);
			// destinationInfoRow.setBackgroundColor(0xff0b4496);
			// departureInfoRow.setBackgroundColor(0xff0b4496);
		} else {
			arrivalInfoRow.setBackgroundColor(Color.BLACK);
			destinationInfoRow.setBackgroundColor(Color.BLACK);
			departureInfoRow.setBackgroundColor(Color.BLACK);
		}
	}
}
