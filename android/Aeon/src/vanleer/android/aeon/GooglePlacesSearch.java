package vanleer.android.aeon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

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
	private static final String GOOGLE_REVERSE_GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json";
	private static final String GOOGLE_DISTANCE_MATRIX_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";
    private String apiKey = "AIzaSyCXMEFDyFQK2Wu0-w0dyxs-nEO3uZoXUCc";
    private boolean autocomplete = false;
	private JSONObject searchResults;
	private ArrayList<ItineraryItem> places;
    
    GooglePlacesSearch(String userApiKey, String userClientId) {
    	apiKey = userApiKey;
    	places = new ArrayList<ItineraryItem>();
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
		HttpClient httpClient = new DefaultHttpClient();
		String url = BuildGooglePlacesSearchUrl(latitude, longitude, radius, types, name, sensor);
		HttpResponse response = httpClient.execute(new HttpGet(url));
		StatusLine statusLine = response.getStatusLine();
		if(statusLine.getStatusCode() == HttpStatus.SC_OK) {			 
		    InputStream inStream = response.getEntity().getContent();
		    BufferedReader reader = new BufferedReader(new InputStreamReader(inStream), 8);
		    searchResults = (JSONObject)JSONValue.parse(reader);
		    GetDistances(httpClient, latitude, longitude, sensor);
		} else {
		    //Closes the connection.
		    response.getEntity().getContent().close();
		    throw new IOException(statusLine.getReasonPhrase());
		}
	}
	//https://maps.googleapis.com/maps/api/distancematrix/json?origins=38.74265166666667,-90.09839333333333&destinations=38.723069,-90.121541|38.678696,-90.011518|38.657926,-89.985827|38.750176,-90.071867|38.700788,-90.145431|38.787618,-89.980377|38.690977,-89.975388|38.772802,-90.22048|38.793072,-89.95087|38.796272,-90.231933|38.636884,-90.194924|38.67605,-90.251084|38.616008,-90.128229|38.639316,-90.243432|38.73557,-90.278284|38.889201,-90.174288|38.612563,-90.221894|38.702208,-90.291375|38.810838,-90.296906|38.732243,-89.905683&sensor=true
	private void GetDistances(HttpClient httpClient, double latitude, double longitude, boolean sensor) {
		String url = BuildDistanceMatrixUrl(latitude, longitude, sensor);		
		try {
			HttpResponse response = httpClient.execute(new HttpGet(url));
			StatusLine statusLine = response.getStatusLine();
			if(statusLine.getStatusCode() == HttpStatus.SC_OK) {			 
			    InputStream inStream = response.getEntity().getContent();
			    BufferedReader reader = new BufferedReader(new InputStreamReader(inStream), 8);
			    JSONObject distanceMatrix  = (JSONObject)JSONValue.parse(reader);
			    if(distanceMatrix != null) {
			    	ParseDistanceMatrixResults(distanceMatrix);
			    }
			    
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void ParseDistanceMatrixResults(JSONObject distanceMatrix) {
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

	private String BuildDistanceMatrixUrl(double latitude, double longitude, boolean sensor) {
		String destinations = "";
		String url = GOOGLE_DISTANCE_MATRIX_URL;
		url += "?origins=" + latitude + "," + longitude;
		if(searchResults != null) {
			JSONArray resultArray = (JSONArray) searchResults.get("results");
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
		/*ItineraryItem item = null;
		if(searchResults != null) {
			JSONArray resultArray = (JSONArray) searchResults.get("results");
			if(resultArray != null) {
				JSONObject place = (JSONObject) resultArray.get(index);
				if(place != null) {
				    item = new ItineraryItem(place);	
				}
			}
		}
		
		return item;*/
		return places.get(index);
	}

	public int GetResultCount() {
		int resultCount = 0;
		if(searchResults != null) {			
			JSONArray resultArray = (JSONArray) searchResults.get("results");
			if(resultArray != null) {
				resultCount = resultArray.size();
			}
		}
		return resultCount;
	}
	
	static public String ReverseGeocode(final Location location, Boolean sensor) throws ClientProtocolException, IOException {
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
