package com.goodow.drive.android;

import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.channel.impl.WebSocketBusClient;
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

  static int SUCCESS_RELATION = 0;
  static int SUCCESS_FILE = 0;

  public static void main(String[] args) throws IOException {
    final Bus bus =
        new WebSocketBusClient("ws://data.goodow.com:8080/eventbus/websocket", Json.createObject()
            .set("forkLocal", true));

    bus.registerHandler(Bus.LOCAL_ON_OPEN, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        handlerEventBusOpened(bus);
      }
    });
    bus.registerHandler(Bus.LOCAL_ON_CLOSE, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        log.info("EventBus closed");
        System.exit(0);
      }
    });
    bus.registerHandler(Bus.LOCAL_ON_ERROR, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        log.log(Level.SEVERE, "EventBus Error");
      }
    });

    // Prevent the JVM from exiting
    System.in.read();

  }

  private static void file(final Bus bus, final JsonObject tag) {
    bus.send(Constant.ADDR_TAG_ATTACHMENT, tag, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        if (message.body().has(Constant.KEY_STATUS)
            && "ok".equals(message.body().getString(Constant.KEY_STATUS))) {
          SUCCESS_FILE++;
          System.out.println("当前插入数据量(file)：" + SUCCESS_FILE + "/"
              + InitData.FILE_TABLE_DATA.length());
          if (SUCCESS_FILE < InitData.FILE_TABLE_DATA.length()) {
            file(bus, Json.createObject().set(Constant.KEY_ACTION, "post").set(
                Constant.KEY_ATTACHMENT, InitData.FILE_TABLE_DATA.getObject(SUCCESS_FILE)));
          }
        } else {
          System.out.println("\r\n 插入测试数据" + tag.toString() + "失败");
        }
      }
    });
  }

  private static void handlerEventBusOpened(final Bus bus) {
    bus.send(Constant.ADDR_DB_CLEAN, Json.createObject(), new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        if (message.body().has(Constant.KEY_STATUS)
            && "ok".equals(message.body().getString(Constant.KEY_STATUS))) {
          System.out.println("数据库数据清除成功，准备插入数据");
          final int len_relation_data = InitData.RELATION_TABLE_DATA.length();
          final int len_file_data = InitData.FILE_TABLE_DATA.length();
          System.out.println("\r\n 准备测试数据数量共：" + (len_relation_data + len_file_data));
          JsonObject relation =
              Json.createObject().set(Constant.KEY_ACTION, "post").set(Constant.KEY_TAG,
                  InitData.RELATION_TABLE_DATA.getObject(0));
          relation(bus, relation);

          JsonObject attachment =
              Json.createObject().set(Constant.KEY_ACTION, "post").set(Constant.KEY_ATTACHMENT,
                  InitData.FILE_TABLE_DATA.getObject(0));
          file(bus, attachment);
        } else {
          System.out.println("数据库数据清除失败，拒绝后续数据插入");
        }
      }
    });
  }

  private static void relation(final Bus bus, final JsonObject tag) {
    bus.send(Constant.ADDR_TAG, tag, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        if (message.body().has(Constant.KEY_STATUS)
            && "ok".equals(message.body().getString(Constant.KEY_STATUS))) {
          SUCCESS_RELATION++;
          System.out.println("当前插入数据量(relation)：" + SUCCESS_RELATION + "/"
              + InitData.RELATION_TABLE_DATA.length());
          if (SUCCESS_RELATION < InitData.RELATION_TABLE_DATA.length()) {
            relation(bus, Json.createObject().set(Constant.KEY_ACTION, "post").set(
                Constant.KEY_TAG, InitData.RELATION_TABLE_DATA.getObject(SUCCESS_RELATION)));
          }
        } else {
          System.out.println("\r\n 插入测试数据" + tag.toString() + "失败");
        }
      }
    });
  }
}