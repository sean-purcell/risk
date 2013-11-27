package ibur.risk.ai;

import ibur.risk.Risk;
import ibur.risk.game.Army;
import ibur.risk.game.Country;
import ibur.risk.game.Game;
import ibur.risk.game.Unit;

import java.util.List;

public class AtlasAI extends AI {
	public AtlasAI(Game g, Army a) {
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
				reinforce();
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
		Country c = null;
		while(c == null || !a.get().hasDirectFronts(c)){
			c = a.get().getUnits().get(
					Risk.r.nextInt(a.get().getUnits().size())
					).getLocation();
		}
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
			
			Country c = null;
			while(c == null || !a.get().hasDirectFronts(c)){
				c = a.get().getUnits().get(
						Risk.r.nextInt(a.get().getUnits().size())
						).getLocation();
			}
			
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
		for(int index = 0; index < a.get().getUnits().size(); index++){
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
					while(g.getAttackers() < u.getTroops()/4*3){
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
	
	private void reinforce(){
		for(Unit u : a.get().getUnits()){
			if(!a.get().hasDirectFronts(u.getLocation())){
				List<Country> connectedFronts = a.get().getConnectedFronts(u);
				Country target = connectedFronts.get(Risk.r.nextInt(connectedFronts.size()));
				{
					String message = "" + (char) 3;
					g.message(message,-2);
				}
				sleepTime(100);
				{
					String message = (char) 2 +
							(u.getLocation().getId() < 10 ? "0" : "")
							+ Integer.toString(u.getLocation().getId());
					g.message(message, -2);
				}
				{
					String message = (char) 2 + 
							(target.getId() < 10 ? "0" : "")
							+ Integer.toString(target.getId());
					
					while(u.getTroops() > 1){
						sleepTime(50);
						g.message(message, -2);
					}
				}
			}
		}
		String endTurnMessage = "" + (char) 1 + (char) 99;
		g.message(endTurnMessage, -2);
	}
}
