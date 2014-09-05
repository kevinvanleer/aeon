package vanleer.android.aeon;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import vanleer.util.TimeFormat;

public class Schedule implements Parcelable {
	private final Long INVALID_DURATION = Long.valueOf(-1);
	private Date minArrivalTime = null;
	private Date maxArrivalTime = null;
	private Date arrivalTime = null;
	private Date minDepartureTime = null;
	private Date maxDepartureTime = null;
	private Date departureTime = null;
	private Long minStayDurationSec = INVALID_DURATION;
	private Long maxStayDurationSec = INVALID_DURATION;
	private Long stayDurationSec = INVALID_DURATION;

	public Schedule() {
	}

	private Schedule(Parcel in) {
		readFromParcel(in);
	}

	private void readFromParcel(Parcel in) {
		minArrivalTime = (Date) in.readSerializable();
		maxArrivalTime = (Date) in.readSerializable();
		arrivalTime = (Date) in.readSerializable();
		minDepartureTime = (Date) in.readSerializable();
		maxDepartureTime = (Date) in.readSerializable();
		departureTime = (Date) in.readSerializable();
		minStayDurationSec = in.readLong();
		maxStayDurationSec = in.readLong();
		stayDurationSec = in.readLong();
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeSerializable(minArrivalTime);
		dest.writeSerializable(maxArrivalTime);
		dest.writeSerializable(arrivalTime);
		dest.writeSerializable(minDepartureTime);
		dest.writeSerializable(maxDepartureTime);
		dest.writeSerializable(departureTime);
		dest.writeLong(minStayDurationSec);
		dest.writeLong(maxStayDurationSec);
		dest.writeLong(stayDurationSec);
	}

	public boolean areTimesConstrained() {
		boolean constrained;
		if (isArrivalTimeFlexible() ? !(isDepartureTimeFlexible() || isStayDurationFlexible()) : !(isDepartureTimeFlexible() && isStayDurationFlexible())) {

			Calendar cal = Calendar.getInstance();
			cal.setTime(getArrivalTime());
			long arrivalSec = cal.get(Calendar.SECOND);
			cal.setTime(getDepartureTime());
			long departureSec = cal.get(Calendar.SECOND);

			constrained = (getStayDuration() == (departureSec - arrivalSec));
		} else {
			constrained = ((getMaxStayDuration() * 1000) <= getMaxDepartureTime().getTime() - getMinArrivalTime().getTime());
			constrained &= ((getMaxStayDuration() * 1000) >= getMinDepartureTime().getTime() - getMaxArrivalTime().getTime());
			constrained &= ((getMinStayDuration() * 1000) <= getMaxDepartureTime().getTime() - getMinArrivalTime().getTime());
			constrained &= ((getMinStayDuration() * 1000) >= getMinDepartureTime().getTime() - getMaxArrivalTime().getTime());
		}
		return constrained;
	}

	public boolean isArrivalTimeFlexible() {
		return isDateFlexible(arrivalTime, minArrivalTime, maxArrivalTime);
	}

	public boolean isDepartureTimeFlexible() {
		return isDateFlexible(departureTime, minDepartureTime, maxDepartureTime);
	}

	public boolean isStayDurationFlexible() {
		return isDurationFlexible(stayDurationSec, minStayDurationSec, maxStayDurationSec);
	}

	private boolean isDateFlexible(Date time, Date minTime, Date maxTime) {
		boolean flexible = (time == null || minTime == null || maxTime == null);
		if (!flexible) {
			flexible = ((time.compareTo(minTime) != 0) || (time.compareTo(maxTime) != 0));
		}
		return flexible;
	}

	private boolean isDurationFlexible(Long duration, Long minDuration, Long maxDuration) {
		boolean flexible = ((duration == INVALID_DURATION) || (minDuration == INVALID_DURATION) || (maxDuration == INVALID_DURATION));
		if (!flexible) {
			flexible = !((duration == minDuration) && (duration == maxDuration));
		}
		return flexible;
	}

	public boolean isArrivalTimeValid(Date time) {
		return isArrivalTimeFlexible() ? isDateInBounds(time, minArrivalTime, maxArrivalTime) : (time == arrivalTime);
	}

	public boolean isArrivalTimeValid() {
		return isArrivalTimeValid(arrivalTime);
	}

	public boolean isDepartureTimeValid(Date time) {
		return isDepartureTimeFlexible() ? isDateInBounds(time, minDepartureTime, maxDepartureTime) : (time == departureTime);
	}

	public boolean isDepartureTimeValid() {
		return isDepartureTimeValid(departureTime);
	}

