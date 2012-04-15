package vanleer.android.aeon;

import java.util.Comparator;

public class ItineraryItemDistanceComparator implements Comparator<ItineraryItem> {
	public int compare(ItineraryItem leftItem, ItineraryItem rightItem) {
		int compared = 0;
		try {
			if(leftItem.getDistance() < rightItem.getDistance()) {
				compared = -1;
			}
			else if(leftItem.getDistance() > rightItem.getDistance()) {
				compared = 1;
			}
		}
		catch(NullPointerException e) {
			if(leftItem.getDistance() == null) {
				compared = 1;
			}
			else if(rightItem.getDistance() == null) {
				compared = -1;
			}
		}
		return compared;
	}

}
