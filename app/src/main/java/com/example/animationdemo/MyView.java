package com.example.animationdemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class MyView extends View {
    private final Context mContext;
    private Paint outsideRoundPaint;
    private Paint insideRoundPaint;
    private Paint scaleLinePaint;
    private Paint textPaint;
    private Paint pointerPaint;
    private Paint trianglePaint;
    private float centerX;
    private float centerY;
    private float outsideRadius;
    private float insideRadius;
    private float gapLength;
    private RectF insideRect;
    private float startAngle = 127.5f;
    private float sweepAngle = 285.0f;
    int currentDb;
    float currentDegree;
    public float MaxDb;
    float MaxDegree;
    public float MinDb;
    float MinDegree;
    Bitmap triangle;
    SweepGradient sweepGradient;
    Matrix matrix;
    int scaleCnt;

    private final int[] colors = new int[]{
            0xFF0099FF,//天蓝色
            0xFF00FF00,//绿色
            0xFFFFFF00,//黄色
            0xFFFF0000,//红色
    };


    public MyView(Context context) {
        this(context, null);
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
        initPaint();
    }

    private void init() {
        centerX = Util.getScreenWidth(mContext) / 2;
        centerY = Util.getScreenHeight(mContext) / 2;
        outsideRadius = centerX * 0.85f;
        insideRadius = centerX * 0.55f;
        insideRect = new RectF(centerX - insideRadius, centerY - insideRadius, centerX + insideRadius, centerY + insideRadius);
        gapLength = (float) ((outsideRadius - insideRadius) * 0.2);
        triangle = ((BitmapDrawable) getResources().getDrawable(R.mipmap.triangle)).getBitmap();
        sweepGradient = new SweepGradient(centerX, centerY, colors, null);
        matrix = new Matrix();
    }

    private void initPaint() {
        // 外圈画笔
        outsideRoundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outsideRoundPaint.setStyle(Paint.Style.STROKE);
        outsideRoundPaint.setColor(Color.LTGRAY);
        outsideRoundPaint.setStrokeWidth(Util.toDp(3, mContext));

        //内圈画笔
        insideRoundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        insideRoundPaint.setStyle(Paint.Style.STROKE);
        insideRoundPaint.setStrokeWidth(Util.toDp(8, mContext));

        //刻度线画笔
        scaleLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scaleLinePaint.setStyle(Paint.Style.STROKE);
        scaleLinePaint.setColor(Color.GRAY);
        scaleLinePaint.setStrokeWidth(Util.toDp(1, mContext));

        //中央字体画笔
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setColor(Color.WHITE);
        textPaint.setTypeface(Typeface.SANS_SERIF);

        //指针画笔
        pointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointerPaint.setStyle(Paint.Style.STROKE);
        pointerPaint.setStrokeWidth(Util.toDp(4, mContext));

        //三角形画笔
        trianglePaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //绘制外圈圆环
        canvas.drawCircle(centerX, centerY, outsideRadius, outsideRoundPaint);
        canvas.save();

        //绘制内圈圆环
        matrix.setRotate(90, centerX, centerY);
        sweepGradient.setLocalMatrix(matrix);
        insideRoundPaint.setShader(sweepGradient);
        canvas.drawArc(insideRect, startAngle, sweepAngle, false, insideRoundPaint);
        insideRoundPaint.setShader(null);
        canvas.restore();
        canvas.save();

        //绘制刻度线
        canvas.rotate(startAngle - 180, centerX, centerY);
        scaleLinePaint.setColor(Color.GRAY);
        scaleCnt = (int) (currentDegree / 2.85f);
        for (int i = 0; i <= 100; i++) {
            //指针未抵达位置设置为暗色
            if (i == scaleCnt + 1)
                scaleLinePaint.setColor(Color.DKGRAY);
            canvas.drawLine(centerX - insideRadius - gapLength, centerY, centerX - outsideRadius + gapLength, centerY, scaleLinePaint);
            if (i == 0) { //顺便绘制内圈左边角
                matrix.setRotate(270 - startAngle, centerX, centerY);
                sweepGradient.setLocalMatrix(matrix);
                insideRoundPaint.setShader(sweepGradient);
                canvas.drawLine(centerX - insideRadius - Util.toDp(4, mContext), centerY, centerX - insideRadius + gapLength + Util.toDp(2, mContext), centerY, insideRoundPaint);
            }
            if (i == 100) { //顺便绘制内圈右边角
                matrix.setRotate(270 - startAngle - sweepAngle, centerX, centerY);
                sweepGradient.setLocalMatrix(matrix);
                insideRoundPaint.setShader(sweepGradient);
                canvas.drawLine(centerX - insideRadius - Util.toDp(4, mContext), centerY, centerX - insideRadius + gapLength + Util.toDp(2, mContext), centerY, insideRoundPaint);
            }
            canvas.rotate(2.85f, centerX, centerY);
        }
        insideRoundPaint.setShader(null);
        canvas.restore();
        canvas.save();

        //绘制中央文字
        textPaint.setTextSize(Util.toSp(60, mContext));
        float descent = textPaint.descent();
        float Textwidth = textPaint.measureText(String.valueOf(currentDb));
        canvas.drawText(String.valueOf(currentDb), centerX - Textwidth / 2 - 30, centerY + descent, textPaint);
        textPaint.setTextSize(Util.toSp(30, mContext));
        canvas.drawText("dB", centerX + Textwidth / 2, centerY + descent, textPaint);
        canvas.restore();
        canvas.save();

        //绘制指针
        matrix.setRotate(270 - (startAngle + currentDegree), centerX, centerY);
        sweepGradient.setLocalMatrix(matrix);
        pointerPaint.setShader(sweepGradient);
        canvas.rotate(startAngle + currentDegree - 180, centerX, centerY);
        canvas.drawLine(centerX - insideRadius - gapLength, centerY, centerX - outsideRadius + 0.5f * gapLength, centerY, pointerPaint);
        pointerPaint.setShader(null);
        canvas.restore();
        canvas.save();

        //绘制最小分贝三角形
        canvas.rotate(startAngle + MinDegree - 180, centerX, centerY);
        canvas.drawBitmap(triangle, centerX - outsideRadius + Util.toDp(1.5f, mContext), centerY, trianglePaint);
        canvas.restore();
        canvas.save();

        //绘制最大分贝三角形
        canvas.rotate(startAngle + MaxDegree - 180, centerX, centerY);
        canvas.drawBitmap(triangle, centerX - outsideRadius + Util.toDp(1.5f, mContext), centerY, trianglePaint);
        canvas.restore();
        canvas.save();

    }

    public void setDb(float db) {
        currentDb = (int) db;
        currentDegree = db / 100 * sweepAngle;
        invalidate(); //重新绘制
    }

    public void setMaxDb(float db) {
        MaxDb = db;
        MaxDegree = (MaxDb + 1) / 100 * sweepAngle;
        invalidate(); //重新绘制
    }

    public void setMinDb(float db) {
        MinDb = db;
        MinDegree = MinDb / 100 * sweepAngle;
        invalidate(); //重新绘制
    }
}