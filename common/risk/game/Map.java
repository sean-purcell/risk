package risk.game;

import java.awt.image.BufferedImage;
import java.util.List;

import risk.Risk;

public class Map {
	/**
	 * The texture for the map
	 */
	private BufferedImage texture;
	
	/**
	 * The list of Country's within the map
	 */
	private List<Country> countries;

	public Map(){
		texture = Risk.loadImage("resources/map.png");
	}
	
	public BufferedImage getTexture() {
		return texture;
	}

	public void setTexture(BufferedImage texture) {
		this.texture = texture;
	}
}
