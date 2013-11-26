package risk.ai;

import java.util.List;

import risk.Risk;
import risk.game.Army;
import risk.game.Country;
import risk.game.Game;
import risk.game.Unit;

public class BasicAI extends AI {
	public BasicAI(Game g, Army a) {
		super(g,a);
	}
	
	protected void interact(){
		sleepTime(250);
		switch(this.g.getMode()){
		case 1:
			switch(this.g.getSetupMode()){
			case 4:
				clickCountry();
				break;
			case 5:
				clickOwnedCountry();
				break;
			}
		case 2:
			switch(this.g.getGameMode()){
			case 1:
				deployReinforcements();
				break;
			case 2:
				attack();
				break;
			case 5:
				String endTurnMessage = "" + (char) 1 + (char) 99;
				g.message(endTurnMessage, -2);
				break;
			}
			break;
		}
	}
	
	private void clickCountry(){
		Country c = null;
		while(c == null || c.getUnit() != null){
			c = g.getMap().getCountryById(Risk.r.nextInt(42)+1);
		}
		String message = "" + (char) 2 + 
				(c.getId() < 10 ? "0" : "")
				+ Integer.toString(c.getId());
		g.message(message, -2);
	}
	
	private void clickOwnedCountry(){
		Country c = a.get().getUnits().get(
				Risk.r.nextInt(a.get().getUnits().size())
				).getLocation();
		String message = "" + (char) 2 + 
				(c.getId() < 10 ? "0" : "")
				+ Integer.toString(c.getId());
		g.message(message, -2);
	}
	
	private void deployReinforcements(){
		sleepTime(100);
		String cardsMessage = "" + (char) 1 + (char) 7;
		g.message(cardsMessage, -2);
		
		List<Unit> units = a.get().getUnits();
		
		while(a.get().getFreeUnits() > 0){
			sleepTime(100);
			
			Country c = units.get(Risk.r.nextInt(units.size())).getLocation();
			
			String countryMessage = "" + (char) 2 + 
					(c.getId() < 10 ? "0" : "")
					+ Integer.toString(c.getId());
			
			g.message(countryMessage, -2);
		}
		sleepTime(250);
		
		String endTurnMessage = "" + (char) 1 + (char) 99;
		g.message(endTurnMessage, -2);
	}

	private void attack(){
		for(int index = a.get().getUnits().size() - 1; index>=0; index--){
			Unit u = a.get().getUnits().get(index);
			Country selected = u.getLocation(),
					target = null;
			
			{ //Clear any possible other selected countries
				String message = "" + (char) 3;
				g.message(message, -2);
			}
			
			List<Country> connections = selected.getConnections();
			boolean targetExists = false;
			for(int i = 0; i < connections.size() && !targetExists; i++){
				if(connections.get(i).getUnit().getArmy() != a.get())
					targetExists = true;
			}
			if(targetExists){
				while(target == null || target.getUnit().getArmy() == a.get()){
					target = connections.get(Risk.r.nextInt(connections.size()));
				}

				{ //Select the selected country
					String message = "" + (char) 2 + 
							(selected.getId() < 10 ? "0" : "")
							+ Integer.toString(selected.getId());

					g.message(message, -2);
				}

				sleepTime(500);

				{ //Attack the selected country with all troops
					String message = "" + (char) 2 + 
							(target.getId() < 10 ? "0" : "")
							+ Integer.toString(target.getId());
					g.message(message, -2);
					while(g.getAttackers() < u.getTroops() - 1){
						g.message(message, -2);
						sleepTime(150);
					}
				}
				sleepTime(500);
				while(u.getTroops() > 1 && target.getUnit().getArmy() != a.get() && g.getGameMode() >= 3){
					String message = "" + (char) 1 + (char) 6;
					while(g.getGameMode() == 4){
						sleepTime(250);
					}
					sleepTime(1000);
					g.message(message, -2);
				}
			}
		}
		String endTurnMessage = "" + (char) 1 + (char) 99;
		g.message(endTurnMessage, -2);
	}
}
