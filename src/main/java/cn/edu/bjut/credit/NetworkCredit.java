package cn.edu.bjut.credit;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * network credit schema
 * 
 * References: 
 * [1] Jinseok Kim and Jana Diesner, 2014. A Network-based Approach to 
 * Coauthorship Credit Allocation. Scientometrics, Vol. 101, No. 1, 
 * pp. 587-602.
 * 
 *  @author XU, Shuo (pzczxs@gmail.com)
 * */
public class NetworkCredit extends AuthorshipCredit {
	private double lambda; 
	
	public NetworkCredit(List<BylineInformation> authors, boolean adjustmentFlag, double lambda) {
		super(authors, adjustmentFlag); 
		
		this.lambda = lambda; 
	}
	
	public NetworkCredit(List<BylineInformation> authors, double lambda) {
		super(authors, true); 
		
		this.lambda = lambda; 
	}
	
	public NetworkCredit(BylineInformation[] authors, boolean adjustmentFlag, double lambda) {
		super(authors, adjustmentFlag); 
		
		this.lambda = lambda; 
	}
	
	public NetworkCredit(BylineInformation[] authors, double lambda) {
		super(authors, true); 
		
		this.lambda = lambda; 
	}
	
	public NetworkCredit(int[] authors, boolean adjustmentFlag, double lambda) {
		super(authors, adjustmentFlag); 
		
		this.lambda = lambda; 
	}
	
	public NetworkCredit(int[] authors, double lambda) {
		super(authors, true); 
		
		this.lambda = lambda; 
	}
	
	public NetworkCredit(int[] authors, boolean[] isReprints, boolean adjustmentFlag, double lambda) {
		super(authors, isReprints, adjustmentFlag); 
		
		this.lambda = lambda; 
	}
	
	public NetworkCredit(int[] authors, boolean[] isReprints, double lambda) {
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
		double fractionalCredit = 1.0 / authors.size(); // The fractional value is assigned to each of node
		double transferableCredit = lambda * fractionalCredit; // the transferable credit

		// first author
		double firstAuthorCredit = 0.0; 
		for (int i = 1; i < authors.size(); i++) {
			firstAuthorCredit += 1.0 / (authors.size() - i); 
		}
		firstAuthorCredit = fractionalCredit + transferableCredit * firstAuthorCredit; 
		credits.put(authors.get(0).getAuthorId(), firstAuthorCredit); 
		
		// middle author
		for (int i = 1; i < authors.size() - 1; i++) {
			double middleAuthorCredit = 0.0; 
			for (int j = 1; j < authors.size() - i; j++) {
				middleAuthorCredit += 1.0 / (authors.size() - j); 
			}
			middleAuthorCredit = fractionalCredit - transferableCredit 
					+ transferableCredit * middleAuthorCredit; 
			
			credits.put(authors.get(i).getAuthorId(), middleAuthorCredit); 
		}
		
		// last author
		credits.put(authors.get(authors.size() - 1).getAuthorId(), 
				fractionalCredit - transferableCredit); 
		
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
			AuthorshipCredit authorshipCredit = new NetworkCredit(authors, lambdas[i]); 
			
			Map<Integer, Double> credits = authorshipCredit.getCredit(); 
			for (Map.Entry<Integer, Double> entry: credits.entrySet()) {
				//System.out.print(String.format("%.4f", entry.getValue()) + "\t");
				System.out.print(entry.getValue() + "\t");
			}
			System.out.println(); 
		}
	}
}
