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
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

public class GoogleDirectionsGiver extends AsyncTask<String, Void, DirectionsResult> {
	private static final String GOOGLE_DIRECTIONS_URL = "https://maps.googleapis.com/maps/api/directions/json";
	private static final String apiKey = "AIzaSyCXMEFDyFQK2Wu0-w0dyxs-nEO3uZoXUCc";

	GoogleDirectionsGiver(Location origin, Location destination) {
		execute(buildGoogleDirectionsUrl(origin, destination));
	}

	GoogleDirectionsGiver(Location origin, Location destination, ArrayList<Location> waypoints) {
		execute(buildGoogleDirectionsUrl(origin, destination, waypoints));
	}

	GoogleDirectionsGiver(Location origin, Location destination, ArrayList<Location> waypoints, String mode) {
		execute(buildGoogleDirectionsUrl(origin, destination, waypoints, mode));
	}

	@Override
	protected DirectionsResult doInBackground(String... urls) {
		DirectionsResult result = new DirectionsResult(performHttpGet(urls[0]));
		if (result == null) {
			result = new DirectionsResult(performHttpGet(urls[0]));
		}
		return result;
	}

	private String buildGoogleDirectionsUrl(Location origin, Location destination) {
		return buildGoogleDirectionsUrl(origin, destination, null, "driving");
	}

	private String buildGoogleDirectionsUrl(Location origin, Location destination, ArrayList<Location> waypoints) {
		return buildGoogleDirectionsUrl(origin, destination, waypoints, "driving");
	}

	private String buildGoogleDirectionsUrl(Location origin, Location destination, ArrayList<Location> waypoints, String mode) {
		String url = GOOGLE_DIRECTIONS_URL;

		if (origin == null || destination == null) {
			throw new IllegalArgumentException("Directions API requires an origin and destination.");
		}

		url += "?origin=" + origin.getLatitude() + "," + origin.getLongitude();
		url += "&destination=" + destination.getLatitude() + "," + destination.getLongitude();

		if (waypoints != null) {
			url += "&waypoints=";
			for (Location waypoint : waypoints) {
				url += waypoint.getLatitude() + "," + waypoint.getLongitude() + "|";
			}
			url.substring(0, (url.length() - 1));
		}

		if (mode != null) url += "&mode=" + mode;
		url += "&key=" + apiKey;

		return url;
	}

	/*
	 * public DirectionsResult performDirectionsQuery(Location origin, Location destination) { return performDirectionsQuery(origin, destination, null); } public DirectionsResult performDirectionsQuery(Location origin, Location destination, ArrayList<Location> waypoints) { return performDirectionsQuery(origin, destination, waypoints, "driving"); } public DirectionsResult performDirectionsQuery(Location origin, Location destination, ArrayList<Location> waypoints, String mode) { String url = buildGoogleDirectionsUrl(origin, destination, waypoints, mode); return new DirectionsResult(performHttpGet(url)); }
	 */

	private JSONObject performHttpGet(final String url) {
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
			String errorMessage = "Protocol error retrieving directions for " + url;
			Log.e("Aeon", errorMessage);
			e.printStackTrace();
		} catch (IOException e) {
			String errorMessage = "IO error retrieving directions for " + url;
			Log.e("Aeon", errorMessage);
			e.printStackTrace();
		}
		return jsonResponse;
	}
}
