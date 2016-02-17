package vanleer.android.aeon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.lang.Math;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import vanleer.android.util.InvalidDistanceMatrixResponseException;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

public final class GooglePlacesSearch {
	private static final String GOOGLE_PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
	private static final String GOOGLE_PLACES_AUTOCOMPLETE_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json";
	private static final String GOOGLE_DISTANCE_MATRIX_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";
	private String apiKey = null;
	private final ArrayList<ItineraryItem> places = new ArrayList<ItineraryItem>();
	private final Geocoder externalGeocoder;
	private static final ArrayList<String> placeTypes = new ArrayList<String>();

	GooglePlacesSearch(Geocoder geocoder, String userApiKey, String userClientId) {
		apiKey = userApiKey;
		externalGeocoder = geocoder;
		if (placeTypes.isEmpty()) {
			initializePlaceTypes();
		}
	}

	void performSearch(double latitude, double longitude, String keyword) {
		performSearch(latitude, longitude, null, null, keyword);
	}

	void performSearch(double latitude, double longitude, Double radius, String name) {
		String type = findType(name);
		if (type != null) {
			String[] types = new String[1];
			types[0] = type;
			performSearch(latitude, longitude, radius, types);
		} else {
			performSearch(latitude, longitude, radius, null, name);
		}
	}

