package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.realtime.android.AndroidPlatform;
import com.goodow.realtime.channel.EventBus;
import com.goodow.realtime.channel.EventBus.EventBusHandler;
import com.goodow.realtime.channel.EventHandler;
import com.goodow.realtime.json.JsonObject;

import java.util.logging.Logger;

import android.content.Intent;
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
    eb.registerHandler(SID + "pdf", new EventHandler<JsonObject>() {
      @Override
      public void handler(JsonObject message, EventHandler<JsonObject> reply) {
        Intent intent = new Intent(MainActivity.this, SamplePDF.class);
        intent.putExtra("path", message.getString("path"));
        startActivity(intent);
      }
    });
    eb.registerHandler(SID + "mp4", new EventHandler<JsonObject>() {
      @Override
      public void handler(JsonObject message, EventHandler<JsonObject> reply) {
        Intent intent = new Intent(MainActivity.this, SampleVideo.class);
        intent.putExtra("path", message.getString("path"));
        startActivity(intent);
      }
    });
  }
}
