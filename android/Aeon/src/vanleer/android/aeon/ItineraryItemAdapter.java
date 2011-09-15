package vanleer.android.aeon;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

class ItineraryItemAdapter extends ArrayAdapter<ItineraryItem> {

	private ArrayList<ItineraryItem> items;
	  
	public ItineraryItemAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);		
	}
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi =
					(LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.itinerary_item, null);
		}
		ItineraryItem item = items.get(position);
		if (item != null) {
			TextView name = (TextView) v.findViewById(R.id.textView_name);
			TextView vicinity = (TextView) v.findViewById(R.id.textView_vicinity);
			TextView distance = (TextView) v.findViewById(R.id.textView_distance);
			if (name != null) {
				name.setText(item.name);
			}
			if (vicinity != null) {
				vicinity.setText(item.vicinity);
			}
			if(distance != null) {
				distance.setText(item.GetDistanceMiles());
			}
		}
		return v;
    }
}
