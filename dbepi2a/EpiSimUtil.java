package dbepi2a;

import java.sql.*;
import java.util.*;
import java.io.*;

public class EpiSimUtil {
	
	public static final int population = 1615860;
	public static final int totalNumContacts = 63681132;
	
	public static final String url = "jdbc:mysql://killington.cs.dartmouth.edu:3306/fludatabase";
	public static final String urlGenDB = "jdbc:mysql://killington.cs.dartmouth.edu:3306/fludb_gen_data";
	public static final String user = "dc";
	public static final String pwd = "s4djr8f";
	
	public static final String demoTbl = "demographics";
	public static final String actTbl = "activities";
	public static final String dendroTbl = "new_dendro";
	public static final String dendroConTbl = "dendro_contact";
	public static final String conCountTbl = "num_contacts";
	public static final String conTbl = "new_contact";
	public static final String initTbl = "initInfected";
	public static final String locTbl = "locations";
	public static final String llTbl = "LL_contact";
	public static final String llCountTbl = "LL_contact_count";
	public static final String llCountPurpTbl = "LL_contact_count_purpose";
	public static final String llInterCountTbl = "LL_contact_inter_countB";
	public static final String llFamInterCountTbl = "LL_family_inter_count";
	public static final String locPeopleSocRankTbl = "location_people_social_rank";
	public static final String locPeopleSocRankDupTbl = "location_people_social_rank_dup";
	public static final String locHomeDistTbl = "location_people_home_distance";
	public static final String locHomeDistPercentileTbl = "location_people_home_distance_percentile";
	public static final String sickConTbl = "sick_contact";
	public static final String sickLoadTbl = "sick_load_intF";
	
	public static final String ppInfTbl = "pp_influence";
	public static final String ppDescrTbl = "pp_infl_exp";
	public static final String peopleFromImpHousesTbl = "people_imp_households";
	
	public static final String dendroFile = "mysql-dendro-portland-1-v1.dat";
	public static final String dendroConFile = "mysql-contact-dendro-portland-1-v1.dat";
	public static final String conCountFile = "mysql-count-contact-portland-1-v2.dat";
	
	public static final String[] activityTypeNames = {"home","work","shop","visit","social","other","pickup","school","college"};

	public static Connection dbCon;
	
	public static boolean saveContacts = false;
	public static HashMap<Integer,HashMap<Integer,Integer>> allContacts = new HashMap<Integer,HashMap<Integer,Integer>>();
	
