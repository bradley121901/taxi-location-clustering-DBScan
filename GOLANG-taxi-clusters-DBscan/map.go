package main

import "fmt"
import "time"
import "runtime"
import "os"
import "io"
import "strconv"
import "encoding/csv"
import "sync"
import "math"


type GPScoord struct {
  lat float64
	long float64
}

type LabelledGPScoord struct {
  GPScoord
	ID int     // point ID
	Label int  // cluster ID
  isVisited int
  isNoise int
}

type Job struct { //struct to store jobs
  ID int
	coords []*LabelledGPScoord
	MinPts int
	eps    float64
	offset int
}

const N int= 4
const numConsumerThreads int= 10 //changes number of consumer threads
const MinPts int=5
const eps float64= 0.0003
const filename string="yellow_tripdata_2009-01-15_9h_21h_clean.csv"

func main() {

    start := time.Now();

    gps, minPt, maxPt := readCSVFile(filename)
	fmt.Printf("Number of points: %d\n", len(gps))

	minPt = GPScoord{40.7, -74.}
	maxPt = GPScoord{40.8, -73.93}

	// geographical limits
	fmt.Printf("SW:(%f , %f)\n", minPt.lat, minPt.long)
	fmt.Printf("NE:(%f , %f) \n\n", maxPt.lat, maxPt.long)

	// Parallel DBSCAN
	incx := (maxPt.long-minPt.long)/float64(N)
	incy := (maxPt.lat-minPt.lat)/float64(N)

	var grid [N][N][]*LabelledGPScoord  // a grid of GPScoord slices

	// Create the partition
	// triple loop! not very efficient, but easier to understand

	partitionSize:=0
    for j:=0; j<N; j++ {
        for i:=0; i<N; i++ {

		    for k:=0; k<len(gps);k++{
          pt := gps[k]

			    if (pt.long >= minPt.long+float64(i)*incx-eps) && (pt.long < minPt.long+float64(i+1)*incx+eps) && (pt.lat >= minPt.lat+float64(j)*incy-eps) && (pt.lat < minPt.lat+float64(j+1)*incy+eps) {

                    grid[i][j]= append(grid[i][j], &pt)//changed to pass by reference
					partitionSize++;
                }
			}
	    }
	}


  jobs := make(chan Job) //make channel to process jobs
	runtime.GOMAXPROCS(-1)
  go producer(jobs, grid) //produce jobs
	var mutex sync.WaitGroup
	mutex.Add(numConsumerThreads)

	for i := 0; i < numConsumerThreads; i++ {
		go consumer(jobs, &mutex) //consume jobs
	}

	mutex.Wait()


	// merge clusters
	end := time.Now();
    fmt.Printf("\nExecution time: %s of %d points\n", end.Sub(start), partitionSize)
    fmt.Printf("Number of CPUs: %d", runtime.NumCPU())
}

func producer(jobs chan Job, grid[N][N][]*LabelledGPScoord){ //produce jobs, implementation taken from prodcons.go
  ID := 0;
  for j := 0; j < N; j++ {
    for i := 0; i < N; i++{
      ID += 1
      jobs <- Job{ID, grid[i][j], MinPts, eps, i*10000000+j*1000000}
    }
  }
  close(jobs)
}

func consumer(jobs chan Job, done *sync.WaitGroup) { //consume jobs, implementation taken from prodcons.go
	for {
		job, more := <-jobs
		if more {
			dbscan(job.MinPts, job.eps,job.coords, job.offset)
		} else {
			done.Done()
			return
		}
	}
}

// Applies DBSCAN algorithm on LabelledGPScoord points
// LabelledGPScoord: the slice of LabelledGPScoord points
// MinPts, eps: parameters for the DBSCAN algorithm
// offset: label of first cluster (also used to identify the cluster)
// returns number of clusters found
//dbscan implementation taken from https://en.wikipedia.org/wiki/DBSCAN
func dbscan( MinPts int, eps float64, coords []*LabelledGPScoord, offset int) (nclusters int) {

	nclusters = 0 //cluster counter
  coordLength := len(coords)
	for k:= 0; k < coordLength; k++{
    x := coords[k]
		if x.isVisited == 1{
			continue
		}

		N := rangeQuery(eps, coords, x)

		if len(N) < MinPts {
			x.isVisited = 1
      x.isNoise = 1
			continue
		}

    seedSet := N
    x.isVisited = 1
		nclusters++
		x.Label = offset + nclusters

    for i:=0; i< len(seedSet);i++ {
      y :=  seedSet[i]
			if y.isNoise == 1 {
        y.Label = offset + nclusters
        y.isNoise = 0
			}
			if y.isVisited == 1 {
				continue
			}

      y.isVisited = 1
			y.Label = offset + nclusters

			findNeighbours := rangeQuery(eps,coords, y)
      neighbourLength := len(findNeighbours)
			if neighbourLength >= MinPts {

        for j:= 0; j < neighbourLength; j++{
          seed := findNeighbours[j]
          seedSet = append(seedSet, seed)

        }

			}

		}

	}
	// End of DBscan function
	// Printing the result (do not remove)
	fmt.Printf("Cluster   %10d : [%4d,%6d]\n", offset, nclusters, len(coords))
	return nclusters
}
//rangequery implementation taken from https://en.wikipedia.org/wiki/DBSCAN
func rangeQuery( eps float64, coord []*LabelledGPScoord, Q*LabelledGPScoord) []*LabelledGPScoord{

	NeighborsN := make([]*LabelledGPScoord, 0)
  coordLength := len(coord)
	for n:=0; n< coordLength; n++{
    P := coord[n]
    distance := math.Sqrt(math.Pow(P.long-Q.long, 2) + math.Pow(P.lat-Q.lat, 2))//distance function

      	if  distance <= eps && Q.ID != P.ID{ //and check no coord duplicates
    			NeighborsN = append(NeighborsN, P)
        }

    		}

	return NeighborsN
}


// reads a csv file of trip records and returns a slice of the LabelledGPScoord of the pickup locations
// and the minimum and maximum GPS coordinates
func readCSVFile(filename string) (coords []LabelledGPScoord, minPt GPScoord, maxPt GPScoord) {

    coords= make([]LabelledGPScoord, 0, 5000)

    // open csv file
    src, err := os.Open(filename)
	defer src.Close()
    if err != nil {
        panic("File not found...")
    }

	// read and skip first line
    r := csv.NewReader(src)
    record, err := r.Read()
    if err != nil {
        panic("Empty file...")
    }

    minPt.long = 1000000.
    minPt.lat = 1000000.
    maxPt.long = -1000000.
    maxPt.lat = -1000000.

	var n int=0

    for {
        // read line
        record, err = r.Read()

        // end of file?
        if err == io.EOF {
            break
        }

        if err != nil {
             panic("Invalid file format...")
        }

		// get lattitude
		lat, err := strconv.ParseFloat(record[9], 64)
        if err != nil {
             panic("Data format error (lat)...")
        }

        // is corner point?
		if lat>maxPt.lat {
		    maxPt.lat= lat
		}
		if lat<minPt.lat {
		    minPt.lat= lat
		}

		// get longitude
		long, err := strconv.ParseFloat(record[8], 64)
        if err != nil {
             panic("Data format error (long)...")
        }

        // is corner point?
		if long>maxPt.long {
		    maxPt.long= long
		}

		if long<minPt.long {
		    minPt.long= long
		}

        // add point to the slice
		n++
        pt:= GPScoord{lat,long}
        coords = append(coords, LabelledGPScoord{pt,n,0,0,0})//changed to include cluster ID
    }

    return coords, minPt,maxPt
}
