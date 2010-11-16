import jrtr.*;

import javax.swing.*;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Implements a simple application that opens a 3D rendering window and shows a
 * rotating cube.
 */
public class Aufgabe2 {
	static RenderPanel renderPanel;
	static RenderContext renderContext;
	static SimpleSceneManager sceneManager;
	static Shape shape;
	static float angle;

	/**
	 * An extension of {@link GLRenderPanel} or {@link SWRenderPanel} to provide
	 * a call-back function for initialization.
	 */
	public final static class SimpleRenderPanel extends GLRenderPanel {
		/**
		 * Initialization call-back. We initialize our renderer here.
		 * 
		 * @param r
		 *            the render context that is associated with this render
		 *            panel
		 */
		public void init(RenderContext r) {
			renderContext = r;
			renderContext.setSceneManager(sceneManager);

			// Use a shader
			Shader s = r.makeShader();
			try {
				s.load("..\\shaders\\phong.vert", "..\\shaders\\phong.frag");
			} catch (Exception e) {
				System.out.print("Problem with shader:\n");
				System.out.print(e.getMessage());
			}
			s.use();

			// Register a timer task
			Timer timer = new Timer();
			angle = 0.01f;
			timer.scheduleAtFixedRate(new AnimationTask(), 0, 10);
		}

		/**
		 * A timer task that generates an animation. This task triggers the
		 * redrawing of the 3D scene every time it is executed.
		 */
		public static class AnimationTask extends TimerTask {
			public void run() {
				// Update transformation
				Matrix4f t = shape.getTransformation();
				Matrix4f rotX = new Matrix4f();
				rotX.rotX(angle);
				Matrix4f rotY = new Matrix4f();
				rotY.rotY(angle);
				t.mul(rotX);
				t.mul(rotY);
				shape.setTransformation(t);

				// Trigger redrawing of the render window
				renderPanel.getCanvas().repaint();
			}
		}
	}

	/**
	 * The main function opens a 3D rendering window, constructs a simple 3D
	 * scene, and starts a timer task to generate an animation.
	 */
	public static void main(String[] args) {

		// Construct a data structure that stores the vertices, their
		// attributes, and the triangle mesh connectivity
		VertexData vertexData = null;
		try {
			vertexData = ObjReader.read("C:\\teapot.obj", 1);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Make a scene manager and add the object
		Material material = new Material();
		material.ambient = new Vector3f(0.2f,0.2f,0.2f);
		material.diffuse = new Vector3f(0.5f,0.5f,0.5f);
		material.specular = new Vector3f(1f,0.5f,0.5f);
		material.shininess = 8;
		
		
		sceneManager = new SimpleSceneManager();
		shape = new Shape(vertexData);
		shape.setMaterial(material);
		sceneManager.addShape(shape);
		Light firstLight = new Light();
		firstLight.type = Light.Type.POINT;
		firstLight.position = new Vector3f(-60,20,20);
		firstLight.specular = new Vector3f(0,0,1);
		firstLight.diffuse = new Vector3f(0.7f,0.7f,0.7f);
		firstLight.ambient = new Vector3f(0,0,0);
		sceneManager.addLight(firstLight);
		
		Light secondLight = new Light();
		secondLight.type = Light.Type.POINT;
		secondLight.position = new Vector3f(60,20,20);
		secondLight.specular = new Vector3f(1,0,0);
		secondLight.diffuse = new Vector3f(0.5f,0.5f,0.5f);
		secondLight.ambient = new Vector3f(0,0,0);
		sceneManager.addLight(secondLight);
		

		// Make a render panel. The init function of the renderPanel
		// (see above) will be called back for initialization.
		renderPanel = new SimpleRenderPanel();

		sceneManager.getCamera().setCenterOfProjection(new Vector3f(0, 0, 5));

		// Make the main window of this application and add the renderer to it
		JFrame jframe = new JFrame("simple");
		jframe.setSize(500, 500);
		jframe.setLocationRelativeTo(null); // center of screen
		jframe.getContentPane().add(renderPanel.getCanvas());// put the canvas
		// into a JFrame
		// window

		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setVisible(true); // show window
	}
}
