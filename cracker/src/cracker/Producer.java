package cracker;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import javax.xml.bind.DatatypeConverter;

public class Producer implements Runnable {
	
	private BlockingQueue<Map<String, String>> stack;
	private List<String> dictionary;
	private int flag;
	
	public Producer(BlockingQueue<Map<String, String>> stack, List<String> dictionary, int flag) {
		this.stack = stack;
		this.dictionary = dictionary;
		// flag indicates which permutations to perform
		this.flag = flag;	
	}
	
	public void run() {
		MessageDigest msgDigest = null;
		try {
			msgDigest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} 
		
		// no modification
		if (flag == 0) {
			for (String word : dictionary) {
				hashWord(msgDigest, word);
			}
		// first letter capital
		} else if (flag == 1) {
			for (String word : dictionary) {
				word = setCapital(word);
				hashWord(msgDigest, word);
			}
		// replace vowels with digits
		} else if (flag == 2) {
			for (String word : dictionary) { 
				word = lettersToDigits(word);
				hashWord(msgDigest, word);
			}
		// replace vowels with digits + first letter capital
		} else if (flag == 3) {
			for (String word : dictionary) {
				word = lettersToDigits(word);
				word = setCapital(word);
				hashWord(msgDigest, word);
			}
		// 0-99 added to end
		} else if (flag == 4) {
			for (int i = 0; i <= 99; i++) {
				for (String word : dictionary) {
					word = appendDigits(word, i);
					hashWord(msgDigest, word);
				}						
			}
		// first letter capital + 0-99 added to end
		} else if (flag == 5) {
			for (int i = 0; i <= 99; i++) {
				for (String word : dictionary) {
					word = setCapital(word);
					word = appendDigits(word, i);
					hashWord(msgDigest, word);
				}						
			}
		// first letter capital + replace vowels with digits + 0-99 added to end
		} else if (flag == 6) {
			for (int i = 0; i <= 99; i++) {
				for (String word : dictionary) {
					word = setCapital(word);
					word = lettersToDigits(word);
					word = appendDigits(word, i);
					hashWord(msgDigest, word);
				}
			}
		// each word joined with every other word
		} else if (flag == 7) {
			for (String word : dictionary) {
				for (String otherWord : dictionary) {
					String newWord = concat(word, otherWord);
					hashWord(msgDigest, newWord);
				}
			}
		// each word joined with every other word + first letter capital
		} else if (flag == 8) {
			for (String word : dictionary) {
				for (String otherWord : dictionary) {
					String newWord = concat(word, otherWord);
					newWord = setCapital(newWord);
					hashWord(msgDigest, newWord);
				}
			}
		// each word joined with every other word + first letter capital + 0-99 added to end
		} else if (flag == 9) {
			for (int i = 0; i <= 99; i++) {
				for (String word : dictionary) {
					for (String otherWord : dictionary) {
						String newWord = concat(word, otherWord);
						newWord = setCapital(newWord);
						newWord = appendDigits(newWord, i);
						hashWord(msgDigest, newWord);
					}
				}
			}
		}
		
		System.out.println("Producer " + flag + " stopped");
	}
	
	private String appendDigits(String word, int digit) {
		return word.replaceAll("$", Integer.toString(digit));
	}
	
	private String setCapital(String word) {
		return word.substring(0,1).toUpperCase() + word.substring(1);
	}
	
	private String lettersToDigits(String word) {
		word = word.replaceAll("a|A", "4");
		word = word.replaceAll("e|E", "3");
		word = word.replaceAll("i|I", "1");
		word = word.replaceAll("o|O", "0");	
		return word;
	}
	
	private String concat(String word, String otherWord) {
		return word + otherWord;
	}
	
	private void hashWord(MessageDigest msgDigest, String word) {
		try{ 
			byte[] digest = msgDigest.digest(word.getBytes("UTF-8"));
			String hex = DatatypeConverter.printHexBinary(digest);
			Map<String, String> map = new HashMap<String, String>();
			map.put(hex.toLowerCase(), word);
			// blocks and waits if queue is full
			stack.put(map);
		} catch (UnsupportedEncodingException | InterruptedException e) {
			e.printStackTrace();
		}	
	}	
}
