package com.goodow.drive.android;

import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.channel.impl.WebSocketBus;
import com.goodow.realtime.java.JavaPlatform;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonObject;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataSource {
  private static final Logger log = Logger.getLogger(DataSource.class.getName());

  static {
    JavaPlatform.register();
  }

  public static void main(String[] args) throws IOException {
    final Bus bus =
        new WebSocketBus("ws://data.goodow.com:8080/eventbus/websocket", Json.createObject().set(
            "forkLocal", true));

    bus.registerLocalHandler(Bus.ON_OPEN, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        handlerEventBusOpened(bus);
      }
    });
    bus.registerLocalHandler(Bus.ON_CLOSE, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        log.info("EventBus closed");
        System.exit(0);
      }
    });
    bus.registerLocalHandler(Bus.ON_ERROR, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        log.log(Level.SEVERE, "EventBus Error");
      }
    });

    // Prevent the JVM from exiting
    System.in.read();
  }

  private static void handlerEventBusOpened(final Bus bus) {

  }

}