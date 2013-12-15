package com.goodow.drive.android;

import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.channel.impl.BusClient;
import com.goodow.realtime.channel.impl.BusClientHandler;
import com.goodow.realtime.java.JavaPlatform;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import java.io.IOException;
import java.util.logging.Logger;

public class DataSource {
  private static final Logger log = Logger.getLogger(DataSource.class.getName());

  static {
    JavaPlatform.register();
  }

  public static void main(String[] args) throws IOException {
    final BusClient eb = new BusClient("ws://data.goodow.com:8080/eventbus/websocket", null);

    eb.setListener(new BusClientHandler() {
      @Override
      public void onClose() {
        log.info("BusClient closed");
      }

      @Override
      public void onOpen() {
        registerHandlers(eb);
        JsonObject msg = Json.createObject();
        msg.set("path", "sample.pdf");
        eb.send("dan.pdf", msg, null);

        JsonObject request = Json.createObject();
        request.set("grade", "小班");
        request.set("term", "上学期");
        request.set("domain", "健康");
        request.set("subject", "找朋友");
        eb.send("dan.getFiles", request, new MessageHandler<JsonObject>() {

          @Override
          public void handle(Message<JsonObject> message) {
            assert message.replyAddress() == null;
          }
        });
      }
    });

    // Prevent the JVM from exiting
    System.in.read();
  }

  private static void registerHandlers(final BusClient eb) {
    eb.registerHandler("dan.getFiles", new MessageHandler<JsonObject>() {

      @Override
      public void handle(Message<JsonObject> message) {
        if (!"找朋友".equals(message.body().get("subject"))) {
          return;
        }
        assert message.replyAddress() != null;

        JsonObject msg = Json.createObject();
        JsonObject files = Json.createObject();
        msg.set("files", files);
        JsonArray pdfs = Json.createArray();
        files.set("pdf", pdfs);

        JsonObject pdf1 = Json.createObject();
        pdf1.set("filename", "找朋友 活动设计.pdf");
        pdf1.set("path", "/mnt/sdcard/xx/xx.pdf");
        pdfs.push(pdf1);

        message.reply(msg);
      }
    });
  }
}