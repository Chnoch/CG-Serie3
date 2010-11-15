package jrtr;

import jrtr.RenderContext;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.*;
import java.util.LinkedList;
import javax.vecmath.*;


/**
 * A skeleton for a software renderer. It works in combination with
 * {@link SWRenderPanel}, which displays the output image. In project 3 
 * you will implement your own rasterizer in this class.
 * <p>
 * To use the software renderer, you will simply replace {@link GLRenderPanel} 
 * with {@link SWRenderPanel} in the user application.
 */
public class SWRenderContext implements RenderContext {

	private SceneManagerInterface sceneManager;
	private BufferedImage colorBuffer;
	private float[][] zBuffer;
		
	public void setSceneManager(SceneManagerInterface sceneManager)
	{
		this.sceneManager = sceneManager;	
	}
	
	/**
	 * This is called by the SWRenderPanel to render the scene to the 
	 * software frame buffer.
	 */
	public void display()
	{
		if(sceneManager == null) return;
		
		beginFrame();
	
		SceneManagerIterator iterator = sceneManager.iterator();	
		while(iterator.hasNext())
		{
			draw(iterator.next());
		}		
		
		endFrame();
	}

	/**
	 * This is called by the {@link SWJPanel} to obtain the color buffer that
	 * will be displayed.
	 */
	public BufferedImage getColorBuffer()
	{
		return colorBuffer;
	}
	
	/**
	 * Set a new viewport size. The render context will also need to store
	 * a viewport matrix, which you need to reset here. 
	 */
	public void setViewportSize(int width, int height)
	{
		colorBuffer = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		zBuffer = new float[width][height];		
	}
		
	/**
	 * Clear the framebuffer here.
	 */
	private void beginFrame()
	{
		colorBuffer.getGraphics().clearRect(0, 0, colorBuffer.getWidth(), colorBuffer.getHeight());
		zBuffer = new float[colorBuffer.getWidth()][colorBuffer.getHeight()];
	}
	
	private void endFrame()
	{		
	}
	
