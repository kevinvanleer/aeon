package vanleer.util;

public class TimeFormat {
	public static final long HOURS = 0x1;
	public static final long MINUTES = 0x11;
	public static final long SECONDS = 0x111;
	public static final long MILLISECONDS = 0x1111;
	public static final int LONG_FORMAT = 0;
	public static final int SHORT_FORMAT = 1;
	public static final int _12_HOUR_FORMAT = 2;
	public static final int _24_HOUR_FORMAT = 3;

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
		case(_12_HOUR_FORMAT):
			timeString = GetTime12HourFormat(TIME_MS, RESOLUTION);
		break;
		case(_24_HOUR_FORMAT):
			timeString = GetTime24HourFormat(TIME_MS, RESOLUTION);
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
			if(RESOLUTION == TimeFormat.HOURS) {
				if(minutesPast >= 30) {
					++hours;
				}
			}
			if(hours > 0) {
				timeString = hours + " hr";
			}
		}

		if((RESOLUTION & TimeFormat.MINUTES) == TimeFormat.MINUTES) {
			if(RESOLUTION == TimeFormat.MINUTES) {
				if(secondsPast >= 30) {
					++minutesPast;
					++minutes;
				}
			}
			if(timeString != null) {
				timeString += " " + minutesPast + " min";
			} else if(minutes > 0) {
				timeString = minutes + " min";
			}
		}

		if((RESOLUTION & TimeFormat.SECONDS) == TimeFormat.SECONDS) {
			if(RESOLUTION == TimeFormat.SECONDS) {
				if(millisecondsPast >= 500) {
					++secondsPast;
					++seconds;
				}
			}
			if(timeString != null) {
				timeString += " " + secondsPast + " sec";
			} else if(seconds > 0) {
				timeString = seconds + " sec";
			}			
		}

		if((RESOLUTION & TimeFormat.MILLISECONDS) == TimeFormat.MILLISECONDS) {
			if(timeString != null) {
				timeString += " " + millisecondsPast + " ms";
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
			if(RESOLUTION == TimeFormat.HOURS) {
				if(minutesPast >= 30) {
					++hours;
				}
			}
			timeString += hours;
		}

		if((RESOLUTION & TimeFormat.MINUTES) == TimeFormat.MINUTES) {
			if(RESOLUTION == TimeFormat.MINUTES) {
				if(secondsPast >= 30) {
					++minutesPast;
					++minutes;
				}
			}
			timeString += ":" + minutesPast;
		}

		if((RESOLUTION & TimeFormat.SECONDS) == TimeFormat.SECONDS) {
			if(RESOLUTION == TimeFormat.SECONDS) {
				if(millisecondsPast >= 500) {
					++secondsPast;
					++seconds;
				}
			}
			timeString += ":" + secondsPast;
		}

		if((RESOLUTION & TimeFormat.MILLISECONDS) == TimeFormat.MILLISECONDS) {
			timeString += "." + millisecondsPast;
		}

		return timeString;
	}
	
	static private String GetTime24HourFormat(final long MILLISECONDS, final long RESOLUTION) {
		String timeString = "";

		long seconds = MILLISECONDS / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long minutesPast = minutes - (hours * 60);
		long secondsPast = seconds - (minutes * 60);
		long millisecondsPast = MILLISECONDS - (seconds * 1000);

		if((RESOLUTION & TimeFormat.HOURS) == TimeFormat.HOURS) {
			if(RESOLUTION == TimeFormat.HOURS) {
				if(minutesPast >= 30) {
					++hours;
				}
			}
			timeString += (hours % 24);
		}

		if((RESOLUTION & TimeFormat.MINUTES) == TimeFormat.MINUTES) {
			if(RESOLUTION == TimeFormat.MINUTES) {
				if(secondsPast >= 30) {
					++minutesPast;
					++minutes;
				}
			}
			timeString += ":" + String.format("%02d", minutesPast);
		}

		if((RESOLUTION & TimeFormat.SECONDS) == TimeFormat.SECONDS) {
			if(RESOLUTION == TimeFormat.SECONDS) {
				if(millisecondsPast >= 500) {
					++secondsPast;
					++seconds;
				}
			}
			timeString += ":" + String.format("%02d", secondsPast);
		}

		if((RESOLUTION & TimeFormat.MILLISECONDS) == TimeFormat.MILLISECONDS) {
			timeString += "." + String.format("%03d", millisecondsPast);
		}

		return timeString;
	}
	
	static private String GetTime12HourFormat(final long MILLISECONDS, final long RESOLUTION) {
		String timeString = "";

		long seconds = MILLISECONDS / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long minutesPast = minutes - (hours * 60);
		long secondsPast = seconds - (minutes * 60);
		long millisecondsPast = MILLISECONDS - (seconds * 1000);

		if((RESOLUTION & TimeFormat.HOURS) == TimeFormat.HOURS) {
			if(RESOLUTION == TimeFormat.HOURS) {
				if(minutesPast >= 30) {
					++hours;
				}
			}
			timeString += (hours % 12);
		}

		if((RESOLUTION & TimeFormat.MINUTES) == TimeFormat.MINUTES) {
			if(RESOLUTION == TimeFormat.MINUTES) {
				if(secondsPast >= 30) {
					++minutesPast;
					++minutes;
				}
			}
			timeString += ":" + String.format("%02d", minutesPast);
		}

		if((RESOLUTION & TimeFormat.SECONDS) == TimeFormat.SECONDS) {
			if(RESOLUTION == TimeFormat.SECONDS) {
				if(millisecondsPast >= 500) {
					++secondsPast;
					++seconds;
				}
			}
			timeString += ":" + secondsPast;
		}

		if((RESOLUTION & TimeFormat.MILLISECONDS) == TimeFormat.MILLISECONDS) {
			timeString += "." + String.format("%03d", millisecondsPast);
		}

		return timeString;
	}
	
	protected TimeFormat() {
	}
}
