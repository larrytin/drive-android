package com.goodow.drive.android.adapter;

import java.util.ArrayList;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

/**
 * 公用的翻页适配器
 * 
 * @author DPW
 * 
 */
public class CommonPageAdapter extends PagerAdapter {
  private ArrayList<View> tempView = null;

  public CommonPageAdapter(ArrayList<View> tempView) {
    this.tempView = tempView;
  }

  @Override
  public void destroyItem(ViewGroup container, int position, Object object) {
    if (position >= this.tempView.size()) {
      return;
    }
    ((ViewPager) container).removeView(this.tempView.get(position));
  }

  @Override
  public int getCount() {
    if (this.tempView == null) {
      return 0;
    } else {
      return this.tempView.size();
    }
  }

  @Override
  public Object instantiateItem(ViewGroup container, int position) {
    View view = this.tempView.get(position);
    container.addView(view);
    return view;
  }

  @Override
  public boolean isViewFromObject(View arg0, Object arg1) {
    return arg0 == arg1;
  }
}
