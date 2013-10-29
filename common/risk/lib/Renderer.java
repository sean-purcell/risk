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
import java.util.List;

import javax.swing.JFrame;

import risk.Risk;
import risk.game.Army;
import risk.game.Country;
import risk.game.Game;

/**
 * 
 * @author Sean
 *
 */
public class Renderer extends Canvas{
	
	private static BufferedImage soldier;
	
	private JFrame frame;
	
	private Game game;
	
	private Input input;
	
	private Font army;
	private final String armyFontAddress = "resources/Ver_Army.ttf";
	
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
	
	private Army a = new Army(0);
	
	public void drawSetupMode(Graphics2D g){
		this.drawMap(g);
		this.drawArmyInfo(g);
		this.drawReinforcements(g);
		//this.drawConnections(g);
		g.drawImage(a.getUnitTextures()[1][1],input.pastX+50,input.pastY+50 ,null);
		g.drawImage(a.getSoldierAttacker(), 500,500,null);
	}
	
	public void drawMainMode(Graphics2D g){
		this.drawMap(g);
		//drawConnections(g);
		g.drawString(Integer.toString(game.getFps()), 10, 20);
		this.drawArmyInfo(g);
	}
	
	private void drawMap(Graphics2D g){
		try{
			g.drawImage(game.getMap().getTexture(),0,0,null);
		}
		catch(NullPointerException e){}
	}
	
	private void drawArmyInfo(Graphics2D g){
		
		//g.drawString("ARMY FONT",100,650);
		Army a = game.getCurrentArmy();
		g.setColor(a.getColour());
		g.drawString(a.getName(),50,650);
	}
	
	private void drawReinforcements(Graphics2D g){
		Army a = game.getCurrentArmy();
		setFontSize(g, 20);
		g.drawString("Free Troops: " + a.getFreeUnits(),60,680);
	}
	
	private void drawConnections(Graphics2D g){
		g.setColor(Color.BLACK);
		List<Country> countries = game.getMap().getCountries();
		for(int i = 1; i < countries.size(); i++){
			Country c = countries.get(i);
			List<Country> connections = c.getConnections();
			//System.out.println(c);
			//System.out.println(connections);
			for(Country conn : connections){
				if(conn.getId() > c.getId() && conn.getX() != 0){
					//System.out.println(c + " <-> " + conn);
					g.drawLine(c.getX(), c.getY(), conn.getX(), conn.getY());
				}
			}
		}
	}
	
	private void setFontSize(Graphics2D g,int fontSize){
		g.setFont(g.getFont().deriveFont((float) fontSize));
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
