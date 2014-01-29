package dbepi2a;

import java.sql.*;
import java.util.*;

public class DendroLoadCalc {
	
	public static final boolean doInsert = false;
	
//	public static final double[] sickLoadByDay = {0.0486,0.1793,0.2724,0.2606,0.1453,0.0660,0.0219,0.0061};
	public static final int[] sickLoadByDay = {486,1793,2724,2606,1453,660,219,61};
	
	public long curTime = 0;
	public HashMap<Integer,Long> infections = new HashMap<Integer,Long>();
	public HashMap<Integer,Integer> infectionLoad = new HashMap<Integer,Integer>();
	public TreeMap<Long,TreeMap<Integer,TreeSet<Integer>>> sickContacts = new TreeMap<Long,TreeMap<Integer,TreeSet<Integer>>>();
	
	public ArrayList<Integer> contactsToAdd = new ArrayList<Integer>();
	public HashSet<Integer> inserted = new HashSet<Integer>();
	
	public HashSet<Integer> spurious = new HashSet<Integer>(20000);

	public DendroLoadCalc() {
		this.getIntitiallyInfected();
		this.loadSpuriousSecondaryInf();
	}
	
	public void printContacts() {
		Iterator<Long> timeItr = this.sickContacts.keySet().iterator();
		while (timeItr.hasNext()) {
			Long time = timeItr.next();
			TreeMap<Integer,TreeSet<Integer>> infMap = this.sickContacts.get(time);
			Iterator<Integer> infMapItr = infMap.keySet().iterator();
			while (infMapItr.hasNext()) {
				Integer inf = infMapItr.next();
				Iterator<Integer> vicItr = infMap.get(inf).iterator();
				while (vicItr.hasNext()) {
					System.out.println(time+"\t"+inf+"\t"+vicItr.next());
				}
				System.out.println();
			}
			System.out.println();
		}
	}
	
