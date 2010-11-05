

package jrtr;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Manages textures for the software renderer. Not implemented here.
 */
public class SWTexture implements Texture {

    public BufferedImage texture;

    public BufferedImage getTexture() {
        return texture;
    }

    public void setTexture(BufferedImage texture) {
        this.texture = texture;
    }

    public void load(String fileName) throws IOException {
        File f = new File(fileName);
        BufferedImage im = ImageIO.read(f);
        texture = im;
//        texture = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

        // Paint scaled version of image to new image
//        Graphics2D graphics2D = texture.createGraphics();
//        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
//                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//        graphics2D.drawImage(im, 0, 0, im.getWidth(), im.getHeight(), null);
//
//        graphics2D.dispose();
    }
}
