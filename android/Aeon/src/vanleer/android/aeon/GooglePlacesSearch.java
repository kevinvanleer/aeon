package vanleer.android.aeon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.lang.Math;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;

public final class GooglePlacesSearch {
	private static final String GOOGLE_PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json";
	private static final String GOOGLE_PLACES_AUTOCOMPLETE_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json";
	// private static final String GOOGLE_GEOCODING_URL = "https://maps.googleapis.com/maps/api/geocode/json";
	private static final String GOOGLE_DISTANCE_MATRIX_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";
	private String apiKey = null;
	private final ArrayList<ItineraryItem> places = new ArrayList<ItineraryItem>();
	private final ItineraryItemDistanceComparator distanceCompare = new ItineraryItemDistanceComparator();
	private final Geocoder externalGeocoder;
	private static final ArrayList<String> placeTypes = new ArrayList<String>();

	GooglePlacesSearch(Geocoder geocoder, String userApiKey, String userClientId) {
		apiKey = userApiKey;
		externalGeocoder = geocoder;
		if (placeTypes.isEmpty()) {
			InitializePlaceTypes();
		}
	}

	void PerformSearch(double latitude, double longitude, double radius, String name, boolean sensor) {
		String type = FindType(name);
		if (type != null) {
			String[] types = new String[1];
			types[0] = type;
			PerformSearch(latitude, longitude, radius, types, "", sensor);
		} else {
			PerformSearch(latitude, longitude, radius, null, name, sensor);
		}
	}

	private String FindType(String name) {
		String inferredType = null;

		name = name.toLowerCase();
		if (name.endsWith("es")) {
			name = name.substring(0, (name.length() - 2));
		}
		if (name.endsWith("s")) {
			name = name.substring(0, (name.length() - 1));
		}

		Iterator<String> itr = placeTypes.iterator();
		while (itr.hasNext()) {
			String type = itr.next();
			if (name.compareTo(type.replace("_", " ")) == 0) {
				inferredType = type;
				break;
			}
		}

		return inferredType;
	}

	void PerformSearch(double latitude, double longitude, double radius, String[] types, boolean sensor) {
		PerformSearch(latitude, longitude, radius, types, "", sensor);
	}

	void PerformSearch(double latitude, double longitude, double radius, String[] types, String name, boolean sensor) {
		ClearSearchResults();

		PerformPlacesSearch(latitude, longitude, radius, types, name, sensor);
		PerformGeocodingSearch(name, sensor);

		if (places.size() > 0) {
			JSONObject distanceMatrixResults = GetDistances(latitude, longitude, sensor);
			ParseDistanceMatrixResults(distanceMatrixResults);
			Collections.sort(places, distanceCompare);
		}
	}

	private void ClearSearchResults() {
		if (places != null) {
			places.clear();
		}
	}

	public void PerformPlacesSearch(double latitude, double longitude, double radius, String[] types, String name, boolean sensor) {
		String url = BuildGooglePlacesSearchUrl(latitude, longitude, radius, types, name, sensor);
		JSONObject placesSearchResults = PerformHttpGet(url);
		AddPlacesResults(placesSearchResults);
	}

	public ArrayList<String> performPlacesAutocomplete(String input, boolean sensor, Double latitude, Double longitude, Double radius, String[] types, Long offset) {
		String url = BuildGooglePlacesAutocompleteUrl(input, sensor, latitude, longitude, radius, types, offset);

		JSONObject autocompleteResults = PerformHttpGet(url);
		return GetResultsList(autocompleteResults);
	}

	private ArrayList<String> GetResultsList(JSONObject autocompleteResults) {
		ArrayList<String> results = new ArrayList<String>();

		if (autocompleteResults != null) {
			JSONArray resultArray = (JSONArray) autocompleteResults.get("predictions");
			if (resultArray != null) {
				for (int index = 0; index < resultArray.size(); ++index) {
					JSONObject result = (JSONObject) resultArray.get(index);
					if (result != null) {
						results.add((String) result.get("description"));
					}
				}
			}
		}

		return results;
	}

