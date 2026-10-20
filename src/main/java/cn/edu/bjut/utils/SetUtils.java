package cn.edu.bjut.utils;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NavigableSet;

public class SetUtils {
	public static <T> T maxValue(NavigableSet<T> set) {
		if (set == null || set.isEmpty()) {
			throw new IllegalArgumentException("The set is null or has not any element.");
		}

		return set.last();
	}

	public static <T> T submaxValue(NavigableSet<T> set) {
		if (set == null || set.size() < 2) {
			throw new IllegalArgumentException("The set is null or has less than 2 elements.");
		}

		return set.lower(set.last());
	}

	// compare whether two LinkedHashSets have the same element order
	public static <T> boolean equals(LinkedHashSet<T> set1, LinkedHashSet<T> set2) {
		if (set1 == null || set2 == null || set1.size() != set2.size()) {
			return false; 
		}
		
		if (set1 == set2) {
			return true; 
		}
		
		for (Iterator<T> it1 = set1.iterator(), it2 = set2.iterator(); it1.hasNext() && it2.hasNext(); ) {
			if (!it1.next().equals(it2.next())) {
				return false; 
			}
		}
		
		return true; 
	}
}
