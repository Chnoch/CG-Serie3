

package jrtr;

import jrtr.RenderContext;

import java.awt.image.*;
import java.util.LinkedList;

import javax.vecmath.Matrix4f;
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
        LinkedList<VertexData.VertexElement> vertexElements = vertexData.getElements();
        int indices[] = vertexData.getIndices();
        
        // Don't draw if there are no indices
        if(indices == null) return;
        
        float x,y,z,w;
        Vector4f vec;
        Matrix4f mat, matVP, matCam;
        matVP = renderItem.getT();
        matCam = sceneManager.getCamera().getCameraMatrix();
//        matVP = sceneManager.getFrustum().getProjectionMatrix();
        mat = new Matrix4f();
        mat.set(matCam);
        mat.mul(matVP);
        for (int i=0; i< indices.length/3;i++){
            x = indices[i];
            y = indices[i++];
            z = indices[i++];
            w = 1;
            
            vec = new Vector4f(x,y,z,w);
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
