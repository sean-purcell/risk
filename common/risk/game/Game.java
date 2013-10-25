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
	
	public Game(){
		i = new Input(this);
		r = new Renderer(this,i);
		r.repaint();
	}
	
	public void draw(Graphics2D g){
		
	}

	public Map getMap() {
		return map;
	}

	public void setMap(Map map) {
		this.map = map;
	}
}
