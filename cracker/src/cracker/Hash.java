package cracker;

import java.security.MessageDigest;

import javax.xml.bind.DatatypeConverter;

/**
 * Class Hash provides a static method for computing the digest of a password.
 */
public class Hash {
	/**
	 * Compute the digest of the given password.
	 *
	 * @param  password  Password.
	 *
	 * @return  Digest.
	 */
	public static String passwordHash(String password) {
		try {
			MessageDigest md = MessageDigest.getInstance ("SHA-256");
			byte[] digest = md.digest(password.getBytes("UTF-8"));
			String hex = DatatypeConverter.printHexBinary(digest);
			hex = hex.toLowerCase();
			return hex;
		}
		catch (Throwable exc) {
			throw new IllegalStateException ("Shouldn't happen", exc);
		}
	}
}
