package hw;

import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.math.RoundingMode;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.text.DecimalFormat;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAnimatorControl;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;

import util.CSCIx239;

import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.gl2.GLUT;

public final class Assignment4 {
	private static long start;
	private static boolean axes = true; // Display axes
	private static int mode = 0; // Shader mode
	private static boolean perspProj = false; // Projection type
	private static int obj = 0; // Object
	private static int th = 0; // Azimuth of view angle
	private static int ph = 0; // Elevation of view angle
	private static int fov = 55; // Field of view (for perspective)
	private static double asp = 1; // Aspect ratio
	private static double dim = 3.0; // Size of world
	private static int zh = 90; //  Light azimuth
	private static float y_light = 2; //  Light elevation
	private static int model = 0; // Model display list
	private static int shader; // Shader program
	private static String text[] = {"No Shader", "Texture Shader"};

	private static final DecimalFormat df = new DecimalFormat("##.0");
	private static final GLUT glt = new GLUT();
	private static GLContext glc;
	private static GL3 gl;

	private static final IntBuffer cube_buffer = IntBuffer.allocate(1);
	private static final float cube_data[] =  // Vertex data
		{
		//  X  Y  Z  W   Nx Ny Nz    R G B   s t
		//  Front
		+1,+1,+1,+1,   0, 0,+1,   1,0,0,  1,1,
		-1,+1,+1,+1,   0, 0,+1,   1,0,0,  0,1,
		+1,-1,+1,+1,   0, 0,+1,   1,0,0,  1,0,
		-1,+1,+1,+1,   0, 0,+1,   1,0,0,  0,1,
		+1,-1,+1,+1,   0, 0,+1,   1,0,0,  1,0,
		-1,-1,+1,+1,   0, 0,+1,   1,0,0,  0,0,
		//  Back
		-1,-1,-1,+1,   0, 0,-1,   0,0,1,  1,0,
		+1,-1,-1,+1,   0, 0,-1,   0,0,1,  0,0,
		-1,+1,-1,+1,   0, 0,-1,   0,0,1,  1,1,
		+1,-1,-1,+1,   0, 0,-1,   0,0,1,  0,0,
		-1,+1,-1,+1,   0, 0,-1,   0,0,1,  1,1,
		+1,+1,-1,+1,   0, 0,-1,   0,0,1,  0,1,
		//  Right
		+1,+1,+1,+1,  +1, 0, 0,   1,1,0,  0,1,
		+1,-1,+1,+1,  +1, 0, 0,   1,1,0,  0,0,
		+1,+1,-1,+1,  +1, 0, 0,   1,1,0,  1,1,
		+1,-1,+1,+1,  +1, 0, 0,   1,1,0,  0,0,
		+1,+1,-1,+1,  +1, 0, 0,   1,1,0,  1,1,
		+1,-1,-1,+1,  +1, 0, 0,   1,1,0,  1,0,
		//  Left
		-1,+1,+1,+1,  -1, 0, 0,   0,1,0,  1,1,
		-1,+1,-1,+1,  -1, 0, 0,   0,1,0,  0,1,
		-1,-1,+1,+1,  -1, 0, 0,   0,1,0,  1,0,
		-1,+1,-1,+1,  -1, 0, 0,   0,1,0,  0,1,
		-1,-1,+1,+1,  -1, 0, 0,   0,1,0,  1,0,
		-1,-1,-1,+1,  -1, 0, 0,   0,1,0,  0,0,
		//  Top
		+1,+1,+1,+1,   0,+1, 0,   0,1,1,  1,0,
		+1,+1,-1,+1,   0,+1, 0,   0,1,1,  1,1,
		-1,+1,+1,+1,   0,+1, 0,   0,1,1,  0,0,
		+1,+1,-1,+1,   0,+1, 0,   0,1,1,  1,1,
		-1,+1,+1,+1,   0,+1, 0,   0,1,1,  0,0,
		-1,+1,-1,+1,   0,+1, 0,   0,1,1,  0,1,
		//  Bottom
		-1,-1,-1,+1,   0,-1, 0,   1,0,1,  0,0,
		+1,-1,-1,+1,   0,-1, 0,   1,0,1,  1,0,
		-1,-1,+1,+1,   0,-1, 0,   1,0,1,  0,1,
		+1,-1,-1,+1,   0,-1, 0,   1,0,1,  1,0,
		-1,-1,+1,+1,   0,-1, 0,   1,0,1,  0,1,
		+1,-1,+1,+1,   0,-1, 0,   1,0,1,  1,1,
	};

	static {
		df.setRoundingMode(RoundingMode.HALF_UP);
	}

