package dbepi;
import java.sql.*;
import java.util.*;

public class BufferedBigIntSet {
	private static final String tbl = "int_set";
	private static final String setIdField = "setID";
	private static final String elemField = "element";
	private static final String add = "INSERT IGNORE INTO "+tbl+" ("+elemField+
											","+setIdField+")"+" VALUES (?,?)";
	private static final String find = "SELECT "+elemField+" FROM "+tbl+
											" WHERE "+elemField+" = ? AND "+setIdField+" = ?";
	
	private static final int defaultBufferSize = 1000;
	
//	private static int nextID = 1;
	
	private int setId;
	private PreparedStatement addStmt;
	private PreparedStatement findStmt;
	private int bufferSize;
	private HashSet<Integer> buffer;
	private boolean useDB;

	public BufferedBigIntSet() {
		this.setId = -1;//getNewSetId();
		this.bufferSize = defaultBufferSize;
		this.useDB = false;
		this.buffer = new HashSet<Integer>();
		try {
			Connection con = getDB();
			this.addStmt = con.prepareStatement(add);
			this.addStmt.setInt(2, this.setId);
			this.findStmt = con.prepareStatement(find);
			this.findStmt.setInt(2, this.setId);
		} catch (Exception e) {
			System.out.println("Unable to initiate add statement.");
			System.out.println(e);
		}
	}
	
