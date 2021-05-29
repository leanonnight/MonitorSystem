package com.example.leanonme.camera.app;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.support.annotation.RequiresApi;

import static android.content.Context.BATTERY_SERVICE;

public class Battery {
    private Context context = null;
    private BatteryManager batteryManager;

    Battery(Context context){
        this.context = context;
    }


    /**
     * 获取电池电量
     * @param context
     * @return
     */
    public static int getBatteryLevel(Context context){
//        BatteryManager batteryManager = (BatteryManager)context.getSystemService(BATTERY_SERVICE);
//        int battery = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        int battery = (int)(level * 100 / (float)scale);
        return battery;
    }

    int getBatteryLevel(){
        //        BatteryManager batteryManager = (BatteryManager)context.getSystemService(BATTERY_SERVICE);
//        int battery = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        int battery = (int)(level * 100 / (float)scale);
        return battery;
    }



}
