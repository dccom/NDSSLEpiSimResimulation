/**
 * 
 */
package dbepi2a;

import java.util.*;
import java.io.*;

/**
 * @author dave
 *
 */
public class QuarBuild implements Policy {
	
	public static final int USE_DIRECT_CON = 0;
	public static final int USE_FAMILY_CON = 1;
	public static final int USE_SHAREDPEOPLE_CON = 2;
	
	public static final int USE_POP_SCORE = 10;
	public static final int USE_INF_SCORE = 11;
	public static final int USE_POPINF_SCORE = 12;
	
	public static final long randSeed = 1170275580515L;
	public static Random rand = new Random(randSeed);
	
	public static long defaultClosureTime = 24*60*60*7;
	
	private int graphSelection = USE_DIRECT_CON;
	
	
	String logDir = "";
	public PrintStream infLog;
	public PrintStream buildLog;
	public PrintStream cumLog;
	public PrintStream monLog;
	public PrintStream weightLog;
	public PrintStream effectLog;
	
	private boolean infCountCum = true;
	
	public DendroReSim mySim;
	public long closureDuration = defaultClosureTime;
	public int numClosingEvents = 0;
//	public SortedMap<Long,HashMap<Integer,Integer>> newInf = new TreeMap<Long,HashMap<Integer,Integer>>();
	public HashMap<Integer,Integer> trackLocations;
	public HashMap<Integer,Long> closedLocations = new HashMap<Integer,Long>();
//	public HashMap<Integer,Integer> peopleAffected = new HashMap<Integer,Integer>();
	
	public HashMap<Integer,Integer> aggPop;
	public HashMap<Integer,TreeMap<Integer,Integer>> locCon = new HashMap<Integer,TreeMap<Integer,Integer>>();

