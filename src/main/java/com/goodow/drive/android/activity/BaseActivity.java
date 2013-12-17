package com.goodow.drive.android.activity;

import com.goodow.drive.android.BusProvider;
import com.goodow.drive.android.player.PlayerRegistry;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.JsonObject;

import android.app.Activity;

/**
 * @title: BaseActivity.java
 * @package drive-android
 * @description: TODO
 * @author www.dingpengwei@gmail.com
 * @createDate 2013 2013-12-4 上午11:33:07
 * @updateDate 2013 2013-12-4 上午11:33:07
 * @version V1.0
 */
public class BaseActivity extends Activity {
  public static final String CONTROL = BusProvider.SID + "control";
  protected final Bus bus = BusProvider.get();

  private final MessageHandler<JsonObject> eventHandler = new MessageHandler<JsonObject>() {

    @Override
    public void handle(Message<JsonObject> message) {
      JsonObject msg = message.body();
      if (msg.has("back")) {
        finish();
      }
    }
  };

  @Override
  protected void onPause() {
    super.onPause();
    // Always unregister when an handler no longer should be on the bus.
    bus.unregisterHandler(CONTROL, eventHandler);
  }

  @Override
  protected void onResume() {
    super.onResume();
    // Register handlers so that we can receive event messages.
    bus.registerHandler(CONTROL, eventHandler);
  }
}