	public void getIntitiallyInfected(){
		try {
			Connection con = EpiSimUtil.dbConnect();
			Statement getInitInfected = con.createStatement();
			ResultSet initInfected = getInitInfected.executeQuery(
					"SELECT id FROM "+EpiSimUtil.initTbl);
			while(initInfected.next()){
				Integer p = new Integer(initInfected.getInt(1));
				this.infect(p, new Long(0));
			}
			this.flushContacts();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void insertSickLoad() {
		if (doInsert) {
			try {
				Connection con = EpiSimUtil.dbConnect();
				PreparedStatement insert = con.prepareStatement(
						"INSERT INTO "+EpiSimUtil.sickLoadTbl+" (personID,amount) VALUES(?,?)");
				Iterator<Integer> people = this.infectionLoad.keySet().iterator();
				while (people.hasNext()) {
					Integer person = people.next();
					if (!this.spurious.contains(person) && !this.inserted.contains(person) && this.isInfected(person)) {
						int amount = this.infectionLoad.get(person).intValue();
						insert.setInt(1, person.intValue());
						insert.setInt(2, amount);
						insert.executeUpdate();
						this.inserted.add(person);
					}
				}
				con.close();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}
	
	public void infect(Integer victim, long time) {
		if (DendroReSim.doTrack && (victim.intValue() == DendroReSim.track || victim.intValue() == DendroReSim.track2))
			System.out.println("infect:\t"+victim+"\t"+this.curTime+"\t"+this.infectionLoad.get(victim));
		this.infections.put(victim, new Long(time));
		this.cacheContact(victim);
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
		if (DendroReSim.doTrack && (person.intValue() == DendroReSim.track || person.intValue() == DendroReSim.track2))
			System.out.println("adding contacts: "+person+" time: "+this.curTime);
		try {
			Connection con = EpiSimUtil.dbConnect();
			ResultSet getContactsQ = con.createStatement().executeQuery(
					"SELECT person2ID,time FROM "+EpiSimUtil.sickConTbl+" WHERE person1ID="+person);
			while (getContactsQ.next()) {
				long time = getContactsQ.getLong("time");
				Integer contact = new Integer(getContactsQ.getInt("person2ID"));
				this.addContact(new Long(time), person, contact);
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void addContact(Long time, Integer inf, Integer victim) {
		if (this.sickContacts.containsKey(time)) {
			if (this.sickContacts.get(time).containsKey(inf))
				this.sickContacts.get(time).get(inf).add(victim);
			else {
				TreeSet<Integer> vicSet = new TreeSet<Integer>();
				vicSet.add(victim);
				this.sickContacts.get(time).put(inf, vicSet);
			}
		} else {
			TreeMap<Integer,TreeSet<Integer>> infMap = new TreeMap<Integer,TreeSet<Integer>>();
			TreeSet<Integer> vicSet = new TreeSet<Integer>();
			vicSet.add(victim);
			infMap.put(inf, vicSet);
			this.sickContacts.put(time, infMap);
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
	
//	public void applyContact(Long time) {
//		if (this.sickContacts.containsKey(time)) {
//			TreeMap<Integer,TreeSet<Integer>> infMap = this.sickContacts.get(time);
//			Iterator<Integer> infMapItr = infMap.keySet().iterator();
//			while (infMapItr.hasNext()) {
//				Integer inf = infMapItr.next();
//				if (this.getNumDaysSinceInfection(inf) < sickLoadByDay.length) {
//					Iterator<Integer> vicItr = infMap.get(inf).iterator();
//					while (vicItr.hasNext()) {
//						this.applyContact(inf, vicItr.next(), time);
//					}
//				} else {
//					infMap.remove(inf);
//				}
//			}
//		}
//	}
	
	public void applyContact(Integer inf, Integer victim, Long time) {
		int load = this.getInfectiousLoad(inf);
		if (!this.isInfected(victim))
			this.incInfectionLoad(victim, load);
		if (DendroReSim.doTrack && (victim.intValue() == DendroReSim.track || victim.intValue() == DendroReSim.track2))
			System.out.println(victim+"\t"+inf+"\t"+this.curTime+"\t"+load+"\t"+this.infectionLoad.get(victim));
	}
	
	public void incInfectionLoad(Integer person, int load) {
		if (this.infectionLoad.containsKey(person)) {
			int prevLoad = this.infectionLoad.get(person).intValue();
			Integer newLoad = new Integer(prevLoad + load);
			this.infectionLoad.put(person, newLoad);					
		} else {
			this.infectionLoad.put(person, new Integer(load));
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
		if (!this.infections.containsKey(person))
			return -1;
		long infectionTime = this.infections.get(person).longValue();
		long timeInfectious = this.curTime - infectionTime;
		return EpiSimUtil.secondsToDays(timeInfectious);
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
	
	public void stepSim() {
		long startTime = this.curTime;
		long endTime = this.curTime + EpiSimUtil.dayToSeconds(1);
		try {
			Connection con = EpiSimUtil.dbConnect();
			ResultSet getInfQ = con.createStatement().executeQuery(
					"SELECT victimID,time2 FROM "+EpiSimUtil.dendroTbl+" WHERE time2 >= "
					+startTime+" AND time2 < "+endTime+" ORDER BY time2 ASC");
			Long contact = this.sickContacts.firstKey();
			while (getInfQ.next()) {
				long infTime = getInfQ.getLong("time2");
				Integer victim = new Integer(getInfQ.getInt("victimID"));
				while (contact != null && contact.longValue()+startTime <= infTime) {
//					System.out.println("apply contact "+contact);
					this.curTime = contact.longValue()+startTime;
					this.applyContact(contact);
					contact = this.getNextContactTime(contact);
				}
//				System.out.println("do infection "+infTime);
				this.curTime = infTime;
				this.infect(victim, infTime);
				this.flushContacts();
			}
			while (contact != null) {
//				System.out.println("apply contact "+contact);
				this.curTime = contact.longValue()+startTime;
				this.applyContact(contact);
				contact = this.getNextContactTime(contact);
			}
			this.insertSickLoad();
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		this.curTime = endTime;
//		System.out.println("Day "+EpiSimUtil.secondsToDays(this.curTime)+"\tnum inf: "+this.infections.size());
	}
	
	public void loadSpuriousSecondaryInf() {
		try {
			Connection con = EpiSimUtil.dbConnect();
			ResultSet getInfQ = con.createStatement().executeQuery(
					"SELECT new_dendro.victimID FROM new_dendro INNER JOIN dendro_contact ON new_dendro.victimID = dendro_contact.victimID WHERE numInfected = 1"
					+" AND infectedID NOT IN (SELECT victimID FROM new_dendro UNION SELECT id FROM initInfected)");
			while (getInfQ.next()) {
				Integer victim = new Integer(getInfQ.getInt("new_dendro.victimID"));
				this.spurious.add(victim);
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	//////////////////////////////// STATIC ///////////////////////////////////

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		runSim();

	}
	
	public static void runSim(){
		DendroLoadCalc sim = new DendroLoadCalc();
		for (int i=0; i < 101; i++) {
			sim.stepSim();
			int day = EpiSimUtil.secondsToDays(sim.curTime);
			System.out.println("Day "+day+":\t"+EpiSimUtil.getNumPeopleSick(sim.curTime)+"\t"+sim.infections.size());
		}
//		sim.printContacts();
		sim.insertSickLoad();
	}

}
