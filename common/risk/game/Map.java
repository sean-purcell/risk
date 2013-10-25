package risk.game;

import java.awt.Image;
import java.util.List;

public class Map {
	/**
	 * The texture for the map
	 */
	private Image texture;
	
	/**
	 * The list of Country's within the map
	 */
	private List<Country> countries;

	public Image getTexture() {
		return texture;
	}

	public void setTexture(Image texture) {
		this.texture = texture;
	}
}
