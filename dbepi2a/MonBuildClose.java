package dbepi2a;

import java.util.*;
import java.io.*;


public class MonBuildClose implements Policy {
	
	public static final int USE_DIRECT_CON = 0;
	public static final int USE_FAMILY_CON = 1;
	public static final int USE_SHAREDPEOPLE_CON = 2;
	
	public static int numNonZero = 0;
	public static double[] savedWeights;
	
	public static double alpha = 0.33;
	
	public static final long randSeed = 1170275580515L;
	public static Random rand = new Random(randSeed);
	
	public static long defaultClosureTime = 0;//24*60*60*7;
//	public static long symptomDelay = EpiSimUtil.dayToSeconds(2);
	
	private int graphSelection = USE_DIRECT_CON;
	private boolean sepPopSickMons = false;
	private boolean monUniformRand = false;
	private boolean useMonAgent = false;
	private boolean infCountCum = true;
	private boolean monAtRisk = false;
	private boolean actualInfCount = false;
	private boolean weightByPop = true;
	
	public PrintStream infLog;
	public PrintStream buildLog;
	public PrintStream cumLog;
	public PrintStream monLog;
	public PrintStream weightLog;
	public PrintStream effectLog;
	
	public DendroReSim mySim;
	public double detectionThreshold;
	public long closureDuration = defaultClosureTime;
	public int numClosingEvents = 0;
//	public SortedMap<Long,HashMap<Integer,Integer>> newInf = new TreeMap<Long,HashMap<Integer,Integer>>();
	public HashMap<Integer,Integer> trackLocations;
	public HashMap<Integer,Long> closedLocations = new HashMap<Integer,Long>();
//	public HashMap<Integer,Integer> peopleAffected = new HashMap<Integer,Integer>();
	
	public int numMonitors;
	public HashSet<Integer> monitoredLocs = null;
	public Integer[] trackLocsByIndex;
	public double[] trackLocsWeight;
	public double[] popWeight;
	public double[] sickEdgeWeight;
	public HashMap<Integer,Integer> aggPop;
	public HashMap<Integer,TreeMap<Integer,Integer>> locCon = new HashMap<Integer,TreeMap<Integer,Integer>>();
	public LocationMonitorAgent monAgent;
	
	public int numMonitorDays = 0;
	
