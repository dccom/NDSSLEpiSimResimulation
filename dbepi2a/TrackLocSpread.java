package dbepi2a;

import java.util.*;
//import java.sql.*;

public class TrackLocSpread implements Policy {
	
	public static final int USE_DIRECT_CON = 0;
	public static final int USE_FAMILY_CON = 1;
	
	public static final double[] DEFAULT_TYPE_LIMITS = {0,1,2,3,4,5,6,7,8};
	public static final double[] DEFAULT_DIST_LIMITS = {0.0,100.0,1000.0,10000.0,30000.0};
	public static final double[] DEFAULT_FRACINF_LIMITS = {0.0,0.005,0.01,0.1,0.2};
	public static final double[] DEFAULT_AGGPOP_LIMITS = {0,100,200,500,1000};
	
	private int graphSelection = USE_DIRECT_CON;
	
	private double detect = 0.01;
	
	private HashMap<String,double[]> binLimits = new HashMap<String,double[]>();
	
	private HashMap<Integer,Integer> infLoc = new HashMap<Integer,Integer>();
	private HashMap<Integer,Integer> aggPop = new HashMap<Integer,Integer>();
	private HashMap<Integer,Double> xs = new HashMap<Integer,Double>();
	private HashMap<Integer,Double> ys = new HashMap<Integer,Double>();
	private HashMap<Integer,Integer> actType = new HashMap<Integer,Integer>();
	private HashSet<Integer> detectedLocs = new HashSet<Integer>();
	private HashSet<Integer> newInfLoc = new HashSet<Integer>();
	private HashMap<Integer,TreeMap<Integer,Integer>> locCon = new HashMap<Integer,TreeMap<Integer,Integer>>();

	public TrackLocSpread() {
		this.setLimits();
	}

	public boolean isValidTransEvent(Integer infected, Integer victim,
			Integer location, long time) {
		return true;
	}

	public void newInf(Integer victim, Integer location, long time) {
		Iterator<Integer> locsVisited = EpiSimUtil.getLocationsVisited(victim.intValue()).iterator();
		while (locsVisited.hasNext()) {
			Integer locVisited = locsVisited.next();
			if (this.isInfLoc(locVisited))
				this.incInfCount(locVisited);
			else {
				this.incInfCount(locVisited);
				if (this.isInfLoc(locVisited))
					this.newDetectLoc(locVisited);
			}
		}
	}
	
	private void setLimits() {
		this.binLimits.put("type", DEFAULT_TYPE_LIMITS);
		this.binLimits.put("distance", DEFAULT_DIST_LIMITS);
		this.binLimits.put("fracInf", DEFAULT_FRACINF_LIMITS);
		this.binLimits.put("aggPop", DEFAULT_AGGPOP_LIMITS);
	}
	
	private void incInfCount(Integer location) {
		if (this.infLoc.containsKey(location)) {
			int count = this.infLoc.get(location).intValue();
			this.infLoc.put(location, new Integer(count+1));
		} else {
			this.infLoc.put(location, new Integer(1));
		}
	}
	
	private Integer getAggPop(Integer location) {
		if (!this.aggPop.containsKey(location))
			this.aggPop.put(location, new Integer(EpiSimUtil.getAggPop(location.intValue())));
		return this.aggPop.get(location);
	}
	
	private double getX(Integer location) {
		if (!this.xs.containsKey(location))
			this.xs.put(location, new Double(EpiSimUtil.getX(location.intValue())));
		return this.xs.get(location).doubleValue();
	}
	
	private double getY(Integer location) {
		if (!this.ys.containsKey(location))
			this.ys.put(location, new Double(EpiSimUtil.getY(location.intValue())));
		return this.ys.get(location).doubleValue();
	}
	
	private double getDist(Integer location1, Integer location2) {
		return Math.sqrt(Math.pow(this.getX(location1)-this.getX(location2),2.0) + Math.pow(this.getY(location1)-this.getY(location2),2.0));
	}
	
	private int getDomActType(Integer location) {
		if (!this.actType.containsKey(location))
			this.actType.put(location, new Integer(EpiSimUtil.getDomActType(location)));
		return this.actType.get(location).intValue();
	}
	
