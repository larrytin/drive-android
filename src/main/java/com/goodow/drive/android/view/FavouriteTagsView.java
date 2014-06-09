package com.goodow.drive.android.view;

import com.goodow.android.drive.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import roboguice.inject.InjectView;

public class FavouriteTagsView extends LinearLayout {
  @InjectView(R.id.iv_favourite_tags)
  private ImageView iv_favourite_tags;
  @InjectView(R.id.iv_favourite_tags_del)
  private FrameLayout iv_favourite_tags_del;
  @InjectView(R.id.tv_favourite_tags)
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
  }

}
