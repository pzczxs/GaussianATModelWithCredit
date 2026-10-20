package cn.edu.bjut.ui;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import cn.edu.bjut.corpus.Dictionary;
import cn.edu.bjut.corpus.WordEmbeddings;
import cn.edu.bjut.credit.CreditParameter;
import cn.edu.bjut.credit.CreditScheme;
import cn.edu.bjut.model.ATCreditConstants;
import cn.edu.bjut.model.Constants;
import cn.edu.bjut.model.GaussianATCreditConstants;
import cn.edu.bjut.model.GaussianATCreditEstimator;
import cn.edu.bjut.model.GaussianATCreditParameter;
import cn.edu.bjut.utils.MathUtils;

public class Runner {
	private Options options; 
	private String[] args; 
	
	public Runner(String[] args) {
		this.args = args; 
		
		this.options = new Options(); 
		Option help = new Option("h", "help", false, "Print this usage information");
		options.addOption(help); 
		
		Option scheme = new Option("sc", "scheme", true, "0: arithmetic counting scheme\n1: axiomatic counting scheme\n"
				+ "2: geometric counting scheme\n3: golden number counting scheme\n4: harmonic counting scheme\n"
				+ "5: network-based counting scheme\n6: single author scheme\n7: indiscriminate counting scheme (default)\n");
		options.addOption(scheme); 
		
		Option lambda = new Option("l", "lambda", true, "lambda (default: 0)"); 
		options.addOption(lambda);
		
		Option adjustFlag = new Option("af", "adjustFlag", false, "to adjust the credits according to hyper-authorship strategy"); 
		options.addOption(adjustFlag);
		
		Option stopEarlyFlag = new Option("sf", "stopEarlyFlag", false, "to stop early"); 
		options.addOption(stopEarlyFlag);
		
		Option alpha = new Option("a", "alpha", true, "alpha (default: 0.5)");
		options.addOption(alpha); 
		
		Option beta = new Option("b", "beta", true, "beta (default: 0.1)");
		options.addOption(beta); 
		
		Option kappa = new Option("k", "kappa", true, "kappa (default: 0.1)");
		options.addOption(kappa); 
		
		Option numDimensions = new Option("D", "numDimensions", true, "#of dimensions for word embeddings");
		options.addOption(numDimensions); 
		
		Option embeddingFileName = new Option("e", "embeddingFileName", true, "embedding file name"); 
		options.addOption(embeddingFileName); 
		
		Option numTopics = new Option("K", "numTopics", true, "#of topics (default: 100)"); 
		options.addOption(numTopics); 
		
		Option numTopWords = new Option("tw", "numTopWords", true, "#of top words (default: 20)"); 
		options.addOption(numTopWords); 
		
		Option numTopWords4PMI = new Option("tw4", "numTopWords4PMI", true, "#of top words for PMI (default: 15)"); 
		options.addOption(numTopWords4PMI); 
		
		Option numTopAuthors = new Option("ta", "numTopAuthors", true, "#of top authors (default: 20)");
		options.addOption(numTopAuthors);
		
		Option numInitIterations = new Option("i", "numInitIterations", true, "#of initialization iterations by AT<sup>credit</sup> model (default: 0)");
		options.addOption(numInitIterations); 
		
		Option numIterations = new Option("n", "numIterations", true, "#of estimation iterations (default: 50)");
		options.addOption(numIterations); 
		
		Option seed = new Option("se", "seed", true, "seed for rand (default: 56567651)");
		options.addOption(seed); 
		
		Option filebase = new Option("fb", "filebase", true, "file base"); 
		options.addOption(filebase); 
	}
	
	public CommandLine getCommandLine() {
		if (args == null || args.length == 0) {
			printHelpMessage(); 
		}
		
		CommandLineParser parser = new DefaultParser(); 
		CommandLine cmd = null; 
		try {
			cmd = parser.parse(options, args); 
			
			if (cmd.hasOption('h')) {
				printHelpMessage(); 
			}
		} catch (ParseException e) {
			e.printStackTrace();
			
			printHelpMessage(); 
		}
		
		return cmd; 
	}
	
	public void printHelpMessage() {
		HelpFormatter formatter = new HelpFormatter(); 
		formatter.printHelp("GaussianATModelWithCredit v1.0: to run Gaussian AT<sup>credit</sup> Model.\n"
				+ "Author: XU, Shuo (pzczxs@gmail.com)", options);
		
		System.exit(0);
	}
	
