package cn.edu.bjut.credit;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.edu.bjut.utils.Vectors;

/**
 * 
 * References: 
 * [1] Jonathan Stallings, Eric Vance, Jiansheng Yang, Michael W. Vannier, 
 * Jimin Liang, Liaojun Pang, Liang Dai, Ivan Ye, and Ge Wang, 2013. 
 * Determining Scientific Impact using a Collaboration Index. Proceedings 
 * of the National Academy of Sciences of the United States of America, Vol. 
 * 110, No. 24, pp. 9680-9685. 
 * [2] Ge Wang and Jiansheng Yang, 2010. Axiomatic Quantification of Co-authors' 
 * Relative Contributions. arXiv:1003.3362 [stat.AP]
 * 
 * @author XU, Shuo (pzczxs@gmail.com)
 * */
public class AxiomaticCredit extends AuthorshipCredit {
	private int[] cumsumG; 
	private double lambda; 
	
	public AxiomaticCredit(List<BylineInformation> authors, boolean adjustmentFlag, double lambda) {
		super(authors, adjustmentFlag); 
		
		this.lambda = lambda; 
	}
	
	public AxiomaticCredit(List<BylineInformation> authors, double lambda) {
		super(authors, true); 
		
		this.lambda = lambda; 
	}
	
	public AxiomaticCredit(BylineInformation[] authors, boolean adjustmentFlag, double lambda) {
		super(authors, adjustmentFlag); 
		
		this.lambda = lambda; 
	}
	
	public AxiomaticCredit(BylineInformation[] authors, double lambda) {
		super(authors, true); 
		
		this.lambda = lambda; 
	}
	
	public AxiomaticCredit(int[] authors, boolean adjustmentFlag, double lambda) {
		super(authors, adjustmentFlag); 
		
		this.lambda = lambda; 
	}
	
	public AxiomaticCredit(int[] authors, double lambda) {
		super(authors, true); 
		
		this.lambda = lambda; 
	}
	
	public AxiomaticCredit(int[] authors, boolean[] isReprints, boolean adjustmentFlag, double lambda) {
		super(authors, isReprints, true); 
		
		this.lambda = lambda; 
	}
	
	public AxiomaticCredit(int[] authors, boolean[] isReprints, double lambda) {
		super(authors, isReprints); 
		
		this.lambda = lambda; 
	}
	
	@Override
	protected void fill() {
		super.fill();
		
		cumsumG = Vectors.cumsum(g); 
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
		
		for (int i = 0; i < authorGroups.size(); i++) {
			double credit = 0.0; 
			for (int j = i; j < authorGroups.size(); j++) {
				credit += 1.0 / cumsumG[j]; 
			}
			credit = (1.0 - lambda) / authors.size() + lambda * credit / authorGroups.size(); 
			
			for (int authorId: authorGroups.get(i)) {
				credits.put(authorId, credit); 
			}
		}
		
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
			AuthorshipCredit authorshipCredit = new AxiomaticCredit(authors, lambdas[i]); 
			
			Map<Integer, Double> credits = authorshipCredit.getCredit(); 
			for (Map.Entry<Integer, Double> entry: credits.entrySet()) {
				//System.out.print(String.format("%.4f", entry.getValue()) + "\t");
				System.out.print(entry.getValue() + "\t");
			}
			System.out.println(); 
		}
	}
}
