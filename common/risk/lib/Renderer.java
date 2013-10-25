package risk.lib;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JFrame;

import risk.game.Game;

/**
 * 
 * @author Sean
 *
 */
public class Renderer extends Canvas{
	
	private JFrame frame;
	
	private Game game;
	
	public Renderer(Game game,Input i){
		frame = new JFrame("Risk");
		frame.setPreferredSize(new Dimension(1280,720));
		
		frame.getContentPane().add(this);
		this.setBounds(new Rectangle(1280,720));
		
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.addMouseListener(i);
		this.addKeyListener(i);
		this.addMouseMotionListener(i);
		
		this.game = game;
	}
	
	public void paint(Graphics graphics){
		Graphics2D g = null;
		try{
			g = (Graphics2D) graphics;
		}
		catch(ClassCastException e){
			System.err.println("Graphics is not a Graphics2D instance");
			return;
		}
		game.draw(g);
	}
	
	public void drawMap(Graphics2D g){
		g.drawImage(game.getMap().getTexture(),0,0,null);
	}
	
	public static void draw(Drawable d,Graphics g){
		g.drawImage(d.getTexture(),d.getX(),d.getY(),null);
	}
}
