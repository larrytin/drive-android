package com.goodow.drive.android.view;

import com.goodow.drive.android.toolutils.FontUtil;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

public class FontEditText extends EditText {

  public FontEditText(Context context, AttributeSet attrs) {
    super(context, attrs);
    Typeface typeFace = FontUtil.getInstance(getContext()).getTypeFace();
    this.setTypeface(typeFace);
  }
}
