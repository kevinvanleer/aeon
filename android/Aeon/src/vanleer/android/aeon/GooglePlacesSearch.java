package vanleer.android.aeon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

import android.location.Location;
import android.net.Uri;

public final class GooglePlacesSearch {
	private static final String GOOGLE_PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json"; 
	private static final String GOOGLE_PLACES_AUTOCOMPLETE_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json"; 
	private static final String GOOGLE_GEOCODING_URL = "https://maps.googleapis.com/maps/api/geocode/json";
	private static final String GOOGLE_DISTANCE_MATRIX_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";
	private String apiKey;
	private HttpClient httpClient;
	private boolean autocomplete = false;
	private JSONObject placesSearchResults;
	private JSONObject geocodingSearchResults;
	private ArrayList<ItineraryItem> places;

	GooglePlacesSearch(String userApiKey, String userClientId) {
		apiKey = userApiKey;
		places = new ArrayList<ItineraryItem>();
		httpClient = new DefaultHttpClient();
	}

	void PerformSearch(double latitude, double longitude,
			double radius, String name, boolean sensor) {
		PerformSearch(latitude, longitude, radius, null, name, sensor);
	}

	void PerformSearch(double latitude, double longitude,
			double radius, String[] types, boolean sensor) {
		PerformSearch(latitude, longitude, radius, types, "", sensor);
	}

	void PerformSearch(double latitude, double longitude,
			double radius, String[] types, String name, boolean sensor) {
		PerformPlacesSearch(latitude, longitude, radius, types, name, sensor);
		PerformGeocodingSearch(name, sensor);
		if(GetPlacesSearchResultCount() > 0)
		{
			JSONObject distanceMatrixResults = GetDistances(httpClient, latitude, longitude, sensor);
			ParseDistanceMatrixResults(distanceMatrixResults);
		}
	}

	private void PerformGeocodingSearch(String address, boolean sensor) {
		// TODO Auto-generated method stub
		String url = GOOGLE_GEOCODING_URL + "?address=" + Uri.encode(address) + "&sensor=" + sensor;
		geocodingSearchResults = PerformHttpGet(url);
	}

	private JSONObject PerformHttpGet(String url) {
		JSONObject jsonResponse = null;
		try {
			HttpResponse response = httpClient.execute(new HttpGet(url));
			StatusLine statusLine = response.getStatusLine();
			if(statusLine.getStatusCode() == HttpStatus.SC_OK) {
				InputStream inStream = response.getEntity().getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inStream), 8);
				jsonResponse = (JSONObject)JSONValue.parse(reader);
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

	void PerformPlacesSearch(double latitude, double longitude,
			double radius, String[] types, String name, boolean sensor) {
		String url = BuildGooglePlacesSearchUrl(latitude, longitude, radius, types, name, sensor);
		placesSearchResults = PerformHttpGet(url);
	}

	private JSONObject GetDistances(HttpClient httpClient, double latitude, double longitude, boolean sensor) {
		String url = BuildDistanceMatrixUrl(latitude, longitude, sensor);
		return PerformHttpGet(url);
	}

	private void ParseDistanceMatrixResults(JSONObject distanceMatrix) {
		if(distanceMatrix != null) {
			JSONArray results = (JSONArray) distanceMatrix.get("rows");
			JSONArray resultArray = (JSONArray) ((JSONObject) results.get(0)).get("elements");
			if(resultArray != null) {
				for(int index = 0; index < places.size(); ++index) {
					JSONObject distance = (JSONObject) resultArray.get(index);
					if(distance != null) {
						places.get(index).SetDistance(distance);
					}
				}
			}
		}
	}

	private String BuildDistanceMatrixUrl(double latitude, double longitude, boolean sensor) {
		String destinations = "";
		String url = GOOGLE_DISTANCE_MATRIX_URL;
		url += "?origins=" + latitude + "," + longitude;
		if(placesSearchResults != null) {
			JSONArray resultArray = (JSONArray) placesSearchResults.get("results");
			if(resultArray != null) {
				url += "&destinations=";
				for(int index = 0; index < resultArray.size(); ++index) {
					JSONObject place = (JSONObject) resultArray.get(index);
					if(place != null) {
						places.add(new ItineraryItem(place));
						destinations += places.get(index).GetLocation().getLatitude() + ",";
						destinations += places.get(index).GetLocation().getLongitude() + "|";
					}
				}
				destinations = destinations.substring(0, destinations.length() - 1);
			}
		}
		url += Uri.encode(destinations);
		url += "&sensor=" + sensor;
		return url;
	}

	private String BuildGooglePlacesSearchUrl(double latitude,
			double longitude, double radius, String[] types, String name,
			boolean sensor) {

		String url;
		if(autocomplete) {			
			url = GOOGLE_PLACES_AUTOCOMPLETE_URL;
		} else {
			url = GOOGLE_PLACES_SEARCH_URL;
		}

		url += "?location=" + latitude + "," + longitude;
		url += "&radius=" + radius;

		if(types != null) {	    	
			String typesString = "";
			url += "&types=";
			for(int i = 0; i < types.length; ++i) {
				if(i > 0) {
					typesString += "|";
				}

				typesString += types[i];
			}

			url += Uri.encode(typesString);
		}	    

		if(name != "") {
			if(autocomplete) {
				url += "&input=" + Uri.encode(name);
			} else {
				url += "&name=" + Uri.encode(name);
			}
		}

		url += "&sensor=" + sensor;
		url += "&key=" + apiKey;

		return url;
	} 

	public ItineraryItem GetPlace(final int index) {
		return places.get(index);
	}

	public int GetResultCount() {
		return places.size();
	}

	public int GetGeocodingSearchResultCount() {
		int resultCount = 0;
		if(geocodingSearchResults != null) {
			JSONArray resultArray = (JSONArray) geocodingSearchResults.get("results");
			if(resultArray != null) {
				resultCount = resultArray.size();
			}
		}
		return resultCount;
	}

	public int GetPlacesSearchResultCount() {
		int resultCount = 0;
		if(placesSearchResults != null) {
			JSONArray resultArray = (JSONArray) placesSearchResults.get("results");
			if(resultArray != null) {
				resultCount = resultArray.size();
			}
		}
		return resultCount;
	}

	public String ReverseGeocode(final Location location, Boolean sensor) {
		String bestDescription = "";
		String url = (GOOGLE_GEOCODING_URL + "?latlng=" + Double.toString(location.getLatitude()) +
				"," + Double.toString(location.getLongitude()) + "&sensor=" + sensor.toString());
		JSONObject placemarks  = PerformHttpGet(url);
		if(placemarks != null) {
			JSONArray resultArray = (JSONArray) placemarks.get("results");
			if(resultArray != null) {
				JSONObject placemark = (JSONObject) resultArray.get(0);
				if(placemark != null) {
					bestDescription = (String) placemark.get("formatted_address");
				}
			}
		}

		return bestDescription;
	}

	static String GetGeodeticString(Location location) {
		String latSuffix = "° N";
		String lngSuffix = "° E";
		if(location.getLatitude() < 0) {
			latSuffix = "° S";	
		}
		if(location.getLongitude() < 0) {
			lngSuffix = "° W";	
		}

		String latString = String.format("%1$.4f", Math.abs(location.getLatitude()));
		String lngString = String.format("%1$.4f", Math.abs(location.getLongitude()));

		return (latString + latSuffix + ", " + lngString + lngSuffix);
	}
}
