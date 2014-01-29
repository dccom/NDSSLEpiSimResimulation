package dbepi2a;

import java.sql.*;
import java.util.*;
import java.io.*;

public class DendroReSim {
	
	public static final int track = 1229601;
	public static final int track2 = 1229602;
	
	public static final String savedStateDir = "/net/cbk/dc/EpiSim/reSimSavedState";
	
//	public static final double[] sickLoadByDay = {0.0486,0.1793,0.2724,0.2606,0.1453,0.0660,0.0219,0.0061};
	public static final int[] sickLoadByDay = {486,1793,2724,2606,1453,660,219,61};
	
	public static final boolean doTrack = false;
	
	public long curTime = 0;
	public HashMap<Integer,Long> infections = new HashMap<Integer,Long>();
	public HashMap<Integer,Integer> infectionLoad = new HashMap<Integer,Integer>();
	public TreeMap<Long,TreeMap<Integer,TreeSet<Integer>>> sickContacts = new TreeMap<Long,TreeMap<Integer,TreeSet<Integer>>>();
	public TreeMap<Long,TreeMap<Integer,TreeMap<Integer,Integer>>> sickContactsLoc = new TreeMap<Long,TreeMap<Integer,TreeMap<Integer,Integer>>>();
	public HashMap<Integer,HashSet<Integer>> locsVisited = new HashMap<Integer,HashSet<Integer>>();
	
	public HashMap<Integer,Integer> spuriousInfloc = new HashMap<Integer, Integer>();
	
	public ArrayList<Integer> contactsToAdd = new ArrayList<Integer>();
	
	public ArrayList<Policy> myPolicies = null;

	public DendroReSim() {
		this.locsVisited = EpiSimUtil.getLocationsVisitedByAllInf();
		this.getInfectionLoad();
		this.getIntitiallyInfected();
	}
	
	public DendroReSim(ArrayList<Policy> policies) {
		this.locsVisited = EpiSimUtil.getLocationsVisitedByAllInf();
		this.getInfectionLoad();
		this.getIntitiallyInfected();
		this.myPolicies = policies;
	}
	
	public DendroReSim(int day) {
		this.locsVisited = EpiSimUtil.getLocationsVisitedByAllInf();
		this.loadSavedState(day);
	}
	
	public DendroReSim(ArrayList<Policy> policies, int day) {
		this.locsVisited = EpiSimUtil.getLocationsVisitedByAllInf();
		this.loadSavedState(day);
		this.myPolicies = policies;
	}
	
