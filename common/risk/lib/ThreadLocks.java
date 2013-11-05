package risk.lib;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Basic class that ensures Thread safety via locks. Locks are stored in an
 * integer array, 0 means the lock is free, otherwise it's equal to the id of
 * the thread that owns it
 * 
 * @author Sean
 * 
 */
public class ThreadLocks {

	
	// Should be held while writing or reading core game state
	public static final int GAME_STATE = 0;
	
	// Should be held while initializing game resources when the game starts
	public static final int INIT_RESOURCES = 1;

	public static int[] locks = new int[getNumLocks()];

	/**
	 * Gets the number of locks by a bit of hackish reflection
	 * 
	 * @return
	 */
	public static int getNumLocks() {
		Class<ThreadLocks> c = ThreadLocks.class;
		Field[] fields = c.getFields();
		int numLocks = 0;
		for (Field f : fields) {
			if (Modifier.isStatic(f.getModifiers()) && f.getType() == int.class) {
				System.out.println(f);
				numLocks++;
			}
		}
		return numLocks;
	}

	public synchronized static void requestLock(int lock, int threadId) {
		if (locks[lock] == threadId) {
			return;
		}
		
		// Wait for the lock to become available
		while (locks[lock] != 0)
			// For debug purposes
			System.out.println("I am: " + threadId + ";Waiting on lock held by: " + locks[lock]);
			;
		locks[lock] = threadId;
		return;
	}

	public static void releaseLock(int lock, int threadId) {
		// Prevents threads from releasing locks they don't own
		if (locks[lock] == threadId) {
			locks[lock] = 0;
		}
	}

	public static int checkLock(int lock) {
		return locks[lock];
	}
}
