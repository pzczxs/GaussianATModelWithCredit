package cn.edu.bjut.credit;

public class BylineInformation implements Comparable<BylineInformation> {
	private int authorId; 
	private int seqNo; 
	private boolean isReprint; 
	
	public BylineInformation(int authorId, int seqNo, boolean isReprint) {
		this.authorId = authorId; 
		this.seqNo = seqNo; 
		this.isReprint = isReprint; 
	}

	public int getAuthorId() {
		return authorId;
	}

	public void setAuthorId(int authorId) {
		this.authorId = authorId;
	}

	public int getSeqNo() {
		return seqNo;
	}

	public void setSeqNo(int seqNo) {
		this.seqNo = seqNo;
	}

	public boolean isReprint() {
		return isReprint;
	}

	public void setReprint(boolean isReprint) {
		this.isReprint = isReprint;
	}

	@Override
	public String toString() {
		return authorId + "\t" + seqNo + "\t" + (isReprint? 1: 0);
	}

	@Override
	public int compareTo(BylineInformation o2) {
		if ((isReprint && o2.isReprint) || (seqNo == 1 && o2.isReprint) || (isReprint && o2.seqNo == 1)) {
			return 0; 
		}
		
		if (isReprint && !o2.isReprint) {
			return -1; 
		}
		
		if (!isReprint && o2.isReprint) {
			return 1; 
		}
		
		if (seqNo > o2.seqNo) {
			return 1; 
		} else if (seqNo < o2.seqNo) {
			return -1; 
		} 
		
		return 0;
	}
}
