package jrtr;

import jrtr.RenderContext;

import java.awt.Color;
import java.awt.image.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.vecmath.Color3f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.SingularMatrixException;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector4f;

/**
 * A skeleton for a software renderer. It works in combination with
 * {@link SWRenderPanel}, which displays the output image. In project 3 you will
 * implement your own rasterizer in this class.
 * <p>
 * To use the software renderer, you will simply replace {@link GLRenderPanel}
 * with {@link SWRenderPanel} in the user application.
 */
public class SWRenderContext implements RenderContext {

	private SceneManagerInterface sceneManager;
	private BufferedImage colorBuffer, texture;
	private Matrix4f matVP;
	private List<Vector4f> edges;
	private List<Color3f> colors;
	private List<TexCoord2f> texCoords;
	private int aWidth, aHeight;
	private float[][] zBuffer;
	private RenderItem renderItem;

	public void setSceneManager(SceneManagerInterface sceneManager) {
		this.sceneManager = sceneManager;
	}

	/**
	 * This is called by the SWRenderPanel to render the scene to the software
	 * frame buffer.
	 */
	public void display() {
		if (sceneManager == null)
			return;

		beginFrame();

		SceneManagerIterator iterator = sceneManager.iterator();
		while (iterator.hasNext()) {
			draw(iterator.next());
		}

		endFrame();
	}

	/**
	 * This is called by the {@link SWJPanel} to obtain the color buffer that
	 * will be displayed.
	 */
	public BufferedImage getColorBuffer() {
		return colorBuffer;
	}

	/**
	 * Set a new viewport size. The render context will also need to store a
	 * viewport matrix, which you need to reset here.
	 */
	public void setViewportSize(int width, int height) {
		this.aHeight = height;
		this.aWidth = width;
		// reset the viewport matrix
		matVP = new Matrix4f();
		matVP.setM00(width / 2);
		matVP.setM03((width - 1) / 2);
		matVP.setM11(height / 2);
		matVP.setM13((height - 1) / 2);
		matVP.setM22(1);
		matVP.setM33(1);

		colorBuffer = new BufferedImage(width, height,
				BufferedImage.TYPE_3BYTE_BGR);
	}

	/**
	 * Clear the framebuffer here.
	 */
	private void beginFrame() {
	}

	private void endFrame() {
	}

	/**
	 * The main rendering method. You will need to implement this to draw 3D
	 * objects.
	 */
	private void draw(RenderItem renderItem) {
		this.renderItem = renderItem;
		this.edges = new ArrayList<Vector4f>();
		this.colors = new ArrayList<Color3f>();
		this.texCoords = new ArrayList<TexCoord2f>();
		VertexData vertexData = renderItem.getShape().getVertexData();
		zBuffer = new float[aWidth][aHeight];
		for (int i = 0; i < aWidth; i++) {
			for (int j = 0; j < aHeight; j++) {
				zBuffer[i][j] = Float.MAX_VALUE;
			}
		}

		projection(vertexData);
		rasterization(renderItem, vertexData);
		textures(renderItem);

		System.out.println("Done");
	}

	private void textures(RenderItem item) {
		// Preparing texture
		Texture tex = item.getShape().getMaterial().getTexture();
		if (tex instanceof SWTexture) {
			texture = ((SWTexture) tex).getTexture();
		}
	}

