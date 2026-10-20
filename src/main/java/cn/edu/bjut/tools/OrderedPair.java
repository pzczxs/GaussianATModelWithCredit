package cn.edu.bjut.tools;

/**
 * Pair is a Class for holding mutable pairs of objects.
 *
 * <i>Implementation note:</i>
 * on a 32-bit JVM uses ~ 8 (this) + 4 (first) + 4 (second) = 16 bytes.
 * on a 64-bit JVM uses ~ 16 (this) + 8 (first) + 8 (second) = 32 bytes.
 *
 * Many applications use a lot of Pairs so it's good to keep this
 * number small.
 *
 * @author XU Shuo (pzczxs@gmail.com)
 */
public class OrderedPair<T1, T2> extends AbstractPair<T1, T2> {
    
    public OrderedPair(T1 first, T2 second) {
    	this.first = first;
    	this.second = second;
    }

    @Override
    public boolean equals(Object obj) {
    	if (this == obj) {
			return true;
    	}
    	
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		
		OrderedPair<?, ?> other = (OrderedPair<?, ?>) obj;
		if (first == null) {
			if (other.first != null) {
				return false;
			}
		} else if (!first.equals(other.first)) {
			return false;
		}
		
		if (second == null) {
			if (other.second != null) {
				return false;
			}
		} else if (!second.equals(other.second)) {
			return false;
		}

		return true;
	}

    public T1 getFirst() {
    	return this.first;
    }

    public T2 getSecond() {
    	return this.second;
    }
}
