package risk.lib;

import java.awt.image.BufferedImage;


public class Button implements Clickable, Drawable{
	
	private int x;
	private int y;
	
	private BufferedImage texture;
	
	public boolean overlap(int x,int y){
		return this.x <= x && this.x + texture.getWidth(null) > x && this.y <= y && this.y + texture.getHeight(null) > y;
	}
	
	public BufferedImage getTexture(){
		return texture;
	}
}
