package dbepi2a;
import java.sql.*;
import java.util.*;

public class DendroSimulation {
	
	public long curTime = 0;
	public HashMap<Integer,Long> infections = new HashMap<Integer,Long>();
	public ArrayList<Policy> myPolicies = new ArrayList<Policy>();

	public DendroSimulation(ArrayList<Policy> policies) {
		this.myPolicies = policies;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public int numInfected() {
		return infections.size();
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
				this.newInf(p, new Integer(0), 0);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void stepSim(long timeStep) {
		long endTime = this.curTime + timeStep;
		try {
			Connection con = EpiSimUtil.dbConnect();
			PreparedStatement getInfected = con.prepareStatement(
					"SELECT infectedID FROM "+EpiSimUtil.dendroConTbl+" WHERE victimID = ?");
			Statement getTransEvents = con.createStatement();
			ResultSet transEvents = getTransEvents.executeQuery(
					"SELECT victimID,time,locationID FROM "+EpiSimUtil.dendroTbl+" WHERE time > "+this.curTime+
					" AND time <= "+endTime+" ORDER BY time ASC");
			while (transEvents.next()){
				Integer victim = new Integer(transEvents.getInt("victimID"));
				Integer location = new Integer(transEvents.getInt("locationID"));
				long time = transEvents.getLong("time");
				getInfected.setInt(1, victim.intValue());
				ResultSet infectedIDs = getInfected.executeQuery();
				LinkedList<Integer> infectedPeople = new LinkedList<Integer>();
				boolean infectedPersonPresent = false;
				while(infectedIDs.next()){
					Integer infectedPerson = new Integer(infectedIDs.getInt("infectedID"));
					infectedPeople.add(infectedPerson);
					infectedPersonPresent = infectedPersonPresent || this.isInfected(infectedPerson, time);
				}
				if (infectedPersonPresent){
					//this.infect(victim, time);
					Iterator<Integer> itr = infectedPeople.iterator();
					while (itr.hasNext()){
						Integer infected = itr.next();
						this.infect(infected, victim, location, time);
						Iterator<Integer> itr2 = infectedPeople.iterator();
						while (itr2.hasNext()) {
							this.infect(infected, itr2.next(), location, time);
						}
					}
				}
			}
			this.curTime = endTime;
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public boolean isInfected(Integer person, long time) {
		if (!this.infections.containsKey(person)) return false;
		return this.infections.get(person).longValue() <= time;
	}
	
	public boolean infect(Integer infected, Integer victim, Integer location, long time) {
		boolean isValid = this.isValidTransEvent(infected, victim, location, time);
		if (isValid) {
			this.infections.put(victim, new Long(time));
			this.newInf(victim, location, time);
		}
		return isValid;
	}
	
	public boolean isValidTransEvent(Integer infected, Integer victim, Integer location, long time) {
		if (infected.equals(victim) || !isInfected(infected,time) || isInfected(victim,time)) return false;
		boolean isValid = true;
		Iterator<Policy> itr = this.myPolicies.iterator();
		while(itr.hasNext()) {
			isValid = isValid && itr.next().isValidTransEvent(infected, victim, location, time);
		}
		return isValid;
	}
	
	public void newInf(Integer victim, Integer location, long time) {
		Iterator<Policy> itr = this.myPolicies.iterator();
		while(itr.hasNext()) {
			itr.next().newInf(victim, location, time);
		}
	}

}
