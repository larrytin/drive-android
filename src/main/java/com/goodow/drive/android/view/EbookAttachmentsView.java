package com.goodow.drive.android.view;

import com.goodow.android.drive.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import roboguice.inject.InjectView;

public class EbookAttachmentsView extends RelativeLayout {
  @InjectView(R.id.tv_ebook_text)
  private TextView tv_ebook_text;
  @InjectView(R.id.iv_ebook_image)
  private ImageView iv_ebook_image;

  public EbookAttachmentsView(Context context) {
    super(context);
    initVIew(context);
  }

  public EbookAttachmentsView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initVIew(context);
  }

  public EbookAttachmentsView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initVIew(context);
  }

  public ImageView getImageView() {
    return iv_ebook_image;
  }

  public void setImage(int id) {
    iv_ebook_image.setImageResource(id);
  }

  public void setText(CharSequence text) {
    tv_ebook_text.setText(text);
  }

  private void initVIew(Context context) {
    View.inflate(context, R.layout.result_ebook, this);
  }
}
