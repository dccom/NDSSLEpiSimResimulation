package dbepi;

import java.sql.*;
import java.util.*;
import java.io.*;

public class HierarchGridClusterWorld {
	private static final String clusterDir = "/net/cbk/dc/EpiSim/generatedData/cluster/";
	private static int nextClusterId = 0;
	private LinkedList<HierarchGridCluster> allClusters;
	private TreeSet<ClusterPair> mergeScores;
	
	public HierarchGridClusterWorld(){
		this.allClusters = new LinkedList<HierarchGridCluster>();
		this.mergeScores = new TreeSet<ClusterPair>();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		test();
		mergeMulti(100, 100);
		
	}
	
	public static void mergeMulti(int dimension, int numClusters){
		System.out.println("Starting grid: "+dimension+" by "+dimension+
				"; final number of clusters: "+numClusters);
		HierarchGridClusterWorld world = new HierarchGridClusterWorld();
//		int dimension = 2;
		LocationGridCell[][] grid = makeGrid(dimension);
		for (int i = 0; i < grid.length; i++){
			for (int j = 0; j < grid[i].length; j++){
				ArrayList<LocationGridCell> neighbors = new ArrayList<LocationGridCell>(8);
				if (i == 0){
					if (j == 0){
						neighbors.add(grid[i][j+1]);
						neighbors.add(grid[i+1][j]);
						neighbors.add(grid[i+1][j+1]);
					} else if (j == grid[i].length - 1){
						neighbors.add(grid[i][j-1]);
						neighbors.add(grid[i+1][j]);
						neighbors.add(grid[i+1][j-1]);
					} else {
						neighbors.add(grid[i][j-1]);
						neighbors.add(grid[i][j+1]);
						neighbors.add(grid[i+1][j-1]);
						neighbors.add(grid[i+1][j]);
						neighbors.add(grid[i+1][j+1]);
					}
				} else if (i == grid.length - 1){
					if (j == 0){
						neighbors.add(grid[i][j+1]);
						neighbors.add(grid[i-1][j]);
						neighbors.add(grid[i-1][j+1]);
					} else if (j == grid[i].length - 1){
						neighbors.add(grid[i][j-1]);
						neighbors.add(grid[i-1][j]);
						neighbors.add(grid[i-1][j-1]);
					} else {
						neighbors.add(grid[i][j-1]);
						neighbors.add(grid[i][j+1]);
						neighbors.add(grid[i-1][j-1]);
						neighbors.add(grid[i-1][j]);
						neighbors.add(grid[i-1][j+1]);
					}
				} else {
					if (j == 0){
						neighbors.add(grid[i][j+1]);
						neighbors.add(grid[i-1][j]);
						neighbors.add(grid[i-1][j+1]);
						neighbors.add(grid[i+1][j]);
						neighbors.add(grid[i+1][j+1]);
					} else if (j == grid[i].length - 1){
						neighbors.add(grid[i][j-1]);
						neighbors.add(grid[i-1][j]);
						neighbors.add(grid[i-1][j-1]);
						neighbors.add(grid[i+1][j]);
						neighbors.add(grid[i+1][j-1]);
					} else {
						neighbors.add(grid[i][j-1]);
						neighbors.add(grid[i][j+1]);
						neighbors.add(grid[i-1][j]);
						neighbors.add(grid[i-1][j-1]);
						neighbors.add(grid[i-1][j+1]);
						neighbors.add(grid[i+1][j]);
						neighbors.add(grid[i+1][j-1]);
						neighbors.add(grid[i+1][j+1]);
					}
				}
				grid[i][j].setNeighbors(neighbors);
				ArrayList<LocationGridCell> cells = new ArrayList<LocationGridCell>();
				cells.add(grid[i][j]);
				HierarchGridCluster newCluster = new HierarchGridCluster(cells,getNextClusterId());
				grid[i][j].setCluster(newCluster);
				world.allClusters.add(newCluster);
			}
		}
		while (world.allClusters.size() > numClusters){
			world.mergeMax();
		}
		try {
			Connection con = EpiSimUtil.dbConnect();
			PreparedStatement getXY = con.prepareStatement(
					"SELECT x,y FROM "+EpiSimUtil.locTbl+" WHERE locationID = ?");
			Iterator<HierarchGridCluster> clusterItr = world.allClusters.iterator();
			while (clusterItr.hasNext()){
				HierarchGridCluster cluster = clusterItr.next();
				try {
					FileWriter fw = new FileWriter(clusterDir+"cluster"+cluster.getId());
					Iterator<Integer> locationItr = cluster.getLocationIdItr();
					while (locationItr.hasNext()){
						int x,y;
						getXY.setInt(1, locationItr.next().intValue());
						ResultSet getXYQ = getXY.executeQuery();
						if(getXYQ.first()){
							x = getXYQ.getInt("x");
							y = getXYQ.getInt("y");
							fw.write(x+"\t"+y+"\n");
						}
					}
					fw.close();
				} catch (Exception e){
					System.out.println(e);
				}
			}
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static void test(){
		HierarchGridClusterWorld world = new HierarchGridClusterWorld();
		int dimension = 2;
		LocationGridCell[][] grid = makeGrid(dimension);
		for (int i = 0; i < grid.length; i++){
			for (int j = 0; j < grid[i].length; j++){
				ArrayList<LocationGridCell> neighbors = new ArrayList<LocationGridCell>(8);
				if (i == 0){
					if (j == 0){
						neighbors.add(grid[i][j+1]);
						neighbors.add(grid[i+1][j]);
						neighbors.add(grid[i+1][j+1]);
					} else if (j == grid[i].length - 1){
						neighbors.add(grid[i][j-1]);
						neighbors.add(grid[i+1][j]);
						neighbors.add(grid[i+1][j-1]);
					} else {
						neighbors.add(grid[i][j-1]);
						neighbors.add(grid[i][j+1]);
						neighbors.add(grid[i+1][j-1]);
						neighbors.add(grid[i+1][j]);
						neighbors.add(grid[i+1][j+1]);
					}
				} else if (i == grid.length - 1){
					if (j == 0){
						neighbors.add(grid[i][j+1]);
						neighbors.add(grid[i-1][j]);
						neighbors.add(grid[i-1][j+1]);
					} else if (j == grid[i].length - 1){
						neighbors.add(grid[i][j-1]);
						neighbors.add(grid[i-1][j]);
						neighbors.add(grid[i-1][j-1]);
					} else {
						neighbors.add(grid[i][j-1]);
						neighbors.add(grid[i][j+1]);
						neighbors.add(grid[i-1][j-1]);
						neighbors.add(grid[i-1][j]);
						neighbors.add(grid[i-1][j+1]);
					}
				} else {
					if (j == 0){
						neighbors.add(grid[i][j+1]);
						neighbors.add(grid[i-1][j]);
						neighbors.add(grid[i-1][j+1]);
						neighbors.add(grid[i+1][j]);
						neighbors.add(grid[i+1][j+1]);
					} else if (j == grid[i].length - 1){
						neighbors.add(grid[i][j-1]);
						neighbors.add(grid[i-1][j]);
						neighbors.add(grid[i-1][j-1]);
						neighbors.add(grid[i+1][j]);
						neighbors.add(grid[i+1][j-1]);
					} else {
						neighbors.add(grid[i][j-1]);
						neighbors.add(grid[i][j+1]);
						neighbors.add(grid[i-1][j]);
						neighbors.add(grid[i-1][j-1]);
						neighbors.add(grid[i-1][j+1]);
						neighbors.add(grid[i+1][j]);
						neighbors.add(grid[i+1][j-1]);
						neighbors.add(grid[i+1][j+1]);
					}
				}
				grid[i][j].setNeighbors(neighbors);
				ArrayList<LocationGridCell> cells = new ArrayList<LocationGridCell>();
				cells.add(grid[i][j]);
				HierarchGridCluster newCluster = new HierarchGridCluster(cells,getNextClusterId());
				grid[i][j].setCluster(newCluster);
				world.allClusters.add(newCluster);
			}
		}
		world.mergeMax();
		try {
			Connection con = EpiSimUtil.dbConnect();
			PreparedStatement getXY = con.prepareStatement(
					"SELECT x,y FROM "+EpiSimUtil.locTbl+" WHERE locationID = ?");
			Iterator<HierarchGridCluster> clusterItr = world.allClusters.iterator();
			while (clusterItr.hasNext()){
				HierarchGridCluster cluster = clusterItr.next();
				try {
					FileWriter fw = new FileWriter(clusterDir+"cluster"+cluster.getId());
					Iterator<Integer> locationItr = cluster.getLocationIdItr();
					while (locationItr.hasNext()){
						int x,y;
						getXY.setInt(1, locationItr.next().intValue());
						ResultSet getXYQ = getXY.executeQuery();
						if(getXYQ.first()){
							x = getXYQ.getInt("x");
							y = getXYQ.getInt("y");
							fw.write(x+"\t"+y+"\n");
						}
					}
					fw.close();
				} catch (Exception e){
					System.out.println(e);
				}
			}
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public void mergeMax(){
		ClusterPair maxPair = null;
		HierarchGridCluster cur1, cur2;
		Iterator<HierarchGridCluster> allClusterItr = allClusters.iterator();
		while (allClusterItr.hasNext()){
			cur1 = allClusterItr.next();
			Iterator<HierarchGridCluster> clusterNeighborItr = cur1.neighborItr();
			while (clusterNeighborItr.hasNext()){
				cur2 = clusterNeighborItr.next();
				ClusterPair pair = getClusterPair(cur1, cur2);
				if (maxPair == null){
					maxPair = pair;
				} else if (maxPair.score < pair.score){
					maxPair = pair;
				}
			}
		}
		if (maxPair != null){
			this.allClusters.remove(maxPair.cluster2);
			maxPair.cluster1.mergeCluster(maxPair.cluster2);
		}
	}
	
	public ClusterPair getClusterPair(HierarchGridCluster cluster1, HierarchGridCluster cluster2){
		HierarchGridCluster bigCluster, smallCluster;
		if (cluster1.compareTo(cluster2) >= 0){
			bigCluster = cluster1;
			smallCluster = cluster2;
		} else {
			bigCluster = cluster2;
			smallCluster = cluster1;
		}
		ClusterPair thePair = new ClusterPair(bigCluster, smallCluster, Double.MAX_VALUE);
		ClusterPair curPair = null;
		Iterator<ClusterPair> pairItr = this.mergeScores.iterator();
		if (pairItr.hasNext()){
			curPair = pairItr.next();
			if(curPair.compareTo(thePair) == 0){
				thePair = curPair;
			} else {
				while (pairItr.hasNext() && curPair.cluster1.compareTo(thePair.cluster1) <= 0){
					curPair = pairItr.next();
					if(curPair.compareTo(thePair) == 0){
						thePair = curPair;
						break;
					}
				}
			}
		}
		if (thePair.score == Double.MAX_VALUE){
			thePair.score = bigCluster.computePotentialInternalEdgeDensity(smallCluster);
			this.mergeScores.add(thePair);
		}
		return thePair;
	}
	
	public static int getNextClusterId(){
		return nextClusterId++;
	}
	
	public static LocationGridCell[][] makeGrid(int dimension){
		double[] xdiv = getXDivisions(dimension);
		double[] ydiv = getYDivisions(dimension);
		LocationGridCell[][] grid = new LocationGridCell[xdiv.length-1][ydiv.length-1];
		try {
			Connection con = EpiSimUtil.dbConnect();
			PreparedStatement getLocationIDs = con.prepareStatement(
					"SELECT locationID FROM "+EpiSimUtil.locTbl+
					" WHERE x >= ? AND x < ? AND y >= ? AND y < ?");
			for (int x = 0; x < xdiv.length-1; x++){
				for (int y = 0; y < ydiv.length-1; y++){
					HashSet<Integer> locationIDs = new HashSet<Integer>();
					getLocationIDs.setDouble(1, xdiv[x]);
					getLocationIDs.setDouble(2, xdiv[x+1]);
					getLocationIDs.setDouble(3, ydiv[y]);
					getLocationIDs.setDouble(4, ydiv[y+1]);
					ResultSet getLocationIDsQ = getLocationIDs.executeQuery();
					while (getLocationIDsQ.next()){
						locationIDs.add(new Integer(getLocationIDsQ.getInt("locationID")));
					}
					grid[x][y] = new LocationGridCell(locationIDs);
				}
			}
			con.close();
			return grid;
		} catch (Exception e){
			System.out.println(e);
			return null;
		}
	}
	
	public static double[] getXDivisions(int dimension){
		double min = EpiSimUtil.getMinX();
		double max = EpiSimUtil.getMaxX();
		double range = max - min;
		double cellSize = range / dimension;
		int numCells = (int)(range/cellSize) + 1;
		double[] divs = new double[numCells];
		double curDiv = min;
		for (int i = 0; i < divs.length; i++){
			divs[i] = curDiv;
			curDiv += cellSize;
		}
		return divs;
	}
	
	public static double[] getYDivisions(int dimension){
		double min = EpiSimUtil.getMinY();
		double max = EpiSimUtil.getMaxY();
		double range = max - min;
		double cellSize = range / dimension;
		int numCells = (int)(range/cellSize) + 1;
		double[] divs = new double[numCells];
		double curDiv = min;
		for (int i = 0; i < divs.length; i++){
			divs[i] = curDiv;
			curDiv += cellSize;
		}
		return divs;
	}
	
//	public static ArrayList<Integer> getLocationIDs(double xmin, double xmax, double ymin, double ymax){
//		try {
//			Connection con = EpiSimUtil.dbConnect();
//			
//		} catch (Exception e){
//			System.out.println(e);
//			return null;
//		}
//	}
	
	private class ClusterPair implements Comparable<ClusterPair> {
		private HierarchGridCluster cluster1;
		private HierarchGridCluster cluster2;
		public double score;
		
		public ClusterPair(HierarchGridCluster cluster1, HierarchGridCluster cluster2, double score){
			this.cluster1 = cluster1;
			this.cluster2 = cluster2;
			this.score = score;
		}
		
//		public ClusterPair(int cluster1, int cluster2, double score){
//			this.cluster1 = new Integer(cluster1);
//			this.cluster2 = new Integer(cluster2);
//			this.score = score;
//		}
		
		public int compareTo(ClusterPair other){
			int comp = this.cluster1.compareTo(other.cluster1);
			if (comp == 0) return this.cluster2.compareTo(other.cluster2);
			return comp;
		}
	}

}
