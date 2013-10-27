package risk.game;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
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
		countries = new ArrayList<Country>();
		initCountries();
	}
	
	private void initCountries(){
		for(int )
	}
	
	public BufferedImage getTexture() {
		return texture;
	}

	public void setTexture(BufferedImage texture) {
		this.texture = texture;
	}
	
	public Country getCountryById(int id){
		for(Country c : countries){
			if(c.getId() == id){
				return c;
			}
		}
		return null;
	}
	
	public Country getCountryByColor(int color){
		for(Country c : countries){
			if(c.getClickColor() == color){
				return c;
			}
		}
		return null;
	}
}
