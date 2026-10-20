package cn.edu.bjut.credit;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Single-author counting
 * 
 * The first author receives the publication full credit, whereas all other 
 * co-authors receive no credit (Cole & Cole, 1973).
 * 
 *  References: 
 *  [1] Jonathan R. Cole and Stephen Cole, 1973. Social Stratification in 
 *  Science. University of Chicago Press, Chicago, IL, USA. 
 *  
 *  @author XU, Shuo (pzczxs@gmail.com)
 * */
public class SingleAuthorCredit extends AuthorshipCredit {

	public SingleAuthorCredit(List<BylineInformation> authors) {
		super(authors); 
	}
	
	public SingleAuthorCredit(BylineInformation[] authors) {
		super(authors); 
	}
	
	public SingleAuthorCredit(int[] authors) {
		super(authors); 
	}
	
	public SingleAuthorCredit(int[] authors, boolean[] isReprints) {
		super(authors, isReprints); 
	}

	@Override
	protected void calculate() {
		this.credits = new LinkedHashMap<Integer, Double>();
		
		// the authors with the first position
		double credit = 1.0 / g[0]; 
		for (int authorId: authorGroups.get(0)) {
			credits.put(authorId, credit); 
		}
		
		// the other authors
		for (int i = 1; i < authorGroups.size(); i++) {
			for (int authorId: authorGroups.get(i)) {
				credits.put(authorId, 0.0); 
			}
		}
	}
}
