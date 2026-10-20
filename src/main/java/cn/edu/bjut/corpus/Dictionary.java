package cn.edu.bjut.corpus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import cn.edu.bjut.utils.MapUtils;

public class Dictionary {
	private HashBiMap<String, Integer> wordMap; 
	private BiMap<Integer, String> wordMapInv; 
	
	public Dictionary() {
		this.wordMap = HashBiMap.create();
		this.wordMapInv = wordMap.inverse(); 
	}
	
	public HashBiMap<String, Integer> getWord2IdMap() {
		return wordMap; 
	}
	
	public BiMap<Integer, String> getId2WordMap() {
		return wordMapInv; 
	}
	
	public String get(int id) {
		return wordMapInv.get(id);
	}
	
	public Integer get(String word) {
		return wordMap.get(word);
	}

	public boolean contains(String word) {
		return wordMap.containsKey(word);
	}
	
	public boolean contains(int id) {
		return wordMapInv.containsKey(id);
	}

	public int add(String word){
		if (!contains(word)){
			int id = wordMap.size();
			wordMap.put(word, id);
			
			return id;
		} 
		
		return get(word);
	}
	
	public int add(CharSequence word) {
		return add(word.toString()); 
	}
	
	public int size() {
		return wordMap.size(); 
	}
	
	public Dictionary intersection(Dictionary another) {
		Dictionary dict = new Dictionary(); 
		
		for (Map.Entry<String, Integer> entry: wordMap.entrySet()) {
			if (another.wordMap.containsKey(entry.getKey())) {
				dict.add(entry.getKey()); 
			}
		}
		
		return dict; 
	}
	
	public Dictionary union(Dictionary another) {
		Dictionary dict = new Dictionary(); 
		
		for (Map.Entry<String, Integer> entry: wordMap.entrySet()) {
			dict.add(entry.getKey()); 
		}
		
		for (Map.Entry<String, Integer> entry: another.wordMap.entrySet()) {
			dict.add(entry.getKey()); 
		}
		
		return dict; 
	}
	
	/**
	 * read dictionary from file
	 */
	public boolean readWordMap(String wordMapFile){		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(wordMapFile), "UTF-8"));
			
			//read map
			for (String line; (line = reader.readLine()) != null; ) {
				int pos = line.trim().lastIndexOf('='); 
				if (pos == -1) {
					continue; 
				}
				
				String word = line.trim().substring(0, pos);
				int id = Integer.parseInt(line.trim().substring(pos + 1));
				
				wordMap.put(word, id);
			}
			
			reader.close();
			return true;
		} catch (Exception e) {
			System.err.println("Error while reading dictionary:" + e.getMessage());
			e.printStackTrace();
			return false;
		}		
	}
	
	public boolean writeWordMap(String wordMapFile){
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(wordMapFile), "UTF-8"));

			//write word to id
			for (Map.Entry<String, Integer> e: MapUtils.sortByValue(wordMap).entrySet()) {
				writer.write(e.getKey() + "=" + e.getValue() + "\n"); 
				
			}
			
			writer.close();
			return true;
		}  catch (Exception e) {
			System.err.println("Error while writing word map " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
}
