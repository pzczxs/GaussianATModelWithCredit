package cn.edu.bjut.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

import org.apache.commons.math3.special.Gamma;
import org.ejml.alg.dense.decomposition.TriangularSolver;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.interfaces.decomposition.CholeskyDecomposition;
import org.ejml.ops.CommonOps;

import cn.edu.bjut.corpus.CorpusResolver;
import cn.edu.bjut.corpus.LabelCorpus;
import cn.edu.bjut.corpus.Wikipedia;
import cn.edu.bjut.credit.BylineInformation;
import cn.edu.bjut.credit.CreditCalculator;
import cn.edu.bjut.credit.CreditParameter;
import cn.edu.bjut.tools.IntegerDoublePair;
import cn.edu.bjut.tools.IntegerUnorderedPair;
import cn.edu.bjut.utils.CokusRandom;
import cn.edu.bjut.utils.MapUtils;
import cn.edu.bjut.utils.MathUtils;

/*
 * Gibbs sampling algorithm for Gaussian Author-Topic (AT) <sup>credit</sup> model. 
 * 
 * References: 
 * [1] 
 * 
 * Author: XU, Shuo (pzczxs@gmail.com)
 * */
public class GaussianATCreditEstimator implements IEstimator, IPerplexity {
	private String filebase; 
	private LabelCorpus corpus;
	private GaussianATCreditParameter param;
	private int numTerms; // number of unique words
	private int numAuthors; // number of unique authors
	private int numDimensions; // number of dimensions for word embeddings

	private CokusRandom rand;
	private boolean stopEarlyFlag; 
	
	private CreditParameter creditParam; 
	private double[][] credits; 
	
	private DenseMatrix64F[] wordVectors; 
	
	private CholeskyDecomposition<DenseMatrix64F> decomposer; 
	
	//  caching the choelsky of prior psi
	private DenseMatrix64F cholPsi;

	// mean vector associated with each topic in the current iteration. This is the
	// Bayesian mean (i.e has the prior part too)
	private DenseMatrix64F[] topicMeans; // \{\vec{\mu}_k\}_{k = 1}^K

	// Cholesky Lower Triangular Decomposition of covariance matrix associated with
	// each topic.
	private DenseMatrix64F[] topicCholeskyLTriangularMat;

	// log-determinant of covariance matrix for each topic. Since 0.5*logDet is
	// required in (see logMultivariateTDensity), therefore that value is kept.
	private double[] logDeterminants;
	
	private int[][] nak; // size: numAuthors x numTopics, each element is n_a^{(k)}
	private int[] na; // size: numAuthors, \vec{n}_a
	private int[][] nkt; // size: numTopics x numTerms, each element is n_k^{(t)}
	private int[] nk; // size: numTopics, \vec{n}_k

	private int[][] z;
	private int[][] x; 

	private double alphaSum; // K * \alpha
	private double betaSum; // V * \beta
	
	public GaussianATCreditEstimator(String filebase, long seed, boolean stopEarlyFlag, 
			DenseMatrix64F[] wordVectors, GaussianATCreditParameter param, CreditParameter creditParam) {
		this.filebase = filebase;

		this.corpus = new LabelCorpus(filebase);
		this.param = param;
		this.numTerms = corpus.getNumTerms();
		this.numAuthors = corpus.getAuthorsV(); 
		
		this.creditParam = creditParam;
		this.credits = CreditCalculator.calculate(corpus.getDocAuthors(), creditParam); 
		
		this.alphaSum = param.getNumTopics() * param.getAlpha();
		this.betaSum = numTerms * param.getBeta(); 
		
		this.rand = new CokusRandom(seed);
		this.stopEarlyFlag = stopEarlyFlag; 
		
		this.wordVectors = wordVectors; 
		this.numDimensions = wordVectors[0].getNumElements(); 
		
		this.decomposer = DecompositionFactory.chol(numDimensions, true);
		
		cholPsi = new DenseMatrix64F(numDimensions, numDimensions);
		CommonOps.addEquals(cholPsi, param.getPsi());
		if (!decomposer.decompose(cholPsi)) { // cholesky decomp
			System.out.println("Matrix couldn't be Cholesky decomposed");
			System.exit(1);
		}
	}
	
	public GaussianATCreditEstimator(String filebase, long seed, DenseMatrix64F[] wordVectors, 
			GaussianATCreditParameter param, CreditParameter creditParam) {
		this(filebase, seed, true, wordVectors, param, creditParam); 
	}
	
	public GaussianATCreditParameter getParameter() {
		return this.param; 
	}
	
	public CreditParameter getCreditParameter() {
		return this.creditParam; 
	}
	
	public DenseMatrix64F[] getWordVectors() {
		return this.wordVectors; 
	}
	
