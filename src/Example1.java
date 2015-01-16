import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.gl2.GLUT;

public final class Example1 {
	private static long start;
	private static boolean axes = true;       //  Display axes
	private static int mode=0;       //  Shader mode
	private static int move=1;       //  Move light
	private static boolean perspProj=false;       //  Projection type
	private static int obj=0;        //  Object
	private static int th=0;         //  Azimuth of view angle
	private static int ph=0;         //  Elevation of view angle
	private static int fov=55;       //  Field of view (for perspective)
	private static double asp=1;     //  Aspect ratio
	private static double dim=3.0;   //  Size of world
	private static int model;        //  Model display list
	private static int shader[] = {0,0}; //  Shader program
	private static String text[] = {"No Shader", "Basic Shader"};

	private static final GLUT glt = new GLUT();
	private static GL2 gl;

	private static double Sin(double th) {
		return Math.sin(Math.PI / (180 * th));
	}

	private static double Cos(double th) {
		return Math.cos(Math.PI / (180 * th));
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

	private static void setup(GL2 gl2, int width, int height) {
		//  Ratio of the width to the height of the window
		asp = (height>0) ? (double)width/height : 1;
		//  Set the viewport to the entire window
		gl2.glViewport(0,0, width,height);
		project(gl2, perspProj ? fov : 0.0, asp, dim);
	}

	private static void project(GL2 gl2, double fov, double asp,double dim) {
		//  Tell OpenGL we want to manipulate the projection matrix
		gl2.glMatrixMode(GL2.GL_PROJECTION);
		//  Undo previous transformations
		gl2.glLoadIdentity();
		//  Perspective transformation
		if (fov > 0.0) {
			GLU glu = GLU.createGLU(gl2);
			glu.gluPerspective(fov,asp,dim/16,16*dim);
		} else { //  Orthogonal transformation
			gl2.glOrtho(-asp*dim,asp*dim,-dim,+dim,-dim,+dim);
		}
		//  Switch to manipulating the model matrix
		gl2.glMatrixMode(GL2.GL_MODELVIEW);
		//  Undo previous transformations
		gl2.glLoadIdentity();
	}

	/**
	 * Rendering takes place here.
	 * @param gl2
	 * @param width
	 * @param height
	 */
	private static void display(GL2 gl2, int width, int height) {
		double len = 2.0; // Length of axes
		// Erase the window and the depth buffer
		gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		// Enable Z-buffering in OpenGL
		gl2.glEnable(GL2.GL_DEPTH_TEST);
		// Undo previous transformations
		gl2.glLoadIdentity();
		// Perspective - set eye position
		if (perspProj) {
			double Ex = -2*dim*Sin(th)*Cos(ph);
			double Ey = +2*dim*Sin(ph);
			double Ez = +2*dim*Cos(th)*Cos(ph);
			GLU glu = GLU.createGLU(gl2);
			glu.gluLookAt(Ex, Ey, Ez, 0.0, 0.0, 0.0, 0.0, Cos(ph), 0.0);
		} else { // Orthogonal - set world orientation
			gl2.glRotatef(ph,1,0,0);
			gl2.glRotatef(th,0,1,0);
		}
		//  Select shader (0 => no shader)
		gl2.glUseProgram(shader[mode]);
		//  Export time to uniform variable
		if (mode == 1) {
			//float time = 0.001 * glu.glutGet(); // GLUT.GLUT_ELAPSED_TIME
			float time = (float) (0.001 * (System.currentTimeMillis() - start));
			int id = gl2.glGetUniformLocation(shader[mode], "time");
			if (id >= 0) {
				gl2.glUniform1f(id,time);
			}
		}
		//  Draw the model, teapot or cube
		gl2.glColor3f(1,1,0);
		if (obj == 2) {
			gl2.glCallList(model);
		} else if (obj == 1) {
			glt.glutSolidTeapot(1.0);
		} else {
			cube(gl2);
		}
		//  No shader for what follows
		gl2.glUseProgram(0);
		//  Draw axes - no lighting from here on
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
			//  Label axes
			gl2.glRasterPos3d(len,0.0,0.0);
			Print("X");
			gl2.glRasterPos3d(0.0,len,0.0);
			Print("Y");
			gl2.glRasterPos3d(0.0,0.0,len);
			Print("Z");
		}
		//  Display parameters
		gl2.glWindowPos2i(5,5);
		Print("Angle=" + th + "," + ph + "  Dim=" + dim + " Projection=" + (perspProj ? "Perpective" : "Orthogonal") + " " + text[mode]);
		//  Render the scene and make it visible
		errCheck(gl2, "display");
		gl2.glFlush();
	}

	private static void Print(String s) {
		for (int i = 0, n = s.length(); i < n; i++) {
			glt.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, s.charAt(i));
		}
	}

	private static void errCheck(GL2 gl2, String s) {
		int err = gl2.glGetError();
		if (err > 0) {
			GLU glu = GLU.createGLU(gl2);
			String errstr = glu.gluErrorString(err);
			System.err.println("ERROR: " + errstr + " [" + s + "]");
		}
	}

	public static void main(String[] args) {
		GLProfile glprofile = GLProfile.getDefault();
		GLCapabilities glcapabilities = new GLCapabilities(glprofile);
		final GLCanvas glcanvas = new GLCanvas(glcapabilities);
		glcanvas.addGLEventListener(new GLEventListener() {
			@Override
			public void reshape(GLAutoDrawable glautodrawable, int x, int y, int width, int height) {
				// Called when application first loads or the window is resized
				Example1.setup(glautodrawable.getGL().getGL2(), width, height);
			}
			
			@Override
			public void init(GLAutoDrawable glautodrawable) {
				start = System.currentTimeMillis();
				gl = glautodrawable.getGL().getGL2();
			}
			
			@Override
			public void dispose(GLAutoDrawable glautodrawable) {
			}
			
			@Override
			public void display(GLAutoDrawable glautodrawable) {
				// Called when rendering is necessary
				Example1.display(glautodrawable.getGL().getGL2(), glautodrawable.getSurfaceWidth(), glautodrawable.getSurfaceHeight());
			}
		});
		glcanvas.addKeyListener(new KeyListener() {
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
						obj = (obj+1)%3;
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
				project(gl, perspProj ? fov:0,asp,dim);
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
		});
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
	}
}