	private String BuildGooglePlacesSearchUrl(double latitude, double longitude, double radius, String[] types, String name, boolean sensor) {

		String url = GOOGLE_PLACES_SEARCH_URL;

		url += "?location=" + latitude + "," + longitude;
		url += "&radius=" + radius;

		if (types != null) {
			url += "&types=" + GetTypesUrlPart(types);
		}

		if (name != "") {
			url += "&name=" + Uri.encode(name);
		}

		url += "&sensor=" + sensor;
		url += "&key=" + apiKey;

		return url;
	}

	private String BuildGooglePlacesAutocompleteUrl(String input, boolean sensor, Double latitude, Double longitude, Double radius, String[] types, Long offset) {
		String url = GOOGLE_PLACES_AUTOCOMPLETE_URL;

		if (input == null || input == "") {
			throw new IllegalArgumentException("Places autocomplete search requires an input string");
		}

		url += "?input=" + Uri.encode(input);
		url += "&types=establishment";
		if (types != null) {
			url += "|" + GetTypesUrlPart(types);
		}

		if ((latitude != null) && (longitude != null)) {
			url += "&location=" + latitude + "," + longitude;
		}

		if (radius != null) {
			url += "&radius=" + radius;
		}

		if ((offset != 0) && (offset != null)) {
			url += "&offset=" + offset;
		}

		url += "&sensor=" + sensor;
		url += "&key=" + apiKey;

		return url;
	}

	private String GetTypesUrlPart(String[] types) {
		String typesUrlPart = "";

		for (int i = 0; i < types.length; ++i) {
			if (i > 0) {
				typesUrlPart += "|";
			}

			typesUrlPart += types[i];
		}

		return Uri.encode(typesUrlPart);
	}

	private void AddPlacesResults(JSONObject placesSearchResults) {
		if (placesSearchResults != null) {
			JSONArray placesResultArray = (JSONArray) placesSearchResults.get("results");
			if (placesResultArray != null) {
				for (int index = 0; index < placesResultArray.size(); ++index) {
					JSONObject place = (JSONObject) placesResultArray.get(index);
					if (place != null) {
						places.add(new ItineraryItem(place));
					}
				}
			}
		}
	}

