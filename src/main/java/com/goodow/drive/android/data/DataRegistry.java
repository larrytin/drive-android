package com.goodow.drive.android.data;

import com.goodow.drive.android.BusProvider;
import com.goodow.drive.android.Constant;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import android.content.Context;
import android.widget.Toast;

public class DataRegistry {

  private final Bus bus = BusProvider.get();
  private Context context = null;

  public DataRegistry(Context context) {
    this.context = context;
  }

  public void subscribe() {
    bus.registerHandler(Constant.ADDR_TOPIC, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();

        // 仅处理get动作
        if (!"get".equalsIgnoreCase(body.getString("action"))) {
          return;
        }
        // 解析查询条件
        JsonObject query = body.getObject("query");
        String type = query.getString(Constant.TYPE);// 解析请求的模块类型
        if ("和谐".equals(type)) {
          // 和谐

        } else if ("托班".equals(type)) {
          // 托班

        } else if ("示范课".equals(type)) {
          // 示范课

        } else if ("入学准备".equals(type)) {
          // 入学准备

        } else if ("智能开发".equals(type)) {
          // 智能开发

        } else if ("电子书".equals(type)) {
          // 图画书

        } else if ("收藏".equals(type)) {
          // 收藏
          JsonObject msg = Json.createObject();
          JsonArray activitys = Json.createArray();
          for (int i = 0; i < 30; i++) {
            JsonObject activity = Json.createObject();
            JsonObject tag = Json.createObject();
            tag.set(Constant.GRADE, "大班");
            tag.set(Constant.TERM, "上");
            tag.set(Constant.TOPIC, "健康");
            activity.set(Constant.TAGS, tag);
            activity.set(Constant.TITLE, "找朋友" + i);
            activitys.insert(i, activity);
          }
          msg.set("activities", activitys);
          message.reply(msg);
        } else {
          Toast.makeText(context, "不支持" + type, Toast.LENGTH_SHORT).show();
          return;
        }
      }
    });
  }

}
