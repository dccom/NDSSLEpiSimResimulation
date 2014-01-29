/**
 * 
 */
package dbepi2a;

import java.util.*;
import java.sql.*;


/**
 * @author dave
 *
 */
public class PeopleQuarantine implements Policy {
	
	public static final int QRTN_MOST_SOC = -1;
	public static final int QRTN_RAND_INF = -2;
	
	public static final long randSeed = 1170275580515L;
	public static Random rand = new Random(randSeed);
	
	private DendroReSim sim = null;
	
	private HashMap<Integer,Long> startQrtn;
	private HashMap<Integer,Long> endQrtn;

	/**
	 * 
	 */
	public PeopleQuarantine() {
		this.startQrtn = new HashMap<Integer,Long>();
		this.endQrtn = new HashMap<Integer,Long>();
	}
	
	public void setMySim(DendroReSim sim) {
		this.sim = sim;
	}

	/* (non-Javadoc)
	 * @see dbepi2a.Policy#isValidTransEvent(java.lang.Integer, java.lang.Integer, java.lang.Integer, long)
	 */
	public boolean isValidTransEvent(Integer infected, Integer victim,
			Integer location, long time) {
		if (isQrtnd(infected, time) || isQrtnd(victim, time))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see dbepi2a.Policy#newInf(java.lang.Integer, java.lang.Integer, long)
	 */
	public void newInf(Integer victim, Integer location, long time) {
		// TODO Auto-generated method stub

	}
	
	private boolean isQrtnd(Integer person, long time) {
		if (startQrtn.containsKey(person) && startQrtn.get(person).longValue() <= time 
				&& endQrtn.containsKey(person) && endQrtn.get(person).longValue() > time)
			return true;
		return false;
	}
	
	public void quarantine(Integer person, long start, long end) {
		this.startQrtn.put(person, new Long(start));
		this.endQrtn.put(person, new Long(end));
	}
	
	public void qrtnMostSoc(int numPeople, long start, long end) {
		Iterator<Integer> people = EpiSimUtil.getMostSocPeople(numPeople).iterator();
		while (people.hasNext()) {
			Integer person = people.next();
			this.quarantine(person, start, end);
		}
	}
	
	public void qrtnRandInfectious(int numPeople, long start, long end) {
		if (this.sim == null) {
			System.out.println("Error: have not specified simulation object!");
			return;
		}
		HashSet<Integer> people = this.sim.getInfectiousPeople();
		HashSet<Integer> indices = new HashSet<Integer>(numPeople);
		while (indices.size() < numPeople) {
			Integer index = rand.nextInt(people.size());
			if (!indices.contains(index)) {
				indices.add(index);
			}
		}
		Iterator<Integer> itr = people.iterator();
		for (int i=0; i < people.size(); i++) {
			Integer person = itr.next();
			if (indices.contains(new Integer(i))) {
				this.quarantine(person, start, end);
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		quarantinePeopleForNDays(Integer.parseInt(args[0]), Integer.parseInt(args[1]), 
//				Integer.parseInt(args[2]), Integer.parseInt(args[3]));
		quarantinePeopleForOneDayMultiStart(Integer.parseInt(args[0]), Integer.parseInt(args[1]));

	}
	
	public static void quarantinePeopleForOneDay(int qrtnSelectionMethod, int numPeople, int day) {
		if (qrtnSelectionMethod == QRTN_MOST_SOC) {
			System.out.println("Quarantine most sociable people.");
		} else if (qrtnSelectionMethod == QRTN_RAND_INF) {
			System.out.println("Quarantine random infectious people.");
		}
		System.out.println("Number of people to quarantine:\t"+numPeople);
		System.out.println("Start quarantine:\t"+day);
		int nextDay = day+1;
		System.out.println("End quarantine:\t"+nextDay);
		PeopleQuarantine policy =  new PeopleQuarantine();
		ArrayList<Policy> policies = new ArrayList<Policy>();
		policies.add(policy);
		DendroReSim sim = new DendroReSim(policies);
		policy.setMySim(sim);
		while (sim.curTime < EpiSimUtil.dayToSeconds(100)) {
			if (EpiSimUtil.secondsToDays(sim.curTime) == day) {
				if (qrtnSelectionMethod == QRTN_MOST_SOC) {
					policy.qrtnMostSoc(numPeople, sim.curTime, sim.curTime + EpiSimUtil.dayToSeconds(1));
				} else if (qrtnSelectionMethod == QRTN_RAND_INF) {
					policy.qrtnRandInfectious(numPeople, sim.curTime, sim.curTime + EpiSimUtil.dayToSeconds(1));
				}
			}
			sim.stepSim();
			System.out.println(EpiSimUtil.secondsToDays(sim.curTime)+"\t"+sim.infections.size());
		}
	}
	
	public static void quarantinePeopleForOneDayMultiStart(int startDay, int period) {
		int infQstart = startDay;
		double fracOfInfPplToQ = 0.1;
		for (; infQstart < 100; infQstart += period) {
			PeopleQuarantine policy =  new PeopleQuarantine();
			ArrayList<Policy> policies = new ArrayList<Policy>();
			policies.add(policy);
			DendroReSim sim = new DendroReSim(policies, infQstart);
			policy.setMySim(sim);
			int infQSimInfCount = sim.getInfectiousPeople().size();
			int numInfQSim = (int)(infQSimInfCount * fracOfInfPplToQ);
			policy.qrtnRandInfectious(numInfQSim, sim.curTime, sim.curTime + EpiSimUtil.dayToSeconds(1));
			sim.stepSim();
			int infQSimDiff = sim.getInfectiousPeople().size() - infQSimInfCount;
			int socQSimDiff = Integer.MAX_VALUE;
			int numSocQSim = (int)(1.0*numInfQSim);
			while (socQSimDiff > infQSimDiff) {
				policy =  new PeopleQuarantine();
				policies = new ArrayList<Policy>();
				policies.add(policy);
				sim = new DendroReSim(policies, infQstart);
				policy.setMySim(sim);
				int socQSimInfCount = sim.getInfectiousPeople().size();
				policy.qrtnMostSoc(numSocQSim, sim.curTime, sim.curTime + EpiSimUtil.dayToSeconds(1));
				sim.stepSim();
				socQSimDiff = sim.getInfectiousPeople().size() - socQSimInfCount;
				numSocQSim = (int)(2.0*numSocQSim);
			}
			System.out.println(infQstart+"\t"+infQSimInfCount+"\t"+numInfQSim+"\t"+infQSimDiff+"\t"+
					numSocQSim+"\t"+socQSimDiff);
		}
		
		
	}
	
	public static void quarantinePeopleForNDays(int qrtnSelectionMethod, int numPeople, int day, int duration) {
		if (qrtnSelectionMethod == QRTN_MOST_SOC) {
			System.out.println("Quarantine most sociable people.");
		} else if (qrtnSelectionMethod == QRTN_RAND_INF) {
			System.out.println("Quarantine random infectious people.");
		}
		System.out.println("Number of people to quarantine:\t"+numPeople);
		System.out.println("Start quarantine:\t"+day);
		int nextDay = day+duration;
		System.out.println("End quarantine:\t"+nextDay);
		PeopleQuarantine policy =  new PeopleQuarantine();
		ArrayList<Policy> policies = new ArrayList<Policy>();
		policies.add(policy);
		DendroReSim sim = new DendroReSim(policies);
		policy.setMySim(sim);
		while (sim.curTime < EpiSimUtil.dayToSeconds(100)) {
			if (EpiSimUtil.secondsToDays(sim.curTime) == day) {
				if (qrtnSelectionMethod == QRTN_MOST_SOC) {
					policy.qrtnMostSoc(numPeople, sim.curTime, sim.curTime + EpiSimUtil.dayToSeconds(duration));
				} else if (qrtnSelectionMethod == QRTN_RAND_INF) {
					policy.qrtnRandInfectious(numPeople, sim.curTime, sim.curTime + EpiSimUtil.dayToSeconds(duration));
				}
			}
			sim.stepSim();
			System.out.println(EpiSimUtil.secondsToDays(sim.curTime)+"\t"+sim.infections.size());
		}
	}

}
