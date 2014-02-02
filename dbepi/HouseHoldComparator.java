package dbepi;

import java.util.*;

public class HouseHoldComparator implements Comparator<Integer> {
	public java.util.HashMap<Integer,Integer> houseHoldScores;
	
	public HouseHoldComparator(java.util.HashMap<Integer,Integer> map){
		this.houseHoldScores = map;
	}
	
	public int compare(Integer one,Integer two) {
		Integer oneScore = houseHoldScores.get(one);
		Integer twoScore = houseHoldScores.get(two);
		int comp = -1*oneScore.compareTo(twoScore);
		if (comp != 0) return comp;
		return one.compareTo(two);
	}

}
