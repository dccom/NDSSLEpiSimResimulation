package dbepi2a;
import java.sql.*;
import java.util.*;

public class DynBuildingClosureWorkers implements Policy {
	
	// Tracks the number of sick people at the locations monitored for closing
	private HashMap<Integer,Integer> impLoc = new HashMap<Integer,Integer>();
	// Map of the people sent home to the locations sent home from.
	private HashMap<Integer,Integer> closedWorkLocPeople = new HashMap<Integer,Integer>();
	// Set of locations closed at an given point in the simulation
	private HashSet<Integer> closedWorkLoc = new HashSet<Integer>();

	/**
	 * 
	 * @param impLoc HashMap: key: locations to monitor; value number of sick people at the location (typically 0 at initialization)
	 */
	public DynBuildingClosureWorkers(HashMap<Integer,Integer> impLoc) {
		this.impLoc = impLoc;
	}

	/**
	 * If either the infected or victim were sent away from the location the transmission is invalid
	 */
	public boolean isValidTransEvent(Integer infected, Integer victim,
			Integer location, long time) {
		if ((closedWorkLocPeople.containsKey(infected) && closedWorkLocPeople.get(infected).equals(location))
			|| (closedWorkLocPeople.containsKey(victim) && closedWorkLocPeople.get(victim).equals(location)))
			return false;
		return true;
	}
	
	/**
	 * Any location visited by the victim that is in impLoc will have it's infection count incremented.
	 */
	public void newInf(Integer victim, Integer location, long time) {
		Iterator<Integer> locs = EpiSimUtil.getLocationsVisited(victim.intValue()).iterator();
		while (locs.hasNext()) {
			location = locs.next();
			if (impLoc.containsKey(location)) impLoc.put(location, new Integer(impLoc.get(location).intValue()+1));
		}
	}
	
