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
	private int cycle;
	
	public Producer(BlockingQueue<HashMap> stack, String dictPath) {
		this.stack = stack;
		this.dictionary = new ArrayList<String>();
		this.cycle = -1;
		
		try (BufferedReader reader = new BufferedReader(new FileReader(dictPath))) {
			String line;			
			while ((line = reader.readLine()) != null) {
				dictionary.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			
			tmpDictionary = dictionary;
			
			for (String word : tmpDictionary) {
				// add 0-99 to end of word				
				if (cycle >= 0 && cycle < 100) {
					//for (int i = 0; i < 100; i++) {
						//System.out.println("TEST1: " + word);
						word = word.replaceAll("$", Integer.toString(cycle));
						//System.out.println("TEST2: " + word);
					//}		
				}
				try {
					digest = msgDigest.digest(word.getBytes("UTF-8"));
					String hex = DatatypeConverter.printHexBinary(digest);
					HashMap<String, String> map = new HashMap<String, String>();
					map.put(hex.toLowerCase(), word);
					stack.put(map);
					//stack.put(hex.toLowerCase());
				} catch (UnsupportedEncodingException | InterruptedException e) {
					e.printStackTrace();
				}		
			}
			
			cycle++;
			System.out.println(cycle);
		}		
	}
}
