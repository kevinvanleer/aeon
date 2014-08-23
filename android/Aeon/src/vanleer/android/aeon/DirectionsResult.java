package vanleer.android.aeon;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import android.location.Address;

public class DirectionsResult {

	private JSONObject rawJson = null;

	DirectionsResult(JSONObject rawJson) {
		this.rawJson = rawJson;
	}

	public int getRouteCount() {

		int routeCount = 0;

		JSONArray routeArray = getRoutes();
		if (routeArray != null || getStatus() != "OK") {
			routeCount = routeArray.size();
		}

		return routeCount;
	}

	private String getStatus() {
		return (String) rawJson.get("status");
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
		if (route == null) {
			throw new NullPointerException();
		}

		return (String) route.get("summary");
	}

	private JSONArray getLegs(JSONObject route) {
		if (route == null) {
			throw new NullPointerException();
		}

		return (JSONArray) route.get("legs");
	}

	private JSONArray getSteps(JSONObject leg) {
		if (leg == null) {
			throw new NullPointerException();
		}

		return (JSONArray) leg.get("steps");
	}

	public Address getOrigin() {
		return getOrigin((JSONObject) getRoutes().get(0));
	}

	public Address getOrigin(JSONObject route) {
		if (route == null) {
			throw new NullPointerException();
		}

		Address originAddress = new Address(null);

		JSONArray legs = getLegs(route);
		JSONObject firstLeg = (JSONObject) legs.get(0);
		JSONObject startLocation = (JSONObject) firstLeg.get("start_location");

		originAddress.setLatitude((Double) startLocation.get("lat"));
		originAddress.setLongitude((Double) startLocation.get("lng"));
		originAddress.setAddressLine(0, (String) startLocation.get("start_address"));

		return originAddress;
	}

	public Address getDestination() {
		return getDestination((JSONObject) getRoutes().get(0));
	}

	public Address getDestination(JSONObject route) {
		if (route == null) {
			throw new NullPointerException();
		}

		Address destinationAddress = new Address(null);

		JSONArray legs = getLegs(route);
		JSONObject firstLeg = (JSONObject) legs.get(legs.size() - 1);
		JSONObject endLocation = (JSONObject) firstLeg.get("end_location");

		destinationAddress.setLatitude((Double) endLocation.get("lat"));
		destinationAddress.setLongitude((Double) endLocation.get("lng"));
		destinationAddress.setAddressLine(0, (String) endLocation.get("end_address"));

		return destinationAddress;
	}

	public int getRouteDuration() {
		return getRouteDuration((JSONObject) getRoutes().get(0));
	}

	public Integer getRouteDuration(JSONObject route) {
		if (route == null) {
			throw new NullPointerException();
		}

		int duration = 0;
		JSONArray legs = getLegs(route);

		for (Object obj : legs) {
			JSONObject leg = (JSONObject) obj;
			duration += getLegDuration(leg);
		}

		return duration;
	}

	private Integer getLegDuration(JSONObject leg) {
		if (leg == null) {
			throw new NullPointerException();
		}

		return (Integer) ((JSONObject) leg.get("duration")).get("value");
	}

	public int getRouteDistance() {
		return getRouteDistance((JSONObject) getRoutes().get(0));
	}

	public Integer getRouteDistance(JSONObject route) {
		if (route == null) {
			throw new NullPointerException();
		}

		int distance = 0;
		JSONArray legs = getLegs(route);

		for (Object obj : legs) {
			JSONObject leg = (JSONObject) obj;
			distance += getLegDistance(leg);
		}

		return distance;
	}

	private Integer getLegDistance(JSONObject leg) {
		if (leg == null) {
			throw new NullPointerException();
		}

		return (Integer) ((JSONObject) leg.get("distance")).get("value");
	}
}
