package cn.edu.bjut.tools;

public class UnorderedPair<T extends Comparable<T>> extends AbstractPair<T, T> implements Comparable<UnorderedPair<T>> {
	
	/*
	 * Creates an unordered pair of the specified elements. The order of the arguments is irrelevant, 
	 * so the first argument is not guaranteed to be returned by {@link #getFirst()}, for example. 
	 * @param a one element of the pair. Must not be <tt>null</tt>. 
	 * @param b one element of the pair. Must not be <tt>null</tt>. May be the same as <tt>a</tt>. 
	 * */
	public UnorderedPair(final T a, final T b) {
		if (a == null || b == null) {
			throw new IllegalArgumentException(); 
		}
		
		if (a.compareTo(b) < 0) {
			this.first = a; 
			this.second = b; 
		} else {
			this.first = b; 
			this.second = a; 
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}

		UnorderedPair<?> other = (UnorderedPair<?>) obj;
		if (!first.equals(other.first) || !second.equals(other.second)) {
			return false; 
		}

		return true;
	}
	
	@Override
	public int compareTo(final UnorderedPair<T> o) {
		int firstCmp = first.compareTo(o.first);
		
		if (firstCmp != 0) {
			return firstCmp; 
		}
		
		return second.compareTo(o.second); 
	}
}
