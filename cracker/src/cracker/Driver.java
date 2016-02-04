package cracker;

import java.util.Scanner;

public class Driver {
	public static void main (String args[]) throws Exception {
		Scanner scanner = new Scanner(System.in);
		
		// dictSmall.txt // dictionary.txt
		System.out.println("Enter the file path to the dictionary list: ");
		String dictPath = scanner.nextLine();
		
		// hashes.txt // password.txt
		System.out.println("Enter the file path to the hashed password list: ");
		String passPath = scanner.nextLine();
		
		scanner.close();
		
		Cracker c = new Cracker(dictPath, passPath);
		
	}

}
