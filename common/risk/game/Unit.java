package risk.game;

import java.awt.Graphics2D;

public class Unit {
	
	private int troops;
	
	/**
	 * Represents the last direction of movement for the unit
	 */
	private int dir = 1;
	
	
	/**
	 * Represents the army this unit is associated wit
	 */
	private Army army;
	
	/**
	 * The current location of the unit.  Should not be null except for during initialization
	 */
	private Country location;
	
	public Unit(int troops, Army army, Country location){
		this.troops = troops;
		this.setArmy(army);
		this.setLocation(location);
	}
	
	public void drawSelf(Graphics2D g,int x,int y){
		
	}
	
	public Country getLocation() {
		return location;
	}

	public void setLocation(Country location) {
		this.location = location;
	}

	public Army getArmy() {
		return army;
	}

	public void setArmy(Army army) {
		this.army = army;
	}
}
