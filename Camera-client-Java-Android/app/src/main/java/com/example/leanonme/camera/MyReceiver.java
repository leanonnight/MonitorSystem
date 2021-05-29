package com.example.leanonme.camera;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static com.example.leanonme.camera.MainActivity.writeLog;

public class MyReceiver extends BroadcastReceiver {

    private static final String TAG = "camera_monitor";
    public static AlarmManagerUtils alarmManagerUtils;
    static int sendJpegCounterTimes = 0;    //当前已计数次数
    static long interval = 6000;//ms        //一次计数时间间隔
    static int SendJpegTime = 600;           //多少次
    static boolean isFirst = true;
    @SuppressLint("NewApi")
    @Override
    public void onReceive(Context context, Intent intent) {
        alarmManagerUtils = new AlarmManagerUtils(context,interval);
        //高版本重复设置闹钟达到低版本中setRepeating相同效果
        alarmManagerUtils.getUpAlarmManagerWorkOnReceiver();

        writeLog(TAG + "   HeartBeat: " + sendJpegCounterTimes * alarmManagerUtils.times);

//        if(isFirst && sendJpegCounterTimes == 1){
//            isFirst = false;
//
//            sendJpegCounterTimes = 0;
//            alarmManagerUtils.cancelAlarmManager();
//            MainActivity.startSendJpegServer();
//        }
        Log.e(TAG ,"HeartBeat: " + sendJpegCounterTimes);
//        if(sendJpegCounterTimes++ >= SendJpegTime / alarmManagerUtils.times ){
        if(sendJpegCounterTimes++ >= SendJpegTime){
            sendJpegCounterTimes = 0;
            alarmManagerUtils.cancelAlarmManager();
            Log.e(TAG ,"   over!!!!!!");
            writeLog(TAG + "   over!!!!!!");
            MainActivity.startSendJpegServer();
        }
    }
}
