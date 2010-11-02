

package jrtr;

import jrtr.RenderContext;

import java.awt.Color;
import java.awt.image.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.SingularMatrixException;
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
    private BufferedImage colorBuffer;
    private Matrix4f matVP;
    private List<Vector4f> edges;
    private int aWidth, aHeight;
    private int[][] zBuffer;

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
        this.edges = new ArrayList<Vector4f>();
        VertexData vertexData = renderItem.getShape().getVertexData();
        zBuffer = new int[aWidth][aHeight];
        for (int i=0;i<aWidth;i++){
            for (int j=0;j<aHeight;j++) {
                zBuffer[i][j] = Integer.MAX_VALUE;
            }
        }
        

        projection(vertexData);
        rasterization(vertexData);
    }

    private void rasterization(VertexData data) {

        for (int i = 0; i < edges.size(); i++) {
            // create rectangles to compute pixels
            Vector4f a = this.edges.get(i++);
            Vector4f b = this.edges.get(i++);
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
                // coefficients.invert();
                coefficients.invert(coefficients);
            } catch (SingularMatrixException exc) {
                singular = true;
            }

            if (!singular && coefficients.determinant() > 0.1) {

                // calculate bounding box
                int tlX, tlY, trX, trY, blX, blY, brX, brY;

                tlX = min(a.getX(), b.getX(), c.getX());
                tlY = max(a.getY(), b.getY(), c.getY());
                trX = max(a.getX(), b.getX(), c.getX());
                trY = min(a.getY(), b.getY(), c.getY());
                blX = min(a.getX(), b.getX(), c.getX());
                blY = min(a.getY(), b.getY(), c.getY());
                brX = max(a.getX(), b.getX(), c.getX());
                brY = max(a.getY(), b.getY(), c.getY());

                for (int j = tlX; j < trX; j++) {
                    for (int k = tlY; k > blY; k--) {
                        // calculate whether to paint
                        float aw = coefficients.m00 * j + coefficients.m10 * k
                                + coefficients.m20;
                        System.out.println(aw);
                        float bw = coefficients.m01 * j + coefficients.m11 * k
                                + coefficients.m21;
                        System.out.println(bw);
                        float cw = coefficients.m02 * j + coefficients.m12 * k
                                + coefficients.m22;
                        System.out.println(cw + "\n");

                        if (aw > 0 && bw > 0 && cw > 0) {
                            drawPixel(j, k, );
                        }

                    }
                }
            }
        }
    }

    /**
     * 3D to 2D Projection
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
                    this.edges.add(vec);

                    vec.setX(vec.getX() / vec.getW());
                    vec.setY(vec.getY() / vec.getW());

                    if (vec.getX() > 0 && vec.getY() > 0
                            && vec.getX() < this.aWidth
                            && vec.getY() < this.aHeight) {
                        drawPixel((int) vec.getX(), (int) vec.getY(), vec.getZ());
                    }

                }

            }
        }
    }

    private void drawPixel(int x, int y, float z) {
        
        colorBuffer.setRGB(x, aHeight - y, Color.white.getRGB());
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
