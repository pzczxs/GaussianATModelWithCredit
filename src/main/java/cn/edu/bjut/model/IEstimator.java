package cn.edu.bjut.model;

import java.io.File;
import java.io.PrintWriter;

/**
 * Interface for simple Gibbs sampling implementations
 * 
 * @author XU, Shuo (pzczxs@gmail.com)
 */
public interface IEstimator {
    /**
     * initialize Markov chain
     */
    public void init();
    
    /**
     * initialize Markov chain by Author-Topic (AT) <sup>credit</sup> model
     */
    public void init(int niter);

    /**
     * run Gibbs sampler
     * 
     * @param niter 
     * 		number of Gibbs iterations
     */
    public void estimate(int niter);
    
    public void printAssign(File f); 
    
    public void printHyperparameters(File f); 
    
    public void printHyperparameters(PrintWriter pw); 
    
    public void printAssign(PrintWriter pw); 
}
