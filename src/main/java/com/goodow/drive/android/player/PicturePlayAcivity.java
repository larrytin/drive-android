package com.goodow.drive.android.player;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.activity.BaseActivity;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class PicturePlayAcivity extends BaseActivity {
  private final static String TAG = PicturePlayAcivity.class.getSimpleName();
  private ImageView mImageView;
  private Bitmap mBitmap;
  private ProgressBar mProgressBar;
  private ImageView mBackImageView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.i(TAG, "onCreate");
    this.setContentView(R.layout.activity_picture);
    mBackImageView = (ImageView) this.findViewById(R.id.iv_act_favour_back);
    mBackImageView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("return", true), null);
      }
    });
    mImageView = (ImageView) this.findViewById(R.id.actvity_picture);
    mProgressBar = (ProgressBar) findViewById(R.id.activity_pictureProgressBar);
    JsonObject jsonObject = (JsonObject) getIntent().getExtras().getSerializable("msg");
    String path = Constant.STORAGE_DIR + jsonObject.getString("path");
    String bitmapconfig = jsonObject.getString("bitmapconfig");
    Log.i(TAG, bitmapconfig + "");
    setImage(path, bitmapconfig);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mBitmap != null && !mBitmap.isRecycled()) {
      mBitmap.recycle();
    }
    System.gc();
    Log.i(TAG, "onDestroy");
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    Log.i(TAG, "onNewIntent");
    JsonObject jsonObject = (JsonObject) intent.getExtras().getSerializable("msg");
    String path = Constant.STORAGE_DIR + jsonObject.getString("path");
    String bitmapconfig = jsonObject.getString("bitmapconfig");
    Log.i(TAG, "bitmapconfig:" + bitmapconfig + ":onNewIntent");
    setImage(path, bitmapconfig);
  }

  // 读取sd卡里面的图片
  private void setImage(String path, String bitmapconfig) {
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
    if (bitmapconfig != null) {
      opt.inPreferredConfig = Bitmap.Config.valueOf(bitmapconfig);
      Log.i(TAG, "opt.inPreferredConfig:" + opt.inPreferredConfig + ":setImage");
    }
    // 这次再真正地生成一个有像素的，经过缩放了的bitmap
    opt.inJustDecodeBounds = false;
    Log.i(TAG, "opt.inPreferredConfig:" + opt.inPreferredConfig + ":setImage");
    // 防止内存溢出
    try {
      mBitmap = BitmapFactory.decodeFile(path, opt);
    } catch (OutOfMemoryError e) {
      e.printStackTrace();
    }

    mImageView.setImageBitmap(mBitmap);
    mProgressBar.setVisibility(View.GONE);
    mImageView.setVisibility(View.VISIBLE);
  }
}
