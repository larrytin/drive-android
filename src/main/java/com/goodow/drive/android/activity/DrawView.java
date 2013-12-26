package com.goodow.drive.android.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

/**
 * 
 * @category: View实现涂鸦、撤销以及重做功能
 * 
 * 
 */
@SuppressLint("ViewConstructor")
public class DrawView extends View {

  private class DrawPath {
    public Path path;// 路径
    public Paint paint;// 画笔
  }

  private Bitmap mBitmap;
  private Canvas mCanvas;
  private Path mPath;
  private Paint mBitmapPaint;// 画布的画笔
  private Paint mPaint;// 真实的画笔
  private float mX, mY;// 临时点坐标

  private static final float TOUCH_TOLERANCE = 4;
  // 保存Path路径的集合,用List集合来模拟栈
  private static List<DrawPath> savePath;

  // 记录Path路径的对象
  private DrawPath dp;

  private int screenWidth, screenHeight;// 屏幕長寬

  public DrawView(Context context, int w, int h) {
    super(context);
    screenWidth = w;
    screenHeight = h;

    mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
    // 保存一次一次绘制出来的图形
    mCanvas = new Canvas(mBitmap);
    mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    mPaint = new Paint();
    mPaint.setAntiAlias(true);
    mPaint.setStyle(Paint.Style.STROKE);
    mPaint.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘
    mPaint.setStrokeCap(Paint.Cap.SQUARE);// 形状
    mPaint.setStrokeWidth(5);// 画笔宽度
    mPaint.setColor(Color.RED);

    savePath = new ArrayList<DrawPath>();
  }

  // 清除
  public void clear() {
    mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
    mCanvas.setBitmap(mBitmap);// 重新设置画布，相当于清空画布
    savePath.clear();
    mPath = null;
    invalidate();
  }

  @Override
  public void onDraw(Canvas canvas) {
    // canvas.drawColor(0xFFAAAAAA);
    canvas.drawColor(Color.TRANSPARENT);
    // 将前面已经画过得显示出来
    canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
    if (mPath != null) {
      // 实时的显示
      canvas.drawPath(mPath, mPaint);
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    float x = event.getX();
    float y = event.getY();

    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        // 每次down下去重新new一个Path
        mPath = new Path();
        // 每一次记录的路径对象是不一样的
        dp = new DrawPath();
        dp.path = mPath;
        dp.paint = mPaint;
        touch_start(x, y);
        invalidate();
        break;
      case MotionEvent.ACTION_MOVE:
        touch_move(x, y);
        invalidate();
        break;
      case MotionEvent.ACTION_UP:
        touch_up();
        invalidate();
        break;
    }
    return true;
  }

  private void touch_move(float x, float y) {
    float dx = Math.abs(x - mX);
    float dy = Math.abs(mY - y);
    if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
      // 从x1,y1到x2,y2画一条贝塞尔曲线，更平滑(直接用mPath.lineTo也是可以的)
      mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
      mX = x;
      mY = y;
    }
  }

  private void touch_start(float x, float y) {
    mPath.moveTo(x, y);
    mX = x;
    mY = y;
  }

  private void touch_up() {
    mPath.lineTo(mX, mY);
    mCanvas.drawPath(mPath, mPaint);
    // 将一条完整的路径保存下来(相当于入栈操作)
    savePath.add(dp);
    mPath = null;// 重新置空
  }
}
