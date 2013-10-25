package risk;

import java.util.Random;

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
}
