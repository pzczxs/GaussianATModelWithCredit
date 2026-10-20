package cn.edu.bjut.tools;

public class IntegerOrderedPair extends OrderedPair<Integer, Integer> implements Comparable<IntegerOrderedPair> {
	
	public IntegerOrderedPair(Integer first, Integer second) {
		super(first, second); 
	}
	
	public IntegerOrderedPair(int first, int second) {
		super(Integer.valueOf(first), Integer.valueOf(second)); 
	}
	
	@Override
	public int compareTo(IntegerOrderedPair o2) {
		if (first > o2.getFirst()) {
			return 1; 
		} else if (first < o2.getFirst()) {
			return -1; 
		} else {
			if (second > o2.getSecond()) {
				return 1;
			} else if (second == o2.getSecond()) {
				return 0;
			} else { 
				return -1;
			}
		}
	}
}