	private void rasterization(RenderItem item, VertexData data) {

		for (int i = 0; i < edges.size(); i++) {

			Color3f aCol = this.colors.get(i);
			// TexCoord2f aTex = this.texCoords.get(i);
			Vector4f a = this.edges.get(i++);
			Color3f bCol = this.colors.get(i);
			// TexCoord2f bTex = this.texCoords.get(i);
			Vector4f b = this.edges.get(i++);
			Color3f cCol = this.colors.get(i);
			// TexCoord2f cTex = this.texCoords.get(i);
			Vector4f c = this.edges.get(i);

			// calculate edge functions
			Matrix3f coefficients;
			coefficients = new Matrix3f();
			coefficients.setM00(a.getX());
			coefficients.setM01(a.getY());
			coefficients.setM02(a.getW());
			coefficients.setM10(b.getX());
			coefficients.setM11(b.getY());
			coefficients.setM12(b.getW());
			coefficients.setM20(c.getX());
			coefficients.setM21(c.getY());
			coefficients.setM22(c.getW());

			boolean singular = false;

			try {
				coefficients.invert();
			} catch (SingularMatrixException exc) {
				singular = true;
			}

			if (!singular) {

				if (checkConditions(a, b, c)) {

					System.out.println("Calculating a bounding box");
					// calculate bounding box
					int xLeft, yTop, xRight, yBottom;

					xLeft = min(a.getX(), b.getX(), c.getX());
					yTop = max(a.getY(), b.getY(), c.getY());
					xRight = max(a.getX(), b.getX(), c.getX());
					yBottom = min(a.getY(), b.getY(), c.getY());

					if (i > 37) {

						drawing(coefficients, xLeft, xRight, yTop, yBottom, a,
								b, c);
					} else {
						drawing(coefficients, xLeft, xRight, yTop, yBottom, a,
								b, c, aCol);
					}

				} else {
					drawing(coefficients, 0, aWidth, aHeight, 0, a, b, c, aCol);
					System.out.println("Not all vertices in view");
				}
			}
		}
	}

	private boolean checkConditions(Vector4f a, Vector4f b, Vector4f c) {
		return a.getX() > 0 && b.getX() > 0 && c.getX() > 0 && a.getY() > 0
				&& b.getY() > 0 && c.getY() > 0;
	}

	private void drawing(Matrix3f coefficients, int xLeft, int xRight,
			int yTop, int yBottom, Vector4f a, Vector4f b, Vector4f c,
			Color3f col) {
		for (int j = xLeft; j < xRight; j++) {
			for (int k = yTop; k > yBottom; k--) {
				// Interpolate w
				float Z_Slope = (float) (1 / b.z - 1 / a.z)
						/ (float) (b.x - a.x);
				float z = a.z + ((j - a.x) * Z_Slope);
				// calculate whether to paint
				float alpha = coefficients.m00 * j / z + coefficients.m10 * k
						/ z + coefficients.m20;
				float beta = coefficients.m01 * j / z + coefficients.m11 * k
						/ z + coefficients.m21;
				float gamma = coefficients.m02 * j / z + coefficients.m12 * k
						/ z + coefficients.m22;

				if (alpha > 0 && beta > 0 && gamma > 0) {
					drawPixel((int) (j / z), (int) (k / z), 1 / z, col);
				}
			}
		}
	}

	private void drawing(Matrix3f coefficients, int xLeft, int xRight,
			int yTop, int yBottom, Vector4f a, Vector4f b, Vector4f c) {

		Texture tex = this.renderItem.getShape().getMaterial().getTexture();
		BufferedImage im = null;
		if (tex instanceof SWTexture) {
			im = ((SWTexture) tex).getTexture();
		}

		for (int j = xLeft; j < xRight; j++) {
			for (int k = yTop; k > yBottom; k--) {
				// Interpolate w
				float Z_Slope = (float) (1 / b.z - 1 / a.z)
						/ (float) (b.x - a.x);
				float z = a.z + ((j - a.x) * Z_Slope);
				// calculate whether to paint
				float alpha = coefficients.m00 * j / z + coefficients.m10 * k
						/ z + coefficients.m20;
				float beta = coefficients.m01 * j / z + coefficients.m11 * k
						/ z + coefficients.m21;
				float gamma = coefficients.m02 * j / z + coefficients.m12 * k
						/ z + coefficients.m22;

				if (alpha > 0 && beta > 0 && gamma > 0) {

					try {
						int col = im.getRGB((int) (j / z), (int) (k / z));
						drawPixel((int) (j / z), (int) (k / z), 1 / z, col);
					} catch (ArrayIndexOutOfBoundsException exc) {
						System.out.println("catch");
						int col = im.getRGB(0,0);
						drawPixel((int) (j / z), (int) (k / z), 1 / z, col);
					}

				}
			}
		}
	}

