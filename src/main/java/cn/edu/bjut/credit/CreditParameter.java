package cn.edu.bjut.credit;

public class CreditParameter {
	protected CreditScheme scheme; 
	protected double lambda;  
	protected boolean adjustmentFlag; 
	
	public CreditParameter(CreditScheme scheme, double lambda, boolean adjustmentFlag) {
		this.scheme = scheme; 
		this.lambda = lambda; 
		this.adjustmentFlag = adjustmentFlag; 
	}
	
	public CreditParameter(CreditScheme scheme, double lambda) {
		this(scheme, lambda, true); 
	}
	
	public CreditParameter() {
		this(CreditScheme.UNIFORM_CREDIT, 0.0, false); 
	}

	public CreditScheme getScheme() {
		return scheme;
	}

	public void setScheme(CreditScheme scheme) {
		this.scheme = scheme;
	}

	public double getLambda() {
		return lambda;
	}

	public void setLambda(double lambda) {
		this.lambda = lambda;
	}

	public boolean isAdjustmentFlag() {
		return adjustmentFlag;
	}

	public void setAdjustmentFlag(boolean adjustmentFlag) {
		this.adjustmentFlag = adjustmentFlag;
	}
}