	public MonBuildClose(int numMonitors, double detectionThreshold, long closureDuration, HashMap<Integer,Integer> impLoc, LocationMonitorAgent monAgent) {
		this.useMonAgent = true;
		this.monAgent = monAgent;
		this.monAgent.setClosingAgent(this);
		this.numMonitors = numMonitors;
		this.detectionThreshold = detectionThreshold;
		this.closureDuration = closureDuration;
		this.trackLocations = impLoc;
		this.aggPop = new HashMap<Integer,Integer>(this.trackLocations.size());
		this.trackLocsByIndex = new Integer[this.trackLocations.size()];
		this.trackLocsWeight = new double[this.trackLocations.size()];
		this.sickEdgeWeight = new double[this.trackLocations.size()];
		this.popWeight = new double[this.trackLocations.size()];
		Iterator<Integer> itr = this.trackLocations.keySet().iterator();
		for (int i=0; i < this.trackLocsByIndex.length && itr.hasNext(); i++) {
			this.trackLocsByIndex[i] = itr.next();
		}
//		this.refreshMonLocs();
		this.setPopWeights();
		this.initSickEdgeWeights();
		try {
			this.infLog = new PrintStream("infLog-mons"+this.numMonitors+"detect"+this.detectionThreshold+"dur"+this.closureDuration+"loc"+this.trackLocations.size()+".log");
			this.buildLog = new PrintStream("buildLog-mons"+this.numMonitors+"detect"+this.detectionThreshold+"dur"+this.closureDuration+"loc"+this.trackLocations.size()+".log");
			this.cumLog = new PrintStream("cumLog-mons"+this.numMonitors+"detect"+this.detectionThreshold+"dur"+this.closureDuration+"loc"+this.trackLocations.size()+".log");
			this.monLog = new PrintStream("monLog-mons"+this.numMonitors+"detect"+this.detectionThreshold+"dur"+this.closureDuration+"loc"+this.trackLocations.size()+".log");
			this.weightLog = new PrintStream("weightLog-mons"+this.numMonitors+"detect"+this.detectionThreshold+"dur"+this.closureDuration+"loc"+this.trackLocations.size()+".log");
			this.printWeightHeader();
			this.effectLog = new PrintStream("effectLog-mons"+this.numMonitors+"detect"+this.detectionThreshold+"dur"+this.closureDuration+"loc"+this.trackLocations.size()+".log");
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public MonBuildClose(int numMonitors, double detectionThreshold, long closureDuration, HashMap<Integer,Integer> impLoc) {
		this.numMonitors = numMonitors;
		this.detectionThreshold = detectionThreshold;
		this.closureDuration = closureDuration;
		this.trackLocations = impLoc;
		this.aggPop = new HashMap<Integer,Integer>(this.trackLocations.size());
		this.trackLocsByIndex = new Integer[this.trackLocations.size()];
		this.trackLocsWeight = new double[this.trackLocations.size()];
		this.sickEdgeWeight = new double[this.trackLocations.size()];
		this.popWeight = new double[this.trackLocations.size()];
		Iterator<Integer> itr = this.trackLocations.keySet().iterator();
		for (int i=0; i < this.trackLocsByIndex.length && itr.hasNext(); i++) {
			this.trackLocsByIndex[i] = itr.next();
		}
//		this.refreshMonLocs();
		this.setPopWeights();
		this.initSickEdgeWeights();
		try {
			this.infLog = new PrintStream("infLog-mons"+this.numMonitors+"detect"+this.detectionThreshold+"dur"+this.closureDuration+"loc"+this.trackLocations.size()+".log");
			this.buildLog = new PrintStream("buildLog-mons"+this.numMonitors+"detect"+this.detectionThreshold+"dur"+this.closureDuration+"loc"+this.trackLocations.size()+".log");
			this.cumLog = new PrintStream("cumLog-mons"+this.numMonitors+"detect"+this.detectionThreshold+"dur"+this.closureDuration+"loc"+this.trackLocations.size()+".log");
			this.monLog = new PrintStream("monLog-mons"+this.numMonitors+"detect"+this.detectionThreshold+"dur"+this.closureDuration+"loc"+this.trackLocations.size()+".log");
			this.weightLog = new PrintStream("weightLog-mons"+this.numMonitors+"detect"+this.detectionThreshold+"dur"+this.closureDuration+"loc"+this.trackLocations.size()+".log");
			this.printWeightHeader();
			this.effectLog = new PrintStream("effectLog-mons"+this.numMonitors+"detect"+this.detectionThreshold+"dur"+this.closureDuration+"loc"+this.trackLocations.size()+".log");
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public MonBuildClose(int numMonitors, double detectionThreshold, HashMap<Integer,Integer> impLoc) {
		this.numMonitors = numMonitors;
		this.detectionThreshold = detectionThreshold;
		this.trackLocations = impLoc;
		this.aggPop = new HashMap<Integer,Integer>(this.trackLocations.size());
		this.trackLocsByIndex = new Integer[this.trackLocations.size()];
		this.sickEdgeWeight = new double[this.trackLocations.size()];
		this.popWeight = new double[this.trackLocations.size()];
		Iterator<Integer> itr = this.trackLocations.keySet().iterator();
		for (int i=0; i < this.trackLocsByIndex.length && itr.hasNext(); i++) {
			this.trackLocsByIndex[i] = itr.next();
		}
//		this.refreshMonLocs();
		this.setPopWeights();
		this.initSickEdgeWeights();
		try {
			this.infLog = new PrintStream("infLog-detect"+this.detectionThreshold+"dur"+this.closureDuration+"loc"+this.trackLocations.size()+".log");
			this.buildLog = new PrintStream("buildLog-detect"+this.detectionThreshold+"dur"+this.closureDuration+"loc"+this.trackLocations.size()+".log");
			this.cumLog = new PrintStream("cumLog-detect"+this.detectionThreshold+"dur"+this.closureDuration+"loc"+this.trackLocations.size()+".log");
			this.weightLog = new PrintStream("weightLog-mons"+this.numMonitors+"detect"+this.detectionThreshold+"dur"+this.closureDuration+"loc"+this.trackLocations.size()+".log");
			this.printWeightHeader();
			this.effectLog = new PrintStream("effectLog-mons"+this.numMonitors+"detect"+this.detectionThreshold+"dur"+this.closureDuration+"loc"+this.trackLocations.size()+".log");
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void setPopWeights() {
		double total = 0.0;
		for (int i=0; i < this.trackLocsByIndex.length; i++) {
			Integer pop = new Integer(EpiSimUtil.getAggPop(this.trackLocsByIndex[i].intValue()));
			this.aggPop.put(this.trackLocsByIndex[i], pop);
			this.popWeight[i] = pop.doubleValue();
			total += this.popWeight[i];
		}
		for (int i=0; i < this.trackLocsByIndex.length; i++) {
			this.popWeight[i] = this.popWeight[i] / total;
			if (this.popWeight[i] == 0.0) {
				System.out.println("person "+trackLocsByIndex[i]+" has zero pop weight.");
			}
		}
	}
	
	public void initSickEdgeWeights() {
		for (int i=0; i < this.sickEdgeWeight.length; i++)
			this.sickEdgeWeight[i] = 1.0 / (double)this.sickEdgeWeight.length;
	}
	
	public void setMySim(DendroReSim sim) {
		this.mySim = sim;
	}
	
//	public void refreshMonLocs() {
//		monitoredLocs = new HashSet<Integer>(this.numMonitors);
//		this.calcWeights();
//		WeightedRandPerm perm = new WeightedRandPerm(rand, this.trackLocsWeight);
//		perm.reset(this.trackLocsWeight.length);
//		for (int i = 0; perm.hasNext() && i < this.numMonitors; i++) {
//			monitoredLocs.add(trackLocsByIndex[perm.next()]);
//		}
//	}
	
	public void refreshMonLocs() {
//		if (this.useDelay)
//			this.flushNewInf(this.mySim.curTime);
		if (this.numMonitors == 0 || this.trackLocations.size() == 0)
			return;
		else if (this.weightByPop)
			this.refreshMonLocsWeightByPop();
		else if (this.actualInfCount)
			this.refreshMonLocsActInfCount();
		else if (this.monAtRisk)
			this.refreshMonLocsAtRiskRand();
		else if (this.useMonAgent)
			this.refreshMonLocsUseMonAgent();
		else if (this.monUniformRand)
			this.refreshMonLocsUniform();
		else if (this.sepPopSickMons)
			this.refreshMonLocsSepPopSickmons();
		else
			this.refreshMonLocsUseAllMons();
	}
	
	private void refreshMonLocsActInfCount() {
		monitoredLocs = new HashSet<Integer>(this.numMonitors);
		this.calcWeightsActInfCount();
		
		WeightedRandPerm perm = new WeightedRandPerm(rand, this.trackLocsWeight);
		perm.reset(this.trackLocsWeight.length);
		for (int i = 0; perm.hasNext() && i < this.numMonitors;) {
			int index = perm.next();
			if (!this.isClosed(trackLocsByIndex[index], this.mySim.curTime)) {
				monitoredLocs.add(trackLocsByIndex[index]);
				i++;
			}
		}
	}
	
	private void calcWeightsActInfCount() {
		double base = 0.01;
		for (int index=0; index < this.trackLocsByIndex.length; index++) {
			this.trackLocsWeight[index] = (double)this.numInfected(this.trackLocsByIndex[index]) * 
			                                    (double)this.getAggPop(this.trackLocsByIndex[index]);
		}
		this.trackLocsWeight = EpiSimUtil.normalize(this.trackLocsWeight);
		for (int index=0; index < this.trackLocsWeight.length; index++) {
			this.trackLocsWeight[index] = base + (1.0-base)*this.trackLocsWeight[index];
		}
	}
	
	private void refreshMonLocsAtRisk() {
		TreeMap<Integer,Integer> atRiskLocs = new TreeMap<Integer,Integer>();
		Iterator<Integer> atRiskPeople = this.mySim.getAtRiskPeople().iterator();
		Iterator<Integer> implocs = this.trackLocations.keySet().iterator();
		while (implocs.hasNext()) {
			atRiskLocs.put(implocs.next(), new Integer(0));
		}
		while (atRiskPeople.hasNext()) {
			Integer person = atRiskPeople.next();
			Iterator<Integer> locs = this.mySim.locsVisited.get(person).iterator();
			while (locs.hasNext()) {
				Integer loc = locs.next();
				if (this.trackLocations.containsKey(loc));
					EpiSimUtil.incMapValue(atRiskLocs, loc, -1);
			}
		}
		Iterator<Integer> itr = atRiskLocs.keySet().iterator();
		monitoredLocs = new HashSet<Integer>(this.numMonitors);
		for (int i = 0; i < this.numMonitors;) {
			Integer next = itr.next();
			if (next != null && !this.monitoredLocs.contains(next)&& !this.isClosed(next, this.mySim.curTime)) {
				monitoredLocs.add(next);
				i++;
			}
		}
		System.out.println("Locations monitored with at-risk score >=\t"+atRiskLocs.get(itr.next()));
	}
	
	private void refreshMonLocsAtRiskRand() {
		double base = 0.05;
		TreeMap<Integer,Integer> atRiskLocs = new TreeMap<Integer,Integer>();
		Iterator<Integer> atRiskPeople = this.mySim.getAtRiskPeople().iterator();
		Iterator<Integer> implocs = this.trackLocations.keySet().iterator();
		while (implocs.hasNext()) {
			atRiskLocs.put(implocs.next(), new Integer(0));
		}
		while (atRiskPeople.hasNext()) {
			Integer person = atRiskPeople.next();
			Iterator<Integer> locs = this.mySim.locsVisited.get(person).iterator();
			while (locs.hasNext()) {
				Integer loc = locs.next();
				if (this.trackLocations.containsKey(loc));
					EpiSimUtil.incMapValue(atRiskLocs, loc, -1);
			}
		}
		double[] weights = new double[this.trackLocsByIndex.length];
		for (int i=0; i < this.trackLocsByIndex.length; i++) {
			weights[i] = atRiskLocs.get(this.trackLocsByIndex[i]).doubleValue();
		}
		weights = EpiSimUtil.normalize(weights);
		for (int i=0; i < weights.length; i ++) {
			weights[i] = base + (1 - base)*weights[i];
		}
		this.trackLocsWeight = weights;
		monitoredLocs = new HashSet<Integer>(this.numMonitors);
		
		WeightedRandPerm perm = new WeightedRandPerm(rand, this.trackLocsWeight);
		perm.reset(this.trackLocsWeight.length);
		for (int i = 0; perm.hasNext() && i < this.numMonitors;) {
			int index = perm.next();
			if (!this.isClosed(trackLocsByIndex[index], this.mySim.curTime)) {
				monitoredLocs.add(trackLocsByIndex[index]);
				i++;
			}
		}
	}
	

	private void refreshMonLocsUseMonAgent() {
//		System.out.println("made it!");
		if (this.monitoredLocs == null) {
			this.refreshMonLocsUniform();
			return;
		}
		this.trackLocsWeight = this.monAgent.calcWeights();
		monitoredLocs = new HashSet<Integer>(this.numMonitors);
		
		WeightedRandPerm perm = new WeightedRandPerm(rand, this.trackLocsWeight);
		perm.reset(this.trackLocsWeight.length);
		for (int i = 0; perm.hasNext() && i < this.numMonitors;) {
			int index = perm.next();
			if (!this.isClosed(trackLocsByIndex[index], this.mySim.curTime)) {
				monitoredLocs.add(trackLocsByIndex[index]);
				i++;
			}
		}
	}
	
	private void refreshMonLocsUniform() {
		monitoredLocs = new HashSet<Integer>(this.numMonitors);
		for (int i = 0; i < this.numMonitors;) {
			int index = rand.nextInt(this.trackLocsByIndex.length);
			if (!this.monitoredLocs.contains(trackLocsByIndex[index])&& !this.isClosed(trackLocsByIndex[index], this.mySim.curTime)) {
				monitoredLocs.add(trackLocsByIndex[index]);
				i++;
			}
		}
	}
	
	private void refreshMonLocsWeightByPop() {
		monitoredLocs = new HashSet<Integer>(this.numMonitors);
		this.calcWeights();
		
		WeightedRandPerm perm = new WeightedRandPerm(rand, this.popWeight);
		perm.reset(this.trackLocsWeight.length);
		for (int i = 0; perm.hasNext() && i < this.numMonitors;) {
			int index = perm.next();
			if (!this.isClosed(trackLocsByIndex[index], this.mySim.curTime)) {
				monitoredLocs.add(trackLocsByIndex[index]);
				i++;
			}
		}
	}
	
	private void refreshMonLocsUseAllMons() {
		monitoredLocs = new HashSet<Integer>(this.numMonitors);
		this.calcWeights();
		
		WeightedRandPerm perm = new WeightedRandPerm(rand, this.trackLocsWeight);
		perm.reset(this.trackLocsWeight.length);
		for (int i = 0; perm.hasNext() && i < this.numMonitors;) {
			int index = perm.next();
			if (!this.isClosed(trackLocsByIndex[index], this.mySim.curTime)) {
				monitoredLocs.add(trackLocsByIndex[index]);
				i++;
			}
		}
	}
	
	private void refreshMonLocsSepPopSickmons() {
		double numSickMons = this.getAlpha()*this.numMonitors;
		double numPopMons = this.numMonitors - numSickMons;
		monitoredLocs = new HashSet<Integer>(this.numMonitors);
		boolean allAtMin = this.calcWeights();
		
		// Add Sick monitors
		if (!allAtMin) {
			WeightedRandPerm perm = new WeightedRandPerm(rand, this.sickEdgeWeight);
			perm.reset(this.sickEdgeWeight.length);
			for (int i = 0; perm.hasNext() && i < numSickMons;) {
				int index = perm.next();
				if (this.sickEdgeWeight[index] != Double.MIN_VALUE 
						&& !this.isClosed(trackLocsByIndex[index], this.mySim.curTime)) {
					monitoredLocs.add(trackLocsByIndex[index]);
					i++;
				}
			}
		}
		
		// Add pop monitors
		WeightedRandPerm perm = new WeightedRandPerm(rand, this.popWeight);
		perm.reset(this.popWeight.length);
		for (int i = 0; perm.hasNext() && i < numPopMons;) {
			int index = perm.next();
			if (!monitoredLocs.contains(trackLocsByIndex[index]) 
					&& !this.isClosed(trackLocsByIndex[index], this.mySim.curTime)) {
				monitoredLocs.add(trackLocsByIndex[index]);
				i++;
			}
		}
	}
	
	public boolean calcWeights() {
		boolean allAtMin = true;
		double total = 0;
		if (this.getAlpha() != 0.0) {
			for (int i=0; i < this.trackLocsByIndex.length; i++) {
				Integer location = this.trackLocsByIndex[i];
				this.sickEdgeWeight[i] = this.calcUnNormWeight(location);
				total += this.sickEdgeWeight[i];
				if (this.sickEdgeWeight[i] != Double.MIN_VALUE)
					allAtMin = false;
			}
			if (total > 0.0) {
				for (int i=0; i < this.trackLocsWeight.length; i++) {
					this.sickEdgeWeight[i] = this.sickEdgeWeight[i] / total;
					if (this.sickEdgeWeight[i] <= 0.0) this.sickEdgeWeight[i] = Double.MIN_VALUE;
				}
			}
		}
		for (int i=0; i < this.trackLocsWeight.length; i++) {
//			if (this.varAlpha)
//				this.calcAlpha();
			this.trackLocsWeight[i] = (MonBuildClose.this.getAlpha()*this.sickEdgeWeight[i]) + ((1.0-MonBuildClose.this.getAlpha())*this.popWeight[i]);
		}
		return allAtMin;
	}
	
	public double calcUnNormWeight(Integer location) {
//		if (this.isClosed(location, this.mySim.curTime))
//			return Double.MIN_VALUE;
		double weight = this.getNumClosedLocCon(location);//*this.getAggPop(location).doubleValue();
		if (weight <= 0.0) weight = Double.MIN_VALUE;
		return weight;
	}
	
	public double getNumClosedLocCon(Integer location) {
		double count = 0.0;
		TreeMap<Integer,Integer> connections = getLocCon(location);
		Iterator<Integer> itr = connections.keySet().iterator();
		while (itr.hasNext()) {
			Integer loc = itr.next();
			if (this.isClosed(loc, this.mySim.curTime) 
					&& !this.isClosed(loc, this.mySim.curTime-EpiSimUtil.dayToSeconds(1))) {
				count += connections.get(loc).doubleValue();
//				System.out.println("location2ID: "+location+"\tlocation1ID: "+loc+"\ttally: "+connections.get(loc));
			}
		}
		if (count != 0.0)
			numNonZero++;
		return count;
	}
	
	public TreeMap<Integer,Integer> getLocCon(Integer location) {
		if (!this.locCon.containsKey(location)) {
			this.locCon.put(location, this.getConnectedLocs(location));
		}
		return this.locCon.get(location);
	}
	
	public TreeMap<Integer,Integer> getConnectedLocs(Integer location) {
		if (this.graphSelection == MonBuildClose.USE_DIRECT_CON) {
			return EpiSimUtil.getLocThatPointTo(location);
		} else if (this.graphSelection == MonBuildClose.USE_FAMILY_CON) {
			return EpiSimUtil.getLocThatPointToThroughFamily(location);
		} else {
			return EpiSimUtil.getLocWithSharedPeople(location);
		}
	}
	
	public Integer getAggPop(Integer location) {
		if (!this.aggPop.containsKey(location))
			this.aggPop.put(location, new Integer(EpiSimUtil.getAggPop(location.intValue())));
		return this.aggPop.get(location);
	}
	
	public double getAlpha() {
		return MonBuildClose.alpha;
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
				if (!this.isClosed(locVisited, time) && this.monitoredLocs.contains(locVisited) && this.fracInfected(locVisited) > this.detectionThreshold)
					this.closeLocation(locVisited, time);
			}
		}
	}
	
//	public void addToNewInf(Integer location, long time) {
//		Long timeIndex = new Long(time+symptomDelay);
//		if (this.newInf.containsKey(timeIndex)) {
//			if (this.newInf.get(timeIndex).containsKey(location)) {
//				int oldCount = this.newInf.get(timeIndex).get(location).intValue();
//				this.newInf.get(timeIndex).put(location, new Integer(oldCount+1));
//			} else {
//				this.newInf.get(timeIndex).put(location, new Integer(1));
//			}
//		} else {
//			HashMap<Integer,Integer> newInfByLoc = new HashMap<Integer,Integer>();
//			newInfByLoc.put(location, new Integer(1));
//			this.newInf.put(timeIndex, newInfByLoc);
//		}
//	}
	
//	public void flushNewInf(long time) {
//		Iterator<Long> itr = this.newInf.keySet().iterator();
//		while (itr.hasNext()) {
//			Long next = itr.next();
//			if (next.longValue() <= time) {
//				Iterator<Integer> itr2 = this.newInf.get(next).keySet().iterator();
//				while (itr2.hasNext()) {
//					Integer locVisited = itr2.next();
//					
//				}
//			}
//		}
//	}
	
	public void incInfectionCount(Integer location) {
		if (this.trackLocations.containsKey(location)) {
			int numSick = this.trackLocations.get(location).intValue();
			numSick += 1;
			this.trackLocations.put(location, new Integer(numSick));
		}
	}
	
	public double fracInfected(Integer location) {
		if (this.trackLocations.containsKey(location)) {
			return (double)this.numInfected(location) / (double)this.getAggPop(location);
		}
		return Double.NEGATIVE_INFINITY;
	}
	
	public int numInfected(Integer location) {
		if (this.infCountCum)
			return this.numInfectedCum(location);
		else
			return this.numInfectedInst(location);
			
	}
	
	public int numInfectedCum(Integer location) {
		if (this.trackLocations.containsKey(location)) {
			return this.trackLocations.get(location);
		}
		return Integer.MIN_VALUE;
	}
	
	public int numInfectedInst(Integer location) {
		int tally = 0;
		Iterator<Integer> people = this.mySim.getInfectiousPeople().iterator();
		while (people.hasNext()) {
			Integer p = people.next();
			if (this.mySim.locsVisited.get(p).contains(location))
				tally++;
		}
		return tally;
	}
	
	public void closeLocation(Integer location, long time) {
		this.buildLog.println(time+"\t"+location);
		this.closedLocations.put(location, new Long(time));
		this.numClosingEvents++;
//		this.affectPeople(location);
	}
	
	public void closeLocations(Iterator<Integer> locs, int day) {
		long time = EpiSimUtil.dayToSeconds(day);
		while (locs.hasNext()) {
			this.closeLocation(locs.next(), time);
		}
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
	
	public HashSet<Integer> getClosedLocations(long time) {
		HashSet<Integer> locs = new HashSet<Integer>();
		Iterator<Integer> itr = this.closedLocations.keySet().iterator();
		while (itr.hasNext()) {
			Integer next = itr.next();
			if (this.isClosed(next, time))
				locs.add(next);
		}
		return locs;
	}
	
//	public void affectPeople(Integer location) {
//		Iterator<Integer> people = EpiSimUtil.getPeopleAtLocation(location).iterator();
//		while (people.hasNext()) {
//			Integer person = people.next();
//			if (this.peopleAffected.containsKey(person)) {
//				int oldCount = this.peopleAffected.get(person).intValue();
//				this.peopleAffected.put(person, new Integer(oldCount+1));
//			} else {
//				this.peopleAffected.put(person, new Integer(1));
//			}
//		}
//	}
	
	public void printEffectStats() {
		int day = EpiSimUtil.secondsToDays(this.mySim.curTime);
		
	}
	
	public void printCumStats(DendroReSim sim) {
		long startTime = sim.curTime-EpiSimUtil.dayToSeconds(1);
//		long endTime = sim.curTime;
		int day = EpiSimUtil.secondsToDays(startTime);
//		this.cumLog.println(day+"\t"+sim.infections.size()+"\t"+this.peopleAffected.size()+"\t"+this.numClosingEvents+"\t"+this.monitoredLocs.size()+"\t"+this.numMonitorDays);
		this.cumLog.println(day+"\t"+sim.infections.size()+"\t"+this.numClosingEvents+"\t"+this.numMonitors+"\t"+this.numMonitorDays);
	}
	
	public void printMonStats() {
		if (this.numMonitors == 0 || this.trackLocations.isEmpty())
			return;
		Iterator<Integer> monLocsItr = this.monitoredLocs.iterator();
		if (monLocsItr.hasNext()) this.monLog.print(monLocsItr.next());
		while (monLocsItr.hasNext()) {
			this.monLog.print("\t"+monLocsItr.next());
		}
		this.monLog.println();
	}
	
	public void printWeightStats() {
		if (this.numMonitors == 0 || this.trackLocations.isEmpty())
			return;
		this.weightLog.print(this.trackLocsWeight[0]);
		for (int i=1; i < this.trackLocsWeight.length; i++) {
			this.weightLog.print("\t"+this.trackLocsWeight[i]);
		}
		this.weightLog.println();
	}
	
	public void printWeightHeader() {
		this.weightLog.print(this.trackLocsByIndex[0]);
		for (int i=1; i < this.trackLocsByIndex.length; i++) {
			this.weightLog.print("\t"+this.trackLocsByIndex[i]);
		}
		this.weightLog.println();
	}
	
	public void printParams() {
		System.out.println("alpha:\t"+alpha);
		System.out.println("Seperate Population and connection monitors:\t"+this.sepPopSickMons);
		System.out.println("Distribute monitors uniformly and randomly:\t"+this.monUniformRand);
		System.out.println("Use mon agent:\t"+this.useMonAgent);
		if (this.graphSelection == USE_DIRECT_CON)
			System.out.println("Use direct connection bassed on people going from one location to another.");
		else if (this.graphSelection == USE_FAMILY_CON)
			System.out.println("Use shared households as connection between locations.");
		if (this.infCountCum)
			System.out.println("Number of infections at locations is cummulative");
		else
			System.out.println("Number of infecions at locations is only those who are infectious");
	}
	
	/////////////////////////////////////// STATIC //////////////////////////////////////////

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			alpha = Double.parseDouble(args[0]);
		}
		multiRun();
//		testRun2();
//		printInfPerDay(args[0]);
//		System.out.println(System.currentTimeMillis());
//		testWeights();

	}
	
	public static void multiRun() {
		System.out.println("alpha: "+alpha);
		int[] numLocs = {10000};//{1000,2000,5000,10000};
		double[] detect = {0.01};//{0.005,0.01,0.02,0.05};
		long[] duration = {EpiSimUtil.dayToSeconds(100)};//{EpiSimUtil.dayToSeconds(3),EpiSimUtil.dayToSeconds(7),EpiSimUtil.dayToSeconds(10),EpiSimUtil.dayToSeconds(14)};
		int[] numMons = {2000,1000};
		for (int numLocsIndex=0; numLocsIndex<numLocs.length; numLocsIndex++) {
			for (int detectIndex=0; detectIndex<detect.length; detectIndex++) {
				for (int durationIndex=0; durationIndex<duration.length; durationIndex++) {
					for (int monIndex=0; monIndex<numMons.length; monIndex++) {
						MonBuildClose policy =  
							new MonBuildClose(numMons[monIndex], detect[detectIndex],duration[durationIndex],EpiSimUtil.getLocsHighPop(numLocs[numLocsIndex]));
//							new MonBuildClose(numMons[monIndex], detect[detectIndex],duration[durationIndex],EpiSimUtil.getLocsHighPop(numLocs[numLocsIndex]),new HarmonicAvgLabels());
						ArrayList<Policy> policies = new ArrayList<Policy>();
						policies.add(policy);
						DendroReSim sim = new DendroReSim(policies);
						policy.setMySim(sim);
						policy.printParams();
						int prevNumInf = Integer.MAX_VALUE;
						int curNumInf = sim.infections.size();
						// next two lines for closing populous locations initially
						int numInitClose = 10;
						policy.closeLocations(EpiSimUtil.getLocsHighPop(numInitClose).keySet().iterator(), 0);
						while (sim.curTime < EpiSimUtil.dayToSeconds(100) && curNumInf / prevNumInf < 2) {//sim.infections.size() < 56569) {
							policy.refreshMonLocs();
							sim.stepSim();
							prevNumInf = curNumInf;
							curNumInf = sim.infections.size();
							if (policy.numMonitors != 0)
								policy.numMonitorDays += policy.numMonitors;
							policy.printCumStats(sim);
							policy.printMonStats();
							policy.printWeightStats();
//							System.out.println("Day "+EpiSimUtil.secondsToDays(sim.curTime)+"\tnumNonZero\t"+numNonZero);
							numNonZero = 0;
						}
					}
				}
			}
		}
	}
	
	public static void testWeights() {
		int endDay = 30;
		int numMons = 2000;
		double detect = 0.01;
		long duration = EpiSimUtil.dayToSeconds(7);
		int numLocs = 5000;
		double[] alphas = {0.99,0.0};
		for (int i=0; i<alphas.length; i++) {
			alpha = alphas[i];
			MonBuildClose policy =  
				new MonBuildClose(numMons, detect, duration, EpiSimUtil.getLocsHighOutDeg(numLocs));
			ArrayList<Policy> policies = new ArrayList<Policy>();
			policies.add(policy);
			DendroReSim sim = new DendroReSim(policies);
			policy.setMySim(sim);
			int prevNumInf = Integer.MAX_VALUE;
			int curNumInf = sim.infections.size();
			while (sim.curTime < EpiSimUtil.dayToSeconds(endDay) && curNumInf / prevNumInf < 2) {//sim.infections.size() < 56569) {
				policy.refreshMonLocs();
				sim.stepSim();
				prevNumInf = curNumInf;
				curNumInf = sim.infections.size();
				policy.printCumStats(sim);
				int sum = 0;
				for (int j=0; j < policy.trackLocsWeight.length; j++) {
					sum += policy.trackLocsWeight[j];
				}
				System.out.println("Day "+EpiSimUtil.secondsToDays(sim.curTime)+"\tnumNonZero\t"+numNonZero+"\tSum of weights "+sum);
				numNonZero = 0;
			}
			if (i==0)
				savedWeights = policy.trackLocsWeight;
			else {
				compWeights(savedWeights, policy.trackLocsWeight);
			}
		}
	}
	
	public static void compWeights(double[] one, double[] two) {
		int numDiff = 0;
		if (one.length != two.length) {
			System.out.println("weight arrays not equal length");
			return;
		}
		for (int i=0; i < one.length; i++) {
			if (one[i] != two[i])
				numDiff++;
		}
		System.out.println("The two weight arrays differ by "+numDiff+" elements out of "+one.length);
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
