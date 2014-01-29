package dbepi2a;

public interface LocationMonitorAgent {
	
	public void setClosingAgent(MonBuildClose agent);
	
	public double[] calcWeights();
	
	public double[] calcWeights(double[] oldWeights);

}