	/**
	 * The main rendering method. You will need to implement this to draw
	 * 3D objects.
	 */
	private void draw(RenderItem renderItem){		
		VertexData vertexData = renderItem.getShape().getVertexData();
		LinkedList<VertexData.VertexElement> elements = vertexData.getElements();
		int indices[] = vertexData.getIndices();
		float[] v = null, c = null, t=null;
		
		//get the vertex position and color array
		for(VertexData.VertexElement element : elements){
			if(element.getSemantic() == VertexData.Semantic.POSITION){
				v = element.getData();
			}
			if(element.getSemantic() == VertexData.Semantic.COLOR){
				c = element.getData();
			}
			if(element.getSemantic() == VertexData.Semantic.TEXCOORD){
				t = element.getData();
			}
		}
		//compute the world-to-pixel matrix
		Matrix4f world = new Matrix4f();
		Matrix4f camera = new Matrix4f();		
		Matrix4f projection = new Matrix4f();
		Matrix4f viewport = new Matrix4f();
		Matrix4f fullTransform = new Matrix4f();
		
		world.set(renderItem.getT());
		camera.set(sceneManager.getCamera().getCameraMatrix());		
		projection.set(sceneManager.getFrustum().getProjectionMatrix());		
		viewport.set(viewportMatrix(0, colorBuffer.getWidth(), 0, colorBuffer.getHeight()));
		
		fullTransform.setIdentity();
		fullTransform.mul(viewport);
		fullTransform.mul(projection);
		fullTransform.mul(camera);
		fullTransform.mul(world);
		
		
		for(int i = 0; i<indices.length; i+=3){
			
			int index1 = indices[i];
			int index2 = indices[i+1];
			int index3 = indices[i+2];
			
			Vector4f point1=new Vector4f(), point2=new Vector4f(), point3=new Vector4f();
			Vector4f transformedPoint1, transformedPoint2, transformedPoint3;
			
			//read vertex coordinates
			point1.x = v[index1*3];
			point1.y = v[index1*3+1];
			point1.z = v[index1*3+2];
			point1.w = 1;			
			transformedPoint1 = VectorUtil.matMulVec4f(fullTransform, point1);	
			point2.x = v[index2*3];
			point2.y = v[index2*3+1];
			point2.z = v[index2*3+2];
			point2.w = 1;			
			transformedPoint2 = VectorUtil.matMulVec4f(fullTransform, point2);	
			point3.x = v[index3*3];
			point3.y = v[index3*3+1];
			point3.z = v[index3*3+2];
			point3.w = 1;			
			transformedPoint3 = VectorUtil.matMulVec4f(fullTransform, point3);	
			
			//read colors
			Color color1 = new Color(c[index1*3], c[index1*3+1], c[index1*3+2]);
			Color color2 = new Color(c[index2*3], c[index2*3+1], c[index2*3+2]);
			Color color3 = new Color(c[index3*3], c[index3*3+1], c[index3*3+2]);
			
			//read texture coordinates		
			Point2f tex1 = new Point2f(t[index1*2], t[index1*2+1]);
			Point2f tex2 = new Point2f(t[index2*2], t[index2*2+1]);
			Point2f tex3 = new Point2f(t[index3*2], t[index3*2+1]);
			
			
			TriangleCalc triangle = new TriangleCalc(transformedPoint1, transformedPoint2, transformedPoint3, tex1, tex2, tex3, 
					renderItem.getShape().getMaterial().getTexture().getImage());			
			int[] box = createBoundingBox(transformedPoint1, transformedPoint2, transformedPoint3);			
			
			//check pixels if in triangle
			for(int x=box[0]; x<box[1];x++){
				for(int y=box[2]; y<box[3];y++){
					if(triangle.insideTriangle(x, y)){
						float w = triangle.interpolate1W(x, y);
						if(w > zBuffer[x][y]) {
							zBuffer[x][y] = w;
							try{
								if(x>colorBuffer.getWidth()/2){
									colorBuffer.setRGB(x, colorBuffer.getHeight()-1-y, (int) triangle.bilinearFiltering(x, y));	
								} else{
									colorBuffer.setRGB(x, colorBuffer.getHeight()-1-y, (int) triangle.nearestNeighbour(x, y));	
								}	
							} catch (Exception e){
							}
						}
					}
				}
			}			
		    //draw bounding boxes
		/*
			colorBuffer.getGraphics().drawRect(box[0], colorBuffer.getHeight()-1-box[3], 
					box[1]-box[0], (box[3]-box[2]));	
			*/
		}

	}

	/**
	 * Does nothing. We will not implement shaders for the software renderer.
	 */
	public Shader makeShader()	
	{
		return new SWShader();
	}

	/**
	 * Does nothing. We will not implement textures for the software renderer.
	 */
	public Texture makeTexture()
	{
		return new SWTexture();
	}
	
	private static Matrix4f viewportMatrix(int x0, int x1, int y0, int y1){
		return new Matrix4f((x1-x0)/2f, 0f, 0f, (x0+x1)/2f,
				0f, (y1-y0)/2f, 0f, (y0+y1)/2f,
				0f, 0f, 0.5f, 0.5f,
				0f, 0f, 0f, 1f);
	}
	
	private int[] createBoundingBox(Vector4f p1,Vector4f p2, Vector4f p3) {		
		final int SPACE = 1;  //makes the boxes a bit bigger to account for numeric errors
		int xmin=0, xmax=colorBuffer.getWidth(), ymin=0, ymax=colorBuffer.getHeight();
		float point1x = p1.x/p1.w;
		float point2x = p2.x/p2.w;
		float point3x = p3.x/p3.w;
		float point1y = p1.y/p1.w;
		float point2y = p2.y/p2.w;
		float point3y = p3.y/p3.w;		
		xmin = (int) Math.max(Math.min(Math.min(point1x, point2x), point3x)-SPACE, 0);
		xmax = (int) Math.min(Math.max(Math.max(point1x, point2x), point3x)+SPACE, colorBuffer.getWidth());
		ymin = (int) Math.max(Math.min(Math.min(point1y, point2y), point3y)-SPACE, 0);
		ymax = (int) Math.min(Math.max(Math.max(point1y, point2y), point3y)+SPACE, colorBuffer.getHeight());		
		return new int[] {xmin, xmax, ymin, ymax};
	}
	
	
	
}
