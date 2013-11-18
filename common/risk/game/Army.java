package risk.game;

import static risk.Risk.DEBUG;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import risk.Risk;
import risk.ai.AI;
import risk.ai.BasicAI;

/**
 * Represents an Army
 * 
 * @author Gabriel Ip
 * @author Sean Purcell
 * @author Miranda Zhou
 * 
 */
public class Army {

	private static BufferedImage soldierAttackTexture;
	private static BufferedImage soldierDefendTexture;
	private static final String soldierBattleAddress = "resources/soldierBattle.png";

	static {
		soldierAttackTexture = Risk.loadImage(soldierBattleAddress);
		soldierDefendTexture = Risk
				.flipImage(soldierAttackTexture, true, false);
	}

	// Colour constants for when java.awt.Color does not have a good enough
	// colour
	private static final Color ORANGE = new Color(224, 158, 0);
	private static final Color PURPLE = new Color(153, 17, 153);
	private static final Color GREEN = Color.GREEN.darker().darker();

	private List<Unit> units;

	private List<Card> cards;

	private int freeUnits;

	/**
	 * The type of this army: 0 = Red 1 = Blue 2 = Grey 3 = Green 4 = Purple 5 =
	 * Orange
	 */
	private int type;

	// 0 = locally controlled, 1 = AI controlled, 2 = non-local
	private int controller;
	private AI ai;
	
	/**
	 * Pointer to the game object to get necessary data
	 */
	private Game g;

	private BufferedImage soldierAttacker;
	private BufferedImage soldierDefender;

	public Army(int type, Game g, int controller) {
		units = new ArrayList<Unit>();
		cards = new ArrayList<Card>();
		this.setType(type);
		this.g = g;
		
		this.controller = controller;
		if(controller == 1){
			ai = new BasicAI(g, this);
		}

		soldierAttacker = Risk.changeImageColour(soldierAttackTexture,
				getColour());
		soldierDefender = Risk.changeImageColour(soldierDefendTexture,
				getColour());
	}

	public void enterTurn(){
		if(controller == 1){
			ai.activate();
		}
	}
	
	public void exitTurn(){
		if(controller == 1){
			ai.deactivate();
		}
	}
	
	public int calculateContinentBonus() {
		int bonus = 0;
		Map map = g.getMap();
		int numContinents = map.getNumContinents();
		for (int i = 0; i < numContinents; i++) {
			boolean ownAll = true;
			List<Country> cont = map.getContinent(i + 1);
			for (int j = 0; j < cont.size() && ownAll; j++) {
				if (cont.get(j).getUnit().getArmy() != this) {
					ownAll = false;
				}
			}
			if (ownAll) {
				bonus += map.getContinentBonus(i + 1);
			}
		}
		System.out.println("Bonus: " + bonus);
		return bonus;
	}

	public void addCard() {
		if (cards.size() >= 5 && !DEBUG) {
			return;
		}
		cards.add(new Card());
	}

	public List<Card> getCards() {
		return cards;
	}

	public boolean useCards() {
		boolean oneEach = true;
		List<Card> first = new ArrayList<Card>();
		for (int i = 0; i < 3; i++) { // Checks if there are at three of a
										// certain type
			List<Card> subCards = getCardsOf(i);
			if (subCards.size() >= 3) {
				for (int j = 0; j < 3; j++) {
					cards.remove(subCards.get(j));
				}
				return true;
			}
			if (subCards.size() != 0) {
				first.add(subCards.get(0));
			} else {
				oneEach = false;
			}
		}

		if (oneEach) { // Otherwise if there was at least one of each type
			for (Card c : first) {
				cards.remove(c);
			}
			return true;
		}
		return false;

	}

	private List<Card> getCardsOf(int type) { // Gets the cards for this army of
												// a certain type
		List<Card> copy = (List<Card>) ((ArrayList<Card>) cards).clone();
		Iterator<Card> i = copy.listIterator(); // I didnt have to use iterators
												// but why not
		while (i.hasNext()) {
			if (i.next().getType() != type) {
				i.remove();
			}
		}
		return copy;
	}

	public int getFreeUnits() {
		return freeUnits;
	}

	public void setFreeUnits(int freeUnits) {
		this.freeUnits = freeUnits;
	}

	public List<Unit> getUnits() {
		return units;
	}

	public void setUnits(List<Unit> units) {
		this.units = units;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Color getColour() {
		return getColorByType(type);
	}

	public String getName() {
		String n = null;
		switch (type) {
		case 0:
			n = "RED";
			break;
		case 1:
			n = "BLUE";
			break;
		case 2:
			n = "GREY";
			break;
		case 3:
			n = "GREEN";
			break;
		case 4:
			n = "PURPLE";
			break;
		case 5:
			n = "ORANGE";
			break;
		}
		return n;
	}

	public BufferedImage getSoldierAttacker() {
		return soldierAttacker;
	}

	public void setSoldierAttacker(BufferedImage soldierAttacker) {
		this.soldierAttacker = soldierAttacker;
	}

	public BufferedImage getSoldierDefender() {
		return soldierDefender;
	}

	public void setSoldierDefender(BufferedImage soldierDefender) {
		this.soldierDefender = soldierDefender;
	}

	public static Color getColorByType(int type) {
		Color c = null;
		switch (type) {
		case 0:
			c = Color.RED;
			break;
		case 1:
			c = Color.BLUE;
			break;
		case 2:
			c = Color.DARK_GRAY;
			break;
		case 3:
			c = GREEN;
			break;
		case 4:
			c = PURPLE;
			break;
		case 5:
			c = ORANGE;
			break;
		}
		return c;
	}
}
