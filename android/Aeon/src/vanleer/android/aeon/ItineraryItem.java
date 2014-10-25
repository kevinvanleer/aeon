package vanleer.android.aeon;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import vanleer.util.DistanceUnit;
import vanleer.util.InvalidDistanceMatrixResponseException;
import vanleer.util.TimeFormat;

import android.location.Location;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public final class ItineraryItem implements Parcelable {
	public DistanceUnit distanceUnit = DistanceUnit.MILES;
	private UUID id = UUID.randomUUID();
	private JSONObject googlePlaceResult = null;
	private JSONObject googleGeocodingResult = null;
	private Address geocodingAddress = null;
	private JSONObject googleDistanceMatrixResult = null;
	private Location location;
	private Long travelDurationSec;
	private Long distance;
	private String iconUrl;
	private Schedule times;
	private String phoneNumber;
	private String name;
	private static final double MILES_PER_METER = 0.00062137119;
	private boolean enRoute = false;
	private boolean atLocation = false;
	private boolean locationExpired = false;

	public ItineraryItem(Address address) {
		geocodingAddress = address;
		name = getGeocodingName();
		setLocation();
		phoneNumber = "NONE";
	}

	public ItineraryItem(JSONObject searchResult) {
		if (isGeocodingResult(searchResult)) {
			googleGeocodingResult = searchResult;
			name = getGeocodingName();
		} else {
			googlePlaceResult = searchResult;
			name = getPlaceName();
		}
		setLocation();

		phoneNumber = "NONE";
	}

	public ItineraryItem(String fakeName) {
		location = null;
		name = fakeName;
		travelDurationSec = null;
		distance = null;
		times = null;
		iconUrl = null;
		phoneNumber = "NONE";
	}

	private ItineraryItem(Parcel in) {
		readFromParcel(in);
	}

	public ItineraryItem(Location myLocation, Address locationAddress) {
		updateLocation(myLocation, locationAddress);
		travelDurationSec = (long) 0;
		distance = (long) 0;
		times = null;
		iconUrl = null;
		phoneNumber = "NONE";
	}

	public ItineraryItem(Location myLocation, Location previousLocation, Address locationAddress) {
		this(myLocation, locationAddress);
		try {
			GoogleDirectionsGiver giver = new GoogleDirectionsGiver(previousLocation, myLocation);

			// TODO: Don't require synchronous waiting here
			DirectionsResult result = giver.get(5, TimeUnit.SECONDS);
			if (result.getRouteDuration() != null) {
				travelDurationSec = result.getRouteDuration();
			}
			if (result.getRouteDistance() != null) {
				distance = result.getRouteDistance();
			}
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean matches(ItineraryItem other) {
		return id.equals(other.id);
	}

	void updateLocation(Location newLocation, Address locationAddress) {
		if (newLocation == null) {
			throw new NullPointerException();
		}
		if (locationAddress == null) {
			geocodingAddress = null;
			name = "Address unknown";
		} else {
			geocodingAddress = locationAddress;
			name = getGeocodingName();
		}

		location = newLocation;
	}

	public void updateSchedule(Date departureTime) {
		Calendar arrival = Calendar.getInstance();
		arrival.setTime(departureTime);
		arrival.add(Calendar.SECOND, travelDurationSec.intValue());
		times.update(arrival.getTime());
	}

	private void readFromParcel(Parcel in) {
		id = (UUID) in.readSerializable();
		googlePlaceResult = (JSONObject) in.readSerializable();
		geocodingAddress = in.readParcelable(null);
		googleGeocodingResult = (JSONObject) in.readSerializable();
		googleDistanceMatrixResult = (JSONObject) in.readSerializable();
		location = in.readParcelable(null);
		travelDurationSec = in.readLong();
		distance = in.readLong();
		iconUrl = in.readString();
		times = in.readParcelable(Schedule.class.getClassLoader());
		phoneNumber = in.readString();
		name = in.readString();
		enRoute = in.readByte() != 0;
		atLocation = in.readByte() != 0;
		locationExpired = in.readByte() != 0;

		if (travelDurationSec < 0) {
			travelDurationSec = null;
		}
		if (distance < 0) {
			distance = null;
		}
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeSerializable(id);
		dest.writeSerializable(googlePlaceResult);
		dest.writeParcelable(geocodingAddress, flags);
		dest.writeSerializable(googleGeocodingResult);
		dest.writeSerializable(googleDistanceMatrixResult);
		dest.writeParcelable(location, flags);
		if (travelDurationSec == null) {
			dest.writeLong(-1);
		} else {
			dest.writeLong(travelDurationSec);
		}
		if (distance == null) {
			dest.writeLong(-1);
		} else {
			dest.writeLong(distance);
		}
		dest.writeString(iconUrl);
		dest.writeParcelable(getSchedule(), flags);
		dest.writeString(phoneNumber);
		dest.writeString(name);
		dest.writeByte((byte) (enRoute ? 1 : 0));
		dest.writeByte((byte) (atLocation ? 1 : 0));
		dest.writeByte((byte) (locationExpired ? 1 : 0));
	}

	private boolean isGeocodingResult(JSONObject result) {
		boolean isGeocodingResult = false;

		if (result != null) {
			isGeocodingResult = (result.get("formatted_address") != null);
		}

		return isGeocodingResult;
	}

	public String setName(final String userDefinedName) {
		return name = userDefinedName;
	}

	public String getName() {
		return name;
	}

	public boolean enRoute() {
		return enRoute;
	}

	public void setEnRoute() {
		enRoute = true;
		atLocation = !enRoute;
		locationExpired = !enRoute;
	}

	public boolean atLocation() {
		return atLocation;
	}

	public void setAtLocation() {
		atLocation = true;
		enRoute = !atLocation;
		locationExpired = !atLocation;
	}

	public boolean locationExpired() {
		return locationExpired;
	}

	public void setLocationExpired() {
		locationExpired = true;
		enRoute = !locationExpired;
		atLocation = !locationExpired;
	}

	private String getGeocodingName() {
		String streetNumber = "";
		String route = "";
		String establishment = "";
		String addressName = "Address unknown";

		if (geocodingAddress != null) {
			// if (geocodingAddress.getFeatureName() != null) {
			// addressName = geocodingAddress.getFeatureName();
			if (geocodingAddress.getMaxAddressLineIndex() >= 1) {
				addressName = geocodingAddress.getAddressLine(0);
				addressName += ", " + geocodingAddress.getAddressLine(1);
				// addressName += ", " + geocodingAddress.getAddressLine(2);
			} else {
				if (geocodingAddress.getMaxAddressLineIndex() >= 0) {
					addressName = geocodingAddress.getAddressLine(0);
				}
				if (geocodingAddress.getLocality() != null) {
					addressName += ", " + geocodingAddress.getLocality();
				}
				if (geocodingAddress.getAdminArea() != null) {
					addressName += ", " + geocodingAddress.getAdminArea();
				}
			}
		} else if (googleGeocodingResult != null) {
			JSONArray addressComponents = (JSONArray) googleGeocodingResult.get("address_components");
			for (int i = 0; i < addressComponents.size(); ++i) {
				JSONObject addressComponent = (JSONObject) addressComponents.get(i);
				if (addressComponent != null) {
					JSONArray componentTypes = (JSONArray) addressComponent.get("types");
					for (int j = 0; j < componentTypes.size(); ++j) {
						String componentType = (String) componentTypes.get(j);
						if (componentType.equals("street_number")) {
							streetNumber = (String) addressComponent.get("long_name");
						} else if (componentType.equals("route")) {
							route = (String) addressComponent.get("short_name");
						} else if (componentType.equals("establishment")) {
							establishment = (String) addressComponent.get("short_name");
						}
					}
				}
			}

			if (!(route.isEmpty() || establishment.isEmpty())) {
				addressName = (route + " " + establishment);
			} else {
				addressName = (streetNumber + " " + route).trim();
			}
		}

		return addressName;
	}

	private String getPlaceName() {
		return (String) googlePlaceResult.get("name");
	}

	String getVicinity() {
		String vicinity;

		if (googlePlaceResult != null) {
			vicinity = getPlaceVicinity();
		} else if (geocodingAddress != null) {
			vicinity = getGeocodingVicinity();
		} else if (googleGeocodingResult != null) {
			vicinity = getGeocodingVicinity();
		} else {
			vicinity = "unknown";
		}

		return vicinity;
	}

	private String getGeocodingVicinity() {
		String vicinity = null;

		if (geocodingAddress != null) {
			vicinity = geocodingAddress.getAddressLine(1);
		} else if (googleGeocodingResult != null) {
			String city = "";
			String state = "";
			String zipCode = "";
			JSONArray addressComponents = (JSONArray) googleGeocodingResult.get("address_components");
			for (int i = 0; i < addressComponents.size(); ++i) {
				JSONObject addressComponent = (JSONObject) addressComponents.get(i);
				if (addressComponent != null) {
					JSONArray componentTypes = (JSONArray) addressComponent.get("types");
					for (int j = 0; j < componentTypes.size(); ++j) {
						String componentType = (String) componentTypes.get(j);
						if (componentType.equals("locality") || componentType.equals("sublocality")) {
							city = (String) addressComponent.get("long_name");
						} else if (componentType.equals("administrative_area_level_1")) {
							state = (String) addressComponent.get("short_name");
						} else if (componentType.equals("postal_code")) {
							zipCode = (String) addressComponent.get("long_name");
						}
					}
				}
			}
			vicinity = (city + ", " + state + " " + zipCode).trim();
		}

		return vicinity;
	}

	private String getPlaceVicinity() {
		if (googlePlaceResult == null) {
			// TODO: Fail gracefully
		}

		return (String) googlePlaceResult.get("vicinity");
	}

	Location getLocation() {
		return location;
	}

	void setLocation(Location location) {
		this.location = location;
	}

	void setLocation() {
		if (googlePlaceResult != null) {
			setPlaceLocation();
		} else {
			setGeocodingLocation();
		}
	}

	private void setGeocodingLocation() {
		location = new Location("Google Geocoding");
		if (geocodingAddress != null) {
			location.setLatitude(geocodingAddress.getLatitude());
			location.setLongitude(geocodingAddress.getLongitude());
		} else if (googleGeocodingResult != null) {
			JSONObject jsonGeometry = (JSONObject) googleGeocodingResult.get("geometry");
			if (jsonGeometry != null) {
				JSONObject jsonLocation = (JSONObject) jsonGeometry.get("location");
				if (jsonLocation != null) {
					location.setLatitude((Double) jsonLocation.get("lat"));
					location.setLongitude((Double) jsonLocation.get("lng"));
				}
			}
		}
	}

	void setPlaceLocation() {
		location = new Location("Google Places");

		JSONObject jsonGeometry = (JSONObject) googlePlaceResult.get("geometry");
		if (jsonGeometry != null) {
			JSONObject jsonLocation = (JSONObject) jsonGeometry.get("location");
			if (jsonLocation != null) {
				location.setLatitude((Double) jsonLocation.get("lat"));
				location.setLongitude((Double) jsonLocation.get("lng"));
			}
		}
	}

	void setDistance(JSONObject distanceMatrixData) {
		if (distanceMatrixData == null) {
			throw new InvalidDistanceMatrixResponseException("Uninitialized distance matrix element");
		}
		String statusCode = (String) distanceMatrixData.get("status");
		if (!statusCode.equals("OK")) {
			throw new InvalidDistanceMatrixResponseException(statusCode);
		}

		googleDistanceMatrixResult = distanceMatrixData;

		JSONObject distanceObject = (JSONObject) googleDistanceMatrixResult.get("distance");
		if (distanceObject != null) {
			distance = (Long) distanceObject.get("value");
		}

		JSONObject durationObject = (JSONObject) googleDistanceMatrixResult.get("duration");
		if (durationObject != null) {
			travelDurationSec = (Long) durationObject.get("value");
		}
	}

	void setDistance(final Location origin) {
		distance = (long) location.distanceTo(origin);
		travelDurationSec = (long) 0;
		googleDistanceMatrixResult = null;
	}

	public void setTravelDuration(long time) {
		if (!atLocation()) {
			throw new IllegalStateException();
		}
		travelDurationSec = time;
	}

	public Long getDistance() {
		return distance;
	}

	public String getFormattedDistance() {
		String distanceString;
		if (distance == null) {
			distanceString = "unknown";
		} else {
			switch (distanceUnit) {
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
		String duration = "...";
		if (travelDurationSec != null) {
			duration = "< 1 min";
			if (travelDurationSec >= 60) {
				duration = TimeFormat.format(travelDurationSec * 1000, TimeFormat.LONG_FORMAT, TimeFormat.MINUTES);
			}
		}
		return duration;
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
		if (times == null) {
			times = new Schedule();
		}
		return times;
	}

	public void setSchedule(Schedule times) {
		this.times = times;
	}

	public static final Parcelable.Creator<ItineraryItem> CREATOR = new Parcelable.Creator<ItineraryItem>() {
		public ItineraryItem createFromParcel(Parcel in) {
			return new ItineraryItem(in);
		}

		public ItineraryItem[] newArray(int size) {
			return new ItineraryItem[size];
		}
	};
}
