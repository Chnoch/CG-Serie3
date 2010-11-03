package jrtr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Manages textures for the software renderer. Not implemented here.
 */
public class SWTexture implements Texture {

    public BufferedImage texture;
    
    public void load(String fileName) throws IOException {
	    File f = new File(fileName);
	    texture = ImageIO.read(f);
	}
}
