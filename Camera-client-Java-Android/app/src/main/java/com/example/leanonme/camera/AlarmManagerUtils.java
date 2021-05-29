package com.example.leanonme.camera;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import static com.example.leanonme.camera.MainActivity.writeLog;


public class AlarmManagerUtils {


    public int s = 1000;   //秒
    public int times = 1; //多少个秒
    public long TIME_INTERVAL = times * s;//闹钟执行任务的时间间隔
    private static final String TAG = "camera_monitor";
    private Context context;
    private AlarmManager am;
    private PendingIntent pendingIntent;

    //
    public AlarmManagerUtils(Context aContext,long interval) {
        this.context = aContext;
        createGetUpAlarmManager();//创建AlarmManager
        setTimeInterval(interval);//设置间隔
    }

    private void setTimeInterval(long interval){
        TIME_INTERVAL = interval;
        times = (int)interval / 1000;
    }

//    //饿汉式单例设计模式
//    private AlarmManagerUtils instance = null;
//
//    public AlarmManagerUtils getInstance(Context aContext) {
//        if (instance == null) {
//            synchronized (AlarmManagerUtils.class) {
//                if (instance == null) {
//                    instance = new AlarmManagerUtils(aContext);
//                }
//            }
//        }
//        return instance;
//    }

    public void cancelAlarmManager(){

        writeLog(TAG + "cancelAlarmManager");
        am.cancel(pendingIntent);
    }

    private void createGetUpAlarmManager() {
        am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent("alarmManager");
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);//每隔5秒发送一次广播
    }

    @SuppressLint("NewApi")
    public void getUpAlarmManagerStartWork() {
        //版本适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {// 6.0及以上
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis(), pendingIntent);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {// 4.4及以上
            am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                    pendingIntent);

        } else {
            am.setRepeating(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis(), TIME_INTERVAL, pendingIntent);

        }
    }
    @SuppressLint("NewApi")
    public void setOneAlarmManagerStartWork(){
        //高版本重复设置闹钟达到低版本中setRepeating相同效果
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {// 6.0及以上
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 1000, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {// 4.4及以上
            am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                    + 1000, pendingIntent);
        } else {
            am.setRepeating(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis(), 1000, pendingIntent);
        }
    }

    @SuppressLint("NewApi")
    public void getUpAlarmManagerWorkOnReceiver() {
        //高版本重复设置闹钟达到低版本中setRepeating相同效果
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {// 6.0及以上
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + TIME_INTERVAL, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {// 4.4及以上
            am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                    + TIME_INTERVAL, pendingIntent);
        }
    }
}


