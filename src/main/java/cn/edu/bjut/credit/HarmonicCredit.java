package cn.edu.bjut.credit;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * harmonic credit schema (Liu & Fang, 2012)
 * 
 * The parameter k > 1 (finite natural number) controls the distribution of credits between 
 * consecutive co-authors. 
 * 
 * This credit schema delivers the fractional counting method when k converges to one and 
 * the original harnomic counting method (Hagen, 2008; Sekercioglu, 2008) when k converges 
 * to infinity. 
 * 
 * References: 
 * [1] Cagan H. Sekercioglu, 2008. Quantifying Coauthor Contributions. Science, Vol. 322, 
 * No. 5900, pp. 371-371. 
 * [2] Nils T. Hagen, 2008. Harmonic Allocation of Authorship Credit: Source-Level Correction 
 * of Bibliometric Bias Assures Accurate Publication and Citation Analysis. PLoS ONE, Vol. 3, 
 * No. 12, pp. e4021. 
 * [3] Xuan Zhen Liu and Hui Fang, 2012. Fairly Sharing the Credit of Multi-Authored Papers 
 * and its Application in the Modification of h-Index and g-Index. Scientometrics, Vol. 91, 
 * No. 1, pp. 37-49. 
 * 
 * @author XU, Shuo (pzczxs@gmail.com)
 * */
public class HarmonicCredit extends AuthorshipCredit {
	private double lambda; // lambda is greater than or equal to 1
	
	public HarmonicCredit(List<BylineInformation> authors, boolean adjustmentFlag, double lambda) {
		super(authors, adjustmentFlag); 
		
		this.lambda = lambda; 
	}
	
	public HarmonicCredit(List<BylineInformation> authors, double lambda) {
		super(authors, true); 
		
		this.lambda = lambda; 
	}
	
	public HarmonicCredit(BylineInformation[] authors, boolean adjustmentFlag, double lambda) {
		super(authors, adjustmentFlag); 
		
		this.lambda = lambda; 
	}
	
	public HarmonicCredit(BylineInformation[] authors, double lambda) {
		super(authors, true); 
		
		this.lambda = lambda; 
	}
	
	public HarmonicCredit(int[] authors, boolean adjustmentFlag, double lambda) {
		super(authors, adjustmentFlag); 
		
		this.lambda = lambda; 
	}
	
	public HarmonicCredit(int[] authors, double lambda) {
		super(authors, true); 
		
		this.lambda = lambda; 
	}
	
	public HarmonicCredit(int[] authors, boolean[] isReprints, boolean adjustmentFlag, double lambda) {
		super(authors, isReprints, adjustmentFlag); 
		
		this.lambda = lambda; 
	}
	
	public HarmonicCredit(int[] authors, boolean[] isReprints, double lambda) {
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
	public void calculate() {
		this.credits = new LinkedHashMap<Integer, Double>(); 
		
		if (lambda == 1) {
			double credit = 1.0 / authors.size(); 
			for (int i = 0; i < authors.size(); i++) {
				credits.put(authors.get(i).getAuthorId(), credit); 
			}
		} else if (Math.abs(lambda - Double.MAX_VALUE) < 1E-8) {
			double denominator = 0.0; 
			for (int i = 0; i < authors.size(); i++) {
				denominator += 1.0 / (i + 1); 
			}
			
			for (int i = 0; i < authors.size(); i++) {
				credits.put(authors.get(i).getAuthorId(), 
						(1.0 / (i + 1)) / denominator); 
			}
			
			adjustEqualContributors(); 
			if (adjustmentFlag) {
				adjustHyperAuthorship(); 
			}
		} else {
			double denominator = 0.0; 
			for (int i = 0; i < authors.size(); i++) {
				denominator += 1.0 / Math.pow(i + 1.0, 1.0 - 1.0 / lambda); 
			}
			
			for (int i = 0; i < authors.size(); i++) {
				credits.put(authors.get(i).getAuthorId(), 
						(1.0 / Math.pow(i + 1.0, 1.0 - 1.0 / lambda)) / denominator); 
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
		for (int i = 0; i < lambdas.length - 1; i++) {
			lambdas[i] = i + 1.0; 
		}
		lambdas[lambdas.length - 1] = Double.MAX_VALUE; 
		
		for (int i = 0; i < lambdas.length; i++) {
			//System.out.println("lambda: " + String.format("%.4f", lambdas[i])); 
			AuthorshipCredit authorshipCredit = new HarmonicCredit(authors, lambdas[i]); 
			
			Map<Integer, Double> credits = authorshipCredit.getCredit(); 
			for (Map.Entry<Integer, Double> entry: credits.entrySet()) {
				//System.out.print(String.format("%.4f", entry.getValue()) + "\t");
				System.out.print(entry.getValue() + "\t");
			}
			System.out.println(); 
		}
	}
}
