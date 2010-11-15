package jrtr;

import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.vecmath.Matrix3f;
import javax.vecmath.Point2f;
import javax.vecmath.SingularMatrixException;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

public class TriangleCalc {

	private Matrix3f coefficients, coefficientsTrans;
	private boolean singular = false;
	private Color color1, color2, color3;
	private Point2f tex1, tex2, tex3;
	boolean textured;
	BufferedImage texture;

	public TriangleCalc(Vector4f v1, Vector4f v2, Vector4f v3, Color color1,
			Color color2, Color color3) {
		init(v1, v2, v3);
		this.color1 = color1;
		this.color2 = color2;
		this.color3 = color3;
		textured = false;
	}

	public TriangleCalc(Vector4f v1, Vector4f v2, Vector4f v3, Point2f tex1,
			Point2f tex2, Point2f tex3, BufferedImage tex) {
		init(v1, v2, v3);
		this.tex1 = tex1;
		this.tex2 = tex2;
		this.tex3 = tex3;
		texture = tex;
		textured = true;
	}

	private void init(Vector4f v1, Vector4f v2, Vector4f v3) {
		coefficients = new Matrix3f();
		coefficients.setRow(0, v1.x, v1.y, v1.w);
		coefficients.setRow(1, v2.x, v2.y, v2.w);
		coefficients.setRow(2, v3.x, v3.y, v3.w);
		// check for singularity
		try {
			coefficients.invert();
		} catch (SingularMatrixException e) {
			singular = true;
		}
		coefficientsTrans = (Matrix3f) coefficients.clone();
		coefficientsTrans.transpose();
	}
	
	public int nearestNeighbour(int pixel_Xw, int pixel_Yw) {
		float w_inverse = interpolate1W(pixel_Xw, pixel_Yw);

		Vector3f u = VectorUtil.matMulVec3f(coefficients, new Vector3f(tex1.x, tex2.x, tex3.x));
		float interpolU = (u.x*pixel_Xw+u.y*pixel_Yw+u.z)/w_inverse;
		
		Vector3f v = VectorUtil.matMulVec3f(coefficients, new Vector3f(tex1.y, tex2.y, tex3.y));
		float interpolV = (v.x*pixel_Xw+v.y*pixel_Yw+v.z)/w_inverse;
		
		return texture.getRGB(Math.round(interpolU*texture.getWidth()), Math.round(interpolV*texture.getHeight()));
	}

	public int bilinearFiltering(int pixel_Xw, int pixel_Yw) {
		float w_inverse = interpolate1W(pixel_Xw, pixel_Yw);

		Vector3f u = VectorUtil.matMulVec3f(coefficients, new Vector3f(tex1.x, tex2.x, tex3.x));
		float interpolU = (u.x*pixel_Xw+u.y*pixel_Yw+u.z)/w_inverse;
		
		Vector3f v = VectorUtil.matMulVec3f(coefficients, new Vector3f(tex1.y, tex2.y, tex3.y));
		float interpolV = (v.x*pixel_Xw+v.y*pixel_Yw+v.z)/w_inverse;
		
		int u_0 = (int) Math.floor(interpolU*texture.getWidth());
		int u_1 = (int) Math.ceil(interpolU*texture.getWidth());
		float w_u = (interpolU*texture.getWidth()-u_0)/(u_1-u_0); //horizontal interpolation	
		
		int v_0 = (int) Math.floor(interpolV*texture.getHeight());
		int v_1 = (int) Math.ceil(interpolV*texture.getHeight());
		float w_v = (interpolV*texture.getHeight()-v_0)/(v_1-v_0); //vertical interpolation	
	
		Color col1 = new Color(texture.getRGB(u_0, v_0));
		Color col2 = new Color(texture.getRGB(u_1, v_0));
		
		Color col3 = new Color(texture.getRGB(u_0, v_1));
		Color col4 = new Color(texture.getRGB(u_1, v_1));
		
		Color c_b = filterColor(col1, col2, w_u);
		Color c_t = filterColor(col3, col4, w_u);
		
		Color c = filterColor(c_b, c_t, w_v);
		
		return c.getRGB();
	}
	
	
	private Color filterColor(Color col1, Color col2, float weight) {		
		int red = (int) (col1.getRed()*(1-weight)+col2.getRed()*weight);
		int green = (int) (col1.getGreen()*(1-weight)+col2.getGreen()*weight);
		int blue = (int) (col1.getBlue()*(1-weight)+col2.getBlue()*weight);		
		return new Color(red, green, blue);
	}

	public int interpolateColor(int pixel_Xw, int pixel_Yw) {
		float w_inverse = interpolate1W(pixel_Xw, pixel_Yw);

		Vector3f red = VectorUtil.matMulVec3f(coefficients, new Vector3f(color1.getRed(), color2.getRed(), color3.getRed()));
		int redAmount = (int) ((red.x*pixel_Xw+red.y*pixel_Yw+red.z)/w_inverse);

		Vector3f green = VectorUtil.matMulVec3f(coefficients, new Vector3f(color1.getGreen(), color2.getGreen(), color3.getGreen()));
		int greenAmount = (int) ((green.x*pixel_Xw+green.y*pixel_Yw+green.z)/w_inverse);

		Vector3f blue = VectorUtil.matMulVec3f(coefficients, new Vector3f(color1.getBlue(), color2.getBlue(), color3.getBlue()));
		int blueAmount = (int) ((blue.x*pixel_Xw+blue.y*pixel_Yw+blue.z)/w_inverse);

		return new Color(redAmount, greenAmount, blueAmount).getRGB();
	}

	// checks if the pixel is in the triangle of this function
	public boolean insideTriangle(int pixel_Xw, int pixel_Yw) {		
		Vector3f result = VectorUtil.matMulVec3f(coefficientsTrans, new Vector3f(pixel_Xw, pixel_Yw, 1));		
		return (result.x>0&&result.y>0&&result.z>0&&!singular&&!(coefficients.determinant()<0));
	}

	// interpolates 1/w from pixel coordinates, also used for z-buffer
	public float interpolate1W(int pixel_Xw, int pixel_Yw) {
		Vector3f result = VectorUtil.matMulVec3f(coefficients, new Vector3f(1, 1, 1));
		return result.x*pixel_Xw+result.y*pixel_Yw+result.z;
	}

}
