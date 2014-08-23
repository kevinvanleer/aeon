package vanleer.android.aeon;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import android.location.Address;
import android.location.Location;

public class DirectionsResult {

	private JSONObject rawJson = null;

	DirectionsResult(JSONObject rawJson) {
		this.rawJson = rawJson;
	}

	public int getRouteCount() {

		int routeCount = 0;

		JSONArray routeArray = getRoutes();
		if (routeArray != null) {
			routeCount = routeArray.size();
		}

		return routeCount;
	}

	private JSONArray getRoutes() {
		if (rawJson == null) {
			throw new NullPointerException();
		}

		return (JSONArray) rawJson.get("routes");
	}

	public String getSummary() {
		return getSummary((JSONObject) getRoutes().get(0));
	}

	public String getSummary(JSONObject route) {
		return (String) route.get("summary");
	}

	private JSONArray getLegs(JSONObject route) {
		return (JSONArray) route.get("legs");
	}

	private JSONArray getSteps(JSONObject leg) {
		return (JSONArray) leg.get("steps");
	}

	public Location getOrigin() {
		return getOrigin();
	}

	public Address getOrigin(JSONObject route) {
		Address originAddress = new Address(null);

		JSONArray legs = getLegs(route);
		JSONObject firstLeg = (JSONObject) legs.get(0);
		JSONObject startLocation = (JSONObject) firstLeg.get("start_location");

		originAddress.setLatitude((Double) startLocation.get("lat"));
		originAddress.setLongitude((Double) startLocation.get("lng"));
		originAddress.setAddressLine(0, (String) startLocation.get("start_address"));

		return originAddress;
	}

	public Address getDestination(JSONObject route) {
		Address destinationAddress = new Address(null);

		JSONArray legs = getLegs(route);
		JSONObject firstLeg = (JSONObject) legs.get(legs.size() - 1);
		JSONObject endLocation = (JSONObject) firstLeg.get("end_location");

		destinationAddress.setLatitude((Double) endLocation.get("lat"));
		destinationAddress.setLongitude((Double) endLocation.get("lng"));
		destinationAddress.setAddressLine(0, (String) endLocation.get("end_address"));

		return destinationAddress;
	}

}
