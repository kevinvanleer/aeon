package vanleer.android.aeon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
	//private static final String GOOGLE_REVERSE_GEOCODE_URL = "http://maps.googleapis.com/maps/api/geocode/json?latlng=40.714224,-73.961452&sensor=true_or_false";
	private static final String GOOGLE_REVERSE_GEOCODE_URL = "http://maps.googleapis.com/maps/api/geocode/json";
    private String apiKey = "AIzaSyCXMEFDyFQK2Wu0-w0dyxs-nEO3uZoXUCc";
    private boolean autocomplete = false;	
	private JSONObject places;
    
    GooglePlacesSearch(String userApiKey, String userClientId) {
    	apiKey = userApiKey;
    }
    
	void PerformSearch(double latitude, double longitude,
			double radius, String name, boolean sensor) throws ClientProtocolException, IOException {
		PerformSearch(latitude, longitude, radius, null, name, sensor);
	}
	
	void PerformSearch(double latitude, double longitude,
			double radius, String[] types, boolean sensor) throws ClientProtocolException, IOException {
		PerformSearch(latitude, longitude, radius, types, "", sensor);
	}
	
	void PerformSearch(double latitude, double longitude,
			double radius, String[] types, String name, boolean sensor) throws ClientProtocolException, IOException {
		HttpClient httpclient = new DefaultHttpClient();
		String url = BuildGooglePlacesSearchUrl(latitude, longitude, radius, types, name, sensor);
		HttpResponse response = httpclient.execute(new HttpGet(url));
		StatusLine statusLine = response.getStatusLine();
		if(statusLine.getStatusCode() == HttpStatus.SC_OK) {			 
		    InputStream inStream = response.getEntity().getContent();
		    BufferedReader reader = new BufferedReader(new InputStreamReader(inStream), 8);
		    places = (JSONObject)JSONValue.parse(reader);
		} else {
		    //Closes the connection.
		    response.getEntity().getContent().close();
		    throw new IOException(statusLine.getReasonPhrase());
		}
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
		
	    //FOR DEBUGGING
		//return "https://maps.googleapis.com/maps/api/place/search/json?location=-33.8670522,151.1957362&radius=500&types=food&name=harbour&sensor=false&key=AIzaSyCXMEFDyFQK2Wu0-w0dyxs-nEO3uZoXUCc";	
		return url;
	} 

	public ItineraryItem GetPlace(final int index) {
		ItineraryItem item = null;
		if(places != null) {
			JSONArray resultArray = (JSONArray) places.get("results");
			if(resultArray != null) {
				JSONObject place = (JSONObject) resultArray.get(index);
				if(place != null) {
					//field = (String) place.get(field);
				    item = new ItineraryItem(place);	
				}
			}
		}
		
		return item;
	}

	public int GetResultCount() {
		int resultCount = 0;
		if(places != null) {			
			JSONArray resultArray = (JSONArray) places.get("results");
			if(resultArray != null) {
				resultCount = resultArray.size();
			}
		}
		return resultCount;
	}
	
	static public String ReverseGeocode(final Location location, Boolean sensor) throws ClientProtocolException, IOException {
		//http://maps.googleapis.com/maps/api/geocode/json?latlng=40.714224,-73.961452&sensor=true_or_false
		String bestDescription = "";
		HttpClient httpclient = new DefaultHttpClient();
		String url = (GOOGLE_REVERSE_GEOCODE_URL + "?latlng=" + Double.toString(location.getLatitude()) +
				"," + Double.toString(location.getLongitude()) + "&sensor=" + sensor.toString());
		HttpResponse response = httpclient.execute(new HttpGet(url));
		StatusLine statusLine = response.getStatusLine();
		if(statusLine.getStatusCode() == HttpStatus.SC_OK) {			 
		    InputStream inStream = response.getEntity().getContent();
		    BufferedReader reader = new BufferedReader(new InputStreamReader(inStream), 8);
		    JSONObject placemarks  = (JSONObject)JSONValue.parse(reader);
		    if(placemarks != null) {
		    	JSONArray resultArray = (JSONArray) placemarks.get("results");
		    	if(resultArray != null) {
		    		JSONObject placemark = (JSONObject) resultArray.get(0);
		    		if(placemark != null) {
		    			bestDescription = (String) placemark.get("formatted_address");
		    		}
		    	}
		    }
		} else {
		    //Closes the connection.
		    response.getEntity().getContent().close();
		    throw new IOException(statusLine.getReasonPhrase());
		}
		
		return bestDescription;
	}
}
