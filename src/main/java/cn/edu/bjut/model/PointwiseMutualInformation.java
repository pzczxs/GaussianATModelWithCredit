package cn.edu.bjut.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.bjut.tools.IntegerUnorderedPair;

/**
 * IPointwiseMutualInformation interface for pointwise mutual information calculations
 * 
 * References: 
 * [1] Jey Han Lau, David Newman, and Timothy Baldwin, 2014. Machine Reading Tea Leaves: 
 * Automatically Evaluating Topic Coherence and Topic Model Quality. Proceedings of 
 * the 14th Conference of the European Chapter of the Association for Computational 
 * Linguistics, pp. 530-539. 
 * [2] David Newman, Sarvnaz Karimi, and Lawrence Cavedon, 2009. External Evaluation of 
 * Topic Models. Proceedings of the 14th Australasian Document Computing Symposium, pp. 
 * 11--18. 
 * [3] David Newman, Jey Han Lau, Karl Grieser, and Timothy Baldwin, 2010. Automatic 
 * Evaluation of Topic Coherence. Human Lanugage Technologies: The 2010 Annual Conference 
 * of the North American Chapter of the ACL, pp. 100-108. 
 * 
 * @author XU, Shuo (pzczxs@gmail.com)
 */
public class PointwiseMutualInformation {
	private long totalWindows; 
	private Map<Integer, Integer> termIdToCount; 
	private Map<IntegerUnorderedPair, Integer> termIdPairToCount; 
	
	public PointwiseMutualInformation(long totalWindows, Map<Integer, Integer> termIdToCount, 
			Map<IntegerUnorderedPair, Integer> termIdPairToCount) {
		this.totalWindows = totalWindows; 
		this.termIdToCount = termIdToCount; 
		this.termIdPairToCount = termIdPairToCount; 
	}
	
	/**
	 * @param termIdSet
	 * 		a list of top topic word ids
	 * @param flag
	 * 		flag = true: NPMI; flag = false: PMI
	 * 
	 * Pointwise Mutual Information: 
     * PMI = \sum_{1 \leq i \leq j \leq N} {\log {\frac{\mathrm{Pr}(w_i, w_j)}{\mathrm{Pr}(w_i) \mathrm{Pr}(w_j)}}}
     * 
     * Normalized Pointwise Mutual Information: 
     * NPMI = \sum_{1 \leq i \leq j \leq N} {\frac{\log {\frac{\mathrm{Pr}(w_i, w_j)}{\mathrm{Pr}(w_i) \mathrm{Pr}(w_j)}}}{- \log {\mathrm{Pr}(w_i, w_j)}}}
	 * 
	 * */
	public double score(Set<Integer> termIdSet, boolean flag) {
		if (termIdSet == null || termIdSet.size() < 2) {
			return 0.0; 
		}
		
		double sum = 0.0; 
		List<Integer> termIdList = new ArrayList<Integer>(termIdSet); 
		for (int i = 0; i < termIdList.size() - 1; i++) {
			int ci = (termIdToCount == null)? 0: termIdToCount.getOrDefault(termIdList.get(i), 0); 
			if (ci == 0) {
				continue; 
			}
			
			for (int j = i + 1; j < termIdList.size(); j++) {
				int cj = (termIdToCount == null)? 0: termIdToCount.getOrDefault(termIdList.get(j), 0); 
				if (cj == 0) {
					continue; 
				}
				
				IntegerUnorderedPair pair = new IntegerUnorderedPair(termIdList.get(i), termIdList.get(j)); 
				int cij = (termIdPairToCount == null)? 0: termIdPairToCount.getOrDefault(pair, 0); 
				if (cij == 0) {
					continue; 
				}
				
				double numerator = Math.log(cij) + Math.log(totalWindows) - Math.log(ci) - Math.log(cj); 
				
				if (flag) {
					double denominator = -Math.log(cij) + Math.log(totalWindows); 
					
					sum += numerator / denominator; 
				} else {
					sum += numerator; 
				}
			}
		}
		
		return sum;
	}
}