package ibur.risk.game;

import ibur.risk.Risk;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class Unit {

	private static final int JET_TROOPS = 15;
	private static final int TANK_TROOPS = 5;

	// Non private so it can be accessed from Card
	// Not the most elegant but it works
	static char[][] images = new char[][] { { '[', '{' }, { 'i', 'I' },
			{ 'v', 'V' } };
	static Font sprites;
	static FontMetrics spritesMetrics;

	static {
		sprites = Risk.loadFont("resources/militaryRPG.ttf");
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
	 * The current location of the unit. Should not be null except for during
	 * initialization
	 */
	private Country location;

	public Unit(int troops, Army army, Country location) {
		this.troops = troops;
		this.setArmy(army);
		this.setLocation(location);
	}

	public void drawSelf(Graphics2D g) {
		this.drawSelf(g, location.getX(), location.getY());
	}

	// Big ugly method that draws a unit to the given graphics context
	public void drawSelf(Graphics2D g, int x, int y) {
		Font original = g.getFont();
		g.setFont(sprites.deriveFont(20f));
		g.setColor(army.getColour());
		spritesMetrics = g.getFontMetrics();

		int troopsLeft = troops; // Represents the number of troops left to be
									// accounted for
		int planes = troopsLeft / JET_TROOPS;
		troopsLeft %= JET_TROOPS;
		int tanks = troopsLeft / TANK_TROOPS;
		troopsLeft %= TANK_TROOPS;
		int soldiers = troopsLeft;

		int[] numSprites = new int[] { planes, tanks, soldiers };

		// If dir is one, ie. moving left, get the first element from the
		// arrays, else the second
		int index = dir == 1 ? 0 : 1;

		g.setFont(sprites.deriveFont(17f));
		for (int i = 0; i < tanks; i++) {
			int drawXOffset = ((i - tanks / 2)
					* spritesMetrics.charWidth(images[1][index]) + 1);
			if (planes != 0) {
				drawXOffset += (drawXOffset < 0 ? -1 : 1) * 5;
			}
			int drawX = x + drawXOffset;
			int drawY = y;
			g.drawString(Character.toString(images[1][index]), drawX, drawY);
		}
		g.setFont(sprites.deriveFont(20f));
		for (int i = 0; i < soldiers; i++) {
			int drawX = x
					+ ((i - soldiers / 2)
							* spritesMetrics.charWidth(images[0][index]) + 1);
			int drawY = y + (planes != 0 || tanks != 0 ? 20 : 2);
			g.drawString(Character.toString(images[0][index]), drawX, drawY);
		}
		for (int i = 0; i < planes; i++) {
			int drawX = x
					+ ((i - planes / 2)
							* spritesMetrics.charWidth(images[2][index]) + 1);
			int drawY = y - 14;
			g.drawString(Character.toString(images[2][index]), drawX, drawY);
		}

		g.setFont(original);
	}

	public int getTroops() {
		return troops;
	}

	public void incrementTroops() {
		this.incrementTroops(1);
	}

	public void incrementTroops(int i) {
		troops += i;
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
