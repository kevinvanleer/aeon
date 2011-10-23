package vanleer.android.aeon;

import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

public final class ItineraryItem implements Parcelable {
	private JSONObject googlePlaceResult = null;
	//private GooglePlacesJSONObject googlePlaceResult = null;
	private JSONObject googleGeocodingResult = null;
	//private GoogleGeocodingJSONObject googleGeocodingResult = null;
	private JSONObject googleDistanceMatrixResult = null;
	private Location location;
	private Long travelDuration;
	private Long distance;
	public String iconUrl;
	private Date arrivalTime;
	private Date departureTime;
	private Long stayDuration;
	private String phoneNumber;
	private static final double MILES_PER_METER = 0.00062137119;
	
	public ItineraryItem(JSONObject searchResult) {
		if(IsGeocodingResult(searchResult)) {
			googleGeocodingResult = searchResult;
		} else {
		   googlePlaceResult = searchResult;
		}
		SetLocation();
		
		arrivalTime = new Date();
		departureTime = new Date();
		stayDuration = (long) 0;
		phoneNumber = "NONE";
	}
	
	private ItineraryItem(Parcel in) {
		googlePlaceResult = (JSONObject) in.readSerializable();
		googleGeocodingResult = (JSONObject) in.readSerializable();
		googleDistanceMatrixResult = (JSONObject) in.readSerializable();
		location = in.readParcelable(null);
		travelDuration = in.readLong();
		distance = in.readLong();
		iconUrl = in.readString();
		arrivalTime = (Date) in.readSerializable();
		departureTime = (Date) in.readSerializable();
		stayDuration = in.readLong();
		phoneNumber = in.readString();
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
				travelDuration = (Long) durationObject.get("value");
			}
		}
	}
	
	void SetDistance(final Location origin) {
		distance = (long) location.distanceTo(origin);
		travelDuration = (long) 0;
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
	
	public Long GetTravelDuration() {
		return travelDuration;
	}
	
	public String GetTravelDurationClockFormat() {
		long minutes = travelDuration / 60;
		long seconds = travelDuration - minutes;
		return minutes + ":" + seconds;
	}

	public Date GetArrivalTime() {
		return arrivalTime;
	}

	public void SetArrivalTime(Date arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public Date GetDepartureTime() {
		return departureTime;
	}

	public void SetDepartureTime(Date departureTime) {
		this.departureTime = departureTime;
	}

	public Long GetStayDuration() {
		return stayDuration;
	}
	
	public String GetStayDurationClockFormat() {
		long minutes = stayDuration / 60;
		long seconds = stayDuration - minutes;		
		return minutes + ":" + seconds;
	}

	public void SetStayDuration(Long stayDuration) {
		this.stayDuration = stayDuration;
	}

	public String GetPhoneNumber() {
		return phoneNumber;
	}

	public void SetPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeSerializable(googlePlaceResult);
		dest.writeSerializable(googleGeocodingResult);
		dest.writeSerializable(googleDistanceMatrixResult);
		dest.writeParcelable(location, flags);
		dest.writeLong(travelDuration);
		dest.writeLong(distance);
		dest.writeString(iconUrl);
		dest.writeSerializable(arrivalTime);
		dest.writeSerializable(departureTime);
		dest.writeLong(stayDuration);
		dest.writeString(phoneNumber);
	}
	
	public static final Parcelable.Creator<ItineraryItem> CREATOR =
			new Parcelable.Creator<ItineraryItem>() {
		public ItineraryItem createFromParcel(Parcel in) {
			return new ItineraryItem(in);
		}
		
		 public ItineraryItem[] newArray(int size) {
             return new ItineraryItem[size];
         }
	};
}
