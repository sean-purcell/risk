package risk.game;

import static risk.Risk.DEBUG;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import javax.swing.JOptionPane;

import risk.Risk;
import risk.inet.Client;
import risk.inet.HostMaster;
import risk.inet.HostServer;
import risk.inet.MessageQueuer;
import risk.lib.Button;
import risk.lib.DiceTexture;
import risk.lib.Drawable;
import risk.lib.Input;
import risk.lib.RiskCanvas;
import risk.lib.RiskThread;
import risk.lib.ThreadLocks;

/**
 * Represents the main game logic and loops
 * 
 * @author Sean Purcell
 * 
 */
public class Game extends RiskThread{

	private final int UPDATE_THREAD_ID = 1;

	// This offset will be added to the "source" part of a message to determine
	// the number for the lock
	private final int INPUT_ID_OFFSET = 0x100;

	private final int UPDATE_RATE = 100;

	private RiskCanvas r;
	private Input i;

	private Map map;

	public Thread main;
	
	private HostMaster master;
	private Client cl;
	
	private int playerNum;
	
	/**
	 * Represents the type of this game, 0 means locally hosted normal game<br>
	 * 1 means this is the host for a non-local game<br>
	 * 2 means this is a client for a non-local game
	 */
	private int gameType;
	
	/**
	 * Represents the current mode that the game is in<br>
	 * 0. Title screen<br>
	 * 1. Game setup mode<br>
	 * 2. Main game mode<br>
	 * 3. Display victor<br>
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

	/**
	 * Current part of the game<br>
	 * 1. Deploy begin turn reinforcements<br>
	 * 2. Normal game mode<br>
	 * 3. Battle mode<br>
	 * 4. Dice rolling<br>
	 * 6. Display eliminated army
	 */
	private int gameMode;

	private List<Button> titleScreenButtons;

	private List<Button> receivingPlayersButtons;
	private List<Button> receivingPlayersButtonsNoEndT;
	
	private Button endTurn;
	
	private List<Button> numberButtons;
	private List<Button> colourButtons;
	private int[] dice;
	
	private int[] diceResult;
	
	private int[] diceTimers;

	private int diceSwitchTimer;
	private int diceDisplayCountdown;

	private boolean[] firstTurnContenders;

	private int numTerritoriesClaimed;

	private int numSetupTroops;

	private boolean numAIChosen;
	
	private int numPlayers;
	private int numAI;
	
	private int[] playerTypes;
	
	private int turn;

	private List<Army> armies;

	/**
	 * Applicable during attack and reinforce. Represents the currently
	 * selectedCountry army.
	 */
	private Country selectedCountry;

	private Country attackTarget;

	private int attackers;
	private int defenders;

	private int[] attackerDice;
	private int[] defenderDice;

	private int[] attackerDiceResults;
	private int[] defenderDiceResults;
	
	private int[] attackerDiceTimers;
	private int[] defenderDiceTimers;

	private boolean displayedSorted;

	private int displayEliminatedTimer;
	private Army eliminated;

	private boolean territoryConquered;

	private Button cards;

	private int cardBonus;
	private Iterator<Integer> cardBonusIterator;

	private List<Button> endTurnList;
	private List<Button> cardsList;
	private List<Button> cardsAndEndTurn;
	private List<Button> battleButtonList;

	private BufferedImage battleButtonTexture;

	private List<Button> newGameList;

	private int exceptionCounter;
	
	/**
	 * Set to false if the game should exit
	 */
	private boolean running;

	/**
	 * Represents the current speed of the game in frames per second
	 */
	private int fps;

	public Game() {
		map = new Map();
		i = new Input(this);
		r = new RiskCanvas(this, i);
		this.armies = new ArrayList<Army>();
		this.titleScreenButtons = createMenuButtons(r);
		this.newGameList = createNewGameButton(r);
		main = Thread.currentThread();
		mode = 0;
		running = true;
		
		BufferedImage endTurnImage = Risk.loadImage("resources/endTurn.png");
		endTurn = new Button(1225, 665, endTurnImage, 99);
		
		receivingPlayersButtonsNoEndT = createReceivingPlayersButtons(r);
		receivingPlayersButtons = (List<Button>) ((ArrayList<Button>) receivingPlayersButtonsNoEndT).clone();
		receivingPlayersButtons.add(endTurn);
	}

	// MAIN GAME LOOP AND RELATED MISC

