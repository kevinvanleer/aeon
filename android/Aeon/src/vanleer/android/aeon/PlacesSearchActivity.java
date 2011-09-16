package vanleer.android.aeon;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public final class PlacesSearchActivity extends Activity implements OnClickListener{
	private ArrayList<ItineraryItem> searchResultsList;
	private ArrayAdapter<ItineraryItem> searchResults;
	private ListView searchResultsListView;
	private int listViewId = R.id.listView_searchResults;
    private String apiKey = "AIzaSyCXMEFDyFQK2Wu0-w0dyxs-nEO3uZoXUCc";
	private Location currentLocation;
	private ImageButton searchButton;
	private GooglePlacesSearch googleSearch;
	private LocationManager locationManager;
	private ProgressDialog waitSpinner = null; 
	private EditText searchText;
	private boolean waitingForGps = false; 
   
	//lat=38.742652
	//long=-90.098394
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_destination);
		googleSearch = new GooglePlacesSearch(apiKey, "");
	    searchButton = (ImageButton) findViewById(R.id.imageButton_search);
	    searchButton.setOnClickListener(this);
	    searchText = (EditText) findViewById(R.id.editText_searchQuery);
	    searchResultsList = new ArrayList<ItineraryItem>();
	    searchResults = new ItineraryItemAdapter(this, R.layout.search_result_item, searchResultsList);
		searchResultsListView = (ListView) findViewById(listViewId);
		searchResultsListView.setAdapter(searchResults);
		// Acquire a reference to the system Location Manager	    
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
	
		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		      // Called when a new location is found by the network location provider.
		      makeUseOfNewLocation(location);
		    }
	
		    public void onStatusChanged(String provider, int status, Bundle extras) {}
	
		    public void onProviderEnabled(String provider) {}
	
		    public void onProviderDisabled(String provider) {}
		  };
	
		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	}

	protected void makeUseOfNewLocation(Location location) {
		currentLocation = location;
		TextView locationText = (TextView) findViewById(R.id.textView_currentLocation);
		//TODO: Convert lat/long to city/state
		//locationText.setText(Double.toString(currentLocation.getLatitude()) +
		//		" ," + Double.toString(currentLocation.getLongitude()));
		try {
			locationText.setText(GooglePlacesSearch.ReverseGeocode(currentLocation, true));
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(waitingForGps) {
			waitingForGps  = false;
			onClick(searchButton);
		}
	}

	public void onClick(View v) {
		if(currentLocation == null) {
			waitSpinner = ProgressDialog.show(PlacesSearchActivity.this,
					"", "waiting for GPS...", true);
			waitingForGps = true;
			new Thread() {
				public void run() {
					while(currentLocation == null) {
						try {
							sleep(1);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					waitSpinner.dismiss();
				}
			}.start();
		} else {
			waitSpinner = ProgressDialog.show(PlacesSearchActivity.this,
					"", "searching...", true);
			new Thread() {
				public void run() {
					//TODO:  Figure out how to signal thread to finish 
					while(googleSearch.GetResultCount() == 0) {
						try {
							sleep(1);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					waitSpinner.dismiss();
				}
			}.start();
			searchResultsList.clear();
			searchResults.clear();
			searchResultsListView.clearChoices();
			try {
				googleSearch.PerformSearch(currentLocation.getLatitude(), currentLocation.getLongitude(),
						10000, searchText.getText().toString(), true);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(int i = 0; i < googleSearch.GetResultCount(); ++i) {
				ItineraryItem newItem = googleSearch.GetPlace(i);
				if(newItem != null) {
					newItem.SetDistance(currentLocation);
					searchResultsList.add(newItem);
					searchResults.add(searchResultsList.get(i));
				}
			}
		}
	}
}
