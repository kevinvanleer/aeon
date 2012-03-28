package vanleer.android.aeon.test;

import java.util.ArrayList;

import com.jayway.android.robotium.solo.Solo;

import vanleer.android.aeon.ItineraryItem;
import vanleer.android.aeon.PlacesSearchActivity;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

public class DestinationSearchTest extends ActivityInstrumentationTestCase2<PlacesSearchActivity> {
	private static final String TARGET_PACKAGE_ID = "vanleer.android.aeon";
	private Solo solo;
	
	public DestinationSearchTest() {
		super(TARGET_PACKAGE_ID, PlacesSearchActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		EmulatorTelnetClient.unlockScreen();
		solo = new Solo(getInstrumentation(), getActivity());
	}
	
	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}
	
	public void testCategorySearch() {
		EmulatorTelnetClient.sendLocation(38.74419380, -90.09839319999999);
		solo.enterText(0, "church");
		solo.clickOnImageButton(0);
		assertTrue(solo.waitForText("Church", 1, 5000));
		ArrayList<ListView> listViews = solo.getCurrentListViews();
		ListView resultsListView = listViews.get(0);
		long distance = 0;
		for(int itemIndex = 0; itemIndex < resultsListView.getCount(); ++itemIndex) {
			ItineraryItem result = (ItineraryItem) resultsListView.getItemAtPosition(itemIndex);
			assertTrue(distance <= result.GetDistance());
			distance = result.GetDistance();
		}
	}
	
	public void testNameSearch() {
		EmulatorTelnetClient.sendLocation(38.74419380, -90.09839319999999);
		solo.enterText(0, "Schlafly Bottleworks");
		solo.clickOnImageButton(0);
		assertTrue(solo.waitForText("Schlafly Bottleworks", 1, 5000));
		ArrayList<ListView> listViews = solo.getCurrentListViews();
		ListView resultsListView = listViews.get(0);
		long distance = 0;
		for(int itemIndex = 0; itemIndex < resultsListView.getCount(); ++itemIndex) {
			ItineraryItem result = (ItineraryItem) resultsListView.getItemAtPosition(itemIndex);
			assertTrue(distance <= result.GetDistance());
			distance = result.GetDistance();			
		}
	}
	
	public void testAddressSearch() {
		EmulatorTelnetClient.sendLocation(38.74419380, -90.09839319999999);
		solo.enterText(0, "4812 Danielle CT 62040");
		solo.clickOnImageButton(0);
		assertTrue(solo.waitForText("4812 Danielle Ct", 1, 5000));
		assertTrue(solo.waitForText("Granite City, IL 62040", 1, 1));
		ArrayList<ListView> listViews = solo.getCurrentListViews();
		ListView resultsListView = listViews.get(0);
		long distance = 0;
		for(int itemIndex = 0; itemIndex < resultsListView.getCount(); ++itemIndex) {
			ItineraryItem result = (ItineraryItem) resultsListView.getItemAtPosition(itemIndex);
			assertTrue(distance <= result.GetDistance());
			distance = result.GetDistance();			
		}
	}
	
	/*public void testEstablishmentAddressSearch() {
		EmulatorTelnetClient.sendLocation(38.74419380, -90.09839319999999);
		solo.enterText(0, "2100 Locust Street, St Louis MO");
		solo.clickOnImageButton(0);
		assertTrue(solo.waitForText("The Schlafly Tap Room", 1, 5000));
		ArrayList<ListView> listViews = solo.getCurrentListViews();
		ListView resultsListView = listViews.get(0);
		long distance = 0;
		for(int itemIndex = 0; itemIndex < resultsListView.getCount(); ++itemIndex) {
			ItineraryItem result = (ItineraryItem) resultsListView.getItemAtPosition(itemIndex);
			assertTrue(distance < result.GetDistance());
			distance = result.GetDistance();			
		}
	}*/
	
