package com.goodow.drive.android.toolutils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class FocusedTextView extends TextView {

  public FocusedTextView(Context context) {
    super(context);
  }

  public FocusedTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public FocusedTextView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  public boolean isFocused() {
    return true;
  }

}