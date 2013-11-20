package risk.ai;

import java.lang.ref.WeakReference;

import risk.game.Army;
import risk.game.Game;
import risk.lib.RiskThread;

public abstract class AI extends RiskThread{

	protected abstract void interact();
	
	protected Game g;
	protected WeakReference<Army> a;

	public AI(Game g, Army a) {
		super(a.getName());
		this.g = g;
		this.a = new WeakReference<Army>(a);
		
		this.setDaemon(true);
	}

	public void run() {
		while (a != null && a.get() != null) {
			if(a.get().isActive()){
				this.interact();
			}
			sleepTime(100);
		}
	}
	
	protected void sleepTime(long time){
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			
		}
	}
	
	public void halt(){
		g = null;
		a = null;
	}
}
