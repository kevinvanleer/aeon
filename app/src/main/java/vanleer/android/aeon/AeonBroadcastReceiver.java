package vanleer.android.aeon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AeonBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context theContext, Intent theIntent) {
		if (theIntent.getAction().equals("vanleer.android.aeon.delay_departure")) {
			Object theExtra = theIntent.getExtras().get("destination");
			if (theExtra != null) {
				theIntent.setClass(theContext, Itinerary.class);
				theIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				theContext.startActivity(theIntent);
			}
		}
	}
}
