//package com.example.lean_on_me.wifi.data;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.Socket;
//
//public class DataFrameSocketRunnable {
//    Socket socket;
//    InputStream inputStream;
//    OutputStream outputStream;
//    int socketId;
//    private int frameLen;
//    private int keyNum;
//
//    /**
//     * 这些字段都需要初始化，以供下一次接收数据时使用
//     */
//    //是否开始接收图片
//    boolean isStartRecvData = false;
//    //是否接收到帧头（便于判断需要接收多少字节）
//    boolean isRecvFrameHead = false;
//    //字节输出流 缓存收到的数据
//    ByteArrayOutputStream dataFrameBytes = new ByteArrayOutputStream();
//    //数据帧
//    DataFrame dataFrame = null;
//    //数据帧帧头
//    DataFrame dataFrameHead = null;
//    //当前已接收数据长度
//    int currentRecvLen = 0;
//
//    void filedInit(){
//        isStartRecvData = false;
//        isRecvFrameHead = false;
//        dataFrameBytes = new ByteArrayOutputStream();
//        dataFrame = null;
//        dataFrameHead = null;
//        currentRecvLen = 0;
//    }
//
//    DataFrameSocketRunnable(Socket socket, int socketId){
//        try {
//            this.socket = socket;
//            socket.setKeepAlive(true);
////                socket.setSoTimeout(300000); //设置30s未接到数据则为超时
//            inputStream = socket.getInputStream();
//            outputStream = socket.getOutputStream();
//            this.socketId = socketId;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    void errorProcess(){
//
//    }
//
//
//    public void run() {
//        //此次接收字节数量
//        int num;
//        //此次数据接收缓存
//        byte[] buff = new byte[1460];
//
//        filedInit();
//        while (true) {
//            try {
//                num = inputStream.read(buff);
//                PrintUtil.printIn_MS("currentRecvLen:" + currentRecvLen + " num: " + num );
//
//                //如果连接断开
//                if(num == -1){
//                    errorProcess();
//                    PrintUtil.printIn_Y_S("error! input失败");
//                    break;
//                }
//
//                //尚未开始接受此帧数据
//                if(!isStartRecvData){
//                    if(buff[0] == '$'){ //起始符
//                        PrintUtil.printIn_MS("收到起始符");
//                        isStartRecvData = true;
//                    }
//                }
//
//                //开始接收
//                if(isStartRecvData){
//
//                    //将数据写入缓存
//                    currentRecvLen += num;
//                    dataFrameBytes.write(buff, 0, num);
//
//                    //如果尚未收到帧头且收到数据长度达到了帧头长度，则表明已接收数据中含有帧头，可做下一步解析
//                    if(!isRecvFrameHead && currentRecvLen>= DataFrame.head_len){
//                        dataFrameHead = DataFrame.paraseFarmeHeadBytes(buff);
//                        isRecvFrameHead = true;
//                        PrintUtil.printIn_MS("收到完整帧头");
//                    }
//
//                    //接收完成
//                    if(isRecvFrameHead && currentRecvLen >= dataFrameHead.getFrameLen()){
//                        PrintUtil.printIn_MS("接收完成" + "  currentRecvLen: " + currentRecvLen);
//                        dataFrame = DataFrame.parseFrameBytes(dataFrameBytes.toByteArray());
//                        recvDataFrameCallBack(dataFrame);
//                        filedInit();
//                    }
//                }
//
//            } catch (IOException e) {
//                errorProcess();
//                e.printStackTrace();
//            }
//        }
//
//    }
//
//    private void recvDataFrameCallBack(DataFrame dataFrame) {
//        if(dataFrame.userID == 1){//拍照客户端
//            FileUtil.saveFile("camera.jpeg", dataFrame.getKeyValue("jpeg"));
//            BatteryLevel = ConvertUtil.bytes2Int(dataFrame.getKeyValue("BatteryLevel"), 0);
//        }else if(dataFrame.userID == 2){//接收客户端
//            sendJpeg(outputStream, true);
//        }
//
//    }
//}
//
//}
