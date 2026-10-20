package cn.edu.bjut.tools;

public class IntegerUnorderedPair extends UnorderedPair<Integer> {	
	
	public IntegerUnorderedPair(final Integer a, final Integer b) {
		super(a, b); 
	}
	
	public IntegerUnorderedPair(final int a, final int b) {
		super(Integer.valueOf(a), Integer.valueOf(b)); 
	}
}
