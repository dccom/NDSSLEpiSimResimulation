package dbepi;

//import java.sql.*;
import java.util.*;

public class HierarchGridCluster implements Comparable<HierarchGridCluster>{
	private int id;
//	private double internalEdgeDensity;
	private ArrayList<LocationGridCell> myCells;
	private ArrayList<LocationGridCell> myEdgeCells;

	public HierarchGridCluster(ArrayList<LocationGridCell> cells, int id) {
		this.myCells = cells;
		this.myEdgeCells = cells;
		this.id = id;
		trimEdgeCells();
//		computeInternalEdgeDensity();
	}
	
	public int getId(){
		return this.id;
	}
	
	private void setId(int id){
		this.id = id;
	}
	
	public int compareTo(HierarchGridCluster other){
		return this.id - other.id;
	}
	
	public void trimEdgeCells(){
		Iterator<LocationGridCell> edgeItr  = myEdgeCells.iterator();
		while(edgeItr.hasNext()){
			boolean isInternal = true;
			LocationGridCell edgeCell = edgeItr.next();
			Iterator<LocationGridCell> neighbors = edgeCell.neighborIterator();
			while(neighbors.hasNext()){
				LocationGridCell neighbor = neighbors.next();
				if (!containsCell(neighbor))
					isInternal = false;
			}
			if (isInternal)
				edgeItr.remove();
		}
	}
	
	public boolean containsCell(LocationGridCell other){
		boolean hasCell = false;
		Iterator<LocationGridCell> itr = myCells.iterator();
		while(itr.hasNext()){
			if(itr.next().equals(other))
				hasCell = true;
		}
		return hasCell;
	}
	
//	private void computeInternalEdgeDensity(){
//		int numInternalEdges = 0;
//		int numLocations = 0;
//		try {
//			Connection con = EpiSimUtil.dbConnect();
//			PreparedStatement numEdges = con.prepareStatement(
//					"SELECT COUNT(*) FROM "+EpiSimUtil.llTbl+" WHERE location1ID = ? AND location2ID = ?");
//			Iterator<LocationGridCell> cellItr = this.myCells.iterator();
//			while(cellItr.hasNext()){
//				LocationGridCell cell1 = cellItr.next();
//				numLocations += cell1.numLocations();
//				Iterator<LocationGridCell> cellItr2 = this.myCells.iterator();
//				while(cellItr2.hasNext()){
//					LocationGridCell cell2 = cellItr2.next();
//					if (!cell1.equals(cell2)){
//						Iterator<Integer> location1Itr = cell1.getLocationIDs().iterator();
//						while(location1Itr.hasNext()){
//							Integer location1 = location1Itr.next();
//							Iterator<Integer> location2Itr = cell2.getLocationIDs().iterator();
//							while(location2Itr.hasNext()){
//								Integer location2 = location2Itr.next();
//								numEdges.setInt(1, location1.intValue());
//								numEdges.setInt(2, location2.intValue());
//								ResultSet numEdgesQ = numEdges.executeQuery();
//								if (numEdgesQ.next())
//									numInternalEdges += numEdgesQ.getInt("COUNT(*)");
//							}
//						}
//					}
//				}
//			}
//			if (numLocations > 0)
//				this.internalEdgeDensity = (double)numInternalEdges / (double)numLocations;
//			else
//				this.internalEdgeDensity = -1.0;
//		} catch (Exception e){
//			System.out.println(e);
//			this.internalEdgeDensity = -1.0;
//			return;
//		}
//		
//	}
	
//	public double computePotentialInternalEdgeDensity(HierarchGridCluster other){
////		if (this.equals(other)) return this.internalEdgeDensity;
//		ArrayList<LocationGridCell> cells = this.combinedCells(other);
//		int numInternalEdges = 0;
//		int numLocations = 0;
//		try {
//			Connection con = EpiSimUtil.dbConnect();
//			PreparedStatement numEdges = con.prepareStatement(
//					"SELECT COUNT(*) FROM "+EpiSimUtil.llTbl+" WHERE location1ID = ? AND location2ID = ?");
//			Iterator<LocationGridCell> cellItr = cells.iterator();
//			while(cellItr.hasNext()){
//				LocationGridCell cell1 = cellItr.next();
//				numLocations += cell1.numLocations();
//				Iterator<LocationGridCell> cellItr2 = cells.iterator();
//				while(cellItr2.hasNext()){
//					LocationGridCell cell2 = cellItr2.next();
//					if (!cell1.equals(cell2)){
//						Iterator<Integer> location1Itr = cell1.getLocationIDs().iterator();
//						while(location1Itr.hasNext()){
//							Integer location1 = location1Itr.next();
//							Iterator<Integer> location2Itr = cell2.getLocationIDs().iterator();
//							while(location2Itr.hasNext()){
//								Integer location2 = location2Itr.next();
//								numEdges.setInt(1, location1.intValue());
//								numEdges.setInt(2, location2.intValue());
//								ResultSet numEdgesQ = numEdges.executeQuery();
//								if (numEdgesQ.next())
//									numInternalEdges += numEdgesQ.getInt("COUNT(*)");
//							}
//						}
//					}
//				}
//			}
//			if (numLocations > 0)
//				return (double)numInternalEdges / (double)numLocations;
//			else
//				return -1.0;
//		} catch (Exception e){
//			System.out.println(e);
//			return -1.0;
//		}
//		
//	}
	
