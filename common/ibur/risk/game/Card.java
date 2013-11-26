package ibur.risk.game;

import static ibur.risk.game.Unit.images;
import static ibur.risk.game.Unit.sprites;
import ibur.risk.Risk;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Card {
	private static Color BEIGE = new Color(250, 214, 165);

	private static BufferedImage texture;
	private static String textureAddress = "resources/cardTexture.png";

	private static FontMetrics metrics;

	static {
		// texture = Risk.loadImage(textureAddress);
	}

	// 0 = soldier, 1 = tank, 2 = jet
	private int type;

	public Card() {
		this(Risk.r.nextInt(3));
	}

	public Card(int t) {
		this.type = t;
	}

	public void draw(Graphics2D g, int index) {
		if (metrics == null) {
			metrics = g.getFontMetrics(sprites.deriveFont(30f));
		}
		g.setColor(Color.BLACK);
		g.fillRoundRect(450 + index * 90, 625, 100, 200, 10, 10);
		g.setColor(BEIGE);
		g.fillRoundRect(455 + index * 90, 630, 90, 200, 10, 10);
		g.setFont(sprites.deriveFont(30f));
		g.setColor(Color.BLACK);
		int x = 500 - metrics.charWidth(images[type][0]) / 2 + index * 90;
		int y = 650 + metrics.getHeight() / 2;
		g.drawString(Character.toString(images[type][0]), x, y);
	}

	public int getType() {
		return type;
	}

	@Override
	public String toString() {
		return "Card:" + type;
	}
}