	private void PerformGeocodingSearch(String address, boolean sensor) {
		/*
		 * String url = GOOGLE_GEOCODING_URL + "?address=" + Uri.encode(address) + "&sensor=" + sensor; JSONObject geocodingSearchResults = PerformHttpGet(url);
		 */
		List<Address> geocodingSearchResults = null;
		try {
			geocodingSearchResults = externalGeocoder.getFromLocationName(address, 10);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		AddGeocodingResults(geocodingSearchResults);
	}

	private void AddGeocodingResults(List<Address> geocodingSearchResults) {
		if (geocodingSearchResults != null) {
			if (!geocodingSearchResults.isEmpty()) {
				for (int index = 0; index < geocodingSearchResults.size(); ++index) {
					Address place = geocodingSearchResults.get(index);
					places.add(new ItineraryItem(place));
				}
			}
		}
	}

	private JSONObject GetDistances(double latitude, double longitude, boolean sensor) {
		String url = BuildDistanceMatrixUrl(latitude, longitude, sensor);
		return PerformHttpGet(url);
	}

	public String getReverseGeocodeDescription(final Location location, Boolean sensor) {
		String bestDescription = "Address unknown";

		Address placemark = getBestReverseGeocodeResult(location, sensor);
		if (placemark != null) {
			// bestDescription = (String) placemark.get("formatted_address");
			if (placemark.getMaxAddressLineIndex() >= 0) {
				bestDescription = placemark.getAddressLine(0) + ", ";
			}
			bestDescription += placemark.getLocality();
		}

		return bestDescription;
	}

	public Address getBestReverseGeocodeResult(final Location location, Boolean sensor) {
		/*
		 * JSONObject placemark = null; JSONObject placemarks = getReverseGeocodeResults(location, sensor); if (placemarks != null) { JSONArray resultArray = (JSONArray) placemarks.get("results"); if (resultArray != null) { if (resultArray.size() > 0) { placemark = (JSONObject) resultArray.get(0); } } } return placemark;
		 */
		Address placemark = null;
		List<Address> addresses = getReverseGeocodeResults(location, sensor);
		if (addresses.size() > 0) {
			placemark = getReverseGeocodeResults(location, sensor).get(0);
		}

		return placemark;
	}

	public List<Address> getReverseGeocodeResults(final Location location, Boolean sensor) {
		List<Address> addressList = null;
		try {
			addressList = externalGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return addressList;
		// String url = (GOOGLE_GEOCODING_URL + "?latlng=" + Double.toString(location.getLatitude()) + "," + Double.toString(location.getLongitude()) + "&sensor=" + sensor.toString());
		// return PerformHttpGet(url);
	}

	private JSONObject PerformHttpGet(final String url) {
		AsyncTask<String, Void, JSONObject> get = new AsyncTask<String, Void, JSONObject>() {
			@Override
			protected JSONObject doInBackground(String... arg0) {
				JSONObject jsonResponse = null;
				try {
					HttpClient httpClient = new DefaultHttpClient();
					HttpResponse response = httpClient.execute(new HttpGet(url));
					StatusLine statusLine = response.getStatusLine();
					if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
						InputStream inStream = response.getEntity().getContent();
						BufferedReader reader = new BufferedReader(new InputStreamReader(inStream), 8);
						jsonResponse = (JSONObject) JSONValue.parse(reader);
					}
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return jsonResponse;
			}
		};
		get.execute(url);

		JSONObject jsonResponse = null;
		try {
			jsonResponse = get.get(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jsonResponse;
	}

	private String BuildDistanceMatrixUrl(double latitude, double longitude, boolean sensor) {
		String url = GOOGLE_DISTANCE_MATRIX_URL;
		url += "?origins=" + latitude + "," + longitude;
		String destinations = GetDestinationsUrlPart();
		url += "&destinations=" + Uri.encode(destinations);
		url += "&sensor=" + sensor;
		return url;
	}

	private String GetDestinationsUrlPart() {
		String destinations = "";
		Iterator<ItineraryItem> placeIterator = places.iterator();
		while (placeIterator.hasNext()) {
			ItineraryItem place = placeIterator.next();
			if (place != null) {
				destinations += place.getLocation().getLatitude() + ",";
				destinations += place.getLocation().getLongitude() + "|";
			}
		}
		if (!destinations.isEmpty()) {
			destinations = destinations.substring(0, destinations.length() - 1);
		}
		return destinations;
	}

	private void ParseDistanceMatrixResults(JSONObject distanceMatrix) {
		if (distanceMatrix != null) {
			JSONArray results = (JSONArray) distanceMatrix.get("rows");
			JSONArray resultArray = (JSONArray) ((JSONObject) results.get(0)).get("elements");
			if (resultArray != null) {
				for (int index = 0; index < places.size(); ++index) {
					JSONObject distance = (JSONObject) resultArray.get(index);
					if (distance != null) {
						places.get(index).setDistance(distance);
					}
				}
			}
		}
	}

	public ItineraryItem GetPlace(final int index) {
		return places.get(index);
	}

	public int GetResultCount() {
		return places.size();
	}

	static String GetGeodeticString(Location location) {
		String latSuffix = "° N";
		String lngSuffix = "° E";
		if (location.getLatitude() < 0) {
			latSuffix = "° S";
		}
		if (location.getLongitude() < 0) {
			lngSuffix = "° W";
		}

		String latString = String.format("%1$.4f", Math.abs(location.getLatitude()));
		String lngString = String.format("%1$.4f", Math.abs(location.getLongitude()));

		return (latString + latSuffix + ", " + lngString + lngSuffix);
	}

	private void InitializePlaceTypes() {
		// Levenshtein distance
		// frej
		placeTypes.add("accounting");
		placeTypes.add("airport");
		placeTypes.add("amusement_park");
		placeTypes.add("aquarium");
		placeTypes.add("art_gallery");
		placeTypes.add("atm");
		placeTypes.add("bakery");
		placeTypes.add("bank");
		placeTypes.add("bar");
		placeTypes.add("beauty_salon");
		placeTypes.add("bicycle_store");
		placeTypes.add("book_store");
		placeTypes.add("bowling_alley");
		placeTypes.add("bus_station");
		placeTypes.add("cafe");
		placeTypes.add("campground");
		placeTypes.add("car_dealer");
		placeTypes.add("car_rental");
		placeTypes.add("car_repair");
		placeTypes.add("car_wash");
		placeTypes.add("casino");
		placeTypes.add("cemetery");
		placeTypes.add("church");
		placeTypes.add("city_hall");
		placeTypes.add("clothing_store");
		placeTypes.add("convenience_store");
		placeTypes.add("courthouse");
		placeTypes.add("dentist");
		placeTypes.add("department_store");
		placeTypes.add("doctor");
		placeTypes.add("electrician");
		placeTypes.add("electronics_store");
		placeTypes.add("embassy");
		placeTypes.add("establishment");
		placeTypes.add("finance");
		placeTypes.add("fire_station");
		placeTypes.add("florist");
		placeTypes.add("food");
		placeTypes.add("funeral_home");
		placeTypes.add("furniture_store");
		placeTypes.add("gas_station");
		placeTypes.add("general_contractor");
		placeTypes.add("geocode");
		placeTypes.add("grocery_or_supermarket");
		placeTypes.add("gym");
		placeTypes.add("hair_care");
		placeTypes.add("hardware_store");
		placeTypes.add("health");
		placeTypes.add("hindu_temple");
		placeTypes.add("home_goods_store");
		placeTypes.add("hospital");
		placeTypes.add("insurance_agency");
		placeTypes.add("jewelry_store");
		placeTypes.add("laundry");
		placeTypes.add("lawyer");
		placeTypes.add("library");
		placeTypes.add("liquor_store");
		placeTypes.add("local_government_office");
		placeTypes.add("locksmith");
		placeTypes.add("lodging");
		placeTypes.add("meal_delivery");
		placeTypes.add("meal_takeaway");
		placeTypes.add("mosque");
		placeTypes.add("movie_rental");
		placeTypes.add("movie_theater");
		placeTypes.add("moving_company");
		placeTypes.add("museum");
		placeTypes.add("night_club");
		placeTypes.add("painter");
		placeTypes.add("park");
		placeTypes.add("parking");
		placeTypes.add("pet_store");
		placeTypes.add("pharmacy");
		placeTypes.add("physiotherapist");
		placeTypes.add("place_of_worship");
		placeTypes.add("plumber");
		placeTypes.add("police");
		placeTypes.add("post_office");
		placeTypes.add("real_estate_agency");
		placeTypes.add("restaurant");
		placeTypes.add("roofing_contractor");
		placeTypes.add("rv_park");
		placeTypes.add("school");
		placeTypes.add("shoe_store");
		placeTypes.add("shopping_mall");
		placeTypes.add("spa");
		placeTypes.add("stadium");
		placeTypes.add("storage");
		placeTypes.add("store");
		placeTypes.add("subway_station");
		placeTypes.add("synagogue");
		placeTypes.add("taxi_stand");
		placeTypes.add("train_station");
		placeTypes.add("travel_agency");
		placeTypes.add("university");
		placeTypes.add("veterinary_care");
		placeTypes.add("zoo");
	}
}
