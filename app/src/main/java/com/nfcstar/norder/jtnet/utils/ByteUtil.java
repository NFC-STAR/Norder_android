package com.nfcstar.norder.jtnet.utils;

public class ByteUtil {
    public static byte[] mergeArrays(byte[]... arrays) {
        int totalLength = 0;
        int destPos = 0;

        for (byte[] arr : arrays) {
            if (arr != null) {
                totalLength += arr.length;
            }
        }

        byte[] mergedArrays = new byte[totalLength];

        for (byte[] arr : arrays) {
            if (arr != null) {
                System.arraycopy(arr, 0, mergedArrays, destPos, arr.length);
                destPos += arr.length;
            }
        }
        return mergedArrays;
    }
}
