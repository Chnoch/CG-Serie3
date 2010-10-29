import jrtr.*;
import javax.swing.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import javax.vecmath.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Implements a simple application that opens a 3D rendering window and 
 * shows a rotating cube.
 */
public class simple
{	
	static RenderPanel renderPanel;
	static RenderContext renderContext;
	static SimpleSceneManager sceneManager;
	static Shape shape;
	static float angle;
	

	/**
	 * An extension of {@link GLRenderPanel} or {@link SWRenderPanel} to 
	 * provide a call-back function for initialization. 
	 */ 
	public final static class SimpleRenderPanel extends GLRenderPanel
	{
		/**
		 * Initialization call-back. We initialize our renderer here.
		 * 
		 * @param r	the render context that is associated with this render panel
		 */
		public void init(RenderContext r)
		{
			renderContext = r;
			renderContext.setSceneManager(sceneManager);
	
			// Register a timer task
		    Timer timer = new Timer();
		    angle = 0.01f;
		    timer.scheduleAtFixedRate(new AnimationTask(), 0, 10);
		}
	}

	/**
	 * A timer task that generates an animation. This task triggers
	 * the redrawing of the 3D scene every time it is executed.
	 */
	public static class AnimationTask extends TimerTask
	{
		public void run()
		{
			// Update transformation
//    		Matrix4f t = shape.getTransformation();
//    		Matrix4f rotX = new Matrix4f();
//    		rotX.rotX(angle);
//    		Matrix4f rotY = new Matrix4f();
//    		rotY.rotY(angle);
//    		t.mul(rotX);
//    		t.mul(rotY);
//    		shape.setTransformation(t);
//    		
//    		// Trigger redrawing of the render window
//    		renderPanel.getCanvas().repaint(); 
		}
	}

	/**
	 * A mouse listener for the main window of this application. This can be
	 * used to process mouse events.
	 */
	public static class SimpleMouseListener implements MouseListener
	{
    	public void mousePressed(MouseEvent e) {}
    	public void mouseReleased(MouseEvent e) {}
    	public void mouseEntered(MouseEvent e) {}
    	public void mouseExited(MouseEvent e) {}
    	public void mouseClicked(MouseEvent e) {}
	}
	
	/**
	 * The main function opens a 3D rendering window, constructs a simple 3D
	 * scene, and starts a timer task to generate an animation.
	 */
	public static void main(String[] args)
	{		
		sceneManager = new SimpleSceneManager();
		shape = makeHouse();
		sceneManager.addShape(shape);
		Camera camera = sceneManager.getCamera();
		Frustum frustum = sceneManager.getFrustum();
		
//		camera1(camera, frustum);
		camera2(camera, frustum);

		// Make a render panel. The init function of the renderPanel
		// (see above) will be ca lled back for initialization.
		renderPanel = new SimpleRenderPanel();
		
		// Make the main window of this application and add the renderer to it
		JFrame jframe = new JFrame("simple");
		jframe.setSize(500, 500);
		jframe.setLocationRelativeTo(null); // center of screen
		jframe.getContentPane().add(renderPanel.getCanvas());// put the canvas into a JFrame window

		// Add a mouse listener
	    jframe.addMouseListener(new SimpleMouseListener());
		   	    	    
	    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    jframe.setVisible(true); // show window
	}

    private static void camera1(Camera camera, Frustum frustum) {
        camera.setCenterOfProjection(new Vector3f(0,0,40));
		camera.setLookAtPoint(new Vector3f(0,0,0));
		camera.setUpVector(new Vector3f(0,1,0));
		frustum.setAspectRatio(1);
		frustum.setFarPlane(100);
		frustum.setNearPlane(1);
		frustum.setVertFOV(60);
    }

    private static void camera2(Camera camera, Frustum frustum) {
        camera.setCenterOfProjection(new Vector3f(-10,40,40));
        camera.setLookAtPoint(new Vector3f(-5,0,0));
        camera.setUpVector(new Vector3f(0,1,0));
        frustum.setAspectRatio(1);
        frustum.setFarPlane(100);
        frustum.setNearPlane(1);
        frustum.setVertFOV(60);
    }
	