	private String findType(String name) {
		String inferredType = null;

		name = name.toLowerCase(Locale.US);
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

	void performSearch(double latitude, double longitude, Double radius, String[] types) {
		performSearch(latitude, longitude, radius, types, "");
	}

	void performSearch(double latitude, double longitude, Double radius, String[] types, String name) {
		clearSearchResults();

		performPlacesSearch(latitude, longitude, radius, types, name);
		if (places.isEmpty()) {
			performGeocodingSearch(name);
		}

		if (!places.isEmpty()) {
			JSONObject distanceMatrixResults = getDistances(latitude, longitude);
			parseDistanceMatrixResults(distanceMatrixResults);
		}
	}

	private void clearSearchResults() {
		if (places != null) {
			places.clear();
		}
	}

	// PLACES
	public void performPlacesSearch(double latitude, double longitude, Double radius, String[] types, String name) {
		try {
			String url = buildGooglePlacesSearchUrl(latitude, longitude, radius, types, name);
			JSONObject placesSearchResults = performHttpGet(url);
			addPlacesResults(placesSearchResults);
		} catch (IllegalArgumentException e) {
			Log.d("Aeon", "Places search failed with keyword:  \"" + name + "\"");
		}
	}

	private String buildGooglePlacesSearchUrl(double latitude, double longitude, Double radius, String[] types, String keyword) {

		String url = GOOGLE_PLACES_SEARCH_URL;

		url += "?location=" + latitude + "," + longitude;
		if (radius != null) {
			url += "&radius=" + radius;
		} else {
			url += "&rankby=distance";
			if ((keyword == null || keyword.isEmpty()) && (types == null || types.length == 0)) {
				throw new IllegalArgumentException("A keywork, name or type must be provided if a search radius is not.");
			}
		}

		if (types != null) {
			url += "&types=" + getTypesUrlPart(types);
		}

		if (keyword != "") {
			url += "&keyword=" + Uri.encode(keyword);
		}

		url += "&key=" + apiKey;

		return url;
	}

	private void addPlacesResults(JSONObject placesSearchResults) {
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

	// PLACES

	// AUTOCOMPLETE
	public ArrayList<String> performPlacesAutocomplete(String input, Double latitude, Double longitude, Double radius, String[] types, Long offset) {
		String url = buildGooglePlacesAutocompleteUrl(input, latitude, longitude, radius, types, offset);

		JSONObject autocompleteResults = performHttpGet(url);
		return getAutocompleteResultsList(autocompleteResults);
	}

	private String buildGooglePlacesAutocompleteUrl(String input, Double latitude, Double longitude, Double radius, String[] types, Long offset) {
		String url = GOOGLE_PLACES_AUTOCOMPLETE_URL;

		if (input == null || input == "") {
			throw new IllegalArgumentException("Places autocomplete search requires an input string");
		}

		url += "?input=" + Uri.encode(input);
		/*-url += "&types=establishment";
		if (types != null) {
			url += "|" + getTypesUrlPart(types);
		}*/

		if ((latitude != null) && (longitude != null)) {
			url += "&location=" + latitude + "," + longitude;
		}

		if (radius != null) {
			url += "&radius=" + radius;
		}

		if ((offset != 0) && (offset != null)) {
			url += "&offset=" + offset;
		}

		url += "&key=" + apiKey;

		return url;
	}

	private ArrayList<String> getAutocompleteResultsList(JSONObject autocompleteResults) {
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

	// AUTOCOMPLETE

	// GEOCODING
	private void performGeocodingSearch(String address) {
		List<Address> geocodingSearchResults = null;
		try {
			geocodingSearchResults = externalGeocoder.getFromLocationName(address, 10);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		addGeocodingResults(geocodingSearchResults);
	}

	private void addGeocodingResults(List<Address> geocodingSearchResults) {
		if (geocodingSearchResults != null) {
			if (!geocodingSearchResults.isEmpty()) {
				for (int index = 0; index < geocodingSearchResults.size(); ++index) {
					Address place = geocodingSearchResults.get(index);
					places.add(new ItineraryItem(place));
				}
			}
		}
	}

	public String getReverseGeocodeDescription(final Location location) {
		String bestDescription = "Address unknown";

		Address placemark = getBestReverseGeocodeResult(location);
		if (placemark != null) {
			// bestDescription = (String) placemark.get("formatted_address");
			if (placemark.getMaxAddressLineIndex() >= 0) {
				bestDescription = placemark.getAddressLine(0) + ", ";
			}
			bestDescription += placemark.getLocality();
		}

		return bestDescription;
	}

	public Address getBestReverseGeocodeResult(final Location location) {
		Address placemark = null;
		List<Address> addresses = getReverseGeocodeResults(location);
		if (addresses != null) {
			if (addresses.size() > 0) {
				placemark = getReverseGeocodeResults(location).get(0);
			}
		}

		return placemark;
	}

	public List<Address> getReverseGeocodeResults(final Location location) {
		List<Address> addressList = null;
		try {
			addressList = externalGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return addressList;
	}

	static String getGeodeticString(Location location) {
		String latSuffix = "° N";
		String lngSuffix = "° E";
		if (location.getLatitude() < 0) {
			latSuffix = "° S";
		}
		if (location.getLongitude() < 0) {
			lngSuffix = "° W";
		}

		String latString = String.format(Locale.US, "%1$.4f", Math.abs(location.getLatitude()));
		String lngString = String.format(Locale.US, "%1$.4f", Math.abs(location.getLongitude()));

		return (latString + latSuffix + ", " + lngString + lngSuffix);
	}

	// GEOCODING

	// DISTANCE MATRIX
	private JSONObject getDistances(double latitude, double longitude) {
		String url = buildDistanceMatrixUrl(latitude, longitude);
		return performHttpGet(url);
	}

	private String buildDistanceMatrixUrl(double latitude, double longitude) {
		String url = GOOGLE_DISTANCE_MATRIX_URL;
		url += "?origins=" + latitude + "," + longitude;
		String destinations = getDestinationsUrlPart();
		url += "&destinations=" + Uri.encode(destinations);
		return url;
	}

	private String getDestinationsUrlPart() {
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

	private void parseDistanceMatrixResults(JSONObject distanceMatrix) {
		if (distanceMatrix == null) {
			String message = "Uninitialized distance matrix result.";
			Log.w("Aeon", message);
			throw new InvalidDistanceMatrixResponseException(message);
		}

		String statusCode = (String) distanceMatrix.get("status");
		if (!statusCode.equals("OK")) {
			String message = "Distance matrix search returned bad status.  STATUS_CODE=" + statusCode;
			Log.w("Aeon", message);
			throw new InvalidDistanceMatrixResponseException(message);
		}

		JSONArray results = (JSONArray) distanceMatrix.get("rows");
		if ((results == null) || results.isEmpty()) {
			String message = "Distance matrix search returned no results.";
			Log.w("Aeon", message);
			throw new InvalidDistanceMatrixResponseException(message);
		}

		JSONArray resultArray = (JSONArray) ((JSONObject) results.get(0)).get("elements");
		if ((resultArray == null) || (resultArray.size() != places.size())) {
			String message = "Distance matrix search result count does not match places result count.";
			Log.w("Aeon", message);
			throw new InvalidDistanceMatrixResponseException(message);
		}

		if (resultArray != null) {
			for (int index = places.size() - 1; index >= 0; --index) {
				JSONObject distance = (JSONObject) resultArray.get(index);
				try {
					places.get(index).setDistance(distance);
				} catch (InvalidDistanceMatrixResponseException e) {
					places.remove(index);
				}
			}
		}
	}

	// DISTANCE MATRIX

	public ItineraryItem getPlace(final int index) {
		return places.get(index);
	}

	public int getResultCount() {
		return places.size();
	}

	private String getTypesUrlPart(String[] types) {
		String typesUrlPart = "";

		for (int i = 0; i < types.length; ++i) {
			if (i > 0) {
				typesUrlPart += "|";
			}

			typesUrlPart += types[i];
		}

		return Uri.encode(typesUrlPart);
	}

	private void initializePlaceTypes() {
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

	private JSONObject performHttpGet(final String url) {
		AsyncTask<String, Void, JSONObject> get = new AsyncTask<String, Void, JSONObject>() {
			private final Semaphore available = new Semaphore(1, true);

			@Override
			protected JSONObject doInBackground(String... arg0) {
				JSONObject jsonResponse = null;
				try {
					available.acquire();
					AndroidHttpClient httpClient = AndroidHttpClient.newInstance("aeon");
					HttpResponse response = httpClient.execute(new HttpGet(url));
					StatusLine statusLine = response.getStatusLine();
					if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
						InputStream inStream = response.getEntity().getContent();
						BufferedReader reader = new BufferedReader(new InputStreamReader(inStream), 8);
						jsonResponse = (JSONObject) JSONValue.parse(reader);
					}
					response.getEntity().consumeContent();
					httpClient.close();
					available.release();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
			Log.e("Aeon", e.getMessage(), e);
		}

		return jsonResponse;
	}
}
