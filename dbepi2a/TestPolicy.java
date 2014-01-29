package dbepi2a;

import java.util.*;

public class TestPolicy implements Policy {
	
	public Integer immunePerson;

	public TestPolicy(Integer person) {
		this.immunePerson = person;
	}

	public boolean isValidTransEvent(Integer infected, Integer victim,
			Integer location, long time) {
		if (victim.equals(this.immunePerson))
			return false;
		return true;
	}

	public void newInf(Integer victim, Integer location, long time) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		simpleTest();

	}
	
	public static void simpleTest() {
		Integer person = new Integer(817602);
		ArrayList<Policy> policies = new ArrayList<Policy>();
		policies.add(new TestPolicy(person));
		DendroReSim sim = new DendroReSim(policies);
		for (int i=0; i < 15; i++)
			sim.stepSim();
		if (sim.infectionLoad.containsKey(person))
			System.out.println("Immune person: "+person+" load: "+sim.infectionLoad.get(person));
		else
			System.out.println("Immune person: "+person+" not in infection load list.");
		System.out.println("Immune person: "+person+" is infected: "+sim.isInfected(person));
	}

}
