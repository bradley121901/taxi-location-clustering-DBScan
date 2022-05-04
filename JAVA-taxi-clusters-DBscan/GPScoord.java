public class GPScoord {
	private double longitude;
	private double latitude;

	GPScoord(double x, double y) {
		this.longitude = x;
		this.latitude = y;
	}

	GPScoord() {
	}

	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}
}
