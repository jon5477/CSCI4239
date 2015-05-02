package proj;

import java.security.NoSuchAlgorithmException;

public class MD5 {
	private static final int[] s = new int[] {
		7, 12, 17, 22,  7, 12, 17, 22,  7, 12, 17, 22,  7, 12, 17, 22,
		5,  9, 14, 20,  5,  9, 14, 20,  5,  9, 14, 20,  5,  9, 14, 20,
		4, 11, 16, 23,  4, 11, 16, 23,  4, 11, 16, 23,  4, 11, 16, 23,
		6, 10, 15, 21,  6, 10, 15, 21,  6, 10, 15, 21,  6, 10, 15, 21
	};
	private static final int[] K = new int[] {
		0xd76aa478, 0xe8c7b756, 0x242070db, 0xc1bdceee,
		0xf57c0faf, 0x4787c62a, 0xa8304613, 0xfd469501,
		0x698098d8, 0x8b44f7af, 0xffff5bb1, 0x895cd7be,
		0x6b901122, 0xfd987193, 0xa679438e, 0x49b40821,
		0xf61e2562, 0xc040b340, 0x265e5a51, 0xe9b6c7aa,
		0xd62f105d, 0x02441453, 0xd8a1e681, 0xe7d3fbc8,
		0x21e1cde6, 0xc33707d6, 0xf4d50d87, 0x455a14ed,
		0xa9e3e905, 0xfcefa3f8, 0x676f02d9, 0x8d2a4c8a,
		0xfffa3942, 0x8771f681, 0x6d9d6122, 0xfde5380c,
		0xa4beea44, 0x4bdecfa9, 0xf6bb4b60, 0xbebfbc70,
		0x289b7ec6, 0xeaa127fa, 0xd4ef3085, 0x04881d05,
		0xd9d4d039, 0xe6db99e5, 0x1fa27cf8, 0xc4ac5665,
		0xf4292244, 0x432aff97, 0xab9423a7, 0xfc93a039,
		0x655b59c3, 0x8f0ccc92, 0xffeff47d, 0x85845dd1,
		0x6fa87e4f, 0xfe2ce6e0, 0xa3014314, 0x4e0811a1,
		0xf7537e82, 0xbd3af235, 0x2ad7d2bb, 0xeb86d391
	};
	private static final int A = 0x67452301;
	private static final int B = 0xEFCDAB89;
	private static final int C = 0x98BADCFE;
	private static final int D = 0x10325476;

	public static final void main(String[] args) throws NoSuchAlgorithmException {
		// Data input
		byte[] input = "8A28F1A8B8938A2C1F3572B04C48D5DF52281EE211AD73F77F9704E".getBytes();
		int len = input.length;
		// MD5 Logic
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
		int[] M = new int[16];
		for (int i = 0; i < 16; i++) {
			M[i] += (buffer[(4 * i)] & 0xFF) << 24;
			M[i] += (buffer[(4 * i) + 1] & 0xFF) << 16;
			M[i] += (buffer[(4 * i) + 2] & 0xFF) << 8;
			M[i] += (buffer[(4 * i) + 3] & 0xFF);
			//System.out.println(M[i]);
		}
		// Initialize output
		int[] h = new int[] {A, B, C, D};
		// Initialize hash value for this chunk:
		int a = h[0];
		int b = h[1];
		int c = h[2];
		int d = h[3];
		// Main loop:
		for (int i = 0; i < 64; i++) {
			int F;
			int g;
			if (i >= 0 && i <= 15) {
				F = (b & c) | ((~b) & d);
				g = i;
			} else if (i >= 16 && i <= 31) {
				F = (d & b) | ((~d) & c);
				g = (5 * i + 1) % 16;
			} else if (i >= 32 && i <= 47) {
				F = b ^ c ^ d;
				g = (3 * i + 5) % 16;
			} else if (i >= 48 && i <= 63) {
				F = c ^ (b | (~d));
				g = (7 * i) % 16;
			} else {
				return; // assertion error
			}
			int dTemp = d;
			d = c;
			c = b;
			b = b + Integer.rotateLeft((a + F + K[i] + M[g]), s[i]);
			a = dTemp;
		}
		//Add this chunk's hash to result so far:
		h[0] = h[0] + a;
		h[1] = h[1] + b;
		h[2] = h[2] + c;
		h[3] = h[3] + d;
		System.out.print(Integer.toHexString(h[0]));
		System.out.print(Integer.toHexString(h[1]));
		System.out.print(Integer.toHexString(h[2]));
		System.out.print(Integer.toHexString(h[3]));
		System.out.println();
		System.out.println("11c39f733f718cf653aeb8907d2acad2");
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