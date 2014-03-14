package com.goodow.drive.android.view;

import com.goodow.android.drive.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.SeekBar;

/**
 * 可控制thumb大小的seekbar
 * 
 * @author liudenghui
 * 
 */
public class ThumbSeekbar extends SeekBar {

  public ThumbSeekbar(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.ThumbSeekbar);
    int width = typeArray.getDimensionPixelOffset(R.styleable.ThumbSeekbar_thumb_width, 0);
    int height = typeArray.getDimensionPixelOffset(R.styleable.ThumbSeekbar_thumb_height, 0);
    typeArray.recycle();
    int resId =
        attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "thumb", 0);
    Drawable drawable = getResources().getDrawable(resId);
    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
    Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
    BitmapDrawable bitmapDrawable = new BitmapDrawable(newBitmap);
    this.setThumb(bitmapDrawable);
    this.setThumbOffset(0);
  }

}
