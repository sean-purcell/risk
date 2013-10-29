package risk.lib;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
	
	private Game game;
	
	private Input input;
	
	private Font army;
	private final String armyFontAddress = "resources/Ver_Army.ttf";
	
	public RiskCanvas(Game game,Input i){
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
	
	/**
	 * Override of {@code Component.hasFocus}, checks if this canvas has focus or if the underlying frame has focus
	 */
	@Override
	public boolean hasFocus(){
		return frame.hasFocus() || super.hasFocus();
	}
	
	public List<BufferedImage> generateNumberButtonTextures(){
		final int width = 50;
		final int height = 50;
		
		List<BufferedImage> textures = new ArrayList<BufferedImage>();
		BufferedImage base = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		Graphics baseG = base.getGraphics();
		baseG.setColor(Color.BLACK);
		baseG.fillRoundRect(0, 0, width, height, 10, 10);
		for(char i = '3'; i <= '6'; i++){
			BufferedImage clone = Risk.cloneImage(base);
			Graphics2D g = (Graphics2D) clone.getGraphics();
			g.setFont(army.deriveFont(50f));
			FontMetrics fm = g.getFontMetrics();
			g.drawString(Character.toString(i),width/2 - fm.charWidth(i)/2,height / 2 + fm.getHeight()/3);
			textures.add(clone);
			System.out.println(i+"buttonmade");
		}
		
		return textures;
	}
}
