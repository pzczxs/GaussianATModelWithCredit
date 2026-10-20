package cn.edu.bjut.corpus;

public interface ICorpus {

    int getNumTerms();
    
    int getNumDocs();
    
    int getNumWords();
    
    int getNumWords(int m); 

    int[][] getDocWords();
    
    int[] getDocWords(int m);
}
