package risk.lib;

import java.awt.image.BufferedImage;


public class Button implements Clickable, Drawable{
	
	private int x;
	private int y;
	
	private BufferedImage texture;
	
	public Button(int x, int y, BufferedImage texture) {
		super();
		this.x = x;
		this.y = y;
		this.texture = texture;
	}

	@Override
	public boolean overlaps(int x,int y){
		return this.x <= x && this.x + texture.getWidth(null) > x && this.y <= y && this.y + texture.getHeight(null) > y;
	}
	
	public BufferedImage getTexture(){
		return texture;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}
}
