/**
 * 
 */
package dbepi;
import java.sql.*;
import java.io.*;
import java.util.*;

/**
 * @author dave
 *
 */
public class EpiSimUtil {
	public static Connection fludatabase = null;
	
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
	public static final String locPeopleSocRankTbl = "location_people_social_rank";
	public static final String locPeopleSocRankDupTbl = "location_people_social_rank_dup";
	public static final String locHomeDistTbl = "location_people_home_distance";
	public static final String locHomeDistPercentileTbl = "location_people_home_distance_percentile";
	
	public static final String ppInfTbl = "pp_influence";
	public static final String ppDescrTbl = "pp_infl_exp";
	public static final String peopleFromImpHousesTbl = "people_imp_households";
	
	public static final String dendroFile = "mysql-dendro-portland-1-v1.dat";
	public static final String dendroConFile = "mysql-contact-dendro-portland-1-v1.dat";
	public static final String conCountFile = "mysql-count-contact-portland-1-v2.dat";
	
	public static final String[] activityTypeNames = {"home","work","shop","visit","social","other","pickup","school","college"};
	
	public static int population = 1615860;
	public static int numLocations = 246090;
	public static long simEndTime = 8640000;
	public static final int numHouseHolds = 636389;
	
	
	/**
	 * 
	 */
	public EpiSimUtil() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		populateDendro();
//		populateDendroCon();
//		numTotalContacts();
//		getGenerationStats();
//		getGenerationStatsFirstFortyDays();
//		testEemptyResultSet();
//		compareSocInflPeople(100);
//		getGenerationStatsFirstFortyDays();
//		getSpontaneousInfections();
//		insertAllLocationTypes();
//		insertAllLLcontacts();
//		insertAggregateLocationPopulation();
//		getLLcontactGraphStats();
//		insertLLContactCount();
//		computeEigenScore();
//		getCoordHighestDegLoc(349);
//		insertLLContactCountPurpose(0);
//		insertLLContactInterCount();
//		avgPeopleSocInTopSocLoc(100);
//		populateLocaitonPeopleSocialRankTbl();
//		updateLocaitonPeopleSocialRankDupTblAddTally();
//		populateLocationHomeDistanceTable();
//		printAllPercentileLocationHomeDistance(90);
//		saveAllPercentileLocationHomeDistance(90);
//		printCorrelationOfLargestOneToNHomeDistLocToWork(10000, 90);
//		saveAndPrintAllPercentileLocationHomeDistance(75);
//		printNumLocGivenHomeDistAndAggPop(20,20,90, 43471.861580222, 5370);
//		printHomeDistVsDegree(90);
//		printlocOutDegVsWheightedMeanPersonSocScore();
//		printlocOutDegVsNthPercentilePersonSocScore(80);
//		memTest();
//		printXYOfHomeDistOutDegCluster(400,0,5000,90);
//		printHomeDistVsDegreeRestByNthPerSocScore(1000, 80, 80, 90);
//		printAllPercentileLocationHomeDistanceRestByDuration(90, 60*60);
//		parseVaccFile("mostSocFracOfHighestDegLocRestByhomeDist.vacc");
//		printLocsOfInfectEvent(0, 60*60*24, 100*60*60*24);
//		printLocsOfInfectEvent();
//		printCorInfEventsToLLInterCount();
		int numLocations = 350;//Integer.parseInt(args[0]);
//		getHouseHoldsFromProjImpLoc(numLocations);
//		insertPeopleFromHouseHolds(numLocations);
//		BufferedBigIntSet set = getLocsHighOutDeg(5);
//		Iterator<Integer> setItr = set.iterator();
//		while (setItr.hasNext()) {
//			System.out.println(setItr.next());
//		}

	}
	
	public static long dayToSeconds(int numDays) {
		return numDays*60*60*24;
	}
	
	public static void memTest(){
		int[][] adj = new int[population][278];
		for (int r = 0; r < adj.length; r++){
			for (int c = 0; c < adj[r].length; c++){
				adj[r][c] = 0;
			}
		}
		try {
			Thread.sleep(30000);
			System.out.println("DONE");
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static Connection dbConnectSingle(){
		if (fludatabase != null) return fludatabase;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			fludatabase = DriverManager.getConnection(url, user, pwd);
			return fludatabase;
		} catch (Exception e){
			System.out.println(e);
			return null;
		}
	}
	
	public static Connection dbConnect(){
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			return DriverManager.getConnection(url, user, pwd);
		} catch (Exception e){
			System.out.println(e);
			return null;
		}
	}
	
	public static Connection dbGenDataConnect(){
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			return DriverManager.getConnection(urlGenDB, user, pwd);
		} catch (Exception e){
			System.out.println(e);
			return null;
		}
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
	
	public static double getMinX(){
		double minX = -1.0;
		try {
			Connection con = dbConnect();
			Statement getMinX = con.createStatement();
			ResultSet getMinXQ = getMinX.executeQuery(
					"SELECT MIN(x) AS minx FROM "+locTbl);
			if (getMinXQ.next())
				minX = getMinXQ.getDouble("minx");
			con.close();
			return minX;
		} catch (Exception e){
			System.out.println(e);
			return minX;
		}
	}
	
	public static double getMinY(){
		double minY = -1.0;
		try {
			Connection con = dbConnect();
			Statement getMinY = con.createStatement();
			ResultSet getMinYQ = getMinY.executeQuery(
					"SELECT MIN(y) AS miny FROM "+locTbl);
			if (getMinYQ.next())
				minY = getMinYQ.getDouble("miny");
			con.close();
			return minY;
		} catch (Exception e){
			System.out.println(e);
			return minY;
		}
	}
	
	public static double getMaxX(){
		double maxX = -1.0;
		try {
			Connection con = dbConnect();
			Statement getMaxX = con.createStatement();
			ResultSet getMaxXQ = getMaxX.executeQuery(
					"SELECT MAX(x) AS maxx FROM "+locTbl);
			if (getMaxXQ.next())
				maxX = getMaxXQ.getDouble("maxx");
			con.close();
			return maxX;
		} catch (Exception e){
			System.out.println(e);
			return maxX;
		}
	}
	
	public static double getMaxY(){
		double maxY = -1.0;
		try {
			Connection con = dbConnect();
			Statement getMaxY = con.createStatement();
			ResultSet getMaxYQ = getMaxY.executeQuery(
					"SELECT MAX(y) AS maxy FROM "+locTbl);
			if (getMaxYQ.next())
				maxY = getMaxYQ.getDouble("maxy");
			con.close();
			return maxY;
		} catch (Exception e){
			System.out.println(e);
			return maxY;
		}
	}
	
	public static int getMaxAggPop(){
		int maxAggPop = -1;
		try {
			Connection con = dbConnect();
			Statement getMaxAggPop = con.createStatement();
			ResultSet getMaxAggPopQ = getMaxAggPop.executeQuery(
					"SELECT MAX(aggregatePop) AS max FROM "+locTbl);
			if (getMaxAggPopQ.first())
				maxAggPop = getMaxAggPopQ.getInt("max");
			con.close();
			return maxAggPop;
		} catch (Exception e){
			System.out.println(e);
			return maxAggPop;
		}
	}
	
	public static int getMinAggPop(){
		int minAggPop = -1;
		try {
			Connection con = dbConnect();
			Statement getMinAggPop = con.createStatement();
			ResultSet getMinAggPopQ = getMinAggPop.executeQuery(
					"SELECT MIN(aggregatePop) AS min FROM "+locTbl);
			//con.close();
			if (getMinAggPopQ.first())
				minAggPop = getMinAggPopQ.getInt("min");
			con.close();
			return minAggPop;
		} catch (Exception e){
			System.out.println(e);
			return minAggPop;
		}
	}
	
	public static double getMaxHomeDist(int percentile){
		double maxHomeDist = -1.0;
		try {
			Connection con = dbConnect();
			Statement getMaxHomeDist = con.createStatement();
			ResultSet getMaxHomeDistQ = getMaxHomeDist.executeQuery(
					"SELECT MAX(distance) AS max FROM "+locHomeDistPercentileTbl+" WHERE percentile="+percentile);
			if (getMaxHomeDistQ.first())
				maxHomeDist = getMaxHomeDistQ.getDouble("max");
			con.close();
			return maxHomeDist;
		} catch (Exception e){
			System.out.println(e);
			return maxHomeDist;
		}
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
	
	public static double getMinHomeDist(int percentile){
		double minHomeDist = -1.0;
		try {
			Connection con = dbConnect();
			Statement getMinHomeDist = con.createStatement();
			ResultSet getMinHomeDistQ = getMinHomeDist.executeQuery(
					"SELECT MIN(distance) AS min FROM "+locHomeDistPercentileTbl+" WHERE percentile="+percentile);
			if (getMinHomeDistQ.first())
				minHomeDist = getMinHomeDistQ.getDouble("min");
			con.close();
			return minHomeDist;
		} catch (Exception e){
			System.out.println(e);
			return minHomeDist;
		}
	}
	
	public static HashSet<Integer> getContacts(int person){
		HashSet<Integer> contacts = null;
		try {
			Connection con = dbConnect();
			ResultSet getContactsQ = con.createStatement().executeQuery(
					"SELECT person2ID FROM "+conTbl+" WHERE person1ID="+person);
			getContactsQ.last();
			int numContacts = getContactsQ.getRow();
			getContactsQ.beforeFirst();
			contacts = new HashSet<Integer>(numContacts);
			while (getContactsQ.next()) {
				int contact = getContactsQ.getInt("person2ID");
				contacts.add(new Integer(contact));
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return contacts;
	}
	
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
	
	public static HashSet<Integer> getPeopleAtLocation(Integer location) {
		HashSet<Integer> people = new HashSet<Integer>(getAggPop(location));
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
	
	public static int getNumDistPeopleAtLocations(HashSet<Integer> locations) {
		return getPeopleAtLocations(locations).size();
	}
	
	public static int getNumDistPeopleAtLocations(BufferedBigIntSet locations) {
		return getPeopleAtLocations(locations).size();
	}
	
	public static BufferedBigIntSet getPeopleAtLocations(HashSet<Integer> locations) {
		//int expectedNumberOfPeoplePerLocation = 1000;
		BufferedBigIntSet people = new BufferedBigIntSet();
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
	
	public static BufferedBigIntSet getPeopleAtLocations(BufferedBigIntSet locations) {
		//int expectedNumberOfPeoplePerLocation = 1000;
		BufferedBigIntSet people = new BufferedBigIntSet();
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
	
//	public static HashSet<Integer> getLocsHighOutDegRestHomeDist(int numLocations, double minHomeDist, int homeDistPercentile) {
//		HashSet<Integer> locations = new HashSet<Integer>(numLocations);
//		try {
//			Connection con = dbConnect();
//			ResultSet locsQ = con.createStatement().executeQuery(
//					"SELECT "+locHomeDistPercentileTbl+".locationID FROM "+locHomeDistPercentileTbl+" INNER JOIN "
//					+llCountTbl+" ON "+locHomeDistPercentileTbl+".locationID = "+llCountTbl+".locationID"
//					+" WHERE "+locHomeDistPercentileTbl+".percentile = "+homeDistPercentile+" AND "
//					+locHomeDistPercentileTbl+".distance >= "+minHomeDist+" ORDER BY "+llCountTbl
//					+".outbound DESC LIMIT "+numLocations);
//			while (locsQ.next()) {
//				locations.add(new Integer(locsQ.getInt(locHomeDistPercentileTbl+".locationID")));
//			}
//			con.close();
//		} catch (Exception e) {
//			System.out.println(e);
//		}
//		return locations;
//	}
	
	public static BufferedBigIntSet getLocsHighOutDeg(int numLocations) {
		BufferedBigIntSet locations = new BufferedBigIntSet();
		//HashSet<Integer> locations = new HashSet<Integer>(numLocations);
		try {
			Connection con = dbConnect();
			ResultSet locsQ = con.createStatement().executeQuery(
					"SELECT locationID FROM "+llCountTbl+
					" ORDER BY outbound DESC LIMIT "+numLocations);
			while (locsQ.next()) {
				locations.add(new Integer(locsQ.getInt("locationID")));
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return locations;
	}
	
	public static BufferedBigIntSet getLocsHighOutDegRestHomeDist(int numLocations, double minHomeDist, int homeDistPercentile) {
		BufferedBigIntSet locations = new BufferedBigIntSet();
		//HashSet<Integer> locations = new HashSet<Integer>(numLocations);
		try {
			Connection con = dbConnect();
			ResultSet locsQ = con.createStatement().executeQuery(
					"SELECT "+locHomeDistPercentileTbl+".locationID FROM "+locHomeDistPercentileTbl+" INNER JOIN "
					+llCountTbl+" ON "+locHomeDistPercentileTbl+".locationID = "+llCountTbl+".locationID"
					+" WHERE "+locHomeDistPercentileTbl+".percentile = "+homeDistPercentile+" AND "
					+locHomeDistPercentileTbl+".distance >= "+minHomeDist+" ORDER BY "+llCountTbl
					+".outbound DESC LIMIT "+numLocations);
			while (locsQ.next()) {
				locations.add(new Integer(locsQ.getInt(locHomeDistPercentileTbl+".locationID")));
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return locations;
	}
	
	public static HashMap<Integer,Integer> getLocsDegWithHighOutDegRestHomeDist(int numLocations, double minHomeDist, int homeDistPercentile) {
		HashMap<Integer,Integer> locations = new HashMap<Integer,Integer>(numLocations);
		try {
			Connection con = dbConnect();
			ResultSet locsQ = con.createStatement().executeQuery(
					"SELECT "+locHomeDistPercentileTbl+".locationID,"+llCountTbl+".outbound FROM "+locHomeDistPercentileTbl+" INNER JOIN "
					+llCountTbl+" ON "+locHomeDistPercentileTbl+".locationID = "+llCountTbl+".locationID"
					+" WHERE "+locHomeDistPercentileTbl+".percentile = "+homeDistPercentile+" AND "
					+locHomeDistPercentileTbl+".distance >= "+minHomeDist+" ORDER BY "+llCountTbl
					+".outbound DESC LIMIT "+numLocations);
			while (locsQ.next()) {
				locations.put(new Integer(locsQ.getInt(locHomeDistPercentileTbl+".locationID")), 
						new Integer(locsQ.getInt(llCountTbl+".outbound")));
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return locations;
	}
	
	/**
	 * Returns a list of the housholds whose members visit the highest degree locations
	 * that have a 90th percentile home-distance above 10,000 meters.  The list is 
	 * in descending order of the aggregate location outdegree of each household.
	 * @param numLocations
	 * @return
	 */
	public static HashMap<Integer,Integer> getHouseHoldsScoresFromProjImpLoc(int numLocations) {
		int estNumHouseHolds = 50000;
		double minDist = 10000;
		int percentile = 90;
		HashMap<Integer,Integer> locations = getLocsDegWithHighOutDegRestHomeDist(numLocations,minDist,percentile);
		HashMap<Integer,Integer> houseHoldScores = new HashMap<Integer,Integer>(estNumHouseHolds);
		Connection con = dbConnect();
		try {
			PreparedStatement getHouseHolds = con.prepareStatement(
				"SELECT "+demoTbl+".householdID FROM "+actTbl+" INNER JOIN "+demoTbl+
				" ON "+actTbl+".personID = "+demoTbl+".personID WHERE "+actTbl+".locationID = ?");
			Iterator<Integer> locsItr = locations.keySet().iterator();
			while (locsItr.hasNext()) {
				Integer location = locsItr.next();
				getHouseHolds.setInt(1, location.intValue());
				ResultSet getHouseHoldsQ = getHouseHolds.executeQuery();
				while (getHouseHoldsQ.next()) {
					Integer houseHold = getHouseHoldsQ.getInt(demoTbl+".householdID");
					if (houseHoldScores.containsKey(houseHold)) {
						int oldScore = houseHoldScores.get(houseHold).intValue();
						int newScore = oldScore + locations.get(location).intValue();
						houseHoldScores.put(houseHold, new Integer(newScore));
					} else {
						houseHoldScores.put(houseHold, locations.get(location));
					}
				}
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return houseHoldScores;
	}
	
	public static void printAvgSocVsHouseHoldScore(int numLocations) {
		HashMap<Integer,Integer> houseHoldScores = getHouseHoldsScoresFromProjImpLoc(numLocations);
		try {
			Connection con = dbConnect();
			PreparedStatement getSoc = con.prepareStatement(
					"SELECT "+conCountTbl+".tally FROM "+conCountTbl+" INNER JOIN "+demoTbl+" ON "
					+conCountTbl+".personID = "+demoTbl+".personID WHERE "+demoTbl+".householdID = ?");
			Iterator<Integer> houseHoldItr = houseHoldScores.keySet().iterator();
			while (houseHoldItr.hasNext()) {
				Integer houseHold = houseHoldItr.next();
				getSoc.setInt(1, houseHold.intValue());
				ResultSet getSocQ = getSoc.executeQuery();
				double totalSoc = 0;
				double numPeople = 0;
				while (getSocQ.next()) {
					totalSoc += getSocQ.getInt(conCountTbl+".tally");
					numPeople++;
				}
				double avgSoc = totalSoc / numPeople;
				System.out.println(houseHoldScores.get(houseHold)+"\t"+avgSoc);
			}
			
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
//	/**
//	 * Returns a list of the housholds whose members visit the highest degree locations
//	 * that have a 90th percentile home-distance above 10,000 meters.  The list is 
//	 * in descending order of the aggregate location outdegree of each household.
//	 * @param numLocations
//	 * @return
//	 */
//	public static ArrayList<Integer> getHouseHoldsFromProjImpLoc(int numLocations) {
//		ArrayList<Integer> houseHolds = new ArrayList<Integer>();
//		int estNumHouseHolds = 50000;
//		double minDist = 10000;
//		int percentile = 90;
//		HashMap<Integer,Integer> locations = getLocsDegWithHighOutDegRestHomeDist(numLocations,minDist,percentile);
//		HashMap<Integer,Integer> houseHoldScores = new HashMap<Integer,Integer>(estNumHouseHolds);
//		Connection con = dbConnect();
//		try {
//			PreparedStatement getHouseHolds = con.prepareStatement(
//				"SELECT "+demoTbl+".householdID FROM "+actTbl+" INNER JOIN "+demoTbl+
//				" ON "+actTbl+".personID = "+demoTbl+".personID WHERE "+actTbl+".locationID = ?");
//			Iterator<Integer> locsItr = locations.keySet().iterator();
//			while (locsItr.hasNext()) {
//				Integer location = locsItr.next();
//				getHouseHolds.setInt(1, location.intValue());
//				ResultSet getHouseHoldsQ = getHouseHolds.executeQuery();
//				while (getHouseHoldsQ.next()) {
//					Integer houseHold = getHouseHoldsQ.getInt(demoTbl+".householdID");
//					if (houseHoldScores.containsKey(houseHold)) {
//						int oldScore = houseHoldScores.get(houseHold).intValue();
//						int newScore = oldScore + locations.get(location).intValue();
//						houseHoldScores.put(houseHold, new Integer(newScore));
//					} else {
//						houseHoldScores.put(houseHold, locations.get(location));
//					}
//				}
//			}
//			Integer[] temp = new Integer[1];
//			temp = houseHoldScores.keySet().toArray(temp);
//			Arrays.sort(temp, new HouseHoldComparator(houseHoldScores));
////			TreeMap<Integer,Integer> houseHoldTree = new TreeMap<Integer,Integer>();
////			Iterator<Integer> houseHoldItr = houseHoldScores.keySet().iterator();
////			while (houseHoldItr.hasNext()) {
////				Integer houseHold = houseHoldItr.next();
////				int key = (houseHoldScores.get(houseHold).intValue()*(numHouseHolds+1))+houseHold.intValue();
////				houseHoldTree.put(new Integer(key), houseHold);
////			}
////			Iterator<Integer> houseHoldKeyItr = houseHoldTree.keySet().iterator();
////			while (houseHoldKeyItr.hasNext()) {
////				Integer key = houseHoldKeyItr.next();
////				Integer houseHold = houseHoldTree.get(key);
////				houseHolds.addFirst(houseHold);
////			}
//			
//			Iterator<Integer> testItr = houseHolds.iterator();
//			while (testItr.hasNext()) {
//				Integer houseHold = testItr.next();
//				System.out.println(houseHold+"\t"+houseHoldScores.get(houseHold));
//			}
//			
//			con.close();
//		} catch (Exception e) {
//			System.out.println(e);
//		}
//		return houseHolds;
//	}
	
	/**
	 * Returns a list of the housholds whose members visit the highest degree locations
	 * that have a 90th percentile home-distance above 10,000 meters.  The list is 
	 * in descending order of the aggregate location outdegree of each household.
	 * @param numLocations
	 * @return
	 */
	public static Integer[] getHouseHoldsFromProjImpLoc(int numLocations) {
		HashMap<Integer,Integer> houseHoldScores = getHouseHoldsScoresFromProjImpLoc(numLocations);
//		System.out.println(houseHoldScores.size());
		Comparator<Integer> comp = new HouseHoldComparator(houseHoldScores);
		Integer[] tmp = new Integer[1];
		Integer[] houseHolds = houseHoldScores.keySet().toArray(tmp);
		Arrays.sort(houseHolds, comp);
//		System.out.println(houseHolds.length);
//		for (int i=0; i < houseHolds.length; i++) {
//			System.out.println(houseHolds[i]+"\t"+houseHoldScores.get(houseHolds[i]));
//		}
		return houseHolds;
	}
	
	public static Integer[] getPeopleFromImpHouseHolds(int numPeople, int numLocations) {
		return getPeopleFromHouseHolds(numPeople, getHouseHoldsFromProjImpLoc(numLocations));
	}
	
	public static Integer[] getPeopleFromHouseHolds(int numPeople, Integer[] houseHolds) {
		ArrayList<Integer> people = new ArrayList<Integer>(numPeople);
		try {
			Connection con = dbConnect();
			PreparedStatement getPeople = con.prepareStatement(
					"SELECT personID FROM "+demoTbl+" WHERE householdID = ?");
			for (int house=0; house < houseHolds.length && people.size() < numPeople; house++) {
				getPeople.setInt(1, houseHolds[house].intValue());
				ResultSet getPeopleQ = getPeople.executeQuery();
				while (getPeopleQ.next() && people.size() < numPeople) {
					people.add(new Integer(getPeopleQ.getInt("personID")));
				}
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return people.toArray(new Integer[1]);
	}
	
	public static void insertPeopleFromHouseHolds(int numLocations) {
		Integer[] houseHolds = getHouseHoldsFromProjImpLoc(numLocations);
		try {
			Connection con = dbConnect();
			PreparedStatement getPeople = con.prepareStatement(
					"SELECT personID FROM "+demoTbl+" WHERE householdID = ?");
			Connection con_ins = EpiSimUtil.dbGenDataConnect();
			PreparedStatement insert = con_ins.prepareStatement(
					"INSERT INTO "+peopleFromImpHousesTbl+
					" (personID,rank,numLocations) VALUES (?,?,"+numLocations+")");
			int rank = 0;
			for (int house=0; house < houseHolds.length; house++) {
				getPeople.setInt(1, houseHolds[house].intValue());
				ResultSet getPeopleQ = getPeople.executeQuery();
				while (getPeopleQ.next()) {
					int person = getPeopleQ.getInt("personID");
					insert.setInt(1, person);
					insert.setInt(2, rank);
					insert.executeUpdate();
					rank++;
				}
			}
			con_ins.close();
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static Integer[] getPeopleFromImpHousesTbl(int startRank, int numPeople, int numLocations) {
		Integer[] people = new Integer[numPeople];
		try {
			Connection con = dbGenDataConnect();
			ResultSet getPeopleQ = con.createStatement().executeQuery(
					"SELECT personID FROM "+peopleFromImpHousesTbl+" WHERE numLocations = "+numLocations+
					" AND rank >= "+startRank+" ORDER BY RANK ASC LIMIT "+numPeople);
			for (int i = 0; getPeopleQ.next(); i++) {
				people[i] = new Integer(getPeopleQ.getInt("personID"));
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return people;
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
	
	public static void compareSocInflPeople(int cutoff){
		try {
			Connection db = dbConnect();
			Connection dbGen = dbGenDataConnect();
			Statement getNumPeople = db.createStatement();
			ResultSet numPeopleQ = getNumPeople.executeQuery(
					"SELECT COUNT(*) AS num FROM "+EpiSimUtil.conCountTbl+" WHERE tally >= "+cutoff);
			numPeopleQ.first();
			int numPeople = numPeopleQ.getInt(1);
			System.out.println("Looking at the top "+numPeople+" people.");
			HashSet<Integer> socPop= new HashSet<Integer>(numPeople);
			HashSet<Integer> InfPop3 = new HashSet<Integer>(numPeople);
			HashSet<Integer> InfPop4 = new HashSet<Integer>(numPeople);
			Statement getSocPop = db.createStatement();
			Statement getInfPop3 = dbGen.createStatement();
			Statement getInfPop4 = dbGen.createStatement();
			ResultSet socPopQ = getSocPop.executeQuery(
					"SELECT personID FROM "+EpiSimUtil.conCountTbl+" ORDER BY tally DESC LIMIT "+numPeople);
			while (socPopQ.next()){
				socPop.add(socPopQ.getInt(1));
			}
			ResultSet InfPop3Q = getInfPop3.executeQuery(
					"SELECT personID FROM "+EpiSimUtil.ppInfTbl+" WHERE runID=3"+
					" ORDER BY score DESC LIMIT "+numPeople);
			while (InfPop3Q.next()){
				InfPop3.add(InfPop3Q.getInt(1));
			}
			ResultSet InfPop4Q = getInfPop4.executeQuery(
					"SELECT personID FROM "+EpiSimUtil.ppInfTbl+" WHERE runID=4"+
					" ORDER BY score DESC LIMIT "+numPeople);
			while (InfPop4Q.next()){
				InfPop4.add(InfPop4Q.getInt(1));
			}
			int intSocInfl3 = 0;
			int intSocInfl4 = 0;
			int intInfl3Infl4 = 0;
			Iterator<Integer> socItr = socPop.iterator();
			while(socItr.hasNext()){
				Integer next = socItr.next();
				if(InfPop3.contains(next)) intSocInfl3++;
				if(InfPop4.contains(next)) intSocInfl4++;
			}
			System.out.println("Intersection between soc. and infl3 is "+intSocInfl3);
			System.out.println("Intersection between soc. and infl4 is "+intSocInfl4);
			Iterator<Integer> infItr = InfPop3.iterator();
			while(infItr.hasNext()){
				Integer next = infItr.next();
				if(InfPop4.contains(next)) intInfl3Infl4++;
			}
			System.out.println("Intersection between infl3 and infl4 is "+intInfl3Infl4);
			db.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void populateDendro(){
		Connection con = dbConnect();
		try {
			PreparedStatement pInsert = con.prepareStatement(
					"INSERT INTO "+dendroTbl+
					" (victimID, time, locationID, generation, numInfected) VALUES(?, ?, ?, ?, ?)");
//			pInsert.setInt(1, 1);
//			pInsert.setInt(2, 2);
//			pInsert.setInt(3, 3);
//			pInsert.setInt(4, 4);
//			pInsert.setInt(5, 5);
//			pInsert.executeUpdate();
			BufferedReader rd = new BufferedReader(new FileReader(dendroFile));
			String line;
			StringTokenizer tok;
			int victimID, time, locationID, generation, numInfected;
//			for (int i=0;i<5;i++){
//				line = rd.readLine();
			for (int i=0; i<41282; i++)
				rd.readLine();
			while ((line = rd.readLine()) != null){
				tok = new StringTokenizer(line);
				victimID = Integer.valueOf(tok.nextToken()).intValue();
				time = Integer.valueOf(tok.nextToken()).intValue();
				locationID = Integer.valueOf(tok.nextToken()).intValue();
				generation = Integer.valueOf(tok.nextToken()).intValue();
				numInfected = Integer.valueOf(tok.nextToken()).intValue();
				pInsert.setInt(1, victimID);
				pInsert.setInt(2, time);
				pInsert.setInt(3, locationID);
				pInsert.setInt(4, generation);
				pInsert.setInt(5, numInfected);
				pInsert.executeUpdate();
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void populateDendroCon(){
		Connection con = dbConnect();
		try {
			PreparedStatement pInsert = con.prepareStatement(
					"INSERT INTO "+dendroConTbl+
					" (victimID, infectedID) VALUES(?, ?)");
			BufferedReader rd = new BufferedReader(new FileReader(dendroConFile));
			String line;
			StringTokenizer tok;
			int victimID, infectedID;
			while ((line = rd.readLine()) != null){
				tok = new StringTokenizer(line);
				victimID = Integer.valueOf(tok.nextToken()).intValue();
				infectedID = Integer.valueOf(tok.nextToken()).intValue();
				pInsert.setInt(1, victimID);
				pInsert.setInt(2, infectedID);
				pInsert.executeUpdate();
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void populateConCount(){
		Connection con = dbConnect();
		try {
			PreparedStatement pInsert = con.prepareStatement(
					"INSERT INTO "+conCountTbl+
					" (personID, tally) VALUES(?, ?)");
			BufferedReader rd = new BufferedReader(new FileReader(conCountFile));
			String line;
			StringTokenizer tok;
			int personID, tally;
			while ((line = rd.readLine()) != null){
				tok = new StringTokenizer(line);
				personID = Integer.valueOf(tok.nextToken()).intValue();
				tally = Integer.valueOf(tok.nextToken()).intValue();
				pInsert.setInt(1, personID);
				pInsert.setInt(2, tally);
				pInsert.executeUpdate();
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void numTotalContacts(){
		Connection con = dbConnect();
		long total = 0;
		try {
			PreparedStatement st = con.prepareStatement(
					"SELECT tally FROM "+conCountTbl+" WHERE personID = ?");
			for (int i=1; i <=1615860; i++){
				st.setInt(1, i);
				ResultSet rs = st.executeQuery();
				while (rs.next()){
					total += rs.getInt(1);
				}
			}
			System.out.println("The total number of contacts is: "+total);
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static void getGenerationStats(){
		int[] genTally;
		Connection con = dbConnect();
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT MAX(generation) AS max_gen FROM "+dendroTbl);
			rs.first();
			genTally = new int[rs.getInt(1)+1];
			PreparedStatement pstmt = con.prepareStatement(
					"SELECT time FROM "+dendroTbl+" WHERE generation = ?");
			for (int i=0; i < genTally.length; i++) {
				pstmt.setInt(1, i);
				rs = pstmt.executeQuery();
				rs.last();
				genTally[i] = rs.getRow();
				System.out.println(i+"\t"+genTally[i]);
			}
			double mean = 0;
			double total = 0;
			for (int i=0; i < genTally.length; i++) {
				mean += i*genTally[i];
				total += genTally[i];
			}
			mean /= total;
			System.out.println("mean generation number is: "+mean);
			System.out.println("total number infected is: "+total);
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static void getGenerationStatsFirstFortyDays(){
		int timeCutOff = 40*24*60*60;
		int[] genTally;
		Connection con = dbConnect();
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT MAX(generation) AS max_gen FROM "+dendroTbl);
			rs.first();
			genTally = new int[rs.getInt(1)+1];
			PreparedStatement pstmt = con.prepareStatement(
					"SELECT time FROM "+dendroTbl+" WHERE generation = ? AND time < "+timeCutOff);
			for (int i=0; i < genTally.length; i++) {
				pstmt.setInt(1, i);
				rs = pstmt.executeQuery();
				rs.last();
				genTally[i] = rs.getRow();
				System.out.println(i+"\t"+genTally[i]);
			}
			double mean = 0;
			double total = 0;
			for (int i=0; i < genTally.length; i++) {
				mean += i*genTally[i];
				total += genTally[i];
			}
			mean /= total;
			System.out.println("mean generation number is: "+mean);
			System.out.println("total number infected is: "+total);
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static void testEemptyResultSet(){
		try {
			Connection con = dbGenDataConnect();
			PreparedStatement pSelect = con.prepareStatement(
					"SELECT score FROM "+ppInfTbl+" WHERE personID = ?");
			pSelect.setInt(1, 1);
			ResultSet rs = pSelect.executeQuery();
			rs.last();
			int numberOfRows = rs.getRow();
			System.out.println("Last row number in empty set is: "+numberOfRows);
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static void getSpontaneousInfections(){
		HashSet<Integer> allInfected = new HashSet<Integer>(600000);
		HashSet<Integer> legitInfected = new HashSet<Integer>(600000);
		Connection con = dbConnect();
		try {
			// Get Initially Infected and add to both sets
			Statement getInitInfected = con.createStatement();
			ResultSet initInfectedQ = getInitInfected.executeQuery(
					"SELECT id FROM "+initTbl);
			while (initInfectedQ.next()){
				Integer p = new Integer(initInfectedQ.getInt(1));
				allInfected.add(p);
				legitInfected.add(p);
			}
			// Get people infected by recorded transmission event and add to both sets
			Statement getLegitInfected = con.createStatement();
			ResultSet legitInfectedQ = getLegitInfected.executeQuery(
					"SELECT victimID FROM "+dendroTbl);
			while (legitInfectedQ.next()){
				Integer p = new Integer(legitInfectedQ.getInt(1));
				allInfected.add(p);
				legitInfected.add(p);
			}
			// Get any other infected person in dendro table and add to allInfected set
			Statement getRestInfected = con.createStatement();
			ResultSet restInfectedQ = getRestInfected.executeQuery(
					"SELECT infectedID FROM "+dendroConTbl);
			while (restInfectedQ.next()){
				Integer p = new Integer(restInfectedQ.getInt(1));
				allInfected.add(p);
			}
			System.out.println(
					"There are "+(allInfected.size()-legitInfected.size())+
					" people who do not have recorded infection event, or where not initially infected.");
			// print the people in allInfected not in legitInfected
			Iterator<Integer> itr = allInfected.iterator();
			while(itr.hasNext()){
				Integer p = itr.next();
				if (!legitInfected.contains(p))
					System.out.println(p);
			}
			con.close();
		}  catch (Exception e){
			System.out.println(e);
		}
		
	}
	
	public static void insertAllLocationTypes(){
		boolean[][] activityTypes = new boolean[numLocations+1][9];
		try {
			Connection con = dbConnect();
			PreparedStatement getActivity = con.prepareStatement(
					"SELECT COUNT(*) FROM "+actTbl+" WHERE locationID = ? AND purpose = ?");
			for (int location=1; location <= numLocations; location++){
				for (int type=0; type < 9; type++){
					getActivity.setInt(1, location);
					getActivity.setInt(2, type);
					ResultSet activityQ = getActivity.executeQuery();
					activityQ.first();
					int numMatches = activityQ.getInt(1);
					if (numMatches > 0) activityTypes[location][type] = true;
					else activityTypes[location][type] = false;
				}
			}
			PreparedStatement setType = con.prepareStatement(
					"UPDATE "+locTbl+" SET type = ? WHERE locationID = ?");
			for (int location=1; location <= numLocations; location++){
				setType.setInt(2, location);
				String typeValue = "'";
				for (int type=0; type < 9; type++){
					if (activityTypes[location][type])
						typeValue += activityTypeNames[type] + ",";
				}
				if (typeValue.length() > 1) {
					typeValue = typeValue.substring(0, typeValue.length()-1) + "'";
					setType.setString(1, typeValue);
					System.out.println(location+"\t"+typeValue);
					Statement st = con.createStatement();
					st.executeUpdate(
							"UPDATE "+locTbl+" SET type="+typeValue+" WHERE locationID="+location);
				}
				//else typeValue = "''";
			}
			setType.executeBatch();
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static void insertAggregateLocationPopulation(){
		int initAggPopCapacity = 50;
		try {
			Connection con = dbConnect();
			PreparedStatement getPeople = con.prepareStatement(
					"SELECT personID FROM "+actTbl+" WHERE locationID = ?");
			Statement updateAggPop = con.createStatement();
			for (int location = 1; location <= numLocations; location++){
				HashSet<Integer> aggPop = new HashSet<Integer>(initAggPopCapacity);
				getPeople.setInt(1, location);
				ResultSet getPeopleQ = getPeople.executeQuery();
				while (getPeopleQ.next()){
					aggPop.add(new Integer(getPeopleQ.getInt("personID")));
				}
				updateAggPop.executeUpdate(
						"UPDATE "+locTbl+" SET aggregatePop="+aggPop.size()+" WHERE locationID="+location);
			}
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static void insertAllLLcontacts(){
		try {
			Connection con = dbConnect();
			PreparedStatement getActivities = con.prepareStatement(
					"SELECT locationID,activityID,startTime,purpose FROM "+actTbl+
					" WHERE personID = ? ORDER BY startTime ASC");
			PreparedStatement insertEdge = con.prepareStatement(
					"INSERT INTO "+llTbl+" (location1ID,location2ID,personID,activityID1,activityID2,startTime1,startTime2,purpose1,purpose2) VALUES(?,?,?,?,?,?,?,?,?)");
			for (int person=1; person <= population; person++){
				int location1,location2,activity1,activity2,start1,start2,purpose1,purpose2;
				getActivities.setInt(1, person);
				ResultSet activitiesQ = getActivities.executeQuery();
				location1 = -1;
				activity1 = -1;
				start1    = -1;
				purpose1  = -1;
				if (activitiesQ.next()){
					location1 = activitiesQ.getInt(1);
					activity1 = activitiesQ.getInt(2);
					start1    = activitiesQ.getInt(3);
					purpose1  = activitiesQ.getInt(4);
				}
				while (activitiesQ.next()){
					location2 = activitiesQ.getInt(1);
					activity2 = activitiesQ.getInt(2);
					start2    = activitiesQ.getInt(3);
					purpose2  = activitiesQ.getInt(4);
					
					insertEdge.setInt(1, location1);
					insertEdge.setInt(2, location2);
					insertEdge.setInt(3, person);
					insertEdge.setInt(4, activity1);
					insertEdge.setInt(5, activity2);
					insertEdge.setInt(6, start1);
					insertEdge.setInt(7, start2);
					insertEdge.setInt(8, purpose1);
					insertEdge.setInt(9, purpose2);
					
					insertEdge.executeUpdate();
					
					location1 = location2;
					activity1 = activity2;
					start1    = start2;
					purpose1  = purpose2;
				}
			}
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static void getLLcontactGraphStats(){
		int maxNumOutEdges = 0;
		int maxNumInEdges  = 0;
		try {
			Connection con = dbConnect();
			PreparedStatement getNumOutboundEdges = con.prepareStatement(
					"SELECT COUNT(*) FROM "+llTbl+" WHERE location1ID = ?");
			PreparedStatement getNumInboundEdges = con.prepareStatement(
					"SELECT COUNT(*) FROM "+llTbl+" WHERE location2ID = ?");
			for (int location = 1; location <= numLocations; location++){
				if (location % 1000 == 0) System.out.println("location: "+location);
				getNumOutboundEdges.setInt(1, location);
				getNumInboundEdges.setInt(1, location);
				ResultSet numOutboundEdgesQ = getNumOutboundEdges.executeQuery();
				ResultSet numInboundEdgesQ = getNumInboundEdges.executeQuery();
				if (numOutboundEdgesQ.first() && numOutboundEdgesQ.getInt("COUNT(*)") > maxNumOutEdges)
					maxNumOutEdges = numOutboundEdgesQ.getInt("COUNT(*)");
				if (numInboundEdgesQ.first() && numInboundEdgesQ.getInt("COUNT(*)") > maxNumInEdges)
					maxNumInEdges = numInboundEdgesQ.getInt("COUNT(*)");
					
			}
			System.out.println("The maximum number of outbound edges is: "+maxNumOutEdges);
			System.out.println("The maximum number of inbound edges is:  "+maxNumInEdges);
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static void insertLLContactCount(){
		try {
			Connection con = dbConnect();
			PreparedStatement getNumOutboundEdges = con.prepareStatement(
					"SELECT COUNT(*) FROM "+llTbl+" WHERE location1ID = ?");
			PreparedStatement getNumInboundEdges = con.prepareStatement(
					"SELECT COUNT(*) FROM "+llTbl+" WHERE location2ID = ?");
			PreparedStatement insertLocation = con.prepareStatement(
					"INSERT INTO "+llCountTbl+" (locationID,outbound,inbound) VALUES(?,?,?)");
			for (int location = 1; location <= numLocations; location++){
				if (location % 1000 == 0) System.out.println("location: "+location);
				getNumOutboundEdges.setInt(1, location);
				getNumInboundEdges.setInt(1, location);
				ResultSet numOutboundEdgesQ = getNumOutboundEdges.executeQuery();
				ResultSet numInboundEdgesQ = getNumInboundEdges.executeQuery();
				if (numOutboundEdgesQ.first() && numInboundEdgesQ.first()){
					int outbound = numOutboundEdgesQ.getInt("COUNT(*)");
					int inbound = numInboundEdgesQ.getInt("COUNT(*)");
					insertLocation.setInt(1, location);
					insertLocation.setInt(2, outbound);
					insertLocation.setInt(3, inbound);
					insertLocation.executeUpdate();
				} else {
					System.out.println("Could not get edge count for location "+location);
				}
			}
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static void computeEigenScoreLowMem(){
		double dampFactor = 0.8;
		int numIterations = 100;
		double[] scoreOld = new double[population+1];
		double[] scoreNew = new double[population+1];
		HashMap<Integer,Integer> neighborWeight = new HashMap<Integer,Integer>(100);
		for (int i = 0; i < scoreOld.length; i++){
			scoreOld[i] = 1;
		}
		try {
			Connection con = dbConnect();
			PreparedStatement getNeighbors = con.prepareStatement(
					"SELECT person1ID FROM "+conTbl+" WHERE person2ID = ?");
			PreparedStatement getNeighborCount = con.prepareStatement(
					"SELECT tally FROM "+conCountTbl+" WHERE personID = ?");
			for (int i = 0; i < numIterations; i++){
				for (int person = 1; person <= population; person++){
					getNeighbors.setInt(1, person);
					ResultSet getNeighborsQ = getNeighbors.executeQuery();
					getNeighborsQ.last();
					int numNeighbors = getNeighborsQ.getRow();
					if (numNeighbors == 0){
						scoreNew[person] = 0;
					} else {
						getNeighborsQ.beforeFirst();
						while (getNeighborsQ.next()){
							Integer neighbor = new Integer(getNeighborsQ.getInt("person1ID"));
							if (neighborWeight.containsKey(neighbor)){
								neighborWeight.put(neighbor, new Integer(neighborWeight.get(neighbor) + 1));
							} else {
								neighborWeight.put(neighbor, new Integer(1));
							}
						}
						Iterator<Integer> itr = neighborWeight.keySet().iterator();
						while(itr.hasNext()){
							int neighbor = itr.next().intValue();
							int weight = neighborWeight.get(new Integer(neighbor));
							getNeighborCount.setInt(1, neighbor);
							ResultSet getNeighborCountQ = getNeighborCount.executeQuery();
							getNeighborCountQ.first();
							int tally = getNeighborCountQ.getInt("tally");
							scoreNew[person] += (dampFactor*scoreOld[neighbor]*weight) / tally;
						}
						scoreNew[person] += (1.0 - dampFactor);
					}
					neighborWeight = new HashMap<Integer,Integer>(100);
				}
				scoreOld = scoreNew;
				scoreNew = new double[population+1];
//				System.out.println("Iteration number: "+i);
//				System.out.println("-------------------------------");
//				for (int j = 0; j < scoreOld.length; j++){
//					System.out.println(scoreOld[j]);
//				}
			}
			for (int j = 0; j < scoreOld.length; j++){
				System.out.println(j+"\t"+scoreOld[j]);
			}
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static void computeEigenScore(){
		double dampFactor = 0.8;
		int numIterations = 100;
		double[] scoreOld = new double[population+1];
		double[] scoreNew = new double[population+1];
		HashMap[] neighborWeights = new HashMap[population+1];
		for (int i = 0; i < scoreOld.length; i++){
			scoreOld[i] = 1;
		}
		try {
			Connection con = dbConnect();
			PreparedStatement getNeighbors = con.prepareStatement(
					"SELECT person1ID FROM "+conTbl+" WHERE person2ID = ?");
			PreparedStatement getNeighborCount = con.prepareStatement(
					"SELECT tally FROM "+conCountTbl+" WHERE personID = ?");
			for (int person = 1; person <= population; person++){
				HashMap<Integer,Double> neighborWeight = new HashMap<Integer,Double>(100);
				getNeighbors.setInt(1, person);
				ResultSet getNeighborsQ = getNeighbors.executeQuery();
				while (getNeighborsQ.next()){
					Integer neighbor = new Integer(getNeighborsQ.getInt("person1ID"));
					if (neighborWeight.containsKey(neighbor)){
						neighborWeight.put(neighbor, new Double(neighborWeight.get(neighbor) + 1));
					} else {
						neighborWeight.put(neighbor, new Double(1));
					}
				}
				Iterator<Integer> itr = neighborWeight.keySet().iterator();
				while(itr.hasNext()){
					int neighbor = itr.next().intValue();
					double weight = neighborWeight.get(new Integer(neighbor));
					getNeighborCount.setInt(1, neighbor);
					ResultSet getNeighborCountQ = getNeighborCount.executeQuery();
					getNeighborCountQ.first();
					int tally = getNeighborCountQ.getInt("tally");
					neighborWeight.put(new Integer(neighbor), (dampFactor*weight) / tally);
				}
				neighborWeights[person] = neighborWeight;
			}
			for (int i = 0; i < numIterations; i++){
				for (int person = 1; person <= population; person++){
					HashMap<Integer,Double> neighborWeight = neighborWeights[person];
					Iterator<Integer> itr = neighborWeight.keySet().iterator();
					while(itr.hasNext()){
						Integer neighbor = itr.next();
						double weight = neighborWeight.get(neighbor);
						scoreNew[person] += scoreOld[neighbor]*weight;
					}
					scoreNew[person] += (1.0 - dampFactor);
				}
				scoreOld = scoreNew;
				scoreNew = new double[population+1];
//				System.out.println("Iteration number: "+i);
//				System.out.println("-------------------------------");
//				for (int j = 0; j < scoreOld.length; j++){
//					System.out.println(scoreOld[j]);
//				}
			}
			for (int j = 0; j < scoreOld.length; j++){
				System.out.println(j+"\t"+scoreOld[j]);
			}
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static void getCoordHighestDegLoc(int numLocations){
		try {
			Connection con = dbConnect();
			PreparedStatement getXY = con.prepareStatement(
					"SELECT x,y FROM "+locTbl+" WHERE locationID = ?");
			Statement getLocID = con.createStatement();
			ResultSet getLocIDQ = getLocID.executeQuery(
					"SELECT locationID FROM "+llCountTbl+" ORDER BY outbound DESC LIMIT "+numLocations);
			while (getLocIDQ.next()){
				getXY.setInt(1, getLocIDQ.getInt("locationID"));
				ResultSet getXYQ = getXY.executeQuery();
				if (getXYQ.next())
					System.out.println(getXYQ.getInt("x")+"\t"+getXYQ.getInt("y"));
			}
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static void insertLLContactCountPurpose(int purpose){
		try {
			Connection con = dbConnect();
			PreparedStatement getNumOutboundEdges = con.prepareStatement(
					"SELECT COUNT(*) FROM "+llTbl+" WHERE location1ID = ? AND purpose1="+purpose);
			PreparedStatement getNumInboundEdges = con.prepareStatement(
					"SELECT COUNT(*) FROM "+llTbl+" WHERE location2ID = ? AND purpose2="+purpose);
			PreparedStatement insertLocation = con.prepareStatement(
					"INSERT INTO "+llCountPurpTbl+" (purpose,locationID,outbound,inbound) VALUES('"+activityTypeNames[purpose]+"',?,?,?)");
			for (int location = 1; location <= numLocations; location++){
				if (location % 1000 == 0) System.out.println("location: "+location);
				getNumOutboundEdges.setInt(1, location);
				getNumInboundEdges.setInt(1, location);
				ResultSet numOutboundEdgesQ = getNumOutboundEdges.executeQuery();
				ResultSet numInboundEdgesQ = getNumInboundEdges.executeQuery();
				if (numOutboundEdgesQ.first() && numInboundEdgesQ.first()){
					int outbound = numOutboundEdgesQ.getInt("COUNT(*)");
					int inbound = numInboundEdgesQ.getInt("COUNT(*)");
					insertLocation.setInt(1, location);
					insertLocation.setInt(2, outbound);
					insertLocation.setInt(3, inbound);
					insertLocation.executeUpdate();
				} else {
					System.out.println("Could not get edge count for location "+location);
				}
			}
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
//	public static void insertLLContactInterCount(){
//		try {
//			Connection con = dbConnect();
//			PreparedStatement getCount = con.prepareStatement(
//					"SELECT COUNT(*) FROM "+llTbl+" WHERE location1ID = ? AND location2ID = ?");
//			PreparedStatement insertCount = con.prepareStatement(
//					"INSERT INTO "+llInterCountTbl+" (location1ID,location2ID,tally) VALUES(?,?,?)");
//			for (int location1 = 1; location1 <= numLocations; location1++){
//				for (int location2 = 1; location2 <= numLocations; location2++){
//					getCount.setInt(1, location1);
//					getCount.setInt(2, location2);
//					ResultSet getCountQ = getCount.executeQuery();
//					if (getCountQ.first()){
//						int tally = getCountQ.getInt("COUNT(*)");
//						if (tally > 0) {
//							insertCount.setInt(1, location1);
//							insertCount.setInt(2, location2);
//							insertCount.setInt(3, tally);
//							insertCount.executeUpdate();
//						}
//					} else {
//						System.out.println(location1+"\t"+location2);
//					}
//				}
//			}
//			con.close();
//		} catch (Exception e){
//			System.out.println(e);
//		}
//	}
	
//	public static void insertLLContactInterCount(){
//		try {
//			HashMap<Integer, Integer> tally = new HashMap<Integer, Integer>(1000000);
//			Connection con = dbConnect();
//			PreparedStatement insertCount = con.prepareStatement(
//					"INSERT INTO "+llInterCountTbl+" (location1ID,location2ID,tally) VALUES(?,?,?)");
//			PreparedStatement getEdges = con.prepareStatement(
//					"SELECT location1ID,location2ID FROM "+llTbl+" WHERE location2ID = ?");
////			getEdges.setFetchSize(1000);
////			ResultSet getEdgesQ = getEdges.executeQuery("SELECT location1ID,location2ID FROM "+llTbl);
//			for (int i = 1; i <= numLocations; i++){
//				getEdges.setInt(1, i);
//				ResultSet getEdgesQ = getEdges.executeQuery();
//				while (getEdgesQ.next()){
//					int location1 = getEdgesQ.getInt("location1ID");
//					int location2 = getEdgesQ.getInt("location2ID");
//					Integer hashValue = hashTwoLocations(location1, location2);
//					if (tally.containsKey(hashValue)){
//						Integer newTally = new Integer(tally.get(hashValue).intValue() + 1);
//						tally.put(hashValue, newTally);
//					} else {
//						tally.put(hashValue, new Integer(1));
//					}
//				}
//			}
//			Iterator<Integer> itr = tally.keySet().iterator();
//			while (itr.hasNext()){
//				Integer key = itr.next();
//				int location1 = key.intValue() % (numLocations+1);
//				int location2 = key.intValue() / (numLocations+1);
//				insertCount.setInt(1, location1);
//				insertCount.setInt(2, location2);
//				insertCount.setInt(3, tally.get(key).intValue());
//				insertCount.executeUpdate();
//			}
//			con.close();
//		} catch (Exception e){
//			System.out.println(e);
//		}
//	}
	
	public static void insertLLContactInterCount(){
		try {
			Connection con = dbConnect();
			PreparedStatement insertCount = con.prepareStatement(
					"INSERT INTO "+llInterCountTbl+" (location1ID,location2ID,tally) VALUES(?,?,?)");
			PreparedStatement getEdges = con.prepareStatement(
					"SELECT location1ID,location2ID FROM "+llTbl+" WHERE location1ID = ?");
//			getEdges.setFetchSize(1000);
//			ResultSet getEdgesQ = getEdges.executeQuery("SELECT location1ID,location2ID FROM "+llTbl);
			for (int location1 = 1; location1 <= numLocations; location1++){
				getEdges.setInt(1, location1);
				ResultSet getEdgesQ = getEdges.executeQuery();
				HashMap<Integer, Integer> tally = new HashMap<Integer, Integer>(1000);
				while (getEdgesQ.next()){
//					int location1 = getEdgesQ.getInt("location1ID");
					int location2 = getEdgesQ.getInt("location2ID");
					Integer hashValue = new Integer(location2);
					if (tally.containsKey(hashValue)){
						Integer newTally = new Integer(tally.get(hashValue).intValue() + 1);
						tally.put(hashValue, newTally);
					} else {
						tally.put(hashValue, new Integer(1));
					}
				}
				Iterator<Integer> itr = tally.keySet().iterator();
				while (itr.hasNext()){
					Integer key = itr.next();
					int location2 = key.intValue();
					insertCount.setInt(1, location1);
					insertCount.setInt(2, location2);
					insertCount.setInt(3, tally.get(key).intValue());
					insertCount.executeUpdate();
				}
			}
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static Integer hashTwoLocations(int location1, int location2){
		int hashValue = location1 + location2*(numLocations+1);
		return new Integer(hashValue);
	}
	
	public static void avgPeopleSocInTopSocLoc(int numLocations){
		try {
			Connection con = dbConnect();
			PreparedStatement getPeople = con.prepareStatement(
					"SELECT personID FROM "+actTbl+" WHERE locationID = ?");
			PreparedStatement getPersonScore = con.prepareStatement(
					"SELECT tally FROM "+conCountTbl+" WHERE personID = ?");
			Statement getLocIds = con.createStatement();
			ResultSet getLocIdsQ = getLocIds.executeQuery(
					"SELECT locationID, outbound FROM "+llCountTbl+" ORDER BY outbound DESC LIMIT "+numLocations);
			HashMap<Integer, Integer> locOutScore = new HashMap<Integer, Integer>(numLocations);
			HashMap<Integer, Double> locAvgPersonScore = new HashMap<Integer, Double>(numLocations);
			while (getLocIdsQ.next()) {
				int location = getLocIdsQ.getInt("locationID");
				int outbound = getLocIdsQ.getInt("outbound");
				locOutScore.put(new Integer(location), new Integer(outbound));
				getPeople.setInt(1, location);
				ResultSet getPeopleQ = getPeople.executeQuery();
				double totalScore = 0.0;
				double numPeople = 0.0;
				while (getPeopleQ.next()){
					getPersonScore.setInt(1, getPeopleQ.getInt("personID"));
					ResultSet getPersonScoreQ = getPersonScore.executeQuery();
					if (getPersonScoreQ.first()){
						numPeople++;
						totalScore += getPersonScoreQ.getInt("tally");
					}
					locAvgPersonScore.put(outbound, new Double(totalScore / numPeople));
				}
			}
			Iterator<Integer> itr = locAvgPersonScore.keySet().iterator();
			while (itr.hasNext()){
				Integer next = itr.next();
				System.out.println(next+"\t"+locAvgPersonScore.get(next));
			}
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static int getMaxAggPopAtAnyLocation(){
		try {
			Connection con = dbConnect();
			Statement getMaxAggPop = con.createStatement();
			ResultSet getMaxAggPopQ = getMaxAggPop.executeQuery(
					"SELECT MAX(aggregatePop) AS max FROM "+locTbl);
			if (getMaxAggPopQ.first()) return getMaxAggPopQ.getInt("max");
			else return -1;
		} catch (Exception e){
			System.out.println(e);
			return -1;
		}
	}
	
	public static void populateLocaitonPeopleSocialRankTbl() {
		try {
			Connection con = EpiSimUtil.dbConnect();
			PreparedStatement getScore = con.prepareStatement(
					"SELECT tally FROM "+EpiSimUtil.conCountTbl+" WHERE personID = ?");
			PreparedStatement getPeople = con.prepareStatement(
					"SELECT personID FROM "+EpiSimUtil.actTbl+" WHERE locationID = ?");
			PreparedStatement insert = con.prepareStatement(
					"INSERT INTO "+locPeopleSocRankTbl+" (locationID,personID,rank,tally) VALUES(?,?,?,?)");
			for (int location = 1; location <= numLocations; location++) {
				TreeMap<Integer, HashSet<Integer>> people = new TreeMap<Integer, HashSet<Integer>>();
				getPeople.setInt(1, location);
				ResultSet getPeopleQ = getPeople.executeQuery();
				while (getPeopleQ.next()){
					int person = getPeopleQ.getInt("personID");
					getScore.setInt(1, person);
					ResultSet getScoreQ = getScore.executeQuery();
					getScoreQ.first();
					Integer score = new Integer(getScoreQ.getInt("tally"));
					HashSet<Integer> peopleWithThisScore;
					if (people.containsKey(score)) {
						peopleWithThisScore = people.get(score);
					} else {
						peopleWithThisScore = new HashSet<Integer>(5);
					}
					peopleWithThisScore.add(new Integer(person));
					people.put(score, peopleWithThisScore);
				}
				int rank = 1;
				while (people.size() > 0){
					Integer biggestScore = people.lastKey();
					HashSet<Integer> peopleWithThisScore = people.get(biggestScore);
					people.remove(biggestScore);
					Iterator<Integer> itr = peopleWithThisScore.iterator();
					while (itr.hasNext()){
						Integer person = itr.next();
						insert.setInt(1, location);
						insert.setInt(2, person);
						insert.setInt(3, rank);
						insert.setInt(4, biggestScore.intValue());
						insert.executeUpdate();
						rank++;
					}
				}
			}
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static void updateLocaitonPeopleSocialRankDupTblAddTally() {
		try {
			Connection con = EpiSimUtil.dbConnect();
			PreparedStatement getPersonTally = con.prepareStatement(
					"SELECT personID,tally FROM "+locPeopleSocRankTbl+" WHERE locationID = ?");
			PreparedStatement updateDupTbl = con.prepareStatement(
					"UPDATE "+locPeopleSocRankDupTbl+" SET tally = ? WHERE locationID = ? AND personID = ?");
			for (int location = 1; location <= numLocations; location++){
				getPersonTally.setInt(1, location);
				ResultSet getPersonTallyQ = getPersonTally.executeQuery();
				while(getPersonTallyQ.next()){
					int person = getPersonTallyQ.getInt("personID");
					int tally = getPersonTallyQ.getInt("tally");
					updateDupTbl.setInt(1, tally);
					updateDupTbl.setInt(2, location);
					updateDupTbl.setInt(3, person);
					updateDupTbl.executeUpdate();
				}
			}
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static void populateLocationHomeDistanceTable(){
		try {
			Connection con = dbConnect();
			PreparedStatement getXY = con.prepareStatement(
					"SELECT x,y FROM "+locTbl+" WHERE locationID = ?");
			PreparedStatement getHomeLoc = con.prepareStatement(
					"SELECT homeLocationID FROM "+demoTbl+" WHERE personID = ?");
			PreparedStatement insert = con.prepareStatement(
					"INSERT INTO "+locHomeDistTbl+" (locationID,personID,distance) VALUES (?,?,?)");
			for (int location = 1; location <= numLocations; location++){
				getXY.setInt(1, location);
				ResultSet getXYQ = getXY.executeQuery();
				if (getXYQ.first()){
					double x = getXYQ.getInt("x");
					double y = getXYQ.getInt("y");
					HashSet<Integer> people = getPeopleAtLocation(location);
					Iterator<Integer> peopleItr = people.iterator();
					while(peopleItr.hasNext()){
						int person = peopleItr.next().intValue();
						getHomeLoc.setInt(1, person);
						ResultSet getHomeLocQ = getHomeLoc.executeQuery();
						if (getHomeLocQ.first()){
							int home = getHomeLocQ.getInt("homeLocationID");
							getXY.setInt(1, home);
							getXYQ = getXY.executeQuery();
							if (getXYQ.first()) {
								double homeX = getXYQ.getInt("x");
								double homeY = getXYQ.getInt("y");
								double distance = Math.sqrt(Math.pow((homeX - x),2.0) + Math.pow((homeY - y),2.0));
								insert.setInt(1, location);
								insert.setInt(2, person);
								insert.setDouble(3, distance);
								insert.executeUpdate();
							} else {
								System.out.println("Unable to get coordinates for location: "+location);
							}
						} else {
							System.out.println("Unable to get home location for person: "+person);
						}
					}
				} else {
					System.out.println("Unable to get coordinates for location: "+location);
				}
			}
			
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static HashSet<Integer> getPeopleAtLocation(int location){
		try {
			Connection con = dbConnect();
			Statement getPeople = con.createStatement();
			ResultSet getPeopleQ = getPeople.executeQuery(
					"SELECT personID FROM "+actTbl+" WHERE locationID="+location);
			HashSet<Integer> people = new HashSet<Integer>(10000);
			while(getPeopleQ.next()){
				people.add(new Integer(getPeopleQ.getInt("personID")));
			}
			con.close();
			return people;
		} catch (Exception e){
			System.out.println(e);
			return null;
		}
	}
	/**
	 * 
	 * @param location id of location
	 * @param duration minimum duration that person must be at location in seconds
	 * @return
	 */
	public static HashSet<Integer> getPeopleAtLocationForAtLeastSpecDur(int location, int duration){
		try {
			Connection con = dbConnect();
			Statement getPeople = con.createStatement();
			ResultSet getPeopleQ = getPeople.executeQuery(
					"SELECT personID FROM "+actTbl+" WHERE locationID="+location+" AND duration >= "+duration);
			HashSet<Integer> people = new HashSet<Integer>(10000);
			while(getPeopleQ.next()){
				people.add(new Integer(getPeopleQ.getInt("personID")));
			}
			con.close();
			return people;
		} catch (Exception e){
			System.out.println(e);
			return null;
		}
	}
	
	public static void printAllPercentileLocationHomeDistance(int percentile){
		try {
			Connection con = dbConnect();
			PreparedStatement getXY = con.prepareStatement(
					"SELECT x,y FROM "+locTbl+" WHERE locationID = ?");
			PreparedStatement getDistance = con.prepareStatement(
					"SELECT distance FROM "+locHomeDistTbl+" WHERE locationID = ? ORDER BY distance ASC");
			for (int location = 1; location <= numLocations; location++){
				getXY.setInt(1, location);
				getDistance.setInt(1, location);
				ResultSet getXYQ = getXY.executeQuery();
				ResultSet getDistanceQ = getDistance.executeQuery();
				getDistanceQ.last();
				int numPeople = getDistanceQ.getRow();
//				System.out.println("number of people: "+numPeople);
				int person = (percentile*numPeople)/100;
//				System.out.println("row number: "+person);
				if (person > 0 && getXYQ.first() && getDistanceQ.absolute(person)){
					System.out.println(getXYQ.getDouble("x")+"\t"+getXYQ.getDouble("y")+
							"\t"+getDistanceQ.getDouble("distance"));
				}
			}
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static void printAllPercentileLocationHomeDistanceRestByDuration(int percentile, int duration){
		try {
			Connection con = dbConnect();
			PreparedStatement getXY = con.prepareStatement(
					"SELECT x,y FROM "+locTbl+" WHERE locationID = ?");
			PreparedStatement getDistance = con.prepareStatement(
					"SELECT personID,distance FROM "+locHomeDistTbl+" WHERE locationID = ? ORDER BY distance ASC");
			for (int location = 1; location <= numLocations; location++){
				HashSet<Integer> peopleAtLocLongEnough = getPeopleAtLocationForAtLeastSpecDur(location, duration);
				getXY.setInt(1, location);
				getDistance.setInt(1, location);
				ResultSet getXYQ = getXY.executeQuery();
				ResultSet getDistanceQ = getDistance.executeQuery();
				getDistanceQ.last();
				int numPeople = getDistanceQ.getRow();
				if (numPeople > 0) {
					getDistanceQ.beforeFirst();
					ArrayList<Double> distances = new ArrayList<Double>(numPeople);
					while (getDistanceQ.next()) {
						double distance = getDistanceQ.getDouble("distance");
						Integer personID = new Integer(getDistanceQ.getInt("personID"));
						if (peopleAtLocLongEnough.contains(personID))
							distances.add(distance);
					}
	//				System.out.println("number of people: "+numPeople);
					numPeople = distances.size();
					int index = (percentile*numPeople)/100;
	//				System.out.println("row number: "+person);
					if (index > 0 && getXYQ.first()){
						System.out.println(getXYQ.getDouble("x")+"\t"+getXYQ.getDouble("y")+
								"\t"+distances.get(index));
					}
				}
			}
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static void saveAllPercentileLocationHomeDistance(int percentile){
		try {
			Connection con = dbConnect();
			PreparedStatement getXY = con.prepareStatement(
					"SELECT x,y FROM "+locTbl+" WHERE locationID = ?");
			PreparedStatement getDistance = con.prepareStatement(
					"SELECT distance FROM "+locHomeDistTbl+" WHERE locationID = ? ORDER BY distance ASC");
			PreparedStatement insert = con.prepareStatement(
					"INSERT INTO "+locHomeDistPercentileTbl+" (locationID,x,y,distance,percentile) VALUES (?,?,?,?,"+percentile+")");
			for (int location = 1; location <= numLocations; location++){
				getXY.setInt(1, location);
				getDistance.setInt(1, location);
				ResultSet getXYQ = getXY.executeQuery();
				ResultSet getDistanceQ = getDistance.executeQuery();
				getDistanceQ.last();
				int numPeople = getDistanceQ.getRow();
				int person = (percentile*numPeople)/100;
				if (person > 0 && getXYQ.first() && getDistanceQ.absolute(person)){
					double x = getXYQ.getDouble("x");
					double y = getXYQ.getDouble("y");
					double distance = getDistanceQ.getDouble("distance");
					insert.setInt(1, location);
					insert.setDouble(2, x);
					insert.setDouble(3, y);
					insert.setDouble(4, distance);
					insert.executeUpdate();
				}
			}
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static void saveAndPrintAllPercentileLocationHomeDistance(int percentile){
		try {
			Connection con = dbConnect();
			PreparedStatement getXY = con.prepareStatement(
					"SELECT x,y FROM "+locTbl+" WHERE locationID = ?");
			PreparedStatement getDistance = con.prepareStatement(
					"SELECT distance FROM "+locHomeDistTbl+" WHERE locationID = ? ORDER BY distance ASC");
			PreparedStatement insert = con.prepareStatement(
					"INSERT INTO "+locHomeDistPercentileTbl+" (locationID,x,y,distance,percentile) VALUES (?,?,?,?,"+percentile+")");
			for (int location = 1; location <= numLocations; location++){
				getXY.setInt(1, location);
				getDistance.setInt(1, location);
				ResultSet getXYQ = getXY.executeQuery();
				ResultSet getDistanceQ = getDistance.executeQuery();
				getDistanceQ.last();
				int numPeople = getDistanceQ.getRow();
				int person = (percentile*numPeople)/100;
				if (person > 0 && getXYQ.first() && getDistanceQ.absolute(person)){
					double x = getXYQ.getDouble("x");
					double y = getXYQ.getDouble("y");
					double distance = getDistanceQ.getDouble("distance");
					insert.setInt(1, location);
					insert.setDouble(2, x);
					insert.setDouble(3, y);
					insert.setDouble(4, distance);
					insert.executeUpdate();
					System.out.println(x+"\t"+y+
							"\t"+distance);
				}
			}
			con.close();
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static void printCorrelationOfLargestOneToNHomeDistLocToWork(int n, int percentile){
		int numLoc = 0;
		int numWorkLoc = 0;
		try {
			Connection con = dbConnect();
			PreparedStatement getWorkAct = con.prepareStatement(
					"SELECT personID FROM "+actTbl+" WHERE locationID = ? AND purpose=1");
			Statement getLocs = con.createStatement();
			ResultSet getLocsQ = getLocs.executeQuery(
					"SELECT locationID FROM "+locHomeDistPercentileTbl+
					" WHERE percentile="+percentile+" ORDER BY distance DESC LIMIT "+n);
			while (getLocsQ.next()){
				numLoc++;
				int location = getLocsQ.getInt("locationID");
				getWorkAct.setInt(1, location);
				ResultSet getWorkActQ = getWorkAct.executeQuery();
				if (getWorkActQ.first()){
					numWorkLoc++;
				}
				double fracWorkLoc = (double)numWorkLoc / (double) numLoc;
				System.out.println(numLoc+"\t"+fracWorkLoc);
			}
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static void printNumLocGivenHomeDistAndAggPop(int numHomeDistBins, int numAggPopBins, int percentile){
		int minAggPop = getMinAggPop();
		double minHomeDist = getMinHomeDist(percentile);
		int maxAggPop = getMaxAggPop();
		double maxHomeDist = getMaxHomeDist(percentile);
		int aggPopBinRange = (maxAggPop - minAggPop) / numAggPopBins;
		double homeDistBinRange = (maxHomeDist - minHomeDist) / numHomeDistBins;
		System.out.println("Min agg pop: "+minAggPop);
		System.out.println("Max agg pop: "+maxAggPop);
		System.out.println("Agg pop bin range: "+aggPopBinRange);
		System.out.println("Min home dist: "+minHomeDist);
		System.out.println("Max home dist: "+maxHomeDist);
		System.out.println("Home Dist bin range: "+homeDistBinRange);
		int[][] numLocations = new int[numHomeDistBins][numAggPopBins];
		try {
			Connection con = dbConnect();
			PreparedStatement getLocsByHomeDist = con.prepareStatement(
					"SELECT locationID FROM "+locHomeDistPercentileTbl+" WHERE distance >= ? AND distance < ?");
			PreparedStatement getLocsByAggPop = con.prepareStatement(
					"SELECT locationID FROM "+locTbl+" WHERE aggregatePop >= ? AND aggregatePop < ?");
			for (int homeDistIndex = 0; homeDistIndex < numLocations.length; homeDistIndex++){
				double curMinHomeDist = homeDistIndex*homeDistBinRange + minHomeDist;
				double curMaxHomeDist = (homeDistIndex + 1)*homeDistBinRange + minHomeDist;
				getLocsByHomeDist.setDouble(1, curMinHomeDist);
				getLocsByHomeDist.setDouble(2, curMaxHomeDist);
				ResultSet getLocsByHomeDistQ = getLocsByHomeDist.executeQuery();
				getLocsByHomeDistQ.last();
				int numHomeDistLocs = getLocsByHomeDistQ.getRow();
				getLocsByHomeDistQ.beforeFirst();
				HashSet<Integer> locsRestHomeDist;
				if (numHomeDistLocs > 0) {
					locsRestHomeDist = new HashSet<Integer>(numHomeDistLocs);
				} else {
					locsRestHomeDist = new HashSet<Integer>();
				}
				while (getLocsByHomeDistQ.next()){
					locsRestHomeDist.add(new Integer(getLocsByHomeDistQ.getInt("locationID")));
				}
				for (int aggPopIndex = 0; aggPopIndex < numLocations[homeDistIndex].length; aggPopIndex++){
					numLocations[homeDistIndex][aggPopIndex] = 0;
					int curMinAggPop = aggPopIndex*aggPopBinRange +  minAggPop;
					int curMaxAggPop = (aggPopIndex+1)*aggPopBinRange +  minAggPop;
					getLocsByAggPop.setDouble(1, curMinAggPop);
					getLocsByAggPop.setDouble(2, curMaxAggPop);
					ResultSet getLocsByAggPopQ = getLocsByAggPop.executeQuery();
					getLocsByAggPopQ.last();
					int numAggPopLocs = getLocsByAggPopQ.getRow();
					getLocsByAggPopQ.beforeFirst();
					HashSet<Integer> locsRestAggPop;
					if (numAggPopLocs > 0){
						locsRestAggPop = new HashSet<Integer>(numAggPopLocs);
					} else {
						locsRestAggPop = new HashSet<Integer>();
					}
					while (getLocsByAggPopQ.next()){
						locsRestAggPop.add(new Integer(getLocsByAggPopQ.getInt("locationID")));
					}
					Iterator<Integer> locsRestAggPopItr = locsRestAggPop.iterator();
					while (locsRestAggPopItr.hasNext()){
						if (locsRestHomeDist.contains(locsRestAggPopItr.next())){
							numLocations[homeDistIndex][aggPopIndex]++;
						}
					}
					System.out.println("Home dist index: "+homeDistIndex+". Agg pop index: "+aggPopIndex+
							". Min Home dist: "+curMinHomeDist+". Max Home dist: "+curMaxHomeDist+
							". Min Agg pop: "+curMinAggPop+". Max Agg pop: "+curMaxAggPop+
							". Num locs: "+numLocations[homeDistIndex][aggPopIndex]);
				}
			}
			con.close();
			for (int homeDistIndex = 0; homeDistIndex < numLocations.length; homeDistIndex++){
				for (int aggPopIndex = 0; aggPopIndex < numLocations[homeDistIndex].length; aggPopIndex++){
					System.out.print(numLocations[homeDistIndex][aggPopIndex]+"\t");
				}
				System.out.println();
			}
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static void printNumLocGivenHomeDistAndAggPop(int numHomeDistBins, int numAggPopBins, int percentile, double maxHomeDist, int maxAggPop){
		int minAggPop = getMinAggPop();
		double minHomeDist = getMinHomeDist(percentile);
		int aggPopBinRange = (maxAggPop - minAggPop) / numAggPopBins;
		double homeDistBinRange = (maxHomeDist - minHomeDist) / numHomeDistBins;
		System.out.println("Min agg pop: "+minAggPop);
		System.out.println("Max agg pop: "+maxAggPop);
		System.out.println("Agg pop bin range: "+aggPopBinRange);
		System.out.println("Min home dist: "+minHomeDist);
		System.out.println("Max home dist: "+maxHomeDist);
		System.out.println("Home Dist bin range: "+homeDistBinRange);
		int[][] numLocations = new int[numHomeDistBins][numAggPopBins];
		try {
			Connection con = dbConnect();
			PreparedStatement getLocsByHomeDist = con.prepareStatement(
					"SELECT locationID FROM "+locHomeDistPercentileTbl+" WHERE distance >= ? AND distance < ?");
			PreparedStatement getLocsByAggPop = con.prepareStatement(
					"SELECT locationID FROM "+locTbl+" WHERE aggregatePop >= ? AND aggregatePop < ?");
			for (int homeDistIndex = 0; homeDistIndex < numLocations.length; homeDistIndex++){
				double curMinHomeDist = homeDistIndex*homeDistBinRange + minHomeDist;
				double curMaxHomeDist = (homeDistIndex + 1)*homeDistBinRange + minHomeDist;
				getLocsByHomeDist.setDouble(1, curMinHomeDist);
				getLocsByHomeDist.setDouble(2, curMaxHomeDist);
				ResultSet getLocsByHomeDistQ = getLocsByHomeDist.executeQuery();
				getLocsByHomeDistQ.last();
				int numHomeDistLocs = getLocsByHomeDistQ.getRow();
				getLocsByHomeDistQ.beforeFirst();
				HashSet<Integer> locsRestHomeDist;
				if (numHomeDistLocs > 0) {
					locsRestHomeDist = new HashSet<Integer>(numHomeDistLocs);
				} else {
					locsRestHomeDist = new HashSet<Integer>();
				}
				while (getLocsByHomeDistQ.next()){
					locsRestHomeDist.add(new Integer(getLocsByHomeDistQ.getInt("locationID")));
				}
				for (int aggPopIndex = 0; aggPopIndex < numLocations[homeDistIndex].length; aggPopIndex++){
					numLocations[homeDistIndex][aggPopIndex] = 0;
					int curMinAggPop = aggPopIndex*aggPopBinRange +  minAggPop;
					int curMaxAggPop = (aggPopIndex+1)*aggPopBinRange +  minAggPop;
					getLocsByAggPop.setDouble(1, curMinAggPop);
					getLocsByAggPop.setDouble(2, curMaxAggPop);
					ResultSet getLocsByAggPopQ = getLocsByAggPop.executeQuery();
					getLocsByAggPopQ.last();
					int numAggPopLocs = getLocsByAggPopQ.getRow();
					getLocsByAggPopQ.beforeFirst();
					HashSet<Integer> locsRestAggPop;
					if (numAggPopLocs > 0){
						locsRestAggPop = new HashSet<Integer>(numAggPopLocs);
					} else {
						locsRestAggPop = new HashSet<Integer>();
					}
					while (getLocsByAggPopQ.next()){
						locsRestAggPop.add(new Integer(getLocsByAggPopQ.getInt("locationID")));
					}
					Iterator<Integer> locsRestAggPopItr = locsRestAggPop.iterator();
					while (locsRestAggPopItr.hasNext()){
						if (locsRestHomeDist.contains(locsRestAggPopItr.next())){
							numLocations[homeDistIndex][aggPopIndex]++;
						}
					}
					System.out.println("Home dist index: "+homeDistIndex+". Agg pop index: "+aggPopIndex+
							". Min Home dist: "+curMinHomeDist+". Max Home dist: "+curMaxHomeDist+
							". Min Agg pop: "+curMinAggPop+". Max Agg pop: "+curMaxAggPop+
							". Num locs: "+numLocations[homeDistIndex][aggPopIndex]);
				}
			}
			con.close();
			for (int homeDistIndex = 0; homeDistIndex < numLocations.length; homeDistIndex++){
				for (int aggPopIndex = 0; aggPopIndex < numLocations[homeDistIndex].length; aggPopIndex++){
					System.out.print(numLocations[homeDistIndex][aggPopIndex]+"\t");
				}
				System.out.println();
			}
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static void printHomeDistVsDegree(int percentile){
		HashMap<Integer, Double> distances = new HashMap<Integer,Double>(numLocations);
		HashMap<Integer,Integer> outDegree = new HashMap<Integer,Integer>(numLocations);
		try {
			Connection con = dbConnect();
			Statement getHomeDist = con.createStatement();
			Statement getOutDeg = con.createStatement();
			ResultSet getHomeDistQ = getHomeDist.executeQuery(
					"SELECT locationID,distance FROM "+locHomeDistPercentileTbl+
					" WHERE percentile="+percentile+" ORDER BY distance DESC");
			while (getHomeDistQ.next()){
				distances.put(new Integer(getHomeDistQ.getInt("locationID")), new Double(getHomeDistQ.getDouble("distance")));
			}
			getHomeDistQ.close();
			ResultSet getOutDegQ = getOutDeg.executeQuery(
					"SELECT locationID,outbound FROM "+llCountTbl+" ORDER BY outbound DESC");
			while (getOutDegQ.next()){
				outDegree.put(new Integer(getOutDegQ.getInt("locationID")), new Integer(getOutDegQ.getInt("outbound")));
			}
			con.close();
			Iterator<Integer> locItr = distances.keySet().iterator();
			while(locItr.hasNext()){
				Integer location = locItr.next();
				System.out.println(outDegree.get(location)+"\t"+distances.get(location));
			}
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	/**
	 * Prints to standard out the out degree of a location followed by
	 * the weighted mean social score of the people who visit the location
	 * on each line.
	 * 
	 * weighted mean social score is the total number of contacts the person
	 * has in the corse of the entire simulation weighted by the number of 
	 * times the person visits the location.
	 *
	 */
	public static void printlocOutDegVsWheightedMeanPersonSocScore(){
		HashMap<Integer,Integer> outDegree = new HashMap<Integer,Integer>(numLocations);
		HashMap<Integer,Double> meanSoc = new HashMap<Integer,Double>(numLocations);
		try {
			Connection con = dbConnect();
			Statement getOutDeg = con.createStatement();
			ResultSet getOutDegQ = getOutDeg.executeQuery(
					"SELECT locationID,outbound FROM "+llCountTbl+" ORDER BY outbound DESC");
			while (getOutDegQ.next()){
				outDegree.put(new Integer(getOutDegQ.getInt("locationID")), new Integer(getOutDegQ.getInt("outbound")));
			}
			getOutDegQ.close();
			PreparedStatement getPeopleAtLoc = con.prepareStatement(
					"SELECT personID FROM "+locPeopleSocRankTbl+" WHERE locationID = ? ORDER BY rank ASC");
			PreparedStatement getPersonSoc = con.prepareStatement(
					"SELECT tally FROM "+EpiSimUtil.conCountTbl+" WHERE personID = ?");
			Iterator<Integer> locs = outDegree.keySet().iterator();
			while(locs.hasNext()){
				double mean = 0.0;
				Integer location = locs.next();
				getPeopleAtLoc.setInt(1, location.intValue());
				ResultSet getPeopleAtLocQ = getPeopleAtLoc.executeQuery();
				while(getPeopleAtLocQ.next()){
					int person = getPeopleAtLocQ.getInt("personID");
					getPersonSoc.setInt(1, person);
					ResultSet getPersonSocQ = getPersonSoc.executeQuery();
					if (getPersonSocQ.first()) mean += (double)getPersonSocQ.getInt("tally");
				}
				getPeopleAtLocQ.last();
				if (getPeopleAtLocQ.getRow() > 0) mean /= (double)getPeopleAtLocQ.getRow();
				meanSoc.put(location, mean);
			}
			con.close();
			Iterator<Integer> locItr = outDegree.keySet().iterator();
			while(locItr.hasNext()){
				Integer location = locItr.next();
				System.out.println(outDegree.get(location)+"\t"+meanSoc.get(location));
			}
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	/**
	 * Prints to standard out the out degree of a location followed by
	 * the weighted mean social score of the people who visit the location
	 * on each line.
	 * 
	 * weighted mean social score is the total number of contacts the person
	 * has in the corse of the entire simulation weighted by the number of 
	 * times the person visits the location.
	 *
	 */
	public static void printlocOutDegVsNthPercentilePersonSocScore(int percentile){
		HashMap<Integer,Integer> outDegree = new HashMap<Integer,Integer>(numLocations);
		HashMap<Integer,Integer> soc = new HashMap<Integer,Integer>(numLocations);
		try {
			Connection con = dbConnect();
			Statement getOutDeg = con.createStatement();
			ResultSet getOutDegQ = getOutDeg.executeQuery(
					"SELECT locationID,outbound FROM "+llCountTbl+" ORDER BY outbound DESC");
			while (getOutDegQ.next()){
				outDegree.put(new Integer(getOutDegQ.getInt("locationID")), new Integer(getOutDegQ.getInt("outbound")));
			}
			getOutDegQ.close();
			PreparedStatement getPeopleAtLoc = con.prepareStatement(
					"SELECT tally FROM "+locPeopleSocRankTbl+" WHERE locationID = ? ORDER BY rank DESC");
			Iterator<Integer> locs = outDegree.keySet().iterator();
			while(locs.hasNext()){
				Integer location = locs.next();
				getPeopleAtLoc.setInt(1, location.intValue());
				ResultSet getPeopleAtLocQ = getPeopleAtLoc.executeQuery();
				int numPeople = 0;
				getPeopleAtLocQ.last();
				if (getPeopleAtLocQ.getRow() > 0) numPeople = getPeopleAtLocQ.getRow();
				int score = 0;
				int person = ((numPeople*percentile)/100)+1;
				if (person > 0 && getPeopleAtLocQ.absolute(person)) 
					score = getPeopleAtLocQ.getInt("tally");
				soc.put(location, new Integer(score));
			}
			con.close();
			Iterator<Integer> locItr = outDegree.keySet().iterator();
			while(locItr.hasNext()){
				Integer location = locItr.next();
				System.out.println(outDegree.get(location)+"\t"+soc.get(location));
			}
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static HashMap<Integer,Double> selectLocsByHomeDist(double minDist, double maxDist, int percentile){
		HashMap<Integer, Double> distances = new HashMap<Integer,Double>(1000);
		try {
			Connection con = dbConnect();
			ResultSet getHomeDistQ = con.createStatement().executeQuery(
					"SELECT locationID,distance FROM "+locHomeDistPercentileTbl+
					" WHERE percentile="+percentile+" ORDER BY distance DESC");
			while (getHomeDistQ.next()){
				int location = getHomeDistQ.getInt("locationID");
				double distance = getHomeDistQ.getDouble("distance");
				if (distance >= minDist && distance <= maxDist) 
					distances.put(new Integer(location), new Double(distance));
			}
			con.close();
			return distances;
		} catch (Exception e){
			System.out.println(e);
			return null;
		}
	}
	
	public static void getXYOfHomeDistOutDegClusterByNumLocs(int numLocs, double minDist, double maxDist, int percentile){
		HashSet<Integer> locs = new HashSet<Integer>(numLocs);
		HashMap<Integer,Double> locHomeDist = selectLocsByHomeDist(minDist, maxDist, percentile);
		try {
			Connection con = dbConnect();
			PreparedStatement getXY = con.prepareStatement(
					"SELECT x,y FROM "+locTbl+" WHERE locationID = ?");
			ResultSet getOutDegQ = con.createStatement().executeQuery(
					"SELECT locationID,outbound FROM "+llCountTbl+" ORDER BY outbound DESC");
			while (getOutDegQ.next() && locs.size() < numLocs) {
				int loc = getOutDegQ.getInt("locationID");
				Integer location = new Integer(loc);
				if (locHomeDist.containsKey(location))
					locs.add(location);
			}
			getOutDegQ.close();
			Iterator<Integer> locItr = locs.iterator();
			while (locItr.hasNext()) {
				getXY.setInt(1, locItr.next().intValue());
				ResultSet getXYQ = getXY.executeQuery();
				if (getXYQ.first()) {
					double x = getXYQ.getDouble("x");
					double y = getXYQ.getDouble("y");
					System.out.println(x+"\t"+y);
				}
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void printXYOfHomeDistOutDegCluster(int minDeg, double minDist, double maxDist, int percentile){
		System.out.println("Min out deg: "+minDeg+"; min dist: "+minDist+"; max dist: "+maxDist+"; percentile: "+percentile);
		HashSet<Integer> locs = new HashSet<Integer>(1000);
		HashMap<Integer,Double> locHomeDist = selectLocsByHomeDist(minDist, maxDist, percentile);
		try {
			Connection con = dbConnect();
			PreparedStatement getXY = con.prepareStatement(
					"SELECT x,y FROM "+locTbl+" WHERE locationID = ?");
			ResultSet getOutDegQ = con.createStatement().executeQuery(
					"SELECT locationID,outbound FROM "+llCountTbl+
					" WHERE outbound >= "+minDeg+" ORDER BY outbound DESC");
			while (getOutDegQ.next()) {
				int loc = getOutDegQ.getInt("locationID");
				Integer location = new Integer(loc);
				if (locHomeDist.containsKey(location))
					locs.add(location);
			}
			getOutDegQ.close();
			Iterator<Integer> locItr = locs.iterator();
			while (locItr.hasNext()) {
				getXY.setInt(1, locItr.next().intValue());
				ResultSet getXYQ = getXY.executeQuery();
				if (getXYQ.first()) {
					double x = getXYQ.getDouble("x");
					double y = getXYQ.getDouble("y");
					System.out.println(x+"\t"+y);
				}
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void printHomeDistVsDegreeRestByNthPerSocScore(int minLocOutDeg, int minSocScore, int socPercentile, int homeDistPercentile){
		System.out.println("Min loc out deg: "+minLocOutDeg+"; Min Soc score: "+minSocScore+"; Sociability percentile: "+socPercentile+"; Home dist percentile: "+homeDistPercentile);
		HashMap<Integer, Double> distances = new HashMap<Integer,Double>(numLocations);
		HashMap<Integer,Integer> outDegree = new HashMap<Integer,Integer>(numLocations);
		HashMap<Integer,Integer> soc = new HashMap<Integer,Integer>(numLocations);
		try {
			Connection con = dbConnect();
			ResultSet getHomeDistQ = con.createStatement().executeQuery(
					"SELECT locationID,distance FROM "+locHomeDistPercentileTbl+
					" WHERE percentile="+homeDistPercentile+" ORDER BY distance DESC");
			while (getHomeDistQ.next()){
				distances.put(new Integer(getHomeDistQ.getInt("locationID")), new Double(getHomeDistQ.getDouble("distance")));
			}
			getHomeDistQ.close();
			Statement getOutDeg = con.createStatement();
			ResultSet getOutDegQ = getOutDeg.executeQuery(
					"SELECT locationID,outbound FROM "+llCountTbl+" ORDER BY outbound DESC");
			while (getOutDegQ.next()){
				outDegree.put(new Integer(getOutDegQ.getInt("locationID")), new Integer(getOutDegQ.getInt("outbound")));
			}
			getOutDegQ.close();
			PreparedStatement getPeopleAtLoc = con.prepareStatement(
					"SELECT tally FROM "+locPeopleSocRankTbl+" WHERE locationID = ? ORDER BY rank DESC");
			Iterator<Integer> locs = outDegree.keySet().iterator();
			while(locs.hasNext()){
				Integer location = locs.next();
				getPeopleAtLoc.setInt(1, location.intValue());
				ResultSet getPeopleAtLocQ = getPeopleAtLoc.executeQuery();
				int numPeople = 0;
				getPeopleAtLocQ.last();
				if (getPeopleAtLocQ.getRow() > 0) numPeople = getPeopleAtLocQ.getRow();
				int score = 0;
				int person = ((numPeople*socPercentile)/100)+1;
				if (person > 0 && getPeopleAtLocQ.absolute(person)) 
					score = getPeopleAtLocQ.getInt("tally");
				soc.put(location, new Integer(score));
			}
			con.close();
			Iterator<Integer> locItr = outDegree.keySet().iterator();
			while(locItr.hasNext()){
				Integer location = locItr.next();
				if (soc.get(location) >= minSocScore && outDegree.get(location) >= minLocOutDeg)
					System.out.println(outDegree.get(location)+"\t"+distances.get(location));
			}
		} catch (Exception e){
			System.out.println(e);
		}
	}
	
	public static HashMap<Integer, Double> locationsRestByHomeDist(double minHomeDist, double maxHomeDist, int percentile) {
		HashMap<Integer, Double> distances = new HashMap<Integer,Double>(numLocations);
		try {
			Connection con = dbConnect();
			ResultSet getHomeDistQ = con.createStatement().executeQuery(
					"SELECT locationID,distance FROM "+locHomeDistPercentileTbl+
					" WHERE percentile="+percentile+" AND distance >= "+minHomeDist+" AND distance <= "+maxHomeDist+" ORDER BY distance DESC");
			while (getHomeDistQ.next()){
				distances.put(new Integer(getHomeDistQ.getInt("locationID")), new Double(getHomeDistQ.getDouble("distance")));
			}
			con.close();
			return distances;
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}
	
	public static void peopleCorrVarVaccStrat(int numVaccines) {
		String[] strategyNames = {"Most Sociable",
				"20% Most Sociable People at highest Degree Locations",
				"40% Most Sociable People at highest Degree Locations",
				"20% Most Sociable People at highest Degree Work Locations",
				"40% Most Sociable People at highest Degree Work Locations",
				"20% Most Sociable People at highest Degree School Locations",
				"40% Most Sociable People at highest Degree School Locations",
				"20% Most Sociable People at Largest Home Distance Locations",
				"40% Most Sociable People at Largest Home Distance Locations",
//				"20% Most Sociable People at Largest Home Distance Work Locations",
//				"40% Most Sociable People at Largest Home Distance Work Locations",
//				"20% Most Sociable People at Largest Home Distance School Locations",
//				"40% Most Sociable People at Largest Home Distance School Locations",
				};
		HashSet[] vaccPeople = new HashSet[strategyNames.length];
		int index = 0;
		VaccinationSimulation sim;
		// Most Sociable
		sim = new VaccinationSimulation();
		sim.vaccinateMostSociable(numVaccines);
		vaccPeople[index++] = sim.getVaccinatedPeople();
		// Frac at High Deg Locations
		// 20%
		sim = new VaccinationSimulation();
		sim.vaccinateFracOfMostSocPeopleAtHighDegLoc(numVaccines, 0.2);
		vaccPeople[index++] = sim.getVaccinatedPeople();
		// 40%
		sim = new VaccinationSimulation();
		sim.vaccinateFracOfMostSocPeopleAtHighDegLoc(numVaccines, 0.4);
		vaccPeople[index++] = sim.getVaccinatedPeople();
		// Frac at High Deg Locations Restricted to Work Activities
		// 20%
		sim = new VaccinationSimulation();
		sim.vaccinateFracOfMostSocPeopleAtHighDegLocRestPurpose(numVaccines, 0.2, 1);
		vaccPeople[index++] = sim.getVaccinatedPeople();
		// 40%
		sim = new VaccinationSimulation();
		sim.vaccinateFracOfMostSocPeopleAtHighDegLocRestPurpose(numVaccines, 0.4, 1);
		vaccPeople[index++] = sim.getVaccinatedPeople();
		// Frac at High Deg Locations Restricted to School Activities
		// 20%
		sim = new VaccinationSimulation();
		sim.vaccinateFracOfMostSocPeopleAtHighDegLocRestPurpose(numVaccines, 0.2, 7);
		vaccPeople[index++] = sim.getVaccinatedPeople();
		// 40%
		sim = new VaccinationSimulation();
		sim.vaccinateFracOfMostSocPeopleAtHighDegLocRestPurpose(numVaccines, 0.4, 7);
		vaccPeople[index++] = sim.getVaccinatedPeople();
		// Frac at Large Home Distance
		// 20%
		sim = new VaccinationSimulation();
		sim.vaccinateFracOfMostSocPeopleAtLargestHomeDist(numVaccines, 0.2);
		vaccPeople[index++] = sim.getVaccinatedPeople();
		// 40%
		sim = new VaccinationSimulation();
		sim.vaccinateFracOfMostSocPeopleAtLargestHomeDist(numVaccines, 0.4);
		vaccPeople[index++] = sim.getVaccinatedPeople();
	}
	
	public static void parseVaccFile(String file) {
		int[] numVaccines = VaccinationSimulation.numberOfVaccinesSpread;
		String[] fractions = {"1.0","0.8","0.6","0.4","0.2"};
		String[][] data = new String[numVaccines.length][fractions.length];
		int row = 0;
		int col = 0;
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line;
			while ((line = in.readLine()) != null) {
				StringTokenizer tok = new StringTokenizer(line);
				if (tok.nextToken().equals("There") && tok.nextToken().equals("are")) {
					String numInfected = tok.nextToken();
					if (tok.nextToken().equals("infected")) {
						if (row < data.length) {
							data[row][col] = numInfected;
							row++;
						}  else {
							row = 0;
							col++;
							data[row][col] = numInfected;
							row++;
						}
					}
				}
			}
			for (int i = 0; i < fractions.length; i++) {
				System.out.print("\t"+fractions[i]);
			}
			System.out.println();
			for (int r = 0; r < data.length; r++) {
				System.out.print(numVaccines[r]);
				for (int c = 0; c < data[r].length; c++){
					System.out.print("\t"+data[r][c]);
				}
				System.out.println();
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void printLocsOfInfectEvent(int startTime, int incTime, int endTime){
		int estNumLocs = 100000;
		HashMap<Integer,Integer> numInfected = new HashMap<Integer,Integer>(estNumLocs);
		int time = startTime;
		try {
			Connection con = dbConnect();
			PreparedStatement getInfEvents = con.prepareStatement(
					"SELECT locationID FROM "+EpiSimUtil.dendroTbl+" WHERE time > ? AND time <= ?");
			while (time <= endTime) {
				getInfEvents.setInt(1, time);
				getInfEvents.setInt(2, time+incTime);
				ResultSet getInfEventsQ = getInfEvents.executeQuery();
				while (getInfEventsQ.next()) {
					Integer location = new Integer(getInfEventsQ.getInt("locationID"));
					if (numInfected.containsKey(location)) {
						numInfected.put(location, numInfected.get(location) + 1);
					} else {
						numInfected.put(location, new Integer(1));
					}
				}
				FileWriter fw = new FileWriter(time+".dat");
				Iterator<Integer> itr = numInfected.keySet().iterator();
				while (itr.hasNext()) {
					Integer location = itr.next();
					double x = getX(location.intValue());
					double y = getY(location.intValue());
					fw.write(x+"\t"+y+"\t"+numInfected.get(location)+"\n");
				}
				time += incTime;
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void printLocsOfInfectEvent(){
		int startTime = 0;
		int incTime = 60*60*24;
		int endTime = 100*incTime;
		int estNumLocs = 100000;
		HashMap<Integer,Integer> numInfected = new HashMap<Integer,Integer>(estNumLocs);
		HashMap<Integer,Double> xs = new HashMap<Integer,Double>(estNumLocs);
		HashMap<Integer,Double> ys = new HashMap<Integer,Double>(estNumLocs);
		int time = startTime;
		try {
			Connection con = dbConnect();
			PreparedStatement getInfEvents = con.prepareStatement(
					"SELECT locationID FROM "+EpiSimUtil.dendroTbl+" WHERE time > ? AND time <= ?");
			while (time <= endTime) {
				getInfEvents.setInt(1, time);
				getInfEvents.setInt(2, time+incTime);
				ResultSet getInfEventsQ = getInfEvents.executeQuery();
				while (getInfEventsQ.next()) {
					Integer location = new Integer(getInfEventsQ.getInt("locationID"));
					if (numInfected.containsKey(location)) {
						numInfected.put(location, numInfected.get(location) + 1);
					} else {
						numInfected.put(location, new Integer(1));
					}
				}
				int day = time / incTime;
				FileWriter fw = new FileWriter(day+".dat");
				Iterator<Integer> itr = numInfected.keySet().iterator();
				while (itr.hasNext()) {
					Integer location = itr.next();
					double x,y;
					if (!xs.containsKey(location)) {
						xs.put(location, getX(location.intValue()));
						ys.put(location, getY(location.intValue()));
					}
					x = xs.get(location);
					y = ys.get(location);
					fw.write(x+"\t"+y+"\t"+numInfected.get(location)+"\n");
				}
				fw.close();
				time += incTime;
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void printCorInfEventsToLLInterCount(){
		int startTime = 0;
		int incTime = 5*(60*60*24);
		int endTime = 100*60*60*24;
		int estNumLocs = 100000;
		HashMap<Integer,Integer> aggPop = new HashMap<Integer,Integer>(estNumLocs);
		HashMap<Integer,Integer> numInfected = new HashMap<Integer,Integer>(estNumLocs);
//		HashMap<Integer,Double> xs = new HashMap<Integer,Double>(estNumLocs);
//		HashMap<Integer,Double> ys = new HashMap<Integer,Double>(estNumLocs);
		int time = startTime;
		try {
			Connection con = dbConnect();
			PreparedStatement getInfEvents = con.prepareStatement(
					"SELECT locationID FROM "+EpiSimUtil.dendroTbl+" WHERE time > ? AND time <= ?");
			PreparedStatement getLLInterCount = con.prepareStatement(
					"SELECT tally FROM "+EpiSimUtil.llInterCountTbl+
					" WHERE location1ID = ? AND location2ID = ?");
			PreparedStatement getAggPop = con.prepareStatement(
					"SELECT aggregatePop FROM "+EpiSimUtil.locTbl+" WHERE locationID = ?");
			while (time <= endTime) {
				getInfEvents.setInt(1, time);
				getInfEvents.setInt(2, time+incTime);
				ResultSet getInfEventsQ = getInfEvents.executeQuery();
				while (getInfEventsQ.next()) {
					Integer location = new Integer(getInfEventsQ.getInt("locationID"));
					if (numInfected.containsKey(location)) {
						numInfected.put(location, numInfected.get(location) + 1);
					} else {
						numInfected.put(location, new Integer(1));
					}
					if (!aggPop.containsKey(location)) {
						getAggPop.setInt(1, location.intValue());
						ResultSet getAggPopQ = getAggPop.executeQuery();
						if (getAggPopQ.first()) {
							Integer pop = new Integer(getAggPopQ.getInt("aggregatePop"));
							aggPop.put(location, pop);
						}
					}
				}
				int day = time / (60*60*24);
				FileWriter fw = new FileWriter(day+".dat");
				Iterator<Integer> itr = numInfected.keySet().iterator();
				while (itr.hasNext()) {
					Integer location = itr.next();
					Iterator<Integer> itr2 = numInfected.keySet().iterator();
					while (itr2.hasNext()) {
						Integer location2 = itr2.next();
						getLLInterCount.setInt(1, location.intValue());
						getLLInterCount.setInt(2, location2.intValue());
						ResultSet getLLInterCountQ = getLLInterCount.executeQuery();
						if (getLLInterCountQ.first()) {
							int tally = getLLInterCountQ.getInt("tally");
							double del = Math.abs(
									numInfected.get(location).doubleValue()/aggPop.get(location).doubleValue()
									-  numInfected.get(location2).doubleValue()/aggPop.get(location2).doubleValue());
							fw.write(tally+"\t"+del+"\n");
						}
					}
//					double x,y;
//					if (!xs.containsKey(location)) {
//						xs.put(location, getX(location.intValue()));
//						ys.put(location, getY(location.intValue()));
//					}
//					x = xs.get(location);
//					y = ys.get(location);
//					fw.write(x+"\t"+y+"\t"+numInfected.get(location)+"\n");
				}
				fw.close();
				time += incTime;
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void template(){
		try {
			Connection con = dbConnect();
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

}
