package cn.edu.bjut.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

public class MapUtils {
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry: list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}
	
	// sort by descending
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueReverse(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return -(o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}
	
	public static <K, V> NavigableSet<V> mergeValues(Map<K, ? extends Set<V>> idToSet) {
		if (idToSet == null || idToSet.isEmpty()) {
			return null;
		}

		NavigableSet<V> mergedSet = new TreeSet<V>();
		for (Set<V> set : idToSet.values()) {
			mergedSet.addAll(set);
		}

		return mergedSet;
	}
	
	// compare whether or not two Maps are equal
	public static <K, V> boolean equals(Map<K, LinkedHashSet<V>> map1, Map<K, LinkedHashSet<V>> map2) {
		if (map1 == null || map2 == null || map1.size() != map2.size()) {
			return false; 
		}
		
		if (map1 == map2) {
			return true; 
		}
		
		if (!map1.keySet().equals(map2.keySet())) {
			return false; 
		}
		
		for (Map.Entry<K, LinkedHashSet<V>> entry: map1.entrySet()) {
			LinkedHashSet<V> set1 = entry.getValue(); 
			LinkedHashSet<V> set2 = map2.get(entry.getKey()); 
			
			if (!SetUtils.equals(set1, set2)) {
				return false; 
			}
		}
		
		return true; 
	}
}
