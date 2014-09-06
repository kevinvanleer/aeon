package vanleer.android.aeon;

import java.util.Date;

import junit.framework.TestCase;

public class ScheduleTest extends TestCase {

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
		fail("Not yet implemented");
		// original
	}

	public void testIsDepartureTimeFlexible() {
		fail("Not yet implemented");
		// original
	}

	public void testIsStayDurationFlexible() {
		fail("Not yet implemented");
		// original
	}

	public void testIsArrivalTimeValidDate() {
		fail("Not yet implemented");
		// original
	}

	public void testIsArrivalTimeValid() {
		fail("Not yet implemented");
		// original
	}

	public void testIsDepartureTimeValidDate() {
		fail("Not yet implemented");
		// original
	}

	public void testIsDepartureTimeValid() {
		fail("Not yet implemented");
		// original
	}

	public void testIsStayDurationValidLong() {
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
		fail("Not yet implemented");
		// original
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
		fail("Not yet implemented");
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
		fail("Not yet implemented");
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

	public void testIsDateInBounds_IllegalArgument() {
		fail("Not yet implemented");
		// original
	}

	public void testIsDateInBounds_MinMax() {
		fail("Not yet implemented");
		// original
	}

	public void testIsDateInBounds_OnlyMax() {
		fail("Not yet implemented");
		// original
	}

	public void testIsDateInBounds_OnlyMin() {
		fail("Not yet implemented");
		// original
	}

	public void testIsDurationInBounds_null() {
		fail("Not yet implemented");
		// original
	}

	public void testIsDurationInBounds_invalid() {
		fail("Not yet implemented");
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