	   public static Shape makeHouse()
	    {
	        // A house
	        float vertices[] = {-4,-4,4, 4,-4,4, 4,4,4, -4,4,4,     // front face
	                            -4,-4,-4, -4,-4,4, -4,4,4, -4,4,-4, // left face
	                            4,-4,-4,-4,-4,-4, -4,4,-4, 4,4,-4,  // back face
	                            4,-4,4, 4,-4,-4, 4,4,-4, 4,4,4,     // right face
	                            4,4,4, 4,4,-4, -4,4,-4, -4,4,4,     // top face
	                            -4,-4,4, -4,-4,-4, 4,-4,-4, 4,-4,4, // bottom face
	    
	                            -20,-4,20, 20,-4,20, 20,-4,-20, -20,-4,-20, // ground floor
	                            -4,4,4, 4,4,4, 0,8,4,               // the roof
	                            4,4,4, 4,4,-4, 0,8,-4, 0,8,4,
	                            -4,4,4, 0,8,4, 0,8,-4, -4,4,-4,
	                            4,4,-4, -4,4,-4, 0,8,-4};
	    
	        float normals[] = {0,0,1,  0,0,1,  0,0,1,  0,0,1,       // front face
	                           -1,0,0, -1,0,0, -1,0,0, -1,0,0,      // left face
	                           0,0,-1, 0,0,-1, 0,0,-1, 0,0,-1,      // back face
	                           1,0,0,  1,0,0,  1,0,0,  1,0,0,       // right face
	                           0,1,0,  0,1,0,  0,1,0,  0,1,0,       // top face
	                           0,-1,0, 0,-1,0, 0,-1,0, 0,-1,0,      // bottom face
	    
	                           0,1,0,  0,1,0,  0,1,0,  0,1,0,       // ground floor
	                           0,0,1,  0,0,1,  0,0,1,               // front roof
	                           0.707f,0.707f,0, 0.707f,0.707f,0, 0.707f,0.707f,0, 0.707f,0.707f,0, // right roof
	                           -0.707f,0.707f,0, -0.707f,0.707f,0, -0.707f,0.707f,0, -0.707f,0.707f,0, // left roof
	                           0,0,-1, 0,0,-1, 0,0,-1};             // back roof
	                           
	        float colors[] = {1,0,0, 1,0,0, 1,0,0, 1,0,0,
	                          0,1,0, 0,1,0, 0,1,0, 0,1,0,
	                          1,0,0, 1,0,0, 1,0,0, 1,0,0,
	                          0,1,0, 0,1,0, 0,1,0, 0,1,0,
	                          0,0,1, 0,0,1, 0,0,1, 0,0,1,
	                          0,0,1, 0,0,1, 0,0,1, 0,0,1,
	        
	                          0,0.5f,0, 0,0.5f,0, 0,0.5f,0, 0,0.5f,0,           // ground floor
	                          0,0,1, 0,0,1, 0,0,1,                          // roof
	                          1,0,0, 1,0,0, 1,0,0, 1,0,0,
	                          0,1,0, 0,1,0, 0,1,0, 0,1,0,
	                          0,0,1, 0,0,1, 0,0,1,};
	    
	        // Set up the vertex data
	        VertexData vertexData = new VertexData(42);
	    
	        // Specify the elements of the vertex data:
	        // - one element for vertex positions
	        vertexData.addElement(vertices, VertexData.Semantic.POSITION, 3);
	        // - one element for vertex colors
	        vertexData.addElement(colors, VertexData.Semantic.COLOR, 3);
	        // - one element for vertex normals
	        vertexData.addElement(normals, VertexData.Semantic.NORMAL, 3);
	        
	        // The index data that stores the connectivity of the triangles
	        int indices[] = {0,2,3, 0,1,2,          // front face
	                         4,6,7, 4,5,6,          // left face
	                         8,10,11, 8,9,10,       // back face
	                         12,14,15, 12,13,14,    // right face
	                         16,18,19, 16,17,18,    // top face
	                         20,22,23, 20,21,22,    // bottom face
	                         
	                         24,26,27, 24,25,26,    // ground floor
	                         28,29,30,              // roof
	                         31,33,34, 31,32,33,
	                         35,37,38, 35,36,37,
	                         39,40,41}; 
	    
	        vertexData.addIndices(indices);
	    
	        Shape house = new Shape(vertexData);
	        
	        return house;
	    }
	    

}
