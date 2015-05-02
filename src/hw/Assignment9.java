package hw;

import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.math.RoundingMode;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;
import java.util.concurrent.ThreadLocalRandom;

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

public final class Assignment9 {
	private static final ThreadLocalRandom r = ThreadLocalRandom.current();
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
	private static int shader[] = new int[2]; // Shader program
	private static String text[] = {"No Shader", "Noise Shader"};

	private static final DecimalFormat df = new DecimalFormat("##.0");
	private static final GLUT glt = new GLUT();
	private static GLContext glc;
	private static GL2 gl;

	private static int n; //  Particle count
	private static final int N = 100;
	private static float[] Vert = new float[3*N*N];
	private static float[] Color = new float[3*N*N];
	private static float[] Vel = new float[3*N*N];
	private static float[] Start = new float[N*N];

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
		// Light position and colors
		float[] emission = {(float) 0.0, (float) 0.0, (float) 0.0, (float) 1.0};
		float[] ambient = {(float) 0.3, (float) 0.3, (float) 0.3, (float) 1.0};
		float[] diffuse = {(float) 1.0, (float) 1.0, (float) 1.0, (float) 1.0};
		float[] specular = {(float) 1.0, (float) 1.0, (float) 1.0, (float) 1.0};
		float[] position = {(float) (2 * CSCIx239.Cos(zh)), y_light, (float) (2 * CSCIx239.Sin(zh)), (float) 1.0};
		float[] shinyness = {16};
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
		// Draw light position as sphere (still no lighting here)
		gl2.glColor3f(1,1,1);
		gl2.glPushMatrix();
		gl2.glTranslated(position[0],position[1],position[2]);
		glt.glutSolidSphere(0.03,10,10);
		gl2.glPopMatrix();
		// OpenGL should normalize normal vectors
		gl2.glEnable(GL2.GL_NORMALIZE);
		//  Enable lighting
		gl2.glEnable(GL2.GL_LIGHTING);
		//  glColor sets ambient and diffuse color materials
		gl2.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);
		gl2.glEnable(GL2.GL_COLOR_MATERIAL);
		//  Enable light 0
		gl2.glEnable(GL2.GL_LIGHT0);
		//  Set ambient, diffuse, specular components and position of light 0
		gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, ambient, 0);
		gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, diffuse, 0);
		gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, specular, 0);
		gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, position, 0);
		//  Set materials
		gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, shinyness, 0);
		gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, specular, 0);
		gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, emission, 0);
		//
		//  Draw scene
		//
		// Select shader (0 => no shader)
		gl2.glUseProgram(shader[mode]);
		// Export time to uniform variable
		if (mode == 1) {
			float time = (float) (0.001 * (System.currentTimeMillis() - start));
			int id;
			id= gl2.glGetUniformLocation(shader[mode], "mode");
			if (id >= 0) {
				gl2.glUniform1i(shader[mode], 3);
			}
			id = gl2.glGetUniformLocation(shader[mode], "time");
			if (id >= 0) {
				gl2.glUniform1f(id, time);
			}
			id = gl2.glGetUniformLocation(shader[mode], "marble");
			if (id >= 0) {
				gl2.glUniform1i(id, 0);
			}
			id = gl2.glGetUniformLocation(shader[mode], "SimpTex");
			if (id >= 0) {
				gl2.glUniform1i(id, 1);
			}
			id = gl2.glGetUniformLocation(shader[mode], "PermTex");
			if (id >= 0) {
				gl2.glUniform1i(id, 2);
			}
			id = gl2.glGetUniformLocation(shader[mode], "GradTex");
			if (id >= 0) {
				gl2.glUniform1i(id, 3);
			}
			//if (move) {
				zh = (int) ((90 * time) % 360.0);
			//}
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
		gl2.glDisable(GL2.GL_LIGHTING);
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
		gl2.glWindowPos2i(5, height - 15);
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
				Assignment9.reshape(glautodrawable.getGL().getGL2(), width, height);
			}
			
			@Override
			public void init(GLAutoDrawable glautodrawable) {
				start = System.currentTimeMillis();
				gl = glautodrawable.getGL().getGL2();
				glc = GLContext.getCurrent();
				// Create Shader Programs
				shader[0] = CSCIx239.createShaderProg(gl, "confetti.vert", "");
				shader[1] = CSCIx239.createShaderProg(gl, "fire.vert", "fire.frag");
				
				CSCIx239.loadTexBMP(gl.getGL3(), new File("particle.bmp"));
				// Initialize particles
				InitPart();
			}
			
			@Override
			public void dispose(GLAutoDrawable glautodrawable) {
				gl = null;
			}
			
			@Override
			public void display(GLAutoDrawable glautodrawable) {
				// Called when rendering is necessary
				Assignment9.display(glautodrawable.getGL().getGL2(), glautodrawable.getAnimator(), glautodrawable.getSurfaceWidth(), glautodrawable.getSurfaceHeight());
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
		final Frame frame = new Frame("Advanced Shaders - Jonathan Huang");
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

	/*
	 *  Initialize particles
	 */
	private static void InitPart() {
		//  Array Pointers
		int vert = 0;
		int color = 0;
		int vel = 0;
		int start = 0;
		//  Loop over NxN patch
		int i,j;
		n = mode == 1 ? 15 : N;
		for (i=0;i<n;i++) {
			for (j=0;j<n;j++) {
				//  Location x,y,z
				Vert[vert++] = (float) ((i+0.5)/n-0.75);
				Vert[vert++] = 0;
				Vert[vert++] = (float) ((j+0.5)/n-0.75);
				// Color r,g,b (0.5-1.0)
				Color[color++] = (float) r.nextDouble(0.5, 1.0);
				Color[color++] = (float) r.nextDouble(0.5, 1.0);
				Color[color++] = (float) r.nextDouble(0.5, 1.0);
				//  Velocity
				Vel[vel++] = (float) r.nextDouble(1.0, 4.0);
				Vel[vel++] = (float) r.nextDouble(0, 10.0);
				Vel[vel++] = (float) r.nextDouble(1.0, 4.0);
				//  Launch time
				Start[start++] = (float) r.nextDouble(0.0, 2.0);
			}
		}
	}

	/*
	 *  Draw particles
	 */
	public static void DrawPart(GL2 gl) {
		//  Set particle size
		gl.glPointSize(mode == 1 ? 50 : 2);
		//  Point vertex location to local array Vert
		gl.glVertexPointer(3, GL2.GL_FLOAT,0,FloatBuffer.wrap(Vert));
		//  Point color array to local array Color
		gl.glColorPointer(3, GL2.GL_FLOAT,0,FloatBuffer.wrap(Color));
		//  Point attribute arrays to local arrays
		gl.glVertexAttribPointer(4, 3, GL2.GL_FLOAT, false, 0, FloatBuffer.wrap(Vel));
		gl.glVertexAttribPointer(5, 1, GL2.GL_FLOAT, false, 0, FloatBuffer.wrap(Start));
		//  Enable arrays used by DrawArrays
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
		gl.glEnableVertexAttribArray(4);
		gl.glEnableVertexAttribArray(5);
		//  Set transparent large particles
		if (mode == 1) {
			gl.glEnable(GL2.GL_POINT_SPRITE);
			gl.glTexEnvi(GL2.GL_POINT_SPRITE,GL2.GL_COORD_REPLACE,GL2.GL_TRUE);
			gl.glEnable(GL2.GL_BLEND);
			gl.glBlendFunc(GL2.GL_SRC_ALPHA,GL2.GL_ONE);
			gl.glDepthMask(false);
		}
		//  Draw arrays
		gl.glDrawArrays(GL2.GL_POINTS,0,n*n);
		//  Reset
		if (mode == 1) {
			gl.glDisable(GL2.GL_POINT_SPRITE);
			gl.glDisable(GL2.GL_BLEND);
			gl.glDepthMask(true);
		}
		//  Disable arrays
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
		gl.glDisableVertexAttribArray(4);
		gl.glDisableVertexAttribArray(5);
	}
}