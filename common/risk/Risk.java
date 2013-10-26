package risk;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import risk.game.Game;

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
	
	public static void main(String[] args){
		r = new Random(System.currentTimeMillis());
		g = new Game();
	}
	
	public static BufferedImage loadImage(String ref){
		try{
			return ImageIO.read(new File(ref));
		}
		catch(IOException e){
			System.err.println("ERROR: Image at " + ref + " not found.");
			return null;
		}
	}
}
