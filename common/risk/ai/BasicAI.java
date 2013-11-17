package risk.ai;

import java.lang.ref.WeakReference;

import risk.game.Army;
import risk.game.Game;

public class BasicAI extends Thread implements AI {
	private Game g;
	private WeakReference<Army> a;

	private boolean active;

	private Object setActiveLock;

	public BasicAI(Game g, Army a) {
		this.g = g;
		this.a = new WeakReference<Army>(a);

		this.setDaemon(true);
	}

	public void run() {
		while (a.get() != null) {

		}
	}

	public void activate() {
		setActive(true);
		this.interrupt();
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

}
