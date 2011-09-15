package vanleer.android.aeon;

import android.location.Location;

public final class ItineraryItem {
	public String name;
	private Location location;
	public String vicinity;
	private double distance;
	private static final double MILES_PER_METER = 0.00062137119;
	
	void SetDistance(final Location origin) {
		distance = Math.sqrt(Math.pow((origin.getLatitude() - location.getLatitude()), 2.) + Math.pow((origin.getLongitude() - location.getLongitude()), 2.));
	}
	
	public double GetDistance() {
		return distance;
	}
	
	public String GetDistanceMeters() {
		return Double.toString(distance);
	}
	
	public String GetDistanceMiles() {
		return Double.toString(distance * MILES_PER_METER);
	}
	public String GetDistanceKilometers() {
		return Double.toString(distance / 1000.);
	}
}
