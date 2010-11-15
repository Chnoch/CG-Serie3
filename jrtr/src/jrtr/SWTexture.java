package jrtr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Manages textures for the software renderer. Not implemented here.
 */
public class SWTexture implements Texture {	
	
	BufferedImage image;
	
	public void load(String fileName) throws IOException {		
		File f = new File(fileName);
		image = ImageIO.read(f);
	}
	
	public BufferedImage getImage(){
		return this.image;
	}

}
