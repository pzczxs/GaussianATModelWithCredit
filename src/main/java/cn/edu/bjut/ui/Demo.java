package cn.edu.bjut.ui;

import java.io.File;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import cn.edu.bjut.corpus.Dictionary;
import cn.edu.bjut.corpus.WordEmbeddings;
import cn.edu.bjut.credit.CreditParameter;
import cn.edu.bjut.model.ATCreditConstants;
import cn.edu.bjut.model.Constants;
import cn.edu.bjut.model.GaussianATCreditConstants;
import cn.edu.bjut.model.GaussianATCreditEstimator;
import cn.edu.bjut.model.GaussianATCreditParameter;
import cn.edu.bjut.utils.MathUtils;

public class Demo {
	public final static int TRAIN_INIT_ITERATION = 0; 
	public final static int TEST_INIT_ITERATION = 0; 
	public final static int TRAIN_ITERATION = 50; 
	public final static int TEST_ITERATION = 50; 
	public final static int CHAINS = 5; 
	public final static long[] CHAINS_SEED = new long[] {20091201L, 20291201L, 20491201L, 20691201L, 20891201L}; 
	public final static String TRAIN_FILE_BASE = "data/math_software/math_software"; 
	public final static String TEST_FILE_BASE = "data/math_software/math_software"; 
	
	private DenseMatrix64F[] wordVectors; 
	private GaussianATCreditParameter param; 
	private CreditParameter creditParam; 
	
	public Demo(DenseMatrix64F[] wordVectors, GaussianATCreditParameter param, CreditParameter creditParam) {
		this.wordVectors = wordVectors; 
		this.param = param; 
		this.creditParam = creditParam; 
	}
	
	public void run() {
		GaussianATCreditEstimator estimator = new GaussianATCreditEstimator(TRAIN_FILE_BASE, Constants.SEED, wordVectors, param, creditParam); 
		estimator.init(); 
		estimator.init(TRAIN_INIT_ITERATION); 
		
		if (TRAIN_INIT_ITERATION > 0) {
			estimator.printAssign(new File(TRAIN_FILE_BASE + ATCreditConstants.ASSIGN_EXTENSION));
			estimator.printVartheta(new File(TRAIN_FILE_BASE + ATCreditConstants.VARTHETA_EXTENSION));
			estimator.printVarphi(new File(TRAIN_FILE_BASE + ATCreditConstants.VARPHI_EXTENSION));
			estimator.printTopWords(new File(TRAIN_FILE_BASE + ATCreditConstants.TWORDS_EXTENSION));
			estimator.printTopAuthors(new File(TRAIN_FILE_BASE + ATCreditConstants.TAUTHORS_EXTENSION));
		}
		
		estimator.estimate(TRAIN_ITERATION); 
		
		/*GaussianATCreditInferencer inferencer = new GaussianATCreditInferencer(TEST_FILE_BASE, Constants.SEED, estimator); 
		inferencer.init();
		inferencer.init(TEST_INIT_ITERATION);
		inferencer.inference(TEST_ITERATION);
		System.out.println(inferencer.ppx()); */
		
		estimator.printAssign(new File(TRAIN_FILE_BASE + GaussianATCreditConstants.ASSIGN_EXTENSION));
		estimator.printVartheta(new File(TRAIN_FILE_BASE + GaussianATCreditConstants.VARTHETA_EXTENSION));
		estimator.printMu(new File(TRAIN_FILE_BASE + GaussianATCreditConstants.MU_EXTENSION));
		estimator.printPsi(new File(TRAIN_FILE_BASE + GaussianATCreditConstants.PSI_EXTENSION));
		estimator.printTopWords(new File(TRAIN_FILE_BASE + GaussianATCreditConstants.TWORDS_EXTENSION));
		estimator.printTopAuthors(new File(TRAIN_FILE_BASE + GaussianATCreditConstants.TAUTHORS_EXTENSION));
		estimator.printHyperparameters(new File(TRAIN_FILE_BASE + GaussianATCreditConstants.HYPERPARAMETER_EXTENSION));
	}
	
	public static void main(String[] args) {
		int numTopics = 10; 
		double alpha = 0.5; 
		double beta = 0.1; 
		int numDimensions = 50; 
		int topWords = 20; 
		int topWordsForPMI = 15; 
		int topAuthors = 20; 
		
		String fname = "resources/glove.2024.wikigiga.50d.zip"; 
		WordEmbeddings wordEmbeddings = new WordEmbeddings(numDimensions); 
		wordEmbeddings.read(fname);
		
		Dictionary dict = new Dictionary(); 
		dict.readWordMap(TRAIN_FILE_BASE + ".word.vocab"); 
		DenseMatrix64F matrix = wordEmbeddings.getEmbeddings(dict); 
		
		DenseMatrix64F[] wordVectors = new DenseMatrix64F[matrix.getNumRows()]; // splitting into vectors
		CommonOps.rowsToVector(matrix, wordVectors);
		
		DenseMatrix64F psi = CommonOps.identity(numDimensions); // setting as the identity matrix
		CommonOps.scale(3 * numDimensions, psi);
		
		double nu = numDimensions; 
		
		DenseMatrix64F mu = MathUtils.getSampleMean(wordVectors); // mean of all embeddings
		
		double kappa = 0.1; 
		
		GaussianATCreditParameter param = new GaussianATCreditParameter(numTopics, alpha, beta, psi, nu, mu, kappa, topWords, topWordsForPMI, topAuthors); 
		
		CreditParameter creditParam = new CreditParameter();
		
		Demo demo = new Demo(wordVectors, param, creditParam); 
		demo.run();
		
		System.out.println("done."); 
	}
}
