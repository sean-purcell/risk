package risk.game;

import risk.lib.Clickable;

public class Country implements Clickable{
	/**
	 * The map this country is contained within
	 */
	private Map map;
	
	/**
	 * The unit currently occupying this country
	 */
	private Unit unit;

	@Override
	public boolean overlaps(int x, int y) {
		return false;
	}

	@Override
	public void clicked() {
		
	}
	
	
}
