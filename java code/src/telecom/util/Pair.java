package telecom.util;

public class Pair<E, K> {
	private E first;
	private K second;

	public Pair(E first, K second) {
		this.first = first;
		this.second = second;
	}

	public E getFirst() {
		return first;
	}

	public void setFirst(E first) {
		this.first = first;
	}

	public K getSecond() {
		return second;
	}

	public void setSecond(K second) {
		this.second = second;
	}
	
	
}