	/**
	 * updates params -- mean and the cholesky decomp of covariance matrix using
	 * rank1 update (topic added) or downdate (topic removed)
	 * 
	 * @param topicId
	 * @param wordId
	 * @param isRemoved
	 */
	private void updateTopicParams(int topicId, int wordId, boolean isRemoved) {
		int count = nk[topicId];
		double kappa = param.getKappa() + count;
		double nu = param.getNu() + count;
		double scaleTdistrn = (kappa + 1.0) / (kappa * (nu - numDimensions + 1.0));

		DenseMatrix64F oldLTriangularDecomp = topicCholeskyLTriangularMat[topicId];
		if (isRemoved) {
			/**
			 * Now use the rank1 downdate to calculate the cholesky decomposition of the
			 * updated covariance matrix the update equation is \mathbf{\Sigma}_(N+1) = \mathbf{\Sigma}_(N) -
			 * (\kappa_n + 1)/(\kappa_n)(\vec{x}_{n} - \vec{\mu}_{n-1})(\vec{x}_{n} - \vec{\mu}_{n-1})^T therefore x =
			 * sqrt((\kappa_n - 1)/(\kappa_n)) (\vec{x}_{n} - \vec{\mu}_{n}) Note here \vec{\mu}_n will be the
			 * mean before updating. After updating \mathbf{\Sigma}_n, we will update \vec{\mu}_n.
			 */
			DenseMatrix64F x = new DenseMatrix64F(numDimensions, 1);
			CommonOps.sub(wordVectors[wordId], topicMeans[topicId], x); // calculate (\vec{x}_{n} - \vec{\mu}_{n-1})
			double coeff = Math.sqrt((kappa + 1.0) / kappa);
			CommonOps.scale(coeff, x);
			MathUtils.cholRank1Downdate(oldLTriangularDecomp, x);
			topicCholeskyLTriangularMat[topicId] = oldLTriangularDecomp;// the cholRank1Downdate modifies the
																			// oldLTriangularDecomp, therefore putting
																			// it back to the map
			// updateMean(tableId);
			DenseMatrix64F newMean = new DenseMatrix64F(numDimensions, 1);
			CommonOps.scale(kappa + 1.0, topicMeans[topicId], newMean);
			CommonOps.subEquals(newMean, wordVectors[wordId]);
			CommonOps.divide(kappa, newMean);
			topicMeans[topicId] = newMean;

		} else { // new customer is added
			DenseMatrix64F newMean = new DenseMatrix64F(numDimensions, 1);
			CommonOps.scale(kappa - 1.0, topicMeans[topicId], newMean);
			CommonOps.addEquals(newMean, wordVectors[wordId]);
			CommonOps.divide(kappa, newMean);
			topicMeans[topicId] = newMean;
			/**
			 * The rank1 update equation is \mathbf{\Sigma_{n + 1}} = \mathbf{\Sigma_{n}} + (\kappa_n + 1)/(\kappa_n) * 
			 * (\vec{x}_{n + 1} - \vec{\mu}_{n + 1})(\vec{x}_{n + 1} - \vec{\mu}_{n + 1})^T
			 */
			DenseMatrix64F x = new DenseMatrix64F(numDimensions, 1);
			CommonOps.sub(wordVectors[wordId], topicMeans[topicId], x); // calculate (\vec{x}_{n} - \vec{\mu}_{n-1})
			double coeff = Math.sqrt(kappa / (kappa - 1.0));
			CommonOps.scale(coeff, x);
			MathUtils.cholRank1Update(oldLTriangularDecomp, x);
			topicCholeskyLTriangularMat[topicId] = oldLTriangularDecomp;// the cholRank1Downdate modifies the
																			// oldLTriangularDecomp, therefore putting
																			// it back to the map
		}
		
		// calculate the 0.5*log(det) + D/2*scaleTdistrn; the scaleTdistrn is because
		// the posterior predictive distribution sends in a scaled value of \Sigma
		double logDet = 0.0;
		for (int d = 0; d < numDimensions; d++) {
			logDet += Math.log(oldLTriangularDecomp.get(d, d));
		}
		logDet += numDimensions * Math.log(scaleTdistrn) / 2.0;

		logDeterminants[topicId] = logDet;
	}

	@Override
	public void init() {
		// first check the prior degrees of freedom. It has to be >= number of dimensions
		if (param.getNu() < numDimensions) {
			System.out.println("The initial degrees of freedom of the prior is less than the dimension!. "
					+ "Setting it to the number of dimension: "	+ numDimensions);
			param.setNu(numDimensions);
		}
		
		int numTopics = param.getNumTopics(); 
		
		topicMeans = new DenseMatrix64F[numTopics]; 
		topicCholeskyLTriangularMat = new DenseMatrix64F[numTopics]; 
		logDeterminants = new double[numTopics]; 
		
		// storing zeros in sumTableCustomers and later will keep on adding each
		// customer. Also initialize tableInverseCovariances and determinants
		double scaleTdistrn = (param.getKappa() + 1.0) / (param.getKappa() * (param.getNu() - numDimensions + 1.0));
		for (int k = 0; k < numTopics; k++) {
			DenseMatrix64F priorMean = new DenseMatrix64F(param.getMu());
			DenseMatrix64F initialCholesky = new DenseMatrix64F(cholPsi);

			// calculate the 0.5*log(det) + D/2*scaleTdistrn; the scaleTdistrn is because
			// the posterior predictive distribution sends in a scaled value of \Sigma
			double logDet = 0.0;
			for (int l = 0; l < numDimensions; l++) {
				logDet += Math.log(cholPsi.get(l, l));
			}
			logDet += numDimensions * Math.log(scaleTdistrn) / 2.0;

			logDeterminants[k] = logDet;
			topicMeans[k] = priorMean;
			topicCholeskyLTriangularMat[k] = initialCholesky;
		}
		
		int numDocs = corpus.getNumDocs(); 
		int[][] w = corpus.getDocWords(); 
		BylineInformation[][] a = corpus.getDocAuthors(); 
		
		// allocate
		nak = new int[numAuthors][numTopics];
		na = new int[numAuthors];
		nkt = new int[numTopics][numTerms];
		nk = new int[numTopics];
		z = new int[numDocs][];
		x = new int[numDocs][];
		
		// randomly assign customers to tables.
		for (int m = 0; m < numDocs; m++) { // for each document
			z[m] = new int[w[m].length];
			x[m] = new int[w[m].length]; 
			for (int n = 0; n < w[m].length; n++) {
				int t = w[m][n];
				int k = rand.nextInt(numTopics);
				int idx = rand.nextDiscrete(credits[m]); 
				int i = a[m][idx].getAuthorId();
				
				z[m][n] = k;
				x[m][n] = i;
				nak[i][k]++;
				na[i]++;
				nkt[k][t]++;
				nk[k]++;
				
				updateTopicParams(k, t, false);
			}
		}
	}
	
