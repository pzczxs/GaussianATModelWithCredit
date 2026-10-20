package cn.edu.bjut.model;

import java.io.File;
import java.io.PrintWriter;

/**
 * Interface for simple Gibbs query sampling implementations
 * 
 * @author XU, Shuo (pzczxs@gmail.com)
 */
public interface IInferencer {

    /**
     * initialize Markov chain for querying
     */
    public void init();
    
    /**
     * initialize Markov chain for querying
     */
    public void init(int niter);

    /**
     * run Gibbs sampler for querying
     * 
     * @param niter number of Gibbs iterations
     */
    public void inference(int niter);
    
    public void printAssign(File f); 
    
    public void printAssign(PrintWriter pw);
}
