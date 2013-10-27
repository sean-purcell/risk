package risk.game;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import risk.lib.Clickable;

public class Country implements Clickable{
	
	private static Properties countryData;
	private static final String countryDataAddress = "resources/territoryData.properties";
	
	static{
		countryData = new Properties();
		try {
			countryData.load(new FileInputStream(new File(countryDataAddress)));
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
	 * Represents the Color on the clickMap for this country
	 */
	private int clickColor;
	
	public Country(int id){
		this.id = id;
		this.name = countryData.getProperty("c" + id + "name");
		this.clickColor = Integer.parseInt(countryData.getProperty("c" + id + "color"),16);
	}
	
	@Override
	public boolean overlaps(int x, int y) {
		return false;
	}

	@Override
	public void clicked() {
		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getClickColor() {
		return clickColor;
	}

	public void setClickColor(int clickColor) {
		this.clickColor = clickColor;
	}
}
