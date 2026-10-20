package cn.edu.bjut.model;

import org.ejml.data.DenseMatrix64F;

import com.google.gson.Gson;

public class GaussianATCreditParameter {
	private int numTopics;
	private double alpha;
	private double beta;	
	private DenseMatrix64F psi; 
	private double nu; 
	private DenseMatrix64F mu; 
	private double kappa; 
	
	private int numTopWords;
	private int numTopWordsForPMI; // top-N topic words for PMI score
	private int numTopAuthors; 
	
	public final static int NUM_TOPICS_DEFAULT = 100; 
	public final static double ALPHA_DEFAULT = 0.5; 
	public final static double BETA_DEFAULT = 0.01;  
	public final static double KAPPA_DEFAULT = 0.1;  
	public final static int NUM_TOP_WORDS_DEFAULT = 20; 
	public final static int NUM_TOP_WORDS_FOR_PMI_DEFAULT = 15; 
	public final static int NUM_TOP_AUTHORS_DEFAULT = 20; 
	
	public GaussianATCreditParameter(int numTopics, double alpha, double beta, DenseMatrix64F psi, double nu, 
			DenseMatrix64F mu, double kappa, int numTopWords, int numTopWordsForPMI, int numTopAuthors) {
		this.numTopics = numTopics; 
		this.alpha = alpha; 
		this.beta = beta; 
		this.psi = psi; 
		this.nu = nu; 
		this.mu = mu; 
		this.kappa = kappa; 
		this.numTopWords = numTopWords; 
		this.numTopWordsForPMI = numTopWordsForPMI; 
		this.numTopAuthors = numTopAuthors; 
		
		check(); 
	}
	
	public GaussianATCreditParameter(int numTopics, DenseMatrix64F psi, double nu, DenseMatrix64F mu) {
		this(numTopics, ALPHA_DEFAULT, BETA_DEFAULT, psi, nu, mu, KAPPA_DEFAULT, NUM_TOP_WORDS_DEFAULT, 
				NUM_TOP_WORDS_FOR_PMI_DEFAULT, NUM_TOP_AUTHORS_DEFAULT); 
	}
	
	public int getNumTopics() {
 		return this.numTopics; 
 	}
 	
 	public void setNumTopics(int numTopics) {
 		this.numTopics = numTopics; 
 	}
 	
 	public double getAlpha() {
 		return this.alpha; 
 	}
 	
 	public void setAlpha(double alpha) {
 		this.alpha = alpha; 
 	}
 	
 	public double getBeta() {
 		return this.beta; 
 	}
 	
 	public void setBeta(double beta) {
 		this.beta = beta; 
 	}

	public DenseMatrix64F getPsi() {
		return psi;
	}

	public void setPsi(DenseMatrix64F psi) {
		this.psi = psi;
	}

	public double getNu() {
		return nu;
	}

	public void setNu(double nu) {
		this.nu = nu;
	}

	public DenseMatrix64F getMu() {
		return mu;
	}

	public void setMu(DenseMatrix64F mu) {
		this.mu = mu;
	}

	public double getKappa() {
		return kappa;
	}

	public void setKappa(double kappa) {
		this.kappa = kappa;
	}
	
	public int getNumTopWords() {
 		return this.numTopWords; 
 	}
 	
 	public void setNumTopWords(int numTopWords) {
 		this.numTopWords = numTopWords; 
 	}
 	
 	public int getNumTopWordsForPMI() {
		return numTopWordsForPMI;
	}

	public void setTopWordsForPMI(int numTopWordsForPMI) {
		this.numTopWordsForPMI = numTopWordsForPMI;
	}
 	
 	public int getNumTopAuthors() {
 		return this.numTopAuthors; 
 	}
 	
 	public void setNumTopAuthors(int numTopAuthors) {
 		this.numTopAuthors = numTopAuthors; 
 	}

	private void check() {
		if (numTopics <= 0) {
 			numTopics = NUM_TOPICS_DEFAULT; 
 		}
 		
 		if (alpha <= 0.0) {
			alpha = ALPHA_DEFAULT; 
		}
 		
		if (beta <= 0.0) {
			beta = BETA_DEFAULT; 
		}
		
		if (kappa <= 0.0) {
			kappa = KAPPA_DEFAULT; 
		}
		
		if (numTopWords <= 0) {
			numTopWords = NUM_TOP_WORDS_DEFAULT; 
		}
		
		if (numTopWordsForPMI <= 0) {
			numTopWordsForPMI = NUM_TOP_WORDS_FOR_PMI_DEFAULT; 
		}
		
		if (numTopAuthors <= 0) {
			numTopAuthors = NUM_TOP_AUTHORS_DEFAULT; 
		}
 	}
	
	@Override
	public String toString() {
		Gson gson = new Gson(); 
		
		return gson.toJson(this);
	}
}