	public QuarBuild(long closureDuration, HashMap<Integer,Integer> impLoc, String logDir) {
		this.logDir = logDir;
		this.closureDuration = closureDuration;
		this.trackLocations = impLoc;
		this.aggPop = new HashMap<Integer,Integer>(this.trackLocations.size());
		try {
			this.infLog = new PrintStream(this.logDir+"/"+"infLog-"+"dur"+this.closureDuration+"loc"+this.trackLocations.size()+".log");
			this.buildLog = new PrintStream(this.logDir+"/"+"buildLog-"+"dur"+this.closureDuration+"loc"+this.trackLocations.size()+".log");
			this.cumLog = new PrintStream(this.logDir+"/"+"cumLog-"+"dur"+this.closureDuration+"loc"+this.trackLocations.size()+".log");
			this.effectLog = new PrintStream(this.logDir+"/"+"effectLog-"+"dur"+this.closureDuration+"loc"+this.trackLocations.size()+".log");
			this.effectLog.println("day"+"\t"+"Num contageous"+"\t"+"Num affected"+
						"\t"+"Num contacts closed"+"\t"+"Num sick contacts closed");
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public QuarBuild(long closureDuration, HashMap<Integer,Integer> impLoc) {
		this(closureDuration, impLoc, "");
	}
	
	public void setMySim(DendroReSim sim) {
		this.mySim = sim;
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

	public boolean isValidTransEvent(Integer infected, Integer victim,
			Integer location, long time) {
		if (this.isClosed(location, time)) {
			return false;
		}
		return true;
	}

	public void newInf(Integer victim, Integer location, long time) {
		this.infLog.println(time+"\t"+victim+"\t"+location);
		Iterator<Integer> locsVisited = this.getLocationsVisited(victim).iterator();
		while (locsVisited.hasNext()) {
			Integer locVisited = locsVisited.next();
			if (this.trackLocations.containsKey(locVisited)) {
				this.incInfectionCount(locVisited);
			}
		}
	}
	
	public HashSet<Integer> getLocationsVisited(Integer person) {
		if (this.mySim.locsVisited.containsKey(person))
			return this.mySim.locsVisited.get(person);
		else
			return EpiSimUtil.getLocationsVisited(person.intValue());
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
	
	public void printEffectStats() {
		int day = EpiSimUtil.secondsToDays(this.mySim.curTime);
		HashSet<Integer> infPpl = this.mySim.getInfectiousPeople();
		HashSet<Integer> closedLocs = this.getClosedLocations(this.mySim.curTime);
		HashMap<String,Integer> stats = EpiSimUtil.getEffectStats(infPpl, closedLocs);
		this.effectLog.println(day+"\t"+infPpl.size()+"\t"+stats.get("AFFECTED")+"\t"+
				stats.get("TOT_CON")+"\t"+stats.get("INF_CON"));
	}
	
	public void printCumStats() {
		DendroReSim sim = this.mySim;
		long startTime = sim.curTime-EpiSimUtil.dayToSeconds(1);
		int day = EpiSimUtil.secondsToDays(startTime);
		this.cumLog.println(day+"\t"+sim.infections.size()+"\t"+this.numClosingEvents);
	}
	
	public void printParams() {
		if (this.graphSelection == USE_DIRECT_CON)
			System.out.println("Use direct connection bassed on people going from one location to another.");
		else if (this.graphSelection == USE_FAMILY_CON)
			System.out.println("Use shared households as connection between locations.");
		if (this.infCountCum)
			System.out.println("Number of infections at locations is cummulative");
		else
			System.out.println("Number of infecions at locations is only those who are infectious");
	}
	
	public void closeAtRiskLocPerInfDet (int numPeopleToAffectPerInf, int scoreType) {
		int numInf = this.mySim.getInfectiousPeople().size();
		this.closeAtRiskLocDet(numInf * numPeopleToAffectPerInf, scoreType);
	}
	
	public void closeAtRiskLocDet(int numPeopleToAffect, int scoreType) {
		int numAffected = 0;
		PriorityQueue<KVPair<Integer,Integer>> q;
		if (scoreType == USE_POPINF_SCORE){
			q = this.makeInfPopQueue();
		} else if (scoreType == USE_POP_SCORE){
			q = this.makePopQueue();
		} else if (scoreType == USE_INF_SCORE){
			q = this.makeInfQueue();
		} else
			return;
		while (numAffected < numPeopleToAffect && !q.isEmpty()) {
			KVPair<Integer,Integer> pair = q.poll();
			this.closeLocation(pair.key(), this.mySim.curTime);
			numAffected += this.getAggPop(pair.key()).intValue();
		}
	}
	
	public void closeAtRiskLocPerInfRand (int numPeopleToAffectPerInf, int scoreType) {
		int numInf = this.mySim.getInfectiousPeople().size();
		this.closeAtRiskLocRand(numInf * numPeopleToAffectPerInf, scoreType);
	}
	
	public void closeAtRiskLocRand(int numPeopleToAffect, int scoreType) {
		int numAffected = 0;
		Integer[] locationIDs = new Integer[this.trackLocations.size()];
		Iterator<Integer> itr = this.trackLocations.keySet().iterator();
		for (int i=0; itr.hasNext(); i++)
			locationIDs[i] = itr.next();
		double[] weights = this.makeWeights(locationIDs, scoreType);
		WeightedRandPerm perm = new WeightedRandPerm(rand, weights);
		perm.reset(weights.length);
		while (perm.hasNext() && numAffected < numPeopleToAffect) {
			int index = perm.next();
			this.closeLocation(locationIDs[index], this.mySim.curTime);
			numAffected += this.getAggPop(locationIDs[index]).intValue();
		}
	}
	
	public double[] makeWeights(Integer[] locs, int scoreType) {
		double[] weights = new double[locs.length];
		PriorityQueue<KVPair<Integer,Integer>> q = new PriorityQueue<KVPair<Integer,Integer>>();
		if (scoreType == USE_POPINF_SCORE){
			q = this.makeInfPopQueue();
		} else if (scoreType == USE_POP_SCORE){
			q = this.makePopQueue();
		} else if (scoreType == USE_INF_SCORE){
			q = this.makeInfQueue();
		}
		HashMap<Integer,Integer> scores = new HashMap<Integer,Integer>(q.size());
		while (!q.isEmpty()) {
			KVPair<Integer,Integer> pair = q.poll();
			scores.put(pair.key(), pair.value());
		}
		for (int i=0; i < locs.length; i++) {
			if (scores.containsKey(locs[i]))
				weights[i] = scores.get(locs[i]).doubleValue();
			else
				weights[i] = 1.0;
		}
		weights = EpiSimUtil.normalize(weights);
		return weights;
	}
	
//	public HashMap<Integer,Integer> getScores(Integer[] locs, int scoreType) {
//		HashMap<Integer,Integer> scores = new HashMap<Integer,Integer>(locs.length);
//		if (scoreType == USE_POPINF_SCORE || scoreType == USE_INF_SCORE) {
//			Iterator<Integer> ppl = this.mySim.getInfectiousPeople().iterator();
//			while (ppl.hasNext()) {
//				Integer p = ppl.next();
//				Iterator<Integer> ls = this.getLocationsVisited(p).iterator();
//				while (ls.hasNext())
//					EpiSimUtil.incMapValue(scores, ls.next(), 1);
//			}
//		} else {
//			for (int i=0; i < locs.length; i++)
//				scores.put(locs[i], new Integer(1));
//		}
//		if (scoreType == USE_POPINF_SCORE || scoreType == USE_POP_SCORE) {
//			HashMap<Integer,Integer> inf = new HashMap<Integer,Integer>();
//			Iterator<Integer> ppl = this.mySim.getInfectiousPeople().iterator();
//			while (ppl.hasNext()) {
//				Integer p = ppl.next();
//				Iterator<Integer> ls = this.getLocationsVisited(p).iterator();
//				while (ls.hasNext())
//					EpiSimUtil.incMapValue(inf, ls.next(), 1);
//			}
//			Iterator<Integer> ls = inf.keySet().iterator();
//			while (ls.hasNext()) {
//				Integer key = ls.next();
//				int infc = inf.get(key).intValue();
//				int pop = this.getAggPop(key).intValue();
//				KVPair<Integer,Integer> pair = new KVPair<Integer,Integer>(key, new Integer(infc*pop), -1);
//				q.add(pair);
//			}
//		}
//		return scores;
//	}
	
	public void closeInfPopAtRiskLocPerInf(int numPeopleToAffectPerInf) {
		int numInf = this.mySim.getInfectiousPeople().size();
		this.closeInfPopAtRiskLoc(numInf * numPeopleToAffectPerInf);
	}
	
	public void closeInfPopAtRiskLoc(int numPeopleToAffect) {
		int numAffected = 0;
		PriorityQueue<KVPair<Integer,Integer>> q = this.makeInfPopQueue();
		while (numAffected < numPeopleToAffect && !q.isEmpty()) {
			KVPair<Integer,Integer> pair = q.poll();
			this.closeLocation(pair.key(), this.mySim.curTime);
			numAffected += this.getAggPop(pair.key()).intValue();
		}
		
	}
	
	public PriorityQueue<KVPair<Integer,Integer>> makeInfPopQueue() {
		PriorityQueue<KVPair<Integer,Integer>> q = new PriorityQueue<KVPair<Integer,Integer>>();
		HashMap<Integer,Integer> inf = new HashMap<Integer,Integer>();
		Iterator<Integer> ppl = this.mySim.getInfectiousPeople().iterator();
		while (ppl.hasNext()) {
			Integer p = ppl.next();
			Iterator<Integer> ls = this.getLocationsVisited(p).iterator();
			while (ls.hasNext())
				EpiSimUtil.incMapValue(inf, ls.next(), 1);
		}
		Iterator<Integer> ls = inf.keySet().iterator();
		while (ls.hasNext()) {
			Integer key = ls.next();
			int infc = inf.get(key).intValue();
			int pop = this.getAggPop(key).intValue();
			KVPair<Integer,Integer> pair = new KVPair<Integer,Integer>(key, new Integer(infc*pop), -1);
			q.add(pair);
		}
		return q;
	}
	
	public void closePopAtRiskLocPerInf(int numPeopleToAffectPerInf) {
		int numInf = this.mySim.getInfectiousPeople().size();
		this.closePopAtRiskLoc(numInf * numPeopleToAffectPerInf);
	}
	
	public void closePopAtRiskLoc(int numPeopleToAffect) {
		int numAffected = 0;
		PriorityQueue<KVPair<Integer,Integer>> q = this.makePopQueue();
		while (numAffected < numPeopleToAffect && !q.isEmpty()) {
			KVPair<Integer,Integer> pair = q.poll();
			this.closeLocation(pair.key(), this.mySim.curTime);
			numAffected += this.getAggPop(pair.key()).intValue();
		}
		
	}
	
	public PriorityQueue<KVPair<Integer,Integer>> makePopQueue() {
		PriorityQueue<KVPair<Integer,Integer>> q = new PriorityQueue<KVPair<Integer,Integer>>();
		Iterator<Integer> ls = this.trackLocations.keySet().iterator();
		while (ls.hasNext()) {
			Integer key = ls.next();
			KVPair<Integer,Integer> pair = new KVPair<Integer,Integer>(key, this.getAggPop(key), -1);
			q.add(pair);
		}
		return q;
	}
	
	public void closeInfAtRiskLocPerInf(int numPeopleToAffectPerInf) {
		int numInf = this.mySim.getInfectiousPeople().size();
		this.closeInfAtRiskLoc(numInf * numPeopleToAffectPerInf);
	}
	
	public void closeInfAtRiskLoc(int numPeopleToAffect) {
		int numAffected = 0;
		PriorityQueue<KVPair<Integer,Integer>> q = this.makeInfQueue();
		while (numAffected < numPeopleToAffect && !q.isEmpty()) {
			KVPair<Integer,Integer> pair = q.poll();
			this.closeLocation(pair.key(), this.mySim.curTime);
			numAffected += this.getAggPop(pair.key()).intValue();
		}
		
	}
	
	public PriorityQueue<KVPair<Integer,Integer>> makeInfQueue() {
		PriorityQueue<KVPair<Integer,Integer>> q = new PriorityQueue<KVPair<Integer,Integer>>();
		HashMap<Integer,Integer> inf = new HashMap<Integer,Integer>();
		Iterator<Integer> ppl = this.mySim.getInfectiousPeople().iterator();
		while (ppl.hasNext()) {
			Integer p = ppl.next();
			Iterator<Integer> ls = this.getLocationsVisited(p).iterator();
			while (ls.hasNext())
				EpiSimUtil.incMapValue(inf, ls.next(), 1);
		}
		Iterator<Integer> ls = inf.keySet().iterator();
		while (ls.hasNext()) {
			Integer key = ls.next();
			int infc = inf.get(key).intValue();
			KVPair<Integer,Integer> pair = new KVPair<Integer,Integer>(key, new Integer(infc), -1);
			q.add(pair);
		}
		return q;
	}
	
	
	
	////////////////////////////////////// STATIC //////////////////////////////////////

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		singleRun(10000, Integer.parseInt(args[0]));
		multiRunDet(20);

	}
	
	public static void singleRun(int numLocsToTrack, int numPplAffPerInf) {
		QuarBuild policy = new QuarBuild(EpiSimUtil.dayToSeconds(1), EpiSimUtil.getLocsHighPop(numLocsToTrack));
		ArrayList<Policy> policies = new ArrayList<Policy>();
		policies.add(policy);
		DendroReSim sim = new DendroReSim(policies);
		policy.setMySim(sim);
		policy.printParams();
		while (sim.curTime < EpiSimUtil.dayToSeconds(100)) {
			policy.closeInfAtRiskLocPerInf(numPplAffPerInf);
			policy.printEffectStats();
			sim.stepSim();
			policy.printCumStats();
		}
	}
	
	public static void multiRunDet(int delay) {
		System.out.println("Random seed:\t"+QuarBuild.randSeed);
		System.out.println("Start closings on day\t"+delay);
		int numLocsToTrack = 10000;
		String pwd = System.getenv("PWD");
		int[] scoreTypes = {USE_POPINF_SCORE,USE_POP_SCORE,USE_INF_SCORE};
		String[] scoreDirs = {"/infPop","/pop","/inf"};
		int[] numAffPerInf = {100,200,300,400,500,600};
		String[] numAffPerInfDir = {"/100perInf","/200perInf","/300perInf","/400perInf","/500perInf","/600perInf"};
		for (int scoreIndex=0; scoreIndex < scoreTypes.length; scoreIndex++) {
			for (int affIndex=0; affIndex < numAffPerInf.length; affIndex++) {
				String logDirName = pwd + scoreDirs[scoreIndex] + numAffPerInfDir[affIndex];
				File logDir = new File(logDirName);
				logDir.mkdirs();
				QuarBuild policy = 
					new QuarBuild(EpiSimUtil.dayToSeconds(1), EpiSimUtil.getLocsHighPop(numLocsToTrack), logDirName);
				ArrayList<Policy> policies = new ArrayList<Policy>();
				policies.add(policy);
				DendroReSim sim = new DendroReSim(policies);
				policy.setMySim(sim);
				policy.printParams();
				while (sim.curTime < EpiSimUtil.dayToSeconds(100)) {
					int numPeopleToAffectPerInf = numAffPerInf[affIndex];
					int scoreType = scoreTypes[scoreIndex];
					if (sim.curTime >= EpiSimUtil.dayToSeconds(delay))
						policy.closeAtRiskLocPerInfDet(numPeopleToAffectPerInf, scoreType);
					policy.printEffectStats();
					sim.stepSim();
					policy.printCumStats();
				}
			}
		}
	}
	
	public static void multiRunRand(int delay) {
		System.out.println("Random seed:\t"+QuarBuild.randSeed);
		System.out.println("Start closings on day\t"+delay);
		int numLocsToTrack = 10000;
		String pwd = System.getenv("PWD");
		int[] scoreTypes = {USE_POPINF_SCORE,USE_POP_SCORE,USE_INF_SCORE};
		String[] scoreDirs = {"/infPop","/pop","/inf"};
		int[] numAffPerInf = {100,200,300,400,500,600};
		String[] numAffPerInfDir = {"/100perInf","/200perInf","/300perInf","/400perInf","/500perInf","/600perInf"};
		for (int scoreIndex=0; scoreIndex < scoreTypes.length; scoreIndex++) {
			for (int affIndex=0; affIndex < numAffPerInf.length; affIndex++) {
				String logDirName = pwd + scoreDirs[scoreIndex] + numAffPerInfDir[affIndex];
				File logDir = new File(logDirName);
				logDir.mkdirs();
				QuarBuild policy = 
					new QuarBuild(EpiSimUtil.dayToSeconds(1), EpiSimUtil.getLocsHighPop(numLocsToTrack), logDirName);
				ArrayList<Policy> policies = new ArrayList<Policy>();
				policies.add(policy);
				DendroReSim sim = new DendroReSim(policies);
				policy.setMySim(sim);
				policy.printParams();
				while (sim.curTime < EpiSimUtil.dayToSeconds(100)) {
					int numPeopleToAffectPerInf = numAffPerInf[affIndex];
					int scoreType = scoreTypes[scoreIndex];
					if (sim.curTime >= EpiSimUtil.dayToSeconds(delay))
						policy.closeAtRiskLocPerInfRand(numPeopleToAffectPerInf, scoreType);
					policy.printEffectStats();
					sim.stepSim();
					policy.printCumStats();
				}
			}
		}
	}

}
