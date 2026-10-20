package cn.edu.bjut.credit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public abstract class AuthorshipCredit {
	protected final static double CREDIT_RATIO_THRESHOLD = 0.1; 
	protected final static double CREDIT_RATIO = 0.05; 
	
	protected List<Set<Integer>> authorGroups; 
	protected int[] g; 
	
	protected List<BylineInformation> authors; 
	protected Map<Integer, Double> credits; 
	
	protected boolean adjustmentFlag; // flag = true: adjust the credits; flag = false: don't adjust the credits
	
	public AuthorshipCredit(List<BylineInformation> authors, boolean adjustmentFlag) {
		this.authors = authors; 
		this.adjustmentFlag = adjustmentFlag; 
		
		Collections.sort(this.authors);

		fill(); 
	}
	
	public AuthorshipCredit(List<BylineInformation> authors) {
		this(authors, true); 
	}
	
	public AuthorshipCredit(BylineInformation[] authors, boolean adjustmentFlag) {
		this(Arrays.asList(authors), adjustmentFlag); 
	}
	
	public AuthorshipCredit(BylineInformation[] authors) {
		this(authors, true); 
	}
	
	public AuthorshipCredit(int[] authors, boolean adjustmentFlag) {
		this.authors = new ArrayList<BylineInformation>(); 
		for (int i = 0; i < authors.length; i++) {
			BylineInformation byline = new BylineInformation(authors[i], i + 1, false); 
			this.authors.add(byline); 
		}
		
		this.adjustmentFlag = adjustmentFlag; 
		
		fill(); 
	}
	
	public AuthorshipCredit(int[] authors) {
		this(authors, true); 
	}
	
	public AuthorshipCredit(int[] authors, boolean[] isReprints, boolean adjustmentFlag) {
		assert (authors.length == isReprints.length): 
			"The number of elements in \"authors\" and \"isReprints\" must be equal."; 
		
		this.authors = new ArrayList<BylineInformation>(); 
		for (int i = 0; i < authors.length; i++) {
			BylineInformation byline = new BylineInformation(authors[i], i + 1, isReprints[i]); 
			this.authors.add(byline); 
		}

		Collections.sort(this.authors);
		
		this.adjustmentFlag = adjustmentFlag; 
		
		fill(); 
	}
	
	public AuthorshipCredit(int[] authors, boolean[] isReprints) {
		this(authors, isReprints, true); 
	}
	
	public boolean isAdjustmentFlag() {
		return adjustmentFlag;
	}

	public void setAdjustmentFlag(boolean adjustmentFlag) {
		this.adjustmentFlag = adjustmentFlag;
	}

	protected abstract void calculate(); 
	
	public Map<Integer, Double> getCredit() {
		if (credits == null) {
			calculate(); 
		}
		
		return credits; 
	}
	
	protected void fill() {
		Map<Integer, Set<Integer>> rank2AuthorGroup = new HashMap<Integer, Set<Integer>>(); 
		
		for (int i = 0; i < authors.size(); i++) {
			int authorId = authors.get(i).getAuthorId();
			int rank = (authors.get(i).getSeqNo() == 1 || authors.get(i).isReprint())? 1: authors.get(i).getSeqNo(); 
			
			Set<Integer> authorIds = rank2AuthorGroup.containsKey(rank)? rank2AuthorGroup.get(rank): new HashSet<Integer>(); 
			authorIds.add(authorId); 
			rank2AuthorGroup.put(rank, authorIds); 
		}
		
		Map<Integer, Set<Integer>> rank2AuthorGroupSorted = new TreeMap<Integer, Set<Integer>>(rank2AuthorGroup); 
		authorGroups = new ArrayList<Set<Integer>>(rank2AuthorGroupSorted.values()); 
		g = new int[authorGroups.size()];
		for (int i = 0; i < authorGroups.size(); i++) {
			g[i] = authorGroups.get(i).size(); 
		}
	}
	
	// The credits are adjusted are adjusted according to equal contributors strategy. 
	protected void adjustEqualContributors() {
		for (int i = 0; i < authorGroups.size(); i++) {
			double creditSum = 0.0; 
			for (int authorId: authorGroups.get(i)) {
				creditSum += credits.get(authorId); 
			}
			
			for (int authorId: authorGroups.get(i)) {
				credits.put(authorId, creditSum / g[i]); 
			}
		}
	}
	
	// The credits are adjusted according to hyper-authorship strategy. 
	protected void adjustHyperAuthorship() {
		int firstAuthorId = authors.get(0).getAuthorId(); 
		int idx = 0; 
		for (idx = 1; idx < authors.size(); idx++) {
			int authorId = authors.get(idx).getAuthorId(); 
			if (credits.get(authorId) < CREDIT_RATIO_THRESHOLD * credits.get(firstAuthorId)) {
				break; 
			}
		}
		
		// re-assign the credits
		for (int i = idx; i < authors.size(); i++) {
			int authorId = authors.get(i).getAuthorId(); 
			credits.put(authorId, CREDIT_RATIO * credits.get(firstAuthorId)); 
		}
		
		// re-normalize
		if (idx < authors.size()) {
			double creditSum = 0.0; 
			for (int i = 0; i < authors.size(); i++) {
				int authorId = authors.get(i).getAuthorId(); 
				creditSum += credits.get(authorId); 
			}
			
			for (int i = 0; i < authors.size(); i++) {
				int authorId = authors.get(i).getAuthorId(); 
				credits.put(authorId, credits.get(authorId) / creditSum); 
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(); 
		
		for (BylineInformation author: authors) {
			sb.append(author + "\n"); 
		}
		
		return sb.toString(); 
	}
}
