package cn.edu.bjut.credit;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * geometric credit schema (Abbs, 2011)
 * 
 * The choice of alpha = 2 delivers the original geometric counting method 
 * (Egghe et al., 2000), while alpha = 1 delivers the fractional counting 
 * method. The larger the value of alpha, the larger the ratio of credit 
 * between adjacently positioned co-authors. 
 * 
 * References: 
 * [1] Leo Egghe, Ronald Rousseau, and Guido van Hooydonk, 2000. Methods for 
 * Accrediting Publications to Authors or Countries: Consequences for Evaluation
 * Studies. Journal of the American Society for Information Science, Vol. 51, 
 * No. 2, pp. 145-157. 
 * [2] Ash Mohammad Abbas, 2011. Polynomial Weights or Generalized Geometric 
 * Weights: Yet Another Scheme for Assigning Credits to Multiple Authors. 
 * eprint arXiv:1103.2848. 
 * 
 * @author XU, Shuo (pzczxs@gmail.com)
 * */
public class GeometricCredit extends AuthorshipCredit {
	private double lambda; 
	
	public GeometricCredit(List<BylineInformation> authors, boolean adjustmentFlag, double lambda) {
		super(authors, adjustmentFlag); 
		
		this.lambda = lambda; 
	}
	
	public GeometricCredit(List<BylineInformation> authors, double lambda) {
		super(authors, true); 
		
		this.lambda = lambda; 
	}
	
	public GeometricCredit(BylineInformation[] authors, boolean adjustmentFlag, double lambda) {
		super(authors, adjustmentFlag); 
		
		this.lambda = lambda; 
	}
	
	public GeometricCredit(BylineInformation[] authors, double lambda) {
		super(authors, true); 
		
		this.lambda = lambda; 
	}
	
	public GeometricCredit(int[] authors, boolean adjustmentFlag, double lambda) {
		super(authors, adjustmentFlag); 
		
		this.lambda = lambda; 
	}
	
	public GeometricCredit(int[] authors, double lambda) {
		super(authors, true); 
		
		this.lambda = lambda; 
	}
	
	public GeometricCredit(int[] authors, boolean[] isReprints, boolean adjustmentFlag, double lambda) {
		super(authors, isReprints, adjustmentFlag); 
		
		this.lambda = lambda; 
	}
	
	public GeometricCredit(int[] authors, boolean[] isReprints, double lambda) {
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
		
		if (Math.abs(lambda - 1.0) < 1E-8) { // fractional counting method
			double credit = 1.0 / authors.size(); 
			for (int i = 0; i < authors.size(); i++) {
				credits.put(authors.get(i).getAuthorId(), credit); 
			}
		} else {
			double denominator = Math.pow(lambda, authors.size()) - 1.0; 
			for (int i = 0; i < authors.size(); i++) {
				credits.put(authors.get(i).getAuthorId(), 
						(lambda - 1.0) * Math.pow(lambda, authors.size() - i - 1) / denominator); 
			}
			
			adjustEqualContributors(); 
			if (adjustmentFlag) {
				adjustHyperAuthorship(); 
			}
		}
	}

	public static void main(String[] args) {
		int[] authors = new int[10]; 
		for (int i = 0; i < authors.length; i++) {
			authors[i] = i + 1; 
		}
		
		double[] lambdas = new double[11]; 
		for (int i = 0; i < lambdas.length; i++) {
			lambdas[i] = i * 0.25 + 1.0; 
			//System.out.println("lambda: " + String.format("%.4f", lambdas[i])); 
			AuthorshipCredit authorshipCredit = new GeometricCredit(authors, lambdas[i]); 
			
			Map<Integer, Double> credits = authorshipCredit.getCredit(); 
			for (Map.Entry<Integer, Double> entry: credits.entrySet()) {
				//System.out.print(String.format("%.4f", entry.getValue()) + "\t");
				System.out.print(entry.getValue() + "\t");
			}
			System.out.println(); 
		}
	}
}