	private double fracInfected(Integer location) {
		if (this.infLoc.containsKey(location)) {
			return this.infLoc.get(location).doubleValue() / this.getAggPop(location).doubleValue();
		}
		return Double.NEGATIVE_INFINITY;
	}
	
	private boolean isInfLoc(Integer location) {
		if (this.fracInfected(location) >= this.detect)
			return true;
		return false;
	}
	
	private void newDetectLoc(Integer location) {
		this.newInfLoc.add(location);
	}
	
	private TreeMap<Integer,Integer> getLocCon(Integer location) {
		if (!this.locCon.containsKey(location)) {
			this.locCon.put(location, this.getConnectedLocs(location));
		}
		return this.locCon.get(location);
	}
	
	private TreeMap<Integer,Integer> getConnectedLocs(Integer location) {
		if (this.graphSelection == MonBuildClose.USE_DIRECT_CON) {
			return EpiSimUtil.getLocThatPointTo(location);
		} else {
			return EpiSimUtil.getLocThatPointToThroughFamily(location);
		}
	}
	
	private void calcStats() {
		double[] type = new double[this.binLimits.get("type").length];
		EpiSimUtil.initArray(0.0, type);
		double[] dist = new double[this.binLimits.get("distance").length];
		EpiSimUtil.initArray(0.0, dist);
		double[] inf  = new double[this.binLimits.get("fracInf").length];
		EpiSimUtil.initArray(0.0, inf);
		double[] pop  = new double[this.binLimits.get("aggPop").length];
		EpiSimUtil.initArray(0.0, pop);
		Iterator<Integer> newLocs = this.newInfLoc.iterator();
		while (newLocs.hasNext()) {
			Integer target = newLocs.next();
			TreeMap<Integer,Integer> conLoc = this.getLocCon(target);
			Iterator<Integer> conLocs = conLoc.keySet().iterator();
			while (conLocs.hasNext()) {
				Integer location = conLocs.next();
				
				double myType = this.getDomActType(location);
				EpiSimUtil.incBinCount(myType, type, this.binLimits.get("type"));
				
				double myDist = this.getDist(target, location);
				EpiSimUtil.incBinCount(myDist, dist, this.binLimits.get("distance"));
				
				double myInf = this.fracInfected(location);
				EpiSimUtil.incBinCount(myInf, inf, this.binLimits.get("fracInf"));
				
				double myPop = this.getAggPop(location);
				EpiSimUtil.incBinCount(myPop, pop, this.binLimits.get("aggPop"));
			}
		}
		
		type = EpiSimUtil.normalize(type);
		dist = EpiSimUtil.normalize(dist);
		inf = EpiSimUtil.normalize(inf);
		pop = EpiSimUtil.normalize(pop);
		
		for (int i=0; i < type.length; i++)
			System.out.print(type[i]+"\t");
		for (int i=0; i < dist.length; i++)
			System.out.print(dist[i]+"\t");
		for (int i=0; i < inf.length; i++)
			System.out.print(inf[i]+"\t");
		for (int i=0; i < pop.length; i++)
			System.out.print(pop[i]+"\t");
		
		System.out.println();
		
		flushNewDetLoc();
	}
	
	private void flushNewDetLoc() {
		Iterator<Integer> newLocs = this.newInfLoc.iterator();
		while (newLocs.hasNext()) {
			this.detectedLocs.add(newLocs.next());
		}
		this.newInfLoc = new HashSet<Integer>();
	}
	
	public void printBinHeaders() {
		String[] headers = EpiSimUtil.getBinHeaders(this.binLimits.get("fracInf"));
		for (int i=0; i < headers.length; i++)
			System.out.print(headers[i]+"\t");
		System.out.println();
	}
	
	
	///////////////////////////////////// STATIC ////////////////////////////////////////

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TrackLocSpread policy = new TrackLocSpread();
		policy.printBinHeaders();
//		ArrayList<Policy> policies = new ArrayList<Policy>();
//		policies.add(policy);
//		DendroReSim sim = new DendroReSim(policies);
//		for (int day=0; day < 5; day++) {
//			sim.stepSim();
//			policy.calcStats();
////			System.out.println("Day\t"+EpiSimUtil.secondsToDays(sim.curTime));
//		}
////		System.out.println("Number of locations that have sick people: "+policy.infLoc.size());

	}

}
