package com.goodow.drive.android.toolutils;

import com.goodow.drive.android.Constant;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

public class FontUtil {
  private static Context context;
  private static FontUtil instance = new FontUtil();
  private static Typeface typeface;

  public static FontUtil getInstance(Context context) {
    FontUtil.context = context;
    return instance;
  }

  public Typeface getTypeFace() {
    if (typeface == null) {
      Log.d("System.out", "new typeface");
      typeface = Typeface.createFromAsset(context.getAssets(), Constant.FONT_PATH);
    }
    return typeface;
  }

}