	public void testMultipleSearch() {
		EmulatorTelnetClient.sendLocation(38.74419380, -90.09839319999999);
		solo.enterText(0, "Schlafly Bottleworks");
		solo.clickOnImageButton(0);
		assertTrue(solo.waitForText("Schlafly Bottleworks", 1, 5000));
		solo.clearEditText(0);
		solo.enterText(0, "Schlafly Tap Room");
		solo.clickOnImageButton(0);
		assertTrue(solo.waitForText("Schlafly Tap Room", 1, 5000));
		solo.clearEditText(0);
		solo.enterText(0, "church");
		solo.clickOnImageButton(0);
		assertTrue(solo.waitForText("Church", 1, 5000));
		solo.clearEditText(0);
		solo.enterText(0, "1975 Krenning 63013");
		solo.clickOnImageButton(0);
		assertTrue(solo.waitForText("1975 Krenning Rd", 1, 5000));
		
		ArrayList<ListView> listViews = solo.getCurrentListViews();
		ListView bottleworksListView = listViews.get(0);
		long distance = 0;
		for(int itemIndex = 0; itemIndex < bottleworksListView.getCount(); ++itemIndex) {
			ItineraryItem result = (ItineraryItem) bottleworksListView.getItemAtPosition(itemIndex);
			assertTrue(distance <= result.GetDistance());
			distance = result.GetDistance();			
		}
	}
	
	public void testRepeatedSearch() {
		EmulatorTelnetClient.sendLocation(38.74419380, -90.09839319999999);
		solo.enterText(0, "Schlafly Bottleworks");
		solo.clickOnImageButton(0);
		solo.enterText(0, "Schlafly Tap Room");
		solo.clickOnImageButton(0);
		solo.enterText(0, "city hall");
		solo.clickOnImageButton(0);
		solo.enterText(0, "1975 Krenning 63013");
		solo.clickOnImageButton(0);
		
		ArrayList<ListView> listViews = solo.getCurrentListViews();
		ListView bottleworksListView = listViews.get(0);
		long distance = 0;
		for(int itemIndex = 0; itemIndex < bottleworksListView.getCount(); ++itemIndex) {
			ItineraryItem result = (ItineraryItem) bottleworksListView.getItemAtPosition(itemIndex);
			assertTrue(distance <= result.GetDistance());
			distance = result.GetDistance();			
		}
	}
	
	public void testInvalidCategorySearch() {
		EmulatorTelnetClient.sendLocation(38.74419380, -90.09839319999999);
		solo.enterText(0, "real estate gency");
		solo.clickOnImageButton(0);
		//solo.waitForText("The Schlafly Tap Room", 1, 5000);
		ArrayList<ListView> listViews = solo.getCurrentListViews();
		ListView resultsListView = listViews.get(0);
		long distance = 0;
		for(int itemIndex = 0; itemIndex < resultsListView.getCount(); ++itemIndex) {
			ItineraryItem result = (ItineraryItem) resultsListView.getItemAtPosition(itemIndex);
			assertTrue(distance <= result.GetDistance());
			distance = result.GetDistance();			
		}
	}
	
	public void testInvalidNameSearch() {
		EmulatorTelnetClient.sendLocation(38.74419380, -90.09839319999999);
		solo.enterText(0, "blah");
		solo.clickOnImageButton(0);
		//solo.waitForText("The Schlafly Tap Room", 1, 5000);
		ArrayList<ListView> listViews = solo.getCurrentListViews();
		ListView resultsListView = listViews.get(0);
		long distance = 0;
		for(int itemIndex = 0; itemIndex < resultsListView.getCount(); ++itemIndex) {
			ItineraryItem result = (ItineraryItem) resultsListView.getItemAtPosition(itemIndex);
			assertTrue(distance <= result.GetDistance());
			distance = result.GetDistance();			
		}
	}
	
	public void testInvalidSearch() {
		EmulatorTelnetClient.sendLocation(38.74419380, -90.09839319999999);
		solo.enterText(0, "bibbleh");
		solo.clickOnImageButton(0);
		//solo.waitForText("The Schlafly Tap Room", 1, 5000);
		ArrayList<ListView> listViews = solo.getCurrentListViews();
		ListView resultsListView = listViews.get(0);
		long distance = 0;
		for(int itemIndex = 0; itemIndex < resultsListView.getCount(); ++itemIndex) {
			ItineraryItem result = (ItineraryItem) resultsListView.getItemAtPosition(itemIndex);
			assertTrue(distance <= result.GetDistance());
			distance = result.GetDistance();			
		}
	}
}