	public BufferedBigIntSet(int bufferSize) {
		this.setId = -1;//getNewSetId();
		this.bufferSize = bufferSize;
		this.useDB = false;
		this.buffer = new HashSet<Integer>();
		try {
			Connection con = getDB();
			this.addStmt = con.prepareStatement(add);
			this.addStmt.setInt(2, this.setId);
			this.findStmt = con.prepareStatement(find);
			this.findStmt.setInt(2, this.setId);
		} catch (Exception e) {
			System.out.println("Unable to initiate add statement.");
			System.out.println(e);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BufferedBigIntSet set1 = new BufferedBigIntSet();
		for (int i=0; i < 600; i++) {
			set1.add(new Integer(i));
		}
		BufferedBigIntSet set2 = new BufferedBigIntSet();
		System.out.println("Set1 has "+set1.size()+" elements");
		for (int i=0; i < 600; i++) {
			set2.add(new Integer(i));
		}
		System.out.println("Set2 has "+set2.size()+" elements");
		for (int i=600; i < 1100; i++) {
			set1.add(new Integer(i));
		}
		for (int i=600; i < 1100; i++) {
			set2.add(new Integer(i));
		}
		System.out.println("Set2 has "+set2.size()+" elements");
		System.out.println("Set1 has "+set1.size()+" elements");
		for (int i=0; i < 50; i++) {
			set1.add(new Integer(i));
		}
		System.out.println("Set1 has "+set1.size()+" elements");
		System.out.println("Set1 contains the integer 1050: "+set1.contains(new Integer(1050)));
		System.out.println("Set2 contains the integer 950: "+set1.contains(new Integer(950)));

	}
	
//	private static int getNewSetId() {
//		return nextID++;
//	}
	
	private static int getNewSetId() {
		int newId = -1;
		try {
			Connection con = getDB();
			ResultSet getMaxSetIdQ = con.createStatement().executeQuery(
					"SELECT MAX("+setIdField+") AS max FROM "+tbl);
			if (getMaxSetIdQ.first()) newId = getMaxSetIdQ.getInt("max") + 1;
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return newId;
	}
	
	private static Connection getDB() {
		return EpiSimUtil.dbGenDataConnect();
	}
	
	public int size() {
		if (!this.useDB) return this.buffer.size();
		this.updateDB();
		return getNumRows();
	}
	
	public int getNumRows() {
		try {
			Connection con = getDB();
			ResultSet getNumRowsQ = con.createStatement().executeQuery(
					"SELECT COUNT(*) FROM "+tbl+" WHERE "+setIdField+"="+this.setId);
			if (getNumRowsQ.first()) return getNumRowsQ.getInt(1);
			con.close();
		} catch (Exception e) {
			System.out.println("Can't get number of rows this set takes up.");
			System.out.println(e);
		}
		return -1;
	}
	
	public void add(Integer elem) {
		if (this.buffer.size() < this.bufferSize) this.buffer.add(elem);
		else if (this.buffer.add(elem)) this.updateDB();
	}
	
	private void updateDB() {
		try {
			if (this.setId == -1) {
				this.setId = getNewSetId();
				this.addStmt.setInt(2, this.setId);
				this.findStmt.setInt(2, this.setId);
			}
			Iterator<Integer> elemsItr = this.buffer.iterator();
			while (elemsItr.hasNext()) {
				int elem = elemsItr.next().intValue();
				this.addStmt.setInt(1, elem);
				this.addStmt.addBatch();
			}
			this.addStmt.executeBatch();
			this.buffer = new HashSet<Integer>();
			this.useDB = true;
		} catch (Exception e) {
			System.out.println("Unable to execute update of int set db.");
			System.out.println(e);
		}
	}
	
	public boolean contains(Integer elem) {
		boolean inBuffer = this.buffer.contains(elem);
		if (inBuffer) return true;
		else if (!this.useDB) return inBuffer;
		else return inDB(elem);
	}
	
	private boolean inDB(Integer elem) {
		try {
			this.findStmt.setInt(1, elem.intValue());
			ResultSet findQ = this.findStmt.executeQuery();
			if (findQ.first()) return true;
		} catch (Exception e) {
			System.out.println(e);
		}
		return false;
	}
	
	public Iterator<Integer> iterator() {
		if (!this.useDB) return this.buffer.iterator();
		return new BufferedBigIntSetItr(this);
	}
	
	public class BufferedBigIntSetItr implements Iterator<Integer> {
		private static final String getNextResultSetQuery = 
			"SELECT "+BufferedBigIntSet.elemField+" FROM "+BufferedBigIntSet.tbl+
			" WHERE "+BufferedBigIntSet.setIdField+" = ? LIMIT ?,?";
		private static final int prepStmtParamNum = 2;
		
		private BufferedBigIntSet mySet;
		//private Connection con;
		private PreparedStatement getNextResultSetStmt;
		private int nextRowIndexCurResults;
		private int bufferSize;
		private int nextResultsOffset;
		private ResultSet curResults;
		
		public BufferedBigIntSetItr(BufferedBigIntSet mySet) {
			this.mySet = mySet;
			//this.con = this.mySet.getDB();
			this.bufferSize = this.mySet.bufferSize;
			try {
				this.getNextResultSetStmt = BufferedBigIntSet.getDB().prepareStatement(getNextResultSetQuery);
				this.getNextResultSetStmt.setInt(1, this.mySet.setId);
				this.getNextResultSetStmt.setInt(3, this.bufferSize);
			} catch (Exception e) {
				System.out.println("Unable to get iterator prepared statement");
				System.out.println(e);
			}
			this.mySet.updateDB();
			this.nextResultsOffset = 0;
			this.getNextResultSet();
		}
		
		private void getNextResultSet() {
			try {
				this.getNextResultSetStmt.setInt(prepStmtParamNum, nextResultsOffset);
				this.curResults = this.getNextResultSetStmt.executeQuery();
				this.nextRowIndexCurResults = 1;
				this.nextResultsOffset += this.bufferSize;
			} catch (Exception e) {
				System.out.println("Problem getting next result set in iterator.");
				System.out.println(e);
			}
		}
		
		public boolean hasNext() {
			try {
				if (curResults.absolute(this.nextRowIndexCurResults)) return true;
				this.getNextResultSet();
				if (curResults.absolute(this.nextRowIndexCurResults)) return true;
			} catch (Exception e) {
				System.out.println("Problem with iterators hasNext() method.");
				System.out.println(e);
			}
			return false;
		}
		
		public Integer next() {
			try {
				if (this.curResults.absolute(this.nextRowIndexCurResults)) {
					this.nextRowIndexCurResults++;
					return new Integer(this.curResults.getInt(BufferedBigIntSet.elemField));
				} else {
					this.getNextResultSet();
					if (this.curResults.absolute(this.nextRowIndexCurResults)) {
						this.nextRowIndexCurResults++;
						return new Integer(this.curResults.getInt(BufferedBigIntSet.elemField));
					}
				}
			} catch (Exception e) {
				System.out.println("Problem with iterators next() method.");
				System.out.println(e);
			}
			return null;
		}
		
		public void remove() {
			return;
		}
		
	}

}
