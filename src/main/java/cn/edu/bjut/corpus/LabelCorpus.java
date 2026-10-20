package cn.edu.bjut.corpus;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.edu.bjut.credit.BylineInformation;

public class LabelCorpus extends Corpus implements ILabelCorpus {

	public static final String[] EXTENSIONS = {".labels", ".pubs", ".refs", ".tags", ".years" };
	public static final String AUTHORS_EXTENSION = ".authors";  

	/**
	 * array of labels. Elements are filled as soon as readlabels is called.
	 */
	private int[][][] labels;
	/**
	 * total count of labels
	 */
	private int[] labelsW;
	/**
	 * total range of labels
	 */
	private int[] labelsV;
	
	// for authors
	private BylineInformation[][] authors; 
	private int authorsW; 
	private int authorsV; 

	private String dataFilebase = null;

	/**
	 * 
	 */
	public LabelCorpus() {
		super();
		init();
	}

	/**
	 * @param dataFilebase
	 *            (filename without extension)
	 */
	public LabelCorpus(String dataFilebase) {
		super(dataFilebase + ".corpus");
		this.dataFilebase = dataFilebase;
		init();
	}

	/**
	 * @param dataFilebase
	 *            (filename without extension)
	 * @param readlimit
	 *            number of docs to reduce corpus when reading (-1 = unlimited)
	 */
	public LabelCorpus(String dataFilebase, int readlimit) {
		super(dataFilebase + ".corpus", readlimit);
		this.dataFilebase = dataFilebase;
		init();
	}

	/**
	 * create label corpus from standard one
	 * 
	 * @param corp
	 */
	public LabelCorpus(Corpus corp) {
		this.docs = corp.docs;
		this.numTerms = corp.numTerms;
		init();
	}

	protected void init() {
		this.labels = new int[EXTENSIONS.length][][];
		this.labelsW = new int[EXTENSIONS.length];
		this.labelsV = new int[EXTENSIONS.length];
	}

	/**
	 * loads and returns the document labels of given kind
	 */
	// @Override
	public int[][] getDocLabels(int kind) {
		if (this.labels[kind] == null) {
			readLabels(kind);
		}

		return this.labels[kind];
	}
	
	public BylineInformation[][] getDocAuthors() {
		if (this.authors == null) {
			readAuthors(); 
		}
		
		return this.authors; 
	}

	/**
	 * return the maximum number of labels in any document
	 * 
	 * @param kind
	 * @return
	 */
	public int getLabelsMaxN(int kind) {
		if (this.labels[kind] == null) {
			readLabels(kind);
		}

		int max = 0;
		for (int m = 0; m < this.labels[kind].length; m++) {
			max = Math.max(this.labels[kind][m].length, max);
		}

		return max;
	}

	// @Override
	public int getLabelsW(int kind) {
		if (this.labels[kind] == null) {
			readLabels(kind);
		}

		return this.labelsW[kind];
	}
	
	public int getAuthorsW() {
		if (this.authors == null) {
			readAuthors(); 
		}
		
		return this.authorsW; 
	}

	// @Override
	public int getLabelsV(int kind) {
		if (this.labels[kind] == null) {
			readLabels(kind);
		}

		return this.labelsV[kind];
	}
	
	public int getAuthorsV() {
		if (this.authors == null) {
			readAuthors(); 
		}
		
		return this.authorsV; 
	}

