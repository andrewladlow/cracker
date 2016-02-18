package cracker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.bind.DatatypeConverter;


public class Cracker {
	
	private Object lock = new Object();
	
	private ArrayList<String> dict;
	private ArrayList<String> dictCopy;
	private ArrayList<String> hashedDict;
	//private List<String> hashedPasswords;
	private Map<Integer, String> hashedPasswords;
	
	private int passwordCount = 0;
	private int matchCount = 0;
	
	final protected static char[] hexArray = "0123456789abcdef".toCharArray();
	
	public Cracker(String dictPath, String passPath) throws Exception {	

		System.out.println("Loading dictionary...");
		long start = System.nanoTime();
		dict = new ArrayList<String>();	
		BufferedReader br = new BufferedReader(new FileReader(dictPath));
		String line;
		
		while ((line = br.readLine()) != null) {
			dict.add(line);
		}
		
		br.close();
		dictCopy = dict;
		long end = System.nanoTime();
		System.out.printf("Completed in %.1f seconds", (end - start) / 1e9);
		
		
		//hashedPasswords = readFromFile(passPath, true);
		
		System.out.println("\nLoading passwords...");
		start = System.nanoTime();	
		hashedPasswords = new ConcurrentHashMap<Integer, String>();
		br = new BufferedReader(new FileReader(passPath));
		
		while ((line = br.readLine()) != null) {
			hashedPasswords.put((Integer.parseInt(line.substring(line.indexOf("r")+1, line.indexOf(":")))),line.substring(line.indexOf(":")+1));
		}
		
		br.close();
		end = System.nanoTime();
		System.out.printf("Completed in %.1f seconds", (end - start) / 1e9);		
				
		// loaded both files, proceed with checking	
		
		//while (true) {
			this.hashDictionary();
			this.attack();
			hashedDict = this.transform();
		//}
	}
	
	private ArrayList<String> readFromFile(String filename, Boolean passFlag) throws Exception {
		long start = System.nanoTime();
		
		ArrayList<String> lines = new ArrayList<String>();
		
		BufferedReader br = new BufferedReader(new FileReader(filename));
		
		String line;
		// read each line in full if not password file
		if (!passFlag) {
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
		} else { // passwords in userX:pass format, we only need pass substring
			while ((line = br.readLine()) != null) {
				lines.add(line.substring(line.indexOf(":")+1));
			}
		}
		
		
		
		long end = System.nanoTime();
		
		br.close();
		
		System.out.printf("Completed in %.1f seconds", (end - start) / 1e9);
		
		return lines;
	}
	
	
/*	private void loadDictionary(String dictPath) throws Exception {
		System.out.println("Loading dictionary...");
		long start = System.nanoTime();
		
		BufferedReader brDict = new BufferedReader(new FileReader(dictPath));		
		//dict = new HashSet<String>();	
		//dict = new HashMap<Integer,String>();
		dict = new ArrayList<String>();
		
		String curWord;
		while ((curWord = brDict.readLine()) != null) {
			dict.add(curWord);
		}
		
		long end = System.nanoTime();
		brDict.close();
		
		System.out.printf("Loaded dictionary in %.1f seconds", (end - start) / 1e9);
	}
	
	private void loadPasswords(String passPath) throws Exception {
		System.out.println("\nLoading passwords...");
		long start = System.nanoTime();
		
		BufferedReader brPass = new BufferedReader(new FileReader(passPath));
		hashedPasswords = new ArrayList<String>();
		//hashedPasswords = new HashSet<String>();
		//hashedPasswords = new HashMap<String,Integer>();
		
		String curPass;
		while ((curPass = brPass.readLine()) != null) {
			// passwords in user:pass format but we only need pass substring
			//hashedPasswords.add(curPass.substring(curPass.indexOf(":")+1));
			//hashedPasswords.put(curPass, i);
			hashedPasswords.add(curPass);
		}
		
		long end = System.nanoTime();
		brPass.close();
		
		System.out.printf("Loaded passwords in %.1f seconds", (end - start) / 1e9);
	}*/
	
