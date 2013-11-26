package ibur.risk.ai;

import ibur.risk.game.Army;
import ibur.risk.game.Game;
import ibur.risk.lib.RiskThread;

import java.lang.ref.WeakReference;

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
