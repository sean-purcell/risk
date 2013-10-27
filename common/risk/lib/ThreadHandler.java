package risk.lib;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ThreadHandler {
	
	public static final int UPDATE = 0;
	
	public static int[] locks = new int[getNumLocks()];
	
	/**
	 * Gets the number of locks by a bit of hackish reflection
	 * @return
	 */
	public static int getNumLocks(){
		Class c = ThreadHandler.class;
		Field[] fields = c.getFields();
		int numLocks = 0;
		for(Field f : fields){
			if(Modifier.isStatic(f.getModifiers()) && f.getType() == int.class){
				System.out.println(f.getName());
				numLocks++;
			}
		}
		return numLocks;
	}
}
