package com.example.lean_on_me.wifi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

public class MyBitmap extends View {
    private static final String TAG = "Surveillance";
    private Paint mPaint;
    private Paint textPaint;
    private int screenWidth;
    private int screenHeight;
    private float dpWidth = (float)1920f/420f;  //宽度上每一个像素点的大小
    private float dpHight = (float)1080f/420f;  //高度上每一个像素点的大小
    private Bitmap jpegBitmap;
    private byte[] jpegBytes;
    private int offset;
    private int jpegBytesLen = 0;
    private String title = null;
    public boolean isDrawn = true; //图片是否画完
    public MyBitmap(Context context) {
        this(context,null);
    }
    public MyBitmap(Context context,AttributeSet attrs) {
        this(context,attrs,0);
    }
    public MyBitmap(Context context,AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPaint = new Paint();//新建画笔
        mPaint.setStrokeWidth(3);//设置画笔粗细
        mPaint.setAntiAlias(true);//设置抗锯齿
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        textPaint = new Paint();
        textPaint.setStrokeWidth(4f);
        textPaint.setAntiAlias(true);//设置抗锯齿
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setTextSize(40);
        textPaint.setColor(0xffffffff);

        //禁用硬件加速
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);//不禁用硬件加速Ondraw绘制时会显示延迟(两三秒)
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        DisplayMetrics metrics = new DisplayMetrics();
        metrics = getResources().getDisplayMetrics();
        screenWidth = metrics.widthPixels; //获取屏幕大小
        screenHeight = metrics.heightPixels;

        setMeasuredDimension(screenWidth,screenHeight);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(jpegBytes != null) {

            isDrawn = false;
            if((jpegBitmap = BitmapFactory.decodeByteArray(jpegBytes, offset, jpegBytesLen)) != null){//如果为空说明JPEG格式错误

                Log.e(TAG, "screenWidth:  " + screenWidth + "   screenHeight:  " + screenHeight);
                Log.e(TAG, "jpegBitmapWidth:  " + jpegBitmap.getWidth() + "   jpegBitmapHeight:  " + jpegBitmap.getHeight());
                //jpegBitmap = converBitmap(jpegBitmap,0,(float)screenWidth/(float)jpegBitmap.getWidth(),(float)screenHeight/(float)jpegBitmap.getHeight());


                canvas.drawBitmap(jpegBitmap, 0, 0, mPaint);
                isDrawn = true;
                canvas.drawText(title,10,40,textPaint);

            }else{

            }

        }
    }

    /*
     *旋转图片
     */
    public static Bitmap converBitmap(Bitmap bitmap, int degress,float sx,float sy) {
        if (bitmap != null){

            Matrix m = new Matrix();
            m.postRotate(degress); //旋转角度
            m.postScale(sx,sy); //长和宽放大缩小的比例
            Log.e(TAG, "sx:   " + sx + "   sy:   " + sy);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
            Log.e(TAG, "bitmapWidth:  " + bitmap.getWidth() + "   bitmapHeight:  " + bitmap.getHeight());
            return bitmap;
        }
        return bitmap;
    }


    public void setJpegBytes(byte[] bytes,int offset, int len, String title) {
        this.title = title;
        jpegBytes = bytes;
        jpegBytesLen = len;
        this.offset = offset;
        invalidate();
    }


}
