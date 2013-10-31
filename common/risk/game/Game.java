package risk.game;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import risk.Risk;
import risk.lib.Button;
import risk.lib.DiceTexture;
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
	private List<Button> colourButtons;
	private int[] dice;
	private int[] diceTimers;
	
	private int diceSwitchTimer;
	private int diceDisplayCountdown;
	
	private int numPlayers;
	
	private int turn;
	
	private List<Army> firstTurnContenders; 
	
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
		case 1: updateSetupMode(delta); break;
		}
	}
	
	private void updateSetupMode(int delta){
		switch(setupMode){
		case 0: enterSetupMode(); break;
		case 3:
			for(int i = 0; i < diceTimers.length; i++){
				diceTimers[i] -= delta;
			}
			diceSwitchTimer -= delta;
			if(diceSwitchTimer <= 0){
				diceSwitchTimer += 83; //12 switches per second approximately
				
				boolean diceDone = true;
				for(int i = 0; i < dice.length; i++){
					if(diceTimers[i] > 0){
						dice[i] = Risk.r.nextInt(6)+1;
						diceDone = false;
					}
				}
				if(diceDone){
					diceDisplayCountdown = 1000;
				}
			}
			
			break;
		}
	}
	
	private void enterSetupMode(){
		setupMode = 1;
		initSetupButtons();
		armies = new ArrayList<Army>();
	}
	
	private void initSetupButtons(){
		List<BufferedImage> numberButtonTextures = generateNumberButtonTextures(r);
		numberButtons = new ArrayList<Button>();
		for(int i = 0; i < numberButtonTextures.size(); i++){
 		 	numberButtons.add(new Button(50 + 55 * i,650,numberButtonTextures.get(i),i+3));
		}
		System.out.println("number buttons initialized");
		List<BufferedImage> colourButtonTextures = generateColourButtonTextures();
		colourButtons = new ArrayList<Button>();
		for(int i = 0; i < colourButtonTextures.size(); i++){
			colourButtons.add(new Button(50 + 55 * i,650,colourButtonTextures.get(i),i));
		}
	}
	
	public void draw(Graphics2D g){
		drawMap(g);
		switch(mode){
		case 1: drawSetupMode(g); break;
		case 2: /*drawMainMode(g); */break;
		}
	}
	Army a = new Army(4);
	
	private void drawSetupMode(Graphics2D g){
		Unit u = new Unit(23,a,map.getCountryById(33));
		u.drawSelf(g);
		
		switch(setupMode){
		case 1: 
			drawString(g,"Number of players: ",25,645,30,Color.BLACK);
			break;
		case 2:
			drawString(g,"Choose a colour", 25, 625,40,Color.BLACK);
			drawString(g,"Player " + (turn+1) + ":", 30,645,30,Color.BLACK);
			break;
		case 3:
			drawDice(g);
			break;
		}
		
		drawButtons(g);
	}
	
	private void drawDice(Graphics2D g){
		for(int i = 0; i < numPlayers; i++){
			drawString(g,"Player " + (i + 1), 20 + 100 * i, 645, 25, armies.get(i).getColour());
			g.drawImage(DiceTexture.getDieTexture(dice[i]), 30 + 100 * i, 660, null);
		}
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
		switch(mode){
		case 1:
			switch(setupMode){
			case 1: 
				numPlayers = b.getId();
				setupMode = 2; //Enter choose colour mode
				turn = 0;
				break;
			case 2:
				colourPicked(b);
			}
		}
	}
	
	private void colourPicked(Button b){
		colourButtons.remove(b);
		armies.add(new Army(b.getId()));
		turn++;
		System.out.println(numPlayers);
		if(turn == numPlayers){
			setupMode = 3;
			dice = new int[numPlayers];
			diceTimers = new int[numPlayers];
			for(int i = 0; i < diceTimers.length; i++){
				diceTimers[i] = Risk.r.nextInt(2000) + 1500;
			}
			
			firstTurnContenders = (List<Army>) ((ArrayList<Army>) armies).clone();
		}
	}
	
	public static void draw(Drawable d,Graphics g){
		if(d.getTexture() != null){
			g.drawImage(d.getTexture(),d.getX(),d.getY(),null);
		}
	}
	
	public static void drawDie(Graphics2D g,int x,int y, int val){
		g.drawImage(DiceTexture.getDieTexture(val), x,y,null);
	}
	
	public List<BufferedImage> generateNumberButtonTextures(RiskCanvas riskCanvas){
		final int width = 50;
		final int height = 50;
		
		List<BufferedImage> textures = new ArrayList<BufferedImage>();
		BufferedImage base = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		Graphics baseG = base.getGraphics();
		baseG.setColor(Color.BLACK);
		baseG.fillRoundRect(0, 0, width, height, 10, 10);
		for(char i = '3'; i <= '6'; i++){
			BufferedImage clone = Risk.cloneImage(base);
			Graphics2D g = (Graphics2D) clone.getGraphics();
			g.setColor(Color.DARK_GRAY);
			g.fillRoundRect(5,5,40,40,10,10);
			g.setFont(riskCanvas.army.deriveFont(50f));
			g.setColor(Color.WHITE);
			FontMetrics fm = g.getFontMetrics();
			g.drawString(Character.toString(i),width/2 - fm.charWidth(i)/2,height / 2 + fm.getHeight()/3);
			textures.add(clone);
			System.out.println(i+"buttonmade");
		}
		
		return textures;
	}
	
	public List<BufferedImage> generateColourButtonTextures(){
		final int width = 50;
		final int height = 50;
		
		List<BufferedImage> textures = new ArrayList<BufferedImage>();
		BufferedImage base = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		Graphics baseG = base.getGraphics();
		baseG.setColor(Color.black);
		baseG.fillRoundRect(0, 0, width, height, 10, 10);
		for(int i = 0; i < 6; i++){
			BufferedImage clone = Risk.cloneImage(base);
			Graphics2D g = (Graphics2D) clone.getGraphics();
			g.setColor(Army.getColorByType(i));
			g.fillRoundRect(5,5,40,40,10,10);
			
			textures.add(clone);
		}
		return textures;
	}
	
	// SETTERS AND GETTERS
	public List<Button> getButtonList(){
		switch(mode){
		case 1:
			switch(setupMode){
			case 1: return numberButtons;
			case 2: return colourButtons;
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
