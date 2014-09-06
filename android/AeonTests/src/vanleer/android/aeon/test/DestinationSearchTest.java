package vanleer.android.aeon.test;

import com.robotium.solo.Solo;

import vanleer.android.aeon.ItineraryItem;
import vanleer.android.aeon.PlacesSearchActivity;
import vanleer.android.aeon.R;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.ListView;

public class DestinationSearchTest extends ActivityInstrumentationTestCase2<PlacesSearchActivity> {
	private static final String TARGET_PACKAGE_ID = "vanleer.android.aeon";
	private Solo solo;
	static final int TIMEOUT_MS = 20000;
	
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

	View findView(int id) {
		return solo.getCurrentActivity().findViewById(id);
	}

	void checkDistanceOrder(ListView tested) {
		Long distance = (long) 0;
		for (int itemIndex = 0; itemIndex < tested.getCount(); ++itemIndex) {
			ItineraryItem result = (ItineraryItem) tested.getItemAtPosition(itemIndex);
			if (distance == null) {
				assertTrue(result.getDistance() == null);
			} else if (result.getDistance() != null) {
				assertTrue(distance <= result.getDistance());
				distance = result.getDistance();
			} else {
				distance = null;
			}
		}
	}

	public void testCategorySearch() {
		EmulatorTelnetClient.sendLocation(38.74419380, -90.09839319999999);
		solo.enterText(0, "church");
		solo.clickOnImageButton(0);
		assertTrue(solo.waitForText("Church", 1, TIMEOUT_MS));
		ListView resultsListView = (ListView) findView(R.id.listView_searchResults);
		checkDistanceOrder(resultsListView);
	}

	public void testNameSearch() {
		EmulatorTelnetClient.sendLocation(38.74419380, -90.09839319999999);
		solo.enterText(0, "Schlafly Bottleworks");
		solo.clickOnImageButton(0);
		assertTrue(solo.waitForText("Schlafly Bottleworks", 1, TIMEOUT_MS));
		ListView resultsListView = (ListView) findView(R.id.listView_searchResults);
		checkDistanceOrder(resultsListView);
	}

	public void testAddressSearch() {
		EmulatorTelnetClient.sendLocation(38.74419380, -90.09839319999999);
		solo.enterText(0, "4812 Danielle CT 62040");
		solo.clickOnImageButton(0);
		assertTrue(solo.waitForText("4812 Danielle Ct", 1, TIMEOUT_MS));
		assertTrue(solo.waitForText("Granite City, IL 62040", 1, 1));
		ListView resultsListView = (ListView) findView(R.id.listView_searchResults);
		checkDistanceOrder(resultsListView);
	}

	/*
	 * public void testEstablishmentAddressSearch() { EmulatorTelnetClient.sendLocation(38.74419380, -90.09839319999999); solo.enterText(0, "2100 Locust Street, St Louis MO"); solo.clickOnImageButton(0); assertTrue(solo.waitForText("The Schlafly Tap Room", 1, TIMEOUT_MS)); ArrayList<ListView> listViews = solo.getCurrentListViews(); ListView resultsListView = listViews.get(0); long distance = 0; for(int itemIndex = 0; itemIndex < resultsListView.getCount(); ++itemIndex) { ItineraryItem result = (ItineraryItem) resultsListView.getItemAtPosition(itemIndex); assertTrue(distance < result.GetDistance()); distance = result.GetDistance(); } }
	 */

	public void testMultipleSearch() {
		EmulatorTelnetClient.sendLocation(38.74419380, -90.09839319999999);
		solo.enterText(0, "Schlafly Bottleworks");
		solo.clickOnImageButton(0);
		assertTrue(solo.waitForText("Schlafly Bottleworks", 1, TIMEOUT_MS));
		solo.clearEditText(0);
		solo.enterText(0, "Schlafly Tap Room");
		solo.clickOnImageButton(0);
		assertTrue(solo.waitForText("Schlafly Tap Room", 1, TIMEOUT_MS));
		solo.clearEditText(0);
		solo.enterText(0, "church");
		solo.clickOnImageButton(0);
		assertTrue(solo.waitForText("Church", 1, TIMEOUT_MS));
		solo.clearEditText(0);
		solo.enterText(0, "1975 Krenning 63013");
		solo.clickOnImageButton(0);
		assertTrue(solo.waitForText("1975 Krenning Rd", 1, TIMEOUT_MS));

		ListView resultsListView = (ListView) findView(R.id.listView_searchResults);
		checkDistanceOrder(resultsListView);
	}

	public void testRepeatedSearch() {
		EmulatorTelnetClient.sendLocation(38.74419380, -90.09839319999999);
		solo.enterText(0, "Schlafly Bottleworks");
		solo.clickOnImageButton(0);
		solo.waitForDialogToClose(TIMEOUT_MS);
		solo.enterText(0, "Schlafly Tap Room");
		solo.clickOnImageButton(0);
		solo.waitForDialogToClose(TIMEOUT_MS);
		solo.enterText(0, "city hall");
		solo.clickOnImageButton(0);
		solo.waitForDialogToClose(TIMEOUT_MS);
		solo.enterText(0, "1975 Krenning 63013");
		solo.clickOnImageButton(0);
		solo.waitForDialogToClose(TIMEOUT_MS);

		ListView resultsListView = (ListView) findView(R.id.listView_searchResults);
		checkDistanceOrder(resultsListView);
	}

	public void testInvalidCategorySearch() {
		EmulatorTelnetClient.sendLocation(38.74419380, -90.09839319999999);
		solo.enterText(0, "real estate gency");
		solo.clickOnImageButton(0);
		solo.waitForDialogToClose(TIMEOUT_MS);
		ListView resultsListView = (ListView) findView(R.id.listView_searchResults);
		checkDistanceOrder(resultsListView);
	}

	public void testInvalidNameSearch() {
		EmulatorTelnetClient.sendLocation(38.74419380, -90.09839319999999);
		solo.enterText(0, "63013");
		solo.clickOnImageButton(0);
		solo.waitForDialogToClose(TIMEOUT_MS);
		ListView resultsListView = (ListView) findView(R.id.listView_searchResults);
		checkDistanceOrder(resultsListView);
	}

	public void testInvalidSearch() {
		EmulatorTelnetClient.sendLocation(38.74419380, -90.09839319999999);
		solo.enterText(0, "bibbleh");
		solo.clickOnImageButton(0);
		solo.waitForDialogToClose(TIMEOUT_MS);
		ListView resultsListView = (ListView) findView(R.id.listView_searchResults);
		checkDistanceOrder(resultsListView);
	}

	public void testPreLocatedSearchAutoComplete() {
		EmulatorTelnetClient.sendLocation(38.74419380, -90.09839319999999);
		solo.enterText(0, "sch");
		assertTrue(solo.waitForText("Schlafly Tap Room", 0, TIMEOUT_MS));
		assertTrue(solo.waitForText("Schnucks", 0, TIMEOUT_MS));
		solo.enterText(0, "nu");
		assertTrue(solo.waitForText("Schnucks", 0, TIMEOUT_MS));
	}

	public void testPostLocatedSearchAutoComplete() {
		solo.enterText(0, "sch");
		assertTrue(solo.waitForText("Schiphol", 0, TIMEOUT_MS));
		EmulatorTelnetClient.sendLocation(38.74419380, -90.09839319999999);
		assertTrue(solo.waitForText("Schlafly Tap Room", 0, TIMEOUT_MS));
		assertTrue(solo.waitForText("Schnucks", 0, TIMEOUT_MS));
	}
}
