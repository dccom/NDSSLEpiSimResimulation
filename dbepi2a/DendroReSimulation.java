package dbepi2a;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class DendroReSimulation {
	
	public static final double[] sickLoadByDay = {0.0486,0.1793,0.2724,0.2606,0.1453,0.0660,0.0219,0.0061};
	public static final double minInfectiousContactDuration = 0.016666699200868607;
	
	public long curTime = 0;
	public HashMap<Integer,Long> infections = new HashMap<Integer,Long>();
	public HashMap<Integer,Double> infectionLoad = new HashMap<Integer,Double>();
	public HashMap<Integer,ArrayList<Integer>> sickContacts = new HashMap<Integer,ArrayList<Integer>>();

	public DendroReSimulation() {
		this.getIntitiallyInfected();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		populateSickLoadTbl();

	}
	
	public static void populateSickLoadTbl() {
		long endTime = EpiSimUtil.dayToSeconds(100);
		DendroReSimulation sim = new DendroReSimulation();
		while (sim.curTime < endTime) {
			sim.stepSim(EpiSimUtil.dayToSeconds(1));
		}
		sim.insertSickLoad();
	}
	
	public void insertSickLoad() {
		try {
			Connection con = EpiSimUtil.dbConnect();
			PreparedStatement insert = con.prepareStatement(
					"INSERT INTO "+EpiSimUtil.sickLoadTbl+" (personID,amount) VALUES(?,?)");
			Iterator<Integer> people = this.infectionLoad.keySet().iterator();
			while (people.hasNext()) {
				Integer person = people.next();
				double amount = this.infectionLoad.get(person).doubleValue();
				insert.setInt(1, person.intValue());
				insert.setDouble(2, amount);
				insert.executeUpdate();
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
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
				this.infections.put(p, new Long(0));
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void stepSim(long timeStep) {
		long startTime = this.curTime;
		this.curTime = this.curTime + timeStep;
		try {
			Connection con = EpiSimUtil.dbConnect();
			PreparedStatement getInfected = con.prepareStatement(
					"SELECT infectedID FROM "+EpiSimUtil.dendroConTbl+" WHERE victimID = ?");
			Statement getTransEvents = con.createStatement();
			ResultSet transEvents = getTransEvents.executeQuery(
					"SELECT victimID,time,locationID FROM "+EpiSimUtil.dendroTbl+" WHERE time > "+startTime+
					" AND time <= "+this.curTime+" ORDER BY time ASC");
//			this.updateSickLoad();
			while (transEvents.next()){
				Integer victim = new Integer(transEvents.getInt("victimID"));
				long time = transEvents.getLong("time");
				getInfected.setInt(1, victim.intValue());
				this.infect(victim, time);
			}
			this.updateSickLoad();
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public boolean isInfected(Integer person, long time) {
		if (!this.infections.containsKey(person)) return false;
		return this.infections.get(person).longValue() <= time;
	}
	
	public void infect(Integer victim, long time) {
		this.infections.put(victim, new Long(time));
	}
	
	public boolean isInfected(Integer person) {
		return this.infections.containsKey(person);
	}
	
	public void updateSickLoad() {
		Iterator<Integer> sickItr = this.infections.keySet().iterator();
		while (sickItr.hasNext()) {
			Integer next = sickItr.next();
			this.updateSickLoad(next);
		}
	}
	
	public void updateSickLoad(Integer infectedID) {
		Iterator<Integer> contacts = getPotSickContacts(infectedID).iterator();
		while (contacts.hasNext()) {
			Integer contact = contacts.next();
			if (!this.isInfected(contact)) {
				double infectiousLoad = getInfectiousLoad(infectedID);
				incInfectionLoad(contact, infectiousLoad);
			}
		}
	}
	
	public ArrayList<Integer> getPotSickContacts(Integer infectedID) {
		if (!this.sickContacts.containsKey(infectedID)) {
			try {
				Connection con = EpiSimUtil.dbConnect();
				ResultSet getSickContactsQ = con.createStatement().executeQuery(
						"SELECT person2ID FROM "+EpiSimUtil.sickConTbl+" WHERE person1ID="+infectedID);
				getSickContactsQ.last();
				int numContacts = getSickContactsQ.getRow();
				getSickContactsQ.beforeFirst();
				ArrayList<Integer> contacts;
				if (numContacts > 0) {
					contacts = new ArrayList<Integer>(numContacts);
					while (getSickContactsQ.next()) {
						contacts.add(new Integer(getSickContactsQ.getInt("person2ID")));
					}
				} else {
					contacts = new ArrayList<Integer>(1);
				}
				this.sickContacts.put(infectedID, contacts);
				con.close();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		return this.sickContacts.get(infectedID);
	}
	
	public void incInfectionLoad(Integer person, double load) {
		if (this.infectionLoad.containsKey(person)) {
			double prevLoad = this.infectionLoad.get(person).doubleValue();
			Double newLoad = new Double(prevLoad + load);
			this.infectionLoad.put(person, newLoad);					
		} else {
			this.infectionLoad.put(person, new Double(load));
		}
	}
	
	public double getInfectiousLoad(Integer infectedID) {
		if (isInfected(infectedID)) {
			int numDaysSick = getNumDaysSinceInfection(infectedID);
			if (numDaysSick-1 < sickLoadByDay.length && numDaysSick-1 >= 0)
				return sickLoadByDay[numDaysSick-1];
		}
		return 0.0;
	}
	
	public int getNumDaysSinceInfection(Integer person) {
		long infectionTime = this.infections.get(person).longValue();
		long timeInfectious = this.curTime - infectionTime;
		return EpiSimUtil.secondsToDays(timeInfectious);
	}

}
