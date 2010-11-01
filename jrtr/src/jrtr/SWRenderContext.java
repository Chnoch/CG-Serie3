

package jrtr;

import jrtr.RenderContext;

import java.awt.Color;
import java.awt.image.*;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.media.opengl.GL;
import javax.vecmath.Matrix4f;
import javax.vecmath.Tuple4f;
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
    private int aWidth, aHeight;

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
        VertexData vertexData = renderItem.getShape().getVertexData();
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

                    vec.setX(vec.getX() / vec.getW());
                    vec.setY(vec.getY() / vec.getW());
                    
                    if (vec.getX() > 0 && vec.getY() > 0
                            && vec.getX() < this.aWidth
                            && vec.getY() < this.aHeight) {
                        colorBuffer.setRGB((int) vec.getX(), this.aHeight - (int) vec.getY(),
                                Color.WHITE.getRGB());
                    }
                }

            }
        }
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
