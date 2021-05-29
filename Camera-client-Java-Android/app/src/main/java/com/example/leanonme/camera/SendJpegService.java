package com.example.leanonme.camera;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.Log;

import static com.example.leanonme.camera.MainActivity.writeLog;

public class SendJpegService extends Service {


    private static final String TAG = "camera_monitor";
    private static SendJpegService server;

    public SendJpegService() {

    }

    @Override
    public void onCreate() {
        server = this;

        writeLog(TAG + "onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        writeLog(TAG + "onStartCommand");
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext());
        Intent nfIntent = new Intent(this,MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(this,0,nfIntent,0))
                //设置通知栏大图标
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),R.drawable.camera))
                //设置服务标题
                .setContentTitle("拍照")
                //设置状态栏小图标
                .setSmallIcon(R.drawable.camera)
                //设置服务内容
                .setContentText("发送图片中")
                //设置通知时间
                .setWhen(System.currentTimeMillis());
        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            notification = builder.build();
        }
        //设置通知的声音
        notification.defaults = Notification.DEFAULT_SOUND;
        //如果 id 为 0 ，那么状态栏的 notification 将不会显示。
        startForeground(11,notification);



        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        writeLog(TAG + "onDestroy");
        stopForeground(true);// 停止前台服务--参数：表示是否移除之前的通知
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
