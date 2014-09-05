package vanleer.android.aeon;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

class SearchResultItemAdapter extends ArrayAdapter<ItineraryItem> {

	private ArrayList<ItineraryItem> placesList;
	  
	public SearchResultItemAdapter(Context context, int textViewResourceId, ArrayList<ItineraryItem> items) {
		super(context, textViewResourceId);
		placesList = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi =
					(LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.search_result_item, null);
		}
		if(!placesList.isEmpty()) {
			ItineraryItem item = placesList.get(position);
			if (item != null) {
				TextView name = (TextView) v.findViewById(R.id.textView_name);
				TextView vicinity = (TextView) v.findViewById(R.id.textView_vicinity);
				TextView distance = (TextView) v.findViewById(R.id.textView_distance);
				if (name != null) {
					name.setText(item.getName());
				}
				if (vicinity != null) {
					vicinity.setText(item.getVicinity());
				}
				if(distance != null) {
					distance.setText(item.getFormattedDistance());
				}
			}
		}
		return v;
	}
}
