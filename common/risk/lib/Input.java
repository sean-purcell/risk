package risk.lib;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import risk.Risk;
import risk.game.Country;
import risk.game.Game;

import static risk.Risk.DEBUG;

public class Input implements KeyListener, MouseListener, MouseMotionListener {

	private static final String clickMapAddress = "resources/clickMap.png";

	/**
	 * The id to be used when requesting locks from ThreadHandler
	 */
	private final int THREAD_ID = 2;

	private Game g;

	public int pastX;
	public int pastY;

	private BufferedImage clickMap = Risk.loadImage(clickMapAddress);

	private Country lastC;

	public Input(Game g) {
		this.g = g;
	}

	// SPECIFIC HANDLING METHODS
	private Country getClickedCountry(int x, int y, boolean debug) {
		if (y <= 615) {
			int colour = clickMap.getRGB(x, y);
			colour += 0x1000000; // Shift values to match the range 0x000000 to
									// 0xffffff
			if (debug)
				System.out.println(Integer.toString(colour, 16));
			Country country = g.getMap().getCountryByColour(colour);
			return country;
		}
		return null;
	}

	private Button getClickedButton(int x, int y) {
		if (g.getButtonList() != null) {
			for (Button b : g.getButtonList()) {
				if (b.overlaps(x, y)) {
					System.out.println(b + " pressed");
					return b;
				}
			}
		}
		return null;
	}

	// LISTENER METHOD IMPLMENTATIONS
	@Override
	public void mouseDragged(MouseEvent e) {
		// System.out.println("Mouse Dragged: " + "(" + pastX + "," + pastY +
		// ") -> (" + e.getX() + "," + e.getY() + ")");
		this.pastX = e.getX();
		this.pastY = e.getY();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// System.out.println("Mouse Moved: " + "(" + pastX + "," + pastY +
		// ") -> (" + e.getX() + "," + e.getY() + ")");
		this.pastX = e.getX();
		this.pastY = e.getY();
		Country c = getClickedCountry(pastX, pastY, false);
		if (c != lastC) {
			System.out.println(c);
			lastC = c;
		}

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		System.out.println("Mouse Clicked: (" + x + "," + y + ")");
		if (e.getButton() == MouseEvent.BUTTON1) {
			Country c = getClickedCountry(x, y, true);
			if (c != null) {
				// g.countryClicked(c, x, y);
				String message = "" + (char) 2;
				message += (c.getId() < 10 ? "0" : "")
						+ Integer.toString(c.getId());
				g.message(message, 1);
				return;
			}
			Button b = getClickedButton(x, y);
			if (b != null) {
				String message = "" + (char) 1;
				message += (char) b.getId();
				g.message(message, 1);
				return;
			}

			String message = "" + (char) 3;
			g.message(message, 1);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (!DEBUG) {
			return;
		}
		if (e.getKeyChar() == 'c') {
			for (int j = 0; j < 200; j++) {
				int i = Risk.r.nextInt(42) + 1;
				String message = "" + (char) 2;
				message += (i < 10 ? "0" : "") + Integer.toString(i);
				g.message(message, -1);
				System.out.println("Random country chosen");
			}
		} else if (e.getKeyChar() == 'n') {
			String message = "" + (char) 1 + (char) 99;
			g.message(message, -1);
		} else if (e.getKeyChar() == 'b') {
			String message = "" + (char) 1 + (char) 6;
			g.message(message, -1);
		} else if (e.getKeyChar() == 'w') {
			String message = "" + (char) 0x10 + (char) 1;
			g.message(message, -1);
		} else if (e.getKeyChar() == 'a') {
			String message = "" + (char) 0x10 + (char) 2;
			g.message(message, -1);
		}

	}

	@Override
	public void keyPressed(KeyEvent e) {

	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

}
