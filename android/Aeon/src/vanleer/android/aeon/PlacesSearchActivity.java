package vanleer.android.aeon;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
	private TextView locationText;
	private ImageView locationSensorImage;
	private GooglePlacesSearch googleSearch;
	private LocationManager locationManager;
	private ProgressDialog waitSpinner = null; 
	private EditText searchText;
	private boolean waitingForGps = false; 
	private boolean searching = false;

	//lat=38.74419380
	//lng=-90.09839319999999

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_destination);
		locationSensorImage = (ImageView) findViewById(R.id.imageView_currentLocation);
		locationSensorImage.setVisibility(View.INVISIBLE);
		googleSearch = new GooglePlacesSearch(apiKey, "");
		locationText = (TextView) findViewById(R.id.textView_currentLocation);
		locationText.setText("Waiting for location...");
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
		//TODO: fix bug preventing display of current location if discovered after query started
		locationSensorImage.setVisibility(View.VISIBLE);
		//make the image view square
		MakeImageViewSquare(locationSensorImage);
		locationText.setText(GooglePlacesSearch.GetGeodeticString(currentLocation));
		new Thread() {
			public void run() {
				Message msg = updateCurrentLocationTextHandler.obtainMessage();
				msg.obj = googleSearch.ReverseGeocode(currentLocation, true);
				updateCurrentLocationTextHandler.sendMessage(msg);
			}
		}.start();
		if(waitingForGps) {
			waitingForGps  = false;
			onClick(searchButton);
		}
	}

	final Handler updateCurrentLocationTextHandler = new Handler() {
		public void handleMessage(Message msg) {
			locationText.setText((String) msg.obj);
		}
	};

	private void WaitForGps() {
		waitSpinner = ProgressDialog.show(PlacesSearchActivity.this,
				"", "waiting for location...", true);
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
	}

	private void WaitForSearchResults() {
		waitSpinner = ProgressDialog.show(PlacesSearchActivity.this,
				"", "searching...", true);
		searching = true;
		new Thread() {
			public void run() {
				while(searching) {
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
	}

	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.imageButton_search:
			GetSearchResults();
			break;
		}
	}

	private void GetSearchResults() {	
		if(currentLocation == null) {
			WaitForGps();
		} else {
			WaitForSearchResults();

			searchResultsList.clear();
			searchResults.clear();
			searchResultsListView.clearChoices();
			QuerySearchEngine();		
			
			BuildResultsList();
		}
	}

	private void QuerySearchEngine() {
		googleSearch.PerformSearch(currentLocation.getLatitude(), currentLocation.getLongitude(),
				10000, searchText.getText().toString(), true);
		searching = false;		
	}

	private void BuildResultsList() {
		for(int i = 0; i < googleSearch.GetResultCount(); ++i) {
			ItineraryItem newItem = googleSearch.GetPlace(i);
			if(newItem != null) {
				newItem.SetDistance(currentLocation);
				searchResultsList.add(newItem);
				searchResults.add(searchResultsList.get(i));
			}
		}
	}

	private static void MakeImageViewSquare(ImageView image) {
		LayoutParams params = image.getLayoutParams();
		params.width = image.getHeight();
		image.setLayoutParams(params);
	}
}
