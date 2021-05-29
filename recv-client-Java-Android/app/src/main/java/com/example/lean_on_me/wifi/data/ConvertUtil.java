package com.example.lean_on_me.wifi.data;

public class ConvertUtil {
    /**
     * short到byte[] (,0,29 = 41)
     * @param i 需要转换为byte数组的整行值。
     * @return byte数组
     */
    public static byte[] short2Bytes(short i) {
        byte[] result = new byte[2];
        result[0] = (byte)((i >> 8) & 0xFF);
        result[1] = (byte)(i & 0xFF);
        return result;
    }

    /**
     * int到byte[] (0,0,0,29 = 41)
     * @param i 需要转换为byte数组的整行值。
     * @return byte数组
     */
    public static byte[] int2Bytes(int i) {
        byte[] result = new byte[4];
        result[0] = (byte)((i >> 24) & 0xFF);
        result[1] = (byte)((i >> 16) & 0xFF);
        result[2] = (byte)((i >> 8) & 0xFF);
        result[3] = (byte)(i & 0xFF);
        return result;
    }

    /**
     * 四位byte转化为int
     * @param bytes
     * @param offset
     * @return
     */
    public static int bytes2Int(byte[] bytes, int offset) {
        int value = bytes[offset+3] & 0xFF;
        value |= (bytes[offset+2] << 8) & 0xFF00;
        value |= (bytes[offset+1] << 16) & 0xFF0000;
        value |= (bytes[offset] << 24) & 0xFF000000;
        return value;
    }

    /**
     * 将二位byte转化为short
     * @param bytes
     * @param offset
     * @return
     */
    public static short bytes2short(byte[] bytes, int offset){
        short value = (short)(bytes[offset+1] & 0xFF);
        value |= (short)((bytes[offset] << 8)&0xFF00);
        return value;
    }




    /**
     * 将byte转化为ASCII值
     * @param x
     * @return
     */
    public static int byte2UnsignedInt(byte x) {
        return ((int) x) & 0xff;
    }

    /**
     * 将byte转化为bytes
     * @param b
     * @return
     */
    public static byte[] byte2Bytes(byte b){
        return new byte[]{b};
    }

    /**
     * bytes子数组
     * @param bytes
     * @param offset
     * @param len
     * @return
     */
    public static byte[] bytesSub(byte[] bytes, int offset, int len){
        byte[] bytes1 = new byte[len];
        for(int i=0; i<len; i++){
            bytes1[i] = bytes[i+offset];
        }
        return bytes1;
    }
}
