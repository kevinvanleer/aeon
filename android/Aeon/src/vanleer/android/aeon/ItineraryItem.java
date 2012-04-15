package vanleer.android.aeon;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import vanleer.util.DistanceUnit;
import vanleer.util.TimeFormat;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

public final class ItineraryItem implements Parcelable {
	public DistanceUnit distanceUnit = DistanceUnit.MILES;
	private JSONObject googlePlaceResult = null;
	private JSONObject googleGeocodingResult = null;
	private JSONObject googleDistanceMatrixResult = null;
	private Location location;
	private Long travelDurationSec;
	private Long distance;
	private String iconUrl;
	private Schedule times;
	private String phoneNumber;
	private String name;
	private static final double MILES_PER_METER = 0.00062137119;
	private static final String API_KEY = "AIzaSyCXMEFDyFQK2Wu0-w0dyxs-nEO3uZoXUCc";

	public ItineraryItem(JSONObject searchResult) {
		if(isGeocodingResult(searchResult)) {
			googleGeocodingResult = searchResult;
			name = getGeocodingName();
		} else {
			googlePlaceResult = searchResult;
			name = getPlaceName();
		}
		setLocation();

		phoneNumber = "NONE";
	}

	private ItineraryItem(Parcel in) {
		readFromParcel(in);
	}
	
	public ItineraryItem(Location myLocation) {
		GooglePlacesSearch googleSearch = new GooglePlacesSearch(API_KEY, "");
		location = myLocation;
		name = googleSearch.ReverseGeocode(location, true); 
		googlePlaceResult = null;
		googleGeocodingResult = null;
		googleDistanceMatrixResult = null;
		travelDurationSec = (long) 0;
		distance = (long) 0;
		times = null;
		iconUrl = null;
		phoneNumber = "NONE";
	}

	public ItineraryItem(Location myLocation, Location previousLocation) {
		GooglePlacesSearch googleSearch = new GooglePlacesSearch(API_KEY, "");
		location = myLocation;
		name = googleSearch.ReverseGeocode(location, true); 
	}

	private void readFromParcel(Parcel in) {
		googlePlaceResult = (JSONObject) in.readSerializable();
		googleGeocodingResult = (JSONObject) in.readSerializable();
		googleDistanceMatrixResult = (JSONObject) in.readSerializable();
		location = in.readParcelable(null);
		travelDurationSec = in.readLong();
		distance = in.readLong();
		iconUrl = in.readString();
		times = in.readParcelable(null);
		phoneNumber = in.readString();
		name = in.readString(); 
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeSerializable(googlePlaceResult);
		dest.writeSerializable(googleGeocodingResult);
		dest.writeSerializable(googleDistanceMatrixResult);
		dest.writeParcelable(location, flags);
		dest.writeLong(travelDurationSec);
		dest.writeLong(distance);
		dest.writeString(iconUrl);
		dest.writeParcelable(times, flags);
		dest.writeString(phoneNumber);
		dest.writeString(name);
	}

	private boolean isGeocodingResult(JSONObject result) {
		boolean isGeocodingResult = false;

		if(result != null) {
			isGeocodingResult = (result.get("formatted_address") != null);
		}

		return isGeocodingResult;
	}

	String getName() {
		return name;
	}

	private String getGeocodingName() {
		String streetNumber = "";
		String route = "";
		String establishment = "";

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
					} else if(componentType.equals("establishment")) {
						establishment = (String) addressComponent.get("short_name");
					}
				}
			}
		}

		String addressName;
		if(!(route.isEmpty() || establishment.isEmpty())) {
			addressName = (route + " " + establishment);
		} else {
			addressName = (streetNumber + " " + route).trim();
		}
		
		return addressName;
	}

	private String getPlaceName() {
		return (String) googlePlaceResult.get("name");
	}

	String GetVicinity() {
		String vicinity;

		if(googlePlaceResult != null) {
			vicinity = getPlaceVicinity();
		} else {
			vicinity = getGeocodingVicinity();
		}

		return vicinity;
	}

	private String getGeocodingVicinity() {
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

	private String getPlaceVicinity() {
		return (String) googlePlaceResult.get("vicinity");
	}

	Location getLocation() {
		return location;
	}

	void setLocation() {
		if(googlePlaceResult != null) {
			setPlaceLocation();
		} else {
			setGeocodingLocation();
		}
	}

	private void setGeocodingLocation() {
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

	void setPlaceLocation() {
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

	void setDistance(JSONObject distanceMatrixData) {
		if(distanceMatrixData != null) {
			googleDistanceMatrixResult = distanceMatrixData;

			JSONObject distanceObject = (JSONObject) googleDistanceMatrixResult.get("distance");
			if(distanceObject != null) {
				distance = (Long) distanceObject.get("value");
			}

			JSONObject durationObject = (JSONObject) googleDistanceMatrixResult.get("duration");
			if(durationObject != null) {
				travelDurationSec = (Long) durationObject.get("value");
			}
		}
	}

	void setDistance(final Location origin) {
		distance = (long) location.distanceTo(origin);
		travelDurationSec = (long) 0;
		googleDistanceMatrixResult = null;
	}

	public Long getDistance() {
		return distance;
	}

	public String getFormattedDistance() {
		String distanceString;
		if(distance == null) {
			distanceString = "unknown";
		} else {
			switch(distanceUnit) {
			case METERS:
				distanceString = getDistanceMeters();
				break;
			case KILOMETERS:
				distanceString = getDistanceKilometers();
				break;
			case MILES:
				distanceString = getDistanceMiles();
				break;
			default:
				distanceString = "unsupported unit";
				break;
			}
		}

		return distanceString;
	}
	
	private String getDistanceMeters() {
		return String.format("%1$l m", distance);
	}

	private String getDistanceMiles() {
		return String.format("%1$.1f mi", (distance * MILES_PER_METER));
	}
	private String getDistanceKilometers() {
		return String.format("%1$.1f km", (distance / 1000.));
	}

	public Long getTravelDuration() {
		return travelDurationSec;
	}

	public String getTravelDurationClockFormat() {
		return TimeFormat.format(travelDurationSec * 1000, TimeFormat.SHORT_FORMAT, TimeFormat.MINUTES);
	}

	public String getTravelDurationLongFormat() {
		return TimeFormat.format(travelDurationSec * 1000, TimeFormat.LONG_FORMAT, TimeFormat.MINUTES);
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNum) {
		this.phoneNumber = phoneNum;
	}

	
	public int describeContents() {
		return 0;
	}

	public Schedule getSchedule() {
		return times;
	}

	public void setSchedule(Schedule times) {
		this.times = times;
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