	@Override
	public void init(int niter) {
		int numDocs = corpus.getNumDocs(); 
		int numTopics = param.getNumTopics(); 
		int[][] w = corpus.getDocWords(); 
		BylineInformation[][] a = corpus.getDocAuthors(); 
		double alpha = param.getAlpha(); 
		double beta = param.getBeta(); 
		
		Map<Integer, LinkedHashSet<Integer>> previousTopicIdToTermIdSet = null; 
		Map<Integer, LinkedHashSet<Integer>> currentTopicIdToTermIdSet = null; 
		int consecutiveCount = 0; 
		
		for (int iter = 0; iter < niter; iter++) {
			System.out.print("initialization iter: " + iter);
			for (int m = 0; m < numDocs; m++) {
				double[] pp = new double[a[m].length * numTopics]; 
				
				for (int n = 0; n < w[m].length; n++) {
					int k = z[m][n];
					int t = w[m][n];
					int i = x[m][n]; 
					
					// decrement
					nak[i][k]--;
					na[i]--; 
					nkt[k][t]--;
					nk[k]--;
					
					// recalculate table parameters for this topic
					//updateTopicParams(k, t, true);
					
					// compute weights
					double psum = 0;
					for (int kk = 0; kk < numTopics; kk++) {
						double tmp = (nkt[kk][t] + beta) / (nk[kk] + betaSum);
						
						for (int ii = 0; ii < a[m].length; ii++) {
							int idx = kk*a[m].length + ii; 
							int aid = a[m][ii].getAuthorId(); 
							pp[idx] = (nak[aid][kk] + alpha) / (na[aid] + alphaSum) * tmp * credits[m][ii]; 
							psum += pp[idx];
						}
					}
					
					// sample
					int idx = rand.nextDiscrete(pp, psum); 
					
					int kk = idx / a[m].length; 
					int ii = a[m][idx % a[m].length].getAuthorId(); 
					
					// reassign and increment
					z[m][n] = kk;
					x[m][n] = ii; 
					nak[ii][kk]++;
					na[ii]++; 
					nkt[kk][t]++;
					nk[kk]++;
					
					// recalculate table parameters for this new topic
					//updateTopicParams(kk, t, false);
				} // n
			} // m
			
			//System.out.print("\t" + ppx() + "\t" + avgLogLikelihood());
			
			currentTopicIdToTermIdSet = getTopWordIdSet(); 
			if (stopEarlyFlag) {
				if (previousTopicIdToTermIdSet != null 	&& MapUtils.equals(currentTopicIdToTermIdSet, previousTopicIdToTermIdSet)) {
					consecutiveCount++;
				} else {
					consecutiveCount = 1;
				}
			}
			
			double[] scores = npmi(currentTopicIdToTermIdSet); 
			for (int k = 0; k < numTopics; k++) {
				System.out.print("\t" + scores[k]);
			}
			System.out.println("\t" + MathUtils.average(scores) + "\t" + avgCosine(currentTopicIdToTermIdSet)); 
			
			for (Map.Entry<Integer, LinkedHashSet<Integer>> entry: currentTopicIdToTermIdSet.entrySet()) {
				System.out.println("\t" + entry.getKey() + ": " + entry.getValue()); 
			}
			
			if (stopEarlyFlag) {
				if (consecutiveCount == 3) {
					break; 
				}
				
				previousTopicIdToTermIdSet = currentTopicIdToTermIdSet; 
			}
		} // iter
	}
	