	private void loadSavedState(int day) {
		this.curTime = EpiSimUtil.dayToSeconds(day);
		try {
			BufferedReader rd = this.openInfectionState(day);
			String line;
			while ((line = rd.readLine()) != null) {
				StringTokenizer tok = new StringTokenizer(line);
				Integer person = new Integer(Integer.parseInt(tok.nextToken()));
				infections.put(person, 
						new Long(Long.parseLong(tok.nextToken())));
				this.addContacts(person);
			}
			rd = this.openInfLoadState(day);
			while ((line = rd.readLine()) != null) {
				StringTokenizer tok = new StringTokenizer(line);
				this.infectionLoad.put(new Integer(Integer.parseInt(tok.nextToken())), 
						new Integer(Integer.parseInt(tok.nextToken())));
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	private BufferedReader openInfectionState(int day) {
		BufferedReader rd = null;
		try {
			rd = new BufferedReader(new FileReader(savedStateDir+"/"+day+"/infections.dat"));
		} catch (Exception e) {
			System.out.println(e);
		}
		return rd;
	}
	
	private BufferedReader openInfLoadState(int day) {
		BufferedReader rd = null;
		try {
			rd = new BufferedReader(new FileReader(savedStateDir+"/"+day+"/infload.dat"));
		} catch (Exception e) {
			System.out.println(e);
		}
		return rd;
	}
	
	public void getIntitiallyInfected(){
		try {
			Connection con = EpiSimUtil.dbConnect();
			Statement getInitInfected = con.createStatement();
			ResultSet initInfected = getInitInfected.executeQuery(
					"SELECT id FROM "+EpiSimUtil.initTbl);
			while(initInfected.next()){
				Integer p = new Integer(initInfected.getInt(1));
				this.infect(p, new Integer(0), new Long(0));
			}
			this.flushContacts();
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void getInfectionLoad() {
		try {
			Connection con = EpiSimUtil.dbConnect();
			ResultSet getInfLoadQ = con.createStatement().executeQuery(
					"SELECT personID,amount FROM "+EpiSimUtil.sickLoadTbl);
			while (getInfLoadQ.next()) {
				Integer person = new Integer(getInfLoadQ.getInt("personID"));
				Integer amount = new Integer(getInfLoadQ.getInt("amount"));
				this.infectionLoad.put(person, amount);
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void infect(Integer victim, Integer location, long time) {
		if (DendroReSim.doTrack && (victim.intValue() == DendroReSim.track || victim.intValue() == DendroReSim.track2))
			System.out.println("infect:\t"+victim+"\t"+this.curTime+"\t"+this.infectionLoad.get(victim));
		if (!this.infections.containsKey(victim))
			this.newInf(victim, location, time);
		this.infections.put(victim, new Long(time));
		this.infectionLoad.remove(victim);
		this.cacheContact(victim);
		long origTime = EpiSimUtil.getInfectionTime(victim);
		if (!this.locsVisited.containsKey(victim))
			this.locsVisited.put(victim, EpiSimUtil.getLocationsVisited(victim));
//		if (Math.abs(time - origTime) > EpiSimUtil.dayToSeconds(1)) {
//			System.out.println(victim + "\t" + EpiSimUtil.secondsToDays(origTime) + "\t" + EpiSimUtil.secondsToDays(time));
//		}
	}
	
	public void cacheContact(Integer person) {
		this.contactsToAdd.add(person);
	}
	
	public void flushContacts(){
		Iterator<Integer> itr = this.contactsToAdd.iterator();
		while (itr.hasNext()) {
			this.addContacts(itr.next());
		}
		this.contactsToAdd = new ArrayList<Integer>();
	}
	
	public void addContacts(Integer person) {
		try {
			Connection con = EpiSimUtil.dbConnect();
			ResultSet getContactsQ = con.createStatement().executeQuery(
					"SELECT person2ID,time,locationID FROM "+EpiSimUtil.sickConTbl+" WHERE person1ID="+person);
			while (getContactsQ.next()) {
				long time = getContactsQ.getLong("time");
				Integer contact = new Integer(getContactsQ.getInt("person2ID"));
				Integer location = new Integer(getContactsQ.getInt("locationID"));
				this.addContact(new Long(time), person, contact, location);
				
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void addContact(Long time, Integer inf, Integer victim, Integer location) {
		if (this.sickContacts.containsKey(time)) {
			if (this.sickContacts.get(time).containsKey(inf)) {
				this.sickContacts.get(time).get(inf).add(victim);
				this.sickContactsLoc.get(time).get(inf).put(victim, location);
			} else {
				TreeSet<Integer> vicSet = new TreeSet<Integer>();
				vicSet.add(victim);
				this.sickContacts.get(time).put(inf, vicSet);
				TreeMap<Integer,Integer> locMap = new TreeMap<Integer,Integer>();
				locMap.put(victim, location);
				this.sickContactsLoc.get(time).put(inf, locMap);
			}
		} else {
			TreeMap<Integer,TreeSet<Integer>> infMap = new TreeMap<Integer,TreeSet<Integer>>();
			TreeSet<Integer> vicSet = new TreeSet<Integer>();
			vicSet.add(victim);
			infMap.put(inf, vicSet);
			this.sickContacts.put(time, infMap);
			TreeMap<Integer,TreeMap<Integer,Integer>> locInfMap = new TreeMap<Integer,TreeMap<Integer,Integer>>();
			TreeMap<Integer,Integer> locMap = new TreeMap<Integer,Integer>();
			locMap.put(victim, location);
			locInfMap.put(inf, locMap);
			this.sickContactsLoc.put(time, locInfMap);
		}
	}
	
//	public LinkedList<Long> getContacts(long startTime, long endTime) {
//		LinkedList<Long> contacts = new LinkedList<Long>();
//		startTime = EpiSimUtil.timeToTimeOfDay(startTime);
//		endTime = EpiSimUtil.timeToTimeOfDay(endTime);
//		Iterator<Long> itr = this.sickContacts.keySet().iterator();
//		while (itr.hasNext()) {
//			Long next = itr.next();
//			if (next.longValue() >= startTime && next.longValue() < endTime) {
//				contacts.add(next);
//			}
//		}
//		return contacts;
//	}
	
	public void applyContact(Long time) {
		if (this.sickContacts.containsKey(time)) {
			TreeMap<Integer,TreeSet<Integer>> infMap = this.sickContacts.get(time);
			if (!infMap.isEmpty()) {
				Integer inf = infMap.firstKey();
				this.applyContact(time, inf);
				inf = this.getNextInf(time, inf);
				while (inf != null) {
					this.applyContact(time, inf);
					inf = this.getNextInf(time, inf);
				}
			}
			this.flushContacts();
		}
	}
	
	public void applyContact(Long time, Integer inf) {
		int numDaysInfected = this.getNumDaysSinceInfection(inf);
		if (numDaysInfected >= 0 && numDaysInfected < sickLoadByDay.length) {
			if (this.sickContacts.containsKey(time)) {
				if (this.sickContacts.get(time).containsKey(inf)) {
					TreeSet<Integer> vicSet = this.sickContacts.get(time).get(inf);
					if (!vicSet.isEmpty()) {
						Integer vic = vicSet.first();
						this.applyContact(inf, vic, time);
						vic = this.getNextVic(time, inf, vic);
						while (vic != null) {
							this.applyContact(inf, vic, time);
							vic = this.getNextVic(time, inf, vic);
						}
					}
				}
			}
		} else {
//			this.sickContacts.get(time).remove(inf);
		}
	}
	
	public Long getNextContactTime(Long time) {
		if (time.equals(this.sickContacts.lastKey()))
			return null;
		time = new Long(time.longValue()+1);
		SortedMap<Long,TreeMap<Integer,TreeSet<Integer>>> tailMap = this.sickContacts.tailMap(time);
		return tailMap.firstKey();
	}
	
	public Integer getNextInf(Long time, Integer inf) {
		if (this.sickContacts.containsKey(time)) {
			TreeMap<Integer,TreeSet<Integer>> infMap = this.sickContacts.get(time);
			if (inf.equals(infMap.lastKey()))
				return null;
			inf = new Integer(inf.intValue()+1);
			SortedMap<Integer,TreeSet<Integer>> tailMap = infMap.tailMap(inf);
			return tailMap.firstKey();
		}
		return null;
	}
	
	public Integer getNextVic(Long time, Integer inf, Integer vic) {
		if (this.sickContacts.containsKey(time)){
			if (this.sickContacts.get(time).containsKey(inf)) {
				TreeSet<Integer> vicSet = this.sickContacts.get(time).get(inf);
				if (vic.equals(vicSet.last()))
					return null;
				vic = new Integer(vic.intValue()+1);
				SortedSet<Integer> tailSet = vicSet.tailSet(vic);
				return tailSet.first();
			}
		}
		return null;
	}
	
//	public void applyContact(Long time) {
//		if (this.sickContacts.containsKey(time)) {
//			Integer[] contact = this.sickContacts.get(time);
//			if (contact.length == 2)
//				this.applyContact(contact[0], contact[1], time);
//			else
//				System.out.println("Sick Contact Array not correct length.");
//		}
//	}
	
	public void applyContact(Integer inf, Integer victim, Long time) {
		Integer location = this.sickContactsLoc.get(time).get(inf).get(victim);
		if (this.myPolicies != null && !this.isValidTransEvent(inf, victim, location, this.curTime))
			return;
		int load = this.getInfectiousLoad(inf);
		this.decInfectionLoad(victim, location, load);
		if (DendroReSim.doTrack && (victim.intValue() == DendroReSim.track || victim.intValue() == DendroReSim.track2))
			System.out.println(victim+"\t"+inf+"\t"+this.curTime+"\t"+load+"\t"+this.infectionLoad.get(victim));
	}
	
	public void decInfectionLoad(Integer person, Integer location, int load) {
		if (this.infectionLoad.containsKey(person)) {
			int prevLoad = this.infectionLoad.get(person).intValue();
			Integer newLoad = new Integer(prevLoad - load);
			if (newLoad.intValue() <= 0) {
				this.infect(person, location, this.curTime);
			} else {
				this.infectionLoad.put(person, newLoad);
			}
		}
	}
	
	public int getInfectiousLoad(Integer infectedID) {
		if (isInfected(infectedID)) {
			int numDaysSick = getNumDaysSinceInfection(infectedID);
			if (numDaysSick < sickLoadByDay.length && numDaysSick >= 0)
				return sickLoadByDay[numDaysSick];
		}
		return 0;
	}
	
	public boolean isInfected(Integer person) {
		return this.infections.containsKey(person);
	}
	
	public int getNumDaysSinceInfection(Integer person) {
		long infectionTime = this.infections.get(person).longValue();
		long timeInfectious = this.curTime - infectionTime;
		return EpiSimUtil.secondsToDays(timeInfectious);
	}
	
	public void stepSim() {
		long startTime = this.curTime;
		long endTime = startTime + EpiSimUtil.dayToSeconds(1);
		TreeMap<Long,Integer> spuriousInf = this.getSpuriousInfectionChildren(startTime, endTime);
		Iterator<Long> spurItr = spuriousInf.keySet().iterator();
		Long spurInf = null;
		if (spurItr.hasNext()) spurInf = spurItr.next();
		Long time = this.sickContacts.firstKey();
		while (spurInf != null && spurInf.compareTo(time) <= 0) {
			this.curTime = spurInf.longValue();
			Integer person = spuriousInf.get(spurInf);
			this.infect(person, this.spuriousInfloc.get(person), spurInf);
			this.flushContacts();
			if (spurItr.hasNext())
				spurInf = spurItr.next();
			else
				spurInf = null;
		}
		this.curTime = startTime + time.longValue();
		this.applyContact(time);
		while (!time.equals(this.sickContacts.lastKey())) {
			time = this.getNextContactTime(time);
			while (spurInf != null && spurInf.compareTo(time) <= 0) {
				this.curTime = spurInf.longValue();
				Integer person = spuriousInf.get(spurInf);
				this.infect(person, this.spuriousInfloc.get(person), spurInf);
				this.flushContacts();
				if (spurItr.hasNext())
					spurInf = spurItr.next();
				else
					spurInf = null;
			}
			this.curTime = startTime + time.longValue();
			this.applyContact(time);
		}
		while (spurInf != null) {
			this.curTime = spurInf.longValue();
			Integer person = spuriousInf.get(spurInf);
			this.infect(person, this.spuriousInfloc.get(person), spurInf);
			this.flushContacts();
			if (spurItr.hasNext())
				spurInf = spurItr.next();
			else
				spurInf = null;
		}
		this.curTime = endTime;
	}
	
	public boolean isValidTransEvent(Integer infected, Integer victim, Integer location, long time) {
		if (this.myPolicies == null)
			return true;
		Iterator<Policy> itr = this.myPolicies.iterator();
		while (itr.hasNext()) {
			if (!itr.next().isValidTransEvent(infected, victim, location, time))
				return false;
		}
		return true;
	}
	
	public void newInf(Integer victim, Integer location, long time) {
		if (this.myPolicies == null)
			return;
		Iterator<Policy> itr = this.myPolicies.iterator();
		while (itr.hasNext()) {
			itr.next().newInf(victim, location, time);
		}
	}
	
	public TreeMap<Long,Integer> getSpuriousInfectionChildren(long startTime, long endTime) {
		TreeMap<Long,Integer> inf = new TreeMap<Long,Integer>();
		try {
			Connection con = EpiSimUtil.dbConnect();
			ResultSet getInfQ = con.createStatement().executeQuery(
					"SELECT new_dendro.victimID,new_dendro.time2,new_dendro.locationID FROM new_dendro INNER JOIN dendro_contact ON new_dendro.victimID = dendro_contact.victimID WHERE numInfected = 1 AND time2 >= "
					+startTime+" AND time2 < "+endTime+" AND infectedID NOT IN (SELECT victimID FROM new_dendro UNION SELECT id FROM initInfected)");
			while (getInfQ.next()) {
				Integer victim = new Integer(getInfQ.getInt("new_dendro.victimID"));
				Long time = new Long(getInfQ.getLong("new_dendro.time2"));
				inf.put(time, victim);
				Integer location = new Integer(getInfQ.getInt("new_dendro.locationID"));
				this.spuriousInfloc.put(victim, location);
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return inf;
	}
	
	public void printMissingSickPeople() {
		Iterator<Integer> itr = EpiSimUtil.getSickPeople(this.curTime).iterator();
		while (itr.hasNext()) {
			Integer person = itr.next();
			if (!this.infections.containsKey(person))
				System.out.println("Should be sick but isn't: "+person);
		}
	}
	
	public HashSet<Integer> getInfectiousPeople() {
		HashSet<Integer> people = new HashSet<Integer>();
		Iterator<Integer> itr = this.infections.keySet().iterator();
		while (itr.hasNext()) {
			Integer p = itr.next();
			if (this.getInfectiousLoad(p) > 0)
				people.add(p);
		}
		return people;
	}
	
	public HashSet<Integer> getAtRiskPeople() {
		return this.getAtRiskPeople(3*2724);
	}
	
	public HashSet<Integer> getAtRiskPeople(int threshold) {
		HashSet<Integer> atRisk = new HashSet<Integer>();
		Iterator<Integer> people = this.infectionLoad.keySet().iterator();
		while (people.hasNext()) {
			Integer p = people.next();
			if (this.infectionLoad.get(p).intValue() <= threshold) {
				atRisk.add(p);
			}
		}
		return atRisk;
	}
	
	public HashSet<Integer> getSickNeighborhood(int distance) {
		HashSet<Integer> people = new HashSet<Integer>();
		Iterator<Integer> infp = this.getInfectiousPeople().iterator();
		while (infp.hasNext()) {
			Integer p = infp.next();
			people.add(p);
			people.addAll(EpiSimUtil.getNeighborhood(p, distance));
		}
		return people;
	}
	
	public void saveState() {
		int day = EpiSimUtil.secondsToDays(this.curTime);
		try {
			PrintStream ps = new PrintStream(savedStateDir+"/"+day+"/infections.dat");
			Iterator<Integer> itr = this.infections.keySet().iterator();
			while (itr.hasNext()) {
				Integer next = itr.next();
				if (this.isInfected(next))
					ps.println(next+"\t"+this.infections.get(next));
			}
			ps = new PrintStream(savedStateDir+"/"+day+"/infload.dat");
			itr = this.infectionLoad.keySet().iterator();
			while (itr.hasNext()) {
				Integer next = itr.next();
				ps.println(next+"\t"+this.infectionLoad.get(next));
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	/////////////////////////////////////////////// STATIC ///////////////////////////////////////////

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		runSimpleSim();
//		testSickNeighborhood();
//		evolutionOfSickNeighborhood(Integer.parseInt(args[0]));
//		testSaveState(Integer.parseInt(args[0]));
//		saveAllDays();
		testAllSavedDays(Integer.parseInt(args[0]));

	}
	
	public static void runSimpleSim() {
		//System.out.println("track:\t"+DendroReSim.track);
		DendroReSim sim = new DendroReSim();
		for (int i=0; i < 50; i++) {
			sim.stepSim();
			int day = EpiSimUtil.secondsToDays(sim.curTime);
			double actNumSick = EpiSimUtil.getNumPeopleSick(sim.curTime);
			double numSick = sim.infections.size();
			double percent = (numSick - actNumSick)/actNumSick;
			System.out.println("Day "+day+":\t"+actNumSick+"\t"+numSick+"\t"+percent);
			if (day == 10)
				sim.printMissingSickPeople();
		}
	}
	
	public static void testSickNeighborhood() {
		DendroReSim sim = new DendroReSim();
		EpiSimUtil.saveContacts = true;
		System.out.println(EpiSimUtil.secondsToDays(sim.curTime)+"\t0\t"+sim.getSickNeighborhood(0).size());
		System.out.println(EpiSimUtil.secondsToDays(sim.curTime)+"\t1\t"+sim.getSickNeighborhood(1).size());
		System.out.println(EpiSimUtil.secondsToDays(sim.curTime)+"\t2\t"+sim.getSickNeighborhood(2).size());
		sim.stepSim();
		System.out.println(EpiSimUtil.secondsToDays(sim.curTime)+"\t0\t"+sim.getSickNeighborhood(0).size());
		System.out.println(EpiSimUtil.secondsToDays(sim.curTime)+"\t1\t"+sim.getSickNeighborhood(1).size());
		System.out.println(EpiSimUtil.secondsToDays(sim.curTime)+"\t2\t"+sim.getSickNeighborhood(2).size());
	}
	
	public static void evolutionOfSickNeighborhood(int radius) {
		DendroReSim sim = new DendroReSim();
//		EpiSimUtil.saveContacts = true;
		System.out.println("day\tradius\tneighborhood pop\tinfectious pop");
		System.out.println(EpiSimUtil.secondsToDays(sim.curTime)+"\t"+radius+"\t"+
				sim.getSickNeighborhood(radius).size()+"\t"+sim.getInfectiousPeople().size());
		for (int i=0; i < 100; i++) {
			sim.stepSim();
			System.out.println(EpiSimUtil.secondsToDays(sim.curTime)+"\t"+radius+"\t"+
					sim.getSickNeighborhood(radius).size()+"\t"+sim.getInfectiousPeople().size());
		}
	}
	
	public static void saveAllDays() {
		DendroReSim sim = new DendroReSim();
		for (int i=0; i < 100; i++) {
			sim.stepSim();
			int day = EpiSimUtil.secondsToDays(sim.curTime);
			File dir = new File(DendroReSim.savedStateDir+"/"+day);
			dir.mkdir();
			sim.saveState();
			System.out.println(day+"\t"+sim.getInfectiousPeople().size());
		}
	}
	
	public static void testAllSavedDays(int delay) {
		DendroReSim sim = new DendroReSim();
		int day = 0;
		for (; day <= delay; day++) 
			sim.stepSim();
		for (; day < 100; day++) {
			DendroReSim loadedSim = new DendroReSim(day-delay);
			while (loadedSim.curTime < sim.curTime)
				loadedSim.stepSim();
			int inf1 = sim.getInfectiousPeople().size();
			int inf2 = loadedSim.getInfectiousPeople().size();
			int dif = inf1-inf2;
			System.out.println(EpiSimUtil.secondsToDays(sim.curTime)+"\t"+inf1+"\t"+inf2+"\t"+dif);
			sim.stepSim();
		}
	}
	
	
	public static void testSaveState(int saveDay) {
		DendroReSim sim = new DendroReSim();
		int day = 0;
		for (; day <= saveDay; day++)
			sim.stepSim();
		sim.saveState();
		int theDay = EpiSimUtil.secondsToDays(sim.curTime);
		for (; day < saveDay+5; day++) {
			System.out.println(EpiSimUtil.secondsToDays(sim.curTime)+"\t"+sim.getInfectiousPeople().size());
			sim.stepSim();
		}
		sim = new DendroReSim(theDay);
		for (day = 0; day < 5; day++) {
			System.out.println(EpiSimUtil.secondsToDays(sim.curTime)+"\t"+sim.getInfectiousPeople().size());
			sim.stepSim();
		}
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void testNextContact(){
		TreeMap<Integer,Integer> map = new TreeMap<Integer,Integer>();
		for (int i=0; i < 5; i++){
			Integer next = new Integer(2*i);
			map.put(next , next);
		}
		Integer prev = map.firstKey();
		System.out.println(map.get(prev));
		for (int i=0; i< 4; i++){
			prev = getNextContactTime(map,prev);
			System.out.println(map.get(prev));
		}
	}
	
	public static Integer getNextContactTime(TreeMap<Integer,Integer> map, Integer time) {
		time = new Integer(time.intValue()+1);
		SortedMap<Integer,Integer> tailMap = map.tailMap(time);
		return tailMap.firstKey();
	}

}
