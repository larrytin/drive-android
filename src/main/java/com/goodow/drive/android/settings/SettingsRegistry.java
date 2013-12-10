package com.goodow.drive.android.settings;

import com.goodow.realtime.channel.EventBus;
import com.goodow.realtime.channel.EventHandler;
import com.goodow.realtime.json.JsonObject;

import android.content.Context;

public class SettingsRegistry {
  private EventBus mEventBus;
  private String mSid;
  private Context mContext;

  public SettingsRegistry(EventBus mEventBus, String mSid, Context mContext) {
    super();
    this.mEventBus = mEventBus;
    this.mSid = mSid;
    this.mContext = mContext;
  }

  public void handlerEventBus() {
    mEventBus.registerHandler(mSid + "drive.settings.audio", new EventHandler<JsonObject>() {
      @Override
      public void handler(JsonObject message, EventHandler<JsonObject> reply) {

      }
    });
  }
}
