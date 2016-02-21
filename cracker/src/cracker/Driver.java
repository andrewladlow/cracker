package cracker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Driver {
	
	public static void main(String[] args) throws Exception {
		String passPath = "password.txt";
		String dictPath = "merged.txt";
		
		System.out.println("Loading dictionary");
		
		List<String> dictionary = new ArrayList<String>();
	//	Set<String> hs = new HashSet<String>();
		
		try (BufferedReader reader = new BufferedReader(new FileReader(dictPath))) {
			String line;			
			while ((line = reader.readLine()) != null) {
				dictionary.add(line);
				//hs.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Dictionary loaded");
		
/*		System.out.println(dictionary.size());
		System.out.println(hs.size());
		
		File output = new File("output.txt");
		if (output.exists()) {
			output.delete();
		}
		
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter("output.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (String word : hs) {
			writer.write(word);
			writer.newLine();
			writer.flush();
		}
		
		*/
		
		
	
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		//ExecutorService executor = Executors.newFixedThreadPool(4);
		
		// queue of hashmaps (in format hashedWord : word) added to by producers, taken by consumer
		BlockingQueue<Map<String, String>> queue = new LinkedBlockingQueue<Map<String, String>>();
		
		// checks given hashed word from producers against hashes stored in password text file
        Runnable consumer = new Consumer(queue, passPath);
    	executor.execute(consumer);
    	System.out.println("Consumer started");
		
    	// each thread has increasing computation time based on i
        for (int i = 0; i <= 12; i++) {
        	// hashes words from those stored in dictionary text file, sends to consumer to check
            Runnable producer = new Producer(queue, dictionary, i);
        	executor.execute(producer);
        	System.out.println("Producer " + i + " started");
        }

	}
	
}
