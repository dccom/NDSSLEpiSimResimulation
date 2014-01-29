/**
 * 
 */
package dbepi2a;

/**
 * @author dave
 *
 */
public class KVPair<K extends Comparable,V extends Comparable> implements Comparable {
	private int order;
	private K key;
	private V value;

	/**
	 * 
	 */
	public KVPair(K key, V value, int order) {
		this.key = key;
		this.value = value;
		this.order = order;
	}
	
	/**
	 * 
	 */
	public KVPair(K key, V value) {
		this(key, value, 1);
	}
	
	public K key() {
		return this.key;
	}
	
	public V value() {
		return this.value;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object arg0) {
		KVPair<K,V> arg = (KVPair<K,V>) arg0;
		if (this.order * arg.order == -1)
			System.out.println("Warning! comparing KVPair items with different orderring direction.");
		K okey = arg.key();
		V ovalue = arg.value();
		if (this.value.compareTo(ovalue) == 0)
			return this.order*this.key.compareTo(okey);
		return this.order*this.value.compareTo(ovalue);
	}
	
	public String toString() {
		return this.key.toString() + "\t" + this.value.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		test();

	}
	
	public static void test() {
		KVPair<Integer,Double> a,b,c,d,e;
		a = new KVPair(new Integer(1), new Double(10.0),-1);
		b = new KVPair(new Integer(2), new Double(9.0),-1);
		c = new KVPair(new Integer(3), new Double(8.0),-1);
		d = new KVPair(new Integer(4), new Double(8.0),-1);
		e = new KVPair(new Integer(5), new Double(7.0),-1);
		
		java.util.PriorityQueue<KVPair> q = new java.util.PriorityQueue<KVPair>();
		q.add(a);
		q.add(b);
		q.add(c);
		q.add(d);
		q.add(e);
		
		while (!q.isEmpty())
			System.out.println(q.poll());
	}

}
