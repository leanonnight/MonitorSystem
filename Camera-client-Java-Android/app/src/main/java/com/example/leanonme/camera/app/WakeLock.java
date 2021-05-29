package com.example.leanonme.camera.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.PowerManager;


/**
 * （1）获取相应权限
 */
//<uses-permission android:name="android.permission.WAKE_LOCK" />
//<uses-permission android:name="android.permission.DEVICE_POWER" />

/**
 *  (2) WakeLoke类型（可更改newWakeLock方法的参数）
 * 1.PARTIAL_WAKE_LOCK：保持COU正常运转，屏幕和键盘灯有可能会关闭。
 *
 * 2.SCREEN_DIM_WAKE_LOCK：保存CPU运转，允许保存屏幕显示但有可能变暗，允许关闭键盘灯。
 *
 * 3.FULL_WAKE_LOCK：保持CPU运转，保持屏幕高亮显示，键盘灯也保持亮度。
 *
 * 4.ACQUIRE_CAUSES_WAKEUP：强制屏幕亮起，这种锁主要用于一些必须通知用的操作。
 *
 * 5.ON_AFTER_RELEASE：当锁被释放时，保持屏幕亮起一段时间。
 */

public class WakeLock {
    private PowerManager.WakeLock mWakeLock = null;
    private Context context = null;


    WakeLock(Context context){
        this.context = context;
    }

    /**
     * 获取唤醒锁
     */
    @SuppressLint("InvalidWakeLockTag")
    private void acquireWakeLock()
    {

        if(mWakeLock == null)
        {
            PowerManager mPM = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = mPM.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|
                    PowerManager.ON_AFTER_RELEASE,"PlayService");
            if(mWakeLock!=null)
            {
                mWakeLock.acquire();

            }
        }
    }

    /**
     * 释放锁
     */
    private void releaseWakeLock(){

        if(mWakeLock != null){
            mWakeLock.release();
            mWakeLock = null;
        }
    }



}
