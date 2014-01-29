package dbepi2a;

import java.util.*;

public class HarmonicAvgLabels implements LocationMonitorAgent {
	private static final double epsilon = 1.0E-1;
	private static final boolean test = false;
	private static final boolean infPop = false;
	
	private MonBuildClose myagent;
	private double relBaseWeight = 0.01;
	public HashMap<Integer,Double> test_labels;
	public HashMap<Integer,HashMap<Integer,Double>> test_weights;
	
//	private double[] oldWeights;
//	private double[] newWeights;

	public HarmonicAvgLabels() {
		System.out.println("Harmonic labels |I|*|P|:\t"+infPop);
	}

	public double[] calcWeights() {
		HashMap<Integer,Double> oldLabels = this.getInitLabels();
		
		HashMap<Integer,Double> newLabels = this.calcNewLabels(oldLabels);
		double diff = getDiff(oldLabels, newLabels);
		oldLabels = newLabels;
		int iterations = 0;
		while (diff > epsilon && iterations < 100) {
			newLabels = this.calcNewLabels(oldLabels);
			diff = getDiff(oldLabels, newLabels);
			oldLabels = newLabels;
			iterations++;
		}
		System.out.println("iterations to converge:\t"+iterations+"\tMagnitude at convergence:\t"+getMag(oldLabels)+"\tdiff:\t"+diff);
		double[] weights = new double[this.myagent.trackLocsByIndex.length];
		for (int i=0; i < weights.length; i++) {
			weights[i] = oldLabels.get(this.myagent.trackLocsByIndex[i]);
		}
		weights = EpiSimUtil.normalize(weights);
		for (int i=0; i < weights.length; i++) {
			weights[i] = (1-this.relBaseWeight)*weights[i] + this.relBaseWeight*(1.0/(double)weights.length);
		}
		return weights;
	}
	
	public double[] calcWeights(double[] oldWeights) {
		if (oldWeights.length == 0)
			return this.calcWeights();
		HashMap<Integer,Double> oldLabels = this.getInitLabels(oldWeights);
		
		HashMap<Integer,Double> newLabels = this.calcNewLabels(oldLabels);
		double diff = getDiff(oldLabels, newLabels);
		oldLabels = newLabels;
		int iterations = 0;
		while (diff > epsilon) {
			newLabels = this.calcNewLabels(oldLabels);
			diff = getDiff(oldLabels, newLabels);
			oldLabels = newLabels;
			iterations++;
		}
		System.out.println("iterations to converge:\t"+iterations);
		double[] weights = new double[this.myagent.trackLocsByIndex.length];
		for (int i=0; i < weights.length; i++) {
			weights[i] = oldLabels.get(this.myagent.trackLocsByIndex[i]);
		}
		weights = EpiSimUtil.normalize(weights);
		for (int i=0; i < weights.length; i++) {
			weights[i] = (1-this.relBaseWeight)*weights[i] + this.relBaseWeight*(1.0/(double)weights.length);
		}
		return weights;
	}

	public void setClosingAgent(MonBuildClose agent) {
		this.myagent = agent;
	}
	
	public HashMap<Integer,Double> getInitLabels() {
		HashMap<Integer,Double> labels = new HashMap<Integer,Double>(this.myagent.trackLocations.size());
		Iterator<Integer> locs = this.myagent.trackLocations.keySet().iterator();
		while (locs.hasNext()) {
			Integer loc = locs.next();
			if (this.isLabeled(loc))
				labels.put(loc, this.getLabel(loc));
			else
				labels.put(loc, this.unlabeledInitLabel(loc));
		}
		return labels;
	}
	
	public HashMap<Integer,Double> getInitLabels(double[] oldWeights) {
		HashMap<Integer,Double> labels = new HashMap<Integer,Double>(this.myagent.trackLocations.size());
//		Iterator<Integer> locs = this.myagent.trackLocations.keySet().iterator();
		for (int i=0; i < this.myagent.trackLocsByIndex.length; i++) {
			Integer loc = this.myagent.trackLocsByIndex[i];
			if (this.isLabeled(loc))
				labels.put(loc, this.getLabel(loc));
			else {
				if (oldWeights[i] > 0.0)
					labels.put(loc, oldWeights[i]);
				else
					labels.put(loc, this.unlabeledInitLabel(loc));
			}
		}
		return labels;
	}
	
	public Double unlabeledInitLabel(Integer vertex) {
		if (infPop)
			return new Double(this.basePopInf()*this.myagent.getAggPop(vertex)*this.myagent.getAggPop(vertex)); //PI
		return new Double(this.basePopInf()*this.myagent.getAggPop(vertex)*this.myagent.getAggPop(vertex)); //I
	}
	
	private double basePopInf() {
		return (double)myagent.mySim.infections.size() / (double)EpiSimUtil.population;
	}
	
	public HashMap<Integer,Double> calcNewLabels(HashMap<Integer,Double> oldLabels) {
		HashMap<Integer,Double> newLabels = new HashMap<Integer,Double>(oldLabels.size());
		Iterator<Integer> vertices = oldLabels.keySet().iterator();
		while (vertices.hasNext()) {
			Integer vertex = vertices.next();
			if (this.isLabeled(vertex)) {
				newLabels.put(vertex, this.getLabel(vertex));
			} else {
				HashMap<Integer,Double> weights = this.getNeighbors(vertex);
				Iterator<Integer> neighbors = weights.keySet().iterator();
				double numerator = 0.0;
				double denominator = 0.0;
				while (neighbors.hasNext()) {
					Integer neighbor = neighbors.next();
					if (oldLabels.containsKey(neighbor)) {
						numerator += oldLabels.get(neighbor) * weights.get(neighbor);
						denominator += weights.get(neighbor);
					}
				}
				newLabels.put(vertex, new Double(numerator / denominator));
			}
		}
		return newLabels;
	}
	
