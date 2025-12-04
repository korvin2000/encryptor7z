package com.example.encryption;


public final class ByteUtils {

    public static byte[] UIntToByte2(int value) {
    	if (value < 0) value = 0;
    	if (value > Short.MAX_VALUE) value = Short.MAX_VALUE;
    	value += Short.MIN_VALUE;
		return new byte[] {(byte) (value >>> 8),
		(byte) value};
    }

    public static int Byte2ToUInt(byte[] arr) {
    	int  x =  (arr[0]<<8 | arr[1] & 0xFF);
    	x -= Short.MIN_VALUE;
    	return x;
    }

}
