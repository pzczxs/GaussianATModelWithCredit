package cn.edu.bjut.corpus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import cn.edu.bjut.tools.IntegerUnorderedPair;
import cn.edu.bjut.utils.SetUtils;

public class Wikipedia {
	private final static String SINGLE_TERM_FILE_NAME = "resources/enwiki-terms.zip"; 
	private final static String TERM_PAIR_FILE_NAME = "resources/enwiki-term-pairs.zip"; 
	
	private long totalWindows = -1; 
	
	public Wikipedia() {
		readTotalWindows(); 
	}
	
	private void readTotalWindows() {
		long totalWindows1 = readFirstLine(SINGLE_TERM_FILE_NAME); 
		long totalWindows2 = readFirstLine(TERM_PAIR_FILE_NAME); 
		
		if (totalWindows1 == -1 || totalWindows2 == -1 || totalWindows1 != totalWindows2) {
			throw new IllegalArgumentException("The totalWindows for Wikipedia statistics are not consistent."); 
		}
				
		this.totalWindows = totalWindows1; 
	}
	
	private long readFirstLine(String fname) {
		long total = -1; 
		
		int pos1 = fname.lastIndexOf("."); 
		int pos2 = fname.lastIndexOf('/'); 
		if (pos1 == -1 || pos2 == -1) {
			return total; 
		}
		String innerFileName = fname.substring(pos2 + 1, pos1) + ".txt"; 
		
		try (ZipFile zf = new ZipFile(fname)) {
			for (Enumeration<? extends ZipEntry> entries = zf.entries(); entries.hasMoreElements(); ) {
				ZipEntry entry = entries.nextElement(); 
				
				if (entry.isDirectory() || !entry.getName().trim().equalsIgnoreCase(innerFileName)) {
					continue; 
				}
				
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(zf.getInputStream(entry), StandardCharsets.UTF_8))) {
					total = Long.parseLong(reader.readLine().trim()); 
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return total; 
	}
	
	public Map<Integer, Integer> getTermCount(NavigableSet<Integer> termIdSet) {
		if (termIdSet == null || termIdSet.isEmpty()) {
			return null; 
		}
		
		int pos1 = SINGLE_TERM_FILE_NAME.lastIndexOf("."); 
		int pos2 = SINGLE_TERM_FILE_NAME.lastIndexOf('/'); 
		if (pos1 == -1 || pos2 == -1) {
			return null; 
		}
		String innerFileName = SINGLE_TERM_FILE_NAME.substring(pos2 + 1, pos1) + ".txt"; 
		
		Map<Integer, Integer> termIdToCount = new HashMap<Integer, Integer>();
		int maxTermId = SetUtils.maxValue(termIdSet); 
		try (ZipFile zf = new ZipFile(SINGLE_TERM_FILE_NAME)) {
			for (Enumeration<? extends ZipEntry> entries = zf.entries(); entries.hasMoreElements(); ) {
				ZipEntry entry = entries.nextElement(); 
				
				if (entry.isDirectory() || !entry.getName().trim().equalsIgnoreCase(innerFileName)) {
					continue; 
				}
				
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(zf.getInputStream(entry), StandardCharsets.UTF_8))) {
					for (String line; (line = reader.readLine()) != null;) {
						String[] parts = line.trim().split("\\s+"); 
						
						if (parts.length != 2) {
							continue; 
						}
						
						int termId = Integer.parseInt(parts[0]); 
						if (!termIdSet.contains(termId)) {
							continue; 
						}
						
						if (termId > maxTermId) {
							break; 
						}
						
						int count = Integer.parseInt(parts[1]); 
						termIdToCount.put(termId, count); 
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return termIdToCount.isEmpty()? null: termIdToCount; 
	}
	
	public Map<IntegerUnorderedPair, Integer> getTermPairCount(NavigableSet<Integer> termIdSet) {
		if (termIdSet == null || termIdSet.isEmpty()) {
			return null; 
		}
		
		int pos1 = TERM_PAIR_FILE_NAME.lastIndexOf("."); 
		int pos2 = TERM_PAIR_FILE_NAME.lastIndexOf('/'); 
		if (pos1 == -1 || pos2 == -1) {
			return null; 
		}
		String innerFileName = TERM_PAIR_FILE_NAME.substring(pos2 + 1, pos1) + ".txt"; 
		
		Map<IntegerUnorderedPair, Integer> termIdPairToCount = new HashMap<IntegerUnorderedPair, Integer>();
		int submaxTermId = SetUtils.submaxValue(termIdSet); 
		try (ZipFile zf = new ZipFile(TERM_PAIR_FILE_NAME)) {
			for (Enumeration<? extends ZipEntry> entries = zf.entries(); entries.hasMoreElements(); ) {
				ZipEntry entry = entries.nextElement(); 
				
				if (entry.isDirectory() || !entry.getName().trim().equalsIgnoreCase(innerFileName)) {
					continue; 
				}
				
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(zf.getInputStream(entry), StandardCharsets.UTF_8))) {
					for (String line; (line = reader.readLine()) != null;) {
						String[] parts = line.trim().split("\\s+"); 
						
						if (parts.length != 3) {
							continue; 
						}
						
						int termId1 = Integer.parseInt(parts[0]); 
						int termId2 = Integer.parseInt(parts[1]); 
						if (!termIdSet.contains(termId1) || !termIdSet.contains(termId2)) {
							continue; 
						}
						
						if (termId1 > submaxTermId) {
							break; 
						}
						
						int count = Integer.parseInt(parts[2]); 
						IntegerUnorderedPair pair = new IntegerUnorderedPair(termId1, termId2);
						termIdPairToCount.put(pair, count); 
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return termIdPairToCount.isEmpty()? null: termIdPairToCount; 
	}

	public long getTotalWindows() {
		return totalWindows;
	}
}
