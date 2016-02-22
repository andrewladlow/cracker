package cracker;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import javax.xml.bind.DatatypeConverter;

public class Producer implements Runnable {
	
	private BlockingQueue<Item> queue;
	private List<String> dictionary;
	private int flag;
	
	public Producer(BlockingQueue<Item> queue, List<String> dictionary, int flag) {
		this.queue = queue;
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
		if (flag == 1) {
			for (String word : dictionary) {
				hashWord(msgDigest, word);
			}
		// whole word lower case
		} else if (flag == 2) {
			for (String word : dictionary) {
				word = setAllLowerCase(word);
				hashWord(msgDigest, word);
			}
		// whole word upper case
		} else if (flag == 4) {
			for (String word : dictionary) {
				word = setAllCapital(word);
				hashWord(msgDigest, word);
			}
		// reverse word
		} else if (flag == 3) {
			for (String word : dictionary) {
				String newWord = reverse(word);
				hashWord(msgDigest, newWord);
			}
		// first letter upper case
		} else if (flag == 5) {
			for (String word : dictionary) {
				word = setFirstCapital(word);
				hashWord(msgDigest, word);
			}
		// replace letters with digits
		} else if (flag == 6) {
			for (String word : dictionary) { 
				word = lettersToDigits(word);
				hashWord(msgDigest, word);
			}
		// first letter upper case + replace letters with digits
		} else if (flag == 7) {
			for (String word : dictionary) {
				word = setFirstCapital(word);
				word = lettersToDigits(word);
				hashWord(msgDigest, word);
			}
		// years added to end
		} else if (flag == 8) {
			for (int i = 1930; i <= 2016; i++) {
				for (String word : dictionary) {
					word = appendDigits(word, i);
					hashWord(msgDigest, word);
				}						
			}
		// 0-99 added to beginning
		} else if (flag == 9) {
			for (int i = 0; i <= 99; i++) {
				for (String word : dictionary) {
					word = prependDigits(word, i);
					hashWord(msgDigest, word);
				}						
			}
		// 0-99 added to end
		} else if (flag == 10) {
			for (int i = 0; i <= 99; i++) {
				for (String word : dictionary) {
					word = appendDigits(word, i);
					hashWord(msgDigest, word);
				}						
			}
		// first letter upper case + 0-99 added to end
		} else if (flag == 11) {
			for (int i = 0; i <= 99; i++) {
				for (String word : dictionary) {
					word = setFirstCapital(word);
					word = appendDigits(word, i);
					hashWord(msgDigest, word);
				}						
			}
		// first letter upper case + replace letters with digits + 0-99 added to end
		} else if (flag == 12) {
			for (int i = 0; i <= 99; i++) {
				for (String word : dictionary) {
					word = setFirstCapital(word);
					word = lettersToDigits(word);
					word = appendDigits(word, i);
					hashWord(msgDigest, word);
				}
			}
		// each word joined with every other word
		} else if (flag == 13) {
			for (String word : dictionary) {
				for (String otherWord : dictionary) {
					String newWord = concat(word, otherWord);
					hashWord(msgDigest, newWord);
				}
			}
		// each word joined with every other word + first letter upper case
		} else if (flag == 14) {
			for (String word : dictionary) {
				for (String otherWord : dictionary) {
					String newWord = concat(word, otherWord);
					newWord = setFirstCapital(newWord);
					hashWord(msgDigest, newWord);
				}
			}
		// each word joined with every other word + 0-99 added to end
		} else if (flag == 15) {
			for (int i = 0; i <= 99; i++) {
				for (String word : dictionary) {
					for (String otherWord : dictionary) {
						String newWord = concat(word, otherWord);
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
	
	private String prependDigits(String word, int digit) {
		return word.replaceAll("^", Integer.toString(digit));
	}
	
	private String setFirstCapital(String word) {
		return word.substring(0,1).toUpperCase() + word.substring(1);
	}
	
	private String setAllCapital(String word) {
		return word.toUpperCase();
	}
	
	private String setAllLowerCase(String word) {
		return word.toLowerCase();
	}
	
	private String lettersToDigits(String word) {
		word = word.replaceAll("a|A", "4");
		word = word.replaceAll("e|E", "3");
		word = word.replaceAll("i|I", "1");
		word = word.replaceAll("o|O", "0");	
		return word;
	}
	
	private String reverse(String word) {
		return new StringBuilder(word).reverse().toString();
	}
	
	private String concat(String word, String otherWord) {
		return word + otherWord;
	}
	
	private void hashWord(MessageDigest msgDigest, String word) {
		try { 
			byte[] digest = msgDigest.digest(word.getBytes("UTF-8"));
			String hex = DatatypeConverter.printHexBinary(digest);
			Item item = new Item(hex.toLowerCase(), word);
			// waits if queue is full
			queue.put(item);
		} catch (UnsupportedEncodingException | InterruptedException e) {
			e.printStackTrace();
		}	
	}
}
