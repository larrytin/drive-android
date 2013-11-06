package com.goodow.drive.android.activity.play;

import com.goodow.android.drive.R;
import com.goodow.drive.android.global_data_cache.GlobalDataCacheForMemorySingleton;
import com.goodow.drive.android.toolutils.SimpleProgressDialog;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_picture_player)
public class PicturePlayAcivity extends RoboActivity {
  /**
   * 
   * 如果使用原图，可以用缓存到sd卡来处理，节省内存。 此处用的是缩略图
   * 
   */
  private class InitImageBitmapTask extends AsyncTask<String, Void, Bitmap> {
    @Override
    protected Bitmap doInBackground(String... params) {
      Bitmap bitmap = null;
      try {
        int width = PicturePlayAcivity.this.getResources().getDisplayMetrics().widthPixels;
        int height = PicturePlayAcivity.this.getResources().getDisplayMetrics().heightPixels;

        URLConnection connection =
            (new URL(params[0] + "=s" + ((width > height ? width : height) * 8) / 10)
                .openConnection());
        connection.setDoInput(true);
        connection.connect();
        InputStream bitmapStream = connection.getInputStream();
        try {
          bitmap = BitmapFactory.decodeStream(bitmapStream);
        } catch (OutOfMemoryError e) {
          e.printStackTrace();
        }

        bitmapStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }

      return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
      setImage(result);
    }
  }

  private Bitmap bmp;

  public static String PICTUREURL = "pictureUrl";

  public static String PICTUREPATH = "picturePaht";

  @InjectView(R.id.picture)
  private ImageView imageView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    GlobalDataCacheForMemorySingleton.getInstance.addActivity(this);

    Intent intent = getIntent();
    String picture = intent.getStringExtra(PICTUREURL);
    if (null != picture) {
      new InitImageBitmapTask().execute(picture);
    } else {
      picture = intent.getStringExtra(PICTUREPATH);
      setImage(picture);
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (bmp != null && !bmp.isRecycled()) {
      bmp.recycle();
    }
    System.gc();
    GlobalDataCacheForMemorySingleton.getInstance.removeActivity(this);
  }

  @Override
  protected void onPause() {
    super.onPause();
    SimpleProgressDialog.resetByThisContext(this);
  }

  @Override
  protected void onStop() {
    super.onStop();
  }

  /**
   * @param bitmap: 本地或者服务器上的图片资源,若为null,则加载load_picture_error图片. 1.用Matrix进行处理. 2.根据宽度的比例来进行缩放处理.
   */
  private void setImage(Bitmap bitmap) {
    if (null == bitmap) {
      bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.load_picture_error);
    }
    int bitmapWidth = bitmap.getWidth();
    int bitmapHeight = bitmap.getHeight();
    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
    int screenWidth = displayMetrics.widthPixels;
    float scale = 1;
    // 若图片宽度大于屏幕宽度,则按照比例缩放,否则就按原大小处理
    if (bitmapWidth > screenWidth) {
      scale = ((float) screenWidth) / bitmapWidth;
    }
    if (scale != 1) {
      Matrix matrix = new Matrix();
      matrix.setScale(scale, scale);

      bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
      if (!bitmap.isRecycled()) {
        bitmap.recycle();
      }
      System.gc();
    } else {
      bmp = bitmap;
    }
    imageView.setImageBitmap(bmp);
    ProgressBar progressBar = (ProgressBar) findViewById(R.id.pictureProgressBar);
    progressBar.setVisibility(View.GONE);
    imageView.setVisibility(View.VISIBLE);
  }

  // 缓存到sd卡
  private void setImage(String path) {
    BitmapFactory.Options opt = new BitmapFactory.Options();
    // 这个isjustdecodebounds很重要
    opt.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(path, opt);

    // 获取到这个图片的原始宽度和高度
    int picWidth = opt.outWidth;
    int picHeight = opt.outHeight;

    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
    int screenWidth = displayMetrics.widthPixels;
    int screenHeight = displayMetrics.heightPixels;

    // 若图片宽度大于屏幕宽度,则按照比例缩放,否则就按原大小处理
    if (picWidth > picHeight) {
      if (picWidth > screenWidth) {
        opt.inSampleSize = picWidth / screenWidth;
      }
    } else {
      if (picHeight > screenHeight) {
        opt.inSampleSize = picHeight / screenHeight;
      }
    }

    // 这次再真正地生成一个有像素的，经过缩放了的bitmap
    opt.inJustDecodeBounds = false;
    try {
      bmp = BitmapFactory.decodeFile(path, opt);
    } catch (OutOfMemoryError e) {
      e.printStackTrace();
      // 内存溢出，显示默认图片
      bmp = BitmapFactory.decodeResource(getResources(), R.drawable.load_picture_error);
    }

    imageView.setImageBitmap(bmp);
    ProgressBar progressBar = (ProgressBar) findViewById(R.id.pictureProgressBar);
    progressBar.setVisibility(View.GONE);
    imageView.setVisibility(View.VISIBLE);
  }

}