	/** 
	 * log(PDF) of multivariate Student's t-distribution
	 */
	public double[][] getLogMultivariateTDensity() {
		int numTopics = param.getNumTopics(); 
		
		double[][] logMultivariateTDensity = new double[numTopics][numTerms]; 
		for (int k = 0; k < numTopics; k++) {
			double kappa = param.getKappa() + nk[k];
			double nu = param.getNu() + nk[k];
			double scaleTdistrn = Math.sqrt((kappa + 1.0) / (kappa * (nu - numDimensions + 1.0)));
			
			for (int v = 0; v < numTerms; v++) {
				DenseMatrix64F x = wordVectors[v]; 
				
				// Since I am storing lower triangular matrices, therefore it is easy to
				// calculate the value of (\vec{x} - \vec{\mu})^T \mathbf{\Sigma}^{-1} (\vec{x} - \vec{\mu})
				// therefore I am gonna use triangular solver
				// first calculate (\vec{x} - \vec{\mu})
				DenseMatrix64F xMinusMu = new DenseMatrix64F(numDimensions, 1);
				CommonOps.sub(x, topicMeans[k], xMinusMu);
				// now scale the lower triangular matrix
				DenseMatrix64F lowerTriangularChol = new DenseMatrix64F(numDimensions, numDimensions);
				CommonOps.scale(scaleTdistrn, topicCholeskyLTriangularMat[k], lowerTriangularChol);
				TriangularSolver.solveL(lowerTriangularChol.data, xMinusMu.data, numDimensions); // now xMinusNu has the solved value
				// Now take xTx
				DenseMatrix64F xMinusMuTranspose = new DenseMatrix64F(1, numDimensions);
				CommonOps.transpose(xMinusMu, xMinusMuTranspose);
				DenseMatrix64F mul = new DenseMatrix64F(1, 1);
				CommonOps.mult(xMinusMuTranspose, xMinusMu, mul);
				double val = mul.get(0, 0);
				
				nu -= numDimensions - 1.0;
				logMultivariateTDensity[k][v] = Gamma.logGamma((nu + numDimensions) / 2.0)
						- (Gamma.logGamma(nu / 2.0) + numDimensions / 2.0 * (Math.log(nu) + Math.log(Math.PI))
								+ logDeterminants[k] + (nu + numDimensions) / 2.0 * Math.log(1.0 + val / nu));
			}
		}
		
		return logMultivariateTDensity; 
	}
	
	/** 
	 * log(PDF) of multivariate Student's t-distribution
	 */
	public double[] getLogMultivariateTDensity(int termId) {
		int numTopics = param.getNumTopics(); 
		
		double[] logMultivariateTDensity = new double[numTopics]; 
		for (int k = 0; k < numTopics; k++) {
			double kappa = param.getKappa() + nk[k];
			double nu = param.getNu() + nk[k];
			double scaleTdistrn = Math.sqrt((kappa + 1.0) / (kappa * (nu - numDimensions + 1.0)));
			
			DenseMatrix64F x = wordVectors[termId]; 
			
			// Since I am storing lower triangular matrices, therefore it is easy to
			// calculate the value of (\vec{x} - \vec{\mu})^T \mathbf{\Sigma}^{-1} (\vec{x} - \vec{\mu})
			// therefore I am gonna use triangular solver
			// first calculate (\vec{x} - \vec{\mu})
			DenseMatrix64F xMinusMu = new DenseMatrix64F(numDimensions, 1);
			CommonOps.sub(x, topicMeans[k], xMinusMu);
			// now scale the lower triangular matrix
			DenseMatrix64F lowerTriangularChol = new DenseMatrix64F(numDimensions, numDimensions);
			CommonOps.scale(scaleTdistrn, topicCholeskyLTriangularMat[k], lowerTriangularChol);
			TriangularSolver.solveL(lowerTriangularChol.data, xMinusMu.data, numDimensions); // now xMinusNu has the solved value
			// Now take xTx
			DenseMatrix64F xMinusMuTranspose = new DenseMatrix64F(1, numDimensions);
			CommonOps.transpose(xMinusMu, xMinusMuTranspose);
			DenseMatrix64F mul = new DenseMatrix64F(1, 1);
			CommonOps.mult(xMinusMuTranspose, xMinusMu, mul);
			double val = mul.get(0, 0);
			
			nu -= numDimensions - 1.0;
			logMultivariateTDensity[k] = Gamma.logGamma((nu + numDimensions) / 2.0)
					- (Gamma.logGamma(nu / 2.0) + numDimensions / 2.0 * (Math.log(nu) + Math.log(Math.PI))
							+ logDeterminants[k] + (nu + numDimensions) / 2.0 * Math.log(1.0 + val / nu));
		}
		
		return logMultivariateTDensity; 
	}
	
	/** 
	 * log(PDF) of multivariate Student's t-distribution
	 */
	public double getLogMultivariateTDensity(int topicId, int termId) {
		double kappa = param.getKappa() + nk[topicId];
		double nu = param.getNu() + nk[topicId];
		double scaleTdistrn = Math.sqrt((kappa + 1.0) / (kappa * (nu - numDimensions + 1.0)));
		
		DenseMatrix64F x = wordVectors[termId]; 
		
		// Since I am storing lower triangular matrices, therefore it is easy to
		// calculate the value of (\vec{x} - \vec{\mu})^T \mathbf{\Sigma}^{-1} (\vec{x} - \vec{\mu})
		// therefore I am gonna use triangular solver
		// first calculate (\vec{x} - \vec{\mu})
		DenseMatrix64F xMinusMu = new DenseMatrix64F(numDimensions, 1);
		CommonOps.sub(x, topicMeans[topicId], xMinusMu);
		// now scale the lower triangular matrix
		DenseMatrix64F lowerTriangularChol = new DenseMatrix64F(numDimensions, numDimensions);
		CommonOps.scale(scaleTdistrn, topicCholeskyLTriangularMat[topicId], lowerTriangularChol);
		TriangularSolver.solveL(lowerTriangularChol.data, xMinusMu.data, numDimensions); // now xMinusNu has the solved value
		// Now take xTx
		DenseMatrix64F xMinusMuTranspose = new DenseMatrix64F(1, numDimensions);
		CommonOps.transpose(xMinusMu, xMinusMuTranspose);
		DenseMatrix64F mul = new DenseMatrix64F(1, 1);
		CommonOps.mult(xMinusMuTranspose, xMinusMu, mul);
		double val = mul.get(0, 0);
		
		nu -= numDimensions - 1.0;
		double logMultivariateTDensity = Gamma.logGamma((nu + numDimensions) / 2.0)
				- (Gamma.logGamma(nu / 2.0) + numDimensions / 2.0 * (Math.log(nu) + Math.log(Math.PI))
						+ logDeterminants[topicId] + (nu + numDimensions) / 2.0 * Math.log(1.0 + val / nu));
		
		return logMultivariateTDensity; 
	}

