package risk.lib;

public class B64 {
	public static String toB64(byte[] bytes){
		char[] o = new char[(int) (Math.ceil(bytes.length / 3.0)) * 4];
		int oi = 0;
		int buf = 0;
		for(byte b : bytes){
			switch(oi % 6){
			case 0:
				o[oi/6] = toB64((byte) ((b & 0xFC) >>> 2));
				buf = (b & 0x03) << 4;
				break;
			case 2:
				buf |= (b & 0xF0) >>> 4;
				o[oi/6] = toB64((byte) buf);
				buf = (b & 0x0F) << 2;
				break;
			case 4:
				buf |= (b & 0xC0) >>> 6;
				o[oi/6] = toB64((byte) buf);
				o[oi/6+1] = toB64((byte) (b & 0x3F));
				buf = 0;
				break;
			}
			oi += 8;
		}
		if(oi % 6 == 4){
			o[oi/6] = toB64((byte) buf);
			o[oi/6+1] = '=';
		}else if(oi % 6 == 2){
			o[oi/6] = toB64((byte) buf);
			o[oi/6+1] = '=';
			o[oi/6+2] = '=';
		}
		
		return new String(o);
	}
	
	public static byte[] fromB64(String s){
		System.out.println();
		int padding = s.charAt(s.length() - 2) == '=' ? 2 : s.charAt(s.length() - 1) == '=' ? 1 : 0;
		byte[] b = new byte[s.length() / 4 * 3 - padding];
		char[] c = s.toCharArray();
		int bi = 0;
		int len = b.length;
		
		for(int i = 0; i < c.length && bi / 8 < len; i++){
			byte buf = 0;
			switch(bi % 8){
			case 0:
				buf = fromB64(c[i]);
				b[bi/8] |= buf << 2;
				break;
			case 6:
				buf = fromB64(c[i]);
				b[bi/8] |= buf >>> 4;
				if(bi/8 < len-1) b[bi/8+1] |= (buf & 0x0F) << 4;
				break;
			case 4:
				buf = fromB64(c[i]);
				b[bi/8] |= (buf & 0x3C) >>> 2;
				if(bi/8 < len-1) b[bi/8+1] |= (buf & 0x03) << 6;
				break;
			case 2:
				buf = fromB64(c[i]);
				b[bi/8] |= buf;
				break;
			}
			bi+=6;
		}
		return b;
	}
	
	public static char toB64(byte b){
		if(b <= 9){
			return (char) (b + 48);
		}else if(b <= 35){
			return (char) (b + 55);
		}else if(b <= 61){
			return (char) (b + 61);
		}else if(b == 62){
			return '/';
		}else{
			return '+';
		}
	}
	
	public static byte fromB64(char c){
		if(c == '/'){
			return (byte) 62;
		}else if(c == '+'){
			return (byte) 63;
		}else if(c <= '9'){
			return (byte) (c - 48);
		}else if(c <= 'Z'){
			return (byte) (c - 55);
		}else{
			return (byte) (c - 61);
		}
	}
	
	public static String to6Bits(int i){
		String s = Integer.toBinaryString(i);
		while(s.length() < 6) s = '0' + s;
		return s;
	}
	
	public static String to8Bits(int i){
		String s = Integer.toBinaryString(i);
		while(s.length() < 8) s = '0' + s;
		return s;
	}
}
