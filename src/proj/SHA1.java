package proj;

public class SHA1 {
	private static final int h0 = 0x67452301;
	private static final int h1 = 0xEFCDAB89;
	private static final int h2 = 0x98BADCFE;
	private static final int h3 = 0x10325476;
	private static final int h4 = 0xC3D2E1F0;

	public static final void main(String[] args) {
		// Data input
		byte[] input = "8A28F1A8B8938A2C1F3572B04C48D5DF52281EE211AD73F77F9704E".getBytes();
		int len = input.length;
		// SHA-1 Logic
		int ml = len * 8;
		byte[] buffer = new byte[64];
		for (int i = 0; i < len; i++) {
			buffer[i] = input[i];
		}
		buffer[len] = (byte) 0x80; // Append bit '1' to message
		// Now add the length (in bits) of the entire message as 64 bits
		buffer[60] = (byte) (ml >>> 24 & 0xFF);
		buffer[61] = (byte) (ml >>> 16 & 0xFF);
		buffer[62] = (byte) (ml >>> 8 & 0xFF);
		buffer[63] = (byte) (ml & 0xFF);
		//System.out.println(ml);
		//System.out.println(toView(buffer));
		// Process the message in successive 512-bit chunks
		int[] w = new int[80];
		for (int i = 0; i < 16; i++) {
			w[i] += (buffer[(4 * i)] & 0xFF) << 24;
			w[i] += (buffer[(4 * i) + 1] & 0xFF) << 16;
			w[i] += (buffer[(4 * i) + 2] & 0xFF) << 8;
			w[i] += (buffer[(4 * i) + 3] & 0xFF);
			//System.out.println(w[i]);
		}
		// Extend the sixteen 32-bit words into eighty 32-bit words:
		for (int i = 16; i < 80; i++) {
			w[i] = Integer.rotateLeft((w[i - 3] ^ w[i - 8] ^ w[i - 14] ^ w[i - 16]), 1);
		}
		// Initialize output
		int[] h = new int[] {h0, h1, h2, h3, h4};
		// Initialize hash value for this chunk:
		int a = h[0];
		int b = h[1];
		int c = h[2];
		int d = h[3];
		int e = h[4];
		// Main loop:
		for (int i = 0; i < 80; i++) {
			int f;
			int k;
			if (i >= 0 && i <= 19) {
				f = (b & c) | (~b & d);
				k = 0x5A827999;
			} else if (i >= 20 && i <= 39) {
				f = b ^ c ^ d;
				k = 0x6ED9EBA1;
			} else if (i >= 40 && i <= 59) {
				f = (b & c) | (b & d) | (c & d);
				k = 0x8F1BBCDC;
			} else if (i >= 60 && i <= 79) {
				f = b ^ c ^ d;
				k = 0xCA62C1D6;
			} else {
				return; // assertion error
			}
			int temp = Integer.rotateLeft(a, 5) + f + e + k + w[i];
			e = d;
			d = c;
			c = Integer.rotateLeft(b, 30);
			b = a;
			a = temp;
		}
		// Add this chunk's hash to result so far:
		h[0] = h[0] + a;
		h[1] = h[1] + b;
		h[2] = h[2] + c;
		h[3] = h[3] + d;
		h[4] = h[4] + e;
		System.out.print(Integer.toHexString(h[0]));
		System.out.print(Integer.toHexString(h[1]));
		System.out.print(Integer.toHexString(h[2]));
		System.out.print(Integer.toHexString(h[3]));
		System.out.print(Integer.toHexString(h[4]));
		System.out.println();
		System.out.println("2538dc74217575087468382efa81381cd65f0445");
	}

	private static final String toView(byte[] bytes) {
		//System.out.println("Length: " + bytes.length);
		StringBuilder sb = new StringBuilder(bytes.length + bytes.length * Byte.SIZE);
		for (int i = 0; i < bytes.length; i++) {
			byte b = bytes[i];
			String visual = StringUtil.getLeftPaddedStr(Integer.toBinaryString(b & 0xFF), '0', Byte.SIZE);
			sb.append(visual).append(' ');
			if ((i + 1) % 11 == 0) {
				//sb.append("\r\n");
			}
		}
		return sb.toString();
	}
}