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
public class Aufgabe3 {
	static RenderPanel renderPanel;
	static RenderContext renderContext;
	static SimpleSceneManager sceneManager;
	static Shape shape,shape2;
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
				s.load("..\\shaders\\textures.vert", "..\\shaders\\textures.frag");
			} catch (Exception e) {
				System.out.print("Problem with shader:\n");
				System.out.print(e.getMessage());
			}
			s.use();

			Texture tex1 = renderContext.makeTexture();
			Texture tex2 = renderContext.makeTexture();
			
			try {
				tex1.load("texture2.jpg");
				tex2.load("texture3.jpg");
			} catch (IOException e) {
				System.out.print("Problem with textures:\n");
				System.out.print(e.getMessage());
			}
			
			shape.getMaterial().setTexture(tex1);
			shape2.getMaterial().setTexture(tex2);
			
			
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
				
				// Update transformation
				t = shape2.getTransformation();
				rotX = new Matrix4f();
				rotX.rotX(angle);
				rotY = new Matrix4f();
				rotY.rotY(angle);
				t.mul(rotX);
				t.mul(rotY);
				shape2.setTransformation(t);
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
		// Make a render panel. The init function of the renderPanel
		// (see above) will be called back for initialization.
		renderPanel = new SimpleRenderPanel();

		// Construct a data structure that stores the vertices, their
		// attributes, and the triangle mesh connectivity
		VertexData vertexData = null;
		try {
			vertexData = ObjReader.read(".\\teapot_tex.obj", 1);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Make a scene manager and add the object
		
		sceneManager = new SimpleSceneManager();

		shape = new Shape(vertexData);
		shape2 = new Shape(vertexData);
		Matrix4f mat = shape.getTransformation();
		Matrix4f translation = new Matrix4f(1,0,0,-3,
											0,1,0,0,
											0,0,1,0,
											0,0,0,1);
		mat.add(translation);
		shape.setTransformation(mat);
		
		mat = shape2.getTransformation();
		translation = new Matrix4f(1,0,0,3,
									0,1,0,0,
									0,0,1,0,
									0,0,0,1);
		mat.add(translation);
		shape2.setTransformation(mat);
		
		Material material = makeMaterial();
		Material material2 = makeMaterial();
		shape.setMaterial(material);
		shape2.setMaterial(material2);
		
		
		Light firstLight = new Light();
		firstLight.type = Light.Type.POINT;
		firstLight.position = new Vector3f(-30,15,-30);
		firstLight.specular = new Vector3f(0,0,1);
		firstLight.diffuse = new Vector3f(0.5f,0.5f,0.5f);
		firstLight.ambient = new Vector3f(0.2f,0.2f,0.2f);
		
		Light secondLight = new Light();
		secondLight.type = Light.Type.POINT;
		secondLight.position = new Vector3f(80,20,50);
		secondLight.specular = new Vector3f(1,0,0);
		secondLight.diffuse = new Vector3f(0.5f,0.5f,0.5f);
		secondLight.ambient = new Vector3f(0.2f,0.2f,0.2f);
		
		Light thirdLight = new Light();
		thirdLight.type = Light.Type.POINT;
		thirdLight.position = new Vector3f(0,10,10);
		thirdLight.specular = new Vector3f(1,1,1);
		thirdLight.diffuse = new Vector3f(1f,1f,1f);
		thirdLight.ambient = new Vector3f(0,0,0);
		

		sceneManager.addShape(shape);
		sceneManager.addShape(shape2);
		sceneManager.addLight(firstLight);
		sceneManager.addLight(secondLight);
		sceneManager.addLight(thirdLight);

		sceneManager.getCamera().setCenterOfProjection(new Vector3f(0, 0, 7));

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
	
	private static Material makeMaterial() {
		Material material = new Material();
		material.ambient = new Vector3f(0.2f,0.2f,0.2f);
		material.diffuse = new Vector3f(0.5f,0.5f,0.5f);
		material.specular = new Vector3f(1f,0.5f,0.5f);
		material.shininess = 8;
		
		return material;
	}
}
