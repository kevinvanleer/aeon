package vanleer.android.aeon;

import org.json.simple.JSONObject;

import android.location.Location;

public final class ItineraryItem {
	private JSONObject googlePlaceResult;
	private JSONObject googleDistanceMatrixResult;
	private Location location;
	private Long duration;
	private Long distance;
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
	
	void SetDistance(JSONObject distanceMatrixData) {
		if(distanceMatrixData != null) {
			googleDistanceMatrixResult = distanceMatrixData;
			
			JSONObject distanceObject = (JSONObject) googleDistanceMatrixResult.get("distance");
			if(distanceObject != null) {
				distance = (Long) distanceObject.get("value");
			}
			
			JSONObject durationObject = (JSONObject) googleDistanceMatrixResult.get("duration");
			if(durationObject != null) {
				duration = (Long) durationObject.get("value");
			}
		}
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
		distance = (long) location.distanceTo(origin);
		duration = (long) 0;
		googleDistanceMatrixResult = null;
	}
	
	public Long GetDistance() {
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
	
	public Long GetDuration() {
		return duration;
	}
	
	public String GetDurationClockFormat() {
		long minutes = duration / 60;
		long seconds = duration - minutes;		
		return minutes + ":" + seconds;
	}
	
	private String ReducePrecision(double value) {
		return ReducePrecision(value, 1);
	}
	private String ReducePrecision(double value, int precision) {
		if(precision > 0) {
			++precision;
		}
		
		String stringRep = Double.toString(value);
		int pointIdx = stringRep.indexOf(".");

		//TODO: Add chars necessary to provide desired precision
		if(pointIdx >= stringRep.length()) {
			stringRep.concat("00");
		} else if(pointIdx < 0) {
			stringRep.concat(".00");
		}
			
		return stringRep.substring(0, (stringRep.indexOf(".") + precision));
	}
}
