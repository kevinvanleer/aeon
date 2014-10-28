package vanleer.android.aeon;

import java.util.Date;

import org.junit.Rule;
import org.junit.rules.ExpectedException;

import junit.framework.TestCase;

public class ScheduleTest extends TestCase {
	@Rule
	public ExpectedException exception = ExpectedException.none();

	protected static void tearDownAfterClass() throws Exception {

	}

	public void testSchedule() {
		fail("Not yet implemented");
		// original
	}

	public void testWriteToParcel() {
		fail("Not yet implemented");
		// original
	}

	public void testAreTimesConstrained_FlexibleDeadline_Undefined() {
		fail("Not yet implemented");
	}

	public void testAreTimesConstrained_FlexibleDeadline_OnlyArrival() {
		fail("Not yet implemented");
	}

	public void testAreTimesConstrained_FlexibleDeadline_OnlyDuration() {
		fail("Not yet implemented");
	}

	public void testAreTimesConstrained_FlexibleDeadline_OnlyDeparture() {
		fail("Not yet implemented");
	}

	public void testAreTimesConstrained_HardDeadline_AllDefined() {
		fail("Not yet implemented");
	}

	public void testAreTimesConstrained_HardDeadline_ArrivalDestination() {
		fail("Not yet implemented");
	}

	public void testAreTimesConstrained_HardDeadline_ArrivalDuration() {
		fail("Not yet implemented");
	}

	public void testAreTimesConstrained_HardDeadline_DepartureDuration() {
		fail("Not yet implemented");
	}

	public void testIsArrivalTimeFlexible() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		privateAccess.setMinArrivalTime(new Date(1));
		privateAccess.setArrivalTime(new Date(5));
		privateAccess.setMaxArrivalTime(new Date(10));

