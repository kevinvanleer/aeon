package vanleer.android.aeon;

import vanleer.util.UnfilteredArrayAdapter;

import java.util.ArrayList;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public final class PlacesSearchActivity extends Activity implements OnClickListener {
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
	private AutoCompleteTextView searchText;
	private boolean waitingForGps = false; 
	private boolean searching = false;
	private Long searchRadius = (long) 5000000;
	private UnfilteredArrayAdapter<String> suggestionList;

	//4812 Danielle CT Granite City IL 62040 38.74419380,-90.09839319999999
	//lat=38.74419380
	//lng=-90.09839319999999

	//283 STONEHENGE DR WASHINGTON, MO 63090-4312
	//38.477548,-91.051562
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_destination);

		InitializeMembers();
		ConfigureSearchResultsListViewLongClickListener();
		ConfigureLocationManager();
		ConfigureTextWatcher();
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

	private void InitializeMembers() {
		locationSensorImage = (ImageView) findViewById(R.id.imageView_currentLocation);
		locationSensorImage.setVisibility(View.INVISIBLE);
		googleSearch = new GooglePlacesSearch(apiKey, "");
		locationText = (TextView) findViewById(R.id.textView_currentLocation);
		locationText.setText("Waiting for location...");
		searchButton = (ImageButton) findViewById(R.id.imageButton_search);
		searchButton.setOnClickListener(this);
		suggestionList = new UnfilteredArrayAdapter<String>(
				this, android.R.layout.simple_dropdown_item_1line);
		searchText = (AutoCompleteTextView) findViewById(R.id.editText_searchQuery);
		searchText.setAdapter(suggestionList);
		searchResultsList = new ArrayList<ItineraryItem>();
		searchResults = new SearchResultItemAdapter(
				this, R.layout.search_result_item, searchResultsList);
		searchResultsListView = (ListView) findViewById(listViewId);
		searchResultsListView.setAdapter(searchResults);
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
	}


	private void ConfigureLocationManager() {		
		LocationListener locationListener = CreateLocationListener();

		if(getIntent() != null && getIntent().getExtras() != null) {
			currentLocation = getIntent().getExtras().getParcelable("location");
		}

		if(currentLocation == null) {
			// Register the listener with the Location Manager to receive location updates
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0, locationListener);
			//TODO: locationManager.requestSingleUpdate(
			//		LocationManager.GPS_PROVIDER, locationListener, some looper thing);
		} else {
			locationText.setText(GooglePlacesSearch.GetGeodeticString(currentLocation));
			if(currentLocation.getProvider().equals(LocationManager.GPS_PROVIDER))
			{
				locationSensorImage.setVisibility(View.VISIBLE);
			}
			locationText.setText(googleSearch.ReverseGeocode(currentLocation, true));
		}
	}

	private LocationListener CreateLocationListener() {
		return new LocationListener() {
			public void onLocationChanged(Location location) {
				makeUseOfNewLocation(location);
			}
			public void onStatusChanged(String provider, int status, Bundle extras) {}
			public void onProviderEnabled(String provider) {}
			public void onProviderDisabled(String provider) {}
		};
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

	private void ConfigureTextWatcher() {
		searchText.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(final CharSequence s, int start, int before, int count) {
				if(s.length() > 1) {
					new AsyncTask<CharSequence, Void, ArrayList<String>>() {
						@Override
						protected ArrayList<String> doInBackground(CharSequence... arg0) {
							return performAutocompleteSearch(s);
						}

						@Override
						protected void onPostExecute(ArrayList<String> suggestions) {
							updateAutocompleteChoices(suggestions);
						}
					}.execute(s);
				}
			}
			
			public void afterTextChanged(Editable s) {}
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
		});
	}

	private ArrayList<String> performAutocompleteSearch(CharSequence s) {
		String input = s.toString();
		ArrayList<String> results = null;
		if(input != "") {
			Double latitude = null;
			Double longitude = null;
			Double radius = null;

			if(currentLocation != null) {
				latitude = currentLocation.getLatitude();
				longitude = currentLocation.getLongitude();
				radius = (double) 1000;
			}

			Long offset = (long) searchText.getSelectionStart();

			results = googleSearch.performPlacesAutocomplete(input, true, latitude, longitude, radius, (String[]) null, offset);
		}
		return results;
	}

	private void updateAutocompleteChoices(ArrayList<String> suggestions) {
		suggestionList.clear();
		if(suggestions != null) {
			for(String suggestion : suggestions) {
				suggestionList.add(suggestion);
			}
		}
		suggestionList.notifyDataSetChanged();
	}

	protected void makeUseOfNewLocation(Location location) {
		currentLocation = location;
		locationSensorImage.setVisibility(View.VISIBLE);
		MakeImageViewSquare(locationSensorImage);
		locationText.setText(GooglePlacesSearch.GetGeodeticString(currentLocation));
		new AsyncTask<Void, Void, ArrayList<String>>() {
			@Override
			protected ArrayList<String> doInBackground(Void... arg0) {
				ArrayList<String> suggestions = null;
				Message msg = updateCurrentLocationTextHandler.obtainMessage();
				msg.obj = googleSearch.ReverseGeocode(currentLocation, true);
				updateCurrentLocationTextHandler.sendMessage(msg);
				if(searchText.enoughToFilter()) {
					suggestions = performAutocompleteSearch(searchText.getText());
				}
				return suggestions;
			}
			
			@Override
			protected void onPostExecute(ArrayList<String> suggestions) {
				updateAutocompleteChoices(suggestions);
			}
		}.execute();
		if(waitingForGps) {
			waitingForGps  = false;
			onClick(searchButton);
		}
	}

	final Handler updateCurrentLocationTextHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			locationText.setText((String) msg.obj);
		}
	};

	private void WaitForGps() {
		waitSpinner = ProgressDialog.show(PlacesSearchActivity.this,
				"", "waiting for location...", true);
		waitingForGps = true;
		new Thread() {
			@Override
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
			@Override
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
		//searchRadius = (long) 5000000;
		googleSearch.PerformSearch(currentLocation.getLatitude(), currentLocation.getLongitude(),
				searchRadius, searchText.getText().toString(), true);
		searching = false;		
	}

	private void BuildResultsList() {
		for(int i = 0; i < googleSearch.GetResultCount(); ++i) {
			ItineraryItem newItem = googleSearch.GetPlace(i);
			if(newItem != null) {
				//newItem.SetDistance(currentLocation);
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
