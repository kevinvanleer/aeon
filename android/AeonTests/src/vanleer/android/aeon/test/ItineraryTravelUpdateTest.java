package vanleer.android.aeon.test;

import java.util.ArrayList;

import vanleer.android.aeon.*;
import vanleer.android.aeon.R;

import com.robotium.solo.Solo;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.ListView;

public class ItineraryTravelUpdateTest extends ActivityInstrumentationTestCase2<Itinerary> {
	private static final String TARGET_PACKAGE_ID = "vanleer.android.aeon";
	private Solo solo;

	public ItineraryTravelUpdateTest() {
		super(TARGET_PACKAGE_ID, Itinerary.class);
	}

	View findView(int id) {
		return solo.getCurrentActivity().findViewById(id);
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

	public void testStepThruItinerary() {
		solo.assertCurrentActivity("Itinerary is not the current activity.", Itinerary.class);
		solo.waitForText("Itinerary");
		EmulatorTelnetClient.sendLocation(38.477548, -91.051562);
		assertTrue(solo.waitForText("Stonehenge"));
		solo.clickLongOnText("Next destination");

		assertTrue(solo.waitForActivity(PlacesSearchActivity.class));
		solo.enterText(0, "krakow store");
		solo.clickOnImageButton(0);
		assertTrue(solo.waitForText("Krakow Store"));
		solo.clickLongOnText("Krakow Store", 2);

		assertTrue(solo.waitForActivity(DestinationScheduleActivity.class));
		solo.clickOnView(findView(R.id.checkBox_duration));
		solo.setTimePicker(0, 0, 10);
		solo.clickOnText("Done");

		solo.waitForActivity(Itinerary.class);
		solo.clickLongOnText("Next destination");

		assertTrue(solo.waitForActivity(PlacesSearchActivity.class));
		solo.enterText(0, "burger park");
		solo.clickOnImageButton(0);
		assertTrue(solo.waitForText("Burger Park"));
		solo.clickLongOnText("Burger Park", 1);

		assertTrue(solo.waitForActivity(DestinationScheduleActivity.class));
		solo.clickOnView(findView(R.id.checkBox_duration));
		solo.clickOnView(findView(R.id.checkBox_departureTime));
		solo.clickOnText("Done");

		solo.waitForActivity(Itinerary.class);
		solo.sendKey(Solo.MENU);
		// solo.clickOnText("Add");
		solo.clickOnMenuItem("Add");
		solo.clickOnText("My Location");

		solo.waitForActivity(DestinationScheduleActivity.class);
		solo.clickOnView(findView(R.id.checkBox_departureTime));
		solo.clickOnText("Done");

		solo.waitForActivity(Itinerary.class);
		solo.sleep(60 * 1000 * 2 + 5000);
		EmulatorTelnetClient.sendLocation(38.482120, -91.043894);
		ListView itinerary = (ListView) findView(R.id.listView_itinerary);
		ArrayList<Schedule> initialTimes = new ArrayList<Schedule>();

		for (int i = 0; i < itinerary.getAdapter().getCount(); ++i) {
			ItineraryItem item = (ItineraryItem) itinerary.getAdapter().getItem(i);
			// Now get the current schedule times so they can be compared after execution
			initialTimes.add(new Schedule(item.getSchedule()));
		}

		/*-while () {
			EmulatorTelnetClient.sendLocation(38.482120, -91.043894);
			solo.sleep(500);
		}*/

		solo.sleep(60 * 1000 * 4 + 5000);

		EmulatorTelnetClient.sendLocation(38.497900, -91.045503);

		ArrayList<Schedule> nextTimes = new ArrayList<Schedule>();
		for (int i = 0; i < itinerary.getAdapter().getCount(); ++i) {
			ItineraryItem item = (ItineraryItem) itinerary.getAdapter().getItem(i);
			// Now get the current schedule times so they can be compared after execution
			nextTimes.add(new Schedule(item.getSchedule()));
		}

		solo.sleep(60 * 1000 + 5000);
	}
}
