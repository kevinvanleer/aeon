package vanleer.android.aeon;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import android.location.Location;

public final class ItineraryItem {
	private JSONObject googlePlaceResult = null;
	//private GooglePlacesJSONObject googlePlaceResult = null;
	private JSONObject googleGeocodingResult = null;
	//private GoogleGeocodingJSONObject googleGeocodingResult = null;
	private JSONObject googleDistanceMatrixResult = null;
	private Location location;
	private Long duration;
	private Long distance;
	public String iconUrl;
	private static final double MILES_PER_METER = 0.00062137119;
	
	ItineraryItem(JSONObject searchResult) {
		if(IsGeocodingResult(searchResult)) {
			googleGeocodingResult = searchResult;
		} else {
		   googlePlaceResult = searchResult;
		}
		SetLocation();
	}
	
	private boolean IsGeocodingResult(JSONObject result) {
		boolean isGeocodingResult = false;
		
		if(result != null) {
			isGeocodingResult = (result.get("formatted_address") != null);
		}
		
		return isGeocodingResult;
	}
	
	String GetName() {
		String name;
		
		if(googlePlaceResult != null) {
			name = GetPlaceName();
		} else {
			name = GetGeocodingName();
		}
		
		return name;
	}
	
	private String GetGeocodingName() {
		String streetNumber = "";
		String route = "";
		
		JSONArray addressComponents = (JSONArray) googleGeocodingResult.get("address_components");
		for(int i = 0; i < addressComponents.size(); ++i) {
			JSONObject addressComponent = (JSONObject) addressComponents.get(i);
			if(addressComponent != null) {
				JSONArray componentTypes = (JSONArray) addressComponent.get("types");
				for(int j = 0; j < componentTypes.size(); ++j) {
					String componentType = (String) componentTypes.get(j);
					if(componentType.equals("street_number")) {
						streetNumber = (String) addressComponent.get("long_name");
					} else if(componentType.equals("route")) {
						route = (String) addressComponent.get("short_name");
					}
				}
			}
		}
		
		return (streetNumber + " " + route).trim();
	}

	private String GetPlaceName() {
		return (String) googlePlaceResult.get("name");			
	}
	
	String GetVicinity() {
		String vicinity;
		
		if(googlePlaceResult != null) {
			vicinity = GetPlaceVicinity();
		} else {
			vicinity = GetGeocodingVicinity();
		}
		
		return vicinity;
	}
	
	private String GetGeocodingVicinity() {
		String city = "";
		String state = "";
		String zipCode = "";
		
		JSONArray addressComponents = (JSONArray) googleGeocodingResult.get("address_components");
		for(int i = 0; i < addressComponents.size(); ++i) {
			JSONObject addressComponent = (JSONObject) addressComponents.get(i);
			if(addressComponent != null) {
				JSONArray componentTypes = (JSONArray) addressComponent.get("types");
				for(int j = 0; j < componentTypes.size(); ++j) {
					String componentType = (String) componentTypes.get(j); 
					if(componentType.equals("locality") ||
							componentType.equals("sublocality")) {
						city = (String) addressComponent.get("long_name");
					} else if(componentType.equals("administrative_area_level_1")) {
						state = (String) addressComponent.get("short_name");
					} else if(componentType.equals("postal_code")) {
						zipCode = (String) addressComponent.get("long_name");
					}
				}
			}
		}
		
		return (city + ", " + state + " " + zipCode).trim();
	}

	private String GetPlaceVicinity() {
		return (String) googlePlaceResult.get("vicinity");			
	}
	
	Location GetLocation() {
		return location;
	}
	
	void SetLocation() {
		if(googlePlaceResult != null) {
			SetPlaceLocation();
		} else {
			SetGeocodingLocation();
		}
	}
	
	private void SetGeocodingLocation() {
		location = new Location("Google Geocoding");
		
		JSONObject jsonGeometry = (JSONObject) googleGeocodingResult.get("geometry");
		if(jsonGeometry != null) {
			JSONObject jsonLocation = (JSONObject) jsonGeometry.get("location");
			if(jsonLocation != null) {
				location.setLatitude((Double) jsonLocation.get("lat"));
				location.setLongitude((Double) jsonLocation.get("lng"));
			}
		}
	}

	void SetPlaceLocation() {
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
	
	void SetDistance(final Location origin) {
		distance = (long) location.distanceTo(origin);
		duration = (long) 0;
		googleDistanceMatrixResult = null;
	}
	
	public Long GetDistance() {
		return distance;
	}
	
	public String GetDistanceMeters() {
		return String.format("%1$l m", distance);
	}
	
	public String GetDistanceMiles() {
		return String.format("%1$.1f mi", (distance * MILES_PER_METER));
	}
	public String GetDistanceKilometers() {
		return String.format("%1$.1f km", (distance / 1000.));
	}
	
	public Long GetDuration() {
		return duration;
	}
	
	public String GetDurationClockFormat() {
		long minutes = duration / 60;
		long seconds = duration - minutes;		
		return minutes + ":" + seconds;
	}
}