	private static void initCube(GL3 gl) {
		// Copy data to vertex buffer object
		gl.glGenBuffers(1, cube_buffer);
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, cube_buffer.get());
		gl.glBufferData(GL3.GL_ARRAY_BUFFER, cube_data.length, FloatBuffer.wrap(cube_data), GL3.GL_STATIC_DRAW);
		//  Unbind this buffer
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
	}

	private static void reshape(GL3 gl, int width, int height) {
		//  Ratio of the width to the height of the window
		asp = (height > 0) ? (double) width / height : 1;
		//  Set the viewport to the entire window
		gl.glViewport(0, 0, width, height);
		CSCIx239.project(gl.getGL2(), perspProj ? fov : 0.0, asp, dim);
	}

	/**
	 * Rendering takes place here.
	 * @param gl
	 * @param width
	 * @param height
	 */
	private static void display(GL3 gl, GLAnimatorControl anim, int width, int height) {
		GL2 gl2 = gl.getGL2();
		double len = 2.0; // Length of axes
		// Light position and colors
		float[] position = {(float) (2 * CSCIx239.Cos(zh)), y_light, (float) (2 * CSCIx239.Sin(zh)), (float) 1.0};
		// Erase the window and the depth buffer
		gl2.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);
		// Enable Z-buffering in OpenGL
		gl2.glEnable(GL3.GL_DEPTH_TEST);
		// Undo previous transformations
		gl2.glLoadIdentity();
		// Perspective - set eye position
		if (perspProj) {
			double Ex = -2 * dim * CSCIx239.Sin(th) * CSCIx239.Cos(ph);
			double Ey = +2 * dim * CSCIx239.Sin(ph);
			double Ez = +2 * dim * CSCIx239.Cos(th) * CSCIx239.Cos(ph);
			GLU glu = new GLU();
			glu.gluLookAt(Ex, Ey, Ez, 0, 0, 0, 0, CSCIx239.Cos(ph), 0);
		} else { // Orthogonal - set world orientation
			gl2.glRotatef(ph, 1, 0, 0);
			gl2.glRotatef(th, 0, 1, 0);
		}
		// Draw light position as sphere (still no lighting here)
		gl2.glColor3f(1,1,1);
		gl2.glPushMatrix();
		gl2.glTranslated(position[0], position[1], position[2]);
		glt.glutSolidSphere(0.03, 10, 10);
		gl2.glPopMatrix();
		// END OF FIXED PIPELINE
		float[] ModelViewMatrix = new float[16];
		float[] ProjectionMatrix = new float[16];
		gl2.glGetFloatv(GL2.GL_PROJECTION_MATRIX, FloatBuffer.wrap(ProjectionMatrix));
		gl2.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, FloatBuffer.wrap(ModelViewMatrix));
		// Use our shader
		gl2.glUseProgram(shader);
		// Set Modelview and Projection Matrix
		int loc = gl.glGetUniformLocation(shader, "ModelViewMatrix");
		if (loc >= 0) {
			gl.glUniformMatrix4fv(loc, 1, false, FloatBuffer.wrap(ModelViewMatrix));
		}
		loc = gl.glGetUniformLocation(shader, "ProjectionMatrix");
		if (loc >= 0) {
			gl.glUniformMatrix4fv(loc, 1, false, FloatBuffer.wrap(ProjectionMatrix));
		}
		// Select cube buffer
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, cube_buffer.get(0));
		// Attribute 0: vertex coordinate (vec4) at offset 0
		gl.glEnableVertexAttribArray(0);
		gl.glVertexAttribPointer(0, 4, GL3.GL_FLOAT, false, 12 * 4, 0);
		// Attribute 1:  vertex color (vec3) offset 7 floats
		gl.glEnableVertexAttribArray(1);
		gl.glVertexAttribPointer(1, 3, GL3.GL_FLOAT, false, 12 * 4, 7 * 4);
		// Draw the cube
		gl.glDrawArrays(GL3.GL_TRIANGLES, 0, 36); // cube size = 36
		// Disable vertex arrays
		gl.glDisableVertexAttribArray(0);
		gl.glDisableVertexAttribArray(1);
		// Unbind this buffer
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
		// Back to fixed pipeline
		gl.glUseProgram(0);
		// Draw axes - no lighting from here on
		gl.glDisable(GL2.GL_LIGHTING);
		gl2.glColor3f(1,1,1);
		if (axes) {
			gl2.glBegin(GL3.GL_LINES);
			gl2.glVertex3d(0.0,0.0,0.0);
			gl2.glVertex3d(len,0.0,0.0);
			gl2.glVertex3d(0.0,0.0,0.0);
			gl2.glVertex3d(0.0,len,0.0);
			gl2.glVertex3d(0.0,0.0,0.0);
			gl2.glVertex3d(0.0,0.0,len);
			gl2.glEnd();
			// Label axes
			gl2.glRasterPos3d(len,0.0,0.0);
			CSCIx239.print(glt, "X");
			gl2.glRasterPos3d(0.0,len,0.0);
			CSCIx239.print(glt, "Y");
			gl2.glRasterPos3d(0.0,0.0,len);
			CSCIx239.print(glt, "Z");
		}
		// Display FPS
		gl2.glWindowPos2i(5, height - 15);
		CSCIx239.print(glt, df.format(anim.getLastFPS()) + " FPS");
		// Display parameters
		gl2.glWindowPos2i(5,5);
		CSCIx239.print(glt, "Angle=" + th + "," + ph + "  Dim=" + df.format(dim) + " Projection=" + (perspProj ? "Perpective" : "Orthogonal") + " " + text[mode]);
		// Render the scene and make it visible
		CSCIx239.errCheck(gl, "display");
		gl.glFlush();
	}

	public static void main(String[] args) {
		GLProfile glprofile = GLProfile.getDefault();
		GLCapabilities glcap = new GLCapabilities(glprofile);
		glcap.setDoubleBuffered(true);
		final GLCanvas glcanvas = new GLCanvas(glcap);
		glcanvas.addGLEventListener(new GLEventListener() {
			@Override
			public void reshape(GLAutoDrawable glautodrawable, int x, int y, int width, int height) {
				Assignment4.reshape(glautodrawable.getGL().getGL3(), width, height);
			}
			
			@Override
			public void init(GLAutoDrawable glautodrawable) {
				start = System.currentTimeMillis();
				gl = glautodrawable.getGL().getGL3();
				glc = GLContext.getCurrent();
				// Debug Info
				System.out.println("GL_VERSION: " + gl.glGetString(GL2.GL_VERSION));
				// Load object
				model = CSCIx239.loadOBJ(gl.getGL3(), new File("tyra.obj"));
				// Create Shader Programs
				shader = CSCIx239.createShaderProg(gl.getGL3(), "model.vert", "proctex.frag");
				// Initialize cube
				initCube(gl);
				CSCIx239.errCheck(gl, "init");
				//gl.setSwapInterval(0); // disable vsync
			}
			
			@Override
			public void dispose(GLAutoDrawable glautodrawable) {
				gl = null;
			}
			
			@Override
			public void display(GLAutoDrawable glautodrawable) {
				// Called when rendering is necessary
				Assignment4.display(glautodrawable.getGL().getGL3(), glautodrawable.getAnimator(), glautodrawable.getSurfaceWidth(), glautodrawable.getSurfaceHeight());
			}
		});
		KeyListener kl = new KeyListener() {
			@Override
			public void keyPressed(KeyEvent key) {
				switch (key.getKeyChar()) {
					//  Reset view angle
					case '0':
						th = ph = 0;
						break;
					//  Toggle axes
					case 'a':
					case 'A':
						axes = !axes;
						break;
					//  Toggle projection type
					case 'p':
					case 'P':
						perspProj = !perspProj;
						break;
					//  Toggle objects
					case 'o':
					case 'O':
						obj = (obj + 1) % 3;
						break;
					//  Cycle modes
					case 'm':
					case 'M':
						mode = 1-mode;
						break;
					//  Light elevation
					case '+':
						y_light += 0.1;
						break;
					case '-':
						y_light -= 0.1;
						break;
					//  Light position
					case '[':
						zh--;
						break;
					case ']':
						zh++;
						break;
					default: {
						if (key.isActionKey()) {
							if (key.getKeyCode() == KeyEvent.VK_RIGHT) {
								th += 5;
							} else if (key.getKeyCode() == KeyEvent.VK_LEFT) {
								th -= 5;
							} else if (key.getKeyCode() == KeyEvent.VK_UP) {
								ph += 5;
							} else if (key.getKeyCode() == KeyEvent.VK_DOWN) {
								ph -= 5;
							} else if (key.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
								dim += 0.1;
							} else if (key.getKeyCode() == KeyEvent.VK_PAGE_UP && dim > 1) {
								dim -= 0.1;
							}
							th %= 360;
							ph %= 360;
						}
						break;
					}
				}
				//  Reproject
				glc.makeCurrent();
				CSCIx239.project(gl.getGL2(), perspProj ? fov : 0, asp, dim);
				CSCIx239.errCheck(gl, "proj");
				glc.release();
				//  Tell GLUT it is necessary to redisplay the scene
				glcanvas.repaint();
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				// no-op
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				// no-op
			}
		};
		glcanvas.addKeyListener(kl);
		final Animator animator = new Animator(glcanvas);
		animator.setRunAsFastAsPossible(true);
		animator.setUpdateFPSFrames(10, null);
		final Frame frame = new Frame("OpenGL 3&4 - Jonathan Huang");
		frame.add(glcanvas);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent windowevent) {
				frame.remove(glcanvas);
				frame.dispose();
				System.exit(0);
			}
		});
		frame.setSize(600, 600);
		frame.setVisible(true);
		animator.start();
	}
}