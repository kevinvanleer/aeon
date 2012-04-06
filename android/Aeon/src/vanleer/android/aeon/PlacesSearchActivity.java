package vanleer.android.aeon;

import java.util.ArrayList;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public final class PlacesSearchActivity extends Activity implements OnClickListener, Runnable {
	private ArrayList<ItineraryItem> searchResultsList;
	private SearchResultItemAdapter searchResults;
	private ListView searchResultsListView;
	private int listViewId = R.id.listView_searchResults;
	private String apiKey = "AIzaSyCXMEFDyFQK2Wu0-w0dyxs-nEO3uZoXUCc";
	private Location currentLocation = null;
	private ImageButton searchButton;
	private TextView locationText;
	private ImageView locationSensorImage;
	private GooglePlacesSearch googleSearch;
	private LocationManager locationManager;
	private ProgressDialog waitSpinner = null; 
	private EditText searchText;
	private boolean waitingForGps = false; 
	private boolean searching = false;
	private Long searchRadius;
	static Context thisContext;

	//4812 Danielle CT Granite City IL 62040
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
		searchResults = new SearchResultItemAdapter(this, R.layout.search_result_item, searchResultsList);
		searchResultsListView = (ListView) findViewById(listViewId);
		searchResultsListView.setAdapter(searchResults);
		// Acquire a reference to the system Location Manager	    
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		thisContext = this.getApplicationContext();

		ConfigureSearchResultsListViewLongClickListener();

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

		if(getIntent() != null && getIntent().getExtras() != null) {
			currentLocation = getIntent().getExtras().getParcelable("location");
		}

		if(currentLocation == null) {
			// Register the listener with the Location Manager to receive location updates
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
			//TODO: locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, some looper thing);
		} else {
			locationText.setText(GooglePlacesSearch.GetGeodeticString(currentLocation));
			if(currentLocation.getProvider().equals(LocationManager.GPS_PROVIDER))
			{
				locationSensorImage.setVisibility(View.VISIBLE);
			}
			locationText.setText(googleSearch.ReverseGeocode(currentLocation, true));
		}		
	}
	
	@Override	
	protected void onDestroy() {
		super.onDestroy();
		try {
			waitSpinner.dismiss();
			waitSpinner = null;
		}
		catch(Exception e) {
		
		}
	}

	private void ConfigureSearchResultsListViewLongClickListener() {
		searchResultsListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				Intent resultIntent = new Intent();
				resultIntent.putExtra("itineraryItem", searchResultsList.get(position));
				setResult(Activity.RESULT_OK, resultIntent);
				finish();
				return true;
			}
		});
	}

	protected void makeUseOfNewLocation(Location location) {
		currentLocation = location;
		locationSensorImage.setVisibility(View.VISIBLE);
		MakeImageViewSquare(locationSensorImage);
		locationText.setText(GooglePlacesSearch.GetGeodeticString(currentLocation));
		new Thread() {
			public void run() {
				Message msg = updateCurrentLocationTextHandler.obtainMessage();
				msg.obj = googleSearch.ReverseGeocode(currentLocation, true);
				updateCurrentLocationTextHandler.sendMessage(msg);
			}
		}.start();

		synchronized(this) {
			if(waitingForGps) {
				waitingForGps  = false;
				this.notify();
			}
		}
		onClick(searchButton);
	}

	final Handler updateCurrentLocationTextHandler = new Handler() {
		public void handleMessage(Message msg) {
			locationText.setText((String) msg.obj);
		}
	};

	private void WaitForGps() {
		synchronized(this) {
			waitSpinner = ProgressDialog.show(this,	"", "waiting for location...", true);
			waitingForGps = true;
		}
		new Thread(this).start();
	}

	private void WaitForSearchResults() {
		synchronized(this) {
			waitSpinner = ProgressDialog.show(this,	"", "searching...", true);
			searching = true;
		}
		new Thread(this).start();
	}

	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.imageButton_search:
			GetSearchResults();
			break;
		default:
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
		searchRadius = (long) 5000000;
		googleSearch.PerformSearch(currentLocation.getLatitude(), currentLocation.getLongitude(),
				searchRadius, searchText.getText().toString(), true);
		synchronized(this) {
			searching = false;
			this.notify();
		}
	}

	private void BuildResultsList() {
		for(int i = 0; i < googleSearch.GetResultCount(); ++i) {
			ItineraryItem newItem = googleSearch.GetPlace(i);
			if(newItem != null) {
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

	public void run() {
		synchronized(this) {
			while(searching || waitingForGps) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			waitSpinner.dismiss();
		}
	}
}
