package cn.edu.bjut.utils;

import java.util.Arrays;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import cn.edu.bjut.tools.IntegerDoublePair;

public class MathUtils {
	public static double sum(double[] u) {
		double ret = 0.0; 
		for (int i = 0; i < u.length; i++) {
			ret += u[i]; 
		}
		
		return ret; 
	}
	
	/**
     * average the elements of vec
     * 
     * @param vec
     * @return
     */
    public static double average(double[] vec) {
        double sum = 0.0;
        for (int i = 0; i < vec.length; i++) {
            sum += vec[i];
        }
        return sum / vec.length;

    }
    
    /**
     * average the elements of m
     * 
     * @param m
     * @return
     */
    public static double average(double[][] m) {
        double sum = 0.0;
        int count = 0; 
        for (int i = 0; i < m.length; i++) {
        	for (int j = 0; j < m[i].length; j++) {
        		sum += m[i][j];
        	}
        	count += m[i].length; 
        }
        
        return sum / count;

    }
	
	public static double divergenceKullbackLeibler(double[] u, double[] v) {
		if (sum(u) - 1 > 1E-8 || sum(v) - 1 > 1E-8) {
			throw new IllegalArgumentException("The parameter u or v does not follow multinomial distribution."); 
		}
		
		if (u.length != v.length) {
			throw new IllegalArgumentException("The length of u mus be same as that of v."); 
		}
		
		double divergence = 0; 
		for (int i = 0; i < u.length; i++) {
			divergence += u[i] * (Math.log(u[i]) - Math.log(v[i])); 
		}
		
		return divergence; 
	}
	
	public static double symDivergenceKullbackLeibler(double[] u, double[] v) {
		return 0.5 * (divergenceKullbackLeibler(u, v) + divergenceKullbackLeibler(v, u)); 
	}
	
	public static double divergenceJensenShannon(double[] u, double[] v) {
		if (sum(u) - 1 > 1E-8 || sum(v) - 1 > 1E-8) {
			throw new IllegalArgumentException("The parameter u or v does not follow multinomial distribution."); 
		}
		
		if (u.length != v.length) {
			throw new IllegalArgumentException("The length of u mus be same as that of v."); 
		}
		
		double[] avg = new double[u.length];
		for (int i = 0; i < u.length; i++) {
			avg[i] = 0.5 * (u[i] + v[i]);
		}
		
		return 0.5 * (divergenceKullbackLeibler(u, avg) + divergenceKullbackLeibler(v, avg)); 
	}
	
	public static double norm2(double[] u) {
		double norm = 0.0; 
		for (int i = 0; i < u.length; i++) {
			norm += u[i] * u[i]; 
		}
		
		return Math.sqrt(norm); 
	}
	
	/**
	 * @param u: column/row vector
	 * */
	public static double norm2(DenseMatrix64F u) {
		if (u == null) {
			return 0.0; 
		}
		
		if (u.getNumRows() != 1 && u.getNumCols() != 1) {
			throw new IllegalArgumentException("The vector u must be a column/row vector."); 
		}
		
		DenseMatrix64F mul = new DenseMatrix64F(1, 1);
		if (u.getNumRows() == 1) { // row vector
			DenseMatrix64F uTranspose = new DenseMatrix64F(u.getNumCols(), 1);
			CommonOps.transpose(u, uTranspose);
			CommonOps.mult(u, uTranspose, mul);
		} else { // column vector
			DenseMatrix64F uTranspose = new DenseMatrix64F(1, u.getNumRows());
			CommonOps.transpose(u, uTranspose);
			CommonOps.mult(uTranspose, u, mul);
		}
		
		return Math.sqrt(mul.get(0, 0));
	}
	
	public static double cosine(double[] u, double[] v) {
		if (u.length != v.length) {
			throw new IllegalArgumentException("The length of u mus be same as that of v."); 
		}
		
		double sim = 0.0; 
		for (int i = 0; i < u.length; i++) {
			sim += u[i] * v[i]; 
		}
		
		return sim / (norm2(u) * norm2(v)); 
	}
	
