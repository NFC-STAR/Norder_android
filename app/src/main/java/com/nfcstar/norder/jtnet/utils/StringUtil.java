package com.nfcstar.norder.jtnet.utils;


import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtil {
	/**
	 * 왼쪽으로 자리수만큼 문자 채우기
	 * @param maxLen 리턴받을 결과의 문자열 길이
	 * @param str 원본 문자열
	 * @return
	 */
	public static String getRPadSpace(int maxLen, String str) {
		int padLen = 0;
		try {
			padLen = maxLen - str.getBytes("EUC-KR").length;
			//if(BuildConfig.DEBUG) Log.e("getRPadSpace", "padLen:" + padLen);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if(padLen > 0) {
			String strPad = String.format("%" + padLen + "s", " ");
			str = String.format("%s%s", str, strPad);
		//	if(BuildConfig.DEBUG) {
		//		Log.e("getRPadSpace", "strPad:" + strPad);
		//		Log.e("getRPadSpace", "str:" + str);
		//	}
		} else if(padLen < 0) {
			str = "[ERROR]" + str;
		}
		return str;
	}

	/**
	 * 왼쪽으로 자리수만큼 0 채우기
	 * @param maxLen
	 * @param amount
	 * @return
	 */
	public static String getLPadZero(int maxLen, String amount) {
		int iAmount = convertInteger(amount);
		String str = String.format("%0" + maxLen + "d", iAmount);
		return str;
	}

	/**
	 * String to byteArray 변환
	 * @return
	 */
	public static byte[] stringToByteArray(String str) {
		byte[] destBytes = null;
		try {
			byte[] arrBytes = str.getBytes("EUC-KR");
			int arrLength = arrBytes.length;
			destBytes = new byte[arrLength + 1];
			System.arraycopy(arrBytes, 0, destBytes, 0, arrLength);
			destBytes[arrLength] = (byte) 0x0d;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return destBytes;
	}

	/**
	 * byteArray to String 변환
	 * @param arrBytes
	 * @return
	 */
	public static String byteArrayToString(byte[] arrBytes) {
		int arrLength = arrBytes.length;
		byte[] destBytes = new byte[arrLength - 1];
		System.arraycopy(arrBytes, 0, destBytes, 0, (arrLength - 1));
		String resStr = null;
		try {
			resStr = new String(destBytes, "EUC-KR");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return resStr;
	}

	/**
	 * 오늘날짜
	 * @return
	 */
	public static String getDateTime(String arg) {
		SimpleDateFormat sdf = new SimpleDateFormat(arg);
		String strDate = sdf.format(new Date());
		return strDate;
	}

	/**
	 * int 형으로 변환
	 * @param arg
	 * @return
	 */
	public static int convertInteger(String arg) {
		int rtnNumber = 0;
		if(arg != null) {
			try {
				rtnNumber = Integer.parseInt(arg);
			} catch (Exception e) {
				rtnNumber = 0;
			}
		}
		return rtnNumber;
	}

	/**
	 * StringFormatDigit %s
	 * @param tSize %s * 갯수
	 * @return
	 */
	public static String strFormat(int tSize) {
		String strRes = "";
		for (int i = 0 ; i < tSize ; i++) {
			strRes += "%s";
		}
		return strRes;
	}

	/**
	 * 응답결과 분리
	 * @param str
	 * @param arrSubStringNo
	 * @return
	 */
	public static String[] strSubString(String str, int[] arrSubStringNo) {
		String[] arrResData = new String[arrSubStringNo.length];

		int intResPos = 0;
		for(int i = 0 ; i < arrSubStringNo.length ; i++) {
			arrResData[i] = str.substring(intResPos, (intResPos + arrSubStringNo[i]));
			intResPos = arrSubStringNo[i];
		}
		return arrResData;
	}
}
