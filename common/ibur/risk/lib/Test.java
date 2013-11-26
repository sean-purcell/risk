package ibur.risk.lib;

import ibur.risk.Risk;

public class Test {

	public static void main(String[] args) {
		System.out.println(Integer.toString(0xFE12,36));
		System.out.println(Character.MAX_RADIX);
		int[] a = {5,2,6,1241,34567};
		String str = Risk.serializeIntArray(a);
		System.out.println(str);
		int[] b = Risk.deserializeIntArray(str);
		System.out.println("done");
		
		//int[] c = Risk.rotateArray(a, 3);
		//c = null;
	}

}
