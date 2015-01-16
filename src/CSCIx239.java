import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.gl2.GLUT;

public final class CSCIx239 {
	private CSCIx239() {
	}

	public static double Sin(double th) {
		return Math.sin(Math.PI / (180 * th));
	}

	public static double Cos(double th) {
		return Math.cos(Math.PI / (180 * th));
	}

	public static void print(GLUT glt, String s) {
		for (int i = 0, n = s.length(); i < n; i++) {
			glt.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, s.charAt(i));
		}
	}

	public static void fatal(String s) {
		System.err.println(s);
		System.exit(1);
	}

	public static void errCheck(GL2 gl2, String s) {
		int err = gl2.glGetError();
		if (err > 0) {
			GLU glu = GLU.createGLU(gl2);
			String errstr = glu.gluErrorString(err);
			System.err.println("ERROR: " + errstr + " [" + s + "]");
		}
	}

	public static int loadTexBMP(File file) {
		// TODO STUB
		return 0;
	}

	public static void project(GL2 gl2, double fov, double asp,double dim) {
		// Tell OpenGL we want to manipulate the projection matrix
		gl2.glMatrixMode(GL2.GL_PROJECTION);
		// Undo previous transformations
		gl2.glLoadIdentity();
		// Perspective transformation
		if (fov > 0.0) {
			GLU glu = GLU.createGLU(gl2);
			glu.gluPerspective(fov,asp,dim/16,16*dim);
		} else { // Orthogonal transformation
			gl2.glOrtho(-asp*dim,asp*dim,-dim,+dim,-dim,+dim);
		}
		// Switch to manipulating the model matrix
		gl2.glMatrixMode(GL2.GL_MODELVIEW);
		// Undo previous transformations
		gl2.glLoadIdentity();
	}

	/**
	 * Reads coordinates from the given line.
	 * @param line The line to parse.
	 * @param numRead The number of coordinates to read.
	 * @return
	 */
	private static float[] readCoord(String line, int numRead) {
		float[] ret = new float[numRead];
		String[] coords = line.split(" "); // split by white space
		if (coords.length != ret.length) {
			throw new AssertionError("Number to read does not match coordinates on line.");
		}
		for (int i = 0; i < numRead; i++) {
			ret[i] = Float.parseFloat(coords[i]);
		}
		return ret;
	}

	public static int loadOBJ(GL2 gl2, File file) {
		//float[][] verts = new float[8192][3];
		List<float[]> verts = new ArrayList<float[]>();
		List<float[]> normals = new ArrayList<float[]>();
		List<float[]> textures = new ArrayList<float[]>();
		try (FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);) {
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.charAt(0) == 'v') {
					if (line.charAt(1) == ' ') { // Vertex coordinates (always 3)
						float[] coords = readCoord(line.substring(2), 3);
						verts.add(coords);
					} else if (line.charAt(1) == 'n') { // Normal coordinates (always 3)
						float[] coords = readCoord(line.substring(2), 3);
						normals.add(coords);
					} else if (line.charAt(1) == 't') { // Texture coordinates (always 2)
						float[] coords = readCoord(line.substring(2), 2);
						textures.add(coords);
					}
				} else if (line.charAt(0) == 'f') { // Read and draw facets
					
				}/* else if (line.equals("usemtl")) { // Use material
					setMaterial(str);
				} else if (line.equals("mtllib")) { // Load materials
					loadMaterial(str);
				}*/
			}
		} catch (FileNotFoundException e) {
			fatal(file.getName() + " could not be found.");
			return 0;
		} catch (IOException e) {
			throw new RuntimeException(e.getLocalizedMessage(), e);
		}
		// TODO STUB
		
		// Free arrays
		verts.clear();
		normals.clear();
		textures.clear();
		return 0;
	}

	private static void printShaderLog(GL2 gl2, int obj, File file) {
		IntBuffer len_b = IntBuffer.allocate(1);
		gl2.glGetShaderiv(obj, GL2.GL_INFO_LOG_LENGTH, len_b);
		int len = len_b.get();
		if (len > 1) {
			IntBuffer n = IntBuffer.allocate(1);
			ByteBuffer buffer = ByteBuffer.allocate(len);
			gl2.glGetShaderInfoLog(obj, len, n, buffer);
			System.err.println(file.getName() + ":\n" + new String(buffer.array()));
		}
		gl2.glGetShaderiv(obj, GL2.GL_COMPILE_STATUS, len_b);
		if (len_b.get() == 0) {
			fatal("Error compiling " + file.getName());
		}
	}

	private static void printProgramLog(GL2 gl2, int obj) {
		IntBuffer len_b = IntBuffer.allocate(1);
		gl2.glGetShaderiv(obj, GL2.GL_INFO_LOG_LENGTH, len_b);
		int len = len_b.get();
		if (len > 1) {
			IntBuffer n = IntBuffer.allocate(1);
			ByteBuffer buffer = ByteBuffer.allocate(len);
			gl2.glGetProgramInfoLog(obj, len, n, buffer);
			System.err.println(new String(buffer.array()));
		}
		gl2.glGetProgramiv(obj, GL2.GL_LINK_STATUS, len_b);
		if (len_b.get() == 0) {
			fatal("Error linking program");
		}
	}

	private static String[] readTextFromFile(File file) {
		try (FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);) {
			List<String> ret = new ArrayList<String>();
			String ln = null;
			while ((ln = br.readLine()) != null) {
				ret.add(ln);
			}
			return ret.toArray(new String[ret.size()]);
		} catch (FileNotFoundException e) {
			fatal(file.getName() + " could not be found.");
			return null;
		} catch (IOException e) {
			throw new RuntimeException(e.getLocalizedMessage(), e);
		}
	}

	private static void createShader(GL2 gl2, int prog, int type, File file) {
		int shader = gl2.glCreateShader(type);
		String[] source = readTextFromFile(file);
		gl2.glShaderSource(shader, 1, source, null);
		gl2.glCompileShader(shader);
		printShaderLog(gl2, shader, file);
		gl2.glAttachShader(prog, shader);
	}

	public static int createShaderProg(GL2 gl2, String vertStr, String fragStr) {
		int prog = gl2.glCreateProgram();
		File vertFile = new File(vertStr);
		if (vertFile.exists()) {
			createShader(gl2, prog, GL2.GL_VERTEX_SHADER, vertFile);
		}
		File fragFile = new File(fragStr);
		if (fragFile.exists()) {
			createShader(gl2, prog, GL2.GL_FRAGMENT_SHADER, fragFile);
		}
		gl2.glLinkProgram(prog);
		printProgramLog(gl2, prog);
		return prog;
	}
}