import Util.FileUtil.FileUtil;
import Util.PrintUtil.PrintUtil;
import data.ConvertUtil;
import data.DataFrame;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static Util.FileUtil.FileUtil.openFile;

public class TcpServer{
    //public class camera{
    private static final int port = 6516;
    private static int CLIENT_MAX_NUM = 50;//客户端最大数量
    private static ServerSocket serverSocket;
    private static ExecutorService executorService = Executors.newFixedThreadPool(CLIENT_MAX_NUM);
    private static ArrayList<Socket> sockets = new ArrayList<Socket>(); //保存所有接受的socket
    private static int watchDogCounter = 0;


    private static int BatteryLevel = 10;


    public static void main(String[] args){
        serverSocket  = creatServerSocket(port);
        int socketId = 0;
        while(true){
            try {
                Socket socket = serverSocket.accept();
                System.out.println("socketId: " + socketId + "...");
                sockets.add(socket);
                executorService.submit(new socketRunnable(socket, socketId++));
            } catch (NullPointerException e){
                e.printStackTrace();
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    static ServerSocket creatServerSocket(int port){
        try {
            ServerSocket serverSocket = new ServerSocket(port, CLIENT_MAX_NUM);
            System.out.println("login...");
            return serverSocket;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }



    static class socketRunnable implements Runnable {
        Socket socket;
        InputStream inputStream;
        OutputStream outputStream;
        int socketId;
        private int frameLen;
        private int keyNum;

        /**
         * 这些字段都需要初始化，以供下一次接收数据时使用
         */
        //是否开始接收图片
        boolean isStartRecvData = false;
        //是否接收到帧头（便于判断需要接收多少字节）
        boolean isRecvFrameHead = false;
        //字节输出流 缓存收到的数据
        ByteArrayOutputStream dataFrameBytes = new ByteArrayOutputStream();
        //数据帧
        DataFrame dataFrame = null;
        //数据帧帧头
        DataFrame dataFrameHead = null;
        //当前已接收数据长度
        int currentRecvLen = 0;

        void filedInit(){
            isStartRecvData = false;
            isRecvFrameHead = false;
            dataFrameBytes = new ByteArrayOutputStream();
            dataFrame = null;
            dataFrameHead = null;
            currentRecvLen = 0;
        }

        socketRunnable(Socket socket, int socketId){
            try {
                this.socket = socket;

                PrintUtil.printIn_Y_S("current Connected Num: " + sockets.size() + "  ;  " + socket.getInetAddress().getHostAddress() + " is connected、\n\n");
                socket.setKeepAlive(true);
//                socket.setSoTimeout(300000); //设置30s未接到数据则为超时
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                this.socketId = socketId;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void sendJpeg(){
            File file = openFile("camera.jpeg");
            try {
                System.out.println("send jpeg");

                //读取图片
                FileInputStream in = new FileInputStream(file);
                int jpegLen = in.available();
                byte[] jpeg = new byte[jpegLen];
                in.read(jpeg);

                //创建图片数据帧
                DataFrame picFrame = new DataFrame(3, 1);
                picFrame.addKey("date", FileUtil.getLastModified(file).getBytes());  //创建图片的日期
                picFrame.addKey("BatteryLevel", ConvertUtil.int2Bytes(BatteryLevel));
                picFrame.addKey("jpeg", jpeg);
                picFrame.countFrameLen();
                System.out.println(picFrame.getDataFrameString());
                //发送
                outputStream.write(picFrame.getFrameBytes());
                outputStream.flush();
                System.out.println("send jpeg ok");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void errorProcess(){
            try {
                inputStream.close();
                outputStream.close();
                socket.close();
                if (socketId == Recognize.socketId){    //如果是识别服务器断开
                    Recognize.isConnectedRecognizeServer = false;
                    PrintUtil.printIn_Y_S("recognizeServer is disconnected");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            sockets.remove(socket); //删除该socket
            PrintUtil.printIn_Y_S("current Connected Num: " + sockets.size() + "  ;  " + socket.getInetAddress().getHostAddress() + " is disconnected");
        }


        public void run() {
            //此次接收字节数量
            int num;
            //此次数据接收缓存
            byte[] buff = new byte[1460];

            filedInit();
            while (true) {
                try {
                    num = inputStream.read(buff);
                    //PrintUtil.printIn_MS("currentRecvLen:" + currentRecvLen + " num: " + num );

                    //如果连接断开
                    if(num == -1){
                        errorProcess();
                        // PrintUtil.printIn_Y_S("error! input失败");
                        break;
                    }

                    //尚未开始接受此帧数据
                    if(!isStartRecvData){
                        if(buff[0] == '$'){ //起始符
                            PrintUtil.printIn_MS("收到起始符\n");
                            isStartRecvData = true;
                        }
                    }

                    //开始接收
                    if(isStartRecvData){

                        //将数据写入缓存
                        currentRecvLen += num;
                        dataFrameBytes.write(buff, 0, num);

                        //如果尚未收到帧头且收到数据长度达到了帧头长度，则表明已接收数据中含有帧头，可做下一步解析
                        if(!isRecvFrameHead && currentRecvLen>= DataFrame.head_len){
                            dataFrameHead = DataFrame.paraseFarmeHeadBytes(buff);
                            isRecvFrameHead = true;
                            PrintUtil.printIn_MS("收到完整帧头");
                        }

                        //接收完成
                        if(isRecvFrameHead && currentRecvLen >= dataFrameHead.getFrameLen()){
                            PrintUtil.printIn_MS("接收完成" + "  currentRecvLen: " + currentRecvLen);
                            dataFrame = DataFrame.parseFrameBytes(dataFrameBytes.toByteArray());
                            System.out.println(dataFrame.getDataFrameString());
                            recvDataFrameCallBack(dataFrame);
                            filedInit();
                        }
                    }

                } catch (IOException e) {
                    errorProcess();
                    e.printStackTrace();
                }
            }

        }

        private void sendData(String str){
            try {
                outputStream.write(str.getBytes());
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void recvDataFrameCallBack(DataFrame dataFrame) {
            if(dataFrame.userID == 1){//拍照客户端
                FileUtil.saveFile("camera.jpeg", dataFrame.getKeyValue("jpeg"));
                BatteryLevel = ConvertUtil.bytes2Int(dataFrame.getKeyValue("batteryLevel"), 0);
                sendData("jpeg");
            }else if(dataFrame.userID == 2){//接收客户端

                PrintUtil.printIn_MS("发送图片\n\n");

                sendJpeg();
            }
        }
    }

}
