package cn.edu.bjut.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import cn.edu.bjut.corpus.LabelCorpus;
import cn.edu.bjut.credit.BylineInformation;
import cn.edu.bjut.credit.CreditCalculator;
import cn.edu.bjut.credit.CreditParameter;
import cn.edu.bjut.utils.CokusRandom;
import cn.edu.bjut.utils.MathUtils;

public class GaussianATCreditInferencer implements IInferencer, IPerplexity {
	private LabelCorpus corpus;
	private GaussianATCreditParameter param;
	private GaussianATCreditEstimator estimator;

	private CokusRandom rand;
	
	private CreditParameter creditParam; 
	private double[][] credits; 

	private int[][] z;
	private int[][] x; 
	
	public GaussianATCreditInferencer(String filebase, long seed, GaussianATCreditEstimator estimator) {
		this.estimator = estimator;
		this.param = estimator.getParameter();
		this.corpus = new LabelCorpus(filebase);
		this.creditParam = estimator.getCreditParameter(); 
		this.credits = CreditCalculator.calculate(corpus.getDocAuthors(), creditParam); 
		

		this.rand = new CokusRandom(seed);
	}

	@Override
	public void init() {
		int numDocs = corpus.getNumDocs(); 
		int numTopics = param.getNumTopics(); 
		int[][] w = corpus.getDocWords(); 
		BylineInformation[][] a = corpus.getDocAuthors(); 
		
		// allocate
		z = new int[numDocs][];
		x = new int[numDocs][];
		
		// initialize
		for (int m = 0; m < numDocs; m++) {
			z[m] = new int[w[m].length];
			x[m] = new int[w[m].length];
			for (int n = 0; n < w[m].length; n++) {
				int k = rand.nextInt(numTopics);
				int idx = rand.nextInt(a[m].length);
				int i = a[m][idx].getAuthorId();
				
				z[m][n] = k;
				x[m][n] = i;
			}
		}
	}
	
	@Override
	public void init(int niter) {
		int M = corpus.getNumDocs(); 
		int K = param.getNumTopics(); 
		int[][] w = corpus.getDocWords(); 
		BylineInformation[][] a = corpus.getDocAuthors(); 
		
		double[][] varphi = estimator.getVarphi(false);
		double[][] vartheta = estimator.getVartheta(false); 
		
		for (int iter = 0; iter < niter; iter++) {
			System.out.println("iter: " + iter);
			for (int m = 0; m < M; m++) {
				double[] pp = new double[a[m].length * K]; 
				
				for (int n = 0; n < w[m].length; n++) {
					int t = w[m][n];
					
					// compute weights
					double psum = 0;
					for (int kk = 0; kk < K; kk++) {
						for (int ii = 0; ii < a[m].length; ii++) {
							int idx = kk*a[m].length + ii; 
							int aid = a[m][ii].getAuthorId(); 
							pp[idx] = vartheta[aid][kk] * varphi[kk][t] * credits[m][ii]; 
							psum += pp[idx];
						}
					}
					
					// sample
					int idx = rand.nextDiscrete(pp, psum); 
					
					int kk = idx / a[m].length; 
					int ii = a[m][idx % a[m].length].getAuthorId(); 
					
					// reassign
					z[m][n] = kk;
					x[m][n] = ii; 
				} // n
			} // m
		} // iter
	}

	@Override
	public void inference(int niter) {
		int numDocs = corpus.getNumDocs(); 
		int numTopics = param.getNumTopics(); 
		int[][] w = corpus.getDocWords(); 
		BylineInformation[][] a = corpus.getDocAuthors(); 
		
		double[][] vartheta = estimator.getVartheta(false); 
		double[][] logMultivariateTDensity = estimator.getLogMultivariateTDensity(); 
		
		for (int iter = 0; iter < niter; iter++) {
			System.out.println("iter: " + iter);
			for (int m = 0; m < numDocs; m++) {
				double[] pp = new double[a[m].length * numTopics]; 
				
				for (int n = 0; n < w[m].length; n++) {
					int t = w[m][n];
					
					// compute weights
					double max = Double.NEGATIVE_INFINITY;
					for (int kk = 0; kk < numTopics; kk++) {
						// calculate the likelihood
						double logLikelihood = logMultivariateTDensity[t][kk];
						
						for (int ii = 0; ii < a[m].length; ii++) {
							int idx = kk*a[m].length + ii; 
							int aid = a[m][ii].getAuthorId(); 
							pp[idx] = Math.log(vartheta[aid][kk]) + logLikelihood + Math.log(credits[m][ii]); 
							
							if (pp[idx] > max) {
								max = pp[idx]; 
							}
						}
					}
					
					// to prevent overflow, subtract by log(p_max). This is because when we will be
					// normalizing after exponentiating, each entry will be exp(log p_i - log p_max
					// )/\Sigma_i exp(log p_i - log p_max)
					// the log p_max cancels put and prevents overflow in the exponentiating phase.
					double psum = 0;
					for (int idx = 0; idx < pp.length; idx++) {
						pp[idx] = Math.exp(pp[idx] - max); 
						psum += pp[idx]; 
					}
					
					// sample
					int idx = rand.nextDiscrete(pp, psum); 
					
					int kk = idx / a[m].length; 
					int ii = a[m][idx % a[m].length].getAuthorId(); 
					
					// reassign
					z[m][n] = kk;
					x[m][n] = ii; 
				} // n
			} // m
		} // iter	
	}
	
	@Override
	public double ppx() {
		int numDocs = corpus.getNumDocs();
		int numTopics = param.getNumTopics();
		BylineInformation[][] a = corpus.getDocAuthors(); 
		
		double[][] logVartheta = estimator.getVartheta(true);
		double[][] logMultivariateGaussianDensity = estimator.getLogMultivariateGaussianDensity();

		double loglik = 0.0;
		int numTokens = 0; 
		for (int m = 0; m < numDocs; m++) {
			int[] w = corpus.getDocWords(m);

			for (int n = 0; n < w.length; n++) {
				int t = w[n];

				double[][] tmp = new double[numTopics][a[m].length];
				for (int k = 0; k < numTopics; k++) {
					for (int i = 0; i < a[m].length; i++) {
						int authorId = a[m][i].getAuthorId();
						tmp[k][i] += logVartheta[authorId][k] + Math.log(credits[m][i])
								+ logMultivariateGaussianDensity[k][t];
					}
				}
				loglik += MathUtils.logsum(tmp);
			}

			numTokens += w.length;
		}
		
		return Math.exp(-loglik / numTokens);
	}
	
	@Override
	public void printAssign(File f) {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(f));
			printAssign(writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace(); 
		}
	}

	@Override
	public void printAssign(PrintWriter pw) {
		int numDocs = corpus.getNumDocs(); 
		
		for (int m = 0; m < numDocs; m++) {
			int[] w = corpus.getDocWords(m); 
			
			for (int n = 0; n < w.length; n++) {
				pw.print(w[n] + ":" + z[m][n] + ":" + x[m][n] + " "); 
			}
			pw.println(); 
		}
	}
}
