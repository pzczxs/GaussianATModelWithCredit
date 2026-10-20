package cn.edu.bjut.credit;

import java.util.Map;

public class CreditCalculator {
	
	public static double[][] calculate(BylineInformation[][] a, CreditParameter creditParam) {
		// allocate
		double[][] credits = new double[a.length][]; 
		
		for (int m = 0; m < a.length; m++) {
			credits[m] = new double[a[m].length]; 
			
			AuthorshipCredit authorshipCredit = null;
			switch(creditParam.getScheme()) {
			case ARITHMETIC_CREDIT: 
				if (creditParam instanceof CreditParameterWithRank) {
					CreditParameterWithRank creditParamWithRank = (CreditParameterWithRank) creditParam; 
					authorshipCredit = new ArithmeticCredit(a[m], creditParamWithRank.isAdjustmentFlag(), 
							creditParamWithRank.getTotalNumber(), creditParamWithRank.getRank()); 
				} else {
					authorshipCredit = new ArithmeticCredit(a[m], creditParam.isAdjustmentFlag(), creditParam.getLambda()); 
				}
				
				break; 
			case AXIOMATIC_CREDIT: 
				authorshipCredit = new AxiomaticCredit(a[m], creditParam.isAdjustmentFlag(), creditParam.getLambda()); 
				break; 
			case GEOMETRIC_CREDIT: 
				authorshipCredit = new GeometricCredit(a[m], creditParam.isAdjustmentFlag(), creditParam.getLambda()); 
				break; 
			case GOLDEN_NUMBER_CREDIT:
				authorshipCredit = new GoldenNumberCredit(a[m], creditParam.isAdjustmentFlag(), creditParam.getLambda()); 
				break; 
			case HARMONIC_CREDIT: 
				authorshipCredit = new HarmonicCredit(a[m], creditParam.isAdjustmentFlag(), creditParam.getLambda()); 
				break; 
			case NETWORK_CREDIT: 
				authorshipCredit = new NetworkCredit(a[m], creditParam.isAdjustmentFlag(), creditParam.getLambda()); 
				break; 
			case SINGLE_AUTHOR_CREDIT: 
				authorshipCredit = new SingleAuthorCredit(a[m]); 
				break; 
			case UNIFORM_CREDIT: 
				authorshipCredit = new UniformCredit(a[m]); 
				break; 
			}
			
			Map<Integer, Double> authorId2Credits = authorshipCredit.getCredit(); 
			for (int i = 0; i < a[m].length; i++) {
				credits[m][i] = authorId2Credits.get(a[m][i].getAuthorId()); 
			}
		}
		
		return credits; 
	}
}
