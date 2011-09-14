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

public final class GooglePlacesSearch {
	private static final String GOOGLE_PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json"; 
	private static final String GOOGLE_PLACES_AUTOCOMPLETE_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json"; 
    private String apiKey = "AIzaSyCXMEFDyFQK2Wu0-w0dyxs-nEO3uZoXUCc";
    private String clientId = "";
	private boolean autocomplete = false;	
	private JSONObject places;
    
    GooglePlacesSearch(String userApiKey, String userClientId) {
    	apiKey = userApiKey;
    	clientId = userClientId;
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
   	       url += "&types=";
   	       for(int i = 0; i < types.length; ++i) {
   	    	   if(i > 0) {
   	    		   url += "|";
   	    	   }
   	    		   
   	    	   url += types[i];
   	       }   	       
   	   	}
	    
	    if(name != "") {
	    	if(autocomplete) {
	    		url += "&name=" + name;
	    	} else {
	    		url += "&input=" + name;
	    	}
	    }
	    
	    url += "&sensor=" + sensor;
	    url += "&key=" + apiKey;
		
	    //FOR DEBUGGING
		//return "https://maps.googleapis.com/maps/api/place/search/json?location=-33.8670522,151.1957362&radius=500&types=food&name=harbour&sensor=false&key=AIzaSyCXMEFDyFQK2Wu0-w0dyxs-nEO3uZoXUCc";	
		return url;
	} 
	
	public String GetPlaceField(final int index, final String field) {
		JSONArray resultArray = (JSONArray) places.get("results");
		JSONObject place = (JSONObject) resultArray.get(index);
		return (String) place.get(field);
	}

	public int GetResultCount() {
		JSONArray resultArray = (JSONArray) places.get("results");
		return resultArray.size();
	}
}
