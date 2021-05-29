package com.example.leanonme.camera;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static com.example.leanonme.camera.MainActivity.writeLog;

public class MySystemReceiver extends BroadcastReceiver {

    private static final String TAG = "camera_monitor";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {

            boolean isServiceRunning = false;
            ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service :manager.getRunningServices(Integer.MAX_VALUE)) {
                if("com.example.leanonme.camera.MyService".equals(service.service.getClassName()))
                //Service的类名
                {

                    writeLog(TAG + " running: " + service.service.getClassName());
                    isServiceRunning = true;
                }
            }
            if (!isServiceRunning) {

                writeLog(TAG + " isNotRunning: " + "com.example.leanonme.camera.MyService");
                Intent i = new Intent(context, MyService.class);
                context.startService(i);
            }

        }
    }
}
