package cn.edu.bjut.credit;

public class CreditParameterWithRank extends CreditParameter {
	private int totalNumber; 
	private int rank; 
	
	public CreditParameterWithRank(CreditScheme scheme, int totalNumber, int rank, boolean adjustmentFlag) {
		this.scheme = scheme; 
		this.totalNumber = totalNumber; 
		this.rank = rank; 
		this.adjustmentFlag = adjustmentFlag; 
	}
	
	public CreditParameterWithRank(CreditScheme scheme, int totalNumber, int rank) {
		this(scheme, totalNumber, rank, true); 
	}

	public int getTotalNumber() {
		return totalNumber;
	}

	public void setTotalNumber(int totalNumber) {
		this.totalNumber = totalNumber;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}
}