	/**
	 * 3D to 2D Projection
	 * 
	 * @param vertexData
	 */
	private void projection(VertexData vertexData) {
		LinkedList<VertexData.VertexElement> vertexElements = vertexData
				.getElements();

		int indices[] = vertexData.getIndices();

		// Don't draw if there are no indices
		if (indices == null)
			return;

		float x, y, z, w;
		Vector4f vec;
		Matrix4f mat, matPro, matCam;
		matCam = sceneManager.getCamera().getCameraMatrix();
		matPro = sceneManager.getFrustum().getProjectionMatrix();
		mat = new Matrix4f();
		mat.set(matVP);
		mat.mul(matPro);
		mat.mul(matCam);

		for (int j = 0; j < indices.length; j++) {
			int i = indices[j];
			ListIterator<VertexData.VertexElement> itr = vertexElements
					.listIterator(0);
			while (itr.hasNext()) {
				VertexData.VertexElement e = itr.next();

				if (e.getSemantic() == VertexData.Semantic.POSITION) {
					x = e.getData()[i * 3];
					y = e.getData()[i * 3 + 1];
					z = e.getData()[i * 3 + 2];
					w = 1;
					vec = new Vector4f(x, y, z, w);
					mat.transform(vec);

					// Add as homogenous coordinate before homogenous division
					this.edges.add(new Vector4f(vec));

				} else if (e.getSemantic() == VertexData.Semantic.COLOR) {
					Color3f col = new Color3f(e.getData()[i * 3],
							e.getData()[i * 3 + 1], e.getData()[i * 3 + 2]);
					this.colors.add(col);
				} else if (e.getSemantic() == VertexData.Semantic.NORMAL) {

				} else if (e.getSemantic() == VertexData.Semantic.TEXCOORD) {
					System.out.println("TexCoord");
					TexCoord2f tex = new TexCoord2f(e.getData()[i * 2], e
							.getData()[i * 2 + 1]);
					this.texCoords.add(tex);
				}

			}
		}
	}

	private void drawPixel(int x, int y, float z, Color3f col) {
		if (this.zBuffer[x][y] > z) {
			this.zBuffer[x][y] = z;
			try {
				int color = (int) (255f * col.x) << 16
						| (int) (255f * col.y) << 8 | (int) (255f * col.z);
				colorBuffer.setRGB(x, aHeight - y, color);
			} catch (ArrayIndexOutOfBoundsException exc) {
				System.out.println("Error at pixel: x=" + x + " y=" + y);
			}
		}
	}

	private void drawPixel(int x, int y, float z, int col) {
		if (this.zBuffer[x][y] > z) {
			this.zBuffer[x][y] = z;
			try {
				// int color = (int)(255f*col.x)<<16 | (int)(255f*col.y)<<8 |
				// (int)(255f*col.z);
				colorBuffer.setRGB(x, aHeight - y, col);
			} catch (ArrayIndexOutOfBoundsException exc) {
				System.out.println("Error at pixel: x=" + x + " y=" + y);
			}
		}
	}

	private int min(float a, float b, float c) {
		if (a < b) {
			if (a < c) {
				return (int) a;
			}
		} else {
			if (b < c) {
				return (int) b;
			}
		}
		return (int) c;
	}

	private int max(float a, float b, float c) {
		if (a > b) {
			if (a > c) {
				return (int) a;
			}
		} else {
			if (b > c) {
				return (int) b;
			}
		}
		return (int) c;
	}

	/**
	 * Does nothing. We will not implement shaders for the software renderer.
	 */
	public Shader makeShader() {
		return new SWShader();
	}

	/**
	 * Does nothing. We will not implement textures for the software renderer.
	 */
	public Texture makeTexture() {
		return new SWTexture();
	}
}
