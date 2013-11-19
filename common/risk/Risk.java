package risk;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
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

	public static final boolean DEBUG = false;
	public static final boolean OUTPUT = true;

	/**
	 * The unique id used to hold a lock while loading resources
	 */
	public static final int THREAD_ID = 0x10;

	/**
	 * The Game object, representing the main game logic
	 */
	public static Game g;

	/**
	 * The random object that should be used for all PRNG purposes
	 */
	public static Random r;

	public static void main(String[] args) {
		if(!OUTPUT){
			System.setOut(new PrintStream(new OutputStream(){
				public void write(int b){}
				public void write(byte[] a,int i,int j){}
			}));
		}
		newGame();
	}

	public static void newGame(){
		ThreadLocks.requestLock(ThreadLocks.INIT_RESOURCES, 1);
		r = new Random(System.currentTimeMillis());
		g = new Game();
		ThreadLocks.releaseLock(ThreadLocks.INIT_RESOURCES, 1);

		g.start();
	}
	
	public static BufferedImage loadImage(String ref) {
		try {
			return ImageIO.read(Risk.class.getResourceAsStream("/" + ref));
		} catch (Exception e) {

			try {
				System.out.println("Get as stream didnt work");
				return ImageIO.read(new File("./" + ref));
			} catch (IOException e1) {
				e.printStackTrace();
				e1.printStackTrace();
				return null;
			}
		}
	}

	public static Properties loadProperties(String ref) {
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(ref));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			try {
				p.load(Risk.class.getResourceAsStream("/" + ref));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			try {
				p.load(Risk.class.getResourceAsStream("/" + ref));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		return p;
	}

	public static Font loadFont(String ref) {
		Font f = null;
		try {
			f = Font.createFont(Font.TRUETYPE_FONT, new File(ref));
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			try {
				f = Font.createFont(Font.TRUETYPE_FONT,
						Risk.class.getResourceAsStream("/" + ref));
			} catch (IOException e1) {

			} catch (FontFormatException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		return f;

	}

	/**
	 * Rotates a given list of type T by offset amount.
	 * 
	 * It shifts elements at the front repeatedly until it reaches the desired
	 * rotation.
	 * 
	 * @param list
	 * @param offset
	 */
	public static <T> List<T> rotateList(List<T> list, int offset) {
		for (int i = 0; i < offset; i++) {
			T front = list.remove(0);
			list.add(front);
		}
		return list;
	}
	
	public static int[] rotateArray(int[] arr, int offset){
		for(int i = 0; i < offset; i++){
			int front = arr[0];
			arr = shift(arr);
			arr[arr.length-1] = front;
		}
		return arr;
	}
	
	private static int[] shift(int[] arr){
		for(int i = 0; i < arr.length - 1; i++){
			arr[i] = arr[i+1];
		}
		return arr;
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
				if ((image.getRGB(x, y) & 0xFF000000) != 0x0 /*
															 * represents
															 * complete
															 * transparency
															 */)
					// System.out.println("(" + x + "," + y + ")" +
					// Integer.toString(image.getRGB(x, y),16));
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

	public static void sort(int[] arr) {
		Arrays.sort(arr);
		int[] sorted = new int[arr.length];
		for (int i = 0; i < arr.length; i++) {
			sorted[i] = arr[arr.length - 1 - i];
		}
		for (int i = 0; i < arr.length; i++) {
			arr[i] = sorted[i];
		}
	}
}
