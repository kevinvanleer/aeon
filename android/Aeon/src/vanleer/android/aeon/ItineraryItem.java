package vanleer.android.aeon;

import org.json.simple.JSONObject;

import android.location.Location;

public final class ItineraryItem {
	private JSONObject googlePlaceResult;
	private Location location;
	private double distance;
	public String iconUrl;
	private static final double MILES_PER_METER = 0.00062137119;
	
	ItineraryItem(JSONObject place) {
		googlePlaceResult = place;
		SetLocation();
	}
	
	String GetName() {
		return (String) googlePlaceResult.get("name");			
	}
	
	String GetVicinity() {
		return (String) googlePlaceResult.get("vicinity");			
	}
	
	Location GetLocation() {
		return location;
	}
	
	void SetLocation() {
		location = new Location("Google Places");
		
		JSONObject jsonGeometry = (JSONObject) googlePlaceResult.get("geometry");
		if(jsonGeometry != null) {
			JSONObject jsonLocation = (JSONObject) jsonGeometry.get("location");
			if(jsonLocation != null) {
				location.setLatitude((Double) jsonLocation.get("lat"));
				location.setLongitude((Double) jsonLocation.get("lng"));
			}
		}
	}
	
	void SetDistance(final Location origin) {
		distance = location.distanceTo(origin);
	}
	
	public double GetDistance() {
		return distance;
	}
	
	public String GetDistanceMeters() {
		return ReducePrecision(distance) + " m";
	}
	
	public String GetDistanceMiles() {
		return ReducePrecision(distance * MILES_PER_METER) + " mi";
	}
	public String GetDistanceKilometers() {
		return ReducePrecision(distance / 1000.) + " km";
	}
	
	private String ReducePrecision(double value) {
		return ReducePrecision(value, 2);
	}
	private String ReducePrecision(double value, int precision) {
		String stringRep = Double.toString(value);
		int pointIdx = stringRep.indexOf(".");
		
		if(pointIdx >= stringRep.length()) {
			stringRep.concat("00");
		} else if(pointIdx < 0) {
			stringRep.concat(".00");
		}
			
		return stringRep.substring(0, (stringRep.indexOf(".") + precision));
	}
}