		assertTrue(testSchedule.isArrivalTimeFlexible());
	}

	public void testIsDepartureTimeFlexible() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		privateAccess.setMinDepartureTime(new Date(1));
		privateAccess.setDepartureTime(new Date(5));
		privateAccess.setMaxDepartureTime(new Date(10));

		assertTrue(testSchedule.isDepartureTimeFlexible());

	}

	public void testIsStayDurationFlexible() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		privateAccess.setMinStayDuration((long) 1);
		privateAccess.setStayDuration((long) 5);
		privateAccess.setMaxStayDuration((long) 10);

		assertTrue(testSchedule.isStayDurationFlexible());

	}

	public void testIsDateFlexible_null_argument() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		Date validMin = new Date(1);
		Date validTime = new Date(5);
		Date validMax = new Date(10);

		assertTrue(privateAccess.isDateFlexible(null, null, null));
		assertTrue(privateAccess.isDateFlexible(null, validMin, validMax));
		assertTrue(privateAccess.isDateFlexible(validTime, null, validMax));
		assertTrue(privateAccess.isDateFlexible(validTime, validMin, null));
	}

	public void testIsDateFlexible_true() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		Date validMin = new Date(1);
		Date validTime = new Date(5);
		Date validMax = new Date(10);

		assertTrue(privateAccess.isDateFlexible(validMin, validTime, validMax));
	}

	public void testIsDateFlexible_false() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		Date validMin = new Date(5);
		Date validTime = new Date(5);
		Date validMax = new Date(5);

		assertFalse(privateAccess.isDateFlexible(validMin, validTime, validMax));
	}

	public void testIsDateValid_null() {
		Schedule testSchedule = new Schedule();

		try {
			testSchedule.isDateValid(null, null, null);
			fail("NullPointerException expected.");
		} catch (NullPointerException e) {
			// test passed
		}
	}

	public void testIsDateValid_no_limits() {
		Schedule testSchedule = new Schedule();
		assertTrue(testSchedule.isDateValid(new Date(), null, null));
	}

	public void testIsDateValid_invalid() {
		Schedule testSchedule = new Schedule();

		Date minTime = new Date(5);
		Date time = new Date(1);
		Date maxTime = new Date(10);

		assertFalse(testSchedule.isDateValid(time, minTime, maxTime));
	}

	public void testIsDateValid_flexible() {
		Schedule testSchedule = new Schedule();

		Date minTime = new Date(1);
		Date time = new Date(5);
		Date maxTime = new Date(10);

		assertTrue(testSchedule.isDateValid(time, minTime, maxTime));
	}

	public void testIsDateValid_hard() {
		Schedule testSchedule = new Schedule();

		Date minTime = new Date(5);
		Date time = new Date(5);
		Date maxTime = new Date(5);

		assertTrue(testSchedule.isDateValid(time, minTime, maxTime));
	}

	public void testIsArrivalTimeValid_invalid() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		privateAccess.setMinArrivalTime(new Date(1));
		privateAccess.setArrivalTime(new Date(11));
		privateAccess.setMaxArrivalTime(new Date(10));

		assertFalse(testSchedule.isArrivalTimeValid());
	}

	public void testIsArrivalTimeValid_flexible() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		privateAccess.setMinArrivalTime(new Date(1));
		privateAccess.setArrivalTime(new Date(5));
		privateAccess.setMaxArrivalTime(new Date(10));

		assertTrue(testSchedule.isArrivalTimeValid());
	}

	public void testIsArrivalTimeValid_hard() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		privateAccess.setMinArrivalTime(new Date(5));
		privateAccess.setArrivalTime(new Date(5));
		privateAccess.setMaxArrivalTime(new Date(5));

		assertTrue(testSchedule.isArrivalTimeValid());
	}

	public void testIsDepartureTimeValid() {
		fail("Not yet implemented");
		// original
	}

	public void testIsDurationValid() {
		fail("Not yet implemented");
		// original
	}

	public void testIsStayDurationValid() {
		fail("Not yet implemented");
		// original
	}

	public void testValidate() {
		fail("Not yet implemented");

	}

	public void testUpdate() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();
		privateAccess.setDepartureTime(new Date());
		privateAccess.setStayDuration((long) (30 * 60));
		testSchedule.update(new Date());
		long arrivalTime = testSchedule.getArrivalTime().getTime();
		long departureTime = testSchedule.getDepartureTime().getTime();
		long stayDurationMs = testSchedule.getStayDuration() * 1000;
		assertTrue(stayDurationMs == (30 * 60 * 1000));
		assertTrue(testSchedule.getArrivalTime() != testSchedule.getDepartureTime());
		assertTrue(arrivalTime == (departureTime - stayDurationMs));
	}

	public void testGetArrivalTime() {
		fail("Not yet implemented");
		// original
	}

	public void testGetMinArrivalTime() {
		fail("Not yet implemented");
		// original
	}

	public void testGetMaxArrivalTime() {
		fail("Not yet implemented");
		// original
	}

	public void testGetDepartureTime() {
		fail("Not yet implemented");
		// original
	}

	public void testGetMinDepartureTime() {
		fail("Not yet implemented");
		// original
	}

	public void testGetMaxDepartureTime() {
		fail("Not yet implemented");
		// original
	}

	public void testGetStayDuration() {
		fail("Not yet implemented");
		// original
	}

	public void testGetMinStayDuration() {
		fail("Not yet implemented");
		// original
	}

	public void testGetMaxStayDuration() {
		fail("Not yet implemented");
		// original
	}

	public void testGetArrivalTimeString() {
		fail("Not yet implemented");
	}

	public void testGetDepartureTimeString() {
		fail("Not yet implemented");
	}

	public void testGetStayDurationClockFormat() {
		fail("Not yet implemented");
	}

	public void testGetStayDurationLongFormat() {
		fail("Not yet implemented");
	}

	public void testSetArrivalTime() {
		fail("Not yet implemented");
		// original
	}

	public void testSetHardArrivalTime() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		Date theDate = new Date(100);
		privateAccess.setHardArrivalTime(theDate);
		assertTrue(privateAccess.getArrivalTime().equals(theDate));
		assertTrue(privateAccess.getMaxArrivalTime().equals(theDate));
		assertTrue(privateAccess.getMinArrivalTime().equals(theDate));
		assertFalse(testSchedule.isArrivalTimeFlexible());
	}

	public void testSetHardArrivalTime_Invalid() {
		fail("Not yet implemented");
		// original
	}

	public void testSetMinArrivalTime() {
		fail("Not yet implemented");
		// original
	}

	public void testOverrideMinArrivalTime() {
		fail("Not yet implemented");
	}

	public void testSetMaxArrivalTime() {
		fail("Not yet implemented");
	}

	public void testOverrideMaxArrivalTime() {
		fail("Not yet implemented");
	}

	public void testSetDepartureTime() {
		fail("Not yet implemented");
	}

	public void testSetHardDepartureTime() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		Date theDate = new Date(100);
		privateAccess.setHardDepartureTime(theDate);
		assertTrue(privateAccess.getDepartureTime().equals(theDate));
		assertTrue(privateAccess.getMaxDepartureTime().equals(theDate));
		assertTrue(privateAccess.getMinDepartureTime().equals(theDate));
		assertFalse(testSchedule.isDepartureTimeFlexible());
	}

	public void testSetMinDepartureTime() {
		fail("Not yet implemented");
	}

	public void testOverrideMinDepartureTime() {
		fail("Not yet implemented");
	}

	public void testSetMaxDepartureTime() {
		fail("Not yet implemented");
	}

	public void testOverrideMaxDepartureTime() {
		fail("Not yet implemented");
	}

	public void testSetStayDuration() {
		fail("Not yet implemented");
	}

	public void testSetHardStayDuration() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		Long theDate = Long.valueOf(100);
		privateAccess.setHardStayDuration(theDate);
		assertTrue(privateAccess.getStayDuration().equals(theDate));
		assertTrue(privateAccess.getMaxStayDuration().equals(theDate));
		assertTrue(privateAccess.getMinStayDuration().equals(theDate));
		assertFalse(testSchedule.isStayDurationFlexible());
	}

	public void testSetMinStayDuration() {
		fail("Not yet implemented");
	}

	public void testOverrideMinStayDuration() {
		fail("Not yet implemented");
	}

	public void testSetMaxStayDuration() {
		fail("Not yet implemented");
	}

	public void testOverrideMaxStayDuration() {
		fail("Not yet implemented");
	}

	public void testDescribeContents() {
		fail("Not yet implemented");
	}

	public void testIsDateInBounds_throw() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		try {
			privateAccess.isDateInBounds(null, null, null);
			fail("NullPointerException expected");
		} catch (NullPointerException e) {
			// test passed
		}
	}

	public void testIsDateInBounds_no_min_no_max() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		assertTrue(privateAccess.isDateInBounds(new Date(1), null, null));
	}

	public void testIsDateInBounds_only_max_valid() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		assertTrue(privateAccess.isDateInBounds(new Date(1), null, new Date(2)));
	}

	public void testIsDateInBounds_only_max_invalid() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		assertFalse(privateAccess.isDateInBounds(new Date(3), null, new Date(2)));
	}

	public void testIsDateInBounds_only_min_valid() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		assertTrue(privateAccess.isDateInBounds(new Date(2), new Date(1), null));
	}

	public void testIsDateInBounds_only_min_invalid() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		assertFalse(privateAccess.isDateInBounds(new Date(3), new Date(4), null));
	}

	public void testIsDurationInBounds_valid() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		assertTrue(privateAccess.isDateInBounds(new Date(6), new Date(4), new Date(8)));
	}

	public void testIsDurationInBounds_invalid() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		assertFalse(privateAccess.isDateInBounds(new Date(3), new Date(4), new Date(8)));
	}

	public void testIsDurationInBounds_less_than_min() {
		fail("Not yet implemented");
	}

	public void testIsDurationInBounds_greater_than_max() {
		fail("Not yet implemented");
	}

	public void testIsDurationInBounds_in_bounds() {
		fail("Not yet implemented");
	}
}
