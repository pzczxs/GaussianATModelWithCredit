package cn.edu.bjut.corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.ejml.data.DenseMatrix64F;

public class WordEmbeddings {
	private Map<String, double[]> embeddings; 
	private int numDimensions; 
	private boolean caseInsensitive; 
	
	public WordEmbeddings(int numDimensions, boolean caseInsensitive) {
		this.numDimensions = numDimensions; 
		this.caseInsensitive = caseInsensitive; 
		
		this.embeddings = new HashMap<String, double[]>(); 
	}
	
	public WordEmbeddings(int numDimensions) {
		this(numDimensions, true); 
	}

	public int getNumDimensions() {
		return numDimensions;
	}
	
	public int getNumEmbeddings() {
		return embeddings.size(); 
	}
	
	public boolean isCaseInsensitive() {
		return caseInsensitive;
	}

	public void setCaseInsensitive(boolean caseInsensitive) {
		this.caseInsensitive = caseInsensitive;
	}
	
	public Set<String> getWordSet() {
		return embeddings.keySet(); 
	}

	public double[] getEmbedding(String word) {
		if (word == null || word.trim().equals("")) {
			return null; 
		}
		
		String wordAfter = caseInsensitive? word.trim().toLowerCase(): word.trim(); 
		
		if (embeddings.containsKey(wordAfter)) {
			return embeddings.get(wordAfter); 
		} 
		
		return null; 
	}
	
	public double[] averagePool(Set<String> wordSet) {
		if (wordSet == null || wordSet.isEmpty()) {
			return null; 
		}
		
		double[] avgEmbedding = new double[numDimensions]; 
		for (String word: wordSet) {
			String wordAfter = caseInsensitive? word.trim().toLowerCase(): word.trim(); 
			if (!embeddings.containsKey(wordAfter)) {
				continue; 
			}
			
			double[] embedding = embeddings.get(wordAfter); 
			for (int d = 0; d < numDimensions; d++) {
				avgEmbedding[d] += embedding[d]; 
			}
		}
		
		for (int d = 0; d < numDimensions; d++) {
			avgEmbedding[d] /= wordSet.size(); 
		}
		
		return avgEmbedding; 
	}
	
	public DenseMatrix64F getEmbeddings(Dictionary dict) {
		// initialize the embedding matrix
		DenseMatrix64F matrix = new DenseMatrix64F(dict.size(), numDimensions); 
		
		// now populate the matrix
		for (int i = 0; i < dict.size(); i++) {
			String word = dict.get(i); 
			double[] embedding = getEmbedding(word);
			if (embedding == null) {
				throw new IllegalArgumentException("The unlisted word \"" + word + "\" appears in the given dictionary."); 
			}
			
			for (int j = 0; j < numDimensions; j++) {
				matrix.set(i, j, embedding[j]);
			}
		}
		
		return matrix; 
	}
	
	public void read(String fname) {
		try (ZipFile zf = new ZipFile(new File(fname))) {
			if (zf.size() > 1) {
				throw new IllegalArgumentException("The file \"" + fname + "\" should include only one entry"); 
			}
			
			for (Enumeration<? extends ZipEntry> entries = zf.entries(); entries.hasMoreElements(); ) {
				ZipEntry entry = entries.nextElement(); 
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(zf.getInputStream(entry)));
				
				// read word embeddings
				int nLine = 0; 
				for (String line; (line = reader.readLine()) != null; nLine++) {
					String[] parts = line.trim().split("\\s+"); 
					
					if (parts.length != numDimensions + 1) {
						throw new IllegalArgumentException("The format of file \"" + fname + "\" is wrong at #line: "+ nLine); 
					}

					String word = caseInsensitive? parts[0].toLowerCase(): parts[0]; 
					double[] embedding = new double[parts.length - 1];
					
					for (int i = 1; i < parts.length; i++) {
						double val = Double.parseDouble(parts[i]); 
						embedding[i - 1] = val; 
					}

					embeddings.put(word, embedding); 
				}

				reader.close();
			}
		} catch (IOException e) {
			e.printStackTrace(); 
		} 
	}
	
	public static void main(String[] args) {
		String fname = "resources/glove.2024.wikigiga.50d.zip"; 
		WordEmbeddings wordEmbeddings = new WordEmbeddings(50); 
		wordEmbeddings.read(fname);
		
		Dictionary dict = new Dictionary(); 
		dict.readWordMap("data/syn_bio.word.vocab"); 
		DenseMatrix64F matrix = wordEmbeddings.getEmbeddings(dict); 
		System.out.println(matrix); 
		
		System.out.println(wordEmbeddings.embeddings.keySet()); 
	}
}
