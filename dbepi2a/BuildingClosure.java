package dbepi2a;

import java.util.*;
import java.io.*;

public class BuildingClosure implements Policy {
	
	public static long defaultClosureTime = 24*60*60*7;
	public static long symptomDelay = 24*60*60;
	
	public PrintStream infLog;
	public PrintStream buildLog;
	public PrintStream cumLog;
	
	public double detectionThreshold;
	public long closureDuration = defaultClosureTime;
	public int numClosingEvents = 0;
	public HashMap<Integer,Integer> trackLocations;
	public HashMap<Integer,Long> closedLocations = new HashMap<Integer,Long>();
	public HashMap<Integer,Integer> peopleAffected = new HashMap<Integer,Integer>();

	public BuildingClosure(double detectionThreshold, long closureDuration, HashMap<Integer,Integer> impLoc) {
		this.detectionThreshold = detectionThreshold;
		this.closureDuration = closureDuration;
		this.trackLocations = impLoc;
		try {
			this.infLog = new PrintStream("infLog-detect"+this.detectionThreshold+"dur"+this.closureDuration+"loc"+this.trackLocations.size()+".log");
			this.buildLog = new PrintStream("buildLog-detect"+this.detectionThreshold+"dur"+this.closureDuration+"loc"+this.trackLocations.size()+".log");
			this.cumLog = new PrintStream("cumLog-detect"+this.detectionThreshold+"dur"+this.closureDuration+"loc"+this.trackLocations.size()+".log");
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public BuildingClosure(double detectionThreshold, HashMap<Integer,Integer> impLoc) {
		this.detectionThreshold = detectionThreshold;
		this.trackLocations = impLoc;
	}

	public boolean isValidTransEvent(Integer infected, Integer victim,
			Integer location, long time) {
		if (this.isClosed(location, time)) {
			return false;
		}
		return true;
	}

	public void newInf(Integer victim, Integer location, long time) {
		this.infLog.println(time+"\t"+victim+"\t"+location);
		Iterator<Integer> locsVisited = EpiSimUtil.getLocationsVisited(victim.intValue()).iterator();
		while (locsVisited.hasNext()) {
			Integer locVisited = locsVisited.next();
			if (this.trackLocations.containsKey(locVisited)) {
				this.incInfectionCount(locVisited);
				if (!this.isClosed(locVisited, time) && this.fracInfected(locVisited) > this.detectionThreshold)
					this.closeLocation(locVisited, time+symptomDelay);
			}
		}
	}
	
	public void incInfectionCount(Integer location) {
		if (this.trackLocations.containsKey(location)) {
			int numSick = this.trackLocations.get(location).intValue();
			numSick += 1;
			this.trackLocations.put(location, new Integer(numSick));
		}
	}
	
	public double fracInfected(Integer location) {
		if (this.trackLocations.containsKey(location)) {
			return (double)this.trackLocations.get(location) / (double)EpiSimUtil.getAggPop(location.intValue());
		}
		return Double.NEGATIVE_INFINITY;
	}
	
	public void closeLocation(Integer location, long time) {
		this.buildLog.println(time+"\t"+location);
		this.closedLocations.put(location, new Long(time));
		this.numClosingEvents++;
		this.affectPeople(location);
	}
	
	public boolean isClosed(Integer location, long time) {
		if (this.closedLocations.containsKey(location)) {
			long closeTime = this.closedLocations.get(location).longValue();
			if (time >= closeTime && time < closeTime + this.getClosureDuration(location))
				return true;
		}
		return false;
	}
	
	public long getClosureDuration(Integer location) {
		return this.closureDuration;
	}
	
	public void affectPeople(Integer location) {
		Iterator<Integer> people = EpiSimUtil.getPeopleAtLocation(location).iterator();
		while (people.hasNext()) {
			Integer person = people.next();
			if (this.peopleAffected.containsKey(person)) {
				int oldCount = this.peopleAffected.get(person).intValue();
				this.peopleAffected.put(person, new Integer(oldCount+1));
			} else {
				this.peopleAffected.put(person, new Integer(1));
			}
		}
	}
	
	public void printCumStats(DendroReSim sim) {
		long startTime = sim.curTime-EpiSimUtil.dayToSeconds(1);
//		long endTime = sim.curTime;
		int day = EpiSimUtil.secondsToDays(startTime);
		this.cumLog.println(day+"\t"+sim.infections.size()+"\t"+this.peopleAffected.size()+"\t"+this.numClosingEvents);
	}
	
	
	
	/////////////////////////////////////// STATIC //////////////////////////////////////////

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		multiRun();
//		testRun2();
		printInfPerDay(args[0]);

	}
	
	public static void multiRun() {
		int[] numLocs = {1000,2000,5000,10000};
		double[] detect = {0.005,0.01,0.02,0.05};
		long[] duration = {EpiSimUtil.dayToSeconds(3),EpiSimUtil.dayToSeconds(7),EpiSimUtil.dayToSeconds(10),EpiSimUtil.dayToSeconds(14)};
		for (int numLocsIndex=0; numLocsIndex<numLocs.length; numLocsIndex++) {
			for (int detectIndex=0; detectIndex<detect.length; detectIndex++) {
				for (int durationIndex=0; durationIndex<duration.length; durationIndex++) {
					BuildingClosure policy =  
						new BuildingClosure(detect[detectIndex],duration[durationIndex],EpiSimUtil.getLocsHighOutDeg(numLocs[numLocsIndex]));
					ArrayList<Policy> policies = new ArrayList<Policy>();
					policies.add(policy);
					DendroReSim sim = new DendroReSim(policies);
					while (sim.curTime < EpiSimUtil.dayToSeconds(100) && sim.infections.size() < 56569) {
						sim.stepSim();
						policy.printCumStats(sim);
					}
				}
			}
		}
	}
	
	public static void testRun2() {
		BuildingClosure policy =  
			new BuildingClosure(0.001,EpiSimUtil.dayToSeconds(100),EpiSimUtil.getLocsHighOutDeg(10000));
		ArrayList<Policy> policies = new ArrayList<Policy>();
		policies.add(policy);
		DendroReSim sim = new DendroReSim(policies);
		while (sim.curTime < EpiSimUtil.dayToSeconds(100) && sim.infections.size() < 56569) {
			sim.stepSim();
			policy.printCumStats(sim);
		}
	}
	
	public static void testRun() {
		BuildingClosure policy =  new BuildingClosure(0.001,7*24*60*60,EpiSimUtil.getLocsHighPop(10000));
		ArrayList<Policy> policies = new ArrayList<Policy>();
		policies.add(policy);
		DendroReSim sim = new DendroReSim(policies);
		while(sim.curTime < 100*24*60*60) {
			sim.stepSim();
//			int day = EpiSimUtil.secondsToDays(sim.curTime);
//			double actNumSick = EpiSimUtil.getNumPeopleSick(sim.curTime);
//			double numSick = sim.infections.size();
//			double percent = (numSick - actNumSick)/actNumSick;
//			int build = policy.numClosingEvents;
//			System.out.println("Day "+day+":\t"+actNumSick+"\t"+numSick+"\t"+percent+"\t"+build+"\t"+policy.falseCount+"\t"+policy.compCount);
		}
	}
	
	public static void printInfPerDay(String inFile) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(inFile));
			String line;
			int infCount = 0;
			long endDay = EpiSimUtil.dayToSeconds(1);
			while ((line = in.readLine()) != null) {
				StringTokenizer tok = new StringTokenizer(line);
				long time = Long.parseLong(tok.nextToken());
				if (time <= endDay) {
					infCount++;
				} else {
					System.out.println(EpiSimUtil.secondsToDays(endDay)+"\t"+infCount);
					endDay += EpiSimUtil.dayToSeconds(1);
					infCount = 0;
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

}
