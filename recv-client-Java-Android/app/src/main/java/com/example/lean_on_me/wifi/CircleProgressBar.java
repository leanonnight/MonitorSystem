package com.example.lean_on_me.wifi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.text.DecimalFormat;


public class CircleProgressBar extends View {
    private Paint bigCirclePaint;       /*绘制外层大圈*/
    private Paint smallCirclePaint;     /*绘制内层小圈*/
    private Paint textPaint;            /*绘制进度百分比说明文字*/
    private int bigCircleR = 300;       /*外圈大圆半径*/
    private int smallCircleR = 250;     /*内圈小圆半径*/
    private int textSize = 120;         /*进度文字大小*/
    private float progress = 0;           /*进度,以百分比显示*/
    private float dp = (float)6;        /*像素*/

    public CircleProgressBar(Context context) {
        this(context, null);
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        /**
         * 大圈画笔初始化
         */
        bigCirclePaint = new Paint();//新建画笔
        bigCirclePaint.setStrokeWidth(dp);//设置画笔粗细
        bigCirclePaint.setAntiAlias(true);//设置抗锯齿
        bigCirclePaint.setStyle(Paint.Style.STROKE);
        bigCirclePaint.setColor(0xff3fe3ee);
        /**
         *小圈画笔初始化
         */
        smallCirclePaint = new Paint();
        smallCirclePaint.setStrokeWidth(dp);//设置画笔粗细
        smallCirclePaint.setAntiAlias(true);//设置抗锯齿
        smallCirclePaint.setStyle(Paint.Style.STROKE);
        smallCirclePaint.setColor(0xffffffff);
        /**
         * 进度文字画笔初始化
         */
        textPaint = new Paint();
        textPaint.setStrokeWidth(1);//设置画笔粗细
        textPaint.setAntiAlias(true);//设置抗锯齿
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setTextSize(textSize);
        textPaint.setColor(0xffffffff);
        textPaint.setTextAlign(Paint.Align.CENTER);/*设置文字对齐方式*/

        setLayerType(LAYER_TYPE_SOFTWARE, null);/*关闭硬件加速*/
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
       setMeasuredDimension((int)(bigCircleR*2+dp*2),(int)(bigCircleR*2+dp*2));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(bigCircleR+dp,bigCircleR+dp);    /*将坐标原点定为圆心*/
        /**
         * 绘制外圈
         */
        canvas.drawCircle(0,0,bigCircleR,bigCirclePaint);
        /**
         *绘制内圈
         */
        canvas.drawArc(-smallCircleR,-smallCircleR,smallCircleR,smallCircleR,-90,progress*360,false,smallCirclePaint);
        /**
         * 绘制进度文字
         */
        DecimalFormat decimalFormat= new DecimalFormat( ".00" ); //构造方法的字符格式这里如果小数不足2位,会以0补足.
        String p= decimalFormat.format(progress*100); //format 返回的是字符串
        canvas.drawText(p+"%",0,textSize/3,textPaint);
    }

    void progressUpdata(float progress){
        this.progress = progress;
        invalidate();
    }
}
