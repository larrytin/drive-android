package com.goodow.drive.android.view;

import com.goodow.android.drive.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FavouriteAttachmentsView extends RelativeLayout {

  private TextView tv_favourite_attachments;
  private ImageView iv_favourite_attachments_del;
  private ImageView iv_favourite_attachments;
  private boolean isDeleteState;

  public FavouriteAttachmentsView(Context context) {
    super(context);
    initVIew(context);
  }

  public FavouriteAttachmentsView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initVIew(context);
  }

  public FavouriteAttachmentsView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initVIew(context);
  }

  public ImageView getDelButton() {
    return iv_favourite_attachments_del;
  }

  public ImageView getImageView() {
    return iv_favourite_attachments;
  }

  public boolean isDeleteState() {
    return isDeleteState;
  }

  public void setDeleteSate(boolean bool) {
    if (bool) {
      iv_favourite_attachments_del.setVisibility(View.VISIBLE);
      isDeleteState = true;
    } else {
      iv_favourite_attachments_del.setVisibility(View.INVISIBLE);
      isDeleteState = false;
    }
  }

  public void setText(CharSequence text) {
    tv_favourite_attachments.setText(text);
  }

  private void initVIew(Context context) {
    View.inflate(context, R.layout.result_favourite_attachments, this);
    tv_favourite_attachments = (TextView) findViewById(R.id.tv_favourite_attachments);
    iv_favourite_attachments_del = (ImageView) findViewById(R.id.iv_favourite_attachments_del);
    iv_favourite_attachments = (ImageView) findViewById(R.id.iv_favourite_attachments);
  }
}
