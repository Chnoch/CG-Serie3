package jrtr;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Iterator;
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
		gl.glEnable(GL2.GL_DEPTH_TEST);
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
			draw(iterator.next());
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
		
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
        gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
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
        gl.glBegin(GL2.GL_TRIANGLES);
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
	 */
	private void setMaterial(Material m)
	{
		if(m!=null)
		{
			float diffuse[] = new float[4];
			diffuse[0] = m.diffuse.x;
			diffuse[1] = m.diffuse.y;
			diffuse[2] = m.diffuse.z;
			diffuse[3] = 1.f;
			gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, diffuse, 0);
			
			float ambient[] = new float[4];
			ambient[0] = m.ambient.x;
			ambient[1] = m.ambient.y;
			ambient[2] = m.ambient.z;
			ambient[3] = 1.f;
			gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, ambient, 0);

			float specular[] = new float[4];
			specular[0] = m.specular.x;
			specular[1] = m.specular.y;
			specular[2] = m.specular.z;
			specular[3] = 1.f;
			gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, specular, 0);

			gl.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, m.shininess);

			GLTexture tex = (GLTexture)(m.texture);
			if(tex!=null)
			{
				gl.glEnable(GL2.GL_TEXTURE_2D);
				gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
				gl.glBindTexture(GL2.GL_TEXTURE_2D, tex.getId());
				gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
				gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
			}
			if(m.shader!=null)
			{
				m.shader.use();
			}
		}
	}
	
	/**
	 * Pass the light properties to OpenGL. This assumes the list of lights in 
	 * the scene manager is accessible via a method Iterator<Light> lightIterator().
	 */
	void setLights()
	{	
		int lightIndex[] = {GL2.GL_LIGHT0, GL2.GL_LIGHT1, GL2.GL_LIGHT2, GL2.GL_LIGHT3, GL2.GL_LIGHT4, GL2.GL_LIGHT5, GL2.GL_LIGHT6, GL2.GL_LIGHT7};

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();

		Iterator<Light> iter = sceneManager.lightIterator();

		if(iter.hasNext())
		{
			// Lighting
			gl.glEnable(GL2.GL_LIGHTING);
		}

		int i=0;
		Light l;
		while(iter.hasNext() && i<8)
		{
			l = iter.next(); 

			gl.glEnable(lightIndex[i]);

			if(l.type == Light.Type.DIRECTIONAL)
			{
				float[] direction = new float[4];
				direction[0] = l.direction.x;
				direction[1] = l.direction.y;
				direction[2] = l.direction.z;
				direction[3] = 0.f;
				gl.glLightfv(lightIndex[i], GL2.GL_POSITION, direction, 0);
			}
			if(l.type == Light.Type.POINT || l.type == Light.Type.SPOT)
			{
				float[] position = new float[4];
				position[0] = l.position.x;
				position[1] = l.position.y;
				position[2] = l.position.z;
				position[3] = 1.f;
				gl.glLightfv(lightIndex[i], GL2.GL_POSITION, position, 0);
			}
			if(l.type == Light.Type.SPOT)
			{
				float[] spotDirection = new float[3];
				spotDirection[0] = l.spotDirection.x;
				spotDirection[1] = l.spotDirection.y;
				spotDirection[2] = l.spotDirection.z;
				gl.glLightfv(lightIndex[i], GL2.GL_SPOT_DIRECTION, spotDirection, 0);
				gl.glLightf(lightIndex[i], GL2.GL_SPOT_EXPONENT, l.spotExponent);
				gl.glLightf(lightIndex[i], GL2.GL_SPOT_CUTOFF, l.spotCutoff);
			}

			float[] diffuse = new float[4];
			diffuse[0] = l.diffuse.x;
			diffuse[1] = l.diffuse.y;
			diffuse[2] = l.diffuse.z;
			diffuse[3] = 1.f;
			gl.glLightfv(lightIndex[i], GL2.GL_DIFFUSE, diffuse, 0);

			float[] ambient = new float[4];
			ambient[0] = l.ambient.x;
			ambient[1] = l.ambient.y;
			ambient[2] = l.ambient.z;
			ambient[3] = 0;
			gl.glLightfv(lightIndex[i], GL2.GL_AMBIENT, ambient, 0);

			float[] specular = new float[4];
			specular[0] = l.specular.x;
			specular[1] = l.specular.y;
			specular[2] = l.specular.z;
			specular[3] = 0;
			gl.glLightfv(lightIndex[i], GL2.GL_SPECULAR, specular, 0);
			
			i++;
		}
	}

	private void cleanMaterial(Material m)
	{
		if(m!=null && m.texture!=null)
		{
			gl.glDisable(GL2.GL_TEXTURE_2D);
		}
		if(m!=null && m.shader!=null)
		{
			m.shader.disable();
		}
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
