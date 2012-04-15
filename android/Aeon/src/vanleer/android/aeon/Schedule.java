package vanleer.android.aeon;

import java.text.DateFormat;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

import vanleer.util.TimeFormat;

public class Schedule implements Parcelable{
	private Date minArrivalTime = null;
	private Date maxArrivalTime = null;
	private Date arrivalTime = null;
	private Date minDepartureTime = null;
	private Date maxDepartureTime = null;
	private Date departureTime = null;
	private Long minStayDurationSec = null;
	private Long maxStayDurationSec = null;
	private Long stayDurationSec = null;
	
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
		if(isArrivalTimeFlexible() ? (isDepartureTimeFlexible() || isStayDurationFlexible()) :
			(isDepartureTimeFlexible() && isStayDurationFlexible())) {
			constrained = (stayDurationSec == (departureTime.getTime() - arrivalTime.getTime()));
		} else {
			constrained = (getMaxStayDuration() <=
					getMaxDepartureTime().getTime() - getMinArrivalTime().getTime());
			constrained &= (getMaxStayDuration() >=
					getMinDepartureTime().getTime() - getMaxArrivalTime().getTime());
			constrained &= (getMinStayDuration() <=
					getMaxDepartureTime().getTime() - getMinArrivalTime().getTime());
			constrained &= (getMinStayDuration() >=
					getMinDepartureTime().getTime() - getMaxArrivalTime().getTime());
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
		return !((time == minTime) && (time == maxTime));
	}

	public boolean isDurationFlexible(Long duration, Long minDuration, Long maxDuration) {
		return !((duration == minDuration) && (duration == maxDuration));
	}
	
	public boolean isArrivalTimeValid(Date time) {
		return isArrivalTimeFlexible() ? isDateInBounds(time, minArrivalTime, maxArrivalTime) :
			(time == arrivalTime);
	}
	
	public boolean isArrivalTimeValid() {
		return isArrivalTimeValid(arrivalTime);
	}
	
	public boolean isDepartureTimeValid(Date time) {
		return isDepartureTimeFlexible() ? isDateInBounds(time, minDepartureTime, maxDepartureTime) :
			(time == departureTime);
	}
	
	public boolean isDepartureTimeValid() {
		return isDepartureTimeValid(departureTime);
	}
	
	public boolean isStayDurationValid(Long duration) {
		return isStayDurationFlexible() ?
				isDurationInBounds(duration, minStayDurationSec, maxStayDurationSec) :
					(duration == stayDurationSec);
	}
	
	public boolean isStayDurationValid() {
		return isStayDurationValid(stayDurationSec);
	}
	
	public boolean validate() {
		boolean valid = true;
		valid &= isArrivalTimeValid();
		valid &= isDepartureTimeValid();
		valid &= isStayDurationValid();
		valid &= areTimesConstrained();
		
		return valid;
	}

	private boolean isDateInBounds(Date time, Date minTime, Date maxTime) {
		if(time == null) {
			throw new IllegalArgumentException();
		}
		
		boolean inBounds = true;
		if(minTime != null) {
			inBounds = time.after(minTime);
		}
		if(inBounds && (maxTime != null)) {
			inBounds = time.before(maxTime);
		}
		return inBounds;
	}
	
	private boolean isDurationInBounds(Long duration, Long minDuration, Long maxDuration) {
		if(duration == null) {
			throw new IllegalArgumentException();
		}
		
		boolean inBounds = true;
		if(minDuration != null) {
			inBounds = duration >= minDuration;
		}
		if(inBounds && (maxDuration != null)) {
			inBounds = duration <= maxDuration;
		}
		return inBounds;
	}
	
