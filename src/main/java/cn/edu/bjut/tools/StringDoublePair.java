package cn.edu.bjut.tools;

public class StringDoublePair extends OrderedPair<String, Double> implements Comparable<StringDoublePair> {

	public StringDoublePair(String first, Double second) {
		super(first, second); 
	}
	
	public StringDoublePair(String first, double second) {
		super(first, Double.valueOf(second)); 
	}
	
	@Override
	public int compareTo(StringDoublePair o2) {
		if (second > o2.second) {
			return -1;
		} else if (second < o2.second) {
			return 1;
		} else { 
			return 0;
		}
	}
	
	@Override
    public String toString() { 
           return this.first + " & " + String.format("%.2f", this.second * 100); 
    }
}