	@Override
	public void estimate(int niter) {
		int numDocs = corpus.getNumDocs(); 
		int numTopics = param.getNumTopics(); 
		int[][] w = corpus.getDocWords(); 
		BylineInformation[][] a = corpus.getDocAuthors(); 
		double alpha = param.getAlpha(); 
		
		Map<Integer, LinkedHashSet<Integer>> previousTopicIdToTermIdSet = null; 
		Map<Integer, LinkedHashSet<Integer>> currentTopicIdToTermIdSet = null; 
		int consecutiveCount = 0; 
		
		for (int iter = 0; iter < niter; iter++) {
			System.out.print("estimation iter: " + iter);
			
			for (int m = 0; m < numDocs; m++) {
				double[] pp = new double[a[m].length * numTopics]; 
				
				for (int n = 0; n < w[m].length; n++) {
					int k = z[m][n];
					int t = w[m][n];
					int i = x[m][n]; 
					
					// decrement
					nak[i][k]--;
					na[i]--; 
					nkt[k][t]--;
					nk[k]--;
					
					// recalculate table parameters for this topic
					updateTopicParams(k, t, true);
					
					// compute weights
					double logMax = Double.NEGATIVE_INFINITY;
					for (int kk = 0; kk < numTopics; kk++) {
						double logLikelihood = getLogMultivariateTDensity(kk, t); 
						for (int ii = 0; ii < a[m].length; ii++) {
							int idx = kk*a[m].length + ii; 
							int aid = a[m][ii].getAuthorId(); 
							pp[idx] = Math.log(nak[aid][kk] + alpha) - Math.log(na[aid] + alphaSum) + logLikelihood + Math.log(credits[m][ii]); 
							
							if (pp[idx] > logMax) {
								logMax = pp[idx]; 
							}
						}
					}
					
					// to prevent overflow, subtract by log(p_max). This is because when we will be
					// normalizing after exponentiating, each entry will be exp(log p_i - log p_max
					// )/\Sigma_i exp(log p_i - log p_max)
					// the log p_max cancels put and prevents overflow in the exponentiating phase.
					double psum = 0;
					for (int idx = 0; idx < pp.length; idx++) {
						pp[idx] = Math.exp(pp[idx] - logMax); 
						psum += pp[idx]; 
					}
					
					// sample
					int idx = rand.nextDiscrete(pp, psum); 
					
					int kk = idx / a[m].length; 
					int ii = a[m][idx % a[m].length].getAuthorId(); 
					
					// reassign and increment
					z[m][n] = kk;
					x[m][n] = ii; 
					nak[ii][kk]++;
					na[ii]++; 
					nkt[kk][t]++;
					nk[kk]++;
					
					// recalculate table parameters for this new topic
					updateTopicParams(kk, t, false);
				} // n
			} // m
			
			//System.out.print("\t" + ppx() + "\t" + avgLogLikelihood());
			
			currentTopicIdToTermIdSet = getTopWordIdSet(); 
			if (stopEarlyFlag) {
				if (previousTopicIdToTermIdSet != null 	&& MapUtils.equals(currentTopicIdToTermIdSet, previousTopicIdToTermIdSet)) {
					consecutiveCount++;
				} else {
					consecutiveCount = 1;
				}
				//System.out.println("\tconsecutiveCount = " + consecutiveCount); 
			}
			
			double[] scores = npmi(currentTopicIdToTermIdSet); 
			for (int k = 0; k < numTopics; k++) {
				System.out.print("\t" + scores[k]);
			}
			System.out.println("\t" + MathUtils.average(scores) + "\t" + avgCosine(currentTopicIdToTermIdSet)); 
			
			for (Map.Entry<Integer, LinkedHashSet<Integer>> entry: currentTopicIdToTermIdSet.entrySet()) {
				System.out.println("\t" + entry.getKey() + ": " + entry.getValue()); 
			}
			
			if (stopEarlyFlag) {
				if (consecutiveCount == 3) {
					break; 
				}
				
				previousTopicIdToTermIdSet = currentTopicIdToTermIdSet; 
			}
		} //iter
	}
	