	public static void main(String args[]) {
		Runner runner = new Runner(args); 
		CommandLine cmd = runner.getCommandLine(); 
		
		int scheme = Integer.parseInt(cmd.getOptionValue("scheme", "7")); 
		
		if (scheme < 0 || scheme > 7) {
			runner.printHelpMessage(); 
			return; 
		}
		
		boolean adjustFlag = cmd.hasOption("adjustFlag"); 
		double lambda = Double.parseDouble(cmd.getOptionValue("lambda", "0.0")); 
		
		CreditParameter creditParam = new CreditParameter(CreditScheme.values()[scheme], lambda, adjustFlag); 
		
		double alpha = Double.parseDouble(cmd.getOptionValue("alpha", String.valueOf(GaussianATCreditParameter.ALPHA_DEFAULT)));
		double beta = Double.parseDouble(cmd.getOptionValue("beta", String.valueOf(GaussianATCreditParameter.BETA_DEFAULT)));
		double kappa = Double.parseDouble(cmd.getOptionValue("kappa", String.valueOf(GaussianATCreditParameter.KAPPA_DEFAULT)));
		int numTopics = Integer.parseInt(cmd.getOptionValue("numTopics", String.valueOf(GaussianATCreditParameter.NUM_TOPICS_DEFAULT))); 
		int numDimensions = Integer.parseInt(cmd.getOptionValue("numDimensions", "50"));
		int numTopWords = Integer.parseInt(cmd.getOptionValue("numTopWords", String.valueOf(GaussianATCreditParameter.NUM_TOP_WORDS_DEFAULT)));
		int numTopWords4PMI = Integer.parseInt(cmd.getOptionValue("numTopWords4PMI", String.valueOf(GaussianATCreditParameter.NUM_TOP_WORDS_FOR_PMI_DEFAULT)));
		int numTopAuthors = Integer.parseInt(cmd.getOptionValue("numTopAuthors", String.valueOf(GaussianATCreditParameter.NUM_TOP_AUTHORS_DEFAULT))); 
		long seed = Long.parseLong(cmd.getOptionValue("seed", String.valueOf(Constants.SEED)));
		int numInitIterations = Integer.parseInt(cmd.getOptionValue("numInitIterations", String.valueOf(Constants.TRAIN_INIT_ITERATION))); 
		int numIterations = Integer.parseInt(cmd.getOptionValue("numIterations", String.valueOf(Constants.TRAIN_ITERATION))); 
		
		String filebase = cmd.getOptionValue("filebase"); 
		
		String embeddingFileName = cmd.getOptionValue("embeddingFileName"); 
		WordEmbeddings wordEmbeddings = new WordEmbeddings(numDimensions); 
		wordEmbeddings.read(embeddingFileName);
		
		Dictionary dict = new Dictionary(); 
		dict.readWordMap(filebase + Constants.WORD_DICTIONARY_EXTENTION); 
		DenseMatrix64F matrix = wordEmbeddings.getEmbeddings(dict); 
		
		DenseMatrix64F[] wordVectors = new DenseMatrix64F[matrix.getNumRows()]; // splitting into vectors
		CommonOps.rowsToVector(matrix, wordVectors);
		
		DenseMatrix64F psi = CommonOps.identity(numDimensions); // setting as the identity matrix
		CommonOps.scale(3 * numDimensions, psi);
		
		double nu = numDimensions; 
		
		DenseMatrix64F mu = MathUtils.getSampleMean(wordVectors); // mean of all embeddings
		
		GaussianATCreditParameter param = new GaussianATCreditParameter(numTopics, alpha, beta, psi, nu, mu, kappa, numTopWords, numTopWords4PMI, numTopAuthors); 
		
		boolean stopEarlyFlag = cmd.hasOption("stopEarlyFlag"); 
		GaussianATCreditEstimator estimator = new GaussianATCreditEstimator(filebase, seed, stopEarlyFlag, wordVectors, param, creditParam); 
		estimator.init(); 
		estimator.init(numInitIterations); 
		
		if (numInitIterations > 0) {
			estimator.printAssign(new File(filebase + ATCreditConstants.ASSIGN_EXTENSION));
			estimator.printVartheta(new File(filebase + ATCreditConstants.VARTHETA_EXTENSION));
			estimator.printVarphi(new File(filebase + ATCreditConstants.VARPHI_EXTENSION));
			estimator.printTopWords(new File(filebase + ATCreditConstants.TWORDS_EXTENSION));
			estimator.printTopAuthors(new File(filebase + ATCreditConstants.TAUTHORS_EXTENSION));
		}
		
		estimator.estimate(numIterations); 
		
		estimator.printAssign(new File(filebase + GaussianATCreditConstants.ASSIGN_EXTENSION));
		estimator.printVartheta(new File(filebase + GaussianATCreditConstants.VARTHETA_EXTENSION));
		estimator.printMu(new File(filebase + GaussianATCreditConstants.MU_EXTENSION));
		estimator.printPsi(new File(filebase + GaussianATCreditConstants.PSI_EXTENSION));
		estimator.printTopWords(new File(filebase + GaussianATCreditConstants.TWORDS_EXTENSION));
		estimator.printTopAuthors(new File(filebase + GaussianATCreditConstants.TAUTHORS_EXTENSION));
		estimator.printHyperparameters(new File(filebase + GaussianATCreditConstants.HYPERPARAMETER_EXTENSION));
	}
}
