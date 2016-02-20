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

public class Producer implements Runnable {
	
	private BlockingQueue<HashMap> stack;
	private List<String> dictionary;
	private List<String> tmpDictionary; 
	private int flag;
	
	public Producer(BlockingQueue<HashMap> stack, List<String> dict, int flag) {
		this.stack = stack;
		//this.dictionary = new ArrayList<String>();
		this.dictionary = dict;
		this.flag = flag;
		
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
			//System.out.println("Cycle: " + cycle);
			
			// reset the dictionary contents on each cycle to revert permutations
			tmpDictionary = dictionary;
			
			for (String word : tmpDictionary) {
				System.out.println(flag);
				switch(flag) {
				case 0:
				case 1:
					word = setCapital(word);
				default:
				}
				
				try {
					System.out.println(word);
					digest = msgDigest.digest(word.getBytes("UTF-8"));
					String hex = DatatypeConverter.printHexBinary(digest);
					HashMap<String, String> map = new HashMap<String, String>();
					map.put(hex.toLowerCase(), word);
					stack.put(map);
					//System.out.println("Prod1");
				} catch (UnsupportedEncodingException | InterruptedException e) {
					e.printStackTrace();
				}		
			}
			
	}
	
	private String appendDigits(String word, int digit) {
		return word.replaceAll("$", Integer.toString(digit));
	}
	
	private String setCapital(String word) {
		return word.substring(0,1).toUpperCase() + word.substring(1);
	}
	
	private void hashWord(String word) {
		digest = msgDigest.digest(word.getBytes("UTF-8"));
		String hex = DatatypeConverter.printHexBinary(digest);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(hex.toLowerCase(), word);
		stack.put(map);
	}
	
}
