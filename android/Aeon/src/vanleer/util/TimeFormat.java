package vanleer.util;

public class TimeFormat {
	public static final long HOURS = 0x1;
	public static final long MINUTES = 0x11;
	public static final long SECONDS = 0x111;
	public static final long MILLISECONDS = 0x1111;
	public static final int LONG_FORMAT = 0;
	public static final int SHORT_FORMAT = 1;

	/*public TimeFormat getInstance() {
		return new TimeFormat();
	}*/

	static public String format(final long TIME_MS, final int FORMAT, final long RESOLUTION) {
		String timeString = null;
		
		switch(FORMAT) {
		case(LONG_FORMAT):
			timeString = GetTimeLongFormat(TIME_MS, RESOLUTION);
		break;
		case(SHORT_FORMAT):
			timeString = GetTimeShortFormat(TIME_MS, RESOLUTION);
		break;
		default:
			break;
		}
		
		return timeString;
	}

	static private String GetTimeLongFormat(final long TIME_MS, final long RESOLUTION) {
		String timeString = null;

		long seconds = TIME_MS / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long minutesPast = minutes - (hours * 60);
		long secondsPast = seconds - (minutes * 60);
		long millisecondsPast = TIME_MS - (seconds * 1000);

		if((RESOLUTION & TimeFormat.HOURS) == TimeFormat.HOURS) {
			if(hours > 0) {
				timeString = hours + " hr";
			}
		}

		if((RESOLUTION & TimeFormat.MINUTES) == TimeFormat.MINUTES) {
			if(timeString != null) {
				timeString += minutesPast + " min";
			} else if(minutes > 0) {
				timeString = minutes + " min";
			}
		}

		if((RESOLUTION & TimeFormat.SECONDS) == TimeFormat.SECONDS) {
			if(timeString != null) {
				timeString += secondsPast + " sec";
			} else if(seconds > 0) {
				timeString = seconds + " sec";
			}			
		}

		if((RESOLUTION & TimeFormat.MILLISECONDS) == TimeFormat.MILLISECONDS) {
			if(timeString != null) {
				timeString += millisecondsPast + " ms";
			} else {
				timeString = TIME_MS + " ms";
			}
		}

		return timeString;
	}
	
	static private String GetTimeShortFormat(final long MILLISECONDS, final long RESOLUTION) {
		String timeString = "";

		long seconds = MILLISECONDS / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long minutesPast = minutes - (hours * 60);
		long secondsPast = seconds - (minutes * 60);
		long millisecondsPast = MILLISECONDS - (seconds * 1000);

		if((RESOLUTION & TimeFormat.HOURS) == TimeFormat.HOURS) {
			timeString += hours;
		}

		if((RESOLUTION & TimeFormat.MINUTES) == TimeFormat.MINUTES) {
			timeString += ":" + minutesPast;
		}

		if((RESOLUTION & TimeFormat.SECONDS) == TimeFormat.SECONDS) {
			timeString += ":" + secondsPast;
		}

		if((RESOLUTION & TimeFormat.MILLISECONDS) == TimeFormat.MILLISECONDS) {
			timeString += "." + millisecondsPast;
		}

		return timeString;
	}
	
	protected TimeFormat() {
	}
}
