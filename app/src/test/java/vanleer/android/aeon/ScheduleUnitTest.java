package vanleer.android.aeon;

import java.util.Date;

import org.junit.Rule;
import org.junit.rules.ExpectedException;

//import junit.framework.TestCase;

import org.junit.Test;

import static org.junit.Assert.*;

public class ScheduleUnitTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();

	protected static void tearDownAfterClass() throws Exception {

	}

    //@Test
	public void testSchedule() {
		fail("Not yet implemented");
		// original
	}

    //@Test
	public void testWriteToParcel() {
		fail("Not yet implemented");
		// original
	}

    //@Test
	public void testAreTimesConstrained_FlexibleDeadline_Undefined() {
		fail("Not yet implemented");
	}

    //@Test
	public void testAreTimesConstrained_FlexibleDeadline_OnlyArrival() {
		fail("Not yet implemented");
	}

    //@Test
	public void testAreTimesConstrained_FlexibleDeadline_OnlyDuration() {
		fail("Not yet implemented");
	}

    //@Test
	public void testAreTimesConstrained_FlexibleDeadline_OnlyDeparture() {
		fail("Not yet implemented");
	}

    //@Test
	public void testAreTimesConstrained_HardDeadline_AllDefined() {
		fail("Not yet implemented");
	}

    //@Test
	public void testAreTimesConstrained_HardDeadline_ArrivalDestination() {
		fail("Not yet implemented");
	}

    //@Test
	public void testAreTimesConstrained_HardDeadline_ArrivalDuration() {
		fail("Not yet implemented");
	}

    //@Test
	public void testAreTimesConstrained_HardDeadline_DepartureDuration() {
		fail("Not yet implemented");
	}

    @Test
	public void testIsArrivalTimeFlexible() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		privateAccess.setMinArrivalTime(new Date(1));
		privateAccess.setArrivalTime(new Date(5));
		privateAccess.setMaxArrivalTime(new Date(10));

		assertTrue(testSchedule.isArrivalTimeFlexible());
	}

    @Test
	public void testIsDepartureTimeFlexible() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		privateAccess.setMinDepartureTime(new Date(1));
		privateAccess.setDepartureTime(new Date(5));
		privateAccess.setMaxDepartureTime(new Date(10));

		assertTrue(testSchedule.isDepartureTimeFlexible());

	}

    @Test
	public void testIsStayDurationFlexible() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		privateAccess.setMinStayDuration((long) 1);
		privateAccess.setStayDuration((long) 5);
		privateAccess.setMaxStayDuration((long) 10);

		assertTrue(testSchedule.isStayDurationFlexible());

	}

    @Test
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

    @Test
	public void testIsDateFlexible_true() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		Date validMin = new Date(1);
		Date validTime = new Date(5);
		Date validMax = new Date(10);

		assertTrue(privateAccess.isDateFlexible(validMin, validTime, validMax));
	}
    @Test
	public void testIsDateFlexible_false() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		Date validMin = new Date(5);
		Date validTime = new Date(5);
		Date validMax = new Date(5);

		assertFalse(privateAccess.isDateFlexible(validMin, validTime, validMax));
	}

    @Test
	public void testIsDateValid_null() {
		Schedule testSchedule = new Schedule();

		try {
			testSchedule.isDateValid(null, null, null);
			fail("NullPointerException expected.");
		} catch (NullPointerException e) {
			// test passed
		}
	}

    @Test
	public void testIsDateValid_no_limits() {
		Schedule testSchedule = new Schedule();
		assertTrue(testSchedule.isDateValid(new Date(), null, null));
	}

    @Test
	public void testIsDateValid_invalid() {
		Schedule testSchedule = new Schedule();

		Date minTime = new Date(5);
		Date time = new Date(1);
		Date maxTime = new Date(10);

		assertFalse(testSchedule.isDateValid(time, minTime, maxTime));
	}

    @Test
	public void testIsDateValid_flexible() {
		Schedule testSchedule = new Schedule();

		Date minTime = new Date(1);
		Date time = new Date(5);
		Date maxTime = new Date(10);

		assertTrue(testSchedule.isDateValid(time, minTime, maxTime));
	}

    @Test
	public void testIsDateValid_hard() {
		Schedule testSchedule = new Schedule();

		Date minTime = new Date(5);
		Date time = new Date(5);
		Date maxTime = new Date(5);

		assertTrue(testSchedule.isDateValid(time, minTime, maxTime));
	}

    @Test
	public void testIsArrivalTimeValid_invalid() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		privateAccess.setMinArrivalTime(new Date(1));
		privateAccess.setArrivalTime(new Date(11));
		privateAccess.setMaxArrivalTime(new Date(10));

		assertFalse(testSchedule.isArrivalTimeValid());
	}

    @Test
	public void testIsArrivalTimeValid_flexible() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		privateAccess.setMinArrivalTime(new Date(1));
		privateAccess.setArrivalTime(new Date(5));
		privateAccess.setMaxArrivalTime(new Date(10));

		assertTrue(testSchedule.isArrivalTimeValid());
	}

    @Test
	public void testIsArrivalTimeValid_hard() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		privateAccess.setMinArrivalTime(new Date(5));
		privateAccess.setArrivalTime(new Date(5));
		privateAccess.setMaxArrivalTime(new Date(5));

		assertTrue(testSchedule.isArrivalTimeValid());
	}

    //@Test
	public void testIsDepartureTimeValid() {
		fail("Not yet implemented");
		// original
	}

    //@Test
	public void testIsDurationValid() {
		fail("Not yet implemented");
		// original
	}

    //@Test
	public void testIsStayDurationValid() {
		fail("Not yet implemented");
		// original
	}

    //@Test
	public void testValidate() {
		fail("Not yet implemented");

	}

    @Test
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

    //@Test
	public void testGetArrivalTime() {
		fail("Not yet implemented");
		// original
	}

    //@Test
	public void testGetMinArrivalTime() {
		fail("Not yet implemented");
		// original
	}

    //@Test
	public void testGetMaxArrivalTime() {
		fail("Not yet implemented");
		// original
	}

    //@Test
	public void testGetDepartureTime() {
		fail("Not yet implemented");
		// original
	}

    //@Test
	public void testGetMinDepartureTime() {
		fail("Not yet implemented");
		// original
	}

    //@Test
	public void testGetMaxDepartureTime() {
		fail("Not yet implemented");
		// original
	}

    //@Test
	public void testGetStayDuration() {
		fail("Not yet implemented");
		// original
	}

    //@Test
	public void testGetMinStayDuration() {
		fail("Not yet implemented");
		// original
	}

    //@Test
	public void testGetMaxStayDuration() {
		fail("Not yet implemented");
		// original
	}

    //@Test
	public void testGetArrivalTimeString() {
		fail("Not yet implemented");
	}

    //@Test
	public void testGetDepartureTimeString() {
		fail("Not yet implemented");
	}

    //@Test
	public void testGetStayDurationClockFormat() {
		fail("Not yet implemented");
	}

    //@Test
	public void testGetStayDurationLongFormat() {
		fail("Not yet implemented");
	}

    //@Test
	public void testSetArrivalTime() {
		fail("Not yet implemented");
		// original
	}

    @Test
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

    //@Test
	public void testSetHardArrivalTime_Invalid() {
		fail("Not yet implemented");
		// original
	}

    //@Test
	public void testSetMinArrivalTime() {
		fail("Not yet implemented");
		// original
	}

    //@Test
	public void testOverrideMinArrivalTime() {
		fail("Not yet implemented");
	}

    //@Test
	public void testSetMaxArrivalTime() {
		fail("Not yet implemented");
	}

    //@Test
	public void testOverrideMaxArrivalTime() {
		fail("Not yet implemented");
	}

    //@Test
	public void testSetDepartureTime() {
		fail("Not yet implemented");
	}

    @Test
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

    //@Test
	public void testSetMinDepartureTime() {
		fail("Not yet implemented");
	}

    //@Test
	public void testOverrideMinDepartureTime() {
		fail("Not yet implemented");
	}

    //@Test
	public void testSetMaxDepartureTime() {
		fail("Not yet implemented");
	}

    //@Test
	public void testOverrideMaxDepartureTime() {
		fail("Not yet implemented");
	}

    //@Test
	public void testSetStayDuration() {
		fail("Not yet implemented");
	}

    @Test
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

    //@Test
	public void testSetMinStayDuration() {
		fail("Not yet implemented");
	}

    //@Test
	public void testOverrideMinStayDuration() {
		fail("Not yet implemented");
	}

    //@Test
	public void testSetMaxStayDuration() {
		fail("Not yet implemented");
	}

    //@Test
	public void testOverrideMaxStayDuration() {
		fail("Not yet implemented");
	}

    //@Test
	public void testDescribeContents() {
		fail("Not yet implemented");
	}

    @Test
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

    @Test
	public void testIsDateInBounds_no_min_no_max() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		assertTrue(privateAccess.isDateInBounds(new Date(1), null, null));
	}

    @Test
	public void testIsDateInBounds_only_max_valid() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		assertTrue(privateAccess.isDateInBounds(new Date(1), null, new Date(2)));
	}

    @Test
	public void testIsDateInBounds_only_max_invalid() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		assertFalse(privateAccess.isDateInBounds(new Date(3), null, new Date(2)));
	}

    @Test
	public void testIsDateInBounds_only_min_valid() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		assertTrue(privateAccess.isDateInBounds(new Date(2), new Date(1), null));
	}

    @Test
	public void testIsDateInBounds_only_min_invalid() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		assertFalse(privateAccess.isDateInBounds(new Date(3), new Date(4), null));
	}

    @Test
	public void testIsDurationInBounds_valid() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		assertTrue(privateAccess.isDateInBounds(new Date(6), new Date(4), new Date(8)));
	}

    @Test
	public void testIsDurationInBounds_invalid() {
		Schedule testSchedule = new Schedule();
		Schedule.PrivateTests privateAccess = testSchedule.new PrivateTests();

		assertFalse(privateAccess.isDateInBounds(new Date(3), new Date(4), new Date(8)));
	}

    //@Test
	public void testIsDurationInBounds_less_than_min() {
		fail("Not yet implemented");
	}

    //@Test
	public void testIsDurationInBounds_greater_than_max() {
		fail("Not yet implemented");
	}

    //@Test
	public void testIsDurationInBounds_in_bounds() {
		fail("Not yet implemented");
	}
}
