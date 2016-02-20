package cracker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import javax.xml.bind.DatatypeConverter;

public class Producer2 implements Runnable {
	
	private BlockingQueue<HashMap> stack;
	private List<String> dictionary;
	private List<String> tmpDictionary; 
	private int cycle;
	
	public Producer2(BlockingQueue<HashMap> stack, String dictPath, List<String> dict) {
		this.stack = stack;
		//this.dictionary = new ArrayList<String>();
		this.dictionary = dict;
		this.cycle = 0;
		
/*		try (BufferedReader reader = new BufferedReader(new FileReader(dictPath))) {
			String line;			
			while ((line = reader.readLine()) != null) {
				dictionary.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}
	
	public void run() {
		MessageDigest msgDigest = null;
		try {
			msgDigest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} 
		
		byte[] digest = null;
		while (true) {
			System.out.println("Cycle: " + cycle);
			
			// reset the dictionary contents on each cycle to revert permutations
			tmpDictionary = dictionary;
			
			for (String word : tmpDictionary) {
				// add 0-99 to end of word				
				//if (cycle >= 0 && cycle < 100) {
					word = word.replaceAll("$", Integer.toString(cycle));
				// set first character to capital
				/*} else if (cycle == 100) {
					word = word.substring(0,1).toUpperCase() + word.substring(1);
				// combination of both 
				} else if (cycle >= 100 && cycle < 201) {
					word = word.substring(0,1).toUpperCase() + word.substring(1);
					word = word.replaceAll("$", Integer.toString(cycle-101));
				// 
				} else if (cycle == 201) {
					//System.exit(0);
				}*/
				try {
					//System.out.println(word);
					digest = msgDigest.digest(word.getBytes("UTF-8"));
					String hex = DatatypeConverter.printHexBinary(digest);
					HashMap<String, String> map = new HashMap<String, String>();
					map.put(hex.toLowerCase(), word);
					stack.put(map);
					//System.out.println("Prod2");
				} catch (UnsupportedEncodingException | InterruptedException e) {
					e.printStackTrace();
				}		
			}
			
			cycle++;
		}		
	}
}
