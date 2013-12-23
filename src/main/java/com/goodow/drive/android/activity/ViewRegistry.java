package com.goodow.drive.android.activity;

import com.goodow.drive.android.BusProvider;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.JsonObject;

import android.content.Context;
import android.content.Intent;

public class ViewRegistry {
  public static final String PREFIX = BusProvider.SID + "view.";
  private final Bus bus = BusProvider.get();
  private final Context ctx;

  public ViewRegistry(Context ctx) {
    this.ctx = ctx;
  }

  public void subscribe() {
    bus.registerHandler(PREFIX + "home", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        Intent intent = new Intent(ctx, HomeActivity.class);
        intent.putExtra("msg", message.body());
        ctx.startActivity(intent);
      }
    });

  }
}