package risk.ai;

import java.lang.ref.WeakReference;

import risk.game.Army;
import risk.game.Game;

public abstract class AI extends Thread{

	protected abstract void interact();
	
	protected Game g;
	protected WeakReference<Army> a;

	private boolean active;

	private Object setActiveLock;

	public AI(Game g, Army a) {
		super(a.getName());
		this.g = g;
		this.a = new WeakReference<Army>(a);
		this.setActiveLock = new Object();
		
		this.setDaemon(true);
		this.start();
	}

	public void run() {
		while (a.get() != null) {
			sleepTime(100);
			if(active){
				this.interact();
			}
		}
	}

	public void activate() {
		setActive(true);
	}

	public void deactivate() {
		setActive(false);
	}

	public boolean isActive() {
		synchronized (setActiveLock) {
			return active;
		}
	}

	public void setActive(boolean active) {
		synchronized (setActiveLock) {
			this.active = active;
		}
	}
	
	protected void sleepTime(long time){
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			
		}
	}
}
