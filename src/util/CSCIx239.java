package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public final class CSCIx239 {
	private static final Map<String, Material> materials = new HashMap<String, Material>();

	private CSCIx239() {
	}

	public static double Sin(double th) {
		return Math.sin(Math.PI / 180 * th);
	}

	public static double Cos(double th) {
		return Math.cos(Math.PI / 180 * th);
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
			GLU glu = new GLU();
			String errstr = glu.gluErrorString(err);
			System.err.println("ERROR: " + errstr + " [" + s + "]");
		}
	}

	public static void errCheck(GL3 gl2, String s) {
		int err = gl2.glGetError();
		if (err > 0) {
			GLU glu = new GLU();
			String errstr = glu.gluErrorString(err);
			System.err.println("ERROR: " + errstr + " [" + s + "]");
		}
	}

	/*public static int loadTexBMP(GL2 gl2, File file) {
		Buffer image = null;
		IntBuffer texture = IntBuffer.allocate(1);
		gl2.glGenTextures(1, texture);
		gl2.glBindTexture(GL2.GL_TEXTURE_2D, texture.get());
		gl2.glTexImage2D(GL2.GL_TEXTURE_2D, 0, 3, dx, dy, 0, GL2.GL_RGB, GL2.GL_UNSIGNED_BYTE, image);
		// Scale linearly when image size doesn't match
		gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
		return texture.get(0);
	}*/

	public static int loadTexBMP(GL3 gl, File file) {
		// FIXME Not working
		try {
			Texture texture = TextureIO.newTexture(file, false);
			texture.setTexParameteri(gl, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR);
			texture.setTexParameteri(gl, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR);
			int tId = texture.getTextureObject();
			return tId;
		} catch (GLException | IOException e) {
			throw new RuntimeException(e.getLocalizedMessage(), e);
		}
	}

	public static void project(GL2 gl2, double fov, double asp, double dim) {
		// Tell OpenGL we want to manipulate the projection matrix
		gl2.glMatrixMode(GL2.GL_PROJECTION);
		// Undo previous transformations
		gl2.glLoadIdentity();
		// Perspective transformation
		if (fov != 0.0) {
			GLU glu = new GLU();
			glu.gluPerspective(fov, asp, dim / 16.0, 16.0 * dim);
		} else { // Orthogonal transformation
			gl2.glOrtho(-asp * dim, asp * dim, -dim, +dim, -dim, +dim);
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
	private static float[] readFloat(String line, int numRead) {
		float[] ret = new float[numRead];
		String[] coords = line.split(" "); // split by white space
		if (coords.length != ret.length) {
			System.out.println(line);
			System.out.println("Num Read: " + numRead);
			throw new AssertionError("Number to read does not match coordinates on line.");
		}
		for (int i = 0; i < numRead; i++) {
			ret[i] = Float.parseFloat(coords[i]);
		}
		return ret;
	}

	private static void loadMaterial(GL2 gl2, File file) {
		try (FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);) {
			String line = null;
			Material mat = null;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("newmtl")) {
					if (mat != null) {
						materials.put(mat.name, mat);
						mat = null;
					}
					String name = line.substring(7);
					mat = new Material();
					mat.name = name;
				} else if (mat == null) {
					// If no material short circuit here
					continue;
				} else if (line.charAt(0) == 'K') {
					if (line.charAt(1) == 'e') {
						mat.Ke = readFloat(line.substring(2), 3);
					} else if (line.charAt(1) == 'a') {
						mat.Ka = readFloat(line.substring(2), 3);
					} else if (line.charAt(1) == 'd') {
						mat.Kd = readFloat(line.substring(2), 3);
					} else if (line.charAt(1) == 's') {
						mat.Ks = readFloat(line.substring(2), 3);
					}
				} else if (line.charAt(0) == 'N' && line.charAt(1) == 's') {
					mat.Ns = readFloat(line.substring(2), 1);
				} else if (line.startsWith("map_Kd")) {
					//mat.map = loadTexBMP(gl2, new File(line.substring(7)));
				}
				// Ignore line if we get here
			}
		} catch (FileNotFoundException e) {
			System.err.println("Cannot open material file " + file.getName());
		} catch (IOException e) {
			throw new RuntimeException(e.getLocalizedMessage(), e);
		}
	}

	private static void loadMaterial(GL3 gl, File file) {
		try (FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);) {
			String line = null;
			Material mat = null;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("newmtl")) {
					if (mat != null) {
						materials.put(mat.name, mat);
						mat = null;
					}
					String name = line.substring(7);
					mat = new Material();
					mat.name = name;
				} else if (mat == null) {
					// If no material short circuit here
					continue;
				} else if (line.charAt(0) == 'K') {
					if (line.charAt(1) == 'e') {
						mat.Ke = readFloat(line.substring(2), 3);
					} else if (line.charAt(1) == 'a') {
						mat.Ka = readFloat(line.substring(2), 3);
					} else if (line.charAt(1) == 'd') {
						mat.Kd = readFloat(line.substring(2), 3);
					} else if (line.charAt(1) == 's') {
						mat.Ks = readFloat(line.substring(2), 3);
					}
				} else if (line.charAt(0) == 'N' && line.charAt(1) == 's') {
					mat.Ns = readFloat(line.substring(2), 1);
				} else if (line.startsWith("map_Kd")) {
					mat.map = loadTexBMP(gl, new File(line.substring(7)));
				}
				// Ignore line if we get here
			}
		} catch (FileNotFoundException e) {
			System.err.println("Cannot open material file " + file.getName());
		} catch (IOException e) {
			throw new RuntimeException(e.getLocalizedMessage(), e);
		}
	}

	private static void setMaterial(GL2 gl2, String name) {
		// Search materials for a matching name
		Material mat = materials.get(name);
		if (mat != null) {
			// Set material colors
			gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, FloatBuffer.wrap(mat.Ke));
			gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, FloatBuffer.wrap(mat.Ka));
			gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, FloatBuffer.wrap(mat.Kd));
			gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, FloatBuffer.wrap(mat.Ks));
			gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, FloatBuffer.wrap(mat.Ns));
			// Bind texture if specified
			if (mat.map > 0) {
				gl2.glEnable(GL2.GL_TEXTURE_2D);
				gl2.glBindTexture(GL2.GL_TEXTURE_2D, mat.map);
			} else {
				gl2.glDisable(GL2.GL_TEXTURE_2D);
			}
		} else {
			//  No matches
			System.err.println("Unknown material " + name);
		}
	}

	public static int loadOBJ(GL2 gl2, File file) {
		List<float[]> verts = new ArrayList<float[]>();
		List<float[]> normals = new ArrayList<float[]>();
		List<float[]> textures = new ArrayList<float[]>();
		try (FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);) {
			// Start new displaylist
			int list = gl2.glGenLists(1);
			gl2.glNewList(list, GL2.GL_COMPILE);
			// Push attributes for textures
			gl2.glPushAttrib(GL2.GL_TEXTURE_BIT);
			CSCIx239.errCheck(gl2, "loadOBJ");
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.length() == 0) {
					continue;
				}
				if (line.charAt(0) == 'v') {
					if (line.charAt(1) == ' ') { // Vertex coordinates (always 3)
						float[] coords = readFloat(line.substring(3), 3);
						verts.add(coords);
					} else if (line.charAt(1) == 'n') { // Normal coordinates (always 3)
						float[] coords = readFloat(line.substring(3), 3);
						normals.add(coords);
					} else if (line.charAt(1) == 't') { // Texture coordinates (always 2)
						float[] coords = readFloat(line.substring(3), 2);
						textures.add(coords);
					}
				} else if (line.charAt(0) == 'f') { // Read and draw facets
					gl2.glBegin(GL2.GL_POLYGON);
					String[] tuple = line.substring(2).split(" ");
					for (int i = 0; i < tuple.length; i++) {
						int Kv = 0, Kt = 0, Kn = 0;
						String[] pts = tuple[i].split("/{1,2}"); // Kv, Kt, Kn
						if (pts.length == 3) { // Vertex/Texture/Normal triplet
							Kv = Integer.parseInt(pts[0]);
							Kt = Integer.parseInt(pts[1]);
							Kn = Integer.parseInt(pts[2]);
						} else if (pts.length == 2) { // Vertex/Normal pairs
							Kv = Integer.parseInt(pts[0]);
							Kn = Integer.parseInt(pts[1]);
						} else if (pts.length == 1) { // Vertex index
							Kv = Integer.parseInt(pts[0]);
						} else { // This is an error
							fatal("Invalid facet" + tuple[i]);
						}
						//  Check that vertex is in range
						int numVertices = verts.size();
						int numNormals = normals.size();
						int numTextures = textures.size();
						if (Kv < -numVertices || Kv > numVertices) {
							fatal("Vertex " + Kv + " out of range 1-" + verts.size());
						}
						if (Kn < -numNormals || Kn > numNormals) {
							fatal("Normal " + Kn + " out of range 1-" + normals.size());
						}
						if (Kt < -numTextures || Kt > numTextures) {
							fatal("Texture " + Kt + " out of range 1-" + textures.size());
						}
						//  Draw vertex
						if (Kt > 0) {
							gl2.glTexCoord2fv(FloatBuffer.wrap(textures.get(Kt - 1)));
						}
						if (Kn > 0) {
							gl2.glNormal3fv(FloatBuffer.wrap(normals.get(Kn - 1)));
						}
						if (Kv > 0) {
							gl2.glVertex3fv(FloatBuffer.wrap(verts.get(Kv - 1)));
						}
					}
					gl2.glEnd();
				} else if (line.startsWith("usemtl")) { // Use material
					setMaterial(gl2, line.substring(7));
				} else if (line.equals("mtllib")) { // Load materials
					loadMaterial(gl2, new File(line.substring(7)));
				}
			}
			return list;
		} catch (FileNotFoundException e) {
			fatal(file.getName() + " could not be found.");
			return 0;
		} catch (IOException e) {
			throw new RuntimeException(e.getLocalizedMessage(), e);
		} finally {
			// Pop attributes (textures)
			gl2.glPopAttrib();
			gl2.glEndList();
			// Free arrays
			verts.clear();
			normals.clear();
			textures.clear();
		}
	}

	public static int loadOBJ(GL3 gl, File file) {
		GL2 gl2 = gl.getGL2();
		List<float[]> verts = new ArrayList<float[]>();
		List<float[]> normals = new ArrayList<float[]>();
		List<float[]> textures = new ArrayList<float[]>();
		try (FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);) {
			// Start new displaylist
			int list = gl2.glGenLists(1);
			gl2.glNewList(list, GL2.GL_COMPILE);
			// Push attributes for textures
			gl2.glPushAttrib(GL2.GL_TEXTURE_BIT);
			CSCIx239.errCheck(gl, "loadOBJ");
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.length() == 0) {
					continue;
				}
				if (line.charAt(0) == 'v') {
					if (line.charAt(1) == ' ') { // Vertex coordinates (always 3)
						float[] coords = readFloat(line.substring(3), 3);
						verts.add(coords);
					} else if (line.charAt(1) == 'n') { // Normal coordinates (always 3)
						float[] coords = readFloat(line.substring(3), 3);
						normals.add(coords);
					} else if (line.charAt(1) == 't') { // Texture coordinates (always 2)
						float[] coords = readFloat(line.substring(3), 2);
						textures.add(coords);
					}
				} else if (line.charAt(0) == 'f') { // Read and draw facets
					gl2.glBegin(GL2.GL_POLYGON);
					String[] tuple = line.substring(2).split(" ");
					for (int i = 0; i < tuple.length; i++) {
						int Kv = 0, Kt = 0, Kn = 0;
						String[] pts = tuple[i].split("/{1,2}"); // Kv, Kt, Kn
						if (pts.length == 3) { // Vertex/Texture/Normal triplet
							Kv = Integer.parseInt(pts[0]);
							Kt = Integer.parseInt(pts[1]);
							Kn = Integer.parseInt(pts[2]);
						} else if (pts.length == 2) { // Vertex/Normal pairs
							Kv = Integer.parseInt(pts[0]);
							Kn = Integer.parseInt(pts[1]);
						} else if (pts.length == 1) { // Vertex index
							Kv = Integer.parseInt(pts[0]);
						} else { // This is an error
							fatal("Invalid facet" + tuple[i]);
						}
						//  Check that vertex is in range
						int numVertices = verts.size();
						int numNormals = normals.size();
						int numTextures = textures.size();
						if (Kv < -numVertices || Kv > numVertices) {
							fatal("Vertex " + Kv + " out of range 1-" + verts.size());
						}
						if (Kn < -numNormals || Kn > numNormals) {
							fatal("Normal " + Kn + " out of range 1-" + normals.size());
						}
						if (Kt < -numTextures || Kt > numTextures) {
							fatal("Texture " + Kt + " out of range 1-" + textures.size());
						}
						//  Draw vertex
						if (Kt > 0) {
							gl2.glTexCoord2fv(FloatBuffer.wrap(textures.get(Kt - 1)));
						}
						if (Kn > 0) {
							gl2.glNormal3fv(FloatBuffer.wrap(normals.get(Kn - 1)));
						}
						if (Kv > 0) {
							gl2.glVertex3fv(FloatBuffer.wrap(verts.get(Kv - 1)));
						}
					}
					gl2.glEnd();
				} else if (line.startsWith("usemtl")) { // Use material
					setMaterial(gl2, line.substring(7));
				} else if (line.equals("mtllib")) { // Load materials
					loadMaterial(gl, new File(line.substring(7)));
				}
			}
			return list;
		} catch (FileNotFoundException e) {
			fatal(file.getName() + " could not be found.");
			return 0;
		} catch (IOException e) {
			throw new RuntimeException(e.getLocalizedMessage(), e);
		} finally {
			// Pop attributes (textures)
			gl2.glPopAttrib();
			gl2.glEndList();
			// Free arrays
			verts.clear();
			normals.clear();
			textures.clear();
		}
	}

	private static void printShaderLog(GL2 gl, int obj, File file) {
		IntBuffer len_b = IntBuffer.allocate(1);
		gl.glGetShaderiv(obj, GL2.GL_INFO_LOG_LENGTH, len_b);
		int len = len_b.get();
		if (len > 1) {
			IntBuffer n = IntBuffer.allocate(1);
			ByteBuffer buffer = ByteBuffer.allocate(len);
			gl.glGetShaderInfoLog(obj, len, n, buffer);
			System.err.println(file.getName() + ":\n" + new String(buffer.array()));
		}
		len_b.clear();
		gl.glGetShaderiv(obj, GL2.GL_COMPILE_STATUS, len_b);
		if (len_b.get() == 0) {
			fatal("Error compiling " + file.getName());
		}
	}

	private static void printShaderLog(GL3 gl, int obj, File file) {
		IntBuffer len_b = IntBuffer.allocate(1);
		gl.glGetShaderiv(obj, GL3.GL_INFO_LOG_LENGTH, len_b);
		int len = len_b.get();
		if (len > 1) {
			IntBuffer n = IntBuffer.allocate(1);
			ByteBuffer buffer = ByteBuffer.allocate(len);
			gl.glGetShaderInfoLog(obj, len, n, buffer);
			System.err.println(file.getName() + ":\n" + new String(buffer.array()));
		}
		len_b.clear();
		gl.glGetShaderiv(obj, GL3.GL_COMPILE_STATUS, len_b);
		if (len_b.get() == 0) {
			fatal("Error compiling " + file.getName());
		}
	}

	private static void printProgramLog(GL2 gl2, int obj) {
		IntBuffer len_b = IntBuffer.allocate(1);
		gl2.glGetProgramiv(obj, GL2.GL_INFO_LOG_LENGTH, len_b);
		int len = len_b.get();
		if (len > 1) {
			IntBuffer n = IntBuffer.allocate(1);
			ByteBuffer buffer = ByteBuffer.allocate(len);
			gl2.glGetProgramInfoLog(obj, len, n, buffer);
			System.err.println(new String(buffer.array()));
		}
		len_b.clear();
		gl2.glGetProgramiv(obj, GL2.GL_LINK_STATUS, len_b);
		if (len_b.get() == 0) {
			fatal("Error linking program");
		}
	}

	private static void printProgramLog(GL3 gl, int obj) {
		IntBuffer len_b = IntBuffer.allocate(1);
		gl.glGetProgramiv(obj, GL3.GL_INFO_LOG_LENGTH, len_b);
		int len = len_b.get();
		if (len > 1) {
			IntBuffer n = IntBuffer.allocate(1);
			ByteBuffer buffer = ByteBuffer.allocate(len);
			gl.glGetProgramInfoLog(obj, len, n, buffer);
			System.err.println(new String(buffer.array()));
		}
		len_b.clear();
		gl.glGetProgramiv(obj, GL3.GL_LINK_STATUS, len_b);
		if (len_b.get() == 0) {
			fatal("Error linking program");
		}
	}

	private static String[] readTextFromFile(File file) {
		try (RandomAccessFile raf = new RandomAccessFile(file, "r");) {
			StringBuilder sb = new StringBuilder();
			long length = raf.length();
			byte[] buffer = new byte[(int) Math.min(length, 2048)];
			int bytesRead;
			do {
				bytesRead = raf.read(buffer);
				if (bytesRead == -1) {
					break;
				}
				sb.append(new String(Arrays.copyOfRange(buffer, 0, bytesRead)));
			} while (bytesRead != -1);
			String[] arr = {sb.toString()};
			return arr;
		} catch (FileNotFoundException e) {
			fatal(file.getName() + " could not be found.");
			return null;
		} catch (IOException e) {
			throw new RuntimeException(e.getLocalizedMessage(), e);
		}
	}

	private static void createShader(GL2 gl, int prog, int type, File file) {
		//  Create the shader
		int shader = gl.glCreateShader(type);
		// Load source code from file
		String[] source = readTextFromFile(file);
		gl.glShaderSource(shader, 1, source, null);
		// Compile the shader
		gl.glCompileShader(shader);
		// Check for errors
		printShaderLog(gl, shader, file);
		// Attach to shader program
		gl.glAttachShader(prog, shader);
	}

	private static void createShader(GL3 gl, int prog, int type, File file) {
		//  Create the shader
		int shader = gl.glCreateShader(type);
		// Load source code from file
		String[] source = readTextFromFile(file);
		gl.glShaderSource(shader, 1, source, null);
		// Compile the shader
		gl.glCompileShader(shader);
		// Check for errors
		printShaderLog(gl, shader, file);
		// Attach to shader program
		gl.glAttachShader(prog, shader);
	}

	public static int createShaderProg(GL2 gl, String vertStr, String fragStr) {
		int prog = gl.glCreateProgram();
		File vertFile = new File(vertStr);
		if (vertFile.exists()) {
			createShader(gl, prog, GL2.GL_VERTEX_SHADER, vertFile);
		}
		File fragFile = new File(fragStr);
		if (fragFile.exists()) {
			createShader(gl, prog, GL2.GL_FRAGMENT_SHADER, fragFile);
		}
		gl.glLinkProgram(prog);
		printProgramLog(gl, prog);
		return prog;
	}

	public static int createShaderProg(GL3 gl, String vertStr, String fragStr) {
		int prog = gl.glCreateProgram();
		File vertFile = new File(vertStr);
		if (vertFile.exists()) {
			createShader(gl, prog, GL3.GL_VERTEX_SHADER, vertFile);
		}
		File fragFile = new File(fragStr);
		if (fragFile.exists()) {
			createShader(gl, prog, GL3.GL_FRAGMENT_SHADER, fragFile);
		}
		gl.glLinkProgram(prog);
		printProgramLog(gl, prog);
		return prog;
	}
}