package ibur.risk.lib;

import ibur.risk.Risk;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class DiceTexture {

	private static BufferedImage[] dieTextures;

	// Initialize the dice textures
	static {
		final int width = 50;
		final int height = 50;

		dieTextures = new BufferedImage[6];

		BufferedImage base = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics baseG = base.getGraphics();

		((Graphics2D) baseG).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);

		baseG.setColor(Color.WHITE);
		baseG.fillRect(0, 0, width, height);
		baseG.setColor(Color.BLACK);
		baseG.drawRoundRect(0, 0, width - 1, height - 1, 10, 10);

		for (int i = 1; i <= 6; i++) {
			BufferedImage clone = Risk.cloneImage(base);
			Graphics2D g = (Graphics2D) clone.getGraphics();

			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);

			g.setColor(Color.BLACK);
			drawDiceTexture(g, i);

			dieTextures[i - 1] = clone;
			System.out.println(i + "buttonmade");
		}
	}

	private static void drawDiceTexture(Graphics2D g, int i) {
		System.out.println(i);
		switch (i) {
		case 1:
			g.fillOval(20, 20, 10, 10);
			break;
		case 2:
			g.fillOval(5, 5, 10, 10);
			g.fillOval(35, 35, 10, 10);
			break;
		case 3:
			g.fillOval(5, 5, 10, 10);
			g.fillOval(35, 35, 10, 10);
			g.fillOval(20, 20, 10, 10);
			break;
		case 5:
			g.fillOval(20, 20, 10, 10);
		case 4:
			g.fillOval(5, 5, 10, 10);
			g.fillOval(35, 35, 10, 10);
			g.fillOval(5, 35, 10, 10);
			g.fillOval(35, 5, 10, 10);
			break;
		case 6:
			g.fillOval(5, 5, 10, 10);
			g.fillOval(35, 35, 10, 10);
			g.fillOval(5, 35, 10, 10);
			g.fillOval(35, 5, 10, 10);
			g.fillOval(5, 20, 10, 10);
			g.fillOval(35, 20, 10, 10);
			break;
		}
	}

	public static BufferedImage getDieTexture(int face) {
		try {
			return dieTextures[face - 1];
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}
}
