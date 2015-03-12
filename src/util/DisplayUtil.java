package util;

public class DisplayUtil {
	public static final void displayMatrix(double[] matrix) {
		StringBuilder sb = new StringBuilder();
		// first row
		sb.append("[");
		sb.append(matrix[0]).append(", ").append(matrix[1]).append(", ").append(matrix[2]).append(", ").append(matrix[3]);
		sb.append("]");
		sb.append("\r\n");
		// second row
		sb.append("[");
		sb.append(matrix[4]).append(", ").append(matrix[5]).append(", ").append(matrix[6]).append(", ").append(matrix[7]);
		sb.append("]");
		sb.append("\r\n");
		// third row
		sb.append("[");
		sb.append(matrix[8]).append(", ").append(matrix[9]).append(", ").append(matrix[10]).append(", ").append(matrix[11]);
		sb.append("]");
		sb.append("\r\n");
		// fourth row
		sb.append("[");
		sb.append(matrix[12]).append(", ").append(matrix[13]).append(", ").append(matrix[14]).append(", ").append(matrix[15]);
		sb.append("]");
		sb.append("\r\n");
		sb.append("==========================");
		System.out.println(sb.toString());
	}
}