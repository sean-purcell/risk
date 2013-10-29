package risk.game;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Unit {
	
	private static final int JET_TROOPS = 15;
	private static final int TANK_TROOPS = 5;
	
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
	
	public void drawSelf(Graphics2D g){
		this.drawSelf(g,location.getX(),location.getY());
	}
	
	public void drawSelf(Graphics2D g,int x,int y){
		BufferedImage[][] sprites = army.getUnitTextures();
		
		int troopsLeft = troops; // Represents the number of troops left to be accounted for
		int planes = troopsLeft/JET_TROOPS; troopsLeft %= JET_TROOPS;
		int tank = troopsLeft/TANK_TROOPS; troopsLeft %= TANK_TROOPS;
		int soldiers = troopsLeft;
		
		int[] numSprites = new int[]{planes,tank,soldiers};
		
		//If dir is one, ie. moving left, get the first element from the arrays, else the second
		int index = dir == 1 ? 0 : 1;
		
		if(planes == 0){
			for(int i = 0; i < tank; i++){
				int drawX = x + ((i - tank/2) * sprites[1][dir].getWidth() + 1);
				int drawY = y - sprites[1][dir].getHeight() - 3;
				g.drawImage(sprites[1][dir],drawX,drawY,null);
			}
			for(int i = 0; i < soldiers; i++){
				int drawX = x + ((i - soldiers/2) * sprites[0][dir].getWidth() + 1);
				int drawY = y;
				g.drawImage(sprites[0][dir],drawX,drawY,null);
			}
		}
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
