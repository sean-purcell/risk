package risk.game;

import static risk.game.Unit.images;
import static risk.game.Unit.sprites;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import risk.Risk;

public class Card {
	private static Color BEIGE = new Color(250,214,165);
	
	private static BufferedImage texture;
	private static String textureAddress = "resources/cardTexture.png";
	
	private static FontMetrics metrics;
	
	static{
	//	texture = Risk.loadImage(textureAddress);
	}
	
	// 0 = soldier, 1 = tank, 2 = jet
	private int type;
	
	public Card() {
		type = Risk.r.nextInt(3);
	}
	
	public void draw(Graphics2D g, int index){
		if(metrics == null){
			metrics = g.getFontMetrics(sprites.deriveFont(30f));
		}
		g.setColor(Color.BLACK);
		g.fillRoundRect(450+index*90,575,100,200,10,10);
		g.setColor(BEIGE);
		g.fillRoundRect(455+index * 90, 580, 90, 200, 10, 10);
		g.setFont(sprites.deriveFont(30f));
		g.setColor(Color.BLACK);
		int x = 500 - metrics.charWidth(images[0][type])/2;
		int y = 600 + metrics.getHeight();
		g.drawString(Character.toString(images[0][type]), x, y);
	}

	public int getType() {
		return type;
	}
}
