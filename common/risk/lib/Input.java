package risk.lib;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import risk.Risk;
import risk.game.Game;

public class Input implements KeyListener, MouseListener, MouseMotionListener {
	
	private static final String clickMapAddress = "resources/clickMap.png";
	
	private Game g;
	
	private int pastX;
	private int pastY;
	
	private BufferedImage clickMap = Risk.loadImage(clickMapAddress);
	
	public Input(Game g){
		this.g = g;
	}
	
	// SPECIFIC HANDLING METHODS
	private void testClickedCountry(int x,int y){
		if(y <= 621){
			int c = clickMap.getRGB(x, y);
			c += 0x1000000; //Shift values to match the range 0x000000 to 0xffffff
			System.out.println(Integer.toString(c,16));
		}
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
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		System.out.println("Mouse Clicked: (" + x + "," + y + ")");
		if(e.getButton() == MouseEvent.BUTTON1){
			testClickedCountry(x,y);
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
