package risk.game;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import risk.Risk;

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
	
	static{
		soldierAttackTexture = Risk.loadImage(soldierBattleAddress);
		soldierDefendTexture = Risk.flipImage(soldierAttackTexture, true, false);
	}
	
	// Colour constants for when java.awt.Color does not have a good enough colour
	private static final Color ORANGE = new Color(224,158,  0);
	private static final Color PURPLE = new Color(153, 17,153);
	private static final Color GREEN  = Color.GREEN.darker().darker();
	
	private List<Unit> units;

	private int freeUnits;
	
	/**
	 * The type of this army: 0 = Red 1 = Blue 2 = Grey 3 = Green 4 = Purple 5 =
	 * Orange
	 */
	private int type;

	/**
	 * Pointer to the game object to get necessary data
	 */
	private Game g;
	
	private BufferedImage soldierAttacker;
	private BufferedImage soldierDefender;

	public Army(int type, Game g) {
		units = new ArrayList<Unit>();
		this.setType(type);
		this.g = g;
		
		soldierAttacker = Risk.changeImageColour(soldierAttackTexture, getColour());
		soldierDefender = Risk.changeImageColour(soldierDefendTexture, getColour());
	}

	public int calculateContinentBonus(){
		int bonus = 0;
		Map map = g.getMap();
		int numContinents = map.getNumContinents();
		for(int i = 0; i < numContinents; i++){
			boolean ownAll = true;
			List<Country> cont = map.getContinent(i+1);
			for(int j = 0; j < cont.size() && ownAll; j++){
				if(cont.get(j).getUnit().getArmy() != this){
					ownAll = false;
				}
			}
			if(ownAll){
				bonus += map.getContinentBonus(i+1);
			}
		}
		System.out.println("Bonus: " + bonus);
		return bonus;
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
