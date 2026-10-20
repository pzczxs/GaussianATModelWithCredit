package cn.edu.bjut.credit;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.edu.bjut.utils.Range;

/**
 * arithemetic credit scheme
 * 
 * The credits are linearly distributed in decreasing order among the several co-authors. 
 * The decreasing credits intensity depends on the parameter $\alpha \in [0, 2 / (n (n + 1)))$
 * (Abbas, 2010). The value alpha establishes the difference of credits between two 
 * consecutive co-authors. The higher the value of alpha, the higher the decrease in credits
 * between two consecutive co-authors. At $\alpha = 2 / (n (n + 1))$, the generalized 
 * arithmetic counting method delivers, as a particular case, the original arithmetic 
 * counting method (Trenchard, 1992; van Hooydonk, 1997). 
 * 
 * References: 
 * [1] Paul M. Trenchard, 1992. Hierarchical Bibliometry: A New Objective Measure of 
 * Individual Scientific Performance to Replace Publication Counts and to Complement 
 * Citation Measures. Journal of Information Science, Vol. 18, No. 1, pp. 69-75.
 * [2] G. van Hooydonk, 1997. Fractional Counting of Multiauthored Publications: 
 * Consequences for the Impact of Authors. Journal of the American Society for Information
 * Science, Vol. 48, No. 10, pp. 944-945.  
 * [3] Ash Mohanmmad Abbas, 2010. Generalized Linear weights for Sharing Credits among 
 * Multiple Authors. eprint arXiv:1012.5477. 
 * 
 * @author XU, Shuo (pzczxs@gmail.com)
 * */
public class ArithmeticCredit extends AuthorshipCredit {
	private double lambda; 
	
	public ArithmeticCredit(List<BylineInformation> authors, boolean adjustmentFlag, double lambda) {
		super(authors, adjustmentFlag); 
		
		this.lambda = lambda; 
	}
	
	public ArithmeticCredit(List<BylineInformation> authors, double lambda) {
		super(authors, true); 
		
		this.lambda = lambda; 
	}
	
	public ArithmeticCredit(BylineInformation[] authors, boolean adjustmentFlag, double lambda) {
		super(authors, adjustmentFlag); 
		
		this.lambda = lambda; 
	}
	
	public ArithmeticCredit(BylineInformation[] authors, double lambda) {
		super(authors, true); 
		
		this.lambda = lambda;
	}
	
	public ArithmeticCredit(int[] authors, boolean adjustmentFlag, double lambda) {
		super(authors, adjustmentFlag); 
		
		this.lambda = lambda; 
	}
	
	public ArithmeticCredit(int[] authors, double lambda) {
		super(authors, true); 
		
		this.lambda = lambda; 
	}
	
	public ArithmeticCredit(int[] authors, boolean[] isReprints, boolean adjustmentFlag, double lambda) {
		super(authors, isReprints, adjustmentFlag); 
		
		this.lambda = lambda; 
	}
	
	public ArithmeticCredit(int[] authors, boolean[] isReprints, double lambda) {
		super(authors, isReprints, true); 
		
		this.lambda = lambda; 
	}
	
	public ArithmeticCredit(List<BylineInformation> authors, boolean adjustmentFlag, int totalNumber, int rank) {
		super(authors, adjustmentFlag); 
		
		double[] range = Range.closedClosed(0.0, 2.0 / (authors.size() * (authors.size() + 1)), totalNumber); 
		this.lambda = range[rank]; 
	}
	
	public ArithmeticCredit(List<BylineInformation> authors, int totalNumber, int rank) {
		super(authors, true); 
		
		double[] range = Range.closedClosed(0.0, 2.0 / (authors.size() * (authors.size() + 1)), totalNumber); 
		this.lambda = range[rank]; 
	}
	
	public ArithmeticCredit(BylineInformation[] authors, boolean adjustmentFlag, int totalNumber, int rank) {
		super(authors, adjustmentFlag); 
		
		double[] range = Range.closedClosed(0.0, 2.0 / (authors.length * (authors.length + 1)), totalNumber);
		this.lambda = range[rank]; 
	}
	
	public ArithmeticCredit(BylineInformation[] authors, int totalNumber, int rank) {
		super(authors, true); 
		
		double[] range = Range.closedClosed(0.0, 2.0 / (authors.length * (authors.length + 1)), totalNumber);
		this.lambda = range[rank]; 
	}
	
	public ArithmeticCredit(int[] authors, int totalNumber, boolean adjustmentFlag, int rank) {
		super(authors, adjustmentFlag); 
		
		double[] range = Range.closedClosed(0.0, 2.0 / (authors.length * (authors.length + 1)), totalNumber); 
		this.lambda = range[rank];
	}
	
	public ArithmeticCredit(int[] authors, int totalNumber, int rank) {
		super(authors, true); 
		
		double[] range = Range.closedClosed(0.0, 2.0 / (authors.length * (authors.length + 1)), totalNumber); 
		this.lambda = range[rank];
	}
	
	public ArithmeticCredit(int[] authors, boolean[] isReprints, boolean adjustmentFlag, int totalNumber, int rank) {
		super(authors, isReprints, adjustmentFlag); 
		
		double[] range = Range.closedClosed(0.0, 2.0 / (authors.length * (authors.length + 1)), totalNumber); 
		this.lambda = range[rank];
	}
	
	public ArithmeticCredit(int[] authors, boolean[] isReprints, int totalNumber, int rank) {
		super(authors, isReprints, true); 
		
		double[] range = Range.closedClosed(0.0, 2.0 / (authors.length * (authors.length + 1)), totalNumber); 
		this.lambda = range[rank];
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
		
		double ALPHA_UPPER = 2.0 / (authors.size() * (authors.size() + 1)); 
		if (Math.abs(lambda - ALPHA_UPPER) < 1E-8) { // original arithmetic counting method
			for (int i = 0; i < authors.size(); i++) {
				credits.put(authors.get(i).getAuthorId(), ALPHA_UPPER * (authors.size() - i)); 
			}
		} else {
			double factor = 1.0 / authors.size(); 
			for (int i = 0; i < authors.size(); i++) {
				credits.put(authors.get(i).getAuthorId(), 
						factor + lambda * (authors.size() - 2 * (i + 1) + 1) / 2.0); 
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
		
		double[] lambdas = new double[10]; 
		for (int i = 0; i < lambdas.length; i++) {
			ArithmeticCredit authorshipCredit = new ArithmeticCredit(authors, lambdas.length, i); 
			
			lambdas[i] = authorshipCredit.getLambda(); 
			//System.out.println("lambda: " + String.format("%.4f", lambdas[i])); 
			
			
			Map<Integer, Double> credits = authorshipCredit.getCredit(); 
			for (Map.Entry<Integer, Double> entry: credits.entrySet()) {
				//System.out.print(String.format("%.4f", entry.getValue()) + "\t");
				System.out.print(entry.getValue() + "\t");
			}
			System.out.println(); 
		}
	}
}
