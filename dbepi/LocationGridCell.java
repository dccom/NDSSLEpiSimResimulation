package dbepi;

import java.sql.*;
import java.util.*;

public class LocationGridCell {
//	private double xmin;
//	private double xmax;
//	private double ymin;
//	private double ymax;
//	private ArrayList<Integer> locationIDs;
	private HashSet<Integer> locationIDs;
	private ArrayList<LocationGridCell> neighbors;
	private HierarchGridCluster myCluster;
	private HashMap<LocationGridCell, Integer> numInterEdges;
	
	public LocationGridCell(HashSet<Integer> locationIDs){	//double xmin, double xmax, double ymin, double ymax, 
//		this.xmin = xmin;
//		this.xmax = xmax;
//		this.ymin = ymin;
//		this.ymax = ymax;
		this.locationIDs = locationIDs;
		this.neighbors = null;
		this.myCluster = null;
		this.numInterEdges = new HashMap<LocationGridCell, Integer>();
	}
	
	public HashSet<Integer> getLocationIDs(){
		return this.locationIDs;
	}
	
	public Iterator<LocationGridCell> neighborIterator(){
		return this.neighbors.iterator();
	}
	
	public void setNeighbors(ArrayList<LocationGridCell> neighbors){
		this.neighbors = neighbors;
	}
	
	public ArrayList<LocationGridCell> getNeighbors(){
		return this.neighbors;
	}
	
	public HierarchGridCluster getCluster(){
		return this.myCluster;
	}
	
	public void setCluster(HierarchGridCluster cluster){
		this.myCluster = cluster;
	}
	
	public int numLocations(){
		return locationIDs.size();
	}
	
	public Iterator<Integer> getLocationIdItr(){
		return this.locationIDs.iterator();
	}
	
	public boolean containsLocation(Integer location){
		return this.locationIDs.contains(location);
	}
	
	public int getNumInterEdges(LocationGridCell other){
		if (this.numInterEdges.containsKey(other)){
			return this.numInterEdges.get(other).intValue();
		} else {
			try {
				int count = 0;
				Connection con = EpiSimUtil.dbConnect();
				PreparedStatement getEdgeTally = con.prepareStatement(
						"SELECT tally,location2ID FROM "+EpiSimUtil.llInterCountTbl+" WHERE location1ID = ?");
				Iterator<Integer> myLocs = this.locationIDs.iterator();
				while (myLocs.hasNext()){
					int loc1 = myLocs.next().intValue();
					getEdgeTally.setInt(1, loc1);
					ResultSet getEdgeTallyQ = getEdgeTally.executeQuery();
					while (getEdgeTallyQ.next()){
						Integer location2 = new Integer(getEdgeTallyQ.getInt("location2ID"));
						if (other.containsLocation(location2)){
							count += getEdgeTallyQ.getInt("tally");
						}
					}
				}

				con.close();
				this.numInterEdges.put(other, count);
				return count;
			} catch (Exception e) {
				System.out.println(e);
				return -1;
			}
		}
	}

}
