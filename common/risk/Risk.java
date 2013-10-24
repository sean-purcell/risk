package risk;

import java.util.Random;

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
	 * The random object that should be used for all PRNG purposes
	 */
	public static Random r;
	
	public static void main(String[] args){
		r = new Random(System.currentTimeMillis());
	}
}
