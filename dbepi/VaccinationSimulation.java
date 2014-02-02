/**
 * 
 */
package dbepi;
import java.sql.*;
import java.util.*;
//import java.lang.*;

/**
 * @author dave
 *
 */
public class VaccinationSimulation {
	public static final int[] numberOfVaccinesSpread = {
//		923,
//		1764,
//		3274,
//		5602,
//		9033,
//		14188,
//		21694,
		32476,
		47835,
		69163,
		99902,
		141213,
		194044,
		260652,
		348993,
		484418,
		679868,
		873390//888214
	};
	
	private static final int defaultClosedLocationsSize = 1000;

	
	public static int runID = 4;
	public static long randSeed = 1154622729289L;
	public static Random rand;
	public static long vaccDelay = 0;
	public static long sympDelay = 86400;
	public static int numCases = 565685;
	
	private static int approxNumClosedLocations = 1000;
	
	private HashMap<Integer, Person> people;
	private int numInfected;
	private long curTime;
	private int numVaccines;
	//private HashSet<Integer> closedLocations;
	private BufferedBigIntSet closedLocations;
	private HashMap<Integer,Integer> infectedLocations;
	private boolean trackInfLoc = false;
	
	private Connection fludb;
	private Connection fludbGenData;
	
	/**
	 * 
	 */
	public VaccinationSimulation(int numVaccines) {
		this.numInfected = 0;
		this.people = new HashMap<Integer, Person>(EpiSimUtil.population);
		this.curTime = 0;
		this.numVaccines = numVaccines;
		this.fludb = EpiSimUtil.dbConnect();
		this.fludbGenData = EpiSimUtil.dbGenDataConnect();
		//this.closedLocations = new HashSet<Integer>(approxNumClosedLocations);
		this.closedLocations = new BufferedBigIntSet(defaultClosedLocationsSize);
	}
	
	/**
	 * 
	 */
	public VaccinationSimulation(boolean trackInfLoc) {
		this.numInfected = 0;
		this.people = new HashMap<Integer, Person>(EpiSimUtil.population);
		this.curTime = 0;
		this.numVaccines = 0;
		this.fludb = EpiSimUtil.dbConnect();
		this.fludbGenData = EpiSimUtil.dbGenDataConnect();
		//this.closedLocations = new HashSet<Integer>(approxNumClosedLocations);
		this.closedLocations = new BufferedBigIntSet(defaultClosedLocationsSize);
		this.trackInfLoc = trackInfLoc;
		if (this.trackInfLoc) infectedLocations = new HashMap<Integer,Integer>(50000);
	}

	/**
	 * 
	 */
	public VaccinationSimulation() {
		this.numInfected = 0;
		this.people = new HashMap<Integer, Person>(EpiSimUtil.population);
		this.curTime = 0;
		this.numVaccines = 0;
		this.fludb = EpiSimUtil.dbConnect();
		this.fludbGenData = EpiSimUtil.dbGenDataConnect();
		//this.closedLocations = new HashSet<Integer>(approxNumClosedLocations);
		this.closedLocations = new BufferedBigIntSet(defaultClosedLocationsSize);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		mostSociableSimulation(260652);
//		mostInfluenceSimulation(260652);
		rand = new Random(randSeed);
//		degreeSpreadSimulation();
//		mostSociableLocationsEvenlySimulation(150000, 150);
//		degreeSpreadLLSimulation();
//		randomSpreadSimulation(1);
//		numLocationsForDegreeSpreadLLSim();
//		degreeSpreadSocLLSimulation();
//		degreeSpreadRandLLSimulation();
//		degreeSpreadSocLLSimulationPurp(1);
//		degreeSpreadMostSocLocFracSocDistSimulation();
//		getNumLocationsForDegreeSpreadMostSocLocFracSocDistSimulation();
//		degreeSpreadLargestHomeDistFracSocDistSimulation();
//		degreeSpreadMostSocLocRestByHomeDistFracSocDistSimulation(90, 10000, Double.MAX_VALUE);
//		degreeSpreadMostSocLocRestByHomeDistRandFracDistSimulation(90, 10000, Double.MAX_VALUE);
//		int[] numVaccs = {100, 1000, 10000, 100000};
//		long[] vaccDelays = {EpiSimUtil.dayToSeconds(1), EpiSimUtil.dayToSeconds(3), EpiSimUtil.dayToSeconds(7), EpiSimUtil.dayToSeconds(14)};
//		multiVaccDelayMultiDynVaccConOfInfPeople(0, EpiSimUtil.dayToSeconds(1), numVaccs, vaccDelays);
//		double threshold = Double.parseDouble(args[0]);
//		buildingClosureSim(threshold);
//		int numLocations = Integer.parseInt(args[1]);
//		buildingClosureSim(threshold, numLocations, 10000, 90);
//		buildingClosureSimPlus(threshold, numLocations, 10000, 90);
//		buildingClosureSimPlus(threshold, numLocations, 10000, 90, 10);
//		buildingClosureSim(threshold, numLocations);
//		vaccImportantHouseHoldsSpreadSim(numLocations);
		degreeSpreadSocLLSimulationLocPurp(1);

	}
	
	public static double percentPopVacc (int numVaccines) {
		return (((double)numVaccines) / ((double) EpiSimUtil.population))*100;
	}
	
	public static double percentCasesPrevented (int numInfected) {
		return (((double)(numCases-numInfected))/((double)numCases))*100;
	}
	
	public static void doNothingSim(){
		VaccinationSimulation sim = new VaccinationSimulation();
		sim.runSim();
		
	}
	
	public static void vaccImportantHouseHoldsSpreadSim(int numLocations) {
		System.out.println("Vaccination Simulation - vaccinating house holds that visit the "
				+numLocations+" highest out degree locations with minimum home distance of 10,000 m.");
		System.out.println("% pop. vacc.\t% cases prev.\t num cases");
//		Integer[] houseHolds = EpiSimUtil.getHouseHoldsFromProjImpLoc(numLocations);
//		int largestNumVaccs = numberOfVaccinesSpread[numberOfVaccinesSpread.length-1];
//		Integer[] peopleToVacc = EpiSimUtil.getPeopleFromImpHouseHolds(largestNumVaccs, numLocations);
		for (int i=0; i < numberOfVaccinesSpread.length; i++) {
			int numVaccs = numberOfVaccinesSpread[i];
//			int[] results = vaccImportantHouseHoldsSim(houseHolds,numVaccs);
			int[] results = vaccImportantHouseHoldsSim(numVaccs, numLocations);
			System.out.println(percentPopVacc(results[0])+"\t"+percentCasesPrevented(results[1])+"\t"+results[1]);
		}
	}
	
	public static int[] vaccPeopleSim(Integer[] people, int numVaccines) {
		VaccinationSimulation sim = new VaccinationSimulation(numVaccines);
		sim.getIntitiallyInfected();
		for (int personIndex = 0; personIndex < people.length && sim.numVaccines > 0; personIndex++) {
			Person p = sim.getPerson(people[personIndex]);
			if (p.vaccinate(Long.MIN_VALUE)) sim.numVaccines--;
		}
		sim.runSim();
		int[] rv = {(numVaccines-sim.numVaccines), sim.numInfected(Long.MAX_VALUE)};
		return rv;
	}
	
	public static int[] vaccImportantHouseHoldsSim(int numVaccines, int numLocations) {
		VaccinationSimulation sim = new VaccinationSimulation(numVaccines);
		sim.getIntitiallyInfected();
		int batchSize = 1000;
		int startRank = 0;
		Integer[] people = EpiSimUtil.getPeopleFromImpHousesTbl(startRank, batchSize, numLocations);
		startRank += batchSize;
		while (sim.numVaccines > 0 && people[0] != null) {
			for (int i=0; i < batchSize; i++) {
				if (people[i] != null) {
					Person p = sim.getPerson(people[i]);
					if (p.vaccinate(Long.MIN_VALUE)) sim.numVaccines--;
				}
			}
			people = EpiSimUtil.getPeopleFromImpHousesTbl(startRank, batchSize, numLocations);
			startRank += batchSize;
		}
		sim.runSim();
		int[] rv = {(numVaccines-sim.numVaccines),sim.numInfected(Long.MAX_VALUE)};
		return rv;
	}
	
	public static void degreeSpreadSocLLSimulationLocPurp(int purpose){
		System.out.println("restricted to purpose:\t"+EpiSimUtil.activityTypeNames[purpose]);
		double[] desiredFracAggPopVacc = {1.0,0.8,0.6,0.4,0.2};
		int[][] numInfected = new int[desiredFracAggPopVacc.length][numberOfVaccinesSpread.length];
		
		for (int j = 0; j < desiredFracAggPopVacc.length; j++){
			double thisDesiredFracAggPopVacc = desiredFracAggPopVacc[j];
			for (int i = 0; i < numberOfVaccinesSpread.length; i++){
				int numVaccines = numberOfVaccinesSpread[i];
				numInfected[j][i] = mostSociableLocationsSociableDistSimulationLocPurp(numVaccines, thisDesiredFracAggPopVacc, purpose);
			}
		}
		System.out.print("NumVaccines");
		for (int i = 0; i < desiredFracAggPopVacc.length; i++){
			System.out.print("\t"+desiredFracAggPopVacc[i]+"Vaccs/Loc");
		}
		System.out.println();
		System.out.println("---------------------------------------------------------------");
		for (int i = 0; i < numberOfVaccinesSpread.length; i++){
			System.out.print(numberOfVaccinesSpread[i]);
			for (int j = 0; j < desiredFracAggPopVacc.length; j++)
				System.out.print("\t"+numInfected[j][i]);
			System.out.println();
		}
	}
	
	public static int mostSociableLocationsSociableDistSimulationLocPurp(int numVaccines, double fracPop, int purpose){
		System.out.println("Most Sociable Location Simulation, most sociable distribution, restricted to purpose "+EpiSimUtil.activityTypeNames[purpose]+" ("+purpose+")");
		System.out.println("Number of vaccines: "+numVaccines);
		System.out.println("Fraction of loc pop vacc: "+fracPop);
		VaccinationSimulation sim = new VaccinationSimulation();
		sim.getIntitiallyInfected();
		sim.vaccinateMostSociableLocationsSociablyLocPurp(numVaccines, fracPop, purpose);
		sim.runSim();
		int numInfected = sim.numInfected(Long.MAX_VALUE-1);
		System.out.println("There are "+numInfected+" infected individuals.");
		System.out.println("There are "+sim.numPeople()+" individuals total.");
		System.out.println("======================================");
		return numInfected;
	}
	
