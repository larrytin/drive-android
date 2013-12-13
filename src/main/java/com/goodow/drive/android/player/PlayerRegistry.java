package com.goodow.drive.android.player;

import com.goodow.drive.android.BusProvider;
import com.goodow.realtime.channel.EventBus;
import com.goodow.realtime.channel.EventHandler;
import com.goodow.realtime.json.JsonObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PlayerRegistry {
  static final String PREFIX = BusProvider.SID + "player.";
  private static final String TAG = PlayerRegistry.class.getSimpleName();
  private final EventBus eb = BusProvider.get();
  private final Context mContext;

  public PlayerRegistry(Context mContext) {
    this.mContext = mContext;
  }

  public void subscribe() {
    eb.registerHandler(PREFIX + "pdf", new EventHandler<JsonObject>() {
      @Override
      public void handler(JsonObject message, EventHandler<JsonObject> reply) {
        Intent intent = new Intent(mContext, SamplePDF.class);
        intent.putExtra("msg", message);
        mContext.startActivity(intent);
      }
    });
    eb.registerHandler(PREFIX + "mp4", new EventHandler<JsonObject>() {
      @Override
      public void handler(JsonObject message, EventHandler<JsonObject> reply) {
        Intent intent = new Intent(mContext, SampleVideo.class);
        intent.putExtra("msg", message);
        mContext.startActivity(intent);
      }
    });
    eb.registerHandler(PREFIX + "swf", new EventHandler<JsonObject>() {
      @Override
      public void handler(JsonObject message, EventHandler<JsonObject> reply) {
        Intent intent = new Intent(mContext, FlashPlayerActivity.class);
        intent.putExtra("msg", message);
        mContext.startActivity(intent);
      }
    });
    eb.registerHandler(PREFIX + "jpg", new EventHandler<JsonObject>() {
      @Override
      public void handler(JsonObject message, EventHandler<JsonObject> reply) {
        Intent intent = new Intent(mContext, PicturePlayAcivity.class);
        intent.putExtra("msg", message);
        mContext.startActivity(intent);
      }
    });
    eb.registerHandler(PREFIX + "mp3", new EventHandler<JsonObject>() {
      @Override
      public void handler(JsonObject message, EventHandler<JsonObject> reply) {
        Intent intent = new Intent(mContext, AudioPlayActivity.class);
        intent.putExtra("msg", message);
        mContext.startActivity(intent);
      }
    });
    eb.registerHandler(PREFIX + "control", new EventHandler<JsonObject>() {
      @Override
      public void handler(JsonObject message, EventHandler<JsonObject> reply) {
        if (message.has("back")) {
          // ActivityManager mActivityManager =
          // (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
          // String activityName =
          // mActivityManager.getRunningTasks(1).get(0).topActivity.getClassName();
          // 发送广播
          Intent mIntent = new Intent();
          mIntent.setAction("com.goodow.drive.android.activity.finish");
          mContext.sendBroadcast(mIntent);
          Log.i(TAG, "control finsh");
        }
      }
    });
  }
}
