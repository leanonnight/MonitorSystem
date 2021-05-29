package com.example.leanonme.camera;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;
import static com.example.leanonme.camera.MainActivity.writeLog;

public class MyService extends Service {
    private static final String TAG = "camera_monitor";
    private static Boolean isServering = true;
    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        writeLog(TAG + "onCreate");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        writeLog(TAG + "onStartCommand");
//        new Thread() {
//            @Override
//            public void run() {
//                while(true){
//                    isServering = true;
//                }
//            }
//        }.start();
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext());
        Intent nfIntent = new Intent(this,MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(this,0,nfIntent,0))
                //设置通知栏大图标
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),R.drawable.camera))
                //设置服务标题
                .setContentTitle("远程监控")
                //设置状态栏小图标
                .setSmallIcon(R.drawable.camera)
                //设置服务内容
                .setContentText("服务正在运行")
                //设置通知时间
                .setWhen(System.currentTimeMillis());
        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            notification = builder.build();
        }
        //设置通知的声音
        notification.defaults = Notification.DEFAULT_SOUND;
        //如果 id 为 0 ，那么状态栏的 notification 将不会显示。
        startForeground(110,notification);

        return START_REDELIVER_INTENT;
    }


    @Override
    public void onDestroy() {

        writeLog(TAG + "onDestroy");
        stopForeground(true);// 停止前台服务--参数：表示是否移除之前的通知
        startService(new Intent(this,MyService.class));//运行前台服务 防止被系统杀死
        super.onDestroy();
    }

    public class MyBinder extends Binder {
        public MyService getService(){
            return MyService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        writeLog(TAG + "onBind");

        return new MyBinder();
    }

}