	private void hashDictionary() throws Exception {		
		System.out.println("\nHashing dictionary...");
		long start = System.nanoTime();
		
		// plaintext dict contents should first be hashed with SHA-256
		//hashedDict = new HashSet<String>();
		//hashedDict = new HashMap<String, Integer>();
		
		hashedDict = new ArrayList<String>();
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		
		// ~6 sec faster than for loop using rockyou dict (36 vs 42)
		dict.parallelStream()
			.forEachOrdered(word -> {
				try {
					byte[] digest = md.digest(word.getBytes("UTF-8"));
					String hex = DatatypeConverter.printHexBinary(digest);
					hashedDict.add(hex.toLowerCase());
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		
/*		for (int i = 0; i < dict.size(); i++) {
			byte[] digest = md.digest(dict.get(i).getBytes("UTF-8"));
			
			// convert bytes to string
			String hex = DatatypeConverter.printHexBinary(digest);
			hashedDict.add(hex.toLowerCase());
		}*/
		
		long end = System.nanoTime();
		
		System.out.printf("Hashed dictionary in %.1f seconds", (end - start) / 1e9);
	}
	
	private void attack() throws Exception {		
		
		System.out.println("\nBeginning dictionary attack...");
		System.out.println(hashedPasswords.size());
		long start = System.nanoTime();
		
		File output = new File("output.txt");
		if (output.exists()) {
			output.delete();
		}
		
		BufferedWriter outputWriter = new BufferedWriter(new FileWriter("output.txt"));
		
		// single thread implementation
		
/*		// double loop compares all hashed passes against each dictionary hash to check for matches
		for (int i = 0; i < hashedDict.size(); i++) {
			
			// new thread created for each dictionary word (assuming more words than hashed passwords)
				
			for (int j = 0; j < hashedPasswords.size(); j++) {
				if (hashedDict.get(i).equals(hashedPasswords.get(j))) {
					
					String match = new String("\nuser" + j + ":" + dict.get(i));
					try {
						outStream.write(match.getBytes());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}*/
		
		// parallel implementation -- ~0.3sec slower on 6.5mill dict (needs larger dict to reduce overhead cost?)
		// yes - rockyou.txt 6sec vs 21sec single thread
		
/*	    ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		//ExecutorService executor = Executors.newFixedThreadPool(1);
	    List<Future<String>> futures = new ArrayList<Future<String>>();
	    
	    // run smaller loop on first thread then larger loop in parallel to reduce overhead impact
	    for (int i = 0; i < hashedPasswords.size(); i++) {
	    	final int index = i;
	    	final String word1 = hashedPasswords.get(index);
	    	futures.add(executor.submit(() -> {
                final StringBuilder stringBuilder = new StringBuilder();
                for (int j = 0; j < hashedDict.size(); j++) {
                //for (int j = 0; j < dict.size(); j++) {
                	//System.out.println(index + ": " + j);
                	
                	String word2 = hashedDict.get(j);
                	
                	// hashing in parallel took much longer than precompute -- ~110 sec vs ~30 sec
                	
                	final String word2 = dict.get(j);
                	
            		final MessageDigest md = MessageDigest.getInstance("SHA-256");
        			md.reset();
        			md.update(word2.getBytes());
        			byte[] digest = md.digest();
        			
        			// convert bytes to string
        			String hex = DatatypeConverter.printHexBinary(digest);
        			hex = hex.toLowerCase();
                	
                	
                	//if (word1.equals(hex)) {
                	if (word1.equals(word2)) {
						stringBuilder.append("\nuser" + index + ":" + dict.get(j));
						this.matchCount++;
						break;
					}
                }
                return stringBuilder.toString();
	        }));
	    }
	    
	    executor.shutdown();
	   
	    for (Future<String> future : futures) {
	        //outStream.write(future.get().getBytes());
	    	outputWriter.write(future.get());
	    	//outputWriter.newLine();
	    	outputWriter.flush();
	    }*/
	    	    
		//outStream.close();
		
		
		//////////////////////////////////////////////////////////
		
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	    List<Future<String>> futures = new ArrayList<Future<String>>();
		
		for (int i = 0; i < hashedPasswords.size(); i++) {
			final int index = i;
			futures.add(executor.submit(() -> {
                final StringBuilder stringBuilder = new StringBuilder();
				if (hashedDict.contains(hashedPasswords.get(index))) {
					this.matchCount++;
					synchronized(lock) {
						stringBuilder.append(("user" + (index) + ":" + dict.get(hashedDict.indexOf(hashedPasswords.get(index)))));
	    				hashedPasswords.remove(index);
					}
				}
				return stringBuilder.toString();
			}));
		}    
		
		
	    executor.shutdown();
	    for (Future<String> future : futures) {
	        //outStream.write(future.get().getBytes());
	    	//System.out.println(future.get());
	    	outputWriter.write(future.get());
	    	outputWriter.newLine();
	    	outputWriter.flush();
	    }
		
/*		hashedPasswords.forEach((key, val) -> {
						   if (hashedDict.contains(val)) {
							   matchCount++;
							   synchronized (lock) {
								   try {
									   outputWriter.write("user" + key + ":" + dict.get(hashedDict.indexOf(val)));
									   hashedPasswords.remove(key);
									   outputWriter.newLine();
									   outputWriter.flush();
								   } catch (Exception e) {
									   e.printStackTrace();
								   }
							   }
						   }
					   });*/
		
		
		
		
		outputWriter.close();
		
		
		
		
		
		long end = System.nanoTime();		
		System.out.printf("Completed attack in %.1f seconds\n", (end - start) / 1e9);
		
		System.out.println(hashedPasswords.size());
/*		
		if (this.matchCount < this.passwordCount) {
			this.transform();
			// TODO track which passwords were matched and don't re-test them!
			this.attack();
		}*/
	}
	
	private ArrayList<String> transform() throws Exception {
		ArrayList<String> temp = hashedDict;
		
		for (int i = 0; i < dict.size(); i++) {
			
		}
		
		return temp;
	}
	
	
}

// cracked 139 / 209
