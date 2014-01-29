package dbepi2a;

import java.util.*;

public class StopWatch {
	
	private HashMap<Integer,Long> cumTime = new HashMap<Integer,Long>();
	private HashMap<Integer,Long> startTime = new HashMap<Integer,Long>();

	public StopWatch() {
		// TODO Auto-generated constructor stub
	}
	
	public long start(int id) {
		long time = System.nanoTime();
		this.startTime.put(new Integer(id), new Long(time));
		return time;
	}
	
	public long stop(int id) {
		Integer ido = new Integer(id);
		long endTime = System.nanoTime();
		if (!this.startTime.containsKey(ido))
			return 0L;
		long time = endTime - this.startTime.get(ido);
		EpiSimUtil.incMapValue(cumTime, ido, time);
		return time;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
