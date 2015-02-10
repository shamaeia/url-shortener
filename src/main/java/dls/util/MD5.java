package dls.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A class for calculating the hash values based on MD5 algorithm
 */
public class MD5 {

	/**
	 * Calculate the MD5 hash value of a given string
	 * 
	 * @param input
	 *            The given string to be hashed
	 * @param repeat
	 *            Rehash in the number of repeat, mainly to deal with the rare
	 *            situations of an existing hashed value
	 * @return The 128 bit or 32 character string
	 */
	public static String getMD5(String input, int repeat) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			for (int i = 0; i < repeat; i++) {
				md.update(input.getBytes());
			}
			byte[] digest = md.digest();
			StringBuffer sb = new StringBuffer();
			for (byte b : digest) {
				sb.append(String.format("%02x", b & 0xff));
			}

			return sb.toString();

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
