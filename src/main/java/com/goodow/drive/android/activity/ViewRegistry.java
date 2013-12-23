package com.goodow.drive.android.activity;

import com.goodow.drive.android.BusProvider;
import com.goodow.realtime.channel.Bus;

import android.content.Context;

public class ViewRegistry {
  public static final String PREFIX = BusProvider.SID + "view.";
  private final Bus bus = BusProvider.get();
  private final Context ctx;

  public ViewRegistry(Context ctx) {
    this.ctx = ctx;
  }

  public void subscribe() {
  }
}