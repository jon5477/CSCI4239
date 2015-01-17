package ex;

import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import javax.media.opengl.GL2;
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

public final class Example1 {
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
	private static int model = 0; // Model display list
	private static int shader[] = {0,0}; // Shader program
	private static String text[] = {"No Shader", "Basic Shader"};

	private static final DecimalFormat df = new DecimalFormat("##.0");
	private static final GLUT glt = new GLUT();
	private static GLContext glc;
	private static GL2 gl;

	static {
		df.setRoundingMode(RoundingMode.HALF_UP);
	}

	private static void cube(GL2 gl2) {
		//  Front
		gl2.glColor3f(1,0,0);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glNormal3f( 0, 0,+1);
		gl2.glTexCoord2f(0,0);
		gl2.glVertex3f(-1,-1,+1);
		gl2.glTexCoord2f(1,0);
		gl2.glVertex3f(+1,-1,+1);
		gl2.glTexCoord2f(1,1);
		gl2.glVertex3f(+1,+1,+1);
		gl2.glTexCoord2f(0,1);
		gl2.glVertex3f(-1,+1,+1);
		gl2.glEnd();
		//  Back
		gl2.glColor3f(0,0,1);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glNormal3f( 0, 0,-1);
		gl2.glTexCoord2f(0,0);
		gl2.glVertex3f(+1,-1,-1);
		gl2.glTexCoord2f(1,0);
		gl2.glVertex3f(-1,-1,-1);
		gl2.glTexCoord2f(1,1);
		gl2.glVertex3f(-1,+1,-1);
		gl2.glTexCoord2f(0,1);
		gl2.glVertex3f(+1,+1,-1);
		gl2.glEnd();
		//  Right
		gl2.glColor3f(1,1,0);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glNormal3f(+1, 0, 0);
		gl2.glTexCoord2f(0,0);
		gl2.glVertex3f(+1,-1,+1);
		gl2.glTexCoord2f(1,0);
		gl2.glVertex3f(+1,-1,-1);
		gl2.glTexCoord2f(1,1);
		gl2.glVertex3f(+1,+1,-1);
		gl2.glTexCoord2f(0,1);
		gl2.glVertex3f(+1,+1,+1);
		gl2.glEnd();
		//  Left
		gl2.glColor3f(0,1,0);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glNormal3f(-1, 0, 0);
		gl2.glTexCoord2f(0,0);
		gl2.glVertex3f(-1,-1,-1);
		gl2.glTexCoord2f(1,0);
		gl2.glVertex3f(-1,-1,+1);
		gl2.glTexCoord2f(1,1);
		gl2.glVertex3f(-1,+1,+1);
		gl2.glTexCoord2f(0,1);
		gl2.glVertex3f(-1,+1,-1);
		gl2.glEnd();
		//  Top
		gl2.glColor3f(0,1,1);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glNormal3f( 0,+1, 0);
		gl2.glTexCoord2f(0,0);
		gl2.glVertex3f(-1,+1,+1);
		gl2.glTexCoord2f(1,0);
		gl2.glVertex3f(+1,+1,+1);
		gl2.glTexCoord2f(1,1);
		gl2.glVertex3f(+1,+1,-1);
		gl2.glTexCoord2f(0,1);
		gl2.glVertex3f(-1,+1,-1);
		gl2.glEnd();
		//  Bottom
		gl2.glColor3f(1,0,1);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glNormal3f( 0,-1, 0);
		gl2.glTexCoord2f(0,0);
		gl2.glVertex3f(-1,-1,-1);
		gl2.glTexCoord2f(1,0);
		gl2.glVertex3f(+1,-1,-1);
		gl2.glTexCoord2f(1,1);
		gl2.glVertex3f(+1,-1,+1);
		gl2.glTexCoord2f(0,1);
		gl2.glVertex3f(-1,-1,+1);
		gl2.glEnd();
	}

	private static void reshape(GL2 gl2, int width, int height) {
		//  Ratio of the width to the height of the window
		asp = (height > 0) ? (double) width / height : 1;
		//  Set the viewport to the entire window
		gl2.glViewport(0, 0, width, height);
		CSCIx239.project(gl2, perspProj ? fov : 0.0, asp, dim);
	}

	/**
	 * Rendering takes place here.
	 * @param gl2
	 * @param width
	 * @param height
	 */
	private static void display(GL2 gl2, GLAnimatorControl anim, int width, int height) {
		double len = 2.0; // Length of axes
		// Erase the window and the depth buffer
		gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		// Enable Z-buffering in OpenGL
		gl2.glEnable(GL2.GL_DEPTH_TEST);
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
		// Select shader (0 => no shader)
		gl2.glUseProgram(shader[mode]);
		// Export time to uniform variable
		if (mode == 1) {
			float time = (float) (0.001 * (System.currentTimeMillis() - start));
			int id = gl2.glGetUniformLocation(shader[mode], "time");
			if (id >= 0) {
				gl2.glUniform1f(id,time);
			}
		}
		// Draw the model, teapot or cube
		gl2.glColor3f(1,1,0);
		if (obj == 2) {
			gl2.glCallList(model);
		} else if (obj == 1) {
			glt.glutSolidTeapot(1.0);
		} else {
			cube(gl2);
		}
		// No shader for what follows
		gl2.glUseProgram(0);
		// Draw axes - no lighting from here on
		gl2.glColor3f(1,1,1);
		if (axes) {
			gl2.glBegin(GL2.GL_LINES);
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
		gl2.glWindowPos2i(5, 560);
		CSCIx239.print(glt, df.format(anim.getLastFPS()) + " FPS");
		// Display parameters
		gl2.glWindowPos2i(5,5);
		CSCIx239.print(glt, "Angle=" + th + "," + ph + "  Dim=" + df.format(dim) + " Projection=" + (perspProj ? "Perpective" : "Orthogonal") + " " + text[mode]);
		// Render the scene and make it visible
		CSCIx239.errCheck(gl2, "display");
		gl2.glFlush();
	}

	public static void main(String[] args) {
		GLProfile glprofile = GLProfile.getDefault();
		GLCapabilities glcap = new GLCapabilities(glprofile);
		glcap.setDoubleBuffered(true);
		final GLCanvas glcanvas = new GLCanvas(glcap);
		glcanvas.addGLEventListener(new GLEventListener() {
			@Override
			public void reshape(GLAutoDrawable glautodrawable, int x, int y, int width, int height) {
				Example1.reshape(glautodrawable.getGL().getGL2(), width, height);
			}
			
			@Override
			public void init(GLAutoDrawable glautodrawable) {
				start = System.currentTimeMillis();
				gl = glautodrawable.getGL().getGL2();
				glc = GLContext.getCurrent();
				// Load object
				model = CSCIx239.loadOBJ(gl, new File("tyra.obj"));
				// Create Shader Programs
				shader[1] = CSCIx239.createShaderProg(gl, "basic.vert", "basic.frag");
			}
			
			@Override
			public void dispose(GLAutoDrawable glautodrawable) {
				gl = null;
			}
			
			@Override
			public void display(GLAutoDrawable glautodrawable) {
				// Called when rendering is necessary
				Example1.display(glautodrawable.getGL().getGL2(), glautodrawable.getAnimator(), glautodrawable.getSurfaceWidth(), glautodrawable.getSurfaceHeight());
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
				CSCIx239.project(gl, perspProj ? fov : 0, asp, dim);
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
		animator.setUpdateFPSFrames(3, null);
		final Frame frame = new Frame("Basic Shader");
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