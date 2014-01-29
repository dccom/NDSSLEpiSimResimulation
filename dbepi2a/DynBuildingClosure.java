package dbepi2a;

import java.sql.*;
import java.util.*;

public class DynBuildingClosure implements Policy {

	private HashMap<Integer,Integer> impLoc = new HashMap<Integer,Integer>();
//	private HashMap<Integer,Integer> closedWorkLocPeople = new HashMap<Integer,Integer>();
	private HashSet<Integer> closedWorkLoc = new HashSet<Integer>();
	private HashSet<Integer> peopleAffected = new HashSet<Integer>();

	public DynBuildingClosure(HashMap<Integer,Integer> impLoc) {
		this.impLoc = impLoc;
	}

	public boolean isValidTransEvent(Integer infected, Integer victim,
			Integer location, long time) {
		if (closedWorkLoc.contains(location))
			return false;
		return true;
	}
	
	public void newInf(Integer victim, Integer location, long time) {
		Iterator<Integer> locs = EpiSimUtil.getLocationsVisited(victim.intValue()).iterator();
		while (locs.hasNext()) {
			location = locs.next();
			if (impLoc.containsKey(location)) impLoc.put(location, new Integer(impLoc.get(location).intValue()+1));
		}
	}
	
	public void closeLoc(Integer location) {
		this.closedWorkLoc.add(location);
		try {
			Connection con = EpiSimUtil.dbConnect();
			ResultSet getWorkersQ = con.createStatement().executeQuery(
					"SELECT personID FROM "+EpiSimUtil.actTbl+" WHERE locationID="+location);
			while (getWorkersQ.next()) {
				Integer person = new Integer(getWorkersQ.getInt("personID"));
				this.peopleAffected.add(person);
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
//		double threshold = Double.parseDouble(args[0]);
//		int numLocations = Integer.parseInt(args[1]);
//		buildingClosureSim(threshold, numLocations, 10000, 90);
		memTest();

	}
	
	public static void memTest() {
		HashMap<Integer,Integer> a = new HashMap<Integer,Integer>(565000);
		for (int i=0; i<a.size(); i++)
			a.put(i, i);
	}
	
	public static void buildingClosureSim(double threshold, int numLocations, double minHomeDist, int percentile) {
		HashMap<Integer,Integer> importantLocations = EpiSimUtil.getLocsHighOutDegRestHomeDist(numLocations, minHomeDist, percentile);
//		BufferedBigIntSet importantLocations = EpiSimUtil.getLocsHighOutDegRestHomeDist(numLocations, minHomeDist, percentile);
//		int score = 0;
		long startTime = 0;
		long endTime = EpiSimUtil.dayToSeconds(100);
		long timeStep = EpiSimUtil.dayToSeconds(1);
		System.out.println("Building closure sim (dbepi2).");
		System.out.println("Min home-distance ("+percentile+"th percentile):\t"+minHomeDist);
		System.out.println("Number of locations:\t"+numLocations);
		System.out.println("Closure threshold:\t"+threshold);
		System.out.println("start time (seconds):\t"+startTime);
		System.out.println("end time (seconds):\t"+endTime);
		System.out.println("time step (seconds):\t"+timeStep);
		System.out.println("Day\tnum aff.\tnum inf.\tnum closed");
//		boolean trackInfLoc = true;
		DynBuildingClosure policy = new DynBuildingClosure(importantLocations);
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
			System.out.println((sim.curTime/(60*60*24))+"\t"+policy.peopleAffected.size()+"\t"+sim.numInfected()+"\t"+policy.closedWorkLoc.size());
//			if ((sim.curTime/(60*60*24)) == 85 || (sim.curTime/(60*60*24)) == 90 || (sim.curTime/(60*60*24)) == 95) {
//				System.out.println("Number of distinct people at closed locations:\t"
//						+policy.peopleAffected.size());//+EpiSimUtil.getNumDistPeopleAtLocations(policy.closedWorkLoc));
//			}
			sim.stepSim(timeStep);
		}
//		System.out.println("Total score:\t"+score);
		System.out.println("Number of distinct people at closed locations:\t"
				+policy.peopleAffected.size());//+EpiSimUtil.getNumDistPeopleAtLocations(policy.closedWorkLoc));
	}

}
