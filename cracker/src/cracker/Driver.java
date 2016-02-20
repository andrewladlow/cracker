package cracker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Driver {
	
	public static void main(String[] args) throws Exception {
		String passPath = "password.txt";
		String dictPath = "dictionary/dictionary.txt";
		
		System.out.println("Loading dictionary");
		
		List<String> dictionary = new ArrayList<String>();
		
		try (BufferedReader reader = new BufferedReader(new FileReader(dictPath))) {
			String line;			
			while ((line = reader.readLine()) != null) {
				dictionary.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Dictionary loaded");
	
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		// queue of hashmaps (in format hashedWord : word) added to by producer, taken by consumer
		BlockingQueue<Map<String, String>> stack = new ArrayBlockingQueue<Map<String, String>>(25000);
		
		// checks given hashed password against hashes stored in password text file		
        Runnable consumer = new Consumer(stack, passPath);
    	executor.execute(consumer);
    	System.out.println("Consumer started");
		
        for (int i = 0; i <= 7; i++) {
        	// each thread has increasing computation time based on i
            Runnable producer = new Producer(stack, dictionary, i);
        	executor.execute(producer);
        	System.out.println("Producer " + i + " started");
        }

	}
	
}
