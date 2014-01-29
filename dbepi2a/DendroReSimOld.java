package dbepi2a;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class DendroReSimOld {
	
	public static final double[] sickLoadByDay = {0.0486,0.1793,0.2724,0.2606,0.1453,0.0660,0.0219,0.0061};
	public static final double minInfectiousContactDuration = 0.016666699200868607;
	
	public long curTime = 0;
	public HashMap<Integer,Long> infections = new HashMap<Integer,Long>();
	public HashMap<Integer,Double> infectionLoad = new HashMap<Integer,Double>();
	public ArrayList<Policy> myPolicies = new ArrayList<Policy>();
	public HashMap<Integer,ArrayList<Integer>> sickContacts = new HashMap<Integer,ArrayList<Integer>>();

	public DendroReSimOld(ArrayList<Policy> policies) {
		this.myPolicies = policies;
		this.getIntitiallyInfected();
		this.getInfectionLoad();
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
	
	public void getInfectionLoad() {
		try {
			Connection con = EpiSimUtil.dbConnect();
			ResultSet getInfLoadQ = con.createStatement().executeQuery(
					"SELECT personID,amount FROM "+EpiSimUtil.sickLoadTbl);
			while (getInfLoadQ.next()) {
				Integer person = new Integer(getInfLoadQ.getInt("personID"));
				Double amount = new Double(getInfLoadQ.getDouble("amount"));
				this.infectionLoad.put(person, amount);
			}
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
		Iterator<Integer> loadItr = this.infectionLoad.keySet().iterator();
		while (loadItr.hasNext()) {
			Integer next = loadItr.next();
			if (this.infectionLoad.get(next).doubleValue() <= 0.0) {
				this.infect(next, new Long(this.curTime));
				loadItr.remove();
			}
		}
	}
	
	public void updateSickLoad(Integer infectedID) {
		Iterator<Integer> contacts = getPotSickContacts(infectedID).iterator();
		while (contacts.hasNext()) {
			Integer contact = contacts.next();
			if (!this.isInfected(contact)) {
				double infectiousLoad = getInfectiousLoad(infectedID);
				decInfectionLoad(contact, infectiousLoad);
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
	
	public void decInfectionLoad(Integer person, double load) {
		if (this.infectionLoad.containsKey(person)) {
			double prevLoad = this.infectionLoad.get(person).doubleValue();
			Double newLoad = new Double(prevLoad - load);
			this.infectionLoad.put(person, newLoad);
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
	
	public void stepSim() {
		this.curTime += EpiSimUtil.dayToSeconds(1);
		this.updateSickLoad();
		Iterator<Integer> sickItr = this.infections.keySet().iterator();
		while (sickItr.hasNext()) {
			Integer next = sickItr.next();
			long calcTime = this.infections.get(next);
			if (calcTime == this.curTime) {
				int calcDays = EpiSimUtil.secondsToDays(calcTime);
				int actDays = EpiSimUtil.secondsToDays(EpiSimUtil.getInfectionTime(next));
				if (calcDays != actDays)
					System.out.println("Victim: "+next+"\tActual Day: "+actDays+"\tCalc Day: "+calcDays);
			}
		}
	}
	
	//////////////////////////////////////// STATIC ///////////////////////////////////////

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		runSimpleSim();

	}
	
	public static void runSimpleSim() {
		DendroReSimOld sim = new DendroReSimOld(new ArrayList<Policy>(0));
		while (sim.curTime <= EpiSimUtil.dayToSeconds(100))
			sim.stepSim();
		System.out.println("Number of infected individuals: "+sim.infections.size());
	}
	

}