	public Date getArrivalTime() {
		Date arrival = arrivalTime;
		if(arrival == null) {
			arrival = getMaxArrivalTime();
		}
		if(arrival == null) {
			arrival = getMinArrivalTime();
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
		if(departure == null) {
			departure = getMinDepartureTime();
		}
		if(departure == null) {
			departure = getMaxDepartureTime();
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
		if(duration == null) {
			duration = getMaxStayDuration();
		}
		if(duration == null) {
			duration = getMinStayDuration();
		}
		if((duration == null) && (getDepartureTime() != null) && (getArrivalTime() != null)) {
			duration = (getDepartureTime().getTime() - getArrivalTime().getTime());
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
		if(getArrivalTime() != null) {
			arrival = DateFormat.getTimeInstance(DateFormat.SHORT).format(getArrivalTime());
		}
		return arrival;
	}

	public String getDepartureTimeString() {
		String departure = "undefined";
		if(getDepartureTime() != null) {
			departure = DateFormat.getTimeInstance(DateFormat.SHORT).format(getDepartureTime());
		}
		return departure;
	}

	public String getStayDurationClockFormat() {
		String duration = "undefined";
		if(getStayDuration() != null) {
			duration = TimeFormat.format(getStayDuration() * 1000, TimeFormat.SHORT_FORMAT, TimeFormat.MINUTES);
		}
		return duration;
	}

	public String getStayDurationLongFormat() {
		return TimeFormat.format(getStayDuration() * 1000, TimeFormat.LONG_FORMAT, TimeFormat.MINUTES);
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
		if(hardDeadline) {
			minArrivalTime = time;
			maxArrivalTime = time;
		} 

		if(!isArrivalTimeValid(time)) {
			throw new IllegalArgumentException();
		}

		arrivalTime = time;
	}

	private void setMinArrivalTime(Date time, boolean override) {
		if(!isDateInBounds(arrivalTime, time, maxArrivalTime)) {
			if(override) {
				arrivalTime = time;
			} else {
				throw new IllegalArgumentException();
			}
		}
		minArrivalTime = time;
	}

	private void setMaxArrivalTime(Date time, boolean override) {
		if(!isDateInBounds(arrivalTime, minArrivalTime, time)) {
			if(override) {
				arrivalTime = time;
			} else {
				throw new IllegalArgumentException();
			}
		}
		maxArrivalTime = time;
	}

	private void setDepartureTime(Date time, boolean hardDeadline) {
		if(hardDeadline) {
			maxDepartureTime = time;
			minDepartureTime = time;
		}
		
		if(!isDepartureTimeValid(time)) {
			throw new IllegalArgumentException();
		}

		departureTime = time;
	}

	private void setMinDepartureTime(Date time, boolean override) {
		if(!isDateInBounds(departureTime, time, maxDepartureTime)) {
			if(override) {
				departureTime = time;
			} else {
				throw new IllegalArgumentException();
			}
		}
		minDepartureTime = time;
	}

	private void setMaxDepartureTime(Date time, boolean override) {
		if(!isDateInBounds(departureTime, minDepartureTime, time)) {
			if(override) {
				departureTime = time;
			} else {
				throw new IllegalArgumentException();
			}
		}
		maxDepartureTime = time;
	}

	private void setStayDuration(Long seconds, boolean hardDeadline) {
		if(hardDeadline) {
			maxStayDurationSec = seconds;
			minStayDurationSec = seconds;
		}

		if(!isStayDurationValid(seconds)) {
			throw new IllegalArgumentException();
		}

		stayDurationSec = seconds;
	}
	
	private void setMinStayDuration(Long seconds, boolean override) {
		if(!isDurationInBounds(stayDurationSec, seconds, maxStayDurationSec)) {
			if(override) {
				stayDurationSec = seconds;
			} else {
				throw new IllegalArgumentException();
			}
		}
		minStayDurationSec = seconds;
	}
	
	private void setMaxStayDuration(Long seconds, boolean override) {
		if(!isDurationInBounds(stayDurationSec, minStayDurationSec, seconds)) {
			if(override) {
				stayDurationSec = seconds;
			} else {
				throw new IllegalArgumentException();
			}
		}
		maxStayDurationSec = seconds;
	}

	public int describeContents() {
		return 0;
	}
}
