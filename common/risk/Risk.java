package risk;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import risk.game.Game;
import risk.lib.ThreadLocks;

/**
 * The main class, the game entry point
 * 
 * @author Gabriel Ip
 * @author Sean Purcell
 * @author Miranda Zhou
 * 
 */
public class Risk {

	/**
	 * The Game object, representing the main game logic
	 */
	public static Game g;

	/**
	 * The random object that should be used for all PRNG purposes
	 */
	public static Random r;

	public static void main(String[] args) {
		ThreadLocks.requestLock(ThreadLocks.INIT_RESOURCES, 1);
		r = new Random(System.currentTimeMillis());
		g = new Game();
		ThreadLocks.releaseLock(ThreadLocks.INIT_RESOURCES, 1);
		g.run();
	}

	public static BufferedImage loadImage(String ref) {
		try {
			return ImageIO.read(new File(ref));
		} catch (IOException e) {
			System.err.println("ERROR: Image at " + ref + " not found.");
			return null;
		}
	}

	/**
	 * Rotates a given list of type T by offset amount.
	 * 
	 * It shifts elements at the front repeatedly until it reaches the desired rotation.
	 * @param list
	 * @param offset
	 */
	public static <T> void rotateList(List<T> list, int offset) {
		for (int i = 0; i < offset; i++) {
			T front = list.remove(0);
			list.add(front);
		}
	}

	public static BufferedImage cloneImage(BufferedImage original) {
		ColorModel cm = original.getColorModel();
		WritableRaster raster = (WritableRaster) original.getData();
		boolean isAlphaPremultiplied = original.isAlphaPremultiplied();
		BufferedImage image = new BufferedImage(cm, raster,
				isAlphaPremultiplied, null);
		return image;
	}

	// IMAGE OPERATIONS: NOTE: THESE SHOULD BE DONE ONCE AND THEN CACHED, THEY
	// ARE EXPENSIVE OPERATIONS AND IT IS WORTH THE MEMORY TO STORE THEM TWICE
	// RATHER THAN DO THEM 100 TIMES PER SECOND
	/**
	 * Produces an image where all non transparent pixels in the image are
	 * converted to the given color
	 * 
	 * @param original
	 * @param c
	 * @return
	 */
	public static BufferedImage changeImageColour(BufferedImage original,
			Color c) {
		BufferedImage image = cloneImage(original);
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				if (image.getRGB(x, y) % 0x01000000 == 0 /*
														 * represents complete
														 * transparency
														 */)
					image.setRGB(x, y, c.getRGB());
			}
		}
		return image;
	}

	public static BufferedImage flipImage(BufferedImage original,
			boolean flipHorizontal, boolean flipVertical) {
		BufferedImage image = new BufferedImage(original.getWidth(),
				original.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				int ox = flipHorizontal ? image.getWidth() - 1 - x : x;
				int oy = flipVertical ? image.getHeight() - 1 - y : y;
				int origColour = original.getRGB(ox, oy);
				image.setRGB(x, y, origColour);
			}
		}
		return image;
	}
}
