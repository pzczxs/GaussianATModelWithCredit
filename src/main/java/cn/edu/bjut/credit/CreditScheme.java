package cn.edu.bjut.credit;

public enum CreditScheme {
	ARITHMETIC_CREDIT(0),
	AXIOMATIC_CREDIT(1), 
	GEOMETRIC_CREDIT(2),
	GOLDEN_NUMBER_CREDIT(3),
	HARMONIC_CREDIT(4), 
	NETWORK_CREDIT(5), 
	SINGLE_AUTHOR_CREDIT(6), 
	UNIFORM_CREDIT(7); 
	
	private int code; 
	private CreditScheme(int code) {
		this.code = code; 
	}
	
	public int getCode() {
		return this.code; 
	}
	
	public void setCode(int code) {
		this.code = code; 
	}
}
