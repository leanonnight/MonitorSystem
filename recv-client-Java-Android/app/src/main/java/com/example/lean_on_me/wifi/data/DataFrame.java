package com.example.lean_on_me.wifi.data;


import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * 使用步骤
 * 1.先使用构造函数输入DataFrame关键字个数、
 * 2.使用addKey()函数添加关键字
 * 3.使用countFrameLen函数计算DataFrame长度
 * 4.使用getFrameBytes函数将DataFrame转化为byte[]
 */
public class DataFrame {

    /**
     * 数据帧格式
     */
    //帧头(head)
    private static final byte startChar = '$';      //起始符
    private int frameLen = 0;                //数据帧总长
    private short keyNum = 0;                //关键字数量
    private short userID = 0;                //用户ID
    //帧体(body)
    private DataFrameBody[] dataFrameBody;


    /**
     * 辅助字段
     */
    //帧头长度
    public static final int head_len = 9;
    private int index = 0;


    /**
     * 还不知道frameLen的构造函数
     * @param keyNum 该数据帧关键字数量
     * @param userID 发送该数据帧的ID
     */
    DataFrame(short keyNum, short userID){
        this.keyNum = keyNum;
        this.userID = userID;
        dataFrameBody = new DataFrameBody[this.keyNum];
    }
    public DataFrame(int keyNum, int userID){
        if(keyNum >= Short.MAX_VALUE || userID >= Short.MAX_VALUE){
            return;
        }
        this.keyNum = (short)keyNum;
        this.userID = (short)userID;
        dataFrameBody = new DataFrameBody[this.keyNum];
    }

    /**
     * 已知道frameLen的构造函数
     * @param frameLen
     * @param keyNum
     * @param userID
     */
    DataFrame(int frameLen, int keyNum, int userID){
        if(keyNum >= Short.MAX_VALUE || userID >= Short.MAX_VALUE){
            return;
        }
        this.keyNum = (short)keyNum;
        this.userID = (short)userID;
        this.frameLen = frameLen;
        dataFrameBody = new DataFrameBody[this.keyNum];
    }





    /**
     * 添加关键字
     * @param key 关键字 最长255个字符
     * @param value 关键字内容 最长2GB 2^31
     */
    public void addKey(String key, byte[] value){
        dataFrameBody[index] = new DataFrameBody(key, value);
        index++;
    }

    /**
     * 计算帧长
     */
    public void countFrameLen(){
        frameLen = 0;
        //计算帧头长度
        frameLen = head_len;  //帧头长度

        //计算帧体(body)长度
        for(int i=0; i<keyNum; i++){
            frameLen += dataFrameBody[i].getKeyLen();
        }
    }


    /**
     * 获得该数据帧长度
     */
    public int getFrameLen() {
        return frameLen;
    }

    /**
     * 将该数据帧转化为字节数组
     * @return  字节数组
     * @throws IOException 不理会
     */
    public byte[] getFrameBytes() throws IOException {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();

        //写入帧头(head)
        byteOutput.write(startChar);
        byteOutput.write(ConvertUtil.int2Bytes(frameLen));
        byteOutput.write(ConvertUtil.short2Bytes(keyNum));
        byteOutput.write(ConvertUtil.short2Bytes(userID));

        //写入帧体(body)
        for(int i=0; i<keyNum; i++){
            byteOutput.write(dataFrameBody[i].keyNameLen);
            byteOutput.write(dataFrameBody[i].key.getBytes());
            byteOutput.write(ConvertUtil.int2Bytes(dataFrameBody[i].valueLen));
            byteOutput.write(dataFrameBody[i].value);
        }
        return byteOutput.toByteArray();
    }

    /**
     * 解析帧头
     * @param frameBytes
     * @return
     */
    public static DataFrame paraseFarmeHeadBytes(byte[] frameBytes){
        //解析帧头
        int frameLen = ConvertUtil.bytes2Int(frameBytes, 1);
        short keyNum = ConvertUtil.bytes2short(frameBytes, 5);
        short userID = ConvertUtil.bytes2short(frameBytes, 7);



        System.out.println(Arrays.toString(ConvertUtil.bytesSub(frameBytes, 0, 9)));
        System.out.println("frameLen:" + frameLen + " keyNum: " + keyNum + " userID: " + userID);


        return new DataFrame(frameLen, keyNum, userID);
    }


    /**
     * 将数据帧字节流转化为DataFrame
     * @param frameBytes 数据帧字节流
     * @return DataFrame  返回数据帧格式
     */
    public static DataFrame parseFrameBytes(byte[] frameBytes){
        //解析帧头
        DataFrame dataFrame = paraseFarmeHeadBytes(frameBytes);

        int offset = head_len;  //帧头偏移
        //帧体
        for(int i=0; i<dataFrame.keyNum; i++){

            //关键字长度
            int keyNameLen = ConvertUtil.byte2UnsignedInt(frameBytes[offset]);
            //关键字
            String key = new String(frameBytes, offset + 1, keyNameLen);
            //关键字内容长度偏移
            offset += 1 + keyNameLen;
            //关键字长度
            int valueLen = ConvertUtil.bytes2Int(frameBytes, offset);
            //关键字内容
            byte[] value = ConvertUtil.bytesSub(frameBytes, offset+4, valueLen);
            //关键字内容偏移
            offset += 4 + valueLen;
            dataFrame.addKey(key, value);
        }
        dataFrame.countFrameLen();
        return dataFrame;
    }

    public byte[] getKeyValue(String key){
        for (int i = 0; i < keyNum; i++) {
            if(dataFrameBody[i].key.equals(key)){
                return dataFrameBody[i].value;
            }
        }
        return null;
    }
    public String getDataFrameString(){
        String str = new String();
        str = "\n--------------------------------------------------------------------------------------------------\n";
        //打印头
        str += "|  " + "frameLen: " + frameLen + "  keyNum: " + keyNum + "  userID: " + userID + "\n";

        //打印body
        for(int i=0; i<keyNum; i++){
            String str1;
            byte[] bytes;
            String str2;

            if(dataFrameBody[i].valueLen > 25){
                bytes = ConvertUtil.bytesSub(dataFrameBody[i].value, 0, 25);
            }else{
                bytes = dataFrameBody[i].value;
            }

            str1 = new String(bytes);
            str2 = Arrays.toString(bytes);

            str +="|  " + "len: " + dataFrameBody[i].valueLen + "     " + dataFrameBody[i].key + ": " + str1 + " ;      " + str2 + "\n";
        }

        str +="--------------------------------------------------------------------------------------------------" + "\n";
        return str;
    }

}
