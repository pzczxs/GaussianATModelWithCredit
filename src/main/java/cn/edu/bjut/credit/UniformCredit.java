package cn.edu.bjut.credit;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * uniform credit scheme
 * 
 * References: 
 * [1] Duncan Lindsey, 1980. Production and Citation Measures in the Sociology 
 * of Sciences: The Problem of Multiple Authorship. Social Studies of Science, 
 * Vol. 10, No. 2, pp. 145-162. 
 * [2] Derek de S. Price, 1981. Multiple Authorship. Science, Vol. 212, No. 
 * 4498, pp. 986-986. 
 * [3] Gunnar Sivertsen, Ronald Rousseau, and Lin Zhang, 2019. Measuring Scientific
 * Contributions with Modified Fractional Counting. Journal of Informetrics, Vol. 
 * 13, No. 2, pp. 679-694. 
 * 
 * @author XU, Shuo (pzczxs@gmail.com)
 * */
public class UniformCredit extends AuthorshipCredit {
	
	public UniformCredit(List<BylineInformation> authors) {
		super(authors); 
	}
	
	public UniformCredit(BylineInformation[] authors) {
		super(authors); 
	}
	
	public UniformCredit(int[] authors) {
		super(authors); 
	}
	
	public UniformCredit(int[] authors, boolean[] isReprints) {
		super(authors, isReprints); 
	}

	@Override
	protected void calculate() {
		this.credits = new LinkedHashMap<Integer, Double>();
		
		double credit = 1.0 / authors.size(); 
		for (int i = 0; i < authors.size(); i++) {
			credits.put(authors.get(i).getAuthorId(), credit); 
		}
	}
}
