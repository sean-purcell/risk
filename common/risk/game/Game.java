package risk.game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import risk.lib.Button;
import risk.lib.Drawable;
import risk.lib.Input;
import risk.lib.RiskCanvas;

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
	
	private RiskCanvas r;
	private Input i;
	
	private Map map;
	
	/**
	 * Represents the current mode that the game is in<br>
	 * 1. Game setup mode<br>
	 * 2. Main game mode<br>
	 */
	private int mode;
	
	/**
	 * Represents the current part of setup<br>
	 * 1. Choose number of players<br>
	 * 2. Choose colours<br>
	 * 3. Roll dice<br>
	 * 4. Choose territories<br>
	 * 5. Deploy reinforcements
	 */
	private int setupMode;
	
	private List<Button> numberButtons;
	
	private int numPlayers;
	private int numAI;
	
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
		r = new RiskCanvas(this,i);
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
		switch(setupMode){
		case 0: enterSetupMode();
		}
	}
	
	private void enterSetupMode(){
		setupMode = 1;
		List<BufferedImage> numberButtonTextures = r.generateNumberButtonTextures();
		numberButtons = new ArrayList<Button>();
		for(int i = 0; i < 6; i++){
 		 	numberButtons.add(new Button(50 + 55 * i,650,numberButtonTextures.get(i)));
		}
		System.out.println("number buttons initialized");
	}
	
	public void draw(Graphics2D g){
		drawMap(g);
		switch(mode){
		case 1: drawSetupMode(g); break;
		case 2: /*drawMainMode(g); */break;
		}
	}
	
	private void drawSetupMode(Graphics2D g){
		drawString(g,"Number of players: ",25,645,30,Color.RED);
		drawButtons(g);
	}
	
	public void drawMap(Graphics2D g){
		try{
			g.drawImage(this.getMap().getTexture(),0,0,null);
		}
		catch(NullPointerException e){
			System.out.println("map not found");
		}
	}
	
	public void drawArmyInfo(Graphics2D g){
		
		//g.drawString("ARMY FONT",100,650);
		Army a = this.getCurrentArmy();
		g.setColor(a.getColour());
		g.drawString(a.getName(),50,650);
	}
	
	public void drawReinforcements(Graphics2D g){
		Army a = this.getCurrentArmy();
		setFontSize(g, 20);
		g.drawString("Free Troops: " + a.getFreeUnits(),60,680);
	}
	
	private void drawConnections(Graphics2D g){
		g.setColor(Color.BLACK);
		List<Country> countries = this.getMap().getCountries();
		for(int i = 1; i < countries.size(); i++){
			Country c = countries.get(i);
			List<Country> connections = c.getConnections();
			//System.out.println(c);
			//System.out.println(connections);
			for(Country conn : connections){
				if(conn.getId() > c.getId() && conn.getX() != 0){
					//System.out.println(c + " <-> " + conn);
					g.drawLine(c.getX(), c.getY(), conn.getX(), conn.getY());
				}
			}
		}
	}
	
	public void drawButtons(Graphics2D g){
		if(this.getButtonList() != null){
			for(Button b : this.getButtonList()){
				draw(b,g);
			}
			
		}
	}
	
	public void drawString(Graphics2D g, String str, int x,int y,int fontSize,Color c){
		g.setFont(g.getFont().deriveFont((float)fontSize));
		g.setColor(c);
		g.drawString(str, x, y);
	}
	
	private void setFontSize(Graphics2D g,int fontSize){
		g.setFont(g.getFont().deriveFont((float) fontSize));
	}
	
	// INPUT HANDLING
	public void countryClicked(Country c,int x,int y){
		if(mode == 1){
			return;
		}
	}
	
	public void buttonClicked(Button b, int x,int y){
		
	}
	
	public void mouseClicked(int x,int y, int mouseButton){
		
	}
	
	public static void draw(Drawable d,Graphics g){
		if(d.getTexture() != null){
			g.drawImage(d.getTexture(),d.getX(),d.getY(),null);
		}
	}
	
	// SETTERS AND GETTERS
	public List<Button> getButtonList(){
		switch(mode){
		case 1:
			switch(setupMode){
			case 1: return numPlayers == 0 ? numberButtons.subList(2, 6) : numberButtons.subList(0,6-numPlayers);
			}
		}
		return null;
	}
	
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
