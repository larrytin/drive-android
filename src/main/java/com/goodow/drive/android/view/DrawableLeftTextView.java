package com.goodow.drive.android.view;

import com.goodow.android.drive.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DrawableLeftTextView extends LinearLayout {

  private TextView mTextView;

  public DrawableLeftTextView(Context context) {
    super(context);
    initView(context);
  }

  public DrawableLeftTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  public DrawableLeftTextView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initView(context);
  }

  public CharSequence getText() {
    return mTextView.getText();
  }

  @Override
  public boolean isSelected() {
    return mTextView.isSelected();
  }

  public void setSelect(boolean selected) {
    mTextView.setSelected(selected);
  }

  public void setText(CharSequence str) {
    mTextView.setText(str);
  }

  private void initView(Context context) {
    View.inflate(context, R.layout.soruce_item_sub_tag, this);
    mTextView = (TextView) findViewById(R.id.tv_subtag);
  }

}
