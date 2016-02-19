/*package cracker;

import java.util.HashMap;

public class PasswordChecker implements Runnable {
	public void run() {
		HashMap<Integer, String> hashedPasswords = new HashMap<Integer, String>();
				hashedPasswords.forEach((key, val) -> {
		   if (hashedDict.contains(val)) {
			   matchCount++;
			   synchronized (lock) {
				   try {
					   outputWriter.write("user" + key + ":" + dict.get(hashedDict.indexOf(val)));
					   hashedPasswords.remove(key);
					   outputWriter.newLine();
					   outputWriter.flush();
				   } catch (Exception e) {
					   e.printStackTrace();
				   }
			   }
		   }
	   });
	}

}
*/