	public EpiSimUtil() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		insertAllSickContacts();
//		getMinSickContactDuration();
//		insertInitInfectedContacts();
//		insertTimeAndLocationSickContacts();
//		printSecondaryInfectionsCausedByRandomInfections();
//		insertTime2IntoDendro();
//		loadLocFamilyCon();
//		addActTypeCountToLocTbl();
//		double[] a = {1.0,1.0,1.0};
//		double[] b = EpiSimUtil.normalize(a);
//		for (int i=0; i < b.length; i++)
//			System.out.println(b[i]);
//		double[] bins = new double[3];//{0.0,0.0,0.0};
//		double[] limits = {0.0,1.0,2.0};
//		for (double i=0.0; i < 3; i++)
//			EpiSimUtil.incBinCount(i, bins, limits);
////		EpiSimUtil.incBinCount(1.0, bins, limits);
//		bins = EpiSimUtil.normalize(bins);
//		java.text.DecimalFormat form = new java.text.DecimalFormat("#.##");
//		System.out.println(form.format(10000.666));
//		for (int i=0; i < bins.length; i++)
//			System.out.println(form.format(bins[i]));
//		printMeanConnectedDistance(Double.parseDouble(args[0]));
//		printMonLocsBins(args[0], args[1], Integer.parseInt(args[2]));
//		Map<Integer,Integer> locs = getLocWithSharedPeople(new Integer(1));
//		Iterator<Integer> itr = locs.keySet().iterator();
//		while (itr.hasNext()) {
//			Integer next = itr.next();
//			System.out.println(next+"\t"+locs.get(next));
//		}
//		printMonCount(args[0]);
//		printInfectivityDistributionByDaysSick();
//		insertTimeAndLocationContacts();
//		printLocPop();
//		test_getContactDist(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
//		printContactDistForRandPeople(100, 1239123131084900L ,10);
//		printContactDist(EpiSimUtil.getLocsHighPopLEQ(Integer.parseInt(args[0]),
//				Integer.parseInt(args[1])).keySet().iterator());
//		printCumClosureStatsAggPop(args[0]);
//		insertAllContactCountForLocTbl();
//		printCumClosureStats(args[0], args[1], 1);
//		printEnv();
		printPeopleDeg();

	}
	
	public static void printEnv() {
		Map<String,String> env = System.getenv();
		Iterator<String> itr = env.keySet().iterator();
		while (itr.hasNext()) {
			String key = itr.next();
			String value = env.get(key);
			System.out.println(key+"\t"+value);
		}
	}
	
	public static long dayToSeconds(int numDays) {
		return numDays*60*60*24;
	}
	
	public static int secondsToDays(long seconds) {
		double numDays = ((double) seconds)/(60.0*60.0*24.0);
		return (int)Math.floor(numDays);
	}
	
	public static long timeToTimeOfDay(long time) {
		return time % dayToSeconds(1);
	}
	
	public static void incMapValue(Map<Integer,Integer> map, Integer key, int amount) {
		if (!map.containsKey(key))
			map.put(key, new Integer(amount));
		else {
			amount += map.get(key).intValue();
			map.put(key, new Integer(amount));
		}
//		System.out.println(map.get(key));
	}
	
	public static void incMapValue(Map<Integer,Double> map, Integer key, double amount) {
		if (!map.containsKey(key))
			map.put(key, new Double(amount));
		else {
			amount += map.get(key).doubleValue();
			map.put(key, new Double(amount));
		}
//		System.out.println(map.get(key));
	}
	
	public static void incMapValue(Map<Integer,Long> map, Integer key, long amount) {
		if (!map.containsKey(key))
			map.put(key, new Long(amount));
		else {
			amount += map.get(key).intValue();
			map.put(key, new Long(amount));
		}
//		System.out.println(map.get(key));
	}
	
	public static double[] normalize(double[] weights) {
		double normed[] = new double[weights.length];
		double total = 0.0;
		for (int i=0; i < weights.length; i++) {
			total += weights[i];
			normed[i] = weights[i];
		}
		for (int i=0; i < normed.length; i++) {
			normed[i] /= total;
		}
		return normed;
	}
	
	public static void normalize(Map<Integer,Double> map) {
		double total = 0.0;
		Iterator<Integer> itr = map.keySet().iterator();
		while (itr.hasNext()) {
			total += map.get(itr.next()).doubleValue();
		}
		itr = map.keySet().iterator();
		while (itr.hasNext()) {
			Integer next = itr.next();
			map.put(next, new Double(map.get(next).doubleValue() / total));
		}
	}
	
	public static void incBinCount(double elem, double[] bins, double[] limits) {
		if (bins.length != limits.length) {
			System.out.println("Number of bins should be equal to number of limit points.");
			return;
		}
		if (limits.length ==0) {
			System.out.println("limits array has length zero.");
			return;
		}
		for (int i=0; i < limits.length - 1; i++) {
			if (elem >= limits[i] && elem < limits[i+1])
				bins[i]++;
		}
		if (elem >= limits[limits.length-1]) {
			bins[limits.length-1]++;
		}
	}
	
	public static String[] getBinHeaders(double[] limits) {
		String[] headers = new String[limits.length];
		if (limits.length > 0) {
			java.text.DecimalFormat form = new java.text.DecimalFormat("#.###");
			for (int i=0; i < limits.length-1; i++) {
				headers[i] = "[";
				headers[i] += form.format(limits[i]);
				headers[i] += ",";
				headers[i] += form.format(limits[i+1]);
				headers[i] += ")";
			}
			int i = limits.length-1;
			headers[i] = "[" + limits[i] + ",Inf)";
		}
		return headers;
	}
	
	public static void initArray(double elem, double[] array) {
		for (int i=0; i < array.length; i++)
			array[i] = elem;
	}
	
	public static Connection dbConnect(){
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			return DriverManager.getConnection(url, user, pwd);
		} catch (Exception e){
			System.out.println(e);
			return dbConnect(10);
		}
	}
	
	public static Connection dbConnect(int numTries){
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			return DriverManager.getConnection(url, user, pwd);
		} catch (Exception e){
			System.out.println("Will try "+numTries+" more times to connect");
			System.out.println(e);
			if (numTries > 0)
				return dbConnect(numTries-1);
			return null;
		}
	}
	
	public static Connection dbStaticConnect() {
		if (EpiSimUtil.dbCon == null) {
			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				EpiSimUtil.dbCon = DriverManager.getConnection(url, user, pwd);
			} catch (Exception e){
				System.out.println(e);
			}
		}
		return EpiSimUtil.dbCon;
	}
	
	public static int getNumPeopleSick(long time) {
		int num = 100;
		try {
			Connection con = dbConnect();
			ResultSet getNumSickQ = con.createStatement().executeQuery(
					"SELECT COUNT(*) FROM "+dendroTbl+" WHERE time < "+time);
			if (getNumSickQ.first())
				num += getNumSickQ.getInt("COUNT(*)");
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return num;
	}
	
	public static HashSet<Integer> getSickPeople(long time) {
		HashSet<Integer> people = new HashSet<Integer>();
		try {
			Connection con = dbConnect();
			Statement stmt = con.createStatement();
			ResultSet getInitQ = stmt.executeQuery(
					"SELECT id FROM "+initTbl);
			while (getInitQ.next()) {
				people.add(new Integer(getInitQ.getInt("id")));
			}
			getInitQ.close();
			ResultSet getInfQ = stmt.executeQuery(
					"SELECT victimID FROM "+dendroTbl+" WHERE time < "+time);
			while (getInfQ.next()) {
				people.add(new Integer(getInfQ.getInt("victimID")));
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return people;
	}
	
	public static HashSet<Integer> getMostSocPeople(int numPeople) {
		HashSet<Integer> people = new HashSet<Integer>(numPeople);
		try {
			Connection con = dbConnect();
			ResultSet getPeopleQ = con.createStatement().executeQuery(
					"SELECT personID FROM "+EpiSimUtil.conCountTbl+" ORDER BY tally DESC LIMIT "+numPeople);
			while (getPeopleQ.next() && people.size() <= numPeople) {
				Integer person = getPeopleQ.getInt("personID");
				people.add(person);
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return people;
	}
	
	public static double getX(int location) {
		double x = -1.0;
		try {
			Connection con = dbConnect();
			ResultSet getXQ = con.createStatement().executeQuery(
					"SELECT x FROM "+EpiSimUtil.locTbl+" WHERE locationID="+location);
			if (getXQ.first()) x = getXQ.getDouble("x");
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return x;
	}
	
	public static double getY(int location) {
		double y = -1.0;
		try {
			Connection con = dbConnect();
			ResultSet getXQ = con.createStatement().executeQuery(
					"SELECT y FROM "+EpiSimUtil.locTbl+" WHERE locationID="+location);
			if (getXQ.first()) y = getXQ.getDouble("y");
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return y;
	}
	
	public static int getAggPop(int location) {
		int aggPop = -1;
		try {
			Connection con = dbConnect();
			ResultSet aggPopQ = con.createStatement().executeQuery(
					"SELECT aggregatePop FROM "+locTbl+" WHERE locationID = "+location);
			if (aggPopQ.first()) aggPop = aggPopQ.getInt("aggregatePop");
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return aggPop;
	}
	
	public static int getNumDistPeopleAtLocations(HashSet<Integer> locations) {
		return getPeopleAtLocations(locations).size();
	}
	
	public static HashSet<Integer> getPeopleAtLocations(HashSet<Integer> locations) {
		//int expectedNumberOfPeoplePerLocation = 1000;
		HashSet<Integer> people = new HashSet<Integer>();
		//HashSet<Integer> people = new HashSet<Integer>(expectedNumberOfPeoplePerLocation*locations.size());
		try {
			Connection con = dbConnect();
			PreparedStatement getPeople = con.prepareStatement(
					"SELECT personID FROM "+actTbl+" WHERE locationID = ?");
			Iterator<Integer> locItr = locations.iterator();
			while (locItr.hasNext()) {
				Integer location = locItr.next();
				getPeople.setInt(1, location.intValue());
				ResultSet getPeopleQ = getPeople.executeQuery();
				while (getPeopleQ.next()) {
					Integer person = new Integer(getPeopleQ.getInt("personID"));
					people.add(person);
				}
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return people;
	}
	
	public static HashSet<Integer> getPeopleAtLocation(Integer location) {
		HashSet<Integer> people = new HashSet<Integer>();
		try {
			Connection con = dbConnect();
			ResultSet getPeopleQ = con.createStatement().executeQuery(
					"SELECT personID FROM "+actTbl+" WHERE locationID = "+location);
			while (getPeopleQ.next()) {
				Integer person = new Integer(getPeopleQ.getInt("personID"));
				people.add(person);
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return people;
	}
	
	//
	
	public static HashSet<Integer> getLocationsVisited(int person) {
		HashSet<Integer> locations = null;
		try {
			Connection con = dbConnect();
			ResultSet getLocsQ = con.createStatement().executeQuery(
					"SELECT locationID FROM "+EpiSimUtil.actTbl+" WHERE personID="+person);
			getLocsQ.last();
			int numLocs = getLocsQ.getRow();
			getLocsQ.beforeFirst();
			locations = new HashSet<Integer>(numLocs);
			while (getLocsQ.next()) {
				int location = getLocsQ.getInt("locationID");
				locations.add(new Integer(location));
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return locations;
	}
	
	public static HashMap<Integer,HashSet<Integer>> getLocationsVisitedByAllInf() {
		HashMap<Integer,HashSet<Integer>> people = new HashMap<Integer,HashSet<Integer>>(565685);
		try {
			Connection con = dbConnect();
			ResultSet getPeopleQ = con.createStatement().executeQuery(
					" SELECT DISTINCT locationID,personID FROM "+actTbl+
					" WHERE personID IN (SELECT victimID FROM "+dendroTbl+")");
			while (getPeopleQ.next()) {
				Integer location = getPeopleQ.getInt("locationID");
				Integer person = getPeopleQ.getInt("personID");
				if (!people.containsKey(person))
					people.put(person, new HashSet<Integer>(5));
				people.get(person).add(location);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return people;
	}
	
	public static HashSet<Integer> getRelatives(Integer person) {
		HashSet<Integer> family = new HashSet<Integer>();
		try {
			Connection con = dbConnect();
			Statement stmt = con.createStatement();
			ResultSet houseHoldIDQ = stmt.executeQuery(
					"SELECT householdID FROM "+demoTbl+" WHERE personID="+person);
			if (houseHoldIDQ.first()) {
				int houseHoldID = houseHoldIDQ.getInt("householdID");
				ResultSet familyQ = stmt.executeQuery(
						"SELECT personID FROM "+demoTbl+" WHERE householdID = "+houseHoldID);
				while (familyQ.next()) {
					Integer p = new Integer(familyQ.getInt("personID"));
					family.add(p);
				}
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return family;
	}
	
	public static HashMap<Integer,Integer> getContactsQ(Integer person) {
		HashMap<Integer,Integer> contacts = new HashMap<Integer,Integer>();
		try {
			Connection con = dbConnect();
			ResultSet getConQ = con.createStatement().executeQuery(
					"SELECT person2ID FROM "+conTbl+" WHERE person1ID = "+person);
			while (getConQ.next()) {
				incMapValue(contacts, new Integer(getConQ.getInt("person2ID")), 1);
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return contacts;
	}
	
	public static HashMap<Integer,Integer> getContacts(Integer person) {
		if (EpiSimUtil.allContacts.containsKey(person))
			return EpiSimUtil.allContacts.get(person);
		HashMap<Integer,Integer> contacts = getContactsQ(person);
		if (EpiSimUtil.saveContacts)
			allContacts.put(person, contacts);
		return contacts;
	}
	
	public static Set<Integer> getNeighbors(Integer person) {
		return getContacts(person).keySet();
	}
	
	public static Set<Integer> getNeighborhood(Integer person, int distance) {
		HashSet<Integer> people = new HashSet<Integer>();
		people.add(person);
		if (distance > 0) {
			Iterator<Integer> itr = getNeighbors(person).iterator();
			while (itr.hasNext()) {
				people.addAll(getNeighborhood(itr.next(),distance-1));
			}
		}
		return people;
	}
	
	public static int getContactDist(Integer pS, Integer pD, int maxDistLim) {
		LinkedList<Integer> q = new LinkedList<Integer>();
		q.add(pS);
		HashSet<Integer> visited = new HashSet<Integer>();
		visited.add(pS);
		HashSet<Integer> done = new HashSet<Integer>();
		HashMap<Integer,Integer> distances = new HashMap<Integer,Integer>();
		distances.put(pS, new Integer(0));
		int maxDist = 0;
		while (q.size() > 0 && maxDist < maxDistLim && !distances.containsKey(pD)) {
			Integer u = q.poll();
			Iterator<Integer> neighbors = getNeighbors(u).iterator();
			while (neighbors.hasNext()) {
				Integer v = neighbors.next();
				if (!visited.contains(v) && !done.contains(v)) {
					int d = distances.get(u).intValue() + 1;
					distances.put(v, new Integer(d));
					visited.add(v);
					q.add(v);
					if (d > maxDist)
						maxDist = d;
				}
			}
			done.add(u);
		}
		if (distances.containsKey(pD))
			return distances.get(pD).intValue();
		else
			return -1;
	}
	
	public static void test_getContactDist(Integer source, Integer dest) {
		System.out.print(source+"\t"+dest+"\t");
		System.out.println(getContactDist(source, dest, 10));
	}
	
	public static void printContactDistForRandPeople(int numPairs, long seed, int maxDist) {
		System.out.println("Number of pairs:\t"+numPairs);
		System.out.println("Random seed:\t"+seed);
		System.out.println("Max distance limit:\t"+maxDist);
		Random rand = new Random(seed);
		for (;numPairs > 0; numPairs--) {
			Integer source = new Integer(rand.nextInt(EpiSimUtil.population)+1);
			Integer dest = new Integer(rand.nextInt(EpiSimUtil.population)+1);
			System.out.print(source+"\t"+dest+"\t");
			int distance = getContactDist(source, dest, maxDist);
			if (distance == -1)
				System.out.println("NaN");
			else
				System.out.println(distance);
		}
	}
	
	public static int getDomActType(Integer location) {
		int domTypeCount = 0;
		int domType = -1;
		String stmt = "SELECT "+activityTypeNames[0];
		for (int i=1; i < EpiSimUtil.activityTypeNames.length; i++) {
			stmt += ","+activityTypeNames[i];
		}
		stmt += " FROM "+locTbl+" WHERE locationID="+location;
//		System.out.println(stmt);
		try {
			Connection con = dbConnect();
			ResultSet getActQ = con.createStatement().executeQuery(stmt);
			if (getActQ.first()) {
				for (int type=0; type < EpiSimUtil.activityTypeNames.length; type++) {
					int typeCount = getActQ.getInt(activityTypeNames[type]);
					if (typeCount == domTypeCount) {
						domType = -1;
						break;
					}
					if (typeCount > domTypeCount) {
						domTypeCount = typeCount;
						domType = type;
					}
				}
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return domType;
	}
	
	public static TreeMap<Integer,Integer> getLocThatPointTo(Integer location) {
		TreeMap<Integer,Integer> locs = new TreeMap<Integer,Integer>();
		try {
			Connection con = dbConnect();
			ResultSet getLocQ = con.createStatement().executeQuery(
					"SELECT location1ID,tally FROM "+llInterCountTbl+" WHERE location2ID="+location);
			while (getLocQ.next()) {
				Integer loc = new Integer(getLocQ.getInt("location1ID"));
				Integer tally = new Integer(getLocQ.getInt("tally"));
				locs.put(loc, tally);
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return locs;
	}
	
	public static TreeMap<Integer,Integer> getLocThatPointToThroughFamily(Integer location) {
		TreeMap<Integer,Integer> locs = new TreeMap<Integer,Integer>();
		try {
			Connection con = dbConnect();
			ResultSet getLocQ = con.createStatement().executeQuery(
					"SELECT location1ID,tally FROM "+EpiSimUtil.llFamInterCountTbl+" WHERE location2ID="+location);
			while (getLocQ.next()) {
				Integer loc = new Integer(getLocQ.getInt("location1ID"));
				Integer tally = new Integer(getLocQ.getInt("tally"));
				locs.put(loc, tally);
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return locs;
	}
	
	public static TreeMap<Integer,Integer> getLocWithSharedPeople(Integer location) {
		TreeMap<Integer,Integer> locs = new TreeMap<Integer,Integer>();
		try {
			Connection con = dbConnect();
			ResultSet getLocQ = con.createStatement().executeQuery(
					"SELECT DISTINCT a1.personID,a1.locationID,a2.personID,a2.locationID FROM "+
					"activities AS a1 INNER JOIN activities AS a2 ON a1.personID = a2.personID "+
					"WHERE a1.locationID = "+location+" AND a2.locationID != "+location);
			while (getLocQ.next()) {
				Integer loc = new Integer(getLocQ.getInt("a2.locationID"));
				incMapValue(locs, loc, 1);
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return locs;
	}
	
	public static HashMap<Integer,Integer> getLocsHighPop(int numLocations) {
		HashMap<Integer,Integer> locations = new HashMap<Integer,Integer>(numLocations);
		try {
			Connection con = dbConnect();
			ResultSet getLocsQ = con.createStatement().executeQuery(
					"SELECT locationID,aggregatePop FROM "+locTbl+" ORDER BY aggregatePop DESC LIMIT "+numLocations);
			while (getLocsQ.next()) {
				locations.put(new Integer(getLocsQ.getInt("locationID")), new Integer(0));
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return locations;
	}
	
	public static HashMap<Integer,Integer> getLocsHighPopLEQ(int numLocations, int maxPop) {
		HashMap<Integer,Integer> locations = new HashMap<Integer,Integer>(numLocations);
		try {
			Connection con = dbConnect();
			ResultSet getLocsQ = con.createStatement().executeQuery(
					"SELECT locationID,aggregatePop FROM "+locTbl+" WHERE aggregatePop <= "+maxPop+
					" ORDER BY aggregatePop DESC LIMIT "+numLocations);
			while (getLocsQ.next()) {
				locations.put(new Integer(getLocsQ.getInt("locationID")), new Integer(0));
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return locations;
	}
	
	
	public static HashMap<Integer,Integer> getLocsHighOutDegRestHomeDist(int numLocations, double minHomeDist, int homeDistPercentile) {
		HashMap<Integer,Integer> locations = new HashMap<Integer,Integer>(numLocations);
		try {
			Connection con = dbConnect();
			ResultSet locsQ = con.createStatement().executeQuery(
					"SELECT "+locHomeDistPercentileTbl+".locationID FROM "+locHomeDistPercentileTbl+" INNER JOIN "
					+llCountTbl+" ON "+locHomeDistPercentileTbl+".locationID = "+llCountTbl+".locationID"
					+" WHERE "+locHomeDistPercentileTbl+".percentile = "+homeDistPercentile+" AND "
					+locHomeDistPercentileTbl+".distance >= "+minHomeDist+" ORDER BY "+llCountTbl
					+".outbound DESC LIMIT "+numLocations);
			while (locsQ.next()) {
				locations.put(new Integer(locsQ.getInt(locHomeDistPercentileTbl+".locationID")), new Integer(0));
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return locations;
	}
	
	public static HashMap<Integer,Integer> getLocsHighOutDeg(int numLocations) {
		HashMap<Integer,Integer> locations = new HashMap<Integer,Integer>();
		try {
			Connection con = dbConnect();
			ResultSet locsQ = con.createStatement().executeQuery(
					"SELECT locationID FROM "+llCountTbl+
					" ORDER BY outbound DESC LIMIT "+numLocations);
			while (locsQ.next()) {
				locations.put(new Integer(locsQ.getInt("locationID")), new Integer(0));
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return locations;
	}
	/**
	 * Missed contacts of initially infected
	 *
	 */
	public static void insertAllSickContacts() {
		double minContactDuration = 0.016666699200868607;
		try {
			HashSet<Integer> sick = new HashSet<Integer>(565685);
			Connection con = dbConnect();
			ResultSet getSickQ = con.createStatement().executeQuery(
					"SELECT victimID FROM "+dendroTbl);
			while (getSickQ.next()) {
				Integer p = new Integer(getSickQ.getInt("victimID"));
				sick.add(p);
			}
			getSickQ.close();
			PreparedStatement insert = con.prepareStatement(
					"INSERT INTO "+sickConTbl+" (person1ID,person2ID,duration) VALUES (?,?,?)");
			PreparedStatement getContacts = con.prepareStatement(
					"SELECT person2ID,duration FROM "+conTbl+" WHERE person1ID = ?");
			Iterator<Integer> sickItr = sick.iterator();
			while (sickItr.hasNext()) {
				Integer person = sickItr.next();
				getContacts.setInt(1, person.intValue());
				ResultSet getContactsQ = getContacts.executeQuery();
				while (getContactsQ.next()) {
					insert.setInt(1, person);
					Integer contact = getContactsQ.getInt("person2ID");
					double duration = getContactsQ.getDouble("duration");
					if (sick.contains(contact) && duration >= minContactDuration) {
						insert.setInt(2, contact);
						insert.setDouble(3, duration);
						insert.executeUpdate();
					}
				}
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void insertInitInfectedContacts() {
		int numInserts = 0;
		double minContactDuration = 0.0;
		try {
			HashSet<Integer> sick = new HashSet<Integer>(565685);
			Connection con = dbConnect();
			ResultSet getSickQ = con.createStatement().executeQuery(
					"SELECT victimID FROM "+dendroTbl);
			while (getSickQ.next()) {
				Integer p = new Integer(getSickQ.getInt("victimID"));
				sick.add(p);
			}
			getSickQ.close();
			HashSet<Integer> initInfected = new HashSet<Integer>(100);
			getSickQ = con.createStatement().executeQuery(
					"SELECT id FROM "+initTbl);
			while (getSickQ.next()) {
				Integer p = new Integer(getSickQ.getInt("id"));
				initInfected.add(p);
			}
			getSickQ.close();
			PreparedStatement insert = con.prepareStatement(
					"INSERT INTO "+sickConTbl+" (person1ID,person2ID,duration) VALUES (?,?,?)");
			PreparedStatement getContacts = con.prepareStatement(
					"SELECT person2ID,duration FROM "+conTbl+" WHERE person1ID = ?");
			Iterator<Integer> initInfectedItr = initInfected.iterator();
			while (initInfectedItr.hasNext()) {
				Integer person = initInfectedItr.next();
				getContacts.setInt(1, person.intValue());
				ResultSet getContactsQ = getContacts.executeQuery();
				while (getContactsQ.next()) {
					insert.setInt(1, person);
					Integer contact = getContactsQ.getInt("person2ID");
					double duration = getContactsQ.getDouble("duration");
					if (sick.contains(contact) && duration >= minContactDuration) {
						insert.setInt(2, contact);
						insert.setDouble(3, duration);
						numInserts += insert.executeUpdate();
					}
				}
			}
			con.close();
			System.out.println("Number of inserts: "+numInserts);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void getMinSickContactDuration() {
		double minDuration = Double.MAX_VALUE;
		try {
			Connection con = dbConnect();
			PreparedStatement getDuration = con.prepareStatement(
					"SELECT duration FROM "+conTbl+" WHERE person1ID = ? AND person2ID = ?");
			ResultSet getTransQ = con.createStatement().executeQuery(
					"SELECT dendro_contact.victimID,dendro_contact.infectedID FROM new_dendro INNER JOIN dendro_contact ON new_dendro.victimID = dendro_contact.victimID WHERE new_dendro.numInfected = 1");
			while (getTransQ.next()) {
				int person1 = getTransQ.getInt("dendro_contact.victimID");
				int person2 = getTransQ.getInt("dendro_contact.infectedID");
				getDuration.setInt(1, person1);
				getDuration.setInt(2, person2);
				ResultSet getDurationQ = getDuration.executeQuery();
				if (getDurationQ.first()) {
					double duration = getDurationQ.getDouble("duration");
					if (duration < minDuration) {
						minDuration = duration;
						System.out.println("Current minimum duration is "+minDuration);
					}
				}
			}
			System.out.println("Absolute minimum duration is "+minDuration);
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static long getInfectionTime(Integer person) {
		long time = -1;
		try {
			Connection con = dbConnect();
			ResultSet getInfTimeQ = con.createStatement().executeQuery(
					"SELECT time FROM "+dendroTbl+" WHERE victimID="+person);
			if (getInfTimeQ.first()) {
				time = getInfTimeQ.getLong("time");
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return time;
	}
	
	public static void insertTimeAndLocationSickContacts() {
		int numSickContacts = 17652991;
		int batchSize = 1000;
		System.out.println("Starting: insert location and time into sick contact table.");
		try {
			Connection con = dbConnect();
			PreparedStatement update = con.prepareStatement(
					"UPDATE "+sickConTbl+" SET locationID=?,time=? WHERE person1ID=? AND person2ID=?");
			System.out.println("start act");
			PreparedStatement getAct = con.prepareStatement(
					"SELECT sick_contact.person1ID,sick_contact.person2ID,a1.locationID,a1.startTime,a2.startTime " +
					"FROM sick_contact INNER JOIN " +
					"(activities AS a1 INNER JOIN activities AS a2 ON a1.locationID = a2.locationID AND a1.personID != a2.personID) " +
					"ON sick_contact.person1ID = a1.personID AND sick_contact.person2ID = a2.personID WHERE sick_contact.person1ID=? AND sick_contact.person2ID=?");
			System.out.println("end act");
			PreparedStatement getCon = con.prepareStatement(
					"SELECT person1ID,person2ID,time,locationID FROM "+sickConTbl+" LIMIT ?,"+batchSize);
			System.out.println("end con");
			for (int offset=0; offset <= numSickContacts; offset += batchSize) {
				getCon.setInt(1, offset);
				ResultSet getConQ = getCon.executeQuery();
				while (getConQ.next()) {
					if (getConQ.getInt("locationID") == 0 && getConQ.getLong("time") == 0) {
						int person1 = getConQ.getInt("person1ID");
						int person2 = getConQ.getInt("person2ID");
//						System.out.println(person1+" "+person2);
						update.setInt(3, person1);
						update.setInt(4, person2);
						getAct.setInt(1, person1);
						getAct.setInt(2, person2);
						ResultSet getActQ = getAct.executeQuery();
						long time = Long.MAX_VALUE;
						int location = -1;
						while (getActQ.next()) {
							long time1 = getActQ.getLong("a1.startTime");
							long time2 = getActQ.getLong("a2.startTime");
							if (time1 < time) {
								time = time1;
								location = getActQ.getInt("a1.locationID");
							}
							if (time2 < time) {
								time = time2;
								location = getActQ.getInt("a1.locationID");
							}
						}
//						System.out.println("person1: "+person1+" person2: "+person2+" location: "+location+" time: "+time);
						update.setInt(1, location);
						update.setLong(2, time);
						update.executeUpdate();
					}
				}
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void insertTimeAndLocationContacts() {
		int numContacts = totalNumContacts;
		int batchSize = 1000;
		System.out.println("Starting: insert location and time into contact table.");
		try {
			Connection con = dbConnect();
			PreparedStatement update = con.prepareStatement(
					"UPDATE "+conTbl+" SET locationID=?,time=? WHERE person1ID=? AND person2ID=?");
			System.out.println("start act");
			PreparedStatement getAct = con.prepareStatement(
					"SELECT new_contact.person1ID,new_contact.person2ID,a1.locationID,a1.startTime,a2.startTime " +
					"FROM new_contact INNER JOIN " +
					"(activities AS a1 INNER JOIN activities AS a2 ON a1.locationID = a2.locationID AND a1.personID != a2.personID) " +
					"ON new_contact.person1ID = a1.personID AND new_contact.person2ID = a2.personID WHERE new_contact.person1ID=? AND new_contact.person2ID=?");
			System.out.println("end act");
			PreparedStatement getCon = con.prepareStatement(
					"SELECT person1ID,person2ID,time,locationID FROM "+conTbl+" LIMIT ?,"+batchSize);
			System.out.println("end con");
			for (int offset=0; offset <= numContacts; offset += batchSize) {
				getCon.setInt(1, offset);
				ResultSet getConQ = getCon.executeQuery();
				while (getConQ.next()) {
					if (getConQ.getInt("locationID") == 0 && getConQ.getLong("time") == 0) {
						int person1 = getConQ.getInt("person1ID");
						int person2 = getConQ.getInt("person2ID");
//						System.out.println(person1+" "+person2);
						update.setInt(3, person1);
						update.setInt(4, person2);
						getAct.setInt(1, person1);
						getAct.setInt(2, person2);
						ResultSet getActQ = getAct.executeQuery();
						long time = Long.MAX_VALUE;
						int location = -1;
						while (getActQ.next()) {
							long time1 = getActQ.getLong("a1.startTime");
							long time2 = getActQ.getLong("a2.startTime");
							if (time1 < time) {
								time = time1;
								location = getActQ.getInt("a1.locationID");
							}
							if (time2 < time) {
								time = time2;
								location = getActQ.getInt("a1.locationID");
							}
						}
//						System.out.println("person1: "+person1+" person2: "+person2+" location: "+location+" time: "+time);
						update.setInt(1, location);
						update.setLong(2, time);
						update.executeUpdate();
					}
				}
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static int numInfectedByRandomInfectors(int day) {
		int numInfected = -1;
		try {
			Connection con = dbConnect();
			ResultSet numQ = con.createStatement().executeQuery(
					"SELECT COUNT(*) FROM new_dendro INNER JOIN dendro_contact ON"
					+" new_dendro.victimID = dendro_contact.victimID WHERE numInfected = 1 AND time < "
					+day+"*60*60*24 AND infectedID NOT IN "
					+"(SELECT victimID FROM new_dendro UNION SELECT id FROM initInfected)");
			if (numQ.first())
				numInfected = numQ.getInt("COUNT(*)");
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return numInfected;
	}
	
	public static void printSecondaryInfectionsCausedByRandomInfections() {
		System.out.println("Day\tsecondary count\total count\tfraction of total");
		for (int day=1; day < 101; day++) {
			double totalNumInf = EpiSimUtil.getNumPeopleSick(dayToSeconds(day));
			double numSecond = EpiSimUtil.numInfectedByRandomInfectors(day);
			System.out.println(day+"\t"+numSecond+"\t"+totalNumInf+"\t"+numSecond/totalNumInf);
		}
	}
	
	public static void insertTime2IntoDendro() {
		try {
			Connection con = dbConnect();
			PreparedStatement update = con.prepareStatement(
					"UPDATE "+dendroTbl+" SET time2=? WHERE victimID=?");
			PreparedStatement getContacts = con.prepareStatement(
					"SELECT time FROM "+sickConTbl+" WHERE person2ID=? AND person1ID IN (SELECT infectedID FROM "
					+dendroConTbl+" WHERE victimID=?)");
			ResultSet getInfQ = con.createStatement().executeQuery(
					"SELECT victimID,time FROM "+dendroTbl);
			int count = 0;
			while (getInfQ.next()) {
				int victim = getInfQ.getInt("victimID");
				long infTime = getInfQ.getLong("time");
				int infDay = secondsToDays(infTime);
				long time2 = dayToSeconds(infDay);
				getContacts.setInt(1, victim);
				getContacts.setInt(2, victim);
				ResultSet getConQ = getContacts.executeQuery();
				while (getConQ.next()) {
					long conTime = getConQ.getLong("time")+dayToSeconds(infDay);
					if (conTime <= infTime && conTime > time2)
						time2 = conTime;
				}
				if (getConQ.first()) {
					update.setLong(1, time2);
					update.setInt(2, victim);
					update.executeUpdate();
				} else {
					count++;
					update.setLong(1, infTime);
					update.setInt(2, victim);
					update.executeUpdate();
				}
			}
			System.out.println("Number of people with no infection contacts returned: "+count);
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void loadLocFamilyCon() {
		int numLocs = 10000;
		HashMap<Integer,TreeMap<Integer,Integer>> locs = new HashMap<Integer,TreeMap<Integer,Integer>>(numLocs);
		try {
			Connection con = dbConnect();
			PreparedStatement insert = con.prepareStatement(
					"INSERT INTO "+EpiSimUtil.llFamInterCountTbl+" (location1ID,location2ID,tally) VALUES (?,?,?)");
			PreparedStatement getHouseHolds = con.prepareStatement(
					"SELECT DISTINCT householdID FROM "+actTbl+" WHERE locationID = ?");
			PreparedStatement getLoc = con.prepareStatement(
					"SELECT DISTINCT locationID FROM "+actTbl+" WHERE householdID = ?");
			ResultSet getTopLocsQ = con.createStatement().executeQuery(
					"SELECT locationID FROM "+locTbl+" ORDER BY aggregatePop DESC LIMIT "+numLocs);
			while (getTopLocsQ.next()) {
				Integer location = new Integer(getTopLocsQ.getInt("locationID"));
				locs.put(location, new TreeMap<Integer,Integer>());
			}
			Iterator<Integer> locItr = locs.keySet().iterator();
			int numLocFin = 0;
			while (locItr.hasNext()) {
//				TreeSet<Integer> houseHolds = new TreeSet<Integer>();
				Integer location = locItr.next();
				getHouseHolds.setInt(1, location.intValue());
				ResultSet getHouseHoldsQ = getHouseHolds.executeQuery();
				while (getHouseHoldsQ.next()) {
					int houseHold = getHouseHoldsQ.getInt("householdID");
					getLoc.setInt(1, houseHold);
					ResultSet getLocQ = getLoc.executeQuery();
					while (getLocQ.next()) {
						Integer neighbor = new Integer(getLocQ.getInt("locationID"));
						if (locs.containsKey(neighbor))
							if (locs.get(location).containsKey(neighbor)) {
								int count = locs.get(location).get(neighbor).intValue();
								count++;
								locs.get(location).put(neighbor, new Integer(count));
							} else {
								locs.get(location).put(neighbor, new Integer(1));
							}
					}
				}
				Iterator<Integer> neighItr = locs.get(location).keySet().iterator();
				while (neighItr.hasNext()) {
					Integer neighbor = neighItr.next();
					insert.setInt(1, location);
					insert.setInt(2, neighbor);
					insert.setInt(3, locs.get(location).get(neighbor).intValue());
					insert.executeUpdate();
				}
				numLocFin++;
				System.out.println(numLocFin+"\tout of\t"+locs.size());
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void addActTypeCountToLocTbl() {
		try {
			Connection con = dbConnect();
			PreparedStatement getCount = con.prepareStatement(
					"SELECT COUNT(*) FROM "+actTbl+" WHERE locationID=? AND purpose=?");
//			PreparedStatement update = con.prepareStatement(
//					"UPDATE "+locTbl+" SET ? = ? WHERE locationID=?");
			ResultSet getLocsQ = con.createStatement().executeQuery(
					"SELECT DISTINCT locationID FROM "+actTbl);
			getLocsQ.last();
			int numLocs = getLocsQ.getRow();
			getLocsQ.beforeFirst();
			int cur = 0;
			while(getLocsQ.next()) {
				int location = getLocsQ.getInt("locationID");
				getCount.setInt(1, location);
//				update.setInt(3, location);
				for (int type=0; type < EpiSimUtil.activityTypeNames.length; type++) {
					getCount.setInt(2, type);
//					update.SET.setString(1, activityTypeNames[type]);
					ResultSet getCountQ = getCount.executeQuery();
					if (getCountQ.first()) {
						con.createStatement().executeUpdate(
								"UPDATE "+locTbl+" SET "+activityTypeNames[type]+
								"="+getCountQ.getInt("COUNT(*)")+" WHERE locationID="+location);
//						update.setInt(2, getCountQ.getInt("COUNT(*)"));
//						update.executeUpdate();
					}
				}
				cur++;
				if (cur % 1000 == 0)
					System.out.println(cur+"\tout of\t"+numLocs);
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void printMeanConnectedDistance(double maxDist) {
		double total = 0.0;
		double count = 0.0;
//		double maxDist = 10000;
		double totalNumCon = 0.0;
		double countNumCon = 0.0;
		HashMap<Integer,Double> cLocsX = new HashMap<Integer,Double>(10000);
		HashMap<Integer,Double> cLocsY = new HashMap<Integer,Double>(10000);
		HashMap<Integer,Double> tLocsX = new HashMap<Integer,Double>(40000);
		HashMap<Integer,Double> tLocsY = new HashMap<Integer,Double>(40000);
		try {
			Connection con = dbConnect();
			ResultSet getCenterLocsQ = con.createStatement().executeQuery(
					"SELECT locationID,x,y FROM "+locTbl+" ORDER BY aggregatePop DESC LIMIT 10000");
			while (getCenterLocsQ.next()) {
				Integer location = new Integer(getCenterLocsQ.getInt("locationID"));
				Double x = new Double(getCenterLocsQ.getDouble("x"));
				Double y = new Double(getCenterLocsQ.getDouble("y"));
				cLocsX.put(location, x);
				cLocsY.put(location, y);
			}
			getCenterLocsQ.close();
			ResultSet getTargetLocsQ = con.createStatement().executeQuery(
					"SELECT locationID,x,y FROM "+locTbl+" ORDER BY aggregatePop DESC LIMIT 10000,40000");
			while (getTargetLocsQ.next()) {
				Integer location = new Integer(getTargetLocsQ.getInt("locationID"));
				Double x = new Double(getTargetLocsQ.getDouble("x"));
				Double y = new Double(getTargetLocsQ.getDouble("y"));
				tLocsX.put(location, x);
				tLocsY.put(location, y);
			}
			getTargetLocsQ.close();
			Iterator<Integer> cLocsItr = cLocsX.keySet().iterator();
			while (cLocsItr.hasNext()) {
				Integer cloc = cLocsItr.next();
				Iterator<Integer> tLocsItr = EpiSimUtil.getLocThatPointTo(cloc).keySet().iterator();
				while (tLocsItr.hasNext()) {
					Integer tloc = tLocsItr.next();
					if (tLocsX.containsKey(tloc)) {
						double dist = Math.sqrt(
								Math.pow(cLocsX.get(cloc).doubleValue() - tLocsX.get(tloc).doubleValue(), 2.0) + 
								Math.pow(cLocsY.get(cloc).doubleValue() - tLocsY.get(tloc).doubleValue(), 2.0));
						if (dist <= maxDist) {
							totalNumCon++;
							total += dist;
							count++;
						}
					}
				}
				countNumCon++;
				if (count % 1000 == 0)
					System.out.println("Count: "+count+" total: "+total+" mean: "+total/count+"\t\tMean num con: "+totalNumCon/countNumCon);
			}
			System.out.println("Count: "+count+" total: "+total+" mean: "+total/count+"\t\tMean num con: "+totalNumCon/countNumCon);
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static HashMap<Integer,Integer> getMonLocs(String fileName) {
		HashMap<Integer,Integer> locs = new HashMap<Integer,Integer>();
		try {
			BufferedReader rd = new BufferedReader(new FileReader(fileName));
			String line;
			for (int i=0; (line = rd.readLine()) != null; i++) {
				StringTokenizer tok = new StringTokenizer(line);
				while (tok.hasMoreTokens()) {
					EpiSimUtil.incMapValue(locs, Integer.valueOf(tok.nextToken()), 1);
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return locs;
	}
	
	public static Integer[] getKeysWithMaxMinVal(HashMap<Integer,Integer> map) {
		Integer maxKey = new Integer(-1);
		Integer minKey = new Integer(-1);
		Iterator<Integer> itr = map.keySet().iterator();
		while (itr.hasNext()) {
			Integer next = itr.next();
			if (maxKey.intValue() == -1)
				maxKey = next;
			else {
				if (map.get(maxKey).intValue() < map.get(next).intValue())
					maxKey = next;
			}
			if (minKey.intValue() == -1)
				minKey = next;
			else {
				if (map.get(minKey).intValue() > map.get(next).intValue())
					minKey = next;
			}
		}
		Integer[] r = new Integer[2];
		r[0] = minKey;
		r[1] = maxKey;
		return r;
	}
	
	public static void printMonLocsBins(String fileName1, String fileName2, int numBins) {
		int[][] bins = new int[numBins][numBins];
		for (int i=0; i < numBins; i++) {
			for (int j=0; j < numBins; j++) {
				bins[i][j] = 0;
			}
		}
		HashMap<Integer,Integer> map1 = getMonLocs(fileName1);
		Integer[] keys = getKeysWithMaxMinVal(map1);
		int min1 = map1.get(keys[0]).intValue();
		int max1 = map1.get(keys[1]).intValue();
		HashMap<Integer,Integer> map2 = getMonLocs(fileName2);
		keys = getKeysWithMaxMinVal(map2);
		int min2 = map2.get(keys[0]).intValue();
		int max2 = map2.get(keys[1]).intValue();
		int range1 = max1 - min1;
		int range2 = max2 - min2;
		double binLength1 = (double)range1/(double)numBins;
		double binLength2 = (double)range2/(double)numBins;
		Iterator<Integer> itr = map1.keySet().iterator();
		while (itr.hasNext()) {
			Integer key = itr.next();
			int v1 = map1.get(key).intValue();
			if (map2.containsKey(key)) {
				int v2 = map2.get(key).intValue();
				int i1 = (int)Math.floor((double)(v1-min1)/binLength1);
				int i2 = (int)Math.floor((double)(v2-min2)/binLength2);
				try {
					bins[i1][i2] += 1;
				} catch (Exception e) {
					System.out.println(i1+"\t"+i2);
					System.out.println(e);
				}
			}
		}
		for (int binMin = min1; binMin < max1; binMin += binLength1)
			System.out.print(binMin+"\t");
		System.out.println();
		for (int binMin = min2; binMin < max2; binMin += binLength2)
			System.out.print(binMin+"\t");
		System.out.println();
		for (int i=0; i < numBins; i++) {
			for (int j=0; j < numBins; j++) {
				System.out.print(bins[i][j]+"\t");
			}
			System.out.println();
		}
	}
	
	public static void printMonCount(String fileName) {
		HashMap<Integer,Integer> map = getMonLocs(fileName);
		Iterator<Integer> itr = map.keySet().iterator();
		while (itr.hasNext()) {
			System.out.println(map.get(itr.next()));
		}
	}
	
	public static void printInfectivityDistributionByDaysSick() {
		HashMap<Integer,Double> map = new HashMap<Integer,Double>();
		try {
			Connection con = dbConnect();
			ResultSet infEventsQ = con.createStatement().executeQuery(
					"SELECT d1.time,d2.time FROM (new_dendro AS d1 INNER JOIN dendro_contact AS dc ON d1.victimID = dc.victimID) "+
					"INNER JOIN new_dendro AS d2 ON dc.infectedID = d2.victimID WHERE d1.numInfected=1");
			while (infEventsQ.next()) {
				long time1 = infEventsQ.getLong("d1.time");
				long time2 = infEventsQ.getLong("d1.time");
				int days = EpiSimUtil.secondsToDays(time1 - time2);
				EpiSimUtil.incMapValue(map, new Integer(days), 1.0);
			}
			con.close();
			EpiSimUtil.normalize(map);
			Iterator<Integer> itr = map.keySet().iterator();
			while (itr.hasNext()) {
				Integer next = itr.next();
				System.out.println(next+":\t"+map.get(next));
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void test() {
		TreeMap<Integer,Integer> map = new TreeMap<Integer,Integer>();
		map.put(new Integer(1), new Integer(11));
		map.put(new Integer(2), new Integer(12));
		map.put(new Integer(3), new Integer(13));
		map.put(new Integer(5), new Integer(15));
		SortedMap<Integer,Integer> tailMap;
		Integer key = map.firstKey();
		System.out.println(map.get(key));
		tailMap = map.tailMap(new Integer(key.intValue()+1));
		key = tailMap.firstKey();
		map.put(new Integer(4), new Integer(14));
		
	}
	
	public static void printLocPop() {
		try {
			Connection con = dbConnect();
			ResultSet pops = con.createStatement().executeQuery(
					"SELECT aggregatePop FROM "+locTbl+" ORDER BY aggregatePop DESC");
			while (pops.next()) {
				System.out.println(pops.getInt("aggregatePop"));
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void printCumNumContactsForClosedLocs(String fileName) {
		try {
			int cumCount = 0;
			BufferedReader rd = new BufferedReader(new FileReader(fileName));
			Connection con = dbConnect();
			PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM "+conTbl+" WHERE locationID = ?");
			String line;
			while ((line = rd.readLine()) != null) {
				StringTokenizer tok = new StringTokenizer(line);
				tok.nextToken();
				int location = Integer.parseInt(tok.nextToken());
				ps.setInt(1, location);
				ResultSet rs = ps.executeQuery();
				rs.first();
				cumCount += rs.getInt("COUNT(*)");
			}
			System.out.println(fileName+"\t"+cumCount);
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void printCumNumContactsForClosedLocs(String[] fileNames) {
		for (int i=0; i < fileNames.length; i++) {
			printCumNumContactsForClosedLocs(fileNames[i]);
		}
	}
	
	public static void printContactDist(Iterator<Integer> locs) {
		while(locs.hasNext()) {
			Integer location = locs.next();
			HashSet<Integer> people = EpiSimUtil.getPeopleAtLocation(location);
			try {
				Connection con = dbConnect();
				PreparedStatement getCon = con.prepareStatement(
						"SELECT person2ID FROM "+conTbl+" WHERE person1ID = ?");
				Iterator<Integer> itr = people.iterator();
				while (itr.hasNext()) {
					int in = 0;
					int out = 0;
					Integer person1 = itr.next();
					getCon.setInt(1, person1.intValue());
					ResultSet getConQ = getCon.executeQuery();
					while (getConQ.next()) {
						Integer person2 = new Integer(getConQ.getInt("person2ID"));
						if (people.contains(person2))
							in++;
						else
							out++;
					}
					System.out.println(location+"\t"+person1+"\t"+in+"\t"+out);
				}
				con.close();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}
	
	public static void printCumClosureStatsOld(String fileName) {
		int[] cumConByDay = new int[101];
		for (int i=0; i < cumConByDay.length; i++)
			cumConByDay[i] = 0;
		try {
			BufferedReader rd = new BufferedReader(new FileReader(fileName));
			Connection con = dbConnect();
			PreparedStatement getNumCon = con.prepareStatement(
					"SELECT COUNT(*) FROM "+conTbl+" WHERE locationID = ?");
			String line;
			while ((line = rd.readLine()) != null) {
				StringTokenizer tok = new StringTokenizer(line);
				long time = Long.parseLong(tok.nextToken());
				int day = EpiSimUtil.secondsToDays(time);
				int location = Integer.parseInt(tok.nextToken());
				getNumCon.setInt(1, location);
				ResultSet getNumConQ = getNumCon.executeQuery();
				if (getNumConQ.first())
					cumConByDay[day] += getNumConQ.getInt("COUNT(*)");
			}
			for (int i=0; i < cumConByDay.length; i++)
				System.out.println(i+"\t"+cumConByDay[i]);
			con.close();
		} catch (Exception e) {
			
		}
	}
	
	public static void printCumClosureStatsAggPop(String fileName) {
		int[] cumConByDay = new int[101];
		for (int i=0; i < cumConByDay.length; i++)
			cumConByDay[i] = 0;
		try {
			BufferedReader rd = new BufferedReader(new FileReader(fileName));
			Connection con = dbConnect();
			PreparedStatement getNumCon = con.prepareStatement(
					"SELECT aggregatePop FROM "+locTbl+" WHERE locationID = ?");
			String line;
			while ((line = rd.readLine()) != null) {
				StringTokenizer tok = new StringTokenizer(line);
				long time = Long.parseLong(tok.nextToken());
				int day = EpiSimUtil.secondsToDays(time);
				int location = Integer.parseInt(tok.nextToken());
				getNumCon.setInt(1, location);
				ResultSet getNumConQ = getNumCon.executeQuery();
				if (getNumConQ.first())
					cumConByDay[day] += getNumConQ.getInt("aggregatePop");
			}
			for (int i=0; i < cumConByDay.length; i++)
				System.out.println(i+"\t"+cumConByDay[i]);
			con.close();
		} catch (Exception e) {
			
		}
	}
	
	public static HashMap<String,Integer> getEffectStats(HashSet<Integer> infPpl, HashSet<Integer> closedLocs) {
		HashMap<String,Integer> stats = new HashMap<String,Integer>();
		int affCount = 0;
		int infConCount = 0;
		int totalConCount = 0;
		try {
			Connection con = dbConnect();
			PreparedStatement getNumLocCon = con.prepareStatement(
					"SELECT contactCount,aggregatePop FROM "+locTbl+" WHERE locationID = ?");
			PreparedStatement getNumPplLocCon = con.prepareStatement(
					"SELECT locationID FROM "+conTbl+" WHERE person1ID = ?");
			Iterator<Integer> locs = closedLocs.iterator();
			while (locs.hasNext()) {
				Integer loc = locs.next();
				getNumLocCon.setInt(1, loc.intValue());
				ResultSet getNumLocConQ = getNumLocCon.executeQuery();
				if (getNumLocConQ.first()) {
					affCount += getNumLocConQ.getInt("aggregatePop");
					totalConCount += getNumLocConQ.getInt("contactCount");
				} else {
					System.out.println("Could not get stats on location:\t"+loc);
				}
			}
			Iterator<Integer> ppl = infPpl.iterator();
			while (ppl.hasNext()) {
				HashMap<Integer,Integer> cons = new HashMap<Integer,Integer>();
				Integer p = ppl.next();
				getNumPplLocCon.setInt(1,p.intValue());
				ResultSet getNumPplLocConQ = getNumPplLocCon.executeQuery();
				while (getNumPplLocConQ.next()) {
					Integer loc = getNumPplLocConQ.getInt("locationID");
					EpiSimUtil.incMapValue(cons, loc, 1);
				}
				locs = cons.keySet().iterator();
				while (locs.hasNext()) {
					Integer loc = locs.next();
					if (closedLocs.contains(loc))
						infConCount += cons.get(loc).intValue();
				}
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		stats.put("AFFECTED", new Integer(affCount));
		stats.put("INF_CON", new Integer(infConCount));
		stats.put("TOT_CON", new Integer(totalConCount));
		return stats;
	}
	
	public static void printCumClosureStats(String locFileName, String infFileName, int closDur) {
		int[] contByDay = new int[101];
		int[] contConByDay = new int[101];
//		int[] contDegByDay = new int[101];
		int[] cumConByDay = new int[101];
		int[] cumAggPopByDay = new int[101];
		HashMap<Integer,HashSet<Integer>> locClosures = new HashMap<Integer,HashSet<Integer>>(101);
		for (int i=0; i < cumConByDay.length; i++) {
			cumConByDay[i]  = 0;
			contConByDay[i] = 0;
//			contDegByDay[i] = 0;
			contByDay[i]    = 0;
			cumAggPopByDay[i] = 0;
			locClosures.put(new Integer(i), new HashSet<Integer>());
		}
		try {
			Connection con = dbConnect();
			
			BufferedReader rd = new BufferedReader(new FileReader(locFileName));
			PreparedStatement getNumLocCon = con.prepareStatement(
					"SELECT contactCount,aggregatePop FROM "+locTbl+" WHERE locationID = ?");
			String line;
			while ((line = rd.readLine()) != null) {
				StringTokenizer tok = new StringTokenizer(line);
				long time = Long.parseLong(tok.nextToken());
				int day = EpiSimUtil.secondsToDays(time);
				int location = Integer.parseInt(tok.nextToken());
				getNumLocCon.setInt(1, location);
				ResultSet getNumLocConQ = getNumLocCon.executeQuery();
				if (getNumLocConQ.first()) {
					int conC = getNumLocConQ.getInt("contactCount");
					int pop = getNumLocConQ.getInt("aggregatePop");
					for (int i=0; i < closDur && day+i < 101; i++) {
						Integer key = new Integer(day+i);
						locClosures.get(key).add(new Integer(location));
						cumConByDay[day+i] += conC;
						cumAggPopByDay[day+i] += pop;
					}
				}
			}
			
			rd = new BufferedReader(new FileReader(infFileName));
//			PreparedStatement getNumPplCon = con.prepareStatement(
//					"SELECT tally FROM "+conCountTbl+" WHERE personID = ?");
			PreparedStatement getNumPplLocCon = con.prepareStatement(
					"SELECT COUNT(*) FROM "+conTbl+" WHERE person1ID = ? AND locationID = ?");
			while ((line = rd.readLine()) != null) {
				StringTokenizer tok = new StringTokenizer(line);
				long time = Long.parseLong(tok.nextToken());
				int day = EpiSimUtil.secondsToDays(time);
				int victim = Integer.parseInt(tok.nextToken());
				for (int i=0; i < 8 && day+i < 101; i++)
					contByDay[day+i] += 1;
//				int location = Integer.parseInt(tok.nextToken());
//				getNumPplCon.setInt(1, victim);
				getNumPplLocCon.setInt(1, victim);
//				getNumPplLocCon.setInt(2, location);
//				ResultSet getNumPplConQ = getNumPplCon.executeQuery();
//				ResultSet getNumPplLocConQ = getNumPplLocCon.executeQuery();
				
//				int deg = getNumPplConQ.getInt("tally");
				
				Iterator<Integer> itr = getLocationsVisited(victim).iterator();
				while (itr.hasNext()) {
					Integer location = itr.next();
					getNumPplLocCon.setInt(2, location.intValue());
					ResultSet getNumPplLocConQ = getNumPplLocCon.executeQuery();
					if (getNumPplLocConQ.first()) {
						int numCon = getNumPplLocConQ.getInt("COUNT(*)");
						Iterator<Integer> dayItr = locClosures.keySet().iterator();
						while (dayItr.hasNext()) {
							Integer key = dayItr.next();
							if (locClosures.get(key).contains(location)) {
								contConByDay[key.intValue()] += numCon;
							}
						}
					}
				}
			}
			System.out.println("day"+"\t"+"Num contageous"+"\t"+"Num affected"+
						"\t"+"Num contacts closed"+"\t"+"Num sick contacts closed");
			
			for (int day=0; day < 101; day++) {
				System.out.println(day+"\t"+contByDay[day]+"\t"+cumAggPopByDay[day]+
						"\t"+cumConByDay[day]+"\t"+contConByDay[day]);
			}
			
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void insertAllContactCountForLocTbl() {
		try {
			Connection con = dbConnect();
			PreparedStatement getNumCon = con.prepareStatement(
					"SELECT COUNT(*) FROM "+conTbl+" WHERE locationID = ?");
			PreparedStatement insertNumCon = con.prepareStatement(
					"UPDATE "+locTbl+" SET contactCount=? WHERE locationID = ?");
			for (int location = 1; location <= 246090; location++) {
				getNumCon.setInt(1, location);
				ResultSet getNumConQ = getNumCon.executeQuery();
				if (getNumConQ.first()) {
					int count = getNumConQ.getInt("COUNT(*)");
					insertNumCon.setInt(1, count);
					insertNumCon.setInt(2, location);
					insertNumCon.executeUpdate();
				}
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void printPeopleDeg() {
		try {
			Connection con = dbConnect();
			ResultSet getDegQ = con.createStatement().executeQuery(
					"SELECT personID,tally FROM "+conCountTbl);
			while (getDegQ.next()) {
				int p = getDegQ.getInt("PersonID");
				int d = getDegQ.getInt("tally");
				System.out.println(p+"\t"+d);
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

}
