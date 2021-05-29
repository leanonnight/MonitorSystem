package com.example.lean_on_me.wifi;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.lean_on_me.wifi.data.ConvertUtil;
import com.example.lean_on_me.wifi.data.DataFrame;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public Toast toast = null;

    //IP地址和端口号
//    public final static String IP_ADDRESS = "47.100.181.152";
//    public final static int PORT = 6515;
    public final static String IP_ADDRESS = "121.196.172.87";
    public final static int PORT = 6516;
    //控件
    Button buttonConnection = null;
    Button buttonSend = null;
    MyBitmap myBitmap = null;
    CircleProgressBar circleProgressBar = null;
    FrameLayout mainActivity;

    //handler
    Handler handler = null;

    //数据发送与接收
    Socket socket = null;
    OutputStream outputStream = null;
    InputStream inputStream = null;

    private String TAG = "Surveillance";
    boolean isButtonVisible = true;
    boolean isSuccedShowJpeg = false;
    int numConnectServer = 0; //第几次连接服务器


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置全屏
        setFullWindow();

        //绑定组件
        bindView();

        //设置监听器
        setListener();

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle bundle = msg.getData();
                switch (msg.what){
                    //提示框
                    case 1:
                        showToast(bundle.getString("msg"));
                        break;

                        //显示图片
                    case 2:
                        byte[] bytes_jpeg = bundle.getByteArray("jpeg");
                        byte[] byess_date = bundle.getByteArray("date");
                        byte[] bytes_batteryLevel = bundle.getByteArray("BatteryLevel");
                        String title = new String(byess_date) + "   Power : " + ConvertUtil.bytes2Int(bytes_batteryLevel, 0) + "%";
                        myBitmap.setJpegBytes(bytes_jpeg,0, bytes_jpeg.length, title);
                        isSuccedShowJpeg = true;
                        setButtonVisible(false);

                        //设置进度条是否显示
                    case 3:{
                        boolean isVisiable = bundle.getBoolean("isVisiable");
                        Log.e(TAG,"circleProgressBar: "+isVisiable);
                        if(isVisiable){
                            circleProgressBar.setVisibility(View.VISIBLE);
                        }else{
                            circleProgressBar.setVisibility(View.INVISIBLE);
                        }
                        break;
                    }

                        //设置进度条的进度
                    case 4:{
                        float progress = bundle.getFloat("progress");
                        circleProgressBar.progressUpdata(progress);
                        break;
                    }
                }
            }
        };

        connectSocket(); //自动连接服务器
    }

    void setFullWindow(){
        /**
         * 设置全屏
         */
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
    }

    void setListener(){
        //设置监听器
        mainActivity.setOnClickListener(this);
        buttonConnection.setOnClickListener(this);
        buttonSend.setOnClickListener(this);
    }

    void bindView(){
        buttonConnection = (Button) findViewById(R.id.buttonConnection);
        buttonSend = (Button) findViewById(R.id.buttonSend);
        myBitmap = (MyBitmap)findViewById(R.id.myBitmap);
        mainActivity = (FrameLayout) findViewById(R.id.mainActivity);
        circleProgressBar = (CircleProgressBar) findViewById(R.id.circleProgressBar);
        circleProgressBar.setVisibility(View.INVISIBLE);
    }

    void setButtonVisible(boolean enable){
        if(enable == true){
            isButtonVisible = true;
            buttonConnection.setVisibility(View.VISIBLE);
            buttonConnection.setEnabled(true);
            buttonSend.setVisibility(View.VISIBLE);
            buttonSend.setEnabled(true);
        }else{
            isButtonVisible = false;
            buttonConnection.setVisibility(View.INVISIBLE);
            buttonConnection.setEnabled(false);
            buttonSend.setVisibility(View.INVISIBLE);
            buttonSend.setEnabled(false);
        }

    }

    void connectSocket(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "开始连接");
                try {
                    socket = new Socket(IP_ADDRESS, PORT);
                    inputStream = socket.getInputStream();
                    outputStream = socket.getOutputStream();
                } catch (IOException e) {

                    showToastOnThread("连接失败");
                    Log.e(TAG, "连接失败");
                    e.printStackTrace();
                }

                Log.e(TAG, "连接成功");
                showToastOnThread("连接成功");
                new DataFrameSocketRunnable(socket,1).start();

            }
        }).start();
    }

    void sendData() {
        Log.e(TAG, "发送数据");
        if(socket.isConnected()){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        DataFrame dataFrame = new DataFrame(0,2);
//                        dataFrame.addKey("b", "".getBytes());
                        dataFrame.countFrameLen();
                        byte[] bytes = dataFrame.getFrameBytes();
                        outputStream.write(bytes);
                        outputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }else{
            showToast("请先连接服务器");
        }
    }




    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.mainActivity){
            Log.e(TAG, "onClick   " + "点击主界面");
            //点击主界面
            if(isSuccedShowJpeg == true){
                setButtonVisible(!isButtonVisible);
            }

        }else if(v.getId() == R.id.buttonConnection){
            Log.e(TAG, "onClick   " + "连接按钮");
            //点击连接按钮
            connectSocket();

        }else if(v.getId() == R.id.buttonSend){
            Log.e(TAG, "onClick   " + "发送按钮");
            //点击发送按钮
            sendData();
        }

    }



    public class DataFrameSocketRunnable extends Thread {
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

        void filedInit() {
            isStartRecvData = false;
            isRecvFrameHead = false;
            dataFrameBytes = new ByteArrayOutputStream();
            dataFrame = null;
            dataFrameHead = null;
            currentRecvLen = 0;
        }

        DataFrameSocketRunnable(Socket socket, int socketId) {
            try {
                this.socket = socket;
                socket.setKeepAlive(true);
//                socket.setSoTimeout(300000); //设置30s未接到数据则为超时
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                this.socketId = socketId;

                if(numConnectServer == 0){
                    numConnectServer++;
                    sendData();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        void errorProcess() {

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
                    //Log.e(TAG, "currentRecvLen:" + currentRecvLen + " num: " + num);

                    //如果连接断开
                    if (num == -1) {
                        errorProcess();
                        Log.e(TAG, "error! 连接断开");
                        break;
                    }

                    //尚未开始接受此帧数据
                    if (!isStartRecvData) {
                        if (buff[0] == '$') { //起始符
                            Log.e(TAG, "收到起始符");
                            isStartRecvData = true;

                            message_setCircleProgressVisibility(true);
                        }
                    }

                    //开始接收
                    if (isStartRecvData) {

                        //将数据写入缓存
                        currentRecvLen += num;
                        dataFrameBytes.write(buff, 0, num);

                        //如果尚未收到帧头且收到数据长度达到了帧头长度，则表明已接收数据中含有帧头，可做下一步解析
                        if (!isRecvFrameHead && currentRecvLen >= DataFrame.head_len) {
                            dataFrameHead = DataFrame.paraseFarmeHeadBytes(buff);
                            isRecvFrameHead = true;
                            Log.e(TAG, "收到完整帧头");
                        }

                        if(isRecvFrameHead){    //设置进度条
                            message_setCircleProgress((float)currentRecvLen/dataFrameHead.getFrameLen());
                            Log.e(TAG, "Progress   :" + (float)currentRecvLen/dataFrameHead.getFrameLen());
                        }

                        //接收完成
                        if (isRecvFrameHead && currentRecvLen >= dataFrameHead.getFrameLen()) {
                            Log.e(TAG, "接收完成" + "  currentRecvLen: " + currentRecvLen);
                            dataFrame = DataFrame.parseFrameBytes(dataFrameBytes.toByteArray());
                            recvDataFrameCallBack(dataFrame);
                            filedInit();
                        }
                    }

                } catch (IOException e) {
                    errorProcess();
                    e.printStackTrace();
                    break;
                }
            }

        }

        private void recvDataFrameCallBack(DataFrame dataFrame) {
            Log.e(TAG, dataFrame.getDataFrameString());;
            message_setCircleProgressVisibility(false);
            message_setJpeg(dataFrame);
        }
    }

    void message_setCircleProgress(float progress){
        Message message = new Message();
        message.what = 4;
        Bundle bundle = new Bundle();
        bundle.putFloat("progress", progress);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    void message_setCircleProgressVisibility(boolean isVisiable){
        Message message = new Message();
        message.what = 3;
        Bundle bundle = new Bundle();
        bundle.putBoolean("isVisiable", isVisiable);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    void message_setJpeg(DataFrame dataFrame){
        Message message = new Message();
        message.what = 2;
        Bundle bundle = new Bundle();
        bundle.putByteArray("jpeg", dataFrame.getKeyValue("jpeg"));
        bundle.putByteArray("date", dataFrame.getKeyValue("date"));
        bundle.putByteArray("BatteryLevel", dataFrame.getKeyValue("BatteryLevel"));
        message.setData(bundle);
        handler.sendMessage(message);
    }


    void showToast(String msg){

        if(toast != null){
            toast.setText(msg);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.show();
        }else{
            toast = Toast.makeText(this,msg,Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    void showToastOnThread(String msg){
        Message message = new Message();
        message.what = 1;
        Bundle bundle = new Bundle();
        bundle.putString("msg", msg);
        message.setData(bundle);
        handler.sendMessage(message);
    }

}