	/**
	 * All people who work at location are added to closedWorkLocPeople replacing any entrie allready
	 * present for each person.
	 * @param location the id of the location whose workers are sent home.
	 */
	public void closeLoc(Integer location) {
		this.closedWorkLoc.add(location);
		try {
			Connection con = EpiSimUtil.dbConnect();
			ResultSet getWorkersQ = con.createStatement().executeQuery(
					"SELECT personID FROM "+EpiSimUtil.actTbl+" WHERE purpose=1 AND locationID="+location);
			while (getWorkersQ.next()) {
				Integer person = new Integer(getWorkersQ.getInt("personID"));
				this.closedWorkLocPeople.put(person, location);
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	/**
	 * All people who work at location are added to closedWorkLocPeople replacing any entrie allready
	 * present for each person.  Additionally, all family members of these people are sent home from
	 * work.
	 * @param location the id of the location whose workers are sent home.
	 */
	public void closeLocPlusFamily(Integer location) {
		this.closedWorkLoc.add(location);
		try {
			Connection con = EpiSimUtil.dbConnect();
			ResultSet getWorkersQ = con.createStatement().executeQuery(
					"SELECT personID FROM "+EpiSimUtil.actTbl+" WHERE purpose=1 AND locationID="+location);
			while (getWorkersQ.next()) {
				Integer person = new Integer(getWorkersQ.getInt("personID"));
				this.closedWorkLocPeople.put(person, location);
				Iterator<Integer> familyItr = EpiSimUtil.getRelatives(person).iterator();
				while (familyItr.hasNext()) {
					Integer relative = familyItr.next();
					ResultSet getRelWorkQ = con.createStatement().executeQuery(
							"SELECT locationID FROM "+EpiSimUtil.actTbl+" WHERE purpose=1 AND personID="+relative);
					if (getRelWorkQ.first()) this.closedWorkLocPeople.put(relative, new Integer(getRelWorkQ.getInt("locationID")));
				}
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double threshold = Double.parseDouble(args[0]);
		int numLocations = Integer.parseInt(args[1]);
//		buildingClosureSim(threshold, numLocations, 10000, 90);
//		buildingClosureSim(threshold, numLocations);
		buildingClosureSimPlusFamily(threshold, numLocations, 10000, 90);

	}
	
	public static void buildingClosureSim(double threshold, int numLocations, double minHomeDist, int percentile) {
		HashMap<Integer,Integer> importantLocations = EpiSimUtil.getLocsHighOutDegRestHomeDist(numLocations, minHomeDist, percentile);
//		int score = 0;
		long startTime = 0;
		long endTime = EpiSimUtil.dayToSeconds(100);
		long timeStep = EpiSimUtil.dayToSeconds(1);
		System.out.println("Building closure sim, workers at closed locations.");
		System.out.println("Min home-distance ("+percentile+"th percentile):\t"+minHomeDist);
		System.out.println("Number of locations:\t"+numLocations);
		System.out.println("Closure threshold:\t"+threshold);
		System.out.println("start time (seconds):\t"+startTime);
		System.out.println("end time (seconds):\t"+endTime);
		System.out.println("time step (seconds):\t"+timeStep);
		System.out.println("Day\tnum aff.\tnum inf.\tnum closed");
		DynBuildingClosureWorkers policy = new DynBuildingClosureWorkers(importantLocations);
		ArrayList<Policy> policies = new ArrayList<Policy>(1);
		policies.add(policy);
		DendroSimulation sim = new DendroSimulation(policies);
		sim.getIntitiallyInfected();
		sim.stepSim(startTime);
		while (sim.curTime < endTime) {
			Iterator<Integer> locationsItr = policy.impLoc.keySet().iterator();
			while (locationsItr.hasNext()) {
				Integer location = locationsItr.next();
				if (!policy.closedWorkLoc.contains(location)) {
					double numInfected = 0.0;
					if (policy.impLoc.containsKey(location))
						numInfected = policy.impLoc.get(location).doubleValue();
					double aggPop = (double) EpiSimUtil.getAggPop(location.intValue());
					if (numInfected / aggPop >= threshold) policy.closeLoc(location);
				}
			}
			//score += sim.getNumIncapacitedPeople();
			System.out.println((sim.curTime/(60*60*24))+"\t"+policy.closedWorkLocPeople.size()+"\t"+sim.numInfected()+"\t"+policy.closedWorkLoc.size());
//			if ((sim.curTime/(60*60*24)) == 85 || (sim.curTime/(60*60*24)) == 90 || (sim.curTime/(60*60*24)) == 95) {
//				System.out.println("Number of distinct people at closed locations:\t"
//						+policy.closedWorkLocPeople.size());//+EpiSimUtil.getNumDistPeopleAtLocations(policy.closedWorkLoc));
//			}
			sim.stepSim(timeStep);
		}
//		System.out.println("Total score:\t"+score);
		System.out.println("Number of distinct people at closed locations:\t"
				+policy.closedWorkLocPeople.size());//+EpiSimUtil.getNumDistPeopleAtLocations(policy.closedWorkLoc));
	}
	
	public static void buildingClosureSim(double threshold, int numLocations) {
		HashMap<Integer,Integer> importantLocations = EpiSimUtil.getLocsHighOutDeg(numLocations);
//		BufferedBigIntSet importantLocations = EpiSimUtil.getLocsHighOutDegRestHomeDist(numLocations, minHomeDist, percentile);
//		int score = 0;
		long startTime = 0;
		long endTime = EpiSimUtil.dayToSeconds(100);
		long timeStep = EpiSimUtil.dayToSeconds(1);
		System.out.println("Building closure sim, highest degree locations, workers at closed locations.");
		System.out.println("Number of locations:\t"+numLocations);
		System.out.println("Closure threshold:\t"+threshold);
		System.out.println("start time (seconds):\t"+startTime);
		System.out.println("end time (seconds):\t"+endTime);
		System.out.println("time step (seconds):\t"+timeStep);
		System.out.println("Day\tnum aff.\tnum inf.\tnum closed");
//		boolean trackInfLoc = true;
		DynBuildingClosureWorkers policy = new DynBuildingClosureWorkers(importantLocations);
		ArrayList<Policy> policies = new ArrayList<Policy>(1);
		policies.add(policy);
		DendroSimulation sim = new DendroSimulation(policies);
//		HashMap<Integer,Integer> locations = sim.infectedLocations;
//		Iterator<Integer> locationsItr = importantLocations.iterator();
//		while (locationsItr.hasNext()) {
//			locations.put(locationsItr.next(), new Integer(0));
//		}
		sim.getIntitiallyInfected();
		sim.stepSim(startTime);
		while (sim.curTime < endTime) {
			Iterator<Integer> locationsItr = policy.impLoc.keySet().iterator();
			while (locationsItr.hasNext()) {
				Integer location = locationsItr.next();
				if (!policy.closedWorkLoc.contains(location)) {
					double numInfected = 0.0;
					if (policy.impLoc.containsKey(location))
						numInfected = policy.impLoc.get(location).doubleValue();
					double aggPop = (double) EpiSimUtil.getAggPop(location.intValue());
					if (numInfected / aggPop >= threshold) policy.closeLoc(location);
				}
			}
			//score += sim.getNumIncapacitedPeople();
			System.out.println((sim.curTime/(60*60*24))+"\t"+policy.closedWorkLocPeople.size()+"\t"+sim.numInfected()+"\t"+policy.closedWorkLoc.size());
//			if ((sim.curTime/(60*60*24)) == 85 || (sim.curTime/(60*60*24)) == 90 || (sim.curTime/(60*60*24)) == 95) {
//				System.out.println("Number of distinct people at closed locations:\t"
//						+policy.closedWorkLocPeople.size());//+EpiSimUtil.getNumDistPeopleAtLocations(policy.closedWorkLoc));
//			}
			sim.stepSim(timeStep);
		}
//		System.out.println("Total score:\t"+score);
		System.out.println("Number of distinct people at closed locations:\t"
				+policy.closedWorkLocPeople.size());//+EpiSimUtil.getNumDistPeopleAtLocations(policy.closedWorkLoc));
	}
	
	public static void buildingClosureSimPlusFamily(double threshold, int numLocations, double minHomeDist, int percentile) {
		HashMap<Integer,Integer> importantLocations = EpiSimUtil.getLocsHighOutDegRestHomeDist(numLocations, minHomeDist, percentile);
//		int score = 0;
		long startTime = 0;
		long endTime = EpiSimUtil.dayToSeconds(100);
		long timeStep = EpiSimUtil.dayToSeconds(1);
		System.out.println("Building closure sim, workers at closed locations plus family.");
		System.out.println("Min home-distance ("+percentile+"th percentile):\t"+minHomeDist);
		System.out.println("Number of locations:\t"+numLocations);
		System.out.println("Closure threshold:\t"+threshold);
		System.out.println("start time (seconds):\t"+startTime);
		System.out.println("end time (seconds):\t"+endTime);
		System.out.println("time step (seconds):\t"+timeStep);
		System.out.println("Day\tnum aff.\tnum inf.\tnum closed");
		DynBuildingClosureWorkers policy = new DynBuildingClosureWorkers(importantLocations);
		ArrayList<Policy> policies = new ArrayList<Policy>(1);
		policies.add(policy);
		DendroSimulation sim = new DendroSimulation(policies);
		sim.getIntitiallyInfected();
		sim.stepSim(startTime);
		while (sim.curTime < endTime) {
			Iterator<Integer> locationsItr = policy.impLoc.keySet().iterator();
			while (locationsItr.hasNext()) {
				Integer location = locationsItr.next();
				if (!policy.closedWorkLoc.contains(location)) {
					double numInfected = 0.0;
					if (policy.impLoc.containsKey(location))
						numInfected = policy.impLoc.get(location).doubleValue();
					double aggPop = (double) EpiSimUtil.getAggPop(location.intValue());
					if (numInfected / aggPop >= threshold) policy.closeLocPlusFamily(location);
				}
			}
			//score += sim.getNumIncapacitedPeople();
			System.out.println((sim.curTime/(60*60*24))+"\t"+policy.closedWorkLocPeople.size()+"\t"+sim.numInfected()+"\t"+policy.closedWorkLoc.size());
//			if ((sim.curTime/(60*60*24)) == 85 || (sim.curTime/(60*60*24)) == 90 || (sim.curTime/(60*60*24)) == 95) {
//				System.out.println("Number of distinct people at closed locations:\t"
//						+policy.closedWorkLocPeople.size());//+EpiSimUtil.getNumDistPeopleAtLocations(policy.closedWorkLoc));
//			}
			sim.stepSim(timeStep);
		}
//		System.out.println("Total score:\t"+score);
		System.out.println("Number of distinct people at closed locations:\t"
				+policy.closedWorkLocPeople.size());//+EpiSimUtil.getNumDistPeopleAtLocations(policy.closedWorkLoc));
	}

}
