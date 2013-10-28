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
		countries.add(null); //This is so that the index matches up with the location in the array
		for(int i = 1; i <= Country.numCountries; i++){
			countries.add(new Country(i));
		}
		for(Country c : countries){
			if(c != null)
				c.initConnections(this);
		}
		System.out.println();
	}
	
	public BufferedImage getTexture() {
		return texture;
	}

	public void setTexture(BufferedImage texture) {
		this.texture = texture;
	}
	
	public Country getCountryById(int id){
		return countries.get(id);
	}
	
	public Country getCountryByColour(int colour){
		for(int i = 1; i < countries.size(); i++){
			Country c = countries.get(i);
			if(c.getClickColour() == colour){
				return c;
			}
		}
		return null;
	}

	public List<Country> getCountries() {
		return countries;
	}

	public void setCountries(List<Country> countries) {
		this.countries = countries;
	}
}
