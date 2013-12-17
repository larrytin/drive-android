package com.goodow.drive.android.player;

import com.goodow.drive.android.BusProvider;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.JsonObject;

import android.content.Context;
import android.content.Intent;

public class PlayerRegistry {
  public static final String PREFIX = BusProvider.SID + "player.";
  private static final String TAG = PlayerRegistry.class.getSimpleName();
  private final Bus bus = BusProvider.get();
  private final Context mContext;

  public PlayerRegistry(Context mContext) {
    this.mContext = mContext;
  }

  public void subscribe() {
    bus.registerHandler(PREFIX + "pdf", new MessageHandler<JsonObject>() {
      @SuppressWarnings("static-access")
      @Override
      public void handle(Message<JsonObject> message) {
        bus.send(bus.LOCAL + PREFIX + "pdf" + ".mu", message.body(), null);
      }
    });

    bus.registerHandler(PREFIX + "pdf.jz", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        Intent intent = new Intent(mContext, PdfPlayer.class);
        intent.putExtra("msg", message.body());
        mContext.startActivity(intent);
      }
    });

    bus.registerHandler(PREFIX + "pdf.mu", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        Intent intent = new Intent(mContext, PdfMuPlayer.class);
        intent.putExtra("msg", message.body());
        mContext.startActivity(intent);
      }
    });

    bus.registerHandler(PREFIX + "mp4", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        Intent intent = new Intent(mContext, SampleVideo.class);
        intent.putExtra("msg", message.body());
        mContext.startActivity(intent);
      }
    });
    bus.registerHandler(PREFIX + "swf", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        Intent intent = new Intent(mContext, FlashPlayerActivity.class);
        intent.putExtra("msg", message.body());
        mContext.startActivity(intent);
      }
    });
    bus.registerHandler(PREFIX + "jpg", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        Intent intent = new Intent(mContext, PicturePlayAcivity.class);
        intent.putExtra("msg", message.body());
        mContext.startActivity(intent);
      }
    });
    bus.registerHandler(PREFIX + "mp3", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        Intent intent = new Intent(mContext, AudioPlayActivity.class);
        intent.putExtra("msg", message.body());
        mContext.startActivity(intent);
      }
    });
    // bus.registerHandler(PREFIX + "control", new MessageHandler<JsonObject>() {
    // @Override
    // public void handle(Message<JsonObject> message) {
    // if (message.body().has("back")) {
    // // ActivityManager mActivityManager =
    // // (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
    // // String activityName =
    // // mActivityManager.getRunningTasks(1).get(0).topActivity.getClassName();
    // // 发送广播
    // Intent mIntent = new Intent();
    // mIntent.setAction("com.goodow.drive.android.activity.finish");
    // mContext.sendBroadcast(mIntent);
    // Log.i(TAG, "control finsh");
    // }
    // }
    // });
  }
}
