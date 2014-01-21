package com.example.rgbdotmatrix;

import android.util.Log;

// DotMatrix 와 통신하기 위한 프로토콜 생성
public class ProtocolCreate {
	private static final String TAG = "ProtocolCreate";
	
	public static byte[] toIntsToBytes(int[] inputColor) {

		byte[] output = new byte[inputColor.length * 3 + 2];

		//Start Byte
		output[0] = (byte) '[';
		
		// Color 투명도 제외한 R, G, B 데이터 추출
		for (int i = 0; i < inputColor.length; i++) {
			output[i * 3 + 1] = (byte) ((inputColor[i] >> 16) & 0xff);
			output[i * 3 + 2] = (byte) ((inputColor[i] >> 8) & 0xff);
			output[i * 3 + 3] = (byte) (inputColor[i] & 0xff);
		}
		
		//End Byte
		output[inputColor.length * 3 + 1] = (byte) ']';

		Log.e(TAG, ProtocolCreate.byteArrayToHex(output));
		
		return output;
	}
	
	// byte[] to hex string
	public static String byteArrayToHex(byte[] ba) {
	 
	    StringBuffer sb = new StringBuffer(ba.length * 2);
	    String hexNumber;
	    for (int x = 0; x < ba.length; x++) {
	        hexNumber = "0" + Integer.toHexString(0xff & ba[x]);
	        sb.append(hexNumber.substring(hexNumber.length() - 2));
	    }
	    return sb.toString();
	} 
	
	
	// hex string to byte[]
	public static byte[] hexToByteArray(String hex) {
	 
	    byte[] ba = new byte[hex.length() / 2];
	    for (int i = 0; i < ba.length; i++) {
	        ba[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
	    }
	    return ba;
	}
	
	  
}
