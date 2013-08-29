package com.goodow.drive.android.activity.play;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import android.content.DialogInterface;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.goodow.android.drive.R;
import com.goodow.drive.android.toolutils.SimpleProgressDialog;

@ContentView(R.layout.activity_picture_player)
public class PicturePlayAcivity extends RoboActivity {

  public class ImageDownloadTask extends AsyncTask<Object, Object, Bitmap> {
    private ImageView imageView = null;
    private int _displaywidth = 480;
    private int _displayheight = 800;
    private int _displaypixels = _displaywidth * _displayheight;

    private DialogInterface.OnCancelListener progressDialogOnCancelListener = new DialogInterface.OnCancelListener() {

      @Override
      public void onCancel(DialogInterface dialog) {
        // TODO Auto-generated method stub

      }
    };

    /**
     * 通过URL获得网上图片。如:http://www.xxxxxx.com/xx.jpg
     * */
    public Bitmap getBitmap(String url, int displaypixels, Boolean isBig) throws MalformedURLException, IOException {
      Bitmap bmp = null;
      BitmapFactory.Options opts = new BitmapFactory.Options();

      InputStream stream = new URL(url).openStream();
      byte[] bytes = getBytes(stream);
      // 这3句是处理图片溢出的begin( 如果不需要处理溢出直接 opts.inSampleSize=1;)
      opts.inJustDecodeBounds = true;
      BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
      opts.inSampleSize = computeSampleSize(opts, -1, displaypixels);
      // end
      opts.inJustDecodeBounds = false;
      bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
      return bmp;
    }

    public int getDisplayHeight() {
      return _displayheight;
    }

    public int getDisplayPixels() {
      return _displaypixels;
    }

    public int getDisplayWidth() {
      return _displaywidth;
    }

    public void setDisplayHeight(int height) {
      _displayheight = height;
    }

    /***
     * 这里获取到手机的分辨率大小
     * */
    public void setDisplayWidth(int width) {
      _displaywidth = width;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#onPreExecute()
     */

    @Override
    protected Bitmap doInBackground(Object... params) {
      Bitmap bmp = null;
      imageView = (ImageView) params[1];
      try {
        String url = (String) params[0];
        bmp = getBitmap(url, _displaypixels, true);
      } catch (Exception e) {
        return null;
      }
      return bmp;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
      SimpleProgressDialog.dismiss(PicturePlayAcivity.this);
      if (imageView != null && result != null) {
        imageView.setImageBitmap(result);
        if (null != result && result.isRecycled() == false) {
          System.gc();
        }
      }
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      SimpleProgressDialog.show(PicturePlayAcivity.this, progressDialogOnCancelListener);
    }

    private int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
      double w = options.outWidth;
      double h = options.outHeight;
      int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
      int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));
      if (upperBound < lowerBound) {
        return lowerBound;
      }
      if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
        return 1;
      } else if (minSideLength == -1) {
        return lowerBound;
      } else {
        return upperBound;
      }
    }

    /****
     * 处理图片bitmap size exceeds VM budget （Out Of Memory 内存溢出）
     */
    private int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
      int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
      int roundedSize;
      if (initialSize <= 8) {
        roundedSize = 1;
        while (roundedSize < initialSize) {
          roundedSize <<= 1;
        }
      } else {
        roundedSize = (initialSize + 7) / 8 * 8;
      }
      return roundedSize;
    }

    /**
     * 数据流转成btyle[]数组
     * */
    private byte[] getBytes(InputStream is) {

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] b = new byte[2048];
      int len = 0;
      try {
        while ((len = is.read(b, 0, 2048)) != -1) {
          baos.write(b, 0, len);
          baos.flush();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      byte[] bytes = baos.toByteArray();
      return bytes;
    }
  }

  private class InitImageBitmapTask extends AsyncTask<String, Void, Bitmap> {
    @Override
    protected Bitmap doInBackground(String... params) {
      Bitmap bitmap = null;

      try {
        int width = PicturePlayAcivity.this.getResources().getDisplayMetrics().widthPixels;
        int height = PicturePlayAcivity.this.getResources().getDisplayMetrics().heightPixels;

        URLConnection connection = (new URL(params[0] + "=s" + ((width > height ? width : height) * 8) / 10).openConnection());
        connection.setDoInput(true);
        connection.connect();
        InputStream bitmapStream = connection.getInputStream();
        bitmap = BitmapFactory.decodeStream(bitmapStream);
        bitmapStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }

      return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
      super.onPostExecute(result);
      ProgressBar progressBar = (ProgressBar) findViewById(R.id.pictureProgressBar);
      progressBar.setVisibility(View.GONE);
      imageView.setImageBitmap(result);
      imageView.setVisibility(View.VISIBLE);
    }
  }

  public static String PICTUREURL = "pictureUrl";

  @InjectView(R.id.picture)
  private ImageView imageView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = getIntent();
    String pictureUrl = intent.getStringExtra(PICTUREURL);

    if (null != pictureUrl) {
      new InitImageBitmapTask().execute(pictureUrl);
      // ImageDownloadTask imgtask = new ImageDownloadTask();
      // /** 这里是获取手机屏幕的分辨率用来处理 图片 溢出问题的。begin */
      // DisplayMetrics dm = new DisplayMetrics();
      // getWindowManager().getDefaultDisplay().getMetrics(dm);
      // imgtask.setDisplayWidth(dm.widthPixels);
      // imgtask.setDisplayHeight(dm.heightPixels);
      // imgtask.execute(pictureUrl, imageView);
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    SimpleProgressDialog.resetByThisContext(this);
  }
}
