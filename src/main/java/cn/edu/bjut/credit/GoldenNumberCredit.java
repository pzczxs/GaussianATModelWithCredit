package cn.edu.bjut.credit;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * golden number credit schema
 * 
 * The distribution of credits is based on a number (golden number). 
 * 
 * References: 
 * [1] N. Assimakis and M. Adam, 2010. A New Author's Productivity Index: 
 * p-Index. Scientometrics, Vol. 85, No. 2, pp. 415-427. 
 * 
 * @author XU, Shuo (pzczxs@gmail.com)
 * */
public class GoldenNumberCredit extends AuthorshipCredit {
	private final static double GOLDEN_NUMBER = (Math.sqrt(5.0) - 1.0) / 2.0; 
	private double lambda; 
	
	public GoldenNumberCredit(List<BylineInformation> authors, boolean adjustmentFlag, double lambda) {
		super(authors, adjustmentFlag); 
		
		this.lambda = lambda; 
	}
	
	public GoldenNumberCredit(List<BylineInformation> authors, double lambda) {
		super(authors, true); 
		
		this.lambda = lambda; 
	}
	
	public GoldenNumberCredit(BylineInformation[] authors, boolean adjustmentFlag, double lambda) {
		super(authors, adjustmentFlag); 
		
		this.lambda = lambda; 
	}
	
	public GoldenNumberCredit(BylineInformation[] authors, double lambda) {
		super(authors, true); 
		
		this.lambda = lambda; 
	}
	
	public GoldenNumberCredit(int[] authors, boolean adjustmentFlag, double lambda) {
		super(authors, adjustmentFlag); 
		
		this.lambda = lambda; 
	}
	
	public GoldenNumberCredit(int[] authors, double lambda) {
		super(authors, true); 
		
		this.lambda = lambda; 
	}
	
	public GoldenNumberCredit(int[] authors, boolean[] isReprints, boolean adjustmentFlag, double lambda) {
		super(authors, isReprints, adjustmentFlag); 
		
		this.lambda = lambda; 
	}
	
	public GoldenNumberCredit(int[] authors, boolean[] isReprints, double lambda) {
		super(authors, isReprints, true); 
		
		this.lambda = lambda; 
	}
	
	public double getLambda() {
		return lambda;
	}

	public void setLambda(double lambda) {
		this.lambda = lambda;
	}

	@Override
	protected void calculate() {
		this.credits = new LinkedHashMap<Integer, Double>(); 
		
		for (int i = 0; i < authors.size(); i++) {
			if (i == authors.size() - 1) {
				credits.put(authors.get(i).getAuthorId(), 
						(1.0 - lambda) / authors.size() +lambda *  Math.pow(1 - GOLDEN_NUMBER, authors.size() - 1)); 
			} else {
				credits.put(authors.get(i).getAuthorId(), 
						((1.0 - lambda) / authors.size() + lambda * GOLDEN_NUMBER * Math.pow(1 - GOLDEN_NUMBER,  i))); 
			}
			
		}
		
		adjustEqualContributors(); 
		if (adjustmentFlag) {
			adjustHyperAuthorship(); 
		}
	}

	public static void main(String[] args) {
		int[] authors = new int[10]; 
		for (int i = 0; i < authors.length; i++) {
			authors[i] = i + 1; 
		}
		
		double[] lambdas = new double[11]; 
		for (int i = 0; i < lambdas.length; i++) {
			lambdas[i] = i * 0.1; 
			//System.out.println("lambda: " + String.format("%.4f", lambdas[i])); 
			AuthorshipCredit authorshipCredit = new GoldenNumberCredit(authors, lambdas[i]); 
			
			Map<Integer, Double> credits = authorshipCredit.getCredit(); 
			for (Map.Entry<Integer, Double> entry: credits.entrySet()) {
				//System.out.print(String.format("%.4f", entry.getValue()) + "\t");
				System.out.print(entry.getValue() + "\t");
			}
			System.out.println(); 
		}
	}
}
