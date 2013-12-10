package com.goodow.drive.android.player;

import com.goodow.realtime.channel.EventBus;
import com.goodow.realtime.channel.EventHandler;
import com.goodow.realtime.json.JsonObject;

import android.content.Context;
import android.content.Intent;

public class PlayerRegistry {
  private EventBus mEventBus;
  private String mSid;
  private Context mContext;

  public PlayerRegistry(EventBus mEventBus, String mSid, Context mContext) {
    this.mEventBus = mEventBus;
    this.mSid = mSid;
    this.mContext = mContext;
  }

  public void handlerEventBus() {
    mEventBus.registerHandler(mSid + "drive.player.pdf", new EventHandler<JsonObject>() {
      @Override
      public void handler(JsonObject message, EventHandler<JsonObject> reply) {
        Intent intent = new Intent(mContext, SamplePDF.class);
        intent.putExtra("msg", message);
        mContext.startActivity(intent);
      }
    });
    mEventBus.registerHandler(mSid + "drive.player.mp4", new EventHandler<JsonObject>() {
      @Override
      public void handler(JsonObject message, EventHandler<JsonObject> reply) {
        Intent intent = new Intent(mContext, SampleVideo.class);
        intent.putExtra("msg", message);
        mContext.startActivity(intent);
      }
    });
    mEventBus.registerHandler(mSid + "drive.player.swf", new EventHandler<JsonObject>() {
      @Override
      public void handler(JsonObject message, EventHandler<JsonObject> reply) {
        Intent intent = new Intent(mContext, FlashPlayerActivity.class);
        intent.putExtra("msg", message);
        mContext.startActivity(intent);
      }
    });
    mEventBus.registerHandler(mSid + "drive.player.jpg", new EventHandler<JsonObject>() {
      @Override
      public void handler(JsonObject message, EventHandler<JsonObject> reply) {
        Intent intent = new Intent(mContext, PicturePlayAcivity.class);
        intent.putExtra("msg", message);
        mContext.startActivity(intent);
      }
    });
    mEventBus.registerHandler(mSid + "drive.player.mp3", new EventHandler<JsonObject>() {
      @Override
      public void handler(JsonObject message, EventHandler<JsonObject> reply) {
        Intent intent = new Intent(mContext, AudioPlayActivity.class);
        intent.putExtra("msg", message);
        mContext.startActivity(intent);
      }
    });
  }
}