	/**
	 *  @param u: column/row vector
	 *  @param v: column/row vector
	 * */
	public static double cosine(DenseMatrix64F u, DenseMatrix64F v) {
		if (u == null || v == null) {
			throw new IllegalArgumentException("The vectors u and v must not be null."); 
		}
		
		if (u.getNumRows() != 1 && u.getNumCols() != 1 || u.getNumRows() != 1 && u.getNumCols() != 1) {
			throw new IllegalArgumentException("The vectors u and v must be column/row vectors."); 
		}
		
		DenseMatrix64F mul = new DenseMatrix64F(1, 1);
		if (u.getNumRows() == 1 && v.getNumRows() == 1) { // two row vectors
			DenseMatrix64F vTranspose = new DenseMatrix64F(1, v.getNumRows());
			CommonOps.transpose(v, vTranspose);
			CommonOps.mult(u, vTranspose, mul);
		} else if (u.getNumCols() == 1 && v.getNumCols() == 1) { // two column vectors
			DenseMatrix64F uTranspose = new DenseMatrix64F(1, u.getNumRows());
			CommonOps.transpose(u, uTranspose);
			CommonOps.mult(uTranspose, v, mul);
		} else { // one row vector and one column vector
			throw new IllegalArgumentException("The vectors u and v must be column/row vectors."); 
		}
		
		return mul.get(0, 0) / (norm2(u) * norm2(v)); 
	}
	
	//public static DenseMatrix64F 
	
	public static int[] rankOf(double[] u) {
		IntegerDoublePair[] values = new IntegerDoublePair[u.length];
		for (int n = 0; n < u.length; n++) {
			values[n] = new IntegerDoublePair(n, u[n]); 
		}
		Arrays.sort(values); 
		
		int[] ranks = new int[u.length];
		for (int n = 0; n < u.length; n++) {
			ranks[values[n].getFirst()] = n + 1;
		}
		
		return ranks; 
	}
	
	public static double rhoSpearman(double[] u, double[] v) {
		if (u.length != v.length) {
			throw new IllegalArgumentException("The length of u mus be same as that of v."); 
		}
		
		int[] uRanks = rankOf(u); 
		int[] vRanks = rankOf(v); 
		
		double rho = 0.0; 
		for (int i = 0; i < u.length; i++) {
			rho += (uRanks[i] - vRanks[i]) * (uRanks[i] - vRanks[i]); 
		}
		rho = 1.0 - 6.0 * rho / (u.length * (u.length * u.length - 1)); 
		
		return rho; 
	}
	
	public static double normalizedRhoSpearman(double[] u, double[] v) {
		return (rhoSpearman(u, v) + 1) / 2; 
	}
	
	public static double tauKendall(double[] u, double[] v) {
		if (u.length != v.length) {
			throw new IllegalArgumentException("The length of u mus be same as that of v."); 
		}
		
		int nc = 0; // #of concordant pairs
		int nd = 0; // #of discordant pairs
		int nu = 0; // #of ties in @u
		int nv = 0; // #of ties in @v
		for (int i = 0; i < u.length - 1; i++) {
			for (int j = i + 1; j < u.length; j++) {
				if (((u[i] < u[j]) && (v[i] < v[j])) || ((u[i] > u[j]) && (v[i] > v[j]))) {
					nc++; 
				}
				
				if (((u[i] < u[j]) && (v[i] > v[j])) || ((u[i] > u[j]) && (v[i] < v[j]))) {
					nd++; 
				}
				
				if (Math.abs(u[i] - u[j]) <= Double.MIN_VALUE) {
					nu++; 
				}
				
				if (Math.abs(v[i] - v[j]) <= Double.MIN_VALUE) {
					nv++; 
				}
			}
		}
		
		return (nc - nd) / (Math.sqrt(nc + nd + nu) * Math.sqrt(nc + nd + nv)); 
	}
	
	public static double normalizedTauKendall(double[] u, double[] v) {
		return (tauKendall(u, v) + 1) / 2; 
	}
	
	public static double logsum(final double[] logv) {
		if (logv.length < 2) {
			System.err.println("logsum() -> canot sum less than 2 elements.");
			System.exit(-1);
		}
		
		double sum = (logv[0] > logv[1])? 
				(logv[0] + Math.log(1.0 + Math.exp(logv[1] - logv[0]))): (logv[1] + Math.log(1.0 + Math.exp(logv[0] - logv[1])));
		
		for (int n = 2; n < logv.length; n++) {
			sum = (logv[n] > sum)? 
					(logv[n] + Math.log(1.0 + Math.exp(sum - logv[n]))): (sum + Math.log(1.0 + Math.exp(logv[n] - sum))); 
		}
		
		return sum; 
	}
	