	public double avgCosine(Map<Integer, ? extends Set<Integer>> topicIdToTermIdSet) {
		int numTopics = param.getNumTopics(); 
		double similarity = 0.0;
		
		for (int i = 0; i < numTopics - 1; i++) {
			DenseMatrix64F vector1 = average(topicIdToTermIdSet.get(i)); 
			for (int j = i + 1; j < numTopics; j++) {
				DenseMatrix64F vector2 = average(topicIdToTermIdSet.get(j)); 
				
				similarity += MathUtils.cosine(vector1, vector2); 
			}
		}
		
		return 2.0 * similarity / (numTopics * (numTopics - 1)); 
	}
	
	private DenseMatrix64F average(Set<Integer> termIdSet) {
		if (termIdSet == null || termIdSet.isEmpty()) {
			return null; 
		}
		
		DenseMatrix64F mean = new DenseMatrix64F(numDimensions, 1);
		for (int termId: termIdSet) {
			CommonOps.addEquals(mean, wordVectors[termId]);
		}
		CommonOps.divide(termIdSet.size(), mean);
		
		return mean; 
	}
	
	/**
	 * Normalized Pointwise Mutual Information (NPMI)
	 * */
	public double[] npmi(Map<Integer, ? extends Set<Integer>> topicIdToTermIdSet) {
		int numTopics = param.getNumTopics();
		Wikipedia wikipedia = new Wikipedia(); 	
		
		NavigableSet<Integer> termIdSet = MapUtils.mergeValues(topicIdToTermIdSet); 
		Map<Integer, Integer> termIdToCount = wikipedia.getTermCount(termIdSet); 
		Map<IntegerUnorderedPair, Integer> termIdPairToCount = wikipedia.getTermPairCount(termIdSet); 
		long totalWindows = wikipedia.getTotalWindows(); 
		
		double[] scores = new double[numTopics]; 
		PointwiseMutualInformation pmi = new PointwiseMutualInformation(totalWindows, termIdToCount, termIdPairToCount); 
		for (int k = 0; k < numTopics; k++) {
			scores[k] = pmi.score(topicIdToTermIdSet.get(k), true); 
		}
		
		return scores; 
	}
	
	/** 
	 * log(PDF) of multivariate Gaussian distribution
	 */
	public double[][] getLogMultivariateGaussianDensity() {
		int numTopics = param.getNumTopics();
		double[][] logMultivariateGaussianDensity = new double[numTopics][numTerms]; 
		
		// first divide the cholesky's by the scale term
		// but before that calculate the number of words sitting in each table
		double[] scalar = new double[numTopics];
		for (int k = 0; k < numTopics; k++) {
			scalar[k] = param.getNu() + nk[k] - numDimensions;
		}

		// now divide the cholesky's by sqrt(scalar)
		DenseMatrix64F[] scaledCholeskies = new DenseMatrix64F[numTopics];
		for (int k = 0; k < numTopics; k++) {
			DenseMatrix64F scaledCholesky = new DenseMatrix64F(numDimensions, numDimensions);
			CommonOps.divide(Math.sqrt(scalar[k]), topicCholeskyLTriangularMat[k], scaledCholesky);
			scaledCholeskies[k] = scaledCholesky;
		}

		// logDensity of mulitvariate Normal is given by -0.5*(log |\mathbf{\Sigma}| +
		// D*log(2*\pi) + (\vec{x} - \vec{\mu})^T \mathbf{\Sigma}^{-1} (\vec{x} -
		// \vec{\mu}))
		// calculate log |\mathbf{\Sigma}| for all topics from cholesky
		double[] logDets = new double[numTopics];
		for (int k = 0; k < numTopics; k++) {
			for (int d = 0; d < numDimensions; d++) {
				logDets[k] += Math.log(scaledCholeskies[k].get(d, d));
			}
		}
		
		for (int k = 0; k < numTopics; k++) {
			for (int v = 0; v < numTerms; v++) {
				DenseMatrix64F x = wordVectors[v]; 
				
				// calculate (\vec{x} - \vec{\mu})
				DenseMatrix64F xMinusMu = new DenseMatrix64F(numDimensions, 1);
				CommonOps.sub(x, topicMeans[k], xMinusMu);
				DenseMatrix64F lowerTriangularChol = scaledCholeskies[k];
				TriangularSolver.solveL(lowerTriangularChol.data, xMinusMu.data, numDimensions); // xMinusMu has the solved value
				DenseMatrix64F xMinusMuTranpose = new DenseMatrix64F(1, numDimensions);
				CommonOps.transpose(xMinusMu, xMinusMuTranpose);
				DenseMatrix64F mul = new DenseMatrix64F(1, 1);
				CommonOps.mult(xMinusMuTranpose, xMinusMu, mul);
				double val = mul.get(0, 0);
				logMultivariateGaussianDensity[k][v] = -0.5 * (val + numDimensions * Math.log(2.0 * Math.PI)) + logDets[k];
			}
			
			
		}
		
		return logMultivariateGaussianDensity; 
	}
	
	private int indexOf(BylineInformation[] a, int authorId) {
		int idx = -1; 
		
		for (int i = 0; i < a.length; i++) {
			if (a[i].getAuthorId() == authorId) {
				idx = i; 
				break; 
			}
		}
		
		return idx; 
	}
	
