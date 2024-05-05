// IN2011 Computer Networks
// Coursework 2023/2024
//
// Construct the hashID for a string

import java.lang.StringBuilder;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class HashID {

	public static String computeHashID(String line) throws Exception {
		if (line.endsWith("\n")) {
			// What this does and how it works is covered in a later lecture
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(line.getBytes(StandardCharsets.UTF_8));
			byte[] digest = md.digest();

			// Convert byte array to a hex string
			StringBuilder hexString = new StringBuilder();
			for (byte b : digest) {
				hexString.append(Integer.toHexString(0xFF & b));
			}
			return hexString.toString();

		} else {
			// 2D#4 computes hashIDs of lines, i.e. strings ending with '\n'
			throw new Exception("No new line at the end of input to HashID");
		}
	}
}