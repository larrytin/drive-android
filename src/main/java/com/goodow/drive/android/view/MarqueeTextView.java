package com.goodow.drive.android.view;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.ViewDebug.ExportedProperty;
import android.widget.TextView;

public class MarqueeTextView extends TextView {

  public MarqueeTextView(Context context) {
    super(context);
    initView();
  }

  public MarqueeTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initView();
  }

  public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initView();
  }

  @Override
  @ExportedProperty(category = "focus")
  public boolean isFocused() {
    // 跑马灯的textview需要focus,所以一直返回true
    return true;
  }

  private void initView() {
    this.setMarqueeRepeatLimit(-1);
    this.setSingleLine();
    this.setEllipsize(TruncateAt.MARQUEE);
  }
}
