package dbepi2a;

import java.util.*;

public class DirSampleMonAgent implements LocationMonitorAgent {
	private MonBuildClose myagent;
	private int nonzerocount = 0;
	private int nummoncon = 0;
	private double relBaseWeight = 0.1;
	
	public DirSampleMonAgent() {
		System.out.println("Relative Base Weight:\t"+this.relBaseWeight);
	}

	public double[] calcWeights() {
		double[] weights = new double[this.myagent.trackLocsByIndex.length];
//		System.out.println(this.myagent.monitoredLocs.size());
		for (int i=0; i < this.myagent.trackLocsByIndex.length; i++) {
//			System.out.println("i\t"+i);
			weights[i] = this.expInfInc(this.myagent.trackLocsByIndex[i]) + this.baseWeight();
//			weights[i] = this.baseWeight();
		}
//		System.out.println("Num non-zero predictions:\t"+this.nonzerocount);
//		System.out.println("Num mon con:\t"+this.nummoncon);
		weights = EpiSimUtil.normalize(weights);
		for (int i=0; i < weights.length; i++)
			weights[i] = (1-this.relBaseWeight)*weights[i] + this.relBaseWeight/(double)weights.length;
		return weights;
	}
	
	public double[] calcWeights(double[] oldWeights) {
		return calcWeights();
	}

	public void setClosingAgent(MonBuildClose agent) {
		this.myagent = agent;
	}
	
	private double baseWeight() {
//		return this.basePopInf();
		return 0.0;
	}
	
	private double basePopInf() {
		return (double)myagent.mySim.infections.size() / (double)EpiSimUtil.population;
	}
	
	private double expInfInc(Integer location) {
		TreeMap<Integer,Integer> conlocs = this.myagent.getLocCon(location);
		Iterator<Integer> itr = conlocs.keySet().iterator();
		double num = 0.0;
		double denom = 0.0;
		double aggNeighborPop = 0.0;
		while (itr.hasNext()) {
			Integer conloc = itr.next();
			aggNeighborPop += this.myagent.getAggPop(conloc);
			if ( this.myagent.monitoredLocs.contains(conloc) || this.myagent.isClosed(conloc, this.myagent.mySim.curTime) ) {
//				num += this.myagent.fracInfected(conloc) * conlocs.get(conloc).doubleValue(); //use for I
				num += this.myagent.numInfected(conloc) * conlocs.get(conloc).doubleValue();  //use for IP
				denom += conlocs.get(conloc).doubleValue();
				this.nummoncon++;
			}
		}
		if (this.myagent.monitoredLocs.contains(location))
//			return (double) this.myagent.numInfected(location); //use for I
//			return (double) this.myagent.numInfected(location) * this.myagent.getAggPop(location); //use for IP
			return (double) aggNeighborPop*this.myagent.numInfected(location) * this.myagent.getAggPop(location); //use for IP & influence
		if (denom == 0.0)
			return 0.0;
		this.nonzerocount++;
		num = num*aggNeighborPop; // use for simple influence measure
		return num / denom;
	}
	
	/////////////////////////////////////// STATIC //////////////////////////////////////////
	
	public static void main(String[] args) {
//		testStoreLocPop();
	}
	
	public static void testStoreLocPop() {
		HashMap<Integer,Integer> locations = EpiSimUtil.getLocsHighOutDeg(10000);
		HashMap<Integer,HashSet<Integer>> pop = new HashMap<Integer,HashSet<Integer>>(10000);
		Iterator<Integer> locItr = locations.keySet().iterator();
		while (locItr.hasNext()) {
			Integer loc = locItr.next();
			pop.put(loc, EpiSimUtil.getPeopleAtLocation(loc));
		}
	}

}
