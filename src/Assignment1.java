import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;

public class Assignment1 {
	protected static void setup(GL2 gl2, int width, int height) {
		gl2.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		gl2.glLoadIdentity();
		// coordinate system origin at lower left with width and height same as the window
		GLU glu = new GLU();
		glu.gluOrtho2D(0.0f, width, 0.0f, height);
		gl2.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		gl2.glLoadIdentity();
		gl2.glViewport(0, 0, width, height);
	}

	protected static void render(GL2 gl2, int width, int height) {
		gl2.glClear(GL.GL_COLOR_BUFFER_BIT);
		// draw a triangle filling the window
		gl2.glLoadIdentity();
		gl2.glBegin(GL.GL_TRIANGLES);
		gl2.glColor3f(1, 0, 0);
		gl2.glVertex2f(0, 0);
		gl2.glColor3f(0, 1, 0);
		gl2.glVertex2f(width, 0);
		gl2.glColor3f(0, 0, 1);
		gl2.glVertex2f(width / 2, height);
		gl2.glEnd();
	}

	public static final void main(String[] args) {
		GLProfile glprofile = GLProfile.getDefault();
		GLCapabilities glcapabilities = new GLCapabilities(glprofile);
		final GLCanvas glcanvas = new GLCanvas(glcapabilities);
		glcanvas.addGLEventListener(new GLEventListener() {
			@Override
			public void reshape(GLAutoDrawable glautodrawable, int x, int y, int width, int height) {
				// Called when application first loads or the window is resized
				Assignment1.setup(glautodrawable.getGL().getGL2(), width, height);
			}
			
			@Override
			public void init(GLAutoDrawable glautodrawable) {
			}
			
			@Override
			public void dispose(GLAutoDrawable glautodrawable) {
			}
			
			@Override
			public void display(GLAutoDrawable glautodrawable) {
				// Called when rendering is necessary
				Assignment1.render(glautodrawable.getGL().getGL2(), glautodrawable.getSurfaceWidth(), glautodrawable.getSurfaceHeight());
			}
		});
		final Frame frame = new Frame("Assignment 1 - Jonathan Huang");
		frame.add(glcanvas);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent windowevent) {
				frame.remove(glcanvas);
				frame.dispose();
				System.exit(0);
			}
		});
		frame.setSize(640, 480);
		frame.setVisible(true);
	}
}