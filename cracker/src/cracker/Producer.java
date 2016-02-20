package cracker;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
		this.dictionary = dict;
		this.flag = flag;
		
	}
	
	public void run() {
		MessageDigest msgDigest = null;
		try {
			msgDigest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} 
		
		//System.out.println("Cycle: " + cycle);
		
		// reset the dictionary contents on each cycle to revert permutations
		tmpDictionary = dictionary;
		
		// no modification
		if (flag == 0) {
			for (String word : tmpDictionary) {
				hashWord(msgDigest, word);
			}
		// first letter capital
		} else if (flag == 1) {
			for (String word : tmpDictionary) {
				word = setCapital(word);
				hashWord(msgDigest, word);
			}
		// 0 to 99 added to end
		} else if (flag == 2) {
			for (int i = 0; i <= 99; i++) {
				for (String word : tmpDictionary) {
					word = appendDigits(word, i);
					hashWord(msgDigest, word);
				}						
			}
		// first letter capital and 0-99 added to end
		} else if (flag == 3) {
			for (int i = 0; i <= 99; i++) {
				for (String word : tmpDictionary) {
					word = setCapital(word);
					word = appendDigits(word, i);
					hashWord(msgDigest, word);
				}						
			}
		// each word joined with every other word
		} else if (flag == 4) {
			for (String word : tmpDictionary) {
				for (String otherWord : tmpDictionary) {
					word = word + otherWord;
					hashWord(msgDigest, word);
				}
			}
		// combination of everything (probably won't complete in 10 mins)
		} else if (flag == 5) {
			for (int i = 0; i <= 99; i++) {
				for (String word : tmpDictionary) {
					for (String otherWord : tmpDictionary) {
						word = word + otherWord;
						word = setCapital(word);
						word = appendDigits(word, i);
						hashWord(msgDigest, word);
					}
				}
			}
		}
		
		System.out.println("Thread " + flag + " stopped");
	}
	
	private String appendDigits(String word, int digit) {
		return word.replaceAll("$", Integer.toString(digit));
	}
	
	private String setCapital(String word) {
		return word.substring(0,1).toUpperCase() + word.substring(1);
	}
	
	private void hashWord(MessageDigest msgDigest, String word) {
		try{ 
			byte[] digest = msgDigest.digest(word.getBytes("UTF-8"));
			String hex = DatatypeConverter.printHexBinary(digest);
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(hex.toLowerCase(), word);
			stack.put(map);
		} catch (UnsupportedEncodingException | InterruptedException e) {
			e.printStackTrace();
		}	
	}
	
}
