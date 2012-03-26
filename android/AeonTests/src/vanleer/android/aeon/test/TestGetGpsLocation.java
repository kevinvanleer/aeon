package vanleer.android.aeon.test;

import vanleer.android.aeon.*;

import com.jayway.android.robotium.solo.Solo;

import android.test.ActivityInstrumentationTestCase2;

public class TestGetGpsLocation extends ActivityInstrumentationTestCase2<AeonActivity> {
	private static final String TARGET_PACKAGE_ID = "vanleer.android.aeon";
	private Solo solo;

	public TestGetGpsLocation() throws ClassNotFoundException {
		super(TARGET_PACKAGE_ID, AeonActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		solo = new Solo(getInstrumentation(), getActivity());
	}

	public void testUpdateGpsLocation() {
		solo.assertCurrentActivity("Main menu is not the current activity.", AeonActivity.class);
		solo.clickOnButton("Plan");
		solo.sendKey(Solo.MENU);
		solo.assertCurrentActivity("Itinerary is not the current activity.", Itinerary.class);
		solo.clickOnText("Add");
		solo.clickOnText("Google Search");
		solo.assertCurrentActivity("PlacesSearch is not the current activity.", PlacesSearchActivity.class);
		TestLocationProvider.sendLocation(38.74419380, -90.09839319999999);
		assertTrue(solo.waitForText("4812 Danielle"));
	}
	
	/*public void sendGpsLocation(double latitude, double longitude)
	{
		final String TEST_PROVIDER = "testGps";
		LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
		locationManager.addTestProvider(TEST_PROVIDER, false, false, false, false, false, false, false, Criteria.POWER_LOW, Criteria.ACCURACY_FINE);
		locationManager.setTestProviderEnabled(TEST_PROVIDER, true);
		locationManager.setTestProviderStatus(TEST_PROVIDER, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
		
		Location mockLocation = new Location(TEST_PROVIDER);
		mockLocation.setLatitude(latitude);
		mockLocation.setLongitude(longitude);
		mockLocation.setTime(System.currentTimeMillis());
		mockLocation.setSpeed(0);
		mockLocation.setAccuracy(25);
		mockLocation.setAltitude(0);
		mockLocation.setBearing(0);
		
		locationManager.setTestProviderLocation(TEST_PROVIDER, mockLocation);
	}*/

	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}
}