	public boolean isStayDurationValid(Long duration) {
		return isStayDurationFlexible() ? isDurationInBounds(duration, minStayDurationSec, maxStayDurationSec) : (duration == stayDurationSec);
	}

	public boolean isStayDurationValid() {
		return isStayDurationValid(stayDurationSec);
	}

	public boolean validate() {
		return (isArrivalTimeValid() && isDepartureTimeValid() && isStayDurationValid() && areTimesConstrained());
	}

	public void update(Date newArrivalTime) {
		setArrivalTime(newArrivalTime);

		Calendar departure = Calendar.getInstance();
		departure.setTime(arrivalTime);

		departure.add(Calendar.SECOND, stayDurationSec.intValue());
		departureTime.setTime(departure.getTimeInMillis());
	}

	private boolean isDateInBounds(Date time, Date minTime, Date maxTime) {
		if (time == null) {
			throw new NullPointerException();
		}

		boolean inBounds = true;
		if (minTime != null) {
			inBounds = (time.compareTo(minTime) >= 0);
		}
		if (inBounds && (maxTime != null)) {
			inBounds = (time.compareTo(maxTime) <= 0);
		}
		return inBounds;
	}

	private boolean isDurationInBounds(Long duration, Long minDuration, Long maxDuration) {
		if (duration == null) {
			throw new NullPointerException();
		}

		if (duration == INVALID_DURATION) {
			throw new RuntimeException();
		}

		boolean inBounds = true;
		if (minDuration != INVALID_DURATION) {
			inBounds = duration >= minDuration;
		}
		if (inBounds && (maxDuration != INVALID_DURATION)) {
			inBounds = (duration <= maxDuration);
		}
		return inBounds;
	}