	/**
	 * Begins the main game loop.
	 */
	public void run() {
		long lastTime = System.currentTimeMillis();
		r.resize();

		while (isRunning()) {
			if (r.hasFocus() || Risk.DRAW_WHILE_INACTIVE) { // Ensures that the game does not render when it
								// is not in focus

				// Calculate time since last update
				long time = System.currentTimeMillis();
				int delta = (int) (time - lastTime);
				lastTime = time;
				try {
					setFps(1000 / delta);
				} catch (ArithmeticException e) {
				}

				try {
					// Request lock on GAME_STATE lock to update and render
					ThreadLocks.requestLock(ThreadLocks.GAME_STATE,
							UPDATE_THREAD_ID);
					// Runs the update method with the given delta
					this.update(delta);

					// Create the offscreen buffer containing the frame to be
					// rendered
					r.repaint();
					exceptionCounter = 0;
				} catch (Exception e) {
					e.printStackTrace();
					exceptionCounter++;
					if(exceptionCounter >= 20 && gameType == 2){
						cl.requestResync();
					}
				} finally {
					// Release lock now that we're done with it
					ThreadLocks.releaseLock(ThreadLocks.GAME_STATE,
							UPDATE_THREAD_ID);
				}
				
				// Limits the game to 30 fps
				try {
					Thread.sleep(UPDATE_RATE);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private void gameOver() {
		mode = 3;
	}

	private void update(int delta) {
		switch (mode) {
		case 1:
			updateSetupMode(delta);
			break;
		case 2:
			updateGameMode(delta);
			break;
		}
	}

	private void updateSetupMode(int delta) {
		switch (setupMode) {
		case 0:
			enterSetupMode();
			break;
		case 2:
			pickAIColor();
			break;
		case 3:
			updateSetupDice(delta);
			break;
		}
	}

	private void updateGameMode(int delta) {
		switch (gameMode) {
		case 0:
			gameMode = 1;
		case 1:
			break;
		case 4:
			updateBattleDice(delta);
			break;
		case 6:
			updateEliminatedTimer(delta);
			break;
		}
	}

	private void enterSetupMode() {
		setupMode = 1;
		initSetupButtons();
		armies = new ArrayList<Army>();
	}

	private void pickAIColor(){
		if(gameType == 0){
			if(turn >= numPlayers - numAI){
				this.colourPicked(getButtonList().get(Risk.r.nextInt(getButtonList().size())));
			}
		}else if(gameType == 1){
			if(playerTypes[turn] == 1){
				(new Thread(){
					public void run(){
						String message = "" + (char) 1;
						Button col = (getButtonList().get(Risk.r.nextInt(getButtonList().size())));
						message+=(char)col.getId();
						message(message, -2);
					}
				}).start();
				//
			}
		}
	}
	
	private void enterNextTurn() {
		incrementTurn();
		gameMode = 1;
		selectedCountry = null;
		attackTarget = null;
		territoryConquered = false;
		currentArmy().setFreeUnits(calculateReinforcements());
		
		resetCardButton();
	}

	private int calculateReinforcements() {
		int reinforcements = 0;
		reinforcements += Math.max(3, currentArmy().getUnits().size() / 3);
		reinforcements += currentArmy().calculateContinentBonus();

		return reinforcements;
	}

	private void updateSetupDice(int delta) {
		if (diceDisplayCountdown > 0) {
			diceDisplayDone(delta);
		} else {
			diceDisplayUpdate(delta);
		}
	}

	private void diceDisplayDone(int delta) {
		diceDisplayCountdown -= delta;
		if (diceDisplayCountdown <= 0) {
			int max = 0;
			for (int i = 0; i < numPlayers; i++) {
				if (firstTurnContenders[i] && dice[i] > max) {
					max = dice[i];
				}
			}
			int first = -1;
			for (int i = 0; i < numPlayers; i++) {
				if (dice[i] != max || !firstTurnContenders[i]) {
					firstTurnContenders[i] = false;
				} else {
					diceTimers[i] = this.createDieTimer();
					if (first == -1) { // No one else has won yet
						first = i; // Indicates that that player won the dice
									// roll
					} else {
						first = -2; // Indicates that there is more than one
									// army with the max dice number
					}
				}
			}
			if (first >= 0) {
				enterTerritoryAllocateMode(first);
			}else{
				if(gameType != 2){
					for(int i = 0; i < diceResult.length; i++){
						diceResult[i] = Risk.r.nextInt(6) + 1;
					}
					if(gameType == 1){
						String message = "" + (char) 4 + (char) 2 
								+ Risk.serializeIntArray(diceResult);
						message(message,-5);
					}
				}
			}
		}
	}

	private void diceDisplayUpdate(int delta) {
		for (int i = 0; i < diceTimers.length; i++) {
			diceTimers[i] -= delta;
			if(diceTimers[i] <= 0){
				dice[i] = diceResult[i];
			}
		}
		diceSwitchTimer -= delta;
		if (diceSwitchTimer <= 0) {
			diceSwitchTimer += 83; // 12 switches per second approximately

			boolean diceDone = true;
			for (int i = 0; i < dice.length; i++) {
				if (diceTimers[i] > 0 && firstTurnContenders[i]) {
					dice[i] = Risk.r.nextInt(6) + 1;
					diceDone = false;
				}
			}
			if (diceDone) {
				diceDisplayCountdown = 1000;
			}
		}
	}

	private void updateBattleDice(int delta) {
		if (diceDisplayCountdown <= 0) {
			battleDiceTimerUpdate(delta);
		} else {
			battleDiceTimerDone(delta);
		}
	}

	private void battleDiceTimerUpdate(int delta) {
		diceSwitchTimer -= delta;
		if (diceSwitchTimer <= 0) {
			diceSwitchTimer += 83;
			reRollBattleDice();
		}

		boolean done = true;
		for (int i = 0; i < attackerDice.length; i++) {
			attackerDiceTimers[i] -= delta;
			if (attackerDiceTimers[i] > 0)
				done = false;
			else
				attackerDice[i] = attackerDiceResults[i];
		}
		for (int i = 0; i < defenderDice.length; i++) {
			defenderDiceTimers[i] -= delta;
			if (defenderDiceTimers[i] > 0)
				done = false;
			else
				defenderDice[i] = defenderDiceResults[i];
		}
		if (done) {
			diceDisplayCountdown = 1000;
		}
	}

	private void battleDiceTimerDone(int delta) {
		diceDisplayCountdown -= delta;

		if (diceDisplayCountdown <= 0) {
			if (!displayedSorted) {
				diceDisplayCountdown = 1000;
				Risk.sort(attackerDice);
				Risk.sort(defenderDice);
				displayedSorted = true;
			} else {
				int[] losses = calculateLosses();
				attackers -= losses[0];
				selectedCountry.getUnit().incrementTroops(-losses[0]);
				defenders -= losses[1];
				attackTarget.getUnit().incrementTroops(-losses[1]);
				if (selectedCountry.getUnit().getTroops() <= 1 || attackers < 1) {
					gameMode = 2;
					attackTarget = null;
				} else if (attackTarget.getUnit().getTroops() <= 0) {
					battleWon();
				} else {
					gameMode = 3;
				}
			}
		}
	}

	private void reRollBattleDice() {
		for (int i = 0; i < attackerDice.length; i++) {
			if (attackerDiceTimers[i] > 0)
				attackerDice[i] = Risk.r.nextInt(6) + 1;
		}

		for (int i = 0; i < defenderDice.length; i++) {
			if (defenderDiceTimers[i] > 0)
				defenderDice[i] = Risk.r.nextInt(6) + 1;
		}
	}

	private void updateEliminatedTimer(int delta) {
		displayEliminatedTimer -= delta;
		if (displayEliminatedTimer <= 0) {
			gameMode = 2;
			eliminated = null;
		}
	}

	private void battleWon() {
		gameMode = 2;

		Army defender = attackTarget.getUnit().getArmy();

		this.removeUnit(attackTarget.getUnit());
		this.addUnit(attackers, currentArmy(), attackTarget);

		this.selectedCountry.getUnit().incrementTroops(-attackers);

		selectedCountry = attackTarget;
		attackTarget = null;

		territoryConquered = true;

		if (defender.getUnits().size() == 0) {
			armyEliminated(defender);
		}
	}

	private void armyEliminated(Army eliminatee) {
		Army current = currentArmy();
		for(int i = armies.indexOf(eliminatee); i < playerTypes.length - 1; i++){
			playerTypes[i] = playerTypes[i+1];
		}
		if(playerNum > armies.indexOf(eliminatee)){
			playerNum--;
		}
		armies.remove(eliminatee);
		numPlayers--;
		turn = armies.indexOf(current);
		if (numPlayers == 1) {
			gameOver();
		} else {
			gameMode = 6;
			eliminated = eliminatee;
			displayEliminatedTimer = 3000;
		}
	}

	private int[] calculateLosses() {
		int[] losses = new int[2];
		for (int i = 0; i < Math.min(attackerDice.length, defenderDice.length); i++) {
			if (attackerDice[i] > defenderDice[i]) {
				losses[1]++;
			} else {
				losses[0]++;
			}
		}

		return losses;
	}

	private void enterTerritoryAllocateMode(int first) {
		int offset = first;

		// Set the first army in the list to be the one designated to go first
		Risk.rotateList(armies, offset);
		playerTypes = Risk.rotateArray(playerTypes,offset, numPlayers);
		playerNum -= first;
		playerNum += numPlayers;
		playerNum %= numPlayers;
		setupMode = 4;
		turn = 0;

		int startingTroops = (10 - numPlayers) * 5;
		for (Army a : armies) {
			a.setFreeUnits(startingTroops);
		}
	}

	private void enterSetupReinforcement() {
		setupMode = 5;
		turn = 0;

		numSetupTroops = 3;
	}

	private void enterAttack(Country c) {
		this.gameMode = 3;
		attackTarget = c;
		attackers = 1;
		defenders = c.getUnit().getTroops();
	}

	private void initSetupButtons() {
		List<BufferedImage> numberButtonTextures = generateNumberButtonTextures(r);
		numberButtons = new ArrayList<Button>();
		for (int i = 0; i < numberButtonTextures.size(); i++) {
			numberButtons.add(new Button(50 + 55 * i, 650, numberButtonTextures
					.get(i), i));
		}
		System.out.println("number buttons initialized");
		List<BufferedImage> colourButtonTextures = generateColourButtonTextures();
		colourButtons = new ArrayList<Button>();
		for (int i = 0; i < colourButtonTextures.size(); i++) {
			colourButtons.add(new Button(50 + 55 * i, 650, colourButtonTextures
					.get(i), i));
		}
	}

	private void enterGamePhase() {
		mode = 2;
		setupMode = 0;

		turn = -1;

		endTurnList = new ArrayList<Button>();
		endTurnList.add(endTurn);

		battleButtonList = new ArrayList<Button>();
		battleButtonList.add(endTurn);

		battleButtonTexture = Risk.loadImage("resources/battleButton.png");
		Button rollDice = new Button(1155, 665, battleButtonTexture, 6);
		battleButtonList.add(rollDice);

		cardsList = new ArrayList<Button>();
		cardsAndEndTurn = new ArrayList<Button>();
		cards = new Button(450, 630, 0, 90, 7);

		cardsList.add(cards);
		cardsAndEndTurn.add(endTurn);
		cardsAndEndTurn.add(endTurn);

		initCardBonusIterator();

		enterNextTurn();
	}

	private void initCardBonusIterator() {
		cardBonusIterator = new Iterator<Integer>() {
			// Decided to use the iterator interface to provide
			// handy methods and allow me to use an anonymous class
			public boolean hasNext() {
				return true;
			}

			public Integer next() {
				if (cardBonus == 60) {
					return 4;
				}
				if (cardBonus < 10) {
					return cardBonus + 2;
				}
				return cardBonus + 5;
			}

			public void remove() {
			}
		};
		cardBonus = 4;
	}

	public void draw(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

		try {
			drawMap(g);
			drawUnits(g);
			switch (mode) {
			case 0:
				drawTitleScreen(g);
				break;
			case 1:
				drawSetupMode(g);
				break;
			case 2:
				drawGameMode(g);
				break;
			case 3:
				drawVictor(g);
				break;
			}
			drawButtons(g);
		} catch (RuntimeException e) {
			e.printStackTrace();
			System.err.println("Frame not completed due to error");
		}
	}

	private void drawTitleScreen(Graphics2D g) {
		setFontSize(g, 400);
		FontMetrics fm = g.getFontMetrics();
		drawString(g, "RISK", 400, 640 - fm.stringWidth("RISK") / 2, 360,
				Color.RED);
	}

	private void drawVictor(Graphics2D g) {
		Army victor = armies.get(0);
		String message = victor.getName() + " IS VICTORIOUS!";
		drawCenteredMessage(g, message, victor.getColour());
	}

	private void drawEliminated(Graphics2D g) {
		String message = eliminated.getName() + " IS ELIMINATED";
		drawCenteredMessage(g, message, eliminated.getColour());
	}

	private void drawCenteredMessage(Graphics2D g, String message, Color color) {
		setFontSize(g, 72);
		FontMetrics fm = g.getFontMetrics();
		int x = 640 - fm.stringWidth(message) / 2;
		int y = 360 + fm.getHeight() / 2;

		drawString(g, message, 72, x, y, color);
	}

	public void drawUnits(Graphics2D g) {
		for (Country c : map.getCountries()) {
			if (c != null && c.getUnit() != null) {
				c.getUnit().drawSelf(g);
			}
		}
	}

	private void drawSetupMode(Graphics2D g) {
		switch (setupMode) {
		case 1:
			if(!numAIChosen){
				drawString(g, "Number of AI: ", 30, 25, 645, Color.BLACK);
			}else{
				drawString(g, "Number of players: ", 30, 25, 645, Color.BLACK);
			}
			break;
		case 2:
			drawString(g, "Player " + (turn + 1), 40, 25, 625, Color.BLACK);
			drawString(g, "Choose a colour", 30, 30, 645, Color.BLACK);
			break;
		case 3:
			drawDice(g);
			break;
		case 4:
			drawTurn(g);
			drawClaimTerritories(g);
			break;
		case 5:
			drawTurn(g);
			drawDeploySetupTroops(g);
			break;
		case -1:
			drawPlayersConnected(g);
			break;
		case -2:
			drawString(g, "Waiting for the host to begin the game", 30, 30, 645, Color.BLACK);
			break;
		}
	}

	private void drawGameMode(Graphics2D g) {
		drawTurn(g);
		switch (gameMode) {
		case 2:
		case 5:
			drawSelectedCountry(g);
			drawObjectiveMessage(g);
			break;
		case 3:
			drawSelectedCountry(g);
			drawAttackTarget(g);
			drawObjectiveMessage(g);
			drawBattle(g);
			break;
		case 4:
			drawSelectedCountry(g);
			drawAttackTarget(g);
			drawObjectiveMessage(g);
			drawBattle(g);
			drawBattleDice(g);
			break;
		case 1:
			drawCardBonus(g);
			drawReinforcements(g);
			drawCards(g);
			break;
		case 6:
			drawEliminated(g);
			break;
		}
	}

	private void drawTurn(Graphics2D g) {
		drawString(g, currentArmy().getName(), 40, 25, 625, armies.get(turn)
				.getColour());
	}

	private void drawDice(Graphics2D g) {
		for (int i = 0; i < numPlayers; i++) {
			if (firstTurnContenders[i]) {
				drawString(g, "Player " + (i + 1), 25, 20 + 100 * i, 645,
						armies.get(i).getColour());
				g.drawImage(DiceTexture.getDieTexture(dice[i]), 30 + 100 * i,
						660, null);
			}
		}
	}

	private void drawClaimTerritories(Graphics2D g) {
		if(!isTurn()){
			return;
		}
		drawString(g, "Claim an unclaimed territory.", 30, 30, 645, Color.BLACK);
	}

	private void drawDeploySetupTroops(Graphics2D g) {
		if(!isTurn()){
			return;
		}
		drawString(g, "Deploy " + numSetupTroops + " troops.", 30, 30, 645,
				Color.BLACK);
		drawString(g, "Troops left: " + currentArmy().getFreeUnits(), 30, 30,
				665, Color.BLACK);
	}

	private void drawPlayersConnected(Graphics2D g){
		drawString(g,"Players: " + numPlayers, 40, 25,625,Color.BLACK);
	}
	
	private void drawMap(Graphics2D g) {
		try {
			g.drawImage(this.getMap().getTexture(), 0, 0, null);
		} catch (NullPointerException e) {
			System.out.println("map not found");
		}
	}

	private void drawReinforcements(Graphics2D g) {
		Army a = this.currentArmy();
		this.drawString(g, "Free Troops: " + a.getFreeUnits(), 30, 30, 645,
				Color.BLACK);
	}

	private void drawObjectiveMessage(Graphics2D g) {
		if(!isTurn()){
			return;
		}
		String message = gameMode == 5 ? "Move troops" : "Attack enemy";
		String message2 = gameMode == 5 ? "to reinforce positions."
				: "territories.";
		this.drawString(g, message, 30, 30, 645, Color.BLACK);
		this.drawString(g, message2, 30, 30, 665, Color.BLACK);
	}

	private void drawSelectedCountry(Graphics2D g) {
		if (selectedCountry == null) {
			return;
		}

		drawCountry(g, selectedCountry, 475, 625, "Selected:");
	}

	private void drawAttackTarget(Graphics2D g) {
		if (attackTarget == null) {
			System.err
					.println("drawAttackTarget called with no attack target.");
			return;
		}

		drawCountry(g, attackTarget, 675, 625, "Target:");
	}

	private void drawCountry(Graphics2D g, Country c, int x, int y,
			String message) {
		Image texture = c.getTexture();

		this.setFontSize(g, 30);

		// Determine the x and y coordinates to draw the image at to ensure it's
		// centered
		int nx = x - texture.getWidth(null) / 2;
		int ny = y - texture.getHeight(null) / 2;

		g.drawImage(texture, nx, ny, null);

		FontMetrics fm = g.getFontMetrics();
		drawString(g, message, 30, x - fm.stringWidth(message) / 2,
				y - texture.getHeight(null) / 2, c.getUnit().getArmy()
						.getColour());
	}

	private void drawBattle(Graphics2D g) {
		for (int i = 0; i < Math.min(3, attackers); i++) {
			BufferedImage soldier = currentArmy().getSoldierAttacker();
			int x = 830 - 20 * i;
			int y = 530 + 45 * i;
			g.drawImage(soldier, x, y, null);
		}
		this.setFontSize(g, 25);
		FontMetrics fm = g.getFontMetrics();
		drawString(g, "Attackers: " + attackers, 25,
				880 - fm.stringWidth("Attackers: " + attackers), 705,
				currentArmy().getColour());

		for (int i = 0; i < Math.min(2, defenders); i++) {
			BufferedImage soldier = defendingArmy().getSoldierDefender();
			int x = 1010 + 20 * i;
			int y = 552 + 45 * i;
			g.drawImage(soldier, x, y, null);
		}

		drawString(g, "Defenders: " + defenders, 25, 1010, 705, defendingArmy()
				.getColour());
	}

	private void drawBattleDice(Graphics2D g) {
		for (int i = 0; i < attackerDice.length; i++) {
			int x = 890;
			int y = 522 + 55 * i;
			drawDie(g, x, y, attackerDice[i]);
		}

		for (int i = 0; i < defenderDice.length; i++) {
			int x = 945;
			int y = 544 + 55 * i;
			drawDie(g, x, y, defenderDice[i]);
		}
	}

	private void drawCards(Graphics2D g) {
		if(!isTurn()){
			return;
		}
		int index = 0;
		for (Card c : currentArmy().getCards()) {
			c.draw(g, index);
			index++;
		}
	}

	private void drawCardBonus(Graphics2D g) {
		if(!isTurn()){
			return;
		}
		drawString(g, "Card Bonus: " + cardBonus, 30, 930, 675, Color.BLACK);
	}
	
	private void drawConnections(Graphics2D g) {
		g.setColor(Color.BLACK);
		List<Country> countries = this.getMap().getCountries();
		for (int i = 1; i < countries.size(); i++) {
			Country c = countries.get(i);
			List<Country> connections = c.getConnections();
			// System.out.println(c);
			// System.out.println(connections);
			for (Country conn : connections) {
				if (conn.getId() > c.getId() && conn.getX() != 0) {
					// System.out.println(c + " <-> " + conn);
					g.drawLine(c.getX(), c.getY(), conn.getX(), conn.getY());
				}
			}
		}
	}

	public void drawButtons(Graphics2D g) {
		try{
			if(!isTurn()){
				return;
			}
		}
		catch(Exception e){}
		if (this.getButtonList() != null) {
			for (Button b : this.getButtonList()) {
				draw(b, g);
			}
		}
	}

	public void drawString(Graphics2D g, String str, int fontSize, int x,
			int y, Color c) {
		setFontSize(g, fontSize);
		g.setColor(c);
		g.drawString(str, x, y);
	}

	public int createDieTimer() {
		return 3000;
	}

	public void setFontSize(Graphics2D g, int fontSize) {
		g.setFont(g.getFont().deriveFont((float) fontSize));
	}

	public boolean isTurn(){
		if(mode == 2 || (mode == 1 && setupMode >= 2)){
			if(gameType == 0){
				return playerTypes[turn] == 0;
			}else{
				return playerNum == turn;
			}
		}else{
			return true;
		}
	}
	
	public boolean correctSource(int source){
		switch(source){
		case -1:
			return DEBUG;
		case -2:
			return playerTypes[turn] == 1;
		case 1:
			return isTurn();
		case 5:
		case 6:
		default:
			return true;
		}
	}
	
	private void addUnit(int troops, Army a, Country c) {
		// Due to the many pointers that must be consistent, this is the only
		// method that should be used for creating units
		Unit u = new Unit(troops, a, c);
		a.getUnits().add(u);
		c.setUnit(u);
	}

	private void removeUnit(Unit u) {
		// Should be the only method for removing units
		// Leaves the unit object with no references to be GC'ed

		// Should immediately by called by add unit
		u.getArmy().getUnits().remove(u);
		u.getLocation().setUnit(null);
	}

	// INPUT HANDLING
	public void countryClicked(Country c) {
		switch (mode) {
		case 1:
			countryClickedSetupMode(c);
			break;
		case 2:
			countryClickedGameMode(c);
			break;
		}
	}

	private void countryClickedSetupMode(Country c) {
		switch (setupMode) {
		case 4:
			if (c.getUnit() == null) {
				this.addUnit(1, currentArmy(), c);
				currentArmy().setFreeUnits(currentArmy().getFreeUnits() - 1);
				incrementTurn();
				numTerritoriesClaimed++;
				if (numTerritoriesClaimed == 42) {
					enterSetupReinforcement();
				}
			}
			break;
		case 5:
			if (currentArmy() == c.getUnit().getArmy()) {
				addTroop(c);
				numSetupTroops--;
				if (this.turn == numPlayers - 1
						&& currentArmy().getFreeUnits() == 0) {
					enterGamePhase();
					break;
				}
				if (numSetupTroops == 0) {
					incrementTurn();
					numSetupTroops = Math.min(3, currentArmy().getFreeUnits());
				}
			}
			break;
		}
	}

	private void countryClickedGameMode(Country c) {
		switch (gameMode) {
		case 1:
			if (currentArmy() == c.getUnit().getArmy()
					&& currentArmy().getFreeUnits() > 0) {
				this.addTroop(c);
			}
			break;
		case 2:
			if (currentArmy() == c.getUnit().getArmy()) {
				selectedCountry = c;
			} else if (selectedCountry != null) {
				if (selectedCountry.getConnections().contains(c)
						&& selectedCountry.getUnit().getTroops() > 1) {
					enterAttack(c);
				}
			}
			break;
		case 3:
			if (c == attackTarget) {
				if (attackers < selectedCountry.getUnit().getTroops() - 1) {
					attackers++;
				}
			} else {
				gameMode = 2;
				attackTarget = null;
				countryClicked(c);
			}
			break;
		case 5:
			if (currentArmy() == c.getUnit().getArmy()) {
				if (connected(selectedCountry, c)
						&& selectedCountry.getUnit().getTroops() > 1) {
					c.getUnit().incrementTroops();
					selectedCountry.getUnit().incrementTroops(-1);
				} else {
					selectedCountry = c;
				}
			} else {
				selectedCountry = null;
			}
		}
	}

	/**
	 * Searches through a breadth-first search to see if there is a link through
	 * controlled territories from country a to country b
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private boolean connected(Country a, Country b) {
		if (a == null || b == null) {
			return false;
		}
		if (a.getUnit().getArmy() != b.getUnit().getArmy()) {
			return false;
		}

		boolean[] visited = new boolean[42];

		Queue<Country> searchQ = new ArrayDeque<Country>();
		searchQ.add(a);
		while (!searchQ.isEmpty()) {
			Country top = searchQ.remove();

			if (top == b) {
				return true;
			}

			for (Country c : top.getConnections()) {
				if (c.getUnit().getArmy() == top.getUnit().getArmy()
						&& !visited[c.getId() - 1]) {
					searchQ.add(c);
					visited[c.getId() - 1] = true;
				}
			}
		}
		return false;
	}

	public void buttonClicked(Button b, int x, int y) {
		switch (mode) {
		case 0:
			switch (b.getId()) {
			case 0:
				mode = 1;
				gameType = 0;
				break;
			case 1:
				mode = 1;
				setupMode = -1;
				gameType = 1;
				startAcceptingClients();
				break;
			case 2:
				mode = 1;
				setupMode = -2;
				gameType = 2;
				promptIP();
				break;
			}
		case 1:
			switch (setupMode) {
			case 1:
				if(!numAIChosen){
					numAI = b.getId();
					numAIChosen = true;
					numberButtons = numberButtons.subList(Math.max(0,3-numAI),7-numAI);
				}else{
					System.out.println("numberButtonClicked");
					numPlayers = b.getId() + numAI;
					setupMode = 2; // Enter choose colour mode
					turn = 0;
					playerTypes = new int[numPlayers];
				}
				break;
			case 2:
				colourPicked(b);
				break;
			case -1:
			case -2:
				switch(b.getId()){
				case 0:
					playerAdded(1);
					break;
				case 99:
					doneReceiving();
					break;
				}
			}
			break;
		case 2:
			switch (gameMode) {
			case 3:
				if (b.getId() == 6 && gameType != 2) {
					rollBattleDice();
					break;
				}
			case 2:
			case 1:
				if (b.getId() == 7) {
					if (currentArmy().useCards()) {
						cardsUsed();
						resetCardButton();
					}
				}
			case 5:
				if (b.getId() == 99) {
					switch (gameMode) {
					case 1:
						gameMode++;
						break;
					case 2:
					case 3:
						gameMode = 5;
						break;
					case 5:
						if (territoryConquered) {
							currentArmy().addCard();
						}
						enterNextTurn();
						gameMode = 1;
						break;
					}
				}
				break;
			}
			break;
		case 3:
			switch (b.getId()) {
			case 0:
				Risk.newGame();
				running = false;
				main.stop();
			}
		}
	}

	private void promptIP(){
		String ip = (String) JOptionPane.showInputDialog(
				r,
				"Enter the IP of the game you would like to connect to:",
				"Enter IP",
				JOptionPane.PLAIN_MESSAGE,
				null,
				null,
				null);
		System.out.println(ip);
		cl = Client.makeClient(this,ip);
		if(cl == null){
			int choice = JOptionPane.showConfirmDialog(
					r,
					"Server not found at " + ip + ".  Return to main menu?",
					"No Server",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE,
					null
					);
			if(choice == JOptionPane.YES_OPTION){
				mode = 0;
				setupMode = 0;
				gameType = 0;
				return;
			}else{
				System.exit(5);
			}
		}
		initSetupButtons();	
		mq = new MessageQueuer(this);
		cl.start();
	}
	
	private void startAcceptingClients(){
		this.master = HostMaster.createHostMaster(this);
		if(master == null){
			int choice = JOptionPane.showConfirmDialog(
					r,
					"Could not create server.  Return to main menu?",
					"No Server",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE,
					null
					);
			if(choice == JOptionPane.YES_OPTION){
				mode = 0;
				setupMode = 0;
				gameType = 0;
				return;
			}else{
				System.exit(5);
			}
		}
		master.start();
		
		numPlayers = 1;
		playerTypes = new int[6];
		initSetupButtons();
	}
	
	private void colourPicked(Button b) {
		colourButtons.remove(b);
		if(gameType == 0){
			playerTypes[turn] = turn < numPlayers - numAI ? 0 : 1;
		}
		armies.add(new Army(b.getId(), this,playerTypes[turn]));
		
		turn++;
		System.out.println(numPlayers);
		if (turn == numPlayers) {
			setupMode = 3;
			dice = new int[numPlayers];
			if(gameType != 2)
				diceResult = new int[numPlayers];
			diceTimers = new int[numPlayers];

			firstTurnContenders = new boolean[numPlayers];

			for (int i = 0; i < numPlayers; i++) {
				diceTimers[i] = this.createDieTimer();
				if(gameType != 2)
					diceResult[i] = Risk.r.nextInt(6) + 1;
				firstTurnContenders[i] = true;
			}
			if(gameType == 1){
				String message = "" + (char) 4 + (char) 2 
						+ Risk.serializeIntArray(diceResult);
				message(message,-5);
			}
		}
	}

	private void cardsUsed() {
		currentArmy().setFreeUnits(currentArmy().getFreeUnits() + cardBonus);
		cardBonus = cardBonusIterator.next();
	}

	private void addTroop(Country c) {
		c.getUnit().incrementTroops();
		c.getUnit().getArmy()
				.setFreeUnits(c.getUnit().getArmy().getFreeUnits() - 1);

	}

	private void rollBattleDice() {
		gameMode = 4;

		int attackerNum = Math.min(3, attackers);
		int defenderNum = Math.min(2, defenders);

		attackerDice = new int[attackerNum];
		defenderDice = new int[defenderNum];

		if(gameType != 2){
			attackerDiceResults = new int[attackerNum];
			defenderDiceResults = new int[defenderNum];
			
			attackerDiceTimers = new int[Math.min(3, attackers)];
			defenderDiceTimers = new int[Math.min(2, defenders)];

			for (int i = 0; i < attackerDiceTimers.length; i++) {
				attackerDiceTimers[i] = 2000;
				attackerDiceResults[i] = Risk.r.nextInt(6) + 1;
			}

			for (int i = 0; i < defenderDiceTimers.length; i++) {
				defenderDiceTimers[i] = 2000;
				defenderDiceResults[i] = Risk.r.nextInt(6) + 1;
			}
			
			
			if(gameType == 1)
				transmitBattleData();
		}
		reRollBattleDice();

		displayedSorted = false;
		diceSwitchTimer = 0;
	}
	
	private void transmitBattleData(){
		int i = 0;
		sendDiceInfo(0,attackerDiceResults);
		sendDiceInfo(1,defenderDiceResults);
		sendDiceInfo(2,attackerDiceTimers);
		sendDiceInfo(3,defenderDiceTimers);
	}
	
	private void sendDiceInfo(int i, int[] data){
		String message = "" + (char) 4 + (char) + 3;
		message += (char) i;
		message += Risk.serializeIntArray(data);
		this.message(message,-5);
	}

	private void nullClicked() {
		switch (mode) {
		case 2:
			switch (gameMode) {
			case 3:
				attackTarget = null;
				gameMode = 2;
			case 2:
			case 5:
				selectedCountry = null;
				break;
			}
		}
	}

	private static void draw(Drawable d, Graphics g) {
		if (d.getTexture() != null) {
			g.drawImage(d.getTexture(), d.getX(), d.getY(), null);
		}
	}

	private static void drawDie(Graphics2D g, int x, int y, int val) {
		g.drawImage(DiceTexture.getDieTexture(val), x, y, null);
	}

	private List<BufferedImage> generateNumberButtonTextures(
			RiskCanvas riskCanvas) {
		final int width = 50;
		final int height = 50;

		List<BufferedImage> textures = new ArrayList<BufferedImage>();
		BufferedImage base = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics baseG = base.getGraphics();

		((Graphics2D) baseG).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);

		baseG.setColor(Color.BLACK);
		baseG.fillRoundRect(0, 0, width, height, 10, 10);
		for (char i = '0'; i <= '6'; i++) {
			BufferedImage clone = Risk.cloneImage(base);
			Graphics2D g = (Graphics2D) clone.getGraphics();

			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);

			((Graphics2D) g).setRenderingHint(
					RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

			g.setColor(Color.DARK_GRAY);
			g.fillRoundRect(5, 5, 40, 40, 10, 10);
			g.setFont(riskCanvas.army.deriveFont(50f));
			g.setColor(Color.WHITE);

			FontMetrics fm = g.getFontMetrics();
			g.drawString(Character.toString(i),
					width / 2 - fm.charWidth(i) / 2,
					height / 2 + fm.getHeight() / 3);
			textures.add(clone);
			System.out.println(i + "buttonmade");
		}

		return textures;
	}

	private List<BufferedImage> generateColourButtonTextures() {
		final int width = 50;
		final int height = 50;

		List<BufferedImage> textures = new ArrayList<BufferedImage>();
		BufferedImage base = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics baseG = base.getGraphics();

		((Graphics2D) baseG).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);

		baseG.setColor(Color.black);
		baseG.fillRoundRect(0, 0, width, height, 10, 10);
		for (int i = 0; i < 6; i++) {
			BufferedImage clone = Risk.cloneImage(base);
			Graphics2D g = (Graphics2D) clone.getGraphics();

			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);

			g.setColor(Army.getColorByType(i));
			g.fillRoundRect(5, 5, 40, 40, 10, 10);

			textures.add(clone);
		}
		return textures;
	}

	private List<Button> createMenuButtons(RiskCanvas riskCanvas) {
		String[] buttonStrings = { "New Game","Host Game","Join Game"};
		List<Button> buttons = new ArrayList<Button>();

		BufferedImage base = new BufferedImage(200, 100,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D baseG = (Graphics2D) base.getGraphics();

		baseG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);

		baseG.setColor(Color.BLACK);
		baseG.fillRoundRect(0, 0, 200, 100, 10, 10);

		baseG.setColor(Color.WHITE);
		baseG.fillRoundRect(5, 5, 190, 90, 10, 10);

		int index = 0;

		for (String s : buttonStrings) {
			BufferedImage clone = Risk.cloneImage(base);
			Graphics2D g = (Graphics2D) clone.getGraphics();

			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);

			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

			g.setFont(riskCanvas.army.deriveFont(40f));
			g.setColor(Color.BLACK);
			FontMetrics fm = g.getFontMetrics();

			int x = 100 - fm.stringWidth(s) / 2;

			drawString(g, s, 40, x, 50 + fm.getHeight() / 2, Color.BLACK);

			Button b = new Button(640
					- ((buttonStrings.length - 1) * 205 + 200) / 2 + 205
					* index, 615, clone, index);
			buttons.add(b);
			index++;
		}

		return buttons;
	}

	private List<Button> createNewGameButton(RiskCanvas riskCanvas) {
		BufferedImage text = new BufferedImage(200, 100,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) text.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

		g.setColor(Color.BLACK);
		g.fillRoundRect(0, 0, 200, 100, 10, 10);

		g.setColor(Color.WHITE);
		g.fillRoundRect(5, 5, 190, 90, 10, 10);

		g.setFont(riskCanvas.army.deriveFont(40f));
		g.setColor(Color.BLACK);
		FontMetrics fm = g.getFontMetrics();

		int x = 100 - fm.stringWidth("Title Screen") / 2;

		drawString(g, "Title Screen", 40, x, 50 + fm.getHeight() / 2,
				Color.BLACK);

		Button b = new Button(540, 615, text, 0);
		List<Button> list = new ArrayList<Button>();
		list.add(b);
		return list;
	}

	private List<Button> createReceivingPlayersButtons(RiskCanvas riskCanvas){
		String[] buttonStrings = {"Add AI"};
		List<Button> buttons = new ArrayList<Button>();

		BufferedImage base = new BufferedImage(200, 50,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D baseG = (Graphics2D) base.getGraphics();

		baseG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);

		baseG.setColor(Color.BLACK);
		baseG.fillRoundRect(0, 0, 200, 50, 10, 10);

		baseG.setColor(Color.WHITE);
		baseG.fillRoundRect(5, 5, 190, 40, 10, 10);

		int index = 0;

		for (String s : buttonStrings) {
			BufferedImage clone = Risk.cloneImage(base);
			Graphics2D g = (Graphics2D) clone.getGraphics();

			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);

			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

			g.setFont(riskCanvas.army.deriveFont(40f));
			g.setColor(Color.BLACK);
			FontMetrics fm = g.getFontMetrics();

			int x = 100 - fm.stringWidth(s) / 2;

			drawString(g, s, 40, x, 25 + fm.getHeight() / 2, Color.BLACK);

			Button b = new Button(30, 665, clone, index);
			buttons.add(b);
			index++;
		}

		return buttons;
	}
	
	private void resetCardButton() {
		cards.setWidth(currentArmy().getCards().size() * 90);
	}
	
	/**
	 * All input should go through this, to make it easier to combine local
	 * multiplayer, non-local multiplayer, and AI
	 * 
	 * @param message
	 *            The message. The format of the message is outlined in
	 *            MessageProtocol.txt
	 * @param source
	 */
	public void message(String message, int source) {
		System.out.println("Message received from " + source);
		try {
			Risk.showMessage(message);

			//If there are any exceptions here for some reason we probably don't want to send the message
			if(source < 5 && propogateMessage()){ //If this is not true it was just sent over socket.  We dont want to resend it.
				switch(gameType){
				case 1:
					master.message(message, null);
					break;
				case 2:
					cl.writeMessage(message);
					break;
				}
			}
			
			if(source != -5){ //source of -5 indicates that this should not interpret it
				// Request the GAME_STATE lock to avoid concurrency issues
				ThreadLocks.requestLock(ThreadLocks.GAME_STATE, source
						+ INPUT_ID_OFFSET);
				int t = message.charAt(0);
				switch (t) {
				// hexadecimal used because it seemed fitting
				case 0x1:
					parseButtonMessage(message.substring(1), source);
					break;
				case 0x2:
					parseCountryMessage(message.substring(1), source);
					break;
				case 0x3:
					nullClicked();
					break;
				case 0x4:
					parseGameInfo(message.substring(1),source);
					break;
				case 0x10:
					parseCheatMessage(message.substring(1), source);
					break;
				case 0x20:
					deserializeGameData(message.substring(1));
					break;
				}
			}
			
		} catch (Exception e) {
			if(source != -2 || DEBUG){
				e.printStackTrace();
			}
		} finally {
			ThreadLocks.releaseLock(ThreadLocks.GAME_STATE, source
					+ INPUT_ID_OFFSET);
		}
		main.interrupt();
	}

	private void parseButtonMessage(String str, int source) {
		try{
			if(!correctSource(source)){
				return;
			}
		}
		catch(NullPointerException e){

		}
		int i = str.charAt(0);
		Button clicked = null;
		for (Button b : getButtonList()) {
			if (b.getId() == i) {
				clicked = b;
				break;
			}
		}
		buttonClicked(clicked, 0, 0);
	}

	private void parseCountryMessage(String str, int source) {
		try{
			if(!correctSource(source)){
				return;
			}
		}
		catch(NullPointerException e){

		}
		int i = Integer.parseInt(str.substring(0, 2));
		countryClicked(map.getCountryById(i));
	}

	private void parseGameInfo(String str, int source){
		if(source < 5){
			System.err.println("parseGameInfo called from a source that isnt 5 or 6");
		}
		
		switch(str.charAt(0)){
		case 0x0:
			playerNum = (int) str.charAt(1);
			break;
		case 0x1: // Playertypes array
			numPlayers = (int) str.charAt(1);
			playerTypes = Risk.deserializeIntArray(str.substring(2));
			setupMode = 2;
			break;
		case 0x2: // Who goes first dice
			diceResult = Risk.deserializeIntArray(str.substring(1));
			break;
		case 0x3: // Battle dice
			parseBattleInfo(str.substring(1));
			break;
		}
	}
	
	private void parseBattleInfo(String str){
		switch(str.charAt(0)){
		case 0:
			attackerDiceResults = Risk.deserializeIntArray(str.substring(1));
			break;
		case 1:
			defenderDiceResults = Risk.deserializeIntArray(str.substring(1));
			break;
		case 2:
			attackerDiceTimers = Risk.deserializeIntArray(str.substring(1));
			break;
		case 3:
			defenderDiceTimers = Risk.deserializeIntArray(str.substring(1));
			rollBattleDice();
			reRollBattleDice();
			break;
		}
	}
	
	private void parseCheatMessage(String str, int source) {
		if (!DEBUG) {
			return;
		}

		switch (str.charAt(0)) {
		case 1:
			for (int i = 0; i < attackerDice.length; i++) {
				attackerDice[i] = 6;
				attackerDiceTimers[i] = 0;
			}

			for (int i = 0; i < defenderDice.length; i++) {
				defenderDice[i] = 1;
				defenderDiceTimers[i] = 0;
			}
			break;
		case 2:
			try {
				currentArmy().addCard();
				this.resetCardButton();
			} catch (Exception e) {

			}
		}
	}
	
	public void resyncRequested(HostServer h){
		String message = (char) 0x20 + serializeGameData();
		h.writeMessage(message);
	}
	
	/*
	 * PROTOCOL:
	 * 0=gameMode
	 * 48=playerTypes
	 * 1=turn
	 * 1=selectedCountry.getId()
	 * 1=attackTarget.getId() (0 if null)
	 * 1=attackers
	 * 1=defenders
	 * attackers*8=attackerDiceResults
	 * defenders*8=defenderDiceResults
	 * attackers*8=attackerDiceTimers
	 * defenders*8=defenderDiceTimers
	 */
	
	private String serializeGameData(){
		StringBuffer gameData = new StringBuffer();
		try{
			ThreadLocks.requestLock(ThreadLocks.GAME_STATE, 0x10);
			gameData.append((char) gameMode);
			
			gameData.append(Risk.serializeIntArray(playerTypes));
			
			gameData.append((char)turn);
			
			try{
				gameData.append(selectedCountry.getId());
			}
			catch(NullPointerException e){
				gameData.append((char) 0);
			}
			
			try{
				gameData.append(attackTarget.getId());
			}
			catch(NullPointerException e){
				gameData.append((char) 0);
			}
			
			gameData.append((char) attackers);
			gameData.append((char) defenders);
			
			gameData.append(Risk.serializeIntArray(attackerDiceResults));
			gameData.append(Risk.serializeIntArray(defenderDiceResults));
			
			gameData.append(Risk.serializeIntArray(attackerDiceTimers));
			gameData.append(Risk.serializeIntArray(defenderDiceTimers));
		}
		catch(Exception e){
			e.printStackTrace();
			System.err.println("Could not serialize game data.");
		}
		finally{
			ThreadLocks.releaseLock(ThreadLocks.GAME_STATE, 0x10);
		}
		return gameData.toString();
	}
	
	private void deserializeGameData(String str){
		ThreadLocks.requestLock(ThreadLocks.GAME_STATE, 0x11);
		int index = 0;
		gameMode = (int) str.charAt(index);
		index++;
		playerTypes = Risk.deserializeIntArray(str.substring(index, index+48));
		index+=48;
		
		turn = (int) str.charAt(index);
		index++;
		
		selectedCountry = map.getCountryById(str.charAt(index));
		index++;
		
		attackTarget = map.getCountryById(str.charAt(index));
		index++;
		
		attackers = (int) str.charAt(index);
		index++;
		defenders = (int) str.charAt(index);
		index++;
		
		attackerDiceResults = Risk.deserializeIntArray(str.substring(index, index + 8 * attackers));
		index += 8 * attackers;
		
		defenderDiceResults = Risk.deserializeIntArray(str.substring(index, index + 8 * defenders));
		index += 8 * defenders;
		
		attackerDiceTimers = Risk.deserializeIntArray(str.substring(index, index + 8 * attackers));
		index += 8 * attackers;
		
		defenderDiceTimers = Risk.deserializeIntArray(str.substring(index, index + 8 * defenders));
		index += 8 * defenders;
		
		ThreadLocks.releaseLock(ThreadLocks.GAME_STATE, 0x11);
	}
	
	public void serverAdded (HostServer hs, Socket client){
		if(mode == 1 && gameType == 1){
			if(setupMode == -1){
				System.out.println(client.getInetAddress() + " connected");
				String message = "" + (char) 4 + (char) 0 + (char) numPlayers;
				hs.writeMessage(message);
				playerAdded(2);
			}
		}
	}

	private void playerAdded(int type){
		playerTypes[numPlayers] = type;
		numPlayers++;
		if(type == 1){
			numAI++;
		}
		if(numPlayers == 6){
			doneReceiving();
		}
	}
	
	private void doneReceiving(){
		master.setAcceptingPlayers(false);
		setupMode = 2;
		
		transmitPlayerTypes();
	}
	
	private void transmitPlayerTypes(){
		String message = "" + (char) 4 + (char) + 1;
		message += (char) numPlayers;
		message += Risk.serializeIntArray(playerTypes);
		this.message(message,-5);
	}
	
	private void incrementTurn() {
		turn++;
		turn %= numPlayers;
	}

	public boolean propogateMessage(){
		if(mode == 1){
			if(setupMode < 2){
				return false;
			}
		}
		return true;
	}
	
	// SETTERS AND GETTERS
	public List<Button> getButtonList() {
		switch (mode) {
		case 0:
			return titleScreenButtons;
		case 1:
			switch (setupMode) {
			case 1:
				return numberButtons;
			case 2:
				return colourButtons;
			case -1:
				if(numPlayers < 3){
					return receivingPlayersButtonsNoEndT;
				}else{
					return receivingPlayersButtons;
				}
			}
			break;
		case 2:
			switch (gameMode) {
			case 1:
				if (currentArmy().getFreeUnits() > 0
						|| currentArmy().getCards().size() == 5) {
					return cardsList;
				} else {
					return cardsAndEndTurn;
				}
			case 2:
			case 5:
				return endTurnList;
			case 3:
				return battleButtonList;
			case 4:
				return null;

			}
			break;
		case 3:
			return newGameList;
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

	public Army currentArmy() {
		return armies.get(turn);
	}

	public Army defendingArmy() {
		try {
			return attackTarget.getUnit().getArmy();
		} catch (NullPointerException e) {
		}
		return null;
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

	public boolean isRunning() {
		return running;
	}

	public int getGameMode() {
		return gameMode;
	}

	public int getMode() {
		return mode;
	}

	public int getSetupMode() {
		return setupMode;
	}
	

	public int getAttackers() {
		return attackers;
	}
	
	public void halt(){
		
	}

	public int getGameType() {
		return gameType;
	}
}