	public double computePotentialInternalEdgeDensity(HierarchGridCluster other){
//		if (this.equals(other)) return this.internalEdgeDensity;
		ArrayList<LocationGridCell> cells = this.combinedCells(other);
		int numInternalEdges = 0;
		int numLocations = 0;
		Iterator<LocationGridCell> cellItr = cells.iterator();
		while(cellItr.hasNext()){
			LocationGridCell cell1 = cellItr.next();
			numLocations += cell1.numLocations();
			Iterator<LocationGridCell> cellItr2 = cells.iterator();
			while(cellItr2.hasNext()){
				LocationGridCell cell2 = cellItr2.next();
				if (!cell1.equals(cell2)){
					numInternalEdges += cell1.getNumInterEdges(cell2);
				}
			}
		}
		if (numLocations > 0)
			return (double)numInternalEdges / (double)numLocations;
		else
			return -1.0;
		
	}
	
	private ArrayList<LocationGridCell> combinedCells(HierarchGridCluster other){
		ArrayList<LocationGridCell> cells = new ArrayList<LocationGridCell>(this.myCells.size() + other.myCells.size());
		Iterator<LocationGridCell> itr = this.myCells.iterator();
		while(itr.hasNext()){
			cells.add(itr.next());
		}
		itr = other.myCells.iterator();
		while(itr.hasNext()){
			cells.add(itr.next());
		}
		return cells;
	}
	
//	private ArrayList<LocationGridCell> combinedEdgeCells(HierarchGridCluster other){
//		ArrayList<LocationGridCell> cells = new ArrayList<LocationGridCell>(this.myCells.size() + other.myCells.size());
//		Iterator<LocationGridCell> itr = this.myEdgeCells.iterator();
//		while(itr.hasNext()){
//			cells.add(itr.next());
//		}
//		itr = other.myEdgeCells.iterator();
//		while(itr.hasNext()){
//			cells.add(itr.next());
//		}
//		return cells;
//	}
	
	public void mergeCluster(HierarchGridCluster other){
		Iterator<LocationGridCell> itr = other.myCells.iterator();
		while (itr.hasNext()){
			itr.next().setCluster(this);
		}
		this.myCells.addAll(other.myCells);
		this.myEdgeCells.addAll(other.myEdgeCells);
		this.setId(HierarchGridClusterWorld.getNextClusterId());
		trimEdgeCells();
//		computeInternalEdgeDensity();
	}
	
	public Iterator<HierarchGridCluster> neighborItr(){
		HashSet<HierarchGridCluster> allNeighbors = new HashSet<HierarchGridCluster>();
		Iterator<LocationGridCell> edgeCellItr = this.myEdgeCells.iterator();
		while (edgeCellItr.hasNext()){
			Iterator<LocationGridCell> neighborItr = edgeCellItr.next().neighborIterator();
			while(neighborItr.hasNext()){
				HierarchGridCluster other = neighborItr.next().getCluster();
				if (!this.equals(other))
					allNeighbors.add(other);
			}
		}
		return allNeighbors.iterator();
	}
	
	public Iterator<Integer> getLocationIdItr(){
		LinkedList<Integer> locations = new LinkedList<Integer>();
		Iterator<LocationGridCell> cellItr = this.myCells.iterator();
		while (cellItr.hasNext()){
			Iterator<Integer> locItr = cellItr.next().getLocationIdItr();
			while (locItr.hasNext()){
				locations.add(locItr.next());
			}
		}
		return locations.iterator();
	}

}
