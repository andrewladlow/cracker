package cracker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class Consumer implements Runnable {
	
	private BlockingQueue<Map<String, String>> queue;
	private Map<String, String> passwords;
	
	public Consumer(BlockingQueue<Map<String, String>> queue, String passPath) {
		this.queue = queue;
		this.passwords = new HashMap<String, String>();
		
		try (BufferedReader reader = new BufferedReader(new FileReader(passPath))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String password = line.substring(line.indexOf(":")+1);
				String user = line.substring(0, line.indexOf(":")+1);
				passwords.put(password, user);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	@SuppressWarnings("resource")
	public void run() {
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
		
		int matchCount = 0;
		int passwordCount = passwords.size();
		while (true) {
			try {
				// wait until queue has content from producers
				Map<String, String> map = queue.take();
				Map.Entry<String, String> entry = map.entrySet().iterator().next();
				
				if (passwords.containsKey(entry.getKey())) {
					matchCount++;
					System.out.println("Passwords matched: " + matchCount + " / " + passwordCount);
					// retrieves corresponding user from passwords, then plaintext pass from queue hashmap
					writer.write(passwords.get(entry.getKey()) + entry.getValue());
					writer.newLine();
					writer.flush();
					passwords.remove(entry.getKey());				
				}				
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
		}
	}
}
