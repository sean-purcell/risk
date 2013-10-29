package risk.game;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import risk.Risk;

/**
 * Represents an
 * 
 * @author Gabriel Ip
 * @author Sean Purcell
 * @author Miranda Zhou
 *
 */
public class Army {
	
	private static BufferedImage[] unadjustedUnitTextures;
	private static final String[]  unitTexturesAddress = {"resources/soldier.png","resources/tank.png","resources/soldier.png"};
	
	private static BufferedImage soldierBattleTexture;
	private static final String soldierBattleTextureAddress = "resources/soldierBattle.png";
	
	static{
		unadjustedUnitTextures = new BufferedImage[3];
		for(int i = 0; i < 3; i++){
			unadjustedUnitTextures[i] = Risk.loadImage(unitTexturesAddress[i]);
		}
		
		soldierBattleTexture = Risk.loadImage(soldierBattleTextureAddress);
	}
	
	private List<Unit> units;
	
	private int freeUnits;
	
	/**
	 * The type of this army:
	 * 0 = Red
	 * 1 = Blue
	 * 2 = Black
	 * 3 = Green
	 * 4 = Yellow
	 * 5 = Orange
	 */
	private int type;
	
	private BufferedImage[][] unitTextures;
	private BufferedImage soldierAttacker;
	private BufferedImage soldierDefender;
	
	public Army(int type){
		units = new ArrayList<Unit>();
		this.setType(type);
		initUnitTextures();
	}
	
	private void initUnitTextures(){
		unitTextures = new BufferedImage[3][2];
		for(int i = 0; i < 3; i++){
			unitTextures[i][0] = Risk.changeImageColour(unadjustedUnitTextures[i], this.getColour());
			unitTextures[i][1] = Risk.flipImage(unitTextures[i][0], true, false);
		}
		soldierAttacker = Risk.changeImageColour(soldierBattleTexture,this.getColour());
		soldierDefender = Risk.flipImage(soldierAttacker, true, false);
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
	
	public Color getColour(){
		Color c = null;
		switch(type){
		case 0: c = Color.RED; break;
		case 1: c = Color.BLUE; break;
		case 2: c = Color.BLACK; break;
		case 3: c = Color.GREEN; break;
		case 4: c = Color.YELLOW; break;
		case 5: c = Color.ORANGE; break;
		}
		return c;
	}
	
	public String getName(){
		String n = null;
		switch(type){
		case 0: n = "RED"; break;
		case 1: n = "BLUE"; break;
		case 2: n = "BLACK"; break;
		case 3: n = "GREEN"; break;
		case 4: n = "YELLOW"; break;
		case 5: n = "ORANGE"; break;
		}
		return n;
	}

	public BufferedImage[][] getUnitTextures() {
		return unitTextures;
	}

	public void setUnitTextures(BufferedImage[][] unitTextures) {
		this.unitTextures = unitTextures;
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
	
}
