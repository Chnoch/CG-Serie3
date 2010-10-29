package jrtr;

import java.util.LinkedList;
import java.util.ListIterator;
import javax.media.opengl.*;
import javax.vecmath.*;

/**
 * This class implements a {@link RenderContext} (a renderer) using OpenGL.
 */
public class GLRenderContext implements RenderContext {

	private SceneManagerInterface sceneManager;
	private GL2 gl;
	
	/**
	 * This constructor is called by {@link GLRenderPanel}.
	 * 
	 * @param drawable 	the OpenGL rendering context. All OpenGL calls are
	 * 					directed to this object.
	 */
	public GLRenderContext(GLAutoDrawable drawable)
	{
		gl = drawable.getGL().getGL2();
		gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
	}

		
	/**
	 * Set the scene manager. The scene manager contains the 3D
	 * scene that will be rendered. The scene includes geometry
	 * as well as the camera and viewing frustum.
	 */
	public void setSceneManager(SceneManagerInterface sceneManager)
	{
		this.sceneManager = sceneManager;
	}
	
	/**
	 * This method is called by the GLRenderPanel to redraw the 3D scene.
	 * The method traverses the scene using the scene manager and passes
	 * each object to the rendering method.
	 */
	public void display(GLAutoDrawable drawable)
	{
		gl = drawable.getGL().getGL2();
		
		beginFrame();
		
		SceneManagerIterator iterator = sceneManager.iterator();	
		while(iterator.hasNext())
		{
			RenderItem r = iterator.next();
			if(r.getShape()!=null) draw(r);
		}		
		
		endFrame();
	}
		
	/**
	 * This method is called at the beginning of each frame, i.e., before
	 * scene drawing starts.
	 */
	private void beginFrame()
	{
		setLights();
		
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
        // Load the projection matrix
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadMatrixf(matrix4fToFloat16(sceneManager.getFrustum().getProjectionMatrix()), 0);
	}

	/**
	 * This method is called at the end of each frame, i.e., after
	 * scene drawing is complete.
	 */
	private void endFrame()
	{
        gl.glFlush();		
	}
	
	/**
	 * Convert a Matrix4f to a float array in column major ordering,
	 * as used by OpenGL.
	 */
	private float[] matrix4fToFloat16(Matrix4f m)
	{
		float[] f = new float[16];
		for(int i=0; i<4; i++)
			for(int j=0; j<4; j++)
				f[j*4+i] = m.getElement(i,j);
		return f;
	}
	
	/**
	 * The main rendering method.
	 * 
	 * @param renderItem	the object that needs to be drawn
	 */
	private void draw(RenderItem renderItem)
	{
		VertexData vertexData = renderItem.getShape().getVertexData();
		LinkedList<VertexData.VertexElement> vertexElements = vertexData.getElements();
		int indices[] = vertexData.getIndices();

		// Don't draw if there are no indices
		if(indices == null) return;
		
		// Set the material
		setMaterial(renderItem.getShape().getMaterial());

		// Set the modelview matrix by multiplying the camera matrix and the 
		// transformation matrix of the object
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		Matrix4f t = new Matrix4f();
		t.set(sceneManager.getCamera().getCameraMatrix());
		t.mul(renderItem.getT());
		gl.glLoadMatrixf(matrix4fToFloat16(t), 0);
	     
        // Draw geometry
        gl.glBegin(GL.GL_TRIANGLES);
		for(int j=0; j<indices.length; j++)
		{
			int i = indices[j];
			
			ListIterator<VertexData.VertexElement> itr = vertexElements.listIterator(0);
			while(itr.hasNext())
			{
				VertexData.VertexElement e = itr.next();
				if(e.getSemantic() == VertexData.Semantic.POSITION)
				{
					if(e.getNumberOfComponents()==2)
					{
						gl.glVertex2f(e.getData()[i*2], e.getData()[i*2+1]);
					}
					else if(e.getNumberOfComponents()==3)
					{
						gl.glVertex3f(e.getData()[i*3], e.getData()[i*3+1], e.getData()[i*3+2]);
					}
					else if(e.getNumberOfComponents()==4)
					{
						gl.glVertex4f(e.getData()[i*4], e.getData()[i*4+1], e.getData()[i*4+2], e.getData()[i*4+3]);
					}
				} 
				else if(e.getSemantic() == VertexData.Semantic.NORMAL)
				{
					if(e.getNumberOfComponents()==3)
					{
						gl.glNormal3f(e.getData()[i*3], e.getData()[i*3+1], e.getData()[i*3+2]);
					}
					else if(e.getNumberOfComponents()==4)
					{
						gl.glVertex4f(e.getData()[i*4], e.getData()[i*4+1], e.getData()[i*4+2], e.getData()[i*4+3]);
					}
				}
				else if(e.getSemantic() == VertexData.Semantic.TEXCOORD)
				{
					if(e.getNumberOfComponents()==2)
					{
						gl.glTexCoord2f(e.getData()[i*2], e.getData()[i*2+1]);
					}
					else if(e.getNumberOfComponents()==3)
					{
						gl.glTexCoord3f(e.getData()[i*3], e.getData()[i*3+1], e.getData()[i*3+2]);
					}
					else if(e.getNumberOfComponents()==4)
					{
						gl.glTexCoord4f(e.getData()[i*4], e.getData()[i*4+1], e.getData()[i*4+2], e.getData()[i*4+3]);
					}
				}
				else if(e.getSemantic() == VertexData.Semantic.COLOR)
				{
					if(e.getNumberOfComponents()==3)
					{
						gl.glColor3f(e.getData()[i*3], e.getData()[i*3+1], e.getData()[i*3+2]);
					}
					else if(e.getNumberOfComponents()==4)
					{
						gl.glColor4f(e.getData()[i*4], e.getData()[i*4+1], e.getData()[i*4+2], e.getData()[i*4+3]);
					}
				}

			}
			
		}
        gl.glEnd();
        
        cleanMaterial(renderItem.getShape().getMaterial());
	}

	/**
	 * Pass the material properties to OpenGL, including textures and shaders.
	 * 
	 * To be implemented in the "Textures and Shading" project.
	 */
	private void setMaterial(Material m)
	{
	}
	
	/**
	 * Pass the light properties to OpenGL. This assumes the list of lights in 
	 * the scene manager is accessible via a method Iterator<Light> lightIterator().
	 * 
	 * To be implemented in the "Textures and Shading" project.
	 */
	void setLights()
	{	
	}

	/**
	 * Disable a material.
	 * 
	 * To be implemented in the "Textures and Shading" project.
	 */
	private void cleanMaterial(Material m)
	{
	}

	public Shader makeShader()
	{
		return new GLShader(gl);
	}
	
	public Texture makeTexture()
	{
		return new GLTexture(gl);
	}
}
	