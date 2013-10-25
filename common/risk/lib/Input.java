package risk.lib;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import risk.game.Game;

public class Input implements KeyListener, MouseListener, MouseMotionListener {

	private Game g;
	
	private int pastX;
	private int pastY;
	
	public Input(Game g){
		this.g = g;
	}
	
	
	@Override
	public void mouseDragged(MouseEvent e) {
		System.out.println("Mouse Dragged: " + "(" + pastX + "," + pastY + ") -> (" + e.getX() + "," + e.getY() + ")");
		this.pastX = e.getX();
		this.pastY = e.getY();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		System.out.println("Mouse Moved: " + "(" + pastX + "," + pastY + ") -> (" + e.getX() + "," + e.getY() + ")");
		this.pastX = e.getX();
		this.pastY = e.getY();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
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
