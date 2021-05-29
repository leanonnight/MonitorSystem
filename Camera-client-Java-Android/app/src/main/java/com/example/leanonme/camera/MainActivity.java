package com.example.leanonme.camera;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;


import com.example.leanonme.camera.app.Battery;
import com.example.leanonme.camera.data.ConvertUtil;
import com.example.leanonme.camera.data.DataFrame;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private static final String TAG = "camera_monitor";
    public Toast toast = null;
    public ServerSocket serverSocket;
    private MyService myService;
    //IP地址和端口号
//    public final static String IP_ADDRESS = "115.29.109.104";
//    public final static int PORT = 6515;
//    public final static String IP_ADDRESS = "192.168.1.3";
//    public final static int PORT = 8080;
    //public final static String IP_ADDRESS = "47.100.181.152";
    public final static String IP_ADDRESS = "121.196.172.87";
    public final static int PORT = 6516;

    //控件
    SurfaceView sView;
    static SurfaceHolder surfaceHolder;
    static Camera camera;  //定义系统用的照相机
    static String activityStatus = "";
    static int zoomMax = 0;
    static int zoomNow;
    static int jpegLen = 0;
    static byte[] byteJpegLen = new byte[3];
    byte[] jpegData;
    int heartBeatCounterTimes = 0;
    int cameraZoom = 0;
    int errorTimes = 0;

    //handler
    static Handler handler = null;
    static Socket SOCKET;
    static OutputStream outputStream = null;
    static InputStream inputStream = null;


    static boolean isSendDataOk = true;
    static boolean isConnectTcp = false;
    static boolean isTcpConnecting = false;
    static boolean isPreviewOk = true;
    static boolean isPreview = false;  //camera是否正在预览
    static boolean isSurfaceCreated = false;
    static boolean isCameraSleep = true;
    static boolean isCameraOver = true;
    public static boolean isSendJpegOk = true;
    private AlarmManagerUtils alarmManagerUtils;
    public static boolean isCreatFile = false;
    public static String filedirpath = null;
    public static File file = null;
    public static FileOutputStream fos = null;
    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss : ");
    private static Resources resources;
    private static Context context;

    public static void writeLog(String str){
        String log = df.format(new Date()) + str + "\r\n";
        if(isCreatFile == true){
            try {
                fos.write(log.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static PowerManager.WakeLock mWakeLock = null;

    /**
     * 获取唤醒锁
     */
    @SuppressLint("InvalidWakeLockTag")
    private static void acquireWakeLock()
    {
        if(mWakeLock == null)
        {
            PowerManager mPM = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = mPM.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|
                    PowerManager.ON_AFTER_RELEASE,"PlayService");
            if(mWakeLock!=null)
            {
                mWakeLock.acquire();

                writeLog(TAG + "acquireWakeLock");
            }
        }
    }
    /**
     * 释放锁
     */
    private static void releaseWakeLock(){

        writeLog(TAG + "releaseWakeLock");
        if(mWakeLock != null){
            mWakeLock.release();
            mWakeLock = null;
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        activityStatus = "onStart";
        acquireWakeLock();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");
        wakeLock.acquire();
        OpenCamera();
        writeLog(TAG + "onStart");
        resources = getResources();
    }

    @Override
    protected void onResume() {
        super.onResume();
        OpenCamera();
        activityStatus = "onResume";

        writeLog(TAG + "onResume");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        writeLog(TAG + "onLowMemory");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        writeLog(TAG + "onTrimMemory");
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityStatus = "onPause";

        CloseCamera();
        writeLog(TAG + "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        activityStatus = "onStop";

        CloseCamera();
        writeLog(TAG + "onStop");
    }

    @Override
    protected void onDestroy() {
        startActivity(new Intent("android.intent.action.MAIN"));
        super.onDestroy();
        activityStatus = "onDestory";

        CloseCamera();
        writeLog(TAG + "onDestroy");
    }

    /**
     * 启动摄像头预览
     */
    public void cameraStartPreview(){
        isPreview = true;
        camera.startPreview();
        camera.autoFocus(null);
        writeLog(TAG + "cameraStartPreview");
    }
    /**
     *关闭摄像头预览
     */
    public void cameraStopPreview(){
        isPreview = false;
        camera.stopPreview();
        writeLog(TAG + "cameraStopPreview");
    }

    public static boolean OpenCamera(){
        if(isPreview == false && isSurfaceCreated == true){
            //获取camera对象
            isPreview = true;

            writeLog(TAG + "OpenCamera");
            camera = Camera.open();
            try {
                //设置预览监听
                camera.setPreviewDisplay(surfaceHolder);
                Camera.Parameters parameters = camera.getParameters();

                //设置拍照打开闪光灯
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                //设置方向
                if (resources.getConfiguration().orientation
                        != Configuration.ORIENTATION_LANDSCAPE) {
                    parameters.set("orientation", "portrait");
                    camera.setDisplayOrientation(90);
                    parameters.setRotation(90);
                } else {
                    parameters.set("orientation", "landscape");
                    camera.setDisplayOrientation(0);
                    parameters.setRotation(0);
                }
//                zoomMax = parameters.getMaxZoom();
                camera.setParameters(parameters);
                //
                //启动摄像头预览
                camera.startPreview();
//                camera.startSmoothZoom(zoomNow);
                camera.autoFocus(null);

                writeLog(TAG + "OpenCameraSucced");
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                writeLog(" IOException openCamera error");
                camera.release();
                return false;
            } catch (Exception e){
                e.printStackTrace();
                writeLog("Exception openCamera error");
                camera.release();
                return false;
            }
        }
        return true;
    }

    public void setCameraZoom(int zoom){
        writeLog(TAG + "setCameraZoom: " + zoom);

        if(zoom <= zoomMax){
            camera.startSmoothZoom(zoom);
        }else{

            zoom = zoomMax;
            camera.startSmoothZoom(zoomMax);
        }
        zoomNow = zoom;
        camera.autoFocus(null);

    }

    public static void CloseCamera(){
        writeLog(TAG + "CloseCamera");
        if (camera != null && isPreview == true) {

            isPreview = false;
            zoomNow = 0;
            try{
                camera.stopPreview();
                camera.release();

            } catch (Exception e){
                camera.release();
            }
            writeLog(TAG + "camera.release()");

        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        writeLog(TAG + "surfaceCreated");

        isSurfaceCreated = true;
        OpenCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        writeLog(TAG + "surfaceChanged");

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        writeLog(TAG + "surfaceDestroyed");

        isSurfaceCreated = false;
        CloseCamera();
    }

    public static void startSendJpegServer(){

        new Thread(){
            @Override
            public void run() {
                if(isSendJpegOk == true){
                    isSendJpegOk = false;
                    Log.e(TAG, " startSendJpegServer");
                    sendJpeg();
                }

                while(isSendJpegOk == true);

                Log.e(TAG, " isSendJpegOk");

                MyReceiver.alarmManagerUtils.getUpAlarmManagerWorkOnReceiver();
                super.run();
            }
        }.start();
    }

    public static void sendJpeg(){
        acquireWakeLock();
        Log.e(TAG, "sendJpeg  1");
        isSendJpegOk = false;
        while(!OpenCamera()){//打开相机
            SystemClock.sleep(1000);
        }
        Log.e(TAG, "sendJpeg  2");
        SystemClock.sleep(200);
        while(true){
            new SocketConnectionThread().start();//连接TCP
            isTcpConnecting = true;
            while(isTcpConnecting == true);//等待连接操作完成
            if(isConnectTcp == true){//如果连接成功

                writeLog(TAG + "takePicture");
                camera.takePicture(null,null,new myCameraPictureCallback());//拍照
                break;
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        writeLog(TAG + "onCreate");
        /**
         * 设置全屏
         */
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        activityStatus = "onCreate";

        sView = (SurfaceView) findViewById(R.id.surfaceView);   //获取界面中SurfaceView组件
        surfaceHolder = sView.getHolder();  //获得SurfaceView的SurfaceHolder
        //surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(this);


        /**
         * 创建文件
         */
        try {
            filedirpath = getExternalFilesDir("Documents").getPath();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            file = new File(filedirpath + File.separator + df.format(new Date()) + ".txt");
            file.createNewFile();
            file.canRead();
            file.canWrite();
            fos = new FileOutputStream(file);
            isCreatFile = true;
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        LogCatHelper logCatHelper = LogCatHelper.getInstance(context, filedirpath);
        logCatHelper.start();


        writeLog(TAG + "onCreate");
        /**
         * 与其他线程进行通信
         */
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle bundle = msg.getData();  //获取消息中的Bundle对象
                switch (msg.what) {
                    case 1:

                        break;
                    case 2:
                        showToast(bundle.getString("data"));
                        break;
                    case 3:
                        Log.e(TAG, "handleMessage   " );
                        String head = "jpeg";
                        SocketSendData(head.getBytes());//图片头
                        SocketSendData(byteJpegLen);//该图片长度
                        //SocketSendData(bundle.getByteArray("data"));
                        SocketSendData(jpegData);
                        if(activityStatus.equals("onCreat") || activityStatus.equals("onStart") || activityStatus.equals("onResume")){
                            //启动摄像头预览
                            isPreview = true;
                            camera.startPreview();
                            camera.autoFocus(null);
                        }else{
                            //CloseCamera();
                            isPreviewOk = false;
                            //启动摄像头预览
                            isPreview = true;
                            camera.startPreview();
                            camera.autoFocus(null);
                        }
                        break;
                    case 4:
                        new SocketConnectionThread().start();
                        break;
                    case 5:

                        OpenCamera();
                        break;
                    case 6:

                        new SocketConnectionThread().start();
                        break;
                    case 7:

                        camera.takePicture(null,null,new myCameraPictureCallback());
                        break;
                    default:
                        break;
                }
            }
        };

        Intent intent = new Intent(this,MyService.class);
        startService(intent);
//        bindService(intent, new MyServiceConnection(), Context.BIND_AUTO_CREATE);//运行前台服务 防止被系统杀死
//        startService();
//        /**
//         * 连接TCP服务器
//         */
//        new SocketHeartBeatThread().start();//开始发送心跳包
        HeartBeat(this);
        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        MySystemReceiver receiver = new MySystemReceiver();
        registerReceiver(receiver, filter);

        // 第一次开启应用 10S后就发送一次图片
        new FirstThread().start();
    }

    // 第一次开启应用 10S后就发送一次图片
    public class FirstThread extends Thread{
        @Override
        public void run() {
            super.run();
            try {

                sleep(5000);

                startSendJpegServer();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public class MyServiceConnection implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            writeLog(TAG + "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            writeLog(TAG + "onServiceDisconnected");
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void SendJpegData(byte[] data) throws IOException {
        Log.e(TAG, "SendJpegData");
        DataFrame dataFrame = new DataFrame(2, 1);
        dataFrame.addKey("batteryLevel",Battery.getBatteryLevel(context));
        dataFrame.addKey("jpeg", data);
        dataFrame.countFrameLen();
        Log.e(TAG, "SendJpegData   " + dataFrame.getFrameLen());
        SocketSendData(dataFrame.getFrameBytes());

        if(activityStatus.equals("onCreat") || activityStatus.equals("onStart") || activityStatus.equals("onResume")){
            //启动摄像头预览
            isPreview = true;
            camera.startPreview();
            camera.autoFocus(null);
        }else{
            //CloseCamera();
            isPreviewOk = false;
            //启动摄像头预览
            isPreview = true;
            camera.startPreview();
            camera.autoFocus(null);
        }
        isCameraOver = true;
    }



    //实现客户端套接字
    static class SocketConnectionThread extends Thread {
        Socket socket = new Socket();
        @Override
        public void run() {
            super.run();
            isTcpConnecting = true;
            try {
                SocketAddress socketAddress = new InetSocketAddress(IP_ADDRESS, PORT);
                socket.connect(socketAddress, 5000);
                SOCKET = socket;
                socket.setKeepAlive(true);
                handleSendMessage(2, "服务器连接成功");

                writeLog(TAG + "服务器连接成功");
                OpenCamera();//打开相机

            } catch (IOException e) {
                e.printStackTrace();
                handleSendMessage(2, "服务器连接失败");

                writeLog(TAG + "服务器连接失败");
                isConnectTcp = false;
                isTcpConnecting = false;
                CloseCamera();
                return;
            }
            //绑定套接字的输入输出流
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                new SocketRecvThread().start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            isConnectTcp = true;
            isTcpConnecting = false;
        }
    }

    public void closeTcp(){
        try {
            SOCKET.close();
            writeLog(TAG + "closeTcp");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class myCameraPictureCallback implements Camera.PictureCallback{

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            try {
                SendJpegData(data);
            } catch (IOException e) {
                e.printStackTrace();
            }


            writeLog(TAG + "onPictureTaken: " + "jpegLen:" + jpegLen);
            if(activityStatus.equals("onCreat") || activityStatus.equals("onStart") || activityStatus.equals("onResume")){
                //启动摄像头预览
                isPreview = true;
                camera.startPreview();
                camera.autoFocus(null);
            }else{
                CloseCamera();
            }
        }
    }

    //接受数据线程
    static class SocketRecvThread extends Thread{
        byte[] buff = new byte[1460];
        int num = 0;
        @Override
        public void run() {
            super.run();
            Log.e(TAG, "SocketRecvThread  start");
            while(true){
                try{
                    num = inputStream.read(buff);
                    String str = new String(buff, 0, num);

                    Log.e(TAG, "recvData : " + str);
                    writeLog(TAG + "recvData : " + str);
                    if(str.equals("jpeg")){
                        isSendJpegOk = true;
                        SOCKET.close();
                        releaseWakeLock();
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (StringIndexOutOfBoundsException e){
                    e.printStackTrace();
                }
            }
            Log.e(TAG, "SocketRecvThread  end");
        }
    }


    public static long getNetTime(){
        String webUrl = "http://www.ntsc.ac.cn";//中国科学院国家授时中心
        try {
            URL url = new URL(webUrl);
            URLConnection uc = url.openConnection();
            uc.setReadTimeout(5000);
            uc.setConnectTimeout(5000);
            uc.connect();
            long correctTime = uc.getDate();

            Date date = new Date(correctTime);
            SystemClock.setCurrentTimeMillis(correctTime);//校准当前系统时间
            return correctTime;
        } catch (Exception e) {
            return SystemClock.currentThreadTimeMillis();
        }
    }

    void HeartBeat(Context context){
        alarmManagerUtils = new AlarmManagerUtils(context,MyReceiver.interval);
        alarmManagerUtils.getUpAlarmManagerStartWork();
    }

    //心跳包
    class SocketHeartBeatThread extends Thread {
        int sendJpegCounterTimes = 0;
        int SendJpegTime = 7200;
        long nowTime = SystemClock.elapsedRealtime ()/1000; //秒
        long oldTime = nowTime;


        @Override
        public void run() {
            super.run();
            acquireWakeLock();
            long times = 0;
            while (true) {
                nowTime = SystemClock.elapsedRealtime ()/1000; //秒
                times++;
                if(!(nowTime == oldTime)){//1s
                    oldTime = nowTime;

                    writeLog(TAG + "HeartBeat: " + sendJpegCounterTimes + "  times: " + times);
                    times = 0;

//                    if(sendJpegCounterTimes % 60 == 0){//一分钟校准一次当前系统时间
//                        long time = getNetTime();
//
//                        writeLog(TAG +"系统时间校准!" + time);
//                    }
                    if (sendJpegCounterTimes++ >= SendJpegTime) {
//                        acquireWakeLock();
                        OpenCamera();//打开相机
                        while(true){
                            new SocketConnectionThread().start();//连接TCP
                            isTcpConnecting = true;
                            while(isTcpConnecting == true);//等待连接操作完成
                            if(isConnectTcp == true){//如果连接成功

                                writeLog(TAG + "takePicture");
                                camera.takePicture(null,null,new myCameraPictureCallback());//拍照
                                break;
                            }
                        }
                        sendJpegCounterTimes = 0;
                    }
                }
            }
        }
    }

    void SocketSendData(String data){
        try {
            isSendDataOk = false;
            outputStream.write(data.getBytes());
            outputStream.flush();
            isSendDataOk = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void SocketSendData(byte[] bt){
        try {
            isSendDataOk = false;
            outputStream.write(bt);
            isSendDataOk = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    void showToast(String msg,int duration){

        if(toast != null){
            toast.setText(msg);
            toast.setDuration(duration);
            toast.show();
        }else{
            toast = Toast.makeText(this,msg,duration);
            toast.show();
        }
    }

    void handleSendMessage(int what,float progress){
        Message message = new Message();
        message.what = what;
        Bundle bundle = new Bundle();
        bundle.putFloat("progress",progress);
        message.setData(bundle);
        handler.sendMessage(message);
    }


    static void handleSendMessage(int what, String data){
        Message message = new Message();
        message.what = what;
        Bundle bundle = new Bundle();
        bundle.putString("data",data);
        message.setData(bundle);
        handler.sendMessage(message);
    }
    void handleSendMessage(int what,boolean isVisiable){
        Message message = new Message();
        message.what = what;
        Bundle bundle = new Bundle();
        bundle.putBoolean("isVisiable",isVisiable);
        message.setData(bundle);
        handler.sendMessage(message);
    }
    void handleSendMessage(int what,byte[] data){
        Message message = new Message();
        message.what = what;
        Bundle bundle = new Bundle();
        bundle.putByteArray("data",data);
        message.setData(bundle);
        handler.sendMessage(message);
    }


}
