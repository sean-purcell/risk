package risk.game;

import java.awt.Graphics2D;

import risk.lib.Input;
import risk.lib.Renderer;

/**
 * Represents the main game logic and loops
 * 
 * @author Gabriel Ip
 * @author Sean Purcell
 * @author Miranda Zhou
 *
 */
public class Game {
	
	private Renderer r;
	private Input i;
	
	private Map map;
	
	/**
	 * Represents the current mode that the game is in
	 */
	private int mode;
	
	private int turn;
	
	/**
	 * Set to false if the game should exit
	 */
	private boolean running;
	
	/**
	 * Represents the current speed of the game in frames per second
	 */
	private int fps;
	
	/**
	 * Represents desired fps
	 */
	private int fpsDesired = 60;
	
	public Game(){
		i = new Input(this);
		r = new Renderer(this,i);
		map = new Map();
		running = true;
		r.repaint();
	}

	// MAIN GAME LOOP AND RELATED MISC
	
	/**
	 * Begins the main game loop.
	 */
	public void run(){
		long lastTime = System.currentTimeMillis();
		while(running){
			//Calculate time since last update
			long time = System.currentTimeMillis();
			int delta = (int) (time - lastTime);
			
			//Runs the update method with the given delta
			this.update(delta);
			
			//Renders the game
			r.repaint();
			
			//Limits the game to 30 fps
			while(System.currentTimeMillis() - time + delta <= 33);
		}
	}
	
	private void update(int delta){
		
	}
	
	public void draw(Graphics2D g){
		r.drawMainMode(g);
	}

	// INPUT HANDLING
	
	public void mouseClicked(int x,int y, int mouseButton){
		
	}
	
	
	
	// SETTERS AND GETTERS
	
	public Map getMap() {
		return map;
	}

	public void setMap(Map map) {
		this.map = map;
	}
}