	public void vaccinateMostSociableLocationsSociablyLocPurp(int numVaccines, double fracPop, int purpose){
		try {
			Connection con = EpiSimUtil.dbConnect();
			PreparedStatement getPeople = con.prepareStatement(
					"SELECT DISTINCT "+EpiSimUtil.actTbl+".personID,"+EpiSimUtil.conCountTbl+".tally FROM "+EpiSimUtil.actTbl+
					" INNER JOIN "+EpiSimUtil.conCountTbl+" ON "+EpiSimUtil.actTbl+".personID = "+EpiSimUtil.conCountTbl+
					".personID WHERE "+EpiSimUtil.actTbl+".locationID = ? ORDER BY "+EpiSimUtil.conCountTbl+
					".tally DESC;");
			Statement getLocations = con.createStatement();
			ResultSet getLocationsQ = getLocations.executeQuery(
					"SELECT locations.locationID,aggregatePop FROM "+EpiSimUtil.locTbl+" INNER JOIN "+EpiSimUtil.llCountTbl+
					" ON "+EpiSimUtil.llCountTbl+".locationID = "+EpiSimUtil.locTbl+
					".locationID WHERE "+EpiSimUtil.activityTypeNames[purpose]+" > 0 ORDER BY outbound DESC");
			while (getLocationsQ.next() && numVaccines > 0){
				getPeople.setInt(1, getLocationsQ.getInt(EpiSimUtil.locTbl+".locationID"));
				ResultSet getPeopleQ = getPeople.executeQuery();
				int numVaccinesAtLoaction = (int)(fracPop * getLocationsQ.getInt("aggregatePop"));
				while (getPeopleQ.next() && numVaccinesAtLoaction > 0){
					int person = getPeopleQ.getInt(EpiSimUtil.actTbl+".personID");
					int score = getPeopleQ.getInt("tally");
					Person p = getPerson(new Integer(person));
					p.setScore(score);
					if (!p.isVaccinated(Long.MIN_VALUE)){
						p.vaccinate(Long.MIN_VALUE);
						numVaccinesAtLoaction--;
						numVaccines--;
					}
				}
			}
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
//	public static int[] vaccImportantHouseHoldsSim(Integer[] houseHolds, int numVaccines) {
////		Integer[] houseHolds = EpiSimUtil.getHouseHoldsFromProjImpLoc(numLocations);
//		VaccinationSimulation sim = new VaccinationSimulation(numVaccines);
//		sim.getIntitiallyInfected();
//		try {
//			Connection con = EpiSimUtil.dbConnect();
//			PreparedStatement getPeople = con.prepareStatement(
//					"SELECT personID FROM "+EpiSimUtil.demoTbl+" WHERE householdID = ?");
//			for (int houseHoldIndex = 0; sim.numVaccines > 0 && houseHoldIndex < houseHolds.length; houseHoldIndex++) {
//				getPeople.setInt(1, houseHolds[houseHoldIndex].intValue());
//				ResultSet getPeopleQ = getPeople.executeQuery();
//				while (sim.numVaccines > 0 && getPeopleQ.next()) {
//					int personID = getPeopleQ.getInt("personID");
//					Person p = sim.getPerson(new Integer(personID));
//					if (p.vaccinate(Long.MIN_VALUE)) sim.numVaccines--;
//				}
//			}
//			con.close();
//		} catch (Exception e) {
//			System.out.println(e);
//		}
//		sim.runSim();
//		int[] rv = {(numVaccines-sim.numVaccines),sim.numInfected(Long.MAX_VALUE)};
//		return rv;
//	}
	
	public static void buildingClosureSim(double threshold, int numLocations) {
		//HashSet<Integer> importantLocations = EpiSimUtil.getLocsHighOutDegRestHomeDist(numLocations, minHomeDist, percentile);
		//BufferedBigIntSet importantLocations = EpiSimUtil.getLocsHighOutDegRestHomeDist(numLocations, minHomeDist, percentile);
		BufferedBigIntSet importantLocations = EpiSimUtil.getLocsHighOutDeg(numLocations);
		int score = 0;
		long startTime = 0;
		long endTime = EpiSimUtil.dayToSeconds(100);
		long timeStep = EpiSimUtil.dayToSeconds(1);
		System.out.println("High out degree closure simulation.");
		System.out.println("Number of locations:\t"+numLocations);
		System.out.println("Closure threshold:\t"+threshold);
		System.out.println("start time (seconds):\t"+startTime);
		System.out.println("end time (seconds):\t"+endTime);
		System.out.println("time step (seconds):\t"+timeStep);
		System.out.println("Day\tscore\tnum inf.\tnum closed");
		boolean trackInfLoc = true;
		VaccinationSimulation sim = new VaccinationSimulation(trackInfLoc);
		HashMap<Integer,Integer> locations = sim.infectedLocations;
		Iterator<Integer> locationsItr = importantLocations.iterator();
		while (locationsItr.hasNext()) {
			locations.put(locationsItr.next(), new Integer(0));
		}
		sim.getIntitiallyInfected();
		sim.stepSim(startTime);
		while (sim.curTime < endTime) {
			locationsItr = importantLocations.iterator();
			while (locationsItr.hasNext()) {
				Integer location = locationsItr.next();
				if (!sim.closedLocations.contains(location)) {
					double numInfected = 0.0;
					if (locations.containsKey(location))
						numInfected = locations.get(location).doubleValue();
					double aggPop = (double) EpiSimUtil.getAggPop(location.intValue());
					if (numInfected / aggPop >= threshold) sim.closedLocations.add(location);
				}
				if ((sim.curTime/(60*60*24)) == 85 || (sim.curTime/(60*60*24)) == 90 || (sim.curTime/(60*60*24)) == 95) {
					System.out.println("Number of distinct people at closed locations:\t"
							+EpiSimUtil.getNumDistPeopleAtLocations(sim.closedLocations));
				}
			}
			score += sim.getNumIncapacitedPeople();
			System.out.println((sim.curTime/(60*60*24))+"\t"+score+"\t"+sim.numInfected+"\t"+sim.closedLocations.size());
			sim.stepSim(timeStep);
		}
		System.out.println("Total score:\t"+score);
		System.out.println("Number of distinct people at closed locations:\t"
				+EpiSimUtil.getNumDistPeopleAtLocations(sim.closedLocations));
	}
	
	public static void buildingClosureSim(double threshold, int numLocations, double minHomeDist, int percentile) {
		//HashSet<Integer> importantLocations = EpiSimUtil.getLocsHighOutDegRestHomeDist(numLocations, minHomeDist, percentile);
		BufferedBigIntSet importantLocations = EpiSimUtil.getLocsHighOutDegRestHomeDist(numLocations, minHomeDist, percentile);
		int score = 0;
		long startTime = 0;
		long endTime = EpiSimUtil.dayToSeconds(100);
		long timeStep = EpiSimUtil.dayToSeconds(1);
		System.out.println("Restricted location closure simulation.");
		System.out.println("Min home-distance ("+percentile+"th percentile):\t"+minHomeDist);
		System.out.println("Number of locations:\t"+numLocations);
		System.out.println("Closure threshold:\t"+threshold);
		System.out.println("start time (seconds):\t"+startTime);
		System.out.println("end time (seconds):\t"+endTime);
		System.out.println("time step (seconds):\t"+timeStep);
		System.out.println("Day\tscore\tnum inf.\tnum closed");
		boolean trackInfLoc = true;
		VaccinationSimulation sim = new VaccinationSimulation(trackInfLoc);
		HashMap<Integer,Integer> locations = sim.infectedLocations;
		Iterator<Integer> locationsItr = importantLocations.iterator();
		while (locationsItr.hasNext()) {
			locations.put(locationsItr.next(), new Integer(0));
		}
		sim.getIntitiallyInfected();
		sim.stepSim(startTime);
		while (sim.curTime < endTime) {
			locationsItr = importantLocations.iterator();
			while (locationsItr.hasNext()) {
				Integer location = locationsItr.next();
				if (!sim.closedLocations.contains(location)) {
					double numInfected = 0.0;
					if (locations.containsKey(location))
						numInfected = locations.get(location).doubleValue();
					double aggPop = (double) EpiSimUtil.getAggPop(location.intValue());
					if (numInfected / aggPop >= threshold) sim.closedLocations.add(location);
				}
			}
			score += sim.getNumIncapacitedPeople();
			System.out.println((sim.curTime/(60*60*24))+"\t"+score+"\t"+sim.numInfected+"\t"+sim.closedLocations.size());
			if ((sim.curTime/(60*60*24)) == 85 || (sim.curTime/(60*60*24)) == 90 || (sim.curTime/(60*60*24)) == 95) {
				System.out.println("Number of distinct people at closed locations:\t"
						+EpiSimUtil.getNumDistPeopleAtLocations(sim.closedLocations));
			}
			sim.stepSim(timeStep);
		}
		System.out.println("Total score:\t"+score);
		System.out.println("Number of distinct people at closed locations:\t"
				+EpiSimUtil.getNumDistPeopleAtLocations(sim.closedLocations));
	}
	
	public static void buildingClosureSimPlus(double threshold, int numLocations, double minHomeDist, int percentile) {
		//HashSet<Integer> importantLocations = EpiSimUtil.getLocsHighOutDegRestHomeDist(numLocations, minHomeDist, percentile);
		BufferedBigIntSet importantLocations = EpiSimUtil.getLocsHighOutDegRestHomeDist(numLocations, minHomeDist, percentile);
		int score = 0;
		long startTime = 0;
		long endTime = EpiSimUtil.dayToSeconds(100);
		long timeStep = EpiSimUtil.dayToSeconds(1);
		System.out.println("Restricted location closure simulation with vaccination.");
		System.out.println("Min home-distance ("+percentile+"th percentile):\t"+minHomeDist);
		System.out.println("Number of locations:\t"+numLocations);
		System.out.println("Closure threshold:\t"+threshold);
		System.out.println("start time (seconds):\t"+startTime);
		System.out.println("end time (seconds):\t"+endTime);
		System.out.println("time step (seconds):\t"+timeStep);
		System.out.println("Day\tscore\tnum inf.\tnum closed");
		boolean trackInfLoc = true;
		VaccinationSimulation sim = new VaccinationSimulation(trackInfLoc);
		HashMap<Integer,Integer> locations = sim.infectedLocations;
		Iterator<Integer> locationsItr = importantLocations.iterator();
		while (locationsItr.hasNext()) {
			locations.put(locationsItr.next(), new Integer(0));
		}
		sim.getIntitiallyInfected();
		sim.stepSim(startTime);
		while (sim.curTime < endTime) {
			locationsItr = importantLocations.iterator();
			while (locationsItr.hasNext()) {
				Integer location = locationsItr.next();
				if (!sim.closedLocations.contains(location)) {
					double numInfected = 0.0;
					if (locations.containsKey(location))
						numInfected = locations.get(location).doubleValue();
					double aggPop = (double) EpiSimUtil.getAggPop(location.intValue());
					if (numInfected / aggPop >= threshold) sim.closeLocPlus(location);
				}
			}
			score += sim.getNumIncapacitedPeople();
			System.out.println((sim.curTime/(60*60*24))+"\t"+score+"\t"+sim.numInfected+"\t"+sim.closedLocations.size());
			if ((sim.curTime/(60*60*24)) == 85 || (sim.curTime/(60*60*24)) == 90 || (sim.curTime/(60*60*24)) == 95) {
				System.out.println("Number of distinct people at closed locations:\t"
						+EpiSimUtil.getNumDistPeopleAtLocations(sim.closedLocations));
			}
			sim.stepSim(timeStep);
		}
		System.out.println("Total score:\t"+score);
		System.out.println("Number of distinct people at closed locations:\t"
				+EpiSimUtil.getNumDistPeopleAtLocations(sim.closedLocations));
	}
	
	public static void buildingClosureSimPlus(double threshold, int numLocations, double minHomeDist, int percentile, int llthresh) {
		//HashSet<Integer> importantLocations = EpiSimUtil.getLocsHighOutDegRestHomeDist(numLocations, minHomeDist, percentile);
		BufferedBigIntSet importantLocations = EpiSimUtil.getLocsHighOutDegRestHomeDist(numLocations, minHomeDist, percentile);
		int score = 0;
		long startTime = 0;
		long endTime = EpiSimUtil.dayToSeconds(100);
		long timeStep = EpiSimUtil.dayToSeconds(1);
		System.out.println("Restricted location closure simulation with family vaccination and additional locs.");
		System.out.println("LL inter count threshold: "+llthresh);
		System.out.println("Min home-distance ("+percentile+"th percentile):\t"+minHomeDist);
		System.out.println("Number of locations:\t"+numLocations);
		System.out.println("Closure threshold:\t"+threshold);
		System.out.println("start time (seconds):\t"+startTime);
		System.out.println("end time (seconds):\t"+endTime);
		System.out.println("time step (seconds):\t"+timeStep);
		System.out.println("Day\tscore\tnum inf.\tnum closed");
		boolean trackInfLoc = true;
		VaccinationSimulation sim = new VaccinationSimulation(trackInfLoc);
		HashMap<Integer,Integer> locations = sim.infectedLocations;
		Iterator<Integer> locationsItr = importantLocations.iterator();
		while (locationsItr.hasNext()) {
			locations.put(locationsItr.next(), new Integer(0));
		}
		sim.getIntitiallyInfected();
		sim.stepSim(startTime);
		while (sim.curTime < endTime) {
			locationsItr = importantLocations.iterator();
			while (locationsItr.hasNext()) {
				Integer location = locationsItr.next();
				if (!sim.closedLocations.contains(location)) {
					double numInfected = 0.0;
					if (locations.containsKey(location))
						numInfected = locations.get(location).doubleValue();
					double aggPop = (double) EpiSimUtil.getAggPop(location.intValue());
					if (numInfected / aggPop >= threshold) sim.closeLocPlus(location, llthresh);
				}
			}
			score += sim.getNumIncapacitedPeople();
			System.out.println((sim.curTime/(60*60*24))+"\t"+score+"\t"+sim.numInfected+"\t"+sim.closedLocations.size());
			if ((sim.curTime/(60*60*24)) == 85 || (sim.curTime/(60*60*24)) == 90 || (sim.curTime/(60*60*24)) == 95) {
				System.out.println("Number of distinct people at closed locations:\t"
						+EpiSimUtil.getNumDistPeopleAtLocations(sim.closedLocations));
			}
			sim.stepSim(timeStep);
		}
		System.out.println("Total score:\t"+score);
		System.out.println("Number of distinct people at closed locations:\t"
				+EpiSimUtil.getNumDistPeopleAtLocations(sim.closedLocations));
	}
	
	public static void buildingClosureSim(double threshold) {
		int score = 0;
		long startTime = 0;
		long endTime = EpiSimUtil.dayToSeconds(100);
		long timeStep = EpiSimUtil.dayToSeconds(1);
		System.out.println("Simple location closure simulation.");
		System.out.println("Closure threshold:\t"+threshold);
		System.out.println("start time (seconds):\t"+startTime);
		System.out.println("end time (seconds):\t"+endTime);
		System.out.println("time step (seconds):\t"+timeStep);
		System.out.println("Day\tscore\tnum inf.\tnum closed");
		boolean trackInfLoc = true;
		VaccinationSimulation sim = new VaccinationSimulation(trackInfLoc);
		sim.getIntitiallyInfected();
		sim.stepSim(startTime);
		while (sim.curTime < endTime) {
			HashMap<Integer,Integer> locations = sim.infectedLocations;
			Iterator<Integer> locationsItr = locations.keySet().iterator();
			while (locationsItr.hasNext()) {
				Integer location = locationsItr.next();
				if (!sim.closedLocations.contains(location)) {
					double numInfected = locations.get(location).doubleValue();
					double aggPop = (double) EpiSimUtil.getAggPop(location.intValue());
					if (numInfected / aggPop >= threshold) sim.closedLocations.add(location);
				}
			}
			score += sim.getNumIncapacitedPeople();
			System.out.println((sim.curTime/(60*60*24))+"\t"+score+"\t"+sim.numInfected+"\t"+sim.closedLocations.size());
			sim.stepSim(timeStep);
		}
		System.out.println("Total score: "+score);
	}
	
//	public static void buildingClosureSim(double threshold) {
//		int score = 0;
//		long startTime = 0;
//		long endTime = EpiSimUtil.dayToSeconds(100);
//		long timeStep = EpiSimUtil.dayToSeconds(1);
//		System.out.println("Simple location closure simulation.");
//		System.out.println("Closure threshold:\t"+threshold);
//		System.out.println("start time (seconds):\t"+startTime);
//		System.out.println("end time (seconds):\t"+endTime);
//		System.out.println("time step (seconds):\t"+timeStep);
//		System.out.println("Day\tscore\tnum inf.\tnum closed");
//		VaccinationSimulation sim = new VaccinationSimulation();
//		sim.stepSim(startTime);
//		while (sim.curTime < endTime) {
//			HashMap<Integer,Integer> locations = sim.getNumInfectedPeopleAtLocations();
//			Iterator<Integer> locationsItr = locations.keySet().iterator();
//			while (locationsItr.hasNext()) {
//				Integer location = locationsItr.next();
//				if (!sim.closedLocations.contains(location)) {
//					double numInfected = locations.get(location).doubleValue();
//					double aggPop = (double) EpiSimUtil.getAggPop(location.intValue());
//					if (numInfected / aggPop >= threshold) sim.closedLocations.add(location);
//				}
//			}
//			score += sim.getNumIncapacitedPeople();
//			System.out.println((sim.curTime/(60*60*24))+"\t"+score+"\t"+sim.numInfected+"\t"+sim.closedLocations.size());
//			sim.stepSim(timeStep);
//		}
//		System.out.println("Total score: "+score);
//	}
	
	/**
	 * Vaccination Simulation over spread of number of vaccines where random individuals are vaccinated.  
	 * @param numRuns number of trial to average the results over
	 */
	public static void randomSpreadSimulation(int numRuns){
		int[] numInfected = new int[numberOfVaccinesSpread.length];
		for (int i = 0; i < numberOfVaccinesSpread.length; i++){
			int numVaccines = numberOfVaccinesSpread[i];
			for (int j = 0; j < numRuns; j++){
				numInfected[i] = randomSimulation(numVaccines);
			}
			numInfected[i] /= 1;
		}
		System.out.println("NumVaccines\tNumInfected");
		System.out.println("------------------------");
		for (int i = 0; i < numberOfVaccinesSpread.length; i++){
			System.out.println(numberOfVaccinesSpread[i]+"\t"+numInfected[i]);
		}
	}
	
	/**
	 * 
	 *
	 */
	public static void degreeSpreadLargestHomeDistFracSocDistSimulation(){
		double[] desiredFracAggPopVacc = {1.0,0.8,0.6,0.4,0.2};
		int[][] numInfected = new int[desiredFracAggPopVacc.length][numberOfVaccinesSpread.length];
		
		for (int j = 0; j < desiredFracAggPopVacc.length; j++){
			double fraction = desiredFracAggPopVacc[j];
			for (int i = 0; i < numberOfVaccinesSpread.length; i++){
				int numVaccines = numberOfVaccinesSpread[i];
				numInfected[j][i] = largestHomeDistFracSociableDistSimulation(numVaccines, fraction);
			}
		}
		System.out.print("NumVaccines");
		for (int i = 0; i < desiredFracAggPopVacc.length; i++){
			System.out.print("\t"+desiredFracAggPopVacc[i]+"Vaccs/Loc");
		}
		System.out.println();
		System.out.println("---------------------------------------------------------------");
		for (int i = 0; i < numberOfVaccinesSpread.length; i++){
			System.out.print(numberOfVaccinesSpread[i]);
			for (int j = 0; j < desiredFracAggPopVacc.length; j++)
				System.out.print("\t"+numInfected[j][i]);
			System.out.println();
		}
	}
	
	/**
	 * 
	 *
	 */
	public static void getNumLocationsForDegreeSpreadMostSocLocFracSocDistSimulation(){
		double[] desiredFracAggPopVacc = {1.0,0.8,0.6,0.4,0.2};
		int[][] numInfected = new int[desiredFracAggPopVacc.length][numberOfVaccinesSpread.length];
		
		for (int j = 0; j < desiredFracAggPopVacc.length; j++){
			double fraction = desiredFracAggPopVacc[j];
			for (int i = 0; i < numberOfVaccinesSpread.length; i++){
				int numVaccines = numberOfVaccinesSpread[i];
				numInfected[j][i] = numLocForMostSocLocsFracSociableDistSimulation(numVaccines, fraction);
			}
		}
		System.out.print("NumVaccines");
		for (int i = 0; i < desiredFracAggPopVacc.length; i++){
			System.out.print("\t"+desiredFracAggPopVacc[i]+"Vaccs/Loc");
		}
		System.out.println();
		System.out.println("---------------------------------------------------------------");
		for (int i = 0; i < numberOfVaccinesSpread.length; i++){
			System.out.print(numberOfVaccinesSpread[i]);
			for (int j = 0; j < desiredFracAggPopVacc.length; j++)
				System.out.print("\t"+numInfected[j][i]);
			System.out.println();
		}
	}
	
	/**
	 * 
	 *
	 */
	public static void degreeSpreadMostSocLocRestByHomeDistFracSocDistSimulation(int percentile, double minHomeDist, double maxHomeDist){
		double[] desiredFracAggPopVacc = {1.0,0.8,0.6,0.4,0.2};
		int[][] numInfected = new int[desiredFracAggPopVacc.length][numberOfVaccinesSpread.length];
		
		for (int j = 0; j < desiredFracAggPopVacc.length; j++){
			double fraction = desiredFracAggPopVacc[j];
			for (int i = 0; i < numberOfVaccinesSpread.length; i++){
				int numVaccines = numberOfVaccinesSpread[i];
				numInfected[j][i] = VaccinationSimulation.mostSocLocsRestByHomeDistFracSociableDistSimulation(numVaccines, fraction, percentile, minHomeDist, maxHomeDist);
			}
		}
		System.out.print("NumVaccines");
		for (int i = 0; i < desiredFracAggPopVacc.length; i++){
			System.out.print("\t"+desiredFracAggPopVacc[i]+"Vaccs/Loc");
		}
		System.out.println();
		System.out.println("---------------------------------------------------------------");
		for (int i = 0; i < numberOfVaccinesSpread.length; i++){
			System.out.print(numberOfVaccinesSpread[i]);
			for (int j = 0; j < desiredFracAggPopVacc.length; j++)
				System.out.print("\t"+numInfected[j][i]);
			System.out.println();
		}
	}
	
	/**
	 * 
	 *
	 */
	public static void degreeSpreadMostSocLocRestByHomeDistRandFracDistSimulation(int percentile, double minHomeDist, double maxHomeDist){
		double[] desiredFracAggPopVacc = {1.0,0.8,0.6,0.4,0.2};
		int[][] numInfected = new int[desiredFracAggPopVacc.length][numberOfVaccinesSpread.length];
		
		for (int j = 0; j < desiredFracAggPopVacc.length; j++){
			double fraction = desiredFracAggPopVacc[j];
			for (int i = 0; i < numberOfVaccinesSpread.length; i++){
				int numVaccines = numberOfVaccinesSpread[i];
				numInfected[j][i] = VaccinationSimulation.mostSocLocsRestByHomeDistFracRandDistSimulation(numVaccines, fraction, percentile, minHomeDist, maxHomeDist);
			}
		}
		System.out.print("NumVaccines");
		for (int i = 0; i < desiredFracAggPopVacc.length; i++){
			System.out.print("\t"+desiredFracAggPopVacc[i]+"Vaccs/Loc");
		}
		System.out.println();
		System.out.println("---------------------------------------------------------------");
		for (int i = 0; i < numberOfVaccinesSpread.length; i++){
			System.out.print(numberOfVaccinesSpread[i]);
			for (int j = 0; j < desiredFracAggPopVacc.length; j++)
				System.out.print("\t"+numInfected[j][i]);
			System.out.println();
		}
	}
	
	/**
	 * 
	 *
	 */
	public static void degreeSpreadMostSocLocFracSocDistSimulation(){
		double[] desiredFracAggPopVacc = {1.0,0.8,0.6,0.4,0.2};
		int[][] numInfected = new int[desiredFracAggPopVacc.length][numberOfVaccinesSpread.length];
		
		for (int j = 0; j < desiredFracAggPopVacc.length; j++){
			double fraction = desiredFracAggPopVacc[j];
			for (int i = 0; i < numberOfVaccinesSpread.length; i++){
				int numVaccines = numberOfVaccinesSpread[i];
				numInfected[j][i] = mostSocLocsFracSociableDistSimulation(numVaccines, fraction);
			}
		}
		System.out.print("NumVaccines");
		for (int i = 0; i < desiredFracAggPopVacc.length; i++){
			System.out.print("\t"+desiredFracAggPopVacc[i]+"Vaccs/Loc");
		}
		System.out.println();
		System.out.println("---------------------------------------------------------------");
		for (int i = 0; i < numberOfVaccinesSpread.length; i++){
			System.out.print(numberOfVaccinesSpread[i]);
			for (int j = 0; j < desiredFracAggPopVacc.length; j++)
				System.out.print("\t"+numInfected[j][i]);
			System.out.println();
		}
	}
	
	/**
	 * Vaccination Simulation over spread of number of vaccines and over spread of number of vaccines
	 * per location where locations are chosen randomly and vaccines are distributed at the locations 
	 * based on sociability.
	 *
	 */
	public static void degreeSpreadRandLLSimulation(){
		int[] desiredNumVaccsPerLoc = {10,100,1000,10000};
		int[][] numInfected = new int[desiredNumVaccsPerLoc.length][numberOfVaccinesSpread.length];
		
		for (int j = 0; j < desiredNumVaccsPerLoc.length; j++){
			int desiredNumVaccinesPerLocation = desiredNumVaccsPerLoc[j];
			for (int i = 0; i < numberOfVaccinesSpread.length; i++){
				int numVaccines = numberOfVaccinesSpread[i];
				int numLocations = (numVaccines / desiredNumVaccinesPerLocation) + 1;
				numInfected[j][i] = randomLocationsSociableDistSimulation(numVaccines, numLocations);
			}
		}
		System.out.print("NumVaccines");
		for (int i = 0; i < desiredNumVaccsPerLoc.length; i++){
			System.out.print("\t"+desiredNumVaccsPerLoc[i]+"Vaccs/Loc");
		}
		System.out.println();
		System.out.println("---------------------------------------------------------------");
		for (int i = 0; i < numberOfVaccinesSpread.length; i++){
			System.out.print(numberOfVaccinesSpread[i]);
			for (int j = 0; j < desiredNumVaccsPerLoc.length; j++)
				System.out.print("\t"+numInfected[j][i]);
			System.out.println();
		}
	}
	
	/**
	 * Vaccination Simulation over spread of number of vaccines and over spread of number of vaccines
	 * per location where locations are chosen based on degree and vaccines are distributed at the 
	 * locations based on sociability.
	 *
	 */
	public static void degreeSpreadSocLLSimulation(){
		int[] desiredNumVaccsPerLoc = {10,100,1000,10000};
		int[][] numInfected = new int[desiredNumVaccsPerLoc.length][numberOfVaccinesSpread.length];
		
		for (int j = 0; j < desiredNumVaccsPerLoc.length; j++){
			int desiredNumVaccinesPerLocation = desiredNumVaccsPerLoc[j];
			for (int i = 0; i < numberOfVaccinesSpread.length; i++){
				int numVaccines = numberOfVaccinesSpread[i];
				int numLocations = (numVaccines / desiredNumVaccinesPerLocation) + 1;
				numInfected[j][i] = mostSociableLocationsSociableDistSimulation(numVaccines, numLocations);
			}
		}
		System.out.print("NumVaccines");
		for (int i = 0; i < desiredNumVaccsPerLoc.length; i++){
			System.out.print("\t"+desiredNumVaccsPerLoc[i]+"Vaccs/Loc");
		}
		System.out.println();
		System.out.println("---------------------------------------------------------------");
		for (int i = 0; i < numberOfVaccinesSpread.length; i++){
			System.out.print(numberOfVaccinesSpread[i]);
			for (int j = 0; j < desiredNumVaccsPerLoc.length; j++)
				System.out.print("\t"+numInfected[j][i]);
			System.out.println();
		}
	}
	
	/**
	 * Vaccination Simulation over spread of number of vaccines and over spread of number of vaccines
	 * per location where locations are chosen based on degree and vaccines are distributed at the 
	 * locations based on sociability.  Edges in LL graph are restricted to a particular purpose.
	 * 
	 * @param purpose index for purpose array in EpiSimUtil
	 */
	public static void degreeSpreadSocLLSimulationPurp(int purpose){
		int[] desiredNumVaccsPerLoc = {10,100,1000,10000};
		int[][] numInfected = new int[desiredNumVaccsPerLoc.length][numberOfVaccinesSpread.length];
		
		for (int j = 0; j < desiredNumVaccsPerLoc.length; j++){
			int desiredNumVaccinesPerLocation = desiredNumVaccsPerLoc[j];
			for (int i = 0; i < numberOfVaccinesSpread.length; i++){
				int numVaccines = numberOfVaccinesSpread[i];
				int numLocations = (numVaccines / desiredNumVaccinesPerLocation) + 1;
				numInfected[j][i] = mostSociableLocationsSociableDistSimulationPurp(numVaccines, numLocations, purpose);
			}
		}
		System.out.print("NumVaccines");
		for (int i = 0; i < desiredNumVaccsPerLoc.length; i++){
			System.out.print("\t"+desiredNumVaccsPerLoc[i]+"Vaccs/Loc");
		}
		System.out.println();
		System.out.println("---------------------------------------------------------------");
		for (int i = 0; i < numberOfVaccinesSpread.length; i++){
			System.out.print(numberOfVaccinesSpread[i]);
			for (int j = 0; j < desiredNumVaccsPerLoc.length; j++)
				System.out.print("\t"+numInfected[j][i]);
			System.out.println();
		}
	}
	
	/**
	 * Vaccination Simulation over spread of number of vaccines where locations are chosen based on 
	 * degree and vaccines are distributed at the locations randomly.
	 *
	 */
	public static void degreeSpreadLLSimulation(){
		int[] numInfected = new int[numberOfVaccinesSpread.length];
		int desiredNumVaccinesPerLocation = 100;
		for (int i = 0; i < numberOfVaccinesSpread.length; i++){
			int numVaccines = numberOfVaccinesSpread[i];
			int numLocations = (numVaccines / desiredNumVaccinesPerLocation) + 1;
			numInfected[i] = mostSociableLocationsEvenlySimulation(numVaccines, numLocations);
		}
		System.out.println("NumVaccines\tNumInfected");
		System.out.println("------------------------");
		for (int i = 0; i < numberOfVaccinesSpread.length; i++){
			System.out.println(numberOfVaccinesSpread[i]+"\t"+numInfected[i]);
		}
	}
	
	/**
	 * Calculates the number of locations for simulations using spread of number of vaccines and spread 
	 * fixed number of vaccines per location
	 *
	 */
	public static void numLocationsForDegreeSpreadLLSim(){
		int[] desiredNumVaccsPerLoc = {10,100,1000,10000};
		for (int i = 0; i < numberOfVaccinesSpread.length; i++){
			for (int j = 0; j < desiredNumVaccsPerLoc.length; j++){
				int numVaccines = numberOfVaccinesSpread[i];
				int desiredNumVaccinesPerLocation = desiredNumVaccsPerLoc[j];
				int numLocations = (numVaccines / desiredNumVaccinesPerLocation) + 1;
				System.out.print(numLocations + "\t");
			}
			System.out.println();
		}
	}
	
	/**
	 * Vaccination SImulation over spread of number of vaccines where vaccines are distributed using two 
	 * different strategies: to the people with highest degree, and to the most influencial people 
	 * determined by random walks
	 *
	 */
	public static void degreeSpreadSimulation(){
		LinkedList<Integer> degreeCutOff = new LinkedList<Integer>();
		LinkedList<Integer> numInfectedSoc = new LinkedList<Integer>();
		LinkedList<Integer> numInfectedInf = new LinkedList<Integer>();
		try {
			PreparedStatement getPopCount = EpiSimUtil.dbConnect().prepareStatement(
					"SELECT COUNT(*) AS num FROM "+EpiSimUtil.conCountTbl+" WHERE tally >= ?");
			for (int i=200; i >= 30; i -=10){
				getPopCount.setInt(1, i);
				ResultSet popCount = getPopCount.executeQuery();
				popCount.first();
				int numVaccines = popCount.getInt(1);
				numInfectedSoc.add(new Integer(mostSociableSimulation(numVaccines)));
				numInfectedInf.add(new Integer(mostInfluenceSimulation(numVaccines)));
				degreeCutOff.add(new Integer(i));
			}
			Iterator<Integer> itrS = numInfectedSoc.iterator();
			Iterator<Integer> itrI = numInfectedInf.iterator();
			Iterator<Integer> itrD = degreeCutOff.iterator();
			while(itrS.hasNext() && itrI.hasNext() && itrD.hasNext()){
				System.out.println(itrD.next()+"\t"+itrS.next()+"\t"+itrI.next());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static int randomSimulation(int numVaccines){
		System.out.println("Random Vaccination");
		System.out.println("Number of vaccines: "+numVaccines);
		VaccinationSimulation sim = new VaccinationSimulation();
		sim.getIntitiallyInfected();
		sim.vaccinateRandom(numVaccines);
		sim.runSim();
		int numInfected = sim.numInfected(Long.MAX_VALUE-1);
		System.out.println("There are "+numInfected+" infected individuals.");
		System.out.println("There are "+sim.numPeople()+" individuals total.");
		System.out.println("======================================");
		return numInfected;
	}
	
	public static int mostSociableSimulation(int numVaccines){
		System.out.println("Most Sociable Simulation");
		System.out.println("Number of vaccines: "+numVaccines);
		VaccinationSimulation sim = new VaccinationSimulation();
		sim.getIntitiallyInfected();
		sim.vaccinateMostSociable(numVaccines);
		sim.runSim();
		int numInfected = sim.numInfected(Long.MAX_VALUE-1);
		System.out.println("There are "+numInfected+" infected individuals.");
		System.out.println("There are "+sim.numPeople()+" individuals total.");
		System.out.println("======================================");
		return numInfected;
	}
	
	public static int mostInfluenceSimulation(int numVaccines){
		System.out.println("Most Influencial Simulation");
		System.out.println("Number of vaccines: "+numVaccines);
		System.out.println("runID used: "+runID);
		VaccinationSimulation sim = new VaccinationSimulation();
		sim.getIntitiallyInfected();
		sim.vaccinateMostInfluencial(numVaccines, runID);
		sim.runSim();
		int numInfected = sim.numInfected(Long.MAX_VALUE-1);
		System.out.println("There are "+numInfected+" infected individuals.");
		System.out.println("There are "+sim.numPeople()+" individuals total.");
		System.out.println("======================================");
		return numInfected;
	}
	
	public static int mostSociableLocationsEvenlySimulation(int numVaccines, int numLocations){
		System.out.println("Most Sociable Location Simulation, even random distribution");
		System.out.println("Number of vaccines: "+numVaccines);
		System.out.println("Number of locations: "+numLocations);
		VaccinationSimulation sim = new VaccinationSimulation();
		sim.getIntitiallyInfected();
		int totalNumWaistedVaccs = sim.vaccinateMostSociableLocationsEvenly(numVaccines, numLocations);
		sim.runSim();
		int numInfected = sim.numInfected(Long.MAX_VALUE-1);
		System.out.println("There were "+totalNumWaistedVaccs+" waisted vaccines");
		System.out.println("There are "+numInfected+" infected individuals.");
		System.out.println("There are "+sim.numPeople()+" individuals total.");
		System.out.println("======================================");
		return numInfected;
	}
	
	public static int mostSociableLocationsSociableDistSimulation(int numVaccines, int numLocations){
		System.out.println("Most Sociable Location Simulation, most sociable distribution");
		System.out.println("Number of vaccines: "+numVaccines);
		System.out.println("Number of locations: "+numLocations);
		VaccinationSimulation sim = new VaccinationSimulation();
		sim.getIntitiallyInfected();
		sim.vaccinateMostSociableLocationsSociably(numVaccines, numLocations);
		sim.runSim();
		int numInfected = sim.numInfected(Long.MAX_VALUE-1);
		System.out.println("There are "+numInfected+" infected individuals.");
		System.out.println("There are "+sim.numPeople()+" individuals total.");
		System.out.println("======================================");
		return numInfected;
	}
	
	public static int mostSociableLocationsSociableDistSimulationPurp(int numVaccines, int numLocations, int purpose){
		System.out.println("Most Sociable Location Simulation, most sociable distribution, restricted to purpose "+EpiSimUtil.activityTypeNames[purpose]+" ("+purpose+")");
		System.out.println("Number of vaccines: "+numVaccines);
		System.out.println("Number of locations: "+numLocations);
		VaccinationSimulation sim = new VaccinationSimulation();
		sim.getIntitiallyInfected();
		sim.vaccinateMostSociableLocationsSociablyPurp(numVaccines, numLocations, purpose);
		sim.runSim();
		int numInfected = sim.numInfected(Long.MAX_VALUE-1);
		System.out.println("There are "+numInfected+" infected individuals.");
		System.out.println("There are "+sim.numPeople()+" individuals total.");
		System.out.println("======================================");
		return numInfected;
	}
	
	public static int randomLocationsSociableDistSimulation(int numVaccines, int numLocations){
		System.out.println("Random Location Simulation, most sociable distribution");
		System.out.println("Number of vaccines: "+numVaccines);
		System.out.println("Number of locations: "+numLocations);
		VaccinationSimulation sim = new VaccinationSimulation();
		sim.getIntitiallyInfected();
		sim.vaccinateRandomLocationsSociably(numVaccines, numLocations);
		sim.runSim();
		int numInfected = sim.numInfected(Long.MAX_VALUE-1);
		System.out.println("There are "+numInfected+" infected individuals.");
		System.out.println("There are "+sim.numPeople()+" individuals total.");
		System.out.println("======================================");
		return numInfected;
	}
	
	public static int largestHomeDistFracSociableDistSimulation(int numVaccines, double fraction){
		System.out.println("Largest Home Distance, most sociable distribution, set fraction of pop at location");
		System.out.println("Number of vaccines: "+numVaccines);
		System.out.println("Fraction of Population at a location vaccinated: "+fraction);
		VaccinationSimulation sim = new VaccinationSimulation();
		sim.getIntitiallyInfected();
		sim.vaccinateFracOfMostSocPeopleAtLargestHomeDist(numVaccines, fraction);
		sim.runSim();
		int numInfected = sim.numInfected(Long.MAX_VALUE-1);
		System.out.println("There are "+numInfected+" infected individuals.");
		System.out.println("There are "+sim.numPeople()+" individuals total.");
		System.out.println("======================================");
		return numInfected;
	}
	
	public static int mostSocLocsFracSociableDistSimulation(int numVaccines, double fraction){
		System.out.println("Highest Degree location, most sociable distribution, set fraction of pop at location");
		System.out.println("Number of vaccines: "+numVaccines);
		System.out.println("Fraction of Population at a location vaccinated: "+fraction);
		VaccinationSimulation sim = new VaccinationSimulation();
		sim.getIntitiallyInfected();
		sim.vaccinateFracOfMostSocPeopleAtHighDegLoc(numVaccines, fraction);
		sim.runSim();
		int numInfected = sim.numInfected(Long.MAX_VALUE-1);
		System.out.println("There are "+numInfected+" infected individuals.");
		System.out.println("There are "+sim.numPeople()+" individuals total.");
		System.out.println("======================================");
		return numInfected;
	}
	
	public static int numLocForMostSocLocsFracSociableDistSimulation(int numVaccines, double fraction){
		System.out.println("Highest Degree location, most sociable distribution, set fraction of pop at location");
		System.out.println("Number of vaccines: "+numVaccines);
		System.out.println("Fraction of Population at a location vaccinated: "+fraction);
		VaccinationSimulation sim = new VaccinationSimulation();
		sim.getIntitiallyInfected();
		int numLocations = sim.getNumLocForVaccinateFracOfMostSocPeopleAtHighDegLoc(numVaccines, fraction);
		System.out.println("Number of locations: "+numLocations);
		System.out.println("======================================");
		return numLocations;
	}
	
	public static int mostSocLocsRestByHomeDistFracSociableDistSimulation(int numVaccines, double fraction, int percentile, double minHomeDist, double maxHomeDist){
		System.out.println("Highest Degree location with home distance in interval ["+minHomeDist+","+maxHomeDist+"], most sociable distribution, set fraction of pop at location");
		System.out.println("Number of vaccines: "+numVaccines);
		System.out.println("Fraction of Population at a location vaccinated: "+fraction);
		VaccinationSimulation sim = new VaccinationSimulation();
		sim.getIntitiallyInfected();
		sim.vaccinateFracOfMostSocPeopleAtHighDegLocRestByHomeDist(numVaccines, fraction, percentile, minHomeDist, maxHomeDist);
		sim.runSim();
		int numInfected = sim.numInfected(Long.MAX_VALUE-1);
		System.out.println("There are "+numInfected+" infected individuals.");
		System.out.println("There are "+sim.numPeople()+" individuals total.");
		System.out.println("======================================");
		return numInfected;
	}
	
	public static int mostSocLocsRestByHomeDistFracRandDistSimulation(int numVaccines, double fraction, int percentile, double minHomeDist, double maxHomeDist){
		System.out.println("Highest Degree location with home distance in interval ["+minHomeDist+","+maxHomeDist+"], random distribution, set fraction of pop at location");
		System.out.println("Number of vaccines: "+numVaccines);
		System.out.println("Fraction of Population at a location vaccinated: "+fraction);
		VaccinationSimulation sim = new VaccinationSimulation();
		sim.getIntitiallyInfected();
		sim.vaccinateRandFracPeopleAtHighDegLocRestByHomeDist(numVaccines, fraction, percentile, minHomeDist, maxHomeDist);
		sim.runSim();
		int numInfected = sim.numInfected(Long.MAX_VALUE-1);
		System.out.println("There are "+numInfected+" infected individuals.");
		System.out.println("There are "+sim.numPeople()+" individuals total.");
		System.out.println("======================================");
		return numInfected;
	}
	
	public static void multiVaccDelayMultiDynVaccConOfInfPeople(long startTime, long incTime, int[] numVaccs, long[] vaccDelays) {
		System.out.println("Multi Vaccination effect Delay Sim.");
		for (int i=0; i < vaccDelays.length; i++) {
			vaccDelay = vaccDelays[i];
			multiDynVaccConOfInfPeople(startTime, incTime, numVaccs);
		}
	}
	
	public static void multiDynVaccConOfInfPeople(long startTime, long incTime, int[] numVaccs) {
		System.out.println("dynamic vaccination of contacts of infected people; multiple number of vaccines.");
		System.out.println("Vaccination effect delay: "+vaccDelay+"; Delay of syptom onset: "+sympDelay);
		System.out.println("start vaccinating: "+startTime+"; period of vaccination cycles: "+incTime);
		System.out.println("num vaccs"+"\t"+"num infected");
		int[] numInfected = new int[numVaccs.length];
		for (int i = 0; i < numVaccs.length; i++) {
			numInfected[i] = dynVaccConOfInfPeople(startTime, incTime, numVaccs[i]);
			System.out.println(numVaccs[i]+"\t"+numInfected[i]);
		}
	}
	
	public static int dynVaccConOfInfPeople(long startTime, long incTime, int numVaccs) {
		VaccinationSimulation sim = new VaccinationSimulation(numVaccs);
		sim.getIntitiallyInfected();
		sim.stepSim(startTime);
		while (sim.curTime <= EpiSimUtil.simEndTime && sim.numVaccines > 0) {
			sim.vaccinateContactsOfInfectedPeople(sim.curTime - getSympDelay());
			sim.stepSim(incTime);
		}
		sim.stepSim(EpiSimUtil.simEndTime - sim.curTime);
		int numInfected = sim.numInfected(Long.MAX_VALUE-1);
		return numInfected;
	}
	
	public static long getVaccDelay() {
		return vaccDelay;
	}
	
	public static long getSympDelay() {
		return sympDelay;
	}
	
	public void closeLocPlus(Integer location, int llthresh) {
		this.closedLocations.add(location);
		vaccFamilyOfPeopleAtLoc(location);
		closeNeighborLoc(location, llthresh);
	}
	
	public void closeNeighborLoc(Integer location, int llthresh) {
		try {
			Connection con = EpiSimUtil.dbConnect();
			ResultSet locsQ = con.createStatement().executeQuery(
					"SELECT location2ID, tally FROM "+EpiSimUtil.llInterCountTbl+" WHERE location1ID="+location+" ORDER BY tally DESC");
			while (locsQ.next()) {
				if (locsQ.getInt("tally") < llthresh) break;
				this.closedLocations.add(new Integer(locsQ.getInt("location2ID")));
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void closeLocPlus(Integer location) {
		this.closedLocations.add(location);
		vaccFamilyOfPeopleAtLoc(location);
	}
	
	public void vaccFamilyOfPeopleAtLoc(Integer location) {
		//HashSet<Integer> peopleAtLoc = EpiSimUtil.getPeopleAtLocation(location);
		Iterator<Integer> peopleAtLocItr = EpiSimUtil.getPeopleAtLocation(location).iterator();
		while (peopleAtLocItr.hasNext()) {
			Iterator<Integer> family = EpiSimUtil.getRelatives(peopleAtLocItr.next()).iterator();
			while (family.hasNext()) {
				Person p = getPerson(family.next());
				p.vaccinate(this.curTime);
			}
		}
	}
	
	public HashSet<Integer> getVaccinatedPeople(long time) {
		HashSet<Integer> vacc = new HashSet<Integer>(this.people.size());
		Iterator<Integer> peopleIds = this.people.keySet().iterator();
		while (peopleIds.hasNext()) {
			Integer id = peopleIds.next();
			if (this.people.get(id).isVaccinated(time))
				vacc.add(id);
		}
		return vacc;
	}
	
	public HashSet<Integer> getVaccinatedPeople() {
		return getVaccinatedPeople(Long.MIN_VALUE);
	}
	
	public HashSet<Integer> getInfectedPeople(long time) {
		HashSet<Integer> infected = new HashSet<Integer>(this.people.size());
		Iterator<Integer> itr = this.people.keySet().iterator();
		while(itr.hasNext()){
			Integer person = itr.next();
			if (this.people.get(person).isInfected(time))
				infected.add(person);
		}
		return infected;
	}
	
	public HashSet<Integer> getInfectedPeople() {
		return getInfectedPeople(Long.MAX_VALUE);
	}
	
	public int numInfected(long time){
		int numInfected = 0;
		Iterator<Integer> itr = this.people.keySet().iterator();
		while(itr.hasNext()){
			if (this.people.get(itr.next()).isInfected(time))
				numInfected++;
		}
		return numInfected;
	}
	
	public int numPeople(){
		return this.people.size();
	}
	
	public void getIntitiallyInfected(){
		try {
			Statement getInitInfected = this.fludb.createStatement();
			ResultSet initInfected = getInitInfected.executeQuery(
					"SELECT id FROM "+EpiSimUtil.initTbl);
			while(initInfected.next()){
				Person p = getPerson(new Integer(initInfected.getInt(1)));
				this.infect(p, 0);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void vaccinateRandom(int numVaccines){
		for (int i = 0; i < numVaccines; i++){
			Person p = getPerson(new Integer(rand.nextInt(EpiSimUtil.population) + 1));
			p.vaccinate(Long.MIN_VALUE);
		}
	}
	
	public void vaccinateMostSociable(int numVaccines){
		try {
			Statement getMostSociable = this.fludb.createStatement();
			ResultSet mostSociable = getMostSociable.executeQuery(
					"SELECT personID FROM "+EpiSimUtil.conCountTbl+" ORDER BY tally DESC LIMIT "+numVaccines);
			while(mostSociable.next()){
				Person p = getPerson(new Integer(mostSociable.getInt(1)));
				p.vaccinate(Long.MIN_VALUE);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void vaccinateMostInfluencial(int numVaccines, int runID){
		try {
			Statement getMostInfluencial = this.fludbGenData.createStatement();
			ResultSet mostInfluencial = getMostInfluencial.executeQuery(
					"SELECT personID FROM "+EpiSimUtil.ppInfTbl+" WHERE runID="+runID+
					" ORDER BY score DESC LIMIT "+numVaccines);
			while(mostInfluencial.next()){
				Person p = getPerson(new Integer(mostInfluencial.getInt(1)));
				p.vaccinate(Long.MIN_VALUE);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public int vaccinateMostSociableLocationsEvenly(int numVaccines, int numLocations){
		int numVaccinesPerLoaction = numVaccines / numLocations;
		HashMap<Integer, Integer> waistedVaccs = new HashMap<Integer,Integer>(numLocations);
		int totalNumWaistedVaccs = 0;
		try {
			Connection con = EpiSimUtil.dbConnect();
			PreparedStatement getPeople = con.prepareStatement(
					"SELECT personID FROM "+EpiSimUtil.actTbl+" WHERE locationID = ?");
			Statement getLocations = con.createStatement();
			ResultSet getLocationsQ = getLocations.executeQuery(
					"SELECT locationID FROM "+EpiSimUtil.llCountTbl+" ORDER BY outbound DESC LIMIT "+numLocations);
			while (getLocationsQ.next()){
				int numWaistedVaccs = 0;
				getPeople.setInt(1, getLocationsQ.getInt("locationID"));
				ResultSet getPeopleQ = getPeople.executeQuery();
				getPeopleQ.last();
				int numPeople = getPeopleQ.getRow();
				getPeopleQ.beforeFirst();
				for (int i = 0; i < numVaccinesPerLoaction; i++){
					getPeopleQ.absolute(rand.nextInt(numPeople) + 1);
					Person p = getPerson(new Integer(getPeopleQ.getInt("personID")));
					if (!p.isVaccinated(Long.MIN_VALUE)){
						p.vaccinate(Long.MIN_VALUE);
					} else {
						numWaistedVaccs++;
						totalNumWaistedVaccs++;
						//i--;
					}
					waistedVaccs.put(new Integer(getLocationsQ.getInt("locationID")), new Integer(numWaistedVaccs));
				}
			}
			con.close();
			return totalNumWaistedVaccs;
		} catch (Exception e){
			System.out.println(e);
			return -1;
		}
	}
	
	public void vaccinateMostSociableLocationsSociably(int numVaccines, int numLocations){
		int numVaccinesPerLoaction = numVaccines / numLocations;
		try {
			Connection con = EpiSimUtil.dbConnect();
			PreparedStatement getPeople = con.prepareStatement(
					"SELECT personID FROM "+EpiSimUtil.actTbl+" WHERE locationID = ?");
			PreparedStatement getScore = con.prepareStatement(
					"SELECT tally FROM "+EpiSimUtil.conCountTbl+" WHERE personID = ?");
			Statement getLocations = con.createStatement();
			ResultSet getLocationsQ = getLocations.executeQuery(
					"SELECT locationID FROM "+EpiSimUtil.llCountTbl+" ORDER BY outbound DESC LIMIT "+numLocations);
			while (getLocationsQ.next()){
				TreeSet<Person> people = new TreeSet<Person>();
				getPeople.setInt(1, getLocationsQ.getInt("locationID"));
				ResultSet getPeopleQ = getPeople.executeQuery();
				while (getPeopleQ.next()){
					int person = getPeopleQ.getInt("personID");
					getScore.setInt(1, person);
					ResultSet getScoreQ = getScore.executeQuery();
					getScoreQ.first();
					int score = getScoreQ.getInt("tally");
					Person p = getPerson(new Integer(person));
					p.setScore(score);
					people.add(p);
				}
				Iterator<Person> itr = people.iterator();
				for (int i = 0; i < numVaccinesPerLoaction && itr.hasNext(); i++){
					Person p = itr.next();
					if (!p.isVaccinated(Long.MIN_VALUE)){
						p.vaccinate(Long.MIN_VALUE);
					} 
				}
			}
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public void vaccinateMostSociableLocationsSociablyPurp(int numVaccines, int numLocations, int purpose){
		int numVaccinesPerLoaction = numVaccines / numLocations;
		try {
			Connection con = EpiSimUtil.dbConnect();
			PreparedStatement getPeople = con.prepareStatement(
					"SELECT personID FROM "+EpiSimUtil.actTbl+" WHERE locationID = ?");
			PreparedStatement getScore = con.prepareStatement(
					"SELECT tally FROM "+EpiSimUtil.conCountTbl+" WHERE personID = ?");
			Statement getLocations = con.createStatement();
			ResultSet getLocationsQ = getLocations.executeQuery(
					"SELECT locationID FROM "+EpiSimUtil.llCountPurpTbl+" WHERE purpose='"+EpiSimUtil.activityTypeNames[purpose]+"' ORDER BY outbound DESC LIMIT "+numLocations);
			while (getLocationsQ.next()){
				TreeSet<Person> people = new TreeSet<Person>();
				getPeople.setInt(1, getLocationsQ.getInt("locationID"));
				ResultSet getPeopleQ = getPeople.executeQuery();
				while (getPeopleQ.next()){
					int person = getPeopleQ.getInt("personID");
					getScore.setInt(1, person);
					ResultSet getScoreQ = getScore.executeQuery();
					getScoreQ.first();
					int score = getScoreQ.getInt("tally");
					Person p = getPerson(new Integer(person));
					p.setScore(score);
					people.add(p);
				}
				Iterator<Person> itr = people.iterator();
				for (int i = 0; i < numVaccinesPerLoaction && itr.hasNext(); i++){
					Person p = itr.next();
					if (!p.isVaccinated(Long.MIN_VALUE)){
						p.vaccinate(Long.MIN_VALUE);
					} 
				}
			}
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public void vaccinateRandomLocationsSociably(int numVaccines, int numLocations){
		int numVaccinesPerLoaction = numVaccines / numLocations;
		try {
			Connection con = EpiSimUtil.dbConnect();
			PreparedStatement getPeople = con.prepareStatement(
					"SELECT personID FROM "+EpiSimUtil.actTbl+" WHERE locationID = ?");
			PreparedStatement getScore = con.prepareStatement(
					"SELECT tally FROM "+EpiSimUtil.conCountTbl+" WHERE personID = ?");
			for (int k = 0; k < numLocations; k++){
				int location = rand.nextInt(EpiSimUtil.numLocations);
				TreeSet<Person> people = new TreeSet<Person>();
				getPeople.setInt(1, location);
				ResultSet getPeopleQ = getPeople.executeQuery();
				while (getPeopleQ.next()){
					int person = getPeopleQ.getInt("personID");
					getScore.setInt(1, person);
					ResultSet getScoreQ = getScore.executeQuery();
					getScoreQ.first();
					int score = getScoreQ.getInt("tally");
					Person p = getPerson(new Integer(person));
					p.setScore(score);
					people.add(p);
				}
				Iterator<Person> itr = people.iterator();
				for (int i = 0; i < numVaccinesPerLoaction && itr.hasNext(); i++){
					Person p = itr.next();
					if (!p.isVaccinated(Long.MIN_VALUE)){
						p.vaccinate(Long.MIN_VALUE);
					} 
				}
			}
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
//	public void vaccinateFracOfMostSocPeopleAtHighDegLoc(int numVaccines, double fraction){
//		int numVaccinesPerLoaction = numVaccines / numLocations;
//		try {
//			Connection con = EpiSimUtil.dbConnect();
//			PreparedStatement getPeople = con.prepareStatement(
//					"SELECT personID FROM "+EpiSimUtil.actTbl+" WHERE locationID = ?");
//			PreparedStatement getScore = con.prepareStatement(
//					"SELECT tally FROM "+EpiSimUtil.conCountTbl+" WHERE personID = ?");
//			Statement getLocations = con.createStatement();
//			ResultSet getLocationsQ = getLocations.executeQuery(
//					"SELECT locationID FROM "+EpiSimUtil.llCountTbl+" ORDER BY outbound DESC LIMIT "+numLocations);
//			while (getLocationsQ.next()){
//				TreeSet<Person> people = new TreeSet<Person>();
//				getPeople.setInt(1, getLocationsQ.getInt("locationID"));
//				ResultSet getPeopleQ = getPeople.executeQuery();
//				while (getPeopleQ.next()){
//					int person = getPeopleQ.getInt("personID");
//					getScore.setInt(1, person);
//					ResultSet getScoreQ = getScore.executeQuery();
//					getScoreQ.first();
//					int score = getScoreQ.getInt("tally");
//					Person p = getPerson(new Integer(person));
//					p.setScore(score);
//					people.add(p);
//				}
//				Iterator<Person> itr = people.iterator();
//				for (int i = 0; i < numVaccinesPerLoaction && itr.hasNext(); i++){
//					Person p = itr.next();
//					if (!p.isVaccinated(Long.MIN_VALUE)){
//						p.vaccinate(Long.MIN_VALUE);
//					} 
//				}
//			}
//			con.close();
//		} catch (Exception e){
//			System.out.println(e);
//		}
//	}
	
	public int getNumLocForVaccinateFracOfMostSocPeopleAtHighDegLoc(int numVaccines, double fraction) {
		int numLocations = 0;
		int numFetchRows = 100;
		try {
			Connection con = EpiSimUtil.dbConnect();
			PreparedStatement getAggPop = con.prepareStatement(
					"SELECT aggregatePop FROM "+EpiSimUtil.locTbl+" WHERE locationID = ?");
			PreparedStatement getLocs = con.prepareStatement(
					"SELECT locationID FROM "+EpiSimUtil.llCountTbl+" ORDER BY outbound DESC LIMIT ?,?");
			for (int i = 0; numVaccines > 0; i++) {
				getLocs.setInt(1, i*numFetchRows);
				getLocs.setInt(2, numFetchRows);
				ResultSet getLocsQ = getLocs.executeQuery();
				while (getLocsQ.next() && numVaccines > 0) {
					int location = getLocsQ.getInt("locationID");
					getAggPop.setInt(1, location);
					ResultSet getAggPopQ = getAggPop.executeQuery();
					if (getAggPopQ.first()) {
						double temp = fraction*getAggPopQ.getInt("aggregatePop");
						int curLocNumVaccs = (int) temp;
						if (numVaccines >= curLocNumVaccs) {
							numVaccines -= curLocNumVaccs;
							this.vaccinateMostSocPeopleAtLocation(location, curLocNumVaccs);
							numLocations++;
						} else {
							this.vaccinateMostSocPeopleAtLocation(location, numVaccines);
							numVaccines = 0;
							numLocations++;
						}
					} else {
						System.out.println("#####################################################");
						System.out.println("# Could not get aggregate population for location "+location+"; "+numVaccines+" waisted vaccines as a result.");
						System.out.println("#####################################################");
						numVaccines = 0;
					}
				}
			}
			con.close();
			return numLocations;
		} catch (Exception e){
			System.out.println(e);
			return -1;
		}
	}
	
	public void vaccinateFracOfMostSocPeopleAtLargestHomeDist(int numVaccines, double fraction) {
		int numFetchRows = 100;
		try {
			Connection con = EpiSimUtil.dbConnect();
			PreparedStatement getAggPop = con.prepareStatement(
					"SELECT aggregatePop FROM "+EpiSimUtil.locTbl+" WHERE locationID = ?");
			PreparedStatement getLocs = con.prepareStatement(
					"SELECT locationID FROM "+EpiSimUtil.locHomeDistPercentileTbl+" ORDER BY distance DESC LIMIT ?,?");
			for (int i = 0; numVaccines > 0; i++) {
				getLocs.setInt(1, i*numFetchRows);
				getLocs.setInt(2, numFetchRows);
				ResultSet getLocsQ = getLocs.executeQuery();
				while (getLocsQ.next() && numVaccines > 0) {
					int location = getLocsQ.getInt("locationID");
					getAggPop.setInt(1, location);
					ResultSet getAggPopQ = getAggPop.executeQuery();
					if (getAggPopQ.first()) {
						double temp = fraction*getAggPopQ.getInt("aggregatePop");
						int curLocNumVaccs = (int) temp;
						if (numVaccines >= curLocNumVaccs) {
							numVaccines -= curLocNumVaccs;
							this.vaccinateMostSocPeopleAtLocation(location, curLocNumVaccs);
						} else {
							this.vaccinateMostSocPeopleAtLocation(location, numVaccines);
							numVaccines = 0;
						}
					} else {
						System.out.println("#####################################################");
						System.out.println("# Could not get aggregate population for location "+location+"; "+numVaccines+" waisted vaccines as a result.");
						System.out.println("#####################################################");
						numVaccines = 0;
					}
				}
			}
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public void vaccinateFracOfMostSocPeopleAtHighDegLoc(int numVaccines, double fraction) {
		int numFetchRows = 100;
		try {
			Connection con = EpiSimUtil.dbConnect();
			PreparedStatement getAggPop = con.prepareStatement(
					"SELECT aggregatePop FROM "+EpiSimUtil.locTbl+" WHERE locationID = ?");
			PreparedStatement getLocs = con.prepareStatement(
					"SELECT locationID FROM "+EpiSimUtil.llCountTbl+" ORDER BY outbound DESC LIMIT ?,?");
			for (int i = 0; numVaccines > 0; i++) {
				getLocs.setInt(1, i*numFetchRows);
				getLocs.setInt(2, numFetchRows);
				ResultSet getLocsQ = getLocs.executeQuery();
				while (getLocsQ.next() && numVaccines > 0) {
					int location = getLocsQ.getInt("locationID");
					getAggPop.setInt(1, location);
					ResultSet getAggPopQ = getAggPop.executeQuery();
					if (getAggPopQ.first()) {
						double temp = fraction*getAggPopQ.getInt("aggregatePop");
						int curLocNumVaccs = (int) temp;
						if (numVaccines >= curLocNumVaccs) {
							numVaccines -= curLocNumVaccs;
							this.vaccinateMostSocPeopleAtLocation(location, curLocNumVaccs);
						} else {
							this.vaccinateMostSocPeopleAtLocation(location, numVaccines);
							numVaccines = 0;
						}
					} else {
						System.out.println("#####################################################");
						System.out.println("# Could not get aggregate population for location "+location+"; "+numVaccines+" waisted vaccines as a result.");
						System.out.println("#####################################################");
						numVaccines = 0;
					}
				}
			}
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public void vaccinateFracOfMostSocPeopleAtHighDegLocRestPurpose(int numVaccines, double fraction, int purpose) {
		int numFetchRows = 100;
		try {
			Connection con = EpiSimUtil.dbConnect();
			PreparedStatement getAggPop = con.prepareStatement(
					"SELECT aggregatePop FROM "+EpiSimUtil.locTbl+" WHERE locationID = ?");
			PreparedStatement getLocs = con.prepareStatement(
					"SELECT locationID FROM "+EpiSimUtil.llCountPurpTbl+" WHERE purpose='"+EpiSimUtil.activityTypeNames[purpose]+"' ORDER BY outbound DESC LIMIT ?,?");
			for (int i = 0; numVaccines > 0; i++) {
				getLocs.setInt(1, i*numFetchRows);
				getLocs.setInt(2, numFetchRows);
				ResultSet getLocsQ = getLocs.executeQuery();
				while (getLocsQ.next() && numVaccines > 0) {
					int location = getLocsQ.getInt("locationID");
					getAggPop.setInt(1, location);
					ResultSet getAggPopQ = getAggPop.executeQuery();
					if (getAggPopQ.first()) {
						double temp = fraction*getAggPopQ.getInt("aggregatePop");
						int curLocNumVaccs = (int) temp;
						if (numVaccines >= curLocNumVaccs) {
							numVaccines -= curLocNumVaccs;
							this.vaccinateMostSocPeopleAtLocation(location, curLocNumVaccs);
						} else {
							this.vaccinateMostSocPeopleAtLocation(location, numVaccines);
							numVaccines = 0;
						}
					} else {
						System.out.println("#####################################################");
						System.out.println("# Could not get aggregate population for location "+location+"; "+numVaccines+" waisted vaccines as a result.");
						System.out.println("#####################################################");
						numVaccines = 0;
					}
				}
			}
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public void vaccinateFracOfMostSocPeopleAtHighDegLocRestByHomeDist(int numVaccines, double fraction, int percentile, double minHomeDist, double maxHomeDist){
		HashMap distances = EpiSimUtil.locationsRestByHomeDist(minHomeDist, maxHomeDist, percentile);
		int numFetchRows = 100;
		try {
			Connection con = EpiSimUtil.dbConnect();
			PreparedStatement getAggPop = con.prepareStatement(
					"SELECT aggregatePop FROM "+EpiSimUtil.locTbl+" WHERE locationID = ?");
			PreparedStatement getLocs = con.prepareStatement(
					"SELECT locationID FROM "+EpiSimUtil.llCountTbl+" ORDER BY outbound DESC LIMIT ?,?");
			for (int i = 0; numVaccines > 0; i++) {
				getLocs.setInt(1, i*numFetchRows);
				getLocs.setInt(2, numFetchRows);
				ResultSet getLocsQ = getLocs.executeQuery();
				while (getLocsQ.next() && numVaccines > 0) {
					int location = getLocsQ.getInt("locationID");
					if (distances.containsKey(new Integer(location))) {
						getAggPop.setInt(1, location);
						ResultSet getAggPopQ = getAggPop.executeQuery();
						if (getAggPopQ.first()) {
							double temp = fraction*getAggPopQ.getInt("aggregatePop");
							int curLocNumVaccs = (int) temp;
							if (numVaccines >= curLocNumVaccs) {
								numVaccines -= curLocNumVaccs;
								this.vaccinateMostSocPeopleAtLocation(location, curLocNumVaccs);
							} else {
								this.vaccinateMostSocPeopleAtLocation(location, numVaccines);
								numVaccines = 0;
							}
						} else {
							System.out.println("#####################################################");
							System.out.println("# Could not get aggregate population for location "+location+"; "+numVaccines+" waisted vaccines as a result.");
							System.out.println("#####################################################");
							numVaccines = 0;
						}
					}
				}
			}
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public void vaccinateRandFracPeopleAtHighDegLocRestByHomeDist(int numVaccines, double fraction, int percentile, double minHomeDist, double maxHomeDist){
		HashMap distances = EpiSimUtil.locationsRestByHomeDist(minHomeDist, maxHomeDist, percentile);
		int numFetchRows = 100;
		try {
			Connection con = EpiSimUtil.dbConnect();
			PreparedStatement getAggPop = con.prepareStatement(
					"SELECT aggregatePop FROM "+EpiSimUtil.locTbl+" WHERE locationID = ?");
			PreparedStatement getLocs = con.prepareStatement(
					"SELECT locationID FROM "+EpiSimUtil.llCountTbl+" ORDER BY outbound DESC LIMIT ?,?");
			for (int i = 0; numVaccines > 0; i++) {
				getLocs.setInt(1, i*numFetchRows);
				getLocs.setInt(2, numFetchRows);
				ResultSet getLocsQ = getLocs.executeQuery();
				while (getLocsQ.next() && numVaccines > 0) {
					int location = getLocsQ.getInt("locationID");
					if (distances.containsKey(new Integer(location))) {
						getAggPop.setInt(1, location);
						ResultSet getAggPopQ = getAggPop.executeQuery();
						if (getAggPopQ.first()) {
							double temp = fraction*getAggPopQ.getInt("aggregatePop");
							int curLocNumVaccs = (int) temp;
							if (numVaccines >= curLocNumVaccs) {
								numVaccines -= this.vaccinateRandPeopleAtLocation(location, curLocNumVaccs);
							} else {
								this.vaccinateRandPeopleAtLocation(location, numVaccines);
								numVaccines = 0;
							}
						} else {
							System.out.println("#####################################################");
							System.out.println("# Could not get aggregate population for location "+location+"; "+numVaccines+" waisted vaccines as a result.");
							System.out.println("#####################################################");
							numVaccines = 0;
						}
					}
				}
			}
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public void vaccinateMostSocPeopleAtLocationOrig(int location, int numVaccines) {
		try {
			Connection con = EpiSimUtil.dbConnect();
			PreparedStatement getScore = con.prepareStatement(
					"SELECT tally FROM "+EpiSimUtil.conCountTbl+" WHERE personID = ?");
			Statement getPeople = con.createStatement();
			ResultSet getPeopleQ = getPeople.executeQuery(
					"SELECT personID FROM "+EpiSimUtil.actTbl+" WHERE locationID="+location);
			TreeSet<Person> people = new TreeSet<Person>();
			while (getPeopleQ.next()){
				int person = getPeopleQ.getInt("personID");
				getScore.setInt(1, person);
				ResultSet getScoreQ = getScore.executeQuery();
				getScoreQ.first();
				int score = getScoreQ.getInt("tally");
				Person p = getPerson(new Integer(person));
				p.setScore(score);
				people.add(p);
			}
			Iterator<Person> itr = people.iterator();
			for (int i = 0; i < numVaccines && itr.hasNext();){
				Person p = itr.next();
				if (!p.isVaccinated(Long.MIN_VALUE)){
					p.vaccinate(Long.MIN_VALUE);
					i++;
				} 
			}
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public void vaccinateMostSocPeopleAtLocation(int location, int numVaccines) {
		try {
			Connection con = EpiSimUtil.dbConnect();
			Statement getPeople = con.createStatement();
			ResultSet getPeopleQ = getPeople.executeQuery(
					"SELECT personID FROM "+EpiSimUtil.locPeopleSocRankTbl+" WHERE locationID="+location+" ORDER BY rank ASC");
			for (int i = 0; getPeopleQ.next() && i < numVaccines;){
				int person = getPeopleQ.getInt("personID");
				Person p = getPerson(new Integer(person));
				if (!p.isVaccinated(Long.MIN_VALUE)){
					p.vaccinate(Long.MIN_VALUE);
					i++;
				} 
			}
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public int vaccinateRandPeopleAtLocation(int location, int numVaccines) {
		try {
			Connection con = EpiSimUtil.dbConnect();
			Statement getPeople = con.createStatement();
			ResultSet getPeopleQ = getPeople.executeQuery(
					"SELECT personID FROM "+EpiSimUtil.locPeopleSocRankTbl+" WHERE locationID="+location);
			getPeopleQ.last();
			int numPeople = getPeopleQ.getRow();
			if (numPeople < numVaccines) {
				con.close();
				return vaccinateAllAtLocation(location);
			}
			LinkedList<Integer> peopleAtLoc = new LinkedList<Integer>();
			getPeopleQ.beforeFirst();
			while(getPeopleQ.next()){
				peopleAtLoc.add(new Integer(getPeopleQ.getInt("personID")));
			}
			int numVaccinated = 0;
			while (peopleAtLoc.size() > 0 && numVaccinated < numVaccines){
				int index = rand.nextInt(peopleAtLoc.size());
				Person p = getPerson(peopleAtLoc.get(index));
				peopleAtLoc.remove(index);
				if (!p.isVaccinated(Long.MIN_VALUE)){
					p.vaccinate(Long.MIN_VALUE);
					numVaccinated++;
				} 
			}
			con.close();
			return numVaccinated;
		} catch (Exception e){
			System.out.println(e);
			return 0;
		}
	}
	
	public int vaccinateAllAtLocation(int location) {
		try {
			Connection con = EpiSimUtil.dbConnect();
			Statement getPeople = con.createStatement();
			ResultSet getPeopleQ = getPeople.executeQuery(
					"SELECT personID FROM "+EpiSimUtil.locPeopleSocRankTbl+" WHERE locationID="+location);
			int numVaccinated = 0;
			while (getPeopleQ.next()){
				int person = getPeopleQ.getInt("personID");
				Person p = getPerson(new Integer(person));
				if (!p.isVaccinated(Long.MIN_VALUE)){
					p.vaccinate(Long.MIN_VALUE);
					numVaccinated++;
				} 
			}
			con.close();
			return numVaccinated;
		} catch (Exception e){
			System.out.println(e);
			return 0;
		}
	}
	
	public void vaccinateContactsOfInfectedPeople(long time) {
		Iterator<Integer> infected = getInfectedPeople(time).iterator();
		while (infected.hasNext() && this.numVaccines > 0) {
			vaccinateContacts(infected.next().intValue());
		}
	}
	
	/**
	 * Vaccinate immediate neighbors of person so that the vaccine will be 
	 * effective at the current time + whatever getVaccDelay returns.
	 * 
	 * @param person vaccinate the immediate neighbors of the person with this id
	 */
	public void vaccinateContacts(int person) {
		HashSet<Integer> contacts = EpiSimUtil.getContacts(person);
		Iterator<Integer> conItr = contacts.iterator();
		while (conItr.hasNext()) {
			Integer contact = conItr.next();
			Person p = getPerson(contact);
			if (this.numVaccines > 0) {
				p.vaccinate(this.curTime + getVaccDelay());
				this.numVaccines--;
			} else {
				return;
			}
		}
	}
	
	/**
	 * Vaccinate neighbors at given distance (or less) in contact graph of person so that the vaccine will be 
	 * effective at the current time + whatever getVaccDelay returns.
	 * BFS on contact graph so that if shortage of vaccines then closer neighbors get vaccinated first.
	 * 
	 * @param person vaccinate the neighbors of the person with this id
	 * @param distance distance in contact graph to vaccinate to
	 */
	public void vaccinateNeighbors(int person, int distance) {
		HashSet<Integer> contacts = EpiSimUtil.getContacts(person);
		Iterator<Integer> conItr = contacts.iterator();
		if (distance <= 0) return;
		while (conItr.hasNext()) {
			Integer contact = conItr.next();
			Person p = getPerson(contact);
			if (this.numVaccines > 0) {
				p.vaccinate(this.curTime + getVaccDelay());
				this.numVaccines--;
			} else {
				return;
			}
		}
		conItr = contacts.iterator();
		while (conItr.hasNext()) {
			vaccinateNeighbors(conItr.next().intValue(), distance - 1);
		}
	}
	
	public boolean isClosed(int location) {
		return this.closedLocations.contains(new Integer(location));
	}
	
	public boolean isOpen(int location) {
		return !this.closedLocations.contains(new Integer(location));
	}
	
	public HashMap<Integer,Integer> getNumInfectedPeopleAtLocations() {
		HashSet<Integer> infected = this.getInfectedPeople();
		this.numInfected = infected.size();
		HashMap<Integer,Integer> locations = new HashMap<Integer,Integer>(infected.size()*10);
		Iterator<Integer> infectedItr = infected.iterator();
		while (infectedItr.hasNext()) {
			Integer person = infectedItr.next();
			Iterator<Integer> locsItr = this.getLocationsVisited(person).iterator();
			while(locsItr.hasNext()) {
				Integer location = locsItr.next();
				if (locations.containsKey(locations)) {
					Integer tally = new Integer(locations.get(location).intValue()+1);
					locations.put(location, tally);
				} else {
					locations.put(location, new Integer(1));
				}
			}
		}
		return locations;
	}
	
//	public HashMap<Integer,Integer> getNumInfectedPeopleAtLocations(HashSet<Integer> locations) {
//		HashSet<Integer> infected = this.getInfectedPeople();
//		this.numInfected = infected.size();
//		Iterator<Integer> infectedItr = infected.iterator();
//		while (infectedItr.hasNext()) {
//			Integer person = infectedItr.next();
//			Iterator<Integer> locsItr = this.getLocationsVisited(person).iterator();
//			while(locsItr.hasNext()) {
//				Integer location = locsItr.next();
//				if (locations.containsKey(locations)) {
//					Integer tally = new Integer(locations.get(location).intValue()+1);
//					locations.put(location, tally);
//				} else {
//					locations.put(location, new Integer(1));
//				}
//			}
//		}
//		return locations;
//	}
	
	
	
	/**
	 * Returns the number of people kept from going to a closed location + number of infected people
	 */
	public int getNumIncapacitedPeople() {
		int numInfected = this.numInfected;
		int numClosedLocPeople = 0;
		Iterator<Integer> locationsItr = this.closedLocations.iterator();
		while (locationsItr.hasNext()) {
			Integer location = locationsItr.next();
			numClosedLocPeople += EpiSimUtil.getAggPop(location.intValue());
		}
		return numInfected+numClosedLocPeople;
	}
	
	public void runSim(){
		try {
			PreparedStatement getInfected = this.fludb.prepareStatement(
					"SELECT infectedID FROM "+EpiSimUtil.dendroConTbl+" WHERE victimID = ?");
			Statement getTransEvents = this.fludb.createStatement();
			ResultSet transEvents = getTransEvents.executeQuery(
					"SELECT victimID,time FROM "+EpiSimUtil.dendroTbl+" ORDER BY time ASC");
			while (transEvents.next()){
				Person victim = this.getPerson(new Integer(transEvents.getInt(1)));
				long time = transEvents.getLong(2);
				getInfected.setInt(1, victim.getID());
				ResultSet infectedIDs = getInfected.executeQuery();
				LinkedList<Person> infectedPeople = new LinkedList<Person>();
				boolean infectedPersonPresent = false;
				while(infectedIDs.next()){
					Integer infectedID = new Integer(infectedIDs.getInt(1));
					Person infectedPerson = getPerson(infectedID);
					infectedPeople.add(infectedPerson);
					infectedPersonPresent = infectedPersonPresent || infectedPerson.isInfected(time);
				}
				if (infectedPersonPresent){
					this.infect(victim, time);
					Iterator<Person> itr = infectedPeople.iterator();
					while (itr.hasNext()){
						this.infect(itr.next(), time);
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void stepSim(long timeStep) {
		long endTime = this.curTime + timeStep;
		try {
			PreparedStatement getInfected = this.fludb.prepareStatement(
					"SELECT infectedID FROM "+EpiSimUtil.dendroConTbl+" WHERE victimID = ?");
			Statement getTransEvents = this.fludb.createStatement();
			ResultSet transEvents = getTransEvents.executeQuery(
					"SELECT victimID,time,locationID FROM "+EpiSimUtil.dendroTbl+" WHERE time > "+this.curTime+
					" AND time <= "+endTime+" ORDER BY time ASC");
			while (transEvents.next()){
				if (isOpen(transEvents.getInt("locationID"))) {
					Person victim = this.getPerson(new Integer(transEvents.getInt("victimID")));
					long time = transEvents.getLong("time");
					getInfected.setInt(1, victim.getID());
					ResultSet infectedIDs = getInfected.executeQuery();
					LinkedList<Person> infectedPeople = new LinkedList<Person>();
					boolean infectedPersonPresent = false;
					while(infectedIDs.next()){
						Integer infectedID = new Integer(infectedIDs.getInt("infectedID"));
						Person infectedPerson = getPerson(infectedID);
						infectedPeople.add(infectedPerson);
						infectedPersonPresent = infectedPersonPresent || infectedPerson.isInfected(time);
					}
					if (infectedPersonPresent){
						this.infect(victim, time);
						Iterator<Person> itr = infectedPeople.iterator();
						while (itr.hasNext()){
							this.infect(itr.next(),time);
						}
					}
//					if (infectedPersonPresent){
//						boolean newInf = victim.infect(time);
//						if(newInf) {
//							this.numInfected++;
//							if(this.trackInfLoc)
//								this.incInfCountOfLocVisited(victim);
//						}
//						Iterator<Person> itr = infectedPeople.iterator();
//						while (itr.hasNext()){
//							Person p = itr.next();
//							newInf = p.infect(time);
//							if(newInf) {
//								this.numInfected++;
//								if(this.trackInfLoc)
//									this.incInfCountOfLocVisited(p);
//							}
//						}
//					}
				}
			}
			this.curTime = endTime;
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public Person getPerson(Integer id){
		if (this.people.containsKey(id)) return this.people.get(id);
		Person p = new Person(id.intValue());
		this.people.put(id, p);
		return p;
	}
	
	public HashSet<Integer> getLocationsVisited(Integer id){
		return getPerson(id).getLocationsVisited();
	}
	
	public void incInfCountOfLocVisited(Person p) {
		Iterator<Integer> locsItr = p.getLocationsVisited().iterator();
		while (locsItr.hasNext()) {
			Integer loc = locsItr.next();
			if (this.infectedLocations.containsKey(loc)) {
				Integer value = new Integer(this.infectedLocations.get(loc).intValue()+1);
				this.infectedLocations.put(loc, value);
			} //else {
				//this.infectedLocations.put(loc, new Integer(1));
			//}
		}
	}
	
	public boolean infect(Person p, long time) {
		boolean newInf = p.infect(time);
		if(newInf) {
			this.numInfected++;
			if(this.trackInfLoc)
				this.incInfCountOfLocVisited(p);
		}
		return newInf;
	}
	
	private class Person implements Comparable<Person>{
		int id;
		long vaccinated;
		long infected;
		int score;
		HashSet<Integer> locations;
		
		public Person(int id){
			this.id = id;
			this.vaccinated = Long.MAX_VALUE;
			this.infected = Long.MAX_VALUE;
			this.score = -1;
			this.locations = EpiSimUtil.getLocationsVisited(id);
		}
		
		public Person(int id, int score){
			this.id = id;
			this.vaccinated = Long.MAX_VALUE;
			this.infected = Long.MAX_VALUE;
			this.score = score;
			this.locations = EpiSimUtil.getLocationsVisited(id);
		}
		
		public int getID(){
			return this.id;
		}
		
		public HashSet<Integer> getLocationsVisited() {
			return this.locations;
		}
		
		public boolean isInfected(long time){
			if (this.vaccinated < this.infected) return false;
			return this.infected <= time;
		}
		
		public boolean isVaccinated(long time){
			if (this.infected <= this.vaccinated) return false;
			return this.vaccinated <= time;
		}
		
		public boolean vaccinate(long time){
			if (this.infected <= time) return false;
			if (this.vaccinated < time) return false;
			this.vaccinated = time;
			return true;
		}
		
		public boolean infect(long time){
			if (this.vaccinated < time) return false;
			if (this.infected < time) return false;
			this.infected = time;
			return true;
		}
		
		public void setScore(int score){
			this.score = score;
		}
		
		public int compareTo(Person other){
			int scoreDiff = other.score - this.score;
			if (scoreDiff == 0) return other.id - this.id;
			else return scoreDiff;
		}
	}

}
