
public class TripRecord {
	private String pickup_DateTime;
	private GPScoord pickup_Location;
	private GPScoord dropoff_Location;
	private float trip_Distance;
	private int clusterID;
	private boolean isVisited;
	private boolean isNoise;

	TripRecord(String pD, GPScoord pL, GPScoord dL, float tD) {
		pickup_DateTime = pD;
		pickup_Location = pL;
		dropoff_Location = dL;
		trip_Distance = tD;
		clusterID = 0;
		isVisited = false;
		isNoise = false;
	}

	public boolean getIsVisited() {
		return isVisited;
	}

	public boolean getIsNoise() {
		return isNoise;
	}

	public int getClusterID() {
		return clusterID;
	}

	public void setClusterID(int iD) {
		clusterID = iD;
	}

	public void setIsVisited(boolean set) {
		isVisited = set;
	}

	public void setIsNoise(boolean set) {
		isNoise = set;
	}

	public String getPickUpDateTime() {
		return pickup_DateTime;
	}

	public GPScoord getPickUpLocation() {
		return pickup_Location;
	}

	public GPScoord getDropOffLocation() {
		return dropoff_Location;
	}

	public float getTripDistance() {
		return trip_Distance;
	}

}
