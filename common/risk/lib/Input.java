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

public class Input implements KeyListener, MouseListener, MouseMotionListener {
	
	private static final String clickMapAddress = "resources/clickMap.png";
	
	/**
	 * The id to be used when requesting locks from ThreadHandler
	 */
	private final int THREAD_ID = 2;
	
	private Game g;
	
	private int pastX;
	private int pastY;
	
	private BufferedImage clickMap = Risk.loadImage(clickMapAddress);
	
	private Country lastC;
	
	public Input(Game g){
		this.g = g;
	}
	
	// SPECIFIC HANDLING METHODS
	private Country getClickedCountry(int x,int y){
		if(y <= 615){
			int colour = clickMap.getRGB(x, y);
			colour += 0x1000000; //Shift values to match the range 0x000000 to 0xffffff
//			System.out.println(Integer.toString(colour,16));
			Country country = g.getMap().getCountryByColour(colour);
			if(country != null){
//				System.out.println(country);
			}
			return country;
		}
		return null;
	}
	
	// LISTENER METHOD IMPLMENTATIONS
	@Override
	public void mouseDragged(MouseEvent e) {
		//System.out.println("Mouse Dragged: " + "(" + pastX + "," + pastY + ") -> (" + e.getX() + "," + e.getY() + ")");
		this.pastX = e.getX();
		this.pastY = e.getY();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		//System.out.println("Mouse Moved: " + "(" + pastX + "," + pastY + ") -> (" + e.getX() + "," + e.getY() + ")");
		this.pastX = e.getX();
		this.pastY = e.getY();
		Country c = getClickedCountry(pastX,pastY);
		if(c != lastC){
			System.out.println(c);
			lastC = c;
		}
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		System.out.println("Mouse Clicked: (" + x + "," + y + ")");
		if(e.getButton() == MouseEvent.BUTTON1){
			getClickedCountry(x,y);
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
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
	}

}
