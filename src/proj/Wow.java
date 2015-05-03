package proj;

public class Wow {
	public static final void main(String[] args) {
		generate(2);
	}

	private static final void generate(int n) {
		if (n == 0) {
			return;
		}
		for (byte b = 32; b < 127; b++) {
			rec(n - 1, new char[0], (char) b);
		}
		System.out.println("DONE!");
	}

	private static final void rec(int n, char[] p, char np) {
		char[] newPrefix = new char[p.length + 1];
		System.arraycopy(p, 0, newPrefix, 0, p.length);
		newPrefix[p.length] = np;
		if (n == 0) {
			for (int i = 0; i < newPrefix.length; i++) {
				System.out.print(newPrefix[i]);
			}
			System.out.println();
			return;
		}
		for (byte b = 32; b < 127; b++) {
			rec(n - 1, newPrefix, (char) b);
		}
	}
}