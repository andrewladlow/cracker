package cracker;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Driver2 {
	
	public static void main(String[] args) {
		String passPath = "password.txt";
		String dictPath = "rockyou.txt";
		
		// queue of hashed passwords added to by producer, taken by consumer
		BlockingQueue<HashMap> stack = new ArrayBlockingQueue<HashMap>(100);
		
		// checks given hashed password against hashes stored in password text file
		Consumer consumer = new Consumer(stack, passPath);
		
		// hashes passwords from those stored in dictionary text file
		Producer producer = new Producer(stack, dictPath);
		
		Thread consumerThread = new Thread(consumer);
		Thread producerThread = new Thread(producer);
		
		consumerThread.start();
		producerThread.start();
	}
	
	
}
