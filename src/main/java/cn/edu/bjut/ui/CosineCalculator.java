package cn.edu.bjut.ui;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import cn.edu.bjut.corpus.Dictionary;
import cn.edu.bjut.corpus.WordEmbeddings;
import cn.edu.bjut.model.Constants;
import cn.edu.bjut.utils.MathUtils;

public class CosineCalculator {
	private int numTopics; 
	private int numDimensions; 
	private DenseMatrix64F[] wordVectors; 
	
	public CosineCalculator(int numTopics, int numDimensions, DenseMatrix64F[] wordVectors) {
		this.numTopics = numTopics; 
		this.numDimensions = numDimensions; 
		this.wordVectors = wordVectors; 
	}
	
	public double avgCosine(Map<Integer, ? extends Set<Integer>> topicIdToTermIdSet) {
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
	
	public void read(String fname) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fname)))) {
			for (String line; (line = reader.readLine()) != null;) {
				if (!line.trim().startsWith("estimation iter")) {
					continue; 
				}
				System.out.print(line.trim());
				
				Map<Integer, LinkedHashSet<Integer>> topicIdToTermIdSet = new TreeMap<Integer, LinkedHashSet<Integer>>(); 
				for (int k = 0; k < numTopics; k++) {
					String[] parts = reader.readLine().trim().split("\\s*:\\s*");
					int topicId = Integer.parseInt(parts[0]); 
					
					String[] termIds = parts[1].substring(1, parts[1].length() - 1).split("\\s*,\\s*"); 
					LinkedHashSet<Integer> termIdSet = new LinkedHashSet<Integer>(); 
					for (String termId: termIds) {
						termIdSet.add(Integer.parseInt(termId)); 
					}
				
					topicIdToTermIdSet.put(topicId, termIdSet); 
				}
				
				System.out.println("\t" + avgCosine(topicIdToTermIdSet)); 
				
				for (Map.Entry<Integer, LinkedHashSet<Integer>> entry: topicIdToTermIdSet.entrySet()) {
					System.out.println("\t" + entry.getKey() + ": " + entry.getValue()); 
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		int numDimensions = 50; 
		String embeddingFileName = "resources/glove.2024.wikigiga.50d.zip"; 
		String filebase = "data/math_software/math_software"; 
		
		WordEmbeddings wordEmbeddings = new WordEmbeddings(numDimensions); 
		wordEmbeddings.read(embeddingFileName);
		Dictionary dict = new Dictionary(); 
		dict.readWordMap(filebase + Constants.WORD_DICTIONARY_EXTENTION); 
		DenseMatrix64F matrix = wordEmbeddings.getEmbeddings(dict); 
		
		DenseMatrix64F[] wordVectors = new DenseMatrix64F[matrix.getNumRows()]; // splitting into vectors
		CommonOps.rowsToVector(matrix, wordVectors);
		
		//int[] numTopicCandidates = {10, 20, 30, 40, 50, 60, 80, 90, 100}; 
		int[] numTopicCandidates = {70};
		for (int numTopics: numTopicCandidates) {
			System.out.println("=====" + numTopics + "====="); 
			
			CosineCalculator calculator = new CosineCalculator(numTopics, numDimensions, wordVectors); 
			calculator.read("F:\\dataset\\math_softwares\\topics" + numTopics + ".log");
		}
		
	}
	
}
