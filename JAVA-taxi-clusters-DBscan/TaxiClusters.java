

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TaxiClusters {

  // distance function method
  public double euclidean(TripRecord Q, TripRecord P) {

    return Math.sqrt(Math.pow(Q.getPickUpLocation().getLatitude() - P.getPickUpLocation().getLatitude(), 2)
        + Math.pow(Q.getPickUpLocation().getLongitude() - P.getPickUpLocation().getLongitude(), 2));
  }

  // rangequery to find neighboring points within range of eps
  // found through psuedocode given in assignment
  public ArrayList<TripRecord> RangeQuery(ArrayList<TripRecord> db, TripRecord Q, double eps) {
    ArrayList<TripRecord> neighbors = new ArrayList<>();
    int length = db.size();
    for (int i = 0; i < length; i++) {
      if (euclidean(Q, db.get(i)) <= eps && Q != db.get(i)) {
        neighbors.add(db.get(i));
      }
    }
    return neighbors;
  }

  public static void main(String args[]) throws FileNotFoundException {
    double eps = 0.0001;
    int minPts = 5;
    TaxiClusters tC = new TaxiClusters();
    int count = 0;
    int valid = 0;
    String pickupDateTime = "";
    GPScoord pickupLocation = new GPScoord();
    GPScoord dropoffLocation = new GPScoord();
    double lon = 0;
    double lat = 0;
    float tripDistance = 0;
    ArrayList<TripRecord> db = new ArrayList<>();
    Scanner in = new Scanner(System.in);
    String filename;

    System.out.println("Enter minPts: ");
    minPts = in.nextInt();
    System.out.println("Enter eps: ");
    eps = in.nextDouble();
    System.out.println("Enter filename (Leave blank for:'yellow_tripdata_2009-01-15_1hour_clean.csv')");
    filename = in.nextLine();

    if (filename.equals("")) {
      filename = "yellow_tripdata_2009-01-15_1hour_clean.csv";
    }

    File readFile = new File(filename);
    Scanner sc = new Scanner(readFile);
    sc.useDelimiter(",");
    while (sc.hasNext()) { // while loop that goes through csv files to process strings separated by commas
      valid++;
      if (valid >= 20) {

        count++;
        // count keeps track of which column information is being read
        if (count == 5) {
          pickupDateTime = sc.next();
        } else if (count == 8) {
          tripDistance = Float.parseFloat(sc.next());
        } else if (count == 9) {
          lon = Double.parseDouble(sc.next());
        } else if (count == 10) {
          lat = Double.parseDouble(sc.next());
          pickupLocation = new GPScoord(lon, lat);
        } else if (count == 13) {
          lon = Double.parseDouble(sc.next());
        } else if (count == 14) {
          lat = Double.parseDouble(sc.next());
          dropoffLocation = new GPScoord(lon, lat);
          db.add(new TripRecord(pickupDateTime, pickupLocation, dropoffLocation, tripDistance));// adds new trip record
                                                                                                // into arraylist
        } else if (count == 21) {
          count = 0;
        } else {
          sc.next();
        }
      } else {
        sc.next();
      }

    }

    ArrayList<Cluster> clusters = new ArrayList<>();
    int counter = 0;
    int length = db.size();
    for (int n = 0; n < length; n++) {
      if (db.get(n).getIsVisited()) {
        continue;
      }
      ArrayList<TripRecord> N = tC.RangeQuery(db, db.get(n), eps);
      if (N.size() < minPts) {
        db.get(n).setIsVisited(true);
        db.get(n).setIsNoise(true);
        continue;
      }
      counter++;
      Cluster c = new Cluster();
      db.get(n).setIsVisited(true);
      db.get(n).setClusterID(counter);
      ;
      c.add(db.get(n));

      ArrayList<TripRecord> seedSet = N;

      for (int j = 0; j < seedSet.size(); j++) {

        if (seedSet.get(j).getIsNoise()) {
          seedSet.get(j).setClusterID(counter);
          seedSet.get(j).setIsNoise(false);
          c.add(seedSet.get(j));
        }
        if (seedSet.get(j).getIsVisited()) {
          continue;
        }

        seedSet.get(j).setClusterID(counter);
        seedSet.get(j).setIsVisited(true);
        c.add(seedSet.get(j));

        ArrayList<TripRecord> B = tC.RangeQuery(db, seedSet.get(j), eps);
        if (B.size() >= minPts) {
          for (int k = 0; k < B.size(); k++) {
            if (!seedSet.contains(B.get(k))) {
              seedSet.add(B.get(k));
            }

          }
        }

      }
      clusters.add(c);

    }

    // writes to output file
    try (PrintWriter writer = new PrintWriter(new File("output.csv"))) {
      double avgLong = 0;
      double avgLat = 0;
      StringBuilder sb = new StringBuilder();
      sb.append("Cluster Id");
      sb.append(",");
      sb.append("Longitude");
      sb.append(",");
      sb.append("Latitude");
      sb.append(",");
      sb.append("Number of points");
      sb.append("\n");

      for (int a = 0; a < clusters.size(); a++) {
        avgLong = 0;// intialize variables
        avgLat = 0;// intialize variables
        for (int z = 0; z < clusters.get(a).getSize(); z++) {// calculates total longitude and latitude
          avgLong += clusters.get(a).getLong(z);
          avgLat += clusters.get(a).getLat(z);
        }

        sb.append(clusters.get(a).getClusterID());
        sb.append(",");
        sb.append(avgLong / clusters.get(a).getSize());// divides total longitude with number of trips in cluster to get
                                                       // average
        sb.append(",");
        sb.append(avgLat / clusters.get(a).getSize());// divides total latitude with number of trips in cluster to get
                                                      // average
        sb.append(",");
        sb.append(clusters.get(a).getSize());
        sb.append("\n");
      }

      writer.write(sb.toString());
      writer.close();
    } catch (FileNotFoundException e) {
      System.out.println(e.getMessage());
    }

    sc.close();

  }
}
