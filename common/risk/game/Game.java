package risk.game;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

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
	
	private final int THREAD_ID = 1;
	
	private Renderer r;
	private Input i;
	
	private Map map;
	
	/**
	 * Represents the current mode that the game is in<br>
	 * 1. Game setup mode<br>
	 * 2. Main game mode<br>
	 */
	private int mode;
	
	private int turn;
	
	private List<Army> armies;
	
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
		this.armies = new ArrayList<Army>();
		mode = 1;
		running = true;
		fabricateArmies();
		int a = 1;
		if(a == 1);
	}

	private void fabricateArmies(){
		armies.add(new Army(0));
		armies.add(new Army(1));
		armies.add(new Army(2));
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
				lastTime = time;
				try{
					setFps(1000/delta);
				}
				catch(ArithmeticException e){}
				//ThreadLocks.requestLock(ThreadLocks.UPDATE,THREAD_ID);
				//Runs the update method with the given delta
				this.update(delta);

				//Renders the game
				r.repaint();
				
				//ThreadLocks.releaseLock(ThreadLocks.UPDATE,THREAD_ID);
				//Limits the game to 30 fps
				while(System.currentTimeMillis() - time <= 32);
			}
		}
	}
	
	private void update(int delta){
		switch(mode){
		case 1: updateSetupMode(); break;
		}
	}
	
	private void updateSetupMode(){
		
	}
	
	public void draw(Graphics2D g){
		switch(mode){
		case 1: r.drawSetupMode(g); break;
		case 2:r.drawMainMode(g); break;
		}
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

	public int getFps() {
		return fps;
	}

	public void setFps(int fps) {
		this.fps = fps;
	}

	public List<Army> getArmies() {
		return armies;
	}

	public Army getCurrentArmy(){
		return armies.get(turn);
	}
	
	public void setArmies(List<Army> armies) {
		this.armies = armies;
	}

	public int getTurn() {
		return turn;
	}

	public void setTurn(int turn) {
		this.turn = turn;
	}
}