	public static double logsum(final double[][] logm) {
		int n = 0; 
		for (int i = 0; i < logm.length; i++) {
			n += logm[i].length; 
		}
		
		double[] logv = new double[n]; 
		int idx = 0; 
		for (int i = 0; i < logm.length; i++) {
			for (int j = 0; j < logm[i].length; j++) {
				logv[idx++] = logm[i][j]; 
			}
		}
		
		return logsum(logv); 
	}
	
	public static double logsum(final double[][][] logm) {
		int n = 0; 
		for (int i = 0; i < logm.length; i++) {
			for (int j = 0; j < logm[i].length; j++) {
				n += logm[i][j].length; 
			}
		}
		
		double[] logv = new double[n]; 
		int idx = 0; 
		for (int i = 0; i < logm.length; i++) {
			for (int j = 0; j < logm[i].length; j++) {
				for (int k = 0; k < logm[j].length; k++) {
					logv[idx++] = logm[i][j][k]; 
				}
			}
		}
		
		return logsum(logv); 
	}
	
	/**
	 * mean of the vectors
	 * 
	 * @param vectors
	 * @return
	 */
	public static DenseMatrix64F getSampleMean(DenseMatrix64F[] vectors) {
		DenseMatrix64F mean = new DenseMatrix64F(vectors[0].getNumElements(), 1);// initialized to 0
		for (DenseMatrix64F vector : vectors) {
			CommonOps.addEquals(mean, vector);
		}

		CommonOps.divide(vectors.length, mean);

		return mean;
	}
	
	/**
	 * This function computes the lower triangular Cholesky decomposition L' of
	 * matrix A' from L (the cholesky decomp of A) where A' = A + x*x^T. Please 
	 * refer to pseudo-code in the following Wikipedia page
	 * https://en.wikipedia.org/wiki/Cholesky_decomposition#Rank-one_update
	 */
	public static void cholRank1Update(DenseMatrix64F L, DenseMatrix64F x) {
		// L should be a square lower triangular matrix (although not checking for
		// triangularity here explicitly)
		if (L.getNumRows() != L.getNumCols()) {
			throw new IllegalArgumentException("The matrix L should be a square matrix."); 
		}
		
		int numDimensions = L.getNumCols(); 
		
		if (x.getNumCols() != 1 || x.getNumRows() != numDimensions) {
			throw new IllegalArgumentException("The matrix x should be a column vector."); 
		}

		for (int k = 0; k < numDimensions; k++) {
			double r = Math.sqrt(Math.pow(L.get(k, k), 2) + Math.pow(x.get(k, 0), 2));
			double c = r / L.get(k, k);
			double s = x.get(k, 0) / L.get(k, k);
			L.set(k, k, r);
			for (int l = k + 1; l < numDimensions; l++) {
				double val = (L.get(l, k) + s * x.get(l, 0)) / c;
				L.set(l, k, val);
				val = c * x.get(l, 0) - s * val;
				x.set(l, 0, val);
			}
		}
	}

	/**
	 * This function computes the lower triangular cholesky decomposition L' of
	 * matrix A' from L (the cholesky decomp of A) where A' = A - x*x^T. Please 
	 * refer to pseudo-code in the following Wikipedia page: 
	 * https://en.wikipedia.org/wiki/Cholesky_decomposition#Rank-one_update
	 */
	public static void cholRank1Downdate(DenseMatrix64F L, DenseMatrix64F x) {
		// L should be a square lower triangular matrix (although not checking for
		// triangularity here explicitly)
		if (L.getNumRows() != L.getNumCols()) {
			throw new IllegalArgumentException("The matrix L should be a square matrix."); 
		}
		
		int numDimensions = L.getNumCols(); 
		
		if (x.getNumCols() != 1 || x.getNumRows() != numDimensions) {
			throw new IllegalArgumentException("The matrix x should be a column vector."); 
		}

		for (int k = 0; k < numDimensions; k++) {
			double r = Math.sqrt(L.get(k, k) * L.get(k, k) - x.get(k, 0) * x.get(k, 0));
			double c = r / L.get(k, k);
			double s = x.get(k, 0) / L.get(k, k);
			L.set(k, k, r);
			for (int l = k + 1; l < numDimensions; l++) {
				double val = (L.get(l, k) - s * x.get(l, 0)) / (double) c;
				L.set(l, k, val);
				val = c * x.get(l, 0) - s * L.get(l, k);
				x.set(l, 0, val);
			}
		}
	}
}
