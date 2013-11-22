package risk;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.imageio.ImageIO;

import risk.game.Game;
import risk.lib.RiskThread;
import risk.lib.ThreadLocks;

/**
 * The main class, the game entry point
 * Also general library class
 * 
 * @author Gabriel Ip
 * @author Sean Purcell
 * @author Miranda Zhou
 * 
 */
public class Risk {

	public static final boolean DEBUG = false;
	public static final boolean OUTPUT = true;
	public static final boolean DRAW_WHILE_INACTIVE = true;

	private static List<RiskThread> threadPool; 
	
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
		
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run(){
				System.out.println("Running gc and stopping threads");
				for(RiskThread t : threadPool){
					t.halt();
				}
				System.gc();
			}
		});
		
		threadPool = Collections.synchronizedList(new ArrayList<RiskThread>());
		
		newGame();
	}

	//RiskThreads by default run this on construction
	public static void addThread(RiskThread t){
		threadPool.add(t);
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
	
	public static int[] rotateArray(int[] arr, int offset, int len){
		for(int i = 0; i < offset; i++){
			int front = arr[0];
			arr = shift(arr);
			arr[len-1] = front;
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
	
	public static String read(BufferedReader i){
		StringBuffer s = new StringBuffer();
		try{
			int a = 0;
			while((a = i.read()) != '|' && a != -1){
				s.append((char) a);
			}
		}
		catch(IOException e){
		}
		return s.toString();
	}
	
	public static void showMessage(String message){
		System.out.println(message);
		for(int i = 0; i < message.length(); i++){
			System.out.print(Integer.toString((int)(message.charAt(i)),16) + "-");
		}
		System.out.println();
	}
	
	
	public static String serializeIntArray(int[] a){
		String arr = "";
		for(int i = 0; i < a.length; i++){
			arr += int16ToString(a[i]);
		}
		return arr;
	}
	
	public static String int16ToString(int i){
		String output = Integer.toString(i,16);
		while(output.length() < 8) output = "0" + output;
		return output;
	}
	
	public static int[] deserializeIntArray(String arr){
		int length = arr.length() / 8;
		int[] a = new int[length];
		for(int i = 0; i < length; i++){
			a[i] = Integer.parseInt(arr.substring(i * 8, i * 8 + 8), 16);
		}
		return a;
	}
}
