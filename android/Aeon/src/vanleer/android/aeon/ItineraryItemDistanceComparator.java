package vanleer.android.aeon;

import java.util.Comparator;

public class ItineraryItemDistanceComparator implements Comparator<ItineraryItem> {
	public int compare(ItineraryItem leftItem, ItineraryItem rightItem) {
		int compared = 0;
		try {
			if(leftItem.GetDistance() < rightItem.GetDistance()) {
				compared = -1;
			}
			else if(leftItem.GetDistance() > rightItem.GetDistance()) {
				compared = 1;
			}
		}
		catch(NullPointerException e) {
			if(leftItem.GetDistance() == null) {
				compared = 1;
			}
			else if(rightItem.GetDistance() == null) {
				compared = -1;
			}
		}
		return compared;
	}

}
