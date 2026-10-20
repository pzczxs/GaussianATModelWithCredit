package cn.edu.bjut.tools;

public class IntegerDoublePair extends OrderedPair<Integer, Double> implements Comparable<IntegerDoublePair> {
	
	public IntegerDoublePair(Integer first, Double second) {
		super(first, second); 
	}
	
	public IntegerDoublePair(int first, double second) {
		super(Integer.valueOf(first), Double.valueOf(second)); 
	}
	
	@Override
	public int compareTo(IntegerDoublePair o2) {
		if (second > o2.second) {
			return -1;
		} else if (second < o2.second) {
			return 1;
		} else { 
			return 0;
		}
	}
}
