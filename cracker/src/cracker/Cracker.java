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
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.bind.DatatypeConverter;


public class Cracker {
	
	private Object lock = new Object();
	
	private List<String> dict;
	private List<String> hashedDict;
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
		long end = System.nanoTime();	
		System.out.printf("Completed in %.1f seconds", (end - start) / 1e9);	
		
		System.out.println("\nLoading passwords...");
		start = System.nanoTime();	
		hashedPasswords = new HashMap<Integer, String>();
		br = new BufferedReader(new FileReader(passPath));
		
		while ((line = br.readLine()) != null) {
			hashedPasswords.put(Integer.parseInt(line.substring(4, 5)),line.substring(line.indexOf(":")+1));
		}
		
		br.close();
		end = System.nanoTime();
		System.out.printf("Completed in %.1f seconds", (end - start) / 1e9);		
				
		// loaded both files, proceed with checking	
		this.hashDictionary();
		this.attack();
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
		
		// ~6 sec faster than for loop using rockyou dict
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
	    
/*	    // run smaller loop on first thread then larger loop in parallel to reduce overhead impact
	    for (int i = 0; i < hashedDict.size(); i++) {
	    	final int index = i;
	    	executor.execute(() -> {
	    		if (hashedPasswords.containsKey(hashedDict.get(index))) {
	    			this.matchCount++;
	    			
	    			try {
	    				outputWriter.write("user" + hashedPasswords.get(hashedDict.get(index))+1 + ":" + dict.get(index));
	    				outputWriter.newLine();
	    				outputWriter.flush();
	    			} catch (Exception e) {
	    				e.printStackTrace();
	    			}
	    		}
	        });
	    }*/
		
/*		for (int i = 0; i < hashedPasswords.size(); i++) {
			final int index = i;
			executor.execute(() -> {
				if (hashedDict.contains(hashedPasswords.get(index))) {
				//if (hashedDict.containsKey(hashedPasswords.get(index))) {
					this.matchCount++;
					synchronized (lock) {
						try {
			    				outputWriter.write("user" + (index) + ":" + dict.get(hashedDict.indexOf(hashedPasswords.get(index))));
			    				outputWriter.newLine();
			    				outputWriter.flush();
							
		    			} catch (Exception e) {
		    				e.printStackTrace();
		    			}
					}
				}
			});
		}*/    
/*	    // shouldn't get to this stage?
	    executor.shutdown();
	    if (executor.isTerminated()) {
			outputWriter.close();
	    	//outStream.close();
	    }*/
		
		hashedPasswords.entrySet()
					   .parallelStream()
					   .forEach(password -> {
						   if (hashedDict.contains(password)) {
							   matchCount++;
							   synchronized (lock) {
								   try {
									   outputWriter.write("user" + password.getKey() + ":" + dict.get(hashedDict.indexOf(password)));
									   outputWriter.newLine();
									   outputWriter.flush();
								   } catch (Exception e) {
									   e.printStackTrace();
								   }
							   }
						   }
					   });
		outputWriter.close();
		
		
		
		
		long end = System.nanoTime();		
		System.out.printf("Completed attack in %.1f seconds\n", (end - start) / 1e9);
/*		
		if (this.matchCount < this.passwordCount) {
			this.transform();
			// TODO track which passwords were matched and don't re-test them!
			this.attack();
		}*/
	}
	
	private void transform() throws Exception {
		// apply transformations to dictionary list to further increase crack possibility
		// TODO
	}
	
	
}
