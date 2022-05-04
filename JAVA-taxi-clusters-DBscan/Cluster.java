

import java.util.ArrayList;

public class Cluster {
  private ArrayList<TripRecord> clusterStorage;

  private int size;

  Cluster() {
    clusterStorage = new ArrayList<>();
    size = 0;
  }

  public void add(TripRecord t) {
    clusterStorage.add(t);
    size++;
  }

  public int getClusterID() {
    return clusterStorage.get(0).getClusterID();
  }

  public int getSize() {
    return size;
  }

  public double getLong(int clusterLocation) {
    return clusterStorage.get(clusterLocation).getPickUpLocation().getLongitude();
  }

  public double getLat(int clusterLocation) {
    return clusterStorage.get(clusterLocation).getPickUpLocation().getLatitude();
  }

}
