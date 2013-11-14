package risk.game;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import risk.Risk;

public class Map {

	private static Properties continentData;
	private static final String continentDataAddress = "resources/continentData.properties";

	static {
		continentData = Risk.loadProperties(continentDataAddress);
	}

	/**
	 * The texture for the map
	 */
	private BufferedImage texture;

	/**
	 * The list of Country's within the map
	 */
	private List<Country> countries;

	/**
	 * Lists of countries seperated by continent
	 */
	private List<List<Country>> continents;

	public Map() {
		texture = Risk.loadImage("resources/map.png");
		countries = new ArrayList<Country>();
		initCountries();
		initContinents();
	}

	private void initCountries() {
		countries.add(null); // This is so that the index matches up with the
								// location in the array
		for (int i = 1; i <= Country.numCountries; i++) {
			countries.add(new Country(i, this));
		}
		for (Country c : countries) {
			if (c != null)
				c.initConnections(this);
		}
		System.out.println();
	}

	private void initContinents() {
		continents = new ArrayList<List<Country>>();
		int index = 1;
		for (int i = 1; continentData.getProperty("cont" + i + "name") != null; i++) {
			List<Country> cont = new ArrayList<Country>();
			continents.add(cont);
			System.out.println(index);
			System.out.println(Country.getCountryData().getProperty(
					"c" + index + "cont"));
			for (; Integer.toString(i).equals(
					Country.getCountryData().getProperty("c" + index + "cont")); index++) {
				cont.add(countries.get(index));
			}
		}
		index = 5;
	}

	public BufferedImage getTexture() {
		return texture;
	}

	public void setTexture(BufferedImage texture) {
		this.texture = texture;
	}

	public int getContinentBonus(int id) {
		return Integer
				.parseInt(continentData.getProperty("cont" + id + "army"));
	}

	public String getContinentColor(int id) {
		return continentData.getProperty("cont" + id + "col");
	}

	public Country getCountryById(int id) {
		return countries.get(id);
	}

	public Country getCountryByColour(int colour) {
		for (int i = 1; i < countries.size(); i++) {
			Country c = countries.get(i);
			if (c.getClickColour() == colour) {
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

	public List<Country> getContinent(int id) {
		return continents.get(id - 1);
	}

	public int getNumContinents() {
		return continents.size();
	}
}
