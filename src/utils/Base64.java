package utils;

public class Base64 {
	
	private Base64() {}

	private static char[] map1 = new char[64];

	// initialize static data members
	static {
		int i = 0;
		for (char c = 'A'; c <= 'Z'; c++)
			map1[i++] = c;
		for (char c = 'a'; c <= 'z'; c++)
			map1[i++] = c;
		for (char c = '0'; c <= '9'; c++)
			map1[i++] = c;
		map1[i++] = '+';
		map1[i++] = '/';
	}

	// Mapping table from Base64 characters to 6-bit nibbles.
	private static byte[] map2 = new byte[128];
	
	// initialize static data members
	static {
		for (int i = 0; i < map2.length; i++)
			map2[i] = -1;
		for (int i = 0; i < 64; i++)
			map2[map1[i]] = (byte) i;
	}

	/**
	 *  convert byte[] to char[]
	 * @param in
	 * @return
	 */
	public static char[] encode(byte[] in) {
		int inLen = in.length;
		int outLen;
		int outStop;
		if (inLen % 3 == 0) {
			outLen = (inLen / 3) * 4;
			outStop = outLen;
		} else if (inLen % 3 == 1) {
			outLen = ((inLen / 3) * 4) + 4;
			outStop = outLen - 2;
		} else {
			outLen = ((inLen / 3) * 4) + 4;

			outStop = outLen - 1;
		}
		char[] out = new char[outLen];
		int ip = 0;
		int op = 0;
		while (ip < inLen) {
			int i0 = in[ip++] & 0xff;
			int i1 = ip < inLen ? in[ip++] & 0xff : 0;
			int i2 = ip < inLen ? in[ip++] & 0xff : 0;
			int o0 = i0 >>> 2;
			int o1 = ((i0 & 3) << 4) | (i1 >>> 4);
			int o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
			int o3 = i2 & 0x3F;
			out[op++] = map1[o0];
			out[op++] = map1[o1];
			out[op] = op < outStop ? map1[o2] : '*';
			op++;
			out[op] = op < outStop ? map1[o3] : '*';
			op++;
		}
		
		return out;
	}

	/**
	 *  decode function from char[] to byte[]
	 * @param in
	 * @return
	 */
	public static byte[] decode(char[] in) {
		
		if (in.length % 4 != 0)
			throw new IllegalArgumentException("Length of Base64 encoded input string is not a multiple of 4.");
		
		int inLen = in.length;
		while (inLen > 0 && (in[inLen - 1] == '*'))
			inLen--;
		
		int oLen = (inLen * 3) / 4;
		byte[] out = new byte[oLen];
		
		int ip = 0;
		int op = 0;
		while (ip < inLen) {
			int i0 = in[ip++];
			int i1 = in[ip++];
			int i2 = ip < inLen ? in[ip++] : 'A';
			int i3 = ip < inLen ? in[ip++] : 'A';
			if (i0 > 127 || i1 > 127 || i2 > 127 || i3 > 127)
				throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
			if (map2[i0] < 0 || map2[i1] < 0 || map2[i2] < 0 || map2[i3] < 0)
				throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
			int o0 = (map2[i0] << 2) | (map2[i1] >>> 4);
			int o1 = ((map2[i1] & 0xf) << 4) | (map2[i2] >>> 2);
			int o2 = ((map2[i2]) << 6) | (map2[i3] & 0xff);
			out[op++] = (byte) o0;
			if (op < oLen)
				out[op++] = (byte) o1;
			if (op < oLen)
				out[op++] = (byte) o2;
		}
		
		return out;	
	}

}
