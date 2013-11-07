package risk.game;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import risk.Risk;

public class Country{

	private static Properties countryData;
	private static final String countryDataAddress = "resources/territoryData.properties";

	private static Properties countryConnections;
	private static final String countryConnectionsAddress = "resources/territoryConnections.properties";

	public static int numCountries;

	// Load the country data file
	static {
		countryData = new Properties();
		try {
			countryData.load(new FileInputStream(new File(countryDataAddress)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Count the number of countries by going through the data file until there
	// is no longer an entry for that country id
	static {
		String s = null;
		boolean loop = true;
		numCountries = 1;
		while (loop) {
			s = countryData.getProperty("c" + numCountries + "name");
			if (s == null) {
				loop = false;
				numCountries--; // To fix off by one errors
			} else {
				numCountries++;
			}
		}
		System.out.println(numCountries);
	}

	static {
		countryConnections = new Properties();
		try {
			countryConnections.load(new FileInputStream(new File(
					countryConnectionsAddress)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The map this country is contained within
	 */
	private Map map;

	/**
	 * The Image representing the outlined country
	 */
	private BufferedImage texture;
	
	/**
	 * The unit currently occupying this country
	 */
	private Unit unit;

	/**
	 * Represents the name of the country
	 */
	private String name;

	/**
	 * Represents the id of this country
	 */
	private int id;

	/**
	 * Represents the Colour on the clickMap for this country
	 */
	private int clickColour;

	/**
	 * Self explanatory
	 */
	private int continent;
	
	/**
	 * Represents the locations on the board where units should be drawn
	 */
	private int x;
	private int y;

	/**
	 * List of all connected countries, effectively adjacency list
	 */
	private List<Country> connections;

	public Country(int id, Map m) {
		this.id = id;
		System.out.println("Constructing country with id: " + id);
		this.name = countryData.getProperty("c" + id + "name");
		System.out.println("c" + id + "colour");
		this.clickColour = Integer.parseInt(
				countryData.getProperty("c" + id + "colour"), 16);
		System.out.println(Integer.toString(this.clickColour, 16));

		this.continent = Integer.parseInt(
				countryData.getProperty("c" + id + "cont"));
		
		try {
			this.x = Integer.parseInt(countryData.getProperty("c" + id + "x"));
			this.y = Integer.parseInt(countryData.getProperty("c" + id + "y"));
		} catch (Exception e) {
			System.err.println("Error: data for country " + id
					+ "not complete.");
		}

		{
			String textureAddress = "resources/countries/";
			textureAddress += m.getContinentColor(continent) + "/";
			textureAddress += canonicalize(name) + ".png";
			texture = Risk.loadImage(textureAddress);
		}
		
		this.map = m;
		connections = new ArrayList<Country>();
	}

	private String canonicalize(String str){
		str = str.toLowerCase();
		for(int i = str.length()-1; i >= 0; i++){
			if(str.charAt(i) == '\''){
				str = str.substring(0,i) + str.substring(i+1);
			}
		}
		return str;
	}
	
	public void initConnections(Map m) {
		String connection = null;
		for (int i = 0; (connection = countryConnections.getProperty(this.id
				+ "c" + i)) != null; i++) {
			int country = Integer.parseInt(connection);
			Country c = m.getCountryById(country);

			if (!connections.contains(c)) {
				connections.add(c);
			}
			if (!c.connections.contains(this)) {
				c.connections.add(this);
			}
		}
	}

	public String toString() {
		return name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getClickColour() {
		return clickColour;
	}

	public void setClickColour(int clickColour) {
		this.clickColour = clickColour;
	}

	public List<Country> getConnections() {
		return connections;
	}

	public void setConnections(List<Country> connections) {
		this.connections = connections;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	protected static Properties getCountryData() {
		return countryData;
	}
}
