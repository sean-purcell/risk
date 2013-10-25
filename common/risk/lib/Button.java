package risk.lib;

import java.awt.Image;

public class Button implements Clickable, Drawable{
	
	private int x;
	private int y;
	
	private Image texture;
	
	public boolean overlap(int x,int y){
		
	}
	
	public Image getTexture(){
		return texture;
	}
}
