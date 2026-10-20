package cn.edu.bjut.tools;

abstract class AbstractPair<T1, T2> {
	protected T1 first;
    protected T2 second;
    
    public T1 getFirst() {
    	return this.first;
    }
    
    public T2 getSecond() {
    	return this.second;
    }
    
    @Override
    public int hashCode() {
    	final int prime = 31;
		int result = 1;
		
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		
		return result;
	}
    
    @Override
    public String toString() { 
    	return this.first + "\t" + this.second; 
    }
}
