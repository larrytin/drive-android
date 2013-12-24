package com.goodow.drive.android;

import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.channel.impl.SimpleBus;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import java.io.IOException;
import java.util.logging.Logger;

public class DataSource {
  private static final Logger log = Logger.getLogger(DataSource.class.getName());

  public static void main(String[] args) throws IOException {
    final Bus bus = new SimpleBus();

    registerHandlers(bus);

    JsonObject msg = Json.createObject();

    JsonObject request = Json.createObject();
    request.set("grade", "小班");
    request.set("term", "上学期");
    request.set("domain", "健康");
    request.set("subject", "找朋友");
    bus.send("sid.getFiles", request, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        assert message.replyAddress() == null;
      }
    });
  }

  private static void registerHandlers(final Bus bus) {
    bus.registerHandler("sid.getFiles", new MessageHandler<JsonObject>() {

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