	public Date getArrivalTime() {
		Date arrival = arrivalTime;
		if (arrival == null) {
			arrival = getMaxArrivalTime();
		}
		if (arrival == null) {
			arrival = getMinArrivalTime();
		}
		if (arrival == null) {
			if ((departureTime != null) && (stayDurationSec != INVALID_DURATION)) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(getDepartureTime());
				arrival = new Date((cal.get(Calendar.SECOND) - getStayDuration()) * 1000);
			}
		}
		if (arrival == null) {
			// TODO: SET ARRIVAL TIME FROM TRAVEL DURATION
		}
		return arrival;
	}

	public Date getMinArrivalTime() {
		return minArrivalTime;
	}

	public Date getMaxArrivalTime() {
		return maxArrivalTime;
	}

	public Date getDepartureTime() {
		Date departure = departureTime;
		if (departure == null) {
			departure = getMinDepartureTime();
		}
		if (departure == null) {
			departure = getMaxDepartureTime();
		}
		if (departure == null) {
			if ((arrivalTime != null) && (stayDurationSec != INVALID_DURATION)) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(getArrivalTime());
				departure = new Date((cal.get(Calendar.SECOND) + getStayDuration()) * 1000);
			}
		}
		return departure;
	}

	public Date getMinDepartureTime() {
		return minDepartureTime;
	}

	public Date getMaxDepartureTime() {
		return maxDepartureTime;
	}

	public Long getStayDuration() {
		Long duration = stayDurationSec;
		if (duration == INVALID_DURATION) {
			duration = getMaxStayDuration();
		}
		if (duration == INVALID_DURATION) {
			duration = getMinStayDuration();
		}
		if (duration == INVALID_DURATION) {
			if ((getDepartureTime() != null) && (getArrivalTime() != null)) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(getArrivalTime());
				long arrivalSec = cal.get(Calendar.SECOND);
				cal.setTime(getDepartureTime());
				long departureSec = cal.get(Calendar.SECOND);
				duration = (departureSec - arrivalSec);
			}
		}
		return duration;
	}

	public Long getMinStayDuration() {
		return minStayDurationSec;
	}

	public Long getMaxStayDuration() {
		return maxStayDurationSec;
	}

	public String getArrivalTimeString() {
		String arrival = "undefined";
		if (getArrivalTime() != null) {
			arrival = DateFormat.getTimeInstance(DateFormat.SHORT).format(getArrivalTime());
		}
		return arrival;
	}

	public String getDepartureTimeString() {
		String departure = "undefined";
		if (getDepartureTime() != null) {
			departure = DateFormat.getTimeInstance(DateFormat.SHORT).format(getDepartureTime());
		}
		return departure;
	}

	public String getStayDurationClockFormat() {
		String duration = "undefined";
		if (getStayDuration() != INVALID_DURATION) {
			duration = TimeFormat.format(getStayDuration() * 1000, TimeFormat.SHORT_FORMAT, TimeFormat.MINUTES);
		}
		return duration;
	}

	public String getStayDurationLongFormat() {
		if (getStayDuration() == null) {
			Log.e("Aeon", "Invalid stay duration value, cannot produce long format");
			throw new NullPointerException();
		}

		String duration = "briefly";
		if (getStayDuration() > 0) {
			duration = TimeFormat.format(getStayDuration() * 1000, TimeFormat.LONG_FORMAT, TimeFormat.MINUTES);
		}

		return duration;
	}

	public void setArrivalTime(Date time) {
		setArrivalTime(time, false);
	}

	public void setHardArrivalTime(Date time) {
		setArrivalTime(time, true);
	}

	public void setMinArrivalTime(Date time) {
		setMinArrivalTime(time, false);
	}

	public void overrideMinArrivalTime(Date time) {
		setMinArrivalTime(time, true);
	}

	public void setMaxArrivalTime(Date time) {
		setMaxArrivalTime(time, false);
	}

	public void overrideMaxArrivalTime(Date time) {
		setMaxArrivalTime(time, true);
	}

	public void setDepartureTime(Date time) {
		setDepartureTime(time, false);
	}

	public void setHardDepartureTime(Date time) {
		setDepartureTime(time, true);
	}

	public void setMinDepartureTime(Date time) {
		setMinDepartureTime(time, false);
	}

	public void overrideMinDepartureTime(Date time, boolean override) {
		setMinDepartureTime(time, true);
	}

	public void setMaxDepartureTime(Date time) {
		setMaxDepartureTime(time, false);
	}

	public void overrideMaxDepartureTime(Date time) {
		setMaxDepartureTime(time, true);
	}

	public void setStayDuration(Long seconds) {
		setStayDuration(seconds, false);
	}

	public void setHardStayDuration(Long seconds) {
		setStayDuration(seconds, true);
	}

	public void setMinStayDuration(Long seconds) {
		setMinStayDuration(seconds, false);
	}

	public void overrideMinStayDuration(Long seconds) {
		setMinStayDuration(seconds, true);
	}

	public void setMaxStayDuration(Long seconds) {
		setMaxStayDuration(seconds, false);
	}

	public void overrideMaxStayDuration(Long seconds) {
		setMaxStayDuration(seconds, true);
	}

	private void setArrivalTime(Date time, final boolean hardDeadline) {
		if (hardDeadline) {
			minArrivalTime = time;
			maxArrivalTime = time;
		}

		if (!isArrivalTimeValid(time)) {
			throw new IllegalArgumentException();
		}

		arrivalTime = time;
	}

	private void setMinArrivalTime(Date time, boolean override) {
		try {
			// TODO: change this to test if minTime is less than max and arrival time
			if (!isDateInBounds(arrivalTime, time, maxArrivalTime)) {
				if (override) {
					arrivalTime = time;
				} else {
					throw new IllegalArgumentException();
				}
			}
		} catch (NullPointerException e) {
			// Do nothing
		}
		minArrivalTime = time;
	}

	private void setMaxArrivalTime(Date time, boolean override) {
		if (!isDateInBounds(arrivalTime, minArrivalTime, time)) {
			if (override) {
				arrivalTime = time;
			} else {
				throw new NullPointerException();
			}
		}
		maxArrivalTime = time;
	}

	private void setDepartureTime(Date time, boolean hardDeadline) {
		if (hardDeadline) {
			maxDepartureTime = time;
			minDepartureTime = time;
		}

		if (!isDepartureTimeValid(time)) {
			throw new NullPointerException();
		}

		departureTime = time;
	}

	private void setMinDepartureTime(Date time, boolean override) {
		if (!isDateInBounds(departureTime, time, maxDepartureTime)) {
			if (override) {
				departureTime = time;
			} else {
				throw new NullPointerException();
			}
		}
		minDepartureTime = time;
	}

	private void setMaxDepartureTime(Date time, boolean override) {
		if (!isDateInBounds(departureTime, minDepartureTime, time)) {
			if (override) {
				departureTime = time;
			} else {
				throw new NullPointerException();
			}
		}
		maxDepartureTime = time;
	}

	private void setStayDuration(Long seconds, boolean hardDeadline) {
		if (hardDeadline) {
			maxStayDurationSec = seconds;
			minStayDurationSec = seconds;
		}

		if (!isStayDurationValid(seconds)) {
			throw new RuntimeException();
		}

		stayDurationSec = seconds;
	}

	private void setMinStayDuration(Long seconds, boolean override) {
		if (!isDurationInBounds(stayDurationSec, seconds, maxStayDurationSec)) {
			if (override) {
				stayDurationSec = seconds;
			} else {
				throw new NullPointerException();
			}
		}
		minStayDurationSec = seconds;
	}

	private void setMaxStayDuration(Long seconds, boolean override) {
		if (!isDurationInBounds(stayDurationSec, minStayDurationSec, seconds)) {
			if (override) {
				stayDurationSec = seconds;
			} else {
				throw new NullPointerException();
			}
		}
		maxStayDurationSec = seconds;
	}

	public boolean isArrivalTime(int offset) {
		if (getArrivalTime() == null) return false;

		Calendar offsetArrivalTime = Calendar.getInstance();
		offsetArrivalTime.setTime(getArrivalTime());
		offsetArrivalTime.set(Calendar.SECOND, 0);
		offsetArrivalTime.set(Calendar.MILLISECOND, 0);
		offsetArrivalTime.add(Calendar.MINUTE, offset);

		return areSameMinute(new Date(), offsetArrivalTime.getTime());
	}

	public boolean isArrivalTime() {
		if (getArrivalTime() == null) return false;

		return areSameMinute(new Date(), getArrivalTime());
	}

	public boolean isDepartureTime() {
		if (getDepartureTime() == null) return false;

		return areSameMinute(new Date(), getDepartureTime());
	}

	static private boolean areSameMinute(Date time1, Date time2) {
		if (time1 == null || time2 == null) return false;

		Calendar minuteCalculator = Calendar.getInstance();
		minuteCalculator.setTime(time1);
		minuteCalculator.set(Calendar.SECOND, 0);
		minuteCalculator.set(Calendar.MILLISECOND, 0);

		long delta = time2.getTime() - minuteCalculator.getTime().getTime();

		return ((delta <= 60000) && (delta >= 0));
	}

	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<Schedule> CREATOR = new Parcelable.Creator<Schedule>() {
		public Schedule createFromParcel(Parcel in) {
			return new Schedule(in);
		}

		public Schedule[] newArray(int size) {
			return new Schedule[size];
		}
	};

	public final class PrivateTests {
		public boolean isDurationInBounds(Long duration, Long minDuration, Long maxDuration) {
			return Schedule.this.isDurationInBounds(duration, minDuration, maxDuration);
		}

		public boolean isDurationFlexible(Long duration, Long minDuration, Long maxDuration) {
			return Schedule.this.isDurationFlexible(duration, minDuration, maxDuration);
		}

		public boolean isDateInBounds(Date time, Date minTime, Date maxTime) {
			return Schedule.this.isDateInBounds(time, minTime, maxTime);
		}

		public boolean isDateFlexible(Date time, Date minTime, Date maxTime) {
			return Schedule.this.isDateFlexible(time, minTime, maxTime);
		}

		public Date getMinArrivalTime() {
			return Schedule.this.minArrivalTime;
		}

		public Date getMaxArrivalTime() {
			return Schedule.this.maxArrivalTime;
		}

		public Date getArrivalTime() {
			return Schedule.this.arrivalTime;
		}

		public void setMinArrivalTime(Date time) {
			Schedule.this.minArrivalTime = time;
		}

		public void setMaxArrivalTime(Date time) {
			Schedule.this.maxArrivalTime = time;
		}

		public void setArrivalTime(Date time) {
			Schedule.this.arrivalTime = time;
		}

		public Date getMinDepartureTime() {
			return Schedule.this.minDepartureTime;
		}

		public Date getMaxDepartureTime() {
			return Schedule.this.maxDepartureTime;
		}

		public Date getDepartureTime() {
			return Schedule.this.departureTime;
		}

		public void setMinDepartureTime(Date time) {
			Schedule.this.minDepartureTime = time;
		}

		public void setMaxDepartureTime(Date time) {
			Schedule.this.maxDepartureTime = time;
		}

		public void setDepartureTime(Date time) {
			Schedule.this.departureTime = time;
		}

		public Long getMinStayDuration() {
			return Schedule.this.minStayDurationSec;
		}

		public Long getMaxStayDuration() {
			return Schedule.this.maxStayDurationSec;
		}

		public Long getStayDuration() {
			return Schedule.this.stayDurationSec;
		}

		public void setMinStayDuration(Long time) {
			Schedule.this.minStayDurationSec = time;
		}

		public void setMaxStayDuration(Long time) {
			Schedule.this.maxStayDurationSec = time;
		}

		public void setStayDuration(Long time) {
			Schedule.this.stayDurationSec = time;
		}
	};
}
