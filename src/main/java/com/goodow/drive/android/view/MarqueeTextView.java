package com.goodow.drive.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewDebug.ExportedProperty;
import android.widget.TextView;

public class MarqueeTextView extends TextView {

  public MarqueeTextView(Context context) {
    super(context);
  }

  public MarqueeTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  @ExportedProperty(category = "focus")
  public boolean isFocused() {
    // 跑马灯的textview需要focus,所以一直返回true
    return true;
  }
}