	private boolean isLabeled(Integer vertex) {
		if (test) return this.test_isLabeled(vertex);
		return this.myagent.monitoredLocs.contains(vertex) || this.myagent.isClosed(vertex, this.myagent.mySim.curTime);
	}
	
	private Double getLabel(Integer vertex) {
		if (test) return this.test_getLabel(vertex);
		if (infPop)
			return new Double(this.myagent.numInfected(vertex)*this.myagent.getAggPop(vertex)); //PI
		return new Double(this.myagent.fracInfected(vertex)*this.myagent.getAggPop(vertex)); //I
	}
	
	private HashMap<Integer,Double> getNeighbors(Integer vertex) {
		if (test) return this.test_weights.get(vertex);
		TreeMap<Integer,Integer> conlocs = this.myagent.getLocCon(vertex);
		HashMap<Integer,Double> neighbors = new HashMap<Integer,Double>(conlocs.size());
		Iterator<Integer> itr = conlocs.keySet().iterator();
		while (itr.hasNext()) {
			Integer next = itr.next();
			neighbors.put(next, new Double(conlocs.get(next)));
		}
		return neighbors;
	}
	
	private boolean test_isLabeled(Integer vertex) {
		return this.test_labels.containsKey(vertex);
	}
	
	private Double test_getLabel(Integer vertex) {
		return this.test_labels.get(vertex);
	}
	
	/////////////////////////////////////// STATIC ////////////////////////////////

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		randTest();

	}
	
	public static double getDiff(HashMap<Integer,Double> x, HashMap<Integer,Double> y) {
		Iterator<Integer> itr = x.keySet().iterator();
		double diff = 0.0;
//		double xmag = 0.0;
		while (itr.hasNext()) {
			Integer index = itr.next();
//			System.out.println(x.get(index)+"\t"+y.get(index));
//			xmag += x.get(index) * x.get(index);
			diff += (x.get(index) - y.get(index))*(x.get(index) - y.get(index));
//			System.out.println(diff);
		}
		return Math.sqrt(diff);
	}
	
	public static double getMag(HashMap<Integer,Double> x) {
		Iterator<Integer> itr = x.keySet().iterator();
		double mag = 0.0;
		while (itr.hasNext()) {
			Integer index = itr.next();
			mag += x.get(index) * x.get(index);
		}
		return Math.sqrt(mag);
	}
	
//	public static double getDiff(HashMap<Integer,Double> x, HashMap<Integer,Double> y) {
//		Iterator<Integer> itr = x.keySet().iterator();
//		double diff = 0.0;
//		double xmag = 0.0;
//		while (itr.hasNext()) {
//			Integer index = itr.next();
////			System.out.println(x.get(index)+"\t"+y.get(index));
//			xmag += x.get(index) * x.get(index);
//			diff += (x.get(index) - y.get(index))*(x.get(index) - y.get(index));
////			System.out.println(diff);
//		}
//		return Math.sqrt(diff) / Math.sqrt(xmag);
//	}
	
	public static void printMap(HashMap map) {
		Iterator itr = map.keySet().iterator();
		while (itr.hasNext()) {
			Object next = itr.next();
			System.out.print(next+":"+map.get(next)+"\t");
		}
		System.out.println();
	}
	
	public static void randTest() {
		double labelCeil = 10.0;
		Double unLabel = new Double(1.0);
		double fracCon = 0.3;
		int numVertices = 10;
		int numLabels = 3;
		Random rand = new Random(1847860);
		HashMap<Integer,Double> ls = new HashMap<Integer,Double>(numLabels);
		for (int i=0; i < numLabels; ) {
			Integer newl = new Integer(rand.nextInt(numVertices));
			if (!ls.containsKey(newl)) {
				ls.put(newl, new Double (rand.nextDouble()*labelCeil));
				i++;
			}
		}
		HashMap<Integer,Double> allLabels = new HashMap<Integer,Double>(numVertices);
		HashMap<Integer,HashMap<Integer,Double>> weights = new HashMap<Integer,HashMap<Integer,Double>>(numVertices);
		for (int i=0; i < numVertices; i++) {
			Integer me = new Integer(i);
			HashMap<Integer,Double> neighbors = new HashMap<Integer,Double>();
			for (int j=0; j < numVertices; j++) {
				if (rand.nextDouble() < fracCon) {
					neighbors.put(new Integer(j), new Double(rand.nextDouble()));
				}
			}
			weights.put(me, neighbors);
			if (ls.containsKey(me)) {
				allLabels.put(me, ls.get(me));
			} else {
				allLabels.put(me, unLabel);
			}
		}
		
		HarmonicLabels harm = new HarmonicLabels();
		harm.test_labels = ls;
		harm.test_weights = weights;
		HashMap<Integer,Double> oldLabels = allLabels;
		printMap(oldLabels);
		
		HashMap<Integer,Double> newLabels = harm.calcNewLabels(oldLabels);
		double diff = getDiff(oldLabels, newLabels);
		oldLabels = newLabels;
		printMap(newLabels);
		System.out.println(diff);
		while (diff > epsilon) {
			newLabels = harm.calcNewLabels(oldLabels);
			diff = getDiff(oldLabels, newLabels);
			oldLabels = newLabels;
			printMap(newLabels);
			System.out.println(diff);
		}
	}
	

}
