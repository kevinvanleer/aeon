package vanleer.android.aeon;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public final class PlacesSearchActivity extends Activity implements OnClickListener{
	private ArrayAdapter<String> searchResults;
	private ListView searchResultsListView;
	private int listViewId = R.id.listView_searchResults;
    private String apiKey = "AIzaSyCXMEFDyFQK2Wu0-w0dyxs-nEO3uZoXUCc";
	private Location currentLocation;
	private String query;
	private Button searchButton;
	private GooglePlacesSearch googleSearch;
    
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_destination);
		googleSearch = new GooglePlacesSearch(apiKey, "");
	    searchButton = (Button) findViewById(R.id.imageButton_search);
	    searchButton.setOnClickListener(this);
		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
	
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
		// TODO Auto-generated method stub
		currentLocation = location;
	}

	public void onClick(View v) {
		try {
			EditText searchText = (EditText) findViewById(R.id.editText_searchQuery);
			googleSearch.PerformSearch(currentLocation.getLatitude(), currentLocation.getLongitude(), 10000, searchText.getText().toString(), true);
			searchResults = new ArrayAdapter<String>(this, R.layout.itinerary_item);
			for(int i = 0; i < googleSearch.GetResultCount(); ++i)
			{
				searchResults.add(googleSearch.GetPlaceField(i, "name"));
			}
			searchResultsListView = (ListView) findViewById(listViewId);
			searchResultsListView.setAdapter(searchResults);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
