package jrtr;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;


public class VectorUtil {
	public static Vector4f matMulVec4f(Matrix4f mat, Vector4f vec){
		Vector4f v = new Vector4f();
		v.x = mat.m00*vec.x+mat.m01*vec.y+mat.m02*vec.z+mat.m03*vec.w;
		v.y = mat.m10*vec.x+mat.m11*vec.y+mat.m12*vec.z+mat.m13*vec.w;
		v.z = mat.m20*vec.x+mat.m21*vec.y+mat.m22*vec.z+mat.m23*vec.w;
		v.w = mat.m30*vec.x+mat.m31*vec.y+mat.m32*vec.z+mat.m33*vec.w;
		return v;
	}
	public static Vector3f matMulVec3f(Matrix3f mat, Vector3f vec){
		Vector3f v = new Vector3f();
		v.x = mat.m00*vec.x+mat.m01*vec.y+mat.m02*vec.z;
		v.y = mat.m10*vec.x+mat.m11*vec.y+mat.m12*vec.z;
		v.z = mat.m20*vec.x+mat.m21*vec.y+mat.m22*vec.z;		
		return v;
	}
}
