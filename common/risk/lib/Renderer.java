package risk.lib;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.JFrame;

import risk.game.Country;
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
		
		frame.getContentPane().add(this);
		this.setBounds(new Rectangle(1280,720));
		
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.setBackground(Color.WHITE);
		
		this.addMouseListener(i);
		this.addKeyListener(i);
		this.addMouseMotionListener(i);
		
		
		this.game = game;
	}
	
	public void paint(Graphics graphics){
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
		game.draw(g);
	}
	
	public void drawMainMode(Graphics2D g){
		this.drawMap(g);
		drawConnections(g);
	}
	
	private void drawMap(Graphics2D g){
		try{
			g.drawImage(game.getMap().getTexture(),0,0,null);
		}
		catch(NullPointerException e){}
	}
	
	private void drawConnections(Graphics2D g){
		g.setColor(Color.BLACK);
		List<Country> countries = game.getMap().getCountries();
		for(int i = 1; i < countries.size(); i++){
			Country c = countries.get(i);
			List<Country> connections = c.getConnections();
			System.out.println(c);
			System.out.println(connections);
			for(Country conn : connections){
				if(conn.getId() > c.getId() && conn.getX() != 0){
					System.out.println(c + " <-> " + conn);
					g.drawLine(c.getX(), c.getY(), conn.getX(), conn.getY());
				}
			}
		}
	}
	
	/**
	 * Override of {@code Component.hasFocus}, checks if this canvas has focus or if the underlying frame has focus
	 */
	@Override
	public boolean hasFocus(){
		return frame.hasFocus() || super.hasFocus();
	}
	
	public static void draw(Drawable d,Graphics g){
		if(d.getTexture() != null){
			g.drawImage(d.getTexture(),d.getX(),d.getY(),null);
		}
	}
}
