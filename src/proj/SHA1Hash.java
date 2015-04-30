package proj;

public class SHA1Hash {
	public static final int h0 = 0x67452301;
	public static final int h1 = 0xEFCDAB89;
	public static final int h2 = 0x98BADCFE;
	public static final int h3 = 0x10325476;
	public static final int h4 = 0xC3D2E1F0;

	private static final byte[] hash(byte[] msg) {
		int originalLength = msg.length; // 8 bits in a byte
		int tailLength = originalLength % 64;
		int padLength = 0;
		if (64 - tailLength >= 9) {
			padLength = 64 - tailLength;
		} else {
			padLength = 128 - tailLength;
		}
		byte[] lol = new byte[14];
	}
}