package cracker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Driver2 {
	private static List<String> dictionary;
	
	public static void main(String[] args) throws Exception {
		String passPath = "password.txt";
		String dictPath = "rockyou.txt";
		
		dictionary = new ArrayList<String>();
		
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
		
		// queue of hashed passwords added to by producer, taken by consumer
		BlockingQueue<HashMap> stack = new ArrayBlockingQueue<HashMap>(1000);
		
		// checks given hashed password against hashes stored in password text file
		Consumer consumer = new Consumer(stack, passPath);
		Thread consumerThread = new Thread(consumer);
		consumerThread.start();
		
/*		Producer p1 = new Producer(stack, dictionary, 0);
		Producer p2 = new Producer(stack, dictionary, 1);
		Producer p3 = new Producer(stack, dictionary, 2);
		Producer p4 = new Producer(stack, dictionary, 3);
		
		Thread pt1 = new Thread(p1);
		Thread pt2 = new Thread(p2);
		Thread pt3 = new Thread(p3);
		Thread pt4 = new Thread(p4);
		
		pt1.start();
		pt2.start();
		pt3.start();
		pt4.start();
*/
		
        for (int i = 0; i <= 5; i++) {
            Runnable producer = new Producer(stack, dictionary, i);
        	executor.execute(producer);
        }

	}
	
}
