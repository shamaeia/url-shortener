package dls.util;

/**
 * A utility class for encoding the hashed values to base 62 strings
 * 
 */
public class Coder {

	/**
	 * the alphabet for the short URL
	 */
	private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private static final int BASE = ALPHABET.length();

	/**
	 * Encode a long integer to base 62 system
	 * 
	 * @param num
	 *            Long integer
	 * @return String representation of base 62
	 */
	public static String encode(long num) {
		StringBuilder sb = new StringBuilder();

		while (num > 0) {
			sb.append(ALPHABET.charAt((int) (num % BASE)));
			num /= BASE;
		}

		return sb.reverse().toString();
	}

	/**
	 * Check for validity of a short URL
	 * 
	 * @param shortUrl
	 *            The given short URL
	 * @return true if the length of the given short URL is less than 7, and all
	 *         the digits are from the ALPHABET
	 */
	public static boolean isValidShortUrl(String shortUrl) {
		if (shortUrl.length() > 7) {
			// each short URL is at most 7 character
			return false;
		}
		for (int i = 0; i < shortUrl.length(); i++) {
			if (ALPHABET.indexOf(shortUrl.charAt(i)) == -1) {
				return false;
			}
		}

		return true;
	}
}