	/**
	 * read a label file with one line per document and associated labels
	 * 
	 * @param kind
	 * @return
	 */
	private void readLabels(int kind) {
		List<int[]> data = new ArrayList<int[]>();
		int W = 0;
		int V = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(this.dataFilebase + EXTENSIONS[kind]));
			for (String line; (line = br.readLine()) != null;) {
				line = line.trim();
				if (line.length() == 0) {
					data.add(new int[0]);
					continue;
				}
				String[] parts = line.split(" ");
				int[] a = new int[parts.length];
				for (int i = 0; i < parts.length; i++) {
					a[i] = Integer.parseInt(parts[i].trim());
					if (a[i] >= V) {
						V = a[i] + 1;
					}
				}
				W += a.length;
				data.add(a);
			}
			br.close();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.labels[kind] = data.toArray(new int[0][0]);
		this.labelsW[kind] = W;
		this.labelsV[kind] = V;
	}
	
	/**
	 * read a author file with one line per document and associated labels
	 * 
	 * @param kind
	 * @return
	 */
	private void readAuthors() {
		List<BylineInformation[]> data = new ArrayList<BylineInformation[]>();
		int W = 0;
		int V = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(this.dataFilebase + AUTHORS_EXTENSION));
			for (String line; (line = br.readLine()) != null;) {
				line = line.trim();
				if (line.length() == 0) {
					data.add(new BylineInformation[0]);
					continue;
				}
				
				String[] tokens = line.split(" ");
				BylineInformation[] a = new BylineInformation[tokens.length];
				for (int i = 0; i < tokens.length; i++) {
					String[] parts = tokens[i].split(":"); 
					if (parts.length != 3) {
						System.err.println("The format for the following line is wrong: \n\t" + line); 
					}
					
					BylineInformation byline = new BylineInformation(
							Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), 
							parts[2].equals("1")? true: false); 
					
					a[i] = byline;
					if (a[i].getAuthorId() >= V) {
						V = a[i].getAuthorId() + 1;
					}
				}
				W += a.length;
				data.add(a);
			}
			br.close();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.authors = data.toArray(new BylineInformation[0][0]); 
		this.authorsW = W; 
		this.authorsV = V; 
	}

	@Override
	public ICorpus getTrainCorpus(int split) {
		int trainM = starts[split + 1] - starts[split];

		int[][][] trainLabels = new int[EXTENSIONS.length][trainM][];
		int[] trainLabelsW = new int[EXTENSIONS.length];
		
		// for each label type
		for (int type = 0; type < EXTENSIONS.length; type++) {
			if (this.labels[type] == null) {
				readLabels(type);
			}
			
			int mtrain = 0;
			// before test split
			for (int m = 0; m < starts[split]; m++) {
				trainLabels[type][mtrain] = labels[type][perm[m]];
				trainLabelsW[type] += trainLabels[type][mtrain].length;
				mtrain++;
			}
			
			// after test split
			for (int m = starts[split + 1]; m < docs.length; m++) {
				trainLabels[type][mtrain] = this.labels[type][perm[m]];
				trainLabelsW[type] += trainLabels[type][mtrain].length;
				mtrain++;
			}
		}
		
		/************Authors********************/
		if (authors == null) {
			readAuthors();
		}
		
		BylineInformation[][] trainAuthors = new BylineInformation[trainM][];
		int trainAuthorsW = 0;
		int mtrain = 0;
		
		// before test split
		for (int m = 0; m < starts[split]; m++) {
			trainAuthors[mtrain] = authors[perm[m]];
			trainAuthorsW += trainAuthors[mtrain].length;
			mtrain++;
		}
		
		// after test split
		for (int m = starts[split + 1]; m < docs.length; m++) {
			trainAuthors[mtrain] = authors[perm[m]];
			trainAuthorsW += trainAuthors[mtrain].length;
			mtrain++;
		}

		LabelCorpus trainCorpus = new LabelCorpus((Corpus) super.getTrainCorpus(split));
		trainCorpus.labels = trainLabels;
		trainCorpus.authors = trainAuthors; 
		trainCorpus.labelsV = labelsV;
		trainCorpus.labelsW = trainLabelsW;
		trainCorpus.authorsV = authorsV; 
		trainCorpus.authorsW = trainAuthorsW; 

		return trainCorpus;
	}

	@Override
	public ICorpus getTestCorpus(int split) {
		int testM = starts[split + 1] - starts[split];

		int[][][] testLabels = new int[EXTENSIONS.length][testM][];
		int[] testLabelsW = new int[EXTENSIONS.length];

		// for each label type
		for (int type = 0; type < EXTENSIONS.length; type++) {
			if (labels[type] == null) {
				readLabels(type);
			}

			for (int m = starts[split]; m < starts[split + 1]; m++) {
				testLabels[type][m - starts[split]] = labels[type][perm[m]];
				testLabelsW[type] += testLabels[type][m - starts[split]].length;
			}
		}
		
		/************Authors********************/
		if (authors == null) {
			readAuthors();
		}
		
		BylineInformation[][] testAuthors = new BylineInformation[testM][];
		int testAuthorsW = 0;
		
		for (int m = starts[split]; m < starts[split + 1]; m++) {
			testAuthors[m - starts[split]] = authors[perm[m]];
			testAuthorsW += testAuthors[m - starts[split]].length;
		}

		LabelCorpus testCorpus = new LabelCorpus((Corpus) super.getTestCorpus(split));
		testCorpus.labels = testLabels;
		testCorpus.authors = testAuthors; 
		testCorpus.labelsV = labelsV;
		testCorpus.labelsW = testLabelsW;
		testCorpus.authorsV = authorsV; 
		testCorpus.authorsW = testAuthorsW; 

		return testCorpus;
	}
}
