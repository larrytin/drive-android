package com.goodow.drive.android.data;

import com.goodow.drive.android.BusProvider;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

public class DataRegistry {

  public static final String PREFIX = BusProvider.SID + "category";
  private final Bus bus = BusProvider.get();

  public void subscribe() {
    bus.registerHandler(PREFIX, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();

        if (!"get".equalsIgnoreCase(body.getString("action"))) {
          return;
        }
        JsonObject domain = body.getObject("category");
        if (domain != null) {
          // String tempGrade = domain.getString(HarmonyActivity.SHAREDNAME_GRADE);
          // String tempTerm = domain.getString(HarmonyActivity.SHAREDNAME_TERM);
          // String tempClass = domain.getString(HarmonyActivity.SHAREDNAME_CLASS);
          // 解除以上三个条件的封印，根据条件进行查询
        }
        JsonObject msg = Json.createObject();
        JsonArray subjects = Json.createArray();
        for (int i = 0; i < 30; i++) {
          JsonObject value = Json.createObject();
          value.set("name", "index=" + i);
          subjects.insert(i, value);
        }
        msg.set("subjects", subjects);
        message.reply(msg);
      }
    });
  }

}
