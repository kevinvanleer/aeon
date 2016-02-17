package vanleer.android.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

public class UnfilteredArrayAdapter<T> extends ArrayAdapter<T> {
	private List<T> elements = new ArrayList<T>();
	private Filter filter = new PassAllFilter();

	public UnfilteredArrayAdapter(Context context, int resource,
			int textViewResourceId, List<T> objects) {
		super(context, resource, textViewResourceId, objects);
	}

	public UnfilteredArrayAdapter(Context context, int resource,
			int textViewResourceId, T[] objects) {
		super(context, resource, textViewResourceId, objects);
	}

	public UnfilteredArrayAdapter(Context context, int resource,
			int textViewResourceId) {
		super(context, resource, textViewResourceId);
	}

	public UnfilteredArrayAdapter(Context context, int textViewResourceId,
			List<T> objects) {
		super(context, textViewResourceId, objects);
	}

	public UnfilteredArrayAdapter(Context context, int textViewResourceId,
			T[] objects) {
		super(context, textViewResourceId, objects);
	}

	public UnfilteredArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	@Override
	public Filter getFilter() {
		return filter;
	}

	@Override
	public void remove(T object) {
		elements.remove(object);
	}

	@Override
	public T getItem(int position) {
		return elements.get(position);
	}

	@Override
	public int getCount() {
		return elements.size();
	}

	@Override
	public void add(T object) {
		elements.add(object);
	}
	
	@Override
	public void clear() {
		elements.clear();
	}

	private class PassAllFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence arg0) {
			FilterResults results = new FilterResults();
			results.values = new ArrayList<T>(elements);
			results.count = elements.size();
			return results;
		}

		@Override
		protected void publishResults(CharSequence arg0, FilterResults arg1) {
			notifyDataSetChanged();
		}
	}

}
