/**
 * 
 */
package dbepi;
import java.sql.*;
import java.util.*;

/**
 * @author dave
 *
 */
public class PPContactWalk {
	public static long randSeed;// = 1154622729289L; //7;
	public static int walkLength = 10; // 10 was chosen because it is the mean generation number in first 40 days
	public static int numWalks = 10000;
	public static int numRounds = 10000;
//	public static int population = 1615860;
	public static int run = 5;
	public static Random rand;// = new Random(randSeed);
	
	public HashMap<Integer, Person> people;
	
	private Connection dbcon;
	private PreparedStatement neighborSelect;
	private PreparedStatement numContactSelect;
	private Integer curPerson;
	private Integer prevPerson;

	/**
	 * 
	 */
	public PPContactWalk() {
		curPerson = null;
		prevPerson = null;
		this.dbcon = EpiSimUtil.dbConnect();
		this.people = new HashMap<Integer, Person>(EpiSimUtil.population);
		try {
			this.neighborSelect = this.dbcon.prepareStatement(
					"SELECT person2ID FROM "+EpiSimUtil.conTbl+" WHERE person1ID = ?");
			this.numContactSelect = this.dbcon.prepareStatement(
					"SELECT tally FROM "+EpiSimUtil.conCountTbl+" WHERE personID = ?");
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		test();
		multiRound();

	}
	
	public static void test(){
//		int round = 0;
		
		long startTime = (new java.util.Date()).getTime();
		PPContactWalk walk = new PPContactWalk();
		for (int i=0; i < numWalks; i++) {
			Integer next = new Integer(rand.nextInt(EpiSimUtil.population) + 1);
			walk.walk(next);
		}
		long endTime = (new java.util.Date()).getTime();
		
		System.out.println("Walk length: "+walkLength);
		System.out.println("Number of walks: "+numWalks);
		System.out.println("Start time (ms): "+startTime);
		System.out.println("End time (ms): "+endTime);
		long duration = endTime-startTime;
		System.out.println("Duration (ms): "+duration);
		long durationMin = (duration/1000)/60;
		System.out.println("Duration (min): "+durationMin);
		
		walk.saveToDB();
		
		/////////////////////////////////////////////////////////
		
		startTime = (new java.util.Date()).getTime();
		walk = new PPContactWalk();
		for (int i=0; i < numWalks; i++) {
			Integer next = new Integer(rand.nextInt(EpiSimUtil.population) + 1);
			walk.walk(next);
		}
		endTime = (new java.util.Date()).getTime();
		
		System.out.println("Walk length: "+walkLength);
		System.out.println("Number of walks: "+numWalks);
		System.out.println("Start time (ms): "+startTime);
		System.out.println("End time (ms): "+endTime);
		duration = endTime-startTime;
		System.out.println("Duration (ms): "+duration);
		durationMin = (duration/1000)/60;
		System.out.println("Duration (min): "+durationMin);
		
		walk.saveToDB();
	}
	
	public static void multiRound(){
		randSeed = (new java.util.Date()).getTime();
		rand = new Random(randSeed);
		System.out.println("======================================");
		System.out.println("Random seed: "+randSeed);
		System.out.println("======================================");
		for (int round = 1; round <= numRounds; round++){
			long startTime = (new java.util.Date()).getTime();
			PPContactWalk walk = new PPContactWalk();
			for (int i=0; i < numWalks; i++) {
				Integer next = new Integer(rand.nextInt(EpiSimUtil.population) + 1);
				walk.walk(next);
			}
			long endTime = (new java.util.Date()).getTime();
			
			System.out.println("Round number: "+round);
			System.out.println("Next random long: "+rand.nextLong());
			System.out.println("Walk length: "+walkLength);
			System.out.println("Number of walks: "+numWalks);
			System.out.println("Start time (ms): "+startTime);
			System.out.println("End time (ms): "+endTime);
			long duration = endTime-startTime;
			System.out.println("Duration (ms): "+duration);
			long durationMin = (duration/1000)/60;
			System.out.println("Duration (min): "+durationMin);
			System.out.println("======================================");
			
			walk.saveToDB();
		}
	}
	
	public void printPosVisits(){
		int total = 0;
		Iterator<Integer> itr = this.people.keySet().iterator();
		while(itr.hasNext()){
			Integer key = itr.next();
			int tally = this.people.get(key).getTally();
			if (tally > 0) {
				System.out.println(key+"\t"+tally);
				total += tally;
			}
		}
		//System.out.println("TOTAL number of visits: "+total);
	}
	
	public void saveToDB(){
		try {
			Connection con = EpiSimUtil.dbGenDataConnect();
			PreparedStatement pInsert = con.prepareStatement(
					"INSERT INTO "+EpiSimUtil.ppInfTbl+
					" (runID, personID, score) VALUES("+run+", ?, ?)");
			PreparedStatement pUpdate = con.prepareStatement(
					"UPDATE "+EpiSimUtil.ppInfTbl+
					" SET score = ? WHERE runID = "+run+" AND personID = ?");
			PreparedStatement pSelect = con.prepareStatement(
					"SELECT score FROM "+EpiSimUtil.ppInfTbl+
					" WHERE runID = "+run+" AND personID = ?");
			Iterator<Integer> itr = this.people.keySet().iterator();
			Integer person, score;
			ResultSet rs;
			while(itr.hasNext()){
				person = itr.next();
				score = this.people.get(person).getTally();
				pSelect.setInt(1, person);
				rs = pSelect.executeQuery();
				rs.last();
				boolean entryExists = rs.getRow() > 0;
				if (entryExists) {
					score = new Integer(score+rs.getInt(1));
					pUpdate.setInt(1, score);
					pUpdate.setInt(2, person);
					pUpdate.addBatch();
				} else {
					pInsert.setInt(1, person);
					pInsert.setInt(2, score);
					pInsert.addBatch();
				}
				pInsert.executeBatch();
				pUpdate.executeBatch();
			}
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public void walk(Integer start){
		this.visit(start);
		this.prevPerson = null;
		this.curPerson = start;
		for (int i=0; i < walkLength; i++){
			move();
		}
	}
	
	/**
	 * Returns the number of contacts that a person has.
	 * @param id Integer identifier of query person
	 * @return number of contacts
	 */
	public int numNeighbors(Integer id){
		if (id == null) return -1;
		if (this.people.containsKey(id)){
			return this.people.get(id).contacts.length;
		}
		try {
			this.numContactSelect.setInt(1, id.intValue());
			ResultSet rs = this.numContactSelect.executeQuery();
			rs.first();
//			System.out.println("!! "+rs.getInt(1)+" !!");
			return rs.getInt(1);
		} catch (Exception e){
			try {
				System.out.println("Error: numNeighbors, id="+id.intValue()+" SQL statement: "+this.numContactSelect);
			} catch (Exception e2) {System.out.println("Got here"+e2);}
			System.out.println(e);
			return -1;
		}
	}
	
//	/**
//	 * Returns the id of the person whose contact appears in the nth row of 
//	 * the SELECT statement.
//	 * @param id Integer identifier of query person
//	 * @param n row to select (1,..,numberOfContacts)
//	 * @return Integer identifier of neighbor
//	 */
//	public Integer getNthNeighbor(Integer id, int n){
//		if (id == null) return null;
//		try {
//			this.neighborSelect.setInt(1, id);
//			ResultSet rs = this.neighborSelect.executeQuery();
//			rs.absolute(n);
//			return new Integer(rs.getInt(1));
//		} catch (Exception e){
//			System.out.println("Error: getNthNeighbor");
//			System.out.println(e);
//			return null;
//		}
//	}
	
	/**
	 * Returns the id of the person whose contact appears in the nth row of 
	 * the SELECT statement.
	 * @param id Integer identifier of query person
	 * @param n row to select (1,..,numberOfContacts)
	 * @return Integer identifier of neighbor
	 */
	public Integer getNthNeighbor(Integer id, int n){
		if (id == null) return null;
		if (this.people.containsKey(id)) return this.people.get(id).getNthContact(n);
		else {
			Integer[] contacts = getContacts(id);
			Person p = new Person(contacts);
			this.people.put(id, p);
			return p.getNthContact(n);
		}
	}
	
	public Integer[] getContacts(Integer id){
		if (id == null) return null;
		try {
			this.neighborSelect.setInt(1, id);
			ResultSet rs = this.neighborSelect.executeQuery();
			Integer numContacts = numNeighbors(id);
			Integer[] contacts = new Integer[numContacts];
			for (int i=0; rs.next(); i++){
				contacts[i] = rs.getInt(1);
			}
			return contacts;
		} catch (Exception e){
			System.out.println("Error: getNthNeighbor");
			System.out.println(e);
			return null;
		}
	}
	
	/**
	 * Make a move along the random walk.  Changes current person to
	 * a neighbor and increments the tally on the new current person.
	 * Neighbor is chosen by getNextPerson()
	 *
	 */
	public void move(){
		Integer next = this.getNextPerson();
		if (next == null) return;
		visit(next);
		this.prevPerson = this.curPerson;
		this.curPerson = next;
	}
	
	/**
	 * Randomly chooses a neighbor of the current person.  Each contact 
	 * is given equal probability so that if a neighbor has multiple 
	 * contacts with the current person that neighbor is more likely to 
	 * be chosen.
	 * @return Integer identifier of the chosen neighbor
	 */
	public Integer getNextPerson(){
		if (this.curPerson == null) return null;
		if (this.people.containsKey(this.curPerson)) 
			return this.people.get(this.curPerson).getRandNewContact(this.prevPerson);
		else {
			Integer[] contacts = getContacts(this.curPerson);
			Person p = new Person(contacts);
			this.people.put(this.curPerson, p);
			return p.getRandNewContact(this.prevPerson);
		}
	}
	
//	/**
//	 * Randomly chooses a neighbor of the current person.  Each contact 
//	 * is given equal probability so that if a neighbor has multiple 
//	 * contacts with the current person that neighbor is more likely to 
//	 * be chosen.
//	 * @return Integer identifier of the chosen neighbor
//	 */
//	public Integer getNextPerson_old(){
//		int num = this.numNeighbors(this.curPerson);
//		if (num <= 1) return null;
//		int n = rand.nextInt(num) + 1;
//		Integer nextPerson = this.getNthNeighbor(this.curPerson, n);
//		while (nextPerson.equals(this.curPerson)){
//			n = rand.nextInt(num) + 1;
//			nextPerson = this.getNthNeighbor(this.curPerson, n);
//		}
//		return nextPerson;
//	}
	
	public void visit(Integer id){
		if (id == null) return;
		if (this.people.containsKey(id)) {
			this.people.get(id).visit();
		} else {
			Integer[] contacts = getContacts(id);
			Person p = new Person(contacts);
			p.visit();
			this.people.put(id, p);
		}
	}
	
	private class Person {
		private int tally;
		private Integer[] contacts;
		
		public Person (Integer[] contacts){
			this.contacts = contacts;
			this.tally = 0;
		}
		
		public void visit(){
			this.tally++;
		}
		
		public int getTally(){
			return this.tally;
		}
		
		public Integer getNthContact(int n){
			return contacts[n-1];
		}
		
		public Integer getRandNewContact(Integer prev){
			ArrayList<Integer> newContacts = new ArrayList<Integer>(this.contacts.length);
			for (int i=0; i < this.contacts.length; i++){
				if (!this.contacts[i].equals(prev)) newContacts.add(this.contacts[i]);
			}
			if (newContacts.isEmpty()) return null;
			return newContacts.get(rand.nextInt(newContacts.size()));
		}
		
		public Integer getRandNewContact(ArrayList<Integer> prev){
			ArrayList<Integer> newContacts = new ArrayList<Integer>(this.contacts.length);
			for (int i=0; i < this.contacts.length; i++){
				boolean isNew = true;
				Iterator<Integer> itr = prev.iterator();
				while (isNew && itr.hasNext()) 
					if (this.contacts[i].equals(itr.next())) isNew = false;
				if (isNew) newContacts.add(this.contacts[i]);
			}
			if (newContacts.isEmpty()) return null;
			return newContacts.get(rand.nextInt(newContacts.size()));
		}
		
	}

}
