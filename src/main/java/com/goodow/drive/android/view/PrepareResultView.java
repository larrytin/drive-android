package com.goodow.drive.android.view;

import com.goodow.android.drive.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PrepareResultView extends RelativeLayout {

  private ImageView iv_prepare_left_eye;
  private ImageView iv_prepare_right_eye;
  private TextView tv_prepare_tag;

  public PrepareResultView(Context context) {
    super(context);
    initView(context);
  }

  public PrepareResultView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  public PrepareResultView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initView(context);
  }

  /**
   * 设置蜗牛左眼是否可用
   * 
   * @param bool true左眼可用,睁眼,false不可用,闭眼
   */
  public void setLeftEyeEnable(boolean bool) {
    if (bool) {
      iv_prepare_left_eye.setClickable(true);
      iv_prepare_left_eye.setImageResource(R.drawable.prepare_result_word);
    } else {
      iv_prepare_left_eye.setClickable(false);
      iv_prepare_left_eye.setImageResource(R.drawable.prepare_result_lefteye);
    }
  }

  /**
   * 设置蜗牛左眼点击事件监听
   * 
   * @param l
   */
  public void setOnLeftEyeClickListener(OnClickListener l) {
    iv_prepare_left_eye.setOnClickListener(l);
  }

  /**
   * 设置蜗牛右眼点击事件监听
   * 
   * @param l
   */
  public void setOnRightEyeClickListener(OnClickListener l) {
    iv_prepare_right_eye.setOnClickListener(l);
  }

  /**
   * 设置蜗牛右眼是否可用
   * 
   * @param bool true右眼可用,睁眼,false不可用,闭眼
   */
  public void setRightEyeEnable(boolean bool) {
    if (bool) {
      iv_prepare_right_eye.setClickable(true);
      iv_prepare_right_eye.setImageResource(R.drawable.prepare_result_word);
    } else {
      iv_prepare_right_eye.setClickable(false);
      iv_prepare_right_eye.setImageResource(R.drawable.prepare_result_righteye);
    }
  }

  /**
   * 设置蜗牛身上显示的字
   * 
   * @param text
   */
  public void setText(CharSequence text) {
    tv_prepare_tag.setText(text);
  }

  private void initView(Context context) {
    View.inflate(context, R.layout.result_prepare, this);
    iv_prepare_left_eye = (ImageView) findViewById(R.id.iv_prepare_left_eye);
    iv_prepare_right_eye = (ImageView) findViewById(R.id.iv_prepare_right_eye);
    tv_prepare_tag = (TextView) findViewById(R.id.tv_prepare_tag);
  }

}
