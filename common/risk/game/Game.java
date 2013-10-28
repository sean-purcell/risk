package risk.game;

import java.awt.Graphics2D;

import risk.lib.Input;
import risk.lib.Renderer;
import risk.lib.ThreadLocks;

/**
 * Represents the main game logic and loops
 * 
 * @author Gabriel Ip
 * @author Sean Purcell
 * @author Miranda Zhou
 *
 */
public class Game {
	
	private final int THREAD_ID = 1;
	
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
		map = new Map();
		i = new Input(this);
		r = new Renderer(this,i);
		
		running = true;
		int a = 1;
		if(a == 1);
	}

	// MAIN GAME LOOP AND RELATED MISC
	
	/**
	 * Begins the main game loop.
	 */
	public void run(){
		long lastTime = System.currentTimeMillis();
		while(running){
			if(r.hasFocus()){ //Ensures that the game does not render when it is not in focus
				
				//Calculate time since last update
				long time = System.currentTimeMillis();
				int delta = (int) (time - lastTime);

				ThreadLocks.requestLock(ThreadLocks.UPDATE,THREAD_ID);
				//Runs the update method with the given delta
				this.update(delta);

				//Renders the game
				r.repaint();

				ThreadLocks.releaseLock(ThreadLocks.UPDATE,THREAD_ID);
				//Limits the game to 30 fps
				while(System.currentTimeMillis() - time + delta <= 33);
			}
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
