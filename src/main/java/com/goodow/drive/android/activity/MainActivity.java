package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.player.PlayerRegistry;
import com.goodow.drive.android.settings.SettingsRegistry;
import com.goodow.realtime.android.AndroidPlatform;
import com.goodow.realtime.channel.EventBus;
import com.goodow.realtime.channel.EventBus.EventBusHandler;

import java.util.logging.Logger;

import android.os.Bundle;

public class MainActivity extends BaseActivity {
  static {
    AndroidPlatform.register();
  }
  private static final String SID = "dan.";
  private static final Logger log = Logger.getLogger(MainActivity.class.getName());
  private final EventBus eb = new EventBus("ws://data.goodow.com:8080/eventbus/websocket", null);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_main);

    eb.setListener(new EventBusHandler() {
      @Override
      public void onClose() {
        log.info("EventBus closed");
      }

      @Override
      public void onOpen() {
        handlerEventBusOpened();
      }
    });
  }

  private void handlerEventBusOpened() {
    new PlayerRegistry(eb, SID, MainActivity.this).handlerEventBus();
    new SettingsRegistry(eb, SID, MainActivity.this).handlerEventBus();
  }
}
