package com.goodow.drive.android.view;

import com.goodow.drive.android.toolutils.FontUtil;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class FontTextView extends TextView {

  public FontTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    Typeface typeFace = FontUtil.getInstance(getContext()).getTypeFace();
    this.setTypeface(typeFace);
  }

}
