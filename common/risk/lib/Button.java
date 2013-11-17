package risk.lib;

import java.awt.image.BufferedImage;

public class Button implements Drawable {

	private int x;
	private int y;

	private int w;
	private int h;
	
	private BufferedImage texture;

	private int id;
	
	public Button(int x, int y, BufferedImage texture, int id) {
		this(x,y,texture.getWidth(),texture.getHeight(),id);
		this.texture = texture;
	}
	
	public Button(int x, int y, int w, int h, int id){
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.id = id;
	}

	public boolean overlaps(int x, int y) {
		return this.x <= x && this.x + w > x
				&& this.y <= y && this.y + h > y;
	}

	public BufferedImage getTexture() {
		return texture;
	}

	public boolean matches(int i) {
		return id == i;
	}

	public int getId() {
		return id;
	}

	public String toString() {
		return Integer.toString(id);
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}
	
	public void setWidth(int w){
		this.w = w;
	}
}
