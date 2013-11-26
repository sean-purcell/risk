package ibur.risk.lib;

import ibur.risk.Risk;
import ibur.risk.game.Game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * 
 * @author Sean
 * 
 */
public class RiskCanvas extends JPanel {

	private static BufferedImage soldier;

	private JFrame frame;

	public Game game;

	private Input input;

	public Font army;
	private final String armyFontAddress = "resources/Ver_Army.ttf";

	private BufferedImage buffer;
	private Graphics2D bufferG;

	public RiskCanvas(Game game, Input i) {
		frame = new JFrame("Risk");

		frame.getContentPane().add(this);
		frame.setBounds(new Rectangle(1280, 720));
		frame.getContentPane().setMinimumSize(new Dimension(1280, 720));
		this.setBounds(0, 0, 1280, 720);
		this.setMinimumSize(new Dimension(1280, 720));

		frame.setVisible(true);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setBackground(Color.WHITE);
		frame.getContentPane().setBackground(Color.WHITE);

		this.addMouseListener(i);
		this.addKeyListener(i);
		this.addMouseMotionListener(i);

		frame.addMouseListener(i);
		frame.addKeyListener(i);
		frame.addMouseMotionListener(i);

		frame.setBackground(Color.WHITE);

		soldier = Risk.loadImage("resources/soldier.png");
		this.input = i;
		initFont();

		buffer = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_ARGB);
		bufferG = (Graphics2D) buffer.getGraphics();

		this.game = game;
	}

	private void initFont() {
		this.army = Risk.loadFont(armyFontAddress).deriveFont(36f);
	}

	public void resize() {
		if (frame.getContentPane().getBounds().getHeight() != 720) {
			Dimension r = new Dimension(1280, 720);
			// this.setMinimumSize(r);
			this.setPreferredSize(r);
			frame.getContentPane().setPreferredSize(r);
			// frame.getContentPane().setMinimumSize(r);
			frame.pack();
			System.out.println("Resized");
		} // Ensures that the frame is at the correct size
	}

	@Override
	public void update(Graphics g) {
		super.update(g);
	}

	@Override
	public void paintComponent(Graphics g) {
		if (ThreadLocks.checkLock(ThreadLocks.INIT_RESOURCES) != 0) {
			return;
		}
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 1280, 720);
		g.setFont(army);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

		// If resources have not been initialized yet, don't draw
		if (ThreadLocks.checkLock(ThreadLocks.INIT_RESOURCES) != 0) {
			return;
		}

		game.draw((Graphics2D) g);
	}

	public void createFrame() { // Creates the frame all at once in an offscreen
								// buffer to avoid screen flicker
		bufferG.setColor(Color.WHITE);
		bufferG.fillRect(0, 0, 1280, 720);
		bufferG.setFont(army);
		bufferG.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

		// If resources have not been initialized yet, don't draw
		if (ThreadLocks.checkLock(ThreadLocks.INIT_RESOURCES) != 0) {
			return;
		}

		game.draw(bufferG);
	}

	/**
	 * Override of {@code Component.hasFocus}, checks if this canvas has focus
	 * or if the underlying frame has focus
	 */
	@Override
	public boolean hasFocus() {
		return frame.hasFocus() || super.hasFocus();
	}
}
