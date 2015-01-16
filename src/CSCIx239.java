import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES2;

public final class CSCIx239 {
	private static final void printShaderLog(GL2 gl2, int obj, File file) {
		IntBuffer len_b = IntBuffer.allocate(1);
		gl2.glGetShaderiv(obj, GL2ES2.GL_INFO_LOG_LENGTH, len_b);
		int len = len_b.get();
		if (len > 1) {
			IntBuffer n = IntBuffer.allocate(1);
			ByteBuffer buffer = ByteBuffer.allocate(len);
			gl2.glGetShaderInfoLog(obj, len, n, buffer);
			System.err.println(file.getName() + ":\n" + new String(buffer.array()));
		}
		gl2.glGetShaderiv(obj, GL2ES2.GL_COMPILE_STATUS, len_b);
		if (len_b.get() == 0) {
			throw new RuntimeException("Error compiling " + file.getName());
		}
	}

	private static final void printProgramLog(GL2 gl2, int obj) {
		IntBuffer len_b = IntBuffer.allocate(1);
		gl2.glGetShaderiv(obj, GL2ES2.GL_INFO_LOG_LENGTH, len_b);
		int len = len_b.get();
		if (len > 1) {
			IntBuffer n = IntBuffer.allocate(1);
			ByteBuffer buffer = ByteBuffer.allocate(len);
			gl2.glGetProgramInfoLog(obj, len, n, buffer);
			System.err.println(new String(buffer.array()));
		}
		gl2.glGetProgramiv(obj, GL2ES2.GL_LINK_STATUS, len_b);
		if (len_b.get() == 0) {
			throw new RuntimeException("Error linking program");
		}
	}

	private static final String[] readTextFromFile(File file) {
		try (FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);) {
			List<String> ret = new ArrayList<String>();
			String ln = null;
			while ((ln = br.readLine()) != null) {
				ret.add(ln);
			}
			return ret.toArray(new String[ret.size()]);
		} catch (IOException e) {
			throw new RuntimeException(e.getLocalizedMessage(), e);
		}
	}

	private static final void createShader(GL2 gl2, int prog, int type, File file) {
		int shader = gl2.glCreateShader(type);
		String[] source = readTextFromFile(file);
		gl2.glShaderSource(shader, 1, source, null);
		gl2.glCompileShader(shader);
		printShaderLog(gl2, shader, file);
		gl2.glAttachShader(prog, shader);
	}

	public static final int createShaderProg(GL2 gl2, String vertStr, String fragStr) {
		int prog = gl2.glCreateProgram();
		File vertFile = new File(vertStr);
		if (vertFile.exists()) {
			createShader(gl2, prog, GL2ES2.GL_VERTEX_SHADER, vertFile);
		}
		File fragFile = new File(fragStr);
		if (fragFile.exists()) {
			createShader(gl2, prog, GL2ES2.GL_FRAGMENT_SHADER, fragFile);
		}
		gl2.glLinkProgram(prog);
		printProgramLog(gl2, prog);
		return prog;
	}
}