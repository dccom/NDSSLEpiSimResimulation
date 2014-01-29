package dbepi2a;

public interface Policy {
	
	public boolean isValidTransEvent(Integer infected, Integer victim, Integer location, long time);
	
	public void newInf(Integer victim, Integer location, long time);

}
