package risk.lib;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

import risk.Risk;
import risk.game.Army;
import risk.game.Country;
import risk.game.Game;
import risk.game.Unit;

/**
 * 
 * @author Sean
 *
 */
public class RiskCanvas extends Canvas{
	
	private static BufferedImage soldier;
	
	private JFrame frame;
	
	public Game game;
	
	private Input input;
	
	public Font army;
	private final String armyFontAddress = "resources/Ver_Army.ttf";
	
	public RiskCanvas(Game game,Input i){
		frame = new JFrame("Risk");
		
		frame.getContentPane().add(this);
		frame.setBounds(new Rectangle(1280,720));
		frame.getContentPane().setBounds(0,0,1280,720);
		this.setBounds(new Rectangle(1280,720));
		
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.setBackground(Color.WHITE);
		
		this.addMouseListener(i);
		this.addKeyListener(i);
		this.addMouseMotionListener(i);
		
		soldier = Risk.loadImage("resources/soldier.png");
		this.input = i;
		initFont();
		
		this.game = game;
	}
	
	private void initFont(){
		try {
			this.army = Font.createFont(Font.TRUETYPE_FONT, new File(armyFontAddress));
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.army = this.army.deriveFont(36f);
	}
	
	public void paint(Graphics graphics){
		if(frame.getContentPane().getBounds().getHeight() != 720){
			Rectangle r = new Rectangle(1280,720);
			frame.getContentPane().setBounds(r);
			this.setBounds(r);
			frame.pack();
			System.out.println("Resized");
		} //Ensures that the frame is at the correct size
		
		
		if(ThreadLocks.checkLock(ThreadLocks.INIT_RESOURCES) != 0){
			return;
		}
		Graphics2D g = null;
		try{
			g = (Graphics2D) graphics;
		}
		catch(ClassCastException e){
			System.err.println("Graphics is not a Graphics2D instance");
			return;
		}
		g.setFont(army);
		game.draw(g);
	}
	
	/**
	 * Override of {@code Component.hasFocus}, checks if this canvas has focus or if the underlying frame has focus
	 */
	@Override
	public boolean hasFocus(){
		return frame.hasFocus() || super.hasFocus();
	}
}
