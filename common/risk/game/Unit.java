package risk.game;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Unit {
	
	private static final int JET_TROOPS = 15;
	private static final int TANK_TROOPS = 5;
	
	private static char[][] images = new char[][]{{'[','{'},{'i','I'},{'v','V'}};
	private static Font sprites;
	private static FontMetrics spritesMetrics;
	
	static{
		try {
			sprites = Font.createFont(Font.TRUETYPE_FONT, new File("resources/militaryRPG.ttf")).deriveFont(10f);
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
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
		Font original = g.getFont();
		g.setFont(sprites);
		g.setColor(army.getColour());
		spritesMetrics = g.getFontMetrics();
		
		BufferedImage[][] sprites = army.getUnitTextures();
		
		int troopsLeft = troops; // Represents the number of troops left to be accounted for
		int planes = troopsLeft/JET_TROOPS; troopsLeft %= JET_TROOPS;
		int tanks = troopsLeft/TANK_TROOPS; troopsLeft %= TANK_TROOPS;
		int soldiers = troopsLeft;
		
		int[] numSprites = new int[]{planes,tanks,soldiers};
		
		//If dir is one, ie. moving left, get the first element from the arrays, else the second
		int index = dir == 1 ? 0 : 1;


		for(int i = 0; i < tanks; i++){
			int drawXOffset = ((i - tanks/2) * spritesMetrics.charWidth(images[1][index]) + 1);
			if(planes != 0){
				drawXOffset += (drawXOffset < 0 ? -1 : 1) * 5;
			}
			int drawX = x + drawXOffset;
			int drawY = y;
			g.drawString(Character.toString(images[1][index]),drawX,drawY);
		}
		for(int i = 0; i < soldiers; i++){
			int drawX = x + ((i - soldiers/2) * spritesMetrics.charWidth(images[0][index]) + 1);
			int drawY = y + (planes != 0 || tanks != 0 ? 12 : 2);
			g.drawString(Character.toString(images[0][index]),drawX,drawY);
		}
		for(int i = 0; i < planes; i++){
			int drawX = x + ((i - planes / 2) * spritesMetrics.charWidth(images[2][index])+1);
			int drawY = y -6;
			g.drawString(Character.toString(images[2][index]),drawX,drawY);
		}

		
		g.setFont(original);
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
