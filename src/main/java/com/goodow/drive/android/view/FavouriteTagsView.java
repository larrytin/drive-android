package com.goodow.drive.android.view;

import com.goodow.android.drive.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FavouriteTagsView extends LinearLayout {

  private ImageView iv_favourite_tags;
  private FrameLayout iv_favourite_tags_del;
  private TextView tv_favourite_tags;
  private boolean isDeleteState;

  public FavouriteTagsView(Context context) {
    super(context);
    initView(context);
  }

  public FavouriteTagsView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  public FavouriteTagsView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initView(context);
  }

  public FrameLayout getDelButton() {
    return iv_favourite_tags_del;
  }

  public boolean isDeleteState() {
    return isDeleteState;
  }

  public void setDeleteSate(boolean selected) {
    super.setSelected(selected);
    if (selected) {
      iv_favourite_tags.setBackgroundResource(R.drawable.favour_item_del_bg);
      iv_favourite_tags_del.setVisibility(View.VISIBLE);
      isDeleteState = true;
    } else {
      iv_favourite_tags.setBackgroundResource(R.drawable.favour_item_bg);
      iv_favourite_tags_del.setVisibility(View.INVISIBLE);
      isDeleteState = false;
    }
  }

  public void setText(CharSequence text) {
    tv_favourite_tags.setText(text);
  }

  private void initView(Context context) {
    View.inflate(context, R.layout.result_favourite_tags, this);
    iv_favourite_tags = (ImageView) findViewById(R.id.iv_favourite_tags);
    iv_favourite_tags_del = (FrameLayout) findViewById(R.id.iv_favourite_tags_del);
    tv_favourite_tags = (TextView) findViewById(R.id.tv_favourite_tags);
  }

}