	public double avgLogLikelihood() {
		int numDocs = corpus.getNumDocs();
		BylineInformation[][] a = corpus.getDocAuthors(); 
		
		double[][] logVartheta = getVartheta(true); 
		double[][] logMultivariateGaussianDensity = getLogMultivariateGaussianDensity(); 
		
		double loglik = 0.0;
		int numTokens = 0; 
		for (int m = 0; m < numDocs; m++) {
			int[] w = corpus.getDocWords(m); 
			
			for (int n = 0; n < w.length; n++) {
				int k = z[m][n];
				int t = w[n];
				int i = x[m][n]; 
				int idx = indexOf(a[m], i); 
				
				loglik += logVartheta[i][k] + Math.log(credits[m][idx]) + logMultivariateGaussianDensity[k][t];
			}
			
			numTokens += w.length; 
		}
		
		return (loglik / numTokens);
	}
	
	@Override
	public double ppx() {
		int numDocs = corpus.getNumDocs();
		int numTopics = param.getNumTopics();
		BylineInformation[][] a = corpus.getDocAuthors(); 
		
		double[][] logVartheta = getVartheta(true); 
		double[][] logMultivariateGaussianDensity = getLogMultivariateGaussianDensity(); 
		
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
						tmp[k][i] += logVartheta[authorId][k] + Math.log(credits[m][i]) + logMultivariateGaussianDensity[k][t];
					}
				}
				loglik += MathUtils.logsum(tmp); 
			}
			
			numTokens += w.length; 
		}
		
		return Math.exp(-loglik / numTokens);
	}
	
	/*
	 * @flag = true: log
	 * */
	public double[][] getVartheta(boolean flag) {
		int numTopics = param.getNumTopics();
		double alpha = param.getAlpha();

		double[][] vartheta = new double[numAuthors][numTopics];
		for (int i = 0; i < numAuthors; i++) {
			for (int k = 0; k < numTopics; k++) {
				vartheta[i][k] = (nak[i][k] + alpha) / (na[i] + alphaSum);
				
				if (flag) {
					vartheta[i][k] = Math.log(vartheta[i][k]); 
					if (Double.isNaN(vartheta[i][k])) {
						System.err.println("The vartheta[" +  i + "][" + k + "] is NaN.");
						System.exit(-1);
					}
				}
			}
		}

		return vartheta;
	}
	
	/*
	 * @flag = true: log
	 * */
	public double[][] getVarphi(boolean flag) {
		int numTopics = param.getNumTopics(); 
		double beta = param.getBeta(); 
		
		double[][] varphi = new double[numTopics][numTerms]; 
		for (int k = 0; k < numTopics; k++) {
			for (int v = 0; v < numTerms; v++) {
				varphi[k][v] = (nkt[k][v] + beta) / (nk[k] + betaSum); 
				
				if (flag) {
					varphi[k][v] = Math.log(varphi[k][v]); 
					if (Double.isNaN(varphi[k][v])) {
						System.err.println("The varphi[" +  k + "][" + v + "] is NaN.");
						System.exit(-1);
					}
				}
			}
		}
		
		return varphi; 
	}
	
	/*
	 * @flag = true: log
	 * */
	public double getVarphi(int k, int v, boolean flag) {
		double beta = param.getBeta(); 
		
		double varphi = (nkt[k][v] + beta) / (nk[k] + betaSum); 
		if (flag) {
			varphi = Math.log(varphi); 
			if (Double.isNaN(varphi)) {
				System.err.println("The varphi[" +  k + "][" + v + "] is NaN.");
				System.exit(-1);
			}
		}
		
		return varphi; 
	}
	
	/**
	 * topicId -> a set of top word ids for topicId
	 * */
	private Map<Integer, LinkedHashSet<Integer>> getTopWordIdSet() {
		int numTopics = param.getNumTopics();
		Map<Integer, LinkedHashSet<Integer>> topicIdToWordIdSet = new HashMap<Integer, LinkedHashSet<Integer>>(); 
		
		for (int k = 0; k < numTopics; k++) {
			topicIdToWordIdSet.put(k, getTopWordIdSet(k)); 	
		}
		
		return topicIdToWordIdSet; 
	}
	
	public LinkedHashSet<Integer> getTopWordIdSet(int k) {
		IntegerDoublePair[] unigrams = new IntegerDoublePair[numTerms];
		for (int v = 0; v < numTerms; v++) {
			unigrams[v] = new IntegerDoublePair(v, (double)nkt[k][v]); 
		}
		Arrays.sort(unigrams); 
		
		LinkedHashSet<Integer> topWordIdSet = new LinkedHashSet<Integer>(); 
		for (int i = 0; i < Math.min(param.getNumTopWordsForPMI(), numTerms); i++) {
			topWordIdSet.add(unigrams[i].getFirst()); 
		}
		
		return topWordIdSet; 
	}
	
	public void printTopWords(File f) {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(f));
			printTopWords(writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void printTopWords(PrintWriter pw) {
		double beta = param.getBeta();
		CorpusResolver cr = new CorpusResolver(filebase);
		int numTopics = param.getNumTopics();
		
		for (int k = 0; k < numTopics; k++) {
			IntegerDoublePair[] unigrams = new IntegerDoublePair[numTerms];
			for (int v = 0; v < numTerms; v++) {
				unigrams[v] = new IntegerDoublePair(v, (double)nkt[k][v]); 
			}
			Arrays.sort(unigrams); 
			
			pw.write("Topic " + k + "th:\n"); 
			for (int i = 0; i < Math.min(param.getNumTopWords(), numTerms); i++) {
				pw.write("\t" + cr.getTerm(unigrams[i].getFirst()) + "\t" + (unigrams[i].getSecond() + beta) / (nk[k] + betaSum) + "\t"); 
				
				pw.write("\n"); 
			}
		}
	}
	
	public void printTopAuthors(File f) {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(f));
			printTopAuthors(writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace(); 
		}
	} 
	
	public void printTopAuthors(PrintWriter pw) {
		double alpha = param.getAlpha();
		CorpusResolver cr = new CorpusResolver(filebase); 
		int numTopics = param.getNumTopics(); 
		
		for (int k = 0; k < numTopics; k++) {
			IntegerDoublePair[] authors = new IntegerDoublePair[numAuthors];
			for (int i = 0; i < numAuthors; i++) {
				authors[i] = new IntegerDoublePair(i, (nak[i][k] + alpha) / (na[i] + alphaSum)); 
			}
			Arrays.sort(authors); 
			
			pw.write("Topic " + k + "th:\n"); 
			for (int i = 0; i < Math.min(numAuthors, param.getNumTopAuthors()); i++) {
				pw.write("\t" + cr.getAuthor(authors[i].getFirst()) + "\t" + authors[i].getSecond() + "\n");
			}
		}
	}
	
	public void printVartheta(File f) {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(f));
			printVartheta(writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace(); 
		}
	}

	public void printVartheta(PrintWriter pw) {
		int numTopics = param.getNumTopics();
		double alpha = param.getAlpha();
		
		for (int i = 0; i < numAuthors; i++) {
			for (int k = 0; k < numTopics; k++) {
				if (nak[i][k] != 0) {
					double vartheta = (nak[i][k] + alpha) / (na[i] + alphaSum);
					pw.println(i + " " + k + " " + nak[i][k] + " " + na[i] + " " + vartheta); 
				}
			}
		}
	} 
	
	public void printVarphi(File f) {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(f));
			printVarphi(writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace(); 
		}
	}
	
	public void printVarphi(PrintWriter pw) {
		int numTopics = param.getNumTopics(); 
		double beta = param.getBeta(); 
		
		for (int k = 0; k < numTopics; k++) {
			for (int v = 0; v < numTerms; v++) {
				if (nkt[k][v] != 0) {
					double varphi = (nkt[k][v] + beta) / (nk[k] + betaSum); 
					pw.println(k + " " + v + " " + nkt[k][v] + " " + nk[k] + " " + varphi); 
				}
			}
		}
	}
	
	public void printMu(File f) {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(f));
			printMu(writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace(); 
		}
	}

	public void printMu(PrintWriter pw) {
		int numTopics = param.getNumTopics();
		
		for (int k = 0; k < numTopics; k++) {
			for (int d = 0; d < topicMeans[k].getNumRows(); d++) {
				pw.print(topicMeans[k].get(d, 0) + " ");
			}
			pw.println(); 
		}
	} 
	
	public void printPsi(File f) {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(f));
			printPsi(writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace(); 
		}
	}

	public void printPsi(PrintWriter pw) {
		int numTopics = param.getNumTopics();
		
		for (int k = 0; k < numTopics; k++) {
			DenseMatrix64F chol = topicCholeskyLTriangularMat[k];
			/*DenseMatrix64F cholT = new DenseMatrix64F(chol.getNumRows(),chol.getNumCols());
			CommonOps.transpose(chol, cholT);
			DenseMatrix64F covar = new DenseMatrix64F(chol.getNumRows(),chol.getNumCols());
			CommonOps.mult(chol,cholT,covar);*/

			// write the covariance now
			for (int r = 0; r < chol.getNumRows(); r++) {
				for (int c = 0; c < chol.getNumCols(); c++) {
					pw.print(chol.get(r, c) + " ");
				}
				pw.println(); 
			}
		}
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

	@Override
	public void printHyperparameters(File f) {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(f));
			printHyperparameters(writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace(); 
		}
	}

	@Override
	public void printHyperparameters(PrintWriter pw) {
		pw.println("alpha: " + param.getAlpha()); 
		pw.println("beta: " + param.getBeta()); 
		
		pw.println("psi:"); 
		DenseMatrix64F psi = param.getPsi(); 
		for (int r = 0; r < psi.getNumRows(); r++) {
			for (int c = 0; c < psi.getNumCols(); c++) {
				pw.print(psi.get(r, c) + " "); 
			}
			pw.println(); 
		}
		
		pw.println("nu: " + param.getNu());
		
		pw.println("mu:");
		DenseMatrix64F mu = param.getMu(); 
		for (int d = 0; d < mu.getNumRows(); d++) {
			pw.print(mu.get(d, 0) + " ");
		}
		pw.println(); 
		
		pw.println("kappa: " + param.getKappa()); 
		pw.println("ntopics: " + param.getNumTopics());
		pw.println("scheme: " + creditParam.getScheme());
	}
}
